package com.redxiii.tracplus.ejb.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * @author dfilgueiras
 * Classe responsável por obter o texto em diversos idiomas. Obtem os textos do arquivo messages_xx_yy.properties
 *
 */
@Named
@RequestScoped
public class I18n {

	private static ResourceBundle bundle = ResourceBundle.getBundle("com.redxiii.tracplus.messages");
	
	/**
	 * Retorna o texto no idioma correto pela chave {enumValue.name()} informada 'enum.{enumValue.getClass()}'
	 * @param enumValue
	 * @return texto no idioma padrão
	 */
	public String getEnum(Enum<?> enumObj) {
		return getString("enum." + enumObj.getClass().getSimpleName() + "." + enumObj.name());
	}
	
	/**
	 * Retorna o texto no idioma correto pela chave {key} informada
	 * @param arguments
	 * @return texto no idioma padrão
	 */
	public String getWithArgs(String key, Object argument1) {
		return getWithArgsEx(key, argument1);
	}
	public String getWithArgs(String key, Object argument1, Object argument2) {
		return getWithArgsEx(key, argument1, argument2);
	}
	public String getWithArgs(String key, Object argument1, Object argument2, Object argument3) {
		return getWithArgsEx(key, argument1, argument2, argument3);
	}
	private String getWithArgsEx(String key, Object... arguments) {
		String template = getString(key);
		return MessageFormat.format(template, arguments);
	}
	
	
	
	/**
	 * Retorna o texto no idioma correto pela chave {key} informada
	 * @param chave de busca no arquivo properties
	 * @return texto no idioma padrão
	 */
	public String getString(String key) {
            ResourceBundle bundle = getBundle();
            if (bundle.containsKey(key))
                    return bundle.getString(key);
            return "???" + key + "???";
	}
	
        protected ResourceBundle getBundle() {
            return bundle;
        }
	
}
