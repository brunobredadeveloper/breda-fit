# Breda Fit · breda_dev

App Android de exercícios em casa e acompanhamento corporal, com núcleo **Kotlin Multiplatform**. Interface nativa Jetpack Compose / Material 3, identidade em verde-lima, grafite e azul e ilustrações vetoriais originais.

## Demonstrações 3D · 0.3

Personagem tridimensional original, iluminação, perspectiva, cenário, cadeira/parede em 3D e movimentos articulados. Controles para girar e ampliar; offline, com movimento reduzido e fallback vetorial informado em caso de GPU indisponível. Veja [arquitetura visual e limites](docs/visuals.md).

Avaliação: **Breda Fit 3D 0.3** (`dev.breda.fit.preview03`), instalada ao lado das versões anteriores. Os dados antigos permanecem nelas.

## O que está implementado

- Boas-vindas, perfil adulto, altura, peso inicial, meta e frequência semanal.
- Quatro rotinas: primeira rotina, em pé, mobilidade na cadeira e força em casa.
- Doze movimentos com figura animada, posições inicial/final, instruções e equipamento necessário.
- Sessão com preparação, exercício, descanso, pausa, retomada, encerramento e resumo de esforço percebido.
- Tempo monotônico: conta somente os intervalos ativos; pausa automaticamente ao ir para segundo plano.
- Histórico persistente, registro diário de peso/cintura/gordura/massa muscular, gráficos por data real e exportação CSV.
- IMC informativo, progresso até a meta, massa de gordura e massa livre de gordura quando o percentual foi informado.
- Opção de movimento reduzido, orientação por voz usando TTS Android, controles grandes e descrição acessível das figuras e gráficos.
- Escrita transacional no DataStore; erros de leitura não apagam o histórico; confirmação explícita para excluir dados.

Sem conta ou backend necessários. Sem anúncios, analytics, permissão de internet ou promessas de perda de peso. Não há Firebase configurado. A voz depende do idioma/engine TTS disponível no aparelho.

## Arquitetura

`shared`: Kotlin Multiplatform, com targets Android, JVM, iOS ARM64 e simulador ARM64. Domínio, validação, estatísticas, máquina de estados da sessão e contratos em commonMain.

`androidApp`: aplicativo Android, ViewModel com StateFlow, interface Compose e adapter DataStore. A interface iOS **não está implementada**; os targets iOS preparam o núcleo para uma futura interface e precisam de validação em macOS.

MVVM + Clean Architecture. O domínio não depende de Android. Repository/Adapter isolam persistência; reducer/state machine governa sessões; factory injeta dependências; Flow transporta snapshots imutáveis. Leia [arquitetura](docs/architecture.md).

## Build

JDK 17, Android SDK 37.0, build-tools 36.0.0. Gradle 9.4.1, AGP 9.2.1, Kotlin 2.4.21, Compose BOM 2026.09.00. Android 8+ (minSdk 26). As versões são fixadas para um conjunto estável e reproduzível, sem APIs alpha.

```sh
./gradlew :shared:jvmTest :androidApp:lintDebug :androidApp:assembleDebug
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`. Inclui toda a aplicação; não precisa de computador, Metro ou servidor. Assinatura debug é para avaliação interna. A esteira gera uma chave temporária; nenhum keystore é versionado. Builds diferentes podem exigir desinstalação do APK anterior; exporte dados de teste antes. O aplicativo de teste usa ID `dev.breda.fit.debug`, separado do ID de produção `dev.breda.fit`. Produção utiliza uma chave privada separada. Produção precisa de chave privada do titular, AAB e revisão de publicação.

GitHub Actions realiza testes, lint e build do APK em cada push/PR. O artefato se chama `breda-fit-android-apk`. Um workflow pronto não significa que ele já tenha executado: consulte [validação](docs/validation.md).

## Limites e saúde

Rotinas genéricas educativas; não substituem prescrição individual. Use cadeira firme sem rodas e apoio estável, amplitude confortável e adaptação profissional quando necessária. Dor ou mal-estar são motivo para parar.

IMC é indicador de triagem, não diagnóstico nem percentual de gordura. Massa muscular é informada a partir de uma medição; massa livre de gordura inclui também água, órgãos e ossos. Não são estimadas calorias ou datas para emagrecer. Fontes e critérios em [saúde](docs/health.md).

## Próximos passos para publicação

Revisão das rotinas e ilustrações por profissional de educação física; testes em dispositivos reais e com TalkBack/texto ampliado; assinatura de produção; identidade jurídica, contato de suporte, URL de privacidade e declarações de dados de saúde na Play Console. Não há cobrança ativa nesta versão.

Descrição sugerida do repositório:

> Offline home workouts and body progress tracking for Android, built with Kotlin Multiplatform, Jetpack Compose, MVVM and Clean Architecture by breda_dev.
