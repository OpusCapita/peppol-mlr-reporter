package com.opuscapita.peppol.mlrreporter.consumer;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.queue.consume.ContainerMessageConsumer;
import com.opuscapita.peppol.mlrreporter.creator.MlrReportCreator;
import com.opuscapita.peppol.mlrreporter.creator.MlrType;
import com.opuscapita.peppol.mlrreporter.creator.MlrTypeIdentifier;
import com.opuscapita.peppol.mlrreporter.sender.MlrReportSender;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MlrReporterMessageConsumer implements ContainerMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MlrReporterMessageConsumer.class);

    private MlrReportSender mlrSender;
    private MlrReportCreator mlrCreator;
    private TicketReporter ticketReporter;
    private MlrTypeIdentifier typeIdentifier;

    @Autowired
    public MlrReporterMessageConsumer(MlrReportCreator mlrCreator, MlrReportSender mlrSender,
                                      TicketReporter ticketReporter, MlrTypeIdentifier typeIdentifier) {
        this.mlrSender = mlrSender;
        this.mlrCreator = mlrCreator;
        this.ticketReporter = ticketReporter;
        this.typeIdentifier = typeIdentifier;
    }

    @Override
    public void consume(@NotNull ContainerMessage cm) {
        try {
            if (StringUtils.isBlank(cm.getFileName())) {
                throw new IllegalArgumentException("File name is empty in received message: " + cm.toKibana());
            }

            MlrType type = typeIdentifier.identify(cm);
            if (type == null) {
                logger.debug("Couldn't find a reason to create MLR for file: " + cm.getFileName());
                return;
            }

            String report = mlrCreator.create(cm, type);
            mlrSender.send(cm, report, type);

        } catch (Exception e) {
            String message = "Error occurred while creating MLR report for file: " + cm.getFileName();
            ticketReporter.reportWithContainerMessage(cm, e, message);
            logger.error(message, e);
        }
    }

}
