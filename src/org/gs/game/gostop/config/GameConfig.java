package org.gs.game.gostop.config;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.gs.game.gostop.CardSize;
import org.gs.game.gostop.Resource;
import org.gs.game.gostop.sound.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GameConfig
{
    private static final String PROP_SETTING = "gostop.settings";
    private static final String DEFAULT_SETTING = "/gs_gostop_settings.xml";
    
    private static final String ROOT_NODE = "GSGoStopSettings";
    private static final String USER_MONEY_NODE = "user.money";
    private static final String AVATAR_SIZE_NODE = "avatar.size";
    private static final String SUPPORTED_LOCALE_NODE = "supported.locale";
    private static final String DEFAULT_USER_NODE = "default.user";
    private static final String INITIAL_ATTR = "initial";
    private static final String REFILL_ATTR = "refill";
    private static final String MAX_WIDTH_ATTR = "maxWidth";
    private static final String MAX_HEIGHT_ATTR = "maxHeight";
    private static final String LOCALE_ID_ATTR = "localeId";
    private static final String LOCALE_NAME_ATTR = "localeName";
    private static final String LOGIN_ID_ATTR = "loginId";
    private static final String USER_ALIAS_ATTR = "userAlias";
    private static final String AVATAR_PATH_ATTR = "avatarPath";

    private static final String GAME_TYPE_NODE = "game.type";
    private static final String GAME_MONEY_NODE = "game.money";
    private static final String START_CARDS_NODE = "start.cards";
    private static final String BONUS_CARDS_NODE = "bonus.cards";
    private static final String MISSION_TYPE_NODE = "mission.type";
    private static final String GAME_TYPE_ID_ATTR = "typeId";
    private static final String GAME_PLAYERS_ATTR = "players";
    private static final String WIN_POINTS_ATTR = "winPoints";
    private static final String LEAF_PENALTY_ATTR = "leafPenalty";
    private static final String TABLE_CARDS_ATTR = "table.cards";
    private static final String PLAYER_CARDS_ATTR = "player.cards";
    private static final String MONEY_PER_POINT_ATTR = "money.per.point";
    private static final String TRIPLE_ATTR = "triple";
    private static final String DOUBLE_ATTR = "double";
    private static final String KING_ATTR = "king";
    private static final String CARDS_ATTR = "cards";
    private static final String MISSIONT_TYPE_ATTR = "type";
    private static final String BONUS_ATTR = "bonus";
    
    private static final String CARD_IMAGE_NODE = "card.image";
    private static final String SIZE_ATTR = "size";
    private static final String WIDTH_ATTR = "width";
    private static final String HEIGHT_ATTR = "height";
    private static final String IMAGE_ATTR = "image";
    
    private static final String GAME_LAYOUT_NODE = "game.layout";
    private static final String BACKGROUND_NODE = "background";
    private static final String PLAYER_LAYOUT_NODE = "player.layout";
    private static final String TEXT_FONT_NODE = "text.font";
    private static final String MISSION_RECT_ATTR = "mission.rect";
    private static final String STACK_RECT_ATTR = "stack.rect";
    private static final String ALERT_SIZE_ATTR = "alert.size";
    private static final String OPPOSITE_ATTR = "opposite";
    private static final String PLAYER_SIDE_ATTR = "side";
    private static final String RECT_ATTR = "rect";
    private static final String TEXT_RECT_ATTR = "text.rect";
    private static final String AVATAR_RECT_ATTR = "avatar.rect";
    private static final String POINT_RECT_ATTR = "point.rect";
    private static final String PENALTY_RECT_ATTR = "penalty.rect";
    private static final String BONUS_RECT_ATTR = "bonus.rect";
    private static final String ALERT_POINT_ATTR = "alert.point";
    private static final String HOLD_RECT_ATTR = "hold.rect";
    private static final String KINGS_RECT_ATTR = "kings.rect";
    private static final String TENS_RECT_ATTR = "tens.rect";
    private static final String FIVES_RECT_ATTR = "fives.rect";
    private static final String LEAVES_RECT_ATTR = "leaves.rect";
    private static final String TARGET_ATTR = "target";
    private static final String NAME_ATTR = "name";
    private static final String STYLE_ATTR = "style";
    private static final String COLOR_ATTR = "color";
    
    private static final String SOUNDS_NODE = "sounds";
    private static final String SOUND_ITEM_NODE = "sound.item";
    private static final String VOICES_NODE = "voices";
    private static final String VOICE_TYPE_NODE = "voice.type";
    private static final String ID_ATTR = "id";
    private static final String PATH_ATTR = "path";
    private static final String TYPE_ATTR = "type";
    
    private static GameConfig _instance = null;
    
    private int avatarMaxWidth;
    private int avatarMaxHeight;
    
    private XPath xpath;

    /**
     * 단순히 카드크기와 카드이미지경로만저장하는 클래스
     */
    private static class CardImageInfo
    {
        private int width;
        private int height;
        private String imagePath;
    }
    
    private HashMap<CardSize,CardImageInfo> cardImages; 
    
    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name)
    {
        return Enum.valueOf(enumType, name.toUpperCase());
    }
    
    public static GameConfig getInstance()
        throws Exception
    {
        if (_instance == null)
            _instance = new GameConfig();
        
        return _instance;
    }
    
    public static UserLocale getDefaultLocale()
    {
        return getSupportedLocale(null);
    }

    private GameConfig()
        throws Exception
    {
        String configPath = System.getProperty(PROP_SETTING);
        InputStream is;
        
        if (configPath == null || configPath.length() == 0)
            is = getClass().getResourceAsStream(DEFAULT_SETTING);
        else
            is = new FileInputStream(configPath);

        try
        {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(is);
            
            xpath = XPathFactory.newInstance().newXPath();
            initConfig(doc);
        }
        finally
        {
            xpath = null;
            is.close();
        }
    }
    
    private void initConfig(Document doc)
        throws XPathExpressionException
    {
        Element settings = getElement('/' + ROOT_NODE + '/' + "audio.settings", doc);
        initAudioSettings(settings);

        settings = getElement('/' + ROOT_NODE + '/' + "user.settings", doc);
        initUserSettings(settings);
        
        settings = getElement('/' + ROOT_NODE + '/' + "game.settings", doc);
        initGameSettings(settings);
        
        settings = getElement('/' + ROOT_NODE + '/' + "image.settings", doc);
        initImageSettings(settings);

        settings = getElement('/' + ROOT_NODE + '/' + "layout.settings", doc);
        initLayoutSettings(settings);
    }
    
    private void initUserSettings(Element userSettings)
        throws XPathExpressionException
    {
        Element elm = getElement(USER_MONEY_NODE, userSettings);
        if (elm != null)
        {
            GameUser.setInitialMoney(Long.parseLong(elm.getAttribute(INITIAL_ATTR)));
            GameUser.setRefillMoney(Long.parseLong(elm.getAttribute(REFILL_ATTR)));
        }
        
        elm = getElement(AVATAR_SIZE_NODE, userSettings);
        if (elm != null)
        {
            avatarMaxWidth = getIntAttribute(elm, MAX_WIDTH_ATTR);
            avatarMaxHeight = getIntAttribute(elm, MAX_HEIGHT_ATTR);
        }
        else
        {
            avatarMaxWidth = 60;
            avatarMaxHeight = 125;
        }

        NodeList nl = getNodeList(SUPPORTED_LOCALE_NODE, userSettings);
        for (int i = 0; i < nl.getLength(); i++)
        {
            elm = (Element)nl.item(i);
            UserLocale.addUserLocale(elm.getAttribute(LOCALE_ID_ATTR),
                                     elm.getAttribute(LOCALE_NAME_ATTR));
        }
        
        nl = getNodeList(DEFAULT_USER_NODE, userSettings);
        for (int i = 0; i < nl.getLength(); i++)
        {
            elm = (Element)nl.item(i);
            GameUser.addGameUser(elm.getAttribute(LOGIN_ID_ATTR), null, null,
                                 elm.getAttribute(USER_ALIAS_ATTR),
                                 elm.getAttribute(AVATAR_PATH_ATTR),
                                 getSupportedLocale(null).getLocaleId(),
                                 null,
                                 GameUser.GameUserType.COMPUTER);
        }
    }

    /**
     * Game Setting을 초기화 한다.
     */
    private void initGameSettings(Element gameSettings)
        throws XPathExpressionException
    {
        //GameType을 가져온다.
        //2인 게임과 3인 게임이 있음
        NodeList nl = getNodeList(GAME_TYPE_NODE, gameSettings);
        for (int i = 0; i < nl.getLength(); i++)
        {
            //첫번째원소를가져다가
            Element elm = (Element)nl.item(i);
            //플레이어수, 나는점수,피박페널티장수를설정한다.
            GameType gameType = GameType.addGameType(elm.getAttribute(GAME_TYPE_ID_ATTR),
                                                     getIntAttribute(elm, GAME_PLAYERS_ATTR),
                                                     getIntAttribute(elm, WIN_POINTS_ATTR),
                                                     getIntAttribute(elm, LEAF_PENALTY_ATTR));
            //시작할때나눠줄카드세팅
            elm = getElement(START_CARDS_NODE, elm);
            //테이블에놓을카드수, 플레이어에게줄카드수
            gameType.setStartCards(elm.getAttribute(TABLE_CARDS_ATTR),
                                   elm.getAttribute(PLAYER_CARDS_ATTR));
        }

        //포인트당 돈? 설정???
        Element elm = getElement(GAME_MONEY_NODE, gameSettings);
        GameType.setGameMoneyTypes(elm.getAttribute(MONEY_PER_POINT_ATTR));

        //쌍피설정타입, 광도있는듯?
        nl = getNodeList(BONUS_CARDS_NODE, gameSettings);
        for (int i = 0; i < nl.getLength(); i++)
        {
            elm = (Element)nl.item(i);
            BonusCards.addBonusCards(getIntAttribute(elm, TRIPLE_ATTR),
                                     getIntAttribute(elm, DOUBLE_ATTR),
                                     getIntAttribute(elm, KING_ATTR));
        }

        //미션설정
        nl = getNodeList(MISSION_TYPE_NODE, gameSettings);
        for (int i = 0; i < nl.getLength(); i++)
        {
            elm = (Element)nl.item(i);
            GameMission.addGameMission(elm.getAttribute(CARDS_ATTR),
                                       elm.getAttribute(MISSIONT_TYPE_ATTR),
                                       elm.getAttribute(BONUS_ATTR));
        }
    }

    /**
     * 이미지경로등을초기화한다.
     * 카드 이미지파일에 카드전체이미지가붙어있으며,
     * 카드사이즈는 BIG/HOLD/NORMAL/SMALL 이있따.
     * @param imageSettings
     * @throws XPathExpressionException
     */
    private void initImageSettings(Element imageSettings)
        throws XPathExpressionException
    {
        NodeList nl = getNodeList(CARD_IMAGE_NODE, imageSettings);
        
        cardImages = new HashMap<CardSize,CardImageInfo>(4);
        
        for (int i = 0; i < nl.getLength(); i++)
        {
            Element elm = (Element)nl.item(i);
            CardImageInfo cii = new CardImageInfo();
            
            cii.width = getIntAttribute(elm, WIDTH_ATTR);
            cii.height = getIntAttribute(elm, HEIGHT_ATTR);
            cii.imagePath = elm.getAttribute(IMAGE_ATTR);
            
            cardImages.put(valueOf(CardSize.class, elm.getAttribute(SIZE_ATTR)), cii);
        }
    }

    /**
     * 레이아웃설정을 읽어들인다.
     * @param layoutSettings
     * @throws XPathExpressionException
     */
    private void initLayoutSettings(Element layoutSettings)
        throws XPathExpressionException
    {
        NodeList nl = getNodeList(GAME_LAYOUT_NODE, layoutSettings);

        for (int i = 0; i < nl.getLength(); i++)
        {
            //게임 세팅 순서대로읽기
            Element elm = (Element)nl.item(i);
            //게임 타입 명 읽기
            GameType gameType = GameType.getGameType(elm.getAttribute(GAME_TYPE_NODE));
            
            if (gameType != null)
            {
                //미션, 카드스택, 경고메시지 위치 설정
                GameLayout gameLayout = new GameLayout(getRectAttribute(elm, MISSION_RECT_ATTR),
                                                       getRectAttribute(elm, STACK_RECT_ATTR),
                                                       getSizeAttribute(elm, ALERT_SIZE_ATTR));
                
                Element elmBg = getElement(BACKGROUND_NODE, elm);
                if (elmBg != null)
                {
                    //배경이미지설정
                    gameLayout.setBackgroundInfo(getIntAttribute(elmBg, WIDTH_ATTR),
                                                 getIntAttribute(elmBg, HEIGHT_ATTR),
                                                 elmBg.getAttribute(IMAGE_ATTR));
                }

                //좌측, 우측, 플레이이의 레이아웃을설정한다.
                NodeList PLs = getNodeList(PLAYER_LAYOUT_NODE, elm);
                for (int p = 0; p < PLs.getLength(); p++)
                {
                    Element elmPL = (Element)PLs.item(p);
                    //플레이어위치,전체크기,아바타위치,점수위치,패널위치,보너스위치, 경고위치,홀드?위치?,광위치,텐스?위치?,멍텅위치,피위치
                    gameLayout.addPlayerLayout(Boolean.valueOf(elmPL.getAttribute(OPPOSITE_ATTR)),
                                               elmPL.getAttribute(PLAYER_SIDE_ATTR),
                                               getRectAttribute(elmPL, RECT_ATTR),
                                               getRectAttribute(elmPL, TEXT_RECT_ATTR),
                                               getRectAttribute(elmPL, AVATAR_RECT_ATTR),
                                               getRectAttribute(elmPL, POINT_RECT_ATTR),
                                               getRectAttribute(elmPL, PENALTY_RECT_ATTR),
                                               getRectAttribute(elmPL, BONUS_RECT_ATTR),
                                               getPointAttribute(elmPL, ALERT_POINT_ATTR),
                                               getRectAttribute(elmPL, HOLD_RECT_ATTR),
                                               getRectAttribute(elmPL, KINGS_RECT_ATTR),
                                               getRectAttribute(elmPL, TENS_RECT_ATTR),
                                               getRectAttribute(elmPL, FIVES_RECT_ATTR),
                                               getRectAttribute(elmPL, LEAVES_RECT_ATTR));
                }

                //글꼴등설정
                NodeList TFs = getNodeList(TEXT_FONT_NODE, elm);
                for (int p = 0; p < TFs.getLength(); p++)
                {
                    Element elmTF = (Element)TFs.item(p);
                    gameLayout.addTextFont(elmTF.getAttribute(TARGET_ATTR),
                                           elmTF.getAttribute(NAME_ATTR),
                                           getIntAttribute(elmTF, SIZE_ATTR),
                                           elmTF.getAttribute(STYLE_ATTR),
                                           elmTF.getAttribute(COLOR_ATTR));
                }
                
                gameType.setGameLayout(gameLayout);
            }
        }
    }
    
    private void initAudioSettings(Element audioSettings)
        throws XPathExpressionException
    {
        Element elmSounds = getElement(SOUNDS_NODE, audioSettings);
        NodeList nl = getNodeList(SOUND_ITEM_NODE, elmSounds);

        for (int i = 0; i < nl.getLength(); i++)
        {
            Element elm = (Element)nl.item(i);
            String type = elm.getAttribute(TYPE_ATTR);
            GameSoundType soundType = type == null || type.length() == 0
                    ? GameSoundType.SOUND : valueOf(GameSoundType.class, type);
            
            GameSoundManager.addSoundItem(new GameSoundItem(elm.getAttribute(ID_ATTR),
                                                            elm.getAttribute(PATH_ATTR),
                                                            soundType));
        }
        
        Element elmVoices = getElement(VOICES_NODE, audioSettings);
        nl = getNodeList(VOICE_TYPE_NODE, elmVoices);

        for (int i = 0; i < nl.getLength(); i++)
        {
            Element elm = (Element)nl.item(i);
            
            GameSoundManager.addVoiceType(new GameVoiceType(elm.getAttribute(ID_ATTR),
                                                            elm.getAttribute(PATH_ATTR)));
        }
    }
    
    public int getAvatarMaxWidth()
    {
        return avatarMaxWidth;
    }
    
    public int getAvatarMaxHeight()
    {
        return avatarMaxHeight;
    }
    
    public static UserLocale getSupportedLocale(Locale locale)
    {
        UserLocale supportedLocale = null;
        List<UserLocale> supportedLocales = UserLocale.getUserLocales();
        
        if (supportedLocales != null)
        {
            if (locale == null)
                locale = Locale.getDefault();
            
            locale = Resource.getResourceLocale(locale);
            System.out.println("locale:"+locale);
            for (int i = 0; i < supportedLocales.size() && supportedLocale == null; i++)
            {
                if (locale.equals(supportedLocales.get(i).getLocale()))
                    supportedLocale = supportedLocales.get(i);
            }
        }
        
        return supportedLocale;
    }
    
    public GameMission getRandomGameMission()
    {
        return GameMission.getRandomMission();
    }
    
    public Dimension getCardSize(CardSize mode)
    {
        Dimension size = null;
        CardImageInfo cii = cardImages.get(mode);
        
        if (cii != null)
            size = new Dimension(cii.width, cii.height);
        
        return size;
    }

    /**
     *  카드사이즈에따라서 CardImage Url을 리턴한다.
     */
    public URL getCardImageURL(CardSize mode)
    {
        URL url = null;
        CardImageInfo cii = cardImages.get(mode);
        
        if (cii != null)
            url = getClass().getResource(cii.imagePath);
        
        return url;
    }
    
    private static Dimension getSizeAttribute(Element elm, String attrName)
    {
        StringTokenizer st = new StringTokenizer(elm.getAttribute(attrName), ",");
        
        return new Dimension(Integer.parseInt(st.nextToken()),
                             Integer.parseInt(st.nextToken()));
    }
    
    private static Point getPointAttribute(Element elm, String attrName)
    {
        StringTokenizer st = new StringTokenizer(elm.getAttribute(attrName), ",");
        
        return new Point(Integer.parseInt(st.nextToken()),
                         Integer.parseInt(st.nextToken()));
    }
    
    private static Rectangle getRectAttribute(Element elm, String attrName)
    {
        StringTokenizer st = new StringTokenizer(elm.getAttribute(attrName), ",");
        
        return new Rectangle(Integer.parseInt(st.nextToken()),
                             Integer.parseInt(st.nextToken()),
                             Integer.parseInt(st.nextToken()),
                             Integer.parseInt(st.nextToken()));
    }
    
    private static int getIntAttribute(Element elm, String attrName)
    {
        return Integer.parseInt(elm.getAttribute(attrName));
    }
    
    private Element getElement(String expression, Object item)
        throws XPathExpressionException
    {
        return (Element)xpath.evaluate(expression, item, XPathConstants.NODE);
    }
    
    private NodeList getNodeList(String expression, Object item)
        throws XPathExpressionException
    {
        return (NodeList)xpath.evaluate(expression, item, XPathConstants.NODESET);
    }
}
