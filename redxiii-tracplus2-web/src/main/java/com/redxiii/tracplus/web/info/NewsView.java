package com.redxiii.tracplus.web.info;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import com.redxiii.tracplus.ejb.util.AppConfiguration;

@Named
@RequestScoped
public class NewsView {

	private List<String> newsList = new ArrayList<String>();
	
	@PostConstruct
	public void init() {
	
		newsList.clear();
		
		Configuration configuration = AppConfiguration.getInstance();
		if (configuration.containsKey("web.news.itens")) {
			int qtd = configuration.getInt("web.news.itens", 0);
			for (int c = 1; c <= qtd; c++) {
				String key = "web.news.item-" + c;
				if (configuration.containsKey(key)) {
					String news = configuration.getString(key, null);
					if (StringUtils.isNotBlank(news))
						newsList.add(news);
				}
			}
		}
	}
	
	public List<String> getNewsList() {
		return newsList;
	}
}
