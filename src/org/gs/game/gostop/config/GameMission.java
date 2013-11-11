package org.gs.game.gostop.config;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.gs.game.gostop.Main;
import org.gs.game.gostop.item.CardItem;

public class GameMission
{
    private static ArrayList<GameMission> _gameMissions = null;
 
    public static enum MissionType { AND, OR, COUNT, UNKNOWN }
    public static final GameMission UnknownMission = new GameMission(null, MissionType.UNKNOWN, null);
    public static final GameMission NoMission = new GameMission(null, MissionType.UNKNOWN, null);

    public static final int TEN_CLASS = 21;
    public static final int FIVE_CLASS = 22;
    public static final int LEAF_CLASS = 23;
    
    private int[] cards;
    private MissionType type;
    private int[] bonus;
    
    public static void addGameMission(String cards, String type, String bonus)
    {
        if (_gameMissions == null)
            _gameMissions = new ArrayList<GameMission>(16);
        
        _gameMissions.add(new GameMission(cards, type, bonus));
    }
    
    public static GameMission getRandomMission()
    {
        GameMission gm = null;
        
        if (_gameMissions != null)
        {
            int index = Main.getRandom().nextInt(_gameMissions.size()+1);    // including no mission
         
            if (index < _gameMissions.size())
                gm = _gameMissions.get(index);
            else
                gm = NoMission;
        }
        
        return gm;
    }
    
    private GameMission(String cards, String type, String bonus)
    {
        this(parseCards(cards), GameConfig.valueOf(MissionType.class, type), parseBonus(bonus));
    }
    
    private GameMission(int[] cards, MissionType type, int[] bonus)
    {
        this.cards = cards;
        this.type = type;
        this.bonus = bonus;
    }
    
    public int[] getCards()
    {
        return cards;
    }
    
    public MissionType getMissionType()
    {
        return type;
    }
    
    public int[] getBonus()
    {
        return bonus;
    }
    
    public int getRandomBonus()
    {
        int bonus = -1;
        
        if (this.bonus != null && this.bonus.length > 0)
            bonus = this.bonus[Main.getRandom().nextInt(this.bonus.length)];
        
        return bonus;
    }
    
    public boolean isMissionCard(int cardCode)
    {
        boolean mission = false;
        
        for (int i = 0; i < cards.length && mission == false; i++)
            mission = cardCode == cards[i];
        
        return mission;
    }
    
    private static int[] parseCards(String cards)
    {
        int[] cardIds = new int[cards.length()/2];
        int count = 0;
        int i = 0;

        while (i < cards.length())
        {
            char type = cards.charAt(i+1);
            
            if ('a' <= type && type <= 'd')
            {
                int major = Integer.parseInt(cards.substring(i, i+1), 16);
                
                cardIds[count++] = CardItem.getCardCode(major, type);
                i += 2;
            }
            else    // count mission
            {
                int next = 2;
                
                if (Character.isDigit(type))
                {
                    type = cards.charAt(i+2);
                    next = 3;
                }
                
                int major = 0;
                if (type == 'T')
                    major = TEN_CLASS;
                else if (type == 'F')
                    major = FIVE_CLASS;
                else if (type == 'L')
                    major = LEAF_CLASS;
                
                if (major != 0)
                {
                    int minor = Integer.parseInt(cards.substring(i, i+next-1));
                    cardIds[count++] = CardItem.getCardCode(major, minor);
                }
                
                i += next;
            }
        }
        
        return cardIds;
    }
    
    private static int[] parseBonus(String bonus)
    {
        StringTokenizer st = new StringTokenizer(bonus, "x, ");
        int[] bonusValues = new int[st.countTokens()];
        int i = 0;
        
        while (st.hasMoreTokens())
            bonusValues[i++] = Integer.parseInt(st.nextToken());
        
        return bonusValues; 
    }
}
