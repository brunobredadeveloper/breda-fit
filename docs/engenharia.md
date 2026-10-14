# Engenharia — Breda Fit

## Problema técnico

Organizar rotinas educativas, acompanhar sessões e registrar medidas sem inventar calorias, diagnósticos ou resultados de emagrecimento.

## Responsabilidades e código

| Fronteira | Responsabilidade real | Onde inspecionar |
|---|---|---|
| Domínio compartilhado | Modelos, validações e estatísticas commonMain | [Models.kt](../shared/src/commonMain/kotlin/dev/breda/fit/Models.kt) |
| Sessão | Máquina de estados e relógio monotônico | [Session.kt](../shared/src/commonMain/kotlin/dev/breda/fit/Session.kt) |
| Apresentação Android | FitViewModel e StateFlow | [FitViewModel.kt](../androidApp/src/main/kotlin/dev/breda/fit/presentation/FitViewModel.kt) |
| Infraestrutura e visual | DataStore e cena OpenGL ES | [LocalFitRepository.kt](../androidApp/src/main/kotlin/dev/breda/fit/data/LocalFitRepository.kt) |

## Invariantes e decisões

- Sessão interrompida não deve ser contabilizada como treino concluído.
- Massa muscular informada não é massa livre de gordura calculada.
- Erro de leitura não pode apagar o histórico.

## Como examinar os testes

Os arquivos abaixo fazem parte do código versionado. A existência de uma suíte não comprova sua aprovação; o resultado remoto está em [evidências](evidencias.md).

- [androidApp/src/androidTest/kotlin/dev/breda/fit/ExerciseVisualTest.kt](../androidApp/src/androidTest/kotlin/dev/breda/fit/ExerciseVisualTest.kt)
- [androidApp/src/androidTest/kotlin/dev/breda/fit/FitUiTest.kt](../androidApp/src/androidTest/kotlin/dev/breda/fit/FitUiTest.kt)
- [shared/src/commonTest/kotlin/dev/breda/fit/FitTests.kt](../shared/src/commonTest/kotlin/dev/breda/fit/FitTests.kt)

## Limites e compromissos

Interface iOS não implementada; targets KMP não comprovam app iOS. Rotinas precisam de revisão profissional antes de publicação comercial. O conceito fotorrealista não foi integrado; as capturas mostram o 3D real atual, com ativos CC0 e limitações descritas.

A separação de responsabilidades documentada é a encontrada no código. Não se afirma certificação SOLID, paridade com produtos comerciais, auditoria de segurança ou operação em produção.

## Reproduzir a validação

```sh
python3 tools/human/build_model.py
./gradlew :shared:jvmTest :androidApp:lintDebug :androidApp:assembleDebug
```

[Voltar ao README](../README.md) · [Fluxos](fluxos.md)
