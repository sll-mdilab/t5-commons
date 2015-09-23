package net.sllmdilab.commons.database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.sllmdilab.commons.exceptions.DatabaseException;
import net.sllmdilab.commons.util.Constants;

import org.apache.commons.lang3.StringEscapeUtils;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;

public class MLDBClient {

	private ContentSource contentSource;

	private final SimpleDateFormat isoDateFormat = new SimpleDateFormat(Constants.ISO_DATE_FORMAT);

	public MLDBClient(ContentSource contenSource) {
		isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		this.contentSource = contenSource;
	}

	public String getTrends(String idPatient, String idObservationCode, Date startTime, Date endTime)
			throws DatabaseException {

		return sendQuery(buildObservationsXQuery(idPatient, idObservationCode, startTime, endTime));
	}

	public String getTrendsForDevice(String idDevice, String idObservationCode, Date startTime, Date endTime)
			throws DatabaseException {

		return sendQuery(buildObservationsXQueryForDevice(idDevice, idObservationCode, startTime, endTime));
	}

	public String getObservationSummary(String idPatient, Date startTime, Date endTime) throws DatabaseException {

		return sendQuery(buildObservationSummaryXQuery(idPatient, startTime, endTime));
	}

	public String getObservationSummaryForDevice(String idDevice, Date startTime, Date endTime)
			throws DatabaseException {

		return sendQuery(buildObservationSummaryXQueryForDevice(idDevice, startTime, endTime));
	}

	public String getPatientSummary(Date startTime, Date endTime) throws DatabaseException {

		return sendQuery(buildPatientSummaryXQuery(startTime, endTime));
	}

	public String sendQuery(String xQuery) {
		try {
			Session session = contentSource.newSession();

			Request request = session.newAdhocQuery(xQuery);

			ResultSequence rs = session.submitRequest(request);

			return rs.asString();
		} catch (RequestException e) {
			throw new DatabaseException("Database connection error.", e);
		}
	}

	public String insertDocument(String uri, String document) {
		try {
			Session session = contentSource.newSession();

			Request request = session.newAdhocQuery(buildInsertMessageXQuery());
			request.setNewStringVariable("URI", uri);
			request.setNewStringVariable("XML_BODY", document);

			ResultSequence rs = session.submitRequest(request);

			return rs.asString();
		} catch (RequestException e) {
			throw new DatabaseException("Database connection error.", e);
		}
	}

	private String buildInsertMessageXQuery() {
		//@formatter:off
		String xQuery =
			"declare variable $XML_BODY as xs:string external;" +
			"declare variable $URI as xs:string external;" +
			"xdmp:document-insert($URI, xdmp:unquote($XML_BODY))";
		//@formatter:on

		return xQuery;
	}

	private String buildObservationsXQuery(String idPatient, String idObservationCode, Date startTime, Date endTime) {

		String isoStartTime = isoDateFormat.format(startTime);
		String isoEndTime = isoDateFormat.format(endTime);

		//@formatter:off
		
		String xQuery = 
			"<trend>\n"+
			"{\n"+
			"let $obs_type := '" + StringEscapeUtils.escapeXml10(idObservationCode) +"'\n"+
			"let $pid := '" + StringEscapeUtils.escapeXml10(idPatient) +"'\n"+
			"let $timeFrom := xs:dateTime('"+ isoStartTime + "')\n"+
			"let $timeUntil := xs:dateTime('" + isoEndTime +"')\n"+
			"let $obs := doc()/PCD_01_Message/Patient_Result[//Patient/Identifier = $pid]/Order_Observations/MDS/VMD/CHAN/Metric/Observation[Timestamp >= $timeFrom and Timestamp <= $timeUntil][ObsIdentifier = $obs_type]\n"+
			"for $o in $obs\n"+
			"return \n"+
			"<point uid=\"{$o/@uid}\"\n"+
			"setid=\"{$o/@setid}\"\n"+
			"time=\"{data($o/Timestamp)}\"\n"+
			"value = \"{data($o/Value)}\"\n"+
			"unit=\"{$o/Unit}\"\n"+
			"unitSystem=\"{$o/Unit/@codingSystemName}\"\n"+
			"sampleRate=\"{$o/../Facet/Observation[ObsIdentifier = \"MDC_ATTR_SAMP_RATE\"]/Value}\" \n"+
			"dataRange=\"{$o/../Facet/Observation[ObsIdentifier = \"MDC_ATTR_DATA_RANGE\"]/Value}\" \n"+
			"endTime=\"{$o/../../../../../Order/@timeStampEnd}\">\n"+
			"</point>"+
			"}\n"+
			"</trend>";
		
		//@formatter:on
		return xQuery;
	}

