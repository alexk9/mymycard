package org.gs.game.gostop.item;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.File;

import javax.swing.JComponent;

import org.gs.game.gostop.GamePanel;
import org.gs.game.gostop.GamePlayer;

/**
 * 게임참여자의 아바타를 표시해 주며, 마우스로 클릭시 사용자의 정보를 표시해 주는 대화상자를 보여줍니다.
 */
public class PlayerAvatarItem extends GameItem implements ImageObserver
{
    private GamePlayer gamePlayer;
    private Image avatar;
    private boolean loaded;
    private int width;
    private int height;
    
    public PlayerAvatarItem(JComponent parent, Rectangle rect, String avatarPath,
                            GamePlayer gamePlayer)
    {
        super(parent, rect);
     
        this.gamePlayer = gamePlayer;
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        loaded = false;
        if (new File(avatarPath).isFile())
            avatar = toolkit.getImage(avatarPath);
        else
            avatar = toolkit.getImage(getClass().getResource(avatarPath));
        toolkit.prepareImage(avatar, -1, -1, this);

        // to register it as a child item of the game panel
        fireZOrderChanged(false);
    }
    
    public void paintItem(Graphics g)
    {
        fillBackground(g);
        
        if (loaded)
            g.drawImage(avatar, rect.x, rect.y, width, height, null);
    }

    public int getZOrder()
    {
        return GamePanel.LABEL_ZORDER;
    }
    
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
    {
        loaded = (infoflags & (ALLBITS|ABORT)) != 0;

        if (loaded)
        {
            this.width = rect.width;
            this.height = rect.height;
            
            if (rect.width/width > rect.height/height)
                this.width = (int)(width * ((float)rect.height/height));
            else
                this.height = (int)(height * ((float)rect.width/width));
            
            repaint();
        }
        
        return !loaded;
    }
    
    public GamePlayer getGamePlayer()
    {
        return gamePlayer;
    }
}
