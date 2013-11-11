package org.gs.game.gostop.action.post;

import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.GameTable;
import org.gs.game.gostop.action.GameAction;

/**
 * 미션을 완성한 경우 미션 보너스를 치는 사람에게 더해줍니다
 */
public class AddMissionBonusPostAction implements IGamePostAction
{
    private GamePlayer gamePlayer;
    private GameTable gameTable;
    private int missionBonus;
    
    public AddMissionBonusPostAction(GamePlayer gamePlayer, GameTable gameTable,
                                     int missionBonus)
    {
        this.gamePlayer = gamePlayer;
        this.gameTable = gameTable;
        this.missionBonus = missionBonus;
    }

    public void onActionComplete(GameAction ga)
    {
        gameTable.addMissionBonus(gamePlayer, missionBonus);
    }
}
