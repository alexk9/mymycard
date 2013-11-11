package org.gs.game.gostop.dlg;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.gs.game.gostop.CardSize;
import org.gs.game.gostop.Main;
import org.gs.game.gostop.Resource;
import org.gs.game.gostop.event.*;
import org.gs.game.gostop.item.CardItem;
import org.gs.game.gostop.sound.GameSoundManager;

/**
 * 질의 대화상자
 * 질의 대화상자는 질의에 따라 시간 제한이 있을 수 있는데, 그 시간동안 선택을 하지 않을 경우 NO(아니오)를 선택한 것으로 처리합니다.
 */
public class GameQueryDlg extends GameDialog
{
    private static final long serialVersionUID = -3914711810584625833L;
    
    private static final String DIALOG_TITLE_LBL = "game.query.title";
    public static final String QUERY_FOUR_CARD = "game.query.four.cards";
    public static final String SWING_CARDS = "game.query.swing.cards";
    public static final String SELECT_CARD = "game.query.select.card";
    public static final String QUERY_NINE = "game.query.nine";
    public static final String QUERY_GO = "game.query.go";
    public static final String QUERY_MORE_GAME_ON_DRAW = "game.query.more.game.on.draw";
    
    private static final String QUERY_BUTTON_GO = "game.query.button.go";
    private static final String QUERY_BUTTON_STOP = "game.query.button.stop";
    
    private static Container defaultParent = null;
    
    private String msgId;
    private GameEventType completeEvent;
    private boolean eventOnNoOnly;
    private Object resultObject;
    private List<CardItem> cardItems;
    private int points;
    
    public GameQueryDlg(Container parent, String msgId, GameEventType completeEvent,
                        boolean eventOnNoOnly)
    {
        this(parent, msgId, completeEvent, eventOnNoOnly, null, null);
    }

    public GameQueryDlg(Container parent, String msgId, GameEventType completeEvent,
                        Object resultObject)
    {
        this(parent, msgId, completeEvent, false, resultObject, null);
    }
    
    public GameQueryDlg(Container parent, String msgId, GameEventType completeEvent,
                        List<CardItem> cardItems)
    {
        this(parent, msgId, completeEvent, false, null, cardItems);
    }
    
    public GameQueryDlg(Container parent, String msgId, GameEventType completeEvent,
                        int points)
    {
        this(parent, msgId, completeEvent, false, null, null, points);
    }
    
    public GameQueryDlg(Container parent, String msgId, GameEventType completeEvent,
                        boolean eventOnNoOnly, Object resultObject,
                        List<CardItem> cardItems)
    {
        this(parent, msgId, completeEvent, eventOnNoOnly, resultObject, cardItems, 0);
    }
    
    public GameQueryDlg(Container parent, String msgId, GameEventType completeEvent,
                        boolean eventOnNoOnly, Object resultObject,
                        List<CardItem> cardItems, int points)
    {
        super(getParent(parent), Resource.getProperty(DIALOG_TITLE_LBL));
        
        this.msgId = msgId;
        this.completeEvent = completeEvent;
        this.eventOnNoOnly = eventOnNoOnly;
        this.resultObject = resultObject;
        this.cardItems = cardItems;
        this.points = points;

        initDialog();
        
        GameSoundManager.playSound(GameSoundManager.SOUND_DECISION, null);
    }
    
    public static void setDefaultParent(Container defaultParent)
    {
        GameQueryDlg.defaultParent = defaultParent;
    }
    
    public void actionPerformed(ActionEvent e)
    {
        GameSoundManager.stopAllSound();
        
        dispose();
        
        fireDialogEvent(e.getSource() == yesButton);
    }

    private void initDialog()
    {
        JComponent content = getContentPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        
        content.setBorder(new CompoundBorder(content.getBorder(),
                          new EmptyBorder(10,10,10,10)));   // margins
        content.setLayout(gbl);

        addComponent(content, Box.createVerticalStrut(6), -1f, gbc);

        JLabel jLabel = new JLabel(Resource.format(msgId, points), JLabel.LEADING);
        addComponent(content, jLabel, -1f, gbc);
        
        addComponent(content, Box.createVerticalStrut(6), -1f, gbc);

        Box boxButtons = Box.createHorizontalBox();
        addButtons(boxButtons);
        addComponent(content, boxButtons, -1f, gbc);

        // initialize the dialog properties
        setAlpha(0.85f);
        setSize(getPreferredSize());
        Main.moveToCenter(this);

        addComponentListener(new ComponentAdapter()
        {
            public void componentShown(ComponentEvent e)
            {
                yesButton.requestFocus();
            }
        });
    }
    
    public void fireDialogEvent(boolean result)
    {
        setResult(result);
        
        if (completeEvent != null && (eventOnNoOnly == false || result == false))
        {
            GameEvent ge;
            
            if (cardItems == null)
                ge = new GameEvent(this, completeEvent, new GameEventResult(result, resultObject));
            else
                ge = new GameEvent(this, completeEvent, new GameEventResult(resultObject, result ? 0 : 1));
            
            GameEventManager.fireGameEvent(ge, false);
        }
    }

    private void addButtons(Box boxButtons)
    {
        String buttonText;
        
        boxButtons.add(Box.createHorizontalGlue());
        
        if (cardItems == null)
        {
            buttonText = QUERY_GO.equals(msgId) ? Resource.getProperty(QUERY_BUTTON_GO)
                    : Resource.getStandardProperty(Resource.STD_YES_BUTTON);
            yesButton = new GameButton(buttonText);
        }
        else
            yesButton = new GameButton(cardItems.get(0).getCardImageIcon(CardSize.NORMAL));
        yesButton.addActionListener(this);
        boxButtons.add(yesButton);
        yesButton.requestFocusInWindow();
        
        if (cardItems == null)
        {
            buttonText = QUERY_GO.equals(msgId) ? Resource.getProperty(QUERY_BUTTON_STOP)
                    : Resource.getStandardProperty(Resource.STD_NO_BUTTON);
            noButton = new GameButton(buttonText);
        }
        else
            noButton = new GameButton(cardItems.get(1).getCardImageIcon(CardSize.NORMAL));
        noButton.addActionListener(this);
        boxButtons.add(noButton);
    }

    private void addComponent(JComponent content, Component comp, float weightx,
                              GridBagConstraints gbc)
    {
        GridBagLayout gbl = (GridBagLayout)content.getLayout();
    
        if (weightx > 0)
        {
            gbc.gridwidth = 1;
            gbc.weightx = weightx;
        }
        else
            gbc.gridwidth = GridBagConstraints.REMAINDER;
    
        gbl.setConstraints(comp, gbc);
        content.add(comp);
    }
    
    private static Container getParent(Container parent)
    {
        return parent == null ? defaultParent : parent;
    }
}
