package thedrake.game;

import java.io.PrintWriter;
import java.util.*;

public class GameState implements JSONSerializable{
	private final Board board;
	private final PlayingSide sideOnTurn;
	private final Army blueArmy;
	private final Army orangeArmy;
	private final GameResult result;
	
	public GameState(
			Board board, 
			Army blueArmy,
			Army orangeArmy) {
		this(board, blueArmy, orangeArmy, PlayingSide.BLUE, GameResult.IN_PLAY);
	}
	
	public GameState(
			Board board, 
			Army blueArmy, 
			Army orangeArmy, 
			PlayingSide sideOnTurn, 
			GameResult result) {
		this.board = board;
		this.sideOnTurn = sideOnTurn;
		this.blueArmy = blueArmy;
		this.orangeArmy = orangeArmy;
		this.result = result;
	}
	
	public Board board() {
		return board;
	}
	
	public PlayingSide sideOnTurn() {
		return sideOnTurn;
	}
	
	public GameResult result() {
		return result;
	}
	
	public Army army(PlayingSide side) {
		if(side == PlayingSide.BLUE) {
			return blueArmy;
		}
		
		return orangeArmy;
	}
	
	public Army armyOnTurn() {
		return army(sideOnTurn);
	}
	
	public Army armyNotOnTurn() {
		if(sideOnTurn == PlayingSide.BLUE)
			return orangeArmy;
		
		return blueArmy;
	}

	// Vrátí dlaždici, která se nachází na hrací desce na pozici pos.
	// armády nějakého hráče a pokud ne, vrátí dlaždici z objektu board
	// Musí tedy zkontrolovat, jestli na této pozici není jednotka z
	public Tile tileAt(BoardPos pos) {
		if (armyOnTurn().boardTroops().at(pos).isPresent()) {
			return armyOnTurn().boardTroops().at(pos).get();
		}
		if (armyNotOnTurn().boardTroops().at(pos).isPresent()) {
			return armyNotOnTurn().boardTroops().at(pos).get();
		}
		return board.at(pos);

	}


	// Vrátí true, pokud je možné ze zadané pozice začít tah nějakou
	// jednotkou. Vrací false, pokud stav hry není IN_PLAY, pokud
	// na dané pozici nestojí žádné jednotka nebo pokud na pozici
	// stojí jednotka hráče, který zrovna není na tahu.
	// Při implementaci vemte v úvahu zahájení hry. Dokud nejsou
	// postaveny stráže, žádné pohyby jednotek po desce nejsou možné.
	private boolean canStepFrom(TilePos origin) {
		if(!origin.equals(TilePos.OFF_BOARD) && !blueArmy.boardTroops().isPlacingGuards()
				&& !orangeArmy.boardTroops().isPlacingGuards() && result.equals(GameResult.IN_PLAY)
			&& armyOnTurn().boardTroops().at(board.positionFactory().pos(origin.i(), origin.j())).isPresent() ) {
			return true;
		}
		return false;

	}

	// Vrátí true, pokud je možné na zadanou pozici dokončit tah nějakou
	// jednotkou. Vrací false, pokud stav hry není IN_PLAY nebo pokud
	// na zadanou dlaždici nelze vstoupit (metoda Tile.canStepOn).
	private boolean canStepTo(TilePos target) {

		if (result != GameResult.IN_PLAY || !(target instanceof BoardPos) || !board().at((BoardPos) target).canStepOn() ||
				!armyOnTurn().boardTroops().isLeaderPlaced() ||
				armyOnTurn().boardTroops().isPlacingGuards() ||
				armyOnTurn().boardTroops().at(target).isPresent() ||
				armyNotOnTurn().boardTroops().at(target).isPresent()) {
			return false;
		}

		return true;


	}

