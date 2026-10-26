# Changelog

All notable changes to the Skrybe Java SDK. Releases are built by JitPack from
the git tag (`v2.0.0` is served as `com.github.skry-be:skrybe-sdk-java:v2.0.0`).

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Methods that call new API endpoints need a Skrybe install that has them
deployed; the API side lives in skry-be/skrybe-dashboard.

## [Unreleased]

## [2.0.0] - 2026-10-26

A rewrite covering the whole public API, in step with `@skrybe/sdk` 2.0.0,
`skrybe/sdk-php` 2.0.0 and `@skrybe/cli` 0.2.0. See "Upgrading from 1.x" in
the README, which covers both the `v1.0.0` tag (a Map-based API over Spring's
`RestTemplate`) and the untagged builder API that followed it on `main`.

### Added
- A `Skrybe` client with resources: `emails()`, `campaigns()`, `lists()`,
  `subscribers()`, `templates()`, `autoresponders()`, `brands()`, `stats()`.
- Campaigns: `list`/`listAll`, `get`, `stats`, `send` (with `dryRun`),
  `testSend`, `schedule`/`unschedule`, `update`, `duplicate`, `delete`,
  `activity`/`activityAll`.
- Lists: `get`, `create`, `update`, `delete`, `activeSubscriberCount`, and
  custom fields (`addCustomField`, `renameCustomField`, `deleteCustomField`).
- Subscribers: `subscribe` (with custom fields), `unsubscribe`, `delete`,
  `status`, `list`/`listAll`.
- Templates: `list`, `get`, `create`, `update`, `delete`.
- Autoresponders: `list`, `get`, `emailStats`, `create`, `createEmail`.
- `brands().list()` and `stats().totalEmailsSent()`.
- Builders for every multi-field request (`SendEmailParams`,
  `CreateCampaignParams`, `SubscribeParams`, ...) and enums for the values the
  API restricts (`Tracking`, `CampaignStatus`, `OptIn`, `AutoresponderType`, ...).
- Typed results (`Campaign`, `CampaignDetail`, `SkrybeListDetail`,
  `SubscribersPage`, ...). Fields the SDK does not model yet are kept in
  `getExtra()` instead of failing the call.
- Unchecked exceptions under `SkrybeException`: `AuthenticationException`,
  `InvalidRequestException`, `RateLimitException`, `ConnectionException` and
  `ApiException`, each with a stable `getErrorCode()` and, where known, a
  `getHint()`. Prose failures the API returns with HTTP 200 are recognised and
  thrown.
- `Skrybe.builder(apiKey)` with `baseUrl`, `timeout`, `maxRetries` (reads
  only, honouring `Retry-After`) and `httpClient` (proxies, custom CAs).
- DEBUG logging of method, path, status and timing through the
  `com.skrybe.sdk` `System.Logger`. Bodies are never logged.

### Changed
- `SkrybeSDK` is now `Skrybe`, and the base URL is the install root
  (`https://dashboard.skry.be`), not its `/api` path.
- Methods return parsed results and throw on failure, instead of returning
  response wrappers with the raw body.
- `campaigns().create()` takes `send(true)` instead of `sendCampaign(true)`,
  and tracking as `Tracking.ON`/`OFF`/`ANONYMOUS` instead of booleans.
- The Maven groupId is `com.github.skry-be`, matching the coordinates JitPack
  serves.
- Requires Java 11 or newer (unchanged), and depends on Jackson instead of
  org.json.

### Removed
- `ValidationException`; missing parameters throw `InvalidRequestException`
  with the code `missing_params`, before anything is sent.
- `EmailResponse`, `CampaignResponse` and `TransactionalEmailResponse`.
- The deprecated eleven-argument `createCampaign(...)`.
- The jar-with-dependencies build artifact.

### Fixed
- `addSubscriber()` (v1.0.0) called `api/subscribers/add.php` and
  `getSubscribers()` (later on `main`) called `api/subscribers/get-active.php`;
  neither exists. `subscribers().subscribe()`, `list()` and `listAll()` replace
  them.
- `createCampaign()` always sent `send_campaign=0` and both tracking flags,
  overriding the brand's defaults; fields that are not set are no longer sent.
- `getLists()` and `getCampaigns()` could not read a brand without lists or
  campaigns, nor list names containing quotes, and returned campaigns in no
  particular order.
- Prose failures such as `Invalid API key`, returned with HTTP 200, were
  reported as successes or as parse errors.
- The pom no longer declares a dependency on its own published artifact.

## [1.0.0] - 2025-06-13

### Added
- Initial release: `SkrybeSDK` with Map-based `sendEmail`, `createCampaign`,
  `getLists`, `getCampaigns`, `getSubscribers` and `addSubscriber`, over
  Spring's `RestTemplate`.
