package org.gs.game.gostop.action.post;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.gs.game.gostop.CardClass;
import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.GameTable;
import org.gs.game.gostop.PlayerStatus;
import org.gs.game.gostop.action.GameAction;
import org.gs.game.gostop.action.ShowAlertAction;
import org.gs.game.gostop.item.AlertItem;
import org.gs.game.gostop.play.GameRule;
import org.gs.game.gostop.play.GameRule.GoStopRule;

/**
 * 새로운 약이나 미션의 완성을 검사합니다.
 */
public class CheckNewRulePointsPostAction implements IGamePostAction
{
    private GamePlayer gamePlayer;
    private GameTable gameTable;
    private GameAction curAction;
    private GameAction lastAction;
    private int actionUnit;
    
    public CheckNewRulePointsPostAction(GamePlayer gamePlayer, GameTable gameTable,
                                        GameAction curAction, int actionUnit)
    {
        this.gamePlayer = gamePlayer;
        this.gameTable = gameTable;
        this.curAction = curAction;
        this.actionUnit = actionUnit;
        lastAction = null;
    }
    
    public void onActionComplete(GameAction ga)
    {
        PlayerStatus playerStatus = gamePlayer.getPlayerStatus();
        List<GoStopRule> rules = new ArrayList<GoStopRule>();
        List<GoStopRule> rTemp;
        
        rTemp = GameRule.getGoStopRule(playerStatus.getTakenCards(CardClass.TEN));
        if (rTemp != null)
            rules.addAll(rTemp);
        
        rTemp = GameRule.getGoStopRule(playerStatus.getTakenCards(CardClass.FIVE));
        if (rTemp != null)
            rules.addAll(rTemp);
        
        if (rules.size() > 0
            && (gamePlayer.getRuleIds() == null
                || rules.size() > gamePlayer.getRuleIds().size()))
            addAlertActions(rules);

        int missionBonus;
        if ((missionBonus = gameTable.checkNewMission(gamePlayer)) > 0)
        {
            Point pt = gamePlayer.getTakenRect(CardClass.TEN).getLocation();
            
            if (gamePlayer.isOpposite())
                pt.x = gamePlayer.getAlertLocation().x;
            else
                pt.y = gamePlayer.getAlertLocation().y;
            
            GameAction cur = new ShowAlertAction(gamePlayer, actionUnit*5,
                                                 AlertItem.ALERT_MISSION, pt);  // show-alert
            IGamePostAction ambpa = new AddMissionBonusPostAction(gamePlayer,
                                                                  gameTable,
                                                                  missionBonus);
            cur.addGamePostAction(ambpa);
            if (lastAction == null)
                lastAction = curAction;
            lastAction.setNextAction(cur);
            lastAction = cur;
        }
        
        if (curAction.getCompleteEventType() != null
            && lastAction != null && lastAction != curAction)
        {
            lastAction.setCompleteEvent(curAction.getCompleteEvent());
            curAction.setCompleteEventType(null);
        }
    }
    
    private void addAlertActions(List<GoStopRule> rules)
    {
        List<String> ruleIds = gamePlayer.getRuleIds();
        GameAction cur;
        
        lastAction = curAction;
        
        if (ruleIds == null)
            ruleIds = new ArrayList<String>();
        
        for (GoStopRule rule: rules)
        {
            if (ruleIds.contains(rule.getRuleId()) == false)
            {
                Point pt = gamePlayer.getTakenRect(rule.getRuleCardClass()).getLocation();
                
                if (gamePlayer.isOpposite())
                    pt.x = gamePlayer.getAlertLocation().x;
                else
                    pt.y = gamePlayer.getAlertLocation().y;
                
                cur = new ShowAlertAction(gamePlayer, actionUnit*5,
                                          rule.getRuleId(), pt);      // show-alert
                lastAction.setNextAction(cur);
                lastAction = cur;
                
                ruleIds.add(rule.getRuleId());
            }
        }
        
        gamePlayer.setRuleIds(ruleIds);
    }
}
