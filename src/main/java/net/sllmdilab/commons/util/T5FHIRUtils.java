package net.sllmdilab.commons.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;

public class T5FHIRUtils {
	public static final String SP_API_KEY = "api_key";
	public static final String DEFAULT_CODE_SYSTEM = "MDC";

	private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");

	public static Date getStartTimeFromNullableRange(DateRangeParam dateRange) {
		Date startTime;

		if (dateRange != null) {
			startTime = dateRange.getLowerBoundAsInstant();
			if (startTime != null) {
				return startTime;
			}
		}

		return new Date(0);
	}

	public static Date getEndTimeFromNullableRange(DateRangeParam dateRange) {
		Date startTime;

		if (dateRange != null) {
			startTime = dateRange.getUpperBoundAsInstant();
			if (startTime != null) {
				return startTime;
			}
		}

		return new Date();
	}

	public static String getValueOrNull(TokenParam param) {
		return (param == null) ? null : param.getValue();
	}

	public static String getValueOrNull(StringParam param) {
		return (param == null) ? null : param.getValue();
	}

	public static String generateObservationId() {
		return generateUniqueId();
	}

	public static String convertDateToXMLType(Date date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.ISO_DATE_FORMAT).withZone(
				ZoneId.of("UTC"));

		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));

		return zonedDateTime.format(dateTimeFormatter);
	}

	public static void verifyApiKey(String actualApiKey, String inputApiKey) {
		if (!StringUtils.isBlank(actualApiKey)) {
			if (!actualApiKey.equals(inputApiKey)) {
				throw new AuthenticationException("Incorrect API key.");
			}
		}
	}

	public static Date xmlDateTimeToDate(String strTime) {
		Calendar calendar = DatatypeConverter.parseDateTime(strTime);
		calendar.setTimeZone(utcTimeZone);
		return calendar.getTime();
	}

	public static String xmlToString(Document xmlDocument) throws TransformerException, UnsupportedEncodingException {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		DOMSource source = new DOMSource(xmlDocument);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(stream);

		transformer.transform(source, result);

		byte[] bytes = stream.toByteArray();
		return new String(bytes, "UTF-8");
	}

	public static String generateUniqueId() {
		return UUID.randomUUID().toString();
	}
	
	public static boolean hasId(IResource resource) {
		return resource.getId() != null && !resource.getId().isEmpty();
	}
	
	public static String convertDateToHL7Type(Date date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSZ").withZone(
				ZoneId.of("UTC"));

		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));

		return zonedDateTime.format(dateTimeFormatter);
	}
	
	public static Date convertHL7DateTypeToDate(String hl7DateStr) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSS[X]");
		// If no time zone, set UTC
		if(!includesTimeZone(hl7DateStr)){
			dateTimeFormatter = dateTimeFormatter.withZone(ZoneId.of("UTC"));
		}
		ZonedDateTime zdt = ZonedDateTime.parse(hl7DateStr, dateTimeFormatter);
		return Date.from(zdt.toInstant());		
	}
	
	/**
	 * Assumes the HL7 Date Format yyyyMMddHHmmss.SSS[X]
	 * @param hl7DateString
	 * @return
	 */
	private static boolean includesTimeZone(String hl7DateString){
		return hl7DateString.contains("+") || hl7DateString.contains("-") || hl7DateString.contains("Z");
	}

}
