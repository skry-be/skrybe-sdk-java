package com.skrybe.sdk.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.skrybe.sdk.exception.InvalidRequestException;
import com.skrybe.sdk.internal.Errors;
import com.skrybe.sdk.internal.Fields;
import com.skrybe.sdk.internal.Json;
import com.skrybe.sdk.internal.Transport;
import com.skrybe.sdk.model.SendEmailResult;
import com.skrybe.sdk.model.SendTransactionalResult;
import com.skrybe.sdk.params.SendEmailParams;
import com.skrybe.sdk.params.SendTransactionalParams;

import java.util.List;

/** Transactional and bulk email. */
public final class Emails {
    /** send.php refuses more than this many {@code to} addresses, and says so with a 400. */
    public static final int MAX_RECIPIENTS = 250;

    private final Transport transport;

    /**
     * Internal: get one from {@code Skrybe.emails()}.
     *
     * @param transport the client's transport
     */
    public Emails(Transport transport) {
        this.transport = transport;
    }

    /**
     * {@code api/emails/send.php}: a bulk send to addresses or lists. It creates
     * a campaign behind the scenes; the subject doubles as its title. Never
     * retried: a repeat could send twice.
     *
     * @return the API's message and the campaign id
     * @throws InvalidRequestException {@code missing_params}, {@code recipients_missing} or
     *     {@code too_many_recipients}, before anything is sent
     */
    public SendEmailResult send(SendEmailParams params) {
        Errors.requireParams("params", params);
        Errors.requireParams(
                "fromName", params.getFromName(),
                "fromEmail", params.getFromEmail(),
                "replyTo", params.getReplyTo(),
                "subject", params.getSubject(),
                "htmlText", params.getHtmlText());
        List<String> to = params.getTo() == null ? List.of() : params.getTo();
        List<String> listIds = params.getListIds() == null ? List.of() : params.getListIds();
        if (to.isEmpty() && listIds.isEmpty()) {
            throw new InvalidRequestException(
                    "recipients_missing", "Nothing to send to.", "Pass `to` addresses, or `listIds`.", null, null, null);
        }
        // Checked here so a 251-address send fails before it is transmitted.
        if (to.size() > MAX_RECIPIENTS) {
            throw new InvalidRequestException(
                    "too_many_recipients",
                    to.size() + " recipients is more than the " + MAX_RECIPIENTS + " this endpoint accepts.",
                    "Split the send, or put the addresses on a list and pass `listIds`.",
                    null, null, null);
        }

        JsonNode data = transport.json("api/emails/send.php", new Fields()
                .with("from_name", params.getFromName())
                .with("from_email", params.getFromEmail())
                .with("reply_to", params.getReplyTo())
                .with("subject", params.getSubject())
                .with("html_text", params.getHtmlText())
                .with("plain_text", params.getPlainText())
                // Both go over the wire as JSON strings inside a form body.
                .with("to", to.isEmpty() ? null : Json.write(to))
                .with("recipient-variables", params.getRecipientVariables() == null || params.getRecipientVariables().isEmpty()
                        ? null : Json.write(params.getRecipientVariables()))
                .with("list_ids", Fields.joinIds(listIds))
                .with("query_string", params.getQueryString())
                .with("track_opens", params.getTrackOpens())
                .with("track_clicks", params.getTrackClicks())
                .with("schedule_date_time", params.getScheduleDateTime())
                .with("schedule_timezone", params.getScheduleTimezone()),
                "POST", Transport.Encoding.FORM, false, false);
        return new SendEmailResult(text(data.get("message"), "Sent."), text(data.path("data").get("campaign_id"), null));
    }

    /**
     * {@code api/emails/send-transactional.php}: one email, delivered
     * immediately with nothing queued. For OTPs, password resets and receipts.
     * Never retried.
     *
     * @return the API's message and the message id
     * @throws InvalidRequestException {@code missing_params} or {@code body_missing}, before anything is sent
     */
    public SendTransactionalResult sendTransactional(SendTransactionalParams params) {
        Errors.requireParams("params", params);
        Errors.requireParams("toEmail", params.getToEmail(), "subject", params.getSubject());
        if (isEmpty(params.getHtmlBody()) && isEmpty(params.getPlainBody())) {
            throw new InvalidRequestException(
                    "body_missing", "A message body is required.", "Pass `htmlBody`, `plainBody`, or both.", null, null, null);
        }

        // The one endpoint that reads php://input: a form post gets "Invalid JSON payload."
        JsonNode data = transport.json("api/emails/send-transactional.php", new Fields()
                .with("to_email", params.getToEmail())
                .with("subject", params.getSubject())
                .with("html_body", params.getHtmlBody())
                .with("plain_body", params.getPlainBody())
                .with("from_name", params.getFromName())
                .with("from_email", params.getFromEmail())
                .with("reply_to", params.getReplyTo()),
                "POST", Transport.Encoding.JSON, false, false);
        return new SendTransactionalResult(text(data.get("message"), "Email sent successfully."), text(data.get("message_id"), null));
    }

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /** A string field's value, a number's digits, or the fallback. */
    private static String text(JsonNode node, String fallback) {
        if (node == null || node.isNull() || node.isMissingNode() || node.isContainerNode()) {
            return fallback;
        }
        return node.asText();
    }
}
