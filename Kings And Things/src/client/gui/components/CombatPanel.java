package client.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import client.gui.Board;
import client.gui.components.combat.AbstractCombatArmyPanel;
import client.gui.components.combat.ActiveCombatArmyPanel;
import client.gui.components.combat.InactiveCombatArmyPanel;
import client.gui.components.combat.RetreatPanel;
import client.gui.components.combat.RollForDamagePanel;

import com.google.common.eventbus.Subscribe;

import common.Constants;
import common.Constants.CombatPhase;
import common.Constants.HexContentsTarget;
import common.Constants.UpdateInstruction;
import common.Constants.UpdateKey;
import common.event.EventDispatch;
import common.event.UpdatePackage;
import common.event.network.CurrentPhase;
import common.event.network.HexStatesChanged;
import common.event.network.ViewHexContentsResponse;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;

public class CombatPanel extends JPanel
{
	private static final long serialVersionUID = -8151724738245642539L;
	
	private HexState hs;
	private final Player p;
	private final ActiveCombatArmyPanel playerPanel;
	private final ArrayList<InactiveCombatArmyPanel> otherArmies;
	private final JScrollPane scrollPane;
	private final JLabel combatPhaseLabel;
	private final ArrayList<Player> allPlayersInCombat;
	private final ArrayList<Integer> playerOrderList;
	private final Player defendingPlayer;
	private final HashSet<HexState> adjacentPlayerOwnedHexes;
	private CombatPhase currentPhase;
	private final JFrame parent;
	private final AtomicBoolean isClosing = new AtomicBoolean();

	public CombatPanel(HexState hs, Collection<HexState> adjacentPlayerOwnedHexes, Player p, Collection<Player> otherPlayers, CombatPhase currentPhase, Player defendingPlayer, Collection<Integer> playerOrder, JFrame parent)
	{
		this.p = p;
		this.parent = parent;
		parent.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.hs = hs;
		this.currentPhase = currentPhase;
		HashSet<Player> allPlayers = new HashSet<Player>();
		allPlayers.add(p);
		allPlayers.addAll(otherPlayers);
		Set<ITileProperties> explorationDefenders = hs.getFightingThingsInHexNotOwnedByPlayers(allPlayers);
		this.adjacentPlayerOwnedHexes = new HashSet<HexState>(adjacentPlayerOwnedHexes.size());
		for(HexState adjacentHs : adjacentPlayerOwnedHexes)
		{
			this.adjacentPlayerOwnedHexes.add(adjacentHs);
		}
		playerOrderList = new ArrayList<Integer>(playerOrder.size());
		for(Integer i : playerOrder)
		{
			playerOrderList.add(i);
		}
		allPlayersInCombat = new ArrayList<Player>();
		allPlayersInCombat.add(p);
		this.defendingPlayer = defendingPlayer;
		
		setLayout(new GridBagLayout());
		playerPanel = new ActiveCombatArmyPanel(p.getName(), p.getID(), "No one");
		if(p.getID() == defendingPlayer.getID() && explorationDefenders.size()>0)
		{
			playerPanel.init(explorationDefenders);
		}
		else
		{
			playerPanel.init(hs.getFightingThingsInHexOwnedByPlayer(p));
		}
		
		otherArmies = new ArrayList<InactiveCombatArmyPanel>(otherPlayers.size());
		for(Player otherPlayer : otherPlayers)
		{
			allPlayersInCombat.add(otherPlayer);
			InactiveCombatArmyPanel otherPanel = new InactiveCombatArmyPanel(otherPlayer.getName(),otherPlayer.getID(),"No one");

			if(otherPlayer.getID() == defendingPlayer.getID() && explorationDefenders.size()>0)
			{
				otherPanel.init(explorationDefenders);
			}
			else
			{
				otherPanel.init(hs.getFightingThingsInHexOwnedByPlayer(otherPlayer));
			}
			
			otherArmies.add(otherPanel);
		}
		scrollPane = new JScrollPane();
		combatPhaseLabel = new JLabel();
	}
	
