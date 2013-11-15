package org.gs.game.gostop;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gs.game.gostop.config.PlayerLayout;
import org.gs.game.gostop.config.PlayerSide;
import org.gs.game.gostop.item.*;
import org.gs.game.gostop.play.GamePenalty;
import org.gs.game.gostop.play.GameRule;

/**
 * 게임 참가자의 먹은 패, 점수상태, 박상태 등을 관리합니다.
 */
public class PlayerStatus
{
    private static final int LC_SIZE = 18;

    /**
     * 플레이어의 위치?
     */
    private PlayerSide playerSide;
    /**
     * 먹은 광의수
     */
    private List<CardItem> takenKings;
    private List<CardItem> takenTens;
    private List<CardItem> takenFives;
    /**
     * 피의 개수?
     */
    private List<CardItem> takenLeaves;
    private LeafCountItem leafCounter;
    private HashMap<CardClass,Rectangle> takenRects;
    private int prevPoints;
    private int prevLeafPoints;
    private static NINE_STATUS nineDecided;
    
    private PlayerPointItem playerPoint;
    private PlayerPenaltyItem playerPenalty;
    private PlayerBonusItem playerBonus;
    private int missionBonus;

    /**
     * 9자를 어떻게 쓸건지...
     * 쌍피로 쓸껀지...,
     */
    private static enum NINE_STATUS { NOT_DECIDED, USE_AS_TEN, USE_AS_LEAF }

    public PlayerStatus(GamePanel gamePanel, PlayerLayout playerLayout)
    {
        playerSide = playerLayout.getPlayerSide();
        playerPoint = new PlayerPointItem(gamePanel, playerLayout.getPointRect());
        playerPenalty = new PlayerPenaltyItem(gamePanel, playerLayout.getPenaltyRect());
        playerBonus = new PlayerBonusItem(gamePanel, playerLayout.getBonusRect());
        
        takenRects = new HashMap<CardClass,Rectangle>(4);
        for (CardClass cc: CardClass.values())
        {
            if (CardClass.TEN_LEAF != cc)
                takenRects.put(cc, playerLayout.getTakenRect(cc));
        }
        
        CardSize cardSize = playerLayout.isOpposite() ? CardSize.NORMAL : CardSize.HOLD;
        Dimension cardDim = CardItem.getCardImageInfo(cardSize).getSize();
        Rectangle rl = playerLayout.getTakenRect(CardClass.LEAF);
        Rectangle rect = new Rectangle(rl.x, rl.y+cardDim.height-LC_SIZE, LC_SIZE, LC_SIZE);
        leafCounter = new LeafCountItem(gamePanel, rect);
    }//
    
    public void initPlayerStatus()
    {
        takenKings = new ArrayList<CardItem>(6);
        takenTens = new ArrayList<CardItem>(8);
        takenFives = new ArrayList<CardItem>(8);
        takenLeaves = new ArrayList<CardItem>(16);
        
        prevPoints = 0;
        prevLeafPoints = 0;
        nineDecided = NINE_STATUS.NOT_DECIDED;
        
        playerPoint.setPoints(0);
        playerPenalty.clearPenalties();
        playerBonus.clearBonus();
        leafCounter.setCount(0);
        missionBonus = 0;
    }
    
    public PlayerSide getPlayerSide()
    {
        return playerSide;
    }

    /**
     * 주어진 카드 클래스에 해당하는 카드를 리턴한다.
     * @param cardClass
     * @return
     */
    public List<CardItem> getTakenCards(CardClass cardClass)
    {
        List<CardItem> cardItems;
        
        if (cardClass == CardClass.KING)
            cardItems = takenKings;
        else if (cardClass == CardClass.TEN
                 || (cardClass == CardClass.TEN_LEAF && nineDecided != NINE_STATUS.USE_AS_LEAF))
            cardItems = takenTens;
        else if (cardClass == CardClass.FIVE)
            cardItems = takenFives;
        else
            cardItems = takenLeaves;
        
        return cardItems;
    }

    public int getBonusCount()
    {
        int count = 0;
        
        if (playerBonus.getGoCount() > 2)
            count += playerBonus.getGoCount() - 2;
        
        count += playerBonus.getTripleCount();
        
        if (takenTens.size() > 6)       // mongDDa
            count += takenTens.size() - 6;
        
        return count;
    }
    
