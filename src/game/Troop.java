package thedrake.game;

import  java.util.List;

public class Troop {

    private final String name;
    private final Offset2D aversPivot;
    private final Offset2D reversPivot;
    private List<TroopAction> avers;
    private List<TroopAction> revers;


    public Troop(String name, Offset2D aversPivot, Offset2D reversPivot, List<TroopAction> avers, List<TroopAction> revers) {
        this.name = name;
        this.aversPivot = aversPivot;
        this.reversPivot = reversPivot;
        this.revers = revers;
        this.avers = avers;
    }

    public Troop(String name, Offset2D pivot, List<TroopAction> avers, List<TroopAction> revers) {
        this(name, pivot, pivot, avers, revers);
    }

    public Troop(String name, List<TroopAction> avers, List<TroopAction> revers) {
        this(name, new Offset2D(1, 1), new Offset2D(1, 1), avers, revers);
    }

    public String name() {
        return this.name;
    }

    public Offset2D pivot(TroopFace face) {
        if(face == TroopFace.AVERS) {
            return this.aversPivot;
        }
            return this.reversPivot;
    }

    //Vrací seznam akcí pro zadanou stranu jednotky
    public List<TroopAction> actions(TroopFace face) {
        if(face.equals(TroopFace.AVERS)) {
            return  avers;
        }
        return revers;
    }
}

