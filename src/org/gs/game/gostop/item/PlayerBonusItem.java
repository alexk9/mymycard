package org.gs.game.gostop.item;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JComponent;

import org.gs.game.gostop.GamePanel;
import org.gs.game.gostop.Resource;

/**
 * 게임참여자의 보너스 상태 (고횟수, 흔듦 횟수 및 뻑 횟수)를 관리 및 표시합니다
 */
public class PlayerBonusItem extends GameItem
{
    private static final String GO_LBL = "player.bonus.go";
    private static final String TRIPLE_LBL = "player.bonus.triple";
    private static final String SWAMP_LBL = "player.bonus.swamp";
    
    private BonusLabelItem goLabel;
    private BonusLabelItem tripleLabel;
    private BonusLabelItem swampLabel;
    
    private static class BonusLabelItem extends GameItem
    {
        private int count;
        private String labelId;
        
        private BonusLabelItem(JComponent parent, Rectangle rect, String labelId)
        {
            super(parent, rect);
            
            this.count = 0;
            this.labelId = labelId;
        }

        public int getZOrder()
        {
            return GamePanel.LABEL_ZORDER;
        }

        public void paintItem(Graphics g)
        {
            g.setColor(count > 0 ? Color.BLUE : new Color(160, 160, 160));
            drawString(g, Resource.format(labelId, count > 0 ? count : ""),
                       1, 0, TextAlign.RIGHT);
        }
        
        private void addCount()
        {
            count++;
            repaint();
        }
        
        private void resetCount()
        {
            count = 0;
            repaint();
        }
        
        private int getCount()
        {
            return count;
        }
    }
    
    public PlayerBonusItem(JComponent parent, Rectangle rect)
    {
        super(parent, rect);

        if (rect.width > rect.height)
        {
            int offset = 2;
            int lblWidth = (rect.width - offset*2) / 3;
            goLabel = new BonusLabelItem(parent, new Rectangle(rect.x, rect.y, lblWidth, rect.height),
                                         GO_LBL);
            tripleLabel = new BonusLabelItem(parent, new Rectangle(rect.x+offset+lblWidth, rect.y, lblWidth, rect.height),
                                             TRIPLE_LBL);
            swampLabel = new BonusLabelItem(parent, new Rectangle(rect.x+(offset+lblWidth)*2, rect.y, lblWidth, rect.height),
                                            SWAMP_LBL);
        }
        else
        {
            int offset = 2;
            int lblHeight = (rect.height - offset*2) / 3;
            goLabel = new BonusLabelItem(parent, new Rectangle(rect.x, rect.y, rect.width, lblHeight),
                                         GO_LBL);
            tripleLabel = new BonusLabelItem(parent, new Rectangle(rect.x, rect.y+offset+lblHeight, rect.width, lblHeight),
                                             TRIPLE_LBL);
            swampLabel = new BonusLabelItem(parent, new Rectangle(rect.x, rect.y+(offset+lblHeight)*2, rect.width, lblHeight),
                                            SWAMP_LBL);
        }

        clearBonus();
        
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
        
        g.setFont(new Font(Font.DIALOG, Font.BOLD, 11));
        
        Shape clip = g.getClip();
        
        if (clip.intersects(goLabel.getRect()))
            goLabel.paintItem(g);
        
        if (clip.intersects(tripleLabel.getRect()))
            tripleLabel.paintItem(g);
        
        if (clip.intersects(swampLabel.getRect()))
            swampLabel.paintItem(g);
    }

    public void clearBonus()
    {
        goLabel.resetCount();
        tripleLabel.resetCount();
        swampLabel.resetCount();
    }
    
    public int getGoCount()
    {
        return goLabel.getCount();
    }
    
    public void addGoCount()
    {
        goLabel.addCount();
    }
    
    public int getTripleCount()
    {
        return tripleLabel.getCount();
    }
    
    public void addTripleCount()
    {
        tripleLabel.addCount();
    }
    
    public int getSwampCount()
    {
        return swampLabel.getCount();
    }
    
    public void addSwampCount()
    {
        swampLabel.addCount();
    }
}
