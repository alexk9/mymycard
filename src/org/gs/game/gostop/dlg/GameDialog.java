package org.gs.game.gostop.dlg;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.gs.game.gostop.event.GameEvent;
import org.gs.game.gostop.event.GameEventManager;
import org.gs.game.gostop.event.GameEventResult;
import org.gs.game.gostop.event.GameEventType;

/**
 * Translucent game dialog panel
 * 반투명 대화상자 - GameDialog (extends JPanel)
 * 대화상자처럼 모양을 내기 위해 JLabel에서 상속된 CaptionBar를 포함합니다.
 * CaptionBar는 대화상자 제목과 타임아웃 시간을 표시합니다.
 * GameDialog의 paint() 메소드에서 반투명 처리를 합니다.
 *
 * public void paint(Graphics g) {
 *     if (image == null || image.getWidth() != getWidth() || image.getHeight() != getHeight())
 *         image = (BufferedImage)createImage(getWidth(), getHeight());
 *     Graphics2D ig2 = image.createGraphics();
 *     ig2.setClip(g.getClip());
 *     super.paint(ig2);
 *     ig2.dispose();
 *     Graphics2D g2 = (Graphics2D) g.create();
 *     g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
 *     g2.drawImage(image, 0, 0, null);
 *  }
 */
public abstract class GameDialog extends JPanel implements ActionListener
{
    private static final long serialVersionUID = 5127813997772366238L;
    protected static float DEFAULT_ALPHA = 0.75f;
    
    private CaptionBar titleBar;
    private JPanel contentPanel;
    private BufferedImage image = null;
    private float alpha;
    private boolean result;
    private boolean isResultSet;

    protected JButton yesButton;
    protected JButton noButton;
    
    private static class CaptionBar extends JLabel
    {
        private static final long serialVersionUID = 7416139869226196605L;
        private Color sColor;
        private Color eColor;
        private int timeout;
        
        private CaptionBar(String text)
        {
            this(text, new Color(10,36,106), new Color(166,202,240));
        }
        
        private CaptionBar(String text, Color sColor, Color eColor)
        {
            super(' ' + text);
            
            this.sColor = sColor;
            this.eColor = eColor;
        }
        
        public void paintComponent(Graphics g)
        {
            Graphics2D g2d = (Graphics2D)g;
            GradientPaint gradient = new GradientPaint(0, 0, sColor, getWidth(), 0, eColor);
            
            g2d.setPaint(gradient);
            g.fillRect(0, 0, getWidth(), getHeight());
            g2d.setPaint(null);
            
            if (timeout > 0)
            {
                Font oldF = g.getFont();
                Color oldC = g.getColor();
                
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                int h = fm.getHeight();
                char[] textChars = Integer.toString(timeout).toCharArray();
                int w = fm.charsWidth(textChars, 0, textChars.length);
                
                g.setColor(Color.RED);
                g.drawChars(textChars, 0, textChars.length, getWidth()-w-3, h-3);
                g.setColor(oldC);
                g.setFont(oldF);
            }
            
            super.paintComponent(g);
        }
        
        public void setText(String text)
        {
            super.setText(' ' + text);
        }
        
        public void setOpaque(boolean isOpaque)
        {
            super.setOpaque(false);     // this should be false
        }
        
        public void setTimeout(int timeout)
        {
            if (this.timeout != timeout)
            {
                this.timeout = timeout;
                
                int width = getWidth();
                repaint(width-20, 0, width, getHeight());
            }
        }
    }
    
    private static class TimeoutHandler extends AWTEvent implements ActiveEvent
    {
        private static final long serialVersionUID = 487341524392708683L;
        
        GameDialog gameDialog;
        
        private TimeoutHandler(GameDialog gameDialog)
        {
            super(gameDialog, Event.ACTION_EVENT);
            
            this.gameDialog = gameDialog;
        }

        public void dispatch()
        {
            if (gameDialog.noButton != null)
                gameDialog.noButton.doClick();
        }
    }
    
    public GameDialog(Container parent, String title)
    {
        this(parent, null, title);
    }
    
    public GameDialog(Container parent, LayoutManager layout, String title)
    {
        super(new BorderLayout(), true);
        
        titleBar = new CaptionBar(title);
        titleBar.setBackground(SystemColor.activeCaption);
        titleBar.setForeground(SystemColor.activeCaptionText);
        titleBar.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
        super.add(titleBar, BorderLayout.PAGE_START);
        
        contentPanel = new JPanel(layout == null ? new FlowLayout() : layout);
        contentPanel.setOpaque(false);
        super.add(contentPanel, BorderLayout.CENTER);
        alpha = DEFAULT_ALPHA;
        result = false;
        isResultSet = false;
        
        yesButton = null;
        noButton = null;
        
        // set its border with a dialog border
        setBorder(new BevelBorder(BevelBorder.RAISED));
        
        setOpaque(false);
        setVisible(false);
        
        parent.add(this);
    }
    
    public void paint(Graphics g)
    {
        if (image == null
            || image.getWidth() != getWidth() || image.getHeight() != getHeight())
            image = (BufferedImage)createImage(getWidth(), getHeight());

        Graphics2D ig2 = image.createGraphics();
        ig2.setClip(g.getClip());
        super.paint(ig2);
        ig2.dispose();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2.drawImage(image, 0, 0, null);
    }
    
    public void invalidate()
    {
        super.invalidate();
        repaint();
    }
    
    public void setAlpha(float alpha)
    {
        this.alpha = alpha;
        repaint();
    }
    
    public void setTitle(String title)
    {
        titleBar.setText(title);
        
        Rectangle rt = titleBar.getBounds();
        
        repaint(rt.x, rt.y, rt.width, rt.height);
    }
    
    public boolean getResult()
    {
        return result;
    }
    
    public void setResult(boolean result)
    {
        isResultSet = true;
        this.result = result;
    }
    
    public boolean isResultSet()
    {
        return isResultSet;
    }
    
    public void clickButtonOnTimeout()
    {
        if (noButton != null)
        {
            TimeoutHandler e = new TimeoutHandler(this);
            
            // Disposing this dialog from another thread causes a problem,
            // so disposing it from the main event thread.
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
        }
    }
    
    public void setTimeout(int timeout)
    {
        titleBar.setTimeout(timeout);
    }
    
    abstract public void actionPerformed(ActionEvent e);
    
    protected JComponent getContentPanel()
    {
        return contentPanel;
    }
    
    protected void fireDialogEvent(GameEventType eventType, boolean result, Object resultObject)
    {
        GameEventResult ger = new GameEventResult(result, resultObject);
        GameEventManager.fireGameEvent(new GameEvent(this, eventType, ger), false);
    }
    
    protected void dispose()
    {
        Container parent = getParent();
        
        if (parent != null)
        {
            Rectangle r = getBounds();
            parent.remove(this);
            parent.repaint(r.x, r.y, r.width, r.height);
        }
    }
}
