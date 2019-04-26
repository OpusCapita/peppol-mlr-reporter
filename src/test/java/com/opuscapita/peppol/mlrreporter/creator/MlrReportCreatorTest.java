package com.opuscapita.peppol.mlrreporter.creator;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.ContainerMessageMetadata;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.commons.storage.Storage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
public class MlrReportCreatorTest {

    @Autowired
    private MlrReportCreator creator;

    @MockBean
    private Storage storage;

    @MockBean
    private AdditionalMetadataExtractor metadataExtractor;

    @Test
    public void reportSuccess() throws Exception {
        MlrType type = MlrType.AP;
        ContainerMessage cm = prepareContainerMessage();
        ContainerMessageMetadata metadata = cm.getMetadata();
        MlrAdditionalMetadata more = prepareAdditionalMetadata();

        Mockito.when(storage.get(any())).thenReturn(null);
        Mockito.when(metadataExtractor.extract(any())).thenReturn(more);

        String result = creator.create(cm, type);

        assertTrue(result.contains("<cbc:ID>" + more.getDocumentId() + "-MLR</cbc:ID>"));
        assertTrue(result.contains(
                "<cac:SenderParty>" +
                    "<cbc:EndpointID>" + metadata.getSenderId() + "</cbc:EndpointID>" +
                    "<cac:PartyName>" +
                        "<cbc:Name>" + more.getSenderName() + "</cbc:Name>" +
                    "</cac:PartyName>" +
                "</cac:SenderParty>"
        ));
        assertTrue(result.contains(
                "<cac:ReceiverParty>" +
                    "<cbc:EndpointID>" + metadata.getRecipientId() + "</cbc:EndpointID>" +
                    "<cac:PartyName>" +
                        "<cbc:Name>" + more.getReceiverName() + "</cbc:Name>" +
                    "</cac:PartyName>" +
                "</cac:ReceiverParty>"
        ));
        assertTrue(result.contains(
                "<cac:DocumentResponse>" +
                    "<cac:Response>" +
                        "<cbc:ResponseCode>" + type.name() + "</cbc:ResponseCode>" +
                    "</cac:Response>" +
                    "<cac:DocumentReference>" +
                        "<cbc:ID>" + metadata.getMessageId() + "</cbc:ID>" +
                    "</cac:DocumentReference>" +
                "</cac:DocumentResponse>"
        ));
        assertFalse(result.contains("#"));
        assertFalse(result.contains("$"));
    }

    @Test
    public void reportErrorWithBadIssueDate() throws Exception {
        MlrType type = MlrType.ER;
        ContainerMessage cm = prepareContainerMessage();
        MlrAdditionalMetadata more = prepareAdditionalMetadata();

        more.setIssueDate("oops");

        Mockito.when(storage.get(any())).thenReturn(null);
        Mockito.when(metadataExtractor.extract(any())).thenReturn(more);

        String result = creator.create(cm, type);

        assertTrue(result.contains("<cbc:ResponseCode>RE</cbc:ResponseCode>"));
        assertTrue(result.contains("<cbc:StatusReasonCode>" + MlrStatusCode.SV.name() + "</cbc:StatusReasonCode>"));
        assertTrue(result.contains("DOCUMENT_ERROR"));
        assertFalse(result.contains("#"));
        assertFalse(result.contains("$"));
    }

    private ContainerMessage prepareContainerMessage() {
        ContainerMessage cm = new ContainerMessage("/private/cold/test.xml", Source.A2A);
        ContainerMessageMetadata metadata = ContainerMessageMetadata.createDummy();
        cm.setMetadata(metadata);

        return cm;
    }

    private MlrAdditionalMetadata prepareAdditionalMetadata() {
        MlrAdditionalMetadata metadata = new MlrAdditionalMetadata();
        metadata.setDocumentId("doc_id");
        metadata.setIssueDate("2017-07-18");
        metadata.setIssueTime("11:12:13");
        metadata.setSenderName("sender_name");
        metadata.setReceiverName("receiver_name");

        return metadata;
    }

}
