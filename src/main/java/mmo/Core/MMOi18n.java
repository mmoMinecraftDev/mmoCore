/*
 * This file is part of mmoMinecraft (https://github.com/mmoMinecraftDev).
 *
 * mmoMinecraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmo.Core;

import java.util.HashMap;

/**
 *
 * @author Xaymar
 */
/*
 * Needs to use:
 * http://download.oracle.com/javase/6/docs/api/java/util/Locale.html
 * http://download.oracle.com/javase/6/docs/api/java/util/ResourceBundle.html
 *
 * This seems overly complex - only needs a simple string->string translation
 * per language - the actual language control will all be hidden away within
 * mmoCore, as will the per-player language choice. Default language must always
 * be "en" (even if that's a translation of the language used to develop).
 *
 * Translation commands should be provided as commands (permissions etc) within
 * mmoCore. There should also be a command to reload translations (watch files
 * for changes in the future?)
 *
 * The keys used to translate (and hence show in the config files) are the real
 * strings with "[. ]" replaced with "_" (for use as YAML nodes).
 *
 * /mmolocale [a-z]{2}(-[A-Z]{2})?
 * /mmotranslate mmoPlugin "original string" "translated string"
 */
public class MMOi18n {

	private HashMap<String, HashMap<String, String>> languageMap = new HashMap<String, HashMap<String, String>>();
	private String defaultLanguage = "en";
	private String selectedLanguage = "en";

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
