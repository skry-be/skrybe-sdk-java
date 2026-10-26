# Skrybe Java SDK

[![JitPack](https://jitpack.io/v/skry-be/skrybe-sdk-java.svg)](https://jitpack.io/#skry-be/skrybe-sdk-java)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

The official Java SDK for the [Skrybe](https://skry.be) email marketing API.
It covers every brand-key endpoint: transactional and bulk email, the full
campaign lifecycle, lists and custom fields, subscribers, templates and
autoresponders.

Requests are built with builders, results come back as typed objects, and
failures are unchecked exceptions with stable codes. The API answers most
failures with HTTP 200 and a sentence of prose; the SDK recognises those and
throws, so you can branch on them.

## Requirements

- Java 11 or newer
- One runtime dependency, Jackson (`jackson-databind`), resolved for you

## Install

The SDK is distributed through [JitPack](https://jitpack.io/#skry-be/skrybe-sdk-java),
which builds it from the git tag.

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
    <version>v2.0.0</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.skry-be:skrybe-sdk-java:v2.0.0'
}
```

## Quick start

```java
import com.skrybe.sdk.Skrybe;
import com.skrybe.sdk.params.SendTransactionalParams;

Skrybe skrybe = new Skrybe(System.getenv("SKRYBE_API_KEY"));

skrybe.emails().sendTransactional(SendTransactionalParams.builder()
        .toEmail("ada@example.com")
        .subject("Your code")
        .plainBody("Your code is 123456")
        .build());
```

Use a brand's API key, from Settings in the Skrybe dashboard, and keep it
server-side. A `Skrybe` client is thread-safe: build one and share it.

### Options

```java
Skrybe skrybe = Skrybe.builder(apiKey)
        .baseUrl("https://dashboard.skry.be") // the default
        .timeout(Duration.ofSeconds(30))      // per attempt, the default
        .maxRetries(2)                        // reads only; see below
        .httpClient(HttpClient.newBuilder()   // e.g. a proxy, or an SSLContext with a corporate CA
                .proxy(ProxySelector.of(new InetSocketAddress("proxy", 3128)))
                .build())
        .build();
```

Reads are retried after a dropped connection or a 429/502/503/504, honouring
`Retry-After`. Writes are never retried: the API has no idempotency keys, so a
retried send could go out twice. A timeout is reported at once, not retried.

Each request is logged at DEBUG to the `com.skrybe.sdk` `System.Logger`
(method, path, status and timing). Request bodies are never logged: they carry
the API key.

## Usage

Parameters with more than a couple of fields take a builder, e.g.
`CreateCampaignParams.builder()...build()`; only the fields you set are sent.
Values the API restricts are enums in `com.skrybe.sdk.params` (`Tracking`,
`CampaignStatus`, `OptIn`, ...). Results are classes in `com.skrybe.sdk.model`
with getters named after the API's fields (`from_name` is `getFromName()`);
fields this SDK does not know yet are in `getExtra()`.

### Email

```java
// One email, delivered immediately. For OTPs, password resets and receipts.
skrybe.emails().sendTransactional(SendTransactionalParams.builder()
        .toEmail("ada@example.com")
        .subject("Reset your password")
        .htmlBody("<p>...</p>")
        // fromName, fromEmail and replyTo fall back to the brand's.
        .build());

// A bulk send to up to 250 addresses, or to lists.
SendEmailResult sent = skrybe.emails().send(SendEmailParams.builder()
        .fromName("Ada")
        .fromEmail("ada@example.com")
        .replyTo("ada@example.com")
        .subject("Hello [recipient.first]")
        .htmlText("<p>Hi [recipient.first]</p>")
        .to("grace@example.com")
        .recipientVariable("grace@example.com", "first", "Grace")
        .build());
sent.getCampaignId();
```

### Campaigns

```java
CreateCampaignResult created = skrybe.campaigns().create(CreateCampaignParams.builder()
        .fromName("Ada").fromEmail("ada@example.com").replyTo("ada@example.com")
        .title("October news").subject("What we shipped").htmlText(html)
        .build());
int id = Integer.parseInt(created.getCampaignId());

skrybe.campaigns().testSend(id, "me@example.com");                                     // up to 5 addresses
skrybe.campaigns().send(id, Recipients.builder().listIds(listId).dryRun(true).build()); // count, send nothing
skrybe.campaigns().send(id, Recipients.builder().listIds(listId).build());
// or
skrybe.campaigns().schedule(id, ScheduleCampaignParams.builder()
        .listIds(listId).at("2027-06-15 18:05").timezone("Africa/Lagos").build());
skrybe.campaigns().unschedule(id);

skrybe.campaigns().list(ListCampaignsParams.builder().status(CampaignStatus.SENT).limit(20).build());
skrybe.campaigns().listAll();
skrybe.campaigns().get(id, true);                       // with its HTML
skrybe.campaigns().update(id, UpdateCampaignParams.builder().subject("New subject").build());
skrybe.campaigns().duplicate(id, "Copy");
skrybe.campaigns().delete(id);

CampaignStats stats = skrybe.campaigns().stats(id);
stats.getOpens().getRate();
skrybe.campaigns().activity(id, CampaignActivityParams.builder().type(ActivityType.CLICKS).limit(100).build());
skrybe.campaigns().activityAll(id, ActivityType.OPENS);
```

### Lists, subscribers and custom fields

A list id is the encrypted value `lists().list()` returns, not the number in
the dashboard URL.

```java
List<SkrybeList> lists = skrybe.lists().list();
SkrybeListDetail list = skrybe.lists().create("Customers", OptIn.DOUBLE);
skrybe.lists().update(list.getId(), UpdateListParams.builder().name("All customers").build());
skrybe.lists().addCustomField(list.getId(), "Birthday", CustomFieldType.DATE);
skrybe.lists().renameCustomField(list.getId(), "Birthday", "DOB");
skrybe.lists().deleteCustomField(list.getId(), "DOB");
skrybe.lists().activeSubscriberCount(list.getId());
skrybe.lists().delete(list.getId());

SubscribeOutcome outcome = skrybe.subscribers().subscribe(SubscribeParams.builder()
        .listId(listId)
        .email("ada@example.com")
        .name("Ada")
        .field("Birthday", "1990-12-10")
        .build()); // SUBSCRIBED or ALREADY_SUBSCRIBED
skrybe.subscribers().status(listId, "ada@example.com");
skrybe.subscribers().list(listId, ListSubscribersParams.builder().status(SubscriberStatus.ACTIVE).limit(100).build());
skrybe.subscribers().listAll(listId);
skrybe.subscribers().unsubscribe(listId, "ada@example.com");
skrybe.subscribers().delete(listId, "ada@example.com");
```

### Templates

```java
Template t = skrybe.templates().create(TemplateParams.builder().name("Welcome").htmlText(html).build());
skrybe.templates().list();
skrybe.templates().get(t.getId(), true);
skrybe.templates().update(t.getId(), TemplateParams.builder().name("Welcome v2").build());
skrybe.templates().delete(t.getId());
```

### Autoresponders

```java
Autoresponder ar = skrybe.autoresponders().create(CreateAutoresponderParams.builder()
        .name("Onboarding").type(AutoresponderType.DRIP).listId(listId).build());
skrybe.autoresponders().createEmail(ar.getId(), CreateAutoresponderEmailParams.builder()
        .subject("Day 3")
        .htmlText(html)
        .when("+3 days")
        .enabled(true) // emails are created off unless this is set
        .build());
skrybe.autoresponders().list(listId);
skrybe.autoresponders().get(ar.getId()).getEmailList();
skrybe.autoresponders().emailStats(emailId);
```

### Brands and stats

```java
skrybe.brands().list();               // the key's own brand
skrybe.stats().totalEmailsSent();     // install-wide, public
```

## Errors

Every failure is an unchecked `com.skrybe.sdk.exception.SkrybeException`:

| Exception | `getType()` | When |
|---|---|---|
| `AuthenticationException` | `authentication` | The API key is missing or wrong. |
| `InvalidRequestException` | `invalid_request` | The request is wrong; retrying it unchanged fails again. Includes `missing_params`, caught before anything is sent. |
| `RateLimitException` | `rate_limit` | A quota or rate limit was hit. |
| `ConnectionException` | `connection` | The API could not be reached (`network_error`) or did not answer in time (`timeout`). |
| `ApiException` | `api` | Anything else the API refused, e.g. `campaign_not_found`, or a response it could not parse (`malformed_response`). |

```java
try {
    skrybe.campaigns().send(id, Recipients.builder().listIds(listId).build());
} catch (RateLimitException e) {
    // e.g. quota_exceeded
} catch (ApiException e) {
    if (e.getErrorCode().equals("campaign_not_draft")) {
        // already sent
    }
    log.warn("{} {}", e.getMessage(), e.getHint());
}
```

Each exception has `getErrorCode()` (a stable slug), `getHint()` (what to do
about it, when known), `getStatus()` (HTTP status, when there was a response)
and `getRaw()` (the response body).

## Security

- **TLS is always verified.** There is no option to turn it off. Pass an
  `HttpClient` with your own `SSLContext` to trust a corporate CA instead.
- **Keep your JDK current.** Java trusts the CAs in its own `cacerts`, so CA
  distrust events only reach you through JDK updates.
- **Redirects are not followed**, so a misconfigured base URL cannot turn a
  POST carrying your API key into a request somewhere else.

## Upgrading from 1.x

2.0 is a rewrite. Methods moved onto resources, take builders, and return
parsed values instead of raw response bodies. The `v1.0.0` tag took `Map`
options; the untagged code that followed it on `main` took builders. Both map
onto 2.x like this:

| 1.x | 2.x |
|---|---|
| `new SkrybeSDK(key)`, `new SkrybeSDK(key, baseUrl)` | `new Skrybe(key)`, `Skrybe.builder(key).baseUrl(...).build()`. The base URL is the install root, without `/api`. |
| `sendEmail(map)` / `sendEmail(EmailRequest)` | `emails().send(SendEmailParams)`, returns `SendEmailResult` (`getMessage()`, `getCampaignId()`) |
| `sendTransactionalEmail(TransactionalEmailRequest)` | `emails().sendTransactional(SendTransactionalParams)`, returns `SendTransactionalResult` |
| `createCampaign(map)` / `createCampaign(CampaignRequest)` | `campaigns().create(CreateCampaignParams)`. `sendCampaign(true)` is `send(true)`; tracking takes `Tracking.ON`/`OFF`/`ANONYMOUS`. Returns `CreateCampaignResult` (`getOutcome()`, `getCampaignId()`). |
| `getLists()` / `getLists(includeHidden)` | `lists().list()` / `lists().list(includeHidden)`, returns `List<SkrybeList>` |
| `getCampaigns()` / `getCampaigns(map)` | `campaigns().list(ListCampaignsParams)`, or `listAll()` for every page |
| `getSubscribers(listId)` / `getSubscribers(listId, map)` | `subscribers().list(listId, ListSubscribersParams)`, or `listAll(listId)` |
| `getActiveSubscriberCount(listId)` | `lists().activeSubscriberCount(listId)`, returns `long` |
| `addSubscriber(listId, map)` | `subscribers().subscribe(SubscribeParams)` (1.x called an endpoint that did not exist) |

Failures now throw instead of returning an error body, and `ValidationException`
is gone: a missing parameter throws `InvalidRequestException` with the code
`missing_params`. The coordinates are `com.github.skry-be:skrybe-sdk-java`, as
JitPack serves them; Spring and org.json are no longer pulled in.

## Development

```bash
mvn verify
```

The tests run against a local `com.sun.net.httpserver` stub, so they assert the
exact wire format and need no network. `Skrybe.VERSION` must match `pom.xml`
and the newest released heading in `CHANGELOG.md`; a test checks both.

## License

MIT
