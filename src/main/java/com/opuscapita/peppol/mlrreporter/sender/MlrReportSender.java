package com.opuscapita.peppol.mlrreporter.sender;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.commons.queue.RetryOperation;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.mlrreporter.creator.MlrType;
import com.opuscapita.peppol.mlrreporter.email.AccessPointManager;
import com.opuscapita.peppol.mlrreporter.email.EmailSender;
import com.opuscapita.peppol.mlrreporter.email.dto.AccessPoint;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Component
@RefreshScope
public class MlrReportSender {

    private static final Logger logger = LoggerFactory.getLogger(MlrReportSender.class);

    @Value("${fake-sending:}")
    private String fakeConfig;

    private final Storage storage;
    private final A2ASender a2ASender;
    private final XIBSender xibSender;
    private final EmailSender emailSender;
    private final AccessPointManager apManager;

    @Autowired
    public MlrReportSender(Storage storage, A2ASender a2ASender, XIBSender xibSender,
                           EmailSender emailSender, AccessPointManager apManager) {
        this.storage = storage;
        this.a2ASender = a2ASender;
        this.xibSender = xibSender;
        this.emailSender = emailSender;
        this.apManager = apManager;
    }

    public void send(ContainerMessage cm, String report, MlrType type) throws Exception {
        String pathName = FilenameUtils.getFullPath(cm.getFileName());
        String baseName = FilenameUtils.getBaseName(cm.getFileName());
        String fileName = baseName + "-" + type.name().toLowerCase() + "-mlr.xml";

        storeReport(report, pathName, fileName);

        RetryOperation.start(() -> sendReport(report, fileName, cm), 30, 1200000);
    }

    private void storeReport(String report, String pathName, String fileName) throws IOException {
        logger.debug("Storing MLR as " + fileName);
        storage.put(IOUtils.toInputStream(report, StandardCharsets.UTF_8), pathName, fileName);
        logger.info("MLR successfully stored as " + pathName + fileName);
    }

    private void sendReport(String report, String fileName, ContainerMessage cm) throws Exception {
        logger.info("MlrReportSender.sendReport called for message: " + cm.getFileName() + ", source[" + cm.getSource() + "]");
        if (Source.XIB.equals(cm.getSource()) && !fakeConfig.contains("xib")) {
            xibSender.send(report, fileName);
        }
        if (Source.A2A.equals(cm.getSource()) && !fakeConfig.contains("a2a")) {
            a2ASender.send(report, fileName);
        }
        if (Source.NETWORK.equals(cm.getSource()) && !fakeConfig.contains("network")) {
            AccessPoint accessPoint = apManager.fetchAccessPoint(cm);
            emailSender.send(prettyPrint(report), fileName, accessPoint);
        }
    }

    private String prettyPrint(String report) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StreamSource source = new StreamSource(new StringReader(report));
            StreamResult result = new StreamResult(new StringWriter());
            transformer.transform(source, result);
            return result.getWriter().toString();
        } catch (Exception e) {
            return report;
        }
    }

}
