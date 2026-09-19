package com.example.qbot

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("KeyboardSettings", MODE_PRIVATE)
        val currentLayout = sharedPref.getString("layout", "QWERTY")

        val rgLayout = findViewById<RadioGroup>(R.id.rg_layout)
        if (currentLayout == "NUMERIC") {
            findViewById<RadioButton>(R.id.rb_numeric).isChecked = true
        }

        rgLayout.setOnCheckedChangeListener { _, checkedId ->
            val layout = if (checkedId == R.id.rb_numeric) "NUMERIC" else "QWERTY"
            sharedPref.edit {
                putString("layout", layout)
            }
        }

        findViewById<MaterialButton>(R.id.btn_enable_kb).setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btn_select_kb).setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }
    }
}
