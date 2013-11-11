package org.gs.game.gostop;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.gs.game.gostop.action.*;
import org.gs.game.gostop.config.*;
import org.gs.game.gostop.dlg.GameQueryDlg;
import org.gs.game.gostop.dlg.PlayerRankingDlg;
import org.gs.game.gostop.dlg.SetupUserDlg;
import org.gs.game.gostop.event.*;
import org.gs.game.gostop.item.*;
import org.gs.game.gostop.sound.GameSoundManager;
//Panel where to play game. display cards and 3 persons.

/**
 * GamePanel에서관리하는것들
 * 자식 GameItem을 ZOrder에 따라 관리 (addChildItem)
 * 다시 그려야 할 자식 GameItem을 ZOrder에 따라 그려줌 (paintComponent)
 * 마우스이벤트를 받아 해당 GameItem에 전달 (initEventListeners, onMouseClicked, onMouseExited, onMouseMoved)
 * 게임 참여자의 관리 (setGamePlayers)
 * 카드 이미지의 초기화 및 보너스카드 설정 (getImage, initCards)
 * 게임 참여자 영역 및 테이블 영역 관리 (initGame, recalcTableCardPoints)
 * ActionManager, GameManager 등 초기화 및 관리
 * GameEvent 처리 (onGameEvent)
 */
public class GamePanel extends JPanel implements IGameEventListener, ActionListener
{
    private static final long serialVersionUID = -2758783194195314947L;
    
    public static final int ANIMATION_ZORDER = 100;     // higher
    public static final int CARD_ZORDER = 600;          // 101 ~ 600
    public static final int LABEL_ZORDER = 800;
    public static final int BACKGROUND_ZORDER = 900;    // lower
    
    public static final int ACTION_TIME_UNIT = 50;      // milliseconds
    public static final int TIME_UNITS_PER_SECOND = 1000 / ACTION_TIME_UNIT;
    
    private static final String PICK_LEAD_LBL = "mission.pick.lead";
    private static final String PICK_LEAD2_LBL = "mission.pick.lead2";
    private static final String GAME_MUTE_SOUND_LBL = "game.muteSound";
    
    public static final String MENU_USER_INFO = "MENU_USER_INFO";
    public static final String MENU_PLAYER_RANKING = "MENU_PLAYER_RANKING";
    public static final String MENU_MUTE_SOUND = "MENU_MUTE_SOUND";
    
    private GameConfig gameConfig;
    private Image bgImage;
    private boolean bgLoaded;
    private int imageCount;
    private volatile int loadedImages;

    private Rectangle tableRect;
    private List<GameItem> childItems;
    private List<CardItem> cardItems;
    
    private MissionItem missionItem;
    private AlertItem alertItem;
    private GameItem mouseOverItem;
    private GameItem popupItem;
    
    private GameTable gameTable;
    private ActionManager actionManager;
    private GameManager gameManager;

    private List<IGamePanelListener> gamePanelListeners;
    
    public GamePanel(GameConfig gameConfig)
    {
        setLayout(null);    // Without a Layout Manager (i.e. Absolute Positioning)
        
        this.gameConfig = gameConfig;
        //ActionManager는.화면상에서.진행될.액션을.처리해주는.관리자인데,처리인터벌을.50으로하여.생성해준다.
        actionManager = new ActionManager(ACTION_TIME_UNIT);
        //게임의진행을담당하는.매니저를.생성한다.
        gameManager = new GameManager(this);
        //
        gameTable = new GameTable(gameManager);
        gamePanelListeners = null;


        //GameType을 첫번째걸로 세팅한다. 하위메소드를호출하여, 화면크기등도재배치한다.
        setGameType(GameType.getGameTypes().get(0));
        setPreferredSize(getGameLayout().getBackgroundSize());
        
        bgLoaded = false;
        loadedImages = 0;
        imageCount = 0;

        //배경이미지를 설정합니다.
        bgImage = getImage(getGameLayout().getBackgroundImageURL(), null);
        getImage(gameConfig.getCardImageURL(CardSize.BIG), CardSize.BIG);
        getImage(gameConfig.getCardImageURL(CardSize.HOLD), CardSize.HOLD);
        getImage(gameConfig.getCardImageURL(CardSize.NORMAL), CardSize.NORMAL);
        getImage(gameConfig.getCardImageURL(CardSize.SMALL), CardSize.SMALL);
        
        childItems = null;
        cardItems = null;
        missionItem = null;
        mouseOverItem = null;
        popupItem = null;
        
        GameQueryDlg.setDefaultParent(this);
        
        initEventListeners();
    }
   /* 게임을초기화하고선을정한다.*/
    public void initGame(GameType gameType, BonusCards bonusCards, List<GameUser> gameUsers)
    {
        showShuffle(false, 0);
        
        childItems = null;
        cardItems = null;
        
        setGameType(gameType);
        setGamePlayers(gameUsers);
        initCards(bonusCards);
        pickFirstPlayer();
    }
    
