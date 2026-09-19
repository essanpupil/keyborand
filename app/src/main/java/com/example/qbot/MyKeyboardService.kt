package com.example.qbot

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.view.ViewGroup

class MyKeyboardService : InputMethodService() {

    private var isCaps = false
    private var isNumeric = false
    private var keyboardRoot: ViewGroup? = null

    private val keyMapping by lazy {
        mapOf(
            R.id.btn_q to ("Q" to "1"),
            R.id.btn_w to ("W" to "2"),
            R.id.btn_e to ("E" to "3"),
            R.id.btn_r to ("R" to "4"),
            R.id.btn_t to ("T" to "5"),
            R.id.btn_y to ("Y" to "6"),
            R.id.btn_u to ("U" to "7"),
            R.id.btn_i to ("I" to "8"),
            R.id.btn_o to ("O" to "9"),
            R.id.btn_p to ("P" to "0"),
            R.id.btn_a to ("A" to "-"),
            R.id.btn_s to ("S" to "/"),
            R.id.btn_d to ("D" to ":"),
            R.id.btn_f to ("F" to ";"),
            R.id.btn_g to ("G" to "("),
            R.id.btn_h to ("H" to ")"),
            R.id.btn_j to ("J" to "$"),
            R.id.btn_k to ("K" to "&"),
            R.id.btn_l to ("L" to "@"),
            R.id.btn_shift to ("SHIFT" to "#+="),
            R.id.btn_z to ("Z" to "."),
            R.id.btn_x to ("X" to ","),
            R.id.btn_c to ("C" to "?"),
            R.id.btn_v to ("V" to "!"),
            R.id.btn_b to ("B" to "'"),
            R.id.btn_n to ("N" to "\""),
            R.id.btn_m to ("M" to "_"),
            R.id.btn_mode_switch to ("123" to "ABC")
        )
    }

    override fun onCreateInputView(): View {
        val keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as ViewGroup
        keyboardRoot = keyboardView
        setupKeyboard(keyboardView)
        return keyboardView
    }

    private fun setupKeyboard(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is Button) {
                child.setOnClickListener {
                    handleKeyPress(child)
                }
            } else if (child is ViewGroup) {
                setupKeyboard(child)
            }
        }
    }

    private fun handleKeyPress(button: Button) {
        val ic = currentInputConnection ?: return
        val text = button.text.toString()
        when (text) {
            "DEL" -> {
                ic.deleteSurroundingText(1, 0)
            }
            "SPACE" -> {
                ic.commitText(" ", 1)
            }
            "ENTER" -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            }
            "SHIFT" -> {
                isCaps = !isCaps
                updateKeyboard()
            }
            "123" -> {
                isNumeric = true
                updateKeyboard()
            }
            "ABC" -> {
                isNumeric = false
                updateKeyboard()
            }
            "#+=" -> {
                // Could implement secondary symbols here
            }
            else -> {
                val code = if (isNumeric) text else {
                    if (isCaps) text.uppercase() else text.lowercase()
                }
                ic.commitText(code, 1)
            }
        }
    }

    private fun updateKeyboard() {
        keyboardRoot?.let { updateButtonText(it) }
    }

    private fun updateButtonText(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is Button) {
                val mapping = keyMapping[child.id]
                if (mapping != null) {
                    val baseText = if (isNumeric) mapping.second else mapping.first
                    child.text = if (!isNumeric && baseText.length == 1) {
                        if (isCaps) baseText.uppercase() else baseText.lowercase()
                    } else {
                        baseText
                    }
                }
            } else if (child is ViewGroup) {
                updateButtonText(child)
            }
        }
    }
}
