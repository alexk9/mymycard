package org.gs.game.gostop.play;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gs.game.gostop.CardClass;
import org.gs.game.gostop.item.CardItem;

/**
 * GS고스톱의 게임 규칙은 org.gs.game.gostop.play 패키지의 GameRule 클래스에 명시되어 있습니다.
 * 여기에 쌍피패, 삼피패, 패의 종류, 약 등이 정의되어 있으며, 패의 종류별 점수를 계산하는 메소드들도 정의되어 있습니다.
 */
public class GameRule
{
    public static final int RAIN_KING = CardItem.getCardCode(0xc, 'a');
    public static final int NINE_TEN = CardItem.getCardCode(0x9, 'a');
    
    /**
     * Card codes for double points
     */
    private static final int[] _doubleLeaves =
    {
        NINE_TEN,
        CardItem.getCardCode(0xb, 'b'),
        CardItem.getCardCode(0xc, 'd'),
        CardItem.getCardCode(0xd, 'b'),
        CardItem.getCardCode(0xd, 'c'),
        CardItem.getCardCode(0xd, 'd'),
    };
    
    /**
     * Card codes for triple points
     */
    private static final int[] _tripleLeaves =
    {
        CardItem.getCardCode(0xd, 'a')
    };
    
    private static final CardClass[] _cardClasses =
    {
        CardClass.KING, CardClass.FIVE, CardClass.LEAF, CardClass.LEAF,     // 1
        CardClass.TEN, CardClass.FIVE, CardClass.LEAF, CardClass.LEAF,      // 2
        CardClass.KING, CardClass.FIVE, CardClass.LEAF, CardClass.LEAF,     // 3
        CardClass.TEN, CardClass.FIVE, CardClass.LEAF, CardClass.LEAF,      // 4
        CardClass.TEN, CardClass.FIVE, CardClass.LEAF, CardClass.LEAF,      // 5
        CardClass.TEN, CardClass.FIVE, CardClass.LEAF, CardClass.LEAF,      // 6
        CardClass.TEN, CardClass.FIVE, CardClass.LEAF, CardClass.LEAF,      // 7
        CardClass.KING, CardClass.TEN, CardClass.LEAF, CardClass.LEAF,      // 8
        CardClass.TEN_LEAF, CardClass.FIVE, CardClass.LEAF, CardClass.LEAF, // 9
        CardClass.TEN, CardClass.FIVE, CardClass.LEAF, CardClass.LEAF,      // a
        CardClass.KING, CardClass.LEAF, CardClass.LEAF, CardClass.LEAF,     // b
        CardClass.KING, CardClass.TEN, CardClass.FIVE, CardClass.LEAF,      // c
        CardClass.LEAF, CardClass.LEAF, CardClass.LEAF, CardClass.LEAF,     // d (bonus)
        CardClass.KING                                                      // e (bonus)
    };
    
    private static final int[] _kingCards =
    {
        CardItem.getCardCode(0x1, 'a'),
        CardItem.getCardCode(0x3, 'a'),
        CardItem.getCardCode(0x8, 'a'),
        CardItem.getCardCode(0xb, 'a'),
        RAIN_KING,
        CardItem.getCardCode(0xe, 'a'),
    };
    
    private static final int[][] _ruleCards =
    {
        {   // game.rule.godori
            CardItem.getCardCode(0x2, 'a'),
            CardItem.getCardCode(0x4, 'a'),
            CardItem.getCardCode(0x8, 'b')
        },
        {   // game.rule.hongdan
            CardItem.getCardCode(0x1, 'b'),
            CardItem.getCardCode(0x2, 'b'),
            CardItem.getCardCode(0x3, 'b')
        },
        {   // game.rule.chodan
            CardItem.getCardCode(0x4, 'b'),
            CardItem.getCardCode(0x5, 'b'),
            CardItem.getCardCode(0x7, 'b')
        },
        {   // game.rule.chungdan
            CardItem.getCardCode(0x6, 'b'),
            CardItem.getCardCode(0x9, 'b'),
            CardItem.getCardCode(0xa, 'b')
        },
    };
    
    public static class GoStopRule
    {
        private String ruleId;
        private CardClass ruleCardClass;
        private int rulePoint;
        private int[] ruleCards;
        
