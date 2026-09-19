package com.example.qbot

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.view.ViewGroup

class MyKeyboardService : InputMethodService() {

    private var isCaps = false
    private var isNumeric = false
    private var isSymbols = false
    private var isForcedNumeric = false
    private var keyboardRoot: ViewGroup? = null
    private var currentLayoutResId: Int = -1

    private fun getKeyMapping(): Map<Int, Triple<String, String, String>> {
        return mapOf(
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
            R.id.btn_mode_switch to Triple("123", "ABC", "ABC"),
            R.id.btn_layout_toggle to Triple("pin", "pin", "pin")
        )
    }

    override fun onCreateInputView(): View {
        val sharedPref = getSharedPreferences("KeyboardSettings", MODE_PRIVATE)
        val layout = sharedPref.getString("layout", "QWERTY")
        val layoutRes = if (layout == "NUMERIC") R.layout.numeric_keyboard_view else R.layout.keyboard_view
        return inflateLayout(layoutRes)
    }

    private fun inflateLayout(layoutResId: Int): View {
        val view = layoutInflater.inflate(layoutResId, null) as ViewGroup
        keyboardRoot = view
        currentLayoutResId = layoutResId
        setupKeyboard(view)
        updateKeyboard()
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        
        val sharedPref = getSharedPreferences("KeyboardSettings", MODE_PRIVATE)
        val preferredLayout = sharedPref.getString("layout", "QWERTY")

        var shouldBeNumeric = preferredLayout == "NUMERIC"
        info?.let {
            val inputType = it.inputType
            if (when (inputType and EditorInfo.TYPE_MASK_CLASS) {
                EditorInfo.TYPE_CLASS_NUMBER,
                EditorInfo.TYPE_CLASS_PHONE -> true
                else -> false
            }) {
                shouldBeNumeric = true
            }
        }

        isForcedNumeric = shouldBeNumeric
        val targetLayout = if (isForcedNumeric) R.layout.numeric_keyboard_view else R.layout.keyboard_view
        
        if (currentLayoutResId != targetLayout) {
            setInputView(inflateLayout(targetLayout))
        } else {
            isNumeric = isForcedNumeric
            isSymbols = false
            isCaps = false
            updateKeyboard()
        }
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
        val id = button.id

        when {
            text == "DEL" -> ic.deleteSurroundingText(1, 0)
            text == "SPACE" -> ic.commitText(" ", 1)
            text == "ENTER" -> ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            text == "ABC" -> {
                val sharedPref = getSharedPreferences("KeyboardSettings", MODE_PRIVATE)
                sharedPref.edit().putString("layout", "QWERTY").apply()
                isForcedNumeric = false
                setInputView(inflateLayout(R.layout.keyboard_view))
            }
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
            id == R.id.btn_layout_toggle -> {
                val sharedPref = getSharedPreferences("KeyboardSettings", MODE_PRIVATE)
                sharedPref.edit().putString("layout", "NUMERIC").apply()
                setInputView(inflateLayout(R.layout.numeric_keyboard_view))
            }
            id == R.id.btn_randomize -> {
                randomizeNumericKeyboard()
            }
            else -> {
                val code = if (isNumeric || isSymbols || isForcedNumeric) text else {
                    if (isCaps) text.uppercase() else text.lowercase()
                }
                ic.commitText(code, 1)
            }
        }
    }

    private fun randomizeNumericKeyboard() {
        val root = keyboardRoot ?: return
        val digits = (0..9).map { it.toString() }.shuffled()
        updateNumericButtons(root, digits)
    }

    private fun updateNumericButtons(viewGroup: ViewGroup, digits: List<String>) {
        var currentDigitIndex = 0
        fun traverse(view: View) {
            if (view is Button) {
                val text = view.text.toString()
                if (text.length == 1 && text[0].isDigit() && currentDigitIndex < digits.size) {
                    view.text = digits[currentDigitIndex]
                    currentDigitIndex++
                }
            } else if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    traverse(view.getChildAt(i))
                }
            }
        }
        traverse(viewGroup)
    }

    private fun updateKeyboard() {
        if (currentLayoutResId == R.layout.keyboard_view) {
            keyboardRoot?.let { updateButtonText(it) }
        }
    }

    private fun updateButtonText(viewGroup: ViewGroup) {
        val mapping = getKeyMapping()
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is Button) {
                val keyMap = mapping[child.id]
                if (keyMap != null) {
                    val baseText = when {
                        isSymbols -> keyMap.third
                        isNumeric -> keyMap.second
                        else -> keyMap.first
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
