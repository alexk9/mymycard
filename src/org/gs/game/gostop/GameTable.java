package org.gs.game.gostop;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gs.game.gostop.config.BonusCards;
import org.gs.game.gostop.config.GameType;
import org.gs.game.gostop.config.GameUser;
import org.gs.game.gostop.config.WinType;
import org.gs.game.gostop.item.CardItem;
import org.gs.game.gostop.item.MissionItem;
import org.gs.game.gostop.play.GamePenalty;
import org.gs.game.gostop.play.GameRule;
import org.gs.game.gostop.play.GameRule.GoStopRule;

/**
 * 화투 테이블을 표현하며, 각 사용자의 먹은 패와 바닥 패, 미션, 뒷패, 고한 참가자 등을 관리합니다. 게임 참가자의 가진 패는 GameTable을 통해 볼 수 없습니다.
 */
public class GameTable
{
    private GameType gameType;
    private BonusCards bonusCards;
    private MissionItem missionItem;
    /**
     * 테이블 위에 놓일 카드가 위치할 포인트를 저장해둠.
     * 각 포인트는 순서대로 다음과 같다.
     * 8 2 0 4 a
     *   6 q 7
     * 9 3 1 5 b
     * q가 카드 스택이 놓여 있는 곳
     */
    private TableCardPoint[] tableCardPoints;
    private HashMap<GameUser,PlayerStatus> playersStatus;
    private ICardDeck cardDeck;
    private int curTurn;
    /**
     * ??? goPlayers???
     */
    private List<GamePlayer> goPlayers;
    
    protected GameTable(ICardDeck cardDeck)
    {
        tableCardPoints = null;
        playersStatus = new HashMap<GameUser,PlayerStatus>(4);
        this.cardDeck = cardDeck;
        gameType = null;
    }
    
    public void setGameType(GameType gameType)
    {
        this.gameType = gameType;
    }
    
    public GameType getGameType()
    {
        return gameType;
    }
    
    public void setBonusCards(BonusCards bonusCards)
    {
        this.bonusCards = bonusCards;
    }
    
    public BonusCards getBonusCards()
    {
        return bonusCards;
    }
    
    public void setMissionItem(MissionItem missionItem)
    {
        this.missionItem = missionItem;
    }
    
    public MissionItem getMissionItem()
    {
        return missionItem;
    }
    
    public boolean isMissionAvailable()
    {
        return missionItem.isMissionAvailable();
    }
    
    public boolean isMissionCard(int cardCode)
    {
        return missionItem.isMissionCard(cardCode);
    }
    
    public boolean isRuleAvailable(GoStopRule rule)
    {
        boolean available = true;
        PlayerStatus taken = null;
        
        for (PlayerStatus ps: playersStatus.values())
        {
            if (available)
            {
                for (int cardCode: rule.getRuleCards())
                {
                    if (available && ps.isCardTaken(cardCode, false))
                    {
                        if (taken == null)
                            taken = ps;
                        else if (taken != ps)
                            available = false;
                    }
                }
            }
        }
        
        return available;
    }
    
    public boolean isKingAvailable(int majorCode)
    {
        int kingCode = CardItem.getCardCode(majorCode, 'a');
        int index = CardItem.getCardIndex(kingCode);
        
        return GameRule.getClardClass(index) == CardClass.KING
               && isCardTaken(kingCode, false) == false;
    }
    
    public boolean isDoubleLeafAvailable(int majorCode)
    {
        boolean available = false;
        
        for (int i = 'a'; i <= 'd' && available == false; i++)
        {
            int cc = CardItem.getCardCode(majorCode, i);
            
            if (isCardTaken(cc, false) == false)
            {
                CardClass cClass = GameRule.getClardClass(CardItem.getCardIndex(cc));
                
                available = (cClass == CardClass.LEAF && GameRule.getLeafPoints(cc) > 1)
                            || cClass == CardClass.TEN_LEAF;
            }
        }
        
        return available;
    }
    
    public int[] getUntakenCardCodes(int cardCode)
    {
        ArrayList<Integer> untaken = new ArrayList<Integer>();
        
        int majorCode = CardItem.getMajorCode(cardCode);
        for (int i = 'a'; i <= 'd'; i++)
        {
            int cc = CardItem.getCardCode(majorCode, i);
            
            if (cc == cardCode)
                untaken.add(cc);
            else
            {
                boolean taken = false;
                
                for (PlayerStatus ps: playersStatus.values())
                    taken = taken || ps.isCardTaken(cc, false);
                
                if (taken == false)
                    untaken.add(cc);
            }
        }
        
        int[] result = new int[untaken.size()];
        for (int i = 0; i < untaken.size(); i++)
            result[i] = untaken.get(i);

        return result;
    }

