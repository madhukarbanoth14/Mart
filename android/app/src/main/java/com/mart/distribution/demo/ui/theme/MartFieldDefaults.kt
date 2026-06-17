package com.mart.distribution.demo.ui.theme

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

object MartFieldDefaults {
    /** Latin/English text fields — avoids Hindi and other script IME suggestions. */
    val englishTextKeyboard =
        KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            capitalization = KeyboardCapitalization.Words,
            autoCorrect = true,
        )

    val englishMultilineKeyboard =
        KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            capitalization = KeyboardCapitalization.Sentences,
            autoCorrect = true,
        )

    /** Light surfaces — typed text is dark/black. */
    @Composable
    fun outlinedColors() =
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = WholesaleText,
            unfocusedTextColor = WholesaleText,
            disabledTextColor = WholesaleInk4,
            cursorColor = WholesaleBlue,
            focusedBorderColor = WholesaleBlue,
            unfocusedBorderColor = WholesaleBorder,
            focusedLabelColor = WholesaleBlue,
            unfocusedLabelColor = WholesaleMuted,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        )

    /** Login / reset screens on dark background — white field, black text. */
    @Composable
    fun outlinedOnDarkColors() =
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = WholesaleText,
            unfocusedTextColor = WholesaleText,
            disabledTextColor = WholesaleInk4,
            cursorColor = WholesaleBlue,
            focusedBorderColor = WholesaleBlue,
            unfocusedBorderColor = WholesaleBorder,
            focusedLabelColor = WholesaleBlue,
            unfocusedLabelColor = WholesaleMuted,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White.copy(alpha = 0.96f),
        )
}
