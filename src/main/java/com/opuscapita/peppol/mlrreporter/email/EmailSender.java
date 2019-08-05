package com.opuscapita.peppol.mlrreporter.email;

import com.opuscapita.peppol.commons.auth.AuthorizationService;
import com.opuscapita.peppol.mlrreporter.email.dto.AccessPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

    private final EmailConfig emailConfig;
    private final RestTemplate restTemplate;
    private final AuthorizationService authService;

    @Autowired
    public EmailSender(EmailConfig emailConfig, AuthorizationService authService, RestTemplateBuilder restTemplateBuilder) {
        this.emailConfig = emailConfig;
        this.authService = authService;
        this.restTemplate = restTemplateBuilder.build();
    }

    public void send(String report, String fileName, AccessPoint accessPoint) throws IOException {
        if (accessPoint == null) {
            return;
        }

        String endpoint = getEndpoint();
        logger.debug("Sending email request to endpoint: " + endpoint + " for file: " + fileName);

        HttpHeaders headers = new HttpHeaders();
        authService.setAuthorizationHeader(headers);
        headers.setContentType(MediaType.APPLICATION_JSON);
        EmailObject email = createEmailObject(accessPoint);
        email.addAttachment(report, fileName);
        HttpEntity<EmailObject> entity = new HttpEntity<>(email, headers);
        logger.debug("Wrapped and set the request body as email object");

        try {
            ResponseEntity<String> result = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
            if (result.getStatusCode().is2xxSuccessful()) {
                logger.info("MLR successfully sent as email to: " + email.getTo() + ", filename: " + fileName);
            } else {
                throw new RuntimeException(result.getBody());
            }

        } catch (Exception e) {
            throw new IOException("Error occurred while trying to send the MLR to " + email.getTo(), e);
        }
    }

    private EmailObject createEmailObject(AccessPoint accessPoint) {
        EmailObject email = new EmailObject(accessPoint.getEmailList());
        email.setFrom(emailConfig.getFrom());
        email.setReplyTo(emailConfig.getReplyTo());
        email.setSubject(emailConfig.getSubject());
        email.setCustomId(emailConfig.getCustomId());
        email.setText(emailConfig.getText(accessPoint.getName()));
        email.setHtml(emailConfig.getHtml(accessPoint.getName()));
        return email;
    }

    private String getEndpoint() {
        return UriComponentsBuilder
                .fromUriString("http://email")
                .port(3050)
                .path("/api/send")
                .toUriString();
    }
}
