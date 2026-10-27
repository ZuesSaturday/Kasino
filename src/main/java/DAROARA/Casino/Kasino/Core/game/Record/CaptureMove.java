package DAROARA.Casino.Kasino.Core.game.Record;

import DAROARA.Casino.Kasino.Core.KasinoCore.Build;
import DAROARA.Casino.Kasino.Core.KasinoCore.Card;

import java.util.List;

public record CaptureMove(
        Card played,
        List<Card> loose,
        Build build
) implements Move {
    public CaptureMove {
        loose = List.copyOf(loose);
    }
}
