package com.redxiii.tracplus.ejb.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Daniel Filgueiras
 * @since 19/08/2011
 */
public class LookupUtil {
	
	private LookupUtil() {
	}

	@SuppressWarnings({ "unchecked" })
	public static <T> T lookupLocal(Class<? super T> type) {
		try {
			return (T) lookup(getLocalJNDIName(type));
		} catch (NamingException e) {
			throw new RuntimeException("Failed to lookup local interface to EJB " + type, e);
		}
	}

	private static <T> String getLocalJNDIName(Class<? super T> beanClass) {
		String name = beanClass.getSimpleName();
		if (beanClass.isInterface()) {
			name += "Impl";
		}
		return "TracPlus/" + name + "/local";
	}

	public static Object lookup(String name) throws NamingException {
		return new InitialContext().lookup(name);
	}
}
