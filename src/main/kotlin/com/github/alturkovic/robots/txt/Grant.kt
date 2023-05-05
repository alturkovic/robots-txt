package com.github.alturkovic.robots.txt

sealed interface Grant {
    val allowed: Boolean
}

data class MatchedGrant(
    override val allowed: Boolean,
    val matchedRule: Rule,
    val matchedRuleGroup: RuleGroup
): Grant

data object AllowedGrant: Grant {
    override val allowed = true
}
