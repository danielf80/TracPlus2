package com.redxiii.tracplus.web.info;

/**
 * @author Daniel Filgueiras
 * @since 26/07/2012
 */
public class SearchCount {

	private String key;
	private String count;
	
	public SearchCount(String key, String count) {
		this.key = key;
		this.count = count;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
}
