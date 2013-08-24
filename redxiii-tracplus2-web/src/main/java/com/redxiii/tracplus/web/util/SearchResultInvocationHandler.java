package com.redxiii.tracplus.web.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.commons.codec.binary.Hex;

import com.redxiii.tracplus.ejb.search.SearchResult;

public class SearchResultInvocationHandler implements InvocationHandler {

	private final int id;
	private final SearchResult searchResult;
	
	public SearchResultInvocationHandler(int id, SearchResult searchResult) {
		this.id = id;
		this.searchResult = searchResult;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		Object result = method.invoke(searchResult, args);
		if (method.getName().endsWith("getUrl")) {
			String baseUrl = (String) result;
			StringBuilder builder = new StringBuilder()
				.append("id=")
				.append(id)
				.append("&redirectTo=")
				.append(Hex.encodeHexString(baseUrl.getBytes()));
				
			return builder.toString();
		}
			
		return result;
	}

}
