package com.github.alturkovic.robots.txt

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

private class RobotsTxtReaderTest {

    @Test
    fun shouldReadSimpleRobotsTxt() {
        val robotsTxt = RobotsTxtReader.read(
            """
            User-agent: *
            Disallow: /

            User-agent: FooBot
            Allow: /news/
            Crawl-Delay: 10
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt).isEqualTo(
            RobotsTxt(
                ruleGroups = listOf(
                    RuleGroup(
                        userAgents = setOf("*"),
                        rules = listOf(Rule(allowed = false, "/"))
                    ),
                    RuleGroup(
                        userAgents = setOf("FooBot"),
                        rules = listOf(Rule(allowed = true, "/news/")),
                        crawlDelay = 10.seconds
                    )
                )
            )
        )
    }

    @Test
    fun shouldReadSimpleRobotsTxtIgnoringCaseSensitivityInRuleNames() {
        val robotsTxt = RobotsTxtReader.read(
            """
            user-agent: *
            DISALLOW: /

            User-agent: FooBot
            allow: /news/
            Crawl-Delay: 10
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt).isEqualTo(
            RobotsTxt(
                ruleGroups = listOf(
                    RuleGroup(
                        userAgents = setOf("*"),
                        rules = listOf(Rule(allowed = false, "/"))
                    ),
                    RuleGroup(
                        userAgents = setOf("FooBot"),
                        rules = listOf(Rule(allowed = true, "/news/")),
                        crawlDelay = 10.seconds
                    )
                )
            )
        )
    }

    @Test
    fun shouldReadSimpleRobotsTxtIgnoringWhiteSpace() {
        val robotsTxt = RobotsTxtReader.read(
            """
            User-agent       :   *
            Disallow:      /

            User-agent   :   FooBot
            Allow   : /news/
            Crawl-Delay: 10
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt).isEqualTo(
            RobotsTxt(
                ruleGroups = listOf(
                    RuleGroup(
                        userAgents = setOf("*"),
                        rules = listOf(Rule(allowed = false, "/"))
                    ),
                    RuleGroup(
                        userAgents = setOf("FooBot"),
                        rules = listOf(Rule(allowed = true, "/news/")),
                        crawlDelay = 10.seconds
                    )
                )
            )
        )
    }

    @Test
    fun shouldIgnoreIrrelevantLines() {
        val robotsTxt = RobotsTxtReader.read(
            """
            # This is a comment
            user-agent: a
            sitemap: https://example.com/sitemap.xml

            user-agent: b
            disallow: /
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt).isEqualTo(
            RobotsTxt(
                ruleGroups = listOf(
                    RuleGroup(
                        userAgents = setOf("a", "b"),
                        rules = listOf(Rule(allowed = false, "/"))
                    )
                )
            )
        )
    }

    @Test
    fun shouldGroupUserAgentsInRobotsTxt() {
        val robotsTxt = RobotsTxtReader.read(
            """
            user-agent: a
            disallow: /c

            user-agent: b
            disallow: /d

            user-agent: e
            user-agent: f
            disallow: /g

            user-agent: h
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt).isEqualTo(
            RobotsTxt(
                ruleGroups = listOf(
                    RuleGroup(
                        userAgents = setOf("a"),
                        rules = listOf(Rule(allowed = false, "/c"))
                    ),
                    RuleGroup(
                        userAgents = setOf("b"),
                        rules = listOf(Rule(allowed = false, "/d"))
                    ),
                    RuleGroup(
                        userAgents = setOf("e", "f"),
                        rules = listOf(Rule(allowed = false, "/g"))
                    ),
                    RuleGroup(
                        userAgents = setOf("h"),
                        rules = emptyList()
                    ),
                )
            )
        )
    }

    @Test
    fun shouldGroupRulesInRobotsTxt() {
        val robotsTxt = RobotsTxtReader.read(
            """
            user-agent: FooBot
            disallow: /fish

            user-agent: *
            disallow: /carrots

            user-agent: FooBot
            disallow: /shrimp
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt).isEqualTo(
            RobotsTxt(
                ruleGroups = listOf(
                    RuleGroup(
                        userAgents = setOf("FooBot"),
                        rules = listOf(Rule(allowed = false, "/fish"), Rule(allowed = false, "/shrimp"))
                    ),
                    RuleGroup(
                        userAgents = setOf("*"),
                        rules = listOf(Rule(allowed = false, "/carrots"))
                    )
                )
            )
        )
    }

