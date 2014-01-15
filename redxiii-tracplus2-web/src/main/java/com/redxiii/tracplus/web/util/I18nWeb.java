/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redxiii.tracplus.web.util;

import com.redxiii.tracplus.ejb.util.I18n;
import java.util.ResourceBundle;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 *
 * @author dfilgueiras
 */
@Named
@RequestScoped
public class I18nWeb extends I18n {

    protected ResourceBundle getBundle() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null && context.getViewRoot() != null) {
            return ResourceBundle.getBundle("com.redxiii.tracplus.messages", context.getViewRoot().getLocale());
        }
        return super.getBundle();
    }
}
