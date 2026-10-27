package DAROARA.Casino.Kasino.Core.AI;

import DAROARA.Casino.Kasino.Core.KasinoCore.Build;
import DAROARA.Casino.Kasino.Core.KasinoCore.Card;
import DAROARA.Casino.Kasino.Core.KasinoCore.Player;
import DAROARA.Casino.Kasino.Core.game.Game;
import DAROARA.Casino.Kasino.Core.game.Record.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CasinoAI {

    // Limits the number of subsets and capture combinations to examine.
    private static final int MAX_SUBSETS = 60_000;
    private static final int MAX_CAPTURE_GROUPS = 60;

    /**
     * Selects the highest-scoring legal move for the computer.
     */
    public static Move chooseMove(Game game, Random random) {

        Move bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        // Generate possible moves and evaluate each legal option.
        for (Move move : generateMoves(game)) {

            if (game.Validate(move) != null) {
                continue; // Ignore moves that violate the game rules.
            }

            // Small random bonus prevents the AI from always making
            // exactly the same choice when scores are close.
            double score = scoreMove(game, move)
                    + random.nextDouble() * 0.3;

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        if (bestMove == null) {
            throw new IllegalStateException(
                    "The computer found no legal move."
            );
        }

        return bestMove;
    }

    /**
     * Assigns a strategic score to a move.
     * Higher scores indicate moves the AI prefers.
     */
    private static double scoreMove(Game game, Move move) {

        // Capturing collects cards from the table and possibly a build.
        if (move instanceof CaptureMove capture) {

            List<Card> capturedCards = new ArrayList<>();
            capturedCards.add(capture.played());
            capturedCards.addAll(capture.loose());

            if (capture.build() != null) {
                capturedCards.addAll(capture.build().getCards());
            }

            // Prefer captures, especially when valuable cards are taken.
            return 3 + capturedCards.stream()
                    .mapToDouble(card -> 1 + 3.0 * card.Points())
                    .sum();
        }

        // Raising a build increases its value while preserving the build.
        if (move instanceof RaiseMove raise) {
            return 2.5 + raise.target().getCards().stream()
                    .mapToDouble(card -> 3.0 * card.Points())
                    .sum()
                    + 0.4 * raise.target().getCards().size();
        }

        // Creating a new build can prepare a future capture.
        if (move instanceof BuildMove build) {
            return 1.5 + 0.4 * (build.loose().size() + 1);
        }

        // Adding a set contributes cards toward an existing build.
        if (move instanceof AddSetMove addSet) {
            return 1.5 + 0.4 * addSet.loose().size();
        }

        // Standing on a target combines the played card with that target.
        if (move instanceof StandMove stand) {
            return 1.5 + 0.4 * (
                    stand.played().Points()
                            + stand.target().getCards().size()
                            + 1
            );
        }

        // Trailing discards a card onto the table.
        // Penalize discarding cards that are worth points.
        return -4 - 3.0 * move.played().Points();
    }

    /**
     * Generates possible moves using the current player's hand
     * and the cards on the floor.
     */
    private static List<Move> generateMoves(Game game) {

        Player player = game.getCurrent();
        List<Card> floor = new ArrayList<>(game.getFloor());

        // Find groups of floor cards whose values add up to 10 or less.
        List<Subset> subsets = findSubsets(floor);
        List<Move> moves = new ArrayList<>();

        for (Card card : player.getHand()) {

            // The player can always attempt to trail a card.
            moves.add(new TrailMove(card));

            // Find floor groups matching the played card's value.
            List<List<Card>> matchingGroups = subsets.stream()
                    .filter(set -> set.Sum() == card.Value())
                    .map(Subset::Cards)
                    .limit(MAX_CAPTURE_GROUPS)
                    .toList();

            // Find a build that can be captured using this card.
            Build matchingBuild = game.getBuilds().stream()
                    .filter(build -> build.getValue() == card.Value())
                    .findFirst()
                    .orElse(null);

            // Generate captures from matching groups and combinations
            // of non-overlapping groups.
            for (List<Card> loose : combineCaptureGroups(matchingGroups)) {
                if (!loose.isEmpty() || matchingBuild != null) {
                    moves.add(new CaptureMove(card, loose, matchingBuild));
                }
            }

            // A new build can be created if the player has a card
            // in their hand matching the resulting target value.
            if (player.getActiveBuild() == null) {
                for (Subset subset : subsets) {

                    int target = card.Value() + subset.Sum();

                    if (target > 10) {
                        continue;
                    }

                    boolean hasTargetCard = player.getHand().stream()
                            .anyMatch(other ->
                                    other != card && other.Value() == target
                            );

                    if (hasTargetCard) {
                        moves.add(new BuildMove(
                                card, subset.Cards(), target
                        ));
                    }
                }
            }

            // Consider raising existing builds or adding cards to them.
            for (Build build : game.getBuilds()) {

                moves.add(new RaiseMove(card, build));

                int needed = build.getValue() - card.Value();

                if (needed <= 0) {
                    continue;
                }

                // Find floor groups that complete the build's value.
                for (Subset subset : subsets) {
                    if (subset.Sum() == needed) {
                        moves.add(new AddSetMove(
                                card, subset.Cards(), build
                        ));
                    }
                }
            }
        }

        return moves;
    }

    /**
     * Finds combinations of cards whose total value does not exceed 10.
     */
    private static List<Subset> findSubsets(List<Card> cards) {

        List<List<Card>> cardGroups = new ArrayList<>();

        collectSubsets(
                cards, 0, 10, new ArrayList<>(), cardGroups
        );

        List<Subset> subsets = new ArrayList<>();

        // Store each combination along with its total value.
        for (List<Card> group : cardGroups) {
            int sum = group.stream()
                    .mapToInt(Card::Value)
                    .sum();

            subsets.add(new Subset(group, sum));
        }

        return subsets;
    }

    /**
     * Recursively generates card combinations without exceeding
     * the specified remaining value or the subset limit.
     */
    private static void collectSubsets(
            List<Card> cards,
            int start,
            int remaining,
            List<Card> current,
            List<List<Card>> results) {

        if (results.size() >= MAX_SUBSETS) {
            return;
        }

        for (int i = start;
             i < cards.size() && results.size() < MAX_SUBSETS;
             i++) {

            Card card = cards.get(i);

            // Skip cards that would exceed the allowed total.
            if (card.Value() > remaining) {
                continue;
            }

            current.add(card);
            results.add(new ArrayList<>(current));

            // Continue searching with the remaining value.
            collectSubsets(
                    cards,
                    i + 1,
                    remaining - card.Value(),
                    current,
                    results
            );

            // Backtrack so the next combination can be explored.
            current.remove(current.size() - 1);
        }
    }

    /**
     * Creates possible capture groups by combining groups that
     * do not share any cards.
     */
    private static List<List<Card>> combineCaptureGroups(
            List<List<Card>> groups) {

        List<List<Card>> results = new ArrayList<>();

        // An empty group represents capturing a matching build
        // without taking any loose floor cards.
        results.add(new ArrayList<>());

        for (int i = 0; i < groups.size(); i++) {

            // Include each individual matching group.
            results.add(new ArrayList<>(groups.get(i)));

            // Try combining it with every later non-overlapping group.
            for (int j = i + 1; j < groups.size(); j++) {

                if (Collections.disjoint(groups.get(i), groups.get(j))) {

                    List<Card> combined =
                            new ArrayList<>(groups.get(i));
                    combined.addAll(groups.get(j));
                    results.add(combined);
                }
            }
        }
        return results;
    }
}