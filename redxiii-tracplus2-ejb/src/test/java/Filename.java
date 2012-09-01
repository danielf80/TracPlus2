import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.Normalizer;

import org.joda.time.LocalDate;


/**
 * @author Daniel Filgueiras
 * @since 27/08/2012
 */
public class Filename {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws MalformedURLException, UnsupportedEncodingException {
		System.out.println(new LocalDate().toDate().getTime());
	}
	
}
