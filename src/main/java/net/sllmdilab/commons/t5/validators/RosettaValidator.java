package net.sllmdilab.commons.t5.validators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sllmdilab.commons.exceptions.RosettaInitializationException;
import net.sllmdilab.commons.exceptions.RosettaLookupException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class checks if a term code passed in OBX-3.2 exists in Rosetta table Usage: initialize per class by calling
 * loadRosetta()
 */

public class RosettaValidator {

	private DocumentBuilder docBuilder;
	private Transformer transformer;
	private XPath xPath;

	private Document docRosettaHarmonized;
	private Document docRosettaTable;
	private Document docRosettaUnits;

	private Map<String, Element> mapRosettaTerms;
	private Map<String, Element> mapHarmonizedRosetta;
	private Map<String, Element> mapRosettaUnits;
	private Map<String, String> mapHarmonizedRosettaDescription;
	private Map<String, String> mapHarmonizedRosettaUcumUnits;
	private Map<String, String> mapMdcUcumUnit;
	private Map<String, String> mapSynonyms;

	public RosettaValidator() throws RosettaInitializationException {
		loadRosetta("/rosetta_harmonized.xml", "/rosetta_terms.xml", "/rosetta_units.xml", true);
	}

	private void loadRosetta(String xmlFilePathHarmonized, String xmlFilePathTable, String xmlRosettaUnits,
			boolean forceReInit) {

		DocumentBuilderFactory DocFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuilder = DocFactory.newDocumentBuilder();

			TransformerFactory factory = TransformerFactory.newInstance();
			transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			XPathFactory xpathfactory = XPathFactory.newInstance();
			xPath = xpathfactory.newXPath();

			mapRosettaTerms = new HashMap<String, Element>();
			mapHarmonizedRosetta = new HashMap<String, Element>();
			mapRosettaUnits = new HashMap<String, Element>();
			mapHarmonizedRosettaDescription = new HashMap<String, String>();
			mapHarmonizedRosettaUcumUnits = new HashMap<String, String>();
			mapMdcUcumUnit = new HashMap<String, String>();
			mapSynonyms = new HashMap<String, String>();

			docRosettaTable = docBuilder.parse(RosettaValidator.class.getResourceAsStream(xmlFilePathTable));

			// Cache in the map
			String xpathExpr = "/Rosetta/term/REFID";
			NodeList refidNodes = null;
			refidNodes = (NodeList) xPath.evaluate(xpathExpr, docRosettaTable, XPathConstants.NODESET);

			if (refidNodes != null) {
				for (int i = 0; i < refidNodes.getLength(); i++) {
					Element elem = (Element) refidNodes.item(i);
					String REFID = elem.getTextContent();
					mapRosettaTerms.put(REFID, elem);
				}
			}

			docRosettaHarmonized = docBuilder.parse(RosettaValidator.class.getResourceAsStream(xmlFilePathHarmonized));

			// Cache in the map
			xpathExpr = "/HRTM/HRosetta/term/REFID";
			refidNodes = null;
			refidNodes = (NodeList) xPath.evaluate(xpathExpr, docRosettaHarmonized, XPathConstants.NODESET);
			if (refidNodes != null) {
				for (int i = 0; i < refidNodes.getLength(); i++) {
					Element elem = (Element) refidNodes.item(i);
					String REFID = elem.getTextContent();
					mapHarmonizedRosetta.put(REFID, elem);
				}
			}

			docRosettaUnits = docBuilder.parse(RosettaValidator.class.getResourceAsStream(xmlRosettaUnits));

			// Cache Units table in the map
			xpathExpr = "/RTM/Units/term/UOM_MDC";
			NodeList unitNodes = (NodeList) xPath.evaluate(xpathExpr, docRosettaUnits, XPathConstants.NODESET);
			if (unitNodes != null) {
				for (int i = 0; i < unitNodes.getLength(); i++) {
					Element elem = (Element) unitNodes.item(i);
					String uomMdc = elem.getTextContent();
					Element term = (Element) elem.getParentNode();
					if (term != null)
						mapRosettaUnits.put(uomMdc, term);
				}
			}

		} catch (SAXException | IOException | XPathExpressionException | TransformerConfigurationException
				| ParserConfigurationException e) {
			throw new RosettaInitializationException(e);
		}
	}

	/**
	 * Check if REFID is in Rosetta Harmonized table.
	 * 
	 * @param refid
	 * @return
	 */
	public boolean isInHarmonizedTable(String refid) {
		return mapHarmonizedRosetta.containsKey(refid);
	}

	/**
	 * Check if REFID is in Rosetta raw table.
	 * 
	 * @param refid
	 * @return
	 */
	public boolean isInTermsTable(String refid) {
		return mapRosettaTerms.containsKey(refid);
	}

	/**
	 * Get description in Harmonized table. If not found return empty string.
	 * 
	 * @param refid
	 * @return
	 * @throws RosettaLookupException
	 */
	public String getHarmonizedDescription(String refid) throws RosettaLookupException {
		String result = "";

		if (mapHarmonizedRosetta.containsKey(refid)) {

			if (mapHarmonizedRosettaDescription.containsKey(refid)) {
				result = mapHarmonizedRosettaDescription.get(refid);
			} else {

				// Cache in the map
				String xpathExpr = "/HRTM/HRosetta/term[REFID = \"" + refid + "\"]";
				Node node = null;
				try {
					node = (Node) xPath.evaluate(xpathExpr, docRosettaHarmonized, XPathConstants.NODE);
				} catch (XPathExpressionException e) {
					throw new RosettaLookupException(e);
				}
				if (node != null) {
					Element elem = (Element) ((Element) node).getElementsByTagName("Vendor_Description").item(0);
					result = elem.getTextContent();
				}

				mapHarmonizedRosettaDescription.put(refid, result);
			}
		}
		return result;
	}

	/**
	 * Get synonyms of the term look both ways - synonym of this term and if this term is synonym of the other term.
	 * UNION of those. If not found return empty string.
	 * 
	 * @param refid
	 * @return
	 * @throws RosettaLookupException
	 */
	public String getHarmonizedSynonym(String refid) throws RosettaLookupException {
		String result = "";

		if (mapSynonyms.containsKey(refid))
			result = mapSynonyms.get(refid);
		else {
			NodeList list;
			try {
			Element elemTerm = mapHarmonizedRosetta.get(refid);
			list = ((Element) elemTerm.getParentNode()).getElementsByTagName("Synonym");
			} catch(NullPointerException e) {
				return null;
			}

			if (list.getLength() > 0) {
				Element elemSyn = (Element) list.item(0);
				String refidSyn = elemSyn.getTextContent();
				if (refidSyn != null && !refidSyn.equals("")) {
					result = refidSyn;
				}
			}

			// Try to find synonyms other way
			if (result.equals("")) {
				// Cache in the map
				String xpathExpr = "/HRTM/HRosetta/term[Synonym = \"" + refid + "\"]/REFID";
				Node node = null;
				try {
					node = (Node) xPath.evaluate(xpathExpr, docRosettaHarmonized, XPathConstants.NODE);
				} catch (XPathExpressionException e) {
					throw new RosettaLookupException(e);
				}
				if (node != null) {
					result = node.getTextContent();
				}
			}
			// if synonym was not found, then "" is put in hash map, and next time even negative cases are cached, no
			// new XPath needed
			mapSynonyms.put(refid, result);
		}

		return result;
	}

	/**
	 * Get description in Harmonized table.
	 * 
	 * @param refid
	 * @return
	 * @throws RosettaLookupException
	 */
	public String getHarmonizedUCUMUnits(String refid) throws RosettaLookupException {
		String result = "";

		if (mapHarmonizedRosetta.containsKey(refid)) {

			if (mapHarmonizedRosettaUcumUnits.containsKey(refid)) {
				result = mapHarmonizedRosettaUcumUnits.get(refid);
			} else {
				// Cache in the map
				String xpathExpr = "/HRTM/HRosetta/term[REFID = \"" + refid + "\"]";
				Node node = null;
				try {
					node = (Node) xPath.evaluate(xpathExpr, docRosettaHarmonized, XPathConstants.NODE);
				} catch (XPathExpressionException e) {
					throw new RosettaLookupException(e);
				}

				if (node != null) {
					Element elem = (Element) ((Element) node).getElementsByTagName("UOM_UCUM").item(0);
					result = elem.getTextContent();
				}

				mapHarmonizedRosettaUcumUnits.put(refid, result);
			}
		}
		return result;
	}

	public String getUCUMUnit(String mdcUnit) throws RosettaLookupException {
		String result = "";

		if (mapMdcUcumUnit.containsKey(mdcUnit)) {
			result = mapMdcUcumUnit.get(mdcUnit);
		} else {
			if (mapRosettaUnits.containsKey(mdcUnit)) {
				Element elemTrem = mapRosettaUnits.get(mdcUnit);
				NodeList nl = elemTrem.getElementsByTagName("UOM_UCUM");

				if (nl == null || nl.getLength() == 0) {
					result = "n/a";
				} else {
					Element elemUcum = (Element) nl.item(0);
					result = elemUcum.getTextContent();
				}
			} else {
				result = "n/a";
			}

			// Cache in the map
			mapMdcUcumUnit.put(mdcUnit, result);
		}

		return result;
	}
}
