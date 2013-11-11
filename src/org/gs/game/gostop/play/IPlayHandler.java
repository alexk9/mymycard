package org.gs.game.gostop.play;

import org.gs.game.gostop.TableCardPoint;
import org.gs.game.gostop.action.GameAction;

/**
 * 게임 플레이어의 결정이 필요할 경우에 호출되는 메소드들이 IPlayHandler 인터페이스에 정의되어 있는데,
 * 플레이어가 컴인 경우 SimpleAutoPlayHandler가 그 인터페이스를 구현하였고,
 * 로그인한 사용자의 경우는 LocalPlayHandler에 의해서 사용자의 입력을 받을 수 있도록 설계되어 있습니다.
 */
public interface IPlayHandler
{
    /**
     *  가진 패 중 하나를 선택한 후, GameEventType.ITEM_CLICKED 이벤트를 생성합니다.
     *  패를 흔들 경우나 바닥에 같은 패가 두가지 있을 경우는 GameEventType.SWING_DECIDED 또는 GameEventType.SELECTED_CARD_ON_TABLE를 각각 생성합니다.
     */
    void pickCard();

    /**
     * 자신의 순서가 끝나고 다음 플레이어로 넘어가기 전에 호출됩니다.
     */
    void onPostActive();

    /**
     * 뒷장에 대해 바닥에 같은 패가 두가지 있을 경우, 바닥 패를 선택하기 위한 GameAction을 생성합니다.
     */
    GameAction getSelectTableCardAction(TableCardPoint flipTcp);

    /**
     * 고할 것인가를 결정해서 GameEventType.GO_DECIDED 이벤트를 생성합니다.
     */
    void decideGo();

    /**
     * 구십끗을 쌍피로 쓸 것인가를 결정해서 GameEventType.NINE_DECIDED 이벤트를 생성합니다.
     */
    void decideNine();

    /**
     * 받은 패가 총통일 경우 고할 것인지를 결정하여 GameEventType.FOUR_CARDS_DECIDED 이벤트를 생성합니다.
     */
    void decideGoOnFourCards();
}
