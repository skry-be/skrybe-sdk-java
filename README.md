# Skrybe SDK for Java

[![](https://jitpack.io/v/skry-be/skrybe-sdk-java.svg)](https://jitpack.io/#skry-be/skrybe-sdk-java)

A Java SDK for interacting with the Skrybe email marketing platform API. This SDK provides easy access to Skrybe's features including email sending, campaign management, subscriber management, and more.

## Installation

### Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.skry-be:skrybe-sdk-java:v1.0.0'
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.skry-be</groupId>
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

### Send an Email

```java
Map<String, Object> emailOptions = new HashMap<>();
emailOptions.put("fromName", "John Doe");
emailOptions.put("fromEmail", "john@example.com");
emailOptions.put("subject", "Welcome!");
emailOptions.put("htmlText", "<h1>Welcome to our platform!</h1>");
emailOptions.put("to", Arrays.asList("recipient@example.com"));

sdk.sendEmail(emailOptions);
```

### Create a Campaign

```java
Map<String, Object> campaignOptions = new HashMap<>();
campaignOptions.put("fromName", "John Doe");
campaignOptions.put("fromEmail", "john@example.com");
campaignOptions.put("title", "Monthly Newsletter");
campaignOptions.put("subject", "Your Monthly Update");
campaignOptions.put("htmlText", "<h1>Monthly Newsletter</h1>");
campaignOptions.put("listIds", Arrays.asList("list-id-1", "list-id-2"));

sdk.createCampaign(campaignOptions);
```

### Manage Lists

```java
// Get all lists
List<Map<String, Object>> lists = sdk.getLists();

// Get lists including hidden ones
List<Map<String, Object>> allLists = sdk.getLists(true);
```

### Work with Subscribers

```java
// Get subscribers from a list
Map<String, Object> options = new HashMap<>();
options.put("page", 1);
options.put("limit", 100);
List<Map<String, Object>> subscribers = sdk.getSubscribers("list-id", options);

// Add a subscriber to a list
Map<String, Object> subscriberData = new HashMap<>();
subscriberData.put("email", "new@example.com");
subscriberData.put("name", "New User");
sdk.addSubscriber("list-id", subscriberData);
```

## Error Handling

The SDK throws `SkrybeException` for API-related errors and `ValidationException` for validation errors:

```java
try {
    sdk.sendEmail(emailOptions);
} catch (ValidationException e) {
    // Handle validation errors
    System.err.println("Validation error: " + e.getMessage());
} catch (SkrybeException e) {
    // Handle API errors
    System.err.println("API error: " + e.getMessage());
}
```

## Rate Limiting

The SDK automatically handles rate limiting with a minimum interval of 100ms between requests.

## Building Locally

To build the SDK locally:

```bash
mvn clean package
```

## License

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