package com.redxiii.tracplus.ejb.search.updater;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
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
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class LuceneIndexFactory implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Version version;
	private Directory directory;
	private StandardAnalyzer analyzer;
	private IndexWriterConfig writerConfig;
	private long writeLockTimeout = 30 * 1000;
	private long writeLockRetries = 5;
	
	@PostConstruct
    public void init() {
		
		logger.info("Creating index factory");
		try {
			File path = new File(AppConfiguration.getRootConfigFolder() + "/wiki-lucene-index/");
			
			if (path.exists()) {
			    path.createNewFile();
			}
			
			this.version = Version.LUCENE_43;
			this.directory = new SimpleFSDirectory(path);
			this.analyzer = new StandardAnalyzer(version);
			this.writerConfig = new IndexWriterConfig(version, analyzer);
			
			this.writerConfig.setWriteLockTimeout(writeLockTimeout);
		} catch (IOException e) {
			logger.error("Error loading lucene index", e);
		}
	}
	
	@Lock(LockType.READ)
	public Directory getDirectory() {
		return directory;
	}
	
	@Lock(LockType.READ)
	public Version getVersion() {
		return version;
	}
	
	@Lock(LockType.READ)
	public IndexWriter createIndexWriter() {
		
		for (int c = 0; c < writeLockRetries; c++) {
	        try {
	        	logger.info("Creating index writer");
	            return new IndexWriter(directory, writerConfig);
	            
	        } catch (LockObtainFailedException e) {
	        	if (c < writeLockRetries)
	        		logger.warn("Error obtain lock: {}", e.getMessage());
	        	else
	        		logger.error("Error creating Lucene index writer", e);
	        	
	        	
	        } catch (CorruptIndexException e) {
	            logger.error("Error creating Lucene index writer", e);
	            return null;
	        } catch (IOException e) {
	        	logger.error("Error creating Lucene index writer", e);
	        	return null;
	        }
		}
        return null;
    }
	
	@Lock(LockType.READ)
	public IndexReader createIndexReader() {
		logger.info("Creating index reader");
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			new TracPlusException(TracPlusException.Code.UNKNOWN, "Fail creating reader", e);
		}
		return reader;
	}
	
	@Lock(LockType.READ)
	public Query createQuery(String field, String queryTerm) {
		logger.info("Creating query");
		try {
			QueryParser query = new QueryParser(version, field, analyzer);
			return query.parse(queryTerm);
		} catch (ParseException e) {
			return null;
		}
	}
}
