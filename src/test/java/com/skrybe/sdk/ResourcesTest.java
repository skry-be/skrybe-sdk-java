package com.skrybe.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skrybe.sdk.exception.ApiException;
import com.skrybe.sdk.exception.InvalidRequestException;
import com.skrybe.sdk.model.Autoresponder;
import com.skrybe.sdk.model.Brand;
import com.skrybe.sdk.model.Campaign;
import com.skrybe.sdk.model.CampaignDetail;
import com.skrybe.sdk.model.CreateCampaignResult;
import com.skrybe.sdk.model.SendEmailResult;
import com.skrybe.sdk.model.SendTransactionalResult;
import com.skrybe.sdk.model.SkrybeList;
import com.skrybe.sdk.model.SubscribeOutcome;
import com.skrybe.sdk.model.Template;
import com.skrybe.sdk.params.ActivityType;
import com.skrybe.sdk.params.AutoresponderType;
import com.skrybe.sdk.params.CampaignActivityParams;
import com.skrybe.sdk.params.CampaignStatus;
import com.skrybe.sdk.params.CreateAutoresponderEmailParams;
import com.skrybe.sdk.params.CreateAutoresponderParams;
import com.skrybe.sdk.params.CreateCampaignParams;
import com.skrybe.sdk.params.CustomFieldType;
import com.skrybe.sdk.params.ListCampaignsParams;
import com.skrybe.sdk.params.OptIn;
import com.skrybe.sdk.params.Recipients;
import com.skrybe.sdk.params.ScheduleCampaignParams;
import com.skrybe.sdk.params.SendEmailParams;
import com.skrybe.sdk.params.SendTransactionalParams;
import com.skrybe.sdk.params.SubscribeParams;
import com.skrybe.sdk.params.SubscriberStatus;
import com.skrybe.sdk.params.TemplateEditor;
import com.skrybe.sdk.params.TemplateParams;
import com.skrybe.sdk.params.Tracking;
import com.skrybe.sdk.params.UpdateCampaignParams;
import com.skrybe.sdk.params.UpdateListParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcesTest {
    private static final String KEY = "test-api-key";
    private static final ObjectMapper JSON = new ObjectMapper();

    private static Skrybe client(Stub stub) {
        return Skrybe.builder(KEY).baseUrl(stub.url).build();
    }

    /** The fields of the only request, less the api key every request carries. */
    private static Map<String, String> sent(Stub stub) {
        Map<String, String> fields = new LinkedHashMap<>(stub.only().fields);
        assertEquals(KEY, fields.remove("api_key"));
        return fields;
    }

    private static Map<String, String> map(String... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return m;
    }

    // ---------------------------------------------------------------- emails

    @Test
    void sendPostsRecipientsAsJsonStringsInsideAFormBody() throws IOException {
        try (Stub stub = Stub.start("{\"ok\":true,\"message\":\"Queued\",\"data\":{\"campaign_id\":\"c1\"}}")) {
            SendEmailResult result = client(stub).emails().send(SendEmailParams.builder()
                    .fromName("Ada")
                    .fromEmail("ada@example.com")
                    .replyTo("ada@example.com")
                    .subject("Hi")
                    .htmlText("<p>Hi [recipient.first]</p>")
                    .to("a@example.com")
                    .recipientVariable("a@example.com", "first", "A")
                    .trackOpens(Tracking.ON)
                    .build());

            assertEquals("Queued", result.getMessage());
            assertEquals("c1", result.getCampaignId());
            assertEquals("/api/emails/send.php", stub.only().path);
            assertEquals(map(
                    "from_name", "Ada",
                    "from_email", "ada@example.com",
                    "reply_to", "ada@example.com",
                    "subject", "Hi",
                    "html_text", "<p>Hi [recipient.first]</p>",
                    "to", "[\"a@example.com\"]",
                    "recipient-variables", "{\"a@example.com\":{\"first\":\"A\"}}",
                    "track_opens", "1"), sent(stub));
        }
    }

    @Test
    void sendRefusesBeforeAnyRequestWithNobodyOrTooManyToSendTo() throws IOException {
        try (Stub stub = Stub.start("{}")) {
            Skrybe skrybe = client(stub);
            SendEmailParams.Builder base = SendEmailParams.builder().fromName("a").fromEmail("b").replyTo("c").subject("d").htmlText("e");

            InvalidRequestException none = assertThrows(InvalidRequestException.class, () -> skrybe.emails().send(base.build()));
            assertEquals("recipients_missing", none.getErrorCode());

            List<String> many = new ArrayList<>();
            for (int i = 0; i < 251; i++) {
                many.add("u" + i + "@example.com");
            }
            InvalidRequestException tooMany = assertThrows(InvalidRequestException.class, () -> skrybe.emails().send(base.to(many).build()));
            assertEquals("too_many_recipients", tooMany.getErrorCode());
            assertEquals(0, stub.received.size());
        }
    }

    @Test
    void sendAcceptsExactly250AndListIds() throws IOException {
        try (Stub stub = Stub.start("{\"ok\":true}")) {
            List<String> max = new ArrayList<>();
            for (int i = 0; i < 250; i++) {
                max.add("u" + i + "@example.com");
            }
            SendEmailResult r = client(stub).emails().send(SendEmailParams.builder()
                    .fromName("a").fromEmail("b").replyTo("c").subject("d").htmlText("e").to(max).listIds("L1", "L2").build());
            assertEquals("Sent.", r.getMessage());
            assertNull(r.getCampaignId());
            assertEquals("L1,L2", stub.only().fields.get("list_ids"));
        }
    }

    @Test
    void missingRequiredParamsAreCaughtBeforeSending() throws IOException {
        try (Stub stub = Stub.start("{}")) {
            Skrybe skrybe = client(stub);
            InvalidRequestException e = assertThrows(InvalidRequestException.class,
                    () -> skrybe.campaigns().create(CreateCampaignParams.builder().title("t").build()));
            assertEquals("missing_params", e.getErrorCode());
            assertEquals("Missing required parameters: fromName, fromEmail, replyTo, subject, htmlText.", e.getMessage());

            List<Executable> calls = List.of(
                    () -> skrybe.emails().send(SendEmailParams.builder().to("a@b.c").build()),
                    () -> skrybe.emails().sendTransactional(SendTransactionalParams.builder().plainBody("x").build()),
                    () -> skrybe.campaigns().schedule(1, ScheduleCampaignParams.builder().listIds("A").build()),
                    () -> skrybe.templates().create(TemplateParams.builder().name("x").build()),
                    () -> skrybe.autoresponders().create(CreateAutoresponderParams.builder().name("x").listId("A").build()),
                    () -> skrybe.autoresponders().createEmail(1, CreateAutoresponderEmailParams.builder().subject("s").build()),
                    () -> skrybe.subscribers().subscribe(SubscribeParams.builder().email("a@b.c").listId("").build()),
                    () -> skrybe.lists().get(null),
                    () -> skrybe.campaigns().send(1, null));
            for (Executable call : calls) {
                assertEquals("missing_params", assertThrows(InvalidRequestException.class, call).getErrorCode());
            }
            assertEquals(0, stub.received.size());
        }
    }

    @Test
    void sendTransactionalPostsRawJson() throws Exception {
        try (Stub stub = Stub.start("{\"status\":\"success\",\"message\":\"Sent\",\"message_id\":\"m1\"}")) {
            SendTransactionalResult result = client(stub).emails().sendTransactional(SendTransactionalParams.builder()
                    .toEmail("a@example.com").subject("OTP").plainBody("1234").build());

            assertEquals("Sent", result.getMessage());
            assertEquals("m1", result.getMessageId());
            assertEquals("/api/emails/send-transactional.php", stub.only().path);
            assertEquals("application/json", stub.only().contentType);
            assertEquals(map("to_email", "a@example.com", "subject", "OTP", "plain_body", "1234", "api_key", KEY),
                    JSON.readValue(stub.only().rawBody, Map.class));
        }
    }

    @Test
    void sendTransactionalNeedsABody() throws IOException {
        try (Stub stub = Stub.start("{}")) {
            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> client(stub).emails()
                    .sendTransactional(SendTransactionalParams.builder().toEmail("a@b.c").subject("s").build()));
            assertEquals("body_missing", e.getErrorCode());
            assertEquals(0, stub.received.size());
        }
    }

    @Test
    void jsonEndpointsReportANonJsonBodyAsMalformed() throws IOException {
        try (Stub stub = Stub.start("<html>oops</html>")) {
            ApiException e = assertThrows(ApiException.class, () -> client(stub).emails()
                    .sendTransactional(SendTransactionalParams.builder().toEmail("a@b.c").subject("s").htmlBody("h").build()));
            assertEquals("malformed_response", e.getErrorCode());
            assertEquals(200, e.getStatus());
            assertEquals("<html>oops</html>", e.getRaw());
        }
    }

    // ------------------------------------------------------------- campaigns

    private static CreateCampaignParams.Builder draft() {
        return CreateCampaignParams.builder().fromName("a").fromEmail("b@c.d").replyTo("b@c.d").title("t").subject("s").htmlText("h");
    }

    @Test
    void createReturnsTheNewIdFromTheJsonAnswer() throws IOException {
        try (Stub stub = Stub.start("{\"status\":\"Campaign created\",\"campaign_id\":\"42\"}")) {
            CreateCampaignResult r = client(stub).campaigns().create(draft().listIds("L1", "L2").build());
            assertEquals(CreateCampaignResult.Outcome.CREATED, r.getOutcome());
            assertEquals("42", r.getCampaignId());
            Map<String, String> fields = sent(stub);
            assertEquals("/api/campaigns/create.php", stub.only().path);
            assertEquals("1", fields.get("json"));
            assertEquals("L1,L2", fields.get("list_ids"));
            assertFalse(fields.containsKey("send_campaign"));
        }
    }

    @Test
    void createSpellsExcludeSegmentsAsCreatePhpReadsIt() throws IOException {
        try (Stub stub = Stub.start("Campaign created")) {
            CreateCampaignResult r = client(stub).campaigns().create(draft()
                    .segmentIds(3, 4).excludeListIds("X").excludeSegmentIds(9).trackClicks(Tracking.ANONYMOUS).build());
            assertEquals(CreateCampaignResult.Outcome.CREATED, r.getOutcome());
            assertNull(r.getCampaignId());
            Map<String, String> fields = sent(stub);
            assertEquals("3,4", fields.get("segment_ids"));
            assertEquals("X", fields.get("exclude_list_ids"));
            assertEquals("9", fields.get("exclude_segments_ids"));
            assertFalse(fields.containsKey("exclude_segment_ids"));
            assertEquals("2", fields.get("track_clicks"));
        }
    }

    @Test
    void createReportsSendingAndScheduledOutcomes() throws IOException {
        try (Stub stub = Stub.start("Campaign created and now sending")) {
            assertEquals(CreateCampaignResult.Outcome.SENDING,
                    client(stub).campaigns().create(draft().listIds("L").send(true).build()).getOutcome());
            assertEquals("1", sent(stub).get("send_campaign"));
        }
        try (Stub stub = Stub.start("Campaign scheduled")) {
            assertEquals(CreateCampaignResult.Outcome.SCHEDULED,
                    client(stub).campaigns().create(draft().scheduleDateTime("June 1, 2027 6:05pm").build()).getOutcome());
        }
    }

    @Test
    void createDoesNotMistakeAnUnknownBodyForSuccess() throws IOException {
        try (Stub stub = Stub.start("<html>Bad gateway</html>")) {
            ApiException e = assertThrows(ApiException.class, () -> client(stub).campaigns().create(draft().build()));
            assertEquals("unexpected_response", e.getErrorCode());
        }
    }

    @Test
    void listReadsNumberedRecordsAndNoCampaignsFoundAsEmpty() throws IOException {
        try (Stub stub = Stub.start("{\"campaign1\":{\"id\":3,\"title\":\"T\",\"status\":\"sent\",\"sent_at\":1700000000,\"scheduled_at\":null}}")) {
            List<Campaign> rows = client(stub).campaigns().list(ListCampaignsParams.builder().status(CampaignStatus.SENT).limit(10).build());
            assertEquals(1, rows.size());
            assertEquals(3, rows.get(0).getId());
            assertEquals("T", rows.get(0).getTitle());
            assertEquals("sent", rows.get(0).getStatus());
            assertEquals(1700000000L, rows.get(0).getSentAt());
            assertNull(rows.get(0).getScheduledAt());
            assertEquals(map("limit", "10", "status", "sent"), sent(stub));
            assertEquals("/api/campaigns/get-campaigns.php", stub.only().path);
        }
        try (Stub stub = Stub.start("No campaigns found")) {
            assertTrue(client(stub).campaigns().list().isEmpty());
        }
    }

    private static String campaignsPage(int n) {
        StringBuilder out = new StringBuilder("{");
        for (int i = 0; i < n; i++) {
            out.append(i == 0 ? "" : ",").append("\"campaign").append(i + 1).append("\":{\"id\":").append(i).append('}');
        }
        return out.append('}').toString();
    }

    @Test
    void listAllWalksPagesUntilAShortOne() throws IOException {
        try (Stub stub = Stub.start(campaignsPage(100), campaignsPage(3))) {
            assertEquals(103, client(stub).campaigns().listAll().size());
            assertEquals(2, stub.received.size());
            assertEquals("1", stub.received.get(0).fields.get("page"));
            assertEquals("2", stub.received.get(1).fields.get("page"));
            assertEquals("100", stub.received.get(1).fields.get("limit"));
        }
    }

    @Test
    void sendAndSchedulePassRecipientsAndDryRunOnlyWhenAsked() throws IOException {
        try (Stub stub = Stub.start("{\"status\":\"sending\",\"campaign_id\":5,\"recipients\":10}")) {
            var r = client(stub).campaigns().send(5, Recipients.builder().listIds("A").segmentIds(7).excludeListIds("B").build());
            assertEquals("sending", r.getStatus());
            assertEquals(5, r.getCampaignId());
            assertEquals(10, r.getRecipients());
            assertEquals("/api/campaigns/send.php", stub.only().path);
            assertEquals(map("campaign_id", "5", "list_ids", "A", "segment_ids", "7", "exclude_list_ids", "B"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"status\":\"dry_run\",\"campaign_id\":5,\"recipients\":10,\"scheduled_at\":1,\"timezone\":\"Africa/Lagos\"}")) {
            var r = client(stub).campaigns().schedule(5, ScheduleCampaignParams.builder()
                    .listIds("A").excludeSegmentIds(8).at("2027-06-15 18:05").timezone("Africa/Lagos").dryRun(true).build());
            assertEquals("dry_run", r.getStatus());
            assertEquals(1L, r.getScheduledAt());
            assertEquals("/api/campaigns/schedule.php", stub.only().path);
            assertEquals(map(
                    "campaign_id", "5",
                    "list_ids", "A",
                    // send.php and schedule.php read exclude_segment_ids; create.php is the odd one out.
                    "exclude_segment_ids", "8",
                    "dry_run", "1",
                    "schedule_date_time", "2027-06-15 18:05",
                    "schedule_timezone", "Africa/Lagos"), sent(stub));
        }
    }

    @Test
    void eachCampaignEndpointPostsToItsOwnPath() throws IOException {
        Map<String, Consumer<Skrybe>> calls = new LinkedHashMap<>();
        calls.put("/api/campaigns/get-campaign.php", s -> s.campaigns().get(1, true));
        calls.put("/api/campaigns/stats.php", s -> s.campaigns().stats(1));
        calls.put("/api/campaigns/test-send.php", s -> s.campaigns().testSend(1, "a@b.c", "d@e.f"));
        calls.put("/api/campaigns/unschedule.php", s -> s.campaigns().unschedule(1));
        calls.put("/api/campaigns/update-campaign.php", s -> s.campaigns().update(1, UpdateCampaignParams.builder().subject("x").build()));
        calls.put("/api/campaigns/duplicate-campaign.php", s -> s.campaigns().duplicate(1));
        calls.put("/api/campaigns/delete-campaign.php", s -> s.campaigns().delete(1));
        calls.put("/api/campaigns/get-activity.php", s -> s.campaigns().activity(1, CampaignActivityParams.builder().type(ActivityType.CLICKS).build()));
        for (Map.Entry<String, Consumer<Skrybe>> call : calls.entrySet()) {
            try (Stub stub = Stub.start("{\"campaign_id\":1}")) {
                call.getValue().accept(client(stub));
                assertEquals(call.getKey(), stub.only().path);
                assertEquals("1", stub.only().fields.get("campaign_id"));
            }
        }
    }

    @Test
    void campaignEndpointsSendTheirOwnFields() throws IOException {
        try (Stub stub = Stub.start("{\"id\":1,\"html_text\":\"<p/>\",\"list_ids\":[\"A\"],\"segment_ids\":[2],\"track_opens\":1}")) {
            CampaignDetail c = client(stub).campaigns().get(1, true);
            assertEquals(map("campaign_id", "1", "include_content", "yes"), sent(stub));
            assertEquals("<p/>", c.getHtmlText());
            assertEquals(List.of("A"), c.getListIds());
            assertEquals(List.of(2), c.getSegmentIds());
            assertEquals(1, c.getTrackOpens());
        }
        try (Stub stub = Stub.start("{\"id\":1}")) {
            client(stub).campaigns().get(1);
            assertEquals(map("campaign_id", "1"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"campaign_id\":1,\"results\":[{\"email\":\"a@b.c\",\"ok\":true,\"error\":null}]}")) {
            var r = client(stub).campaigns().testSend(1, List.of("a@b.c", "d@e.f"));
            assertEquals("a@b.c,d@e.f", sent(stub).get("emails"));
            assertTrue(r.getResults().get(0).isOk());
        }
        try (Stub stub = Stub.start("{\"id\":2}")) {
            client(stub).campaigns().update(1, UpdateCampaignParams.builder()
                    .title("T").preheader("P").fromName("F").trackOpens(Tracking.OFF).build());
            assertEquals(map("campaign_id", "1", "title", "T", "preheader", "P", "from_name", "F", "track_opens", "0"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"id\":2}")) {
            client(stub).campaigns().duplicate(1, "Copy");
            assertEquals(map("campaign_id", "1", "title", "Copy"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"campaign_id\":1,\"type\":\"opens\",\"page\":2,\"limit\":5,\"total\":0,\"activity\":[]}")) {
            client(stub).campaigns().activity(1, CampaignActivityParams.builder().page(2).limit(5).build());
            assertEquals(map("campaign_id", "1", "page", "2", "limit", "5"), sent(stub));
        }
    }

    @Test
    void activityAllStopsAtTheTotal() throws Exception {
        try (Stub stub = Stub.start(activityPage(1000, 1500), activityPage(500, 1500))) {
            assertEquals(1500, client(stub).campaigns().activityAll(1, ActivityType.OPENS).size());
            assertEquals(2, stub.received.size());
            assertEquals(map("campaign_id", "1", "type", "opens", "page", "2", "limit", "1000", "api_key", KEY),
                    stub.received.get(1).fields);
        }
    }

    private static String activityPage(int n, int total) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            rows.add(Map.of("email", "a@b.c"));
        }
        return JSON.writeValueAsString(Map.of("activity", rows, "total", total));
    }

    @Test
    void campaignsNotFoundIsAnApiException() throws IOException {
        try (Stub stub = Stub.start("Campaign does not exist")) {
            ApiException e = assertThrows(ApiException.class, () -> client(stub).campaigns().stats(999));
            assertEquals("campaign_not_found", e.getErrorCode());
        }
    }

    // ----------------------------------------------------------------- lists

    @Test
    void listSurvivesAnUnescapedQuoteInAName() throws IOException {
        try (Stub stub = Stub.start(Stub.phpNumberedPayload("list", "A1", "Q4 \"Big\" Push"))) {
            List<SkrybeList> lists = client(stub).lists().list();
            assertEquals(1, lists.size());
            assertEquals("A1", lists.get(0).getId());
            assertEquals("Q4 \"Big\" Push", lists.get(0).getName());
            assertEquals(map("include_hidden", "no"), sent(stub));
        }
    }

    @Test
    void listCanIncludeHiddenLists() throws IOException {
        try (Stub stub = Stub.start(Stub.phpNumberedPayload("list", "A1", "x"))) {
            client(stub).lists().list(true);
            assertEquals(map("include_hidden", "yes"), sent(stub));
        }
    }

    @Test
    void listReturnsEmptyForABrandWithoutLists() throws IOException {
        try (Stub stub = Stub.start("No lists found")) {
            assertTrue(client(stub).lists().list().isEmpty());
        }
    }

    @Test
    void activeSubscriberCountNeverReportsAFailureAsZero() throws IOException {
        try (Stub stub = Stub.start("<b>Warning</b>")) {
            ApiException e = assertThrows(ApiException.class, () -> client(stub).lists().activeSubscriberCount("A1"));
            assertEquals("malformed_response", e.getErrorCode());
        }
    }

    @Test
    void managesCustomFields() throws IOException {
        try (Stub stub = Stub.start("{\"id\":\"A1\"}")) {
            client(stub).lists().renameCustomField("A1", "Birthday", "DOB");
            assertEquals("/api/lists/rename-custom-field.php", stub.only().path);
            assertEquals(map("list_id", "A1", "name", "Birthday", "new_name", "DOB"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"id\":\"A1\",\"custom_fields\":[{\"name\":\"DOB\",\"type\":\"Date\"}]}")) {
            var list = client(stub).lists().addCustomField("A1", "DOB", CustomFieldType.DATE);
            assertEquals("/api/lists/add-custom-field.php", stub.only().path);
            assertEquals(map("list_id", "A1", "name", "DOB", "type", "date"), sent(stub));
            assertEquals("Date", list.getCustomFields().get(0).getType());
        }
        try (Stub stub = Stub.start("{\"id\":\"A1\"}")) {
            client(stub).lists().deleteCustomField("A1", "DOB");
            assertEquals("/api/lists/delete-custom-field.php", stub.only().path);
            assertEquals(map("list_id", "A1", "name", "DOB"), sent(stub));
        }
    }

    @Test
    void createUpdateGetAndDeleteList() throws IOException {
        try (Stub stub = Stub.start("{\"id\":\"A1\",\"name\":\"News\",\"opt_in\":\"double\",\"subscribers\":{\"active\":3}}")) {
            var list = client(stub).lists().create("News", OptIn.DOUBLE);
            assertEquals("A1", list.getId());
            assertEquals("double", list.getOptIn());
            assertEquals(3, list.getSubscribers().getActive());
            assertEquals("/api/lists/create.php", stub.only().path);
            assertEquals(map("name", "News", "opt_in", "double"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"id\":\"A1\",\"name\":\"Renamed\"}")) {
            client(stub).lists().update("A1", UpdateListParams.builder().name("Renamed").build());
            assertEquals("/api/lists/update-list.php", stub.only().path);
            assertEquals(map("list_id", "A1", "name", "Renamed"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"id\":\"A1\"}")) {
            client(stub).lists().get("A1");
            assertEquals("/api/lists/get-list.php", stub.only().path);
            assertEquals(map("list_id", "A1"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"id\":\"A1\",\"deleted\":true}")) {
            client(stub).lists().delete("A1");
            assertEquals("/api/lists/delete.php", stub.only().path);
        }
        try (Stub stub = Stub.start("List is used by a scheduled or sending campaign")) {
            ApiException e = assertThrows(ApiException.class, () -> client(stub).lists().delete("A1"));
            assertEquals("list_in_use", e.getErrorCode());
        }
    }

    // ----------------------------------------------------------- subscribers

    @Test
    void subscribePostsToTheRootEndpointInPlainTextMode() throws IOException {
        try (Stub stub = Stub.start("1")) {
            Map<String, String> custom = new LinkedHashMap<>();
            custom.put("Favourite colour", "blue");
            custom.put("email", "forged@example.com");
            SubscribeOutcome outcome = client(stub).subscribers().subscribe(SubscribeParams.builder()
                    .listId("A1")
                    .email("a@example.com")
                    .name("Ada")
                    .gdpr(true)
                    .fields(custom)
                    .build());

            assertEquals(SubscribeOutcome.SUBSCRIBED, outcome);
            assertEquals("/subscribe", stub.only().path);
            assertEquals(map(
                    "Favouritecolour", "blue",
                    "email", "a@example.com",
                    "list", "A1",
                    "boolean", "true",
                    "name", "Ada",
                    "gdpr", "true"), sent(stub));
            // Custom fields go first; the documented email overwrites a forged one in place.
            assertTrue(stub.only().rawBody.startsWith("Favouritecolour=blue&email=a%40example.com&list=A1&boolean=true"));
        }
    }

    @Test
    void subscribeSendsTheOptionalFieldsByTheirWireNames() throws IOException {
        try (Stub stub = Stub.start("true")) {
            client(stub).subscribers().subscribe(SubscribeParams.builder()
                    .listId("A1").email("a@b.c").country("NG").ipAddress("1.2.3.4").referrer("https://x.y").silent(true)
                    .field("Birth day", "1990-12-10").build());
            assertEquals(map(
                    "Birthday", "1990-12-10",
                    "email", "a@b.c",
                    "list", "A1",
                    "boolean", "true",
                    "country", "NG",
                    "ipaddress", "1.2.3.4",
                    "referrer", "https://x.y",
                    "silent", "true"), sent(stub));
        }
    }

    @Test
    void subscribeReportsAnExistingAddressRatherThanThrowing() throws IOException {
        try (Stub stub = Stub.start("Already subscribed.")) {
            assertEquals(SubscribeOutcome.ALREADY_SUBSCRIBED,
                    client(stub).subscribers().subscribe(SubscribeParams.builder().listId("A1").email("a@b.c").build()));
        }
    }

    @Test
    void unsubscribeDeleteAndStatusUseTheirOwnFieldNames() throws IOException {
        try (Stub stub = Stub.start("true")) {
            client(stub).subscribers().unsubscribe("A1", "a@b.c");
            assertEquals("/unsubscribe", stub.only().path);
            assertEquals(map("email", "a@b.c", "list", "A1", "boolean", "true"), sent(stub));
        }
        try (Stub stub = Stub.start("true")) {
            client(stub).subscribers().delete("A1", "a@b.c");
            assertEquals("/api/subscribers/delete.php", stub.only().path);
            assertEquals(map("list_id", "A1", "email", "a@b.c"), sent(stub));
        }
        try (Stub stub = Stub.start("Soft bounced\n")) {
            assertEquals("Soft bounced", client(stub).subscribers().status("A1", "a@b.c"));
            assertEquals("/api/subscribers/subscription-status.php", stub.only().path);
            assertEquals(map("email", "a@b.c", "list_id", "A1"), sent(stub));
        }
    }

    @Test
    void doesNotReportAnUnrecognisedBodyAsSuccess() throws IOException {
        try (Stub stub = Stub.start("Something new")) {
            ApiException e = assertThrows(ApiException.class, () -> client(stub).subscribers().unsubscribe("A1", "a@b.c"));
            assertEquals("unexpected_response", e.getErrorCode());
        }
        try (Stub stub = Stub.start("Email does not exist.")) {
            ApiException e = assertThrows(ApiException.class, () -> client(stub).subscribers().unsubscribe("A1", "a@b.c"));
            assertEquals("subscriber_not_found", e.getErrorCode());
        }
    }

    @Test
    void listAllSubscribersWalksPagesOf1000() throws Exception {
        try (Stub stub = Stub.start(subscribersPage(1000), subscribersPage(1))) {
            assertEquals(1001, client(stub).subscribers().listAll("A1", SubscriberStatus.ACTIVE).size());
            assertEquals(map("list_id", "A1", "status", "active", "page", "2", "limit", "1000", "api_key", KEY),
                    stub.received.get(1).fields);
            assertEquals("/api/subscribers/get-subscribers.php", stub.received.get(1).path);
        }
    }

    private static String subscribersPage(int n) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            rows.add(Map.of());
        }
        return JSON.writeValueAsString(Map.of("subscribers", rows, "total", 1001));
    }

    @Test
    void subscriberRowsCarryTheirCustomFields() throws IOException {
        String body = "{\"list_id\":\"A1\",\"status\":null,\"page\":1,\"limit\":100,\"total\":2,\"subscribers\":["
                + "{\"email\":\"a@b.c\",\"name\":\"A\",\"status\":\"active\",\"joined_at\":1700000000,\"custom_fields\":{\"Birthday\":\"1990-12-10\",\"City\":null}},"
                + "{\"email\":\"d@e.f\",\"name\":\"\",\"status\":\"bounced\",\"joined_at\":null,\"custom_fields\":[]}]}";
        try (Stub stub = Stub.start(body)) {
            var page = client(stub).subscribers().list("A1");
            assertEquals(2, page.getTotal());
            assertNull(page.getStatus());
            assertEquals("1990-12-10", page.getSubscribers().get(0).getCustomFields().get("Birthday"));
            assertTrue(page.getSubscribers().get(0).getCustomFields().containsKey("City"));
            assertTrue(page.getSubscribers().get(1).getCustomFields().isEmpty());
            assertNull(page.getSubscribers().get(1).getJoinedAt());
        }
    }

    // ------------------------------------------------------------- templates

    @Test
    void templateCreateMapsCamelCaseOntoTheApiFields() throws IOException {
        try (Stub stub = Stub.start("{\"id\":9,\"name\":\"Welcome\"}")) {
            Template t = client(stub).templates().create(TemplateParams.builder()
                    .name("Welcome").htmlText("<p/>").editor(TemplateEditor.HTML).build());
            assertEquals(9, t.getId());
            assertEquals("Welcome", t.getName());
            assertEquals("/api/templates/create-template.php", stub.only().path);
            assertEquals(map("name", "Welcome", "html_text", "<p/>", "editor", "html"), sent(stub));
        }
    }

    @Test
    void templateListGetUpdateAndDelete() throws IOException {
        try (Stub stub = Stub.start("{\"templates\":[{\"id\":1,\"name\":\"A\"}]}")) {
            List<Template> list = client(stub).templates().list();
            assertEquals(1, list.size());
            assertEquals(1, list.get(0).getId());
            assertEquals("/api/templates/get-templates.php", stub.only().path);
            assertEquals(map(), sent(stub));
        }
        try (Stub stub = Stub.start("{\"id\":1}")) {
            client(stub).templates().get(1, true);
            assertEquals("/api/templates/get-template.php", stub.only().path);
            assertEquals(map("template_id", "1", "include_content", "yes"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"id\":1}")) {
            client(stub).templates().update(1, TemplateParams.builder().name("v2").editor(TemplateEditor.DRAGDROP).build());
            assertEquals("/api/templates/update-template.php", stub.only().path);
            assertEquals(map("template_id", "1", "name", "v2", "editor", "dragdrop"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"template_id\":1,\"deleted\":true}")) {
            client(stub).templates().delete(1);
            assertEquals("/api/templates/delete-template.php", stub.only().path);
            assertEquals(map("template_id", "1"), sent(stub));
        }
    }

    // -------------------------------------------------------- autoresponders

    @Test
    void autoresponderCreatePostsTheTypeAndList() throws IOException {
        try (Stub stub = Stub.start("{\"id\":4,\"name\":\"Birthdays\",\"type\":\"annually\"}")) {
            Autoresponder a = client(stub).autoresponders().create(CreateAutoresponderParams.builder()
                    .name("Birthdays").type(AutoresponderType.ANNUALLY).listId("A1").customField("Birthday").build());
            assertEquals(4, a.getId());
            assertEquals("/api/autoresponders/create-autoresponder.php", stub.only().path);
            assertEquals(map("name", "Birthdays", "type", "annually", "list_id", "A1", "custom_field", "Birthday"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"id\":5}")) {
            client(stub).autoresponders().create(CreateAutoresponderParams.builder()
                    .name("Dates").type(AutoresponderType.ON_DATE).listId("A1").customField("D").build());
            assertEquals("on_date", sent(stub).get("type"));
        }
    }

    @Test
    void createEmailSendsEnabledOnlyWhenAsked() throws IOException {
        try (Stub stub = Stub.start("{\"id\":8,\"autoresponder_id\":4,\"when\":\"+3 days\",\"enabled\":false}")) {
            var email = client(stub).autoresponders().createEmail(4, CreateAutoresponderEmailParams.builder()
                    .subject("Day 3").htmlText("<p/>").when("+3 days").build());
            assertEquals("+3 days", email.getWhen());
            assertEquals("/api/autoresponders/create-autoresponder-email.php", stub.only().path);
            assertEquals(map("autoresponder_id", "4", "subject", "Day 3", "html_text", "<p/>", "when", "+3 days"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"id\":8,\"enabled\":true}")) {
            var email = client(stub).autoresponders().createEmail(4, CreateAutoresponderEmailParams.builder()
                    .subject("s").htmlText("h").enabled(true).trackOpens(Tracking.ON).build());
            assertTrue(email.isEnabled());
            assertEquals("1", sent(stub).get("enabled"));
        }
    }

    @Test
    void surfacesTheServersWhenValidationAsAnInvalidRequest() throws IOException {
        try (Stub stub = Stub.start("when must be immediately, or a delay such as +3 days")) {
            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> client(stub).autoresponders()
                    .createEmail(4, CreateAutoresponderEmailParams.builder().subject("s").htmlText("h").when("soon").build()));
            assertEquals("invalid_when", e.getErrorCode());
        }
    }

    @Test
    void autoresponderListGetAndStats() throws IOException {
        try (Stub stub = Stub.start("{\"autoresponders\":[{\"id\":1,\"type\":\"drip\",\"emails\":2}]}")) {
            List<Autoresponder> list = client(stub).autoresponders().list("A1");
            assertEquals(1, list.get(0).getId());
            assertEquals(2, list.get(0).getEmails());
            assertEquals("/api/autoresponders/get-autoresponders.php", stub.only().path);
            assertEquals(map("list_id", "A1"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"autoresponders\":[]}")) {
            client(stub).autoresponders().list();
            assertEquals(map(), sent(stub));
        }
        try (Stub stub = Stub.start("{\"id\":1,\"email_list\":[{\"id\":8,\"created_at\":null}]}")) {
            var a = client(stub).autoresponders().get(1);
            assertEquals(8, a.getEmailList().get(0).getId());
            assertEquals("/api/autoresponders/get-autoresponder-emails.php", stub.only().path);
            assertEquals(map("autoresponder_id", "1"), sent(stub));
        }
        try (Stub stub = Stub.start("{\"id\":8,\"autoresponder_id\":1,\"recipients\":10,\"opens\":{\"total\":4,\"unique\":3,\"rate\":30.5},"
                + "\"links\":[{\"url\":\"https://x\",\"clicks\":2,\"unique_clicks\":1}]}")) {
            var s = client(stub).autoresponders().emailStats(8);
            assertEquals(30.5, s.getOpens().getRate());
            assertEquals(1, s.getLinks().get(0).getUniqueClicks());
            assertEquals("/api/autoresponders/autoresponder-stats.php", stub.only().path);
            assertEquals(map("email_id", "8"), sent(stub));
        }
    }

    // -------------------------------------------------------- brands, stats

    @Test
    void brandsListReadsTheNumberedPayload() throws IOException {
        try (Stub stub = Stub.start(Stub.phpNumberedPayload("brand", "86", "Cafe One"))) {
            List<Brand> brands = client(stub).brands().list();
            assertEquals(1, brands.size());
            assertEquals("86", brands.get(0).getId());
            assertEquals("Cafe One", brands.get(0).getName());
            assertEquals("/api/brands/get-brands.php", stub.only().path);
        }
        try (Stub stub = Stub.start("No brands found")) {
            assertTrue(client(stub).brands().list().isEmpty());
        }
    }

    @Test
    void totalEmailsSentIsAGetWithoutTheApiKey() throws IOException {
        try (Stub stub = Stub.start("{\"total_emails_sent\":123}")) {
            assertEquals(123, client(stub).stats().totalEmailsSent());
            assertEquals("GET", stub.only().method);
            assertEquals("/api/stats/emails-sent.php", stub.only().path);
            assertTrue(stub.only().fields.isEmpty());
        }
    }

    // ---------------------------------------------------------------- models

    @Test
    void modelsKeepFieldsTheyDoNotModel() throws IOException {
        try (Stub stub = Stub.start("{\"id\":1,\"name\":\"T\",\"brand_new_field\":{\"a\":1}}")) {
            Template t = client(stub).templates().get(1);
            assertEquals(Map.of("a", 1), t.getExtra().get("brand_new_field"));
            assertTrue(t.toString().contains("\"brand_new_field\""));
        }
    }

    @Test
    void aWrongShapeIsMalformedNotAJacksonError() throws IOException {
        try (Stub stub = Stub.start("{\"id\":\"not a number\"}")) {
            ApiException e = assertThrows(ApiException.class, () -> client(stub).templates().get(1));
            assertEquals("malformed_response", e.getErrorCode());
        }
    }
}
