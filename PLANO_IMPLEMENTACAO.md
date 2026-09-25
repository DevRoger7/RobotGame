# Plano de Implementação — RobotGame (2º Trabalho: Tratamento de Exceção)

Documento para o Claude Code executar. Todas as decisões de design abaixo já foram tomadas — implemente exatamente como descrito, sem adicionar classes, atributos ou funcionalidades não listados.

- **Repositório:** `DevRoger7/RobotGame` (hoje contém apenas `LICENSE`)
- **Linguagem:** Java 21 (JDK 21 LTS)
- **Build:** Maven, já preparado para JavaFX (a interface gráfica vem numa fase posterior, fora deste plano)
- **IDE do usuário:** IntelliJ IDEA
- **Idioma do código e das mensagens:** português (nomes de classes/métodos conforme o enunciado)

---

## Fase 0 — Estrutura do projeto Maven

```
RobotGame/
├── pom.xml
├── .gitignore            (target/, .idea/, *.iml, out/)
├── LICENSE
└── src/main/java/robotgame/
    ├── excecao/   MovimentoInvalidoException
    ├── modelo/    Robo, RoboInteligente, Tabuleiro, Obstaculo, Bomba, Rocha
    └── console/   Entrada, VisualizacaoConsole, Main1, Main2, Main3, Main4
```

**Regra de arquitetura:** o pacote `modelo` nunca usa `System.out` nem `Scanner`. Toda leitura/impressão fica em `console`. Isso permite que a fase JavaFX reaproveite `modelo` sem alterações.

### `pom.xml`
- `groupId`: `robotgame`, `artifactId`: `RobotGame`, `version`: `1.0`
- `maven.compiler.release` = `21`, `project.build.sourceEncoding` = `UTF-8`
- Dependência `org.openjfx:javafx-controls` na versão 21.x mais recente estável (ainda não usada pelo código — só deixa a fase JavaFX pronta)
- Plugin `org.openjfx:javafx-maven-plugin` (versão estável mais recente), sem `mainClass` definida por enquanto

**Critério de aceite:** `mvn compile` passa; o projeto abre no IntelliJ como projeto Maven.

---

## Fase 1 — Exceção e modelo

### `excecao.MovimentoInvalidoException`
- **Checked**: `extends Exception` (movimento inválido é evento esperado, com recuperação no `catch`).
- Construtor `MovimentoInvalidoException(String movimentoInvalido)` → mensagem `"Movimento inválido: " + movimentoInvalido`.

### `modelo.Tabuleiro`
- `public static final int TAMANHO = 4` → coordenadas válidas de **0 a 3** nos dois eixos (limite superior bloqueia como parede).
- Atributos: `xAlimento`, `yAlimento` (final), `Obstaculo[TAMANHO][TAMANHO] obstaculos`.
- Construtor `Tabuleiro(int xAlimento, int yAlimento)` lança **`IllegalArgumentException`** (unchecked — violação de pré-condição) se:
  - posição fora de 0–3, ou
  - posição = (0,0) (início dos robôs).
- `public static boolean dentroDosLimites(int x, int y)`
- `getXAlimento()`, `getYAlimento()`
- `public boolean alimentoEncontradoPor(Robo robo)` → delega para `robo.encontrouAlimento(xAlimento, yAlimento)`
- `public void adicionarObstaculo(Obstaculo o, int x, int y)` → `IllegalArgumentException` se fora dos limites, em (0,0), na posição do alimento, ou já ocupada.
- `public Obstaculo getObstaculo(int x, int y)` (retorna `null` se vazio)
- `void removerObstaculo(int x, int y)` — **package-private** (só a `Bomba` usa).

### `modelo.Robo`
Atributos privados: `x`, `y`, `xAnterior`, `yAnterior`, `cor` (final), `ativo`, `movimentosValidos`, `movimentosInvalidos`.

- Construtor `Robo(String cor)`: posição (0,0), anterior (0,0), `ativo = true`.
- Getters: `getX`, `getY`, `getCor`, `isAtivo`, `getMovimentosValidos`, `getMovimentosInvalidos`, `getTotalMovimentos` (= válidos + inválidos).
- `setX(int)` / `setY(int)` **validam** (0 a `Tabuleiro.TAMANHO - 1`) e lançam `MovimentoInvalidoException("x=" + valor)` / `("y=" + valor)`. A validação mora nos setters.
- `mover(String direcao) throws MovimentoInvalidoException`:
  1. Se `direcao` não for `"up"`, `"down"`, `"right"` ou `"left"` → incrementa inválidos e lança `MovimentoInvalidoException(direcao)` **antes** do `try`.
  2. Guarda `xAntes`/`yAntes`.
  3. Dentro de `try`, `switch` chama o setter correspondente (`up` = y+1, `down` = y−1, `right` = x+1, `left` = x−1).
  4. No `catch` da exceção do setter → incrementa inválidos e **relança** `new MovimentoInvalidoException(direcao)` (a mensagem passa a informar o comando, não `"y=4"`).
  5. Em caso de sucesso: `xAnterior = xAntes`, `yAnterior = yAntes`, incrementa válidos.
