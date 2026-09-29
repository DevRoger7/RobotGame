# Mapa da UI atual (antes da interface "Labirinto Neon")

Levantamento feito antes da reimplementação da camada visual. A segunda metade registra as
decisões tomadas durante a implementação (seção 9 do plano).

## 1. Ponto de entrada e modos

- `robotgame.ui.RobotGameApp` (`Application`) carrega `main.fxml`, cria uma `Scene` de 660×700 e
  registra `MainController::aoTeclaPressionada` como filtro de `KEY_PRESSED` na cena.
- Execução: `mvn javafx:run` (plugin `javafx-maven-plugin`, `mainClass = robotgame.ui.RobotGameApp`) ou
  pela IDE. Java 21, JavaFX 21.0.12 (`controls`, `fxml`, `media`).
- Os 4 modos são o enum `robotgame.modelo.ModoJogo`: `MANUAL`, `CORRIDA`, `NORMAL_X_INTELIGENTE`,
  `COM_OBSTACULOS`. O modo é escolhido num `ComboBox` e `MainController.iniciarJogo()`:
  - valida cores diferentes (2 robôs) e cria `Tabuleiro(x, y)` (lança `IllegalArgumentException` se fora
    de 0–3 ou em (0,0));
  - cria `Robo(cor1)` e, nos modos de 2 robôs, `Robo(cor2)` ou `RoboInteligente(cor2)` (modos 3 e 4);
  - sorteia a fruta entre cereja, maçã e morango;
  - modo 4 entra em "posicionando obstáculos"; os demais chamam `comecarPartida()`.

## 2. Telas e navegação

- Uma única tela FXML (`main.fxml`) com dois `VBox` empilhados num `StackPane`: `setupPane`
  (configuração) e `gamePane` (jogo). A navegação é mostrar/esconder (`visible` + `managed`).
- A fase de obstáculos do modo 4 é uma barra (`obstaculosBar`) dentro do `gamePane`.
- O fim de partida não tem tela própria: o status mostra o resultado e `statsLabel` mostra as contagens.
- "Novo jogo" para tudo e volta ao `setupPane`.

## 3. Imagens e sons

- Imagens: `src/main/resources/robotgame/ui/imagens/` carregadas por `robotgame.ui.Imagens`
  (`carregar`, `carregarRecortada` — recorta margens transparentes —, `carregarSemFundo` — apaga o fundo
  claro ligado à borda e recorta).
  - `pacman_aberto.png` / `pacman_fechado.png`: um único sprite amarelo; a cor de cada pacman é aplicada
    com `Blend(SRC_ATOP, ColorInput(cor))`. Boca alterna a cada 150 ms (`animacaoBoca`).
  - Rotação pela última direção (baixo 90°, cima 270°, esquerda = espelho horizontal).
  - `fantasma_{azul,vermelho,rosa,ciano}.png` (sem fundo), `obstaculo_pedra.png` (recortada),
    `fruta_cereja.png`, `fruta_maca.png` (sem fundo), `fruta_morango.png` (sem fundo).
  - Não há imagem de explosão.
- Sons: `src/main/resources/robotgame/ui/sons/` tocados por `robotgame.ui.Sons` (um `MediaPlayer` por
  arquivo, instância única no controller):
  - `inicio.wav` — `comecarPartida()`; ao terminar (ou após o limite de 6 s) chama `liberarPartida()`.
  - `wakawaka.wav` — em loop de `liberarPartida()` até o fim; parado na morte e retomado se alguém
    continuar vivo; parado em `finalizar()`.
  - `morte.wav` — `iniciarMorte()` quando um robô deixa de estar ativo.
  - `comer_fruta.wav` — quando um robô chega à fruta (manual e simulação).
  - `pausarTudo`/`retomarTudo` na pausa, `setMudo` no M, `pararTudo` no "Novo jogo".

## 4. Temporização

- Simulação (modos 2–4): `Timeline` com `KeyFrame` de 700 ms (`INTERVALO_SIMULACAO`) chamando
  `Simulacao.proximoPasso()` (um robô por vez, direção aleatória).
- "Prepare-se...": `comecarPartida()` congela a partida, escreve no `statusLabel`, toca `inicio.wav` e
  arma um `PauseTransition` de 6 s; o que terminar primeiro libera a partida, mostra "START" (fade de
  600 ms após 900 ms), inicia a boca e o wakawaka.
- Morte: `Timeline` de 30 ms por 2 s encolhendo, girando (720°) e apagando o sprite do robô.
- Pausa: pausa todas as `Timeline`/`FadeTransition` e os sons; mostra "PAUSADO" sobre o tabuleiro.

## 5. Contadores e estado dos robôs

- `Robo`: `x`, `y`, posição anterior, `ativo`, `movimentosValidos`, `movimentosInvalidos`,
  `getTotalMovimentos()`.
- Movimento inválido: `Robo.mover` lança `MovimentoInvalidoException` e soma 1 em inválidos.
  `RoboInteligente.mover` tenta de novo com direções ainda não tentadas (cada falha soma 1).
- Estado: ativo / explodiu (`!isAtivo()`) / achou (`tabuleiro.alimentoEncontradoPor(robo)`).
- Fim (`Simulacao.verificarFim`): modo 3 termina quando os dois acham; modos 2 e 4 quando um robô ativo
  acha, quando nenhum está ativo, ou após 1000 rodadas.
- Rocha: `Rocha.bater` devolve o robô à posição anterior. Fantasma (`Bomba`): `explodir()` e o
  obstáculo é removido do tabuleiro.

## 6. Modo 4 — colocação de obstáculos

