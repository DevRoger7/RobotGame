package robotgame.excecao;

public class MovimentoInvalidoException extends Exception {

    public MovimentoInvalidoException(String movimentoInvalido) {
        super("Movimento inválido: " + movimentoInvalido);
    }
}
