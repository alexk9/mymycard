package org.gs.game.gostop.config;

import java.awt.Color;
import java.awt.Font;

public class TextFont
{
    private TextTarget target;
    private boolean opposite;
    private String fontName;
    private int fontSize;
    private int fontStyle;
    private Color fontColor;
    
    protected TextFont(TextTarget target, boolean opposite, String fontName,
                     int fontSize, int fontStyle, Color fontColor)
    {
        this.target = target;
        this.opposite = opposite;
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
        this.fontColor = fontColor;
    }
    
    public TextTarget getTarget()
    {
        return target;
    }
    
    public boolean isOpposite()
    {
        return opposite;
    }
    
    public String getFontName()
    {
        return fontName;
    }
    
    public int getFontSize()
    {
        return fontSize;
    }
    
    public int getFontStyle()
    {
        return fontStyle;
    }

    public Font getFont()
    {
        return new Font(fontName, fontStyle, fontSize);
    }
    
    public Color getFontColor()
    {
        return fontColor;
    }
}
