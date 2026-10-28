package robotgame.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import robotgame.excecao.MovimentoInvalidoException;

public class SimulacaoPoder {

    public static final int MAX_RODADAS = 1000;

    private final Tabuleiro tabuleiro;
    private final Robo[] robos;
    private final Random random = new Random();
    private final GeradorPoder gerador = new GeradorPoder(random);
    private Consumer<EventoPartida> ouvinte = evento -> {};

    private int vez;
    private int rodadas;
    private String resultado;
    // Primeiro pacman a chegar à fruta: é o vencedor.
    private Robo primeiroNaFruta;

    public SimulacaoPoder(Tabuleiro tabuleiro, Robo... robos) {
        this.tabuleiro = tabuleiro;
        this.robos = robos;
    }

    public synchronized void setOuvinte(Consumer<EventoPartida> ouvinte) {
        this.ouvinte = ouvinte;
    }

    public synchronized boolean isTerminada() {
        return resultado != null;
    }

    public synchronized String getResultado() {
        return resultado;
    }

    // Cada personagem chama seu próprio passo. O monitor protege decisão, movimento e colisão.
    public synchronized Simulacao.Passo moverPacman(Robo robo) {
        if (isTerminada() || !podeJogarPacman(robo)) {
            return null;
        }
        Simulacao.Passo passo = turnoDoPacman(robo);
        verificarFim();
        return passo;
    }

    public synchronized Simulacao.Passo moverFantasma() {
        if (isTerminada() || tabuleiro.getFantasmaInteligente() == null) {
            return null;
        }
        Simulacao.Passo passo = turnoDoFantasma();
        verificarFim();
        return passo;
    }

    // Relógio da partida: mantém duração da bolinha e limite de tempo sem ordenar movimentos.
    public synchronized void passarTempo() {
        if (isTerminada()) {
            return;
        }
        rodadas++;
        for (EventoPartida evento : gerador.fimDaRodada(tabuleiro, robos)) {
            ouvinte.accept(evento);
        }
        verificarFim();
    }

    // No turno do fantasma, Passo.robo() é null.
    public synchronized Simulacao.Passo proximoPasso() {
        if (isTerminada()) {
            throw new IllegalStateException("A simulação já terminou.");
        }
        // Só acontece se o ator 0 já começar sem poder jogar (ex.: robos[0] eliminado antes da partida).
        while (!podeJogar(vez)) {
            avancarVez();
            if (isTerminada()) {
                throw new IllegalStateException("A simulação já terminou.");
            }
        }

        int ator = vez;
        Simulacao.Passo passo = ator == robos.length ? turnoDoFantasma() : turnoDoPacman(robos[ator]);
        verificarFim();
        // Deixa a vez sempre num ator que pode jogar. Não entra em loop: se nenhum pacman está ativo,
        // verificarFim já terminou a partida; se algum está, ele pode jogar.
        do {
            avancarVez();
        } while (!isTerminada() && !podeJogar(vez));
        return passo;
    }

    // Passa a vez para o próximo ator; quando volta para o ator 0, fecha a rodada.
    private void avancarVez() {
        vez = (vez + 1) % (robos.length + 1);
        if (vez == 0 && !isTerminada()) {
            rodadas++;
            for (EventoPartida evento : gerador.fimDaRodada(tabuleiro, robos)) {
                ouvinte.accept(evento);
            }
            verificarFim();
        }
    }

    private boolean podeJogar(int ator) {
        if (ator == robos.length) {
            return tabuleiro.getFantasmaInteligente() != null;
        }
        // Quem já chegou à fruta para de jogar e espera os outros.
        return podeJogarPacman(robos[ator]);
    }

    private boolean podeJogarPacman(Robo robo) {
        boolean pertenceAPartida = false;
        for (Robo participante : robos) {
            if (participante == robo) {
                pertenceAPartida = true;
                break;
            }
        }
        return pertenceAPartida && robo.isAtivo() && !tabuleiro.alimentoEncontradoPor(robo);
    }

