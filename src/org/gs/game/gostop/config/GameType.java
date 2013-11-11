package org.gs.game.gostop.config;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class GameType
{
    private static ArrayList<GameType> _gameTypes = null;
    private static Integer[] _gameMoneyTypes = null;
    
    private String typeId;
    private int players;
    private int gameMoney;
    private int winPoints;
    private int leafPenalty;
    private int[] tableCards;
    private int[] playerCards;
    private GameLayout gameLayout;

    public static GameType addGameType(String typeId, int players, int winPoints, int leafPenalty)
    {
        if (_gameTypes == null)
            _gameTypes = new ArrayList<GameType>();
        
        GameType gameType = new GameType(typeId, players, winPoints, leafPenalty);
        
        _gameTypes.add(gameType);
        
        return gameType;
    }
    
    public static List<GameType> getGameTypes()
    {
        return _gameTypes;
    }
    
    public static GameType getGameType(String typeId)
    {
        GameType gameType = null;
        
        if (_gameTypes != null)
        {
            for (int i = 0; i < _gameTypes.size() && gameType == null; i++)
            {
                if (typeId.equals(_gameTypes.get(i).getTypeId()))
                    gameType = _gameTypes.get(i);
            }
        }
        
        return gameType;
    }
    
    public static void setGameMoneyTypes(String gameMoneyTypes)
    {
        int[] moneyTypes = parseIntegers(gameMoneyTypes);
        
        if (moneyTypes == null)
            _gameMoneyTypes = null;
        else
        {
            _gameMoneyTypes = new Integer[moneyTypes.length];
            
            for (int i = 0; i < moneyTypes.length; i++)
                _gameMoneyTypes[i] = moneyTypes[i];
        }
    }
    
    public static Integer[] getGameMoneyTypes()
    {
        return _gameMoneyTypes;
    }
    
    private GameType(String typeId, int players, int winPoints, int leafPenalty)
    {
        this(typeId, players, winPoints, leafPenalty, null);
    }
    
    private GameType(String typeId, int players, int winPoints, int leafPenalty,
                     GameLayout gameLayout)
    {
        this.typeId = typeId;
        this.players = players;
        gameMoney = 1;
        this.winPoints = winPoints;
        this.leafPenalty = leafPenalty;
        this.gameLayout = gameLayout;
        tableCards = null;
        playerCards = null;
    }
    
    public String getTypeId()
    {
        return typeId;
    }
    
    public int getPlayers()
    {
        return players;
    }
    
    public int getGameMoney()
    {
        return gameMoney;
    }
    
    public void setGameMoney(int gameMoney)
    {
        this.gameMoney = gameMoney;
    }
    
    public int getWinPoints()
    {
        return winPoints;
    }
    
    public int getLeafPenalty()
    {
        return leafPenalty;
    }
    
    public void setStartCards(String tableCards, String playerCards)
    {
        this.tableCards = parseIntegers(tableCards);
        this.playerCards = parseIntegers(playerCards);
    }
    
    public int[] getTableCards()
    {
        return tableCards;
    }
    
    public int[] getPlayerCards()
    {
        return playerCards;
    }
    
    public int getMaxTurns()
    {
        int turns = 0;
        
        for (int cards: playerCards)
            turns += cards;
        
        return turns;
    }
    
    public void setGameLayout(GameLayout gameLayout)
    {
        this.gameLayout = gameLayout;
    }
    
    public GameLayout getGameLayout()
    {
        return gameLayout;
    }
    
    private static int[] parseIntegers(String value)
    {
        StringTokenizer st = new StringTokenizer(value, ",");
        ArrayList<Integer> values = new ArrayList<Integer>();
        
        while (st.hasMoreTokens())
            values.add(Integer.parseInt(st.nextToken()));
        
        int[] parsed = new int[values.size()];
        for (int i = 0; i < values.size(); i++)
            parsed[i] = values.get(i);
        
        return parsed;
    }
}
