package org.gs.game.gostop.dlg;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.gs.game.gostop.*;
import org.gs.game.gostop.config.GameUser;
import org.gs.game.gostop.config.WinType;
import org.gs.game.gostop.event.GameEventType;
import org.gs.game.gostop.item.CardItem;
import org.gs.game.gostop.play.GamePenalty;
import org.gs.game.gostop.sound.GameSoundManager;

/**
 * 게임 결과 표시 대화상자
 */
public class GameResultDlg extends GameDialog {
    private static final long serialVersionUID = -4918145549693854176L;
    
    private static final String DIALOG_TITLE_LBL = "game.result.title";
    private static final String WINNER_LBL = "game.result.winner";
    private static final String GAME_POINTS_LBL = "game.result.points";
    private static final String PLAYER_LBL = "game.result.player";
    private static final String PENALTIES_LBL = "game.result.penalties";
    private static final String KING_PENALTY_LBL = "game.result.king.penalty";
    private static final String TEN_PENALTY_LBL = "game.result.ten.penalty";
    private static final String LEAF_PENALTY_LBL = "game.result.leaf.penalty";
    private static final String LOST_POINTS_LBL = "game.result.lost";
    private static final String WIN_MONEY_LBL = "game.result.win.money";
    private static final String MORE_GAME_LBL = "game.result.want.more";
    
    private GamePlayer winner;
    private List<GamePlayer> losers;
    private int winPoints;

    public GameResultDlg(Container parent, GamePlayer winner, List<GamePlayer> players,
                         int winPoints)
    {
        super(parent, Resource.getProperty(DIALOG_TITLE_LBL));
        
        this.winner = winner;
        this.losers = new ArrayList<GamePlayer>();
        this.winPoints = winPoints;
        
        for (GamePlayer player: players)
        {
            if (player != winner)
                losers.add(player);
            
            if (player.isOpposite())
            {
                for (CardItem cardItem: player.getHoldCards())
                    cardItem.setFlipped(true);
            }
        }
        
        initDialog();
        setSize(getPreferredSize());
        Main.moveToCenter(this);
        
        String soundId = winner.isComputerPlayer() ? GameSoundManager.SOUND_LOSE_RESULT
                                                   : GameSoundManager.SOUND_WIN_RESULT;
        GameSoundManager.playSound(soundId, null);

        addComponentListener(new ComponentAdapter()
        {
            public void componentShown(ComponentEvent e)
            {
                yesButton.requestFocus();
            }
        });
    }
    
    public void actionPerformed(ActionEvent e)
    {
        GameSoundManager.stopAllSound();
        
        dispose();
        
        fireDialogEvent(GameEventType.MORE_GAME_DECIDED, e.getSource() == yesButton, winner);
    }
    
    private void initDialog()
    {
        List<GameUser> gameUsers = new ArrayList<GameUser>();
        JComponent content = getContentPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        
        content.setBorder(new CompoundBorder(content.getBorder(),
                          new EmptyBorder(10,10,10,10)));   // margins
        content.setLayout(gbl);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);
        
        addLabel(content, Resource.getProperty(WINNER_LBL), 1f, gbc, JLabel.TRAILING);
        addLabel(content, winner.getGameUser().getUserAlias(), -1f, gbc, JLabel.LEADING,
                new Font(Font.DIALOG, Font.BOLD, 14), Color.BLUE);

        int gamePoints = winPoints <= 0 ? winner.getGamePoints() : winPoints;
        addLabel(content, Resource.getProperty(GAME_POINTS_LBL), 1f, gbc, JLabel.TRAILING);
        addLabel(content, Integer.toString(gamePoints), -1f, gbc, JLabel.LEADING,
                new Font(Font.DIALOG, Font.BOLD, 14), Color.BLUE);
        
        addComponent(content, Box.createVerticalStrut(6), -1f, gbc);
        
        // Player, Penalties (King, Ten, Leaf), points
        addLabel(content, Resource.getProperty(PLAYER_LBL), 1f, gbc, JLabel.CENTER);
        addLabel(content, Resource.getProperty(PENALTIES_LBL), 1.5f, gbc, JLabel.CENTER);
        addLabel(content, Resource.getProperty(LOST_POINTS_LBL), -1f, gbc, JLabel.CENTER,
                 null, Color.RED);
        
        // show the game result
        int winnerMoney = 0;
        HashMap<GamePlayer,Integer> playerMoney = calcPlayerMoney(gamePoints);
        GameTable gameTable = getGamePanel().getGameTable();
        
        for (Map.Entry<GamePlayer,Integer> entry: playerMoney.entrySet())
        {
            GamePlayer loser = entry.getKey();
            int lostMoney = entry.getValue();
            List<GamePenalty> penalties = loser.getPenalties(winner);
            
            // save game points
            GameUser gameUser = loser.getGameUser();
            gameUser.updateMoney(-lostMoney,
                                 -lostMoney/gameTable.getGameType().getGameMoney(),
                                 false,
                                 lostMoney == 0 ? WinType.DRAW : WinType.LOSE);
            gameUsers.add(gameUser);
            
            addLabel(content, gameUser.getUserAlias(), 1f, gbc, JLabel.CENTER);
            addLabel(content, getPenaltyString(penalties), 1.5f, gbc, JLabel.CENTER);
            addLabel(content, Integer.toString(lostMoney), -1f, gbc, JLabel.CENTER,
                     null, Color.RED);
            winnerMoney += lostMoney;
        }
        
