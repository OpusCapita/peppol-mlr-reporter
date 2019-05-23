package com.opuscapita.peppol.mlrreporter.email;

import java.io.Serializable;
import java.util.Base64;

public class EmailAttachment implements Serializable {

    private String type;
    private String filename;
    private String content;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        byte[] encodedContent = content.getBytes();
        this.content = Base64.getEncoder().encodeToString(encodedContent);
    }
}
