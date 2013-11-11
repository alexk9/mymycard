package org.gs.game.gostop.action.post;

import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.action.GameAction;

/**
 * 폭탄 후 흔들기 횟수를 증가시켜줍니다
 */
public class BombPostAction implements IGamePostAction
{
    private GamePlayer gamePlayer;
    
    public BombPostAction(GamePlayer gamePlayer)
    {
        this.gamePlayer = gamePlayer;
    }

    public void onActionComplete(GameAction ga)
    {
        gamePlayer.addTripleCount();
    }
}
