package com.redxiii.tracplus.web.banner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.util.ServerConfiguration;

@Named("bannerView")
@SessionScoped
public class BannerView implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<Banner> banners;
	
	public BannerView() {
		// No no-args constructor NEEDED to SessionScoped classes
	}
	
	@PostConstruct
	public void init() {
		banners = new ArrayList<Banner>();
		
		@SuppressWarnings("unchecked")
		List<HierarchicalConfiguration> packNodes = BannerConfig.getInstance().configurationsAt("banner");
		for (HierarchicalConfiguration packNode : packNodes) {
			
			String link = packNode.configurationAt("link").getProperty("[@value]").toString();
			Banner banner = new Banner();
			banner.setLink("redirectTo=" + Hex.encodeHexString(link.getBytes()));
			banner.setImageUrl(packNode.configurationAt("image").getProperty("[@value]").toString());
			banner.setSubject(packNode.configurationAt("subject").getProperty("[@value]").toString());
			banner.setText(packNode.configurationAt("text").getProperty("[@value]").toString());
			banners.add(banner);	
		}
	}
	
	public List<Banner> getBanners() {
		return banners;
	}
}

class BannerConfig extends ServerConfiguration {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private XMLConfiguration configuration;
	private static BannerConfig instance;
	
	private BannerConfig() {
		String url = getServerConfigFolder() + "tracplus2-banners.xml";
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
	
	public synchronized static HierarchicalConfiguration getInstance() {
		if (instance == null)
			instance = new BannerConfig();
		
		return instance.configuration;
	}
}
