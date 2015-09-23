package net.sllmdilab.commons.converter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sllmdilab.commons.converter.T5QueryToFHIRConverter;
import net.sllmdilab.commons.t5.validators.RosettaValidator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.SampledDataDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.parser.IParser;

@RunWith(MockitoJUnitRunner.class)
public class T5QueryToFHIRConverterTest {
	private static final String MOCK_PATIENT_RESOURCE_PREFIX = "Patient/";
	private static final String MOCK_MDC_CODE = "MDC_RESP_RATE";
	private static final String MOCK_UNIT = "MDC_DIM_RESP_PER_MIN";
	private static final String MOCK_DEFAULT_CODE_SYSTEM = "MDC";
	private static final String MOCK_PATIENT_ID = "1912-121212";
	private static final String MOCK_UID = "acbf4228-06ea-4c57-a4c5-3e9a2fa9d0fb";
	private static final String MOCK_DEVICE_ID = "agewg32342tkggjwlek3245";
	private static final String MOCK_DATE = "2015-02-17T13:35:49.403Z";
	private static final String MOCK_VALUE = "40.5";
	private static final String MOCK_DATE2 = "2015-02-17T13:35:44.405Z";
	private static final String MOCK_VALUE2 = "44.2";
	private static final String MOCK_MDC_PID = "010101-2425";
	private static final String MOCK_WAVEFORM_DATA ="NA[0.0^0.1^0.2^0.3]";
	private static final String MOCK_WAVEFORM_DATA_FHIR ="0.0 0.1 0.2 0.3";
	private static final String MOCK_WAVEFORM_UNIT ="MDC_DIM_MILLI_VOLT";
	private static final String MOCK_WAVEFORM_SAMPLE_RATE ="4";

	private static final String MOCK_RESPONSE = "<trend>" + "<point uid=\"" + MOCK_UID + "\"" + " time=\"" + MOCK_DATE
			+ "\" value=\"" + MOCK_VALUE + "\" unit=\"" + MOCK_UNIT + "\" unitSystem = \"" + MOCK_DEFAULT_CODE_SYSTEM
			+ "\"></point>" + "<point time=\"" + MOCK_DATE2 + "\" value=\"" + MOCK_VALUE2 + "\" unit=\"" + MOCK_UNIT
			+ "\" unitSystem = \"" + MOCK_DEFAULT_CODE_SYSTEM + "\"></point>" + "</trend>";
	
	private static final String MOCK_WAVEFORM_RESPONSE = "<trend>" + "<point uid=\"" + MOCK_UID + "\"" + " time=\"" + MOCK_DATE
			+ "\" value=\"" + MOCK_VALUE + "\" unit=\"" + MOCK_UNIT + "\" unitSystem = \"" + MOCK_DEFAULT_CODE_SYSTEM
			+ "\"></point>" + "<point time=\"" + MOCK_DATE2 + "\" value=\"" + MOCK_WAVEFORM_DATA + "\" unit=\"" + MOCK_WAVEFORM_UNIT
			+ "\" unitSystem = \"" + MOCK_DEFAULT_CODE_SYSTEM + "\" sampleRate=\""+  MOCK_WAVEFORM_SAMPLE_RATE +  "\"></point>" + "</trend>";

	private static final String TEST_XML_OBS_NAMES = "<ObsTypes>" + "<ObsName>MDC_RESP_RATE</ObsName>"
			+ "<ObsName>MDC_ECG_CARD_BEAT_RATE</ObsName>" + "</ObsTypes>";

	private static final String TEST_XML_PIDS = "<List><PID>010101-2425</PID><PID>19121212-1212</PID></List>";

	private DocumentBuilderFactory dbf;
	private DocumentBuilder documentBuilder;

	@Spy
	private RosettaValidator mockRosettaValidator;

	@InjectMocks
	private T5QueryToFHIRConverter converter;

