package net.sllmdilab.commons.converter;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sllmdilab.commons.exceptions.XmlParsingException;
import net.sllmdilab.commons.t5.validators.RosettaValidator;
import net.sllmdilab.commons.util.T5FHIRUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.SampledDataDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.model.primitive.StringDt;

public class T5QueryToFHIRConverter {

	private static Logger logger = LoggerFactory.getLogger(T5QueryToFHIRConverter.class);

	private RosettaValidator rosettaValidator;

	public T5QueryToFHIRConverter(RosettaValidator rosettaValidator) {
		this.rosettaValidator = rosettaValidator;
	}

	public List<Observation> convertToObservations(String idPatient, String idPerformer, String idObservationCode,
			String idObservationCodeSystem, Document xmlXQueryResponse) {

		// Read in an map Observation elements, and then sort by timestamp
		HashMap<Date, Element> mapPoints = new HashMap<Date, Element>();
		NodeList listPoints = xmlXQueryResponse.getElementsByTagName("point");
		for (int i = 0; i < listPoints.getLength(); i++) {
			Element elemPoint = (Element) listPoints.item(i);
			Date ts = T5FHIRUtils.xmlDateTimeToDate(elemPoint.getAttribute("time"));

			mapPoints.put(ts, elemPoint);
		}
		Set<Date> setTS = mapPoints.keySet();
		ArrayList<Date> listTS = new ArrayList<>(setTS);
		// Sort timestamps
		Collections.sort(listTS);

		ArrayList<Observation> trendPointsObsList = new ArrayList<>();

		// Code is the observation code
		CodeableConceptDt codeConcept = new CodeableConceptDt(idObservationCodeSystem, idObservationCode);
		try {
			codeConcept.setText(rosettaValidator.getHarmonizedDescription(idObservationCode));
		} catch (Exception e) {
			codeConcept.setText("Description of the code not available");
		}

		long totalRosettamillis = 0;

		// Cache the unit
		String unitUCUM = "";
		try {
			unitUCUM = rosettaValidator.getHarmonizedUCUMUnits(idObservationCode);
		} catch (Exception e) {
			logger.warn("Unit not available for " + idObservationCode);
		}

		for (int i = 0; i < listTS.size(); i++) {
			Observation obs = new Observation();
			trendPointsObsList.add(obs);
			obs.setCode(codeConcept);

			if (idPatient != null) {
				obs.getSubject().setReference("Patient/" + idPatient);
			}

			if (idPerformer != null) {
				obs.addPerformer().setReference("Device/" + idPerformer);
			}

			Element elem = mapPoints.get(listTS.get(i));

			String uid = elem.getAttribute("uid");
			obs.setId(uid);

			obs.setValue(parseValue(elem, unitUCUM));
			obs.setApplies(parseTime(elem));
		}

		logger.debug("Getting harmonized units took " + totalRosettamillis + " ms.");

		return trendPointsObsList;
	}

	private DateTimeDt parseTime(Element elem) {
		String strTime = elem.getAttribute("time");
		DateTimeDt obsTS = new DateTimeDt(T5FHIRUtils.xmlDateTimeToDate(strTime));
		obsTS.setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")));
		return obsTS;
	}

	private IDatatype parseValue(Element elem, String unitUCUM) {
		String value = elem.getAttribute("value");
		String sampleRate = elem.getAttribute("sampleRate");
		String unit = elem.getAttribute("unit");
		String dataRange = elem.getAttribute("dataRange");

		if (isNumeric(value)) {
			return parseScalarValue(value, unit, unitUCUM);
		} else if (value.startsWith("NA")) {
			return parseNumericArrayValue(value, sampleRate, unit, dataRange);
		} else {
			return parseStringValue(value);
		}
	}

	private QuantityDt parseScalarValue(String value, String unit, String unitUCUM) {
		QuantityDt quantity = new QuantityDt();

		quantity.setValue(Double.parseDouble(value));

		if (StringUtils.isBlank(unitUCUM)) {
			quantity.setUnits(unit);
		} else {
			quantity.setUnits(unitUCUM);
		}

		return quantity;
	}

