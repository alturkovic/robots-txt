package com.github.alturkovic.robots.txt

interface LineFilter {
    fun accept(line: String): Boolean
}

class LineSizeFilter(
    private val maxSize: Int
): LineFilter {
    override fun accept(line: String): Boolean = line.length <= maxSize
}

class RobotsLinesFilter(
    private val maxLines: Int
): LineFilter {
    private var seenLines = 0
    override fun accept(line: String): Boolean {
        return seenLines++ < maxLines
    }
}

class RobotsSizeFilter(
    private val maxByteSize: Int
): LineFilter {
    private var seenBytes = 0
    override fun accept(line: String): Boolean {
        seenBytes += line.length
        return seenBytes < maxByteSize
    }
}
