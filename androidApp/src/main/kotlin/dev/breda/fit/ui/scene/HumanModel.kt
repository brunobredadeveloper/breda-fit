package dev.breda.fit.ui.scene

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import androidx.compose.ui.geometry.Offset
import dev.breda.fit.Movement
import dev.breda.fit.Exercise
import dev.breda.fit.ui.pose
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

/** Licensed anatomical meshes with four-weight GPU skinning; assets never leave the device. */
internal class HumanModel(private val assets:AssetManager) {
    private data class Mesh(val vertices:Int,val indices:Int,val count:Int,val material:Int,val texture:Int)
    private val meshes=ArrayList<Mesh>()
    private val rest=Array(16){Array(2){Vec(0f,0f,0f)}}
    private val palette=FloatArray(16*16)
    private var program=0
    private val locations=HashMap<String,Int>()
    private fun uniform(name:String)=locations.getOrPut(name){GLES20.glGetUniformLocation(program,name)}
    private fun attribute(name:String)=locations.getOrPut(name){GLES20.glGetAttribLocation(program,name)}
    fun initialize(){
        val data=ByteBuffer.wrap(assets.open("human/rig.bin").use{it.readBytes()}).order(ByteOrder.LITTLE_ENDIAN)
        for(b in rest)for(i in 0..1)b[i]=Vec(data.float,data.float,data.float)
        program=GLES20.glCreateProgram()
        val vs=compile(GLES20.GL_VERTEX_SHADER,VERTEX);val fs=compile(GLES20.GL_FRAGMENT_SHADER,FRAGMENT)
        GLES20.glAttachShader(program,vs);GLES20.glAttachShader(program,fs);GLES20.glLinkProgram(program)
        val ok=IntArray(1);GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,ok,0);check(ok[0]!=0){GLES20.glGetProgramInfoLog(program)}
        GLES20.glDeleteShader(vs);GLES20.glDeleteShader(fs)
        for((name,texture) in listOf("skin" to "skin","outfit" to null,"shoes" to "shoes","eyes" to "eyes","hair" to "hair","brows" to "brows")){
            val b=ByteBuffer.wrap(assets.open("human/$name.mesh").use{it.readBytes()}).order(ByteOrder.LITTLE_ENDIAN)
            check(b.int==0x34484642);val material=b.int;val n=b.int;val count=b.int
            val vertices=ByteBuffer.allocateDirect(n*68).order(ByteOrder.nativeOrder());val floats=vertices.asFloatBuffer();repeat(n*17){floats.put(b.float)};vertices.position(0)
            val indices=ByteBuffer.allocateDirect(count*2).order(ByteOrder.nativeOrder());val shorts=indices.asShortBuffer();repeat(count){shorts.put(b.short)};indices.position(0)
            val ids=IntArray(2);GLES20.glGenBuffers(2,ids,0)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,ids[0]);GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,n*68,vertices,GLES20.GL_STATIC_DRAW)
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,ids[1]);GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,count*2,indices,GLES20.GL_STATIC_DRAW)
            meshes.add(Mesh(ids[0],ids[1],count,material,texture?.let{loadTexture(it)}?:0))
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,0)
    }
    private fun loadTexture(name:String):Int {
        val options=BitmapFactory.Options().apply{inJustDecodeBounds=true}
        assets.open("human/$name.png").use{BitmapFactory.decodeStream(it,null,options)}
        options.inJustDecodeBounds=false;options.inSampleSize=1
        while(max(options.outWidth,options.outHeight)/options.inSampleSize>1024)options.inSampleSize*=2
        val bitmap=assets.open("human/$name.png").use{checkNotNull(BitmapFactory.decodeStream(it,null,options))}
        val id=IntArray(1);GLES20.glGenTextures(1,id,0);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,id[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);bitmap.recycle();return id[0]
    }
    private fun compile(type:Int,source:String):Int {
        val s=GLES20.glCreateShader(type);GLES20.glShaderSource(s,source);GLES20.glCompileShader(s);val ok=IntArray(1)
        GLES20.glGetShaderiv(s,GLES20.GL_COMPILE_STATUS,ok,0);check(ok[0]!=0){GLES20.glGetShaderInfoLog(s)};return s
    }
    private fun unit(v:Vec)=v*(1f/v.length().coerceAtLeast(.00001f))
    private fun dot(a:Vec,b:Vec)=a.x*b.x+a.y*b.y+a.z*b.z
    private fun cross(a:Vec,b:Vec)=Vec(a.y*b.z-a.z*b.y,a.z*b.x-a.x*b.z,a.x*b.y-a.y*b.x)
    /** Rigid rest-to-pose transform. Bone lengths remain anatomical, without axial stretching. */
    private fun transform(a:Vec,b:Vec,targetA:Vec,targetB:Vec):FloatArray {
        val from=unit(b-a);val to=unit(targetB-targetA);var axis=cross(from,to);val cosine=dot(from,to).coerceIn(-1f,1f)
        if(axis.length()<.00001f)axis=if(abs(from.x)<.8f)cross(from,Vec(1f,0f,0f)) else cross(from,Vec(0f,0f,1f))
        val m=FloatArray(16);Matrix.setIdentityM(m,0);Matrix.translateM(m,0,targetA.x,targetA.y,targetA.z)
        Matrix.rotateM(m,0,acos(cosine)*180f/PI.toFloat(),axis.x,axis.y,axis.z);Matrix.translateM(m,0,-a.x,-a.y,-a.z);return m
    }
    private fun point(m:FloatArray,v:Vec)=Vec(m[0]*v.x+m[4]*v.y+m[8]*v.z+m[12],m[1]*v.x+m[5]*v.y+m[9]*v.z+m[13],m[2]*v.x+m[6]*v.y+m[10]*v.z+m[14])
    private fun set(id:Int,m:FloatArray){m.copyInto(palette,id*16)}
    private fun ik(a:Vec,end:Vec,preference:Vec,upper:Float,lower:Float):Pair<Vec,Vec>{
        val delta=end-a;val length=delta.length().coerceIn(abs(upper-lower)+.001f,upper+lower-.001f);val direction=unit(delta)
        val along=(upper*upper-lower*lower+length*length)/(2f*length)
        val side=preference-a;var bend=side-direction*dot(side,direction)
        if(bend.length()<.0001f)bend=cross(direction,Vec(1f,0f,0f))
        return (a+direction*along+unit(bend)*sqrt((upper*upper-along*along).coerceAtLeast(0f))) to (a+direction*length)
    }
    fun draw(exercise:Exercise,phase:Float,vp:FloatArray,eye:Vec){
        val p=pose(exercise.movement,phase)
        fun coord(o:Offset,side:Float=0f)=if(p.profile)Vec(side,(245f-o.y)/100f,(o.x-119f)/100f)else Vec((o.x-119f)/100f,(245f-o.y)/100f,side)
        val restHip=(rest[10][0]+rest[13][0])*.5f
        val lift=if(exercise.seated || exercise.movement==Movement.SIT_STAND)0f else .18f
        val hip=coord(p.hip)+Vec(0f,.035f+lift,0f)
        val torso=transform(restHip,rest[2][1],hip,coord(p.shoulder)+Vec(0f,lift,0f))
        for(id in 0..3)set(id,torso)
        for(i in 0..1){
            val arm=4+i*3;val leg=10+i*3;val side=if(i==0).14f else -.14f
            val shoulder=point(torso,rest[arm][0]);val hand=coord(p.hands[1-i],if(p.profile)side else .07f)+Vec(0f,lift,0f)
            val elbows=coord(p.elbows[1-i],if(p.profile)side else .04f)+Vec(0f,lift,0f)
            val (elbow,wrist)=ik(shoulder,hand,elbows,(rest[arm][1]-rest[arm][0]).length(),(rest[arm+1][1]-rest[arm+1][0]).length())
            set(arm,transform(rest[arm][0],rest[arm][1],shoulder,elbow));val forearm=transform(rest[arm+1][0],rest[arm+1][1],elbow,wrist);set(arm+1,forearm)
            // Hands follow the forearm rotation; detailed fingers remain in a relaxed pose.
            set(arm+2,forearm)
            val root=point(torso,rest[leg][0]);val ankle=coord(p.feet[1-i],if(p.profile)side else .0f)+Vec(0f,.06f,0f)
            val kneePreference=coord(p.knees[1-i],if(p.profile)side else .12f)+Vec(0f,0f,.12f)
            val (knee,foot)=ik(root,ankle,kneePreference,(rest[leg][1]-rest[leg][0]).length(),(rest[leg+1][1]-rest[leg+1][0]).length())
            set(leg,transform(rest[leg][0],rest[leg][1],root,knee));set(leg+1,transform(rest[leg+1][0],rest[leg+1][1],knee,foot))
            set(leg+2,transform(rest[leg+2][0],rest[leg+2][1],foot,foot+(rest[leg+2][1]-rest[leg+2][0])))
        }
        GLES20.glUseProgram(program);GLES20.glUniformMatrix4fv(uniform("uVp"),1,false,vp,0);GLES20.glUniformMatrix4fv(uniform("uBones[0]"),16,false,palette,0)
        GLES20.glUniform3f(uniform("uEye"),eye.x,eye.y,eye.z);GLES20.glUniform1i(uniform("uTexture"),0)
        val attributes=listOf(Triple("aPosition",3,0),Triple("aNormal",3,12),Triple("aUv",2,24),Triple("aBones",4,32),Triple("aWeights",4,48),Triple("aZone",1,64))
        for(m in meshes){
            GLES20.glUniform1f(uniform("uMaterial"),m.material.toFloat());GLES20.glActiveTexture(GLES20.GL_TEXTURE0);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,m.texture)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,m.vertices);GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,m.indices)
            for((name,size,offset) in attributes){val loc=attribute(name);GLES20.glEnableVertexAttribArray(loc);GLES20.glVertexAttribPointer(loc,size,GLES20.GL_FLOAT,false,68,offset)}
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,m.count,GLES20.GL_UNSIGNED_SHORT,0)
        }
        for((name,_,_) in attributes)GLES20.glDisableVertexAttribArray(attribute(name))
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,0)
    }
    fun dispose(){for(m in meshes){GLES20.glDeleteBuffers(2,intArrayOf(m.vertices,m.indices),0);if(m.texture!=0)GLES20.glDeleteTextures(1,intArrayOf(m.texture),0)};meshes.clear();if(program!=0)GLES20.glDeleteProgram(program);program=0}
    private companion object {
        const val VERTEX="""
            uniform mat4 uVp; uniform mat4 uBones[16];
            attribute vec3 aPosition; attribute vec3 aNormal; attribute vec2 aUv;
            attribute vec4 aBones; attribute vec4 aWeights; attribute float aZone;
            varying vec3 vPosition; varying vec3 vNormal; varying vec2 vUv; varying float vZone;
            void main(){
                mat4 skin=uBones[int(aBones.x)]*aWeights.x+uBones[int(aBones.y)]*aWeights.y+uBones[int(aBones.z)]*aWeights.z+uBones[int(aBones.w)]*aWeights.w;
                vec4 world=skin*vec4(aPosition,1.0);vPosition=world.xyz;vNormal=(skin*vec4(aNormal,0.0)).xyz;
                vUv=vec2(aUv.x,1.0-aUv.y);vZone=aZone;gl_Position=uVp*world;
            }
        """
        const val FRAGMENT="""
            precision mediump float;
            uniform sampler2D uTexture; uniform float uMaterial; uniform vec3 uEye;
            varying vec3 vPosition; varying vec3 vNormal; varying vec2 vUv; varying float vZone;
            const float PI=3.14159265;
            vec3 lighting(vec3 base,vec3 n,vec3 view,vec3 light,float roughness,float intensity){
                vec3 h=normalize(view+light);float nl=max(dot(n,light),0.0);float nv=max(dot(n,view),0.001);
                float nh=max(dot(n,h),0.0);float vh=max(dot(view,h),0.0);float a=roughness*roughness;float a2=a*a;
                float d=a2/(PI*pow(nh*nh*(a2-1.0)+1.0,2.0)+0.0001);
                float k=pow(roughness+1.0,2.0)/8.0;float g=(nv/(nv*(1.0-k)+k))*(nl/(nl*(1.0-k)+k));
                vec3 f=vec3(0.035)+(vec3(1.0)-vec3(0.035))*pow(1.0-vh,5.0);
                return ((1.0-f)*base/PI+d*g*f/(4.0*nv*nl+0.001))*nl*intensity;
            }
            void main(){
                vec4 tex=texture2D(uTexture,vUv);vec3 base;float roughness=0.62;
                if(uMaterial>0.5 && uMaterial<1.5){
                    base=mix(vec3(0.035,0.075,0.10),vec3(0.28,0.62,0.065),smoothstep(0.2,0.8,vZone));roughness=0.85;
                    float weave=0.985+0.015*sin(vUv.x*2400.0)*sin(vUv.y*2400.0);base*=weave;
                }else{
                    if(uMaterial>2.5 && uMaterial<3.5 && tex.a<0.5)discard;
                    base=pow(tex.rgb,vec3(2.2));if(uMaterial>1.5 && uMaterial<2.5)roughness=0.2;
                    if(uMaterial>2.5 && uMaterial<3.5)roughness=0.72;
                }
                vec3 n=normalize(vNormal);if(!gl_FrontFacing)n=-n;vec3 view=normalize(uEye-vPosition);
                vec3 rgb=base*vec3(0.28,0.31,0.34);
                rgb+=lighting(base,n,view,normalize(vec3(-0.7,1.1,1.2)),roughness,3.2);
                rgb+=lighting(base,n,view,normalize(vec3(0.9,0.4,-0.8)),roughness,1.4);
                if(uMaterial<0.5)rgb+=base*0.09*max(dot(-n,normalize(vec3(-0.7,1.1,1.2))),0.0);
                rgb=rgb/(rgb+vec3(0.65));gl_FragColor=vec4(pow(max(rgb,vec3(0.0)),vec3(1.0/2.2)),1.0);
            }
        """
    }
}
