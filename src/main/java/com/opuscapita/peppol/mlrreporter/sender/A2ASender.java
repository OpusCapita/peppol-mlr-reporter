package com.opuscapita.peppol.mlrreporter.sender;

import com.opuscapita.peppol.commons.eventing.TicketReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
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

    @Async("a2aAsyncExecutor")
    public void send(String report, String fileName) {
        logger.info("A2ASender.send called for file: " + fileName + " in thread: " + Thread.currentThread().getName());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Transfer-Encoding", "chunked");
        headers.set("Document-Path", String.format("/mlr/%s", fileName));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Authorization", config.getAuthHeader());
        HttpEntity<Resource> entity = new HttpEntity<>(new ByteArrayResource(report.getBytes()), headers);
        logger.debug("Wrapped and set the request body as file");

        // this method shouldn't throw exception because of @Async annotation
        // that is why I added this ugly retry-once-more logic for connection exceptions
        try {
            sendRequest(fileName, entity);
        } catch (Exception e) {
            if (e instanceof ResourceAccessException) {
                try {
                    sendRequest(fileName, entity);
                } catch (Exception e2) {
                    handleError(fileName, e2);
                }
            } else {
                handleError(fileName, e);
            }
        }
    }

    private void sendRequest(String fileName, HttpEntity<Resource> entity) {
        ResponseEntity<String> result = restTemplate.exchange(config.host, HttpMethod.POST, entity, String.class);
        logger.info("MLR successfully sent to A2A, filename: " + fileName + ", got response: " + result.toString());
    }

    private void handleError(String fileName, Exception e) {
        String message = "Error occurred while trying to send the MLR to A2A: " + fileName;
        ticketReporter.reportWithoutContainerMessage(null, fileName, e, message);
        logger.error(message, e);
    }

}
