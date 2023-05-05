package com.github.alturkovic.robots.txt

import com.github.alturkovic.robots.txt.LongestRuleSelectionStrategy.select
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private class LongestRuleSelectionStrategyTest {

    @Test
    fun shouldSelectMoreSpecificRule() {
        val rules = listOf(
            Rule(allowed = true, "/p"),
            Rule(allowed = false, "/")
        )

        assertThat(select(rules)).isEqualTo(rules[0])
    }

    @Test
    fun shouldSelectLeastRestrictiveRule() {
        val rules = listOf(
            Rule(allowed = true, "/folder"),
            Rule(allowed = false, "/folder")
        )

        assertThat(select(rules)).isEqualTo(rules[0])
    }

    @Test
    fun shouldSelectRuleWithLongerPathThatMatchesMoreCharactersInPath() {
        val rules = listOf(
            Rule(allowed = true, "/page"),
            Rule(allowed = false, "/*.htm")
        )

        assertThat(select(rules)).isEqualTo(rules[1])
    }

    @Test
    fun shouldSelectLeastRestrictiveRuleWithWildcard() {
        val rules = listOf(
            Rule(allowed = true, "/page"),
            Rule(allowed = false, "/*.ph")
        )

        assertThat(select(rules)).isEqualTo(rules[0])
    }

    @Test
    fun shouldSelectMoreSpecificRootRule() {
        val rules = listOf(
            Rule(allowed = true, "/$"),
            Rule(allowed = false, "/")
        )

        assertThat(select(rules)).isEqualTo(rules[0])
    }
}
