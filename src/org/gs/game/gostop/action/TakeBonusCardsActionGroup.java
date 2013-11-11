package org.gs.game.gostop.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gs.game.gostop.*;
import org.gs.game.gostop.action.pre.PlaySoundPreAction;
import org.gs.game.gostop.item.CardItem;
import org.gs.game.gostop.sound.GameSoundManager;

/**
 * 게임 시작시, 바닥에 보너스패가 깔려 있을 경우에 보너스패를 선의 먹은 패 위치로 이동시키고, 새로운 패를 바닥에 뒤집어 놓습니다.
 */
public class TakeBonusCardsActionGroup extends GameActionGroup
{
    private GameTable gameTable;
    private List<CardItem> bonusCards;
    
    public TakeBonusCardsActionGroup(GamePlayer player, int actionUnit,
                                     GameTable gameTable, List<CardItem> bonusCards)
    {
        super(player, actionUnit);
        
        this.gameTable = gameTable;
        this.bonusCards = bonusCards;
        firstAction = null;
    }

    public void execute()
    {
        takeBonusCards();
        
        HashMap<TableCardPoint,List<CardItem>> mapCnt = new HashMap<TableCardPoint,List<CardItem>>();
        int flipCount = bonusCards.size();
        
        for (int i = 0; i < flipCount; i++)
        {
            CardItem cardItem = gameTable.getTopDeckCard(true);
            int majorCode = cardItem.getMajorCode();
            
            if (majorCode > 12)   // bonus card: show + take
            {
                showAndTake(cardItem, i % 4 == 0);
                flipCount++;
            }
            else    // check four cards
            {
                TableCardPoint tcp = gameTable.getTableCardPoint(majorCode);
                List<CardItem> items;
                
                if (mapCnt.containsKey(tcp))
                {
                    items = mapCnt.get(tcp);
                    items.add(cardItem);
                }
                else
                {
                    items = new ArrayList<CardItem>();
                    if (tcp.getCardItems() != null)
                        items.addAll(tcp.getCardItems());
                    items.add(cardItem);
                    mapCnt.put(tcp, items);
                }
                
                showAndPlace(cardItem, tcp, items, i % 4 == 0);
            }
        }
    }

    private void takeBonusCards()
    {
        GamePlayer gamePlayer = getGamePlayer();
        GameAction cur;
        
        lastAction = null;
        
        int i = 0;
        for (CardItem cardItem: bonusCards)
        {
            cur = new MoveAction(cardItem, actionUnit, gamePlayer);
            if ((i++ % 4) == 0)
                cur.setPreExecuteAction(new PlaySoundPreAction(GameSoundManager.SOUND_MOVE));
            if (lastAction == null)
                firstAction = cur;
            else
                lastAction.setNextAction(cur);
            lastAction = cur;
        }
    }
    
    private void showAndTake(CardItem cardItem, boolean sound)
    {
        GameAction cur = new ShowCardAction(cardItem, actionUnit);
        lastAction.setNextAction(cur);
        lastAction = cur;
        
        cur = new MoveAction(cardItem, actionUnit, getGamePlayer());
        cur.setPreExecuteAction(new PlaySoundPreAction(GameSoundManager.SOUND_MOVE));
        lastAction.setNextAction(cur);
        lastAction = cur;
    }
    
    private void showAndPlace(CardItem cardItem, TableCardPoint tcp,
                              List<CardItem> items, boolean sound)
    {
        GameAction cur = new ShowCardAction(cardItem, actionUnit);      // show
        lastAction.setNextAction(cur);
        lastAction = cur;
        
        cur = new MoveAction(cardItem, actionUnit, tcp);                // place
        if (sound)
            cur.setPreExecuteAction(new PlaySoundPreAction(GameSoundManager.SOUND_PUT_ON_EMPTY));
        lastAction.setNextAction(cur);
        lastAction = cur;
        
        if (items.size() == 4)                   // take four cards on the table
        {
            GamePlayer gamePlayer = getGamePlayer();
            
            for (int k = 0; k < items.size(); k++)
            {
                CardItem ci = items.get(k);
                
                cur = new MoveAction(ci, actionUnit, gamePlayer, tcp);
                
                if (k == 0)
                    lastAction.setNextAction(cur);
                else
                    lastAction.addNextAction(cur);                // simultaneous move
            }
            
            lastAction = cur;
        }
    }
}
