/**
 * This file is part of Vitam Project.
 * 
 * Copyright 2010, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Vitam Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Vitam is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Vitam. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.gouv.vitam.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSModelGroupImpl;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

import fr.gouv.vitam.commons.Utility;
import fr.gouv.vitam.commons.VitamArgument;

/**
 * XML manipulation class using SAX
 * 
 * @author "Frederic Bregier"
 * 
 */
public class XmlSaxTools {
	// Parsers
	public static final String SAX_DRIVER = "org.xml.sax.driver";
	public static final String TRANSFORMER_FACTORY = "javax.xml.transform.TransformerFactory";
	public static final String SAXON_TRANSFORMER_NAME = "net.sf.saxon.TransformerFactoryImpl";
	public static final String XALAN_TRANSFORMER_NAME = "org.apache.xalan.processor.TransformerFactoryImpl";
	public static final String RESOURCES_SCHEMATRON_SHOW_ERRORS_XSL = "/resources/schema/show_errors.xsl";
	public static final String RESOURCES_SCHEMATRON_ISO_SVRL_FOR_XSLT1_XSL = "/resources/schema/iso_svrl_for_xslt1.xsl";
	public static final String RESOURCES_SCHEMATRON_ISO_SVRL_FOR_XSLT2_XSL = "/resources/schema/iso_svrl_for_xslt2.xsl";
	public static final String RESOURCES_SCHEMATRON_ISO_ABSTRACT_EXPAND_XSL = "/resources/schema/iso_abstract_expand.xsl";
	public static final String RESOURCES_SCHEMATRON_ISO_DSDL_INCLUDE_XSL = "/resources/schema/iso_dsdl_include.xsl";
	public static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
	public static final String EXTERNAL_SCHEMALOCATION = "http://apache.org/xml/properties/schema/external-schemaLocation";
	public static final String HTTP_XML_ORG_SAX_FEATURES_VALIDATION = "http://xml.org/sax/features/validation";
	public static final String XSD2SCHEMA = "/resources/schema/xsd2schema.xsl";

	public static boolean useSaxonToGetRootFromXSD = true;
	public static boolean useXslt1ForSchematron = true;
	// Parsers
	public static String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
	public static String DEFAULT_TRANSFORMER_NAME = SAXON_TRANSFORMER_NAME;
	// Xsd
	public static String CURRENT_XSD_ROOT = "ArchiveTransfer";
	public static String DEFAULT_LOCATION = "fr:gouv:culture:archivesdefrance:seda:v1.0";
	// Field
	public static String DOCUMENT_FIELD = "Document";
	public static String ATTACHMENT_FIELD = "Attachment";
	public static String FILENAME_ATTRIBUTE = "@filename";
	public static String INTEGRITY_FIELD = "Integrity";
	public static String ALGORITHME_ATTRIBUTE = "@algorithme";
	public static String MIMECODE_ATTRIBUTE = "@mimeCode";
	public static String FORMAT_ATTRIBUTE = "@format";
	
	// XPATH //DOCUMENT_FIELD example: //Document
	// XPATH //DOCUMENT_FIELD/ATTACHMENT_FIELD example: //Document/Attachment
	// XPATH //DOCUMENT_FIELD/ATTACHMENT_FIELD/FILENAME_ATTRIBUTE example:
	// //Document/Attachment/@filename
	// XPATH //DOCUMENT_FIELD/ATTACHMENT_FIELD/MIMECODE_ATTRIBUTE example:
	// //Document/Attachment/@mimeCode
	// XPATH //DOCUMENT_FIELD/ATTACHMENT_FIELD/FORMAT_ATTRIBUTE example:
	// //Document/Attachment/@format
	// XPATH //DOCUMENT_FIELD/INTEGRITY_FIELD example: //Document/Integrity
	// XPATH //DOCUMENT_FIELD/INTEGRITY_FIELD/ALGORITHME_ATTRIBUTE example:
	// //Document/Integrity/@algorithme

	public static String BASE_RESOURCES = null;
	static {
		String val = Utility.resourceToFile(XSD2SCHEMA);
		File file = new File(val);
		BASE_RESOURCES = file.getParentFile().getParentFile().getParent();
	}
	/**
	 * Given an SchemaGrammar, returns a list of the root element names that a Document can have
	 * associated as children. Candidates: those top elements that doesn't need to belong to another
	 * elements...
	 * 
	 * @param _sg
	 *            the XSModel (from getXSModelFromXsdFile)
	 * @return the vector of potential root from XSModel
	 */
	static public List<String> getRootElementNames(XSModel _sg) {
		List<String> ret = new ArrayList<String>();
		HashSet<String> referencedElements = new HashSet<String>();
		HashSet<String> visitedTypes = new HashSet<String>();
		// Get all schema root elements (defined)
		XSNamedMap elementDeclarations = _sg.getComponents(XSConstants.ELEMENT_DECLARATION);
		int n = elementDeclarations.getLength();
		// Visit every top element in grammar. For each of them, store
		// all elements referenced in those types.
		for (int i = 0; i < n; i++) {
			XSObject xso = elementDeclarations.item(i);
			XSElementDeclaration xsed = (XSElementDeclaration) xso;
			XSTypeDefinition xtd = xsed.getTypeDefinition();
			if (xtd != null) {
				storeReferencedElements(xtd, visitedTypes, referencedElements);
			}
		}
		// Make a 2nd visit to fill vector of root element names to return.
		for (int i = 0; i < n; i++) {
			XSObject xso = elementDeclarations.item(i);
			boolean isNotRoot = referencedElements.contains(xso.getName());
			if (!isNotRoot) {
				ret.add(xso.getName());
			}
		}
		return ret;
	}

