# skrybe-sdk-java

[![](https://jitpack.io/v/<GitHubUsername>/skrybe-sdk-java.svg)](https://jitpack.io/#<GitHubUsername>/skrybe-sdk-java)

## Getting the dependency via JitPack

### Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.<GitHubUsername>:skrybe-sdk-java:v1.0.0'
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
  <groupId>com.github.<GitHubUsername></groupId>
  <artifactId>skrybe-sdk-java</artifactId>
  <version>v1.0.0</version>
</dependency>
```

Replace `<GitHubUsername>` with your actual GitHub user/organisation name.

---

## Build locally

```bash
mvn clean package
```

## Usage example

```java
SkrybeSDK sdk = new SkrybeSDK("YOUR_API_KEY");
// ...
```