package com.opuscapita.peppol.mlrreporter.creator;

import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.sniffer.document.HardCodedNamespaceResolver;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class AdditionalMetadataExtractor {

    private final DocumentBuilderFactory documentBuilderFactory;

    public AdditionalMetadataExtractor() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        try {
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Unable to configure DOM parser for secure processing.", e);
        }
    }

    MlrAdditionalMetadata extract(InputStream inputStream) throws OxalisContentException {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);

            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new HardCodedNamespaceResolver());

            String id = retriveValueForXpath(document, xPath, "//cbc:ID");
            String date = retriveValueForXpath(document, xPath, "//cbc:IssueDate");
            String time = retriveValueForXpath(document, xPath, "//cbc:IssueTime");
            String sender = getSenderName(document, xPath);
            String receiver = getReceiverName(document, xPath);

            return new MlrAdditionalMetadata(id, date, time, sender, receiver);
        } catch (Exception e) {
            throw new OxalisContentException("Unable to parse document " + e.getMessage(), e);
        }
    }

    private String getSenderName(Document document, XPath xPath) {
        List<String> paths = new ArrayList<>();

        String type = document.getDocumentElement().getLocalName();
        if ("DespatchAdvice".equalsIgnoreCase(type)) {
            paths.add("//cac:DespatchSupplierParty/cac:Party/cac:PartyName/cbc:Name");
        }
        if ("Catalogue".equalsIgnoreCase(type)) {
            paths.add("//cac:ProviderParty/cac:PartyName/cbc:Name");
            paths.add("//cac:SellerSupplierParty/cac:Party/cac:PartyName/cbc:Name");
        }
        if ("Invoice".equalsIgnoreCase(type) || "CreditNote".equalsIgnoreCase(type) || "Reminder".equalsIgnoreCase(type)) {
            paths.add("//cac:AccountingSupplierParty/cac:Party/cac:PartyName/cbc:Name");
            paths.add("//cac:SellerParty/cac:Party/cac:PartyName/cbc:Name");
        }
        if ("Order".equalsIgnoreCase(type)) {
            paths.add("//cac:BuyerCustomerParty/cac:Party/cac:PartyName/cbc:Name");
        }
        if ("OrderResponse".equalsIgnoreCase(type) || "OrderResponseSimple".equalsIgnoreCase(type)) {
            paths.add("//cac:SellerSupplierParty/cac:Party/cac:PartyName/cbc:Name");
        }
        if ("ApplicationResponse".equalsIgnoreCase(type)) {
            paths.add("//cac:SenderParty/cac:PartyName/cbc:Name");
        }

        return checkPaths(document, xPath, paths);
    }

    private String getReceiverName(Document document, XPath xPath) {
        List<String> paths = new ArrayList<>();

        String type = document.getDocumentElement().getLocalName();
        if ("DespatchAdvice".equalsIgnoreCase(type)) {
            paths.add("//cac:DeliveryCustomerParty/cac:Party/cac:PartyName/cbc:Name");
        }
        if ("Catalogue".equalsIgnoreCase(type)) {
            paths.add("//cac:ReceiverParty/cac:PartyName/cbc:Name");
            paths.add("//cac:BuyerCustomerParty/cac:Party/cac:PartyName/cbc:Name");
        }
        if ("Invoice".equalsIgnoreCase(type) || "CreditNote".equalsIgnoreCase(type) || "Reminder".equalsIgnoreCase(type)) {
            paths.add("//cac:AccountingCustomerParty/cac:Party/cac:PartyName/cbc:Name");
            paths.add("//cac:BuyerParty/cac:Party/cac:PartyName/cbc:Name");
        }
        if ("Order".equalsIgnoreCase(type)) {
            paths.add("//cac:SellerSupplierParty/cac:Party/cac:PartyName/cbc:Name");
        }
        if ("OrderResponse".equalsIgnoreCase(type) || "OrderResponseSimple".equalsIgnoreCase(type)) {
            paths.add("//cac:BuyerCustomerParty/cac:Party/cac:PartyName/cbc:Name");
        }
        if ("ApplicationResponse".equalsIgnoreCase(type)) {
            paths.add("//cac:ReceiverParty/cac:PartyName/cbc:Name");
        }

        return checkPaths(document, xPath, paths);
    }

    private String checkPaths(Document document, XPath xPath, List<String> paths) {
        for (String path : paths) {
            try {
                return retriveValueForXpath(document, xPath, path);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String retriveValueForXpath(Document document, XPath xPath, String s) {
        try {
            String value = xPath.evaluate(s, document);
            if (value == null) {
                throw new IllegalStateException("Unable to find value for Xpath expr " + s);
            }
            return value.trim();
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Unable to evaluate " + s + "; " + e.getMessage(), e);
        }
    }
}
