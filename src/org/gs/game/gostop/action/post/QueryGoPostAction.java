package org.gs.game.gostop.action.post;

import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.action.GameAction;
import org.gs.game.gostop.action.ShowGoAction;
import org.gs.game.gostop.event.GameEvent;

/**
 * 고할 것인가를 묻고 난 후의 처리로, 고한경우 고 메시지를 표시하고, 스톱을 선택한 경우 게임을 중지시킵니다.
 */
public class QueryGoPostAction implements IGamePostAction
{
    private GamePlayer gamePlayer;
    
    public QueryGoPostAction(GamePlayer gamePlayer)
    {
        this.gamePlayer = gamePlayer; 
    }
    
    public void onActionComplete(GameAction ga)
    {
        boolean go = ga.getResult() instanceof Boolean && (Boolean)ga.getResult();
        
        if (go)
        {
            ShowGoAction sga = new ShowGoAction(gamePlayer, 20, null, false);
            
            if (ga.getCompleteEventType() != null)
            {
                sga.setCompleteEvent(getGoGameEvent(go, ga));
                sga.setCompleteEvent(ga.getCompleteEvent());
                ga.setCompleteEventType(null);
            }
            
            ga.setNextAction(sga);
        }
        else if (ga.getCompleteEventType() != null)
            ga.setCompleteEvent(getGoGameEvent(go, ga));
    }

    private GameEvent getGoGameEvent(boolean go, GameAction ga)
    {
        return new GameEvent(gamePlayer, ga.getCompleteEventType(), go);
    }
}
