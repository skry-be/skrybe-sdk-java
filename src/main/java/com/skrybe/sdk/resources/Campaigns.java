package com.skrybe.sdk.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.skrybe.sdk.exception.ApiException;
import com.skrybe.sdk.internal.Errors;
import com.skrybe.sdk.internal.Fields;
import com.skrybe.sdk.internal.Json;
import com.skrybe.sdk.internal.Parse;
import com.skrybe.sdk.internal.Text;
import com.skrybe.sdk.internal.Transport;
import com.skrybe.sdk.model.Campaign;
import com.skrybe.sdk.model.CampaignActivity;
import com.skrybe.sdk.model.CampaignActivityPage;
import com.skrybe.sdk.model.CampaignDetail;
import com.skrybe.sdk.model.CampaignStats;
import com.skrybe.sdk.model.CreateCampaignResult;
import com.skrybe.sdk.model.ScheduleCampaignResult;
import com.skrybe.sdk.model.SendCampaignResult;
import com.skrybe.sdk.model.TestSendResult;
import com.skrybe.sdk.params.ActivityType;
import com.skrybe.sdk.params.CampaignActivityParams;
import com.skrybe.sdk.params.CampaignStatus;
import com.skrybe.sdk.params.CreateCampaignParams;
import com.skrybe.sdk.params.ListCampaignsParams;
import com.skrybe.sdk.params.Recipients;
import com.skrybe.sdk.params.ScheduleCampaignParams;
import com.skrybe.sdk.params.UpdateCampaignParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** The campaign lifecycle: create, send, schedule, edit, and report. */
public final class Campaigns {
    /** The largest page {@code list()} returns. */
    public static final int MAX_PAGE = 100;
    /** The largest page {@code activity()} returns. */
    public static final int MAX_ACTIVITY_PAGE = 1000;
    /** test-send.php refuses more than this many addresses per request. */
    public static final int MAX_TEST_EMAILS = 5;

    private final Transport transport;

    /**
     * Internal: get one from {@code Skrybe.campaigns()}.
     *
     * @param transport the client's transport
     */
    public Campaigns(Transport transport) {
        this.transport = transport;
    }

    /**
     * {@code api/campaigns/create.php} creates a draft, and optionally sends or
     * schedules it in the same call. To send a draft that already exists, see
     * {@link #send(int, Recipients)}. Never retried.
     *
     * @return the outcome, with the new id for a plain create
     */
    public CreateCampaignResult create(CreateCampaignParams params) {
        Errors.requireParams("params", params);
        Errors.requireParams(
                "fromName", params.getFromName(),
                "fromEmail", params.getFromEmail(),
                "replyTo", params.getReplyTo(),
                "title", params.getTitle(),
                "subject", params.getSubject(),
                "htmlText", params.getHtmlText());
        String text = transport.text("api/campaigns/create.php", new Fields()
                .with("from_name", params.getFromName())
                .with("from_email", params.getFromEmail())
                .with("reply_to", params.getReplyTo())
                .with("title", params.getTitle())
                .with("subject", params.getSubject())
                .with("html_text", params.getHtmlText())
                .with("plain_text", params.getPlainText())
                .with("list_ids", Fields.joinIds(params.getListIds()))
                .with("segment_ids", Fields.joinIds(params.getSegmentIds()))
                .with("exclude_list_ids", Fields.joinIds(params.getExcludeListIds()))
                // Sic: create.php reads exclude_segments_ids.
                .with("exclude_segments_ids", Fields.joinIds(params.getExcludeSegmentIds()))
                .with("query_string", params.getQueryString())
                .with("track_opens", params.getTrackOpens())
                .with("track_clicks", params.getTrackClicks())
                .with("send_campaign", Fields.flag(params.isSend()))
                .with("schedule_date_time", params.getScheduleDateTime())
                .with("schedule_timezone", params.getScheduleTimezone())
                // Asks for {"status": ..., "campaign_id": ...} instead of bare prose.
                .with("json", 1), false);
        String trimmed = Text.trim(text);

        if (trimmed.equals("Campaign scheduled")) {
            return new CreateCampaignResult(CreateCampaignResult.Outcome.SCHEDULED, null);
        }
        if (trimmed.equals("Campaign created and now sending")) {
            return new CreateCampaignResult(CreateCampaignResult.Outcome.SENDING, null);
        }
        if (trimmed.equals("Campaign created")) {
            return new CreateCampaignResult(CreateCampaignResult.Outcome.CREATED, null);
        }

        // The json=1 shape, built by hand server-side, so parsed leniently.
        if (trimmed.startsWith("{")) {
            JsonNode parsed = Json.tryParse(trimmed);
            if (parsed != null && parsed.isObject()) {
                JsonNode status = parsed.get("status");
                if (status != null && status.isTextual() && status.asText().startsWith("Campaign created")) {
                    JsonNode id = parsed.get("campaign_id");
                    return new CreateCampaignResult(
                            CreateCampaignResult.Outcome.CREATED,
                            id == null || id.isNull() || id.isContainerNode() ? null : id.asText());
                }
            }
        }

        throw new ApiException(
                "unexpected_response", "Unexpected response from campaigns/create: " + Text.excerpt(trimmed), null, null, text, null);
    }

