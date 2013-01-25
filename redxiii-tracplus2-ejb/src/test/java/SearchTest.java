
import com.redxiii.tracplus.ejb.search.SearchResult;
import com.redxiii.tracplus.ejb.search.TracStuff;
import com.redxiii.tracplus.ejb.search.TracStuffField;
import com.redxiii.tracplus.ejb.search.query.QueryBuilder;
import com.redxiii.tracplus.ejb.search.query.SimpleQuerySpec;
import com.redxiii.tracplus.ejb.search.updater.LuceneIndexManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.search.Query;
import org.junit.Test;
import org.junit.runner.notification.RunListener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dfilgueiras
 */
public class SearchTest {

    public SearchTest() {
    }

    @Test
    public void run() throws FileNotFoundException, IOException {
        
        System.out.println("Iniciando");
        System.setProperty("jboss.server.base.dir", "C:/TEMP");
        
        LuceneIndexManager indexManager = new LuceneIndexManager();
        indexManager.init();
        
        indexManager.purgeAndRecreate();
        
        Collection<TracStuff> stuffs = new HashSet<TracStuff>();
        
        File fLocal = new File(".");
        for (File fData : new File(fLocal,"src/test/resources/testData").listFiles()) {
            BufferedReader reader = new BufferedReader(new FileReader(fData));
            StringBuilder data = new StringBuilder();
            
            String line = reader.readLine();
            while (line != null) {
                data.append(line).append("\r\n");
                
                stuffs.add(new TracStuff(fData.getName(), "localhost", "df", data.toString(), new Date(), System.currentTimeMillis(), "Test", "ticket"));
                
                line = reader.readLine();
            }
            reader.close();
        }
        
        indexManager.updateIndex(stuffs);
        
        QueryBuilder<SimpleQuerySpec> baseBuilder = QueryBuilder.buildSimpleQuery();
     
//        baseBuilder.addStrongRestriction("LockTimeoutException", TracStuffField.CONTENT);
//        baseBuilder.addStrongRestriction("webm", TracStuffField.CONTENT);
        baseBuilder.addLikeRestriction("webm", TracStuffField.CONTENT);
//        baseBuilder.addStrongRestriction("\"could not acquire lock\"", TracStuffField.CONTENT);
//        baseBuilder.addStrongRestriction("Synchronized", TracStuffField.CONTENT);

        Query query = indexManager.buildQuery(baseBuilder.createQuerySpec());
        System.out.println("Query: " + query.toString());
        Set<SearchResult> resultSet = indexManager.doSearch(query);
        
        for (SearchResult result : resultSet) {
            System.out.println("Result: " + result.getId());
        }
    }
}
