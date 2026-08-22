package com.nilian.app

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SecurityPinValidationTest {

    private fun validateAndSanitizePin(rawInput: String): String? {
        val clean = rawInput.filter { it.isDigit() }.take(6)
        return if (clean.length in 4..6) clean else null
    }

    @Test
    fun pinSanitizer_acceptsValid4DigitPin() {
        val pin = "1234"
        val result = validateAndSanitizePin(pin)
        assertThat(result).isEqualTo("1234")
    }

    @Test
    fun pinSanitizer_acceptsValid6DigitPin() {
        val pin = "987654"
        val result = validateAndSanitizePin(pin)
        assertThat(result).isEqualTo("987654")
    }

    @Test
    fun pinSanitizer_filtersOutLettersAndSymbols() {
        val raw = "1a2#3$4"
        val result = validateAndSanitizePin(raw)
        assertThat(result).isEqualTo("1234")
    }

    @Test
    fun pinSanitizer_rejectsShortPin() {
        val raw = "12a"
        val result = validateAndSanitizePin(raw)
        assertThat(result).isNull()
    }

    @Test
    fun pinSanitizer_truncatesTo6Digits() {
        val raw = "1234567890"
        val result = validateAndSanitizePin(raw)
        assertThat(result).isEqualTo("123456")
    }
}
