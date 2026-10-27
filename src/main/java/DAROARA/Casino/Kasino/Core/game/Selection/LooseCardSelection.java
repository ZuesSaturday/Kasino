package DAROARA.Casino.Kasino.Core.game.Selection;

import DAROARA.Casino.Kasino.Core.KasinoCore.Card;

import java.util.List;

public record LooseCardSelection(List<Card> cards)
        implements Selectable {
}
