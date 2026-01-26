package com.skrybe.sdk.model;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * Response object for transactional email sends.
 *
 * On success: {"status": "success", "message": "Email sent successfully."}
 * On failure: {"status": "error", "message": "...", "error_info": "...", "code": 400}
 */
public class TransactionalEmailResponse {
    private int statusCode;
    private String response;
    private boolean success;
    private String status;
    private String message;
    private String errorInfo;
    private Integer errorCode;

    public TransactionalEmailResponse(int statusCode, String response) {
        this.statusCode = statusCode;
        this.response = response;

        // Parse JSON response
        if (response != null && !response.trim().isEmpty()) {
            try {
                JSONObject json = new JSONObject(response);
                this.status = json.optString("status", null);
                this.message = json.optString("message", null);
                this.errorInfo = json.optString("error_info", null);

                if (json.has("code")) {
                    this.errorCode = json.getInt("code");
                }

                this.success = "success".equalsIgnoreCase(this.status);
            } catch (JSONException e) {
                // If JSON parsing fails, fall back to status code check
                this.success = statusCode >= 200 && statusCode < 300;
            }
        } else {
            this.success = statusCode >= 200 && statusCode < 300;
        }
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

    /**
     * Returns the status from the API response ("success" or "error")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the message from the API response
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns additional error information if the request failed
     */
    public String getErrorInfo() {
        return errorInfo;
    }

    /**
     * Returns the error code from the API response (e.g., 400) if present
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "TransactionalEmailResponse{" +
                "statusCode=" + statusCode +
                ", success=" + success +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", errorInfo='" + errorInfo + '\'' +
                ", errorCode=" + errorCode +
                '}';
    }
}
