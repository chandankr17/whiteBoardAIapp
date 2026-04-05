package com.example.whiteboardai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        findViewById<Button>(R.id.btnNewBoard).setOnClickListener {
            val input = EditText(this)
            input.hint = "Enter board name..."
            input.setPadding(32, 16, 32, 16)
            AlertDialog.Builder(this)
                .setTitle("New Board")
                .setMessage("Give your board a name")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Create") { _, _ ->
                    val name = input.text.toString().trim()
                    val intent = Intent(this, DrawingActivity::class.java)
                    intent.putExtra("BOARD_TITLE", if (name.isNotEmpty()) name else "Untitled Board")
                    startActivity(intent)
                }
                .show()
        }

        findViewById<Button>(R.id.btnSavedBoards).setOnClickListener {
            // coming soon
        }
    }
}