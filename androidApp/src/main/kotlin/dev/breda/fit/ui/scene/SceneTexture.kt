package dev.breda.fit.ui.scene

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.*
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.view.Surface
import android.view.TextureView
import android.view.View
import dev.breda.fit.Exercise
import kotlin.math.cos

internal data class SceneInput(val exercise:Exercise,val animate:Boolean,val phase:Float,val orbit:Float)

/** One render dispatcher; each visible scene owns and releases its EGL context/surface. */
private object SceneGpu {
    val handler=Handler(HandlerThread("BredaFit3D").apply{start()}.looper)
    val display by lazy {
        EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY).also { display ->
            check(display!=EGL14.EGL_NO_DISPLAY && EGL14.eglInitialize(display,IntArray(2),0,IntArray(2),0)){"GPU indisponível"}
        }
    }
}

internal class SceneTexture(context:Context,private val ready:()->Unit,private val failed:()->Unit):TextureView(context),TextureView.SurfaceTextureListener {
    private var input:SceneInput?=null
    private var controller:SceneController?=null
    private var visible=true
    init {isOpaque=true;surfaceTextureListener=this;importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO}
    fun update(value:SceneInput){if(input==value)return;input=value;controller?.update(value)}
    override fun onSurfaceTextureAvailable(texture:SurfaceTexture,width:Int,height:Int){val value=input?:return;controller=SceneController(context.applicationContext.assets,texture,value,width,height,{post{ready()}},{post{failed()}}).also{it.start();it.visibility(visible)}}
    override fun onSurfaceTextureSizeChanged(texture:SurfaceTexture,width:Int,height:Int){controller?.resize(width,height)}
    override fun onSurfaceTextureDestroyed(texture:SurfaceTexture):Boolean {val c=controller;controller=null;if(c!=null){c.close(true);return false};return true}
    override fun onSurfaceTextureUpdated(texture:SurfaceTexture){}
    override fun onWindowVisibilityChanged(visibility:Int){super.onWindowVisibilityChanged(visibility);visible=visibility==View.VISIBLE;controller?.visibility(visible)}
    fun release(){controller?.close(false);controller=null}
}

private class SceneController(
    private val assets:android.content.res.AssetManager,private val texture:SurfaceTexture,private var input:SceneInput,private var width:Int,private var height:Int,
    private val ready:()->Unit,private val failed:()->Unit
) {
    @Volatile private var closed=false
    private var active=true;private var initialized=false;private var announced=false
    private var context:EGLContext=EGL14.EGL_NO_CONTEXT;private var surface:EGLSurface=EGL14.EGL_NO_SURFACE
    private var nativeSurface:Surface?=null;private val scene=ExerciseScene(assets);private var origin=SystemClock.uptimeMillis()
    private val handler get()=SceneGpu.handler
    private val frame=object:Runnable {override fun run(){
        if(closed || !initialized || !active)return
        try {
            check(EGL14.eglMakeCurrent(SceneGpu.display,surface,surface,context)){"Conexão gráfica perdida"}
            val elapsed=(SystemClock.uptimeMillis()-origin)%3600L
            val phase=if(input.animate)((1.0-cos(elapsed/3600.0*2*Math.PI))*.5).toFloat() else input.phase
            scene.draw(input.exercise,phase,input.orbit,width,height)
            check(EGL14.eglSwapBuffers(SceneGpu.display,surface)){"Falha ao apresentar a cena"}
            if(!announced){announced=true;ready()}
            if(input.animate && !closed && active)handler.postDelayed(this,33)
        } catch(_:Exception){failed();close(false)}
    }}
    fun start(){handler.post{
        if(closed)return@post
        try {
            val configs=arrayOfNulls<EGLConfig>(1);val count=IntArray(1)
            val attributes=intArrayOf(EGL14.EGL_RENDERABLE_TYPE,EGL14.EGL_OPENGL_ES2_BIT,EGL14.EGL_SURFACE_TYPE,EGL14.EGL_WINDOW_BIT,EGL14.EGL_RED_SIZE,8,EGL14.EGL_GREEN_SIZE,8,EGL14.EGL_BLUE_SIZE,8,EGL14.EGL_ALPHA_SIZE,8,EGL14.EGL_DEPTH_SIZE,16,EGL14.EGL_NONE)
            check(EGL14.eglChooseConfig(SceneGpu.display,attributes,0,configs,0,1,count,0) && count[0]>0)
            context=EGL14.eglCreateContext(SceneGpu.display,configs[0],EGL14.EGL_NO_CONTEXT,intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION,2,EGL14.EGL_NONE),0)
            check(context!=EGL14.EGL_NO_CONTEXT)
            nativeSurface=Surface(texture)
            surface=EGL14.eglCreateWindowSurface(SceneGpu.display,configs[0],nativeSurface,intArrayOf(EGL14.EGL_NONE),0)
            check(surface!=EGL14.EGL_NO_SURFACE && EGL14.eglMakeCurrent(SceneGpu.display,surface,surface,context))
            scene.initialize();initialized=true;handler.post(frame)
        } catch(_:Exception){failed();close(false)}
    }}
    fun update(value:SceneInput){handler.post{if(!closed){if(!input.animate && value.animate)origin=SystemClock.uptimeMillis();input=value;request()}}}
    fun resize(w:Int,h:Int){handler.post{width=w.coerceAtLeast(1);height=h.coerceAtLeast(1);request()}}
    fun visibility(visible:Boolean){handler.post{active=visible;request()}}
    private fun request(){handler.removeCallbacks(frame);if(initialized && active && !closed)handler.post(frame)}
    fun close(releaseTexture:Boolean){closed=true;handler.post{
        handler.removeCallbacks(frame)
        if(context!=EGL14.EGL_NO_CONTEXT){
            if(surface!=EGL14.EGL_NO_SURFACE){EGL14.eglMakeCurrent(SceneGpu.display,surface,surface,context);scene.dispose()}
            EGL14.eglMakeCurrent(SceneGpu.display,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT)
            if(surface!=EGL14.EGL_NO_SURFACE)EGL14.eglDestroySurface(SceneGpu.display,surface)
            EGL14.eglDestroyContext(SceneGpu.display,context);context=EGL14.EGL_NO_CONTEXT;surface=EGL14.EGL_NO_SURFACE
        }
        nativeSurface?.release();nativeSurface=null
        if(releaseTexture)texture.release()
    }}
}
