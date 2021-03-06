package server.logic.game.handlers;

import java.util.Collection;
import java.util.List;

import server.event.internal.MoveThingsCommand;
import server.logic.game.validators.MovementValidator;

import com.google.common.eventbus.Subscribe;

import common.Constants;
import common.Logger;
import common.event.network.CommandRejected;
import common.event.network.HexStatesChanged;
import common.game.HexState;
import common.game.ITileProperties;

public class MovementCommandHandler extends CommandHandler
{
	/**
	 * Call this to move creatures during the movement phase
	 * @param things The list of things the player wants to move
	 * @param playerNumber The player who sent the command
	 * @param hexes The hexes the player wants to move through
	 */
	public void moveThings(Collection<ITileProperties> things, int playerNumber, List<ITileProperties> hexes)
	{
		MovementValidator.validateCanMove(playerNumber, getCurrentState(), hexes, things);
		makeThingsMoved(things, playerNumber, hexes);
	}
	
	private void makeThingsMoved(Collection<ITileProperties> things, int playerNumber, List<ITileProperties> hexes)
	{
		int moveCost = 0;
		for(int i=1; i<hexes.size(); i++)
		{
			moveCost += hexes.get(i).getMoveSpeed();
		}
		
		HexState firstHex = getCurrentState().getBoard().getHexStateForHex(hexes.get(0));
		HexState lastHex = getCurrentState().getBoard().getHexStateForHex(hexes.get(hexes.size()-1));
		
		for(ITileProperties thing : things)
		{
			thing.setMoveSpeed(thing.getMoveSpeed() - moveCost);
			firstHex.removeThingFromHex(thing);
			lastHex.addThingToHex(thing);
		}
		
		HexStatesChanged notification = new HexStatesChanged(2);
		notification.getArray()[0] = firstHex;
		notification.getArray()[1] = lastHex;
		notification.postNetworkEvent(Constants.ALL_PLAYERS_ID);
	}

	@Subscribe
	public void recieveMoveThingsCommand(MoveThingsCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				moveThings(command.getThings(),command.getID(), command.getHexes());

				HexStatesChanged changedHex = new HexStatesChanged(2);
				changedHex.getArray()[0] = getCurrentState().getBoard().getHexStateForHex(command.getHexes().get(0));
				changedHex.getArray()[0] = getCurrentState().getBoard().getHexStateForHex(command.getHexes().get(1));
				changedHex.postNetworkEvent();
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process MoveThingsCommand due to: ", t);
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage(),null).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}
}
