package DAROARA.Casino.Kasino.Core.game.Record;

import DAROARA.Casino.Kasino.Core.KasinoCore.Build;
import DAROARA.Casino.Kasino.Core.KasinoCore.Card;

import java.util.List;
public record AddSetMove(
        Card played,
        List<Card> loose,
        Build target
) implements Move {
    public AddSetMove {
        loose = List.copyOf(loose);
    }
}