	@Before
	public void init() throws Exception {
		
		dbf = DocumentBuilderFactory.newInstance();
		documentBuilder = dbf.newDocumentBuilder();

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void pointIsConverted() throws Exception {
		List<Observation> listObservations = converter.convertToObservations(MOCK_PATIENT_ID, MOCK_DEVICE_ID, MOCK_MDC_CODE,
				MOCK_DEFAULT_CODE_SYSTEM, xmlStringToDocument(MOCK_RESPONSE));

		for (int i = 0; i < listObservations.size(); i++) {
			Observation observation = (Observation) listObservations.get(i);
			IParser p = new FhirContext().newXmlParser().setPrettyPrint(true);
			String messageString = p.encodeResourceToString(observation);
			System.out.println(messageString);
		}

		Observation observation = (Observation) listObservations.get(0);
		DateTimeDt dt = (DateTimeDt) observation.getApplies();
		assertEquals(ZonedDateTime.parse(MOCK_DATE2, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli(),
				dt.getValue().toInstant().toEpochMilli());

		assertEquals(MOCK_MDC_CODE, observation.getCode().getCoding().get(0).getCode());
		assertEquals(MOCK_DEFAULT_CODE_SYSTEM, observation.getCode().getCoding().get(0).getSystem());
		assertEquals(MOCK_VALUE2, ((QuantityDt) observation.getValue()).getValue().toPlainString());
		assertEquals(MOCK_PATIENT_RESOURCE_PREFIX + MOCK_PATIENT_ID, observation.getSubject().getReference().getValueAsString());
		assertEquals("Device/" + MOCK_DEVICE_ID, observation.getPerformer().get(0).getReference().getValueAsString());
	}
	
	@Test
	public void waveformIsConverted() throws Exception {
		List<Observation> listObservations = converter.convertToObservations(MOCK_PATIENT_ID, MOCK_DEVICE_ID, MOCK_MDC_CODE,
				MOCK_DEFAULT_CODE_SYSTEM, xmlStringToDocument(MOCK_WAVEFORM_RESPONSE));

		for (int i = 0; i < listObservations.size(); i++) {
			Observation observation = (Observation) listObservations.get(i);
			IParser p = new FhirContext().newXmlParser().setPrettyPrint(true);
			String messageString = p.encodeResourceToString(observation);
			System.out.println(messageString);
		}

		Observation observation = (Observation) listObservations.get(0);
		DateTimeDt dt = (DateTimeDt) observation.getApplies();
		assertEquals(ZonedDateTime.parse(MOCK_DATE2, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli(),
				dt.getValue().toInstant().toEpochMilli());

		assertEquals(MOCK_MDC_CODE, observation.getCode().getCoding().get(0).getCode());
		assertEquals(MOCK_DEFAULT_CODE_SYSTEM, observation.getCode().getCoding().get(0).getSystem());
		
		SampledDataDt sampledData = ((SampledDataDt) observation.getValue());
		assertEquals(MOCK_WAVEFORM_DATA_FHIR, sampledData.getData());
		assertEquals(250.0, sampledData.getPeriod().doubleValue(), 0.00001);
		assertEquals(MOCK_WAVEFORM_UNIT, sampledData.getOrigin().getUnits());
		
		assertEquals(MOCK_PATIENT_RESOURCE_PREFIX + MOCK_PATIENT_ID, observation.getSubject().getReference().getValueAsString());
		assertEquals("Device/" + MOCK_DEVICE_ID, observation.getPerformer().get(0).getReference().getValueAsString());
	}

	@Test
	public void obsSummaryIsConverted() throws Exception {

		List<Observation> listSummary = converter.convertToObservationSummary(xmlStringToDocument(TEST_XML_OBS_NAMES));
		for (int i = 0; i < listSummary.size(); i++) {
			Observation observation = (Observation) listSummary.get(i);
			IParser p = new FhirContext().newXmlParser().setPrettyPrint(true);
			String messageString = p.encodeResourceToString(observation);
			System.out.println(messageString);
		}

		Observation observation = (Observation) listSummary.get(0);
		assertEquals(MOCK_MDC_CODE, observation.getCode().getCoding().get(0).getCode());
	}

	@Test
	public void patientSummaryIsConverted() throws Exception {

		List<Patient> listSummary = converter.convertToPatientSummary(xmlStringToDocument(TEST_XML_PIDS));
		for (int i = 0; i < listSummary.size(); i++) {
			Patient patient = (Patient) listSummary.get(i);
			IParser p = new FhirContext().newXmlParser().setPrettyPrint(true);
			String messageString = p.encodeResourceToString(patient);
			System.out.println(messageString);
		}

		Patient patient = (Patient) listSummary.get(0);
		assertEquals(MOCK_MDC_PID, patient.getIdentifierFirstRep().getValue().toString());
	}

	private Document xmlStringToDocument(String xmlString) throws SAXException, IOException {
		return documentBuilder.parse(new InputSource(new StringReader(xmlString)));
	}
}
