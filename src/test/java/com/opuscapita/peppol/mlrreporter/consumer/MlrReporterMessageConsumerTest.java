package com.opuscapita.peppol.mlrreporter.consumer;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.ContainerMessageMetadata;
import com.opuscapita.peppol.commons.container.metadata.MetadataExtractor;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.commons.container.state.log.DocumentValidationError;
import com.opuscapita.peppol.commons.storage.Storage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

@Ignore
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
public class MlrReporterMessageConsumerTest {

    @Autowired
    @Qualifier("physicalStorage")
    private Storage storage;

    @Autowired
    private MlrReporterMessageConsumer consumer;

    @Autowired
    private MetadataExtractor metadataExtractor;

    @BeforeClass
    public static void setup() throws IOException {
        FileUtils.deleteDirectory(new File("/private/peppol/test/"));
    }

    @Test
    public void testValidationError() throws Exception {
        ContainerMessage cm = createContainerMessage("/test-materials/sample1.xml", ProcessStep.TEST);

        DocumentValidationError validationError = new DocumentValidationError("Test Validation Error")
                .withLocation("Undefined location")
                .withText("Validation error occurred while testing the document")
                .withIdentifier("ERR-UNKNOWN")
                .withFlag("FATAL")
                .withTest("XSL Validation: " + cm.getFileName());
        cm.getHistory().addValidationError(validationError);

        consumer.consume(cm);

        String result = getFile("sample1-re-mlr.xml");

        assertTrue(result.contains(
                "<cac:Response>" +
                    "<cbc:ResponseCode>RE</cbc:ResponseCode>" +
                    "<cbc:Description>VALIDATION_ERROR</cbc:Description>" +
                "</cac:Response>"
        ));
        assertTrue(result.contains(
                "<cac:Status>" +
                    "<cbc:StatusReasonCode>RVF</cbc:StatusReasonCode>" +
                "</cac:Status>"
        ));
        assertTrue(result.contains("ERR-UNKNOWN"));
        commonAssertions(result);
    }

    @Test
    public void testProcessingError() throws Exception {
        ContainerMessage cm = createContainerMessage("/test-materials/sample2.xml", ProcessStep.TEST);
        cm.getHistory().addError("Processing error occurred, BOOM!");

        consumer.consume(cm);

        String result = getFile("sample2-er-mlr.xml");

        assertTrue(result.contains(
                "<cac:Response>" +
                    "<cbc:ResponseCode>RE</cbc:ResponseCode>" +
                    "<cbc:Description>DOCUMENT_ERROR</cbc:Description>" +
                "</cac:Response>"
        ));
        assertTrue(result.contains(
                "<cac:Status>" +
                    "<cbc:StatusReasonCode>SV</cbc:StatusReasonCode>" +
                "</cac:Status>"
        ));
        assertTrue(result.contains("BOOM"));
        commonAssertions(result);
    }

    @Test
    public void testOutboundRetry() throws Exception {
        ContainerMessage cm = createContainerMessage("/test-materials/sample3.xml", ProcessStep.OUTBOUND);
        cm.getHistory().addInfo("Sent to outbound retry queue");
        consumer.consume(cm);

        String result = getFile("sample3-ab-mlr.xml");

        assertTrue(result.contains(
                "<cac:Response>" +
                    "<cbc:ResponseCode>AB</cbc:ResponseCode>" +
                    "<cbc:Description>Sent to outbound retry queue</cbc:Description>" +
                "</cac:Response>"
        ));
        commonAssertions(result);
    }

    @Test
    public void testDeliverySuccess() throws Exception {
        ContainerMessage cm = createContainerMessage("/test-materials/sample4.xml", ProcessStep.NETWORK);
        cm.getHistory().addInfo("Successfully delivered to network");
        consumer.consume(cm);

        String result = getFile("sample4-ap-mlr.xml");

        assertTrue(result.contains(
                "<cac:Response>" +
                    "<cbc:ResponseCode>AP</cbc:ResponseCode>" +
                "</cac:Response>"
        ));
        commonAssertions(result);
    }

    private void commonAssertions(String result) {
        assertTrue(result.contains("<cbc:ID>9067031258-MLR</cbc:ID>"));
        assertTrue(result.contains(
                "<cac:SenderParty>" +
                    "<cbc:EndpointID>0007:987654321</cbc:EndpointID>" +
                    "<cac:PartyName>" +
                        "<cbc:Name>OpusCapita test</cbc:Name>" +
                    "</cac:PartyName>" +
                "</cac:SenderParty>"
        ));
        assertTrue(result.contains(
                "<cac:ReceiverParty>" +
                    "<cbc:EndpointID>0007:987654321</cbc:EndpointID>" +
                    "<cac:PartyName>" +
                        "<cbc:Name>OpusCapita test</cbc:Name>" +
                    "</cac:PartyName>" +
                "</cac:ReceiverParty>"
        ));
    }

    private ContainerMessage createContainerMessage(String filename, ProcessStep step) throws Exception {
        File testFile = new File(getClass().getResource(filename).getFile());
        String path = storeFile(testFile);
        ContainerMessageMetadata metadata = createMetadata(testFile);
        ContainerMessage cm = new ContainerMessage(path, Source.A2A, step);
        cm.getHistory().addInfo("Ready to run the tests");
        cm.setMetadata(metadata);
        return cm;
    }

    private String storeFile(File file) throws Exception {
        try (InputStream stream = new FileInputStream(file)) {
            return storage.put(stream, "/private/peppol/test/", file.getName());
        }
    }

    private String getFile(String filename) throws Exception {
        InputStream inputStream = storage.get("/private/peppol/test/" + filename);
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

    private ContainerMessageMetadata createMetadata(File file) throws Exception {
        try (InputStream stream = new FileInputStream(file)) {
            return metadataExtractor.extract(stream);
        }
    }
}
