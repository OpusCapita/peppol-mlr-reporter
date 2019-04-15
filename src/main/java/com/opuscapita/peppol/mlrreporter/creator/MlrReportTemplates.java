package com.opuscapita.peppol.mlrreporter.creator;

@SuppressWarnings("WeakerAccess")
public class MlrReportTemplates {

    public static final String RESPONSE_TEMPLATE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<ApplicationResponse xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" xmlns:cec=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\" xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2\">" +
                    "<cbc:ID>${id}</cbc:ID>" +
                    "<cbc:IssueDate>${issue_date}</cbc:IssueDate>" +
                    "#ISSUE_TIME#" +
                    "<cbc:ResponseDate>${response_date}</cbc:ResponseDate>" +
                    "<cbc:ResponseTime>${response_time}</cbc:ResponseTime>" +
                    "<cbc:Note>${note}</cbc:Note>" +
                    "<cac:SenderParty>" +
                    "<cbc:EndpointID>${sender_id}</cbc:EndpointID>" +
                    "<cac:PartyName><cbc:Name>${sender_name}</cbc:Name></cac:PartyName>" +
                    "</cac:SenderParty>" +
                    "<cac:ReceiverParty>" +
                    "<cbc:EndpointID>${recipient_id}</cbc:EndpointID>" +
                    "<cac:PartyName><cbc:Name>${recipient_name}</cbc:Name></cac:PartyName>" +
                    "</cac:ReceiverParty>" +
                    "<cac:DocumentResponse>" +
                    "<cac:Response>" +
                    "<cbc:ResponseCode>${response_code}</cbc:ResponseCode>" +
                    "#DESCRIPTION#" +
                    "</cac:Response>" +
                    "<cac:DocumentReference><cbc:ID>${doc_reference}</cbc:ID></cac:DocumentReference>" +
                    "#LINES#" +
                    "</cac:DocumentResponse>" +
                    "</ApplicationResponse>";

    public static final String ISSUE_TIME_TEMPLATE = "<cbc:IssueTime>${issue_time}</cbc:IssueTime>";

    public static final String DESCRIPTION_TEMPLATE = "<cbc:Description>${description}</cbc:Description>";

    public static final String LINE_TEMPLATE =
            "<cac:LineResponse>" +
                    "<cac:LineReference><cbc:LineID>NA</cbc:LineID>" +
                    "<cac:DocumentReference><cbc:ID>${doc_reference}</cbc:ID>" +
                    "<cbc:XPath>${xpath}</cbc:XPath>" +
                    "</cac:DocumentReference>" +
                    "</cac:LineReference>" +
                    "<cac:Response>" +
                    "<cbc:ReferenceID>${reference_id}</cbc:ReferenceID>" +
                    "<cbc:Description>${description}</cbc:Description>" +
                    "<cac:Status><cbc:StatusReasonCode>${status_code}</cbc:StatusReasonCode></cac:Status>" +
                    "</cac:Response>" +
                    "</cac:LineResponse>";

}
