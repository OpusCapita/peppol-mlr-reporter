package com.opuscapita.peppol.mlrreporter.sender;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.mlrreporter.creator.MlrType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class MlrReportSender {

    private static final Logger logger = LoggerFactory.getLogger(MlrReportSender.class);

    private final Storage storage;

    @Autowired
    public MlrReportSender(Storage storage) {
        this.storage = storage;
    }

    public void send(ContainerMessage cm, String report, MlrType type) throws IOException {
        String pathName = FilenameUtils.getFullPath(cm.getFileName());
        String baseName = FilenameUtils.getBaseName(cm.getFileName());
        String fileName = baseName + "-" + type.name().toLowerCase() + "-mlr.xml";

        storeReport(report, pathName, fileName);

        sendReport(report, fileName, cm.getSource());
    }

    private void storeReport(String report, String pathName, String fileName) throws IOException {
        logger.debug("Storing MLR as " + fileName);
        storage.putToCustom(IOUtils.toInputStream(report, StandardCharsets.UTF_8), pathName, fileName);
        logger.info("MLR successfully stored as " + pathName + fileName);
    }

    // should send the report to specified business platform
    private void sendReport(String report, String fileName, Source source) {

    }
}
