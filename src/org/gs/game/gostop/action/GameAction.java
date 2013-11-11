package org.gs.game.gostop.action;

import java.util.ArrayList;
import java.util.List;

import org.gs.game.gostop.action.post.IGamePostAction;
import org.gs.game.gostop.action.pre.IPreExecuteAction;
import org.gs.game.gostop.event.GameEvent;
import org.gs.game.gostop.event.GameEventManager;
import org.gs.game.gostop.event.GameEventType;

/**
 * UI요소(화투)의 이동은 org.gs.game.gostop.action 패키지에서 처리하는데
 * GameAction 추상 클래스가 이동의 내용을 표현하며, GameAction들은 ActionManager에 등록되어 처리됩니다.
 * GameEventManager와 마찬가지로 ActionManager도 별도의 쓰레드에서 등록된 GameAction들을 처리합니다.
 *
 * GameAction은 execute(float progress) 메소드 내에서 필요한 작업을 실행하며,
 * ActionManager는 등록된 GameAction의 주어진 시간(duration) 동안 주기적(GamePanel.ACTION_TIME_UNIT)으로 이 실행 메소드를 호출해줍니다.
 * GameAction은 실행완료시 연결된 복수의 GameAction들을 새로이 등록할 수 있으며,
 * 완료 이벤트 타입이 정의되어 있는 경우 그 이벤트를 GameEventManager에 등록할 수 있습니다.
 * 또한 특별한 완료 후 처리가 필요한 경우에는 IGamePostAction을 등록해서 호출 받을 수 있습니다.
 *
 * GameAction에서 UI요소를 이동할 때, 그리는 부분까지 실행하는 것이 아니라,
 * 새로운 위치를 결정하고, 데이터의 논리적 정보를 변경하는 등의 처리를 합니다.
 * 그리고 필요한 요소에 대해서 repaint를 호출하면 AWT의 Event 처리 쓰레드에서
 * 각 요소의 paint/GamePanel.paintComponent/GameItem.paintItem 등을 호출해서 실질적으로 그려주게 됩니다.
 */
public abstract class GameAction
{
    protected Object target;
    protected long start;
    protected int duration;     // in ActionManager interval units
    protected List<GameAction> nextActions;
    protected int nextStart;    // in ActionManager interval units
    protected GameEventType completeEventType;
    protected GameEvent completeEvent;
    protected Object result;
    protected IPreExecuteAction preExecuteAction;
    protected List<IGamePostAction> gamePostActions;

    public GameAction(Object target, int duration)
    {
        this.target = target;
        this.duration = duration;
        
        nextActions = null;
        completeEventType = null;
        completeEvent = null;
        result = null;
        preExecuteAction = null;
        gamePostActions = null;
    }
    
    public abstract boolean execute(float progress);

    public void setStarted()
    {
        start = System.currentTimeMillis();
        
        onPreExecute();
    }
    
    public long getStartTime()
    {
        return start;
    }
    
    public int getDuration()
    {
        return duration;
    }
    
    public void setNextAction(GameAction nextAction)
    {
        setNextAction(nextAction, -1);
    }

    public void setNextAction(GameAction nextAction, int nextStart)
    {
        addNextAction(nextAction);
        
        this.nextStart = nextStart <= 0 ? duration : nextStart;
    }
    
    public void addNextAction(GameAction nextAction)
    {
        if (nextActions == null)
            nextActions = new ArrayList<GameAction>();
        
        nextActions.add(nextAction);
    }
    
    public void setNextActions(List<GameAction> nextActions, int nextStart)
    {
        this.nextActions = nextActions;
        this.nextStart = nextStart <= 0 ? duration : nextStart;
    }
    
    public List<GameAction> getNextActions()
    {
        List<GameAction> nextActions = this.nextActions;
        
        this.nextActions = null;
        
        return nextActions;
    }
    
    public boolean canExecuteNextAction(int elapsed)
    {
        return nextActions != null && elapsed >= nextStart;
    }
    
    protected void onPreExecute()
    {
        if (preExecuteAction != null)
            preExecuteAction.onPreExecute(this);
    }
    
    public void onActionComplete()
    {
        doPostActions();
        
        if (completeEventType != null)
            fireActionEvent(getCompleteEvent());
    }
    
    public void setCompleteEventType(GameEventType completeEventType)
    {
        this.completeEventType = completeEventType;
    }
    
    public GameEventType getCompleteEventType()
    {
        return completeEventType;
    }
    
    public void setCompleteEvent(GameEvent completeEvent)
    {
        this.completeEvent = completeEvent;
        this.completeEventType = completeEvent.getEventType();
    }
    
    public GameEvent getCompleteEvent()
    {
        if (completeEvent == null)
            completeEvent = new GameEvent(target, completeEventType);
        
        return completeEvent;
    }
    
    public void setResult(Object result)
    {
        this.result = result;
    }
    
    public Object getResult()
    {
        return result;
    }
    
    public void setPreExecuteAction(IPreExecuteAction preExecuteAction)
    {
        this.preExecuteAction = preExecuteAction;
    }
    
    public void addGamePostAction(IGamePostAction gamePostAction)
    {
        if (gamePostActions == null)
            gamePostActions = new ArrayList<IGamePostAction>();
        
        gamePostActions.add(gamePostAction);
    }
    
    protected void fireActionEvent(GameEvent e)
    {
        GameEventManager.fireGameEvent(e, false);
    }
    
    protected void doPostActions()
    {
        if (gamePostActions != null)
        {
            for (IGamePostAction gamePostAction: gamePostActions)
                gamePostAction.onActionComplete(this);
        }
    }
}
