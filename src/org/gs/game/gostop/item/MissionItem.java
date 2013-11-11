package org.gs.game.gostop.item;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.gs.game.gostop.*;
import org.gs.game.gostop.config.GameMission;
import org.gs.game.gostop.config.PlayerSide;
import org.gs.game.gostop.config.GameMission.MissionType;
import org.gs.game.gostop.play.GameRule;

/**
 * 미션의 내용과 상태를 관리 및 표시해 주며, 프레이어의 미션상태가 변경되었는지, 미션을 완수했는지도 결정합니다.
 */
public class MissionItem extends GameItem
{
    private static final String MISSION_LBL = "mission.title";
    private static final String NO_MISSION_LBL = "mission.no.mission";
    private static final String[] COUNT_MISIONS_LBL =
    {
        "mission.count.tens", "mission.count.fives", "mission.count.leaves"
    };
    private static final int asize = 10;
    private static final int[][][] _arrows =
    {
        // PlayerSide.LEFT,
        { { asize, 0 }, { asize, asize }, { 0, asize/2 } },
        // PlayerSide.RIGHT,
        { { 0, 0 }, { 0, asize }, { asize, asize/2 } },
        // PlayerSide.BOTTOM,
        { { 0, 0 }, { asize, 0 }, { asize/2, asize } },
        // PlayerSide.TOP
        { { 0, asize }, { asize, asize }, { asize/2, 0 } },
    };
    
    private static final int TITLE_HEIGHT = 21;
    private static final int BOTTON_HEIGHT = 19;
    
    private Font titleFont;
    private Color titleColor;
    private Font infoFont;
    private Font missionFont;
    private Color infoColor;
    private String mainMsgId;
    private String subMsgId;
    private GameMission gameMission;
    private int missionBonus;
    private List<MissionCardItem> cardItems;
    private List<MissionLabelItem> labelItems;
    private boolean[] missionTaken;
    
    private static class MissionCardItem extends CardItem
    {
        private PlayerSide takenSide;
        
        private MissionCardItem(JComponent parent, Rectangle rect, int cardCode)
        {
            super(parent, rect,
                  CardItem.getMajorCode(cardCode), CardItem.getMinorCode(cardCode),
                  true, GamePanel.CARD_ZORDER);
            
            takenSide = null;
        }
        
        public void paintItem(Graphics g)
        {
            super.paintItem(g);
            
            if (takenSide != null)
                drawTakenArrow(g, rect, takenSide);
        }
        
        private PlayerSide getTakenSide()
        {
            return takenSide;
        }
        
        private void setTakenSide(PlayerSide takenSide)
        {
            if (this.takenSide != takenSide)
            {
                this.takenSide = takenSide;
                repaint();
            }
        }
    }
    
    private static class MissionLabelItem extends GameItem
    {
        private String label;
        private Color color;
        private Font font;
        private PlayerSide takenSide;
        
        private MissionLabelItem(JComponent parent, Rectangle rect,
                                 String label, Color color, Font font)
        {
            super(parent, rect);
            
            this.label = label;
            this.color = color;
            this.font = font;
            takenSide = null;
        }
        
        public int getZOrder()
        {
            return GamePanel.LABEL_ZORDER;
        }

        public void paintItem(Graphics g)
        {
            g.setColor(color);
            g.setFont(font);
            drawString(g, label, 0, 0, TextAlign.CENTER);
            
            if (takenSide != null)
                drawTakenArrow(g, rect, takenSide);
        }

        private void setTakenSide(PlayerSide takenSide)
        {
            if (this.takenSide != takenSide)
            {
                this.takenSide = takenSide;
                repaint();
            }
        }
    }
    
    public MissionItem(JComponent parent, Rectangle rect,
                       Font titleFont, Color titleColor, Font infoFont, Color infoColor)
    {
        super(parent, rect);
        
        this.titleFont = titleFont;
        this.titleColor = titleColor;
        this.infoFont = infoFont;
        this.infoColor = infoColor;
        missionFont = new Font(titleFont.getFontName(), Font.BOLD, 32);
        
        mainMsgId = null;
        subMsgId = null;
        gameMission = null;
        cardItems = new ArrayList<MissionCardItem>(4);
        labelItems = new ArrayList<MissionLabelItem>(2);
        missionTaken = null;
    }

