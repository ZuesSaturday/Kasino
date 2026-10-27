package DAROARA.Casino.Kasino.Core.game;

import DAROARA.Casino.Kasino.Core.game.Record.*;
import DAROARA.Casino.Kasino.Core.KasinoCore.*;

import java.util.*;

public class Game {
    private Deck deck;
    private final int cardsPerDeal;
    private Player lastCapturer;

    private final GameMode gameMode;

    private final boolean PackPoints;
    private final boolean FiveSpadePoints;

    private List<Player> Players;
    private List<Side> Sides;
    private final List<Card> Floor;
    private final List<Build> Builds;
    private int CurrentPlayerIndex = 0;
    private Player Current;
    private int Half = 1;
    private boolean IsOver;
    private String LastAction = "";

    public Game(GameMode mode, boolean packPoints, boolean fiveSpadePoints, Random rnd) {
        this.gameMode = mode;
        this.PackPoints = packPoints;
        this.FiveSpadePoints = fiveSpadePoints;
        this.cardsPerDeal = mode == GameMode.ThreePlayer?13:10;
        this.Floor = new ArrayList<>();
        this.Builds = new ArrayList<>();
        CreatePlayers(mode);
        this.Current = Players.get(CurrentPlayerIndex);
        do {
            for (Player p : Players) p.getHand().clear();
            deck = new Deck().shuffleDeck(rnd);
            Deal();
        } while (Players.stream().anyMatch(p ->
                p.getHand().stream().filter(c -> c.Value() == 10).count() == 4));
        if (mode == GameMode.ThreePlayer) Floor.add(deck.drawCard());
    }

    public Game(Game source) {
        this.gameMode = source.gameMode;
        this.PackPoints = source.PackPoints;
        this.FiveSpadePoints = source.FiveSpadePoints;
        this.cardsPerDeal = source.cardsPerDeal;

        // Cards can be shared if Card is immutable.
        // Copy the deck's remaining card list; see Deck copy constructor below.
        this.deck = new Deck(source.deck);

        this.Floor = new ArrayList<>(source.Floor);
        this.Builds = new ArrayList<>();
        this.Players = new ArrayList<>();
        this.Sides = new ArrayList<>();

        this.CurrentPlayerIndex = source.CurrentPlayerIndex;
        this.Half = source.Half;
        this.IsOver = source.IsOver;
        this.LastAction = source.LastAction;

        // Copy sides and their captured cards.
        java.util.Map<Side, Side> sideCopies = new java.util.IdentityHashMap<>();
        for (Side oldSide : source.Sides) {
            Side newSide = new Side(oldSide.getName());
            sideCopies.put(oldSide, newSide);
            this.Sides.add(newSide);
        }

        // Copy players, retaining their copied side references and hands.
        java.util.Map<Player, Player> playerCopies = new java.util.IdentityHashMap<>();
        for (Player oldPlayer : source.Players) {
            Player newPlayer = new Player(
                    oldPlayer.getName(),
                    sideCopies.get(oldPlayer.getSide())
            );
            newPlayer.getHand().addAll(oldPlayer.getHand());
            newPlayer.setComputer(oldPlayer.isComputer());
            this.Players.add(newPlayer);
            playerCopies.put(oldPlayer, newPlayer);
        }

        // Copy builds and point their owners at the copied players.
        java.util.Map<Build, Build> buildCopies = new java.util.IdentityHashMap<>();
        for (Build oldBuild : source.Builds) {
            Player newOwner = playerCopies.get(oldBuild.getOwner());

            // Replace with your actual Build constructor/signature.
            Build newBuild = new Build(
                    newOwner,
                    oldBuild.getValue()
            );
            newBuild.addBuildLayers(oldBuild.getBuild());

            this.Builds.add(newBuild);
            buildCopies.put(oldBuild, newBuild);
        }

        // Restore each player's active-build reference to the copied build.
        for (Player oldPlayer : source.Players) {
            Build oldActive = oldPlayer.getActiveBuild();
            if (oldActive != null) {
                playerCopies.get(oldPlayer)
                        .setActiveBuild(buildCopies.get(oldActive));
            }
        }

        this.Current = this.Players.get(this.CurrentPlayerIndex);

        // Map lastCapturer to the copied side.
        this.lastCapturer = source.lastCapturer == null
                ? null
                : playerCopies.get(source.lastCapturer);
    }

