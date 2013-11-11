package org.gs.game.gostop.config;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class BonusCards
{
    private static ArrayList<BonusCards> _bonusCards = null;
    
    private int trippleCount;
    private int doubleCount;
    private int kingCount;
    
    public static void addBonusCards(int trippleCount, int doubleCount, int kingCount)
    {
        if (_bonusCards == null)
            _bonusCards = new ArrayList<BonusCards>();
        
        _bonusCards.add(new BonusCards(trippleCount, doubleCount, kingCount));
    }
    
    public static List<BonusCards> getBonusCards()
    {
        return _bonusCards;
    }
    
    public BonusCards(int trippleCount, int doubleCount, int kingCount)
    {
        this.trippleCount = trippleCount;
        this.doubleCount = doubleCount;
        this.kingCount = kingCount;
    }
    
    public int getTripleCount()
    {
        return trippleCount;
    }
    
    public int getDoubleCount()
    {
        return doubleCount;
    }
    
    public int getKingCount()
    {
        return kingCount;
    }
    
    public boolean equals(Object obj)
    {
        boolean result;
        
        if (obj instanceof BonusCards)
        {
            BonusCards bc = (BonusCards)obj;
            result = trippleCount == bc.trippleCount
                     && doubleCount == bc.doubleCount && kingCount == bc.kingCount;
        }
        else
            result = super.equals(obj);
        return result;
    }
    
    public static String getPropString(BonusCards bc)
    {
        String str;
        
        if (bc != null)
            str = Integer.toString(bc.trippleCount) + ','
                  + Integer.toString(bc.doubleCount) + ','
                  + Integer.toString(bc.kingCount);
        else
            str = "";
        
        return str;
    }
    
    public static BonusCards valueOf(String str)
    {
        BonusCards value = _bonusCards != null && _bonusCards.size() > 0
                           ? _bonusCards.get(0) : null;
        
        if (str != null && str.length() > 0)
        {
            StringTokenizer st = new StringTokenizer(str, ",");
            ArrayList<Integer> values = new ArrayList<Integer>(4);
            
            while (st.hasMoreTokens())
                values.add(Integer.valueOf(st.nextToken()));
            
            if (values.size() >= 3)
            {
                BonusCards bc = new BonusCards(values.get(0), values.get(1),
                                               values.get(2));
                int index = _bonusCards.indexOf(bc);
                
                if (index >= 0)
                    value = _bonusCards.get(index);
            }
        }
        
        return value;
    }
}
