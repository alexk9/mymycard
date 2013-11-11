package org.gs.game.gostop.action;

import java.awt.Point;

import org.gs.game.gostop.CardSize;
import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.GameTable;
import org.gs.game.gostop.TableCardPoint;
import org.gs.game.gostop.action.pre.PlaySoundPreAction;
import org.gs.game.gostop.item.CardItem;
import org.gs.game.gostop.sound.GameSoundManager;

/**
 *  게임시작시 화투를 바닥에 깔거나 치는 사람에게 나눠주기를 처리합니다.
 */
public class DealAction extends GameAction
{
    private Point fromPos;
    private Point toPos;
    private boolean turnOnComplete;
    private CardSize finalCardMode;
    private TableCardPoint tableCardPoint;
    private GamePlayer gamePlayer;
    boolean sortHoldCards;
    
    public DealAction(CardItem cardItem, int duration, GameTable gameTable,
                      boolean sound)
    {
        super(cardItem, duration);
        
        fromPos = cardItem.getRect().getLocation();
        this.tableCardPoint = gameTable.getTableCardPoint(-1);
        toPos = tableCardPoint.getPoint();
        turnOnComplete = true;
        finalCardMode = cardItem.getCardSize();
        gamePlayer = null;
        
        if (sound)
            setPreExecuteAction(new PlaySoundPreAction(GameSoundManager.SOUND_MOVE));
    }

    public DealAction(CardItem cardItem, int duration, GamePlayer gamePlayer,
                      boolean sound)
    {
        this(cardItem, duration, gamePlayer, false, sound);
    }

    public DealAction(CardItem cardItem, int duration, GamePlayer gamePlayer,
                      boolean sortHoldCards, boolean sound)
    {
        super(cardItem, duration);
        
        fromPos = cardItem.getRect().getLocation();
        toPos = null;
        this.gamePlayer = gamePlayer;
        this.sortHoldCards = sortHoldCards;
        this.turnOnComplete = gamePlayer.isOpposite() == false;
        this.finalCardMode = gamePlayer.isOpposite() ? CardSize.SMALL : CardSize.HOLD;;
        tableCardPoint = null;
        
        if (sound)
            setPreExecuteAction(new PlaySoundPreAction(GameSoundManager.SOUND_MOVE));
    }
    
    public boolean execute(float progress)
    {
        boolean completed = progress >= 1.0f;
        CardItem cardItem = getCardItem();

        if (progress > 1.0f)
            progress = 1.0f;
        
        if (gamePlayer != null && toPos == null)
            toPos = gamePlayer.addHoldCard(cardItem, sortHoldCards);
        
        Point target = new Point((int)(fromPos.x+(toPos.x-fromPos.x)*progress),
                                 (int)(fromPos.y+(toPos.y-fromPos.y)*progress));
        
        cardItem.moveItem(target);
        
        if (completed)
        {
            if (turnOnComplete)
                cardItem.setFlipped(true);
            
            if (finalCardMode != null)
                cardItem.setCardSize(finalCardMode);
            
            if (tableCardPoint != null)
                tableCardPoint.addCardItem(cardItem);
        }
        
        return completed;
    }

    private CardItem getCardItem()
    {
        return (CardItem)target;
    }
}
