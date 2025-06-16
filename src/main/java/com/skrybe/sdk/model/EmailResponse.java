package com.skrybe.sdk.model;

public class EmailResponse {
    private int statusCode;
    private String response;
    private boolean success;
    private boolean scheduled;

    public EmailResponse(int statusCode, String response) {
        this.statusCode = statusCode;
        this.response = response;
        this.success = statusCode >= 200 && statusCode < 300;
        this.scheduled = response != null && response.toLowerCase().contains("scheduled");
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponse() {
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    @Override
    public String toString() {
        return "EmailResponse{" +
                "statusCode=" + statusCode +
                ", response='" + response + '\'' +
                ", success=" + success +
                ", scheduled=" + scheduled +
                '}';
    }
} 