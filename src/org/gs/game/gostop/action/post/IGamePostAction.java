package org.gs.game.gostop.action.post;

import org.gs.game.gostop.action.GameAction;

/**
 * GameAction의 완료 후 처리를 정의하는 인터페이스입니다.
 */
public interface IGamePostAction
{
    void onActionComplete(GameAction ga);
}
