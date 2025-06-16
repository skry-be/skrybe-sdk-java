package com.skrybe.sdk;

import com.skrybe.sdk.model.*;
import com.skrybe.sdk.exception.SkrybeException;
import com.skrybe.sdk.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class SkrybeSDKTest {
    private SkrybeSDK sdk;
    private static final String API_KEY = "crZYDp0w6432WU7GYVAq";

    @BeforeEach
    void setUp() {
        sdk = new SkrybeSDK(API_KEY);
    }

    @Test
    void constructor_ShouldValidateApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new SkrybeSDK(null));
        assertThrows(IllegalArgumentException.class, () -> new SkrybeSDK(""));
        assertThrows(IllegalArgumentException.class, () -> new SkrybeSDK("  "));
    }

    @Test
    void constructor_ShouldValidateBaseUrl() {
        assertThrows(IllegalArgumentException.class, () -> new SkrybeSDK("test", null));
        assertThrows(IllegalArgumentException.class, () -> new SkrybeSDK("test", ""));
        assertThrows(IllegalArgumentException.class, () -> new SkrybeSDK("test", "  "));
    }

    @Test
    void getLists_ShouldReturnTypeSafeLists() {
        assertDoesNotThrow(() -> {
            List<SkrybeList> lists = sdk.getLists();
            assertNotNull(lists, "Lists should not be null");
            
            if (!lists.isEmpty()) {
                SkrybeList firstList = lists.get(0);
                assertNotNull(firstList.getId(), "List ID should not be null");
                assertNotNull(firstList.getName(), "List name should not be null");
                System.out.println("First list: " + firstList);
            }
        });
    }

    @Test
    void getSubscribers_ShouldValidateListId() {
        assertThrows(ValidationException.class, () -> sdk.getSubscribers(null));
        assertThrows(ValidationException.class, () -> sdk.getSubscribers(""));
        assertThrows(ValidationException.class, () -> sdk.getSubscribers("  "));
    }

    @Test
    void getSubscribers_ShouldReturnTypeSafeSubscribers() {
        assertDoesNotThrow(() -> {
            // First get a list ID
            List<SkrybeList> lists = sdk.getLists();
            assertFalse(lists.isEmpty(), "Should have at least one list");
            
            String listId = lists.get(0).getId();
            List<Subscriber> subscribers = sdk.getSubscribers(listId);
            assertNotNull(subscribers, "Subscribers should not be null");
            
            if (!subscribers.isEmpty()) {
                Subscriber firstSubscriber = subscribers.get(0);
                assertNotNull(firstSubscriber.getEmail(), "Subscriber email should not be null");
                assertEquals(listId, firstSubscriber.getListId(), "List ID should match");
                System.out.println("First subscriber: " + firstSubscriber);
            }
        });
    }

    @Test
    void getCampaigns_ShouldReturnTypeSafeCampaigns() {
        assertDoesNotThrow(() -> {
            List<Campaign> campaigns = sdk.getCampaigns();
            assertNotNull(campaigns, "Campaigns should not be null");
            
            if (!campaigns.isEmpty()) {
                Campaign firstCampaign = campaigns.get(0);
                assertNotNull(firstCampaign.getId(), "Campaign ID should not be null");
                assertNotNull(firstCampaign.getTitle(), "Campaign title should not be null");
                System.out.println("First campaign: " + firstCampaign);
            }
        });
    }

    @Test
    void createCampaign_ShouldValidateRequest() {
        assertThrows(ValidationException.class, () -> sdk.createCampaign((CampaignRequest) null));
    }

    @Test
    void createCampaign_ShouldCreateCampaignWithBuilder() {
        assertDoesNotThrow(() -> {
            // First get a list ID
            List<SkrybeList> lists = sdk.getLists();
            assertFalse(lists.isEmpty(), "Should have at least one list");
            
            String listId = lists.get(0).getId();
            
            CampaignRequest request = CampaignRequest.builder()
                .title("Test Campaign from Updated SDK")
                .fromName("Test User")
                .fromEmail("test@example.com")
                .subject("Test Subject")
                .htmlText("<h1>Hello from Updated SDK!</h1>")
                .addListId(listId)
                .sendCampaign(false) // Save as draft
                .trackOpens(true)
                .trackClicks(true)
                .build();
            
            CampaignResponse response = sdk.createCampaign(request);
            
            assertNotNull(response, "Response should not be null");
            assertTrue(response.getStatusCode() >= 200, "Status code should be successful");
            assertNotNull(response.getResponse(), "Response body should not be null");
            System.out.println("Campaign response: " + response);
        });
    }

    @Test
    void campaignBuilder_ShouldValidateRequiredFields() {
        // Test missing title
        assertThrows(IllegalArgumentException.class, () -> {
            CampaignRequest.builder()
                .fromName("Test")
                .fromEmail("test@example.com")
                .subject("Test")
                .htmlText("<h1>Test</h1>")
                .addListId("test-list")
                .build();
        });

        // Test missing from name
        assertThrows(IllegalArgumentException.class, () -> {
            CampaignRequest.builder()
                .title("Test")
                .fromEmail("test@example.com")
                .subject("Test")
                .htmlText("<h1>Test</h1>")
                .addListId("test-list")
                .build();
        });

        // Test missing list IDs
        assertThrows(IllegalArgumentException.class, () -> {
            CampaignRequest.builder()
                .title("Test")
                .fromName("Test")
                .fromEmail("test@example.com")
                .subject("Test")
                .htmlText("<h1>Test</h1>")
                .build();
        });
    }

    @Test
    void campaignBuilder_ShouldSetDefaults() {
        CampaignRequest request = CampaignRequest.builder()
            .title("Test")
            .fromName("Test")
            .fromEmail("test@example.com")
            .subject("Test")
            .htmlText("<h1>Test</h1>")
            .addListId("test-list")
            .build();

        assertFalse(request.isSendCampaign(), "Default should be false for sendCampaign");
        assertTrue(request.isTrackOpens(), "Default should be true for trackOpens");
        assertTrue(request.isTrackClicks(), "Default should be true for trackClicks");
        assertEquals("test@example.com", request.getReplyTo(), "Default replyTo should be fromEmail");
    }
} 