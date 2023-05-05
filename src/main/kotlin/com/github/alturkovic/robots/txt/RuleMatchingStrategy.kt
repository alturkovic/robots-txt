package com.github.alturkovic.robots.txt

interface RuleMatchingStrategy {
    fun matches(rule: Rule, path: String): Boolean
}

data object WildcardRuleMatchingStrategy : RuleMatchingStrategy {
    override fun matches(rule: Rule, path: String): Boolean {
        return matches(rule.pattern, path)
    }

    // taken from Google
    // https://github.com/google/robotstxt-java/blob/master/src/main/java/com/google/search/robotstxt/RobotsLongestMatchStrategy.java
    private fun matches(pattern: String, path: String): Boolean {
        // "Prefixes" array stores "path" prefixes that match specific prefix of "pattern".
        // Prefixes of "pattern" are iterated over in ascending order in the loop below.
        // Each prefix is represented by its end index (exclusive), the array stores them in ascending order.
        val prefixes = IntArray(path.length + 1)
        prefixes[0] = 0
        var prefixesCount = 1
        for (i in pattern.indices) {
            val ch = pattern[i]

            // '$' in the end of pattern indicates its termination.
            if (ch == '$' && i + 1 == pattern.length) return prefixes[prefixesCount - 1] == path.length

            // In case of '*' occurrence all path prefixes starting from the shortest one may be matched.
            if (ch == '*') {
                prefixesCount = path.length - prefixes[0] + 1
                for (j in 1 until prefixesCount) prefixes[j] = prefixes[j - 1] + 1
            } else {
                // Iterate over each previous prefix and try to extend by one character.
                var newPrefixesCount = 0
                for (j in 0 until prefixesCount) {
                    if (prefixes[j] < path.length && path[prefixes[j]] == ch) {
                        prefixes[newPrefixesCount++] = prefixes[j] + 1
                    }
                }
                if (newPrefixesCount == 0) return false
                prefixesCount = newPrefixesCount
            }
        }
        return true
    }
}
