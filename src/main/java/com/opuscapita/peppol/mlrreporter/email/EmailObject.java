package com.opuscapita.peppol.mlrreporter.email;

import org.springframework.http.MediaType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmailObject implements Serializable {

    private String to;
    private String from;
    private String replyTo;
    private String customId;
    private String subject;
    private String text;
    private String html;
    private List<EmailAttachment> attachments;

    public EmailObject(String to) {
        this.setTo(to);
        this.attachments = new ArrayList<>();
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<EmailAttachment> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(String content, String filename) {
        EmailAttachment attachment = new EmailAttachment();
        attachment.setContent(content);
        attachment.setFilename(filename);
        attachment.setType(MediaType.TEXT_XML_VALUE);
        this.attachments.add(attachment);
    }

}
