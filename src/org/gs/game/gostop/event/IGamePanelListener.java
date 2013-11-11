package org.gs.game.gostop.event;

public interface IGamePanelListener
{
    void gameStopped();
    
    void onMenuSelected(Object selectedObject, String menuCmd);
}
