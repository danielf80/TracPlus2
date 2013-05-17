package com.redxiii.tracplus.ejb.util.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoToolWithBase64 extends CryptoToolBase implements CryptoTool {

	private static final Logger logger = LoggerFactory.getLogger(CryptoToolWithBase64.class);
	
	public CryptoToolWithBase64(String passPhrase) {
		super(passPhrase);
	}
	
	@Override
	public InputStream getEncryptInStream(InputStream stream) throws IOException {
		CipherInputStream cipherStream = new CipherInputStream(stream, dcipher);	// Thanks to L.Lima
		return new Base64InputStream(new InputStreamWrapper(cipherStream));
	}
	
	@Override
	public OutputStream getEncryptOutStream(OutputStream stream) throws IOException {
		CipherOutputStream cipherStream = new CipherOutputStream(stream, ecipher);	// Thanks to L.Lima
		return new Base64OutputStream(cipherStream);
	}
	
	/* (non-Javadoc)
	 * @see com.redxiii.safesync.CryptoTool#encrypt(java.io.InputStream, java.io.OutputStream)
	 */
	public void encrypt(InputStream inStream, OutputStream outStream) throws IOException {
		
		int count = 0;
		byte[] rawData = new byte[bufferSize];
		
		OutputStream b64Stream = getEncryptOutStream(outStream); 
		
		logger.debug("Reading, Encrypting and Encoding...");
		while ((count = inStream.read(rawData)) != -1) {
			logger.trace("Compressing, Encrypting and Writing {} bytes...", count);
			b64Stream.write(rawData, 0, count);
		}
		b64Stream.flush();
		b64Stream.close();
		
	}
		
	/* (non-Javadoc)
	 * @see com.redxiii.safesync.CryptoTool#decrypt(java.io.InputStream, java.io.OutputStream)
	 */
	public void decrypt(InputStream inStream, OutputStream outStream) throws IOException {
		
		int count;
		byte[] safeData = new byte[bufferSize];
		
		InputStream b64Stream = getEncryptInStream(inStream); 
		
		logger.debug("Reading, Decrypting and Decoding ...");
		while ((count = b64Stream.read(safeData)) != -1) {
			logger.trace("Read {} bytes...", count);
			outStream.write(safeData);
		}
		outStream.flush();
		outStream.close();
	}
}

class InputStreamWrapper extends InputStream {

	private InputStream inStream;
	
	public InputStreamWrapper(InputStream inStream) {
		this.inStream = inStream;
	}

	@Override
	public int read() throws IOException {
		return inStream.read();
	}
	
	@Override
	public int available() throws IOException {
		return 1;	// to avoid �java.io.IOException: Empty InputStream� on initialization
	}
	
	@Override
	public boolean markSupported() {
		return inStream.markSupported();
	}
	
	@Override
	public void close() throws IOException {
		inStream.close();
	}
	
	
}
