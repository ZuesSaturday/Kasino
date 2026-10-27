package DAROARA.Casino.Kasino.Core.KasinoCore;

import java.util.ArrayList;
import java.util.List;

public class Build {

    private Player Owner;
    private int Value;
    private List<BuildLayer> build;

    public Build(Player owner,int value) {
        this.Owner = owner;
        this.Value = value;
        this.build = new ArrayList<>();
    }

    public Build(Build source, Player owner) {
        this.Owner = owner;
        this.Value = source.Value;
        this.build = new ArrayList<>();
        for (BuildLayer layer : source.build)
            this.build.add(new BuildLayer(new ArrayList<>(layer.getSetCards())));
    }

    private void setNewOwner(Player newOwner) {
        this.Owner = newOwner;
    }

    public void Raise(Player owner, Card added) {
        build.getFirst().getSetCards().add(added);
        this.Value += added.Value();
        Owner.setActiveBuild(null);
        setNewOwner(owner);
        owner.setActiveBuild(this);
    }

    public void Add(List<Card> set) {
        build.add(new BuildLayer(set));
    }

    public void addBuildLayers(List<BuildLayer> build) {
        this.build = build;
    }

    public Player getOwner() {
        return Owner;
    }

    public int getValue() {
        return Value;
    }

    public boolean isFull() {
        return Value == 10 || build.size()>1;

    }

    public List<Card> getCards() {
        return build.stream().flatMap(l -> l.getSetCards().stream()).toList();
    }

    public List<BuildLayer> getBuild() {
        return build;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card c:getCards()){
            sb.append(c.label()).append(" ");
        }
        return sb.toString().trim();
    }

}