        addComponent(content, Box.createVerticalStrut(6), -1f, gbc);
        
        addLabel(content, Resource.getProperty(WIN_MONEY_LBL), 1f, gbc, JLabel.TRAILING);
        addLabel(content, Integer.toString(winnerMoney), -1f, gbc, JLabel.LEADING,
                new Font(Font.DIALOG, Font.BOLD, 14), Color.BLUE);
        
        addComponent(content, Box.createVerticalStrut(6), -1f, gbc);
        addLabel(content, Resource.getProperty(MORE_GAME_LBL), -1f, gbc, JLabel.LEADING);
        addComponent(content, Box.createVerticalStrut(6), -1f, gbc);
        
        Box boxButtons = Box.createHorizontalBox();
        addButtons(boxButtons);
        addComponent(content, boxButtons, -1f, gbc);

        // save game points
        winner.getGameUser().updateMoney(winnerMoney,
                                         winnerMoney/gameTable.getGameType().getGameMoney(),
                                         false, WinType.WIN);
        gameUsers.add(winner.getGameUser());
        GameUser.updateGameUsers(gameUsers);
    }
    
    private HashMap<GamePlayer,Integer> calcPlayerMoney(int gamePoints)
    {
        HashMap<GamePlayer,Integer> playerPoints = new HashMap<GamePlayer,Integer>(2);
        GameTable gameTable = getGamePanel().getGameTable();
        List<GamePlayer> goPlayers = gameTable.getGoPlayers();
        int goPenalty = 0;
        
        if (goPlayers != null)
        {
            goPlayers.remove(winner);
            if (goPlayers.size() == 0 
                || gameTable.getGameType().getPlayers() == 2
                || goPlayers.size() == 2)
                goPlayers = null;
        }
        
        for (GamePlayer loser: losers)
        {
            List<GamePenalty> penalties = loser.getPenalties(winner);
            int lostMoney;
            
            if (winPoints <= 0 && loser.hasNoTakenCards())
                lostMoney = 0;
            else
            {
                if (winPoints > 0)
                    lostMoney = winPoints * gameTable.getGameType().getGameMoney();
                else
                    lostMoney = gamePoints * (int)Math.pow(2, penalties.size())
                                * gameTable.getGameType().getGameMoney();
                if (goPlayers != null && goPlayers.contains(loser) == false)
                {
                    goPenalty = lostMoney;
                    lostMoney = 0;
                }
            }
            
            playerPoints.put(loser, lostMoney);
        }
        
        if (goPlayers != null)
        {
            GamePlayer loser = goPlayers.get(0);
            
            playerPoints.put(loser, playerPoints.get(loser)+goPenalty);
        }
        
        return playerPoints;
    }
    
    private String getPenaltyString(List<GamePenalty> penalties)
    {
        StringBuffer sb = new StringBuffer();
        
        for (GamePenalty gp: penalties)
        {
            if (sb.length() > 0)
                sb.append(", ");
            if (GamePenalty.KING == gp)
                sb.append(Resource.getProperty(KING_PENALTY_LBL));
            else if (GamePenalty.TEN == gp)
                sb.append(Resource.getProperty(TEN_PENALTY_LBL));
            else if (GamePenalty.LEAF == gp)
                sb.append(Resource.getProperty(LEAF_PENALTY_LBL));
        }
        
        return sb.toString();
    }
    
    private void addButtons(Box boxButtons)
    {
        boxButtons.add(Box.createHorizontalGlue());
        
        yesButton = new GameButton(Resource.getStandardProperty(Resource.STD_YES_BUTTON));
        yesButton.addActionListener(this);
        boxButtons.add(yesButton);
        yesButton.requestFocusInWindow();
        
        noButton = new GameButton(Resource.getStandardProperty(Resource.STD_NO_BUTTON));
        noButton.addActionListener(this);
        boxButtons.add(noButton);
    }

    private void addLabel(JComponent content, String label, float weightx,
                          GridBagConstraints gbc, int align)
    {
        addLabel(content, label, weightx, gbc, align, null, null);
    }
    
    private void addLabel(JComponent content, String label, float weightx,
                          GridBagConstraints gbc, int align, Font font, Color foreColor)
    {
        JLabel jLabel = new JLabel(label, align);

        if (font != null)
            jLabel.setFont(font);
        
        if (foreColor != null)
            jLabel.setForeground(foreColor);
        
        addComponent(content, jLabel, weightx, gbc);
    }
    
    private void addComponent(JComponent content, Component comp, float weightx,
                              GridBagConstraints gbc)
    {
        GridBagLayout gbl = (GridBagLayout)content.getLayout();
        
        if (weightx > 0)
        {
            gbc.gridwidth = 1;
            gbc.weightx = weightx;
        }
        else
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            
        gbl.setConstraints(comp, gbc);
        content.add(comp);
    }
    
    private GamePanel getGamePanel()
    {
        return (GamePanel)getParent();
    }
}
