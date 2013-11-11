package org.gs.game.gostop.event;

import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.item.CardItem;

/**
 * GS고스톱에서는 게임요소 (GameItem)들과 GamePanel과의 통신이 필요한 경우 GameEvent를 이용합니다.
 * 이 이벤트는 GameEventManager에 의해 관리되는데,
 * 이벤트가 등록되면 이벤트관리자는 별도의 쓰레드에서 비동기적으로 이벤트 수신자에게 알려주지만,
 * 필요시에는 동기적으로 처리될 수도 있습니다.
 */
public class GameEvent
{
    private Object sourceObject;
    private GameEventType eventType;
    private GameEventResult result;
    
    public GameEvent(Object sourceObject, GameEventType eventType)
    {
        this(sourceObject, eventType, (GameEventResult)null);
    }
    
    public GameEvent(Object sourceObject, GameEventType eventType, boolean result)
    {
        this(sourceObject, eventType, new GameEventResult(result));
    }
    
    public GameEvent(Object sourceObject, GameEventType eventType, Object resultObject)
    {
        this(sourceObject, eventType, new GameEventResult(resultObject));
    }
    
    public GameEvent(Object sourceObject, GameEventType eventType, GameEventResult result)
    {
        this.sourceObject = sourceObject;
        this.eventType = eventType;
        this.result = result;
    }
    
    public Object getSourceObject()
    {
        return sourceObject;
    }
    
    public GameEventType getEventType()
    {
        return eventType;
    }
    
    public boolean getBoolResult()
    {
        return result == null ? false : result.getBoolResult(); 
    }
    
    public String getStringResult()
    {
        return getEventResult(String.class);
    }
    
    public CardItem getCardItemResult()
    {
        return getEventResult(CardItem.class);
    }
    
    public GamePlayer getGamePlayerResult()
    {
        return getEventResult(GamePlayer.class);
    }
    
    public <T> T getEventResult(Class<T> tClass)
    {
        return result == null ? null : result.getResultObject(tClass);
    }
    
    public int getIntResult()
    {
        return result == null ? -1 : result.getIntResult();
    }
}
