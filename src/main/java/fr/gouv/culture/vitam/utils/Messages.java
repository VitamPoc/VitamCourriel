package fr.gouv.culture.vitam.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class Messages {
	public static final String VITAM_LOCALE = "vitam.locale";
	private static final String BUNDLE_NAME = "resources/Vitam"; //$NON-NLS-1$

	private static ResourceBundle RESOURCE_BUNDLE = null;
	public static String slocale = "fr";
	
	static {
		slocale = SystemPropertyUtil.get(VITAM_LOCALE, "fr");
		if (slocale == null || slocale.isEmpty()) {
			slocale = "fr";
		}
		init(new Locale(slocale));
	}
	
	public static void init(Locale locale) {
		if (locale == null) {
			slocale = "fr";
			locale = new Locale(slocale);
		} else {
			slocale = locale.getLanguage();
		}
		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, locale);
		//XXX FIXME if some error code to reset: ErrorCode.updateLang();
	}

	public static String getDotString(String dotkey) {
		return getString(dotkey.replaceFirst("_", "."));
	}
	
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
	
	public static String getString(String key, Object ...args) {
		try {
			String source = RESOURCE_BUNDLE.getString(key);
			return MessageFormat.format(source, args);
		} catch (MissingResourceException e) {
			return key;
		}
	}
}
