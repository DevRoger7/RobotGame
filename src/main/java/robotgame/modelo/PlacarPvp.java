package robotgame.modelo;

// Placar do PvP entre partidas seguidas. Empate não conta para a série.
public class PlacarPvp {

    public enum Formato {
        LIVRE("LIVRE", 0),
        MELHOR_DE_3("MELHOR DE 3", 3),
        MELHOR_DE_5("MELHOR DE 5", 5);

        private final String nome;
        private final int partidas;

        Formato(String nome, int partidas) {
            this.nome = nome;
            this.partidas = partidas;
        }

        public boolean isLivre() {
            return this == LIVRE;
        }

        // Maioria das partidas: 2 na melhor de 3, 3 na melhor de 5. Não se aplica ao LIVRE.
        public int vitoriasParaVencer() {
            return partidas / 2 + 1;
        }

        @Override
        public String toString() {
            return nome;
        }
    }

    private final Formato formato;
    private final int[] vitorias = new int[2]; // 0 = Jogador 1, 1 = Jogador 2
    private int empates;

    public PlacarPvp(Formato formato) {
        this.formato = formato;
    }

    public Formato getFormato() {
        return formato;
    }

    public int getVitorias(int jogador) {
        return vitorias[jogador];
    }

    public int getEmpates() {
        return empates;
    }

    public int getPartidaAtual() {
        return vitorias[0] + vitorias[1] + empates + 1;
    }

    public void registrarVitoria(int jogador) {
        garantirSerieAberta();
        vitorias[jogador]++;
    }

    public void registrarEmpate() {
        garantirSerieAberta();
        empates++;
    }

    public boolean isDecidida() {
        if (formato.isLivre()) {
            return false;
        }
        int necessarias = formato.vitoriasParaVencer();
        return vitorias[0] >= necessarias || vitorias[1] >= necessarias;
    }

    // LIVRE: "2"; em série: "1/2" (vitórias/necessárias).
    public String textoVitorias(int jogador) {
        if (formato.isLivre()) {
            return String.valueOf(vitorias[jogador]);
        }
        return vitorias[jogador] + "/" + formato.vitoriasParaVencer();
    }

    private void garantirSerieAberta() {
        if (isDecidida()) {
            throw new IllegalStateException("A série já foi decidida: comece uma nova.");
        }
    }
}