    /** @return the first page, newest first */
    public List<Campaign> list() {
        return list(ListCampaignsParams.builder().build());
    }

    /** @return one page, newest first. An empty page is an empty list, not an error. */
    public List<Campaign> list(ListCampaignsParams params) {
        ListCampaignsParams p = params == null ? ListCampaignsParams.builder().build() : params;
        String body = transport.text("api/campaigns/get-campaigns.php", new Fields()
                .with("page", p.getPage())
                .with("limit", p.getLimit())
                .with("status", p.getStatus()), true);
        if (Errors.isProseEmpty(body)) {
            return new ArrayList<>();
        }
        List<Campaign> out = new ArrayList<>();
        for (JsonNode row : Parse.numberedRecords(body, "campaign")) {
            out.add(Parse.bind(row, "campaign payload", body, Campaign.class));
        }
        return out;
    }

    /** @return every campaign, fetched page by page at the largest page size */
    public List<Campaign> listAll() {
        return listAll(null);
    }

    /** @return every campaign with the status (or every campaign, for null), fetched page by page */
    public List<Campaign> listAll(CampaignStatus status) {
        List<Campaign> all = new ArrayList<>();
        for (int page = 1; ; page++) {
            List<Campaign> rows = list(ListCampaignsParams.builder().page(page).limit(MAX_PAGE).status(status).build());
            all.addAll(rows);
            if (rows.size() < MAX_PAGE) {
                return all;
            }
        }
    }

    /** @return the campaign with its settings, without its content */
    public CampaignDetail get(int campaignId) {
        return get(campaignId, false);
    }

    /** @return the campaign with its settings, and its HTML and plain text when includeContent is true */
    public CampaignDetail get(int campaignId, boolean includeContent) {
        String body = transport.text("api/campaigns/get-campaign.php", new Fields()
                .with("campaign_id", campaignId)
                .with("include_content", includeContent ? "yes" : null), true);
        return Parse.jsonObject(body, "campaign", CampaignDetail.class);
    }

    /** @return opens, clicks, bounces, complaints, unsubscribes and per-link clicks */
    public CampaignStats stats(int campaignId) {
        String body = transport.text("api/campaigns/stats.php", new Fields().with("campaign_id", campaignId), true);
        return Parse.jsonObject(body, "campaign stats", CampaignStats.class);
    }

    /**
     * {@code api/campaigns/send.php} sends an existing draft. It only ever
     * claims a draft, so a repeat after a lost response answers "Campaign has
     * already been sent" rather than sending twice. It is still not retried:
     * that answer would turn a success into a reported failure.
     */
    public SendCampaignResult send(int campaignId, Recipients recipients) {
        Errors.requireParams("recipients", recipients);
        Fields fields = new Fields().with("campaign_id", campaignId);
        recipientFields(fields, recipients.getListIds(), recipients.getSegmentIds(),
                recipients.getExcludeListIds(), recipients.getExcludeSegmentIds(), recipients.isDryRun());
        String body = transport.text("api/campaigns/send.php", fields, false);
        return Parse.jsonObject(body, "send result", SendCampaignResult.class);
    }

    /**
     * Sends the campaign as a test to up to 5 addresses. Each request counts
     * against a per-brand limit shared with the dashboard (20 an hour), so it
     * is never retried.
     */
    public TestSendResult testSend(int campaignId, Collection<String> emails) {
        Errors.requireParams("emails", emails);
        String body = transport.text("api/campaigns/test-send.php", new Fields()
                .with("campaign_id", campaignId)
                .with("emails", String.join(",", emails)), false);
        return Parse.jsonObject(body, "test send result", TestSendResult.class);
    }

    /** As {@link #testSend(int, Collection)}. */
    public TestSendResult testSend(int campaignId, String... emails) {
        return testSend(campaignId, emails == null ? null : Arrays.asList(emails));
    }

