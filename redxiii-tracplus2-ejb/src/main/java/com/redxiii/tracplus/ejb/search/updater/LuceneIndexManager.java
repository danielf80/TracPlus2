package com.redxiii.tracplus.ejb.search.updater;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ejb.AccessTimeout;
import javax.ejb.LockType;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.search.LuceneResult;
import com.redxiii.tracplus.ejb.search.SearchManager;
import com.redxiii.tracplus.ejb.search.SearchResult;
import com.redxiii.tracplus.ejb.search.TracStuff;
import com.redxiii.tracplus.ejb.search.TracStuffField;
import com.redxiii.tracplus.ejb.search.query.SimpleQuerySpec;

@Named
public class LuceneIndexManager implements Serializable, SearchManager {

    private static final long serialVersionUID = 1L;
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm:ss");
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private LuceneIndexFactory indexFactory;
    
    private FieldType createFieldType(boolean stored, boolean indexed) {
    	FieldType fieldType = new FieldType();
        fieldType.setStored(stored);
        fieldType.setIndexed(indexed);
        fieldType.freeze();
        
        return fieldType;
    }
    
    public void updateIndex(Collection<TracStuff> stuffs) {
        IndexWriter writer = null;
        try {
            writer = indexFactory.createIndexWriter();

            final FieldType fIndexedStored = createFieldType(true, true);
            final FieldType fIndexed = createFieldType(false, true);
            final FieldType fStored = createFieldType(true, false);
            
            for (TracStuff tracStuff : stuffs) {
                logger.info("Indexing: {} owned by {} modified at {}", new Object[]{
                        tracStuff.getId(), 
                        tracStuff.getAuthor(),
                        formatter.print(tracStuff.getModifiedTimestamp()) });

                Document doc = new Document();
                
                doc.add(new Field(TracStuffField.ID.toString(), tracStuff.getId(), fStored));
                
                // Indexed and Stored data
                doc.add(new Field(TracStuffField.DESCRIPTION.toString(), tracStuff.getDescription(), fIndexedStored));
                doc.add(new Field(TracStuffField.CONTEXT.toString(), tracStuff.getContext(), fIndexedStored));   // This is stored for display icon
                
                // Indexed data
                doc.add(new Field(TracStuffField.CONTENT.toString(), tracStuff.getContent(), fIndexed));
                doc.add(new LongField(TracStuffField.MODIFIED_TIMESTAMP.toString(), tracStuff.getModifiedTimestamp(), fIndexed));
                
                //Storage data
                doc.add(new Field(TracStuffField.AUTHOR.toString(), tracStuff.getAuthor(), fStored));
                doc.add(new Field(TracStuffField.URL.toString(), tracStuff.getUrl(), fStored));
                doc.add(new Field(TracStuffField.MODIFIED_DATE.toString(), tracStuff.getModifiedDate(), fStored));
                doc.add(new Field(TracStuffField.STATUS.toString(), tracStuff.getStatus(), fStored));
                doc.add(new Field(TracStuffField.CC.toString(), tracStuff.getCc(), fStored));
                

                writer.updateDocument(new Term(TracStuffField.ID.toString(), tracStuff.getId()), doc);
            }

            writer.commit();
        } catch (Exception e) {
            logger.error("Fail to recreate index", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @javax.ejb.Lock(LockType.WRITE)
    @AccessTimeout(30000)
    public void purgeAndRecreate() {
        IndexWriter writer = null;
        try {
            writer = indexFactory.createIndexWriter();
            writer.deleteAll();
            writer.commit();
        } catch (Exception e) {
            logger.error("Fail to recreate index");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @javax.ejb.Lock(LockType.READ)
    @Override
    public Query buildQuery(SimpleQuerySpec spec) {
        BooleanQuery mainQuery = new BooleanQuery();

        Set<TracStuffField> fields = spec.getQueryFields();
        for (TracStuffField field : fields) {
            Set<String> values = spec.getValuesRestriction(field);
            
            BooleanClause.Occur occur = BooleanClause.Occur.SHOULD;
            
            BooleanQuery fieldQuery;
            if (values.size() == 1) {
                fieldQuery = mainQuery;
                if (spec.isStrongRestriction(field, values.iterator().next())) {
                    occur = Occur.MUST;
                }
            } else {
                fieldQuery = new BooleanQuery();
                mainQuery.add(fieldQuery, Occur.MUST);
            }
            
            for (String value : values) {

                Query query = null;
                if (spec.isLikeRestriction(field, value)) {
//                    query = new FuzzyQuery(new Term(field.name(), value), spec.getLikeRestrictionWeight(field, value));
                	query = new FuzzyQuery(new Term(field.name(), value));
                } else {
                    if (value.contains(" ")) {
                        query = new PhraseQuery();
                        for (String part : value.split(" ")){
                            ((PhraseQuery)query).add(new Term(field.name(), part));
                        }
                    } else {
                        query = new TermQuery(new Term(field.name(), value));
                    }
                }

                fieldQuery.add(query, occur);
            }
        }

        if (spec.isRecentFilterEnable()) {
            DateTime start = new DateTime().withMillisOfDay(0).minusDays(spec.getRecentFilterDays());

            Query query = NumericRangeQuery.newLongRange(
                    TracStuffField.MODIFIED_TIMESTAMP.name(), start.getMillis(), System.currentTimeMillis(), true, true);

            mainQuery.add(query, BooleanClause.Occur.MUST);
        }

        if (spec.getLuceneQuery() != null) {
            mainQuery.add(indexFactory.createQuery(TracStuffField.CONTENT.name(), spec.getLuceneQuery()), Occur.SHOULD);
        }

        return mainQuery;
    }

    @javax.ejb.Lock(LockType.READ)
    @Override
    public Set<SearchResult> doSearch(Query query) {

        logger.info("Query: '{}'", query);
        Set<SearchResult> results = new LinkedHashSet<SearchResult>();

        if (query == null) {
            return results;
        }

        IndexReader reader = indexFactory.createIndexReader();
        IndexSearcher searcher = null;
        try {
        	searcher = new IndexSearcher(reader);
        	
            TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
            searcher.search(query, collector);

            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            int index = 0;
            for (ScoreDoc hit : hits) {
                Document doc = searcher.doc(hit.doc);
                results.add(new LuceneResult(index++, doc, hit.score));
            }
        } catch (CorruptIndexException e) {
            logger.error("Error searching Lucene index", e);
        } catch (IOException e) {
            logger.error("Error searching Lucene index", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("Error closing Lucene index", e);
                }
            }
        }

        logger.debug("Found {} lucene documents", results.size());
        return results;
    }
}
