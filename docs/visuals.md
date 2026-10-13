# Breda Fit 3D · 0.3

As demonstrações usam malhas tridimensionais originais, renderizadas em OpenGL ES 2 através de TextureView. Personagem articulado com rosto, cabelo, roupa, tênis, luz principal/preenchimento, materiais com brilho discreto e perspectiva. Cadeira e parede também são geometria tridimensional. O piso possui uma sombra de contato estilizada; não há simulação física ou shadow mapping dinâmico.

Nos painéis grandes é possível girar a câmera em passos de 45 graus, restaurar o ângulo padrão e ampliar a demonstração. Os modelos e shaders estão no app, sem downloads, internet ou bibliotecas remotas. O renderer EGL usa um dispatcher dedicado, libera superfícies/contextos ao desmontar e interrompe renderização contínua quando invisível. Miniaturas estáticas só desenham ao criar/redimensionar/alterar a cena; animação limita-se a aproximadamente 30 fps.

A preferência de movimento reduzido continua mostrando uma pose estática. A animação é uma demonstração visual, não conta repetições e não controla o tempo da sessão. Domínio KMP, registros, metas e estatísticas permanecem separados da apresentação Android. Se a GPU não inicializar, o app informa a indisponibilidade do 3D e apresenta o guia vetorial, preservando o treino.

Avaliação: `dev.breda.fit.preview03`, “Breda Fit 3D 0.3”. Instala ao lado das versões anteriores, sem apagar seus dados; não há migração automática entre IDs. Assinatura CI temporária; publicação e atualizações comerciais exigem credenciais de assinatura privadas estáveis.

A aparência 3D não equivale a biomecânica validada: as poses continuam ilustrativas e exigem revisão profissional antes da publicação comercial. Não são humanos escaneados, captura de movimento ou modelos clínicos. Os testes verificam inicialização real da GPU, catálogo e posições estáticas; movimento, GPU e desempenho físico devem ser avaliados no celular.
