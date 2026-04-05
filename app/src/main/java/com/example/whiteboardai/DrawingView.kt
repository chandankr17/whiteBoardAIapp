package com.example.whiteboardai

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

enum class DrawingTool { PEN, ERASER, LINE, RECT, CIRCLE, TEXT }

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var currentTool = DrawingTool.PEN
    private var currentColor = Color.BLACK

    private var penSize = 8f
    private var eraserSize = 30f

    private var paint = Paint()
    private var path = Path()

    private var canvasBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null

    private var startX = 0f
    private var startY = 0f
    private var currentX = 0f
    private var currentY = 0f

    private val undoStack = ArrayDeque<Bitmap>()
    private val redoStack = ArrayDeque<Bitmap>()

    var onTextRequested: ((x: Float, y: Float) -> Unit)? = null

    init {
        paint.apply {
            color = currentColor
            style = Paint.Style.STROKE
            strokeWidth = penSize
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (canvasBitmap == null) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawCanvas = Canvas(canvasBitmap!!)
            drawCanvas?.drawColor(Color.WHITE)
        } else {
            // preserve existing drawing when size changes
            val oldBitmap = canvasBitmap!!
            val newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val newCanvas = Canvas(newBitmap)
            newCanvas.drawColor(Color.WHITE)
            newCanvas.drawBitmap(oldBitmap, 0f, 0f, null)
            canvasBitmap = newBitmap
            drawCanvas = newCanvas
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, null)
        when (currentTool) {
            DrawingTool.PEN, DrawingTool.ERASER -> canvas.drawPath(path, paint)
            DrawingTool.LINE -> canvas.drawLine(startX, startY, currentX, currentY, paint)
            DrawingTool.RECT -> canvas.drawRect(
                minOf(startX, currentX), minOf(startY, currentY),
                maxOf(startX, currentX), maxOf(startY, currentY), paint
            )
            DrawingTool.CIRCLE -> {
                val radius = Math.hypot(
                    (currentX - startX).toDouble(),
                    (currentY - startY).toDouble()
                ).toFloat()
                canvas.drawCircle(startX, startY, radius, paint)
            }
            else -> {}
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (currentTool == DrawingTool.TEXT) {
                    onTextRequested?.invoke(x, y)
                    return true
                }
                saveToUndoStack()
                startX = x; startY = y
                currentX = x; currentY = y
                if (currentTool == DrawingTool.PEN || currentTool == DrawingTool.ERASER) {
                    path.moveTo(x, y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                currentX = x; currentY = y
                if (currentTool == DrawingTool.PEN || currentTool == DrawingTool.ERASER) {
                    path.lineTo(x, y)
                }
            }
            MotionEvent.ACTION_UP -> {
                currentX = x; currentY = y
                when (currentTool) {
                    DrawingTool.PEN, DrawingTool.ERASER -> {
                        drawCanvas?.drawPath(path, paint)
                        path.reset()
                    }
                    DrawingTool.LINE -> drawCanvas?.drawLine(startX, startY, x, y, paint)
                    DrawingTool.RECT -> drawCanvas?.drawRect(
                        minOf(startX, x), minOf(startY, y),
                        maxOf(startX, x), maxOf(startY, y), paint
                    )
                    DrawingTool.CIRCLE -> {
                        val radius = Math.hypot(
                            (x - startX).toDouble(),
                            (y - startY).toDouble()
                        ).toFloat()
                        drawCanvas?.drawCircle(startX, startY, radius, paint)
                    }
                    else -> {}
                }
            }
        }
        invalidate()
        return true
    }

    private fun saveToUndoStack() {
        canvasBitmap?.let {
            undoStack.addLast(it.copy(it.config ?: Bitmap.Config.ARGB_8888, true))
            if (undoStack.size > 20) undoStack.removeFirst()
            redoStack.clear()
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            canvasBitmap?.let {
                redoStack.addLast(it.copy(it.config ?: Bitmap.Config.ARGB_8888, true))
            }
            val previous = undoStack.removeLast()
            drawCanvas?.drawBitmap(previous, 0f, 0f, null)
            invalidate()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            canvasBitmap?.let {
                undoStack.addLast(it.copy(it.config ?: Bitmap.Config.ARGB_8888, true))
            }
            val next = redoStack.removeLast()
            drawCanvas?.drawBitmap(next, 0f, 0f, null)
            invalidate()
        }
    }

    fun clearCanvas() {
        saveToUndoStack()
        paint.xfermode = null
        drawCanvas?.drawColor(Color.WHITE)
        path.reset()
        invalidate()
    }

    fun setTool(tool: DrawingTool) {
        currentTool = tool
        paint.xfermode = null
        if (tool == DrawingTool.ERASER) {
            paint.color = Color.WHITE
            paint.strokeWidth = eraserSize
            paint.style = Paint.Style.STROKE
        } else {
            paint.color = currentColor
            paint.strokeWidth = penSize
            paint.style = Paint.Style.STROKE
        }
    }

    fun setColor(color: Int) {
        currentColor = color
        if (currentTool != DrawingTool.ERASER) paint.color = color
    }

    fun setPenSize(size: Float) {
        penSize = size
        if (currentTool != DrawingTool.ERASER) paint.strokeWidth = size
    }

    fun setEraserSize(size: Float) {
        eraserSize = size
        if (currentTool == DrawingTool.ERASER) paint.strokeWidth = size
    }

    fun drawText(text: String, x: Float, y: Float) {
        saveToUndoStack()
        val textPaint = Paint().apply {
            color = currentColor
            textSize = penSize * 5f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            xfermode = null
        }
        drawCanvas?.drawText(text, x, y, textPaint)
        invalidate()
    }

    fun getBitmap(): Bitmap = canvasBitmap!!
}