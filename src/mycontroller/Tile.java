package mycontroller;

import tiles.MapTile;

import utilities.Coordinate;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * 
 * Class that translates a MapTile to a Tile so that we can use it in the Map and Path Finders.
 */
public class Tile {
	
	public static enum TileType {
		ROAD,
		WALL,
		GRASS,
		LAVA,
		MUD,
		START,
		EXIT,
		EMPTY
	}

	private static final int LAVA_COST = 50;
		
	private Coordinate coords;
	private TileType type;
	private Integer section;
	private MapTile tile;
	
	public Tile(Coordinate coords, MapTile mapTile) {
		this.coords = coords;
		this.type = getType(mapTile);
		this.section = null;
		this.tile = mapTile;
	}
	
	public Tile(Coordinate coords, MapTile mapTile, Integer section) {
		this.coords = coords;
		this.type = getType(mapTile);
		this.section = section;
	}
	
	public Integer getSection() {
		return section;
	}

	public void setSection(Integer section) {
		this.section = section;
	}

	public Coordinate getCoords() {
		return coords;
	}

	public TileType getType() {
		return type;
	}
	
	public MapTile getTile() {
		return tile;
	}

	/**
	 * Returns the type of tile according to our newly defined TileType enum
	 * 
	 * @param MapTile tile
	 * @return TileType
	 */
	private TileType getType(MapTile tile) {
		if (tile == null) {
			return TileType.EMPTY;
		}
		
		TileType tileType = null;
		
		switch (tile.getType()) {
			
			case WALL: 
				tileType = TileType.WALL;
				break;
				
			case ROAD: 
				tileType = TileType.ROAD;
				break;
				
			case TRAP:
				if (tile instanceof tiles.GrassTrap) {
					tileType = TileType.GRASS;
				} else if (tile instanceof tiles.MudTrap) {
					tileType = TileType.MUD;
				} else if (tile instanceof tiles.LavaTrap) {
					tileType = TileType.LAVA;
				}
				break;
				
			case START: 
				
				tileType = TileType.START;
				break;
				
			case FINISH:
				
				tileType = TileType.EXIT;
				break;
				
			default: tileType = TileType.EMPTY;
			
		}
		
		return tileType;
		
	}

	/**
	 * Only the lava tile has the cost, all others have a cost of 0.
	 * 
	 * @param tileType
	 * @return
	 */
	public static int getTileCost(TileType tileType) {
		
		if(tileType == Tile.TileType.LAVA) {
			return LAVA_COST;
		}
		return 0;
	}
	
	
}
