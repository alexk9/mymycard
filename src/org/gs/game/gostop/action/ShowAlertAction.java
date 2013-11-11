package org.gs.game.gostop.action;

import java.awt.Point;

import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.event.GameEventType;
import org.gs.game.gostop.item.AlertItem;
import org.gs.game.gostop.sound.GameSoundManager;

/**
 * 일정시간동안 메시지를 표시해 줍니다 (예, "선", "쪽" 등)
 */
public class ShowAlertAction extends GameAction
{
    private String msgId;
    private Point point;
    private boolean soundPlayed;
    
    public ShowAlertAction(GamePlayer gamePlayer, int duration, String msgId)
    {
        this(gamePlayer, duration, msgId, null, null);
    }
    
    public ShowAlertAction(GamePlayer gamePlayer, int duration, String msgId,
                           GameEventType completeEventType)
    {
        this(gamePlayer, duration, msgId, null, completeEventType);
    }
    
    public ShowAlertAction(GamePlayer gamePlayer, int duration, String msgId, Point point)
    {
        this(gamePlayer, duration, msgId, point, null);
    }
    
    public ShowAlertAction(GamePlayer gamePlayer, int duration, String msgId,
                           Point point, GameEventType completeEventType)
    {
        super(gamePlayer, duration);
        
        this.msgId = msgId;
        this.point = point == null ? gamePlayer.getAlertLocation() : point;
        setCompleteEventType(completeEventType);
        soundPlayed = false;
    }
    
    public boolean execute(float progress)
    {
        boolean completed = progress >= 1.0f;
        GamePlayer gamePlayer = getGamePlayer();
        AlertItem alertItem = gamePlayer.getAlertItem();

        if (msgId != null)
        {
            alertItem.setMessage(msgId, point);
            msgId = null;
        }
        else if (completed)
            alertItem.hide();
        
        if (soundPlayed == false)
        {
            GameSoundManager.playSound(getSoundId(), gamePlayer.getVoiceTypeId());
            soundPlayed = true;
        }
        
        return completed;
    }
    
    protected String getSoundId()
    {
        return getGamePlayer().getAlertItem().getMessageId();
    }

    private GamePlayer getGamePlayer()
    {
        return (GamePlayer)target;
    }
}
