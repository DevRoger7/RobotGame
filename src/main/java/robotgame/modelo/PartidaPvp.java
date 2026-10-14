package robotgame.modelo;

import java.util.Random;
import java.util.function.Consumer;

import robotgame.excecao.MovimentoInvalidoException;

// Modo PvP: cada jogador anda quando quiser; o tempo corre em rodadas (fantasma, poder e bolinha).
public class PartidaPvp {

    public static final int MAX_RODADAS = 120;

    private final Tabuleiro tabuleiro;
    private final Robo[] robos;
    private final Random random = new Random();
    private final GeradorPoder gerador = new GeradorPoder(random);
    private Consumer<EventoPartida> ouvinte = evento -> {};

    private int rodadas;
    private String resultado;
    private Robo vencedor;

    public PartidaPvp(Tabuleiro tabuleiro, Robo jogador1, Robo jogador2) {
        this.tabuleiro = tabuleiro;
        this.robos = new Robo[] {jogador1, jogador2};
    }

    public void setOuvinte(Consumer<EventoPartida> ouvinte) {
        this.ouvinte = ouvinte;
    }

    public boolean isTerminada() {
        return resultado != null;
    }

    public String getResultado() {
        return resultado;
    }

    // null enquanto a partida não termina ou quando termina empatada.
    public Robo getVencedor() {
        return vencedor;
    }

    public void mover(Robo robo, String direcao) {
        if (isTerminada() || !robo.isAtivo()) {
            return;
        }
        try {
            robo.mover(direcao);
        } catch (MovimentoInvalidoException e) {
            // O próprio Robo publica o evento INVALIDO.
            return;
        }

        int x = robo.getX();
        int y = robo.getY();
        Obstaculo obstaculo = tabuleiro.getObstaculo(x, y);
        ItemPoder item = tabuleiro.getItemPoder();
        if (obstaculo != null) {
            obstaculo.bater(robo, tabuleiro);
            publicar(robo.isAtivo() ? EventoPartida.Tipo.ROCHA : EventoPartida.Tipo.EXPLODIU, robo, direcao, obstaculo);
        } else if (item != null && item.getX() == x && item.getY() == y) {
            robo.darPoder();
            tabuleiro.removerItemPoder();
            gerador.itemFoiComido();
            publicar(EventoPartida.Tipo.PEGOU_PODER, robo, direcao, null);
        } else {
            publicar(EventoPartida.Tipo.MOVEU, robo, direcao, null);
        }

        if (robo.isAtivo()) {
            encontrarFantasma(robo, direcao);
        }
        if (robo.isAtivo()) {
            encontrarRival(robo, direcao);
        }
        verificarFim();
    }

    // Chamado a cada INTERVALO_PVP: o fantasma anda, o poder gasta um turno e a bolinha aparece/some.
    public void passarRodada() {
        if (isTerminada()) {
            return;
        }
        rodadas++;

        FantasmaAleatorio fantasma = tabuleiro.getFantasmaAleatorio();
        if (fantasma != null) {
            String direcao = fantasma.decidirDirecao(tabuleiro, random);
            fantasma.mover(direcao);
            ouvinte.accept(new EventoPartida(EventoPartida.Tipo.FANTASMA_MOVEU, null, direcao,
                    fantasma.getX(), fantasma.getY(), null));
            for (Robo r : robos) {
                if (r.isAtivo()) {
                    encontrarFantasma(r, direcao);
                }
            }
        }

        for (Robo r : robos) {
            if (r.isAtivo() && r.isComPoder()) {
                r.gastarTurnoDePoder();
                if (!r.isComPoder()) {
                    publicar(EventoPartida.Tipo.PODER_ACABOU, r, null, null);
                }
            }
        }

        for (EventoPartida evento : gerador.fimDaRodada(tabuleiro, robos)) {
            ouvinte.accept(evento);
        }
        verificarFim();
    }

    // Quem tem poder come o fantasma e perde o poder; quem não tem é eliminado.
    private void encontrarFantasma(Robo robo, String direcao) {
        FantasmaAleatorio fantasma = tabuleiro.getFantasmaAleatorio();
        if (fantasma == null || fantasma.getX() != robo.getX() || fantasma.getY() != robo.getY()) {
            return;
        }
        if (robo.isComPoder()) {
            tabuleiro.removerFantasmaAleatorio();
            robo.perderPoder();
            publicar(EventoPartida.Tipo.COMEU_FANTASMA, robo, direcao, null);
        } else {
            robo.explodir();
            publicar(EventoPartida.Tipo.ELIMINADO_PELO_FANTASMA, robo, direcao, null);
        }
    }

    // Encostar no rival elimina quem está sem poder; se os dois têm poder, ou nenhum, convivem.
    private void encontrarRival(Robo robo, String direcao) {
        Robo rival = rival(robo);
        if (!rival.isAtivo() || rival.getX() != robo.getX() || rival.getY() != robo.getY()) {
            return;
        }
        if (robo.isComPoder() && !rival.isComPoder()) {
            rival.explodir();
            publicar(EventoPartida.Tipo.PACMAN_ELIMINADO, rival, direcao, null);
        } else if (rival.isComPoder() && !robo.isComPoder()) {
            robo.explodir();
            publicar(EventoPartida.Tipo.PACMAN_ELIMINADO, robo, direcao, null);
        }
    }

    private void verificarFim() {
        if (isTerminada()) {
            return;
        }
        boolean vivo1 = robos[0].isAtivo();
        boolean vivo2 = robos[1].isAtivo();
        if (!vivo1 && !vivo2) {
            resultado = "Os dois jogadores foram eliminados: empate!";
        } else if (vivo1 != vivo2) {
            vencedor = vivo1 ? robos[0] : robos[1];
            resultado = "Jogador " + (vivo1 ? 1 : 2) + " é o último pacman vivo e venceu!";
        } else if (rodadas >= MAX_RODADAS) {
            resultado = "Limite de " + MAX_RODADAS + " rodadas atingido: empate!";
        }
    }

    private Robo rival(Robo robo) {
        return robos[0] == robo ? robos[1] : robos[0];
    }

    private void publicar(EventoPartida.Tipo tipo, Robo robo, String direcao, Obstaculo obstaculo) {
        ouvinte.accept(new EventoPartida(tipo, robo, direcao, robo.getX(), robo.getY(), obstaculo));
    }
}
