package org.gs.game.gostop.dlg;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.gs.game.gostop.Main;
import org.gs.game.gostop.MainFrame;
import org.gs.game.gostop.Resource;
import org.gs.game.gostop.config.BonusCards;
import org.gs.game.gostop.config.GameType;
import org.gs.game.gostop.config.GameUser;

/**
 * 게임 설정 대화상자
 */
public class SetupGameDlg extends JDialog implements ActionListener
{
    private static final long serialVersionUID = -2779796337833783171L;
    
    private static final String DLG_TITLE = "setup.game.title";
    private static final String GAME_TYPE_LBL = "setup.game.type";
    private static final String GAME_MONEY_LBL = "setup.game.money.per.point";
    private static final String BONUS_CARDS_LBL = "setup.game.bonus.cards";
    private static final String BONUS_CARDS_FORMAT = "setup.game.bonus.cards.format";
    private static final String PLAY_WITH_COM_LBL = "setup.game.play.with.computer";
    private static final String GAME_PLAYERS_LBL = "setup.game.players";
    private static final String USER_FORMAT = "setup.game.player.format";
    private static final String PLAYER_FORMAT = "setup.game.player.label";

    private HashMap<JRadioButton,GameType> mapRbGameType;
    private JComboBox moneyCombo;
    private JComboBox bonusCombo;
    private JCheckBox playWithComputer;
    private Box players;
    
    private GameType selectedGame;
    private BonusCards bonusCards;
    private java.util.List<GameUser> gamePlayers;
    
    private JButton okButton;
    private JButton cancelButton;
    
    private static class ComputerPlayer
    {
        private GameUser gu;
        private String formatted;
        
        private ComputerPlayer(GameUser gu)
        {
            this.gu = gu;
            formatted = null;
        }
        
        public String toString()
        {
            if (formatted == null)
            {
                formatted = Resource.format(USER_FORMAT, gu.getUserAlias(), gu.getMoney(),
                                            gu.getWins(), gu.getDraws(), gu.getLoses());
            }
            
            return formatted;
        }
    }
    
    /**
     * PlayerComboModel shares the list of computer players,
     * but it excludes selected players from other PlayerComboModel instances 
     */
    private static class PlayerComboModel extends DefaultComboBoxModel
    {
        private static final long serialVersionUID = 4332098216717027548L;
        private static final java.util.List<PlayerComboModel> models = new ArrayList<PlayerComboModel>();
        private static ComputerPlayer[] computerPlayers = null;
        
        public PlayerComboModel()
        {
            synchronized (models)
            {
                ComputerPlayer leastPlayed = null;
                int leastGames = Integer.MAX_VALUE;
                
                for (ComputerPlayer player: getComputerPlayers())
                {
                    boolean selected = false;
                    
                    for (int i = 0; i < models.size() && selected == false; i++)
                        selected = player.equals(models.get(i).getSelectedItem());
                    
                    if (selected == false)
                    {
                       addElement(player);
                       
                       GameUser gu = player.gu;
                       int games = gu.getWins() + gu.getDraws() + gu.getLoses();
                       if (games < leastGames)
                       {
                           leastPlayed = player;
                           leastGames = games;
                       }
                    }
                }
                
                setSelectedItem(leastPlayed);
                models.add(this);
            }
        }

        private ComputerPlayer[] getComputerPlayers()
        {
            if (computerPlayers == null)
            {
                GameUser[] comUsers = GameUser.getComputerUsers();
                
                if (comUsers == null)
                    computerPlayers = new ComputerPlayer[0];
                else
                {
                    computerPlayers = new ComputerPlayer[comUsers.length];
                    
                    for (int i = 0; i < comUsers.length; i++)
                        computerPlayers[i] = new ComputerPlayer(comUsers[i]);
                }
            }
            
            return computerPlayers;
        }
        
