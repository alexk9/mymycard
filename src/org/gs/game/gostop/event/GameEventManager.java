package org.gs.game.gostop.event;

import java.util.ArrayList;
import java.util.List;

/**
 *생성되는 이벤트를 받기 위해서는 IGameEventListener 인터페이스를 구현한 후
 * GameEventManager의 addGameEventListener를 이용해 등록을 하여야 합니다.
 * 현재는 GamePanel만이 이를 이용하고 있습니다.
 *   public interface IGameEventListener {
 *       void onGameEvent(GameEvent e);
 *   }
 *   이벤트를 등록하여 이벤트 수신자에게 알려 주기 위해서는 GameEventManager의 fireGameEvent를 호출하는데,
 *   이벤트 정보와 동기여부를 넘겨줘야 합니다.
 *   public class GameEventManager extends Thread {
 *   public static void addGameEventListener(IGameEventListener listener);
 *   public static void fireGameEvent(GameEvent e, boolean synchronous);
 *   }
 *   이벤트 정보 (GameEvent)는 이벤트를 생성자, 이벤트 종류, 이벤트 결과 등을 포함하는데,
 *   이벤트 결과 (GameEventResult)는 이벤트에 따른 boolean, int, Object 등의 정보를 포함할 수 있습니다.
 *   public class GameEvent {
 *       private Object sourceObject;
 *       private GameEventType eventType;
 *       private GameEventResult result;
 *   }
 *
 *    public class GameEventResult {
 *        private boolean boolResult;
 *        private Object resultObject;
 *        private int intResult;
 *    }
 */
public class GameEventManager extends Thread
{
    private static GameEventManager _instance = null;
    
    private List<IGameEventListener> eventListeners;
    private List<GameEvent> eventQueue;

    public static void addGameEventListener(IGameEventListener listener)
    {
        getInstance().addListener(listener);
    }

    /**
     * Fires an event from a dedicated thread
     * 
     * @param e the event to fire
     * @param synchronous true to handle the event synchronously
     */
    public static void fireGameEvent(GameEvent e, boolean synchronous)
    {
        if (synchronous)
            getInstance().handleGameEvent(e);
        else
            getInstance().addItemEvent(e);
    }
    
    private static synchronized GameEventManager getInstance()
    {
        if (_instance == null)
            _instance = new GameEventManager();
        
        return _instance;
    }
    
    private GameEventManager()
    {
        eventQueue = new ArrayList<GameEvent>(8);
        eventListeners = new ArrayList<IGameEventListener>(4);
        
        setName("ItemEventMgr");
    }
    
    private synchronized void addListener(IGameEventListener listener)
    {
        if (eventListeners.contains(listener) == false)
            eventListeners.add(listener);
    }
    
    public void run()
    {
        do
        {
            handleGameEvent(getNextGameEvent());
        } while (true);
    }
    
    private synchronized GameEvent getNextGameEvent()
        throws RuntimeException
    {
        GameEvent event = null;
        
        do
        {
            if (eventQueue.size() > 0)
                event = eventQueue.remove(0);
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
        } while (event == null);
        
        return event;
    }
    
    private synchronized void addItemEvent(GameEvent e)
    {
        eventQueue.add(e);
        
        if (isAlive() == false)
            start();
        
        notify();
    }
    
    private void handleGameEvent(GameEvent e)
    {
        for (IGameEventListener gel: eventListeners)
            gel.onGameEvent(e);
    }
}