    private Simulacao.Passo turnoDoPacman(Robo robo) {
        boolean comecouComPoder = robo.isComPoder();
        int xAntes = robo.getX();
        int yAntes = robo.getY();
        String tentativa = robo.decidirDirecao(tabuleiro, random);
        String direcao = tentativa;
        StringBuilder mensagem = new StringBuilder("Pacman " + robo.getCor() + " tenta " + tentativa);

        try {
            robo.mover(tentativa);
            direcao = direcaoDoDeslocamento(xAntes, yAntes, robo.getX(), robo.getY());
            mensagem.append(": agora em ").append(posicao(robo));
            resolverCasa(robo, direcao, mensagem);
        } catch (MovimentoInvalidoException e) {
            mensagem.append(": ").append(e.getMessage());
        }

        if (comecouComPoder && robo.isAtivo() && robo.isComPoder()) {
            robo.gastarTurnoDePoder();
            if (!robo.isComPoder()) {
                mensagem.append(". O poder acabou");
                publicar(EventoPartida.Tipo.PODER_ACABOU, robo, direcao, null);
            }
        }
        return new Simulacao.Passo(robo, direcao, mensagem.toString());
    }

    private void resolverCasa(Robo robo, String direcao, StringBuilder mensagem) {
        int x = robo.getX();
        int y = robo.getY();
        FantasmaInteligente fantasma = tabuleiro.getFantasmaInteligente();
        Obstaculo obstaculo = tabuleiro.getObstaculo(x, y);
        ItemPoder item = tabuleiro.getItemPoder();

        if (fantasma != null && fantasma.getX() == x && fantasma.getY() == y) {
            if (robo.isComPoder()) {
                tabuleiro.removerFantasmaInteligente();
                robo.perderPoder();
                mensagem.append(". Comeu o fantasma inteligente");
                publicar(EventoPartida.Tipo.COMEU_FANTASMA, robo, direcao, null);
                // O fantasma esperto anda e pode estar parado em cima da fruta.
                if (tabuleiro.alimentoEncontradoPor(robo)) {
                    publicar(EventoPartida.Tipo.ACHOU_ALIMENTO, robo, direcao, null);
                }
            } else {
                robo.explodir();
                mensagem.append(". Foi eliminado pelo fantasma inteligente!");
                publicar(EventoPartida.Tipo.ELIMINADO_PELO_FANTASMA, robo, direcao, null);
            }
        } else if (obstaculo instanceof Fantasma && robo.isComPoder()) {
            tabuleiro.removerObstaculo(x, y);
            robo.perderPoder();
            mensagem.append(". Comeu o fantasma #").append(obstaculo.getId());
            publicar(EventoPartida.Tipo.COMEU_FANTASMA_BURRO, robo, direcao, obstaculo);
        } else if (obstaculo != null) {
            mensagem.append(". Bateu em ").append(obstaculo.getNome()).append(" #").append(obstaculo.getId());
            obstaculo.bater(robo, tabuleiro);
            mensagem.append(robo.isAtivo() ? " e voltou para " + posicao(robo) : " e foi eliminado!");
            publicar(robo.isAtivo() ? EventoPartida.Tipo.ROCHA : EventoPartida.Tipo.EXPLODIU, robo, direcao, obstaculo);
            if (tabuleiro.getObstaculo(x, y) == null) {
                ouvinte.accept(new EventoPartida(EventoPartida.Tipo.FANTASMA_SUMIU, robo, direcao, x, y, obstaculo));
            }
        } else if (item != null && item.getX() == x && item.getY() == y) {
            robo.darPoder();
            tabuleiro.removerItemPoder();
            gerador.itemFoiComido();
            mensagem.append(". Pegou o poder!");
            publicar(EventoPartida.Tipo.PEGOU_PODER, robo, direcao, null);
        } else if (tabuleiro.alimentoEncontradoPor(robo)) {
            mensagem.append(" — encontrou o alimento!");
            publicar(EventoPartida.Tipo.ACHOU_ALIMENTO, robo, direcao, null);
        } else {
            publicar(EventoPartida.Tipo.MOVEU, robo, direcao, null);
        }
    }

