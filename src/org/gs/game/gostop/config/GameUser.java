package org.gs.game.gostop.config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.gs.game.gostop.Main;
import org.gs.game.gostop.event.GameEvent;
import org.gs.game.gostop.event.GameEventType;
import org.gs.game.gostop.event.IGameEventListener;
import org.gs.game.gostop.sound.GameSoundManager;
import org.gs.game.gostop.sound.GameVoiceType;
import org.gs.game.gostop.utils.Base64Coder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 사용자 정보를 저장하는 클래스
 */
public class GameUser
{
    private static final String USER_CONFIG_DIR = "../config";
    private static final String USER_CONFIG_FILE = "gsUserConfig.xml";
    
    private static final String ROOT_TAG = "gsusers";
    private static final String USER_TAG = "game.user";
    private static final String LOGINID_ATTR = "login.id";
    private static final String USER_TYPE_ATTR = "user.type";
    private static final String PASSWORD_TAG = "password";
    private static final String USERINFO_TAG = "user.info";
    private static final String USER_NAME_PROP = "user.name";
    private static final String USER_ALIAS_PROP = "user.alias";
    private static final String AVATAR_PATH_PROP = "avatar.path";
    private static final String USER_LOCALE_PROP = "user.locale";
    private static final String USER_VOICEID_PROP = "user.voiceId";
    private static final String GAME_MONEY_TYPE_PROP = "game.moneyType";
    private static final String GAME_BONUS_CARDS_PROP = "game.bonusCards";
    private static final String GAME_MUTE_SOUND_PROP = "game.muteSound";
    private static final String MONEY_PROP = "money";
    private static final String ALL_IN_COUNT_PROP = "all.in.count";
    private static final String WINS_PROP = "wins";
    private static final String DRAWS_PROP = "draws";
    private static final String LOSES_PROP = "loses";
    private static final String HIGHEST_MONEY_PROP = "highest.money";
    private static final String LOWEST_MONEY_PROP = "lowest.money";
    private static final String BEST_POINTS_PROP = "best.points";
    private static final String BEST_MONEY_PROP = "best.money";
    private static final String WORST_POINTS_PROP = "worst.points";
    private static final String WORST_MONEY_PROP = "worst.money";
    private static final String NULL_PROP = "";

    private static final String _cipherAlgorithm = "DES";

    public static enum GameUserType { HUMAN, COMPUTER }

    private static String _userConfigFolder = null;
    private static String _userConfigPath = null;
    private static GameUser[] _defaultUsers = null;
    private static long _initialMoney = 200000;
    private static long _refillMoney = 100000;
    
    private String loginId;
    private String password;
    private String userName;
    private String userAlias;
    private String avatarPath;
    private String userLocale;
    private String userVoiceId;
    private int gameMoneyType;
    private BonusCards gameBonusCards;
    private boolean muteSound;
    private GameUserType userType;
    private long money;
    private int allInCount;
    private int wins;
    private int draws;
    private int loses;
    private long highestMoney;
    private long lowestMoney;
    private int bestPoints;
    private long bestMoney;
    private int worstPoints;
    private long worstMoney;
    private List<IGameEventListener> eventListeners;
    
    public GameUser(String loginId, String password, String userName, String userAlias,
            String avatarPath, String userLocale,
            String userVoiceId, GameUserType userType,
            long money, int allInCount, int wins, int draws, int loses)
    {
        this(loginId, password, userName, userAlias, avatarPath, userLocale,
             userVoiceId, 0, null, false, userType, money, allInCount,
             wins, draws, loses, money, money, 0, 0, 0, 0);
    }
    
