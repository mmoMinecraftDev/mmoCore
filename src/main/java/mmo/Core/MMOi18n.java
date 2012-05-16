/*
 * This file is part of mmoCore <http://github.com/mmoMinecraftDev/mmoCore>.
 *
 * mmoCore is free software: you can redistribute it and/or modify
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

/*
 * Needs to use:
 * http://download.oracle.com/javase/6/docs/api/java/util/Locale.html
 * http://download.oracle.com/javase/6/docs/api/java/util/ResourceBundle.html
 * Comment by Xaymar: I don't see a particular use aside from it enforcing ISO. ResourceBundle looks like a "simpler" version of HashMap with less dynamic Ability.(Only took a quick look at both)
 *
 * This seems overly complex - only needs a simple string->string translation
 * per language - the actual language control will all be hidden away within
 * mmoCore, as will the per-player language choice. Default language must always
 * be "en" (even if that's a translation of the language used to develop).
 * Comment by Xaymar: Consider this done.
 *
 * Translation commands should be provided as commands (permissions etc) within
 * mmoCore. There should also be a command to reload translations (watch files
 * for changes in the future?)
 * Comment by Xaymar: Client Preferences?
 *
 * The keys used to translate (and hence show in the config files) are the real
 * strings with "[. ]" replaced with "_" (for use as YAML nodes).
 * Comment by Xaymar: Loading is done outside of this, copied comment into MMOPlugin.java
 *
 * /mmolocale [a-z]{2}(-[A-Z]{2})?
 * /mmotranslate mmoPlugin "original string" "translated string"
 * Comment by Xaymar: How should we store per Player settings? I don't think having lots of copies of Player settings will work out as MMOi18n gets created for _every_ plugin that inherits MMOPlugin.
 *			We need some central storage place for stuff, like the shared DB you mentioned.
 */
public class MMOi18n {
	private HashMap<String, HashMap<String, String>> locales = new HashMap<String, HashMap<String, String>>();
	private String localeDefault = "en";
	private HashMap<String, String> localeSelected = null;

	protected boolean localeExists(String locale) {
		if (locale.matches("[a-z]{2}(?:-[A-Z]{2})?") == true) {
			return locales.containsKey(locale);
		}
		return false;
	}

	protected boolean addLocale(String locale) {
		if (localeExists(locale) == false) {
			locales.put(locale, new HashMap<String, String>());
			return true;
		}
		return false;
	}

	protected boolean removeLocale(String locale) {
		if (localeExists(locale)) {
			HashMap<String, String> loc = locales.get(locale);
			loc.clear();
			loc = null;
			locales.remove(locale);
			return true;
		}
		return false;
	}

	protected boolean localeDefault(String locale) {
		if (localeExists(locale)) {
			localeDefault = locale;
			return true;
		}
		return false;
	}

	private HashMap<String, String> localeTable(String locale) {
		if (localeExists(locale)) {
			return locales.get(locale);
		} else {
			if (localeExists(localeDefault)) {
				return locales.get(localeDefault);
			} else {
				return new HashMap<String, String>();
			}
		}
	}

	protected void selectLocale(String locale) {
		localeSelected = localeTable(locale);
	}

	protected void setTranslation(String ident, String translation) {
		localeSelected.put(ident, translation);
	}

	protected String getTranslation(String ident) {
		if (localeSelected.containsKey(ident)) {
			return localeSelected.get(ident);
		}
		HashMap<String, String> locDef = localeTable(localeDefault);
		if (locDef.containsKey(ident)) {
			return locDef.get(ident);
		}
		return "[i18n doesn't know about '" + ident + "']";
	}
}
