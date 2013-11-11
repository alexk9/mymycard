package org.gs.game.gostop.play;

import org.gs.game.gostop.GamePlayer;
import org.gs.game.gostop.GameTable;

public class PlayHandlerFactory
{
    public static IPlayHandler getPlayHandler(GamePlayer gamePlayer, GameTable gameTable)
    {
        IPlayHandler playHandler;
        
        if (gamePlayer.isOpposite())
        {
            if (gamePlayer.isComputerPlayer())
                playHandler = new AutoPlayHandler(gamePlayer, gameTable);
            else    // TODO: support the remote player
                playHandler = null;
        }
        else
            playHandler = new LocalPlayHandler(gamePlayer, gameTable);
        
        return playHandler;
    }
}
