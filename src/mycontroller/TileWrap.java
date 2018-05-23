package mycontroller;

import tiles.MapTile;

import utilities.Coordinate;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * Class that keeps track of all the tiles and their types
 */
public class TileWrap {
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
	private static final int GRASS_COST = 10;
	private static final int MUD_COST = 10;
		
	private Coordinate coords;
	private TileType type;
	private Integer section;
	private MapTile tile;
	
	
	public TileWrap(Coordinate coords, MapTile mapTile) {
		this.coords = coords;
		this.type = getType(mapTile);
		this.section = null;
		this.tile = mapTile;
	}
	
	public TileWrap(Coordinate coords, MapTile mapTile, Integer section) {
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
	 * Returns the type of tile it is
	 * @param tile
	 * @return
	 */
	private TileType getType(MapTile tile) {
		if (tile == null) {
			return TileType.EMPTY;
		}
		
		TileType res = null;
		
//		if (tile.getType().equals(Til)) {
//			if (tile instanceof tiles.GrassTrap) {
//				res = TileType.GRASS;
//			} else if (tile instanceof tiles.MudTrap) {
//				res = TileType.MUD;
//			} else if (tile instanceof tiles.LavaTrap) {
//				res = TileType.LAVA;
//			}
//		} else {
		System.out.println(tile.getType());
		System.out.println("Getting the tile type");
			switch (tile.getType()) {
				
				case WALL: 
					res = TileType.WALL;
					break;
					
				case ROAD: 
					res = TileType.ROAD;
					break;
					
				case TRAP:
					if (tile instanceof tiles.GrassTrap) {
						res = TileType.GRASS;
					} else if (tile instanceof tiles.MudTrap) {
						res = TileType.MUD;
					} else if (tile instanceof tiles.LavaTrap) {
						res = TileType.LAVA;
					}
					break;
					
				case START: 
					
					res = TileType.START;
					break;
					
				case FINISH:
					
					res = TileType.EXIT;
					break;
					
				default: res = TileType.EMPTY;
				System.out.println(tile.getType());
				System.out.println("Empty");
			}
//		}
		
		return res;
		
	}
	
	@Override
	public String toString() {
		switch (type) {
			case WALL: return "W";
			case ROAD: return ".";
			case LAVA: return "L";
			case GRASS: return "G";
			case MUD: return "M";
			case START: return "S";
			case EXIT: return "E";
			case EMPTY: return " ";
		}
		return null;
	}

	/**
	 * Returns an approximation to the negative affect the tile will have on the car
	 * @param tileType
	 * @return
	 */
	public static int getTileCost(TileType tileType) {
		switch (tileType) {
			case LAVA: return LAVA_COST;
			case GRASS: return GRASS_COST;
			case MUD: return MUD_COST;
			default: return 0;
		}
	}
	
	
}
