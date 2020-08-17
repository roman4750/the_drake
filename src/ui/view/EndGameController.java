package thedrake.ui.view;

import thedrake.game.GameResult;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;


public class EndGameController implements Initializable {

    @FXML public Label wonLabel;

    public void initialize(URL location, ResourceBundle resources) {
    }

    public void BackToMenuButton() {
        GameResult.changeStateTo(GameResult.MAIN_MENU);
    }

    @FXML
    public void setWonText(String text) {
        wonLabel.setText(text);
    }
}
