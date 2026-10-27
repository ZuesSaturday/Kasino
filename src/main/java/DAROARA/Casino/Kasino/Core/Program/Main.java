package DAROARA.Casino.Kasino.Core.Program;

import DAROARA.Casino.Kasino.Core.AI.CasinoAI;
import DAROARA.Casino.Kasino.Core.KasinoCore.*;
import DAROARA.Casino.Kasino.Core.game.Game;
import DAROARA.Casino.Kasino.Core.game.GameMode;
import DAROARA.Casino.Kasino.Core.game.Record.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== KASINO ===");
        GameMode mode = askMode();
        boolean packPoints = askYesNo("Play with pack points (+5 for the most cards in home)?");
        boolean fiveSpadePoints = askYesNo("Play with Spades points (+1 per 5 spade cards in home)?");
        Random rng = new Random();
        Game game = new Game(mode, packPoints,fiveSpadePoints,rng);
        System.out.println("Who plays?  1) I choose each seat   2) Random human/computer seats");

        if (askNumber("Choice", 2) == 2) {
            int humansWanted = askNumber(
                    "How many human players (1-" + game.getPlayers().size() + ")",
                    game.getPlayers().size());

            List<Player> shuffled = new ArrayList<>(game.getPlayers());
            Collections.shuffle(shuffled, rng);

            for (int i = 0; i < shuffled.size(); i++) {
                shuffled.get(i).setComputer(i >= humansWanted);
            }
        } else {
            for (Player player : game.getPlayers()) {
                player.setComputer(askYesNo(
                        "Is " + player.getName() + " a computer player?"));
            }
        }

        if (game.getPlayers().stream().allMatch(Player::isComputer)) {
            game.getPlayers().getFirst().setComputer(false);
            System.out.println("At least one human is needed, so "
                    + game.getPlayers().getFirst().getName() + " is now human.");
        }

        int humans = (int) game.getPlayers().stream()
                .filter(player -> !player.isComputer())
                .count();

        System.out.println("Seats: " + game.getPlayers().stream()
                .map(player -> player.getName() + " = "
                        + (player.isComputer() ? "computer" : "human"))
                .collect(Collectors.joining(", ")));

        System.out.println("Press Enter to start...");
        readLine();

        List<String> recent = new ArrayList<>();
        Player lastHuman = null;

        while (!game.isOver()) {
            Player current = game.getCurrent();

            if (current.isComputer()) {
                Move aiMove = CasinoAI.chooseMove(game, rng);
                game.TryPlay(aiMove);
                recent.add(game.getLastAction());
                continue;
            }

            if (humans > 1 && lastHuman != current) {
                System.out.print("Pass to " + current.getName()
                        + " and press Enter...");
                readLine();
                tryClear();
                lastHuman = current;
            }

            showTurn(game, recent);
            System.out.print(current.getName() + "> ");

            String line = readLine();
            if (line == null) return;

            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("q")) return;
            if (line.equals("h") || line.equals("?")) {
                printHelp();
                continue;
            }

            String error = "";
            Move move = parse(line, game, error);

            if (move == null || !game.TryPlay(move)) {
                System.out.println("! " + error);
                continue;
            }

            System.out.println(game.getLastAction());
            recent.clear();
            if (humans > 1) recent.add(game.getLastAction());
        }

        showResults(game, recent);
    }

    private static final Scanner SCANNER = new Scanner(System.in);

    private static String readLine() {
        return SCANNER.hasNextLine() ? SCANNER.nextLine() : null;
    }

    private static GameMode askMode() {
        System.out.println("1) 2 players   2) 3 players   3) 4 players"
                + "   4) 4 players, 2v2 partners");

        while (true) {
            System.out.print("Mode: ");
            switch (Objects.requireNonNullElse(readLine(), "").trim()) {
                case "1": return GameMode.TwoPlayer;
                case "2": return GameMode.ThreePlayer;
                case "3": return GameMode.FourPlayer;
                case "4": return GameMode.FourPlayerPartners;
                default: break;
            }
        }
    }

    private static boolean askYesNo(String question) {
        while (true) {
            System.out.print(question + " (y/n): ");
            String answer = Objects.requireNonNullElse(readLine(), "")
                    .trim().toLowerCase(Locale.ROOT);

            if (answer.equals("y")) return true;
            if (answer.equals("n")) return false;
        }
    }

    private static int askNumber(String prompt, int max) {
        while (true) {
            System.out.print(prompt + ": ");
            try {
                int number = Integer.parseInt(
                        Objects.requireNonNullElse(readLine(), "").trim());
                if (number >= 1 && number <= max) return number;
            } catch (NumberFormatException ignored) {
                // Ask again.
            }
        }
    }

    private static void showTurn(Game game, List<String> log) {
        if (!log.isEmpty()) {
            System.out.println("Since your last turn:");
            for (String action : log) {
                System.out.println("  " + action);
            }
        }

        System.out.println();
        System.out.println("Loose: " + (game.getFloor().isEmpty()
                ? "-"
                : game.getFloor().stream()
                .map(card -> (game.getFloor().indexOf(card) + 1)
                        + ":" + card.label())
                .collect(Collectors.joining("  "))));

        for (int i = 0; i < game.getBuilds().size(); i++) {
            Build build = game.getBuilds().get(i);
            String cards = build.toString();

            System.out.println("Build b" + (i + 1) + ": "
                    +"value("+build.getValue() +") ["+ cards +"] owner "
                    + build.getOwner().getName()
                    + (build.isFull() ? " (full)" : ""));
        }

            System.out.println("Homes: " + game.getPlayers().stream()
                    .map(player ->player.getName() + ": " + (player.getHome().isEmpty()
                    ? "-" :player.getHome().getLast().label()))
                    .collect(Collectors.joining("  ")));

        Player player = game.getCurrent();
        System.out.println(player.getName() + "'s hand: "
                + player.getHand().stream()
                .map(card -> (player.getHand().indexOf(card) + 1)
                        + ":" + card.label())
                .collect(Collectors.joining("  ")));

        if (player.getActiveBuild() != null) {
            System.out.println("You own the build of "
                    + player.getActiveBuild().getValue() + ".");
            if (game.getGameMode() == GameMode.TwoPlayer && game.getHalf() == 1) {
                System.out.println("First half: you must capture it or add "
                        + "a second set to it (no trailing).");
            }
        }

        System.out.println("(h = help)");
    }

    private static void printHelp() {
        System.out.println("""
                Commands (card = your hand number, floor# = loose card number, b# = build number)
                  t <card>                        trail a card onto the floor
                  c <card> [floor#...] [b#...]    capture floor cards that match or add up to your card,
                                                  plus builds worth its value      e.g.  c 2 1 3 b1
                  b <card> <target> <floor#...>   build: your card + floor cards add up to the target;
                                                  you must keep another card of that value
                  r <card> <b#>                   raise an opponent's build with your card (it becomes yours)
                  a <card> <b#> <floor#...>       add a set equal to the build's value
                  s <card> <b#>                   add stand card equal to build value
                  h  help      q  quit
                """);
    }

    private static void showResults(Game game, List<String> log) {
        for (String action : log) {
            System.out.println(action);
        }

        System.out.println("=== Game over ===");
        for (Player p : game.getPlayers()) {
            System.out.println(p.getName() + ": " + p.getHome().size()
                    + " cards, " + Scoring.CardPoints(p.getSide())
                    + " card points, total " + game.ScoreOf(p.getSide()));
        }

        int best = game.getSides().stream()
                .mapToInt(game::ScoreOf)
                .max()
                .orElseThrow();

        List<String> winners = game.getSides().stream()
                .filter(side -> game.ScoreOf(side) == best)
                .map(Side::getName)
                .toList();

        System.out.println(winners.size() == 1
                ? "Winner: " + winners.getFirst()
                : "Tie: " + String.join(", ", winners));
    }

    private static void tryClear() {
        try {
            new ProcessBuilder(
                    System.getProperty("os.name").toLowerCase().contains("win")
                            ? "cmd" : "clear",
                    System.getProperty("os.name").toLowerCase().contains("win")
                            ? "/c" : "")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (IOException | InterruptedException ignored) {
            // Clearing the console is optional.
            Thread.currentThread().interrupt();
        }
    }

    private static boolean tryIndex(String text, int count, int[] index) {
        try {
            int number = Integer.parseInt(text);
            if (number >= 1 && number <= count) {
                index[0] = number - 1;
                return true;
            }
        } catch (NumberFormatException ignored) {
            // Invalid index.
        }

        index[0] = -1;
        return false;
    }

    private static boolean tryLoose(
            Iterable<String> tokens, Game game,
            List<Card> loose, String error) {

        for (String token : tokens) {
            int[] index = {-1};
            if (!tryIndex(token, game.getFloor().size(), index)) {
                error = "'" + token + "' is not a loose floor card number.";
                return false;
            }
            loose.add(game.getFloor().get(index[0]));
        }
        return true;
    }

    private static Build tryBuildRef(String text, Game game) {
        if (text.startsWith("b") || text.startsWith("B")) {
            text = text.substring(1);
        }

        int[] index = {-1};
        if (!tryIndex(text, game.getBuilds().size(), index)) return null;
        return game.getBuilds().get(index[0]);
    }

    private static Move parse(String line, Game game, String error) {
        String[] tokens = line.trim().split("\\s+");
        List<Card> hand = game.getCurrent().getHand();

        int[] handIndex = {-1};
        if (tokens.length < 2
                || !tryIndex(tokens[1], hand.size(), handIndex)) {
            error = "Start with a command and your card number, e.g. 't 3'. Type h for help.";
            return null;
        }

        Card card = hand.get(handIndex[0]);

        switch (tokens[0].toLowerCase(Locale.ROOT)) {
            case "t":
                return new TrailMove(card);

            case "c": {
                List<Card> loose = new ArrayList<>();
                Build b = null;

                for (int i = 2; i < tokens.length; i++) {
                    String token = tokens[i];

                    if (token.startsWith("b") || token.startsWith("B")) {
                        Build build = tryBuildRef(token, game);
                        if (build == null) {
                            error = "'" + token + "' is not a build on the floor.";
                            return null;
                        }
                        b = build;
                    } else {
                        int[] index = {-1};
                        if (!tryIndex(token, game.getFloor().size(), index)) {
                            error = "'" + token + "' is not a loose floor card number.";
                            return null;
                        }
                        loose.add(game.getFloor().get(index[0]));
                    }
                }

                return new CaptureMove(card, loose, b);
            }

            case "b": {
                if (tokens.length < 4) {
                    error = "Usage: b <card> <target> <floor#...>";
                    return null;
                }

                int target;
                try {
                    target = Integer.parseInt(tokens[2]);
                } catch (NumberFormatException exception) {
                    error = "Usage: b <card> <target> <floor#...>";
                    return null;
                }

                List<Card> loose = new ArrayList<>();
                if (!tryLoose(Arrays.asList(tokens).subList(3, tokens.length),
                        game, loose, error)) {
                    return null;
                }
                return new BuildMove(card, loose, target);
            }

            case "r": {
                if (tokens.length != 3) {
                    error = "Usage: r <card> <b#>";
                    return null;
                }

                Build build = tryBuildRef(tokens[2], game);
                if (build == null) {
                    error = "Usage: r <card> <b#>";
                    return null;
                }
                return new RaiseMove(card, build);
            }

            case "a": {
                if (tokens.length < 4) {
                    error = "Usage: a <card> <b#> <floor#...>";
                    return null;
                }

                Build build = tryBuildRef(tokens[2], game);
                if (build == null) {
                    error = "Usage: a <card> <b#> <floor#...>";
                    return null;
                }

                List<Card> loose = new ArrayList<>();
                if (!tryLoose(Arrays.asList(tokens).subList(3, tokens.length),
                        game, loose, error)) {
                    return null;
                }
                return new AddSetMove(card, loose, build);
            }

            case "s": {
                if (tokens.length <3) {
                    error = "Usage: s <card> <b#>";
                    return null;
                }

                Build build = tryBuildRef(tokens[2], game);
                if (build == null) {
                    error = "Usage: s <card> <b#>";
                    return null;
                }

                return new StandMove(card, build);

            }

            default:
                error = "Unknown command. Type h for help.";
                return null;
        }
    }

}
