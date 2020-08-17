package thedrake.game;

import java.util.*;

public class BoardTroops {
	private final PlayingSide playingSide;
	private final Map<BoardPos, TroopTile> troopMap;
	private final TilePos leaderPosition;
	private final int guards;
	
	public BoardTroops(PlayingSide playingSide) { 
		this.playingSide = playingSide;
		this.troopMap = Collections.emptyMap();
		this.leaderPosition = TilePos.OFF_BOARD;
		this.guards = 0;
	}
	
	public BoardTroops(
			PlayingSide playingSide,
			Map<BoardPos, TroopTile> troopMap,
			TilePos leaderPosition, 
			int guards) {
		this.playingSide = playingSide;
		this.troopMap = troopMap;
		this.leaderPosition = leaderPosition;
		this.guards = guards;
	}

	public Optional<TroopTile> at(TilePos pos) {
		if( this.troopMap.containsKey(pos) ) {
			Optional<TroopTile> troopTile = Optional.ofNullable(this.troopMap.get(pos));
			return troopTile;
		}
		return Optional.empty();
	}
	
	public PlayingSide playingSide() {
		return this.playingSide;
	}
	
	public TilePos leaderPosition() {
		return  this.leaderPosition;
	}

	public int guards() {
		return  this.guards;
	}

	public Map<BoardPos, TroopTile> troopMap() {
		return this.troopMap;
	}

	public boolean isLeaderPlaced() {
		if( this.leaderPosition() != TilePos.OFF_BOARD ) {
			return true;
		}
		return false;
	}
	
	public boolean isPlacingGuards() {
		if( this.isLeaderPlaced() && this.guards() < 2 ) {
			return  true;
		}
		return false;
	}	
	
	public Set<BoardPos> troopPositions() {
		Set<BoardPos> boardSetPos = new HashSet<BoardPos>();

		this.troopMap.forEach( (key, value) -> { if(value.hasTroop()) { boardSetPos.add(key); }  } );

		return  boardSetPos;
	}

	public BoardTroops placeTroop(Troop troop, BoardPos target) {
		if( this.troopMap.containsKey(target) ) {
			throw new IllegalStateException("It`s not possible to place on a tile");
		}

		TroopTile newTroopTile = new TroopTile(troop, this.playingSide(), TroopFace.AVERS);
		Map<BoardPos, TroopTile> newMap = new HashMap<>(troopMap);
		newMap.put(target, newTroopTile);

		if( !this.isLeaderPlaced() ) {
			return new BoardTroops(this.playingSide(), newMap, target, this.guards());
		} else if(this.isPlacingGuards()) {
			return new BoardTroops(this.playingSide(), newMap, this.leaderPosition(), this.guards() + 1);
		} else {
			return new BoardTroops(this.playingSide(), newMap, this.leaderPosition(), this.guards());
		}
	}
	
	public BoardTroops troopStep(BoardPos origin, BoardPos target) {
		if(!this.isLeaderPlaced()) {
			throw new IllegalStateException(
					"Cannot move troops before the leader is placed.");
		}

		if(this.isPlacingGuards()) {
			throw new IllegalStateException(
					"Cannot move troops before guards are placed.");
		}

		if(!at(origin).isPresent())
			throw new IllegalArgumentException();

		Map<BoardPos, TroopTile> newTroop = new HashMap<>(troopMap);
		if( this.at(target).isPresent() ) {
			throw new IllegalArgumentException();
		}

		BoardTroops newBoard;

		if( this.leaderPosition().equals(origin) ) {
			newBoard  = new BoardTroops(this.playingSide(), newTroop, TilePos.OFF_BOARD, this.guards());
		} else {
			newBoard  = new BoardTroops(this.playingSide(), newTroop, this.leaderPosition(), this.guards());
		}

		newBoard = newBoard.placeTroop(newBoard.troopMap.get(origin).troop(), target);
		if( newBoard.troopMap.get(origin).face() == newBoard.troopMap.get(target).face() ) {
			newBoard = newBoard.troopFlip(target);
		}
		newBoard = newBoard.removeTroop(origin);

		return newBoard;

	}

	public BoardTroops troopFlip(BoardPos origin) {
		if(!isLeaderPlaced()) {
			throw new IllegalStateException(
					"Cannot move troops before the leader is placed.");			
		}
		
		if(isPlacingGuards()) {
			throw new IllegalStateException(
					"Cannot move troops before guards are placed.");			
		}
		
		if(!at(origin).isPresent())
			throw new IllegalArgumentException();
		
		Map<BoardPos, TroopTile> newTroops = new HashMap<>(troopMap);
		TroopTile tile = newTroops.remove(origin);
		newTroops.put(origin, tile.flipped());

		return new BoardTroops(playingSide(), newTroops, leaderPosition, guards);
	}
	
	public BoardTroops removeTroop(BoardPos target) {
		if(!this.isLeaderPlaced()) {
			throw new IllegalStateException(
					"Cannot move troops before the leader is placed.");
		}

		if(this.isPlacingGuards()) {
			throw new IllegalStateException(
					"Cannot move troops before guards are placed.");
		}

		if(!at(target).isPresent())
			throw new IllegalArgumentException();

		Map<BoardPos, TroopTile> newTroop = new HashMap<>(troopMap);

		TilePos newPos;
		if( this.leaderPosition().equals(target) ) {
			newPos = TilePos.OFF_BOARD;
		} else {
			newPos = this.leaderPosition();
		}

		newTroop.remove(target);

		return  new BoardTroops(this.playingSide(), newTroop, newPos, this.guards());

	}
//
//	public Object troopMap() {
//	}
}
