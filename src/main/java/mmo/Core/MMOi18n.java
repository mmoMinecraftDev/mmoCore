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
	private String defaultLanguage = "";
	private String selectedLanguage = "";

	public boolean languageExists(String languageName) throws Exception {
		if (languageName.matches("[a-z]{2}(?:-[A-Z]{2})?") == true) {
			return languageMap.containsKey(languageName);
		} else {
			throw new Exception("Given language name is not ISO-639-2");
		}
	}

	public boolean addLanguage(String languageName) throws Exception {
		if (languageName.matches("[a-z]{2}(?:-[A-Z]{2})?") == true) {
			if (languageExists(languageName) == false) {
				languageMap.put(languageName, new HashMap<String, String>());
				return true;
			}
			return false;
		} else {
			throw new Exception("Given language name is not ISO-639-2");
		}
	}

	public boolean setDefaultLanguage(String languageName) throws Exception {
		if (languageName.matches("[a-z]{2}(?:-[A-Z]{2})?") == true) {
			if (languageExists(languageName)) {
				defaultLanguage = languageName;
				return true;
			}
			return false;
		} else {
			throw new Exception("Given language name is not ISO-639-2");
		}
	}

	public boolean setLanguage(String languageName) throws Exception {
		if (languageName.matches("[a-z]{2}(?:-[A-Z]{2})?") == true) {
			if (languageExists(languageName)) {
				selectedLanguage = languageName;
				return true;
			}
			selectedLanguage = defaultLanguage;
			return false;
		} else {
			throw new Exception("Given language name is not ISO-639-2");
		}
	}

	public boolean removeLanguage(String languageName) throws Exception {
		if (languageName.matches("[a-z]{2}(?:-[A-Z]{2})?") == true) {
			if (languageExists(languageName)) {
				languageMap.remove(languageName);
				return true;
			}
			return false;
		} else {
			throw new Exception("Given language name is not ISO-639-2");
		}
	}

	private HashMap<String, String> getLanguageTable(String languageName) throws Exception {
		if (languageExists(languageName)) {
			return languageMap.get(languageName);
		} else {
			return null;
		}
	}

	public boolean addString(String ID, String to) throws Exception {
		if (languageExists(selectedLanguage)) {
			HashMap<String, String> langTable = getLanguageTable(selectedLanguage);
			langTable.put(ID, to);
			return true;
		}
		return false;
	}

	public String getString(String ID) throws Exception {
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

	public boolean removeString(String ID) throws Exception {
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
