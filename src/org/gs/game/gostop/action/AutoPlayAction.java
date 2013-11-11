package org.gs.game.gostop.action;

import org.gs.game.gostop.TableCardPoint;
import org.gs.game.gostop.play.AutoPlayHandler;

/**
 * 컴의 차례에 바닥에 같은 패가 두장 있을 때 어느 것을 먹을 것인가를 처리합니다.
 */
public class AutoPlayAction extends GameAction
{
    private TableCardPoint flipTcp;
    
    public AutoPlayAction(AutoPlayHandler playHandler, TableCardPoint flipTcp)
    {
        super(playHandler, 0);
        
        this.flipTcp = flipTcp;
    }

    public boolean execute(float progress)
    {
        selectTableCard();
        
        return true;
    }
    
    private void selectTableCard()
    {
        AutoPlayHandler playHandler = (AutoPlayHandler)target;
        
        setResult(playHandler.selectTableCard(flipTcp) == 0);
    }
}
