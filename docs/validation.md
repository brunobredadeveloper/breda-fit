# Validação — 12/10/2026

17 testes do núcleo Kotlin compartilhado compilados com Kotlin 2.4.21 e executados pelo Gradle/JUnit 4: aprovados. Cobertura inclui IMC, meta, ausência de gordura inventada, substituição diária, dados inválidos, pausa, transições exatas, tick longo, fim sem descanso, gravação idempotente, frequência semanal, sequência, limites de progresso, dados futuros, catálogo, exportação, composição inconsistente e transição sem descanso.

Build Android local concluído com sucesso: :shared:jvmTest, :androidApp:lintDebug, :androidApp:assembleDebug e :androidApp:assembleDebugAndroidTest. Lint: zero issues. Dois testes de interface compilados, ainda não executados em emulador/aparelho.

APK local anterior de avaliação: dev.breda.fit.debug, versão 0.1.0-debug, Android 8+ (API 26), target API 37. Assinatura v2/v3 verificada por apksigner; alinhamento de 16 KB verificado por zipalign. Tamanho: 12928526 bytes. SHA-256: `38aab1e05f18d033549967caa5264e75f3aa49a657c09dd99b0ea87a6ea94d92`. Dados fictícios são recomendados para avaliação. A assinatura debug do APK CI é temporária e separada de produção.

GitHub Actions executado com sucesso: testes, lint sem erros, build, assinatura de teste e verificação antes do upload. Instalação física e revisão profissional das demonstrações ainda pendentes.

Núcleo iOS e UI iOS: targets de núcleo declarados, não compilados em macOS; UI iOS não implementada.

## Avaliação no aparelho

1. Boas-vindas e validação de perfil com números em formato 82,5 e 82.5.
2. Reabrir o app: navegação, perfil e registros mantidos; não voltar ao onboarding.
3. Catálogo e movimentos em animação, posições fixas, movimento reduzido e TalkBack.
4. Preparação/exercício/descanso, pausa/retomada, background, saída e conclusão.
5. Tempo ativo exclui descanso e pausa; salvar duas vezes não duplica.
6. Registrar peso duas vezes no mesmo dia substitui; opcionais ausentes não viram zero.
7. Gráficos, metas, medidas e exportação CSV; cancelamento da exportação não acusa erro.
8. Texto ampliado, telas pequenas, orientação paisagem e TTS ausente/instalado.
9. Confirmar exclusão explicitamente; falha de armazenamento não limpa dados automaticamente.


GitHub Actions: execução 38324340446 concluída com sucesso em 12/10/2026 UTC e no Brasil, commit 1145b6d1020eef7da07782851b893f1e17ccdda5. Testes shared, lint, APK principal e APK de testes compilados; assinaturas verificadas. APK CI SHA-256: 77f5b900b2db2385a2fd49d83bbfbbb4b90dc091337725d7b6f66409840852cd. Testes instrumentados ainda não executados em aparelho.
