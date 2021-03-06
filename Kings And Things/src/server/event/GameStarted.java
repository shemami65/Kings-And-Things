package server.event;

import server.logic.game.GameState;

import common.event.AbstractInternalEvent;

public class GameStarted extends AbstractInternalEvent{
	
	private final boolean isDemoMode;
	private final GameState currentState;

	public GameStarted(boolean isDemoMode, GameState currentState){
		super();
		this.isDemoMode = isDemoMode;
		this.currentState = currentState;
	}

	public boolean isDemoMode(){
		return isDemoMode;
	}

	public GameState getCurrentState(){
		return currentState;
	}
}
