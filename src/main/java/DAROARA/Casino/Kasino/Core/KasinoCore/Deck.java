 package DAROARA.Casino.Kasino.Core.KasinoCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck {
    private List<Card> cards = new ArrayList<>();

    public Deck() {
        for (Suit suit : Suit.values()) {
            for (int value = 1; value <= 10; value++) {
                cards.add(new Card(value, suit));
            }
        }
    }

    public Deck(Deck deck) {
        this.cards = deck.cards;
    }

    public Deck shuffleDeck(Random random) {
        if (cards.size() != 40) {
            throw new IllegalStateException("Deck must contain exactly 40 cards");
        }
        Collections.shuffle(cards, random);
        return this;
    }
    public Card drawCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("The deck is empty!");
        }
        return cards.remove(cards.size() - 1);
    }

    public int remainingCards() {
        return cards.size();
    }
}
