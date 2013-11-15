package org.gs.game.gostop.play;

import java.util.List;

import org.gs.game.gostop.*;
import org.gs.game.gostop.action.AutoPlayAction;
import org.gs.game.gostop.action.GameAction;
import org.gs.game.gostop.action.ShowGoAction;
import org.gs.game.gostop.config.BonusCards;
import org.gs.game.gostop.event.GameEvent;
import org.gs.game.gostop.event.GameEventManager;
import org.gs.game.gostop.event.GameEventResult;
import org.gs.game.gostop.event.GameEventType;
import org.gs.game.gostop.item.CardItem;
import org.gs.game.gostop.play.GameRule.GoStopRule;

/**
 * PC 플레이 핸들러.
 */
public class AutoPlayHandler implements IPlayHandler
{
    private GamePlayer gamePlayer;
    private GameTable gameTable;
    
    protected AutoPlayHandler(GamePlayer gamePlayer, GameTable gameTable)
    {
        this.gamePlayer = gamePlayer;
        this.gameTable = gameTable;
    }

    /**
     * 카드를 골라봅시다.
     */
    public void pickCard()
    {
        TableCardPoint tcp;
        GameEvent event;
        CardItem cardItem = pickTakeableCard();

        //내가 먹으려고 하는 카드 위치를 얻어봅시다.
        tcp = gameTable.getTableCardPoint(cardItem.getMajorCode(), false);

        //카드 위치가 없고, 보너스카드가 아니고... 내가 든 카드 중 같은 카드가 3장이라면.. 흔들어야지 뭐
        if (tcp == null && cardItem.isBonusCard() == false
            && gamePlayer.getHoldCardCount(cardItem.getMajorCode()) >= 3)
        {   // swing
            event = new GameEvent(gamePlayer, GameEventType.SWING_DECIDED,
                                  new GameEventResult(true, cardItem));
        }
        //먹을 카드가 있고, 결정할게 필요하다면...
        else if (tcp != null && tcp.needToQueryForTaking())
        {
            //두 카드중에 하나를 선택하자.
            int index = selectTableCard(tcp);
            
            event = new GameEvent(gamePlayer, GameEventType.SELECTED_CARD_ON_TABLE,
                                  new GameEventResult(cardItem, index));
        }
        else
        // 카드 아이템이 클릭 되었어요~
            event = new GameEvent(cardItem, GameEventType.ITEM_CLICKED);
        
        GameEventManager.fireGameEvent(event, false);
    }

    public void onPostActive()
    {
        gamePlayer.arrangeHoldCards();
    }
    
    public GameAction getSelectTableCardAction(TableCardPoint flipTcp)
    {
        return new AutoPlayAction(this, flipTcp);
    }

    /**
     * 카드 우선순위를 구함. 자동임
     * @param flipTcp
     * @return
     */
    public int selectTableCard(TableCardPoint flipTcp)
    {
        CardItem ci0 = flipTcp.getCardItems().get(0);
        CardItem ci1 = flipTcp.getCardItems().get(1);
        
        return getCardPriority(ci0, false) > getCardPriority(ci1, false) ? 0 : 1;
    }
    
    public void decideGo()
    {
        //고할지말지 확인하기 위해, 싼 카드랑 다른 사람 카드를 확인한다.
        boolean canGo = checkSwampedCards() && checkOtherPoints();

        //let's go!
        if (canGo)
        {
            ShowGoAction sga = new ShowGoAction(gamePlayer, 20,
                                                GameEventType.GO_DECIDED, false);
            
            gamePlayer.getGamePanel().getActionManager().addItem(sga);
        }
        else
        {
            GameEvent event = new GameEvent(gamePlayer, GameEventType.GO_DECIDED,
                                            new GameEventResult(false));
            GameEventManager.fireGameEvent(event, false);
        }
    }
    
    public void decideNine()
    {
        GameEvent event = new GameEvent(gamePlayer, GameEventType.NINE_DECIDED,
                                        new GameEventResult(true));    // use as leaf for now
        GameEventManager.fireGameEvent(event, false);
    }

