package com.opuscapita.peppol.mlrreporter.email;

import com.opuscapita.peppol.commons.auth.AuthorizationService;
import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.AccessPointInfo;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.mlrreporter.email.dto.AccessPoint;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class AccessPointManager {

    private static final Logger logger = LoggerFactory.getLogger(AccessPointManager.class);

    private final RestTemplate restTemplate;
    private final TicketReporter ticketReporter;
    private final AuthorizationService authService;

    @Autowired
    public AccessPointManager(AuthorizationService authService, TicketReporter ticketReporter, RestTemplateBuilder restTemplateBuilder) {
        this.authService = authService;
        this.ticketReporter = ticketReporter;
        this.restTemplate = restTemplateBuilder.build();
    }

    public AccessPoint fetchAccessPoint(ContainerMessage cm) {
        AccessPointInfo accessPointInfo = cm.getApInfo();
        if (accessPointInfo == null || StringUtils.isBlank(accessPointInfo.getId())) {
            reportMissingAccessPointEmail(cm, null);
            return null;
        }

        String endpoint = getEndpoint(accessPointInfo.getId());
        logger.debug("Sending fetchAccessPoint request to endpoint: " + endpoint + " for file: " + cm.getFileName());

        HttpHeaders headers = new HttpHeaders();
        authService.setAuthorizationHeader(headers);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        try {
            ResponseEntity<AccessPoint> result = restTemplate.exchange(endpoint, HttpMethod.GET, entity, AccessPoint.class);
            logger.debug("FetchAccessPoint request successfully sent, got response: " + result.toString());

            AccessPoint accessPoint = result.getBody();
            if (accessPoint == null || StringUtils.isBlank(accessPoint.getEmailList())) {
                reportMissingAccessPointEmail(cm, null);
                return null;
            }
            return accessPoint;

        } catch (Exception e) {
            logger.error("Error occurred while trying to get the Access Point request for file: " + cm.getFileName(), e);
            reportMissingAccessPointEmail(cm, e);
            return null;
        }
    }

    private void reportMissingAccessPointEmail(ContainerMessage cm, Throwable e) {
        String shortDescription = "Missing access point info for file " + cm.getFileName();
        String additionalDetails = "Email address of sending access point is required to send email notification.";
        ticketReporter.reportWithContainerMessage(cm, e, shortDescription, additionalDetails);
    }

    private String getEndpoint(String accessPointId) {
        return UriComponentsBuilder
                .fromUriString("http://peppol-monitor")
                .port(3041)
                .path("/api/get-access-point/" + accessPointId)
                .toUriString();
    }
}
