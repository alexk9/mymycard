package org.gs.game.gostop.action;

import java.util.List;

import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.GameTable;
import org.gs.game.gostop.TableCardPoint;
import org.gs.game.gostop.action.post.CheckNewRulePointsPostAction;
import org.gs.game.gostop.action.post.SwampPostAction;
import org.gs.game.gostop.action.pre.PlaySoundPreAction;
import org.gs.game.gostop.item.AlertItem;
import org.gs.game.gostop.item.CardItem;
import org.gs.game.gostop.sound.GameSoundManager;

/**
 * 상황에 따른 메시지 (따닥, 뻑, 쪽, 횡재, 쓸 등)을 표시하고,
 * 바닥의 화투를 치는 사람의 먹은 카드위치로 이동을 처리합니다.
 * 필요시 상대방의 피를 가져오는 처리도 실행합니다.
 * 새로운 약이나 미션을 완성했는지 확인하는 CheckNewRulePointsPostAction을 후처리로 등록합니다.
 */
public class TakeActionGroup extends GameActionGroup
{
    private GameTable gameTable;
    private TableCardPoint layTcp;
    private int selected;
    private TableCardPoint flipTcp;
    private int selectedFlip;
    int takeOthers;
    
    public TakeActionGroup(GamePlayer player, int actionUnit, GameTable gameTable,
                           TableCardPoint layTcp, int selected,
                           TableCardPoint flipTcp, int selectedFlip)
    {
        super(player, actionUnit);

        this.gameTable = gameTable;
        this.layTcp = layTcp;
        this.selected = selected;
        this.flipTcp = flipTcp;
        setSelectedFlip(selectedFlip);
        takeOthers = 0;
    }
    
    public void setSelectedFlip(int selectedFlip)
    {
        this.selectedFlip = selectedFlip;
    }

    public void execute()
    {
        String alertMsg = showAlert();
        
        if (AlertItem.ALERT_THIRD_SWAMP.equals(alertMsg))
            setCompleteEventType(null);         // it will be handled by SwampPostAction
        else
            takeCards(alertMsg);
    }

    private String showAlert()
    {
        String alertMsg;
        GamePlayer gamePlayer = getGamePlayer();
        GameAction cur;

        if (gameTable.isLastTurn())
            alertMsg = null;
        else if (flipTcp == layTcp)
        {
            int count = layTcp.getCardCount(true);
            
            if (count == 4)                                         // DDaDak
            {
                if (gameTable.isFirstTurn())
                    gamePlayer.setKeepSwamping(true);
                alertMsg = AlertItem.ALERT_CONCUR;
            }
            else if (count == 3)                // PPeok (Swamp), no swamp on last turn
            {
                if (gameTable.isFirstTurn())
                {
                    gamePlayer.setKeepSwamping(true);
                    alertMsg = AlertItem.ALERT_FIRST_SWAMP;
                }
                else if (gamePlayer.keepSwamping())
                {
                    if (gamePlayer.getSwampCount() == 0)
                        alertMsg = AlertItem.ALERT_FIRST_SWAMP;
                    else if (gamePlayer.getSwampCount() == 1)
                        alertMsg = AlertItem.ALERT_SECOND_SWAMP;
                    else
                        alertMsg = AlertItem.ALERT_THIRD_SWAMP;
                }
                else if (gamePlayer.getSwampCount() >= 2)
                    alertMsg = AlertItem.ALERT_THIRD_SWAMP;
                else
                    alertMsg = AlertItem.ALERT_SWAMP;
                layTcp.setSwampedPlayer(gamePlayer);
            }
            else 
            {
                gamePlayer.setKeepSwamping(false);
                if (count == 2)                                    // JJok
                    alertMsg = AlertItem.ALERT_SMACK;
                else
                    alertMsg = null;
            }
            
            if (alertMsg != null)
            {
                cur = new ShowAlertAction(gamePlayer, actionUnit*5,
                                          alertMsg, layTcp.getPoint());      // show-alert
                firstAction = lastAction = cur;
                
                if (count == 3 || gamePlayer.keepSwamping())
                {
                    int takePoints = 0;
                    
                    if (gamePlayer.keepSwamping())
                        takePoints = gameTable.getGameType().getWinPoints() * gameTable.getCurTurn();
                    
                    cur.addGamePostAction(new SwampPostAction(gamePlayer, gameTable,
                                                              takePoints, count == 3));
                }
            }
        }
        else if (flipTcp.getCardCount(true) == 4 && gameTable.isLastTurn() == false)    // taking swamped cards
        {
            gamePlayer.setKeepSwamping(false);
            alertMsg = AlertItem.ALERT_LUCKY;
            cur = new ShowAlertAction(gamePlayer, actionUnit*3,
                                      alertMsg, flipTcp.getPoint());      // show-alert
            if (lastAction == null)
                firstAction = cur;
            else
                lastAction.setNextAction(cur);
            lastAction = cur;
        }
        else
        {
            gamePlayer.setKeepSwamping(false);
            alertMsg = null;
        }
        
        return alertMsg;
    }
    
