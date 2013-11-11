package org.gs.game.gostop.config;

import java.awt.Point;
import java.awt.Rectangle;

import org.gs.game.gostop.CardClass;

public class PlayerLayout
{
    private boolean opposite;
    private PlayerSide playerSide;
    private Rectangle rect;
    private Rectangle rectText;
    private Rectangle rectAvatar;
    private Rectangle rectPoint;
    private Rectangle rectPenalty;
    private Rectangle rectBonus;
    private Point pointAlert;
    private Rectangle rectHold;
    private Rectangle rectKings;
    private Rectangle rectTens;
    private Rectangle rectFives;
    private Rectangle rectLeaves;
    
    protected PlayerLayout(boolean opposite, String playerSide, Rectangle rect,
                           Rectangle rectText, Rectangle rectAvatar,
                           Rectangle rectPoint, Rectangle rectPenalty,
                           Rectangle rectBonus, Point pointAlert, Rectangle rectHold,
                           Rectangle rectKings, Rectangle rectTens, Rectangle rectFives,
                           Rectangle rectLeaves)
    {
        this.opposite = opposite;
        this.playerSide = GameConfig.valueOf(PlayerSide.class, playerSide);
        this.rect = rect;
        this.rectText = rectText;
        this.rectAvatar = rectAvatar;
        this.rectPoint = rectPoint;
        this.rectPenalty = rectPenalty;
        this.rectBonus = rectBonus;
        this.pointAlert = pointAlert;
        this.rectHold = rectHold;
        this.rectKings = rectKings;
        this.rectTens = rectTens;
        this.rectFives = rectFives;
        this.rectLeaves = rectLeaves;
    }
    
    public boolean isOpposite()
    {
        return opposite;
    }
    
    public PlayerSide getPlayerSide()
    {
        return playerSide;
    }
    
    public Rectangle getRect()
    {
        return rect;
    }
    
    public Rectangle getTextRect()
    {
        return rectText;
    }
    
    public Rectangle getAvatarRect()
    {
        return rectAvatar;
    }
    
    public Rectangle getPointRect()
    {
        return rectPoint;
    }
    
    public Rectangle getPenaltyRect()
    {
        return rectPenalty;
    }
    
    public Rectangle getBonusRect()
    {
        return rectBonus;
    }
    
    public Point getAlertLocation()
    {
        return pointAlert;
    }

    public Rectangle getHoldRect()
    {
        return rectHold;
    }

    public Rectangle getTakenRect(CardClass cardClass)
    {
        Rectangle rect;
        
        if (CardClass.KING.equals(cardClass))
            rect = rectKings;
        else if (CardClass.TEN.equals(cardClass) || CardClass.TEN_LEAF.equals(cardClass))
            rect = rectTens;
        else if (CardClass.FIVE.equals(cardClass))
            rect = rectFives;
        else if (CardClass.LEAF.equals(cardClass))
            rect = rectLeaves;
        else
            rect = null;
        
        return rect;

    }
}
