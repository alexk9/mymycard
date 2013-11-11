package org.gs.game.gostop.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserLocale
{
    private static ArrayList<UserLocale> _userLocales = null;
    
    private String id;
    private String name;
    private Locale locale;
    
    public static void addUserLocale(String id, String name)
    {
        if (_userLocales == null)
            _userLocales = new ArrayList<UserLocale>(4);
        
        _userLocales.add(new UserLocale(id, name));
    }
    
    public static List<UserLocale> getUserLocales()
    {
        return _userLocales;
    }
    
    public static UserLocale getUserLocale(String id)
    {
        UserLocale ulocale = null;
        
        if (_userLocales != null)
        {
            for (int i = 0; i < _userLocales.size() && ulocale == null; i++)
            {
                if (id.equals(_userLocales.get(i).getLocaleId()))
                    ulocale = _userLocales.get(i);
            }
        }
        
        return ulocale;
    }
    
    private UserLocale(String id, String name)
    {
        this(id, name, getLocaleFromId(id));
    }
    
    private UserLocale(String id, String name, Locale locale)
    {
        this.id = id;
        this.name = name;
        this.locale = locale;
    }
    
    private static Locale getLocaleFromId(String id)
    {
        Locale locale;
        
        if (id != null)
        {
            int index = id.indexOf('_');
            String language;
            String country;
            if (index > 0)
            {
                language = id.substring(0, index);
                country = id.substring(index+1);
            }
            else
            {
                language = id;
                country = "";
            }
            locale = new Locale(language, country);
        }
        else
            locale = null;
        
        return locale;
    }
    
    public String getLocaleId()
    {
        return id;
    }
    
    public String getLocaleName()
    {
        return name;
    }
    
    public Locale getLocale()
    {
        return locale;
    }
    
    public String toString()
    {
        return name;
    }
}
