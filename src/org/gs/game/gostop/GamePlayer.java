package org.gs.game.gostop;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;

import org.gs.game.gostop.action.*;
import org.gs.game.gostop.config.*;
import org.gs.game.gostop.config.GameUser.GameUserType;
import org.gs.game.gostop.event.*;
import org.gs.game.gostop.item.*;
import org.gs.game.gostop.play.PlayHandlerFactory;
import org.gs.game.gostop.play.GamePenalty;
import org.gs.game.gostop.play.IPlayHandler;

/**
 * 게임을 하는 참가자를 표현하며, 사용자 정보, 가지고 있는 패, 먹은 패, 플레이 핸들러 등의 정보를 포함하고 있습니다.
 */
public class GamePlayer
{
    private GamePanel gamePanel;
    private GameTable gameTable;
    private GameUser gameUser;
    private PlayerLayout playerLayout;
    private PlayerPaneItem playerPanel;
    private PlayerStatus playerStatus;
    private PlayerLabelItem playerLabel;
    private CardItem selectedCard;              // to pick the lead player
    private List<CardItem> holdCards;
    private List<String> ruleIds;
    private IPlayHandler playHandler;
    private boolean keepTurn;
    private boolean keepSwamping;
    private int flipCount;  // the number of count which the player can flip the deck cards
                            // without laying down in case of bombing
    
    protected GamePlayer(GameUser gameUser, GamePanel gamePanel, PlayerLayout playerLayout)
    {
        this.gamePanel = gamePanel;
        gameTable = gamePanel.getGameTable();
        this.gameUser = gameUser;
        this.playerLayout = playerLayout;
        playerStatus = new PlayerStatus(gamePanel, playerLayout);
        playerPanel = new PlayerPaneItem(gamePanel, playerLayout.getRect());
        
        createPlayerLabel();
        
        new PlayerAvatarItem(gamePanel, playerLayout.getAvatarRect(),
                             gameUser.getAvatarPath(), this);
        
        playHandler = PlayHandlerFactory.getPlayHandler(this, gameTable);
        
        initPlayer(false);
    }
    
    public GameUser getGameUser()
    {
        return gameUser;
    }
    
    public Point getAlertLocation()
    {
        return playerLayout.getAlertLocation();
    }
    
    public boolean isOpposite()
    {
        return playerLayout.isOpposite();
    }
    
    public boolean isComputerPlayer()
    {
        return GameUserType.COMPUTER.equals(gameUser.getUserType());
    }
    
    public void setSelectedCard(CardItem selectedCard)
    {
        this.selectedCard = selectedCard;
    }
    
    public CardItem getSelectedCard()
    {
        return selectedCard;
    }

    public void initPlayer(boolean leadPlayer)
    {
        playerLabel.setLeadPlayer(leadPlayer);
        holdCards = new ArrayList<CardItem>();
        playerStatus.initPlayerStatus();
        flipCount = 0;
        keepSwamping = false;
        ruleIds = null;
    }
    
    public boolean isLeadPlayer()
    {
        return playerLabel.isLeadPlayer();
    }
    
    public List<CardItem> getHoldCards()
    {
        return holdCards;
    }
    
    public Point addHoldCard(CardItem cardItem, boolean sortBeforeAdd)
    {
        if (sortBeforeAdd)
            arrangeHoldCards();
        
        Point point = getHoldCardPoint(holdCards.size());
        
        cardItem.setZOrder(GamePanel.CARD_ZORDER);
        holdCards.add(cardItem);

        // for DEBUGGING
        //cardItem.setFlipped(true);
        
        return point;
    }

    /**
     * 사용자가 들고있는 카드를 순서대로 소팅한다.
     */
    public void sortHoldCards()
    {
        CardItem[] cards = getSortedHoldCards();
        
        for (int i = 0; i < cards.length; i++)
            holdCards.set(i, cards[i]);
        
        arrangeHoldCards();
    }

    public boolean isHoldingCard(int cardCode)
    {
        boolean isHolding = false;
        
        if (holdCards != null)
        {
            for (int i = 0; i < holdCards.size() && isHolding == false; i++)
            {
                if (cardCode == holdCards.get(i).getCardCode())
                    isHolding = true;
            }
        }
        
        return isHolding;
    }
    