    /**
     * 받은 패가 총통일 경우 고할 것인지를 결정하여 GameEventType.FOUR_CARDS_DECIDED 이벤트를 생성합니다.
     */
    public void decideGoOnFourCards()
    {
        ShowGoAction sga = new ShowGoAction(gamePlayer, 20,
                                            GameEventType.FOUR_CARDS_DECIDED, true);
        
        gamePlayer.getGamePanel().getActionManager().addItem(sga);
    }

    /**
     * 쓸만한 카드를 한장 구해보자.
     * @return
     */
    private CardItem pickTakeableCard()
    {
        CardItem cardItem = null;
        List<CardItem> holdCards = gamePlayer.getHoldCards();
        int topPriority = Integer.MIN_VALUE;

        /*
        들고 있는 카드가 있을때...
         */
        if (holdCards.size() > 0)
        {
            //플레이어의 상태를 보자....
            PlayerStatus playerStatus = gamePlayer.getPlayerStatus();
            CardItem bonusCard = null;
            
            for (CardItem ci: holdCards)
            {
                //카드가 보너스 카드이냐?
                if (ci.isBonusCard())
                {
                    if (bonusCard == null || ci.getCardClass() != CardClass.LEAF)
                        bonusCard = ci;
                }
                else
                {
                    //카드의 우선순위를 구하자...
                    int priority = getCardPriority(ci, true);

                    //ci카드의 우선순위가 높으면 최상의 후보 카드를 바꿔준다.
                    if (topPriority < priority)
                    {
                        topPriority = priority;
                        cardItem = ci;
                    }
                }
            }

            //보너스카드를 가지고 있고....
            if (bonusCard != null
                    //보너스카드가 피가 아니거나, 플레이어가 피를 하나라도 가지고 있다면...
                && (bonusCard.getCardClass() != CardClass.LEAF
                    || playerStatus.getTakenCards(CardClass.LEAF).size() > 0))
                //걍 보너스카드를 내자?
                cardItem = bonusCard;
            else if (topPriority < 0)
            {
                if (bonusCard != null)
                    cardItem = bonusCard;
                //까기만 할 수 있는 카드가 하나 있다면...
                else if (gamePlayer.getFlipCount() > 0)
                    //덱에 있는 카드를 한장 볼까?
                    cardItem = gameTable.getTopDeckCard(false);
            }
        }
        
        if (cardItem == null)
            cardItem = gameTable.getTopDeckCard(false);
        
        return cardItem;
    }

