# Rascunho de privacidade — revisar antes da publicação

Breda Fit registra nome escolhido, idade, altura, meta de peso, frequência desejada, medidas informadas e sessões concluídas no armazenamento privado do aparelho.

Não exige conta, não inclui publicidade, analytics, upload ou integração com plataforma de saúde nesta versão. O Android backup está desabilitado. Desinstalar ou confirmar a opção de apagar dados remove o histórico local. A exportação voluntária de CSV grava medidas no local escolhido pelo usuário; o controle do arquivo exportado fica com ele.

Orientação por voz usa o mecanismo TTS do aparelho. O funcionamento e eventual conectividade do mecanismo dependem do provedor instalado e das configurações do sistema.

Antes da Play Store: incluir nome/identidade jurídica do controlador, e-mail real de suporte, política em URL pública, versão/data, direitos e canal de contato aplicáveis. Não inventar contato. Revisar declarações de dados de saúde e Data Safety conforme comportamento efetivo do build final e eventuais SDKs adicionados.


Signing update: GitHub builds generate an ephemeral debug key. No keystore is committed. Different build runs may require uninstalling the previous evaluation APK before installing; export test measurements first. Production signing remains separate.
