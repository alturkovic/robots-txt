package com.github.alturkovic.robots.txt

import com.github.alturkovic.robots.txt.StringUtils.greatestCommonPrefix

data class RobotsTxt(
    val ruleGroups: List<RuleGroup>,
    private val ruleMatchingStrategy: RuleMatchingStrategy = WildcardRuleMatchingStrategy,
    private val ruleSelectionStrategy: RuleSelectionStrategy = LongestRuleSelectionStrategy
) {
    fun query(userAgent: String, path: String): Grant {
        val candidates = candidates(userAgent).ifEmpty { return AllowedGrant }

        val mostSpecificRuleGroup = candidates.findBestUserAgentMatch(userAgent)
        val matchedRules = mostSpecificRuleGroup.rules.filter { ruleMatchingStrategy.matches(it, path) }
        val mostImportantRule = ruleSelectionStrategy.select(matchedRules) ?: return AllowedGrant
        return MatchedGrant(mostImportantRule.allowed, mostImportantRule, mostSpecificRuleGroup)
    }

    private fun candidates(userAgent: String) = ruleGroups.filter { it.isApplicableTo(userAgent) }

    private fun List<RuleGroup>.findBestUserAgentMatch(userAgent: String): RuleGroup {
        var longestMatch = 0
        lateinit var bestMatch: RuleGroup
        for (candidate in this) {
            for (ruleGroupAgent in candidate.userAgents) {
                val matchLength = calculateUserAgentMatchLength(userAgent, ruleGroupAgent)
                if (matchLength > longestMatch) {
                    longestMatch = matchLength;
                    bestMatch = candidate
                }
            }
        }
        return bestMatch
    }

    private fun calculateUserAgentMatchLength(userAgent: String, ruleGroupAgent: String): Int =
        if (ruleGroupAgent == "*") 1 else greatestCommonPrefix(userAgent, ruleGroupAgent).length
}
