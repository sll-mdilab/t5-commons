package net.sllmdilab.commons.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sllmdilab.commons.converter.ObservationToT5XmlConverter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.DecimalDt;

public class ObservationToT5XmlConverterTest {

	private static final String MOCK_UNIVERSAL_SERVICE_ID_CODING_SYSTEM = "T5";
	private static final String MOCK_UNIVERSAL_SERVICE_ID = "mobile monitoring";
	private static final String MOCK_TIMESTAMP_ZONED = "2015-03-26T16:32:43.000Z";
	private static final String MOCK_TIMESTAMP = "2015-03-26T16:32:43.000";
	private static final String MOCK_VALUE = "1337.42";
	private static final String MOCK_ID = "id3526347";
	private static final String MOCK_CODE = "MDC_MOCK_CODE";
	private static final String MOCK_SYSTEM = "MDC";
	private static final String MOCK_UNIT_CODE = "MDC_MOCK_UNIT";
	private static final String MOCK_PATIENT_ID = "121212-1212";
	
	private XPath xPath;
	private ObservationToT5XmlConverter converter;

	@Before
	public void init() throws Exception {
		converter = new ObservationToT5XmlConverter();
		xPath = XPathFactory.newInstance().newXPath();
	}

	@Test
	public void pointIsConverted() throws Exception {
		Observation observation = new Observation();

		DateTimeDt applies = new DateTimeDt(MOCK_TIMESTAMP_ZONED);
		observation.setEffective(applies);

		observation.setId(MOCK_ID);
		observation.setCode(new CodeableConceptDt(MOCK_SYSTEM, MOCK_CODE));

		QuantityDt quantity = new QuantityDt();
		quantity.setValue(new DecimalDt(MOCK_VALUE));
		quantity.setCode(MOCK_UNIT_CODE);
		observation.setValue(quantity);

		observation.getSubject().setReference(MOCK_PATIENT_ID);

		Document document = converter.convertObservationToT5Xml(observation);

		Element root = document.getDocumentElement();

		assertXpExists("/PCD_01_Message", root);
		assertXpNotEmpty("/PCD_01_Message/@id", root);
		assertXpNotEmpty("/PCD_01_Message/@timeStamp", root);

		assertXpExists("/PCD_01_Message/Sending_Application", root);

		assertXpExists("/PCD_01_Message/Sending_Facility", root);

		assertXpExists("/PCD_01_Message/Patient_Result", root);
		assertXpExists("/PCD_01_Message/Patient_Result/Patient", root);
		assertXpEquals(MOCK_PATIENT_ID, "/PCD_01_Message/Patient_Result/Patient/Identifier", root);

		assertXpExists("/PCD_01_Message/Patient_Result/Order_Observations", root);
		assertXpExists("/PCD_01_Message/Patient_Result/Order_Observations/Order", root);

		assertXpEquals(MOCK_TIMESTAMP, "/PCD_01_Message/Patient_Result/Order_Observations/Order/@timeStamp", root);
		assertXpEquals(MOCK_UNIVERSAL_SERVICE_ID_CODING_SYSTEM,
				"/PCD_01_Message/Patient_Result/Order_Observations/Order/UniversalServiceID/@codingSystemName", root);
		assertXpEquals(MOCK_UNIVERSAL_SERVICE_ID,
				"/PCD_01_Message/Patient_Result/Order_Observations/Order/UniversalServiceID", root);

		assertXpExists("/PCD_01_Message/Patient_Result/Order_Observations/MDS/VMD/CHAN/Metric/Observation", root);

		assertXpEquals("1", "/PCD_01_Message/Patient_Result/Order_Observations/MDS/@index", root);

		assertXpEquals("1.1.1.1",
				"/PCD_01_Message/Patient_Result/Order_Observations/MDS/VMD/CHAN/Metric/Observation/@hierarchy", root);
		assertXpEquals(MOCK_ID,
				"/PCD_01_Message/Patient_Result/Order_Observations/MDS/VMD/CHAN/Metric/Observation/@uid", root);

		assertXpEquals(MOCK_CODE,
				"/PCD_01_Message/Patient_Result/Order_Observations/MDS/VMD/CHAN/Metric/Observation/ObsIdentifier", root);
		assertXpEquals(
				MOCK_SYSTEM,
				"/PCD_01_Message/Patient_Result/Order_Observations/MDS/VMD/CHAN/Metric/Observation/ObsIdentifier/@codingSystemName",
				root);
		assertXpEquals(MOCK_TIMESTAMP,
				"/PCD_01_Message/Patient_Result/Order_Observations/MDS/VMD/CHAN/Metric/Observation/Timestamp", root);
		assertXpEquals(MOCK_VALUE,
				"/PCD_01_Message/Patient_Result/Order_Observations/MDS/VMD/CHAN/Metric/Observation/Value", root);
		assertXpEquals(
				MOCK_SYSTEM,
				"/PCD_01_Message/Patient_Result/Order_Observations/MDS/VMD/CHAN/Metric/Observation/Unit/@codingSystemName",
				root);
		assertXpEquals(MOCK_UNIT_CODE,
				"/PCD_01_Message/Patient_Result/Order_Observations/MDS/VMD/CHAN/Metric/Observation/Unit", root);
	}

	private void assertXpEquals(String expected, String xPath, Element element) throws XPathExpressionException {
		assertEquals(expected, xp(xPath, element));
	}

	private void assertXpNotEmpty(String xPath, Element element) throws XPathExpressionException {
		String value = xp(xPath, element);

		assertNotNull("XPath " + xPath + " did not exist.", StringUtils.isBlank(value));
	}

	private void assertXpExists(String expression, Element element) throws XPathExpressionException {
		assertNotNull("Node " + expression + " did not exist.",
				xPath.evaluate(expression, element, XPathConstants.NODE));
	}

	private String xp(String expression, Element element) throws XPathExpressionException {
		return (String) xPath.evaluate(expression, element, XPathConstants.STRING);
	}

}
