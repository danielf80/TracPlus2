package com.redxiii.tracplus.ejb.util.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface CryptoTool {

	void encrypt(InputStream inStream, OutputStream outStream)
			throws IOException;

	void decrypt(InputStream inStream, OutputStream outStream)
			throws IOException;
	
	InputStream getEncryptInStream(InputStream stream) throws IOException;
	
	OutputStream getEncryptOutStream(OutputStream stream) throws IOException;
	
	String getPasswordHash();
}