	/**
	 * For the given type, store referenced elements in referencedElements hashtable
	 */
	static private void storeReferencedElements(XSTypeDefinition xtd, HashSet<String> visitedTypes,
			HashSet<String> referencedElements) {
		// REVISIT: a) Do not use hashtable attribute, but a dynamic one OR b) synchronize methods
		String typeName = xtd.getName();
		// To avoid a named type to be revisited several times, we mark current type as a visited
		// one...
		// For in-line (un-named) types there's no solution at all...
		if (typeName != null && visitedTypes.contains(typeName)) {
			return;
		}
		// REVISIT:
		// A) Foo value 'v'. We should use a vector of unique values (now hash keys...)
		// B) Keep NameSpaces in mind as well!!
		if (typeName != null) {
			visitedTypes.add(typeName);
		}
		// REVISIT: ? Used better than: if(xtd.getTypeCategory()==XSTypeDefinition.COMPLEX_TYPE)
		if (xtd instanceof XSComplexTypeDecl) {
			XSComplexTypeDecl xtdc = (XSComplexTypeDecl) xtd;
			XSParticle xsp = xtdc.getParticle();
			if (xsp != null) {
				refElemsFromParticle(xsp, visitedTypes, referencedElements);
			}
		} // else ... REVISIT: Simple types may hide some valueable info?
	}

	static private void refElemsFromParticle(XSParticle xsp, HashSet<String> visitedTypes,
			HashSet<String> referencedElements) {
		XSTerm xsterm = xsp.getTerm();
		// If it is a complexType, call this very method for every particle
		if (xsterm instanceof XSModelGroupImpl) {
			// Obtain particles for this group implementation
			XSModelGroupImpl xmgi = (XSModelGroupImpl) xsterm;
			XSObjectList ol = xmgi.getParticles();
			if (ol != null) {
				int n = ol.getLength();
				for (int j = 0; j < n; j++) {
					Object o = ol.item(j);
					refElemsFromParticle((XSParticle) o, visitedTypes, referencedElements);
				}
			}
		}
		// If it is an element: A) store its name in referencedElements hash and
		// b) visit its type as well, searching other referenced elements.
		else if (xsterm instanceof XSElementDecl) {
			XSElementDecl xeldec = (XSElementDecl) xsterm;
			String elemName = xeldec.getName();
			// REVISIT: Foo value 'r'. We should use a vector of unique values (now hash keys...)
			referencedElements.add(elemName);
			XSTypeDefinition xtd = xeldec.getTypeDefinition();
			if (xtd != null) {
				storeReferencedElements(xtd, visitedTypes, referencedElements);
			}
		}
	}

	/**
	 * Better method to get root elements from a XSD schema
	 * 
	 * @param file
	 * @param config
	 * @return list of root
	 * @throws JDOMException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public static List<String> getRootElementNamesUsingSaxon(File file, VitamArgument config)
			throws JDOMException, IOException,
			TransformerException {
		Map<String, String> param = new HashMap<String, String>();
		String path = file.getParentFile().toURI().getPath();
		param.put("path", path); //$NON-NLS-1$
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setIgnoringElementContentWhitespace(true);
		Document document = builder.build(file);
		JDOMSource source = new JDOMSource(document);
		JDOMResult res = new JDOMResult();
		Transformer transformer = getTransformer(path, param, config);
		transformer.transform(source, res);
		Document result = res.getDocument();
		param.clear();
		param = null;
		List<String> list = setCombos(result);
		transformer.reset();
		transformer = null;
		document.removeContent();
		document = null;
		result.removeContent();
		result = null;
		builder = null;
		source = null;
		res = null;
		Utility.freeMemory();
		return list;
	}

	private static Transformer getTransformer(String path, Map<String, String> param,
			VitamArgument config)
			throws TransformerConfigurationException {
		Transformer transformer = null;
		Templates templates = null;
		System.setProperty(TRANSFORMER_FACTORY,
				SAXON_TRANSFORMER_NAME);
		TransformerFactory tfactory = TransformerFactory.newInstance();
		templates = tfactory.newTemplates(new StreamSource(
				XmlSaxTools.class.getResourceAsStream(XSD2SCHEMA)));
		transformer = templates.newTransformer();
		transformer.setURIResolver(new VitamUriResolver(new String[] { BASE_RESOURCES,
				Utility.resourceToParent(XSD2SCHEMA), path }));
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		Object[] keys = param.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			transformer.setParameter(keys[i].toString(),
					param.get(keys[i]));
		}
		keys = null;
		templates = null;
		tfactory = null;
		System.setProperty(TRANSFORMER_FACTORY,
				DEFAULT_TRANSFORMER_NAME);
		return transformer;
	}

	private static List<String> setCombos(Document doc) {
		@SuppressWarnings("unchecked")
		List<Element> children = doc.getRootElement().getChildren();
		List<String> listRootElements = new ArrayList<String>();
		if (children.size() > 0) {
			for (Element child : children) {
				if (child.getName().equals("element") || child.getName().equals("complexType")) { //$NON-NLS-1$ //$NON-NLS-2$
					Attribute attr = child.getAttribute("name"); //$NON-NLS-1$
					listRootElements.add(attr.getValue());
				}
			}
		}
		children.clear();
		children = null;
		return listRootElements;
	}

}
