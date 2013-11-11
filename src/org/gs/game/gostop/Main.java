package org.gs.game.gostop;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Random;

import javax.swing.UIManager;

public class Main
{
    private static Random _random = new Random(System.currentTimeMillis());
    
    public static void main(String[] args)
        throws Exception
    {
        //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        
        MainFrame mainFrame = new MainFrame();
        
        mainFrame.setDefaultCloseOperation(MainFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        moveToCenter(mainFrame);
        
        try
        {
            // supported from java 1.6 update 10
            Class<?> awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
            java.lang.reflect.Method mSetWindowOpaque
                = awtUtilitiesClass.getMethod("setWindowOpaque",
                      java.awt.Window.class, boolean.class);
            mSetWindowOpaque.invoke(null, mainFrame, false);
                    
            java.lang.reflect.Method mSetWindowOpacity
                = awtUtilitiesClass.getMethod("setWindowOpacity",
                                              java.awt.Window.class, float.class);
            mSetWindowOpacity.invoke(null, mainFrame, Float.valueOf(0.75f));
        }
        catch (Throwable ex)
        {
        }
        
        mainFrame.setVisible(true);
    }
    
    public static void moveToCenter(Component component)
    {
        Dimension dParent;
        Dimension dComponent = component.getSize();
        Component parent = component.getParent();
        Point offset;

        if (parent == null)
        {
            dParent = Toolkit.getDefaultToolkit().getScreenSize();
            offset = new Point(0, 0);
        }
        else
        {
            dParent = parent.getSize();
            offset = parent.getLocation();
        }
        
        component.setLocation(offset.x + (int)(dParent.getWidth()-dComponent.getWidth())/2,
                              offset.y + (int)(dParent.getHeight()-dComponent.getHeight())/2);
    }
    
    public static Random getRandom()
    {
        return _random;
    }
}
