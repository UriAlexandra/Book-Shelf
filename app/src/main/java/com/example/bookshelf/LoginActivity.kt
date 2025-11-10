// package név: állítsd a sajátodhoz, ha más
package com.example.bookshelf

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+ edge-to-edge élmény bekapcsolása
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Rendszersávok (status/nav bar) miatt a fő konténer paddingjának beállítása
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Nézetek lekérése az XML-ből
        val usernameInput = findViewById<TextInputEditText>(R.id.input_username)
        val passwordInput = findViewById<TextInputEditText>(R.id.input_password)
        val loginButton = findViewById<MaterialButton>(R.id.login_button)
        val themeSwitch = findViewById<SwitchMaterial>(R.id.theme_switch)

        // Bejelentkezés gomb logika (demo jellegű, fix “admin”/“jelszo123” ellenőrzés)
        loginButton.setOnClickListener {
            val username = usernameInput.text?.toString()?.trim().orEmpty()
            val password = passwordInput.text?.toString()?.trim().orEmpty()

            // Üres mezők kezelése
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.bs_credentials_empty),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Dummy ellenőrzés: NE használd élesben! (csak példa)
            if (username != getString(R.string.bs_admin_username) ||
                password != getString(R.string.bs_admin_password)
            ) {
                Toast.makeText(
                    this,
                    getString(R.string.bs_credentials_wrong),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Sikeres belépés → főképernyő
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // (Opcionális) Login képernyő befejezése, hogy Back-re ne ide ugorjon vissza
            // finish()
        }

        // Téma váltó kapcsoló (Dark/Light)
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // isChecked = világos téma; kikapcsolva = sötét téma
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}
