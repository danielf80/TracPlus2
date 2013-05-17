package com.redxiii.tracplus.ejb.search.updater;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Named;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.search.TracPlusException;
import com.redxiii.tracplus.ejb.util.AppConfiguration;

/**
 * @author Daniel
 *
 */
@Named
@Singleton
public class LuceneIndexFactory implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Version version;
	private Directory directory;
	private StandardAnalyzer analyzer;
	
	@PostConstruct
    public void init() {
		
		try {
			File path = new File(AppConfiguration.getRootConfigFolder() + "/wiki-lucene-index/");
			
			if (path.exists()) {
			    path.createNewFile();
			}
			
			this.version = Version.LUCENE_43;
			this.directory = new SimpleFSDirectory(path);
			this.analyzer = new StandardAnalyzer(version);
			
		} catch (IOException e) {
			logger.error("Error loading lucene index", e);
		}
	}
	
	public Directory getDirectory() {
		return directory;
	}
	
	public Version getVersion() {
		return version;
	}
	
	public IndexWriter createIndexWriter() {
        try {
            return new IndexWriter(directory, new IndexWriterConfig(version, analyzer));
        } catch (CorruptIndexException e) {
            logger.error("Error updating Lucene index", e);
        } catch (LockObtainFailedException e) {
            logger.error("Error updating Lucene index", e);
        } catch (IOException e) {
            logger.error("Error updating Lucene index", e);
        }
        return null;
    }
	
	public IndexReader createIndexReader() {
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			new TracPlusException(TracPlusException.Code.UNKNOWN, "Fail creating reader", e);
		}
		return reader;
	}
	
	public Query createQuery(String field, String queryTerm) {
		try {
			QueryParser query = new QueryParser(version, field, analyzer);
			return query.parse(queryTerm);
		} catch (ParseException e) {
			return null;
		}
	}
}
