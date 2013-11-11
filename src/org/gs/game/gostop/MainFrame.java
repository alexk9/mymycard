package org.gs.game.gostop;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.gs.game.gostop.config.GameConfig;
import org.gs.game.gostop.config.GameUser;
import org.gs.game.gostop.dlg.PlayerRankingDlg;
import org.gs.game.gostop.dlg.SetupGameDlg;
import org.gs.game.gostop.dlg.SetupUserDlg;
import org.gs.game.gostop.event.IGamePanelListener;
import org.gs.game.gostop.item.PlayerAvatarItem;
import org.gs.game.gostop.sound.GameSoundManager;

/**
 * 게임 윈도우 자체
 */
public class MainFrame extends JFrame implements IGamePanelListener
{
    private static final long serialVersionUID = 3554648699845768453L;
    private static final String FRAME_TITLE = "main.frame.title";
    
    private static MainFrame _instance = null;  // singleton instance

    /**
     * 게임이실행되는패널
     */
    private GamePanel gamePanel;
    private GameConfig gameConfig;
    private GameUser gameUser;
    private int gameMoneyPerPoint;
    private SetupUserDlg dlgUserInfo;
    private PlayerRankingDlg dlgPlayerRanking;

    public MainFrame()
        throws Exception
    {
        super(Resource.format(FRAME_TITLE, 1));

        _instance = this;
        gameConfig = GameConfig.getInstance();
        gameUser = null;
        dlgUserInfo = null;
        dlgPlayerRanking = null;
        gameMoneyPerPoint = 1;
        
        initContentPane();
        
        setResizable(false);
    }
    
    private void initContentPane()
    {
        Container contentPane = getContentPane();
        
        gamePanel = new GamePanel(gameConfig);
        gamePanel.addGamePanelEventListener(this);
        gamePanel.requestFocusInWindow();
        contentPane.add(gamePanel);
    }

    protected void processWindowEvent(WindowEvent e) 
    {
        super.processWindowEvent(e);
        
        if (e.getID() == WindowEvent.WINDOW_OPENED)
        {
            if (setupUser())
                setupGame();
            else
                gameStopped();
        }
    }
    
    public boolean setupUser()
    {
        SetupUserDlg sud = new SetupUserDlg(this);
        
        sud.pack();
        Main.moveToCenter(sud);
        sud.setVisible(true);
        
        gameUser = sud.getGameUser();
        
        if (gameUser != null)
        {
            if (Resource.getLocale().equals(gameUser.getUserLocale()) == false)
            {
                Resource.setLocale(gameUser.getUserLocale());
                setTitle(Resource.format(FRAME_TITLE, gameMoneyPerPoint));
                invalidate();
            }
            
            GameSoundManager.setMute(gameUser.getMuteSound());
        }
        
        return gameUser != null;
    }
    
    public void setupGame()
    {
        SetupGameDlg sgd = new SetupGameDlg(this);
        
        sgd.pack();
        Main.moveToCenter(sgd);
        sgd.setVisible(true);
        
        if (sgd.getSelectedGame() != null)
        {
            gamePanel.initGame(sgd.getSelectedGame(), sgd.getSelectedBonus(),
                               sgd.getSelectedUsers());
            gameMoneyPerPoint = sgd.getSelectedGame().getGameMoney();
            setTitle(Resource.format(FRAME_TITLE, gameMoneyPerPoint));
        }
        else
            gameStopped();
    }

    public void gameStopped()
    {
        dispose();
        System.exit(0);
    }
    
    public void onMenuSelected(Object selectedObject, String menuCmd)
    {
        if (GamePanel.MENU_USER_INFO.equals(menuCmd))
        {
            if (selectedObject instanceof PlayerAvatarItem)
            {
                PlayerAvatarItem pai = (PlayerAvatarItem)selectedObject;
                
                onGameUserInfo(pai.getGamePlayer().getGameUser());
            }
        }
        else if (GamePanel.MENU_PLAYER_RANKING.equals(menuCmd))
            onPlayerRanking();
        else if (GamePanel.MENU_MUTE_SOUND.equals(menuCmd))
        {
            boolean muteSound = (Boolean)selectedObject;
            
            gameUser.setMuteSound(muteSound);
            GameSoundManager.setMute(muteSound);
        }
    }
    
    public void onChildDlgDisposed(Component child, Object... params)
    {
        if (child == dlgUserInfo)
            onUserInfoDisposed((Boolean)params[0]);
        else if (child == dlgPlayerRanking)
            dlgPlayerRanking = null;
    }
    
    public static GameConfig getGameConfig()
    {
        return _instance.gameConfig;
    }
    
    public static GameUser getGameUser()
    {
        return _instance.gameUser;
    }

    private void onGameUserInfo(GameUser gameUser)
    {
        if (dlgUserInfo == null)
        {
            dlgUserInfo = new SetupUserDlg(this, gameUser, gameUser == this.gameUser);
        
            dlgUserInfo.pack();
            Main.moveToCenter(dlgUserInfo);
            dlgUserInfo.setVisible(true);
        }
        else
        {
            dlgUserInfo.setUser(gameUser, gameUser == this.gameUser);
            dlgUserInfo.pack();
        }
    }
    
    private void onUserInfoDisposed(boolean updated)
    {
        dlgUserInfo = null;
        
        if (updated)
        {
            Resource.setLocale(gameUser.getUserLocale());
            setTitle(Resource.format(FRAME_TITLE, gameMoneyPerPoint));
            repaint();
        }
    }
    
    private void onPlayerRanking()
    {
        if (dlgPlayerRanking == null)
        {
            dlgPlayerRanking = new PlayerRankingDlg(this);
        
            dlgPlayerRanking.pack();
            Main.moveToCenter(dlgPlayerRanking);
            dlgPlayerRanking.setVisible(true);
        }
        else
        {
            dlgPlayerRanking.refreshRanking();
            dlgPlayerRanking.pack();
        }
    }
}