    public PlayerStatus getPlayerStatus()
    {
        return playerStatus;
    }
    
    public List<GamePenalty> getPenalties(GamePlayer winner)
    {
        return gameTable.getPenalties(winner, this);
    }
    
    public boolean hasNoTakenCards()
    {
        return playerStatus.hasNoTakenCards();
    }
    
    public int getGamePoints()
    {
        return gameTable.getCardPoints(gameUser);
    }
    
    public AlertItem getAlertItem()
    {
        return gamePanel.getAlertItem();
    }
    
    public boolean checkFourCards()
    {
        int major = getMajorCodeOfFourCards();
     
        if (major > 0)
            playHandler.decideGoOnFourCards();
        
        return major > 0;
    }
    
    public void addTripleCount()
    {
        playerStatus.addTripleCount();
    }
    
    public void addGoCount()
    {
        playerStatus.addGoCount();
    }
    
    public int getGoCount()
    {
        return playerStatus.getGoCount();
    }
    
    public Rectangle getTakenRect(CardClass cardClass)
    {
        return playerLayout.getTakenRect(cardClass);
    }
    
    public void addTakenCard(CardItem card)
    {
        playerStatus.addTakenCard(card, isOpposite());
    }

    /**
     * 현 플레이어의 상태를 Active하게 바꾸고 타 플레이어의 상태를 최신으로 업데이트 함.
     * 그리고, 카드 집기를 시작하자.
     */
    public void play()
    {
        playerPanel.setActive(true);
        gameTable.refreshOtherPlayerStatus(this);
        playHandler.pickCard();
    }
    
    public int getHoldCardCount(int majorCode)
    {
        int count = 0;
        
        for (CardItem card: holdCards)
        {
            if (card.getMajorCode() == majorCode)
                count++;
        }
        
        return count;
    }
    
    public List<CardItem> getHoldCards(int majorCode, boolean remove)
    {
        List<CardItem> cardItems = new ArrayList<CardItem>();
        
        for (CardItem card: holdCards)
        {
            if (card.getMajorCode() == majorCode)
                cardItems.add(card);
        }
        
        if (remove)
            holdCards.removeAll(cardItems);
        
        return cardItems;
    }
    
    public void removeHoldCard(CardItem cardItem)
    {
        holdCards.remove(cardItem);
    }
    
    public void disableCardClick()
    {
        if (isOpposite() == false)
        {
            // disable holding cards to be clicked
            for (CardItem cardItem: getHoldCards())
            {
                cardItem.setActiveStatus(CardItem.Status.NONE);
                cardItem.setCanClick(false);
            }
        }
    }
    
    public void onPostActive()
    {
        playHandler.onPostActive();
        playerPanel.setActive(false);
    }
    
    public void setKeepTurn(boolean keepTurn)
    {
        this.keepTurn = keepTurn;
    }
    
    public boolean getKeepTurn()
    {
        return keepTurn;
    }
    
    public void addFlipCount(int flipCount)
    {
        this.flipCount += flipCount;
    }
    
    public int getFlipCount()
    {
        return flipCount;
    }
    
    public void addSwampCount()
    {
        playerStatus.addSwampCount();
    }
    
    public int getSwampCount()
    {
        return playerStatus.getSwampCount();
    }
    
    public boolean keepSwamping()
    {
        return keepSwamping;
    }
    
    public void setKeepSwamping(boolean keepSwamping)
    {
        this.keepSwamping = keepSwamping;
    }
    
    // handle nineDecided
    public boolean checkNineDecide()
    {
        boolean needToDecide = false;
        
        if (playerStatus.needToDecideNine(gameTable.getGameType().getWinPoints()))
        {
            playHandler.decideNine();
            needToDecide = true;
        }
        
        return needToDecide;
    }
    
    public boolean calculatePoints(boolean queryGo)
    {
        boolean continuePlay = true;
        int prevPoints = playerStatus.getPrevPoints();
        int newPoints = playerStatus.refreshCardPoints();

        gameTable.refreshOtherPlayerStatus(this);
        
        if (queryGo
            && newPoints >= gameTable.getGameType().getWinPoints()
            && prevPoints < newPoints
            && playerStatus.getPrevLeafPoints() <= playerStatus.getCardPoints(CardClass.LEAF))
        {
            if (gameTable.isLastTurn())
            {
                GameEvent event = new GameEvent(this, GameEventType.GO_DECIDED,
                                                new GameEventResult(false));    // stop
                GameEventManager.fireGameEvent(event, false);
            }
            else
                playHandler.decideGo();
            
            continuePlay = false;
        }
        
        return continuePlay;
    }
    
