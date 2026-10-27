package DAROARA.Casino.Kasino.Core.KasinoCore;

import java.util.List;

public class BuildLayer {
    private final List<Card> setCards;

    public BuildLayer(List<Card> setCards) {
        this.setCards = setCards;
    }

    public List<Card> getSetCards() {
        return setCards;
    }
}
