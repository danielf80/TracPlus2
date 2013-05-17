package com.redxiii.tracplus.ejb.search.filters;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.inject.Default;
import javax.inject.Named;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.entity.Attachment;
import com.redxiii.tracplus.ejb.entity.Ticket;
import com.redxiii.tracplus.ejb.entity.Wiki;
import com.redxiii.tracplus.ejb.util.ServerConfiguration;

@Default
@Named
public class TicketIndexFilter implements IndexFilter {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final Map<String, String> filters;
	private final Map<Field, String> filtersField = new HashMap<Field, String>();
	private boolean enabled = true;
	
	public TicketIndexFilter() {
		filters = FilterConfiguration.getFilters();
		try {
			for (String filterField : filters.keySet()) {
				String regexp = filters.get(filterField);
				Field field = Ticket.class.getDeclaredField(filterField);
				field.setAccessible(true);
				logger.debug("New filter found at field '{}' with regexp '{}'", filterField, regexp);
				filtersField.put(field, regexp);
			}
		} catch (Exception e) {
			logger.error("Error getting field for filtering: {}", filtersField, e);
		}
	}
	
	@Override
	public boolean isAllowed(Attachment attachment) {
		return true;
	}

	@Override
	public boolean isAllowed(Ticket ticket) {
		if (enabled) {
			for (Entry<Field, String> entry : filtersField.entrySet()) {
				Field field = entry.getKey();
				String regexp = entry.getValue();
				
				try {
					Object obj = field.get(ticket);
					if (obj != null) {
						String strValue = obj.toString();
						if (strValue.matches(regexp)) {
							logger.debug("Ticket is not allowed: {}", ticket);
							return false;
						}
						
					}
				} catch (Exception e) {
					logger.error("Error performing filter operation on field: {}", field, e);
					enabled = false;
					logger.warn("Filter operation permanently disabled");
				}
			}
		}
		return true;
	}

	@Override
	public boolean isAllowed(Wiki wiki) {
		return true;
	}

}

class FilterConfiguration extends ServerConfiguration {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private XMLConfiguration configuration;
	private static FilterConfiguration instance;
	
	private FilterConfiguration() {
		String url = getServerConfigFolder() + "tracplus2-index-filters.xml";
		try {
			configuration = new XMLConfiguration(url);
			configuration.setAutoSave(true);
			configuration.setReloadingStrategy(new FileChangedReloadingStrategy());	// 5 seconds
			
			logger.debug("Configuration file '{}' loaded", url);
		} catch (ConfigurationException e) {
			logger.error("Unable to load configuration file {}", e, url);
			configuration = new XMLConfiguration();
		} 
	}
	
	public synchronized static HierarchicalConfiguration getConfiguration() {
		if (instance == null)
			instance = new FilterConfiguration();
		
		return instance.configuration;
	}
	
	public static Map<String, String> getFilters() {
		
		Map<String, String> filters = new HashMap<String, String>();
		
		List<HierarchicalConfiguration> packNodes = getConfiguration().configurationsAt("filter");
		for (HierarchicalConfiguration packNode : packNodes) {
			String field = packNode.configurationAt("field").getProperty("[@value]").toString();
			String regexp = packNode.configurationAt("regexp").getProperty("[@value]").toString();
		
			filters.put(field, regexp);
		}
		
		return filters;
	}
}