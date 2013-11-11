package org.gs.game.gostop.dlg;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.gs.game.gostop.MainFrame;
import org.gs.game.gostop.Resource;
import org.gs.game.gostop.config.GameUser;

public class PlayerRankingDlg extends JDialog implements ActionListener
{
    private static final long serialVersionUID = 2065181419353619315L;

    public static final String PLAYER_RANKING_TITLE = "player.ranking.title";
    
    private static final String PLAYER_RANKING_LOGIN ="player.ranking.login";
    private static final String PLAYER_RANKING_NAME ="player.ranking.name";
    private static final String PLAYER_RANKING_ALIAS ="player.ranking.alias";
    private static final String PLAYER_RANKING_MONEY ="player.ranking.money";
    private static final String PLAYER_RANKING_REFILL ="player.ranking.refill";
    private static final String PLAYER_RANKING_WINS = "player.ranking.wins";
    private static final String PLAYER_RANKING_WIN_RATIO = "player.ranking.win.ratio";
    private static final String PLAYER_RANKING_LOSES = "player.ranking.loses";
    private static final String PLAYER_RANKING_DRAWS = "player.ranking.draws";
    private static final String PLAYER_RANKING_HIGHEST = "player.ranking.highest";
    private static final String PLAYER_RANKING_LOWEST = "player.ranking.lowest";
    private static final String PLAYER_RANKING_BEST_POINTS = "player.ranking.best.points";
    private static final String PLAYER_RANKING_BEST_MONEY = "player.ranking.best.money";
    private static final String PLAYER_RANKING_WORST_POINTS = "player.ranking.worst.points";
    private static final String PLAYER_RANKING_WORST_MONEY = "player.ranking.worst.money";
    
    private JTable rankTable;
    private JButton okButton;
    
    private static class PlayerTableModel extends AbstractTableModel
    {
        private static final long serialVersionUID = -2608377239771461952L;
        private GameUser[] players;
        private String[] columns =
        {
            Resource.getProperty(PLAYER_RANKING_LOGIN),
            Resource.getProperty(PLAYER_RANKING_NAME),
            Resource.getProperty(PLAYER_RANKING_ALIAS),
            Resource.getProperty(PLAYER_RANKING_MONEY),
            Resource.getProperty(PLAYER_RANKING_REFILL),
            Resource.getProperty(PLAYER_RANKING_WINS),
            Resource.getProperty(PLAYER_RANKING_WIN_RATIO),
            Resource.getProperty(PLAYER_RANKING_LOSES),
            Resource.getProperty(PLAYER_RANKING_DRAWS),
            Resource.getProperty(PLAYER_RANKING_HIGHEST),
            Resource.getProperty(PLAYER_RANKING_LOWEST),
            Resource.getProperty(PLAYER_RANKING_BEST_POINTS),
            Resource.getProperty(PLAYER_RANKING_BEST_MONEY),
            Resource.getProperty(PLAYER_RANKING_WORST_POINTS),
            Resource.getProperty(PLAYER_RANKING_WORST_MONEY),
        };
        
        private PlayerTableModel()
        {
            players = null;
        }
        
        public String getColumnName(int col)
        {
            return columns[col];
        }
        
        public Class<?> getColumnClass(int col)
        {
            return getValueAt(0, col).getClass();
        }

        public int getRowCount()
        {
            return players == null ? 0 : players.length;
        }

        public int getColumnCount()
        {
            return columns.length;
        }

        public Object getValueAt(int row, int col)
        {
            GameUser player = players[row];
            Object value = null;
            
            if (col == 0)
                value = player.getLoginId();
            else if (col == 1)
                value = player.getUserName();
            else if (col == 2)
                value = player.getUserAlias();
            else if (col == 3)
                value = player.getMoney();
            else if (col == 4)
                value = player.getAllInCount();
            else if (col == 5)
                value = player.getWins();
            else if (col == 6)
                value = player.getWins() / (float)(player.getWins()+player.getLoses());
            else if (col == 7)
                value = player.getLoses();
            else if (col == 8)
                value = player.getDraws();
            else if (col == 9)
                value = player.getHighestMoney();
            else if (col == 10)
                value = player.getLowestMoney();
            else if (col == 11)
                value = player.getBestPoints();
            else if (col == 12)
                value = player.getBestMoney();
            else if (col == 13)
                value = player.getWorstPoints();
            else if (col == 14)
                value = player.getWorstMoney();
            
            return value;
        }
        
        private void setPlayers(GameUser[] players)
        {
            this.players = players;
            
            fireTableDataChanged();
        }
    }
    
    public PlayerRankingDlg(Frame parent)
    {
        super(parent, Resource.getProperty(PLAYER_RANKING_TITLE), false);

        initContentPane();
        refreshRanking();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getRootPane().setDefaultButton(okButton);
    }

    public void refreshRanking()
    {
        PlayerTableModel tableModel = (PlayerTableModel)rankTable.getModel();
        
        tableModel.setPlayers(GameUser.getGameUsers());
    }
    
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == okButton)
            dispose();
    }
    
    public void dispose()
    {
        super.dispose();
        
        if (getParent() instanceof MainFrame)
            ((MainFrame)getParent()).onChildDlgDisposed(this);
    }
    
    private void initContentPane()
    {
        Container contentPane = getContentPane();
        JPanel mainPanel;

        if (contentPane instanceof JPanel)
            mainPanel = (JPanel)contentPane;
        else
        {
            mainPanel = new JPanel();
            setContentPane(mainPanel);
        }
        
        mainPanel.setBorder(new CompoundBorder(mainPanel.getBorder(),
                                               new EmptyBorder(10,10,10,10)));   // margins
        mainPanel.setLayout(new BorderLayout(6, 6));

        rankTable = new JTable(new PlayerTableModel());
        rankTable.setAutoCreateRowSorter(true);
        rankTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        initColumnSizes();
        
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(rankTable);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        Box boxButtons = new Box(BoxLayout.X_AXIS);
        boxButtons.add(Box.createHorizontalGlue());
        okButton = new JButton(Resource.getStandardProperty(Resource.STD_OK_BUTTON));
        okButton.addActionListener(this);
        boxButtons.add(okButton);
        boxButtons.add(Box.createHorizontalGlue());
        mainPanel.add(boxButtons, BorderLayout.SOUTH);
    }

    private void initColumnSizes()
    {
        String[] headers = ((PlayerTableModel)rankTable.getModel()).columns;
        TableCellRenderer tcr = rankTable.getTableHeader().getDefaultRenderer();
        TableColumnModel tcm = rankTable.getColumnModel();
        TableColumn column = null;
        Component comp;

        for (int i = 0; i < rankTable.getColumnCount(); i++)
        {
            column = tcm.getColumn(i);
            comp = tcr.getTableCellRendererComponent(rankTable, headers[i],
                                                     false, false, 0, 0);
            column.setPreferredWidth(comp.getPreferredSize().width);
        }
    }
}
