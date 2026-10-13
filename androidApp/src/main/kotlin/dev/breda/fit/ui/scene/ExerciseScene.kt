package dev.breda.fit.ui.scene

import android.opengl.GLES20
import android.opengl.Matrix
import androidx.compose.ui.geometry.Offset
import dev.breda.fit.Exercise
import dev.breda.fit.Movement
import dev.breda.fit.ui.pose
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

internal data class Vec(val x:Float,val y:Float,val z:Float) {
    operator fun plus(v:Vec)=Vec(x+v.x,y+v.y,z+v.z)
    operator fun minus(v:Vec)=Vec(x-v.x,y-v.y,z-v.z)
    operator fun times(s:Float)=Vec(x*s,y*s,z*s)
    fun length()=sqrt(x*x+y*y+z*z)
}
internal data class Material(val r:Float,val g:Float,val b:Float,val gloss:Float=.10f)

/** Original procedural meshes. No network model downloads or medical simulation. */
internal class ExerciseScene {
    private lateinit var sphere:FloatBuffer
    private lateinit var cap:FloatBuffer
    private var sphereCount=0;private var capCount=0;private var program=0
    private val model=FloatArray(16);private val view=FloatArray(16);private val projection=FloatArray(16)
    private val vp=FloatArray(16);private val mvp=FloatArray(16);private val inverse=FloatArray(16);private val normal=FloatArray(16)
    private var aPosition=0;private var aNormal=0;private var uModel=0;private var uMvp=0;private var uNormal=0;private var uColor=0;private var uEye=0
    private val skin=Material(.80f,.49f,.34f,.16f)
    private val shirt=Material(.57f,.82f,.24f,.04f)
    private val trousers=Material(.16f,.25f,.31f,.04f)
    private val shoe=Material(.84f,.89f,.83f,.15f)
    private val hair=Material(.055f,.075f,.070f,.08f)
    private val accent=Material(.62f,.92f,.28f,.12f)
    fun initialize() {
        program=GLES20.glCreateProgram()
        val vertex=shader(GLES20.GL_VERTEX_SHADER,"""
            uniform mat4 uMvp; uniform mat4 uModel; uniform mat4 uNormal;
            attribute vec3 aPosition; attribute vec3 aNormal;
            varying vec3 vNormal; varying vec3 vPosition;
            void main(){vPosition=(uModel*vec4(aPosition,1.0)).xyz;vNormal=(uNormal*vec4(aNormal,0.0)).xyz;gl_Position=uMvp*vec4(aPosition,1.0);}
        """)
        val fragment=shader(GLES20.GL_FRAGMENT_SHADER,"""
            precision mediump float;
            uniform vec4 uColor; uniform vec3 uEye;
            varying vec3 vNormal; varying vec3 vPosition;
            void main(){
                vec3 n=normalize(vNormal);vec3 eye=normalize(uEye-vPosition);
                vec3 key=normalize(vec3(-0.7,1.2,1.0));vec3 fill=normalize(vec3(1.0,0.4,-0.6));
                float diffuse=max(dot(n,key),0.0);float rim=pow(1.0-max(dot(n,eye),0.0),3.0);
                float spec=pow(max(dot(n,normalize(key+eye)),0.0),28.0)*uColor.a;
                vec3 light=vec3(0.31+0.60*diffuse+0.18*max(dot(n,fill),0.0));
                vec3 rgb=uColor.rgb*light+vec3(spec)+vec3(0.10,0.16,0.18)*rim;
                gl_FragColor=vec4(pow(max(rgb,vec3(0.0)),vec3(0.85)),1.0);
            }
        """)
        GLES20.glAttachShader(program,vertex);GLES20.glAttachShader(program,fragment);GLES20.glLinkProgram(program)
        val linked=IntArray(1);GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,linked,0)
        check(linked[0]!=0){GLES20.glGetProgramInfoLog(program)}
        GLES20.glDeleteShader(vertex);GLES20.glDeleteShader(fragment)
        aPosition=GLES20.glGetAttribLocation(program,"aPosition");aNormal=GLES20.glGetAttribLocation(program,"aNormal")
        uModel=GLES20.glGetUniformLocation(program,"uModel");uMvp=GLES20.glGetUniformLocation(program,"uMvp");uNormal=GLES20.glGetUniformLocation(program,"uNormal")
        uColor=GLES20.glGetUniformLocation(program,"uColor");uEye=GLES20.glGetUniformLocation(program,"uEye")
        val full=mesh(-PI/2,PI/2);sphere=full.first;sphereCount=full.second
        val top=mesh(0.0,PI/2);cap=top.first;capCount=top.second
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }
    private fun shader(type:Int,source:String):Int {
        val s=GLES20.glCreateShader(type);GLES20.glShaderSource(s,source);GLES20.glCompileShader(s)
        val ok=IntArray(1);GLES20.glGetShaderiv(s,GLES20.GL_COMPILE_STATUS,ok,0);check(ok[0]!=0){GLES20.glGetShaderInfoLog(s)};return s
    }
    private fun mesh(start:Double,end:Double):Pair<FloatBuffer,Int> {
        val values=ArrayList<Float>();val rings=18;val segments=32
        fun point(lat:Double,lon:Double){val x=(cos(lat)*cos(lon)).toFloat();val y=sin(lat).toFloat();val z=(cos(lat)*sin(lon)).toFloat();values.addAll(listOf(x,y,z,x,y,z))}
        for(r in 0 until rings)for(s in 0 until segments){val a=start+(end-start)*r/rings;val b=start+(end-start)*(r+1)/rings;val c=2*PI*s/segments;val d=2*PI*(s+1)/segments
            point(a,c);point(b,c);point(b,d);point(a,c);point(b,d);point(a,d)
        }
        return ByteBuffer.allocateDirect(values.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply{put(values.toFloatArray());position(0)} to values.size/6
    }
    private fun ellipsoid(center:Vec,size:Vec,material:Material,top:Boolean=false,angle:Float=0f,axis:Vec=Vec(0f,1f,0f)) {
        Matrix.setIdentityM(model,0);Matrix.translateM(model,0,center.x,center.y,center.z)
        if(angle!=0f)Matrix.rotateM(model,0,angle,axis.x,axis.y,axis.z)
        Matrix.scaleM(model,0,size.x.coerceAtLeast(.001f),size.y.coerceAtLeast(.001f),size.z.coerceAtLeast(.001f))
        Matrix.multiplyMM(mvp,0,vp,0,model,0);Matrix.invertM(inverse,0,model,0);Matrix.transposeM(normal,0,inverse,0)
        GLES20.glUniformMatrix4fv(uModel,1,false,model,0);GLES20.glUniformMatrix4fv(uMvp,1,false,mvp,0);GLES20.glUniformMatrix4fv(uNormal,1,false,normal,0)
        GLES20.glUniform4f(uColor,material.r,material.g,material.b,material.gloss)
        val buffer=if(top)cap else sphere;buffer.position(0);GLES20.glVertexAttribPointer(aPosition,3,GLES20.GL_FLOAT,false,24,buffer)
        buffer.position(3);GLES20.glVertexAttribPointer(aNormal,3,GLES20.GL_FLOAT,false,24,buffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,if(top)capCount else sphereCount)
    }
    private fun bone(a:Vec,b:Vec,radius:Float,material:Material,depth:Float=radius) {
        val delta=b-a;val length=delta.length().coerceAtLeast(.001f)
        val angle=acos((delta.y/length).coerceIn(-1f,1f))*180f/PI.toFloat()
        val axis=Vec(delta.z,0f,-delta.x).let{if(it.length()<.0001f)Vec(1f,0f,0f)else it}
        ellipsoid((a+b)*.5f,Vec(radius,length*.5f+radius*.6f,depth),material,angle=angle,axis=axis)
    }
    fun draw(exercise:Exercise,phase:Float,orbit:Float,width:Int,height:Int) {
        GLES20.glViewport(0,0,width,height);GLES20.glClearColor(.035f,.075f,.09f,1f);GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program);GLES20.glEnableVertexAttribArray(aPosition);GLES20.glEnableVertexAttribArray(aNormal)
        val pose=pose(exercise.movement,phase)
        val theta=((if(pose.profile)63f else 22f)+orbit)*PI.toFloat()/180f
        val eye=Vec(sin(theta)*4.5f,1.72f,cos(theta)*4.5f)
        Matrix.setLookAtM(view,0,eye.x,eye.y,eye.z,0f,1.12f,0f,0f,1f,0f)
        Matrix.perspectiveM(projection,0,35f,width.toFloat()/height.coerceAtLeast(1),.1f,15f);Matrix.multiplyMM(vp,0,projection,0,view,0)
        GLES20.glUniform3f(uEye,eye.x,eye.y,eye.z)
        fun coordinate(p:Offset,side:Float=0f)=if(pose.profile)Vec(side,(245f-p.y)/100f,(p.x-119f)/100f) else Vec((p.x-119f)/100f,(245f-p.y)/100f,side)
        val hip=coordinate(pose.hip);val shoulder=coordinate(pose.shoulder);val head=coordinate(pose.head)
        // A low studio plinth and layered contact shadow are geometry, not a flat person image.
        ellipsoid(Vec(0f,-.08f,0f),Vec(.98f,.055f,.82f),Material(.12f,.23f,.25f,.05f))
        ellipsoid(Vec(0f,-.065f,0f),Vec(.94f,.04f,.78f),Material(.08f,.14f,.16f,.02f))
        ellipsoid(Vec(hip.x,-.014f,hip.z),Vec(.40f,.008f,.36f),Material(.025f,.05f,.055f,0f))
        if(exercise.seated || exercise.movement==Movement.SIT_STAND) chair(if(exercise.seated).65f else .58f)
        if(exercise.movement==Movement.BACK_LEG) chair(.82f,.75f)
        if(exercise.movement==Movement.WALL_PUSH){bone(Vec(-.43f,0f,.98f),Vec(-.43f,2.04f,.98f),.027f,Material(.35f,.49f,.48f));bone(Vec(.43f,0f,.98f),Vec(.43f,2.04f,.98f),.027f,Material(.35f,.49f,.48f));ellipsoid(Vec(0f,1.02f,1.03f),Vec(.46f,1.04f,.055f),Material(.16f,.27f,.29f))}
        for(i in 0..1){val side=if(i==0)-.12f else .12f;val knee=coordinate(pose.knees[i],if(pose.profile)side else .015f);val foot=coordinate(pose.feet[i],if(pose.profile)side else .02f)
            val root=hip+Vec(side,0f,0f);bone(root,knee,.10f,trousers,.10f);bone(knee,foot,.075f,trousers);ellipsoid(knee,Vec(.08f,.08f,.08f),trousers)
            val tilt=if(exercise.movement==Movement.ANKLE && i==0)-15f+phase*30f else if(exercise.movement==Movement.CALF)phase*12f else 0f
            ellipsoid(foot+Vec(0f,-.032f,.065f),Vec(.086f,.020f,.16f),Material(.045f,.09f,.10f),angle=tilt,axis=Vec(1f,0f,0f))
            ellipsoid(foot+Vec(0f,.008f,.065f),Vec(.083f,.057f,.15f),shoe,angle=tilt,axis=Vec(1f,0f,0f))
            ellipsoid(foot+Vec(0f,.04f,.10f),Vec(.044f,.023f,.065f),accent)
        }
        ellipsoid(hip+Vec(0f,-.005f,0f),Vec(.22f,.14f,.15f),trousers)
        bone(hip,shoulder,.245f,shirt,.16f)
        for(i in 0..1){val side=if(i==0)-.22f else .22f
            val start=shoulder+Vec(side,-.015f,0f);val elbow=coordinate(pose.elbows[i],if(pose.profile)side else 0f);val hand=coordinate(pose.hands[i],if(pose.profile)side else .025f)
            bone(start,elbow,.062f,skin);bone(start,start+(elbow-start)*.40f,.09f,shirt)
            ellipsoid(elbow,Vec(.062f,.064f,.062f),skin);bone(elbow,hand,.048f,skin);ellipsoid(hand,Vec(.047f,.070f,.036f),skin)
        }
        bone(shoulder,head+Vec(0f,-.11f,0f),.061f,skin)
        ellipsoid(head,Vec(.15f,.205f,.145f),skin)
        ellipsoid(head+Vec(0f,.04f,-.05f),Vec(.155f,.19f,.137f),hair,true)
        ellipsoid(head+Vec(-.01f,.132f,.055f),Vec(.145f,.055f,.103f),hair)
        for(side in listOf(-1f,1f)){
            ellipsoid(head+Vec(side*.148f,.00f,.00f),Vec(.027f,.043f,.027f),skin)
            ellipsoid(head+Vec(side*.056f,.025f,.134f),Vec(.027f,.030f,.015f),Material(.94f,.96f,.91f,.10f))
            ellipsoid(head+Vec(side*.056f,.024f,.148f),Vec(.012f,.017f,.007f),hair)
            bone(head+Vec(side*.075f,.069f,.127f),head+Vec(side*.040f,.072f,.140f),.007f,hair)
        }
        ellipsoid(head+Vec(0f,-.025f,.150f),Vec(.027f,.035f,.035f),skin)
        bone(head+Vec(-.033f,-.091f,.123f),head+Vec(.033f,-.091f,.123f),.008f,Material(.43f,.21f,.17f,.02f))
        // Small original breda_dev outfit mark.
        ellipsoid(shoulder+Vec(-.10f,-.16f,.160f),Vec(.025f,.028f,.007f),Material(.91f,.99f,.77f,.02f))
        GLES20.glDisableVertexAttribArray(aPosition);GLES20.glDisableVertexAttribArray(aNormal)
    }
    private fun chair(seat:Float,z:Float=-.38f){
        val frame=Material(.32f,.42f,.43f,.22f);val cushion=Material(.18f,.30f,.30f,.04f)
        for(x in listOf(-.30f,.30f))for(depth in listOf(z-.24f,z+.26f))bone(Vec(x,0f,depth),Vec(x,seat,depth),.026f,frame)
        ellipsoid(Vec(0f,seat,z),Vec(.37f,.045f,.34f),cushion)
        for(x in listOf(-.30f,.30f))bone(Vec(x,seat,z-.25f),Vec(x,seat+.58f,z-.25f),.023f,frame)
        ellipsoid(Vec(0f,seat+.37f,z-.25f),Vec(.34f,.22f,.042f),cushion)
    }
    fun dispose(){if(program!=0)GLES20.glDeleteProgram(program);program=0}
}