    /**
     * 카드의 우선순위 값을 구한다.
     * @param cardItem 카드 아이템 인풋
     * @param checkTable 테이블에 존재여부를 체크할건지여부
     * @return
     */
    private int getCardPriority(CardItem cardItem, boolean checkTable)
    {
        int priority = 0;
        int goPriority = 0;
        TableCardPoint tcp = null;

        //테이블을 체크할꺼면...
        //현재 테이블에 해당 카드 짝이 있니????
        if (checkTable)
            tcp = gameTable.getTableCardPoint(cardItem.getMajorCode(), false);

        //go를 한 플레이어를 구한다???
        List<GamePlayer> goPlayers = gameTable.getGoPlayers();
        //플레이어가 3이고, goPlayer가 null이 아니고,
        //go한 플레이어가 1명일때, 그리고 그 플레이어가 현 게임 플레이어가 아닌 경우...
        if (gameTable.getGameType().getPlayers() == 3 && goPlayers != null
            && goPlayers.size() == 1 && goPlayers.get(0) != gamePlayer){
            //go prioriy???
            goPriority = getPriorityOnOtherGo(cardItem, tcp, checkTable, goPlayers.get(0));
        }

        //테이블에 짝이 있는게 아니고, 카드가 세장이 같은 족뽀이거나..
        //또는 내가 같은 카드를 세장 가졌다면...
        if (tcp != null
            && (tcp.getCardCount(true) == 3
                || gamePlayer.getHoldCardCount(cardItem.getMajorCode()) == 3)
            && gameTable.canGetOtherLeaves(gamePlayer))
            priority = 2000;
        //미션이 수행 가능할 경우... 내가 든 카드가 미션카드이고... 바닥에 미션 카드가 깔린경우.
        else if (gameTable.isMissionAvailable()
                 && (cardItem.isMissionCard() || (tcp != null && tcp.hasMissionCard())))
            priority = 1000;
        //바닥에 패가 깔렷고. 쌍피가 있는 겨우..
        else if (tcp != null && tcp.hasMultiLeaves())
            priority = 900;
        //현재 카드가 피이고, 피의 포인트가 쌍피 이상이거나
        // 현 카드가 9짜 쌍피인경우...
        else if ((cardItem.getCardClass() == CardClass.LEAF
                  && GameRule.getLeafPoints(cardItem.getCardCode()) > 1)
                 || cardItem.getCardClass() == CardClass.TEN_LEAF)
            priority = 650;
        //바닥에 깔린게 잇는디, 그것이 광인 경우
        else if (tcp != null && tcp.hasKingCard())
            priority = 600;
        //내꺼 카드가 광인경우
        else if (cardItem.getCardClass() == CardClass.KING)
            priority = 550;
        else
        {
            GoStopRule rule;
            //내가 든 카드가 피이면 150, 아니면 100
            priority = cardItem.getCardClass() == CardClass.LEAF ? 150 : 100;

            //바닥에 깔렸다면...
            if (tcp != null)
            {
                for (CardItem ci: tcp.getCardItems())
                {
                    //바닥에 깔린 패의 족보가 뭐가 있는지...
                    rule = GameRule.getGoStopRule(ci.getCardCode());
                    
                    if (rule != null && gameTable.isRuleAvailable(rule))
                    {
                        //족보가 걸렷고, 살아있다면...
                        //점수에 따라서 우선순위 올라감.
                        int curPriority = rule.getRulePoint() * 100 + 40;

                        //내가 가지고 있는것으 개수가 있으면 가중치...
                        if (getTakenCount(rule) > 1)
                            curPriority += rule.getRulePoint() * 100;

                        //내꺼보다 우선순위가 크면, 바꿔치기함.
                        if (priority < curPriority)
                            priority = curPriority;
                    }
                }
            }
            //카드 한쌍이 이미 먹혔고, 내가 들고 있는 카드가 2장 인 경우
            else if (gameTable.isCardTaken(cardItem.getMajorCode(), true)
                     && gamePlayer.getHoldCardCount(cardItem.getMajorCode()) > 1)
                priority = 10;

            boolean missionAvail = gameTable.isMissionAvailable();
            int[] untakenCards;

            //테이블에서 체크할거면 테이블에서 찾음
            if (checkTable)
                untakenCards = gameTable.getUntakenCardCodes(cardItem.getCardCode());
            else
            {
                untakenCards = new int[1];
                untakenCards[0] = cardItem.getCardCode();
            }
            //미션 카드중 사용 가능한 놈들에 대해서 돌아보자.
            for (int untaken: untakenCards)
            {
                //untaken한 카드에 대한 카드 클래스... 광인지 등 여부를 추출
                CardClass cardClass = GameRule.getClardClass(CardItem.getCardIndex(untaken));
                //광이면 ... 광이면...
                if (cardClass == CardClass.KING)
                {
                    //현재 카드의 우선순위가 300보다 작고, 플레이어가 현재 카드를 들고 있다면.. 340으로 우선순위 조정.
                    if (priority < 300)
                        priority = gamePlayer.isHoldingCard(untaken) ? 340 : 300;
                }
                //내가 든 카드가 피이고, 짝 카드가 쌍피이거나....
                //내 카드가 9짜 쌍피인경우...
                else if ((cardClass == CardClass.LEAF && GameRule.getLeafPoints(untaken) > 1)
                         || cardClass == CardClass.TEN_LEAF)
                {
                    //우선순위 상향
                    if (priority < 400)
                        priority = gamePlayer.isHoldingCard(untaken) ? 440 : 400;
                }
                else
                {
                    //게임 룰에서...이카드로 쓸수 있는 족보가 있는지 찾아보고 리턴한다.
                    rule = GameRule.getGoStopRule(untaken);

                    //적용가능한 족보가 있다면 우선순위를 재조정한다.
                    if (rule != null && gameTable.isRuleAvailable(rule))
                    {
                        //일단 점수 * 100 으로 우선순위를 정하고...
                        int curPriority = rule.getRulePoint() * 100;
                        //해당 룰의 카드를 몇장을 가졌ㄴ느지를 보고...
                        int takenCount = getTakenCount(rule);
                        
                        if (takenCount > 1)
                            curPriority += rule.getRulePoint() * 100;
                        
                        if (untaken == cardItem.getCardCode())
                            curPriority += 40;
                        
                        if (priority < curPriority)
                            priority = curPriority;
                    }
                }

                //우선순위가 1000보다 작은 상황에서 미션 수행이 가능한게 있으면 미션쪽으로 눈을 돌려볼까?/
                if (missionAvail && gameTable.isMissionCard(untaken) && priority < 1000)
                    priority = untaken == cardItem.getCardCode() ? 1020 : 1000;
            }
        }

        //goPriority와 그냥 priority간에 비교
        if (goPriority > priority)
            priority = goPriority;
        
        if (checkTable)
        {
            if (goPriority != 0)
                priority = goPriority;
            else if (tcp == null && priority > 0)
                priority = -priority;
        }

        return priority;
    }

