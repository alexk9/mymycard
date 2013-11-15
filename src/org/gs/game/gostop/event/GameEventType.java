package org.gs.game.gostop.event;

public enum GameEventType
{
    ITEM_CLICKED,
    ZORDER_CHANGED,

    /**
     * 선 뽑기 종료
     */
    PICK_LEAD_COMPLETED,
    ALERT_COMPLETED,
    STACK_COMPLETED,
    DEAL_COMPLETED,
    FOUR_CARDS_DECIDED,
    FLIP_STACK_COMPLETED,
    /** 흔듭시다. */
    SWING_DECIDED,
    /** 테이블 상의 카드를 선택했어요. */
    SELECTED_CARD_ON_TABLE,
    PLAY_COMPLETED,
    NINE_DECIDED,
    /**
     * GO할지 말지를 결정했어요.
     */
    GO_DECIDED,
    MORE_GAME_DECIDED,
    
    GAME_USER_UPDATED,
    
    MENU_CLICKED
}
