package net.sllmdilab.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Date;

import net.sllmdilab.commons.util.T5FHIRUtils;

import org.junit.Test;

public class T5FHIRUtilsTest {

	@Test
	public void shouldParseHL7DateTypeStringToCorrectDate() {
		
		String hl7Date = "20150818130436.710";
		String hl7DateZulu = "20150818130436.710Z";
		String hl7DatePlusZeroOffset = "20150818130436.710+0000";
		String hl7DatePlusOneOffset = "20150818130436.710+0100";
		String hl7DateMinusOneOffset = "20150818130436.710-0100";
		
		Date actualDate = T5FHIRUtils.convertHL7DateTypeToDate(hl7Date);
		Date actualDateZulu = T5FHIRUtils.convertHL7DateTypeToDate(hl7DateZulu);
		Date actualDatePlusZeroOffset = T5FHIRUtils.convertHL7DateTypeToDate(hl7DatePlusZeroOffset);
		Date actualDatePlusOneOffset = T5FHIRUtils.convertHL7DateTypeToDate(hl7DatePlusOneOffset);
		Date actualDateMinusOneOffset = T5FHIRUtils.convertHL7DateTypeToDate(hl7DateMinusOneOffset);
		
		long expected = 1439903076710L;
		
		assertEquals(expected, actualDate.toInstant().toEpochMilli());
		assertEquals(expected, actualDateZulu.toInstant().toEpochMilli());
		assertEquals(expected, actualDatePlusZeroOffset.toInstant().toEpochMilli());
		assertNotEquals(expected, actualDatePlusOneOffset.toInstant().toEpochMilli());
		assertNotEquals(expected, actualDateMinusOneOffset.toInstant().toEpochMilli());
	}

}