        private GoStopRule(String ruleId, CardClass ruleCardClass,
                           int rulePoint, int[] ruleCards)
        {
            this.ruleId = ruleId;
            this.ruleCardClass = ruleCardClass;
            this.rulePoint = rulePoint;
            this.ruleCards = ruleCards;
        }
        
        public String getRuleId()
        {
            return ruleId;
        }
        
        public CardClass getRuleCardClass()
        {
            return ruleCardClass;
        }
        
        public int getRulePoint()
        {
            return rulePoint;
        }
        
        public int[] getRuleCards()
        {
            return ruleCards;
        }
    }
    
    private static final GoStopRule[] _gostopRule =
    {
        new GoStopRule("game.rule.godori", CardClass.TEN, 5, _ruleCards[0]),
        new GoStopRule("game.rule.hongdan", CardClass.FIVE, 3, _ruleCards[1]),
        new GoStopRule("game.rule.chodan", CardClass.FIVE, 3, _ruleCards[2]),
        new GoStopRule("game.rule.chungdan", CardClass.FIVE, 3, _ruleCards[3]),
    };
    
    
    public static CardClass getClardClass(int index)
    {
        return _cardClasses[index];
    }

    public static int getKingPoints(List<CardItem> kings)
    {
        int points = 0;
        
        if (kings.size() >= 3)
        {
            if (kings.size() == 6)
                points = 36;            // 6 * 6
            else if (kings.size() == 5)
                points = 15;            // 5 * 3
            else if (kings.size() == 4)
                points = 4;
            else
            {
                boolean foundRainKing = false;
                
                for (int i = 0; i < kings.size() && foundRainKing == false; i++)
                    foundRainKing = kings.get(i).getCardCode() == RAIN_KING;
                
                points = foundRainKing ? 2 : 3;
            }
        }
        
        return points;
    }
    
    public static int getTenPoints(List<CardItem> tens)
    {
        int points = 0;

        for (int i = 0; i < _ruleCards.length; i++)
        {
            int count = 0;
         
            for (CardItem cardItem: tens)
            {
                if (Arrays.binarySearch(_ruleCards[i], cardItem.getCardCode()) >= 0)
                    count++;
            }
            
            if (count == _ruleCards[i].length)
                points += _gostopRule[i].rulePoint;
        }
        
        if (tens.size() >= 5)
            points += tens.size() - 4;
        
        return points;
    }
    
    public static int getFivePoints(List<CardItem> fives)
    {
        return getTenPoints(fives);
    }

    public static int getLeafCount(List<CardItem> leaves)
    {
        int leafCounts = 0;
        
        for (CardItem cardItem: leaves)
            leafCounts += getLeafPoints(cardItem.getCardCode());
        
        return leafCounts;
    }
    
    public static int getLeafPoints(int cardCode)
    {
        int points;
        
        if (Arrays.binarySearch(_doubleLeaves, cardCode) >= 0)
            points = 2;
        else if (Arrays.binarySearch(_tripleLeaves, cardCode) >= 0)
            points = 3;
        else
            points = 1;
        
        return points;
    }
    
    public static List<GoStopRule> getGoStopRule(List<CardItem> cards)
    {
        List<GoStopRule> rules = null;
        
        for (int i = 0; i < _ruleCards.length; i++)
        {
            int count = 0;
         
            for (CardItem cardItem: cards)
            {
                if (Arrays.binarySearch(_ruleCards[i], cardItem.getCardCode()) >= 0)
                    count++;
            }
            
            if (count == _ruleCards[i].length)
            {
                if (rules == null)
                    rules = new ArrayList<GoStopRule>();
                
                rules.add(_gostopRule[i]);
            }
        }
        
        return rules;
    }
    
    public static GoStopRule getGoStopRule(int cardCode)
    {
        GoStopRule rule = null;
        
        for (int i = 0; i < _gostopRule.length && rule == null; i++)
        {
            if (Arrays.binarySearch(_gostopRule[i].ruleCards, cardCode) >= 0)
                rule = _gostopRule[i];
        }
        
        return rule;
    }
    
    public static int[] getKingCodes()
    {
        return _kingCards;
    }
    
    public static int[] getDoubleLeafCodes()
    {
        return _doubleLeaves;
    }
    
    public static int[] getTripleLeafCodes()
    {
        return _tripleLeaves;
    }
}
