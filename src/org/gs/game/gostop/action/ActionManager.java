package org.gs.game.gostop.action;

import java.util.ArrayList;
import java.util.List;


/**
 * GameAction을 관리하고, 주어진 시간동안 주기적으로 실행시켜줍니다.
 */
public class ActionManager extends Thread
{
    private static final String ACTION_MGR_THREAD = "ActionMgr";
    
    private int interval;   // in milliseconds
    private List<GameAction> gameActions;
    boolean started;
    boolean stopRequested;
 
    public ActionManager(int interval)
    {
        this.interval = interval;
        gameActions = new ArrayList<GameAction>();
        started = false;
        stopRequested = false;
        
        setName(ACTION_MGR_THREAD);
    }

    public void run()
    {
        while (stopRequested == false)
        {
            try
            {
                synchronized (this)
                {
                    if (gameActions.size() > 0)
                    {
                        wait(interval);
                        executeActions();
                    }
                    else
                        wait();
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public synchronized void stopManager()
    {
        stopRequested = true;
        this.notify();
    }
    
    public synchronized void addItem(GameAction action)
    {
        boolean needToNotify = gameActions.size() == 0;
        
        action.setStarted();
        gameActions.add(action);
        
        if (needToNotify)
            notify();
        
        if (started == false)
        {
            started = true;
            start();
        }
    }
    
    public synchronized void addItems(List<GameAction> actions)
    {
        boolean needToNotify = gameActions.size() == 0;
        
        for (GameAction action: actions)
        {
            action.setStarted();
            gameActions.add(action);
        }
        
        if (needToNotify)
            notify();
        
        if (started == false)
        {
            started = true;
            start();
        }
    }
    
    public synchronized void abortAllActions()
    {
        gameActions.clear();
    }
    
    public synchronized boolean isComplete()
    {
        if (ACTION_MGR_THREAD.equals(Thread.currentThread().getName()))
            throw new RuntimeException("waitForComplete cannot be called from " + ACTION_MGR_THREAD);
        
        return gameActions.size() == 0;
    }
    
    private synchronized void executeActions()
    {
        List<GameAction> completedItems = new ArrayList<GameAction>();
        List<GameAction> nextItems = new ArrayList<GameAction>();
        
        for (GameAction action: gameActions)
        {
            int elapsed = (int)((System.currentTimeMillis() - action.getStartTime())/interval);
            float progress = action.getDuration() == 0 ? 1.0f
                                   : elapsed / (float)action.getDuration();
                                    
            if (action.execute(progress))
            {
                action.onActionComplete();
                completedItems.add(action);
            }
            
            if (action.canExecuteNextAction(elapsed))
                nextItems.addAll(action.getNextActions());
        }
        
        gameActions.removeAll(completedItems);
        
        if (nextItems.size() > 0)
        {
            for (GameAction item: nextItems)
                addItem(item);
        }
    }
}