    public void addGamePanelEventListener(IGamePanelListener gpl)
    {
        if (gamePanelListeners == null)
            gamePanelListeners = new ArrayList<IGamePanelListener>();
        
        gamePanelListeners.add(gpl);
    }
    
    public GameTable getGameTable()
    {
        return gameTable;
    }
    
    public ActionManager getActionManager()
    {
        return actionManager;
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y,
                               int width, int height)
    {
        boolean completed = (infoflags & (ALLBITS|ABORT)) != 0;

        if (completed)
        {
            bgLoaded = bgLoaded || img == bgImage;

            loadedImages++;

            if (loadedImages == imageCount)
            {
                repaint();  // for background repaint
                initCards(new BonusCards(0, 0, 0));
                showShuffle(true, Integer.MAX_VALUE);
            }
        }

        return !completed;
    }

    public void onGameEvent(GameEvent e)
    {
        //선뽑기종료후
        if (e.getEventType() == GameEventType.PICK_LEAD_COMPLETED)
            gameManager.onPickLeadComplete();
        //경고메시지 종료후? (ex: PICK_READ_COMPLETED -> popup alert dialog -> ALERT_COMPLETED
        else if (e.getEventType() == GameEventType.ALERT_COMPLETED)
            gameManager.onAlertComplete();
        //스택 쌓기가 완료되면, 호출될 이벤트
        else if (e.getEventType() == GameEventType.STACK_COMPLETED)
            gameManager.onStackComplete();
        //게임시작시의 패 돌리기가 모두끝나면호출되는이벤트이다.
        else if (e.getEventType() == GameEventType.DEAL_COMPLETED)
            gameManager.onDealComplete();
        else if (e.getEventType() == GameEventType.FOUR_CARDS_DECIDED)
            gameManager.onFourCardsDecided(e.getBoolResult());
        else if (e.getEventType() == GameEventType.FLIP_STACK_COMPLETED)
            gameManager.onFlipStackCompleted();
        else if (e.getEventType() == GameEventType.SWING_DECIDED)
            gameManager.onSwingDecided(e.getBoolResult(), e.getCardItemResult());
        else if (e.getEventType() == GameEventType.SELECTED_CARD_ON_TABLE)
            gameManager.onSelectedCardOnTable(e.getCardItemResult(), e.getIntResult());
        else if (e.getEventType() == GameEventType.NINE_DECIDED)
            gameManager.onNineDecided(e.getBoolResult());
        else if (e.getEventType() == GameEventType.PLAY_COMPLETED)
            gameManager.onPlayCompleted();
        else if (e.getEventType() == GameEventType.GO_DECIDED)
            gameManager.onGoDecided(e.getBoolResult(), e.getIntResult());
        else if (e.getEventType() == GameEventType.MORE_GAME_DECIDED)
        {
            if (e.getBoolResult())
                gameManager.onNewGame(e.getGamePlayerResult());
            else
                fireGamePanelEvent(e);
        }
        else if (e.getEventType() == GameEventType.ZORDER_CHANGED)
            onZOrderChanged((GameItem)e.getSourceObject());
        else if (e.getEventType() == GameEventType.ITEM_CLICKED)
        {
            if (e.getSourceObject() instanceof CardItem)
                gameManager.onCardClicked((CardItem)e.getSourceObject());
        }
    }

