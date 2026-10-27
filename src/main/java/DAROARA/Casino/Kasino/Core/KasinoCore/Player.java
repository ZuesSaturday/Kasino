package DAROARA.Casino.Kasino.Core.KasinoCore;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private final Side side;
    private final List<Card> Hand;
    private final List<Card> Home;
    private Build ActiveBuild;
    private boolean isComputer;

    public Player(String name,Side side) {
        this.name = name;
        this.side = side;
        this.Hand = new ArrayList<>();
        this.Home = new ArrayList<>();

    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return Hand;
    }

    public Side getSide() {
        return side;
    }

    public boolean isComputer() {
        return isComputer;
    }

    public Build getActiveBuild() {
        return ActiveBuild;
    }

    public void setActiveBuild(Build activeBuild) {
        ActiveBuild = activeBuild;
    }

    public List<Card> getHome() {
        return Home;
    }

    public void setComputer(boolean computer) {
        isComputer = computer;
    }
}
