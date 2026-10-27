package DAROARA.Casino.Kasino.Core.KasinoCore;

import java.util.ArrayList;
import java.util.List;

public class Side {

    public final String Name;
    public final List<Card> Home = new ArrayList<>();

    public Side(String name) {
        this.Name = name;
    }

    public String getName() {
        return Name;
    }

}
