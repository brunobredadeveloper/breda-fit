# Arquitetura e decisões

## Fronteiras

- Models/Catalog: entidades imutáveis, catálogo de exercícios e rotinas, sem Android.
- Validation/ProgressCalculator: regras e transformações puras, independentes da persistência.
- SessionEngine: reducer determinístico; recebe milissegundos transcorridos de um clock externo.
- FitRepository: porta de observação e atualização atômica.
- LocalFitRepository: adapter Android DataStore. Decodifica e valida antes de escrever; não usa recuperação destrutiva automática.
- FitViewModel: orquestra ações e snapshots StateFlow; lifecycle, monotonic clock e IDs pertencem à plataforma Android.
- ui: Compose, formulário, navegação por quatro tabs, biblioteca, modais e sessão. Desenho das figuras é Canvas vetorial original, não uma foto licenciada de terceiros.

O Composition Root é FitApplication + ViewModel factory. Não há service locator global no domínio. SOLID se manifesta nas fronteiras e na injeção de FitRepository: cálculos/cronômetro não conhecem DataStore nem Activities. Não foram criadas interfaces para todo detalhe sem uma razão de substituição.

## Estados da sessão

READY → WORK → REST → WORK… → FINISHED. Não há descanso depois do último intervalo. Todo tempo excedente em um tick é consumido pelas transições seguintes; preparação e descanso nunca entram em activeMs. Na pausa, o estado permanece idêntico. Retomar rebaseia o clock monotônico para excluir o período pausado. ON_STOP pausa; retomar exige ação do usuário. Pausar cancela o job do cronômetro, e retomar cria um novo job. A tela fica acordada apenas durante a sessão ativa; a flag é removida ao pausar, concluir ou sair.

Só uma sessão concluída pode ser gravada. UUID torna salvar idempotente mesmo quando a UI repete uma ação. Encerrar antes de concluir é explícito e não registra um treino completo. Esforço percebido é um relato do usuário, não uma medição de sensor.

## Persistência e privacidade

Snapshot JSON versionado no DataStore Preferences, validado antes da gravação. edit serializa as mutações. Codificação, validação e decodificação JSON ficam fora da thread principal. Um registro de medidas por dia: atualizar hoje substitui o registro de hoje, não duplica. Histórico limitado a 2000 medidas e 2000 sessões; atingir o limite gera erro, sem descartar silenciosamente registros antigos. Corrupção ou schema futuro bloqueia leitura/escrita; usuário pode tentar novamente ou confirmar destruição.

Android allowBackup=false; nenhum dado do app é enviado por seu código para rede. Exportação usa Storage Access Framework, sem permissão ampla de arquivos. CSV contém as medidas e a data ISO (YYYY-MM-DD), formatada pelo adapter Android; dados de perfil e treinos não estão no CSV. TTS é fornecido pelo Android e pode depender de engine/idioma externo: não constitui garantia de funcionamento offline de toda engine instalada.

O processo pode morrer durante um treino: a sessão ativa não é persistida nem restaurada como se estivesse concluída. Perfil e históricos gravados permanecem. É uma limitação explícita deste primeiro release.

## Distribuição de teste

Debug usa dev.breda.fit.debug. A esteira gera uma chave temporária, sem keystore no repositório. Não há garantia de atualização entre APKs de execuções distintas; exporte dados de teste antes de desinstalar. Release tem ID separado dev.breda.fit e exige assinatura privada do titular.

## Multiplataforma real

O núcleo commonMain não importa Java/Android. Targets Android e JVM consomem o mesmo código; iOS ARM64/simulador declarados. A UI atual é Android nativa. A existência de targets iOS não representa um aplicativo iOS pronto nem uma compilação nativa iOS validada.
