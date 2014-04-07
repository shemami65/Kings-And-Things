package client.gui.components;

import java.awt.FlowLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import common.Constants;
import common.game.HexState;
import common.game.ITileProperties;

public class HexContentsPanel extends JPanel
{
	private static final long serialVersionUID = 7410672134640031418L;
	
	private final HexState model;
	private final boolean myHex;
	
	/**
	 * Simple panel for displaying a hex's contents
	 * @param model The hex to display contents of
	 */
	public HexContentsPanel(HexState model, boolean myHex)
	{
		this.model = model;
		this.myHex = myHex;
		init();
	}
	
	private void init()
	{
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		for(ITileProperties tp : model.getThingsInHex())
		{
			if(tp.isCreature() && !tp.isSpecialCharacter())
			{
				if((myHex && !tp.isFaceUp()) || (!myHex && tp.isFaceUp()))
				{
					tp.flip();
				}
			}
			Image tileImage = Constants.getImageForTile(tp);
			add(new JLabel(new ImageIcon(tileImage)));
		}
	}
}
