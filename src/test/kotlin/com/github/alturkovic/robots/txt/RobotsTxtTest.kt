package com.github.alturkovic.robots.txt

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

private class RobotsTxtTest {

    @Test
    fun shouldAllowUnmatched() {
        val robotsTxt = RobotsTxtReader.read(
            """
                # No rules defined
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/unmatched/foo.txt").allowed).isTrue()
    }

    @Test
    fun shouldAllowAnyUserAgent() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /
                Allow: /public/
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/private/foo.txt").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/public/foo.txt").allowed).isTrue()
    }

    @Test
    fun shouldAllowWithCrawlDelay() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Allow: /delayed/
                Crawl-Delay: 2
            """.trimIndent().byteInputStream()
        )

        val grant = robotsTxt.query("FooBot", "/delayed/foo.txt")
        assertThat(grant).isInstanceOf(MatchedGrant::class.java)
        assertThat(grant.allowed).isTrue()
        assertThat((grant as MatchedGrant).matchedRuleGroup.crawlDelay!!).isEqualTo(2.seconds)
    }

    @Test
    fun shouldDisallowAllUserAgents() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/public/foo.txt").allowed).isFalse()
    }

    @Test
    fun shouldAllowAllUserAgents() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: 
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/").allowed).isTrue()
    }

    @Test
    fun shouldAllowSpecificUserAgent() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /
                
                User-Agent: FooBot
                Allow: /
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/private/foo.txt").allowed).isTrue()
        assertThat(robotsTxt.query("BarBot", "/private/foo.txt").allowed).isFalse()
    }

    @Test
    fun shouldAllowWildcards() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /wild*/
                Allow: /wildest/
                Disallow: /*.gif$
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/wildest/data.txt").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/my.gif/pictures").allowed).isTrue()

        assertThat(robotsTxt.query("FooBot", "/wild/data.txt").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/wilder/data.txt").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/my.gif").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/root/my.gif").allowed).isFalse()
    }

    @Test
    fun shouldUseBestMatchingUserAgent() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: Foo
                Disallow: /foo.txt
                
                User-agent: Bar
                Disallow: /foo.html
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/foo.html").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/foo.txt").allowed).isFalse()
    }

    @Test
    fun shouldDisallowStartingWith() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /fish
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/Fish.asp").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/catfish").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/?id=fish").allowed).isTrue()

        assertThat(robotsTxt.query("FooBot", "/fish").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fish.html").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fish.xml").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fish/salmon.html").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fishheads").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fishheads/yummy.html").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fish.php?id=anything").allowed).isFalse()
    }

    @Test
    fun shouldDisallowStartingWithWildcard() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /fish*
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/Fish.asp").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/catfish").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/?id=fish").allowed).isTrue()

        assertThat(robotsTxt.query("FooBot", "/fish").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fish.html").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fish.xml").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fish/salmon.html").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fishheads").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fishheads/yummy.html").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fish.php?id=anything").allowed).isFalse()
    }

    @Test
    fun shouldDisallowFolder() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /fish/
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/fish").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/fish.html").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/fish.xml").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/?id=fish").allowed).isTrue()

        assertThat(robotsTxt.query("FooBot", "/fish/").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fish/salmon.html").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fish/?id=anything").allowed).isFalse()
    }

    @Test
    fun shouldDisallowStartingWithAndWildcardWithExtension() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /fish*.php
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/fish.xml").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/Fish.PHP").allowed).isTrue()

        assertThat(robotsTxt.query("FooBot", "/fish.php").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/fishheads/catfish.php?parameters").allowed).isFalse()
    }

    @Test
    fun shouldDisallowWildcardWithExtension() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /*.php
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/fish.xml").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/windows.PHP").allowed).isTrue()

        assertThat(robotsTxt.query("FooBot", "/filename.php").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/folder/filename.php").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/folder/filename.php?parameter").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/folder/any.php.file.html").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/folder/any.php.file.html/filename.php").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/filename.php5").allowed).isFalse()
    }

    @Test
    fun shouldDisallowEndingWithExtension() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /*.php$
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/fish.xml").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/folder/filename.php/").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/folder/filename.php?parameter").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/folder/any.php.file.html").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/filename.php5").allowed).isTrue()
        assertThat(robotsTxt.query("FooBot", "/windows.PHP").allowed).isTrue()

        assertThat(robotsTxt.query("FooBot", "/filename.php").allowed).isFalse()
        assertThat(robotsTxt.query("FooBot", "/folder/filename.php").allowed).isFalse()
    }

    @Test
    fun shouldDisallowWithUrlInParameters() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /foo?bar=qux&baz=http://foo.bar?tar&par
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/foo?bar=qux&baz=http://foo.bar?tar&par").allowed).isFalse()
    }

    @Test
    fun shouldDisallowWithNonAsciiCharacter() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /foo/ツ
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/foo/ツ").allowed).isFalse()
    }

    @Test
    fun shouldDisallowWithEscapedCharacters() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /foo/%E3%83%84
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/foo/ツ").allowed).isFalse()
    }

    @Test
    fun shouldDisallowWithNonEscapedCharacters() {
        val robotsTxt = RobotsTxtReader.read(
            """
                User-agent: *
                Disallow: /foo/%E
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt.query("FooBot", "/foo/%E").allowed).isFalse()
    }
}
