package org.gs.game.gostop.action;

import java.awt.Point;
import java.awt.Rectangle;

import org.gs.game.gostop.Main;
import org.gs.game.gostop.item.CardItem;

/**
 * 화투를 섞는 것처럼 보이게 하는 이동을 처리합니다.
 */
public class RandomMoveAction extends GameAction
{
    private Rectangle bound;
    
    /**
     * Initializes a random update item
     * 
     * @param item the item to update
     * @param bound the bound rectangle
     */
    public RandomMoveAction(CardItem item, int duration, Rectangle bound)
    {
        super(item, duration);
        
        this.bound = bound;
    }

    public boolean execute(float progress)
    {
        boolean completed = progress >= 1.0f;
        CardItem item = getCardItem();
        
        if (completed)
            item.setFlipped(false);
        else
        {
            Rectangle rect = item.getRect();
            int x = bound.x + Main.getRandom().nextInt(bound.width - rect.width);
            int y = bound.y + Main.getRandom().nextInt(bound.height - rect.height);
            
            item.moveItem(new Point(x,y));
        }
        
        return completed;
    }

    private CardItem getCardItem()
    {
        return (CardItem)target;
    }
}
