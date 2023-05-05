package com.github.alturkovic.robots.txt

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

private class RobotsTxtTest {

    private val robotsTxt = RobotsTxtReader.read(Unit::class.java.getResourceAsStream("/robots.txt"))

    @Test
    fun shouldAllowUnmatched() {
        assertThat(robotsTxt.query("MyBot", "/unmatched/foo.txt").allowed).isTrue()
    }

    @Test
    fun shouldTestUnspecifiedUserAgent() {
        val userAgent = "MyBot"

        assertThat(robotsTxt.query(userAgent, "/root").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/root/public/foo.txt").allowed).isTrue()

        assertThat(robotsTxt.query(userAgent, "/root/private/foo.txt").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/root/foo.txt").allowed).isFalse()
    }

    @Test
    fun shouldAllowWithCrawlDelay() {
        val grant = robotsTxt.query("Delayed", "/root/delayed/foo.txt")
        assertThat(grant).isInstanceOf(MatchedGrant::class.java)
        assertThat(grant.allowed).isTrue()
        assertThat((grant as MatchedGrant).matchedRuleGroup.crawlDelay!!).isEqualTo(2.seconds)
    }

    @Test
    fun shouldTestDisallowAllUserAgent() {
        val userAgent = "DisallowAll" // Disallow: /

        assertThat(robotsTxt.query(userAgent, "/root/private/foo.txt").allowed).isFalse()
    }

    @Test
    fun shouldTestAllowedUserAgent() {
        val userAgent = "Allowed" // Disallow:

        assertThat(robotsTxt.query(userAgent, "/").allowed).isTrue()
    }

    @Test
    fun shouldTestSuperUserUserAgent() {
        val userAgent = "SuperUser" // Allow: /

        assertThat(robotsTxt.query(userAgent, "/root/private/foo.txt").allowed).isTrue()
    }

    @Test
    fun shouldTestWildcardUserAgent() {
        val userAgent = "Wild"

        assertThat(robotsTxt.query(userAgent, "/wildest/data.txt").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/root/my.gif/pictures").allowed).isTrue()

        assertThat(robotsTxt.query(userAgent, "/wild/data.txt").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/wilder/data.txt").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/my.gif").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/root/my.gif").allowed).isFalse()
    }

    @Test
    fun shouldTestBestMatchingUserAgent() {
        val userAgent = "FishBot" // Disallow: /fish.xml

        assertThat(robotsTxt.query(userAgent, "/fish.html").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/fish.xml").allowed).isFalse()
    }

    @Test
    fun shouldTestFishStartingWith() {
        val userAgent = "FishStartingWith" // Disallow: /fish

        assertThat(robotsTxt.query(userAgent, "/Fish.asp").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/catfish").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/?id=fish").allowed).isTrue()

        assertThat(robotsTxt.query(userAgent, "/fish").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fish.html").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fish.xml").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fish/salmon.html").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fishheads").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fishheads/yummy.html").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fish.php?id=anything").allowed).isFalse()
    }

    @Test
    fun shouldTestFishStartingWithWildcard() {
        val userAgent = "FishStartingWithWildcard" // Disallow: /fish*

        assertThat(robotsTxt.query(userAgent, "/Fish.asp").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/catfish").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/?id=fish").allowed).isTrue()

        assertThat(robotsTxt.query(userAgent, "/fish").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fish.html").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fish.xml").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fish/salmon.html").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fishheads").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fishheads/yummy.html").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fish.php?id=anything").allowed).isFalse()
    }

    @Test
    fun shouldTestFishFolder() {
        val userAgent = "FishFolder" // Disallow: /fish/

        assertThat(robotsTxt.query(userAgent, "/fish").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/fish.html").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/fish.xml").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/?id=fish").allowed).isTrue()

        assertThat(robotsTxt.query(userAgent, "/fish/").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fish/salmon.html").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fish/?id=anything").allowed).isFalse()
    }

    @Test
    fun shouldTestFishPhp() {
        val userAgent = "FishPhp" // Disallow: /fish*.php

        assertThat(robotsTxt.query(userAgent, "/fish.xml").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/Fish.PHP").allowed).isTrue()

        assertThat(robotsTxt.query(userAgent, "/fish.php").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/fishheads/catfish.php?parameters").allowed).isFalse()
    }

    @Test
    fun shouldTestPhp() {
        val userAgent = "Php" // Disallow: /*.php

        assertThat(robotsTxt.query(userAgent, "/fish.xml").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/windows.PHP").allowed).isTrue()

        assertThat(robotsTxt.query(userAgent, "/filename.php").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/folder/filename.php").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/folder/filename.php?parameter").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/folder/any.php.file.html").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/folder/any.php.file.html/filename.php").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/filename.php5").allowed).isFalse()
    }

    @Test
    fun shouldTestPhpEnding() {
        val userAgent = "PhpEnding" // Disallow: /*.php$

        assertThat(robotsTxt.query(userAgent, "/fish.xml").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/folder/filename.php/").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/folder/filename.php?parameter").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/folder/any.php.file.html").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/filename.php5").allowed).isTrue()
        assertThat(robotsTxt.query(userAgent, "/windows.PHP").allowed).isTrue()

        assertThat(robotsTxt.query(userAgent, "/filename.php").allowed).isFalse()
        assertThat(robotsTxt.query(userAgent, "/folder/filename.php").allowed).isFalse()
    }
}
