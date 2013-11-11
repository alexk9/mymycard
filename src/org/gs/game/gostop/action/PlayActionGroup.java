package org.gs.game.gostop.action;

import java.util.List;

import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.GameTable;
import org.gs.game.gostop.TableCardPoint;
import org.gs.game.gostop.action.post.BombPostAction;
import org.gs.game.gostop.action.pre.PlaySoundPreAction;
import org.gs.game.gostop.event.GameEventType;
import org.gs.game.gostop.item.AlertItem;
import org.gs.game.gostop.item.CardItem;
import org.gs.game.gostop.sound.GameSoundManager;

/**
 * 게임 플레이어가 화투를 선택하였을 때의 화투를 바닥까지 내려놓는 과정을 처리합니다.
 * 폭탄 처리, 보너스 카드 내려 놓기, 일반 치기 및 (폭탄한 후 다음 순서에서) 뒤집기 등을 구분해서 처리합니다.
 * FlipDeckActionGroup을 생성해 연결 GameAction으로 지정하여 게임을 진행합니다.
 */
public class PlayActionGroup extends GameActionGroup
{
    private GameTable gameTable;
    private CardItem cardItem;
    private int selected;
    
    public PlayActionGroup(GamePlayer player, int actionUnit,
                           CardItem cardItem, GameTable gameTable)
    {
        this(player, actionUnit, cardItem, -1, gameTable);
    }
    
    public PlayActionGroup(GamePlayer player, int actionUnit,
                           CardItem cardItem, int selected, GameTable gameTable)
    {
        super(player, actionUnit);
    
        this.cardItem = cardItem;
        this.selected = selected;
        this.gameTable = gameTable;
        setCompleteEventType(GameEventType.PLAY_COMPLETED);
    }
    
    public void execute()
    {
        GamePlayer gamePlayer = getGamePlayer();
        
        gamePlayer.setKeepTurn(false);
        
        if (gameTable.getTableCardPoint(cardItem.getMajorCode(), false) != null
            && gamePlayer.getHoldCardCount(cardItem.getMajorCode()) == 3)
            fireBomb(gamePlayer);
        else if (gamePlayer.isHoldingCard(cardItem.getCardCode()))
        {
            if (cardItem.isBonusCard())
            {
                gamePlayer.setKeepTurn(true);
                laydownBonus(gamePlayer);
            }
            else
                laydownAndFlip(gamePlayer);
        }
        else
            flipOnly(gamePlayer);
    }
    
    private void laydownBonus(GamePlayer gamePlayer)
    {
        GameAction cur = new ShowCardAction(cardItem, actionUnit);      // show
        lastAction = firstAction = cur;
        gamePlayer.removeHoldCard(cardItem);
        
        cur = new MoveAction(cardItem, actionUnit, gamePlayer);         // lay-down
        lastAction.setNextAction(cur);
        lastAction = cur;
        
        CardItem nextCard = gameTable.getTopDeckCard(true);
        cur = new DealAction(nextCard, actionUnit, gamePlayer, true, true); // take-from-deck
        lastAction.setNextAction(cur);
        lastAction = cur;
    }
    
    private void fireBomb(GamePlayer gamePlayer)
    {
        TableCardPoint tcp = gameTable.getTableCardPoint(cardItem.getMajorCode(), false);
        List<CardItem> cardItems = gamePlayer.getHoldCards(cardItem.getMajorCode(), true);
        GameAction cur;
        
        tcp.setBombTarget(true);
        gamePlayer.addFlipCount(2);
        
        for (CardItem ci: cardItems)
        {
            ci.setActiveStatus(CardItem.Status.NONE);
            ci.setCanClick(false);
            
            cur = new ShowCardAction(ci, actionUnit);         // show
            if (lastAction == null)
                firstAction = cur;
            else
                lastAction.setNextAction(cur);
            lastAction = cur;
            
            cur = new MoveAction(ci, actionUnit, tcp);        // lay-down to table
            cur.setPreExecuteAction(new PlaySoundPreAction(GameSoundManager.SOUND_PUT_ON_CARD));
            lastAction.setNextAction(cur);
            lastAction = cur;
        }
        
        cur = new ShowAlertAction(gamePlayer, actionUnit*5,
                                  AlertItem.ALERT_BOMB, tcp.getPoint());  // show-alert
        cur.addGamePostAction(new BombPostAction(gamePlayer));
        lastAction.setNextAction(cur);
        lastAction = cur;
        
        cur = new FlipDeckActionGroup(gamePlayer, actionUnit, gameTable, tcp, selected);
        lastAction.setNextAction(cur);
        lastAction = cur;
    }
    
    private void laydownAndFlip(GamePlayer gamePlayer)
    {
        TableCardPoint tcp = gameTable.getTableCardPoint(cardItem.getMajorCode(), true);
        
        GameAction cur = new ShowCardAction(cardItem, actionUnit);      // show
        lastAction = firstAction = cur;
        gamePlayer.removeHoldCard(cardItem);
        
        cur = new MoveAction(cardItem, actionUnit, tcp);                // lay-down to table
        String soundId = tcp.getCardCount(false) == 0 ? GameSoundManager.SOUND_PUT_ON_EMPTY
                                                        : GameSoundManager.SOUND_PUT_ON_CARD;
        cur.setPreExecuteAction(new PlaySoundPreAction(soundId));
        lastAction.setNextAction(cur);
        lastAction = cur;
        
        if (tcp.getCardCount(true) == 3)                                // taking swamped cards
        {
            cur = new ShowAlertAction(gamePlayer, actionUnit*5,
                                      AlertItem.ALERT_LUCKY, tcp.getPoint());      // show-alert
            lastAction.setNextAction(cur);
            lastAction = cur;
        }
        
        cur = new FlipDeckActionGroup(gamePlayer, actionUnit, gameTable, tcp, selected);
        lastAction.setNextAction(cur);
        lastAction = cur;
    }
    
    private void flipOnly(GamePlayer gamePlayer)
    {
        gamePlayer.addFlipCount(-1);
        
        GameAction cur = new FlipDeckActionGroup(gamePlayer, actionUnit, gameTable,
                                                 null, selected);
        firstAction = lastAction = cur;
    }
}
