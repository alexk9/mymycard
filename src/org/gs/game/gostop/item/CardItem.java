package org.gs.game.gostop.item;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.gs.game.gostop.CardClass;
import org.gs.game.gostop.CardImageInfo;
import org.gs.game.gostop.CardSize;
import org.gs.game.gostop.Resource;
import org.gs.game.gostop.play.GameRule;

/**
 * 화투를 표시하는 UI요소로, 상황에 따라 여러가지 크기와 상태마크를 표현할 수 있습니다. 게임 사용자가 칠 차례가 될 경우에는 손에 쥔 카드를 클릭해 선택할 수 있습니다.
 */
public class CardItem extends GameItem
{
    // card active status values
    public static enum Status { NONE, SAFE, FIRST, BOMB };
    private static final Color[] statusBgColor
        = { null, Color.GRAY, Color.BLUE, Color.BLACK };
    private static final Color[] statusFgColor
        = { null, Color.BLUE, Color.WHITE, Color.RED };
    private static final String statusTextPrefix = "card.status.";
    private static final String missionText = "mission";
    private static final Color clrCanClick = new Color(160, 160, 0);
    private static final int markSize = 10;     // mark size in pixels
    
    private static HashMap<CardSize,CardImageInfo> _cardImages = new HashMap<CardSize,CardImageInfo>();
    
    private int cardCode;
    private int index;
    private CardSize cardSize;
    private boolean flipped;
    private int zorder;
    private Status activeStatus;
    private boolean mission;
    
    public static void addCardImage(CardSize cardSize, Dimension size, Image image)
    {
        _cardImages.put(cardSize, new CardImageInfo(size, image));
    }
    
    public static CardImageInfo getCardImageInfo(CardSize cardSize)
    {
        return _cardImages.get(cardSize);
    }
    
    public CardItem(JComponent parent, Rectangle rect, int majorCode, int minorCode,
                    boolean turned, int zorder)
    {
        super(parent, rect);
        
        cardCode = getCardCode(majorCode, minorCode);
        index = getCardIndex(majorCode, minorCode);
        this.cardSize = CardSize.NORMAL;
        this.flipped = turned;
        this.zorder = zorder;
        activeStatus = Status.NONE;
        mission = false;
    }

    public void paintItem(Graphics g)
    {
        CardImageInfo imgInfo = _cardImages.get(cardSize);
        Image image = imgInfo.getImage();
        int srcy = index * imgInfo.getSize().height;

        if (flipped)
        {
            g.drawImage(image, rect.x, rect.y, rect.x+rect.width, rect.y+rect.height,
                        0, srcy, rect.width, srcy+rect.height, null);
            
            if (activeStatus != Status.NONE)
            {
                // draw the status mark
                final int margin = 1;
                
                drawMarker(g, rect.x + rect.width - markSize - margin, rect.y + margin,
                           statusTextPrefix + activeStatus.name().toLowerCase(),
                           statusBgColor[activeStatus.ordinal()],
                           statusFgColor[activeStatus.ordinal()]);
            }
            
            if (mission)
            {
                // draw the status mark
                final int margin = 1;
                
                drawMarker(g, rect.x + margin, rect.y + rect.height - markSize - margin,
                           statusTextPrefix + missionText,
                           Color.GREEN, Color.MAGENTA);
            }
            
            if (mouseOver)
                drawBorder(g, activeBorder);
        }
        else
        {   // draw the last image which represents the back side of cards
            int srcHeight = image.getHeight(null);
            g.drawImage(image, rect.x, rect.y, rect.x+rect.width, rect.y+rect.height,
                        0, srcHeight-rect.height, rect.width, srcHeight, null);
            
            if (canClick && mouseOver == false)
                drawBorder(g, clrCanClick);
        }
    }
    
    public void setZOrder(int zorder)
    {
        if (this.zorder != zorder)
        {
            this.zorder = zorder;
            fireZOrderChanged(true);
        }
    }
    
    public int getZOrder()
    {
        return zorder;
    }
    
    public int getCardCode()
    {
        return cardCode;
    }
    
