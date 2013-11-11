package org.gs.game.gostop.sound;

public class GameSoundItem
{
    private String soundId;
    private String soundPath;
    private GameSoundType soundType;
    
    public GameSoundItem(String soundId, String soundPath, GameSoundType soundType)
    {
        this.soundId = soundId;
        this.soundPath = soundPath;
        this.soundType = soundType;
    }
    
    public String getSoundId()
    {
        return soundId;
    }
    
    public String getSoundPath()
    {
        return soundPath;
    }
    
    public GameSoundType getSoundType()
    {
        return soundType;
    }
}
