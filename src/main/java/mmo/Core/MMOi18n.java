/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmo.Core;

import java.util.HashMap;

/**
 *
 * @author Xaymar
 */
public class MMOi18n {

	private HashMap<String, HashMap<String, String>> languageMap = new HashMap<String, HashMap<String, String>>();
	private String defaultLanguage = "EN";
	private String selectedLanguage = "EN";

	public MMOi18n() {
		addLanguage(defaultLanguage);
	}

	public boolean languageExists(String languageName) {
		return languageMap.containsKey(languageName);
	}

	public boolean addLanguage(String languageName) {
		if (languageExists(languageName) == false) {
			languageMap.put(languageName, new HashMap<String, String>());
			return true;
		}
		return false;
	}

	public boolean setDefaultLanguage(String languageName) {
		if (languageExists(languageName)) {
			defaultLanguage = languageName;
			return true;
		}
		return false;
	}

	public boolean setLanguage(String languageName) {
		if (languageExists(languageName)) {
			selectedLanguage = languageName;
			return true;
		}
		selectedLanguage = defaultLanguage;
		return false;
	}

	public boolean removeLanguage(String languageName) {
		if (languageExists(languageName)) {
			languageMap.remove(languageName);
			return true;
		}
		return false;
	}

	private HashMap<String, String> getLanguageTable(String languageName) {
		if (languageExists(languageName)) {
			return languageMap.get(languageName);
		} else {
			return null;
		}
	}
	
	public boolean addString(String ID, String to) {
		if (languageExists(selectedLanguage)) {
			HashMap<String, String> langTable = getLanguageTable(selectedLanguage);
			langTable.put(ID, to);
			return true;
		}
		return false;
	}
	
	public String getString(String ID) {
		if (languageExists(selectedLanguage)) {
			HashMap<String, String> langTable = getLanguageTable(selectedLanguage);
			if (langTable.containsKey(ID)) {
				return langTable.get(ID);
			}
		} else {
			HashMap<String, String> langTable = getLanguageTable(defaultLanguage);
			if (langTable.containsKey(ID)) {
				return langTable.get(ID);
			}
		}
		return "STRING NOT IN DEFAULT I18N.";
	}
	
	public boolean removeString(String ID) {
		if (languageExists(selectedLanguage)) {
			HashMap<String, String> langTable = getLanguageTable(selectedLanguage);
			if (langTable.containsKey(ID)) {
				langTable.remove(ID);
				return true;
			}
		}
		return false;
	}
}
