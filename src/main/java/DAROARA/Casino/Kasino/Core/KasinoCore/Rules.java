package DAROARA.Casino.Kasino.Core.KasinoCore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Rules {

    public static boolean CanBuild(Player player, Card played, List<Card> loose, int target) {
        return player.getActiveBuild() == null
                && !loose.isEmpty()
                && target <= 10
                && played.Value() + sum(loose) == target
                && holdsAnotherOfValue(player, played, target);
    }

    public static Build CreateBuild(Player player,Card played,List<Card> SelectedCards,int target) {
        if (!CanBuild(player,played,SelectedCards,target)) throw new RuntimeException("Illegal  Build");
        var build = new Build(player,target);
        player.setActiveBuild(build);
        AddToBuild(player,played,build,SelectedCards);
        return build;
    }

    public static boolean CanAdd(Player player, Card played, Build build, List<Card> loose) {   // now public
        return build.equals(player.getActiveBuild())      // null-safe; the old version NPE'd
                && !build.isFull()
                && played.Value() + sum(loose) == build.getValue();
    }

    public static void AddToBuild(Player player,Card played,Build build,List<Card> selectedCards) {
        if (!CanAdd(player,played,build,selectedCards)) throw new IllegalStateException("Illegal addition to build.");
        addSet(player, played, build, selectedCards);
    }

    public static boolean CanRaise(Player player, Build build, Card added) {
        if (build.getOwner().getSide() == player.getSide()) return false;
        if (build.getValue() == 10) return false;
        if (build.isFull()) return false;
        if (player.getActiveBuild()!=null) return false;

        int newValue = build.getValue() + added.Value();
        if (newValue>10) return false;
        return player.getHand().stream().anyMatch(card -> card != added && card.Value() ==newValue);
    }

    public static void Raise(Player player, Build build, Card added) {
        if (!CanRaise(player,build,added)) throw new RuntimeException("Illegal raise.");
        player.getHand().remove(added);
        build.Raise(player,added);
    }


    public static boolean CanStand(Player player,Card played,Build build) {
        return player.getActiveBuild().equals(build) && player.getHand().stream().anyMatch(c->c.Value() == played.Value());
    }

    public static void Stand(Player player,Card played,Build build) {
        if (!CanStand(player,played,build)) throw new RuntimeException("You cnt stand a build that is not yours or you dont have another card to cspture build.");
        addSet(player, played, build, List.of());
    }

    private static void addSet(Player player, Card played, Build build, List<Card> loose) {
        player.getHand().remove(played);
        List<Card> set = new ArrayList<>(loose);      // loose is immutable (List.copyOf in the records)
        set.add(played);
        set.sort(Comparator.comparingInt(Card::Value).reversed());
        build.Add(set);
    }

    private static int sum(List<Card> cards) { return cards.stream().mapToInt(Card::Value).sum(); }
    private static boolean holdsAnotherOfValue(Player p, Card played, int value) {
        return p.getHand().stream().anyMatch(c -> !c.equals(played) && c.Value() == value);
    }
}