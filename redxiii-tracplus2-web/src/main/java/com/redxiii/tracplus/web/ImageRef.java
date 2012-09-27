/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redxiii.tracplus.web;

import com.redxiii.tracplus.ejb.util.AppConfiguration;
import java.io.Serializable;
import java.net.URL;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 * @author dfilgueiras
 */
@Named
@ApplicationScoped
public class ImageRef implements Serializable {
    
    private String logoUrl;
    private String splashUrl;
    
    @PostConstruct
    public void init() {
        logoUrl = AppConfiguration.getInstance().getString("web.appearance.images.logo","/resources/gfx/trac_logo.png");
        splashUrl = AppConfiguration.getInstance().getString("web.appearance.images.splash","/resources/gfx/trac_logo.png"); 
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getSplashUrl() {
        return "background:url(" + splashUrl + ") no-repeat;";
    }
    
}
