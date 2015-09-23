package net.sllmdilab.commons.converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sllmdilab.commons.exceptions.T5Exception;
import net.sllmdilab.commons.util.Constants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.primitive.DateTimeDt;

public class ObservationToT5XmlConverter {

	private static final String ELEMENT_UNIVERSAL_SERVICE_ID = "UniversalServiceID";
	private static final String ELEMENT_IDENTIFIER = "Identifier";
	private static final String ELEMENT_ORDER_OBSERVATIONS = "Order_Observations";
	private static final String ELEMENT_ORDER = "Order";
	private static final String ELEMENT_UNIT = "Unit";
	private static final String ELEMENT_METRIC = "Metric";
	private static final String ELEMENT_VALUE = "Value";
	private static final String ELEMENT_TIMESTAMP = "Timestamp";
	private static final String ELEMENT_OBS_IDENTIFIER = "ObsIdentifier";
	private static final String ELEMENT_OBSERVATION = "Observation";
	private static final String ELEMENT_CHAN = "CHAN";
	private static final String ELEMENT_VMD = "VMD";
	private static final String ELEMENT_MDS = "MDS";
	private static final String ELEMENT_PATIENT = "Patient";
	private static final String ELEMENT_PATIENT_RESULT = "Patient_Result";
	private static final String ELEMENT_SENDING_FACILITY = "Sending_Facility";
	private static final String ELEMENT_SENDING_APPLICATION = "Sending_Application";
	private static final String ELEMENT_PCD_01_MESSAGE = "PCD_01_Message";

	private static final String ATTRIBUTE_TIME_STAMP = "timeStamp";
	private static final String ATTRIBUTE_ID = "id";
	private static final String ATTRIBUTE_ID_UNIVERSAL_TYPE = "idUniversalType";
	private static final String ATTRIBUTE_ID_UNIVERSAL = "idUniversal";
	private static final String ATTRIBUTE_ID_LOCAL = "idLocal";
	private static final String ATTRIBUTE_UID = "uid";
	private static final String ATTRIBUTE_HIERARCHY = "hierarchy";
	private static final String ATTRIBUTE_INDEX = "index";
	private static final String ATTRIBUTE_CODING_SYSTEM_NAME = "codingSystemName";
	private static final String ATTRIBUTE_TYPE_HL7V2 = "typeHL7V2";

