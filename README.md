# Skrybe SDK for Java

[![Maven Central](https://img.shields.io/badge/maven--central-v1.0.0-blue)](https://search.maven.org/artifact/com.skrybe/skrybe-sdk-java)
[![JitPack](https://jitpack.io/v/skry-be/skrybe-sdk-java.svg)](https://jitpack.io/#skry-be/skrybe-sdk-java)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A modern, type-safe Java SDK for interacting with the Skrybe email marketing platform API. This SDK provides easy access to Skrybe's features with proper error handling, input validation, and builder patterns for complex operations.

## ✨ Features

- **📧 Email Sending**: Send individual emails with rich HTML content and recipient variables
- **⚡ Transactional Emails**: Immediate delivery for OTP, password resets, alerts, and other time-sensitive messages
- **📊 Campaign Management**: Create and manage email campaigns with builder pattern
- **📋 List Management**: Retrieve and work with mailing lists
- **👥 Subscriber Management**: Get subscribers and active subscriber counts
- **🔒 Type Safety**: Strongly typed models instead of generic Maps
- **🏗️ Builder Pattern**: Easy-to-use builder for complex operations
- **✅ Input Validation**: Built-in validation with helpful error messages
- **⚠️ Proper Exception Handling**: Custom exceptions with detailed error information
- **🚀 Modern Java**: Uses latest Java HTTP client and follows current best practices

## Installation

### Maven
```xml
<dependency>
    <groupId>com.skrybe</groupId>
    <artifactId>skrybe-sdk-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```groovy
dependencies {
    implementation 'com.skrybe:skrybe-sdk-java:1.0.0'
}
```

### JitPack (Alternative)
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.yourusername</groupId>
    <artifactId>skrybe-sdk-java</artifactId>
    <version>v1.0.0</version>
</dependency>
```

## Usage Examples

### Initialize the SDK

```java
// Initialize with default settings
SkrybeSDK sdk = new SkrybeSDK("YOUR_API_KEY");

// Or with custom base URL
SkrybeSDK sdk = new SkrybeSDK("YOUR_API_KEY", "https://your-custom-url.com");
```

### 📧 Send Individual Emails (NEW!)

```java
import com.skrybe.sdk.model.EmailRequest;
import com.skrybe.sdk.model.EmailResponse;

try {
    // Create email with builder pattern
    EmailRequest email = EmailRequest.builder()
        .fromName("John Doe")
        .fromEmail("john@example.com")
        .replyTo("support@example.com")  // Optional - defaults to fromEmail
        .subject("Welcome to our service!")
        .htmlText("""
            <html>
                <body style="font-family: Arial, sans-serif;">
                    <h1>Hello [recipient.name]!</h1>
                    <p>Welcome to our service. Your ID is: [recipient.id]</p>
                    <p>We're excited to have you on board!</p>
                </body>
            </html>
            """)
        .plainText("Hello [recipient.name]! Welcome to our service. Your ID is: [recipient.id]")
        .addRecipient("customer@example.com")
        .addRecipientVariable("customer@example.com", "name", "Jane Smith")
        .addRecipientVariable("customer@example.com", "id", "12345")
        .trackOpens(true)
        .trackClicks(true)
        .build();

    EmailResponse response = sdk.sendEmail(email);
    
    if (response.isSuccess()) {
        System.out.println("Email sent successfully!");
        if (response.isScheduled()) {
            System.out.println("Email was scheduled for later delivery");
        }
    } else {
        System.err.println("Email failed to send: " + response.getResponse());
    }
    
} catch (ValidationException e) {
    System.err.println("Validation error: " + e.getMessage());
} catch (SkrybeException e) {
    System.err.println("API error: " + e.getMessage());
}
```

### ⚡ Send Transactional Emails (NEW!)

For time-sensitive, one-off emails like OTP codes, password resets, or alerts that need immediate delivery:

```java
import com.skrybe.sdk.model.TransactionalEmailRequest;
import com.skrybe.sdk.model.TransactionalEmailResponse;

try {
    // Create transactional email - delivered immediately, not queued
    TransactionalEmailRequest request = TransactionalEmailRequest.builder()
        .toEmail("user@example.com")
        .subject("Your one-time passcode")
        .htmlBody("<p>Your code is <strong>384092</strong>. It expires in 5 minutes.</p>")
        .plainBody("Your code is 384092. It expires in 5 minutes.")
        // Optional overrides (uses brand defaults if omitted)
        .fromName("Skrybe Security")
        .fromEmail("no-reply@yourdomain.com")
        .replyTo("support@yourdomain.com")
        .build();

    TransactionalEmailResponse response = sdk.sendTransactionalEmail(request);

    if (response.isSuccess()) {
        System.out.println("Transactional email sent: " + response.getMessage());
    } else {
        System.err.println("Failed: " + response.getMessage());
        if (response.getErrorInfo() != null) {
            System.err.println("Details: " + response.getErrorInfo());
        }
    }

} catch (ValidationException e) {
    System.err.println("Validation error: " + e.getMessage());
} catch (SkrybeException e) {
    System.err.println("API error: " + e.getMessage());
}
```

**Key differences from regular email sending:**
- Single recipient only (`toEmail` vs `to` array)
- Immediate delivery (no scheduling, no queuing)
- Uses JSON body (not form-encoded)
- Brand defaults for `fromName`, `fromEmail`, `replyTo` if not specified

### 📋 Manage Lists

```java
import com.skrybe.sdk.SkrybeSDK;
import com.skrybe.sdk.model.SkrybeList;
import com.skrybe.sdk.exception.SkrybeException;

try {
    // Get all lists (returns proper SkrybeList objects!)
    List<SkrybeList> lists = sdk.getLists();
    
    // Work with type-safe objects
    for (SkrybeList list : lists) {
        System.out.println("List ID: " + list.getId());
        System.out.println("List Name: " + list.getName());
        
        // Get active subscriber count for this list (NEW!)
        int activeCount = sdk.getActiveSubscriberCount(list.getId());
        System.out.println("Active Subscribers: " + activeCount);
        System.out.println("---");
    }
    
} catch (SkrybeException e) {
    System.err.println("API error: " + e.getMessage());
}
```

### 👥 Work with Subscribers

```java
import com.skrybe.sdk.model.Subscriber;
import com.skrybe.sdk.exception.ValidationException;

try {
    // Get subscribers from a specific list (with validation!)
    List<Subscriber> subscribers = sdk.getSubscribers("your-list-id");
    
    // Work with type-safe Subscriber objects
    for (Subscriber subscriber : subscribers) {
        System.out.println("Email: " + subscriber.getEmail());
        System.out.println("Name: " + subscriber.getName());
        System.out.println("List ID: " + subscriber.getListId());
    }
    
    // Get just the count without fetching all subscribers (NEW!)
    int activeCount = sdk.getActiveSubscriberCount("your-list-id");
    System.out.println("Total active subscribers: " + activeCount);
    
} catch (ValidationException e) {
    System.err.println("Validation error: " + e.getMessage());
} catch (SkrybeException e) {
    System.err.println("API error: " + e.getMessage());
}
```

### 📊 Create a Campaign (Builder Pattern!)

```java
import com.skrybe.sdk.model.CampaignRequest;
import com.skrybe.sdk.model.CampaignResponse;

try {
    // Use the fluent builder pattern - much cleaner than 11 parameters!
    CampaignRequest request = CampaignRequest.builder()
        .title("Monthly Newsletter")
        .fromName("John Doe")
        .fromEmail("john@example.com")
        .replyTo("support@example.com")  // Optional - defaults to fromEmail
        .subject("Your Monthly Update")
        .htmlText("<h1>Welcome to our newsletter!</h1><p>Great content here...</p>")
        .plainText("Welcome to our newsletter! Great content here...")  // Optional
        .addListId("list-id-1")
        .addListId("list-id-2")
        // Or use: .listIds(Arrays.asList("list-id-1", "list-id-2"))
        .sendCampaign(false)  // false = save as draft, true = send immediately
        .trackOpens(true)     // Default: true
        .trackClicks(true)    // Default: true
        .build();  // Validates all required fields!

    CampaignResponse response = sdk.createCampaign(request);
    
    if (response.isSuccess()) {
        System.out.println("Campaign created successfully!");
        System.out.println("Response: " + response.getResponse());
    } else {
        System.err.println("Campaign creation failed with status: " + response.getStatusCode());
    }
    
} catch (IllegalArgumentException e) {
    // Builder validation errors
    System.err.println("Invalid campaign data: " + e.getMessage());
} catch (ValidationException e) {
    // API validation errors
    System.err.println("Validation error: " + e.getMessage());
} catch (SkrybeException e) {
    // Network or API errors
    System.err.println("API error: " + e.getMessage());
}
```

### 📈 Get Campaigns

```java
import com.skrybe.sdk.model.Campaign;

try {
    // Get all campaigns (returns proper Campaign objects!)
    List<Campaign> campaigns = sdk.getCampaigns();
    
    for (Campaign campaign : campaigns) {
        System.out.println("Campaign ID: " + campaign.getId());
        System.out.println("Title: " + campaign.getTitle());
        System.out.println("From: " + campaign.getFromName() + " <" + campaign.getFromEmail() + ">");
        System.out.println("Subject: " + campaign.getSubject());
        System.out.println("Status: " + campaign.getStatus());
        System.out.println("---");
    }
    
} catch (SkrybeException e) {
    System.err.println("API error: " + e.getMessage());
}
```

## ⚠️ Exception Handling

The SDK uses a proper exception hierarchy:

```java
try {
    // SDK operations
    EmailResponse response = sdk.sendEmail(emailRequest);
    List<SkrybeList> lists = sdk.getLists();
    
} catch (ValidationException e) {
    // Input validation errors (invalid parameters, missing required fields, etc.)
    System.err.println("Validation error: " + e.getMessage());
    
} catch (SkrybeException e) {
    // API errors (network issues, authentication, server errors, etc.)
    System.err.println("API error: " + e.getMessage());
    
} catch (IllegalArgumentException e) {
    // Builder validation errors (caught at build time)
    System.err.println("Invalid input: " + e.getMessage());
}
```

## 📚 Model Classes

The SDK provides type-safe model classes:

| Class | Description | Key Properties |
|-------|-------------|----------------|
| `SkrybeList` | Represents a mailing list | `id`, `name` |
| `Subscriber` | Represents a subscriber | `id`, `email`, `name`, `listId` |
| `Campaign` | Represents a campaign | `id`, `title`, `fromName`, `fromEmail`, `subject`, `status` |
| `CampaignRequest` | Builder for creating campaigns | All campaign fields with validation |
| `CampaignResponse` | Response from campaign creation | `statusCode`, `response`, `success` |
| `EmailRequest` | Builder for sending emails | All email fields with validation |
| `EmailResponse` | Response from email sending | `statusCode`, `response`, `success`, `scheduled` |
| `TransactionalEmailRequest` | Builder for transactional emails | `toEmail`, `subject`, `htmlBody`, `plainBody`, optional overrides |
| `TransactionalEmailResponse` | Response from transactional sends | `statusCode`, `success`, `message`, `errorInfo`, `errorCode` |

## 🏗️ Builder Pattern Benefits

Instead of this old approach with 11 parameters:
```java
// ❌ Old way - error-prone and hard to remember parameter order
sdk.createCampaign(title, fromName, fromEmail, replyTo, subject, 
                   htmlText, listIds, plainText, sendCampaign, 
                   trackOpens, trackClicks);
```

Use the modern builder pattern:
```java
// ✅ New way - self-documenting and validates input
CampaignRequest request = CampaignRequest.builder()
    .title("Newsletter")
    .fromName("John")
    .fromEmail("john@example.com")
    .subject("Monthly Update")
    .htmlText("<h1>Content</h1>")
    .addListId("list-123")
    .build();  // Validates required fields automatically!

sdk.createCampaign(request);
```

## 🔄 Migration from v1.x

If you're upgrading from the older version, here's what changed:

### 🆕 New Features
- **Email Sending**: `sendEmail(EmailRequest)` - Send individual emails with rich content
- **Transactional Emails**: `sendTransactionalEmail(TransactionalEmailRequest)` - Immediate delivery for OTP, alerts, etc.
- **Active Subscriber Count**: `getActiveSubscriberCount(listId)` - Get subscriber counts efficiently

### 📝 Method Signatures
- `getLists()` now returns `List<SkrybeList>` instead of `List<Map<String, Object>>`
- `getSubscribers(listId)` now returns `List<Subscriber>` instead of `List<Map<String, Object>>`
- `getCampaigns()` now returns `List<Campaign>` instead of `List<Map<String, Object>>`
- `createCampaign(CampaignRequest)` replaces the 11-parameter version

### ⚠️ Exception Handling
- Methods now throw `SkrybeException` and `ValidationException` instead of `IOException`
- Input validation happens at build/call time with helpful error messages

### 🔄 Backward Compatibility
The old `createCampaign()` method with 11 parameters is still available but **deprecated**. It internally converts to the new API, so you get the benefits of validation while maintaining compatibility.

## 🛠️ Building Locally

```bash
# Build the project
mvn clean package

# Run tests
mvn test

# Generate JavaDoc
mvn javadoc:javadoc
```

## 📄 License

MIT License

Copyright (c) 2024 Skrybe

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.