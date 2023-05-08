package com.github.alturkovic.robots.txt

import com.github.alturkovic.robots.txt.StringUtils.greatestCommonPrefix

data class RobotsTxt(
    val ruleGroups: List<RuleGroup>,
    private val ruleMatchingStrategy: RuleMatchingStrategy = WildcardRuleMatchingStrategy,
    private val ruleSelectionStrategy: RuleSelectionStrategy = LongestRuleSelectionStrategy
) {
    fun query(userAgent: String, path: String): Grant {
        val candidates = candidates(userAgent).ifEmpty { return NonMatchedAllowedGrant }

        val mostSpecificRuleGroups = candidates.findBestUserAgentMatches(userAgent)
        val mostImportantRule = mostSpecificRuleGroups.findMostImportantRule(path) ?: return NonMatchedAllowedGrant
        val mostSpecificRuleGroup = mostSpecificRuleGroups.first { mostImportantRule in it.rules }
        return MatchedGrant(mostImportantRule.allowed, mostImportantRule, mostSpecificRuleGroup)
    }

    private fun candidates(userAgent: String) = ruleGroups.filter { it.isApplicableTo(userAgent) }

    private fun List<RuleGroup>.findMostImportantRule(path: String): Rule? {
        val matchedRules = this
            .flatMap { it.rules }
            .filter { ruleMatchingStrategy.matches(it, path) }

        return ruleSelectionStrategy.select(matchedRules)
    }

    private fun List<RuleGroup>.findBestUserAgentMatches(userAgent: String): List<RuleGroup> {
        var longestMatch = 0
        val bestMatches = mutableListOf<RuleGroup>()
        for (candidate in this) {
            for (ruleGroupAgent in candidate.userAgents) {
                val matchLength = calculateUserAgentMatchLength(userAgent, ruleGroupAgent)
                if (matchLength > longestMatch) {
                    longestMatch = matchLength
                    bestMatches.clear()
                    bestMatches.add(candidate)
                } else if (matchLength == longestMatch) {
                    bestMatches.add(candidate)
                }
            }
        }
        return bestMatches
    }

    private fun calculateUserAgentMatchLength(userAgent: String, ruleGroupAgent: String): Int =
        if (ruleGroupAgent == "*") 1 else greatestCommonPrefix(userAgent, ruleGroupAgent).length
}
