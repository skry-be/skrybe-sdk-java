package com.skrybe.sdk.model;

/** What {@code emails().sendTransactional()} did. */
public final class SendTransactionalResult {
    private final String message;
    private final String messageId;

    /**
     * @param message the API's message
     * @param messageId the message id, or null
     */
    public SendTransactionalResult(String message, String messageId) {
        this.message = message;
        this.messageId = messageId;
    }

    /** @return the API's message */
    public String getMessage() {
        return message;
    }

    /** @return the message id, or null when the API did not say */
    public String getMessageId() {
        return messageId;
    }

    @Override
    public String toString() {
        return "SendTransactionalResult{message=" + message + ", messageId=" + messageId + "}";
    }
}
