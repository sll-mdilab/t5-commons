package net.sllmdilab.commons.database;

import static java.lang.System.currentTimeMillis;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sllmdilab.commons.exceptions.DatabaseException;
import net.sllmdilab.commons.util.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;

public class MLDBClient {
	private static final Logger logger = LoggerFactory.getLogger(MLDBClient.class);

	private ContentSource contentSource;

	private final SimpleDateFormat isoDateFormat = new SimpleDateFormat(Constants.ISO_DATE_FORMAT);

	public MLDBClient(ContentSource contenSource) {
		isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		this.contentSource = contenSource;
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

	public Document sendQueryParseResponse(String xQuery) {
		long clockStartMillis = currentTimeMillis();
		String response = sendQuery(xQuery);
		logger.debug("Fetching from DB took " + (currentTimeMillis() - clockStartMillis) + " ms.");

		try {
			return parseXml(response);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new DatabaseException(e);
		}
	}

	private Document parseXml(String response) throws SAXException, IOException, ParserConfigurationException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new InputSource(new StringReader(response)));
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
}
