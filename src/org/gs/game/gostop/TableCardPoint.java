package org.gs.game.gostop;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.gs.game.gostop.item.CardItem;
import org.gs.game.gostop.play.GameRule;

/**
 * Table card position on the table (not on the stack)
 * 바닥의 패의 위치를 표현하며, 해당 위치에 놓여진 패를 관리합니다.
 */
public class TableCardPoint
{
    protected static int maxOverlapWidth;
    
    private Point point;
    private List<CardItem> cardItems;
    private int majorCode;      // -1: unassigned, 0: intermediate (reserved)
    private String swampedPlayerId;
    private boolean bombTarget;
    
    protected TableCardPoint(Point point, List<CardItem> cardItems)
    {
        this.point = point;
        this.cardItems = cardItems;
        this.majorCode = -1;
        swampedPlayerId = null;
        bombTarget = false;
    }
    
    public Point getPoint()
    {
        return point;
    }
    
    public List<CardItem> getCardItems()
    {
        return cardItems;
    }
    
    public int getCardCount(boolean excludeBonus)
    {
        int count = 0;
        
        if (excludeBonus)
        {
            if (cardItems != null)
            {
                for (CardItem cardItem: cardItems)
                {
                    if (cardItem.isBonusCard() == false)
                        count++;
                }
            }
        }
        else if (cardItems != null)
            count = cardItems.size();
        
        return count;
    }
    
    public boolean isEmpty()
    {
        return majorCode < 0; 
    }
    
    protected void setMajorCode(int majorCode)
    {
        this.majorCode = majorCode;
    }
    
    public int getMajorCode()
    {
        return majorCode; 
    }
    
    public boolean isBonusCard()
    {
        return cardItems != null && cardItems.size() > 0 && cardItems.get(0).isBonusCard();
    }
    
    public int addCardItem(CardItem cardItem)
    {
        if (majorCode > 0 && majorCode != cardItem.getMajorCode()
            && cardItem.isBonusCard() == false)
            throw new RuntimeException("Invalid Card Item");
        
        if (cardItem.isBonusCard() == false)
            setMajorCode(cardItem.getMajorCode());
        
        if (cardItems == null)
            cardItems = new ArrayList<CardItem>(4);
        
        cardItems.add(cardItem);
        
        reorderCards();
        
        return cardItems.size();
    }
    
    public void addCardItems(List<CardItem> cards)
    {
        if (cards != null)
        {
            for (CardItem card: cards)
                addCardItem(card);
        }
    }
    
    public List<CardItem> removeCardItems()
    {
        List<CardItem> removedItems = cardItems;
        
        majorCode = -1;
        cardItems = null;
        swampedPlayerId = null;
        bombTarget = false;
        
        return removedItems;
    }
    
    public boolean removeCardItem(CardItem cardItem)
    {
        boolean removed = false;
        
        if (cardItems != null && cardItems.remove(cardItem))
        {
            removed = true;
            if (cardItems.size() == 0)
                removeCardItems();
            else
                reorderCards();
        }
        
        return removed;
    }

    /**
     * 먹으려면 결정이 필요한가요?
     * 카드가 두장이고, 두개의 카드 클라스가 다르고 또는 카드클라스는 같으나 피의 점수가 다를 경우 결정해야쥐
     * @return
     */
    public boolean needToQueryForTaking()
    {
        return getCardCount(false) == 2
               && (cardItems.get(0).getCardClass() != cardItems.get(1).getCardClass()
                   || (CardClass.LEAF.equals(cardItems.get(0).getCardClass())
                       && GameRule.getLeafPoints(cardItems.get(0).getCardCode())
                          != GameRule.getLeafPoints(cardItems.get(1).getCardCode())));
    }
    
    public void setSwampedPlayer(GamePlayer gamePlayer)
    {
        swampedPlayerId = gamePlayer.getGameUser().getLoginId();
    }
    
    public boolean isSwampedPlayer(GamePlayer gamePlayer)
    {
        return gamePlayer.getGameUser().getLoginId().equals(swampedPlayerId);
    }
    
    public void setBombTarget(boolean bombTarget)
    {
        this.bombTarget = bombTarget;
    }
    
    public boolean isBombTarget()
    {
        return bombTarget;
    }
    
    public boolean hasMissionCard()
    {
        boolean has = false;
        
        if (cardItems != null)
        {
            for (CardItem ci: cardItems)
                has = has || ci.isMissionCard();
        }
        
        return has;
    }

    /**
     * 바닥에 쌍피 또는 쓰리피가 깔려있는지 여부
     * @return
     */
    public boolean hasMultiLeaves()
    {
        boolean has = false;
        
        if (cardItems != null)
        {
            for (CardItem ci: cardItems)
                has = has || GameRule.getLeafPoints(ci.getCardCode()) > 1;
        }
        
        return has;
    }
    
    public boolean hasKingCard()
    {
        boolean has = false;
        
        if (cardItems != null)
        {
            for (CardItem ci: cardItems)
                has = has || ci.getCardClass() == CardClass.KING;
        }
        
        return has;
    }
    
    private void reorderCards()
    {
        if (cardItems != null && cardItems.size() > 0)
        {
            int cardWidth = cardItems.get(0).getRect().width;
            int xoff = cardWidth / 2;
            int count = cardItems.size();
            
            if (maxOverlapWidth < count * xoff + cardWidth)
                xoff = (maxOverlapWidth-cardWidth)/count;
            
            for (int i = 0; i < count; i++)
            {
                cardItems.get(i).setZOrder(GamePanel.CARD_ZORDER-i);
                cardItems.get(i).moveItem(new Point(point.x+xoff*i, point.y));
            }
        }
    }
}
