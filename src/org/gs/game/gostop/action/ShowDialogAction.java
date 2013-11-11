package org.gs.game.gostop.action;

import org.gs.game.gostop.GamePanel;
import org.gs.game.gostop.dlg.GameDialog;

/**
 *  대화상자를 표시해줍니다.
 */
public class ShowDialogAction extends GameAction
{
    boolean completed;
    boolean disposeOnComplete;
    boolean nextOnResult;
    
    public ShowDialogAction(GameDialog gameDlg, int duration, boolean disposeOnComplete)
    {
        this(gameDlg, duration, disposeOnComplete, false);
    }
    
    public ShowDialogAction(GameDialog gameDlg, int duration, boolean disposeOnComplete,
                            boolean nextOnResult)
    {
        super(gameDlg, duration);
        
        this.completed = false;
        this.disposeOnComplete = disposeOnComplete;
        this.nextOnResult = nextOnResult;
    }

    public boolean execute(float progress)
    {
        GameDialog dlg = getGameDialog();
        completed = progress >= 1.0f || dlg.isResultSet();
        
        if (dlg.isResultSet() == false && dlg.isVisible() == false)
            dlg.setVisible(true);
        
        if (completed)
        {
            if (duration > 0 && dlg.isResultSet() == false)
                dlg.clickButtonOnTimeout();
            
            if (disposeOnComplete)
                dlg.getParent().remove(dlg);
            
            setResult(dlg.getResult());
        }
        else if (duration > 0)
        {
            int timeout = (int)((1 - progress) * (duration / GamePanel.TIME_UNITS_PER_SECOND));
            
            if (timeout <= 5)
                dlg.setTimeout(timeout);
        }
        
        return completed;
    }
    
    public boolean canExecuteNextAction(int elapsed)
    {
        return nextActions != null && (completed || super.canExecuteNextAction(elapsed))
               && (nextOnResult == false || getGameDialog().getResult()); 
    }
    
    private GameDialog getGameDialog()
    {
        return (GameDialog)target;
    }
}