    private void fireGamePanelEvent(GameEvent ge)
    {
        if (gamePanelListeners != null)
        {
            for (IGamePanelListener gpl: gamePanelListeners)
            {
                if (ge.getEventType() == GameEventType.MORE_GAME_DECIDED
                    && ge.getBoolResult() == false)
                    gpl.gameStopped();
                if (ge.getEventType() == GameEventType.MENU_CLICKED)
                    gpl.onMenuSelected(ge.getSourceObject(), ge.getStringResult());
            }
        }
    }

    /**
     * 게임타입을.설정하고, 그에따라서, 화면의레이아웃, 폰트, 등을설정한다.
     * @param gameType
     */
    private void setGameType(GameType gameType)
    {
        gameTable.setGameType(gameType);
        
        GameLayout gameLayout = gameType.getGameLayout();
        
        recalcTableRect();
        //테이블 상에 카드가 놓여질 위치(바닥에 깔린 패)를 미리 계산해둔다. 최대 12개의 자리가 필요함.
        recalcTableCardPoints();
        
        TextFont mf = gameLayout.getTextFont(TextTarget.MISSION, false);
        TextFont mfi = gameLayout.getTextFont(TextTarget.MISSION_INFO, false);
        missionItem = new MissionItem(this, gameLayout.getMissionRect(),
                                      mf.getFont(), mf.getFontColor(),
                                      mfi.getFont(), mfi.getFontColor());
        addChildItem(missionItem, false);
        
        gameTable.setMissionItem(missionItem);
        
        TextFont af = gameLayout.getTextFont(TextTarget.ALERT, false);
        alertItem = new AlertItem(this, gameLayout.getAlertsize(), af);
        addChildItem(alertItem, false);
    }
    
    protected GameConfig getGameConfig()
    {
        return gameConfig;
    }
    
    protected List<CardItem> getCardItems()
    {
        return cardItems;
    }
    
    protected GameLayout getGameLayout()
    {
        return gameTable.getGameType().getGameLayout();
    }
    
    protected AlertItem getAlertItem()
    {
        return alertItem;
    }
    
    protected MissionItem getMissionItem()
    {
        return missionItem;
    }
    
    protected void repaintTable()
    {
        repaint(tableRect);
    }
    
    protected void paintComponent(Graphics g)
    {
        //super.paintComponent(g);

        if (bgLoaded)
        {
            Rectangle rc = g.getClipBounds();
            float rx = bgImage.getWidth(null) / (float)getWidth();
            float ry = bgImage.getHeight(null) / (float)getHeight();
            g.drawImage(bgImage, rc.x, rc.y, rc.x+rc.width, rc.y+rc.height,
                        (int)(rc.x*rx), (int)(rc.y*ry),
                        (int)((rc.x+rc.width)*rx),(int)((rc.y+rc.height)*ry), null);
        }

        Shape clip = g.getClip();
        Graphics g2 = g.create();
        
        try
        {
            if (childItems != null)
            {
                // drawing items with a same z-order by inserted order
                for (int i = childItems.size()-1; i >= 0; i--)
                {
                    GameItem gi = childItems.get(i);
                    
                    if (clip.intersects(gi.getRect()))
                        gi.paintItem(g2);
                }
            }
        }
        finally
        {
            g2.dispose();
        }
    }
    
    private void onZOrderChanged(GameItem item)
    {
        addChildItem(item, false);
    }
    
    private void initEventListeners()
    {
        mouseOverItem = null;
        
        MouseAdapter ma = new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                onMouseClicked(e);
            }

            public void mouseExited(MouseEvent e)
            {
                onMouseExited(e);
            }
            
            public void mouseMoved(MouseEvent e)
            {
                onMouseMoved(e);
            }
            
            public void mouseDragged(MouseEvent e)
            {
                onMouseMoved(e);
            }
            
