package com.github.senocak.ratehighway.util

import java.security.SecureRandom
import java.util.Random

class RandomStringGenerator(length: Int, random: Random, symbols: String) {
    private val random: Random
    private val symbols: CharArray
    private val buf: CharArray

    init {
        require(value = length >= 1)
        require(value = symbols.length >= 2)
        this.random = random
        this.symbols = symbols.toCharArray()
        buf = CharArray(length)
    }

    constructor(length: Int, random: Random = SecureRandom()):
        this(length = length, random = random, symbols = alphaNum)

    fun next(): String {
        for (i: Int in buf.indices) {
            buf[i] = symbols[random.nextInt(symbols.size)]
        }
        return String(chars = buf)
    }

    companion object {
        private const val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val lower: String = upper.lowercase()
        private const val digits = "0123456789"
        private val alphaNum: String = upper + lower + digits
    }
}
