package org.gs.game.gostop.action.pre;

import org.gs.game.gostop.action.GameAction;
import org.gs.game.gostop.sound.GameSoundManager;

public class PlaySoundPreAction implements IPreExecuteAction
{
    private String alertId;
    private String voiceTypeId;
    private boolean mix;
    private int delay;
    
    public PlaySoundPreAction(String alertId)
    {
        this(alertId, null, true);
    }
    
    public PlaySoundPreAction(String alertId, String voiceTypeId, boolean mix)
    {
        this.alertId = alertId;
        this.voiceTypeId = voiceTypeId;
        this.mix = mix;
        if (mix && (alertId == GameSoundManager.SOUND_PUT_ON_CARD
                    || alertId == GameSoundManager.SOUND_PUT_ON_EMPTY))
            this.delay = 150;
        else
            delay = 0;
    }
    
    public void onPreExecute(GameAction gameAction)
    {
        GameSoundManager.playSound(alertId, voiceTypeId, mix, delay);
    }
}
