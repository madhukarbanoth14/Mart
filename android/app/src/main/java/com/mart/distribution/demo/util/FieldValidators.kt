package com.mart.distribution.demo.util

import android.util.Patterns

/**
 * Input filters (keystroke-level) and submit validators for form fields.
 */
object FieldFilters {
    fun personName(
        raw: String,
        maxLen: Int = 80,
    ): String =
        raw
            .filter { it.isLetter() || it.isWhitespace() || it in ".-'&" }
            .take(maxLen)

    fun businessName(
        raw: String,
        maxLen: Int = 100,
    ): String =
        raw
            .filter { it.isLetterOrDigit() || it.isWhitespace() || it in ".-'&/#()," }
            .take(maxLen)

    fun email(
        raw: String,
        maxLen: Int = 120,
    ): String =
        raw
            .trimStart()
            .filter { it.isLetterOrDigit() || it in "@._-+ " }
            .take(maxLen)
            .trimEnd()

    /** Mobile or email login id — digits-only until @ appears, then email chars. */
    fun loginIdentifier(
        raw: String,
        maxLen: Int = 120,
    ): String {
        val trimmed = raw.trimStart().take(maxLen)
        return if ('@' in trimmed) {
            email(trimmed, maxLen)
        } else {
            phone(raw, maxLen = 10)
        }
    }

    /** Indian mobile: digits only, max 10. */
    fun phone(
        raw: String,
        maxLen: Int = 10,
    ): String = raw.filter(Char::isDigit).take(maxLen)

    fun digits(
        raw: String,
        maxLen: Int,
    ): String = raw.filter(Char::isDigit).take(maxLen)

    fun decimal(
        raw: String,
        maxDecimals: Int = 2,
    ): String {
        val cleaned =
            buildString {
                var dotSeen = false
                var decimals = 0
                for (ch in raw) {
                    when {
                        ch.isDigit() -> {
                            if (dotSeen) {
                                if (decimals >= maxDecimals) continue
                                decimals++
                            }
                            append(ch)
                        }
                        ch == '.' && !dotSeen -> {
                            dotSeen = true
                            append(ch)
                        }
                    }
                }
            }
        return cleaned
    }

    fun percentage(raw: String): String = decimal(raw, maxDecimals = 2).take(6)

    fun gstin(raw: String): String =
        raw
            .uppercase()
            .filter { it.isLetterOrDigit() }
            .take(15)

    fun brandType(raw: String): String =
        raw
            .uppercase()
            .filter { it.isLetter() }
            .take(5)

    fun cardNumber(raw: String): String = digits(raw, maxLen = 16)

    /** Formats as MM/YY while typing. */
    fun cardExpiry(raw: String): String {
        val digits = digits(raw, maxLen = 4)
        return when {
            digits.length <= 2 -> digits
            else -> "${digits.take(2)}/${digits.drop(2)}"
        }
    }

    fun cvv(raw: String): String = digits(raw, maxLen = 4)

    fun hexToken(raw: String): String =
        raw
            .lowercase()
            .filter { it.isDigit() || it in 'a'..'f' }
            .take(64)

    fun url(raw: String, maxLen: Int = 500): String =
        raw
            .trimStart()
            .filter { !it.isISOControl() }
            .take(maxLen)
}

object FieldValidators {
    private val personNamePattern = Regex("^[\\p{L}][\\p{L}\\s.'-&]{1,79}$")
    private val gstinPattern = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")
    private val indianPhonePattern = Regex("^[6-9]\\d{9}$")

    fun personName(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.length < 2) return "Enter a valid name (at least 2 characters)"
        if (!personNamePattern.matches(trimmed)) {
            return "Name can only contain letters, spaces, and .-'&"
        }
        return null
    }

    fun businessName(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.length < 2) return "Enter a valid name (at least 2 characters)"
        if (!Regex("^[\\p{L}\\d][\\p{L}\\d\\s.'&/#(),-]{1,99}$").matches(trimmed)) {
            return "Name contains invalid characters"
        }
        return null
    }

    fun email(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return "Email is required"
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            return "Enter a valid email address"
        }
        return null
    }

    fun loginIdentifier(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return "Enter mobile number or email"
        return if ('@' in trimmed) {
            email(trimmed)
        } else {
            phoneRequired(trimmed)
        }
    }

    fun phoneOptional(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        return phoneRequired(trimmed)
    }

    fun phoneRequired(value: String): String? {
        val digits = value.filter(Char::isDigit)
        if (!indianPhonePattern.matches(digits)) {
            return "Enter a valid 10-digit mobile number (starts with 6–9)"
        }
        return null
    }

    fun password(
        value: String,
        minLen: Int = 8,
    ): String? {
        if (value.length < minLen) return "Password must be at least $minLen characters"
        return null
    }

    fun passwordOptional(
        value: String,
        minLen: Int = 8,
    ): String? {
        if (value.isBlank()) return null
        return password(value, minLen)
    }

    fun gstinOptional(value: String): String? {
        val trimmed = value.trim().uppercase()
        if (trimmed.isEmpty()) return null
        if (trimmed.length != 15 || !gstinPattern.matches(trimmed)) {
            return "Enter a valid 15-character GSTIN"
        }
        return null
    }

    fun decimalInRange(
        value: String,
        min: Double,
        max: Double,
        label: String,
    ): String? {
        val parsed = value.trim().toDoubleOrNull()
            ?: return "$label must be a number"
        if (parsed !in min..max) return "$label must be between $min and $max"
        return null
    }

    fun brandType(value: String): String? {
        val upper = value.trim().uppercase()
        if (upper !in setOf("OWN", "OTHER")) return "Brand type must be OWN or OTHER"
        return null
    }

    fun resetToken(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.length < 16) return "Reset token looks invalid"
        if (!Regex("^[0-9a-f]+$").matches(trimmed)) return "Reset token must contain only letters a–f and digits"
        return null
    }

    fun cardNumber(value: String): String? {
        val digits = value.filter(Char::isDigit)
        if (digits.length !in 13..16) return "Enter a valid card number (13–16 digits)"
        return null
    }

    fun cardExpiry(value: String): String? {
        if (!Regex("^(0[1-9]|1[0-2])/\\d{2}$").matches(value.trim())) {
            return "Expiry must be MM/YY"
        }
        return null
    }

    fun cvv(value: String): String? {
        val digits = value.filter(Char::isDigit)
        if (digits.length !in 3..4) return "CVV must be 3 or 4 digits"
        return null
    }

    fun cardName(value: String): String? = personName(value)

    fun urlOptional(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        if (!Patterns.WEB_URL.matcher(trimmed).matches() &&
            !trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return "Enter a valid URL (https://…)"
        }
        return null
    }
}
