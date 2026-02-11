package com.leopc.speakup.utils

import android.util.Patterns

object ValidationUtils {
    
    /**
     * Validates email format using Android's Patterns utility
     * @param email The email address to validate
     * @return true if email is valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validates password strength
     * @param password The password to validate
     * @return true if password meets minimum requirements, false otherwise
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
    
    /**
     * Validates that a string is not empty or blank
     * @param text The text to validate
     * @return true if text is not empty, false otherwise
     */
    fun isNotEmpty(text: String): Boolean {
        return text.isNotBlank()
    }
}
