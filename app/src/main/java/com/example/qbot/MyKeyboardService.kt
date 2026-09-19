package com.example.qbot

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.view.ViewGroup

class MyKeyboardService : InputMethodService() {

    private var isCaps = false
    private var isNumeric = false
    private var isSymbols = false
    private var keyboardRoot: ViewGroup? = null

    private val keyMapping by lazy {
        mapOf(
            R.id.btn_q to Triple("Q", "1", "["),
            R.id.btn_w to Triple("W", "2", "]"),
            R.id.btn_e to Triple("E", "3", "{"),
            R.id.btn_r to Triple("R", "4", "}"),
            R.id.btn_t to Triple("T", "5", "#"),
            R.id.btn_y to Triple("Y", "6", "%"),
            R.id.btn_u to Triple("U", "7", "^"),
            R.id.btn_i to Triple("I", "8", "*"),
            R.id.btn_o to Triple("O", "9", "+"),
            R.id.btn_p to Triple("P", "0", "="),
            R.id.btn_a to Triple("A", "-", "_"),
            R.id.btn_s to Triple("S", "/", "\\"),
            R.id.btn_d to Triple("D", ":", "|"),
            R.id.btn_f to Triple("F", ";", "~"),
            R.id.btn_g to Triple("G", "(", "<"),
            R.id.btn_h to Triple("H", ")", ">"),
            R.id.btn_j to Triple("J", "$", "€"),
            R.id.btn_k to Triple("K", "&", "£"),
            R.id.btn_l to Triple("L", "@", "¥"),
            R.id.btn_shift to Triple("SHIFT", "#+=", "123"),
            R.id.btn_z to Triple("Z", ".", "."),
            R.id.btn_x to Triple("X", ",", ","),
            R.id.btn_c to Triple("C", "?", "?"),
            R.id.btn_v to Triple("V", "!", "!"),
            R.id.btn_b to Triple("B", "'", "'"),
            R.id.btn_n to Triple("N", "\"", "\""),
            R.id.btn_m to Triple("M", "_", "·"),
            R.id.btn_mode_switch to Triple("123", "ABC", "ABC")
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
                    handleKeyPress(child as Button)
                }
            } else if (child is ViewGroup) {
                setupKeyboard(child)
            }
        }
    }

    private fun handleKeyPress(button: Button) {
        val ic = currentInputConnection ?: return
        val text = button.text.toString()
        val id = button.id

        when {
            text == "DEL" -> ic.deleteSurroundingText(1, 0)
            text == "SPACE" -> ic.commitText(" ", 1)
            text == "ENTER" -> ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            id == R.id.btn_shift -> {
                if (!isNumeric && !isSymbols) {
                    isCaps = !isCaps
                } else {
                    isSymbols = !isSymbols
                }
                updateKeyboard()
            }
            id == R.id.btn_mode_switch -> {
                if (isNumeric || isSymbols) {
                    isNumeric = false
                    isSymbols = false
                } else {
                    isNumeric = true
                }
                updateKeyboard()
            }
            else -> {
                val code = if (isNumeric || isSymbols) text else {
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
                    val baseText = when {
                        isSymbols -> mapping.third
                        isNumeric -> mapping.second
                        else -> mapping.first
                    }
                    child.text = if (!isNumeric && !isSymbols && baseText.length == 1) {
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