    /**
     * 다른 사람이 Go를 한 상황에서 우선순위를 얻는다.
     * @param cardItem
     * @param tcp
     * @param checkTable
     * @param goPlayer
     * @return
     */
    private int getPriorityOnOtherGo(CardItem cardItem, TableCardPoint tcp,
                                     boolean checkTable, GamePlayer goPlayer)
    {
        int goPriority = 0;
        //나하고, go한 플레이어? 를 제외하고, 다른 플레이어의 상태를 구한다.
        PlayerStatus otherStatus = gameTable.getOtherPlayerStatus(gamePlayer, goPlayer).get(0);
        //go 한 플레이어의 상태를 구한다.
        PlayerStatus goStatus = gameTable.getPlayerStatus(goPlayer);
        //굳이 위와같이 따로 구하는 이유는 알 수 없음....

        //먹을수 있는 광이 있나용???
        if (gameTable.isKingAvailable(cardItem.getMajorCode()) && tcp == null)
        {
            //go한 player의 상태중...
            //go한 player가 왕을 2장 이상 들고 있으면 우선순위를 확 낯춘다.
            if (goStatus.getTakenCards(CardClass.KING).size() > 1)
                goPriority = -2200;
            //나머지 플레이어가 왕을 2장이상 들고 있으면 2200으로...
            else if (otherStatus.getTakenCards(CardClass.KING).size() > 1)
                goPriority = 2200;
            //나머지 플레이어가 왕을 1장이상 들고 있으면 2100으로...
            else if (otherStatus.getTakenCards(CardClass.KING).size() > 0)
                goPriority = 2100;
        }
        
        int cardStatus = checkCardStatus(cardItem, otherStatus);
        int majorCode = cardItem.getMajorCode();
        //메이저 코드만으로 taken 되었는지 체크함. 이는 죽은자 인지 체크하는 것
        boolean takenHalf = gameTable.isCardTaken(majorCode, true);
        int holdCount = gamePlayer.getHoldCardCount(majorCode);

        //족보카드를 내가 들고 있고...
        if (cardStatus == 2)    // it's the rule card for other
        {
            //족보와 같은 패가 다른 사람이 반을 가져가고, 내가 한장이상 들고 있으면 2400(아마도 우선순위가 낮은것 같다.)
            goPriority = tcp == null ? (takenHalf && holdCount > 1 ? 0 : 2400) :
                takenHalf ? 0 : -2400;
        }
        else if (cardStatus == 1)   // it's a possible card for other's rule
        {
            goPriority = tcp == null ? (takenHalf && holdCount > 1 ? 0 : 2300) :
                takenHalf ? 0 : -2300;
        }
        else
        {
            //내가 족보를 들고 있지않고, 짝 패를 들고 있지도 않을 경우에는...
            //go한 사람의 카드를 체크해보자.
            cardStatus = checkCardStatus(cardItem, goStatus);

            //내 카드가 go한 사람이 들고 있다면???
            if (cardStatus == 2)
            {
                goPriority = tcp == null ? (takenHalf && holdCount > 1 ? 0 : -2400) :
                    takenHalf ? 300 : 500;
            }
            else if (cardStatus == 1)
            {
                goPriority = tcp == null ? (takenHalf && holdCount > 1 ? 0 : -2300) :
                    takenHalf ? 300 : 500;
            }
            else if (tcp != null && tcp.getCardCount(true) > 2)
                goPriority = goStatus.getCardPoints(CardClass.LEAF) > 0 ? -2300 : -2150;
        }
        
        if (goPriority == 0 && tcp == null
            && otherStatus.getCardPoints(CardClass.LEAF) >= 8
            && goStatus.getCardPoints(CardClass.LEAF) < 8
            && gameTable.isDoubleLeafAvailable(cardItem.getMajorCode()))
            goPriority = 2100;
        
        if (checkTable && cardStatus == 0 && goPriority > 0
            && (tcp == null || tcp.getCardCount(true) < 3))
            goPriority = -goPriority;
        
        return goPriority;
    }
    
