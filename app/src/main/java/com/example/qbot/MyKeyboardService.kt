package com.example.qbot

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.view.ViewGroup

class MyKeyboardService : InputMethodService() {

    private var isCaps = false
    private var keyboardRoot: ViewGroup? = null

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
                // Handle numbers mode switch if implemented
            }
            else -> {
                val code = if (isCaps) text.uppercase() else text.lowercase()
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
                val text = child.text.toString()
                if (text.length == 1) {
                    child.text = if (isCaps) text.uppercase() else text.lowercase()
                }
            } else if (child is ViewGroup) {
                updateButtonText(child)
            }
        }
    }
}
