package org.gs.game.gostop.item;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;

import org.gs.game.gostop.GamePanel;

/**
 * 먹은 피의 숫자를 표시하기 위한 요소입니다
 */
public class LeafCountItem extends GameItem
{
    private static Font aliasFont = new Font(Font.DIALOG, Font.BOLD, 14);
    private int zorder;
    private int count;
    
    
    public LeafCountItem(JComponent parent, Rectangle rect)
    {
        super(parent, rect);
        
        zorder = GamePanel.CARD_ZORDER;
        count = 0;
    }

    public int getZOrder()
    {
        return zorder;
    }
    
    public void setZOrder(int zorder)
    {
        if (this.zorder != zorder)
        {
            this.zorder = zorder;
            fireZOrderChanged(true);
        }
    }

    public void paintItem(Graphics g)
    {
        if (count > 0)
        {
            ((Graphics2D)g).setComposite(AlphaComposite.SrcOver.derive(0.6f));
            g.setFont(aliasFont);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height,
                            rect.width/4, rect.height/4);
            g.setColor(Color.BLUE);
            drawString(g, Integer.toString(count), 0, 0, TextAlign.CENTER);
            g.setPaintMode();  // restore the composite mode
        }
    }

    public void setCount(int count)
    {
        if (this.count != count)
        {
            this.count = count;
            repaint();
        }
    }
}
