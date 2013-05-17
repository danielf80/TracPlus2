package com.redxiii.tracplus.ejb.util.crypto;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CryptoToolBase implements CryptoTool {

	private static final Logger logger = LoggerFactory.getLogger(CryptoToolBase.class);
	private static final byte[] salt = "SAFESYNC".getBytes();
	
	protected static final int bufferSize = 1024;
	protected Cipher ecipher;
	protected Cipher dcipher;
	protected SecretKey keySpec;
	
	private byte[] passwordHash;
	
	public CryptoToolBase(String passPhrase) {
		try {
			passwordHash = MessageDigest.getInstance("MD5").digest(passPhrase.getBytes());
			PBEKeySpec password = new PBEKeySpec(passPhrase.toCharArray(), salt, 1000, 128);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			SecretKey key = factory.generateSecret(password);
			keySpec = new SecretKeySpec(key.getEncoded(), "AES");
			
			ecipher = Cipher.getInstance("AES");
			dcipher = Cipher.getInstance("AES");
			
			ecipher.init(Cipher.ENCRYPT_MODE, keySpec);
			dcipher.init(Cipher.DECRYPT_MODE, keySpec);
		} catch (Exception e) {
			logger.error("Unable to init crypto tool", e);
		}
	}
	
	@Override
	public String getPasswordHash() {
		return Hex.encodeHexString(passwordHash);
	}
}