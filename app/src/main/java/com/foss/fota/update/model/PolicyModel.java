package com.foss.fota.update.model;

public class PolicyModel {
    private String key;
    private String message;
    private String value;

    public PolicyModel(String str, String str2, String str3) {
        this.key = str;
        this.value = str2;
        this.message = str3;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public String getMessage() {
        return this.message;
    }
}