    /**
     * Schedules a draft, or moves a scheduled campaign. The recipients are
     * reserved against the quota now and handed back by {@link #unschedule(int)}.
     */
    public ScheduleCampaignResult schedule(int campaignId, ScheduleCampaignParams params) {
        Errors.requireParams("params", params);
        Errors.requireParams("at", params.getAt());
        Fields fields = new Fields().with("campaign_id", campaignId);
        recipientFields(fields, params.getListIds(), params.getSegmentIds(),
                params.getExcludeListIds(), params.getExcludeSegmentIds(), params.isDryRun());
        fields.with("schedule_date_time", params.getAt()).with("schedule_timezone", params.getTimezone());
        String body = transport.text("api/campaigns/schedule.php", fields, false);
        return Parse.jsonObject(body, "schedule result", ScheduleCampaignResult.class);
    }

    /** Back to a draft, with its quota reservation refunded. */
    public void unschedule(int campaignId) {
        String body = transport.text("api/campaigns/unschedule.php", new Fields().with("campaign_id", campaignId), false);
        Parse.jsonObject(body, "unschedule result");
    }

    /**
     * Edits a draft or scheduled campaign. Only the fields set change.
     *
     * @return the campaign as {@link #get(int)} would return it
     */
    public CampaignDetail update(int campaignId, UpdateCampaignParams changes) {
        Errors.requireParams("changes", changes);
        String body = transport.text("api/campaigns/update-campaign.php", new Fields()
                .with("campaign_id", campaignId)
                .with("title", changes.getTitle())
                .with("subject", changes.getSubject())
                .with("preheader", changes.getPreheader())
                .with("from_name", changes.getFromName())
                .with("from_email", changes.getFromEmail())
                .with("reply_to", changes.getReplyTo())
                .with("html_text", changes.getHtmlText())
                .with("plain_text", changes.getPlainText())
                .with("query_string", changes.getQueryString())
                .with("track_opens", changes.getTrackOpens())
                .with("track_clicks", changes.getTrackClicks()), false);
        return Parse.jsonObject(body, "campaign", CampaignDetail.class);
    }

    /** @return a new draft copied from the campaign, in the same brand */
    public CampaignDetail duplicate(int campaignId) {
        return duplicate(campaignId, null);
    }

    /** @return a new draft copied from the campaign, with the title given (or a default one, for null) */
    public CampaignDetail duplicate(int campaignId, String title) {
        String body = transport.text("api/campaigns/duplicate-campaign.php", new Fields()
                .with("campaign_id", campaignId)
                .with("title", title), false);
        return Parse.jsonObject(body, "campaign", CampaignDetail.class);
    }

    /**
     * Deletes the campaign with its links and attachments; a sent one loses its
     * report. Refused while the campaign is preparing or sending.
     */
    public void delete(int campaignId) {
        String body = transport.text("api/campaigns/delete-campaign.php", new Fields().with("campaign_id", campaignId), false);
        Parse.jsonObject(body, "delete result");
    }

    /** @return the first page of opens */
    public CampaignActivityPage activity(int campaignId) {
        return activity(campaignId, CampaignActivityParams.builder().build());
    }

    /**
     * A page of who opened, clicked, bounced, complained or unsubscribed. Opens
     * and clicks are in the order subscribers first did them.
     */
    public CampaignActivityPage activity(int campaignId, CampaignActivityParams params) {
        CampaignActivityParams p = params == null ? CampaignActivityParams.builder().build() : params;
        String body = transport.text("api/campaigns/get-activity.php", new Fields()
                .with("campaign_id", campaignId)
                .with("type", p.getType())
                .with("page", p.getPage())
                .with("limit", p.getLimit()), true);
        return Parse.jsonObject(body, "campaign activity", CampaignActivityPage.class);
    }

    /** @return every row of one activity type, fetched page by page at the largest page size */
    public List<CampaignActivity> activityAll(int campaignId, ActivityType type) {
        Errors.requireParams("type", type);
        List<CampaignActivity> all = new ArrayList<>();
        for (int page = 1; ; page++) {
            CampaignActivityPage r = activity(campaignId,
                    CampaignActivityParams.builder().type(type).page(page).limit(MAX_ACTIVITY_PAGE).build());
            all.addAll(r.getActivity());
            if (r.getActivity().isEmpty() || (long) page * MAX_ACTIVITY_PAGE >= r.getTotal()) {
                return all;
            }
        }
    }

    private static void recipientFields(
            Fields fields, List<String> listIds, List<Object> segmentIds, List<String> excludeListIds,
            List<Object> excludeSegmentIds, boolean dryRun) {
        fields.with("list_ids", Fields.joinIds(listIds))
                .with("segment_ids", Fields.joinIds(segmentIds))
                .with("exclude_list_ids", Fields.joinIds(excludeListIds))
                .with("exclude_segment_ids", Fields.joinIds(excludeSegmentIds))
                .with("dry_run", Fields.flag(dryRun));
    }
}