    private void Deal() {
        for (Player player:Players) {
            for (int i = 0; i < cardsPerDeal; i++) {
                player.getHand().add(deck.drawCard());
            }
        }

    }

    private void CreatePlayers(GameMode mode) {

        if (mode == GameMode.FourPlayerPartners) {
            Side a = new Side("Team A");
            Side b = new Side("Team B");

            this.Players = List.of(
                    new Player("A1", a),
                    new Player("B1", b),
                    new Player("A2", a),
                    new Player("B2", b)
            );
            this.Sides = List.of(a,b);
            return;
        }
        int n = switch (mode) {
            case TwoPlayer -> 2;
            case ThreePlayer -> 3;
            default -> 4;
        };
        List<Side> sides = new ArrayList<>();
        for (int i = 1; i < n+1; i++) {
            var side = new Side("Player "+i);
            sides.add(side);
        }

        List<Player> singles = new ArrayList<>();
        for (Side s: sides) {
            singles.add(new Player(s.Name,s));
        }

        this.Players = singles;
        this.Sides = sides;
    }

    public boolean TryPlay(Move move) {
        if (!Validate(move).isEmpty())return false;
        Apply(move);
        AdvanceTurn();
        return true;
    }

    public String Validate(Move move) {
        if (IsOver) return "Game Over.";

        Player currentPlayer = Current;
        Build mine = currentPlayer.getActiveBuild();

        if (!currentPlayer.getHand().contains(move.played())) return "That card is not in your hand.";
        if (mine != null) {
            boolean capturesMine = move instanceof CaptureMove c && mine.equals(c.build());
            boolean extendsMine = (move instanceof AddSetMove a && mine.equals(a.target()))
                    || (move instanceof StandMove s && mine.equals(s.target()));

            if (gameMode == GameMode.TwoPlayer && Half == 1 && !capturesMine && !extendsMine)
                return "You own a build: in the first half you must capture it or carry on building "
                        + "(add a second set to it). You can't trail.";

            long matching = currentPlayer.getHand().stream().filter(c -> c.Value() == mine.getValue()).count();
            if (move.played().Value() == mine.getValue() && matching == 1 && !capturesMine)
                return "Keep your last " + mine.getValue() + " to capture your build (or capture it now).";
        }

        switch (move) {
            case TrailMove m:
                return "";

            case CaptureMove m:
                if (m.loose().isEmpty() && m.build() == null) return "Pick floor cards or builds to capture.";
                if (m.build() != null) {
                    if (!Builds.contains(m.build())) return "That build is not on the floor.";
                    if (m.build().getValue() != m.played().Value())
                        return "You can only capture builds worth " + m.played().Value() + " with that card.";
                }
                if (!m.loose().isEmpty()) {
                    if (!AllOnFloor(m.loose())) return "Those cards are not all on the floor.";
                    List<Integer> vals = m.loose().stream().map(Card::Value).toList();
                    if (!CanPartition(vals, m.played().Value()))
                        return "Floor cards must match " + m.played().Value() + " or add up to it (in groups).";
                }
                return "";

            case BuildMove m:
                if (Builds.stream().anyMatch(b -> b.getValue() == m.target())) return "build value already exists.";
                if (m.loose().isEmpty() || !AllOnFloor(m.loose())) return "Pick the floor cards to build with.";
                if (!Rules.CanBuild(currentPlayer, m.played(), m.loose(), m.target()))
                    return "Illegal build: the cards must add up to the target (max 10), you must hold another "
                            + "card of that value, and you can only have one build.";
                return "";

            case RaiseMove m:
                if (!Builds.contains(m.target()))
                    return "That build is not on the floor.";
                if (!Rules.CanRaise(currentPlayer, m.target(), m.played()))
                    return "You can't raise that build (it must be an opponent's, not full, under 10 after "
                            + "raising, you must hold a card of the new value and not already own a build).";
                return "";

            case AddSetMove m:
                if (!Builds.contains(m.target())) return "That build is not on the floor.";
                if (m.loose().isEmpty() || !AllOnFloor(m.loose())) return "Pick the floor cards for the second set.";
                if (!Rules.CanAdd(currentPlayer, m.played(), m.target(), m.loose()))
                    return "The second set must add up to " + m.target().getValue()
                            + ", and the build must be yours and not already full.";
                return "";

            case StandMove m:
                if (!Builds.contains(m.target())) return "That build is not on the floor.";
                if (!Rules.CanStand(currentPlayer, m.played(), m.target()))
                    return "You can only stand your own, non-full build with a card of its value, "
                            + "and you must hold another card of that value.";
                return "";
            default:
                return "Unknown move.";
        }
    }

