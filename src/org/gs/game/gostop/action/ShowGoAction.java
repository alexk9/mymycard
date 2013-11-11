package org.gs.game.gostop.action;

import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.event.GameEvent;
import org.gs.game.gostop.event.GameEventType;
import org.gs.game.gostop.item.AlertItem;

/**
 * ShowAlertAction을 상속한 클래스로 "고" 메시지를 표시하고, 고를 위한 처리를 합니다.
 */
public class ShowGoAction extends ShowAlertAction
{
    private GamePlayer gamePlayer;
    private boolean goOn4Cards;
    private boolean addedBonus;
    
    public ShowGoAction(GamePlayer gamePlayer, int duration,
                        GameEventType completeEventType, boolean goOn4Cards)
    {
        super(gamePlayer, duration,
              goOn4Cards ? AlertItem.ALERT_GO_4CARDS : AlertItem.ALERT_GO,
              completeEventType);
        
        this.gamePlayer = gamePlayer;
        this.goOn4Cards = goOn4Cards;
        addedBonus = false;
    }
    
    public boolean execute(float progress)
    {
        if (addedBonus == false)
        {
            if (goOn4Cards == false)
                gamePlayer.addGoCount();
            
            addedBonus = true;
        }
        
        return super.execute(progress);
    }
    
    protected String getSoundId()
    {
        return goOn4Cards ? null : AlertItem.ALERT_GO + '.' + gamePlayer.getGoCount();
    }

    public void onActionComplete()
    {
        doPostActions();
        
        if (completeEventType != null)
            fireActionEvent(new GameEvent(target, completeEventType, true));
    }
}
