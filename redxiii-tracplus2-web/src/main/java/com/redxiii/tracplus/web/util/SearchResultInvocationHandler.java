package com.redxiii.tracplus.web.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.commons.codec.binary.Hex;

import com.redxiii.tracplus.ejb.search.SearchResult;

public class SearchResultInvocationHandler implements InvocationHandler {

	private final SearchResult searchResult;
	private final String replaceUrl;
	
	public SearchResultInvocationHandler(SearchResult searchResult, String replaceUrl) {
		this.searchResult = searchResult;
		this.replaceUrl = replaceUrl;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		Object result = method.invoke(searchResult, args);
		if (method.getName().endsWith("getUrl")) {
			String baseUrl = (String) result;
			result = replaceUrl + Hex.encodeHexString(baseUrl.getBytes());
		}
			
		return result;
	}

}
