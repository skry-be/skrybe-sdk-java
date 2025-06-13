package com.skrybe.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skrybe.sdk.exception.SkrybeException;
import com.skrybe.sdk.exception.ValidationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Pattern;

public class SkrybeSDK {

    private static final String DEFAULT_BASE_URL = "https://dashboard.skry.be";
    private static final double MIN_REQUEST_INTERVAL = 0.1; // seconds
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\s]+@[^@\s]+\\.[^@\s]+$");

    private final String apiKey;
    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private long lastRequestTimeNanos = 0;

    public SkrybeSDK(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL, new RestTemplate());
    }

    public SkrybeSDK(String apiKey, String baseUrl) {
        this(apiKey, baseUrl, new RestTemplate());
    }

    public SkrybeSDK(String apiKey, String baseUrl, RestTemplate restTemplate) {
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey must not be null");
        this.baseUrl = Optional.ofNullable(baseUrl).orElse(DEFAULT_BASE_URL);
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate must not be null");
        this.objectMapper = new ObjectMapper();
    }

    private MultiValueMap<String, String> createFormData(Map<String, Object> data) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("api_key", apiKey);
        data.forEach((key, value) -> {
            if (value != null) {
                if (value instanceof Collection || value instanceof Map) {
                    try {
                        formData.add(key, objectMapper.writeValueAsString(value));
                    } catch (JsonProcessingException e) {
                        throw new SkrybeException("Failed to serialize field: " + key, e);
                    }
                } else {
                    formData.add(key, value.toString());
                }
            }
        });
        return formData;
    }

    private void handleRateLimit() {
        long now = System.nanoTime();
        double elapsed = (now - lastRequestTimeNanos) / 1_000_000_000.0;
        if (elapsed < MIN_REQUEST_INTERVAL) {
            try {
                long sleepMillis = (long) ((MIN_REQUEST_INTERVAL - elapsed) * 1000);
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTimeNanos = System.nanoTime();
    }

    private Object makeRequest(String endpoint, Map<String, Object> data) {
        handleRateLimit();
        String url = baseUrl + endpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", "SkrybeSDK-Java/1.0");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(createFormData(data), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String body = response.getBody();
            if (body == null || body.isEmpty()) {
                return Collections.emptyMap();
            }
            try {
                return objectMapper.readValue(body, Map.class);
            } catch (Exception ex) {
                return body;
            }
        } catch (RestClientException e) {
            throw new SkrybeException(e.getMessage(), e);
        }
    }

    private void validateEmailOptions(Map<String, Object> options) {
        List<String> required = Arrays.asList("fromName", "fromEmail", "subject", "htmlText");
        List<String> errors = new ArrayList<>();

        for (String field : required) {
            if (!options.containsKey(field) || Objects.toString(options.get(field), "").trim().isEmpty()) {
                errors.add("Field '" + field + "' is required");
            }
        }

        Object fromEmail = options.get("fromEmail");
        if (fromEmail != null && !isValidEmail(fromEmail.toString())) {
            throw new ValidationException(Collections.singletonMap("fromEmail", "Invalid email format"));
        }

        Object toField = options.get("to");
        if (toField instanceof Collection<?> emails) {
            for (Object email : emails) {
                if (email != null && !isValidEmail(email.toString())) {
                    throw new ValidationException(Collections.singletonMap("to", "Invalid email format: " + email));
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void validateCampaignOptions(Map<String, Object> options) {
        List<String> required = Arrays.asList("fromName", "fromEmail", "title", "subject", "htmlText");
        List<String> errors = new ArrayList<>();

        for (String field : required) {
            if (!options.containsKey(field) || Objects.toString(options.get(field), "").trim().isEmpty()) {
                errors.add("Field '" + field + "' is required");
            }
        }

        Object fromEmail = options.get("fromEmail");
        if (fromEmail == null || !isValidEmail(fromEmail.toString())) {
            throw new ValidationException(Collections.singletonMap("fromEmail", "Invalid email format"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    // Public API methods

    public Object sendEmail(Map<String, Object> options) {
        validateEmailOptions(options);
        Map<String, Object> payload = new HashMap<>();
        payload.put("from_name", options.get("fromName"));
        payload.put("from_email", options.get("fromEmail"));
        payload.put("reply_to", options.get("replyTo"));
        payload.put("subject", options.get("subject"));
        payload.put("html_text", options.get("htmlText"));
        payload.put("plain_text", options.get("plainText"));
        payload.put("to", options.get("to"));
        payload.put("recipient-variables", options.get("recipientVariables"));

        Object listIds = options.get("listIds");
        if (listIds instanceof Collection<?> ids) {
            payload.put("list_ids", String.join(",", ids.stream().map(Object::toString).toList()));
        }

        payload.put("query_string", options.get("queryString"));
        payload.put("track_opens", options.get("trackOpens"));
        payload.put("track_clicks", options.get("trackClicks"));
        payload.put("schedule_date_time", options.get("scheduleDateTime"));
        payload.put("schedule_timezone", options.get("scheduleTimezone"));
        return makeRequest("/api/emails/send.php", payload);
    }

    public Object createCampaign(Map<String, Object> options) {
        validateCampaignOptions(options);
        Map<String, Object> payload = new HashMap<>();
        payload.put("from_name", options.get("fromName"));
        payload.put("from_email", options.get("fromEmail"));
        payload.put("reply_to", options.get("replyTo"));
        payload.put("title", options.get("title"));
        payload.put("subject", options.get("subject"));
        payload.put("html_text", options.get("htmlText"));
        payload.put("plain_text", options.get("plainText"));
        payload.put("list_ids", options.get("listIds"));
        payload.put("segment_ids", options.get("segmentIds"));
        payload.put("exclude_list_ids", options.get("excludeListIds"));
        payload.put("exclude_segments_ids", options.get("excludeSegmentIds"));
        payload.put("query_string", options.get("queryString"));
        payload.put("track_opens", options.get("trackOpens"));
        payload.put("track_clicks", options.get("trackClicks"));
        payload.put("send_campaign", options.get("sendCampaign"));
        payload.put("schedule_date_time", options.get("scheduleDateTime"));
        payload.put("schedule_timezone", options.get("scheduleTimezone"));
        return makeRequest("/api/campaigns/create.php", payload);
    }

    public Object getLists(boolean includeHidden) {
        return makeRequest("/api/lists/get-lists.php", Map.of(
                "include_hidden", includeHidden ? "yes" : "no"
        ));
    }

    public Object getCampaigns(Map<String, Object> options) {
        if (options == null) {
            options = Collections.emptyMap();
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("page", options.getOrDefault("page", 1));
        payload.put("limit", options.getOrDefault("limit", 10));
        payload.put("status", options.get("status"));
        return makeRequest("/api/campaigns/get-campaigns.php", payload);
    }

    public Object getSubscribers(String listId, Map<String, Object> options) {
        if (options == null) {
            options = Collections.emptyMap();
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("list_id", listId);
        payload.put("page", options.getOrDefault("page", 1));
        payload.put("limit", options.getOrDefault("limit", 10));
        payload.put("status", options.get("status"));
        return makeRequest("/api/subscribers/get-subscribers.php", payload);
    }

    public Object addSubscriber(String listId, Map<String, Object> subscriberData) {
        Map<String, Object> payload = new HashMap<>(subscriberData);
        payload.put("list_id", listId);
        return makeRequest("/api/subscribers/add.php", payload);
    }
} 