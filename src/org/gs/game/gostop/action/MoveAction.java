package org.gs.game.gostop.action;

import java.awt.Point;

import org.gs.game.gostop.CardSize;
import org.gs.game.gostop.GamePanel;
import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.GameTable;
import org.gs.game.gostop.TableCardPoint;
import org.gs.game.gostop.item.CardItem;

/**
 * 화투의 이동을 처리하며, 이동후 바닥에 깔거나, 치는 사람의 먹은 카드에 추가하는 처리를 포함합니다.
 */
public class MoveAction extends GameAction
{
    private Point fromPos;
    private Point toPos;
    private GamePlayer gamePlayer;
    private TableCardPoint tableCardPoint;
    private GameTable gameTable;
    
    // General move
    public MoveAction(CardItem cardItem, int duration, Point toPos)
    {
        this(cardItem, duration, toPos, null, null, null);
    }
    
    // Moves the card to the player's taken list
    public MoveAction(CardItem cardItem, int duration, GamePlayer gamePlayer)
    {
        this(cardItem, duration, gamePlayer, (TableCardPoint)null);
    }
    
    // Moves the card to the table card point
    public MoveAction(CardItem cardItem, int duration, TableCardPoint tcp)
    {
        this(cardItem, duration, tcp.getPoint(), null, tcp, null);
    }
    
    // Moves the card from the table card point to the player's taken list
    public MoveAction(CardItem cardItem, int duration,
                      GamePlayer gamePlayer, TableCardPoint tableCardPoint)
    {
        this(cardItem, duration,
             gamePlayer.getTakenRect(cardItem.getCardClass()).getLocation(),
             gamePlayer, tableCardPoint, null);
    }
    
    // Moves the card from other player's taken list to the player's taken list
    public MoveAction(CardItem cardItem, int duration,
                      GamePlayer gamePlayer, GameTable gameTable)
    {
        this(cardItem, duration,
             gamePlayer.getTakenRect(cardItem.getCardClass()).getLocation(),
             gamePlayer, null, gameTable);
    }
    
    public MoveAction(CardItem cardItem, int duration, Point toPos,
                      GamePlayer gamePlayer, TableCardPoint tableCardPoint,
                      GameTable gameTable)
    {
        super(cardItem, duration);

        fromPos = null;
        this.toPos = toPos;
        this.gamePlayer = gamePlayer;
        this.tableCardPoint = tableCardPoint;
        this.gameTable = gameTable;
    }
    
    public boolean execute(float progress)
    {
        CardItem cardItem = getCardItem();
        
        if (gamePlayer != null && tableCardPoint != null)
        {   // from table to player's taken list
            tableCardPoint.removeCardItem(cardItem);
            tableCardPoint = null;
        }
        else if (gamePlayer != null && gameTable != null)
        {   // from other's taken list to player's taken list
            gameTable.removeTakenLeaf(gamePlayer, cardItem);
            gameTable = null;
        }
        
        if (gamePlayer != null && progress >= 1.0f)
            gamePlayer.addTakenCard(cardItem);
        else if (tableCardPoint != null && progress >= 1.0f)
            tableCardPoint.addCardItem(cardItem);
        else
        {
            if (fromPos == null)
            {
                fromPos = cardItem.getRect().getLocation();
                cardItem.setZOrder(GamePanel.ANIMATION_ZORDER);
            }
            
            if (progress > 1.0f)
                progress = 1.0f;

            Point target = new Point((int)(fromPos.x+(toPos.x-fromPos.x)*progress),
                                     (int)(fromPos.y+(toPos.y-fromPos.y)*progress));
            
            if (CardSize.NORMAL.equals(cardItem.getCardSize()) == false)
                cardItem.setCardSize(CardSize.NORMAL);
            
            cardItem.moveItem(target);
        }
        
        return progress >= 1.0f;
    }
    
    private CardItem getCardItem()
    {
        return (CardItem)target;
    }
}
