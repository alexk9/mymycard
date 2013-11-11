package org.gs.game.gostop.item;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

import org.gs.game.gostop.GamePanel;
import org.gs.game.gostop.Resource;

/**
 * 게임참여자의 현재 점수를 표시합니다.
 */
public class PlayerPointItem extends GameItem
{
    public static final String POINTS_LBL = "player.points";
    
    private int points;
    
    public PlayerPointItem(JComponent parent, Rectangle rect)
    {
        super(parent, rect);
        
        points = 0;
        
        // to register it as a child item of the game panel
        fireZOrderChanged(false);
    }

    public int getZOrder()
    {
        return GamePanel.LABEL_ZORDER;
    }

    public void paintItem(Graphics g)
    {
        fillBackground(g);
        
        g.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        g.setColor(points > 0 ? Color.BLUE : Color.LIGHT_GRAY);
        drawString(g, Resource.format(POINTS_LBL, points > 0 ? points : ""),
                   7, 10, TextAlign.RIGHT);
    }

    public void setPoints(int points)
    {
        if (this.points != points)
        {
            this.points = points;
            repaint();
        }
    }
    
    public int getPoints()
    {
        return points;
    }
}
