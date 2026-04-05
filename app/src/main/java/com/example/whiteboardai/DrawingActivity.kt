package com.example.whiteboardai

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class DrawingActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var penSizeRow: LinearLayout
    private lateinit var eraserSizeRow: LinearLayout
    private lateinit var colorRow: LinearLayout
    private lateinit var btnPen: Button
    private lateinit var btnEraser: Button
    private lateinit var btnShapes: Button
    private lateinit var btnText: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)
        supportActionBar?.hide()

        drawingView = findViewById(R.id.drawingView)
        penSizeRow = findViewById(R.id.penSizeRow)
        eraserSizeRow = findViewById(R.id.eraserSizeRow)
        colorRow = findViewById(R.id.colorRow)
        btnPen = findViewById(R.id.btnPen)
        btnEraser = findViewById(R.id.btnEraser)
        btnShapes = findViewById(R.id.btnShapes)
        btnText = findViewById(R.id.btnText)

        // Show board name dialog if not passed via intent
        val boardTitle = intent.getStringExtra("BOARD_TITLE")
        if (boardTitle != null) {
            findViewById<TextView>(R.id.tvBoardTitle).text = boardTitle
        } else {
            askBoardName()
        }

        // Default tool
        selectTool("pen")

        btnPen.setOnClickListener {
            drawingView.setTool(DrawingTool.PEN)
            selectTool("pen")
        }

        btnEraser.setOnClickListener {
            drawingView.setTool(DrawingTool.ERASER)
            selectTool("eraser")
        }

        btnShapes.setOnClickListener {
            selectTool("shapes")
            showShapesDialog()
        }

        btnText.setOnClickListener {
            drawingView.setTool(DrawingTool.TEXT)
            selectTool("text")
            Toast.makeText(this, "Tap on canvas to add text", Toast.LENGTH_SHORT).show()
        }

        drawingView.onTextRequested = { x, y ->
            val input = EditText(this)
            input.hint = "Type something..."
            input.setPadding(32, 16, 32, 16)
            AlertDialog.Builder(this)
                .setTitle("Add Text")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val text = input.text.toString().trim()
                    if (text.isNotEmpty()) drawingView.drawText(text, x, y)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Colors
        findViewById<View>(R.id.colorBlack).setOnClickListener { drawingView.setColor(Color.BLACK) }
        findViewById<View>(R.id.colorRed).setOnClickListener { drawingView.setColor(Color.RED) }
        findViewById<View>(R.id.colorBlue).setOnClickListener { drawingView.setColor(Color.BLUE) }
        findViewById<View>(R.id.colorGreen).setOnClickListener { drawingView.setColor(Color.GREEN) }
        findViewById<View>(R.id.colorYellow).setOnClickListener { drawingView.setColor(Color.YELLOW) }
        findViewById<View>(R.id.colorOrange).setOnClickListener { drawingView.setColor(Color.parseColor("#FF6600")) }
        findViewById<View>(R.id.colorPurple).setOnClickListener { drawingView.setColor(Color.parseColor("#8800CC")) }

        // Pen size
        val tvPenSize = findViewById<TextView>(R.id.tvPenSize)
        findViewById<SeekBar>(R.id.seekBarPen).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val size = maxOf(progress, 2)
                drawingView.setPenSize(size.toFloat())
                tvPenSize.text = size.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Eraser size
        val tvEraserSize = findViewById<TextView>(R.id.tvEraserSize)
        findViewById<SeekBar>(R.id.seekBarEraser).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val size = maxOf(progress, 5)
                drawingView.setEraserSize(size.toFloat())
                tvEraserSize.text = size.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        findViewById<Button>(R.id.btnUndo).setOnClickListener { drawingView.undo() }
        findViewById<Button>(R.id.btnRedo).setOnClickListener { drawingView.redo() }
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear Board")
                .setMessage("Are you sure?")
                .setPositiveButton("Clear") { _, _ -> drawingView.clearCanvas() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun askBoardName() {
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
                findViewById<TextView>(R.id.tvBoardTitle).text =
                    if (name.isNotEmpty()) name else "Untitled Board"
            }
            .show()
    }

    private fun selectTool(tool: String) {
        val grey = Color.parseColor("#333333")
        btnPen.backgroundTintList = android.content.res.ColorStateList.valueOf(grey)
        btnEraser.backgroundTintList = android.content.res.ColorStateList.valueOf(grey)
        btnShapes.backgroundTintList = android.content.res.ColorStateList.valueOf(grey)
        btnText.backgroundTintList = android.content.res.ColorStateList.valueOf(grey)

        penSizeRow.visibility = View.GONE
        eraserSizeRow.visibility = View.GONE
        colorRow.visibility = View.GONE

        when (tool) {
            "pen" -> {
                btnPen.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4444FF"))
                penSizeRow.visibility = View.VISIBLE
                colorRow.visibility = View.VISIBLE
            }
            "eraser" -> {
                btnEraser.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#888888"))
                eraserSizeRow.visibility = View.VISIBLE
            }
            "shapes" -> {
                btnShapes.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4444FF"))
                colorRow.visibility = View.VISIBLE
            }
            "text" -> {
                btnText.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4444FF"))
                colorRow.visibility = View.VISIBLE
            }
        }
    }

    private fun showShapesDialog() {
        val shapes = arrayOf("Line", "Rectangle", "Circle")
        AlertDialog.Builder(this)
            .setTitle("Choose Shape")
            .setItems(shapes) { _, which ->
                when (which) {
                    0 -> drawingView.setTool(DrawingTool.LINE)
                    1 -> drawingView.setTool(DrawingTool.RECT)
                    2 -> drawingView.setTool(DrawingTool.CIRCLE)
                }
            }
            .show()
    }
}