    public void addMissionBonus(int missionBonus)
    {
        if (this.missionBonus == 0)
            this.missionBonus = missionBonus;
        else
            this.missionBonus *= missionBonus;
    }
    
    public int getMissionBonus()
    {
        return missionBonus;
    }
    
    public int getPrevPoints()
    {
        return prevPoints;
    }
    
    public int getCardPoints()
    {
        return getCardPoints(CardClass.KING) + getCardPoints(CardClass.TEN)
               + getCardPoints(CardClass.FIVE) + getCardPoints(CardClass.LEAF)
               + getGoCount();
    }
    
    public int refreshCardPoints()
    {
        int newPoints = getCardPoints();
        
        playerPoint.setPoints(newPoints);
        
        return newPoints;
    }

    /**
     * 플레이어의 포인트를 얻어보자.
     * @return
     */
    public int getPlayerPoints()
    {
        return playerPoint.getPoints();
    }
    
    public int getCardPoints(CardClass cardClass)
    {
        int points;
        
        if (CardClass.KING.equals(cardClass))
            points = GameRule.getKingPoints(takenKings);
        else if (CardClass.TEN.equals(cardClass))
            points = GameRule.getTenPoints(takenTens);
        else if (CardClass.FIVE.equals(cardClass))
            points = GameRule.getFivePoints(takenFives);
        else if (CardClass.LEAF.equals(cardClass))
        {
            int count = GameRule.getLeafCount(takenLeaves);
            points = count > 9 ? count-9 : 0;
        }
        else
            points = 0;
        
        return points;
    }
    
    public boolean hasNoTakenCards()
    {
        return takenKings.size() == 0 && takenTens.size() == 0
               && takenFives.size() == 0 && takenLeaves.size() == 0;
    }
    
    public void addTakenCard(CardItem cardItem, boolean isOpposite)
    {
        cardItem.setCardSize(isOpposite ? CardSize.NORMAL : CardSize.HOLD);
        
        List<CardItem> takenCards = getTakenCards(cardItem.getCardClass());

        takenCards.add(cardItem);
        arrangeCards(takenCards, cardItem.getCardClass());
    }
    
    public int getGoCount()
    {
        return playerBonus.getGoCount();
    }
    
    public void addGoCount()
    {
        playerBonus.addGoCount();
        prevPoints = refreshCardPoints();
        
        int curLeafPoints = getCardPoints(CardClass.LEAF);
        if (prevLeafPoints < curLeafPoints)
            prevLeafPoints = curLeafPoints;
    }
    
    public int getPrevLeafPoints()
    {
        return prevLeafPoints;
    }
    
    public void addTripleCount()
    {
        playerBonus.addTripleCount();
    }

    /**
     * 카드가 이미 먹혔는지 체크한다.
     * 이미 먹힌, 광,끗,띠,피 중에 있는지 확인하여 리턴한다.
     * @param cardCode
     * @param majorCode
     * @return
     */
    public boolean isCardTaken(int cardCode, boolean majorCode)
    {
        return isCardTaken(takenLeaves, cardCode, majorCode)
               || isCardTaken(takenFives, cardCode, majorCode)
               || isCardTaken(takenTens, cardCode, majorCode)
               || isCardTaken(takenKings, cardCode, majorCode);
    }
    
    public boolean removeLeaf(CardItem cardItem)
    {
        boolean removed = false;
        
        if (takenLeaves.remove(cardItem))
        {
            arrangeCards(takenLeaves, CardClass.LEAF);
            removed = true;
        }
        
        return removed;
    }
    
    public void addSwampCount()
    {
        playerBonus.addSwampCount();
    }
    
    public int getSwampCount()
    {
        return playerBonus.getSwampCount();
    }
    
    public boolean needToDecideNine(int winPoints)
    {
        boolean needToDecide = false;
        
        if (nineDecided == NINE_STATUS.NOT_DECIDED
            && (GameRule.getLeafCount(takenLeaves) >= 8 || getCardPoints() >= winPoints))
        {
            for (int i = 0; i < takenTens.size() && needToDecide == false; i++)
                needToDecide = takenTens.get(i).getCardClass() == CardClass.TEN_LEAF;
        }
        
        return needToDecide;
    }

