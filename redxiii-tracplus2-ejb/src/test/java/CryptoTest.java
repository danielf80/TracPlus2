import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.redxiii.tracplus.ejb.util.crypto.CryptoTool;
import com.redxiii.tracplus.ejb.util.crypto.CryptoToolWithBase64;


public class CryptoTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		CryptoTool tool = new CryptoToolWithBase64("1234567890");
		ByteArrayInputStream inStream = new ByteArrayInputStream("teste".getBytes());
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		tool.encrypt(inStream, outStream);
		
		System.out.println(new String(outStream.toByteArray()));
	}

}