	public Document convertObservationToT5Xml(Observation obs) {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			doc.appendChild(createRootElement(obs, doc));

			return doc;
		} catch (ParserConfigurationException e) {
			throw new T5Exception(e);
		}
	}

	private Element createRootElement(Observation obs, Document doc) {
		Element root = doc.createElement(ELEMENT_PCD_01_MESSAGE);
		root.setAttribute(ATTRIBUTE_ID, generateMessageId());
		root.setAttribute(ATTRIBUTE_TIME_STAMP, getCurrentTimeStamp());

		root.appendChild(createSendingApplicationElement(obs, doc));
		root.appendChild(createSendingFacilityElement(obs, doc));
		root.appendChild(createPatientResultElement(obs, doc));

		return root;
	}

	private Element createSendingApplicationElement(Observation obs, Document doc) {
		Element sendingApplicationElement = doc.createElement(ELEMENT_SENDING_APPLICATION);
		sendingApplicationElement.setAttribute(ATTRIBUTE_ID_LOCAL, "FHIR_API");
		sendingApplicationElement.setAttribute(ATTRIBUTE_ID_UNIVERSAL, "");
		sendingApplicationElement.setAttribute(ATTRIBUTE_ID_UNIVERSAL_TYPE, "");

		return sendingApplicationElement;
	}

	private Element createSendingFacilityElement(Observation obs, Document doc) {
		Element sendingApplicationElement = doc.createElement(ELEMENT_SENDING_FACILITY);
		sendingApplicationElement.setAttribute(ATTRIBUTE_ID_LOCAL, "");
		sendingApplicationElement.setAttribute(ATTRIBUTE_ID_UNIVERSAL, "");
		sendingApplicationElement.setAttribute(ATTRIBUTE_ID_UNIVERSAL_TYPE, "");

		return sendingApplicationElement;
	}

	private Element createPatientResultElement(Observation obs, Document doc) {
		Element patientResultElement = doc.createElement(ELEMENT_PATIENT_RESULT);

		patientResultElement.appendChild(createPatientElement(obs, doc));
		patientResultElement.appendChild(createOrderObservationElement(obs, doc));

		return patientResultElement;
	}

	private Element createPatientElement(Observation obs, Document doc) {
		Element patientElement = doc.createElement(ELEMENT_PATIENT);

		patientElement.appendChild(createIdentifierElement(obs, doc));

		return patientElement;
	}

	private Element createIdentifierElement(Observation obs, Document doc) {
		Element patientElement = doc.createElement(ELEMENT_IDENTIFIER);
		patientElement.setTextContent(obs.getSubject().getReference().getValueAsString());

		return patientElement;
	}

	private Element createOrderObservationElement(Observation obs, Document doc) {
		Element orderObservationElement = doc.createElement(ELEMENT_ORDER_OBSERVATIONS);

		orderObservationElement.appendChild(createOrderElement(obs, doc));
		orderObservationElement.appendChild(createMdsElement(obs, doc));

		return orderObservationElement;
	}

	private Element createOrderElement(Observation obs, Document doc) {
		Element orderElement = doc.createElement(ELEMENT_ORDER);

		orderElement.setAttribute(ATTRIBUTE_TIME_STAMP, getFormattedAppliesTime(obs));

		orderElement.appendChild(createFillerOrderNumberElement(obs, doc));
		orderElement.appendChild(createUniversalServiceIDElement(obs, doc));

		return orderElement;
	}

	private Element createUniversalServiceIDElement(Observation obs, Document doc) {
		Element universalServiceIDElement = doc.createElement(ELEMENT_UNIVERSAL_SERVICE_ID);

		universalServiceIDElement.setAttribute(ATTRIBUTE_CODING_SYSTEM_NAME, "T5");

		universalServiceIDElement.setTextContent("mobile monitoring");

		return universalServiceIDElement;
	}

	private String getFormattedAppliesTime(Observation obs) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.ISO_DATE_FORMAT).withZone(
				ZoneId.of("UTC"));

		DateTimeDt dateValue = (DateTimeDt) obs.getApplies();

		ZoneId zoneId = (dateValue.getTimeZone() != null) ? dateValue.getTimeZone().toZoneId() : ZoneId.of("UTC");

		ZonedDateTime date = ZonedDateTime.ofInstant(dateValue.getValue().toInstant(), zoneId);

		return date.format(dateTimeFormatter);
	}

	public Element createFillerOrderNumberElement(Observation obs, Document doc) {
		return doc.createElement("FillerOrderNumber");
	}

	private Element createMdsElement(Observation obs, Document doc) {
		Element mdsElement = doc.createElement(ELEMENT_MDS);

		mdsElement.setAttribute(ATTRIBUTE_INDEX, "1");

		mdsElement.appendChild(createVmdElement(obs, doc));

		return mdsElement;
	}

	private Element createVmdElement(Observation obs, Document doc) {
		Element vmdElement = doc.createElement(ELEMENT_VMD);

		vmdElement.setAttribute(ATTRIBUTE_INDEX, "1");

		vmdElement.appendChild(createChanElement(obs, doc));

		return vmdElement;
	}

	private Element createChanElement(Observation obs, Document doc) {
		Element chanElement = doc.createElement(ELEMENT_CHAN);
		chanElement.setAttribute(ATTRIBUTE_INDEX, "1");

		chanElement.appendChild(createMetricElement(obs, doc));

		return chanElement;
	}

	private Element createMetricElement(Observation obs, Document doc) {
		Element metricElement = doc.createElement(ELEMENT_METRIC);
		metricElement.setAttribute(ATTRIBUTE_INDEX, "1");

		metricElement.appendChild(createObservationElement(obs, doc));

		return metricElement;
	}

	private Element createObservationElement(Observation obs, Document doc) {
		Element observationElement = doc.createElement(ELEMENT_OBSERVATION);

		observationElement.setAttribute(ATTRIBUTE_HIERARCHY, "1.1.1.1");
		observationElement.setAttribute(ATTRIBUTE_INDEX, "1");
		observationElement.setAttribute(ATTRIBUTE_UID, obs.getId().getValueAsString());

		observationElement.appendChild(createObsIdentifierElement(obs, doc));
		observationElement.appendChild(createValueElement(obs, doc));
		observationElement.appendChild(createUnitElement(obs, doc));
		observationElement.appendChild(createTimestampElement(obs, doc));

		return observationElement;
	}

	private Element createObsIdentifierElement(Observation obs, Document doc) {
		Element obsIdentifierElement = doc.createElement(ELEMENT_OBS_IDENTIFIER);

		obsIdentifierElement.setAttribute(ATTRIBUTE_CODING_SYSTEM_NAME, obs.getCode().getCodingFirstRep().getSystem());
		obsIdentifierElement.setTextContent(obs.getCode().getCodingFirstRep().getCode());

		return obsIdentifierElement;
	}

	private Element createTimestampElement(Observation obs, Document doc) {
		Element timestampElement = doc.createElement(ELEMENT_TIMESTAMP);

		timestampElement.setTextContent(getFormattedAppliesTime(obs));

		return timestampElement;
	}

	private Element createValueElement(Observation obs, Document doc) {
		Element valueElement = doc.createElement(ELEMENT_VALUE);

		QuantityDt quantity = (QuantityDt) obs.getValue();

		valueElement.setTextContent(quantity.getValue().toPlainString());
		valueElement.setAttribute(ATTRIBUTE_TYPE_HL7V2, "NM");

		return valueElement;
	}

	private Element createUnitElement(Observation obs, Document doc) {
		Element unitElement = doc.createElement(ELEMENT_UNIT);

		unitElement.setAttribute(ATTRIBUTE_CODING_SYSTEM_NAME, obs.getCode().getCodingFirstRep().getSystem());

		QuantityDt quantity = (QuantityDt) obs.getValue();
		unitElement.setTextContent(quantity.getCode());

		return unitElement;
	}

	private String generateMessageId() {
		return UUID.randomUUID().toString();
	}

	private String getCurrentTimeStamp() {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.ISO_DATE_FORMAT);
		return dateTimeFormatter.format(LocalDateTime.now());
	}
}
