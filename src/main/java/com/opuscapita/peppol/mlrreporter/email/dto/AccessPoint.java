package com.opuscapita.peppol.mlrreporter.email.dto;

import java.io.Serializable;

public class AccessPoint implements Serializable {

    private String id;
    private String name;
    private String subject;
    private String emailList;
    private String contactPerson;

    public AccessPoint() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getEmailList() {
        return emailList;
    }

    public void setEmailList(String emailList) {
        this.emailList = emailList;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    @Override
    public String toString() {
        return "AccessPoint {id='" + id + "', name='" + name + "'}";
    }

}
