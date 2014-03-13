package common.game;

import common.Constants.Ability;
import common.Constants.Biome;
import common.Constants.Category;
import common.Constants.Restriction;


public class TwoSidedTileProperties implements ITileProperties {
	
	private static final long serialVersionUID = -6715600368392464765L;
	
	private boolean isFaceUp;
	private final TileProperties faceUp;	// keeps track of the face up property of tile
	private final TileProperties faceDown;  // keeps track of the face up property of tile
	
	public TwoSidedTileProperties (TileProperties faceUp, TileProperties faceDown) {
		this.faceUp = faceUp;
		this.faceDown = faceDown;
		isFaceUp = true;
	}

	@Override
	public Category getCategory() {
		if (isFaceUp()) {
			return faceUp.getCategory();
		} else {
			return faceDown.getCategory();
		}
	}

	@Override
	public int getNumber() {
		if (isFaceUp()) {
			return faceUp.getNumber();
		} else {
			return faceDown.getNumber();
		}
	}

	@Override
	public int getValue() {
		if (isFaceUp()) {
			return faceUp.getValue();
		} else {
			return faceDown.getValue();
		}
	}

	@Override
	public void setValue(int value) {
		if (isFaceUp()) {
			faceUp.setValue(value);
		} else {
			faceDown.setValue(value);
		}
	}

	@Override
	public void resetValue() {
		if (isFaceUp()) {
			faceUp.resetValue();
		} else {
			faceDown.resetValue();
		}
	}

	@Override
	public int getMoveSpeed() {
		if (isFaceUp()) {
			return faceUp.getMoveSpeed();
		} else {
			return faceDown.getMoveSpeed();
		}
	}

	@Override
	public void setMoveSpeed(int moveSpeed) {
		if (isFaceUp()) {
			faceUp.setMoveSpeed(moveSpeed);
		} else {
			faceDown.setMoveSpeed(moveSpeed);
		}
	}

	@Override
	public String getName() {
		if (isFaceUp()) {
			return faceUp.getName();
		} else {
			return faceDown.getName();
		}
	}

	@Override
	public Ability[] getAbilities() {
		if (isFaceUp()) {
			return faceUp.getAbilities();
		} else {
			return faceDown.getAbilities();
		}
	}

	@Override
	public boolean isFaceUp() {
		return isFaceUp;
	}

	@Override
	public boolean isInfinit() {
		return faceUp.isInfinit();
	}

	@Override
	public boolean isBuilding() {
		return faceUp.isBuilding();
	}

	@Override
	public boolean isBuildableBuilding() {
		return faceUp.isBuildableBuilding();
	}

	@Override
	public boolean isRestrictedToBiome() {
		return false;
	}

	@Override
	public boolean isCreature() {
		if (isFaceUp()) {
			return faceUp.isCreature();
		} else {
			return faceDown.isCreature();
		}
	}
	
	@Override
	public boolean isSpecialCharacter()
	{
		return faceUp.isSpecialCharacter();
	}

	@Override
	public boolean hasAbility(Ability ability) {
		if (isFaceUp()) {
			return faceUp.hasAbility(ability);
		} else {
			return faceDown.hasAbility(ability);
		}
	}

	@Override
	public boolean isSpecialCreatureWithAbility(Ability ability) {
		if (isFaceUp()) {
			return faceUp.isSpecialCreatureWithAbility(ability);
		} else {
			return faceDown.isSpecialCreatureWithAbility(ability);
		}
	}

	@Override
	public Biome getBiomeRestriction() {
		return faceUp.getBiomeRestriction();
	}

	@Override
	public Restriction getRestriction(int index) {
		return faceUp.getRestriction(index);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + faceUp.hashCode();
		result = prime * result + faceDown.hashCode();
		result = prime * result + (isFaceUp? 1 : 0);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj) {
			return true;
		}
		if ( obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}
		TwoSidedTileProperties o = (TwoSidedTileProperties) obj;
		return o.faceDown.equals(faceDown) && faceUp.equals(o.faceUp) && isFaceUp == o.isFaceUp;
	}

	@Override
	public String toString() {
		return "Side 1: " + faceUp + "\nSide 2: " + faceDown;
	}

	@Override
	public boolean hasRestriction() {
		if (isFaceUp()) {
			return faceUp.hasRestriction();
		} else {
			return faceDown.hasRestriction();
		}
	}

	@Override
	public void flip()
	{
		isFaceUp = !isFaceUp;
	}

	@Override
	public boolean hasFlip()
	{
		return true;
	}

	@Override
	public boolean isHexTile()
	{
		return false;
	}

	@Override
	public boolean isEvent()
	{
		return false;
	}

	@Override
	public boolean isMagicItem()
	{
		return false;
	}

	@Override
	public boolean isTreasure()
	{
		return false;
	}

	@Override
	public boolean isSpecialIncomeCounter()
	{
		return false;
	}

	@Override
	public boolean isFake()
	{
		return false;
	}
}