    public GameUser(String loginId, String password, String userName, String userAlias,
                    String avatarPath, String userLocale,
                    String userVoiceId, int gameMoneyType, BonusCards gameBonusCards, 
                    boolean muteSound, GameUserType userType,
                    long money, int allInCount, int wins, int draws, int loses,
                    long highestMoney, long lowestMoney,
                    int bestPoints, long bestMoney, int worstPoints, long worstMoney)
    {
        this.loginId = loginId;
        this.password = password == null ? NULL_PROP : password;
        this.userName = userName == null ? NULL_PROP : userName;
        this.userAlias = userAlias == null ? NULL_PROP : userAlias;
        this.avatarPath = avatarPath == null ? NULL_PROP : avatarPath;
        if (userLocale == null || userLocale.length() == 0)
            this.userLocale = GameConfig.getDefaultLocale().getLocaleId();
        else
            this.userLocale = userLocale;
        if (userVoiceId == null || userVoiceId.length() == 0)
            this.userVoiceId = null;
        else
            this.userVoiceId = userVoiceId;
        this.gameMoneyType = gameMoneyType;
        this.gameBonusCards = gameBonusCards;
        this.muteSound = muteSound;
        this.userType = userType;
        this.money = money;
        this.allInCount = allInCount;
        this.wins = wins;
        this.draws = draws;
        this.loses = loses;
        this.highestMoney = money > highestMoney ? money : highestMoney;
        this.lowestMoney = money < lowestMoney ? money : lowestMoney;
        this.bestPoints = bestPoints;
        this.bestMoney = bestMoney;
        this.worstPoints = worstPoints;
        this.worstMoney = worstMoney;
        eventListeners = null;
    }
    
    public String getLoginId()
    {
        return loginId;
    }
    