- `mover(int direcao) throws MovimentoInvalidoException` — sobrecarga que **delega** para `mover(String)`: 1→up, 2→down, 3→right, 4→left; `default` incrementa inválidos e lança `MovimentoInvalidoException(String.valueOf(direcao))`.
- `boolean encontrouAlimento(int xAlimento, int yAlimento)` — query pura (CQS), sem efeito colateral.
- `void voltarPosicaoAnterior()` e `void explodir()` (`ativo = false`) — **package-private** (só obstáculos usam).

### `modelo.RoboInteligente extends Robo`
- Sobrescreve **`mover(String)`** (como `mover(int)` delega para `mover(String)`, o polimorfismo cobre as duas sobrecargas).
- Lógica: tenta `super.mover(direcao)`; se lançar, registra a direção como tentada e sorteia (via `Random`) entre as direções **ainda não tentadas**, repetindo até um movimento válido. Nunca repete uma direção que falhou.
- Se as 4 falharem (impossível num 4x4, mas protege o contrato), relança a última exceção.
- As tentativas internas contam como movimentos inválidos (já acontece naturalmente via `super.mover`).

### `modelo.Obstaculo` (abstrata)
- `private final int id`, gerado automaticamente por contador estático (`geradorId`, começando em 1).
- `getId()`
- `public abstract void bater(Robo robo, Tabuleiro tabuleiro)`
- `public abstract String getNome()` e `public abstract char getSimbolo()` — usados na exibição e nas mensagens, para que **nenhum código use `instanceof`** para identificar o tipo do obstáculo.

### `modelo.Bomba extends Obstaculo`
- `bater`: `robo.explodir()` e `tabuleiro.removerObstaculo(robo.getX(), robo.getY())` (a bomba some do tabuleiro).
- Nome `"Bomba"`, símbolo `'B'`.

### `modelo.Rocha extends Obstaculo`
- `bater`: `robo.voltarPosicaoAnterior()`.
- Nome `"Rocha"`, símbolo `'P'`.

**Critério de aceite da fase:** `mvn compile` passa; nenhum arquivo de `modelo` importa `Scanner` nem usa `System.out`.

---

## Fase 2 — Utilitários de console

### `console.Entrada` (métodos estáticos)
- `lerInt(Scanner sc, String prompt, int min, int max)`: lê com `nextLine()`, faz `Integer.parseInt`, repete o pedido se não for número ou estiver fora da faixa. **Nunca usar `nextInt()`** (evita o bug do `\n` no buffer e o crash com letras).
- `lerTexto(Scanner sc, String prompt)`: repete se vazio.
- `lerTabuleiro(Scanner sc)`: lê x e y do alimento (0–3) e tenta `new Tabuleiro(x, y)`; em `IllegalArgumentException`, mostra a mensagem e pede de novo.
- Um único `Scanner` criado no `main` e passado por parâmetro; nunca chamar `close()` dentro de métodos reutilizáveis.

### `console.VisualizacaoConsole` (métodos estáticos)
- `exibir(Tabuleiro t, Robo... robos)`: desenha a matriz 4x4 em eixo cartesiano (**y = 3 no topo, y = 0 embaixo**), com cabeçalho dos números de coluna e de linha.
  - Conteúdo de cada célula, por prioridade: número(s) dos robôs **ativos** naquela posição (índice + 1; ex.: `1`, `2`, `12` se os dois estiverem juntos) → `A` (alimento) → símbolo do obstáculo → `.`.
  - Robôs coloridos com códigos ANSI conforme a cor digitada (vermelho, verde, amarelo, azul, roxo/magenta, ciano; outras cores sem cor). O alinhamento das colunas deve ser calculado sobre o texto **sem** os códigos ANSI.
  - Robô explodido não aparece na matriz.
  - Legenda: cada robô com número, tipo (`getClass().getSimpleName()`), cor e `(explodiu)` se inativo; e `A = Alimento   B = Bomba   P = Rocha`.
- `pausar(long ms)`: `Thread.sleep`, tratando `InterruptedException` com `Thread.currentThread().interrupt()`.
- `mostrarEstatisticas(Robo r)`: `"Robô <cor> (<tipo>): N movimentos — V válidos, I inválidos"`.

---

## Fase 3 — Classes Main (uma por item do enunciado)

