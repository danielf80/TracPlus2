package com.redxiii.tracplus.ejb.util;

import java.io.File;

public abstract class ServerConfiguration {

	private static final String JBOSS_BASE_DIR_KEY = "jboss.server.base.dir";
	private static final String GLASSFISH_BASE_DIR_KEY = "com.sun.aas.instanceRoot";
	
	private static final String JBOSS_CFG_DIR_KEY = File.separator + "configuration" + File.separator;
	private static final String GLASSFISH_CFG_DIR_KEY = File.separator + "config" + File.separator;
	
	public static String getRootConfigFolder() {
		String baseDir = System.getProperty(JBOSS_BASE_DIR_KEY);
		if (baseDir != null && new File(baseDir).exists()) {
			return baseDir + File.separator;
		}
		
		baseDir = System.getProperty(GLASSFISH_BASE_DIR_KEY);
		if (baseDir != null && new File(baseDir).exists()) {
			return baseDir + File.separator;
		}
		
		return System.getProperty("user.dir") + File.separator;
	}
	
	protected static String getServerConfigFolder() {
		String baseDir = System.getProperty(JBOSS_BASE_DIR_KEY);
		if (baseDir != null && new File(baseDir).exists()) {
			return baseDir + JBOSS_CFG_DIR_KEY;
		}
		
		baseDir = System.getProperty(GLASSFISH_BASE_DIR_KEY);
		if (baseDir != null && new File(baseDir).exists()) {
			return baseDir + GLASSFISH_CFG_DIR_KEY;
		}
		
		return System.getProperty("user.dir") + File.separator;
	}
}
