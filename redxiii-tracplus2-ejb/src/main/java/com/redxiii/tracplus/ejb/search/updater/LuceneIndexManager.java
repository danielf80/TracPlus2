package com.redxiii.tracplus.ejb.search.updater;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.inject.Named;

import org.apache.commons.configuration.Configuration;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.search.LuceneResult;
import com.redxiii.tracplus.ejb.search.SearchManager;
import com.redxiii.tracplus.ejb.search.SearchResult;
import com.redxiii.tracplus.ejb.search.TracStuff;
import com.redxiii.tracplus.ejb.search.TracStuffField;
import com.redxiii.tracplus.ejb.search.query.SimpleQuerySpec;
import com.redxiii.tracplus.ejb.util.AppConfiguration;

@Named
@Singleton
@LocalBean
public class LuceneIndexManager implements Serializable, SearchManager {

	private static final long serialVersionUID = 1L;

	private static final Lock indexUpdaterLock = new ReentrantLock();
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private File path;
	private Version version;
	private Directory directory;
	private StandardAnalyzer analyzer;
	
	@PostConstruct
	public void init() throws IOException {
		Configuration configuration = AppConfiguration.getInstance();
		
		version = Version.valueOf(configuration.getString("lucene-index.version",Version.LUCENE_36.name()));
		path = new File( AppConfiguration.getRootConfigFolder() + configuration.getString("lucene-index.directory", "/wiki-lucene-index/"));
		
		if (path.exists()) {
			path.createNewFile();
		}
		
		this.directory = new SimpleFSDirectory(path);
		this.analyzer = new StandardAnalyzer(version);
	}
	
	private IndexWriter createIndexWriter() {
		try {
			return new IndexWriter(directory, new IndexWriterConfig(version, analyzer));
		} catch (CorruptIndexException e) {
			logger.error("Error updating Lucene index",e);
		} catch (LockObtainFailedException e) {
			logger.error("Error updating Lucene index",e);
		} catch (IOException e) {
			logger.error("Error updating Lucene index",e);
		}
		return null;
	}
	
	public void updateIndex(Collection<TracStuff> stuffs) {
		indexUpdaterLock.lock();
		IndexWriter writer = null;
		try {
			writer = createIndexWriter();
			
			for (TracStuff tracStuff : stuffs) {
				logger.info("Indexing: {} at {}", tracStuff.getId(), tracStuff.getCreatedDate());
				
				Document doc = new Document();
				
				doc.add(new Field(TracStuffField.ID.toString(), tracStuff.getContent(), Store.YES, Index.NO));
				doc.add(new Field(TracStuffField.CONTENT.toString(), tracStuff.getContent(), Store.NO, Index.ANALYZED));
				doc.add(new Field(TracStuffField.AUTHOR.toString(), tracStuff.getAuthor(), Store.YES, Index.ANALYZED));
				doc.add(new Field(TracStuffField.DESCRIPTION.toString(), tracStuff.getDescription(), Store.YES, Index.ANALYZED));
				doc.add(new Field(TracStuffField.CONTEXT.toString(), tracStuff.getContext(), Store.YES, Index.ANALYZED));
				doc.add(new Field(TracStuffField.URL.toString(), tracStuff.getUrl(), Store.YES, Index.NO));
				doc.add(new Field(TracStuffField.MODIFIED_DATE.toString(), tracStuff.getModifiedDate(), Store.YES, Index.NO));
				doc.add(new NumericField(TracStuffField.MODIFIED_TIMESTAMP.toString(), Store.NO, true).setLongValue(tracStuff.getModifiedTimestamp()));
				
				writer.updateDocument(new Term(TracStuffField.ID.toString(), tracStuff.getId()), doc);
			}
			
			writer.commit();
		} catch (Exception e) {
			logger.error("Fail to recreate index");
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {}
			}
			indexUpdaterLock.unlock();
		}
	}

	public void purgeAndRecreate() {
		indexUpdaterLock.lock();
		IndexWriter writer = null;
		try {
			writer = createIndexWriter();
			writer.deleteAll();
			writer.commit();
		} catch (Exception e) {
			logger.error("Fail to recreate index");
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {}
			}
			indexUpdaterLock.unlock();
		}
	}
	
	public Query buildQuery(SimpleQuerySpec spec) {
		BooleanQuery mainQuery = new BooleanQuery();
		
		Set<TracStuffField> fields = spec.getQueryFields();
		for (TracStuffField field : fields) {
			Set<String> values = spec.getValuesRestriction(field);
			for (String value : values) {
				
				if (value.length() < 3)
					continue;
				
				BooleanClause.Occur occur = BooleanClause.Occur.SHOULD;
				if (spec.isStrongRestriction(field, value))
					occur = Occur.MUST;
				
				
				Term term = new Term(field.name(), value);
				Query query = null;
				if (spec.isLikeRestriction(field, value))
					query = new FuzzyQuery(term, spec.getLikeRestrictionWeight(field, value));
				else
					query = new TermQuery(term);
				
				
				mainQuery.add(query, occur);
			}
		}
		
		if (spec.isRecentFilterEnable()) {
			DateTime start = new DateTime().withMillisOfDay(0).minusDays(spec.getRecentFilterDays());
			
			Query query = NumericRangeQuery.newLongRange(
					TracStuffField.MODIFIED_TIMESTAMP.name(), start.getMillis(), System.currentTimeMillis(), true, true);
			
			mainQuery.add(query, BooleanClause.Occur.MUST);
		}
		
		if (spec.getLuceneQuery() != null) {
			mainQuery.add(parseLuceneQuery(spec.getLuceneQuery()), Occur.SHOULD); 
		}
		
		return mainQuery;
	}

	private Query parseLuceneQuery(String lucene) {
		try {
			return new QueryParser(version, TracStuffField.CONTENT.name(), analyzer).parse(lucene);
		} catch (ParseException e) {
			logger.error("Error creating query", e);
		}
		return null;
	}
	
	public Set<SearchResult> doSearch(Query query) {

		logger.info("Query: '{}'", query);
		Set<SearchResult> results = new LinkedHashSet<SearchResult>();

		if (query == null)
			return results;

		IndexSearcher searcher = null;
		IndexReader reader = null;
		indexUpdaterLock.lock();
		try {
			reader = IndexReader.open(directory);
			
			searcher = new IndexSearcher(reader) ;
			TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
			searcher.search(query, collector);

			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for (ScoreDoc hit : hits) {
				Document doc = searcher.doc(hit.doc);
				results.add(new LuceneResult(doc, hit.score));
			}
		} catch (CorruptIndexException e) {
			logger.error("Error searching Lucene index", e);
		} catch (IOException e) {
			logger.error("Error searching Lucene index", e);
		} finally {
			if (searcher != null) {
				try {
					searcher.close();
				} catch (IOException e) {
					logger.error("Error closing Lucene index", e);
				}
			}
			if(reader != null) {
	            try {
	                reader.close();
	            } catch(IOException e) {
	                logger.error("Error closing Lucene index", e);
	            }
	        }
			indexUpdaterLock.unlock();	
		}

		return results;
	}

}
