package com.opuscapita.peppol.mlrreporter.creator;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.ContainerMessageMetadata;
import com.opuscapita.peppol.commons.container.state.log.DocumentLog;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.mlrreporter.util.MlrUtils;
import no.difi.oxalis.api.lang.OxalisContentException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MlrReportCreator {

    private static final Logger logger = LoggerFactory.getLogger(MlrReportCreator.class);

    private Storage storage;
    private AdditionalMetadataExtractor metadataExtractor;

    @Autowired
    public MlrReportCreator(Storage storage, AdditionalMetadataExtractor metadataExtractor) {
        this.storage = storage;
        this.metadataExtractor = metadataExtractor;
    }

    public String create(ContainerMessage cm, MlrType type) throws Exception {
        String report = MlrReportTemplates.RESPONSE_TEMPLATE;
        report = fillCommonFields(report, cm);
        report = setResponseCode(report, cm, type);
        return report;
    }

    private String setResponseCode(String template, ContainerMessage cm, MlrType type) {
        String description = MlrReportTemplates.DESCRIPTION_TEMPLATE;

        if (MlrType.AP.equals(type)) {
            description = "";

        } else if (MlrType.AB.equals(type)) {
            String lastLog = cm.getHistory().getLastLog().getMessage();
            description = replace(description, "description", lastLog);

        } else if (MlrType.RE.equals(type)) {
            description = replace(description, "description", "VALIDATION_ERROR");

        } else {
            type = MlrType.RE;
            List<DocumentLog> sendingErrors = cm.getHistory().getLogs().stream().filter(DocumentLog::isSendingError).collect(Collectors.toList());
            if (!sendingErrors.isEmpty()) {
                description = replace(description, "description", sendingErrors.get(sendingErrors.size() - 1).getMessage());
            } else {
                description = replace(description, "description", "DOCUMENT_ERROR");
            }
        }

        template = StringUtils.replace(template, "#DESCRIPTION#", description);
        return replace(template, "response_code", type.name());
    }

    private String fillCommonFields(String template, ContainerMessage cm) throws Exception {
        ContainerMessageMetadata metadata = cm.getMetadata();
        MlrAdditionalMetadata additionalMetadata = getAdditionalMetadata(cm);


        template = replace(template, "note", MlrUtils.getOriginalFilename(cm.getFileName()));
        template = replace(template, "id", additionalMetadata.getDocumentId() + "-MLR");

        try {
            template = replace(template, "issue_date", MlrUtils.convertDateToXml(additionalMetadata.getIssueDate()));
            if (StringUtils.isNotBlank(additionalMetadata.getIssueTime())) {
                String issue_time = MlrReportTemplates.ISSUE_TIME_TEMPLATE;
                try {
                    issue_time = replace(issue_time, "issue_time", MlrUtils.convertTimeToXml(additionalMetadata.getIssueTime()));
                } catch (Exception e) {
                    logger.debug("Failed to parse issue time: '" + additionalMetadata.getIssueTime() + "' for message: " + cm.getFileName());
                }
                template = StringUtils.replace(template, "#ISSUE_TIME#", issue_time);
            }
        } catch (Exception e) {
            logger.info("Failed to parse issue issue date: '" + additionalMetadata.getIssueDate() + "', using current date instead");
            template = replace(template, "issue_date", MlrUtils.convertDateToXml(new Date()));
        }

        // if not replaced - remove placeholders
        template = StringUtils.replace(template, "#ISSUE_DATE#", "");
        template = StringUtils.replace(template, "#ISSUE_TIME#", "");

        Date now = new Date();
        template = replace(template, "response_date", MlrUtils.convertDateToXml(now));
        template = replace(template, "response_time", MlrUtils.convertTimeToXml(now));
        template = replace(template, "sender_id", metadata.getSenderId());
        template = replace(template, "sender_name", additionalMetadata.getSenderName());
        template = replace(template, "recipient_id", metadata.getRecipientId());
        template = replace(template, "recipient_name", additionalMetadata.getReceiverName());
        template = replace(template, "doc_reference", metadata.getMessageId());

        template = StringUtils.replace(template, "#LINES#", createLines(cm, additionalMetadata.getDocumentId()));

        return template;
    }


    private String createLines(ContainerMessage cm, String documentId) {
        List<DocumentLog> errors = cm.getHistory().getLogs().stream().filter(l -> !l.isInfo()).collect(Collectors.toList());
        if (errors.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (DocumentLog error : errors) {
            String template = MlrReportTemplates.LINE_TEMPLATE;
            template = replace(template, "doc_reference", documentId);

            if (error.getValidationError() != null) {
                template = replace(template, "xpath", error.getValidationError().getLocation());
                template = replace(template, "reference_id", error.getValidationError().getIdentifier());
                template = replace(template, "description", error.getValidationError().getDetails());
                template = replace(template, "status_code", error.isWarning() ? MlrStatusCode.RVW.name() : MlrStatusCode.RVF.name());

            } else {
                template = replace(template, "xpath", "NA");
                template = replace(template, "reference_id", error.getSource().name());
                template = replace(template, "description", error.getMessage());
                template = replace(template, "status_code", MlrStatusCode.SV.name());
            }

            result.append(template);
        }

        return result.toString();
    }

    private MlrAdditionalMetadata getAdditionalMetadata(ContainerMessage cm) throws IOException, OxalisContentException {
        try (InputStream content = storage.get(cm.getFileName())) {
            return metadataExtractor.extract(content);
        }
    }

    private String replace(String original, String key, String value) {
        if (value == null) {
            value = "";
        }
        value = StringEscapeUtils.escapeXml10(value);
        value = StringUtils.replace(value, "&apos;", "'"); // requested by Sweden
        value = StringUtils.replace(value, "&quot;", "\"");
        return StringUtils.replace(original, "${" + key + "}", value);
    }

}
