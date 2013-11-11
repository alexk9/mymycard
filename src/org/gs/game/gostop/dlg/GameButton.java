package org.gs.game.gostop.dlg;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Translucent game button
 * 반투명 버튼 - GameButton
 * GameDialog에 일반 버튼을 포함할 경우, 클릭시 반투명 효과가 사라집니다
 * JButton을 상속한 GameButton을 구현하였습니다.
 * GameDialog와 마찬가지로 paint() 메소드에서 반투명 효과를 처리합니다.
 */
public class GameButton extends JButton implements FocusListener
{
    private static final long serialVersionUID = -7237764222472370253L;

    private float alpha;
    
    public GameButton(String text)
    {
        this(text, null);
    }
    
    public GameButton(Icon icon)
    {
        this(null, icon);
    }
    
    public GameButton(String text, Icon icon)
    {
        super(text, icon);
        
        setOpaque(false);
        this.alpha = GameDialog.DEFAULT_ALPHA;
        addFocusListener(this);
    }

    public void paint(Graphics g)
    {
        BufferedImage image = (BufferedImage)createImage(getWidth(), getHeight());

        Graphics2D ig2 = image.createGraphics();
        ig2.setClip(g.getClip());
        super.paint(ig2);
        ig2.dispose();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2.drawImage(image, 0, 0, null);
    }

    public void setAlpha(float alpha)
    {
        this.alpha = alpha;
        repaint();
    }
    
    public void focusGained(FocusEvent e)
    {
        if (e.getComponent() == this || e.getOppositeComponent() == this)
            repaint();
    }
    
    public void focusLost(FocusEvent e)
    {
        if (e.getComponent() == this || e.getOppositeComponent() == this)
            repaint();
    }
}
