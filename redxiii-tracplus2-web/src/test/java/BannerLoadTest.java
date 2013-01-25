import java.net.URL;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import com.redxiii.tracplus.web.banner.Banner;


public class BannerLoadTest {

	/**
	 * @param args
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws ConfigurationException {
		
		URL url = Thread.currentThread().getContextClassLoader().getResource("config/tracplus2-banners.xml");
		XMLConfiguration configuration = new XMLConfiguration(url);
		configuration.setAutoSave(true);
		configuration.setReloadingStrategy(new FileChangedReloadingStrategy());	// 5 seconds
		
		List<HierarchicalConfiguration> packNodes = configuration.configurationsAt("banner");
		for (HierarchicalConfiguration packNode : packNodes) {
			Banner banner = new Banner();
			banner.setLink(packNode.configurationAt("link").getProperty("[@value]").toString());
			banner.setImageUrl(packNode.configurationAt("image").getProperty("[@value]").toString());
			banner.setSubject(packNode.configurationAt("subject").getProperty("[@value]").toString());
			banner.setText(packNode.configurationAt("text").getProperty("[@value]").toString());
		}
	}

}
