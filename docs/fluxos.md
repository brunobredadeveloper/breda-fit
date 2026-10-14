# Fluxos de uso — Breda Fit

Organizar rotinas educativas, acompanhar sessões e registrar medidas sem inventar calorias, diagnósticos ou resultados de emagrecimento.

## Mapa de telas

| Tela / área | O que a pessoa faz | Referência de implementação |
|---|---|---|
| Hoje | Ver rotina e acompanhamento pessoal. | [FitApp.kt](../androidApp/src/main/kotlin/dev/breda/fit/ui/FitApp.kt) |
| Treinos | Consultar rotinas, instruções e demonstrações 3D. | [ExerciseArt3D.kt](../androidApp/src/main/kotlin/dev/breda/fit/ui/ExerciseArt3D.kt) |
| Sessão | Preparar, executar, pausar e encerrar treino. | [SessionScreen.kt](../androidApp/src/main/kotlin/dev/breda/fit/ui/SessionScreen.kt) |
| Evolução | Registrar medidas e consultar gráficos. | [ProgressScreen.kt](../androidApp/src/main/kotlin/dev/breda/fit/ui/ProgressScreen.kt) |
| Perfil | Editar nome, preferências e avatar. | [IdentityFields.kt](../androidApp/src/main/kotlin/dev/breda/fit/ui/IdentityFields.kt) |

## Demonstração guiada

Use dados fictícios, em ambiente de avaliação.

1. Crie um perfil de teste e escolha frequência e meta.
2. Selecione uma rotina e consulte as instruções dos movimentos.
3. Inicie sessão, pause e envie o app ao segundo plano; o tempo deve pausar.
4. Conclua a sessão e consulte o histórico.
5. Registre uma medida e altere o avatar pelo perfil.

## Casos que merecem atenção

- Sessão interrompida não deve ser contabilizada como treino concluído.
- Massa muscular informada não é massa livre de gordura calculada.
- Erro de leitura não pode apagar o histórico.

## Escopo e resultado esperado

Interface iOS não implementada; targets KMP não comprovam app iOS. Rotinas precisam de revisão profissional antes de publicação comercial. O conceito fotorrealista não foi integrado; as capturas mostram o 3D real atual, com ativos CC0 e limitações descritas.

## Galeria e contexto

As imagens abaixo representam um instante de uma execução automatizada. Uma captura estática não demonstra som, suavidade de animação, latência ou persistência.

### Demonstração de exercício na cadeira

![Breda Fit — captura real](imagens/visual-chair.png)

### Visualização 3D ampliada

![Breda Fit — captura real](imagens/visual-expanded-3d.png)

## Checklist para validação do proprietário

- [ ] Instalar/abrir a versão correspondente às evidências.
- [ ] Percorrer a jornada principal e uma condição de falha.
- [ ] Confirmar navegação, retorno, teclado e leitura em tela pequena.
- [ ] Fechar/reabrir e verificar os dados que devem persistir.
- [ ] Avaliar os recursos nativos pertinentes no aparelho real.
- [ ] Registrar versão, aparelho, resultado esperado e observado em eventual relato.

[Voltar ao README](../README.md) · [Evidências](evidencias.md)
