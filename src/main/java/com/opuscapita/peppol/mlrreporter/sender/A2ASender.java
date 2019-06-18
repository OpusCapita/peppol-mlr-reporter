package com.opuscapita.peppol.mlrreporter.sender;

import com.opuscapita.peppol.commons.eventing.TicketReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class A2ASender {

    private static final Logger logger = LoggerFactory.getLogger(A2ASender.class);

    private final A2AConfiguration config;
    private final RestTemplate restTemplate;
    private final TicketReporter ticketReporter;

    public A2ASender(RestTemplate restTemplate, A2AConfiguration config, TicketReporter ticketReporter) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.ticketReporter = ticketReporter;
    }

    void send(String report, String fileName) {
        logger.info("A2ASender.send called for file: " + fileName);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Transfer-Encoding", "chunked");
            headers.set("Document-Path", String.format("/mlr/%s", fileName));
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Authorization", config.getAuthHeader());
            HttpEntity<Resource> entity = new HttpEntity<>(new ByteArrayResource(report.getBytes()), headers);
            logger.debug("Wrapped and set the request body as file");

            ResponseEntity<String> result = restTemplate.exchange(config.host, HttpMethod.POST, entity, String.class);
            logger.info("MLR successfully sent to A2A, filename: " + fileName + ", got response: " + result.toString());
        } catch (Exception e) {
            String message = "Error occurred while trying to send the MLR to A2A: " + fileName;
            ticketReporter.reportWithoutContainerMessage(null, fileName, e, message);
            logger.error(message, e);
        }
    }
}
