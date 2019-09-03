package com.opuscapita.peppol.mlrreporter.sender;

import com.opuscapita.peppol.commons.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Value("${sirius.url:''}")
    private String url;

    @Value("${sirius.username:''}")
    private String username;

    @Value("${sirius.password:''}")
    private String password;

    private final RestTemplate restTemplate;

    @Autowired
    public SiriusSender(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
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
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
        HttpEntity<Resource> entity = new HttpEntity<>(new ByteArrayResource(report.getBytes()), headers);
        logger.debug("Wrapped and set the request body as file");

        try {
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            logger.info("MLR successfully sent to Sirius, filename: " + fileName + ", got response: " + result.toString());
        } catch (Exception e) {
            throw new IOException("Error occurred while trying to send the MLR to Sirius", e);
        }
    }

}