    private void takeCards(String alertMsg)
    {
        GamePlayer gamePlayer = getGamePlayer();
        GameAction cur;
        boolean empty = true;
        
        if (AlertItem.ALERT_SMACK.equals(alertMsg))
            takeOthers++;
        
        if (layTcp != null)
            empty = takeCards(layTcp, selected, layTcp == flipTcp);
        
        if (layTcp != flipTcp)
            empty = takeCards(flipTcp, selectedFlip, layTcp == flipTcp) && empty;
        
        if (empty && gameTable.isLastTurn() == false)   // check for sweep (SSeul)
        {
            TableCardPoint[] excludes = { layTcp, flipTcp };
            if (gameTable.isTableEmpty(excludes))
            {
                cur = new ShowAlertAction(gamePlayer, actionUnit*3,
                                          AlertItem.ALERT_SWEEP, flipTcp.getPoint());   // show-alert
                lastAction.setNextAction(cur);
                lastAction = cur;
                takeOthers++;
            }
        }
        
        if (takeOthers > 0)
        {
            int i = 0;
            for (CardItem ci: gameTable.getOtherLeaves(gamePlayer, takeOthers))
            {
                cur = new MoveAction(ci, actionUnit, gamePlayer, gameTable);    // take-from-others
                if ((i++ % 4) == 0)
                    cur.setPreExecuteAction(new PlaySoundPreAction(GameSoundManager.SOUND_MOVE));
                lastAction.setNextAction(cur);
                lastAction = cur;
            }
        }
        
        if (lastAction == null)                                                 // to check mission
            firstAction = lastAction = new ShowAlertAction(gamePlayer, 0, null);
        
        lastAction.addGamePostAction(new CheckNewRulePointsPostAction(gamePlayer,
                                                                      gameTable,
                                                                      lastAction,
                                                                      actionUnit));
    }
    
    private boolean takeCards(TableCardPoint tcp, int selected, boolean sameTcp)
    {
        boolean empty = false;
        GamePlayer gamePlayer = getGamePlayer();
        GameAction cur;
        int countOWBonus = tcp.getCardCount(true);
        
        if (countOWBonus == 4 || countOWBonus == 2)
        {
            if (countOWBonus == 4 && gameTable.isLastTurn() == false)
                takeOthers += tcp.isSwampedPlayer(gamePlayer) ? 2 : 1;

            int i = 0;
            for (CardItem ci: tcp.getCardItems())
            {
                cur = new MoveAction(ci, actionUnit, gamePlayer, tcp);   // take
                if ((i++ % 4) == 0)
                    cur.setPreExecuteAction(new PlaySoundPreAction(GameSoundManager.SOUND_MOVE));
                if (lastAction == null)
                    firstAction = cur;
                else
                    lastAction.setNextAction(cur);
                lastAction = cur;
            }

            empty = true;
        }
        else if (countOWBonus == 3 && (selected >= 0 || sameTcp == false
                                       || gameTable.isLastTurn()))  // no swamp on last turn
        {
            List<CardItem> cardItems = tcp.getCardItems();
            
            if (selected < 0 && sameTcp == false)
                selected = 0;
            
            for (int i = 0; i < cardItems.size(); i++)
            {
                if (selected >= 0 && (i == selected || i >= 2)
                    || (selected < 0 && gameTable.isLastTurn() && (i+1) < cardItems.size()))
                                                                    // no swamp on last turn
                {
                    cur = new MoveAction(cardItems.get(i), actionUnit, gamePlayer, tcp);   // take
                    if ((i % 4) == 0)
                        cur.setPreExecuteAction(new PlaySoundPreAction(GameSoundManager.SOUND_MOVE));
                    if (lastAction == null)
                        firstAction = cur;
                    else
                        lastAction.setNextAction(cur);
                    lastAction = cur;
                }
            }
        }
        
        return empty;
    }
}