            public void mouseReleased(MouseEvent e)
            {
                onMouseReleased(e);
            }
        };
        
        addMouseListener(ma);
        addMouseMotionListener(ma);
        GameEventManager.addGameEventListener(this);
    }
    
    private void onMouseClicked(MouseEvent e)
    {
        GameItem gi = getItemForPoint(e.getPoint());

        if (gi != null && gi.canClick())
            gi.mouseClicked(e);
    }
    
    private void onMouseExited(MouseEvent e)
    {
        if (mouseOverItem != null)
            mouseOverItem.mouseExited();
    }
    
    private void onMouseMoved(MouseEvent e)
    {
        GameItem gi = getItemForPoint(e.getPoint());

        if (mouseOverItem != null && mouseOverItem != gi)
            mouseOverItem.mouseExited();
        
        if (gi != null && gi.canClick())
            gi.mouseMoved(e);
        
        mouseOverItem = gi;
    }
    
    private void onMouseReleased(MouseEvent e)
    {
        if (e.isPopupTrigger())
            showPopupMenu(e);
    }
    
    protected void showShuffle(boolean show, int duration)
    {
        if (show)
        {
            List<GameAction> actions = new ArrayList<GameAction>();
            
            synchronized (cardItems)
            {
                for (CardItem card: cardItems)
                {
                    card.setFlipped(true);
                    card.setZOrder(CARD_ZORDER);
                    actions.add(new RandomMoveAction(card, duration, tableRect));
                }
            }
            
            actionManager.addItems(actions);
        }
        else
        {
            actionManager.abortAllActions();
            synchronized (cardItems)
            {
                for (CardItem card: cardItems)
                    card.setFlipped(false);
            }
            repaintTable();
        }
    }
    
    private void pickFirstPlayer()
    {
        List<Integer> indexes = new ArrayList<Integer>();
        int index;
        CardItem ci;
        
        for (int i = 0; i < 12; i++)
        {
            do
            {
                index = Main.getRandom().nextInt(12);
            } while (indexes.contains(Integer.valueOf(index)));
            indexes.add(Integer.valueOf(index));
            
            // king or ten cards only
            gameManager.addTableCardForPickLead(cardItems.get(index*4));
        }
        
        List<Integer> cardStack = new ArrayList<Integer>();
        for (int i = 0; i < cardItems.size(); i++)
        {
            ci = cardItems.get(i);
            if (i >= 12*4 || ci.getMinorCode() != 'a')
            {
                cardStack.add(Integer.valueOf(i));
                ci.setFlipped(false);
                ci.setCanClick(false);
            }
        }
        
        setCardStack(cardStack, null);
        
        missionItem.setMessage(PICK_LEAD_LBL, PICK_LEAD2_LBL);
        gameManager.setPlayStep(GameManager.PlayStep.PICK_LEAD);
        
        GameSoundManager.playSound(GameSoundManager.SOUND_PULSE, null);
        
        repaintTable();
    }
    
    private void recalcTableRect()
    {
        Rectangle tblRect = new Rectangle();
        List<Rectangle> excludes = new ArrayList<Rectangle>();
        
        tblRect.setSize(getGameLayout().getBackgroundSize());
        
        excludes.add(getGameLayout().getMissionRect());
        for (PlayerLayout pl: getGameLayout().getPlayerLayouts())
            excludes.add(pl.getRect());
        
        for (Rectangle rect: excludes)
        {   // select the largest rectangle excluding the given rectangle
            if (tblRect.intersects(rect))
            {
                Rectangle isect = tblRect.intersection(rect);
                Rectangle maxsub = new Rectangle(tblRect);
                int maxarea = (isect.y - tblRect.y) * tblRect.width;
                maxsub.height = isect.y - tblRect.y;

                int area = (isect.x-tblRect.x) * tblRect.height;
                if (area > maxarea)
                {
                    maxsub = new Rectangle(tblRect);
                    maxsub.width = isect.x-tblRect.x;
                    maxarea = area;
                }
                
                area = (tblRect.height-isect.height-(isect.y-tblRect.y)) * tblRect.width;
                if (area > maxarea)
                {
                    maxsub = new Rectangle(tblRect);
                    maxsub.height = tblRect.height - isect.height - (isect.y - tblRect.y);
                    maxsub.y = isect.y + isect.height;
                    maxarea = area;
                }
                
                area = (tblRect.width-isect.width-(isect.x-tblRect.x)) * tblRect.height;
                if (area > maxarea)
                {
                    maxsub = new Rectangle(tblRect);
                    maxsub.width = tblRect.width - isect.width - (isect.x - tblRect.x);
                    maxsub.x = isect.x + isect.width;
                    maxarea = area;
                }
                
                tblRect = maxsub;
            }
        }
        
        tableRect = tblRect;
    }
    
    private void setGamePlayers(List<GameUser> gameUsers)
    {
        int opposite = 0;
        
        gameManager.resetGamePlayers();
        
        for (PlayerLayout playerLayout: getGameLayout().getPlayerLayouts())
        {
            GameUser gameUser;
            
            if (playerLayout.isOpposite())
                gameUser = gameUsers.get(opposite++);
            else
                gameUser = MainFrame.getGameUser();
            
            gameManager.addGamePlayer(new GamePlayer(gameUser, this, playerLayout));
        }
        
        repaint();
    }
    //카드를초기화한다.
    private void initCards(BonusCards bonusCards)
    {
        CardItem card;
        Dimension size = gameConfig.getCardSize(CardSize.NORMAL);
        Rectangle rect = new Rectangle(getGameLayout().getStackRect());
        int zorder = CARD_ZORDER;
        
        rect.setSize(size);
        
        for (int major = 1; major <= 12; major++)
        {
            for (int minor = 'a'; minor <= 'd'; minor++)
            {
                card = new CardItem(this, rect, major, minor, false, zorder--);
                addChildItem(card, true);
            }
        }
        
        if (bonusCards.getTripleCount() > 0)
        {
            card = new CardItem(this, rect, 13, 'a', false, zorder--);
            addChildItem(card, true);
        }

        for (byte minor = 'b'; minor < 'b' + bonusCards.getDoubleCount(); minor++)
        {
            card = new CardItem(this, rect, 13, minor, false, zorder--);
            addChildItem(card, true);
        }
        
        if (bonusCards.getKingCount() > 0)
        {
            card = new CardItem(this, rect, 14, 'a', false, zorder--);
            addChildItem(card, true);
        }
        
        gameTable.setBonusCards(bonusCards);
    }
    
    protected void setCardStack(List<Integer> cardStack, GameEventType callbackEventType)
    {
        Rectangle sr = getGameLayout().getStackRect();
        Dimension size = gameConfig.getCardSize(CardSize.NORMAL);
        Point start = new Point(sr.x, sr.y+sr.height-size.height);
        Point end = new Point(sr.x+sr.width-size.width, sr.y);
        List<GameAction> actions = new ArrayList<GameAction>();
        CardItem cardItem;
        float ratio;
        Point point;
        int duration = 5;
        int zorder = CARD_ZORDER;

        //스택으로쌓을카드들의 포지션을 조금씩엇나가게 쌓아준다. 많이쌓일때와 조금쌓일때가구분이가도록....
        for (int i = 0; i < cardStack.size(); i++)
        {
            cardItem = cardItems.get(cardStack.get(i));
            ratio = i / (float)cardStack.size();
            point = new Point((int)(start.x + (end.x-start.x)*ratio),
                              (int)(start.y + (end.y-start.y)*ratio));
            actions.add(new MoveAction(cardItem, duration, point));
            cardItem.setZOrder(zorder--);
        }

        //맨마지막액션뒤에다가콜백이벤트를걸어준다.
        //이액션은각카드에걸려있는액션을말한다.
        actions.get(actions.size()-1).setCompleteEventType(callbackEventType);
        actionManager.addItems(actions);
    }
    
    public GameItem getItemForPoint(Point point)
    {
        GameItem item = null;
        
        if (childItems != null)
        {
            for (int i = 0; i < childItems.size() && item == null; i++)
            {
                if (childItems.get(i).getRect().contains(point))
                    item = childItems.get(i);
            }
        }
        
        return item;
    }
    
    private synchronized void addChildItem(GameItem item, boolean addToCards)
    {
        if (childItems == null)
            childItems = new ArrayList<GameItem>();
     
        childItems.remove(item);
        
        int i = 0;
        while (i < childItems.size() && item.getZOrder() > childItems.get(i).getZOrder())
            i++;
        
        childItems.add(i, item);
        
        if (addToCards && item instanceof CardItem)
        {
            if (cardItems == null)
                cardItems = new ArrayList<CardItem>();
            
            synchronized (cardItems)
            {
                cardItems.add((CardItem)item);
            }
        }
        
        item.repaint();
    }

    /**
     * 테이블상의 카드의 포인트를 재계산한다.
     * 카드 포인트란 테이블상에 카드가 놓여질 위치를 말한다.
     * 이론상 최대 12장의 카드가 놓일수 있으니 그 위치를 계산해주는 것.
     */
    private void recalcTableCardPoints()
    {
        /* 8 2 0 4 a
         *   6 q 7
         * 9 3 1 5 b
         */
        //노말카드의너비와높이를구해준다.
        Dimension size = gameConfig.getCardSize(CardSize.NORMAL);
        //xoff와 yoff의 정확한 의미는 잘 모르겠음.
        int xoff = (tableRect.width - size.width * 5)/6;
        int yoff = (tableRect.height - size.height * 3)/4;


        //카드 포인트들 12개, 이건 뭘까요?
        Point[] cardPoints = new Point[12];
        
        cardPoints[0] = new Point(xoff*3 + size.width*2, yoff);
        cardPoints[1] = new Point(xoff*3 + size.width*2, yoff*3 + size.height*2);
        cardPoints[2] = new Point(xoff*2 + size.width, yoff);
        cardPoints[3] = new Point(xoff*2 + size.width, yoff*3 + size.height*2);
        cardPoints[4] = new Point(xoff*4 + size.width*3, yoff);
        cardPoints[5] = new Point(xoff*4 + size.width*3, yoff*3 + size.height*2);
        cardPoints[6] = new Point(xoff*2 + size.width, yoff*2 + size.height);
        cardPoints[7] = new Point(xoff*4 + size.width*3, yoff*2 + size.height);
        cardPoints[8] = new Point(xoff, yoff);
        cardPoints[9] = new Point(xoff, yoff*3 + size.height*2);
        cardPoints[0xa] = new Point(xoff*5 + size.width*4, yoff);
        cardPoints[0xb] = new Point(xoff*5 + size.width*4, yoff*3 + size.height*2);
        
        for (Point pt: cardPoints)
            pt.translate(tableRect.x, tableRect.y);
     
        int maxOverlapWidth = size.width + (xoff > size.width ? size.width : xoff - 5);
        
        gameManager.resetFreeCardPoints(cardPoints, maxOverlapWidth);
    }
    
    private Image getImage(URL url, CardSize cardSize)
    {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.getImage(url);

        synchronized (this)
        {
            imageCount++;
        }
        
        toolkit.prepareImage(image, -1, -1, this);
        
        if (cardSize != null)
            CardItem.addCardImage(cardSize, gameConfig.getCardSize(cardSize), image);
        
        return image;
    }
    
    private void showPopupMenu(MouseEvent e)
    {
        popupItem = getItemForPoint(e.getPoint());
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;
        
        if (popupItem instanceof PlayerAvatarItem)
        {
            menuItem = new JMenuItem(Resource.getProperty(SetupUserDlg.USER_INFO_TITLE));
            menuItem.setActionCommand(MENU_USER_INFO);
            menuItem.addActionListener(this);
            popup.add(menuItem);
        }
        
        menuItem = new JMenuItem(Resource.getProperty(PlayerRankingDlg.PLAYER_RANKING_TITLE));
        menuItem.setActionCommand(MENU_PLAYER_RANKING);
        menuItem.addActionListener(this);
        popup.add(menuItem);
        
        JCheckBoxMenuItem cbm = new JCheckBoxMenuItem(Resource.getProperty(GAME_MUTE_SOUND_LBL));
        cbm.setActionCommand(MENU_MUTE_SOUND);
        cbm.addActionListener(this);
        cbm.setState(MainFrame.getGameUser().getMuteSound());
        popup.add(cbm);
        
        popup.show(this, e.getX(), e.getY());
    }
    
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() instanceof JCheckBoxMenuItem)
        {
            JCheckBoxMenuItem cbm = (JCheckBoxMenuItem)e.getSource();
            String actionCmd = cbm.getActionCommand();
            GameEvent ge = new GameEvent(cbm.getState(), GameEventType.MENU_CLICKED,
                                         actionCmd);
            fireGamePanelEvent(ge);
        }
        else if (e.getSource() instanceof JMenuItem)
        {
            String actionCmd = ((JMenuItem)e.getSource()).getActionCommand();
            GameEvent ge = new GameEvent(popupItem, GameEventType.MENU_CLICKED,
                                         actionCmd);
            fireGamePanelEvent(ge);
        }
    }
}
