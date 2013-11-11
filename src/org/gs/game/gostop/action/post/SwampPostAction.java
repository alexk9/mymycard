package org.gs.game.gostop.action.post;

import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.GameTable;
import org.gs.game.gostop.action.GameAction;
import org.gs.game.gostop.event.GameEvent;
import org.gs.game.gostop.event.GameEventManager;
import org.gs.game.gostop.event.GameEventResult;
import org.gs.game.gostop.event.GameEventType;

/**
 * 뻑의 횟수를 증가시켜주고, 3번 뻑을 한 경우 게임을 중지시킵니다.
 */
public class SwampPostAction implements IGamePostAction
{
    private GamePlayer gamePlayer;
    private GameTable gameTable;
    private int takePoints;
    private boolean addSwampCount;
    
    public SwampPostAction(GamePlayer gamePlayer, GameTable gameTable,
                           int takePoints, boolean addSwampCount)
    {
        this.gamePlayer = gamePlayer;
        this.gameTable = gameTable;
        this.takePoints = takePoints;
        this.addSwampCount = addSwampCount;
    }

    public void onActionComplete(GameAction ga)
    {
        if (addSwampCount)
            gamePlayer.addSwampCount();
        
        if (takePoints > 0)
            gameTable.takePoints(gamePlayer, takePoints);
        
        if (gamePlayer.getSwampCount() == 3)
        {
            int winPoints = gameTable.getGameType().getWinPoints();
            GameEventResult result = new GameEventResult(false, null, winPoints);
            GameEvent gameEvent = new GameEvent(gamePlayer, GameEventType.GO_DECIDED, result);
            
            GameEventManager.fireGameEvent(gameEvent, false);
        }
    }
}