    public int getMajorCode()
    {
        return getMajorCode(cardCode);
    }
    
    public int getMinorCode()
    {
        return getMinorCode(cardCode);
    }
    
    public void setCardSize(CardSize cardSize)
    {
        setCardSize(cardSize, false);
    }
    
    public void setCardSize(CardSize cardSize, boolean keepCenter)
    {
        if (this.cardSize != cardSize)
        {
            CardImageInfo imgInfo = _cardImages.get(cardSize);
            Rectangle newrect = new Rectangle(rect);
            
            newrect.setSize(imgInfo.getSize());
            
            if (keepCenter)
            {
                newrect.setLocation(rect.x-(newrect.width-rect.width)/2,
                                    rect.y-(newrect.height-rect.height)/2);
            }
            
            this.cardSize = cardSize;
            
            setRect(newrect);
        }
    }
    
    public CardSize getCardSize()
    {
        return cardSize;
    }
    
    public void setFlipped(boolean flipped)
    {
        if (this.flipped != flipped)
        {
            this.flipped = flipped;
            
            repaint();
        }
    }
    
    public boolean isFlipped()
    {
        return flipped;
    }

    public CardClass getCardClass()
    {
        return GameRule.getClardClass(index);
    }
    
    public boolean isBonusCard()
    {
        return isBonusCard(getCardCode());
    }
    
    public void setActiveStatus(Status activeStatus)
    {
        if (this.activeStatus != activeStatus)
        {
            this.activeStatus = activeStatus;
            repaint();
        }
    }
    
    public Status getActiveStatus()
    {
        return activeStatus;
    }
    
    public void setMission(boolean mission)
    {
        if (this.mission != mission)
        {
            this.mission = mission;
            
            if (flipped)
                repaint();
        }
    }
    
    public boolean isMissionCard()
    {
        return mission;
    }

    public static int getCardCode(int majorCode, int minorCode)
    {
        return (majorCode << 8) | minorCode;
    }
    
    public static int getMajorCode(int cardCode)
    {
        return cardCode >> 8;
    }
    
    public static int getMinorCode(int cardCode)
    {
        return cardCode & 0x0FF;
    }
    
    public static int getCardIndex(int cardCode)
    {
        return getCardIndex(getMajorCode(cardCode), getMinorCode(cardCode));
    }
    
    public static boolean isBonusCard(int cardCode)
    {
        return getMajorCode(cardCode) > 12;
    }
    
    public Icon getCardImageIcon(CardSize cardSize)
    {
        CardImageInfo cii = getCardImageInfo(cardSize);
        Dimension size = cii.getSize();
        int srcy = index * size.height;
        BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(cii.getImage(), 0, 0, size.width, size.height,
                     0, srcy, size.width, srcy+size.height, null);
        g2.dispose();
        
        return new ImageIcon(bi);
    }
    
    public int getCardIndex()
    {
        return index;
    }
    
    public void setMouseOver(boolean mouseOver)
    {
        this.mouseOver = mouseOver;
    }
    
    private static int getCardIndex(int majorCode, int minorCode)
    {
        return (majorCode-1) * 4 + minorCode - 'a';
    }
    
    private void drawMarker(Graphics g, int x, int y, String markId,
                            Color bgColor, Color fgColor)
    {
        ((Graphics2D)g).setComposite(AlphaComposite.SrcOver.derive(0.65f));
        g.setColor(bgColor);
        g.fillRoundRect(x, y, markSize, markSize, markSize/4, markSize/4);
        
        g.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));

        // draw the text mark
        FontMetrics fm = g.getFontMetrics();
        char[] textChars = Resource.getProperty(markId).toCharArray();
        int markWidth = fm.charsWidth(textChars, 0, 1);
        int yoff = fm.getLeading()/2 + fm.getMaxAscent();
        
        g.setColor(fgColor);
        g.drawChars(textChars, 0, 1, x+(markSize-markWidth)/2,
                    y + yoff + (markSize-fm.getHeight())/2);
        
        g.setPaintMode();  // restore the composite mode
    }
}
