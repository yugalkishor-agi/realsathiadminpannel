package com.incoteam.realsaathi.utils

object PhoneNumberFormatter {

    fun toE164(rawValue: String, dialCode: String): String {
        val digits = rawValue.filter(Char::isDigit).trimStart('0')
        val code = dialCode.filter(Char::isDigit)
        return if (code.isNotBlank() && digits.length in 6..14) "+$code$digits" else ""
    }

    fun toIndianE164(rawValue: String): String {
        val digits = rawValue.filter(Char::isDigit)
        return when {
            digits.length == 10 -> "+91$digits"
            digits.length == 11 && digits.startsWith("0") -> "+91${digits.drop(1)}"
            digits.length == 12 && digits.startsWith("91") -> "+$digits"
            rawValue.startsWith("+") && digits.length >= 10 -> "+$digits"
            else -> ""
        }
    }

    fun mask(phoneNumber: String): String {
        val digits = phoneNumber.filter(Char::isDigit)
        return if (digits.length >= 12 && digits.startsWith("91")) {
            "+91 xxxxxx${digits.takeLast(4)}"
        } else {
            phoneNumber
        }
    }

    fun toIndianSubscriberNumber(rawValue: String): String {
        val digits = rawValue.filter(Char::isDigit)
        return when {
            digits.length == 10 -> digits
            digits.length == 11 && digits.startsWith("0") -> digits.drop(1)
            digits.length >= 12 && digits.startsWith("91") -> digits.takeLast(10)
            digits.length > 10 -> digits.takeLast(10)
            else -> digits
        }
    }
}
