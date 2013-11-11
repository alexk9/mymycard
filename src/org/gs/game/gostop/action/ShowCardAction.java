package org.gs.game.gostop.action;

import org.gs.game.gostop.CardSize;
import org.gs.game.gostop.GamePanel;
import org.gs.game.gostop.item.CardItem;

/**
 * 카드의 가장 큰 이미지 (CardSize.BIG)를 잠시 보여줍니다 (칠 때, 뒤집을 때)
 */
public class ShowCardAction extends GameAction
{
    public ShowCardAction(CardItem cardItem, int duration)
    {
        super(cardItem, duration);
    }

    public boolean execute(float progress)
    {
        CardItem cardItem = getCardItem();
        
        if (cardItem.isFlipped() == false)
        {
            cardItem.setZOrder(GamePanel.ANIMATION_ZORDER);
            cardItem.setCardSize(CardSize.BIG, true);
            cardItem.setFlipped(true);                      // flip
        }
        
        return progress >= 1.0f;
    }

    protected CardItem getCardItem()
    {
        return (CardItem)target;
    }
}
