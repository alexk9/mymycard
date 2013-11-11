package org.gs.game.gostop.item;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JComponent;

import org.gs.game.gostop.GamePanel;
import org.gs.game.gostop.Resource;
import org.gs.game.gostop.play.GamePenalty;

/**
 * 게임참여자의 벌칙 상황 (광박, 멍박 및 피박)을 표시하는데, 현재 치는 사람에 따라 상황을 표시합니다.
 */
public class PlayerPenaltyItem extends GameItem
{
    private static final String KING_PENALTY_LBL = "player.king.penalty";
    private static final String TEN_PENALTY_LBL = "player.ten.penalty";
    private static final String LEAF_PENALTY_LBL = "player.leaf.penalty";

    private PenaltyLabelItem kingPenalty;
    private PenaltyLabelItem tenPenalty;
    private PenaltyLabelItem leafPenalty;
    
    private static class PenaltyLabelItem extends GameItem
    {
        private boolean status;
        private String labelId;
        
        private PenaltyLabelItem(JComponent parent, Rectangle rect, String labelId)
        {
            super(parent, rect);
            
            status = false;
            this.labelId = labelId;
        }

        public int getZOrder()
        {
            return GamePanel.LABEL_ZORDER;
        }

        public void paintItem(Graphics g)
        {
            g.setColor(status ? Color.RED : Color.GRAY);
            drawString(g, Resource.getProperty(labelId), 0, 0, TextAlign.CENTER);
        }
        
        private void setStatus(boolean status)
        {
            this.status = status;
            repaint();
        }
    }
    
    public PlayerPenaltyItem(JComponent parent, Rectangle rect)
    {
        super(parent, rect);

        int offset = 0;
        int lblWidth = (rect.width - offset*2) / 3;
        kingPenalty = new PenaltyLabelItem(parent, new Rectangle(rect.x, rect.y, lblWidth, rect.height),
                                           KING_PENALTY_LBL);
        tenPenalty = new PenaltyLabelItem(parent, new Rectangle(rect.x+offset+lblWidth, rect.y, lblWidth, rect.height),
                                          TEN_PENALTY_LBL);
        leafPenalty = new PenaltyLabelItem(parent, new Rectangle(rect.x+(offset+lblWidth)*2, rect.y, lblWidth, rect.height),
                                           LEAF_PENALTY_LBL);

        clearPenalties();
        
        // to register it as a child item of the game panel
        fireZOrderChanged(false);
    }

    public int getZOrder()
    {
        return GamePanel.LABEL_ZORDER;
    }

    public void paintItem(Graphics g)
    {
        fillRect(g, rect, Color.DARK_GRAY, 0.4f);

        g.setFont(new Font(Font.DIALOG, Font.BOLD, 11));
        
        Shape clip = g.getClip();
        
        if (clip.intersects(kingPenalty.getRect()))
            kingPenalty.paintItem(g);

        if (clip.intersects(tenPenalty.getRect()))
            tenPenalty.paintItem(g);

        if (clip.intersects(leafPenalty.getRect()))
            leafPenalty.paintItem(g);
    }
    
    public void clearPenalties()
    {
        kingPenalty.setStatus(false);
        tenPenalty.setStatus(false);
        leafPenalty.setStatus(false);
    }
    
    public void setPenalty(GamePenalty gamePanalty, boolean penalty)
    {
        if (gamePanalty == GamePenalty.KING)
            kingPenalty.setStatus(penalty);
        else if (gamePanalty == GamePenalty.TEN)
            tenPenalty.setStatus(penalty);
        else if (gamePanalty == GamePenalty.LEAF)
            leafPenalty.setStatus(penalty);
    }
}