    /**
     * Checks the card status from the player status
     * 고돌,청단,홍단, 초단에 속하는 카드인지 체크함.
     * 
     * @return 0: not found, 속하지 않음.
     *         1: found, 짝이기는 하나 해당 카드는 아님
     *         2: rule card, 해당 카드임
     */
    private int checkCardStatus(CardItem cardItem, PlayerStatus pStatus)
    {
        int status = 0;
        //끗과, 단으에 대해서 체크한다.
        CardClass[] cc = { CardClass.TEN, CardClass.FIVE };
        //현 카드의 majorCode를 얻는다.
        int majorCode = cardItem.getMajorCode();
        
        for (int k = 0; k < cc.length && status == 0; k++)
        {
            //특정 클래스의 카드를 가지고 있는거 다 보자....
            //끗, 단 등
            List<CardItem> cards = pStatus.getTakenCards(cc[k]);
            
            for (int l = 0; l < cards.size() && status == 0; l++)
            {
                //고돌,홍단,청단,초단 중의 룰을 리턴한다.
                GoStopRule rule = GameRule.getGoStopRule(cards.get(l).getCardCode());
                
                if (rule != null && gameTable.isRuleAvailable(rule))
                {
                    //족보가 살아있으면 룰 카드를 가져온다.
                    int[] ruleCards = rule.getRuleCards();
                    
                    for (int i = 0; i < ruleCards.length && status == 0; i++)
                    {
                        //내가 가진 카드가 족보 룰에 속하면 status=2
                        //아니면 status =1
                        if (majorCode == CardItem.getMajorCode(ruleCards[i]))
                            status = cardItem.getCardCode() == ruleCards[i] ? 2 : 1;
                    }
                }
            }
        }
        
        return status;
    }

    /**
     * 싼 카드를 체크하나?
     * @return 싼 카드의 패를 들고 있으면, true, 아니면 false
     */
    private boolean checkSwampedCards()
    {
        boolean canGo = true;
        //테이블 상의 싼 카드를 얻는다.
        //sawmpedCards에 싼 카드의 major코드를 받아온다.
        Integer[] swampedCards = gameTable.getSwampedCardsOnTable();


        if (swampedCards != null)
        {
            for (int i = 0; i < swampedCards.length && canGo; i++)
                canGo = gamePlayer.getHoldCardCount(swampedCards[i]) > 0;
        }

        //내가 싼 카드를 들고 있니?
        return canGo;
    }

