package robotgame.modelo;

import java.util.function.Consumer;

/** Executa cada personagem em um ciclo próprio, sem fila de turnos. */
public class SimulacaoPoderConcorrente {

    public record Atualizacao(Simulacao.Passo passo, boolean houveEliminacao,
                              boolean encontrouAlimento, String resultado) {
    }

    private final SimulacaoPoder simulacao;
    private final Tabuleiro tabuleiro;
    private final Robo[] robos;
    private final Consumer<Atualizacao> ouvinte;
    private final Object controlePausa = new Object();
    private Thread[] threads;
    private volatile boolean ativo;
    private volatile boolean pausado;
    private volatile long intervaloMs;

    public SimulacaoPoderConcorrente(SimulacaoPoder simulacao, Tabuleiro tabuleiro, Robo[] robos,
                                    long intervaloMs, Consumer<Atualizacao> ouvinte) {
        this.simulacao = simulacao;
        this.tabuleiro = tabuleiro;
        this.robos = robos;
        this.intervaloMs = intervaloMs;
        this.ouvinte = ouvinte;
    }

    public void iniciar() {
        if (threads != null) {
            return;
        }
        ativo = true;
        threads = new Thread[robos.length + 2];
        for (int i = 0; i < robos.length; i++) {
            Robo robo = robos[i];
            threads[i] = criarThread("pacman-" + (i + 1), () -> moverPacman(robo), 1);
        }
        threads[robos.length] = criarThread("fantasma-inteligente", this::moverFantasma, 1);
        // Poder e limite de rodada usam relógio próprio; ele não determina a vez dos personagens.
        threads[robos.length + 1] = criarThread("relogio-partida", this::passarTempo, robos.length + 1);
        for (Thread thread : threads) {
            thread.start();
        }
    }

    public void pausar() {
        synchronized (simulacao) {
            pausado = true;
        }
    }

    public void retomar() {
        synchronized (controlePausa) {
            pausado = false;
            controlePausa.notifyAll();
        }
    }

    public void mudarIntervalo(long intervaloMs) {
        this.intervaloMs = intervaloMs;
    }

    public void parar() {
        synchronized (simulacao) {
            ativo = false;
        }
        synchronized (controlePausa) {
            controlePausa.notifyAll();
        }
        if (threads != null) {
            for (Thread thread : threads) {
                thread.interrupt();
            }
        }
    }

    private Thread criarThread(String nome, Runnable movimento, int fatorIntervalo) {
        Thread thread = new Thread(() -> {
            try {
                while (ativo) {
                    Thread.sleep(intervaloMs * fatorIntervalo);
                    synchronized (controlePausa) {
                        while (ativo && pausado) {
                            controlePausa.wait();
                        }
                    }
                    if (ativo && !movimentoAtivo(movimento)) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, nome);
        thread.setDaemon(true);
        return thread;
    }

    private boolean movimentoAtivo(Runnable movimento) {
        synchronized (simulacao) {
            if (!ativo || simulacao.isTerminada()) {
                return false;
            }
            if (pausado) {
                return true;
            }
            movimento.run();
            return ativo && !simulacao.isTerminada();
        }
    }

    private void moverPacman(Robo robo) {
        int ativosAntes = contarAtivos();
        Simulacao.Passo passo = simulacao.moverPacman(robo);
        if (passo != null) {
            ouvinte.accept(new Atualizacao(passo, contarAtivos() < ativosAntes,
                    tabuleiro.alimentoEncontradoPor(robo), simulacao.getResultado()));
        }
    }

    private void moverFantasma() {
        int ativosAntes = contarAtivos();
        Simulacao.Passo passo = simulacao.moverFantasma();
        if (passo != null) {
            ouvinte.accept(new Atualizacao(passo, contarAtivos() < ativosAntes,
                    false, simulacao.getResultado()));
        }
    }

    private void passarTempo() {
        simulacao.passarTempo();
        if (simulacao.isTerminada()) {
            ouvinte.accept(new Atualizacao(null, false, false, simulacao.getResultado()));
        }
    }

    private int contarAtivos() {
        int ativos = 0;
        for (Robo robo : robos) {
            if (robo.isAtivo()) {
                ativos++;
            }
        }
        return ativos;
    }

}
