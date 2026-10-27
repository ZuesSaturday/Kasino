package DAROARA.Casino.Kasino.Core.game.Record;

import DAROARA.Casino.Kasino.Core.KasinoCore.Build;
import DAROARA.Casino.Kasino.Core.KasinoCore.Card;

public record RaiseMove(Card played, Build target) implements Move {
}
