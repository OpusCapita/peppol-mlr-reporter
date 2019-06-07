package com.opuscapita.peppol.mlrreporter.sender;

import com.opuscapita.peppol.commons.auth.AuthorizationService;
import com.opuscapita.peppol.mlrreporter.util.FileMessageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class XIBSender {

    private static final Logger logger = LoggerFactory.getLogger(XIBSender.class);

    private final RestTemplate restTemplate;
    private final AuthorizationService authService;

    public XIBSender(AuthorizationService authService, RestTemplateBuilder restTemplateBuilder) {
        this.authService = authService;
        this.restTemplate = restTemplateBuilder.build();
    }

    void send(String report, String fileName) throws IOException {
        logger.info("XIBSender.send called for file: " + fileName);
        String endpoint = getEndpoint();
        logger.debug("Sending upload-mlr request to endpoint: " + endpoint + " for file: " + fileName);

        HttpHeaders headers = new HttpHeaders();
        authService.setAuthorizationHeader(headers);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileMessageResource(report.getBytes(), fileName));
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        logger.debug("Wrapped and set the request body as input stream");

        try {
            restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
            logger.info("MLR successfully sent to XIB, filename: " + fileName);
        } catch (Exception e) {
            throw new IOException("Error occurred while trying to send the MLR to XIB", e);
        }
    }

    private String getEndpoint() {
        return UriComponentsBuilder
                .fromUriString("http://peppol-xib-adaptor")
                .port(3043)
                .path("/api/upload-mlr")
                .toUriString();
    }
}