	private String buildObservationsXQueryForDevice(String idDevice, String idObservationCode, Date startTime,
			Date endTime) {

		String isoStartTime = isoDateFormat.format(startTime);
		String isoEndTime = isoDateFormat.format(endTime);

		//@formatter:off
		
		String xQuery = 
			"<trend>\n"+
			"{\n"+
			"let $obs_type := '" + StringEscapeUtils.escapeXml10(idObservationCode) +"'\n"+
			"let $did := '" + StringEscapeUtils.escapeXml10(idDevice) +"'\n"+

			"let $timeFrom := xs:dateTime('" + isoStartTime + "')\n"+
			"let $timeUntil := xs:dateTime('" + isoEndTime +"')\n"+
			"let $obs := doc()/PCD_01_Message/Patient_Result/Order_Observations/MDS[//Observation/EquipmentIdentifier = $did]//Observation[ObsIdentifier = $obs_type][Timestamp >= $timeFrom and Timestamp <= $timeUntil]\n"+
			"for $o in $obs\n"+
			"return \n"+
			"<point uid=\"{$o/@uid}\"\n"+
			"setid=\"{$o/@setid}\"\n"+
			"time=\"{data($o/Timestamp)}\"\n"+
			"value = \"{data($o/Value)}\"\n"+
			"unit=\"{$o/Unit}\"\n"+
			"unitSystem=\"{$o/Unit/@codingSystemName}\"\n"+
			"sampleRate=\"{$o/../Facet/Observation[ObsIdentifier = \"MDC_ATTR_SAMP_RATE\"]/Value}\" \n"+
			"dataRange=\"{$o/../Facet/Observation[ObsIdentifier = \"MDC_ATTR_DATA_RANGE\"]/Value}\" \n"+
			"endTime=\"{$o/../../../../../Order/@timeStampEnd}\">\n"+
			"</point>"+
			"}\n"+
			"</trend>";
		
		//@formatter:on
		return xQuery;
	}

	private String buildObservationSummaryXQuery(String idPatient, Date startTime, Date endTime) {

		String isoStartTime = isoDateFormat.format(startTime);
		String isoEndTime = isoDateFormat.format(endTime);

		//@formatter:off
		String xQuery =
			"xquery version \"1.0-ml\";\n"+
			"<ObsTypes>\n"+
			"{\n"+
			"let $pid := '" + StringEscapeUtils.escapeXml10(idPatient) + "' \n"+
			"let $timeFrom := xs:dateTime('" + isoStartTime + "')\n"+
			"let $timeUntil := xs:dateTime('" + isoEndTime + "')\n"+
			"let $o := doc()/PCD_01_Message[//Patient/Identifier = $pid]//MDS/VMD/CHAN/Metric/Observation[Timestamp >= $timeFrom and Timestamp <= $timeUntil]/ObsIdentifier\n"+
			"return \n"+
			"for $v in fn:distinct-values($o)\n"+
			"return\n"+
			"<ObsName>{$v}</ObsName>\n"+
			"}\n"+
			"</ObsTypes>";
		//@formatter:on
		return xQuery;
	}

	private String buildObservationSummaryXQueryForDevice(String idDevice, Date startTime, Date endTime) {

		String isoStartTime = isoDateFormat.format(startTime);
		String isoEndTime = isoDateFormat.format(endTime);

		//@formatter:off
		String xQuery =
			"xquery version '1.0-ml';\n"+
			"<ObsTypes>\n"+
			"{\n"+
			"let $did := '" + StringEscapeUtils.escapeXml10(idDevice) + "' \n"+

			"let $timeFrom := xs:dateTime('" + isoStartTime + "')\n"+
			"let $timeUntil := xs:dateTime('" + isoEndTime + "')\n"+
			"let $o := doc()/PCD_01_Message/Patient_Result/Order_Observations/MDS[//Observation/EquipmentIdentifier = $did]//Observation[Timestamp >= $timeFrom and Timestamp <= $timeUntil]/ObsIdentifier\n"+
			"return \n"+
			"for $v in fn:distinct-values($o)\n"+
			"return\n"+
			"<ObsName>{$v}</ObsName>\n"+
			"}\n"+
			"</ObsTypes>";
		//@formatter:on
		return xQuery;
	}

	private String buildPatientSummaryXQuery(Date startTime, Date endTime) {

		String isoStartTime = isoDateFormat.format(startTime);
		String isoEndTime = isoDateFormat.format(endTime);

		//@formatter:off
		String xQuery = 
			"<List>\n"+
			"{\n"+
			"let $timeFrom := xs:dateTime('" + isoStartTime + "')\n"+
			"let $timeUntil := xs:dateTime('" + isoEndTime + "')\n"+
			"let $p := doc()/PCD_01_Message[//MDS/VMD/CHAN/Metric/Observation[Timestamp >= $timeFrom and Timestamp <= $timeUntil]]//Patient/Identifier\n"+
			"return \n"+
			"for $pid in fn:distinct-values($p)\n"+
			"return\n"+
			"<PID>{$pid}</PID>\n"+
			"}\n"+
			"</List>";
		//@formatter:on
		return xQuery;
	}
}
