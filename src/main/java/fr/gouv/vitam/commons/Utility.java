/**
   This file is part of Vitam Project.

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All Vitam Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Vitam is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Vitam .  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gouv.vitam.commons;

import java.io.File;
import java.net.URL;

/**
 * @author "Frederic Bregier"
 *
 */
public class Utility {
	public final static String resourceToFile(String arg) {
		File test = new File(arg);
		if (test.exists()) {
			return test.getAbsolutePath();
		}
		URL url = arg.getClass().getResource(arg);
		if (url == null) {
			return null;
		}
		String value = url.toString();
		value = value.substring(6).replaceAll("%20", "\\ ");
		return new File(value).getAbsolutePath();
	}

	public final static String resourceToParent(String arg) {
		return new File(arg.getClass().getResource(arg).toString()).getParentFile().toURI()
				.getPath();
	}

	public final static String resourceToURL(String arg) {
		return arg.getClass().getResource(arg).getFile();
	}


	/**
	 * Utility to free memory
	 */
	public final static void freeMemory() {
		long total = Runtime.getRuntime().totalMemory();
		for (int i = 0; i < 10; i++) {
			System.gc();
		}
		while (true) {
			System.gc();
			long newtotal = Runtime.getRuntime().totalMemory();
			if ((((double) (total - newtotal)) / (double) total) < 0.1) {
				break;
			} else {
				total = newtotal;
			}
		}
		System.gc();
	}

	/**
	 * 
	 * @param fullPath
	 * @param fromPath
	 * @return the sub path starting from fromPath (inclusive last dir) of fullPath
	 */
	public static final String getSubPath(File fullPath, File fromPath) {
		String spath = fullPath.getAbsolutePath();
		String sparent = fromPath.getAbsolutePath() + File.separator;
		return spath.replace(sparent, "");
	}
	/**
	 * 
	 * @param name1
	 * @param name2
	 * @return the combination of the 2 file name into one as name1_name2 without extension
	 */
	public static final String getNameFrom2Files(String name1, String name2) {
		int position = name1.lastIndexOf('.');
		if (position < 0) {
			position = name1.length();
		}
		int position2 = name2.lastIndexOf('.');
		if (position2 < 0) {
			position2 = name2.length();
		}
		int slashpos = name2.lastIndexOf('\\') + 1;
		if (slashpos <= 0) {
			slashpos = name2.lastIndexOf('/') + 1;
			if (slashpos < 0) {
				slashpos = 0;
			}
		}
		return name1.substring(0, position) + "_" + name2.substring(slashpos, position2);
	}
}