    /**
     * 카드가 놓일 장소에 대해서 각 포인트를 잡아서 순서대로 저장해둠
     * @param points
     * @param maxOverlapWidth
     */
    protected void resetTableCardPoints(Point[] points, int maxOverlapWidth)
    {
        tableCardPoints = new TableCardPoint[points.length];
        
        for (int i = 0; i < points.length; i++)
            tableCardPoints[i] = new TableCardPoint(points[i], null);
        
        TableCardPoint.maxOverlapWidth = maxOverlapWidth;
    }
    
    protected void clearTableCards()
    {
        for (TableCardPoint oc: tableCardPoints)
            oc.removeCardItems();
        
        curTurn = 0;
        goPlayers = null;
    }
    
    protected void disableTableCardsClick()
    {
        for (TableCardPoint oc: tableCardPoints)
        {
            if (oc.isEmpty() == false)
            {
                for (CardItem cardItem: oc.getCardItems())
                    cardItem.setCanClick(false);
            }
        }
    }

    /**
     * 패가 네장 놓이게 되는 카드 포인트를 리턴한다. 없다면, null을 리턴
     * @return
     */
    protected Point getTableFourCardPoint()
    {
        Point fourCardPoint = null;

        //12개의 포인트에 대해서 루프를 돈다.
        for (int i = 0; i < tableCardPoints.length; i++)
        {
            //카드 놓일 곳이 비지 않았따면...
            if (tableCardPoints[i].isEmpty() == false)
            {
                //다음 카드 포인트에 대해서 로프를 돈다.
                for (int k = i+1; k < tableCardPoints.length; k++)
                {
                    //현재 카드와 이후에 놓여져있는 카드간에 같은 카드가 있다면...
                    if (tableCardPoints[i].getMajorCode() == tableCardPoints[k].getMajorCode())
                    {
                        //현재 카드 포인트에, 이후 카드 포인트에 있는 카드를 더하고, 이후 카드 포인트의 카드를 지운다.
                        tableCardPoints[i].addCardItems(tableCardPoints[k].getCardItems());
                        tableCardPoints[k].removeCardItems();
                        
                        if (tableCardPoints[i].getCardItems().size() == 4)
                            fourCardPoint = tableCardPoints[i].getPoint(); 
                    }
                }
            }
        }
        
        return fourCardPoint;
    }
    
    public TableCardPoint getTableCardPoint(int majorCode)
    {
        return getTableCardPoint(majorCode, true);
    }

    /**
     * 족보 카드 코드를 이용하여, 테이블에 존재하는 카드 위치를 얻는다.
     *
     * @param majorCode
     * @param assignEmpty
     * @return
     */
    public TableCardPoint getTableCardPoint(int majorCode, boolean assignEmpty)
    {
        TableCardPoint emptyPoint = null;
        TableCardPoint tcp = null;
        
        for (int i = 0; i < tableCardPoints.length && tcp == null; i++)
        {
            //언제 majorCode가 0보다 작아지는지 모르게따....
            if (majorCode < 0 && tableCardPoints[i].isEmpty())
            {
                tcp = tableCardPoints[i];
                tcp.setMajorCode(0);
            }
            //majorCode>0 뭔가 카드 코드가 있음.
            //현재 포인트의 카드가 바로 그 카드!!!
            else if (majorCode > 0 && tableCardPoints[i].getMajorCode() == majorCode)
                tcp = tableCardPoints[i];
            //empty point가 아직 할당 되어 짔찌 않고... 현재의 포인트가 empy이면 할당함...
            else if (emptyPoint == null && tableCardPoints[i].isEmpty())
                emptyPoint = tableCardPoints[i];
        }
        
        if (tcp == null && assignEmpty)
        {
            tcp = emptyPoint;
            tcp.setMajorCode(majorCode);
        }
        
        return tcp;
    }
    
    public final TableCardPoint[] getTableCardPoints()
    {
        return tableCardPoints;
    }
    
    public CardItem getTopDeckCard(boolean remove)
    {
        return cardDeck.getTopDeckCard(remove);
    }
    
    public void setCanClickTopCard(boolean canClick)
    {
        cardDeck.setCanClickTopCard(canClick);
    }
    
