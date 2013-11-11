package org.gs.game.gostop.config;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 게임의위치정보를저장하고있는, 데이터클래스
 */
public class GameLayout
{
    private static final String OPPOSITE_PREFIX = "OPPOSITE.";
    
    private int bgWidth;
    private int bgHeight;
    private String bgImage;
    private Rectangle rectMission;
    private Rectangle rectStack;
    private Dimension sizeAlert;
    private List<PlayerLayout> playerLayouts;
    private List<TextFont> textFonts;
    
    private static enum FontStyle
    {
        PLAIN(Font.PLAIN),
        BOLD(Font.BOLD),
        ITALIC(Font.ITALIC);
        
        private int style;
        
        private FontStyle(int style)
        {
            this.style = style;
        }
        
        private int getStyle()
        {
            return style;
        }
    }
    
    private static enum FontColor
    {
        BLACK(Color.BLACK),
        BLUE(Color.BLUE),
        CYAN(Color.CYAN),
        DARK_GRAY(Color.DARK_GRAY),
        GRAY(Color.GRAY),
        GREEN(Color.GREEN),
        LIGHT_GRAY(Color.LIGHT_GRAY),
        MAGENTA(Color.MAGENTA),
        ORANGE(Color.ORANGE),
        PINK(Color.PINK),
        RED(Color.RED),
        WHITE(Color.WHITE),
        YELLOW(Color.YELLOW);
     
        private Color color;
        
        private FontColor(Color color)
        {
            this.color = color;
        }
        
        private Color getColor()
        {
            return color;
        }
    }
    
    public GameLayout(Rectangle rectMission, Rectangle rectStack, Dimension sizeAlert)
    {
        setBackgroundInfo(0, 0, null);
        this.rectMission = rectMission;
        this.rectStack = rectStack;
        this.sizeAlert = sizeAlert;
        playerLayouts = null;
    }
    
    public void setBackgroundInfo(int bgWidth, int bgHeight, String bgImage)
    {
        this.bgWidth = bgWidth;
        this.bgHeight = bgHeight;
        this.bgImage = bgImage;
    }
    
    public Dimension getBackgroundSize()
    {
        return new Dimension(bgWidth, bgHeight);
    }

    public URL getBackgroundImageURL()
    {
        return getClass().getResource(bgImage);
    }
    
    public Rectangle getMissionRect()
    {
        return rectMission;
    }
    
    public Rectangle getStackRect()
    {
        return rectStack;
    }
    
    public Dimension getAlertsize()
    {
        return sizeAlert;
    }
    
    public void addPlayerLayout(boolean opposite, String playerSide, Rectangle rect,
                                Rectangle rectText, Rectangle rectAvatar,
                                Rectangle rectPoint, Rectangle rectPenalty,
                                Rectangle rectBonus, Point pointAlert, Rectangle rectHold,
                                Rectangle rectKings, Rectangle rectTens,
                                Rectangle rectFives, Rectangle rectLeaves)
    {
        if (playerLayouts == null)
            playerLayouts = new ArrayList<PlayerLayout>();
        
        playerLayouts.add(new PlayerLayout(opposite, playerSide, rect, rectText, rectAvatar,
                                           rectPoint, rectPenalty, rectBonus, pointAlert,
                                           rectHold, rectKings, rectTens, rectFives,
                                           rectLeaves));
    }
    
    public List<PlayerLayout> getPlayerLayouts()
    {
        return playerLayouts;
    }
    
    public void addTextFont(String target, String fontName, int fontSize,
                            String fontStyle, String fontColor)
    {
        boolean opposite;
        
        target = target.toUpperCase();
        if (target.startsWith(OPPOSITE_PREFIX))
        {
            target = target.substring(OPPOSITE_PREFIX.length());
            opposite = true;
        }
        else
            opposite = false;
        TextTarget textTarget = GameConfig.valueOf(TextTarget.class, target);

        int style = Font.PLAIN;
        
        if (fontStyle != null)
        {
            StringTokenizer st = new StringTokenizer(fontStyle, ",|");
            
            while (st.hasMoreTokens())
                style |= GameConfig.valueOf(FontStyle.class, st.nextToken()).getStyle();
        }
        
        Color color = GameConfig.valueOf(FontColor.class, fontColor).getColor();
        
        if (textFonts == null)
            textFonts = new ArrayList<TextFont>();
        
        textFonts.add(new TextFont(textTarget, opposite, fontName, fontSize, style, color));
    }
    
    public TextFont getTextFont(TextTarget target, boolean opposite)
    {
        TextFont textFont = null;
        
        if (textFonts != null)
        {
            for (int i = 0; i < textFonts.size() && textFont == null; i++)
            {
                TextFont tf = textFonts.get(i);
                if (target == tf.getTarget() && opposite == tf.isOpposite())
                    textFont = tf;
            }
        }
        
        return textFont;
    }
}
