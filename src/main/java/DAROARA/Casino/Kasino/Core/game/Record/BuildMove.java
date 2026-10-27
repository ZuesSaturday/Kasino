package DAROARA.Casino.Kasino.Core.game.Record;

import DAROARA.Casino.Kasino.Core.KasinoCore.Card;

import java.util.List;

public record BuildMove(
        Card played,
        List<Card> loose,
        int target
) implements Move {
    public BuildMove {
        loose = List.copyOf(loose);
    }
}