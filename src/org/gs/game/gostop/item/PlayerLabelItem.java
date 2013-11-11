package org.gs.game.gostop.item;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import org.gs.game.gostop.GamePanel;
import org.gs.game.gostop.Resource;
import org.gs.game.gostop.config.GameUser;
import org.gs.game.gostop.config.TextFont;
import org.gs.game.gostop.event.GameEvent;
import org.gs.game.gostop.event.IGameEventListener;

/**
 *  게임참여자의 간단한 문자 정보 (GS$, 전적 및 선)를 관리 및 표시합니다. 사용자 정보가 변경 이벤트를 받기 위해 IGameEventListener를 구현했습니다.
 */
public class PlayerLabelItem extends GameItem implements IGameEventListener
{
    public static final String MONEY_LBL = "player.label.money";
    public static final String RECORD_LBL = "player.label.record";
    public static final String SHORT_RECORD_LBL = "player.label.short.record";
    private static final String LEAD_LBL = "player.label.lead";
    
    private GameUser gameUser;
    private long initMoney;
    private int initWins;
    private int initDraws;
    private int initLoses;
    private TextFont aliasFont;
    private TextFont infoFont;
    private boolean leadPlayer;
    
    public PlayerLabelItem(JComponent parent, Rectangle rect, GameUser gameUser,
                           TextFont aliasFont, TextFont infoFont)
    {
        super(parent, rect);
        
        canClick = true;    // to get the mouseMoved event
        
        this.gameUser = gameUser;
        this.aliasFont = aliasFont;
        this.infoFont = infoFont;
        leadPlayer = false;
        initMoney = gameUser.getMoney();
        initWins = gameUser.getWins();
        initDraws = gameUser.getDraws();
        initLoses = gameUser.getLoses();
        
        // to register it as a child item of the game panel
        fireZOrderChanged(false);
        
        gameUser.addUpdateListener(this);
    }

    public void paintItem(Graphics g)
    {
        int top = Y_MARGIN;

        fillBackground(g);

        // alias
        g.setFont(aliasFont.getFont());
        g.setColor(aliasFont.getFontColor());
        drawString(g, gameUser.getUserAlias(), top);
        
        if (leadPlayer)
        {
            String label = Resource.getProperty(LEAD_LBL);
            FontMetrics fm = g.getFontMetrics();
            char[] labelChars = label.toCharArray();
            int labelWidth = fm.charsWidth(labelChars, 0, labelChars.length);
            g.fillRect(rect.x+rect.width-8-labelWidth,rect.y+top,
                       labelWidth+6,fm.getHeight()+fm.getLeading());
            g.setColor(Color.MAGENTA);
            drawString(g, label, X_MARGIN, top, TextAlign.RIGHT);
        }
        
        top += getTextLineHeight(g);
        
        // info
        g.setFont(infoFont.getFont());
        
        if (mouseOver)
        {
            g.setColor(new Color(0, 128, 0));
            drawString(g, Resource.format(MONEY_LBL, gameUser.getMoney()-initMoney), top); 
            top += getTextLineHeight(g);
            drawString(g, Resource.format(RECORD_LBL, gameUser.getWins()-initWins,
                                          gameUser.getDraws()-initDraws,
                                          gameUser.getLoses()-initLoses), top);
        }
        else
        {
            g.setColor(infoFont.getFontColor());
            drawString(g, Resource.format(MONEY_LBL, gameUser.getMoney()), top); 
            top += getTextLineHeight(g);
            drawString(g, Resource.format(SHORT_RECORD_LBL, gameUser.getWins(),
                                          gameUser.getLoses()), top);
        }
    }
    
    public int getZOrder()
    {
        return GamePanel.LABEL_ZORDER;
    }
    
    public void setLeadPlayer(boolean leadPlayer)
    {
        boolean old = this.leadPlayer;
        this.leadPlayer = leadPlayer;
        
        if (old != leadPlayer)
            repaint();
    }
    
    public boolean isLeadPlayer()
    {
        return leadPlayer;
    }

    public void onGameEvent(GameEvent e)
    {
        repaint();
    }
    
    public void mouseClicked(MouseEvent e)
    {
        // do nothing
    }
}
