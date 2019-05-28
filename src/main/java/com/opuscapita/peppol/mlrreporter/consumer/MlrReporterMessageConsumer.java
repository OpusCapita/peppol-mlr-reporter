package com.opuscapita.peppol.mlrreporter.consumer;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.container.state.log.DocumentLog;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.queue.consume.ContainerMessageConsumer;
import com.opuscapita.peppol.mlrreporter.creator.MlrReportCreator;
import com.opuscapita.peppol.mlrreporter.creator.MlrType;
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

    @Autowired
    public MlrReporterMessageConsumer(MlrReportCreator mlrCreator, MlrReportSender mlrSender, TicketReporter ticketReporter) {
        this.mlrSender = mlrSender;
        this.mlrCreator = mlrCreator;
        this.ticketReporter = ticketReporter;
    }

    @Override
    public void consume(@NotNull ContainerMessage cm) {
        try {
            if (StringUtils.isBlank(cm.getFileName())) {
                throw new IllegalArgumentException("File name is empty in received message: " + cm.toKibana());
            }

            if (cm.isInbound()) {
                logger.debug("Skipping MLR creation for inbound file: " + cm.getFileName());
                return;
            }

            MlrType type = getMlrType(cm);
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

    private MlrType getMlrType(ContainerMessage cm) {
        logger.debug("Checking validation errors of the message: " + cm.getFileName());
        if (!cm.getHistory().getValidationErrors().isEmpty()) {
            logger.info("Creating 'RE' MLR for the message: " + cm.toKibana() + " reason: Validation Errors");
            return MlrType.RE;
        }

        logger.debug("Checking sending errors of the message: " + cm.getFileName());
        if (hasLookupError(cm)) {
            logger.info("Creating 'ER' MLR for the message: " + cm.toKibana() + " reason: Sending Errors");
            return MlrType.ER;
        }

        logger.debug("Checking any other errors of the message: " + cm.getFileName());
        if (cm.getHistory().hasError()) {
            logger.info("Creating 'ER' MLR for the message: " + cm.toKibana() + " reason: Processing Errors");
            return null; // disabling for now
            //return MlrType.ER;
        }

        logger.debug("Checking for outbound retries of the message: " + cm.getFileName());
        if (ProcessStep.OUTBOUND.equals(cm.getStep())) {
            logger.info("Creating 'AB' MLR for the message: " + cm.toKibana() + " reason: Outbound Retry");
            return MlrType.AB;
        }

        logger.debug("Checking for successfully delivery of the message: " + cm.getFileName());
        if (ProcessStep.NETWORK.equals(cm.getStep())) {
            logger.info("Creating 'AP' MLR for the message: " + cm.toKibana() + " reason: Successful Delivery");
            return MlrType.AP;
        }
        return null;
    }

    private boolean hasLookupError(ContainerMessage cm) {
        return cm.getHistory().getLogs().stream().filter(DocumentLog::isSendingError).anyMatch(log -> {
            String code = log.getMessage().split(":")[0];
            return "UNKNOWN_RECIPIENT".equals(code) || "UNSUPPORTED_DATA_FORMAT".equals(code);
        });
    }

}