	private StringDt parseStringValue(String value) {
		return new StringDt(value);
	}

	private SampledDataDt parseNumericArrayValue(String samples, String sampleRate, String unitCode, String dataRange) {
		SampledDataDt sampledData = new SampledDataDt();

		setSampleData(samples, sampledData);

		setPeriod(sampleRate, sampledData);

		setOrigin(unitCode, sampledData);

		setRange(dataRange, sampledData);

		return sampledData;
	}

	private void setRange(String dataRange, SampledDataDt sampledData) {
		Pattern pattern = Pattern.compile("NR\\[(.*)\\]");
		Matcher matcher = pattern.matcher(dataRange);
		if (!matcher.matches()) {
			throw new XmlParsingException("Unable to parse numeric array.");
		}
		String[] range = matcher.group(1).split("\\^");
		if (range.length != 2) {
			throw new XmlParsingException("Data range contains " + range.length + " values, should contain 2");
		}
		sampledData.setLowerLimit(Double.parseDouble(range[0]));
		sampledData.setUpperLimit(Double.parseDouble(range[1]));
	}

	private void setOrigin(String unitCode, SampledDataDt sampledData) {
		QuantityDt origin = new QuantityDt(0.0);
		origin.setUnits(unitCode);
		sampledData.setOrigin(origin);
	}

	private void setPeriod(String sampleRate, SampledDataDt sampledData) {
		if (!StringUtils.isBlank(sampleRate) && isNumeric(sampleRate)) {
			double dblSampleRate = Double.parseDouble(sampleRate);
			if (dblSampleRate > 0) {
				sampledData.setPeriod(sampleRateToPeriod(Double.parseDouble(sampleRate)));
			}
		}
	}

	private double sampleRateToPeriod(double sampleRate) {
		return 1000.0 / sampleRate;
	}

	private void setSampleData(String valueString, SampledDataDt sampledData) {
		Pattern pattern = Pattern.compile("NA\\[(.*)\\]");
		Matcher matcher = pattern.matcher(valueString);
		if (!matcher.matches()) {
			throw new XmlParsingException("Unable to parse numeric array.");
		}
		sampledData.setData(matcher.group(1).replaceAll("\\^", " "));
	}

	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public List<Observation> convertToObservationSummary(Document xmlXQueryResponse) {

		InstantDt now = new InstantDt();
		now.setValue(new Date(System.currentTimeMillis()));

		ArrayList<Observation> summaryList = new ArrayList<Observation>();

		NodeList listNames = xmlXQueryResponse.getElementsByTagName("ObsName");
		for (int i = 0; i < listNames.getLength(); i++) {
			Element elem = (Element) listNames.item(i);
			String obsName = elem.getTextContent();
			// Code is the observation code
			CodeableConceptDt codeConcept = new CodeableConceptDt("MDC", obsName);
			try {
				codeConcept.setText(rosettaValidator.getHarmonizedDescription(obsName));
			} catch (Exception e) {
				codeConcept.setText("Description of the name not available");
			}

			Observation obs = new Observation();
			obs.setCode(codeConcept);
			obs.setId(T5FHIRUtils.generateUniqueId());
			summaryList.add(obs);
		}

		return summaryList;
	}

	public List<Patient> convertToPatientSummary(Document xmlXQueryResponse) {
		ArrayList<Patient> summaryList = new ArrayList<>();
		NodeList listPid = xmlXQueryResponse.getElementsByTagName("PID");

		for (int i = 0; i < listPid.getLength(); i++) {
			Element elem = (Element) listPid.item(i);
			String strPid = elem.getTextContent();

			Patient patient = new Patient();
			summaryList.add(patient);
			patient.addIdentifier().setValue(strPid);
			patient.setId(strPid);
		}

		return summaryList;
	}
}
