package org.gs.game.gostop.event;

public class GameEventResult
{
    private boolean boolResult;
    private Object resultObject;
    private int intResult;
    
    public GameEventResult()
    {
        this(false, null);
    }
    
    public GameEventResult(boolean boolResult)
    {
        this(boolResult, null);
    }
    
    public GameEventResult(Object resultObject)
    {
        this(false, resultObject);
    }
    
    public GameEventResult(boolean boolResult, Object resultObject)
    {
        this(boolResult, resultObject, -1);
    }
    
    public GameEventResult(Object resultObject, int intResult)
    {
        this(false, resultObject, intResult);
    }
    
    public GameEventResult(boolean boolResult, Object resultObject, int intResult)
    {
        setBoolResult(boolResult);
        setResultObject(resultObject);
        setIntResult(intResult);
    }
    
    public void setBoolResult(boolean boolResult)
    {
        this.boolResult = boolResult;
    }
    
    public boolean getBoolResult()
    {
        return boolResult;
    }
    
    public void setResultObject(Object resultObject)
    {
        this.resultObject = resultObject;
    }
    
    public <T> T getResultObject(Class<T> tclass)
    {
        return resultObject != null && tclass.isInstance(resultObject)
               ? tclass.cast(resultObject) : null;
    }
    
    public void setIntResult(int selected)
    {
        this.intResult = selected;
    }
    
    public int getIntResult()
    {
        return intResult;
    }
}
