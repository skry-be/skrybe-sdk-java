package com.skrybe.sdk.model;

public class CampaignResponse {
    private int statusCode;
    private String response;
    private boolean success;

    public CampaignResponse(int statusCode, String response) {
        this.statusCode = statusCode;
        this.response = response;
        this.success = statusCode >= 200 && statusCode < 300;
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

    @Override
    public String toString() {
        return "CampaignResponse{" +
                "statusCode=" + statusCode +
                ", response='" + response + '\'' +
                ", success=" + success +
                '}';
    }
} 