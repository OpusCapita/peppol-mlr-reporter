package com.opuscapita.peppol.mlrreporter.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class MlrUtils {

    public static String getOriginalFilename(String filename) {
        String base = FilenameUtils.getBaseName(filename);
        String[] parts = base.split("_");
        String last = parts[parts.length - 1];
        if (parts.length > 1 && StringUtils.isNumeric(last)) {
            return base.substring(0, base.length() - (last.length() + 1));
        }
        return base;
    }

    public static XMLGregorianCalendar convertToXml(String date) throws ParseException, DatatypeConfigurationException {
        return convertToXml(new SimpleDateFormat("yyyy-MM-dd").parse(date));
    }

    public static String convertDateToXml(String date) throws ParseException, DatatypeConfigurationException {
        String convertedDate = convertToXml(new SimpleDateFormat("yyyy-MM-dd").parse(date)).toString();
        convertedDate = convertedDate.split("T")[0];
        return convertedDate;
    }

    public static String convertTimeToXml(String time) throws ParseException, DatatypeConfigurationException {
        String convertedTime = convertToXml(new SimpleDateFormat("HH:mm:ss").parse(time)).toString();
        convertedTime = convertedTime.split("T")[1];
        convertedTime = convertedTime.split("\\.")[0];
        return convertedTime;
    }

    public static String convertDateToXml(Date date) throws DatatypeConfigurationException {
        String convertedDate = convertToXml(date).toString();
        convertedDate = convertedDate.split("T")[0];
        return convertedDate;
    }

    public static String convertTimeToXml(Date date) throws DatatypeConfigurationException {
        String convertedTime = convertToXml(date).toString();
        convertedTime = convertedTime.split("T")[1];
        convertedTime = convertedTime.split("\\.")[0];
        return convertedTime;
    }

    public static XMLGregorianCalendar convertToXmlTime(String time) throws ParseException, DatatypeConfigurationException {
        return convertToXml(new SimpleDateFormat("HH:mm:ss").parse(time));
    }

    private static XMLGregorianCalendar convertToXml(Date date) throws DatatypeConfigurationException {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
    }
}
