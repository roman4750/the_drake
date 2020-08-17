package thedrake.game;

import java.util.ArrayList;
import java.util.List;

public class SlideAction extends TroopAction {

    protected SlideAction(int offsetX, int offsetY) {
        super(offsetX, offsetY);
    }

    public SlideAction(Offset2D offset) {
        super(offset);
    }

    @Override
    public List<Move> movesFrom(BoardPos origin, PlayingSide side, GameState state) {
        List<Move> answer = new ArrayList<>();
        TilePos target = origin.stepByPlayingSide(offset(), side);

        while(!target.equals(TilePos.OFF_BOARD)) {
            if(state.canStep(origin, target)) {
                answer.add(new StepOnly(origin, (BoardPos)target));
            } else if(state.canCapture(origin, target)) {
                answer.add(new StepAndCapture(origin, (BoardPos)target));
            } else {
                break;
            }

            TilePos temporary = target.stepByPlayingSide(offset(), side);
            target = temporary;
        }


        return answer;
    }
}
