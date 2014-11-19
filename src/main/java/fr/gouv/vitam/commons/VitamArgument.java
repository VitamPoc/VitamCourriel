/**
   This file is part of VITAM Project.

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All VITAM Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   VITAM is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with VITAM.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gouv.vitam.commons;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.dom4j.Document;

/**
 * @author "Frederic Bregier"
 *
 */
public class VitamArgument {
	public DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private HashMap<String, Object> specialArguments;
	private File currentFileChecked;
	private File xmlInFile;
	private Document xmlInDom4J;
	/**
	 * array of integer<br>
	 * (systemError,<br>
	 * file Error, Warning, Success,<br>
	 * digest Error, Warning, Success,<br>
	 * format Error, Warning, Success,<br>
	 * show Error, Warning, Success)
	 */
	private HashMap<String, Integer> outputValues;
	/**
	 * @param specialArguments
	 * @param currentFileChecked
	 * @param xmlInFile
	 * @param xmlInDom4J
	 */
	public VitamArgument(HashMap<String, Object> specialArguments, File currentFileChecked,
			File xmlInFile, Document xmlInDom4J) {
		this.specialArguments = specialArguments;
		this.currentFileChecked = currentFileChecked;
		this.xmlInFile = xmlInFile;
		this.xmlInDom4J = xmlInDom4J;
	}
	
	/**
	 * 
	 * @param key
	 * @return the value
	 */
	public Object getSpecialArg(String key) {
		return specialArguments.get(key);
	}
	/**
	 * 
	 * @param key
	 * @param value
	 * @return the old value
	 */
	public Object setSpecialArgs(String key, Object value) {
		return specialArguments.put(key, value);
	}
	/**
	 * 
	 * @param key
	 * @return the value
	 */
	public Integer getOutput(String key) {
		return outputValues.get(key);
	}
	/**
	 * 
	 * @param key
	 * @param value
	 * @return the old value
	 */
	public Integer setOuput(String key, Integer value) {
		return outputValues.put(key, value);
	}

	/**
	 * @return the xmlInFile
	 */
	public File getXmlInFile() {
		return xmlInFile;
	}

	/**
	 * @param xmlInFile the xmlInFile to set
	 */
	public void setXmlInFile(File xmlInFile) {
		this.xmlInFile = xmlInFile;
	}

	/**
	 * @return the xmlInDom4J
	 */
	public Document getXmlInDom4J() {
		return xmlInDom4J;
	}

	/**
	 * @param xmlInDom4J the xmlInDom4J to set
	 */
	public void setXmlInDom4J(Document xmlInDom4J) {
		this.xmlInDom4J = xmlInDom4J;
	}

	/**
	 * @return the currentFileChecked
	 */
	public File getCurrentFileChecked() {
		return currentFileChecked;
	}

	/**
	 * @return the specialArguments
	 */
	public HashMap<String, Object> getSpecialArguments() {
		return specialArguments;
	}
	
}
