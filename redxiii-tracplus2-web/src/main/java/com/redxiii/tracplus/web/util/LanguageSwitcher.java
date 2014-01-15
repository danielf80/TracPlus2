package com.redxiii.tracplus.web.util;

import java.io.Serializable;
import java.util.Locale;

import javax.faces.context.FacesContext;

public class LanguageSwitcher implements Serializable {

	private static final long serialVersionUID = 1L;
	private Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();

	public Locale getLocale() {
		return locale;
	}

	public String getLanguage() {
		return locale.getLanguage();
	}

	/**
	 * Sets the current {@code Locale} for each user session
	 * 
	 * @param languageCode - ISO-639 language code
	 */
	public void changeLanguage(String language) {
		locale = new Locale(language);
		FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
	}
}