    public void updateInfo(String password, String userName, String userAlias,
                           String avatarPath, String userLocale, String userVoiceId)
    {
        if (password != null && password.length() > 0)
            this.password = password;
        this.userName = userName;
        this.userAlias = userAlias;
        this.avatarPath = avatarPath;
        this.userLocale = userLocale;
        this.userVoiceId = userVoiceId;
        
        updateGameUser(this);
        
        fireUpdateEvent();
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public String getUserName()
    {
        return userName;
    }
    
    public String getUserAlias()
    {
        return userAlias;
    }
    
    public String getAvatarPath()
    {
        return avatarPath;
    }
    
    public String getUserLocale()
    {
        return userLocale;
    }
    
    public String getUserVoiceId()
    {
        if (userVoiceId == null)
        {
            List<GameVoiceType> voiceTypes = GameSoundManager.getVoiceTypes();
            int index = Main.getRandom().nextInt(voiceTypes.size());
            
            userVoiceId = voiceTypes.get(index).getVoiceTypeId();
        }
        
        return userVoiceId;
    }
    
    public int getGameMoneyType()
    {
        return gameMoneyType;
    }
    
    public void setGameMoneyType(int gameMoneyType)
    {
        this.gameMoneyType = gameMoneyType;
    }
    
    public BonusCards getGameBonusCards()
    {
        return gameBonusCards;
    }
    
    public void setGameBonusCards(BonusCards gameBonusCards)
    {
        this.gameBonusCards = gameBonusCards;
    }
    
    public boolean getMuteSound()
    {
        return muteSound;
    }
    
    public void setMuteSound(boolean muteSound)
    {
        this.muteSound = muteSound;
    }
    
    public GameUserType getUserType()
    {
        return userType;
    }
    
    public void updateMoney(long updateMoney, boolean allIn, WinType winType)
    {
        updateMoney(updateMoney, 0, allIn, winType);
    }
    
    public void updateMoney(long updateMoney, int points, boolean allIn, WinType winType)
    {
        money += updateMoney;
        
        if (money > highestMoney)
            highestMoney = money;
        if (money < lowestMoney)
            lowestMoney = money;
        
        if (allIn)
            allInCount++;
        
        if (winType == WinType.WIN)
        {
            wins++;
            if (updateMoney > bestMoney)
                bestMoney = updateMoney;
            if (bestPoints < points)
                bestPoints = points;
        }
        else if (winType == WinType.DRAW)
            draws++;
        else if (winType == WinType.LOSE)
        {
            loses++;
            if (updateMoney < worstMoney)
                worstMoney = updateMoney;
            if (worstPoints > points)
                worstPoints = points;
        }
        
        fireUpdateEvent();
    }
    
    public long getMoney()
    {
        return money;
    }
    
    public int getAllInCount()
    {
        return allInCount;
    }
    
    public int getWins()
    {
        return wins;
    }
    
    public int getDraws()
    {
        return draws;
    }
    
    public int getLoses()
    {
        return loses;
    }
    
    public long getHighestMoney()
    {
        return highestMoney;
    }
    
    public long getLowestMoney()
    {
        return lowestMoney;
    }
    
    public int getBestPoints()
    {
        return bestPoints;
    }
    
    public long getBestMoney()
    {
        return bestMoney;
    }
    
    public int getWorstPoints()
    {
        return worstPoints;
    }
    
    public long getWorstMoney()
    {
        return worstMoney;
    }
    
    public void addUpdateListener(IGameEventListener eventListener)
    {
        if (eventListeners == null)
            eventListeners = new ArrayList<IGameEventListener>();
        
        eventListeners.add(eventListener);
    }
    
    protected GameUser clone()
    {
        return new GameUser(loginId, null, userName, userAlias, avatarPath, userLocale,
                            getUserVoiceId(), gameMoneyType, gameBonusCards, muteSound,
                            userType, money, allInCount, wins, draws, loses,
                            highestMoney, lowestMoney,
                            bestPoints, bestMoney, worstPoints, worstMoney);
    }
    
    private void fireUpdateEvent()
    {
        if (eventListeners != null)
        {
            GameEvent gameEvent = new GameEvent(this, GameEventType.GAME_USER_UPDATED);
            
            for (IGameEventListener eventListener: eventListeners)
                eventListener.onGameEvent(gameEvent);
        }
    }
    
    public static void setInitialMoney(long initialMoney)
    {
        _initialMoney = initialMoney;
    }
    
    public static void setRefillMoney(long refillMoney)
    {
        _refillMoney = refillMoney;
    }
    
    public static long getRefillMoney()
    {
        return _refillMoney;
    }

    public static GameUser getGameUser(String loginId, String encryptedPassword)
    {
        GameUser gu = getGameUser(loginId);
        
        return gu != null && gu.password.equals(encryptedPassword) ? gu.clone() : null;
    }
    
    public static boolean isGameUser(String loginId)
    {
        GameUser gu = getGameUser(loginId);
        
        return gu != null && GameUserType.HUMAN == gu.getUserType();
    }
    
    public static GameUser addGameUser(String loginId, String password, String userName,
                                       String userAlias, String avatarPath, String userLocale,
                                       String userVoiceId, GameUserType userType)
    {
        GameUser user = userType == GameUserType.COMPUTER ? getDefaultUser(loginId) : null;
        
        if (user != null)
            return user;
        
        Document doc = getConfigDocument();
        
        if (doc == null)
        {
            try
            {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                doc = db.newDocument();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        Element userNode = getUserElement(doc, loginId);
        if (userNode == null)
        {
            try
            {
                user = new GameUser(loginId, password, userName, userAlias, avatarPath,
                                    userLocale, userVoiceId,
                                    userType, _initialMoney, 0, 0, 0, 0);
                
                Element rootNode = doc.getDocumentElement();
                if (rootNode == null)
                {
                    rootNode = doc.createElement(ROOT_TAG);
                    doc.appendChild(rootNode);
                }
                
                userNode = doc.createElement(USER_TAG);
                setGameUserNode(userNode, user);
                rootNode.appendChild(userNode);
                
                saveDocument(doc, getUserConfigFilePath());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        if (userType == GameUserType.COMPUTER)
            addDefaultUser(user);
        
        return user;
    }

    public static GameUser[] getGameUsers()
    {
        GameUser[] gameUsers = null;
        Document doc = getConfigDocument();
        
        if (doc != null)
        {
            NodeList nl = doc.getElementsByTagName(USER_TAG);
            gameUsers = new GameUser[nl.getLength()];
            
            for (int i = 0; i < nl.getLength(); i++)
            {
                Element elm = (Element)nl.item(i);
                gameUsers[i] = getGameUser(elm).clone();
            }
        }
        
        return gameUsers;
    }
    
    private static void updateGameUser(GameUser gameUser)
    {
        List<GameUser> gameUsers = new ArrayList<GameUser>();
        
        gameUsers.add(gameUser);
        updateGameUsers(gameUsers);
    }

    public static void updateGameUsers(List<GameUser> gameUsers)
    {
        Document doc = getConfigDocument();
        Element userNode;
        
        for (GameUser gameUser: gameUsers)
        {
            userNode = getUserElement(doc, gameUser.getLoginId());
            setGameUserNode(userNode, gameUser);
        }
        
        saveDocument(doc, getUserConfigFilePath());
    }
    
    public static GameUser[] getComputerUsers()
    {
        GameUser[] computerUsers = null;
        
        try
        {
            GameUser[] defaultUsers = getDefaultUsers();
            
            if (defaultUsers != null)
            {
                computerUsers = new GameUser[defaultUsers.length];
                
                for (int i = 0; i < defaultUsers.length; i++)
                    computerUsers[i] = defaultUsers[i].clone();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return computerUsers;
    }
    
    public static String getUserConfigFolder()
    {
        if (_userConfigFolder == null)
        {
            String resource = '/' + GameUser.class.getName().replace('.', '/') + ".class";
            URL url = GameUser.class.getResource(resource);
            String path = url.getPath();
            int index = path.lastIndexOf("!");
            
            if (index > 0)    // jar path: file:/${root}/lib/xxxx.jar!/org/.../GameUser.class
            {
                path = path.substring(0, index);
                if ((index = path.lastIndexOf('/')) > 0)
                    path = path.substring(0, index+1);
                if (path.startsWith("file:"))
                    path = path.substring(5);
            }
            else    // direct class path: /${root}/bin/org/.../GameUser.class
                path = path.substring(0, path.length()-resource.length()+1);
            
            File folder = new File(path + USER_CONFIG_DIR);
            folder.mkdirs();
            
            try
            {
                _userConfigFolder = folder.getCanonicalPath();
            }
            catch (Exception e)
            {
                _userConfigFolder = path + USER_CONFIG_DIR;
                e.printStackTrace();
            }
        }
        
        return _userConfigFolder;
    }
    
    private static void saveDocument(Document doc, String path)
    {
        try
        {
            // save the XML configuration to the file
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.setOutputProperty(OutputKeys.METHOD, "xml");
            trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    
            trans.transform(new DOMSource(doc), new StreamResult(path));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private static GameUser getDefaultUser(String loginId)
    {
        GameUser defaultUser = null;
        
        try
        {
            GameUser[] defaultUsers = getDefaultUsers();
            
            if (defaultUsers != null)
            {
                for (int i = 0; i < defaultUsers.length && defaultUser == null; i++)
                {
                    if (defaultUsers[i].getLoginId().equals(loginId))
                        defaultUser = defaultUsers[i];
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return defaultUser;
    }

    private static void addDefaultUser(GameUser user)
    {
        GameUser[] users;
        
        if (_defaultUsers == null)
            users = new GameUser[1];
        else
        {
            users = new GameUser[_defaultUsers.length + 1];
            System.arraycopy(_defaultUsers, 0, users, 0, _defaultUsers.length);
        }
        
        users[users.length-1] = user;
        _defaultUsers = users;
    }
    
    private static GameUser[] getDefaultUsers()
        throws XPathExpressionException
    {
        if (_defaultUsers == null)
        {
            ArrayList<GameUser> users = new ArrayList<GameUser>();
            Document doc = getConfigDocument();
            
            if (doc != null)
            {
                XPath xpath = XPathFactory.newInstance().newXPath();
                NodeList nl = (NodeList)xpath.evaluate('/' + ROOT_TAG + '/' + USER_TAG,
                                                       doc, XPathConstants.NODESET);
                Element elmUser = null;
                
                for (int i = 0; elmUser == null && i < nl.getLength(); i++)
                {
                    Element elm = (Element)nl.item(i);
                    if (GameUserType.COMPUTER
                        == GameConfig.valueOf(GameUserType.class, elm.getAttribute(USER_TYPE_ATTR)))
                    {
                        GameUser user = getGameUser(elm);
                        
                        if (user != null)
                            users.add(user);
                    }
                }
            }
            
            _defaultUsers = users.toArray(new GameUser[users.size()]);
        }

        return _defaultUsers;
    }
    
    private static GameUser getGameUser(String loginId)
    {
        GameUser user = null;
        Document doc = getConfigDocument();
        
        if (doc != null)
        {
            Element elm = getUserElement(doc, loginId);
            
            if (elm != null)
                user = getGameUser(elm);
        }
        
        return user;
    }

    private static GameUser getGameUser(Element userNode)
    {
        GameUser user = null;
        String loginId = userNode.getAttribute(LOGINID_ATTR);
        NodeList nl2 = userNode.getElementsByTagName(PASSWORD_TAG);
        String password = getNodeText(nl2.item(0));
        
        NodeList nl3 = userNode.getElementsByTagName(USERINFO_TAG);
        String userInfo = decryptText(getNodeText(nl3.item(0)), loginId);
        StringReader sr = new StringReader(userInfo);
        Properties props = new Properties();
        
        try
        {
            props.load(sr);
            
            user = new GameUser(loginId,
                                password,
                                props.getProperty(USER_NAME_PROP),
                                props.getProperty(USER_ALIAS_PROP),
                                props.getProperty(AVATAR_PATH_PROP),
                                props.getProperty(USER_LOCALE_PROP),
                                props.getProperty(USER_VOICEID_PROP),
                                getIntProp(props, GAME_MONEY_TYPE_PROP, 0),
                                BonusCards.valueOf(props.getProperty(GAME_BONUS_CARDS_PROP)),
                                Boolean.valueOf(props.getProperty(GAME_MUTE_SOUND_PROP)),
                                GameConfig.valueOf(GameUserType.class, userNode.getAttribute(USER_TYPE_ATTR)),
                                Long.parseLong(props.getProperty(MONEY_PROP)),
                                Integer.parseInt(props.getProperty(ALL_IN_COUNT_PROP)),
                                Integer.parseInt(props.getProperty(WINS_PROP)),
                                Integer.parseInt(props.getProperty(DRAWS_PROP)),
                                Integer.parseInt(props.getProperty(LOSES_PROP)),
                                getLongProp(props, HIGHEST_MONEY_PROP, _initialMoney),
                                getLongProp(props, LOWEST_MONEY_PROP, _initialMoney),
                                getIntProp(props, BEST_POINTS_PROP, 0),
                                getLongProp(props, BEST_MONEY_PROP, 0),
                                getIntProp(props, WORST_POINTS_PROP, 0),
                                getLongProp(props, WORST_MONEY_PROP, 0));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return user;
    }
    
    private static long getLongProp(Properties props, String propName, long defValue)
    {
        String propValue = props.getProperty(propName);
        
        return propValue == null ? defValue : Long.parseLong(propValue);
    }
    
    private static int getIntProp(Properties props, String propName, int defValue)
    {
        String propValue = props.getProperty(propName);
        
        return propValue == null ? defValue : Integer.parseInt(propValue);
    }
    
    private static void setGameUserNode(Element userNode, GameUser user)
    {
        userNode.setAttribute(LOGINID_ATTR, user.getLoginId());
        userNode.setAttribute(USER_TYPE_ATTR, user.getUserType().name());
        
        if (user.getPassword() != null && user.getPassword().length() > 0)
        {
            Element elmPassword = getOrCreateChildElement(userNode, PASSWORD_TAG);
            elmPassword.setTextContent(user.getPassword());
        }
        
        Element elmUserInfo = getOrCreateChildElement(userNode, USERINFO_TAG);
        
        Properties props = new Properties();
        props.setProperty(USER_NAME_PROP, user.getUserName());
        props.setProperty(USER_ALIAS_PROP, user.getUserAlias());
        props.setProperty(AVATAR_PATH_PROP, user.getAvatarPath());
        props.setProperty(USER_LOCALE_PROP, user.getUserLocale());
        props.setProperty(USER_VOICEID_PROP, user.getUserVoiceId());
        props.setProperty(GAME_MONEY_TYPE_PROP, Integer.toString(user.getGameMoneyType()));
        props.setProperty(GAME_BONUS_CARDS_PROP, BonusCards.getPropString(user.getGameBonusCards()));
        props.setProperty(GAME_MUTE_SOUND_PROP, Boolean.toString(user.getMuteSound()));
        props.setProperty(MONEY_PROP, Long.toString(user.getMoney()));
        props.setProperty(ALL_IN_COUNT_PROP, Integer.toString(user.getAllInCount()));
        props.setProperty(WINS_PROP, Integer.toString(user.getWins()));
        props.setProperty(DRAWS_PROP, Integer.toString(user.getDraws()));
        props.setProperty(LOSES_PROP, Integer.toString(user.getLoses()));
        props.setProperty(HIGHEST_MONEY_PROP, Long.toString(user.getHighestMoney()));
        props.setProperty(LOWEST_MONEY_PROP, Long.toString(user.getLowestMoney()));
        props.setProperty(BEST_POINTS_PROP, Integer.toString(user.getBestPoints()));
        props.setProperty(BEST_MONEY_PROP, Long.toString(user.getBestMoney()));
        props.setProperty(WORST_POINTS_PROP, Integer.toString(user.getWorstPoints()));
        props.setProperty(WORST_MONEY_PROP, Long.toString(user.getWorstMoney()));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            props.store(baos, null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        String userInfo = encryptText(baos.toString(), user.getLoginId());
        elmUserInfo.setTextContent(userInfo);
    }
    
    private static Element getUserElement(Document element, String loginId)
    {
        NodeList nl = element.getElementsByTagName(USER_TAG);
        Element elmUser = null;
        
        for (int i = 0; elmUser == null && i < nl.getLength(); i++)
        {
            Element elm = (Element)nl.item(i);
            if (loginId.equals(elm.getAttribute(LOGINID_ATTR)))
                elmUser = elm;
        }
        
        return elmUser;
    }
    
    private static Element getOrCreateChildElement(Element parent, String nodeName)
    {
        Element child = null;
        Node nodeChild = parent.getFirstChild();
        
        while (nodeChild != null && child == null)
        {
            if (nodeChild.getNodeType() == Node.ELEMENT_NODE
                && nodeName.equals(nodeChild.getNodeName()))
                child = (Element)nodeChild;
            else
                nodeChild = nodeChild.getNextSibling();
        }
        
        if (child == null)
        {
            child = parent.getOwnerDocument().createElement(nodeName);
            parent.appendChild(child);
        }

        return child;
    }
    
    private static String getNodeText(Node elm)
    {
        String text = null;
        Node node = elm != null ? elm.getFirstChild() : null;
        
        while (node != null && text == null)
        {
            if (node.getNodeType() == Node.TEXT_NODE)
                text = node.getTextContent();
            else
                node = node.getNextSibling();
        }
        
        return text;
    }
    
    private static Document getConfigDocument()
    {
        Document doc = null;
        File cfgFile = new File(getUserConfigFilePath());
        
        if (cfgFile.exists())
        {
            try
            {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                doc = db.parse(cfgFile);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        return doc;
    }
    
    private static String getUserConfigFilePath()
    {
        if (_userConfigPath == null)
            _userConfigPath = getUserConfigFolder() + File.separatorChar + USER_CONFIG_FILE;
        
        return _userConfigPath;
    }

    private static String encryptText(String text, String key)
    {
        String encrypted;
        
        try
        {
            SecretKeySpec ks = new SecretKeySpec(generateKey(key), _cipherAlgorithm);
            Cipher cipher = Cipher.getInstance(_cipherAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, ks);
            byte[] encryptedBytes = cipher.doFinal(text.getBytes());
            encrypted = new String(Base64Coder.encode(encryptedBytes));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            encrypted = text;
        }
        
        return encrypted;
    }
    
    private static String decryptText(String text, String key)
    {
        String decrypted;
        
        try
        {
            SecretKeySpec ks = new SecretKeySpec(generateKey(key), _cipherAlgorithm);
            Cipher cipher = Cipher.getInstance(_cipherAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, ks);
            byte[] decryptedBytes = cipher.doFinal(Base64Coder.decode(text));
            decrypted = new String(decryptedBytes);
        }
        catch (Exception e)
        {
            decrypted = text;
        }
        
        return decrypted;
    }
    
    private static byte[] generateKey(String key)
    {
        byte[] desKey = new byte[8];
        byte[] bkey = key.getBytes();
        
        if (bkey.length < desKey.length)
        {
            System.arraycopy(bkey, 0, desKey, 0, bkey.length);
            
            for (int i = bkey.length; i < desKey.length; i++)
                desKey[i] = 0;
        }
        else
            System.arraycopy(bkey, 0, desKey, 0, desKey.length);
        
        return desKey;
    }
}
