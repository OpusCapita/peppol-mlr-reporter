package com.opuscapita.peppol.mlrreporter.creator;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.container.state.log.DocumentLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MlrTypeIdentifier {

    private static final Logger logger = LoggerFactory.getLogger(MlrTypeIdentifier.class);

    public MlrType identify(ContainerMessage cm) {
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
            return null; // disabling for now
            //logger.info("Creating 'ER' MLR for the message: " + cm.toKibana() + " reason: Processing Errors");
            //return MlrType.ER;
        }

        logger.debug("Checking for outbound retries of the message: " + cm.getFileName());
        if (ProcessStep.OUTBOUND.equals(cm.getStep()) && cm.isOutbound()) { // this is disabled for inbound-flow
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
