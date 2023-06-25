package com.example.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_MOVE
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import java.nio.file.Path as Path1

class DrawingView( context:Context,attrs:AttributeSet):View(context,attrs) {
    private var mDrawpath: CustomPath?=null
    private var mCanvasBitMap : Bitmap?=null
    private var mDrawPaint: Paint?=null
    private var MCanvasPaint:Paint?=null
    private var brushsize: Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas:Canvas? =null
    private var mPaths = ArrayList<CustomPath>()
    private var mundoPaths = ArrayList<CustomPath>()



    init {
        setUPDrawing()
    }
    fun onclickundo(){
        if(mPaths.size>0){
            mundoPaths.add(mPaths.removeAt(mPaths.size-1))
        }
        invalidate()
    }
    fun onclickredo(){
        if(mundoPaths.size>0){
            mPaths.add(mundoPaths.removeAt(mundoPaths.size-1))
        }
        invalidate()
    }

    private fun setUPDrawing() {
        mDrawpath=CustomPath(color,brushsize)
        mDrawPaint=Paint()
        mDrawPaint!!.color=color
        mDrawPaint!!.style=Paint.Style.STROKE
        mDrawPaint!!.strokeJoin=Paint.Join.ROUND
        mDrawPaint!!.strokeCap=Paint.Cap.ROUND
        MCanvasPaint=Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitMap= Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitMap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitMap!!, 0f, 0f, MCanvasPaint)

        for(path in mPaths){
            mDrawPaint!!.strokeWidth =path!!.brushthickness
            mDrawPaint!!.color=path!!.color
            canvas?.drawPath(path,mDrawPaint!!)
        }
        if (!mDrawpath!!.isEmpty) {
            mDrawPaint!!.strokeWidth =mDrawpath!!.brushthickness
            mDrawPaint!!.color=mDrawpath!!.color
            canvas?.drawPath(mDrawpath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
     val touchx = event?.x
        val touchy = event?.y
        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                mDrawpath!!.color = color
                mDrawpath!!.brushthickness= brushsize
                mDrawpath!!.reset()
                mDrawpath!!.moveTo(touchx!!,touchy!!)
            }
            MotionEvent.ACTION_MOVE ->{
                mDrawpath!!.lineTo(touchx!!,touchy!!)
            }
            MotionEvent.ACTION_UP->{
                mPaths.add(mDrawpath!!)
                mDrawpath=CustomPath(color,brushsize)
            }
            else->{return false}

        }
        invalidate()


        return true
    }


    internal inner class CustomPath(var color:Int,var brushthickness:Float): Path() {

    }
    fun setsizebrush(newsize:Float){
        brushsize =TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newsize,resources.displayMetrics)
        mDrawPaint!!.strokeWidth=brushsize
    }



    fun setcolor(newColor: String){
        color= Color.parseColor(newColor)
        mDrawPaint!!.color=color
    }

}