    public void paintItem(Graphics g)
    {
        fillBackground(g);
        Rectangle rFill = new Rectangle(rect.x, rect.y, rect.width, TITLE_HEIGHT); 
        fillRect(g, rFill, Color.DARK_GRAY, 0.4f);
        rFill.setBounds(rect.x, rect.y+rect.height-BOTTON_HEIGHT, rect.width, BOTTON_HEIGHT);
        fillRect(g, rFill, Color.DARK_GRAY, 0.4f);

        // title
        g.setFont(titleFont);
        g.setColor(titleColor);
        drawString(g, Resource.getProperty(MISSION_LBL), X_MARGIN, 0, TextAlign.CENTER);
     
        if (gameMission != null)
            drawMission(g);
        else if (mainMsgId != null)
            drawMessage(g);
    }

    public int getZOrder()
    {
        return GamePanel.LABEL_ZORDER;
    }
    
    public void setMessage(String mainMsgId, String subMsgId)
    {
        this.mainMsgId = mainMsgId;
        this.subMsgId = subMsgId;
        gameMission = null;
        
        repaint();
    }
    
    public void setMission(GameMission gameMission)
    {
        this.gameMission = gameMission;
        mainMsgId = null;
        subMsgId = null;
        
        if (gameMission != null && gameMission != GameMission.UnknownMission
            && gameMission != GameMission.NoMission)
        {
            missionBonus = gameMission.getRandomBonus();
            cardItems.clear();
            labelItems.clear();
            
            if (gameMission.getMissionType() == GameMission.MissionType.AND)
                initAndMission();
            else if (gameMission.getMissionType() == GameMission.MissionType.OR)
                initOrMission();
            else if (gameMission.getMissionType() == GameMission.MissionType.COUNT)
                initCountMission();
        }
        
        repaint();
    }
    
    public int checkNewMission(PlayerStatus playerStatus)
    {
        int missionBonus = 0;

        if (gameMission != null && gameMission != GameMission.UnknownMission
            && gameMission != GameMission.NoMission)
        {
            if (gameMission.getMissionType() == MissionType.AND)
                missionBonus = checkAndMission(playerStatus);
            else if (gameMission.getMissionType() == MissionType.OR)
                missionBonus = checkOrMission(playerStatus);
            else if (gameMission.getMissionType() == MissionType.COUNT)
                missionBonus = checkCountMission(playerStatus);
        }
        
        return missionBonus;
    }
    
    public boolean isMissionAvailable()
    {
        boolean available = gameMission != null && gameMission != GameMission.UnknownMission
                            && gameMission != GameMission.NoMission;
        
        if (available)
        {
            if (gameMission.getMissionType() == MissionType.AND)
            {
                PlayerSide takenSide = null;
                
                for (MissionCardItem mci: cardItems)
                {
                    if (available)
                    {
                        available = mci.getTakenSide() == null || takenSide == null
                                    || takenSide == mci.getTakenSide();
                        
                        if (takenSide == null)
                            takenSide = mci.getTakenSide();
                    }
                }
            }
            else if (gameMission.getMissionType() == MissionType.OR)
            {
                available = false;
                for (MissionCardItem mci: cardItems)
                {
                    if (available == false)
                        available = mci.getTakenSide() == null;
                }
            }
        }
        
        return available;
    }
    
    public boolean isMissionCard(int cardCode)
    {
        boolean missionCard = false;
        
        if (cardItems != null)
        {
            for (int i = 0; i < cardItems.size() && missionCard == false; i++)
                missionCard = cardItems.get(i).getCardCode() == cardCode;
        }
        
        return missionCard;
    }
    
    private void drawMessage(Graphics g)
    {
        g.setFont(infoFont);
        g.setColor(infoColor);
        
        drawMissionText(g, Resource.getProperty(mainMsgId),
                        subMsgId == null ? null : Resource.getProperty(subMsgId),
                        TextAlign.LEFT);
    }
    
