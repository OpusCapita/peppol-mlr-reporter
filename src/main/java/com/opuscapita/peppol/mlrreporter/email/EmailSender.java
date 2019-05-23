package com.opuscapita.peppol.mlrreporter.email;

import com.opuscapita.peppol.commons.auth.AuthorizationService;
import com.opuscapita.peppol.commons.container.metadata.AccessPointInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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

    public EmailSender(EmailConfig emailConfig, AuthorizationService authService, RestTemplateBuilder restTemplateBuilder) {
        this.emailConfig = emailConfig;
        this.authService = authService;
        this.restTemplate = restTemplateBuilder.build();
    }

    public void send(String report, String fileName, AccessPointInfo apInfo) throws IOException {
        String endpoint = getEndpoint();
        logger.debug("Sending email request to endpoint: " + endpoint + " for file: " + fileName);

        HttpHeaders headers = new HttpHeaders();
        authService.setAuthorizationHeader(headers);
        headers.setContentType(MediaType.APPLICATION_JSON);
        EmailObject email = createEmailObject(apInfo);
        email.addAttachment(report, fileName);
        HttpEntity<EmailObject> entity = new HttpEntity<>(email, headers);
        logger.debug("Wrapped and set the request body as email object");

        try {
            restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
            logger.info("MLR successfully sent as email to: " + email.getTo() + ", filename: " + fileName);
        } catch (Exception e) {
            throw new IOException("Error occurred while trying to send the MLR to " + email.getTo(), e);
        }
    }

    private EmailObject createEmailObject(AccessPointInfo receiver) {
        String emailAddress = getEmailAddress(receiver);
        EmailObject email = new EmailObject(emailAddress);
        email.setFrom(emailConfig.getFrom());
        email.setReplyTo(emailConfig.getReplyTo());
        email.setSubject(emailConfig.getSubject());
        email.setCustomId(emailConfig.getCustomId());
        email.setText(emailConfig.getText(receiver.getName()));
        email.setHtml(emailConfig.getHtml(receiver.getName()));
        return email;
    }

    private String getEmailAddress(AccessPointInfo apInfo) {
//        return apInfo.getEmailAddress();
        return "";
    }

    private String getEndpoint() {
        return UriComponentsBuilder
                .fromUriString("http://email")
                .port(3050)
                .path("/api/send")
                .toUriString();
    }
}
