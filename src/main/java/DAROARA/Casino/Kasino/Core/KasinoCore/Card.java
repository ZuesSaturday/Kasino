package DAROARA.Casino.Kasino.Core.KasinoCore;

import static DAROARA.Casino.Kasino.Core.KasinoCore.Suit.*;

public record Card(int Value, Suit suit) {

    @Override
    public int Value() {
        return Value;
    }

    @Override
    public Suit suit() {
        return suit;
    }

    public int Points() {
        if (this.Value == 1) return 1;
        if (this.Value == 2&&this.suit == Spades) return 1;
        if (this.Value == 10 && this.suit == Diamonds) return 2;
        return  0;
    }
    public Object label() {
        String rank = this.Value() == 1 ? "A": String.valueOf(this.Value());
        String suit = "";
        switch (this.suit) {
            case Clubs -> suit = "♣";
            case Diamonds -> suit ="♦";
            case Hearts -> suit ="♥";
            case Spades ->  suit ="♠";
        }
        return rank + suit;
    }
}