    private void drawMission(Graphics g)
    {
        if (gameMission == GameMission.UnknownMission
            || gameMission == GameMission.NoMission)
        {
            g.setFont(missionFont);
            g.setColor(infoColor);
            String mission = gameMission == GameMission.UnknownMission
                             ? "?" : Resource.getProperty(NO_MISSION_LBL);
            drawMissionText(g, mission, null, TextAlign.CENTER);
        }
        else
            drawMissionItems(g);
    }
    
    private void drawMissionItems(Graphics g)
    {
        for (GameItem gameItem: cardItems)
            gameItem.paintItem(g);
        
        for (GameItem gameItem: labelItems)
            gameItem.paintItem(g);
    }
    
    private void drawMissionText(Graphics g, String mainMsg, String subMsg,
                                 TextAlign align)
    {
        Rectangle rInfo = getInfoRect();
        FontMetrics fm = g.getFontMetrics();
        int msgHeight = fm.getHeight() + (subMsg == null ? 0 : fm.getHeight());
        int top = rect.y + TITLE_HEIGHT + (rInfo.height - msgHeight) / 2;
        
        drawString(g, mainMsg, X_MARGIN * 2, top, align);
        
        if (subMsg != null)
        {
            top += fm.getHeight();
            drawString(g, subMsg, X_MARGIN*2, top, align);
        }
    }
    
    private Rectangle getInfoRect()
    {
        return new Rectangle(rect.x, rect.y+TITLE_HEIGHT,
                             rect.width, rect.height-TITLE_HEIGHT-BOTTON_HEIGHT);
    }
    
    private void initAndMission()
    {
        int[] cardCodes = gameMission.getCards();
        Dimension imgSize = CardItem.getCardImageInfo(CardSize.NORMAL).getSize();
        Rectangle rInfo = getInfoRect();
        int space = 5;
        int left = rect.x + (rInfo.width-(imgSize.width*(cardCodes.length+1)+space*cardCodes.length))/2;
        int top = rInfo.y + (rInfo.height - imgSize.height) / 2;
        
        for (int i = 0; i < cardCodes.length; i++)
        {
            Rectangle rect = new Rectangle(left, top, imgSize.width, imgSize.height);
            MissionCardItem mci = new MissionCardItem(parent, rect, cardCodes[i]);
            cardItems.add(mci);
            left += imgSize.width + space;
        }
        
        Rectangle rect = new Rectangle(left, top+imgSize.height-40, imgSize.width+10, 40);
        MissionLabelItem mli = new MissionLabelItem(parent, rect, "x" + missionBonus,
                                                    infoColor, missionFont);
        labelItems.add(mli);
        initMissionPlayers(1);
    }
    
    private void initOrMission()
    {
        int[] cardCodes = gameMission.getCards();
        Dimension imgSize = CardItem.getCardImageInfo(CardSize.NORMAL).getSize();
        Rectangle rInfo = getInfoRect();
        int space = 10;
        int left = rect.x + (rInfo.width-(imgSize.width*cardCodes.length*2+space*(cardCodes.length-1)))/2;
        int top = rInfo.y + (rInfo.height - imgSize.height) / 2;
        
        for (int i = 0; i < cardCodes.length; i++)
        {
            Rectangle rect = new Rectangle(left, top, imgSize.width, imgSize.height);
            MissionCardItem mci = new MissionCardItem(parent, rect, cardCodes[i]);
            cardItems.add(mci);
            
            left += imgSize.width;
            rect = new Rectangle(left, top+imgSize.height-40, imgSize.width+space, 40);
            MissionLabelItem mli = new MissionLabelItem(parent, rect, "x" + missionBonus,
                                                        infoColor, missionFont);
            labelItems.add(mli);

            left += imgSize.width + space;
        }
        
        initMissionPlayers(cardCodes.length);
    }
    
