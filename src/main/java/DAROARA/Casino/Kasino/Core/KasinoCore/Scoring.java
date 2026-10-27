package DAROARA.Casino.Kasino.Core.KasinoCore;

import java.util.List;

public class Scoring {
    public static int CardPoints(Side side) {
        int points = 0;
        for (Card card: side.Home) {
            points += card.Points();
        }
        return points;
    }

    public static int Total(Side side, List<Side> allSides, boolean packPoints, boolean fiveSpadePoints) {
        int total = CardPoints(side);
        if (packPoints) {
            int mine = side.Home.size();
            if (allSides.stream().filter(s -> s != side).allMatch(s -> s.Home.size() < mine)) total += 5;
        }
        if (fiveSpadePoints) {
            total += (int) side.Home.stream().filter(c -> c.suit() == Suit.Spades).count() / 5;
        }
        return total;
    }

}
