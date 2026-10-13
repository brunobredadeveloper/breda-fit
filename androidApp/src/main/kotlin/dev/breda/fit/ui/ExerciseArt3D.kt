package dev.breda.fit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.breda.fit.Exercise
import dev.breda.fit.ui.scene.SceneInput
import dev.breda.fit.ui.scene.SceneTexture

@Composable fun ExerciseArt(exercise:Exercise,animate:Boolean,modifier:Modifier=Modifier,fixedPhase:Float=0f) {
    var expanded by remember{mutableStateOf(false)}
    ScenePanel(exercise,animate && !expanded,modifier,fixedPhase,onExpand={expanded=true})
    if(expanded)Dialog(onDismissRequest={expanded=false},properties=DialogProperties(usePlatformDefaultWidth=false)){
        Surface(Modifier.fillMaxSize().padding(12.dp),color=Ink,shape=RoundedCornerShape(28.dp)){
            Column(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                Text(exercise.title,style=MaterialTheme.typography.headlineSmall)
                Text("Explore o movimento de outros ângulos.",color=Mist)
                ScenePanel(exercise,animate,Modifier.fillMaxWidth().weight(1f),fixedPhase)
                Text(exercise.cue,color=Lime)
                Button(onClick={expanded=false},modifier=Modifier.fillMaxWidth()){Text("Fechar demonstração")}
            }
        }
    }
}

@Composable private fun ScenePanel(exercise:Exercise,animate:Boolean,modifier:Modifier,fixedPhase:Float,onExpand:(()->Unit)?=null) {
    var ready by remember(exercise.id){mutableStateOf(false)}
    var failed by remember(exercise.id){mutableStateOf(false)}
    var orbit by remember(exercise.id){mutableFloatStateOf(0f)}
    BoxWithConstraints(modifier.clip(RoundedCornerShape(24.dp)).background(Color(0xFF091319)).testTag("exercise-art-${exercise.id}").semantics{contentDescription="Demonstração 3D: ${exercise.title}. ${exercise.cue}"}) {
        val controls=maxWidth>=180.dp && maxHeight>=150.dp
        if(failed){
            ExerciseArt2D(exercise,false,Modifier.fillMaxSize(),fixedPhase)
            Text("3D indisponível · guia ilustrado",Modifier.align(Alignment.TopCenter).padding(12.dp),color=Mist,style=MaterialTheme.typography.labelSmall)
        } else {
            AndroidView(factory={context->SceneTexture(context,{ready=true},{failed=true})},update={it.update(SceneInput(exercise,animate,fixedPhase.coerceIn(0f,1f),orbit))},onRelease={it.release()},modifier=Modifier.fillMaxSize().padding(bottom=if(controls)60.dp else 0.dp))
            if(ready)Box(Modifier.size(1.dp).testTag("scene-ready-${exercise.id}"))
            if(!ready)CircularProgressIndicator(Modifier.size(24.dp).align(Alignment.Center),color=Lime)
            if(controls){
                Surface(Modifier.align(Alignment.TopStart).padding(12.dp),color=Color(0xFF1C3037).copy(alpha=.92f),shape=RoundedCornerShape(8.dp)){
                    Text("3D / ${if(animate)"MOVIMENTO" else "POSTURA"}",Modifier.padding(horizontal=10.dp,vertical=6.dp),color=Lime,style=MaterialTheme.typography.labelSmall)
                }
                Row(Modifier.align(Alignment.BottomCenter).padding(8.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)){
                    Surface(color=Color(0xFF14272E).copy(alpha=.92f),shape=RoundedCornerShape(12.dp)){
                        Row {
                            TextButton(onClick={orbit=(orbit-45f)%360f},contentPadding=PaddingValues(horizontal=10.dp),modifier=Modifier.semantics{contentDescription="Girar personagem para a esquerda"}){Text("↶")}
                            TextButton(onClick={orbit=0f},contentPadding=PaddingValues(horizontal=8.dp)){Text("Padrão",style=MaterialTheme.typography.labelSmall)}
                            TextButton(onClick={orbit=(orbit+45f)%360f},contentPadding=PaddingValues(horizontal=10.dp),modifier=Modifier.semantics{contentDescription="Girar personagem para a direita"}){Text("↷")}
                        }
                    }
                    if(onExpand!=null)FilledTonalButton(onClick=onExpand,contentPadding=PaddingValues(horizontal=10.dp)){Text("Ampliar",style=MaterialTheme.typography.labelSmall)}
                }
            }
        }
    }
}
