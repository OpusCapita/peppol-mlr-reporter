package com.opuscapita.peppol.mlrreporter.creator;

public enum MlrStatusCode {

    SV("Syntax Violation"),
    RVF("Rule Violation Fatal"),
    RVW("Rule Violation Warning");

    private String explanation;

    MlrStatusCode(String explanation) {
        this.explanation = explanation;
    }

    public String getExplanation() {
        return explanation;
    }
}
