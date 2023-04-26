package com.mellow.bat


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*
import kotlin.math.abs
import kotlin.math.max

class GameView(val ctx: Context, val att: AttributeSet): SurfaceView(ctx,att) {

    var ball = BitmapFactory.decodeResource(ctx.resources,R.drawable.game)
    var br1 = BitmapFactory.decodeResource(ctx.resources,R.drawable.br1)
    var br2 = BitmapFactory.decodeResource(ctx.resources,R.drawable.br2)
    var br3 = BitmapFactory.decodeResource(ctx.resources,R.drawable.br3)
    var bl1 = BitmapFactory.decodeResource(ctx.resources,R.drawable.bl1)
    var bl2 = BitmapFactory.decodeResource(ctx.resources,R.drawable.bl2)

    var music = ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("music",true)
    var sounds = ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("sounds",true)
    private var paintB: Paint = Paint(Paint.DITHER_FLAG)
    private var paintT = Paint().apply {
        color = Color.WHITE
        textSize = 130f
        style = Paint.Style.FILL
    }
    private var listener: EndListener? = null
    private val random = Random()
    private var millis = 0
    private lateinit var list1: MutableList<Bitmap>
    var player = MediaPlayer.create(ctx,R.raw.bg)
    var sound = MediaPlayer.create(ctx,R.raw.sound)

    init {
        player.setOnCompletionListener {
            it.start()
        }
        if(music) player.start()
         ball = Bitmap.createScaledBitmap(ball,ball.width/4,ball.height/4,true)
        br1 = Bitmap.createScaledBitmap(br1,br1.width/4,br1.height/4,true)
        br2 = Bitmap.createScaledBitmap(br2,br2.width/4,br2.height/4,true)
        br3 = Bitmap.createScaledBitmap(br3,br3.width/4,br3.height/4,true)
        bl1 = Bitmap.createScaledBitmap(bl1,bl1.width/4,bl1.height/4,true)
        bl2 = Bitmap.createScaledBitmap(bl2,bl2.width/4,bl2.height/4,true)
        list1 = mutableListOf(bl1,bl2,br1,br2,br3)
        holder.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {

            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                val canvas = holder.lockCanvas()
                if(canvas!=null) {
                    by = canvas.height-200
                    bx = (canvas.width/2f-ball.width/2f).toInt()
                    draw(canvas)
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                paused = true
                player.stop()
            }

        })
        val updateThread = Thread {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    if (!paused) {
                        update.run()
                        millis ++
                    }
                }
            }, 500, 16)
        }

        updateThread.start()
    }
    var code = -1f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event!!.action) {
            MotionEvent.ACTION_UP -> {
                g = -1
            }
            MotionEvent.ACTION_DOWN -> {
                code = event.x
                g = 1
            }
        }
        postInvalidate()
        return true
    }
    var g = 0
    var paused = false
    var list = mutableListOf<Model>()
    var delta = 8
    var bx = 0
    var health = 3
    var score = 0
    var by = 0
    var dy = 0

    val update = Runnable{
        var isEnd = false
        var sc = false
        if(paused) return@Runnable
        try {
            val canvas = holder.lockCanvas()
            if(code!=-1f) {
                if(by<=canvas.height-200f) {
                    by += dy
                    if(abs(code-bx)>=delta) {
                        if(code>bx) bx+=delta
                        else bx-=delta
                    }
                    if(g<=0) dy += 2
                    else dy = -10
                } else {
                    by = canvas.height-200
                    code = -1f
                }
            }
            if(bx<=-ball.width) bx = canvas.width
            if(bx>=ball.width+canvas.width) bx = 0
            var i = 0
            while(i<list.size) {
                Log.d("TAG","$i")
                list[i].y+=5
                if(abs(list[i].x-bx)<=ball.width*1.5 && abs(list[i].y-(by)) <=list1[list[i].cur].height) {
                    isEnd = true
                    list.removeAt(i)
                    break
                } else if(list[i].y>=canvas.height+list1[list[i].cur].height) {
                    score += 5
                    if(sounds) sound.start()
                    list.removeAt(i)
                } else i++
            }
            var r = 0f
            var l = 0f
            while(list.size<6) {
                var f = random.nextInt(5)
                list.add(Model(if(f<=1) 0f else (canvas.width-list1[f].width).toFloat(),-max((if(f<=1) l else r),1f*list1[f].height),f))
                if(f>1) r+= list1[f].height
                else l += list1[f].height
            }
            canvas.drawColor(ctx.getColor(R.color.ggg))
            canvas.drawText(score.toString(),canvas.width/2f-50f,canvas.height/6f,paintT)
            for(i in list) {
                canvas.drawBitmap(list1[i.cur],i.x,i.y,paintB)
            }
            canvas.drawBitmap(ball,bx.toFloat(),by.toFloat(),paintB)
            holder.unlockCanvasAndPost(canvas)
            if(isEnd) {
                Log.d("TAG","END")
                togglePause()
                if(listener!=null) listener!!.end()
            }
            if(sc) {
                listener?.score(health)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEndListener(list: EndListener) {
        this.listener = list
    }
    fun togglePause() {
        paused = !paused
    }
    companion object {
        interface EndListener {
            fun end();
            fun score(score: Int);
        }
        data class Model(var x: Float, var y: Float, var cur: Int)
    }
    val b = ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE).getInt("color",0)
}