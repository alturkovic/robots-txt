package com.github.alturkovic.robots.txt

import kotlin.time.Duration

data class RuleGroup(
    val userAgents: Set<String> = emptySet(),
    val rules: List<Rule>,
    val crawlDelay: Duration? = null,
) {
    fun isApplicableTo(userAgent: String) = userAgents.any { it == "*" || userAgent.startsWith(it, ignoreCase = true) }
}
