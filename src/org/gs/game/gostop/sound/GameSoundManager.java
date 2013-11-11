package org.gs.game.gostop.sound;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameSoundManager extends Thread
{
    private static GameSoundManager _instance = null;
    private static boolean _bMute = false;
    
    private static final String SOUND_PREFIX = "sound.";
    private static final String ALERT_PREFIX = "alert.";
    private static final String RULE_PREFIX = "game.rule.";
    
    public static final String SOUND_PULSE = "sound.pulse";
    public static final String SOUND_PLAY = "sound.play";
    public static final String SOUND_PUT_ON_EMPTY = "sound.put.on.empty";
    public static final String SOUND_PUT_ON_CARD = "sound.put.on.card";
    public static final String SOUND_MOVE = "sound.move";
    public static final String SOUND_DECISION = "sound.decision";
    public static final String SOUND_MISSION = "sound.mission";
    public static final String SOUND_WIN_RESULT = "sound.win.result";
    public static final String SOUND_LOSE_RESULT = "sound.lose.result";
    
    private HashMap<String,GameSoundItem> soundItems;
    private HashMap<String,GameVoiceType> voiceTypes;
    private ArrayList<GameSoundItem> playQueue;
    private GameSoundPlayer curPlayer;
    
    public static void addSoundItem(GameSoundItem soundItem)
    {
        getInstance().soundItems.put(soundItem.getSoundId(), soundItem);
    }
    
    public static void addVoiceType(GameVoiceType voiceType)
    {
        getInstance().voiceTypes.put(voiceType.getVoiceTypeId(), voiceType);
    }

    public static void playSound(String alertId, String voiceTypeId)
    {
        playSound(alertId, voiceTypeId, false, 0);
    }
    
    public static void playSound(String alertId, String voiceTypeId, boolean mix, int delay)
    {
        if (_bMute == false && alertId != null && alertId.length() > 0)
            getInstance().playSoundItem(alertId, voiceTypeId, mix, delay);
    }
    
    public static void stopAllSound()
    {
        getInstance().stopAllSoundPlay();
    }
    
    public static List<GameVoiceType> getVoiceTypes()
    {
        List<GameVoiceType> voiceTypes = new ArrayList<GameVoiceType>(8);
        
        voiceTypes.addAll(getInstance().voiceTypes.values());
        
        return voiceTypes;
    }
    
    public static GameVoiceType getVoiceType(String voiceTypeId)
    {
        return getInstance().voiceTypes.get(voiceTypeId);
    }
    
    public static void setMute(boolean bMute)
    {
        _bMute = bMute;
        
        if (bMute)
            stopAllSound();
    }
    
    private static synchronized GameSoundManager getInstance()
    {
        if (_instance == null)
            _instance = new GameSoundManager();
        
        return _instance;
    }
    
    private GameSoundManager()
    {
        soundItems = new HashMap<String,GameSoundItem>(16);
        voiceTypes = new HashMap<String,GameVoiceType>(8);
        playQueue = new ArrayList<GameSoundItem>(2);
        curPlayer = null;

        setName("SoundMgr");
    }
    
    public void run()
    {
        do
        {
            playSound(getNextSoundItem());
        } while (true);
    }
    
    private void playSound(GameSoundItem soundItem)
    {
        InputStream is = getClass().getResourceAsStream(soundItem.getSoundPath());
        
        if (is != null)
        {
            try
            {
                synchronized (this)
                {
                    curPlayer = new GameSoundPlayer();
                }
                
                curPlayer.play(is);
                
                synchronized (this)
                {
                    curPlayer = null;
                }
            }
            finally
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private synchronized void stopAllSoundPlay()
    {
        if (curPlayer != null)
            curPlayer.stop();
        
        playQueue.clear();
    }
    
    private synchronized GameSoundItem getNextSoundItem()
        throws RuntimeException
    {
        GameSoundItem soundItem = null;
        
        do
        {
            if (playQueue.size() > 0)
                soundItem = playQueue.remove(0);
            else
            {
                try
                {
                    wait();
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        } while (soundItem == null);
        
        return soundItem;
    }

    private synchronized void playSoundItem(String alertId, String voiceTypeId,
                                            boolean mix, int delay)
    {
        GameSoundItem soundItem;
        
        if (alertId.startsWith(ALERT_PREFIX))
            soundItem = soundItems.get(SOUND_PREFIX + alertId.substring(ALERT_PREFIX.length()));
        else if (alertId.startsWith(SOUND_PREFIX))
            soundItem = soundItems.get(alertId);
        else if (alertId.startsWith(RULE_PREFIX))
            soundItem = soundItems.get(SOUND_PREFIX + alertId.substring(RULE_PREFIX.length()));
        else
            soundItem = null;
        
        if (soundItem != null)
        {
            if (soundItem.getSoundType() == GameSoundType.VOICE)
            {
                GameVoiceType voiceType = voiceTypes.get(voiceTypeId);
                
                if (voiceType == null)
                    voiceType = voiceTypes.values().iterator().next();
                
                String path = voiceType.getVoiceTypeFolder() + '/' + soundItem.getSoundPath();
                soundItem = new GameSoundItem(soundItem.getSoundId(), path,
                                              GameSoundType.SOUND);
            }
            
            if (mix || delay > 0)
                new SoundPlayThread(soundItem, delay);
            else
            {
                playQueue.add(soundItem);
                
                if (isAlive() == false)
                    start();
                
                notify();
            }
        }
    }
    
    private static class SoundPlayThread extends Thread
    {
        private static int _threadCnt = 0;
        
        private GameSoundItem soundItem;
        private int delay; 
        
        private SoundPlayThread(GameSoundItem soundItem, int delay)
        {
            this.soundItem = soundItem;
            this.delay = delay;
            setName("SoundMgr-" + Integer.toString(++_threadCnt));
            start();
        }
        
        public void run()
        {
            if (delay > 0)
            {
                try
                {
                    Thread.sleep(delay);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            
            InputStream is = getClass().getResourceAsStream(soundItem.getSoundPath());
            
            if (is != null)
            {
                try
                {
                    new GameSoundPlayer().play(is);
                }
                finally
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
