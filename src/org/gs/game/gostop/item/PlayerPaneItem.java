package org.gs.game.gostop.item;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

import org.gs.game.gostop.GamePanel;

/**
 * 게임참여자의 전체 영역을 표시하며, 참여자의 칠 순서가 되었을 때 오렌지색 테두리를 그려줍니다
 */
public class PlayerPaneItem extends GameItem
{
    private boolean active;
    
    public PlayerPaneItem(JComponent parent, Rectangle rect)
    {
        super(parent, rect);
        
        active = false;
        
        // to register it as a child item of the game panel
        fireZOrderChanged(false);
    }

    public void paintItem(Graphics g)
    {
        fillBackground(g);
        
        if (active)
            drawBorder(g, activeBorder);
    }
    
    public int getZOrder()
    {
        return GamePanel.BACKGROUND_ZORDER;
    }
    
    public void setActive(boolean active)
    {
        this.active = active;
        repaint();
    }
}
