package com.github.alturkovic.robots.txt

sealed interface Grant {
    val allowed: Boolean
}

data class MatchedGrant(
    override val allowed: Boolean,
    val matchedRule: Rule,
    val matchedRuleGroup: RuleGroup
): Grant

object NonMatchedAllowedGrant: Grant {
    override val allowed = true
}
