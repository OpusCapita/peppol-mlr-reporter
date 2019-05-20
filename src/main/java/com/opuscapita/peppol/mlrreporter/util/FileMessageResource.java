package com.opuscapita.peppol.mlrreporter.util;

import org.springframework.core.io.ByteArrayResource;

public class FileMessageResource extends ByteArrayResource {

    private String filename;

    public FileMessageResource(byte[] byteArray) {
        super(byteArray);
    }

    public FileMessageResource(byte[] byteArray, String filename) {
        super(byteArray);
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return filename;
    }
}
