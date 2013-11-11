package org.gs.game.gostop.action.post;

import org.gs.game.gostop.action.GameAction;
import org.gs.game.gostop.action.TakeActionGroup;

/**
 * 바닥의 같은 패 중 어느것을 먹을 것인가에 대한 선택 결과를 TakeActionGroup에 전달해 줍니다.
 */
public class SelectCardOnTablePostAction implements IGamePostAction
{
    private TakeActionGroup nextAction;
    
    public SelectCardOnTablePostAction()
    {
        nextAction = null;
    }
    
    public void setNextAction(TakeActionGroup nextAction)
    {
        this.nextAction = nextAction;
    }
    
    public void onActionComplete(GameAction ga)
    {
        if (nextAction != null)
            nextAction.setSelectedFlip(Boolean.TRUE.equals(ga.getResult()) ? 0 : 1);
    }
}
