package com.opuscapita.peppol.mlrreporter.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Base64;

@Component
@RefreshScope
public class SiriusSender implements RetryableSender  {

    private final static Logger logger = LoggerFactory.getLogger(SiriusSender.class);

    private final RestTemplate restTemplate;
    private final SiriusConfiguration config;

    @Autowired
    public SiriusSender(@Qualifier("siriusRestTemplate") RestTemplate restTemplate, SiriusConfiguration config) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    @Override
    public void send(String report, String fileName) throws Exception {
        logger.debug("SiriusSender.send called for the message: " + fileName);
        retrySend(report, fileName);
    }

    @Override
    public void retrySend(String report, String fileName) throws Exception {
        logger.info("SiriusSender.send called for file: " + fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Transfer-Encoding", "chunked");
        headers.set("File-Type", "MLR");
        headers.set("File-Name", fileName);
        headers.set("senderApplication", "PEPPOL-AP");
        headers.set("Authorization", config.getAuthHeader());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        HttpEntity<Resource> entity = new HttpEntity<>(new ByteArrayResource(report.getBytes()), headers);
        logger.debug("Wrapped and set the request body as file");

        try {
            ResponseEntity<String> result = restTemplate.exchange(config.getUrl(), HttpMethod.POST, entity, String.class);
            logger.info("MLR successfully sent to Sirius, filename: " + fileName + ", got response: " + result.toString());
        } catch (Exception e) {
            throw new IOException("Error occurred while trying to send the MLR to Sirius", e);
        }
    }

}
