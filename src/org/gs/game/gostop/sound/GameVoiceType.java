package org.gs.game.gostop.sound;

public class GameVoiceType
{
    private String voiceTypeId;
    private String voiceTypeFolder;
    
    public GameVoiceType(String voiceTypeId, String voiceTypeFolder)
    {
        this.voiceTypeId = voiceTypeId;
        this.voiceTypeFolder = voiceTypeFolder;
    }
    
    public String getVoiceTypeId()
    {
        return voiceTypeId;
    }
    
    public String getVoiceTypeFolder()
    {
        return voiceTypeFolder;
    }
    
    public String toString()
    {
        return voiceTypeId;
    }
}