    public boolean isCardTaken(int cardCode, boolean majorCode)
    {
        boolean taken = false;
        Iterator<PlayerStatus> itPS = playersStatus.values().iterator();
        
        while (taken == false && itPS.hasNext())
            taken = itPS.next().isCardTaken(cardCode, majorCode);
        
        return taken;
    }
    
    public List<CardItem> getOtherLeaves(GamePlayer gamePlayer, int count)
    {
        List<CardItem> result = new ArrayList<CardItem>();
        GameUser gameUser = gamePlayer.getGameUser();
        
        for (Map.Entry<GameUser,PlayerStatus> entry: playersStatus.entrySet())
        {
            if (gameUser.getLoginId().equals(entry.getKey().getLoginId()) == false)
            {
                List<CardItem> leaves = new ArrayList<CardItem>();
                List<CardItem> takenLeaves = entry.getValue().getTakenCards(CardClass.LEAF);
                int minPoints = Integer.MAX_VALUE;
                int minIndex = -1;
                int ptcount = count;
                
                for (int i = takenLeaves.size()-1; i >= 0 && ptcount > 0; i--)
                {
                    int points = GameRule.getLeafPoints(takenLeaves.get(i).getCardCode());
                    if (points <= ptcount)
                    {
                        ptcount -= points;
                        leaves.add(takenLeaves.get(i));
                    }
                    else if (minPoints > points)
                    {
                        minPoints = points;
                        minIndex = i;
                    }
                }
                
                if (ptcount > 0 && minIndex >= 0)
                {
                    CardItem cardItem = takenLeaves.get(minIndex);
                    int points = GameRule.getLeafPoints(cardItem.getCardCode()) - ptcount;
                    
                    for (int i = 0; i < leaves.size() && points > 0; )
                    {
                        int p = GameRule.getLeafPoints(leaves.get(i).getCardCode());
                        if (p <= points)
                        {
                            leaves.remove(i);
                            points -= p;
                        }
                        else
                            i++;
                    }
                    
                    leaves.add(cardItem);
                }
                else if (ptcount > 0 && entry.getValue().isTakenNineUndecided())
                    entry.getValue().setNineDecided(false);
                
                result.addAll(leaves);
            }
        }
        
        return result;
    }
    
    public boolean canGetOtherLeaves(GamePlayer gamePlayer)
    {
        boolean canGet = true;
        GameUser gameUser = gamePlayer.getGameUser();
        
        for (Map.Entry<GameUser,PlayerStatus> entry: playersStatus.entrySet())
        {
            if (canGet && gameUser.getLoginId().equals(entry.getKey().getLoginId()) == false)
            {
                List<CardItem> takenLeaves = entry.getValue().getTakenCards(CardClass.LEAF);
                
                canGet = takenLeaves.size() > 0;
            }
        }
        
        return canGet;
    }
    
    public void removeTakenLeaf(GamePlayer gamePlayer, CardItem cardItem)
    {
        GameUser gameUser = gamePlayer.getGameUser();
        
        for (Map.Entry<GameUser,PlayerStatus> entry: playersStatus.entrySet())
        {
            if (gameUser.getLoginId().equals(entry.getKey().getLoginId()) == false)
                entry.getValue().removeLeaf(cardItem);
        }
    }
    
    public boolean isTableEmpty(TableCardPoint[] excludes)
    {
        boolean empty = true;
        
        for (int i = 0; i < tableCardPoints.length && empty == true; i++)
        {
            boolean exclude = false;
            for (int k = 0; k < excludes.length && exclude == false; k++)
            {
                if (excludes[k] == tableCardPoints[i])
                    exclude = true;
            }
            
            empty = exclude || tableCardPoints[i].getCardCount(false) == 0;
        }
        
        return empty;
    }
    
    public Integer[] getSwampedCardsOnTable()
    {
        ArrayList<Integer> swampedCards = new ArrayList<Integer>(2);
        
        for (TableCardPoint tcp: tableCardPoints)
        {
            if (tcp.getCardCount(true) == 3)
                swampedCards.add(tcp.getMajorCode());
        }
        
        return swampedCards.size() == 0 ?
                        null : swampedCards.toArray(new Integer[swampedCards.size()]);
    }
    
    protected void addPlayer(GamePlayer gamePlayer)
    {
        playersStatus.put(gamePlayer.getGameUser(), gamePlayer.getPlayerStatus());
    }
    
