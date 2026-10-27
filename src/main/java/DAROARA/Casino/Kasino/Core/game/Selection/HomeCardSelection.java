package DAROARA.Casino.Kasino.Core.game.Selection;

import DAROARA.Casino.Kasino.Core.KasinoCore.Card;
import DAROARA.Casino.Kasino.Core.KasinoCore.Player;

public record HomeCardSelection(
        Player owner,
        Card card) implements Selectable {

}