	public void init()
	{
		JPanel contentsPanel = new JPanel();
		contentsPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		
		contentsPanel.add(playerPanel,constraints);
		constraints.gridx++;

		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.NONE;
		for(AbstractCombatArmyPanel panel : otherArmies)
		{
			contentsPanel.add(panel,constraints);
			constraints.gridx++;
		}
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weighty = 0;
		updateCombatPhaseLabel();
		combatPhaseLabel.setHorizontalAlignment(SwingConstants.CENTER);
		combatPhaseLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		combatPhaseLabel.setFont(Board.STATUS_INDICATOR_FONT);
		contentsPanel.add(combatPhaseLabel,constraints);
		
		setLayout(new GridBagLayout());
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(contentsPanel);
		add(scrollPane,constraints);
		
		playerPanel.addTargetSelectActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				String[] targetNames = new String[otherArmies.size()];
				for(int i=0; i<targetNames.length; i++)
				{
					targetNames[i] = otherArmies.get(i).getPlayerName();
				}
				String currentTarget = playerPanel.getTargetPlayerName();
				String targetName = (String) JOptionPane.showInputDialog(playerPanel, "Select the player you would like to target", "Change Target", JOptionPane.PLAIN_MESSAGE, null, targetNames, currentTarget.equals("No one")? targetNames[0] : currentTarget);
				int targetID = Constants.PUBLIC;
				for(InactiveCombatArmyPanel panel : otherArmies)
				{
					if(panel.getPlayerName().equals(targetName))
					{
						targetID = panel.getPlayerID();
					}
				}
				new UpdatePackage(UpdateInstruction.TargetPlayer, UpdateKey.Player, targetID, "Combat Panel for: " + playerPanel.getPlayerName()).postNetworkEvent(playerPanel.getPlayerID());
			}});
		playerPanel.addFightOnButtonListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				new UpdatePackage(UpdateInstruction.Skip, "Combat Panel for: " + playerPanel.getPlayerName()).postNetworkEvent(playerPanel.getPlayerID());
			}});
		playerPanel.addRetreatButtonListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				SwingWorker<Void,Void> retreatDialogBuilder = new SwingWorker<Void,Void>(){
					private volatile ViewHexContentsResponse response = null;
					
					@Override
					protected Void doInBackground() throws Exception
					{
						for(HexState hs : adjacentPlayerOwnedHexes)
						{
							UpdatePackage msg = new UpdatePackage(UpdateInstruction.ViewContents, UpdateKey.Hex, hs.getHex(), "Retreat Panel for player number: " + playerPanel.getPlayerID());
							msg.putData(UpdateKey.Category,HexContentsTarget.RETREAT);
							msg.postNetworkEvent(playerPanel.getPlayerID());
							while(response==null)
							{
								Thread.sleep(100);
							}
							HashSet<ITileProperties> thingsInHex = new HashSet<ITileProperties>(hs.getThingsInHex());
							for(ITileProperties thing : thingsInHex)
							{
								hs.removeThingFromHex(thing);
							}
							for(ITileProperties thing : response.getContents())
							{
								hs.addThingToHex(thing);
							}
							response = null;
						}
						return null;
					}
					
					@Override
					protected void done()
					{
						JFrame retreatDialog = new JFrame("Retreat!");
						retreatDialog.add(new RetreatPanel(adjacentPlayerOwnedHexes,hs,playerPanel.getPlayerID(),retreatDialog));
						retreatDialog.pack();
						retreatDialog.setLocationRelativeTo(null);
						retreatDialog.setVisible(true);
						EventDispatch.unregisterFromInternalEvents(this);
					}
					
					@Subscribe
					public void recieveHexContents(ViewHexContentsResponse response)
					{
						if(response.getTarget() == HexContentsTarget.RETREAT)
						{
							this.response = response;
						}
					}};
				EventDispatch.registerOnInternalEvents(retreatDialogBuilder);
				retreatDialogBuilder.execute();
			}});

		EventDispatch.registerOnInternalEvents(this);
	}
	
	private String getPlayerNameByAttackerNumber(int num)
	{
		int defenderIndex = playerOrderList.indexOf(defendingPlayer.getID());
		int attackerID = playerOrderList.get((defenderIndex + num) % playerOrderList.size());
		for(Player p : allPlayersInCombat)
		{
			if(p.getID() == attackerID)
			{
				return p.getName();
			}
		}
		
		throw new IllegalStateException("Unable to find player with ID: " + attackerID);
	}
	
	private void setCombatPhase(CombatPhase phase)
	{
		currentPhase = phase;
		updateCombatPhaseLabel();
	}
	
	private void updateCombatPhaseLabel()
	{
		String phaseText = "";
		switch(currentPhase)
		{
			case APPLY_RANGED_HITS:
			case APPLY_MELEE_HITS:
			case APPLY_MAGIC_HITS:
			{
				phaseText = "Apply Damage";
				break;
			}
			case ATTACKER_ONE_RETREAT:
			{
				phaseText = getPlayerNameByAttackerNumber(1) + " Retreat";
				break;
			}
			case ATTACKER_THREE_RETREAT:
			{
				phaseText = getPlayerNameByAttackerNumber(3) + " Retreat";
				break;
			}
			case ATTACKER_TWO_RETREAT:
			{
				phaseText = getPlayerNameByAttackerNumber(2) + " Retreat";
				break;
			}
			case DEFENDER_RETREAT:
			{
				phaseText = getPlayerNameByAttackerNumber(0) + " Retreat";
				break;
			}
			case DETERMINE_DAMAGE:
			{
				if(isClosing.compareAndSet(false, true))
				{
					if(!isStillInCombat())
					{
						JOptionPane.showMessageDialog(this, "Your rag tag army has been destroyed!", "Defeat!", JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane.showMessageDialog(this, "Your enemies have been destroyed!", "Victory!", JOptionPane.INFORMATION_MESSAGE);
						JFrame determineDamageFrame = new JFrame("Determine damage to hex");
						RollForDamagePanel panel = new RollForDamagePanel(hs, p, determineDamageFrame);
						panel.init();
						determineDamageFrame.setContentPane(panel);
						determineDamageFrame.pack();
						determineDamageFrame.setLocationRelativeTo(null);
						determineDamageFrame.setVisible(true);
					}
					close();
				}
				break;
			}
			case DETERMINE_DEFENDERS:
			{
				phaseText = "Determine Defenders";
				break;
			}
			case MAGIC_ATTACK:
			{
				phaseText = "Magic Attack";
				break;
			}
			case MELEE_ATTACK:
			{
				phaseText = "Melee Attack";
				break;
			}
			case NO_COMBAT:
			{
				if(isClosing.compareAndSet(false, true))
				{
					if(!isStillInCombat())
					{
						JOptionPane.showMessageDialog(this, "Your rag tag army has been destroyed!", "Defeat!", JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane.showMessageDialog(this, "Your enemies have been destroyed!", "Victory!", JOptionPane.INFORMATION_MESSAGE);
					}
					close();
					phaseText = "No Combat";
				}
				break;
			}
			case PLACE_THINGS:
			{
				if(isClosing.compareAndSet(false, true))
				{
					if(!isStillInCombat())
					{
						JOptionPane.showMessageDialog(this, "Your rag tag army has been destroyed!", "Defeat!", JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane.showMessageDialog(this, "Your enemies have been destroyed!", "Victory!", JOptionPane.INFORMATION_MESSAGE);
					}
					close();
				}
				break;
			}
			case RANGED_ATTACK:
			{
				phaseText = "Ranged Attack";
				break;
			}
			case SELECT_TARGET_PLAYER:
			{
				phaseText = "Select Target Player";
				break;
			}
			case BRIBE_CREATURES:
			{
				phaseText = "Select Creatures to Bribe";
				break;
			}
		}
		combatPhaseLabel.setText(phaseText);
	}
	
	private void combatHexChanged(HexState hex, boolean isRetreat)
	{
		hs = hex;
		playerPanel.removeThingsNotInList(hex.getFightingThingsInHex(), isRetreat);
		
		for(AbstractCombatArmyPanel p : otherArmies)
		{
			p.removeThingsNotInList(hex.getFightingThingsInHex(), isRetreat);
		}
		if(!isStillInCombat())
		{
			if(isClosing.compareAndSet(false, true))
			{
				JOptionPane.showMessageDialog(this, isRetreat?"Your cowardice has cost you the battle!":"Your rag tag army has been destroyed!", "Defeat!", JOptionPane.INFORMATION_MESSAGE);
				close();
			}
		}
		validate();
	}
	
	private boolean isStillInCombat()
	{
		return hs.getFightingThingsInHexOwnedByPlayer(p).size()>0 || (p.getID() == defendingPlayer.getID() && hs.getFightingThingsInHexNotOwnedByPlayers(allPlayersInCombat).size()>0);
	}
	
	private void close()
	{
		EventDispatch.unregisterFromInternalEvents(playerPanel);
		for(AbstractCombatArmyPanel p : otherArmies)
		{
			EventDispatch.unregisterFromInternalEvents(p);
		}
		EventDispatch.unregisterFromInternalEvents(this);
		parent.dispose();
	}
	
	@Subscribe
	public void recieveHexChanged(final HexStatesChanged evt)
	{
		for(final HexState hs : evt.getArray())
		{
			if(hs.getHex().equals(this.hs.getHex()))
			{
				Runnable logic = new Runnable(){
					@Override
					public void run(){
						combatHexChanged(hs,evt.getArray().length==2);
					}
				};
				if(!SwingUtilities.isEventDispatchThread())
				{
					SwingUtilities.invokeLater(logic);
				}
				else
				{
					logic.run();
				}
			}
		}
	}

	@Subscribe
	public void recieveCombatPhaseChanged(final CurrentPhase<CombatPhase> evt)
	{
		Runnable logic = new Runnable(){
			@Override
			public void run(){
				setCombatPhase(evt.getPhase());
			}
		};
		if(!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(logic);
		}
		else
		{
			logic.run();
		}
	}
}
