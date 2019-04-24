package com.opuscapita.peppol.mlrreporter.util;

import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;

import static org.junit.Assert.assertTrue;

public class MlrUtilsTest {

    @Test
    public void convertToXmlDate() throws Exception {
        String date = "2017-07-18";
        XMLGregorianCalendar convertedDate = MlrUtils.convertToXml(date);
        assertTrue(convertedDate.toString().contains("2017-07-18"));
    }

    @Test
    public void convertToXmlTime() throws Exception {
        String time = "11:57:14";
        assertTrue(MlrUtils.convertToXmlTime(time).toString().contains("11:57:14"));
    }

    @Test
    public void testConvertDateToXml() throws Exception {
        String date = "2017-07-18";
        String result = MlrUtils.convertDateToXml(date);
        assertTrue(result.contains("2017-07-18"));
    }

    @Test
    public void testConvertTimeToXml() throws Exception {
        String time = "11:57:14";
        String result = MlrUtils.convertTimeToXml(time);
        assertTrue(result.contains("11:57:14"));
    }

}