package thedrake.game;

import java.util.ArrayList;
import java.util.List;

public class TroopTile implements Tile {

    private Troop troop;
    private PlayingSide playingSide;
    private TroopFace troopFace;

    public TroopTile(Troop troop, PlayingSide side, TroopFace face) {
        this.troop = troop;
        this.playingSide = side;
        this.troopFace = face;
    }

    public PlayingSide side() {
        return playingSide;
    }

    public TroopFace face() {
        return troopFace;
    }

    public Troop troop() {
        return troop;
    }

    public boolean canStepOn() {
        return false;
    }

    public boolean hasTroop() {
        return true;
    }

    public TroopTile flipped() {
        if( troopFace == TroopFace.AVERS ) {
            return new TroopTile(this.troop, this.playingSide, TroopFace.REVERS);
        }
        return  new TroopTile(this.troop, this.playingSide, TroopFace.AVERS);
    }

    @Override
    public List<Move> movesFrom(BoardPos pos, GameState state) {
        List<Move> answer = new ArrayList<>();

        List<TroopAction> action = troop.actions(this.face());

        for(TroopAction iter: action) {
            answer.addAll(iter.movesFrom(pos, playingSide, state));
        }

        return answer;
    }

}
