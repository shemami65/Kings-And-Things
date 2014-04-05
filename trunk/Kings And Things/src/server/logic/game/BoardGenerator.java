package server.logic.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import server.logic.exceptions.NoMoreTilesException;
import common.Constants;
import common.Constants.Biome;
import common.game.ITileProperties;

/**
 * This class creates a playing board according to the rules for removing
 * certain hexes before generating the board, also contains methods for putting
 * hexes back.
 */
public class BoardGenerator implements Serializable
{
	private static final long serialVersionUID = 3289384481395756865L;
	
	private final int numPlayers;
	private final HexTileManager hexManager;
	private final HashSet<ITileProperties> temporarilyRemovedHexes;
	
	/**
	 * Creates a new BoardGenerator, setup to create boards for the specified
	 * number of players.
	 * @param numPlayers The number of players that will be playing on the generated
	 * board. Must be between 2 and 4, both inclusive.
	 * @param hexManager The HexTileManager that represents the hexes from the bank.
	 * @throws IllegalArgumentException if the entered number of players is not between 2
	 * and 4 or if hexManager is null
	 */
	public BoardGenerator(int numPlayers, HexTileManager hexManager)
	{
		if(numPlayers < Constants.MIN_PLAYERS || Constants.MAX_PLAYERS < numPlayers)
		{
			throw new IllegalArgumentException("Can not generate board with: " + numPlayers + " players");
		}
		if(hexManager==null)
		{
			throw new IllegalArgumentException("The hex manager must not be null");
		}
		this.numPlayers = numPlayers;
		this.hexManager = hexManager;
		temporarilyRemovedHexes = new HashSet<ITileProperties>();
	}
	
	public BoardGenerator(BoardGenerator other)
	{
		numPlayers = other.numPlayers;
		hexManager = other.hexManager.clone();
		temporarilyRemovedHexes = Constants.deepCloneCollection(other.temporarilyRemovedHexes,new HashSet<ITileProperties>());
	}
	
	@Override
	public BoardGenerator clone()
	{
		return new BoardGenerator(this);
	}
	
	/**
	 * Call this method to generate a new board to play on. All hexes
	 * will be face down.
	 * @return A HexBoard created according to this BoardGenerator's
	 * constructor parameters.
	 * @throws NoMoreTilesException If the board could not be created
	 * due to insufficient hex tiles, remember to put hexes back when
	 * finished with a board.
	 */
	public HexBoard createNewBoard() throws NoMoreTilesException
	{
		if(numPlayers == Constants.MAX_PLAYERS)
		{
			tempRemoveHexesOfType(Biome.Sea, 4);
		}
		else
		{
			for(Biome b : Biome.values())
			{
				tempRemoveHexesOfType(b, 2);
			}
			tempRemoveHexesOfType(Biome.Sea, 3);
		}
		
		ArrayList<ITileProperties> hexes = new ArrayList<ITileProperties>();
		
		int boardSize = numPlayers==Constants.MAX_PLAYERS? Constants.MAX_HEXES_ON_BOARD : Constants.MIN_HEXES_ON_BOARD;
		for(int i=0; i<boardSize; i++)
		{
			ITileProperties hex = hexManager.drawTile();
			if(hex.isFaceUp())
			{
				hex.flip();
			}
			hexes.add(hex);
		}
		
		return new HexBoard(hexes);
	}
	
	/**
	 * Call this method to put a hex from a board generated by this
	 * class back into the bank so that it can be drawn again later.
	 * @param hex the hex to put back
	 * @throws IllegalArgumentException if hex is null, or does not
	 * represent a hex tile.
	 */
	public void putHexBack(ITileProperties hex)
	{
		hexManager.reInsertTile(hex);
	}
	
	/**
	 * During board construction some hexes are temporarily removed
	 * from the bank, call this method after the player setup phase
	 * is done, to put them back into the bank.
	 */
	public void setupFinished()
	{
		for(ITileProperties tp : temporarilyRemovedHexes)
		{
			putHexBack(tp);
		}
		
		temporarilyRemovedHexes.clear();
	}
	
	/**
	 * Call this method to mark a hex as needing to be put back in the bank.
	 * The hex will be put back with the next call to setupFinished().
	 * @param hex The hex to place aside
	 * @throws IllegalArgumentException if hex is null or does not represent
	 * a hex tile.
	 */
	public void placeHexAside(ITileProperties hex)
	{
		if(hex == null)
		{
			throw new IllegalArgumentException("The entered hex tile must not be null.");
		}
		if(!hex.isHexTile())
		{
			throw new IllegalArgumentException("The entered tile must be a hex tile.");
		}
		temporarilyRemovedHexes.add(hex);
	}
	
	private void tempRemoveHexesOfType(Biome type, int num) throws NoMoreTilesException
	{
		for(int i=0; i<num; i++)
		{
			temporarilyRemovedHexes.add(hexManager.drawHexTileByType(type));
		}
	}
}
