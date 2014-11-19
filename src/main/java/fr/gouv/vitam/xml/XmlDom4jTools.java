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
   along with VITAM .  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gouv.vitam.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.XMLWriter;

import fr.gouv.vitam.commons.VitamArgument;

/**
 * @author "Frederic Bregier"
 *
 */
public class XmlDom4jTools {
	public static final DocumentFactory factory = DocumentFactory.getInstance();

	public final static void removeAllNamespaces(Document doc) {
		Element root = doc.getRootElement();
		Namespace namespace = root.getNamespace();
		if (namespace != Namespace.NO_NAMESPACE) {
			root.remove(namespace);
			removeNamespaces(root.content());
		}
	}

	public final static void unfixNamespaces(Document doc, Namespace original) {
		Element root = doc.getRootElement();
		if (original != null) {
			setNamespaces(root.content(), original);
		}
	}

	private final static void setNamespace(Element elem, Namespace ns) {
		elem.setQName(QName.get(elem.getName(), ns,
				elem.getQualifiedName()));
	}

	/**
	 * Recursively removes the namespace of the element and all its children: sets to
	 * Namespace.NO_NAMESPACE
	 */
	public final static void removeNamespaces(Element elem) {
		setNamespaces(elem, Namespace.NO_NAMESPACE);
	}

	/**
	 * Recursively removes the namespace of the list and all its children: sets to
	 * Namespace.NO_NAMESPACE
	 */
	private final static void removeNamespaces(List<?> l) {
		setNamespaces(l, Namespace.NO_NAMESPACE);
	}

	/**
	 * Recursively sets the namespace of the element and all its children.
	 */
	private final static void setNamespaces(Element elem, Namespace ns) {
		setNamespace(elem, ns);
		setNamespaces(elem.content(), ns);
	}

	/**
	 * Recursively sets the namespace of the List and all children if the current namespace is match
	 */
	private final static void setNamespaces(List<?> l, Namespace ns) {
		Node n = null;
		for (int i = 0; i < l.size(); i++) {
			n = (Node) l.get(i);

			if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
				Namespace namespace = ((Attribute) n).getNamespace();
				if (!namespace.equals(ns)) {
					((Attribute) n).setNamespace(ns);
				}
			}
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Namespace namespace = ((Element) n).getNamespace();
				if (!namespace.equals(ns)) {
					if (ns.equals(Namespace.NO_NAMESPACE)) {
						((Element) n).remove(namespace);
					}
					setNamespaces((Element) n, ns);
				}
			}
		}
	}

	public final static Element fillInformation(Element parent, String name, String attribut, String info) {
		Element element = null;
		element = factory.createElement(name);
		element.addAttribute(attribut, info);
		parent.add(element);
		return element;
	}

	public final static void addElement(XMLWriter writer,
			Element parent, Element sub) {
		if (sub != null) {
			parent.add(sub);
		}
	}

	public final static void addAttribute(Element element, String attribut, String info) {
		element.addAttribute(attribut, info);
	}

	public final static Element initializeCheck(VitamArgument argument, File current_file) {
		Element root = null;
		root = factory.createElement("vitam");
		argument.setXmlInDom4J(factory.createDocument(root));
		Element element = factory.createElement("xmlfile");
		element.addAttribute("file", current_file.getAbsolutePath());
		root.add(element);
		return root;
	}

	public final static Element finalizeOneCheck(VitamArgument result,
			File current_file, String number) {
		Element root = null;
		root = result.getXmlInDom4J().getRootElement();
		return root;
	}

	public final static void addDate(VitamArgument argument, Element root) {
		addAttribute(root, "date", argument.dateFormat.format(new Date()));
	}

	public final static void removeEmptyDocument(Document doc) {
		@SuppressWarnings("unchecked")
		Iterator<Node> nodes = doc.nodeIterator();
		List<Attribute> toremove = new ArrayList<>();
		while (nodes.hasNext()) {
			Node node = (Node) nodes.next();
			if (node instanceof Element) {
				removeEmptyElement((Element) node);
			} else if (node instanceof Attribute) {
				if (((Attribute) node).getValue().length() == 0) {
					toremove.add(((Attribute) node));
				}
				//removeEmptyAttribute((Attribute) node);
			}
		}
		for (Attribute attribute : toremove) {
			doc.remove(attribute);
		}
		toremove.clear();
	}

	public final static void removeEmptyAttribute(Attribute attrib) {
		if (attrib.getValue().length() == 0) {
			attrib.detach();
		}
	}

	public final static void removeEmptyElement(Element root) {
		// look first at attribute
		if (root.attributeCount() > 0) {
			@SuppressWarnings("unchecked")
			Iterator<Attribute> attribs = root.attributeIterator();
			List<Attribute> toremove = new ArrayList<>();
			while (attribs.hasNext()) {
				Attribute attribute = (Attribute) attribs.next();
				if (attribute.getValue().length() == 0) {
					toremove.add(attribute);
				}
				//removeEmptyAttribute(attribute);
			}
			for (Attribute attribute : toremove) {
				root.remove(attribute);
			}
			toremove.clear();
		}
		@SuppressWarnings("unchecked")
		Iterator<Element> elements = root.elementIterator();
		List<Element> toremove = new ArrayList<>();
		while (elements.hasNext()) {
			Element elt = (Element) elements.next();
			// look at its descendant
			removeEmptyElement(elt);
			if (elt.attributeCount() > 0) {
				continue;
			}
			if (elt.hasContent()) {
				continue;
			}
			toremove.add(elt);
			//elt.detach();
		}
		for (Element element : toremove) {
			root.remove(element);
		}
		toremove.clear();
	}
	/**
	 * 
	 * @param source
	 *            what we are going to add
	 * @param target
	 *            where we are going to add
	 */
	public static final void checkPresence(Element source, Element target) {
		if (target.selectSingleNode("./" + source.getName()) != null) {
			// node already there so checking sub node if any
			@SuppressWarnings("unchecked")
			List<Element> listElements = source.elements();
			@SuppressWarnings("unchecked")
			List<Attribute> listAttributes = source.attributes();
			Element newSource = (Element) target.selectSingleNode("./" + source.getName());
			if (newSource != null) {
				for (Attribute attribute : listAttributes) {
					checkPresence(attribute, newSource);
				}
				for (Element element : listElements) {
					checkPresence(element, newSource);
				}
			}
		} else {
			source.detach();
			target.add(source);
		}
	}

	/**
	 * 
	 * @param source
	 *            what we are going to add
	 * @param target
	 *            where we are going to add
	 */
	public static final void checkPresence(Attribute source, Element target) {
		if (target.selectSingleNode("./@" + source.getName()) != null) {
			// not ignoring this attribute
			source.detach();
			target.add(source);
		}
	}

}
