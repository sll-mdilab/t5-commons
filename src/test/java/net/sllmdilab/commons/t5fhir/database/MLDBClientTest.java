package net.sllmdilab.commons.t5fhir.database;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Iterator;

import net.sllmdilab.commons.database.MLDBClient;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ResultChannelName;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.types.ValueType;
import com.marklogic.xcc.types.XdmItem;

public class MLDBClientTest {
	private static final String MDC_MOCK_CODE = "MDC_MOCK_CODE";
	private static final String DEFAULT_CODE_SYSTEM = "MDC";
	private static final String PATIENT_ID = "191212-1212";

	@Mock
	private ContentSource contentSource;

	@InjectMocks
	private MLDBClient mldbClient;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

//	@Test
//	public void getTrendsRunsQuery() throws Exception {
//		Date startDate = new Date(0);
//		Date endDate = new Date(1337);
//
//		Session mockSession = Mockito.mock(Session.class);
//
//		when(mockSession.submitRequest(any())).thenReturn(
//				new MockResultSequence());
//		when(contentSource.newSession()).thenReturn(mockSession);
//
//		mldbClient.getTrends(PATIENT_ID, MDC_MOCK_CODE,
//				startDate, endDate);
//
//		verify(mockSession).newAdhocQuery(contains(PATIENT_ID));
//		verify(mockSession).newAdhocQuery(contains(MDC_MOCK_CODE));
//		verify(mockSession).newAdhocQuery(contains(DEFAULT_CODE_SYSTEM));
//	}

	private class MockResultSequence implements ResultSequence {

		@Override
		public String asString() {
			return "";
		}

		@Override
		public String asString(String arg0) {
			return null;
		}

		@Override
		public String[] asStrings() {
			return null;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public XdmItem[] toArray() {
			return null;
		}

		@Override
		public ValueType getValueType() {
			return null;
		}

		@Override
		public void close() {
		}

		@Override
		public ResultItem current() {
			return null;
		}

		@Override
		public ResultSequence getChannel(ResultChannelName arg0) {
			return null;
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public boolean isCached() {
			return false;
		}

		@Override
		public boolean isClosed() {
			return false;
		}

		@Override
		public XdmItem itemAt(int arg0) {
			return null;
		}

		@Override
		public Iterator<ResultItem> iterator() {
			return null;
		}

		@Override
		public ResultItem next() {
			return null;
		}

		@Override
		public ResultItem resultItemAt(int arg0) {
			return null;
		}

		@Override
		public void rewind() {
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public ResultSequence toCached() {
			return null;
		}

		@Override
		public ResultItem[] toResultItemArray() {
			return null;
		}
	}
}
