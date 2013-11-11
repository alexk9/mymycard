package org.gs.game.gostop.action;

import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.GameTable;
import org.gs.game.gostop.Main;
import org.gs.game.gostop.TableCardPoint;
import org.gs.game.gostop.event.GameEvent;
import org.gs.game.gostop.event.GameEventType;
import org.gs.game.gostop.item.CardItem;

/**
 * 컴의 선잡기시에 뒤집히지 않은 바닥의 카드를 선택합니다.
 */
public class PickTableCardAction extends GameAction
{
    private GameTable gameTable;
    
    public PickTableCardAction(GamePlayer player, int duration, GameTable gameTable)
    {
        super(player, duration);
        
        this.gameTable = gameTable;
    }

    public boolean execute(float progress)
    {
        boolean completed = progress >= 1.0f;
        
        if (completed)
        {
            pickCard();
            
            if (nextActions == null)   // final pick
                fireActionEvent(new GameEvent(null, GameEventType.PICK_LEAD_COMPLETED));
        }
        
        return completed;
    }
    
    private void pickCard()
    {
        CardItem cardItem;
        TableCardPoint[] tableCardPoints = gameTable.getTableCardPoints();
        
        do
        {
            int index = Main.getRandom().nextInt(tableCardPoints.length);
            if (tableCardPoints[index].isEmpty())
                cardItem = null;
            else
                cardItem = tableCardPoints[index].getCardItems().get(0);
        } while (cardItem == null || cardItem.isFlipped());
        
        cardItem.setFlipped(true);
        getGamePlayer().setSelectedCard(cardItem);
    }
    
    private GamePlayer getGamePlayer()
    {
        return (GamePlayer)target;
    }
}