    public PlayerStatus getPlayerStatus(GamePlayer player)
    {
        PlayerStatus pStatus = null;
        String loginId = player.getGameUser().getLoginId();
        
        for (Map.Entry<GameUser,PlayerStatus> entry: playersStatus.entrySet())
        {
            if (loginId.equals(entry.getKey().getLoginId()))
            {
                pStatus = entry.getValue();
                break;
            }
        }
        
        return pStatus;
    }
    
    public List<PlayerStatus> getOtherPlayerStatus(GamePlayer... exceptPlayers)
    {
        List<PlayerStatus> pStatus = new ArrayList<PlayerStatus>(2);
        List<String> exceptLoginIds = new ArrayList<String>(2);
        
        for (GamePlayer exceptPlayer: exceptPlayers)
            exceptLoginIds.add(exceptPlayer.getGameUser().getLoginId());
        
        for (Map.Entry<GameUser,PlayerStatus> entry: playersStatus.entrySet())
        {
            if (exceptLoginIds.contains(entry.getKey().getLoginId()) == false)
                pStatus.add(entry.getValue());
        }
        
        return pStatus;
    }
    
    public int getCurTurn()
    {
        return curTurn;
    }
    
    public void addCurTurn()
    {
        curTurn++;
    }
    
    public boolean isFirstTurn()
    {
        return curTurn == 1;
    }
    
    public boolean isLastTurn()
    {
        return curTurn == gameType.getMaxTurns();
    }
    
    public boolean isGameOver()
    {
        return curTurn > gameType.getMaxTurns();
    }
    
    public void takePoints(GamePlayer gamePlayer, int points)
    {
        int takenPoints = 0;
        GameUser gameUser = gamePlayer.getGameUser();
        
        for (GameUser gu: playersStatus.keySet())
        {
            if (gameUser.getLoginId().equals(gu.getLoginId()) == false)
            {
                gu.updateMoney(-points*gameType.getGameMoney(), false, WinType.NONE);
                takenPoints += points;
            }
        }
        
        // Let's serialize the points after this game :)
        gameUser.updateMoney(takenPoints*gameType.getGameMoney(), false, WinType.NONE);
    }
    
    public void refreshOtherPlayerStatus(GamePlayer activePlayer)
    {
        GameUser activeUser = activePlayer.getGameUser();

        for (Map.Entry<GameUser,PlayerStatus> entry: playersStatus.entrySet())
        {
            GameUser gameUser = entry.getKey();
            PlayerStatus playerStatus = entry.getValue();
            
            if (activeUser.getLoginId().equals(gameUser.getLoginId()))
            {
                playerStatus.setPenalty(GamePenalty.KING, false);
                playerStatus.setPenalty(GamePenalty.TEN, false);
                playerStatus.setPenalty(GamePenalty.LEAF, false);
            }
            else
            {
                playerStatus.refreshCardPoints();
                
                boolean penalty = getPlayerCards(activeUser, CardClass.KING).size() >= 3
                                  && playerStatus.getTakenCards(CardClass.KING).size() == 0;
                playerStatus.setPenalty(GamePenalty.KING, penalty);
                
                penalty = getPlayerCards(activeUser, CardClass.TEN).size() >= 5
                          && playerStatus.getTakenCards(CardClass.TEN).size() == 0;
                playerStatus.setPenalty(GamePenalty.TEN, penalty);

                if (getLeafPoints(activeUser) > 0)
                {
                    int leafCount = getLeafCount(gameUser);
                    if (leafCount <= getGameType().getLeafPenalty() && leafCount > 0)
                    {
                        if (playerStatus.isTakenNineUndecided()
                            && leafCount + 2 > getGameType().getLeafPenalty()
                            && (playerStatus.getTakenCards(CardClass.TEN).size() > 1
                                || getPlayerCards(activeUser, CardClass.TEN).size() < 5))
                            penalty = false;
                        else
                            penalty = true;
                    }
                    else
                        penalty = false;
                }
                else
                    penalty = false;
                playerStatus.setPenalty(GamePenalty.LEAF, penalty);
            }
        }
    }
    
    public List<GamePenalty> getPenalties(GamePlayer winner, GamePlayer gamePlayer)
    {
        return getPenalties(winner.getGameUser(), gamePlayer.getGameUser(), true);
    }
    
    public int getCurrentWinMoney(GamePlayer activePlayer)
    {
        int winnerPoints = 0;
        int gamePoints = activePlayer.getGamePoints();
        GameUser activeUser = activePlayer.getGameUser();

        for (Map.Entry<GameUser,PlayerStatus> entry: playersStatus.entrySet())
        {
            GameUser gameUser = entry.getKey();
            
            if (activeUser.getLoginId().equals(gameUser.getLoginId()) == false)
            {                
                List<GamePenalty> penalties = getPenalties(activeUser, gameUser, false);

                if (entry.getValue().hasNoTakenCards() == false)
                    winnerPoints += gamePoints * (int)Math.pow(2, penalties.size());
            }
        }
        
        return winnerPoints * gameType.getGameMoney();
    }
    