        public void setSelectedItem(Object selected)
        {
            Object preSelected = getSelectedItem();
            
            if (selected != preSelected)
            {
                super.setSelectedItem(selected);
    
                synchronized (models)
                {
                    for (PlayerComboModel model: models)
                    {
                        if (model != this)
                        {
                            int index = model.getIndexOf(selected);
                            
                            if (index >= 0)
                            {
                                model.removeElementAt(index);
                                model.fireIntervalRemoved(model, index, index);
                            }
                            
                            if (preSelected != null)
                            {
                                model.addElement(preSelected);
                                index = model.getIndexOf(preSelected);
                                model.fireIntervalAdded(model, index, index);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static class BonusCardsEntry
    {
        private BonusCards bcs;
        private String formatted;
        
        private BonusCardsEntry(BonusCards bonusCards)
        {
            this.bcs = bonusCards;
        }
        
        public String toString()
        {
            if (formatted == null)
            {
                formatted = Resource.format(BONUS_CARDS_FORMAT, bcs.getTripleCount(),
                                            bcs.getDoubleCount(), bcs.getKingCount());
            }
            
            return formatted;
        }
        
        public boolean equals(Object obj)
        {
            boolean result;
            
            if (obj instanceof BonusCardsEntry)
                result = ((BonusCardsEntry)obj).bcs.equals(bcs);
            else
                result = super.equals(obj);
            
            return result;
        }
    }
    
    public SetupGameDlg(Frame parent)
    {
        super(parent, Resource.getProperty(DLG_TITLE), true);
        
        selectedGame = null;
        bonusCards = null;
        gamePlayers = null;
        
        initContentPane();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getRootPane().setDefaultButton(okButton);
    }
    
    private void initContentPane()
    {
        Container contentPane = getContentPane();
        JPanel mainPanel;
        GameUser gameUser = MainFrame.getGameUser();

        if (contentPane instanceof JPanel)
            mainPanel = (JPanel)contentPane;
        else
        {
            mainPanel = new JPanel();
            setContentPane(mainPanel);
        }
        
        mainPanel.setBorder(new CompoundBorder(mainPanel.getBorder(),
                            new EmptyBorder(10,10,10,10)));   // margins
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        Container boxOuter = new Container();
        GridBagLayout gbl = new GridBagLayout();
        boxOuter.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        
        Box gameTypeBox = Box.createVerticalBox();
        ButtonGroup bg = new ButtonGroup();
        gameTypeBox.setBorder(new TitledBorder(Resource.getProperty(GAME_TYPE_LBL)));
        mapRbGameType = new HashMap<JRadioButton,GameType>();
        for (GameType gt: GameType.getGameTypes())
        {
            JRadioButton rb = new JRadioButton(Resource.getProperty(gt.getTypeId()));
            rb.setAlignmentX(LEFT_ALIGNMENT);
            rb.addActionListener(this);
            rb.setEnabled(false);  // for now
            gameTypeBox.add(rb);
            bg.add(rb);
            mapRbGameType.put(rb, gt);
        }
        bg.getElements().nextElement().setSelected(true);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(gameTypeBox, gbc);
        boxOuter.add(gameTypeBox);

        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.weightx = 1.0;
        JLabel gameMoneyLabel = new JLabel(Resource.getProperty(GAME_MONEY_LBL));
        gbl.setConstraints(gameMoneyLabel, gbc);
        boxOuter.add(gameMoneyLabel);
        moneyCombo = new JComboBox(GameType.getGameMoneyTypes());
        int moneyType = gameUser.getGameMoneyType();
        if (moneyType > 0)
            moneyCombo.setSelectedItem(moneyType);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(moneyCombo, gbc);
        boxOuter.add(moneyCombo);
        
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.weightx = 1.0;
        JLabel bonusCardsLabel = new JLabel(Resource.getProperty(BONUS_CARDS_LBL));
        gbl.setConstraints(bonusCardsLabel, gbc);
        boxOuter.add(bonusCardsLabel);
        bonusCombo = new JComboBox(getBonusCardsEntries());
        if (gameUser.getGameBonusCards() != null)
            bonusCombo.setSelectedItem(new BonusCardsEntry(gameUser.getGameBonusCards()));
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(bonusCombo, gbc);
        boxOuter.add(bonusCombo);
        
        playWithComputer = new JCheckBox(Resource.getProperty(PLAY_WITH_COM_LBL), true);
        playWithComputer.setEnabled(false); // for now
        playWithComputer.addActionListener(this);
        gbl.setConstraints(playWithComputer, gbc);
        boxOuter.add(playWithComputer);
        
        players = Box.createVerticalBox();
        players.setBorder(new TitledBorder(Resource.getProperty(GAME_PLAYERS_LBL)));
        showPlayers(playWithComputer.isSelected());
        gbl.setConstraints(players, gbc);
        boxOuter.add(players);
        
        mainPanel.add(boxOuter);
        mainPanel.add(Box.createVerticalStrut(6));
        
        Box boxButtons = Box.createHorizontalBox();
        boxButtons.add(Box.createHorizontalGlue());
        okButton = new JButton(Resource.getStandardProperty(Resource.STD_OK_BUTTON));
        boxButtons.add(okButton);
        okButton.addActionListener(this);
        cancelButton = new JButton(Resource.getStandardProperty(Resource.STD_CANCEL_BUTTON));
        cancelButton.addActionListener(this);
        boxButtons.add(cancelButton);
        mainPanel.add(boxButtons);
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == playWithComputer)
            showPlayers(playWithComputer.isSelected());
        else if (mapRbGameType.get(e.getSource()) != null)
            showPlayers(playWithComputer.isSelected());
        else if (e.getSource() == cancelButton)
            dispose();
        else if (e.getSource() == okButton)
        {
            GameUser gameUser = MainFrame.getGameUser();
            
            selectedGame = getGameType();
            Integer gameMoney = (Integer)moneyCombo.getSelectedItem();
            selectedGame.setGameMoney(gameMoney);
            gameUser.setGameMoneyType(gameMoney);
            
            gamePlayers = new ArrayList<GameUser>();
            for (Component c: players.getComponents())
            {
                JComboBox cbo = (JComboBox)((JComponent)c).getComponent(1);
                gamePlayers.add(((ComputerPlayer)cbo.getSelectedItem()).gu);
            }
            
            bonusCards = ((BonusCardsEntry)bonusCombo.getSelectedItem()).bcs;
            gameUser.setGameBonusCards(bonusCards);
            
            dispose();
        }
    }
    
    public GameType getSelectedGame()
    {
        return selectedGame;
    }
    
    public BonusCards getSelectedBonus()
    {
        return bonusCards;
    }
    
    public java.util.List<GameUser> getSelectedUsers()
    {
        return gamePlayers;
    }
    
    private void showPlayers(boolean show)
    {
        if (show)
        {
            GameType gt = getGameType();

            // adding computer players
            while (players.getComponentCount() < gt.getPlayers() - 1)
            {
                JComponent c = Box.createHorizontalBox();
                
                c.add(new JLabel(Resource.format(PLAYER_FORMAT, players.getComponentCount()+1)));
                c.add(new JComboBox(new PlayerComboModel()));
                players.add(c);
            }

            // removing computer players
            while (players.getComponentCount() > gt.getPlayers() - 1)
            {
                JComponent c = (JComponent)players.getComponent(gt.getPlayers() - 1);
                JComboBox cbo = (JComboBox)c.getComponent(1);
                cbo.setSelectedItem(null);
                players.remove(players.getComponentCount() - 1);
            }
        }
        
        players.setVisible(show);
        
        pack();
        Main.moveToCenter(this);
    }

    private BonusCardsEntry[] getBonusCardsEntries()
    {
        java.util.List<BonusCards> bcs = BonusCards.getBonusCards();
        BonusCardsEntry[] bces = new BonusCardsEntry[bcs.size()];
        
        for (int i = 0; i < bcs.size(); i++)
            bces[i] = new BonusCardsEntry(bcs.get(i));
        
        return bces;
    }
    
    private GameType getGameType()
    {
        for (JRadioButton rb: mapRbGameType.keySet())
        {
            if (rb.isSelected())
                return mapRbGameType.get(rb);
        }
        
        return null;
    }
}