	// Vrátí true, pokud je možné na zadané pozici vyhodit soupeřovu jednotku.
	// Vrací false, pokud stav hry není IN_PLAY nebo pokud
	// na zadané pozici nestojí jednotka hráče, který zrovna není na tahu.
	private boolean canCaptureOn(TilePos target) {
		if (result != GameResult.IN_PLAY ||
				!armyOnTurn().boardTroops().isLeaderPlaced() ||
				armyOnTurn().boardTroops().isPlacingGuards() ||
				armyOnTurn().boardTroops().at(target).isPresent() ||
				!armyNotOnTurn().boardTroops().at(target).isPresent()) {
			return false;
		}

		return true;

	}
	
	public boolean canStep(TilePos origin, TilePos target)  {
		return canStepFrom(origin) && canStepTo(target);
	}
	
	public boolean canCapture(TilePos origin, TilePos target)  {
		return canStepFrom(origin) && canCaptureOn(target);
	}

	// Vrátí true, pokud je možné na zadanou pozici položit jednotku ze
	// zásobníku.. Vrací false, pokud stav hry není IN_PLAY, pokud je zásobník
	// armády, která je zrovna na tahu prázdný, pokud není možné na danou
	// dlaždici vstoupit. Při implementaci vemte v úvahu zahájení hry, kdy
	// se vkládání jednotek řídí jinými pravidly než ve střední hře.
	public boolean canPlaceFromStack(TilePos target) {
		if( armyOnTurn().stack().isEmpty() ||
				!(target instanceof BoardPos) || !board().at((BoardPos) target).canStepOn() ||
				result != GameResult.IN_PLAY ||
				armyOnTurn().boardTroops().at(target).isPresent() ||
				armyNotOnTurn().boardTroops().at(target).isPresent()) {

			return false;
		}

		if(!armyOnTurn().boardTroops().isLeaderPlaced() && ((armyOnTurn() == blueArmy && target.row() != 1) || (armyOnTurn() == orangeArmy && target.row() != board.dimension()))) { return false; }

		int a, b;
		if(armyOnTurn().boardTroops().isPlacingGuards() &&
				(((a = Math.abs(armyOnTurn().boardTroops().leaderPosition().i() - target.i())) > 1) ||
						((b = Math.abs(armyOnTurn().boardTroops().leaderPosition().j() - target.j())) > 1) ||
						(a == 1 && b == 1))) { return false; }

		return true;

	}
	
	public GameState stepOnly(BoardPos origin, BoardPos target) {		
		if(canStep(origin, target))		 
			return createNewGameState(
					armyNotOnTurn(),
					armyOnTurn().troopStep(origin, target), GameResult.IN_PLAY);
		
		throw new IllegalArgumentException();
	}
	
	public GameState stepAndCapture(BoardPos origin, BoardPos target) {
		if(canCapture(origin, target)) {
			Troop captured = armyNotOnTurn().boardTroops().at(target).get().troop();
			GameResult newResult = GameResult.IN_PLAY;
			
			if(armyNotOnTurn().boardTroops().leaderPosition().equals(target))
				newResult = GameResult.VICTORY;
			
			return createNewGameState(
					armyNotOnTurn().removeTroop(target), 
					armyOnTurn().troopStep(origin, target).capture(captured), newResult);
		}
		
		throw new IllegalArgumentException();
	}
	
	public GameState captureOnly(BoardPos origin, BoardPos target) {
		if(canCapture(origin, target)) {
			Troop captured = armyNotOnTurn().boardTroops().at(target).get().troop();
			GameResult newResult = GameResult.IN_PLAY;
			
			if(armyNotOnTurn().boardTroops().leaderPosition().equals(target))
				newResult = GameResult.VICTORY;
			
			return createNewGameState(
					armyNotOnTurn().removeTroop(target),
					armyOnTurn().troopFlip(origin).capture(captured), newResult);
		}
		
		throw new IllegalArgumentException();
	}
	
	public GameState placeFromStack(BoardPos target) {
		if(canPlaceFromStack(target)) {
			return createNewGameState(
					armyNotOnTurn(), 
					armyOnTurn().placeFromStack(target), 
					GameResult.IN_PLAY);
		}
		
		throw new IllegalArgumentException();
	}
	
	public GameState resign() {
		return createNewGameState(
				armyNotOnTurn(), 
				armyOnTurn(), 
				GameResult.VICTORY);
	}
	
