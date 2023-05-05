image:https://img.shields.io/badge/Java-8%2B-ED8B00?style=for-the-badge&labelColor=ED8B00&logo=java&color=808080[Java] 

image:https://img.shields.io/jitpack/v/github/alturkovic/robots-txt?style=for-the-badge&labelColor=007ec5&color=808080&logo=Git&logoColor=white[JitPack] 

image:https://img.shields.io/github/license/alturkovic/robots-txt?style=for-the-badge&color=808080&logo=Open%20Source%20Initiative&logoColor=white[License]

# Robots.txt

Java library for reading and querying [robots.txt](http://www.robotstxt.org/orig.html) files.

## Using the library in Java

1. Parse `robots.txt`:
```
RobotsTxt robotsTxt = RobotsTxtReader.read(inputStream);
```

2. Query `robotsTxt`:
```
Grant grant = robotsTxt.query("GoogleBot", "/path");
boolean canAccess = grant.getAllowed();
if (grant instanceof MatchedGrant) {
  Duration crawlDelay = ((MatchedGrant) grant).getMatchedRuleGroup().getCrawlDelay();
}
```

## Importing into your project

### Maven

Add the JitPack repository into your `pom.xml`.

```
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

Add the following under your `<dependencies>`:

```
<dependencies>
  <dependency>
    <groupId>com.github.alturkovic</groupId>
    <artifactId>robots-txt</artifactId>
    <version>[insert latest version here]</version>
  </dependency>
</dependencies>
```