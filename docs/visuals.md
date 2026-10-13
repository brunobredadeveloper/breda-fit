# Demonstrações humanas 0.4

## Modelo e materiais

A geometria humana CC0 do MakeHuman Community substitui as esferas/cilindros do personagem 0.3. Rosto e mãos são malhas anatômicas; pele, olhos, cabelo, sobrancelhas e tênis utilizam texturas licenciadas. A roupa mantém suas dobras modeladas, com cores próprias da breda_dev, sem os logos/texturas do material fonte. Fontes, hashes e licença estão em `tools/human/` e no APK.

`build_model.py` é um conversor original, não usa código de aplicação do MakeHuman. Aplica morfologia adulta, ajusta acessórios por coordenadas baricêntricas, remove faces ocultas sob a roupa, calcula normais suaves, agrupa pesos em 16 ossos e exporta malhas indexadas de 16 bits. Quatro pesos normalizados por vértice, materiais separados, texturas limitadas a 1024px na GPU e mipmaps reduzem custo e serrilhado. O APK inclui todos os ativos.

## Animação e renderização

`HumanModel` usa OpenGL ES 2, VBOs e skinning na GPU. Transformações rígidas preservam comprimento dos ossos; IK de dois segmentos posiciona membros. O torso acompanha a inclinação e as mãos seguem os antebraços. Iluminação GGX aproximada, cores em espaço linear, rugosidade por material e tone mapping. Não há captura de movimento, rig facial animado, tecido físico ou renderização fotográfica.

`ExerciseScene` mantém câmera, palco, cadeira/parede e sombras de contato geométricas. Uma única fila gráfica atende TextureViews; cenas estáticas só são redesenhadas quando necessário. Superfícies, buffers e texturas são liberados ao sair. Falha de EGL/shader/ativos aciona guia vetorial explicitamente informado, sem afetar treino ou histórico.

## Experiência

Girar para ambos os lados, restaurar câmera e ampliar a demonstração. Movimento reduzido mantém pose estática. A sessão só anima durante exercício ativo. Instruções textuais, posições inicial/final e descrição acessível continuam disponíveis. O modelo é um adulto fictício vestido; o app não recebe nem envia modelos ou medidas pela rede.

## Limites

Animações educativas interpoladas, sem validação biomecânica ou prescrição individual. Precisam de revisão profissional e avaliação em dispositivos reais antes de publicação. Não se afirma equivalência a vídeo de instrutor, qualidade de captura fotogramétrica ou validação de iOS. A imagem anexada à entrega deve ser obtida do renderizador do aplicativo em teste instrumentado.
