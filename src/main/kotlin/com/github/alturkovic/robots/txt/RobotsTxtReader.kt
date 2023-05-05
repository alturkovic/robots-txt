package com.github.alturkovic.robots.txt

import com.github.alturkovic.robots.txt.RobotsLineParser.CrawlDelayEntry
import com.github.alturkovic.robots.txt.RobotsLineParser.RuleEntry
import com.github.alturkovic.robots.txt.RobotsLineParser.SkipLineEntry
import com.github.alturkovic.robots.txt.RobotsLineParser.UserAgentEntry
import mu.KotlinLogging
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data object RobotsTxtReader {
    private val log = KotlinLogging.logger {}

    fun read(
        input: InputStream,
        ruleMatchingStrategy: RuleMatchingStrategy = WildcardRuleMatchingStrategy,
        ruleSelectionStrategy: RuleSelectionStrategy = LongestRuleSelectionStrategy,
        ignoreLinesLongerThan: Long = Long.MAX_VALUE
    ): RobotsTxt {
        val builder = RobotsBuilder()

        BufferedReader(InputStreamReader(input)).lineSequence()
            .map { it.trim() }
            .filter { it.length < ignoreLinesLongerThan }
            .forEach {
                when (val entry = RobotsLineParser.parseLine(it)) {
                    is UserAgentEntry -> builder.acceptUserAgent(entry.userAgent)
                    is RuleEntry -> builder.acceptRule(entry.rule)
                    is CrawlDelayEntry -> builder.acceptCrawlDelay(entry.crawlDelay)

                    is SkipLineEntry -> log.debug { "Ignoring line: $it" }
                }
            }

        return builder.build(ruleMatchingStrategy, ruleSelectionStrategy)
    }
}

private object RobotsLineParser {
    private val disallowKeys = listOf("disallow", "dissallow", "dissalow", "disalow", "diasllow", "disallaw")

    fun parseLine(line: String): LineEntry {
        if (line.startsWith('#')) return SkipLineEntry

        val colonIndex = line.indexOf(':')
        if (colonIndex == -1) return SkipLineEntry

        val key = line.substring(0, colonIndex).trim().takeIf { it.isNotBlank() } ?: return SkipLineEntry
        val value = line.substring(colonIndex + 1, line.indexOf('#').takeIf { it != -1 } ?: line.length).trim()

        return when {
            key.equals("user-agent", ignoreCase = true) -> UserAgentEntry(value)
            key.equals("crawl-delay", ignoreCase = true) -> CrawlDelayEntry(value.toInt().seconds)
            key.equals("allow", ignoreCase = true) -> RuleEntry(Rule(allowed = true, value))
            disallowKeys.contains(key.lowercase()) -> RuleEntry(Rule(allowed = false, value))
            else -> SkipLineEntry
        }
    }

    sealed interface LineEntry

    data class UserAgentEntry(
        val userAgent: String
    ) : LineEntry

    data class RuleEntry(
        val rule: Rule
    ) : LineEntry

    data class CrawlDelayEntry(
        val crawlDelay: Duration
    ) : LineEntry

    data object SkipLineEntry : LineEntry
}

private class RobotsBuilder {
    var completedRuleGroups = mutableListOf<MutableRuleGroup>()
    var currentRuleGroup = MutableRuleGroup()
    var foundContent = false

    fun acceptUserAgent(userAgent: String) {
        if (foundContent) {
            finalizeCurrentGroup()
            foundContent = false
        }
        currentRuleGroup.userAgents.add(userAgent)
    }

    fun acceptRule(rule: Rule) {
        foundContent = true
        currentRuleGroup.rules.add(rule)
    }

    fun acceptCrawlDelay(crawlDelay: Duration) {
        foundContent = true
        currentRuleGroup.crawlDelay = crawlDelay
    }

    private fun finalizeCurrentGroup() {
        if (currentRuleGroup.userAgents.isNotEmpty()) completedRuleGroups.add(currentRuleGroup)
        currentRuleGroup = MutableRuleGroup()
    }

    fun build(ruleMatchingStrategy: RuleMatchingStrategy, ruleSelectionStrategy: RuleSelectionStrategy): RobotsTxt {
        finalizeCurrentGroup()
        mergeCompletedRuleGroups()

        val ruleGroups = completedRuleGroups.map { it.toRuleGroup() }
        return RobotsTxt(ruleGroups, ruleMatchingStrategy, ruleSelectionStrategy)
    }

    private fun mergeCompletedRuleGroups() {
        val userAgentToRuleGroups = mergeGroupsByUserAgent()
        completedRuleGroups = userAgentToRuleGroups
            .values.distinct()
            .map { mergeRuleGroups(it) }.toMutableList()
    }

    private fun mergeGroupsByUserAgent() = completedRuleGroups.groupBy { it.userAgents }

    private fun mergeRuleGroups(ruleGroups: List<MutableRuleGroup>): MutableRuleGroup {
        if (ruleGroups.size == 1) return ruleGroups.first()

        val mergedGroup = MutableRuleGroup()
        mergedGroup.crawlDelay = ruleGroups.minByOrNull { it.crawlDelay ?: Duration.INFINITE }?.crawlDelay
        mergedGroup.userAgents.addAll(ruleGroups.flatMap { it.userAgents })
        mergedGroup.rules.addAll(ruleGroups.flatMap { it.rules })
        return mergedGroup
    }

    data class MutableRuleGroup(
        var userAgents: MutableSet<String> = mutableSetOf(),
        var rules: MutableList<Rule> = mutableListOf(),
        var crawlDelay: Duration? = null,
    )

    private fun MutableRuleGroup.toRuleGroup() = RuleGroup(userAgents, rules, crawlDelay)
}
