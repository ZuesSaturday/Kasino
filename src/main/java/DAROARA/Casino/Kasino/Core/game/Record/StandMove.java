package DAROARA.Casino.Kasino.Core.game.Record;

import DAROARA.Casino.Kasino.Core.KasinoCore.Build;
import DAROARA.Casino.Kasino.Core.KasinoCore.Card;

public record StandMove(
        Card played,
        Build target
        ) implements Move {

}
