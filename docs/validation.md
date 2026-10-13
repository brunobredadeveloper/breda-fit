# Validação humana — Breda Fit 0.4.0 — 13/10/2026

GitHub Actions [38446052966](https://github.com/brunobredadeveloper/breda-fit/actions/runs/38446052966), código `658d56ef75e2cebea18094df24eaea9195d625ff`: concluído com sucesso. Os ativos gráficos foram preservados no repositório pelo job `preserve-human-assets`, commit `f2b6925bb9b48b01bb2d0e51c595effa3407d6b3`.

- 17 testes KMP JVM e 5 testes nativos: **22 aprovados**, zero falhas.
- Doze cenas inicializaram o modelo humano na GPU. Duas posições da cadeira, alteração real de pixels ao girar, ampliação/fechamento e navegação/perfil aprovados.
- Capturas nativas do catálogo, posições da cadeira e demonstração ampliada inspecionadas. Corpo, face, dedos, roupa e tênis renderizados; membros conectados e cenário enquadrado. O catálogo de testes é uma grade de inspeção, não a tela de catálogo do produto.
- Emulador Android 15/API 35, Pixel 2/SwiftShader. Log de diagnóstico de runtime/renderização sem erros nesta execução. Não é um benchmark ou teste em celular físico.
- Lint: zero erros e quatro avisos (três versões de dependências e construtor de View programática).
- APKs debug/preview/instrumentação compilados; apksigner confirmou assinaturas v2 e v3.
- APK preview: **21.567.540 bytes**, SHA-256 `269522c0b982a8222b6acc9cfc26168027d9038eb5df4049b30585c3df5a6978`. Digest do artefato, CRC ZIP e bloco de assinatura conferidos após download.
- ID `dev.breda.fit.preview04`, nome “Breda Fit Humano 0.4”. Instalação independente; dados anteriores permanecem nos apps antigos, sem migração automática.
- Vinte arquivos de origem com SHA-256 fixado. Download limpo/conversão local reproduziu os mesmos arquivos finais. Geometria finita, pesos normalizados, IDs do rig e índices de 16 bits verificados.

Modelo anatômico adulto CC0 e materiais texturizados, offline, com animação por esqueleto na GPU. Não é pessoa digitalizada, simulação biomecânica ou captura fotográfica. Domínio, estatísticas e persistência preservados. A qualidade dos movimentos precisa de revisão profissional antes de publicação comercial. Não foi compilado iOS nesta execução; UI iOS não implementada.

Veja [visual](visuals.md) e [licença/proveniência](../tools/human/NOTICE.md). No celular: testar movimento reduzido, câmera, ampliar, pausa, retomada, background e diferentes tamanhos de tela.

---

## Histórico de validação

# Validação 3D — Breda Fit 0.3.0 — 13/10/2026

GitHub Actions [38439250615](https://github.com/brunobredadeveloper/breda-fit/actions/runs/38439250615), código `0db7348290a5f2e50fc8b78cef6c0f560486a203`: concluído com sucesso.

- 17 testes do domínio KMP e 5 testes nativos de interface: **22 aprovados**.
- GPU/EGL inicializou nos 12 exercícios e nas duas poses da cadeira. A câmera mudou os pixels renderizados; a demonstração ampliada abriu, inicializou sua cena e fechou corretamente.
- Capturas nativas do catálogo, duas poses e tela ampliada inspecionadas. Materiais/luz e membros refinados; controles têm espaço separado dos pés.
- Emulador Android 15/API 35, Pixel 2, GPU SwiftShader. Não é uma medição de FPS, energia ou desempenho do celular físico.
- Lint: zero erros, quatro avisos (três atualizações de dependências e construtor de view usada exclusivamente por AndroidView, sem inflação XML).
- Debug, preview e instrumentação compilados; assinaturas v2/v3 verificadas por apksigner.
- APK preview: 12.858.561 bytes, SHA-256 `9479a63b186cec6ae51ee256d37dde44d2f4844045b9532a1e3ca284d3c98a49`. Digest do artefato, integridade ZIP e bloco de assinatura verificados após download.
- ID `dev.breda.fit.preview03`, nome “Breda Fit 3D 0.3”. Instalação independente para preservar os apps anteriores; não migra dados automaticamente.

Cena nativa OpenGL ES 2, original e offline. São personagens 3D estilizados com poses ilustrativas, não biomecânica clinicamente validada. Domínio, estatísticas e persistência preservados. iOS não compilado nesta execução; UI iOS não implementada.

Veja [arquitetura visual](visuals.md). Teste câmera, ampliar/fechar, movimento reduzido, pausar/retomar, navegação, orientação e background no aparelho físico.

---

## Histórico de validação

# Validação visual — Breda Fit 0.2.0 — 13/10/2026

GitHub Actions [38430422213](https://github.com/brunobredadeveloper/breda-fit/actions/runs/38430422213), código `15deba70ed9089d5b62ee1c6ab5b52469a7e0c38`.

- 17 testes do domínio Kotlin compartilhado e 4 testes nativos de interface: 21 aprovados.
- Android lint: zero erros; três avisos de atualização de dependências.
- APKs debug, preview e instrumentação compilados e assinaturas v2/v3 verificadas na esteira.
- Emulador Android 15/API 35, perfil Pixel 2: criação de perfil/navegação, navegação com perfil existente, catálogo com 12 ilustrações e comparação de duas poses de cadeira.
- Capturas nativas usadas para inspeção de proporções e enquadramento. A revisão visual orientou o refinamento da postura em pé, da amplitude ilustrativa do miniagachamento e da perna de apoio.
- APK preview: 12.825.735 bytes, SHA-256 `8038d5a1347e08a55aaf37ec65b76c918d753c4c9ac3ddb4b9f70b04b4ba0394`. Digest do artefato, integridade ZIP e bloco de assinatura conferidos após download.
- Avaliação separada: `dev.breda.fit.preview02`, “Breda Fit Visual 0.2”. Dados do app anterior permanecem nele e não são copiados automaticamente.

Estas verificações validam renderização/software, não biomecânica, eficácia de treino ou execução física. O conteúdo continua sujeito à revisão profissional antes da publicação comercial. O núcleo iOS não foi compilado nesta execução; UI iOS não implementada.

Veja [ilustrações e limites](visuals.md). Domínio, sessões, estatísticas e persistência não foram alterados nesta rodada.

---

## Histórico de validação

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
