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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Component
public class MlrReportSender {

    private static final Logger logger = LoggerFactory.getLogger(MlrReportSender.class);

    @Value("${fake-sending:}")
    private String fakeConfig;

    private final Storage storage;
    private final A2ASender a2ASender;
    private final XIBSender xibSender;

    @Autowired
    public MlrReportSender(Storage storage, A2ASender a2ASender, XIBSender xibSender) {
        this.storage = storage;
        this.a2ASender = a2ASender;
        this.xibSender = xibSender;
    }

    public void send(ContainerMessage cm, String report, MlrType type) throws IOException, TransformerException {
        String pathName = FilenameUtils.getFullPath(cm.getFileName());
        String baseName = FilenameUtils.getBaseName(cm.getFileName());
        String fileName = baseName + "-" + type.name().toLowerCase() + "-mlr.xml";

        storeReport(report, pathName, fileName);

        sendReport(report, fileName, cm.getSource());
    }

    private void storeReport(String report, String pathName, String fileName) throws IOException {
        logger.debug("Storing MLR as " + fileName);
        storage.put(IOUtils.toInputStream(report, StandardCharsets.UTF_8), pathName, fileName);
        logger.info("MLR successfully stored as " + pathName + fileName);
    }

    // should send the report to specified business platform
    private void sendReport(String report, String fileName, Source source) throws IOException {
        if (Source.XIB.equals(source) && !fakeConfig.contains("xib")) {
            xibSender.send(report, fileName);
        }
        if (Source.A2A.equals(source) && !fakeConfig.contains("a2a")) {
            a2ASender.send(report, fileName);
        }
    }

    private String prettyPrint(String report) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        StreamSource source = new StreamSource(new StringReader(report));
        StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(source, result);
        return result.getWriter().toString();
    }
}