    /**
     * 딴 놈들의 점수를 확인한다.
     * @return
     */
    private boolean checkOtherPoints()
    {
        boolean canGo = true;
        //나 말고 다른놈의 상태를 가져온다.
        List<PlayerStatus> pStatus = gameTable.getOtherPlayerStatus(gamePlayer);
        //승점을 가져온다.
        int winPoints = gameTable.getGameType().getWinPoints();

        for (int i = 0; i < pStatus.size() && canGo; i++)
        {
            PlayerStatus ps = pStatus.get(i);
            //플레이어 상태에 있는 포인트를 그냥 물어온다.
            int points = ps.getPlayerPoints();
            //피 점수를 물어온다.
            int leafCount = GameRule.getLeafCount(ps.getTakenCards(CardClass.LEAF));

            //9짜가 아직 결정이 안된경우... 피 점수를 추가해준다.
            if (ps.isTakenNineUndecided())
                leafCount += 2;

            //point가 0 이거나 포인트가 나는 점수 -4 보다 작으면 고 하자
            canGo = points == 0 || points < winPoints - 4;

            // can go!
            if (canGo)
            {
                //왕 몇개 먹었니?
                int kingCount = ps.getTakenCards(CardClass.KING).size();
                //다른 사람이 몇개나 먹을수 있니?
                int kingForOthers = getMaxKingCountForOthers();

                //광의 카드 포인트가 승점-1 이고, 다른 사람이 먹을수 있는 개수가 0개일 경우 => 즉 광으로는 날수 없음.
                //또는 왕의 개수?가 승점-1이고 ㅇ다른 사람이 먹을 수 잇는 왕이 1개 이상이고, (내가 비광을 들고 있거나
                if ((ps.getCardPoints(CardClass.KING) == (winPoints - 1) && kingForOthers == 0)
                     || (kingCount == (winPoints - 1) && kingForOthers > 0
                         && (ps.isCardTaken(GameRule.RAIN_KING, false)
                             || (kingForOthers == 1 && kingCount <= 2
                                 && gameTable.isCardTaken(GameRule.RAIN_KING, false) == false))))
                {
                    //띠를 4개이상 먹었거나,
                    //끗을 4개이상 먹었거나,
                    //내가 가진 피의 개수 -1 + 딴놈이 먹을 수 있는 피의 개수가 1보다 크면 can NOT go!!!
                    if (ps.getTakenCards(CardClass.TEN).size() >= 4
                        || ps.getTakenCards(CardClass.FIVE).size() >= 4
                        || leafCount - 9 + getMaxLeafCountForOthers() >= 1)
                        canGo = false;
                }
                else if (kingCount > 1)
                    canGo = kingCount + kingForOthers < winPoints;
                // let's check other's fate
                //else if (kingCount == 1)
                //    canGo = getMaxKingCountForOthers() > 2;
            }

            //앞서까지 go 할수 있다고 판단했다면..
            if (canGo)
            {
                //띠와 끗에 대한 이야기...
                CardClass[] cc = { CardClass.TEN, CardClass.FIVE };
                
                for (int k = 0; k < cc.length && canGo; k++)
                {
                    //띠나 끗에서 내가 먹은 카드들을 골라보자.
                    List<CardItem> cards = ps.getTakenCards(cc[k]);
                    
                    for (int l = 0; l < cards.size() && canGo; l++)
                    {
                        //내가 먹은 띠나 끗에 의한 족보를 찾아보자, 고도리나, ~단 류가 나오겠지?
                        GoStopRule rule = GameRule.getGoStopRule(cards.get(l).getCardCode());

                        //해당 족보가 살아있나?
                        if (rule != null && gameTable.isRuleAvailable(rule))
                        {
                            int[] cardCodes = rule.getRuleCards();
                            int untaken = 0;
                            boolean canTake = false;

                            //족보가 살아있다면, 족보에 해당하는 카드들을 찾아보자.
                            for (int m = 0; m < cardCodes.length && canTake == false; m++)
                            {
                                if (gameTable.isCardTaken(cardCodes[m], false) == false)
                                {
                                    //족보가 살아있다면, 내가 그 족보를 먹을수 있는 상황인지 확인하자.
                                    untaken++;
                                    canTake = canTake(cardCodes[m]);
                                }
                            }

                            //안먹은 카드가 1장 이상이거나, 내가 먹을 수 있다고 판단되면 고하자.
                            canGo = canTake || untaken > 1;
                        }
                    }
                }
            }

            //마지막으로 go할수 있는지 알아볼까?
            if (canGo)
                //내가 가진 피 개수 - 9 + 다른 사람이 먹을수 있는 피의 최대 개수 < 승점 보다 작다면 고고(왜???)
                canGo = leafCount - 9 + getMaxLeafCountForOthers() < winPoints;
        }
        
        return canGo;
    }

