package com.redxiii.tracplus.web.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@ApplicationScoped
public class ManifestReader {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final Map<String, String> properties = new LinkedHashMap<String, String>();
	
	private final List<String> keys = new ArrayList<String>();
	
	@PostConstruct
	public void init() {
		Configuration configuration = getManifestProperties();
		
		if (configuration != null) {
			Iterator<String> iterator = configuration.getKeys();
			while (iterator.hasNext()) {
				String key = iterator.next();
				if (key.matches("^[\\w\\-\\d]+")) {
					keys.add(key);
					properties.put(key, configuration.getString(key));
				} else {
					logger.warn("Manifest line/key discarted: " + key);
				}
			}
		}
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public List<String> getKeys() {
		return keys;
	}
	
	public Configuration getManifestProperties() {
		try {
			try {
				ProtectionDomain domain = ManifestReader.class.getProtectionDomain();
				if (domain != null) {
					CodeSource codeSource = domain.getCodeSource();
					if (codeSource != null) {
						String location = codeSource.getLocation().toExternalForm();
						logger.info("SourceLocation: {}", location);
						
						// Glassfish: ... domain1/applications/redxiii-tracplus2/redxiii-tracplus2-web_war/WEB-INF/classes/com/redxiii/tracplus/web/util/ManifestReader.class
						int earFolderIndex = location.indexOf("/", location.indexOf("redxiii-tracplus.ear"));
						
						String earFolder = location.substring(0, earFolderIndex);
						logger.info("earFolder: {}", earFolder);
						
						URL url = new URL(earFolder + "/" + JarFile.MANIFEST_NAME);
						if (url != null) {
							logger.info("Reading manifest file '{}' from ProtectionDomain", url);
							return new PropertiesConfiguration(url);
						}
					}
				}
			} catch (MalformedURLException e) {
				logger.error("Unable to read manifest file and get build revision",e);
			}
		} catch (ConfigurationException e) {
			logger.error("Unable to read manifest file and get build revision",e);
		}
		return null;
	}
}
