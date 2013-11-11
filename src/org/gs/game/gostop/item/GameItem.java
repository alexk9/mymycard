package org.gs.game.gostop.item;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import org.gs.game.gostop.event.GameEvent;
import org.gs.game.gostop.event.GameEventManager;
import org.gs.game.gostop.event.GameEventType;

/**
 * GS 고스톱에서 메인 UI는 org.gs.game.gostop.GamePanel에 구현되었는데
 * GamePanel은 JPanel을 상속 받았지만 GamePanel내의 대부분의 자식 UI구성요소는
 * org.gs.game.gostop.item.GameItem에서 상속 받아 SWING 클래스들보다 가볍게 구현되었습니다.
 * GameItem은 abstract 클래스로 상속받는 클래드들은 다음의 두 메소드를 구현해아 합니다.
 *     public abstract void paintItem(Graphics g);
 *     public abstract int getZOrder();
 * paintItem 메소드는 UI요소를 그리는데 사용되고,
 * getZOrder 메소드는 UI요소가 겹칠 경우에 그리는 순서를 결정할 때 사용됩니다.
 * 이외에도 GameItem에는 요소의 이동, 마우스 이벤트 처리, 배경 및 글자의 그리기, 이벤트의 알림을 위한 기본적인 메소드들이 구현되어 있습니다.
 *
 * GameItem들은 GamePanel에서 관리되는데 각 요소들의 그리기와 마우스 처리 등을 호출해 주며, 요소들로부터의 이벤트들을 받아 GameManager에게 전달해 주는 역할도 합니다.
 */
public abstract class GameItem
{
    protected static final int X_MARGIN = 5;
    protected static final int Y_MARGIN = 3;
    protected static final Color borderColor = Color.LIGHT_GRAY;
    protected static final Color activeBorder = Color.ORANGE;
    
    protected JComponent parent;
    protected Rectangle rect;
    protected boolean canClick;
    protected boolean mouseOver;
    
    protected static enum TextAlign { LEFT, CENTER, RIGHT }
    
    public GameItem(JComponent parent, Rectangle rect)
    {
        this.parent = parent;
        this.rect = new Rectangle(rect);
        canClick = false;
        mouseOver = false;
    }
    
    public abstract void paintItem(Graphics g);
    public abstract int getZOrder();
    
    public void repaint()
    {
        if (rect != null && rect.isEmpty() == false)
            parent.repaint(rect);
    }
    
    public void setRect(Rectangle rect)
    {
        Rectangle prevrect = this.rect;
        
        this.rect = new Rectangle(rect);
        
        if (prevrect.isEmpty() == false)
            parent.repaint(prevrect);
        
        repaint();
    }
    
    public void moveItem(Point pos)
    {
        if (rect.getLocation().equals(pos) == false)
        {
            Rectangle toRect = new Rectangle(rect);
            
            toRect.setLocation(pos);
            setRect(toRect);
        }
    }
    
    public Rectangle getRect()
    {
        return rect;
    }
    
    public void setCanClick(boolean canClick)
    {
        this.canClick = canClick;
        mouseOver = canClick && mouseOver;
    }
    
    public boolean canClick()
    {
        return canClick;
    }
    
    protected void fillBackground(Graphics g)
    {
        fillRect(g, rect, borderColor, 0.2f);
    }
    
    protected void fillRect(Graphics g, Rectangle rFill, Color color, float alpha)
    {
        g.setColor(color);
        ((Graphics2D)g).setComposite(AlphaComposite.SrcOver.derive(alpha));
        ((Graphics2D)g).fill(rFill);
        g.setPaintMode();  // restore the composite mode
    }

    protected int drawString(Graphics g, String text, int top)
    {
        return drawString(g, text, X_MARGIN, top, TextAlign.LEFT);
    }
    
    protected int drawString(Graphics g, String text, int xmargin, int top, TextAlign align)
    {
        int drawnChars = text.length();
        FontMetrics fm = g.getFontMetrics();
        int yoff = fm.getLeading()/2 + fm.getMaxAscent();
        char[] textChars = text.toCharArray();
        int clientWidth = (int)rect.getWidth() - xmargin * 2;
        int charsWidth;
        
        do
        {
            charsWidth = fm.charsWidth(textChars, 0, drawnChars);
            if (charsWidth > clientWidth)
                drawnChars--;
        } while (charsWidth > clientWidth && drawnChars > 0);
    
        int xoff = rect.x + xmargin;
        if (drawnChars == text.length() && align != TextAlign.LEFT)
        {
            if (align == TextAlign.RIGHT)
                xoff = rect.x + rect.width - xmargin - charsWidth;
            else
                xoff += (rect.width - charsWidth) / 2;
        }
        
        g.drawChars(textChars, 0, drawnChars, xoff, rect.y + top + yoff);
        
        return drawnChars;
    }
    
    protected int getTextLineHeight(Graphics g)
    {
        FontMetrics fm = g.getFontMetrics();
        
        return fm.getHeight();
    }
    
    public void mouseClicked(MouseEvent e)
    {
        fireItemEvent(new GameEvent(this, GameEventType.ITEM_CLICKED));
    }
    
    public void mouseMoved(MouseEvent e)
    {
        boolean oldOver = mouseOver;
        
        mouseOver = canClick;
        
        if (canClick && oldOver != mouseOver)
            repaint();
    }

    public void mouseExited()
    {
        mouseOver = false;
        
        if (canClick)
            repaint();
    }

    protected void drawBorder(Graphics g, Color color)
    {
        g.setColor(color);
        g.drawRect(rect.x, rect.y, rect.width-1, rect.height-1);
    }
    
//    protected void drawBorder(Graphics g)
//    {
//        g.setColor(borderColor);
//        ((Graphics2D)g).setComposite(AlphaComposite.SrcOver.derive(0.4f));
//        g.drawRect(rect.x, rect.y, rect.width-1, rect.height-1);
//        g.setPaintMode();  // restore the composite mode
//    }
    
    protected void fireZOrderChanged(boolean synchronous)
    {
        fireItemEvent(new GameEvent(this, GameEventType.ZORDER_CHANGED), synchronous);
    }
    
    protected void fireItemEvent(GameEvent e)
    {
        fireItemEvent(e, false);
    }

    protected void fireItemEvent(GameEvent e, boolean synchronous)
    {
        GameEventManager.fireGameEvent(e, synchronous);
    }
}
