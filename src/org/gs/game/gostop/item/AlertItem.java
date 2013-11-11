package org.gs.game.gostop.item;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JComponent;

import org.gs.game.gostop.CardImageInfo;
import org.gs.game.gostop.CardSize;
import org.gs.game.gostop.GamePanel;
import org.gs.game.gostop.Resource;
import org.gs.game.gostop.config.TextFont;

/**
 * 게임 중 사용자의 선택이나 게임의 규칙 등을 표시해주는 아이템으로 GamePanel 내에 하나의 인스턴스로 관리됩니다.
 */
public class AlertItem extends GameItem
{
    public static final String ALERT_LEAD = "alert.lead";
    public static final String ALERT_4CARDS = "alert.4cards";
    public static final String ALERT_GO_4CARDS = "alert.go.4cards";
    public static final String ALERT_GO = "alert.go";
    public static final String ALERT_SWING = "alert.swing";                 // HeunDeul
    public static final String ALERT_BOMB = "alert.bomb";                   // PokTan
    public static final String ALERT_CONCUR = "alert.concur";               // DDaDak
    public static final String ALERT_SWAMP = "alert.swamp";                 // PPeok
    public static final String ALERT_FIRST_SWAMP = "alert.first.swamp";     // CheotPPeok
    public static final String ALERT_SECOND_SWAMP = "alert.second.swamp";   // YeonPPeok
    public static final String ALERT_THIRD_SWAMP = "alert.third.swamp";     // SamPPeok
    public static final String ALERT_SMACK = "alert.smack";                 // JJok
    public static final String ALERT_SWEEP = "alert.sweep";                 // SSeul
    public static final String ALERT_LUCKY = "alert.lucky";                 // Assa
    public static final String ALERT_REFILL = "alert.refill";               // Refill money
    public static final String ALERT_MISSION = "alert.mission.complete";    // Mission complete
    
    private Dimension size;
    private TextFont alertFont;
    private boolean show;
    private String msgId;
    private List<CardItem> cardItems;
    
    public AlertItem(JComponent parent, Dimension size, TextFont alertFont)
    {
        super(parent, new Rectangle());

        this.size = size;
        this.alertFont = alertFont;
        
        hide();
    }

    public int getZOrder()
    {
        return GamePanel.ANIMATION_ZORDER;
    }

    public synchronized void paintItem(Graphics g)
    {
        if (show && rect.isEmpty() == false)
        {
            if (cardItems != null)
                drawSwingCards(g);
            else if (msgId != null)
            {
                int yoff = (rect.height - g.getFontMetrics().getHeight())/2;
                g.setFont(alertFont.getFont());
                g.setColor(alertFont.getFontColor());
                
                String msg = Resource.getProperty(msgId);
                ((Graphics2D)g).setComposite(AlphaComposite.SrcOver.derive(0.65f));
                drawString(g, msg, X_MARGIN, yoff, TextAlign.CENTER);
                g.setPaintMode();  // restore the composite mode
            }
        }
    }
    
    public synchronized void setMessage(String msgId, Point point)
    {
        show = true;
        this.msgId = msgId;
        cardItems = null;
        
        if (point != null)
            setRect(new Rectangle(point.x, point.y, size.width, size.height));
        else
            repaint();
        
        fireZOrderChanged(true);
    }
    
    public synchronized void setSwingCards(String msgId, List<CardItem> cardItems, Point point)
    {
        show = true;
        this.msgId = msgId;
        this.cardItems = cardItems;
        setRect(new Rectangle(point.x, point.y, size.width, size.height));
        
        fireZOrderChanged(true);
    }
    
    public synchronized void hide()
    {
        show = false;
        msgId = null;
        cardItems = null;
        setRect(new Rectangle());
    }
    
    public String getMessageId()
    {
        return msgId;
    }
    
    public Dimension getSize()
    {
        return size;
    }
    
    private void drawSwingCards(Graphics g)
    {
        ((Graphics2D)g).setComposite(AlphaComposite.SrcOver.derive(0.65f));

        String msg = Resource.getProperty(msgId);
        g.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
        g.setColor(alertFont.getFontColor());
        drawString(g, msg, X_MARGIN, 0, TextAlign.CENTER);
        
        CardImageInfo cii = CardItem.getCardImageInfo(CardSize.NORMAL);
        int img_w = cii.getSize().width;
        int img_h = cii.getSize().height;
        int dest_x = rect.x + X_MARGIN;
        int dest_y = rect.y + getTextLineHeight(g) + Y_MARGIN;
        
        for (CardItem cardItem: cardItems)
        {
            int img_y = img_h * cardItem.getCardIndex();
            
            g.drawImage(cii.getImage(), dest_x, dest_y, dest_x+img_w, dest_y+img_h,
                        0, img_y, img_w, img_y+img_h, null);
            dest_x += img_w + 2;
        }

        g.setPaintMode();  // restore the composite mode
    }
}