    private Simulacao.Passo turnoDoFantasma() {
        FantasmaInteligente fantasma = tabuleiro.getFantasmaInteligente();
        String direcao = fantasma.decidirDirecao(tabuleiro, robos);
        fantasma.mover(direcao);
        int x = fantasma.getX();
        int y = fantasma.getY();

        StringBuilder mensagem = new StringBuilder("Fantasma inteligente ");
        mensagem.append(direcao == null ? "fica parado em (" : "vai para " + direcao + ": agora em (")
                .append(x).append(",").append(y).append(")");
        ouvinte.accept(new EventoPartida(EventoPartida.Tipo.FANTASMA_MOVEU, null, direcao, x, y, null));

        List<Robo> naCasa = new ArrayList<>();
        for (Robo r : robos) {
            // Quem já está na fruta fica a salvo do fantasma.
            if (r.isAtivo() && r.getX() == x && r.getY() == y && !tabuleiro.alimentoEncontradoPor(r)) {
                naCasa.add(r);
            }
        }

        Robo comPoder = null;
        for (Robo r : naCasa) {
            if (r.isComPoder()) {
                comPoder = r;
                break;
            }
        }

        if (comPoder != null) {
            tabuleiro.removerFantasmaInteligente();
            comPoder.perderPoder();
            mensagem.append(". Pacman ").append(comPoder.getCor()).append(" comeu o fantasma inteligente");
            publicar(EventoPartida.Tipo.COMEU_FANTASMA, comPoder, direcao, null);
        } else {
            for (Robo r : naCasa) {
                r.explodir();
                mensagem.append(". Pacman ").append(r.getCor()).append(" foi eliminado!");
                publicar(EventoPartida.Tipo.ELIMINADO_PELO_FANTASMA, r, direcao, null);
            }
        }
        return new Simulacao.Passo(null, direcao, mensagem.toString());
    }

    // A partida só acaba quando todos os pacmans vivos chegaram à fruta; vence quem chegou primeiro.
    private void verificarFim() {
        boolean todosVivosNaFruta = true;
        for (Robo r : robos) {
            if (!r.isAtivo()) {
                continue;
            }
            if (tabuleiro.alimentoEncontradoPor(r)) {
                if (primeiroNaFruta == null) {
                    primeiroNaFruta = r;
                }
            } else {
                todosVivosNaFruta = false;
            }
        }
        if (!algumAtivo()) {
            resultado = "Os três pacmans foram eliminados!";
        } else if (todosVivosNaFruta || (rodadas >= MAX_RODADAS && primeiroNaFruta != null)) {
            resultado = "Pacman " + primeiroNaFruta.getCor() + " encontrou o alimento e venceu!";
        } else if (rodadas >= MAX_RODADAS) {
            resultado = "Limite de " + MAX_RODADAS + " rodadas atingido: nenhum pacman encontrou o alimento.";
        }
    }

    private boolean algumAtivo() {
        for (Robo r : robos) {
            if (r.isAtivo()) {
                return true;
            }
        }
        return false;
    }

    private void publicar(EventoPartida.Tipo tipo, Robo robo, String direcao, Obstaculo obstaculo) {
        ouvinte.accept(new EventoPartida(tipo, robo, direcao, robo.getX(), robo.getY(), obstaculo));
    }

    private static String direcaoDoDeslocamento(int xAntes, int yAntes, int x, int y) {
        if (y > yAntes) {
            return "up";
        }
        if (y < yAntes) {
            return "down";
        }
        return x < xAntes ? "left" : "right";
    }

    private static String posicao(Robo robo) {
        return "(" + robo.getX() + "," + robo.getY() + ")";
    }
}