    private void initCountMission()
    {
        int cardCode = gameMission.getCards()[0];
        Rectangle rInfo = getInfoRect();
        int cardClassIndex = CardItem.getMajorCode(cardCode) - GameMission.TEN_CLASS;
        int count = CardItem.getMinorCode(cardCode);
        String mission = Resource.format(COUNT_MISIONS_LBL[cardClassIndex], count);
        int textHeight = 40;
        
        rInfo.y += (rInfo.height - textHeight) / 2;
        rInfo.height = textHeight;
        
        MissionLabelItem mli = new MissionLabelItem(parent, rInfo, mission + " x" + missionBonus,
                                                    infoColor, missionFont);
        labelItems.add(mli);
        initMissionPlayers(1);
    }
    
    private void initMissionPlayers(int missions)
    {
        missionTaken = new boolean[missions];
        
        for (int i = 0; i < missions; i++)
            missionTaken[i] = false;
    }
    
    private int checkAndMission(PlayerStatus playerStatus)
    {
        int missionBonus = 0;
        
        if (missionTaken[0] == false)
        {
            boolean takenAll = true;
            boolean takenAllByCurrentPlayer = true;
            
            for (MissionCardItem mci: cardItems)
            {
                if (mci.getTakenSide() == null)
                {
                    if (playerStatus.isCardTaken(mci.getCardCode(), false))
                        mci.setTakenSide(playerStatus.getPlayerSide());
                    else
                    {
                        takenAll = false;
                        takenAllByCurrentPlayer = false;
                    }
                }
                else if (takenAllByCurrentPlayer)
                    takenAllByCurrentPlayer = mci.getTakenSide() == playerStatus.getPlayerSide();
            }
            
            if (takenAll)
            {
                if (takenAllByCurrentPlayer)
                {
                    missionBonus = this.missionBonus;
                    labelItems.get(0).setTakenSide(playerStatus.getPlayerSide());
                }
                
                missionTaken[0] = true;
            }
        }
        
        return missionBonus;
    }
    
    private int checkOrMission(PlayerStatus playerStatus)
    {
        int missionBonus = 1;
        
        for (int i = 0; i < missionTaken.length; i++)
        {
            if (missionTaken[i] == false)
            {
                MissionCardItem mci = cardItems.get(i);
                
                missionTaken[i] = playerStatus.isCardTaken(mci.getCardCode(), false);
                
                if (missionTaken[i])
                {
                    mci.setTakenSide(playerStatus.getPlayerSide());
                    //labelItems.get(i).setTakenSide(playerStatus.getPlayerSide());
                    missionBonus *= this.missionBonus;
                }
            }
        }
        
        return missionBonus > 1 ? missionBonus: 0;
    }
    
    private int checkCountMission(PlayerStatus playerStatus)
    {
        int missionBonus = 0;
        
        if (missionTaken[0] == false)
        {
            int cardCode = gameMission.getCards()[0];
            int mClass = CardItem.getMajorCode(cardCode);
            CardClass cardClass = mClass == GameMission.TEN_CLASS ? CardClass.TEN
                    : (mClass == GameMission.FIVE_CLASS ? CardClass.FIVE : CardClass.LEAF);
            int count = CardItem.getMinorCode(cardCode);
            List<CardItem> takenCards = playerStatus.getTakenCards(cardClass);

            if (cardClass == CardClass.LEAF)
                missionTaken[0] = GameRule.getLeafCount(takenCards) >= count;
            else
                missionTaken[0] = takenCards.size() >= count;
                
            if (missionTaken[0])
            {
                missionBonus = this.missionBonus;
                labelItems.get(0).setTakenSide(playerStatus.getPlayerSide());
            }
        }
        
        return missionBonus;
    }
    
    private static void drawTakenArrow(Graphics g, Rectangle rect, PlayerSide takenSide)
    {
        ((Graphics2D)g).setComposite(AlphaComposite.SrcOver.derive(0.65f));
        
        g.setColor(Color.BLUE);
        
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        int[][] points = _arrows[takenSide.ordinal()];
        int margin = 1;
        int xStart = rect.x+rect.width-asize-margin;
        int yStart = rect.y+margin;
        
        for (int i = 0; i < points.length; i++)
        {
            xPoints[i] = xStart + points[i][0];
            yPoints[i] = yStart + points[i][1];
        }
        
        g.fillPolygon(xPoints, yPoints, xPoints.length);
        
        g.setPaintMode();  // restore the composite mode
    }
}
