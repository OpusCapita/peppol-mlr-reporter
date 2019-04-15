package com.opuscapita.peppol.mlrreporter.creator;

/**
 * Specification: http://www.unece.org/fileadmin/DAM/trade/edifact/code/4343cl.htm
 */
public enum MlrType {

    RE("rejected"),
    ER("error"),
    AB("acknowledged"),
    AP("accepted");

    private String explanation;

    MlrType(String explanation) {
        this.explanation = explanation;
    }

    public String getExplanation() {
        return explanation;
    }
}
