package org.gs.game.gostop;

import java.awt.Dimension;
import java.awt.Image;

/**
 * 카드이미지정보, 사이즈와 실제이미지객체
 */
public class CardImageInfo
{
    private Dimension size;
    private Image image;
    
    public CardImageInfo(Dimension size, Image image)
    {
        this.size = size;
        this.image = image;
    }

    public Dimension getSize()
    {
        return size;
    }
    
    public Image getImage()
    {
        return image;
    }
}
