package thedrake.ui;

import thedrake.game.Move;
import thedrake.game.GameState;


public interface TileViewContext {

    void tileViewSelected(TileView tileView);

    void stackViewSelected(StackView stackView);

    void executeMove(Move move);

    GameState getGameState();

}
