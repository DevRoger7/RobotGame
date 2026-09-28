package robotgame.modelo;

public enum ModoJogo {

    MANUAL("Controle manual (1 robô)",
            "Você controla o robô com as setas do teclado até encontrar o alimento.",
            false, false),
    CORRIDA("Corrida aleatória (2 robôs)",
            "Dois robôs se movem aleatoriamente, um de cada vez. Vence quem achar o alimento primeiro.",
            false, false),
    NORMAL_X_INTELIGENTE("Robô normal x robô inteligente",
            "O robô inteligente não repete movimentos inválidos. A partida segue até os dois acharem o alimento.",
            true, false),
    COM_OBSTACULOS("Normal x inteligente com obstáculos",
            "Você posiciona fantasmas e rochas no tabuleiro. Fantasma pega o pacman; rocha o faz voltar.",
            true, true);

    private final String nome;
    private final String explicacao;
    private final boolean comRoboInteligente;
    private final boolean comObstaculos;

    ModoJogo(String nome, String explicacao, boolean comRoboInteligente, boolean comObstaculos) {
        this.nome = nome;
        this.explicacao = explicacao;
        this.comRoboInteligente = comRoboInteligente;
        this.comObstaculos = comObstaculos;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public boolean isManual() {
        return this == MANUAL;
    }

    public int getQuantidadeRobos() {
        return isManual() ? 1 : 2;
    }

    public boolean isComRoboInteligente() {
        return comRoboInteligente;
    }

    public boolean isComObstaculos() {
        return comObstaculos;
    }

    @Override
    public String toString() {
        return nome;
    }
}
