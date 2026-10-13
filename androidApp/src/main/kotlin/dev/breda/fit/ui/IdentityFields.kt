package dev.breda.fit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.breda.fit.*

internal val LocalAvatarStyle=staticCompositionLocalOf{AvatarStyle.CAIO}
internal fun AvatarStyle.label()=when(this){AvatarStyle.CAIO->"Caio";AvatarStyle.AURORA->"Aurora";AvatarStyle.ALEX->"Alex"}
internal fun GenderIdentity.label()=when(this){GenderIdentity.UNSPECIFIED->"Prefiro não informar";GenderIdentity.WOMAN->"Mulher";GenderIdentity.MAN->"Homem";GenderIdentity.NON_BINARY->"Pessoa não binária";GenderIdentity.SELF_DESCRIBED->"Quero me descrever"}

@Composable internal fun IdentityFields(
    name:String,avatar:AvatarStyle,identity:GenderIdentity,description:String,
    onName:(String)->Unit,onAvatar:(AvatarStyle)->Unit,onIdentity:(GenderIdentity)->Unit,onDescription:(String)->Unit
){
    Field("Nome social ou de preferência",name,false,onName)
    Text("É assim que vamos chamar você. Não precisamos do seu nome de registro.",color=Mist,style=MaterialTheme.typography.bodySmall)
    Text("Identidade de gênero · opcional",style=MaterialTheme.typography.titleMedium)
    GenderIdentity.entries.forEach { option ->
        FilterChip(selected=identity==option,onClick={onIdentity(option);if(option!=GenderIdentity.SELF_DESCRIBED)onDescription("")},label={Text(option.label())},modifier=Modifier.testTag("identity-${option.name}"))
    }
    if(identity==GenderIdentity.SELF_DESCRIBED)Field("Como você se identifica?",description,false,onDescription)
    Text("Escolha seu avatar",style=MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
        AvatarStyle.entries.forEach { style->FilterChip(selected=avatar==style,onClick={onAvatar(style)},label={Text(style.label())},modifier=Modifier.testTag("avatar-option-${style.name}")) }
    }
    Text("São atletas fictícios. Qualquer avatar pode ser escolhido, independentemente da sua identidade de gênero.",color=Mist,style=MaterialTheme.typography.bodySmall)
    CompositionLocalProvider(LocalAvatarStyle provides avatar){ExerciseArt(Catalog.exercise("march"),false,Modifier.fillMaxWidth().height(240.dp))}
}