Padrão de mensagem por jogada (Main2, Main3, Main4): `Robô <cor> tenta <direção>: agora em (x,y)` ou `Robô <cor> tenta <direção>: Movimento inválido: <direção>`. Sorteio: `random.nextInt(4) + 1` e chamada a `mover(int)`. Pausa de **700 ms** entre jogadas. Os robôs jogam **alternadamente**.

### `Main1` — Item 1: um robô, movimentos do usuário
1. Lê a cor do robô e o tabuleiro (`Entrada.lerTabuleiro`).
2. Pergunta o formato da sessão: (1) palavras em inglês ou (2) números 1–4. O formato vale para a sessão inteira.
3. Laço até `tabuleiro.alimentoEncontradoPor(robo)`: exibe a matriz, lê o comando com `nextLine()` e chama `mover(String)` ou `mover(Integer.parseInt(...))`; após sucesso mostra a nova posição.
   - `catch (MovimentoInvalidoException e)` → imprime `e.getMessage()` e continua.
   - `catch (NumberFormatException e)` (modo número) → `"Digite apenas o número do movimento."` e continua.
4. Exibe a matriz final, mensagem de vitória e estatísticas.

### `Main2` — Item 2: dois robôs aleatórios
- Dois `Robo` (cores lidas do usuário) + tabuleiro.
- Movimento inválido: o robô **perde a vez**.
- Termina quando **um** encontra o alimento. Mostra quem achou e as estatísticas (válidos/inválidos) dos dois.

### `Main3` — Item 3: normal × inteligente
- `robos[0] = new Robo(...)`, `robos[1] = new RoboInteligente(...)`.
- Termina quando **ambos** encontram o alimento. Um robô que já chegou deixa de se mover (a vez passa direto ao outro); imprimir `>>> Robô <cor> encontrou o alimento!` no momento em que cada um chega.
- Ao final, estatísticas dos dois (número de movimentos até achar).

### `Main4` — Item 4: obstáculos
1. Robô normal + inteligente, tabuleiro.
2. Lê a quantidade de bombas (0 a 14) e de rochas (0 a 14 − bombas) — 14 = 16 células − (0,0) − alimento.
3. Para cada obstáculo, lê x e y e chama `tabuleiro.adicionarObstaculo`; em `IllegalArgumentException`, mostra a mensagem e pede de novo.
4. Laço, pulando robôs inativos, enquanto: nenhum vencedor **e** pelo menos um robô ativo **e** rodadas < `MAX_RODADAS = 1000` (evita laço infinito se rochas cercarem o alimento).
5. Após movimento **válido**, consulta `tabuleiro.getObstaculo(x, y)`; se houver: imprime `Bateu em <nome> #<id>!`, chama `bater(...)` e depois imprime `Voltou para (x,y)` se ainda ativo ou `O robô <cor> explodiu!` se não.
6. Robô ativo na posição do alimento → vencedor.
7. Resultado final: vencedor, ou `"Os dois robôs explodiram!"`, ou mensagem de limite de rodadas. Estatísticas dos dois.

---

## Fase 4 — Testes de aceite (executar e conferir a saída)

| # | Classe | Cenário | Esperado |
|---|---|---|---|
| 1 | Main1 | Alimento `9`, depois (0,0) | Pede de novo nos dois casos |
| 2 | Main1 | Modo palavras, robô em (0,0): `left` | `Movimento inválido: left` |
| 3 | Main1 | Modo palavras: `hp` | `Movimento inválido: hp` |
| 4 | Main1 | Modo números: `abc`, depois `7`, depois `2` | Mensagem de número; `Movimento inválido: 7`; `Movimento inválido: down` |
| 5 | Main1 | Alimento (1,1): `right`, `up` | Vitória; estatísticas corretas |
| 6 | Main2 | Execução completa | Termina no primeiro que acha; contadores coerentes |
| 7 | Main3 | Execução completa | Só termina quando os dois chegam; inteligente registra tentativas internas como inválidas |
| 8 | Main4 | Bomba em (0,0) ou no alimento | Rejeitada com mensagem, pede de novo |
| 9 | Main4 | Bombas em (1,0) e (0,1) | Os dois explodem; bombas somem da matriz |
| 10 | Main4 | Alimento (3,3), rochas em (2,3) e (3,2) | Para no limite de 1000 rodadas com a mensagem correspondente |
| 11 | Main4 | Rocha no caminho | Robô volta à posição anterior |

Para testar sem esperar, pode-se reduzir a pausa **temporariamente** — o código entregue deve ficar com 700 ms.

---

## Fora do escopo deste plano

- **Interface JavaFX** (o "Desafio" do enunciado): será planejada separadamente depois que o console estiver testado. Ela deve consumir o pacote `modelo` sem modificá-lo.
- Testes JUnit, persistência, menu único agregando os quatro Mains.