    @Test
    fun shouldGroupRulesInRobotsTxtWithUserAgentInGroupAndStandalone() {
        val robotsTxt = RobotsTxtReader.read(
            """
            User-Agent: FooBot
            Disallow: /
            Allow: /x/
            
            User-Agent: BarBot
            Disallow: /
            Allow: /y/
            Allow: /w/
            
            User-Agent: BazBot
            User-Agent: FooBot
            Allow: /z/
            Disallow: /
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt)
            .usingRecursiveComparison().ignoringCollectionOrder()
            .isEqualTo(
                RobotsTxt(
                    ruleGroups = listOf(
                        RuleGroup(
                            userAgents = setOf("FooBot"),
                            rules = listOf(
                                Rule(allowed = false, "/"),
                                Rule(allowed = true, "/x/")
                            )
                        ),
                        RuleGroup(
                            userAgents = setOf("BarBot"),
                            rules = listOf(
                                Rule(allowed = false, "/"),
                                Rule(allowed = true, "/y/"),
                                Rule(allowed = true, "/w/")
                            )
                        ),
                        RuleGroup(
                            userAgents = setOf("BazBot", "FooBot"),
                            rules = listOf(
                                Rule(allowed = false, "/"),
                                Rule(allowed = true, "/z/")
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun shouldIgnoreDisallowTypos() {
        val robotsTxt = RobotsTxtReader.read(
            """
            User-agent: FooBot
            disallow: /a/
            dissallow: /b/
            dissalow: /c/
            disalow: /d/
            diasllow: /e/
            disallaw: /f/
            """.trimIndent().byteInputStream()
        )

        assertThat(robotsTxt).isEqualTo(
            RobotsTxt(
                ruleGroups = listOf(
                    RuleGroup(
                        userAgents = setOf("FooBot"),
                        rules = listOf(
                            Rule(allowed = false, "/a/"),
                            Rule(allowed = false, "/b/"),
                            Rule(allowed = false, "/c/"),
                            Rule(allowed = false, "/d/"),
                            Rule(allowed = false, "/e/"),
                            Rule(allowed = false, "/f/")
                        )
                    )
                )
            )
        )
    }

    @Test
    fun shouldIgnoreTooLongLines() {
        val robotsTxt = RobotsTxtReader.read(
            """
            User-agent: FooBot
            Disallow: /a/
            Disallow: /bbbbbbbbbb
            """.trimIndent().byteInputStream(),
            lineFilter = LineSizeFilter(20)
        )

        assertThat(robotsTxt).isEqualTo(
            RobotsTxt(
                ruleGroups = listOf(
                    RuleGroup(
                        userAgents = setOf("FooBot"),
                        rules = listOf(
                            Rule(allowed = false, "/a/")
                        )
                    )
                )
            )
        )
    }

    @Test
    fun shouldSkipTooLongRobotsUsingByteLimit() {
        val robotsTxt = RobotsTxtReader.read(
            """
            User-agent: FooBot
            Disallow: /a/
            Disallow: /bbbbbbbbbb
            """.trimIndent().byteInputStream(),
            lineFilter = RobotsSizeFilter(32)
        )

        assertThat(robotsTxt).isEqualTo(
            RobotsTxt(
                ruleGroups = listOf(
                    RuleGroup(
                        userAgents = setOf("FooBot"),
                        rules = listOf(
                            Rule(allowed = false, "/a/")
                        )
                    )
                )
            )
        )
    }

    @Test
    fun shouldSkipTooLongRobotsUsingLineLimit() {
        val robotsTxt = RobotsTxtReader.read(
            """
            User-agent: FooBot
            Disallow: /a/
            Disallow: /bbbbbbbbbb
            """.trimIndent().byteInputStream(),
            lineFilter = RobotsLinesFilter(2)
        )

        assertThat(robotsTxt).isEqualTo(
            RobotsTxt(
                ruleGroups = listOf(
                    RuleGroup(
                        userAgents = setOf("FooBot"),
                        rules = listOf(
                            Rule(allowed = false, "/a/")
                        )
                    )
                )
            )
        )
    }

    @Test
    fun shouldParseCrEnding() {
        val robotsTxt = RobotsTxtReader.read(
            (
                    "User-Agent:FooBot\n" +
                            "Disallow: /\n" +
                            "Allow: /x/\rAllow:/y/\n" +
                            "Al\r\r\r\rDisallow: /z/\n"
                    ).byteInputStream()
        )

        assertThat(robotsTxt)
            .usingRecursiveComparison().ignoringCollectionOrder()
            .isEqualTo(
                RobotsTxt(
                    ruleGroups = listOf(
                        RuleGroup(
                            userAgents = setOf("FooBot"),
                            rules = listOf(
                                Rule(allowed = false, "/"),
                                Rule(allowed = true, "/x/"),
                                Rule(allowed = true, "/y/"),
                                Rule(allowed = false, "/z/"),
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun shouldParseCrLfEnding() {
        val robotsTxt = RobotsTxtReader.read(
            (
                    "User-Agent:FooBot\r\n" +
                            "Disallow: /\r\n" +
                            "Allow: /x/\r\n" +
                            "User-Agent:BarBot\r\n" +
                            "Disallow: /\r\n" +
                            "Allow: /y/\r\n"
                    ).byteInputStream()
        )

        assertThat(robotsTxt)
            .usingRecursiveComparison().ignoringCollectionOrder()
            .isEqualTo(
                RobotsTxt(
                    ruleGroups = listOf(
                        RuleGroup(
                            userAgents = setOf("FooBot"),
                            rules = listOf(
                                Rule(allowed = false, "/"),
                                Rule(allowed = true, "/x/"),
                            )
                        ),
                        RuleGroup(
                            userAgents = setOf("BarBot"),
                            rules = listOf(
                                Rule(allowed = false, "/"),
                                Rule(allowed = true, "/y/"),
                            )
                        )
                    )
                )
            )
    }
}