    public void arrangeHoldCards()
    {
        for (int i = 0; i < holdCards.size(); i++)
            holdCards.get(i).moveItem(getHoldCardPoint(i));
    }
    
    public GamePanel getGamePanel()
    {
        return gamePanel;
    }
    
    public GameAction getSelectTableCardAction(TableCardPoint flipTcp)
    {
        return playHandler.getSelectTableCardAction(flipTcp);
    }
    
    public List<String> getRuleIds()
    {
        return ruleIds;
    }
    
    public void setRuleIds(List<String> ruleIds)
    {
        this.ruleIds = ruleIds;
    }
    
    public String getVoiceTypeId()
    {
        return gameUser.getUserVoiceId();
    }
    
    private void createPlayerLabel()
    {
        GameLayout gameLayout = gamePanel.getGameLayout();
        TextFont aliasFont;
        TextFont infoFont;
        
        if (isOpposite())
        {
            aliasFont = gameLayout.getTextFont(TextTarget.ALIAS, true);
            infoFont = gameLayout.getTextFont(TextTarget.INFO, true);
        }
        else
        {
            aliasFont = gameLayout.getTextFont(TextTarget.ALIAS, false);
            infoFont = gameLayout.getTextFont(TextTarget.INFO, false);
        }
        
        playerLabel = new PlayerLabelItem(gamePanel, playerLayout.getTextRect(), gameUser,
                                          aliasFont, infoFont);
    }
    
    private Point getHoldCardPoint(int index)
    {
        int[] cardsPerLine = gameTable.getGameType().getPlayerCards();
        Rectangle rectHold = playerLayout.getHoldRect();
        Dimension cardSize = getHoldCardSize();
        int indexInLine = index;
        int maxCardsPerLine = 0;
        int line = 0;
        
        for (int i = 0; i < cardsPerLine.length; i++)
        {
            if (i == line && indexInLine >= cardsPerLine[i])
            {
                indexInLine -= cardsPerLine[i];
                line++;
            }
            if (maxCardsPerLine < cardsPerLine[i])
                maxCardsPerLine = cardsPerLine[i];
        }

        int cardSpaceX = (rectHold.width-cardSize.width*maxCardsPerLine)/(maxCardsPerLine-1);
        int cardSpaceY = (rectHold.height-cardSize.height*cardsPerLine.length)/(cardsPerLine.length-1);
        
        return new Point(rectHold.x+indexInLine*(cardSize.width+cardSpaceX),
                         rectHold.y+line*(cardSize.height+cardSpaceY));
    }
    
    private Dimension getHoldCardSize()
    {
        Dimension size;
        
        if (isOpposite())
            size = gamePanel.getGameConfig().getCardSize(CardSize.SMALL);
        else
            size = gamePanel.getGameConfig().getCardSize(CardSize.HOLD);
        
        return size;
    }
    
    private CardItem[] getSortedHoldCards()
    {
        CardItem[] cards = holdCards.toArray(new CardItem[holdCards.size()]);
        Comparator<CardItem> comp = new Comparator<CardItem>()
        {
            public int compare(CardItem o1, CardItem o2)
            {
                return o1.getCardCode() - o2.getCardCode();
            }
        };
        
        Arrays.sort(cards, comp);
        
        return cards;
    }

    private int getMajorCodeOfFourCards()
    {
        int major = -1;
        boolean fourCards = false;
        CardItem[] cards = getSortedHoldCards();
        int count = 0;
        
        for (int i = 0; i < cards.length && fourCards == false
                        && cards[i].getMajorCode() < 13; i++)
        {
            if (cards[i].getMajorCode() == major)
            {
                if (++count == 4)
                    fourCards = true;
            }
            else
            {
                major = cards[i].getMajorCode();
                count = 1;
            }
        }
        
        return fourCards ? major : -1;
    }
}
