package org.gs.game.gostop;

import org.gs.game.gostop.item.CardItem;

/**
 * 게임 테이블의 뒷패를 얻어오는 인터페이스로 GameManager가 구현합니다.
 */
public interface ICardDeck
{
    CardItem getTopDeckCard(boolean remove);
    
    void setCanClickTopCard(boolean canClick);
}