	public GameState draw() {
		return createNewGameState(
				armyOnTurn(), 
				armyNotOnTurn(), 
				GameResult.DRAW);
	}
	
	private GameState createNewGameState(Army armyOnTurn, Army armyNotOnTurn, GameResult result) {
		if(armyOnTurn.side() == PlayingSide.BLUE) {
			return new GameState(board, armyOnTurn, armyNotOnTurn, PlayingSide.BLUE, result);
		}
		
		return new GameState(board, armyNotOnTurn, armyOnTurn, PlayingSide.ORANGE, result); 
	}

    public void toJSON(PrintWriter writer) {
        Board board = this.board;

        String json = "{";
        json += "\"result\":";
        json += "\"" + this.result + "\"";
        json += ",";
        json += "\"board\":";
        json += "{";
        json += "\"dimension\":";
        json += board.dimension();
        json += ",";
        json += "\"tiles\":";
        json += "[";

        for (int i = 0; i < board.dimension(); ++i) {
            for (int j = 0; j < board.dimension(); ++j) {
                if (board.at(board.positionFactory().pos(j, i)) == BoardTile.EMPTY) {
                    json += "\"empty\",";
                } else if (board.at(board.positionFactory().pos(j, i)) == BoardTile.MOUNTAIN) {
                    json += "\"mountain\",";
                }
            }
        }

        json = json.substring(0, json.length() - 1);
        json += "]";
        json += "},";
        json += "\"blueArmy\":";
        json += "{";
        json += "\"boardTroops\":";
        json += "{";
        json += "\"side\":";
        json += "\"BLUE\"";
        json += ",";
        json += "\"leaderPosition\":";
        json += "\"" + blueArmy.boardTroops().leaderPosition() + "\"";
        json += ",";
        json += "\"guards\":";
        json += blueArmy.boardTroops().guards();
        json += ",";
        json += "\"troopMap\":";
        json += "{";

        Map<BoardPos, TroopTile> blueArmyPos = new TreeMap<>(blueArmy.boardTroops().troopMap());

        String[] jsonBlueArmyTroops = new String[1];
        jsonBlueArmyTroops[0] = "";

        blueArmyPos.forEach((pos, tile) -> {
            jsonBlueArmyTroops[0] += "\"" + pos.toString() + "\":";
            jsonBlueArmyTroops[0] += "{";
            jsonBlueArmyTroops[0] += "\"troop\":";
            jsonBlueArmyTroops[0] += "\"" + tile.troop().name() + "\"";
            jsonBlueArmyTroops[0] += ",";
            jsonBlueArmyTroops[0] += "\"side\":";
            jsonBlueArmyTroops[0] += "\"" + tile.side() + "\"";
            jsonBlueArmyTroops[0] += ",";
            jsonBlueArmyTroops[0] += "\"face\":";
            jsonBlueArmyTroops[0] += "\"" + tile.face() + "\"";
            jsonBlueArmyTroops[0] += "}";
            jsonBlueArmyTroops[0] += ",";
        });

        if (!blueArmyPos.isEmpty()) {
            jsonBlueArmyTroops[0] = jsonBlueArmyTroops[0].substring(0, jsonBlueArmyTroops[0].length() - 1);
            json += jsonBlueArmyTroops[0];
        }

        json += "}";
        json += "}";
        json += ",";
        json += "\"stack\":";
        json += "[";

        String[] jsonBlueArmyStack = new String[1];
        jsonBlueArmyStack[0] = "";

        blueArmy.stack().stream().forEach(troop -> {
            jsonBlueArmyStack[0] += "\"" + troop.name() + "\"";
            jsonBlueArmyStack[0] += ",";
        });

        if (jsonBlueArmyStack[0] != "") {
            jsonBlueArmyStack[0] = jsonBlueArmyStack[0].substring(0, jsonBlueArmyStack[0].length() - 1);
            json += jsonBlueArmyStack[0];
        }

        json += "]";
        json += ",";
        json += "\"captured\":";
        json += "[";

        String[] jsonBlueArmyCaptured = new String[1];
        jsonBlueArmyCaptured[0] = "";

        blueArmy.captured().stream().forEach(troop -> {
            jsonBlueArmyCaptured[0] += "\"" + troop.name() + "\"";
            jsonBlueArmyCaptured[0] += ",";
        });

        if (jsonBlueArmyCaptured[0] != "") {
            jsonBlueArmyCaptured[0] = jsonBlueArmyCaptured[0].substring(0, jsonBlueArmyCaptured[0].length() - 1);
            json += jsonBlueArmyCaptured[0];
        }

        json += "]";
        json += "}";
        json += ",";
        json += "\"orangeArmy\":";
        json += "{";
        json += "\"boardTroops\":";
        json += "{";
        json += "\"side\":";
        json += "\"ORANGE\"";
        json += ",";
        json += "\"leaderPosition\":";
        json += "\"" + orangeArmy.boardTroops().leaderPosition() + "\"";
        json += ",";
        json += "\"guards\":";
        json += orangeArmy.boardTroops().guards();
        json += ",";
        json += "\"troopMap\":";
        json += "{";

        Map<BoardPos, TroopTile> orangeArmyPos = new TreeMap<>(orangeArmy.boardTroops().troopMap());

        String[] jsonOrangeArmyTroops = new String[1];
        jsonOrangeArmyTroops[0] = "";

        orangeArmyPos.forEach((pos, tile) -> {
            jsonOrangeArmyTroops[0] += "\"" + pos.toString() + "\":";
            jsonOrangeArmyTroops[0] += "{";
            jsonOrangeArmyTroops[0] += "\"troop\":";
            jsonOrangeArmyTroops[0] += "\"" + tile.troop().name() + "\"";
            jsonOrangeArmyTroops[0] += ",";
            jsonOrangeArmyTroops[0] += "\"side\":";
            jsonOrangeArmyTroops[0] += "\"" + tile.side() + "\"";
            jsonOrangeArmyTroops[0] += ",";
            jsonOrangeArmyTroops[0] += "\"face\":";
            jsonOrangeArmyTroops[0] += "\"" + tile.face() + "\"";
            jsonOrangeArmyTroops[0] += "}";
            jsonOrangeArmyTroops[0] += ",";
        });

        if (!orangeArmyPos.isEmpty()) {
            jsonOrangeArmyTroops[0] = jsonOrangeArmyTroops[0].substring(0, jsonOrangeArmyTroops[0].length() - 1);
            json += jsonOrangeArmyTroops[0];
        }

        json += "}";
        json += "}";
        json += ",";

        json += "\"stack\":";
        json += "[";

        String[] jsonOrangeArmyStack = new String[1];
        jsonOrangeArmyStack[0] = "";

        orangeArmy.stack().stream().forEach(troop -> {
            jsonOrangeArmyStack[0] += "\"" + troop.name() + "\"";
            jsonOrangeArmyStack[0] += ",";
        });

        if (jsonOrangeArmyStack[0] != "") {
            jsonOrangeArmyStack[0] = jsonOrangeArmyStack[0].substring(0, jsonOrangeArmyStack[0].length() - 1);
            json += jsonOrangeArmyStack[0];
        }

        json += "]";
        json += ",";
        json += "\"captured\":";
        json += "[";

        String[] jsonOrangeArmyCaptured = new String[1];
        jsonOrangeArmyCaptured[0] = "";

        orangeArmy.captured().stream().forEach(troop -> {
            jsonOrangeArmyCaptured[0] += "\"" + troop.name() + "\"";
            jsonOrangeArmyCaptured[0] += ",";
        });

        if (jsonOrangeArmyCaptured[0] != "") {
            jsonOrangeArmyCaptured[0] = jsonOrangeArmyCaptured[0].substring(0, jsonOrangeArmyCaptured[0].length() - 1);
            json += jsonOrangeArmyCaptured[0];
        }

        json += "]";
        json += "}";
        json += "}";
        writer.printf(json);
    }

}