    public void addGoPlayer(GamePlayer goPlayer)
    {
        if (goPlayers == null)
            goPlayers = new ArrayList<GamePlayer>(2);

        goPlayers.remove(goPlayer);
        goPlayers.add(0, goPlayer);
    }
    
    public List<GamePlayer> getGoPlayers()
    {
        return goPlayers;
    }
    
    public int checkNewMission(GamePlayer gamePlayer)
    {
        PlayerStatus playerStatus = playersStatus.get(gamePlayer.getGameUser());
        
        return missionItem.checkNewMission(playerStatus);
    }
    
    public void addMissionBonus(GamePlayer gamePlayer, int missionBonus)
    {
        PlayerStatus playerStatus = playersStatus.get(gamePlayer.getGameUser());
        
        playerStatus.addMissionBonus(missionBonus);

        // Mission Bonus
        GameUser gameUser = gamePlayer.getGameUser();
        gameUser.updateMoney(missionBonus*gameType.getGameMoney(), false, WinType.NONE);
    }
    
    private List<GamePenalty> getPenalties(GameUser winUser, GameUser gameUser,
                                           boolean decideNine)
    {
        List<GamePenalty> penalties = new ArrayList<GamePenalty>();
        PlayerStatus playerStatus = playersStatus.get(gameUser);

        if (playerStatus.hasNoTakenCards() == false)
        {
            if (getPlayerCards(winUser, CardClass.KING).size() >= 3
                && getPlayerCards(gameUser, CardClass.KING).size() == 0)
                penalties.add(GamePenalty.KING);
            if (getPlayerCards(winUser, CardClass.TEN).size() >= 5
                && getPlayerCards(gameUser, CardClass.TEN).size() == 0)
                penalties.add(GamePenalty.TEN);
            if (getLeafPoints(winUser) > 0)
            {
                int leafCount = getLeafCount(gameUser);
                if (leafCount <= getGameType().getLeafPenalty() && leafCount > 0)
                {
                    if (playersStatus.get(gameUser).isTakenNineUndecided() == false
                        || leafCount + 2 <= getGameType().getLeafPenalty()
                        || (getPlayerCards(gameUser, CardClass.TEN).size() == 1
                            && getPlayerCards(winUser, CardClass.TEN).size() >= 5))
                        penalties.add(GamePenalty.LEAF);
                    else if (decideNine)
                        playerStatus.setNineDecided(true);
                }
            }
        }
        
        return penalties;
    }
    
    protected List<CardItem> getPlayerCards(GameUser gameUser, CardClass cardClass)
    {
        List<CardItem> cardItems = null;
        PlayerStatus playerStatus = playersStatus.get(gameUser);
        
        if (playerStatus != null)
            cardItems = playerStatus.getTakenCards(cardClass);
        
        return cardItems;
    }
    
    protected int getCardPoints(GameUser gameUser)
    {
        PlayerStatus ps = playersStatus.get(gameUser);
        int points = ps.getCardPoints() * (int)Math.pow(2, ps.getBonusCount());
        int missionBonus = ps.getMissionBonus();
        
        if (missionBonus > 0)
            points *= missionBonus;
        
        return points;
    }
    
    protected int getLeafCount(GameUser gameUser)
    {
        PlayerStatus playerStatus = playersStatus.get(gameUser);
        
        return GameRule.getLeafCount(playerStatus.getTakenCards(CardClass.LEAF));
    }
    
    protected int getLeafPoints(GameUser gameUser)
    {
        PlayerStatus playerStatus = playersStatus.get(gameUser);
        
        return playerStatus == null ? 0 : playerStatus.getCardPoints(CardClass.LEAF);
    }
    
    protected List<CardItem> getTableBonusCards()
    {
        List<CardItem> bonusCards = null;
        
        for (int i = 0; i < tableCardPoints.length; i++)
        {
            if (tableCardPoints[i].isBonusCard())
            {
                if (bonusCards == null)
                    bonusCards = tableCardPoints[i].removeCardItems();
                else
                    bonusCards.addAll(tableCardPoints[i].removeCardItems());
            }
        }
        
        return bonusCards;
    }
}