    /**
     * 9짜가 결정이 되었는지를 체크한다.
     * @return
     */
    public boolean isTakenNineUndecided()
    {
        //9결정 여부
        boolean undecided = nineDecided == NINE_STATUS.NOT_DECIDED;

        //결정이 안되었다면...
        if (undecided)
        {
            undecided = false;
            //피의 라인에 가서 이놈이 있으면 결정된걸로 하자.
            for (int i = 0; i < takenTens.size() && undecided == false; i++)
                undecided = takenTens.get(i).getCardClass() == CardClass.TEN_LEAF;
        }
        
        return undecided;
    }
    
    public void setNineDecided(boolean useAsLeaf)
    {
        nineDecided = useAsLeaf ? NINE_STATUS.USE_AS_LEAF : NINE_STATUS.USE_AS_TEN;
        
        if (useAsLeaf)
        {
            CardItem nineItem = null;
            
            for (int i = 0; i < takenTens.size() && nineItem == null; i++)
            {
                if (takenTens.get(i).getCardClass() == CardClass.TEN_LEAF)
                    nineItem = takenTens.get(i);
            }
            
            takenTens.remove(nineItem);
            arrangeCards(takenTens, CardClass.TEN);
            
            takenLeaves.add(nineItem);
            arrangeCards(takenLeaves, CardClass.LEAF);
        }
    }
    
    public void setPenalty(GamePenalty gamePanalty, boolean penalty)
    {
        playerPenalty.setPenalty(gamePanalty, penalty);
    }

    /**
     * 이미 데이큰 된 카드들을 돌면서 먹었는지 체크함.
     * 테이큰된 카드는 내부 관리하는 변수들임.
     * @param takenCards
     * @param cardCode
     * @param majorCode major코드로만 비교 할건지 여부.
     * @return
     */
    private boolean isCardTaken(List<CardItem> takenCards, int cardCode, boolean majorCode)
    {
        boolean taken = false;
        
        for (int i = 0; i < takenCards.size() && taken == false; i++)
        {
            if (majorCode)
                taken = cardCode == takenCards.get(i).getMajorCode();
            else
                taken = cardCode == takenCards.get(i).getCardCode();
        }
        
        return taken;
    }
    
    private Rectangle getTakenRect(CardClass cardClass)
    {
        if (cardClass == CardClass.TEN_LEAF)
        {
            if (nineDecided != NINE_STATUS.USE_AS_LEAF)
                cardClass = CardClass.TEN;
            else
                cardClass = CardClass.LEAF;
        }
        
        return takenRects.get(cardClass);
    }
    
    private void arrangeCards(List<CardItem> takenCards, CardClass cardClass)
    {
        int count = takenCards.size();
        
        if (count > 0)
        {
            CardItem cardItem = takenCards.get(0);
            int cardWidth = cardItem.getRect().width;
            int xoff = cardWidth / 2;
            Rectangle rect = getTakenRect(cardItem.getCardClass());
            
            if (rect.width < count * xoff + cardWidth)
                xoff = (rect.width-cardWidth)/count;
                
            for (int i = 0; i < count; i++)
            {
                cardItem = takenCards.get(i);
                cardItem.setZOrder(GamePanel.CARD_ZORDER-i);
                cardItem.moveItem(new Point(rect.x+xoff*i, rect.y));
            }
        }
        
        if (cardClass == CardClass.TEN_LEAF)
            cardClass = nineDecided == NINE_STATUS.USE_AS_LEAF ? CardClass.LEAF : CardClass.TEN;
        
        if (cardClass == CardClass.LEAF)
        {
            leafCounter.setCount(GameRule.getLeafCount(takenCards));
            if (count > 0)
            {
                Rectangle rect = takenCards.get(count-1).getRect();
                leafCounter.moveItem(new Point(rect.x+rect.width-LC_SIZE,
                                               leafCounter.getRect().y));
                leafCounter.setZOrder(GamePanel.CARD_ZORDER-count);
            }
        }
    }
}
