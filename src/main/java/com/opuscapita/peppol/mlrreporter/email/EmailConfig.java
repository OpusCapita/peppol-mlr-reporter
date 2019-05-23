package com.opuscapita.peppol.mlrreporter.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@RefreshScope
public class EmailConfig {

    @Value("${email.from:'noreply@opuscapita.com'}")
    private String from;

    @Value("${email.replyTo:'customerservice.en@opuscapita.com'}")
    private String replyTo;

    @Value("${email.subject:'Peppol Gateway: Invalid document report'}")
    private String subject;

    public String getFrom() {
        return from;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public String getSubject() {
        return subject;
    }

    public String getCustomId() {
        return UUID.randomUUID().toString();
    }

    public String getHtml(String receiverName) {
        return "<html><body><p>" + getText(receiverName) + "</p></body></html>";
    }

    public String getText(String receiverName) {
        return "This is an automatically redirected electronic invoice rejection message.\n\n" +
                "We have received an invalid PEPPOL document sent by " + receiverName + ". Please correct it and resend.\n\n" +
                "You can find detailed information in the attached Message Level Response report. " +
                "If you have any questions concerning the rejection, please reply directly to this e-mail.\n\n";
    }
}