- Clique numa casa: se ocupada, remove; senão cria `Fantasma(próxima cor)` ou `Rocha` e chama
  `Tabuleiro.adicionarObstaculo`, que bloqueia fora do tabuleiro, (0,0), a casa da fruta e casa ocupada.
- Ordem das cores dos fantasmas: azul, vermelho, rosa, ciano (cíclica), indexada por
  `fantasmasColocados` — que só aumenta (remover um fantasma não "devolve" a cor).

## 7. Cores e fruta

- Cores de pacman (`CORES`): amarelo `#ffd800`, vermelho `#ff4444`, azul `#4488ff`, verde `#44ff66`,
  rosa `#ff88cc`, laranja `#ffa033`, roxo `#cc55ff`, ciano `#44eaff`, branco `#f0f0f0`.
  Padrão: pacman 1 amarelo, pacman 2 vermelho. Nos modos de 2 robôs as cores precisam ser diferentes.
- Fruta: sorteada (cereja, maçã, morango) a cada início de partida. Padrão da posição: (0, 3).

## 8. Atalhos

- Tratados em `MainController.aoTeclaPressionada` (filtro na `Scene`): M liga/desliga som; P ou ESPAÇO
  pausa; setas movem no modo manual (ignoradas se congelado, pausado ou terminado).

---

## Decisões da nova interface

1. **Local dos mockups**: o PDF está em `docs/desing/telas.pdf` (pasta com erro de digitação, mantida).
   Páginas: 1 menu, 2 cenário, 3 jogo, 4 jogo manual, 5 fim.
2. **Sprites não são pixel art** (ilustrações de 240–1254 px). Com `setSmooth(false)` a redução direta
   ficaria serrilhada, então `ui.components.Sprites` gera em memória uma cópia reduzida com
   filtro suave no tamanho de exibição × escala da tela, e o `ImageView` usa `setSmooth(false)` e
   `setPreserveRatio(true)`. Os arquivos em disco não mudam.
3. **Cores dos dois pacmans**: o mockup mostra uma só linha de cores. Para não perder a escolha da cor
   do pacman 2, nos modos de 2 robôs a linha ganha as abas "PACMAN 1 / PACMAN 2"; as 9 cores
   existentes aparecem como botões (44 px para caber). A validação "cores diferentes" continua.
4. **Fruta**: sorteada entre as três imagens existentes ao abrir o menu (para aparecer no
   mini-tabuleiro); "Jogar de novo" mantém a mesma fruta.
5. **Eventos da partida**: novo `modelo.EventoPartida`. Mudanças aditivas no modelo, sem alterar
   comportamento: `Robo` aceita um ouvinte chamado nos dois pontos onde já soma movimento inválido;
   `Simulacao` aceita um ouvinte chamado quando detecta movimento, rocha, explosão e fruta. O modo
   manual publica os mesmos eventos a partir da tela. O diário só formata esses eventos.
6. **Mensagem de inválido**: segue o mockup ("tentou ir para y = −1"), calculada da posição atual e da
   direção tentada.
7. **Explosão**: não existe imagem; é desenhada uma estrela (polígono laranja/vermelho).
8. **Velocidade**: intervalo base continua 700 ms ("Normal", nível 3); o slider altera
   `Timeline.setRate`: 1 → 1400 ms, 2 → 1000 ms, 3 → 700 ms, 4 → 450 ms, 5 → 250 ms.
9. **ESPAÇO** continua pausando (como hoje), além do P.
10. **Faixa "Proibido"**: listras vermelhas à esquerda e embaixo do tabuleiro, como no mockup, com
    item na legenda.
11. **Jogar de novo** no modo 4: a configuração guarda a lista de obstáculos colocados e o tabuleiro é
    recriado com eles (fantasmas explodidos voltam).
12. **Fim por limite de 1000 rodadas** (modos 2 e 4): tratado como "NINGUÉM ACHOU A FRUTA".
13. `MainController` e `main.fxml` foram substituídos pelas novas telas (a lógica de partida foi migrada
    sem mudanças de regra para `ui.screens.GameScreen`).
14. **Fontes**: `Press Start 2P` e `Chakra Petch` (Medium, SemiBold, Bold) em `src/main/resources/fonts/`,
    com as licenças OFL. Nomes reais das famílias no JavaFX: `Press Start 2P`, `Chakra Petch Medium`,
    `Chakra Petch SemiBold` e `Chakra Petch` (Bold) — o CSS usa esses nomes.
15. **Acentos na pixel-font**: a Press Start 2P desenha maiúsculas acentuadas com altura de minúscula.
    Como no mockup ("CENARIO", "DIARIO", "SIMULAÇAO"), os textos nessa fonte perdem os acentos e mantêm
    o Ç (`Ui.pixel`). Textos em Chakra Petch mantêm a acentuação.
16. **Legenda**: até 4 itens ficam numa linha só (modos 1–3); o modo 4 usa 3 colunas × 2 linhas.
17. **"Como terminou"**: nos modos 2–4 mostra só eventos decisivos (rocha, explosão, fantasma sumindo,
    fruta); no modo 1, que só tem movimentos, mostra as 3 últimas jogadas.
18. **Modo 3 no fim**: vence quem usou menos movimentos no total (válidos + inválidos); se empatar,
    "EMPATE!". O placar do mais rápido ganha o chip amarelo "VENCEU".
19. **Transição para o fim**: a tela de jogo espera 1,3 s (2,3 s se houver animação de morte) antes de
    abrir a tela de fim, para o jogador ver o último lance; os sons de fruta/morte terminam naturalmente.