    public void Apply(Move move) {
        var p = Current;
        switch (move) {
            case TrailMove m:
                p.getHand().remove(m.played());
                Floor.add(m.played());
                LastAction = p.getName()+" trailed "+ m.played().label() +" .";
                break;

            case CaptureMove m: {
                p.getHand().remove(m.played());
                List<Card> taken = new ArrayList<>(m.loose());
                taken.add(m.played());
                Floor.removeAll(m.loose());
                if (m.build() != null) {
                    taken.addAll(m.build().getCards());
                    m.build().getOwner().setActiveBuild(null);
                    Builds.remove(m.build());          // was never removed before
                }
                p.getSide().Home.addAll(taken);
                lastCapturer = p;
                LastAction = p.getName() + " captured with " + m.played().label() + ".";
                break;
            }

            case BuildMove m:
            {
                Build build = Rules.CreateBuild(p, m.played(), m.loose(), m.target());
                Floor.removeAll(m.loose());
                Builds.add(build);
                LastAction = p.getName()+" built "+ m.target() +".";
                break;
            }

            case RaiseMove m:
                Rules.Raise(p, m.target(), m.played());
                LastAction = p.getName()+" raised a build to "+m.target().getValue()+" with "+m.played().label()+" and now owns it.";
                break;

            case AddSetMove m:
                Rules.AddToBuild(p,m.played(),m.target(),m.loose());
                LastAction = p.getName()+" added another set to the build of "+m.target().getValue()+".";
                break;

            case StandMove m:
                Rules.Stand(p,m.played(),m.target());
                LastAction = p.getName()+" Stood their Build.";
        }
    }

    public boolean AllOnFloor(List<Card> cardList) {
        return Floor.containsAll(cardList) && new HashSet<>(cardList).size() == cardList.size();
    }

    public void AdvanceTurn() {
        CurrentPlayerIndex = (CurrentPlayerIndex +1)%Players.size();
        this.Current = Players.get(CurrentPlayerIndex);
        if (Players.stream().allMatch(p-> p.getHand().isEmpty())) {
            if (deck.remainingCards() > 0) {
                Deal();
                Half++;
            }
            else Finish();
        }
    }
    public void Finish() {
        if (lastCapturer != null) {
            lastCapturer.getHome().addAll(Floor);
            for (Build b:Builds) lastCapturer.getHome().addAll(b.getCards());
        }
        Floor.clear();
        Builds.clear();
        for (Player player:Players)player.setActiveBuild(null);
        IsOver = true;
    }

    public int ScoreOf(Side side) {
        return Scoring.Total(side, Sides, PackPoints, FiveSpadePoints);
    }

    public static boolean CanPartition(List<Integer> val, int tar) {
        int sum = 0;
        for (int num : val) {
            sum+=num;
        }
        if (sum == 0|| sum % tar != 0) return false;
        var sorted = val.stream().sorted(Comparator.reverseOrder()).toList();
        return Search(sorted,new boolean[sorted.size()],tar,0,0,sum/tar);
    }

    private static boolean Search(List<Integer> vals, boolean[] used, int tar, int currentSum, int start, int groupsLeft) {
        if (groupsLeft == 0) return true;
        if (currentSum == tar) return Search(vals,used,tar,0,0,groupsLeft-1);
        for (int i = start; i < vals.size(); i++) {
            if (used[i]||currentSum+ vals.get(i)> tar) continue;
            used[i] = true;
            if(Search(vals, used, tar, currentSum + vals.get(i), i+1, groupsLeft)) return true;
            used[i] = false;
        }
        return false;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public List<Player> getPlayers() {
        return Players;
    }

    public List<Side> getSides() {
        return Sides;
    }

    public List<Card> getFloor() {
        return Floor;
    }

    public Player getCurrent() {
        return Current;
    }

    public List<Build> getBuilds() {
        return Builds;
    }

    public boolean isOver() {
        return IsOver;
    }

    public String getLastAction() {
        return LastAction;
    }

    public int getHalf() {
        return Half;
    }
}
