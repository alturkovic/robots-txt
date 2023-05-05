package com.github.alturkovic.robots.txt

interface RuleSelectionStrategy {
    fun select(rules: List<Rule>): Rule?
}

object LongestRuleSelectionStrategy : RuleSelectionStrategy {
    override fun select(rules: List<Rule>): Rule? {
        val groupedRules = rules.groupBy { it.allowed }

        val longestAllowRule = groupedRules[true]?.withLongestPattern()

        val longestDisallowRule = groupedRules[false]
            ?.filterNot { it.pattern.isBlank() }
            ?.withLongestPattern()
            ?: return longestAllowRule

        if (longestAllowRule == null) return longestDisallowRule

        return if (longestAllowRule.pattern.length >= longestDisallowRule.pattern.length) {
            longestAllowRule
        } else {
            longestDisallowRule
        }
    }

    private fun List<Rule>.withLongestPattern() = maxByOrNull { it.pattern.length }
}