    /**
     * 다른 사람이 먹을 수 있는 최대의 광 숫자를 리턴한다.
     * @return
     */
    private int getMaxKingCountForOthers()
    {
        int remaining = 0;
        //테이블에 보너스 광이 있는지 체크
        boolean hasBonus = gameTable.getBonusCards().getKingCount() > 0;

        //광 목록을 순찰하면서...
        for (int kingCode: GameRule.getKingCodes())
        {
            //검색하는 광 카드를 아무도 안 먹었고,
            // 광이 보너스카드이거나 바닥에 보너스가 깔려있고,
            // s내가 이 카드를 먹을수 없다고 판단한다면, 남은 개수를 ++
            if (gameTable.isCardTaken(kingCode, false) == false
                && (CardItem.isBonusCard(kingCode) == false || hasBonus)
                && canTake(kingCode) == false)
                remaining++;
        }
        
        return remaining;
    }
    
    private int getMaxLeafCountForOthers()
    {
        int maxLeaf = 0;
        BonusCards bonusCards = gameTable.getBonusCards();
        
        if (bonusCards.getTripleCount() > 0)
        {
            for (int tripleCode: GameRule.getTripleLeafCodes())
            {
                if (gameTable.isCardTaken(tripleCode, false) == false
                    && canTake(tripleCode) == false)
                    maxLeaf += 3;
            }
        }
        
        int doubleBonus = bonusCards.getDoubleCount();
        int doubleStart = CardItem.getCardIndex(CardItem.getCardCode(13, 'b'));
        int remainDouble = 0;
        
        for (int doubleCode: GameRule.getDoubleLeafCodes())
        {
            if (gameTable.isCardTaken(doubleCode, false) == false
                && (CardItem.isBonusCard(doubleCode) == false
                    || CardItem.getCardIndex(doubleCode)-doubleStart < doubleBonus)
                && canTake(doubleCode) == false)
            {
                if (CardItem.isBonusCard(doubleCode))
                    maxLeaf += 2;
                else
                    remainDouble++;
            }
        }
        
        if (remainDouble == 0)
            maxLeaf += 4;
        else if (remainDouble == 1)
            maxLeaf += 5;
        else
            maxLeaf += 6;

        maxLeaf = (int)(maxLeaf * 2 / 3.0f);    // 66 %
        
        return maxLeaf;
    }

    /**
     * 내가 이 카드를 먹을 수 있는지 없는지에 대해 판단하여 리턴한다.
     *
     * @param cardCode
     * @return
     */
    private boolean canTake(int cardCode)
    {
        boolean canTake;

        //카드 코드가 보너스카드일 경우....
        if (CardItem.isBonusCard(cardCode))
            //플레이어가 보너스 카드를 가지고 있을 경우... canTake!!!
            canTake = gamePlayer.isHoldingCard(cardCode);
        else
        {
            //카드 코드가 보너스 카드가 아닐 경우...
            int majorCode = CardItem.getMajorCode(cardCode);
            //죽은자 인지 체크한다.
            boolean takenHalf = gameTable.isCardTaken(majorCode, true);

            //죽은자 일 경우...
            if (takenHalf)
                //내가 카드를 몇장 들고 있니? 1장 이상일경우 canTake!!!
                canTake = gamePlayer.getHoldCardCount(majorCode) > 0;
            else
            {

                //내가 카드를 들고 있다면 canTake!!!
                canTake = gamePlayer.isHoldingCard(cardCode);

                //내가 카드를 들고 있지 않은데, 해당 패를 1장 이상 들고 있다면...
                if (canTake == false && gamePlayer.getHoldCardCount(majorCode) > 0)
                {

                    TableCardPoint tcp = gameTable.getTableCardPoint(majorCode, false);

                    //테이블에 세장이 깔려있다면 canTake!!!
                    canTake = tcp != null && tcp.getCardCount(true) == 3;
                }
            }
        }
        
        return canTake;
    }
    
    private int getTakenCount(GoStopRule rule)
    {
        int taken = 0;
        
        for (int cardCode: rule.getRuleCards())
        {
            if (gameTable.isCardTaken(cardCode, false))
                taken++;
        }
        
        return taken;
    }
}
