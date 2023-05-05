package com.github.alturkovic.robots.txt

import com.github.alturkovic.robots.txt.WildcardRuleMatchingStrategy.matches
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private class WildcardRuleMatchingStrategyTest {

    @Test
    fun shouldMatchRootAndAnyLower() {
        // these 3 rules are equivalent
        val rootRules = listOf(
            Rule(allowed = true, "/"),
            Rule(allowed = true, "/*"),
            Rule(allowed = false, "")
        )

        assertThat(rootRules.all { matches(it, "/") }).isTrue()
        assertThat(rootRules.all { matches(it, "/index.html") }).isTrue()
    }

    @Test
    fun shouldMatchRootOnly() {
        val rootRuleOnly = Rule(allowed = true, "/$")

        assertThat(matches(rootRuleOnly, "/")).isTrue()
        assertThat(matches(rootRuleOnly, "/index.html")).isFalse()
    }

    @Test
    fun shouldMatchAnyPathThatStartsWith() {
        // these 2 rules are equivalent
        val rules = listOf(
            Rule(allowed = true, "/fish"),
            Rule(allowed = true, "/fish*")
        )

        assertThat(rules.all { matches(it, "/fish") }).isTrue()
        assertThat(rules.all { matches(it, "/fish.html") }).isTrue()
        assertThat(rules.all { matches(it, "/fish/salmon.html") }).isTrue()
        assertThat(rules.all { matches(it, "/fishheads") }).isTrue()
        assertThat(rules.all { matches(it, "/fishheads/yummy.html") }).isTrue()
        assertThat(rules.all { matches(it, "/fish.php?id=anything") }).isTrue()

        assertThat(rules.all { matches(it, "/Fish.asp") }).isFalse()
        assertThat(rules.all { matches(it, "/catfish") }).isFalse()
        assertThat(rules.all { matches(it, "/?id=fish") }).isFalse()
        assertThat(rules.all { matches(it, "/desert/fish") }).isFalse()
    }

    @Test
    fun shouldMatchAnythingInFolder() {
        val rule = Rule(allowed = true, "/fish/")

        assertThat(matches(rule, "/fish/")).isTrue()
        assertThat(matches(rule, "/fish/?id=anything")).isTrue()
        assertThat(matches(rule, "/fish/salmon.html")).isTrue()

        assertThat(matches(rule, "/fish")).isFalse()
        assertThat(matches(rule, "/fish.html")).isFalse()
        assertThat(matches(rule, "/desert/fish")).isFalse()
        assertThat(matches(rule, "/Fish/Salmon.php")).isFalse()
    }

    @Test
    fun shouldMatchAnythingThatContains() {
        val rule = Rule(allowed = true, "/*.php")

        assertThat(matches(rule, "/index.php")).isTrue()
        assertThat(matches(rule, "/filename.php")).isTrue()
        assertThat(matches(rule, "/folder/filename.php")).isTrue()
        assertThat(matches(rule, "/folder/filename.php?parameters")).isTrue()
        assertThat(matches(rule, "/folder/any.php.file.html")).isTrue()
        assertThat(matches(rule, "/filename.php/")).isTrue()

        assertThat(matches(rule, "/")).isFalse()
        assertThat(matches(rule, "/windows.PHP")).isFalse()
    }

    @Test
    fun shouldMatchAnythingThatEndsWith() {
        val rule = Rule(allowed = true, "/*.php$")

        assertThat(matches(rule, "/filename.php")).isTrue()
        assertThat(matches(rule, "/folder/filename.php")).isTrue()

        assertThat(matches(rule, "/")).isFalse()
        assertThat(matches(rule, "/folder/filename.php?parameters")).isFalse()
        assertThat(matches(rule, "/filename.php/")).isFalse()
        assertThat(matches(rule, "/filename.php5")).isFalse()
        assertThat(matches(rule, "/windows.PHP")).isFalse()
    }

    @Test
    fun shouldMatchOrdered() {
        val rule = Rule(allowed = true, "/fish*.php")

        assertThat(matches(rule, "/fish.php")).isTrue()
        assertThat(matches(rule, "/fishheads/catfish.php?parameters")).isTrue()

        assertThat(matches(rule, "/")).isFalse()
        assertThat(matches(rule, "/filename.php/fish")).isFalse()
        assertThat(matches(rule, "/Fish.php")).isFalse()
    }
}
