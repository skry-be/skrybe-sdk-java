package com.skrybe.sdk;

import com.skrybe.sdk.exception.SkrybeException;
import com.skrybe.sdk.exception.ValidationException;
import com.skrybe.sdk.model.*;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

public class SkrybeSDK {
    private final String apiKey;
    private final String baseUrl;
    private final HttpClient httpClient;

    private static final String DEFAULT_BASE_URL = "https://dashboard.skry.be/api";

    public SkrybeSDK(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL);
    }

    public SkrybeSDK(String apiKey, String baseUrl) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        
        this.apiKey = apiKey.trim();
        this.baseUrl = baseUrl.trim();
        this.httpClient = HttpClient.newHttpClient();
    }

    private String createFormData(Map<String, Object> data) {
        StringBuilder formData = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Handle List values (for arrays like to[])
            if (value instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> list = (java.util.List<Object>) value;
                for (Object item : list) {
                    if (formData.length() > 0) {
                        formData.append("&");
                    }
                    String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
                    String encodedValue = URLEncoder.encode(String.valueOf(item), StandardCharsets.UTF_8);
                    formData.append(encodedKey).append("=").append(encodedValue);
                }
            } else {
                // Handle regular key-value pairs
                if (formData.length() > 0) {
                    formData.append("&");
                }
                String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
                String encodedValue = URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
                formData.append(encodedKey).append("=").append(encodedValue);
            }
        }
        return formData.toString();
    }

    private HttpResponse<String> makeRequest(String endpoint, Map<String, Object> data) throws SkrybeException {
        try {
            String url = baseUrl + endpoint;
            
            // Add API key to data
            if (data == null) {
                data = new HashMap<>();
            }
            data.put("api_key", apiKey);

            // Create request
            String formData = createFormData(data);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

            // Send request
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new SkrybeException("Network error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SkrybeException("Request was interrupted: " + e.getMessage(), e);
        }
    }

    /**
     * Get all lists from Skrybe
     * @return List of SkrybeList objects
     * @throws SkrybeException if the API request fails
     */
    public List<SkrybeList> getLists() throws SkrybeException {
        HttpResponse<String> response = makeRequest("/lists/get-lists.php", null);
        
        if (response.statusCode() != 200) {
            throw new SkrybeException("API request failed with status code: " + response.statusCode());
        }
        
        List<SkrybeList> result = new ArrayList<>();
        String responseBody = response.body();
        
        try {
            JSONObject responseObj = new JSONObject(responseBody);
            for (String key : responseObj.keySet()) {
                if (key.startsWith("list")) {
                    JSONObject listObj = responseObj.getJSONObject(key);
                    SkrybeList list = new SkrybeList();
                    list.setId(listObj.optString("id"));
                    list.setName(listObj.optString("name"));
                    result.add(list);
                }
            }
        } catch (JSONException e) {
            throw new SkrybeException("Failed to parse API response: " + e.getMessage(), e);
        }
        
        return result;
    }

    /**
     * Get active subscribers from a specific list
     * @param listId The ID of the list
     * @return List of Subscriber objects
     * @throws SkrybeException if the API request fails
     * @throws ValidationException if listId is invalid
     */
    public List<Subscriber> getSubscribers(String listId) throws SkrybeException, ValidationException {
        if (listId == null || listId.trim().isEmpty()) {
            throw new ValidationException("List ID cannot be null or empty");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("list_id", listId.trim());
        
        HttpResponse<String> response = makeRequest("/subscribers/get-active.php", data);
        
        if (response.statusCode() != 200) {
            throw new SkrybeException("API request failed with status code: " + response.statusCode());
        }
        
        List<Subscriber> result = new ArrayList<>();
        String responseBody = response.body();
        
        try {
            JSONObject responseObj = new JSONObject(responseBody);
            for (String key : responseObj.keySet()) {
                if (key.startsWith("subscriber")) {
                    JSONObject subscriberObj = responseObj.getJSONObject(key);
                    Subscriber subscriber = new Subscriber();
                    subscriber.setId(subscriberObj.optString("id"));
                    subscriber.setEmail(subscriberObj.optString("email"));
                    subscriber.setName(subscriberObj.optString("name"));
                    subscriber.setListId(listId);
                    result.add(subscriber);
                }
            }
        } catch (JSONException e) {
            throw new SkrybeException("Failed to parse API response: " + e.getMessage(), e);
        }
        
        return result;
    }

    /**
     * Get the active subscriber count for a specific list
     * @param listId The ID of the list
     * @return The number of active subscribers in the list
     * @throws SkrybeException if the API request fails
     * @throws ValidationException if listId is invalid
     */
    public int getActiveSubscriberCount(String listId) throws SkrybeException, ValidationException {
        if (listId == null || listId.trim().isEmpty()) {
            throw new ValidationException("List ID cannot be null or empty");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("list_id", listId.trim());
        
        HttpResponse<String> response = makeRequest("/subscribers/active-subscriber-count.php", data);
        
        if (response.statusCode() != 200) {
            throw new SkrybeException("API request failed with status code: " + response.statusCode());
        }
        
        String responseBody = response.body().trim();
        
        // Check for error responses
        if (responseBody.toLowerCase().contains("error")) {
            throw new SkrybeException("API Error: " + responseBody);
        }
        
        try {
            // The API returns a plain integer as text
            return Integer.parseInt(responseBody);
        } catch (NumberFormatException e) {
            throw new SkrybeException("Invalid response format. Expected integer but got: " + responseBody, e);
        }
    }

    /**
     * Get all campaigns
     * @return List of Campaign objects
     * @throws SkrybeException if the API request fails
     */
    public List<Campaign> getCampaigns() throws SkrybeException {
        HttpResponse<String> response = makeRequest("/campaigns/get-campaigns.php", null);
        
        if (response.statusCode() != 200) {
            throw new SkrybeException("API request failed with status code: " + response.statusCode());
        }
        
        List<Campaign> result = new ArrayList<>();
        String responseBody = response.body();
        
        try {
            JSONObject responseObj = new JSONObject(responseBody);
            for (String key : responseObj.keySet()) {
                if (key.startsWith("campaign")) {
                    JSONObject campaignObj = responseObj.getJSONObject(key);
                    Campaign campaign = new Campaign();
                    campaign.setId(campaignObj.optString("id"));
                    campaign.setTitle(campaignObj.optString("title"));
                    campaign.setFromName(campaignObj.optString("from_name"));
                    campaign.setFromEmail(campaignObj.optString("from_email"));
                    campaign.setSubject(campaignObj.optString("subject"));
                    campaign.setStatus(campaignObj.optString("status"));
                    result.add(campaign);
                }
            }
        } catch (JSONException e) {
            throw new SkrybeException("Failed to parse API response: " + e.getMessage(), e);
        }
        
        return result;
    }

    /**
     * Create a new campaign using the builder pattern
     * @param request CampaignRequest object built using the builder pattern
     * @return CampaignResponse with the result
     * @throws SkrybeException if the API request fails
     * @throws ValidationException if the request is invalid
     */
    public CampaignResponse createCampaign(CampaignRequest request) throws SkrybeException, ValidationException {
        if (request == null) {
            throw new ValidationException("Campaign request cannot be null");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("title", request.getTitle());
        data.put("from_name", request.getFromName());
        data.put("from_email", request.getFromEmail());
        data.put("reply_to", request.getReplyTo());
        data.put("subject", request.getSubject());
        data.put("html_text", request.getHtmlText());
        data.put("list_ids", String.join(",", request.getListIds()));
        
        // Optional parameters
        if (request.getPlainText() != null) {
            data.put("plain_text", request.getPlainText());
        }
        data.put("send_campaign", request.isSendCampaign() ? "1" : "0");
        data.put("track_opens", request.isTrackOpens() ? "1" : "0");
        data.put("track_clicks", request.isTrackClicks() ? "1" : "0");
        
        HttpResponse<String> response = makeRequest("/campaigns/create.php", data);
        return new CampaignResponse(response.statusCode(), response.body());
    }

    /**
     * Send an individual email
     * @param request EmailRequest object built using the builder pattern
     * @return EmailResponse with the result
     * @throws SkrybeException if the API request fails
     * @throws ValidationException if the request is invalid
     */
    public EmailResponse sendEmail(EmailRequest request) throws SkrybeException, ValidationException {
        if (request == null) {
            throw new ValidationException("Email request cannot be null");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("from_name", request.getFromName());
        data.put("from_email", request.getFromEmail());
        data.put("reply_to", request.getReplyTo());
        data.put("subject", request.getSubject());
        data.put("html_text", request.getHtmlText());
        
        // Optional plain text
        if (request.getPlainText() != null && !request.getPlainText().trim().isEmpty()) {
            data.put("plain_text", request.getPlainText());
        }
        
        // Recipients - format as JSON array (required by Skrybe API)
        if (!request.getTo().isEmpty()) {
            try {
                JSONObject toArray = new JSONObject();
                // Create JSON array format: ["email1", "email2", ...]
                StringBuilder jsonArray = new StringBuilder("[");
                for (int i = 0; i < request.getTo().size(); i++) {
                    if (i > 0) jsonArray.append(",");
                    jsonArray.append("\"").append(request.getTo().get(i)).append("\"");
                }
                jsonArray.append("]");
                data.put("to", jsonArray.toString());
            } catch (Exception e) {
                throw new ValidationException("Failed to format recipient emails: " + e.getMessage());
            }
        }
        
        // Recipient variables as JSON string if provided
        if (!request.getRecipientVariables().isEmpty()) {
            try {
                JSONObject recipientVars = new JSONObject();
                for (Map.Entry<String, Map<String, Object>> entry : request.getRecipientVariables().entrySet()) {
                    JSONObject vars = new JSONObject();
                    for (Map.Entry<String, Object> varEntry : entry.getValue().entrySet()) {
                        vars.put(varEntry.getKey(), varEntry.getValue());
                    }
                    recipientVars.put(entry.getKey(), vars);
                }
                data.put("recipient-variables", recipientVars.toString());
            } catch (JSONException e) {
                throw new ValidationException("Invalid recipient variables format: " + e.getMessage());
            }
        }
        
        // Optional list IDs
        if (!request.getListIds().isEmpty()) {
            data.put("list_ids", String.join(",", request.getListIds()));
        }
        
        // Optional query string
        if (request.getQueryString() != null && !request.getQueryString().trim().isEmpty()) {
            data.put("query_string", request.getQueryString());
        }
        
        // Tracking options
        data.put("track_opens", request.isTrackOpens() ? "1" : "0");
        data.put("track_clicks", request.isTrackClicks() ? "1" : "0");
        
        // Optional scheduling
        if (request.getScheduleDateTime() != null && !request.getScheduleDateTime().trim().isEmpty()) {
            data.put("schedule_date_time", request.getScheduleDateTime());
            
            if (request.getScheduleTimezone() != null && !request.getScheduleTimezone().trim().isEmpty()) {
                data.put("schedule_timezone", request.getScheduleTimezone());
            }
        }
        
        HttpResponse<String> response = makeRequest("/emails/send.php", data);
        return new EmailResponse(response.statusCode(), response.body());
    }

    /**
     * @deprecated Use createCampaign(CampaignRequest) instead
     */
    @Deprecated
    public Map<String, Object> createCampaign(
            String title,
            String fromName,
            String fromEmail,
            String replyTo,
            String subject,
            String htmlText,
            String listIds,
            String plainText,
            boolean sendCampaign,
            boolean trackOpens,
            boolean trackClicks) throws SkrybeException {
        
        // Convert to new API internally
        try {
            CampaignRequest.Builder builder = CampaignRequest.builder()
                .title(title)
                .fromName(fromName)
                .fromEmail(fromEmail)
                .replyTo(replyTo)
                .subject(subject)
                .htmlText(htmlText)
                .sendCampaign(sendCampaign)
                .trackOpens(trackOpens)
                .trackClicks(trackClicks);
            
            if (plainText != null) {
                builder.plainText(plainText);
            }
            
            if (listIds != null && !listIds.trim().isEmpty()) {
                builder.listIds(Arrays.asList(listIds.split(",")));
            }
            
            CampaignResponse response = createCampaign(builder.build());
            
            Map<String, Object> result = new HashMap<>();
            result.put("status_code", response.getStatusCode());
            result.put("response", response.getResponse());
            return result;
        } catch (Exception e) {
            throw new SkrybeException("Campaign creation failed: " + e.getMessage(), e);
        }
    }
}