package com.constellations.android.livewallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

public class Configuration
{
    private static Configuration instance;

    private Color customColor;
    private boolean useCustomColor;
    private int resolutionDivisor;

    public synchronized static Configuration getInstance()
    {
        if(instance == null)
            instance = new Configuration();

        return instance;
    }

    private Configuration()
    {
    }

    public synchronized void update(Context c)
    {
        update(PreferenceManager.getDefaultSharedPreferences(c));
    }

    public synchronized void update(SharedPreferences prefs)
    {
        // Get color
        int customColorInt = prefs.getInt("custom_color", 0xFFFFFFFF);
        customColor = Color.valueOf(customColorInt);

        useCustomColor = prefs.getBoolean("use_fixed_color", false);

        resolutionDivisor = parseInt(prefs, "res_divisor", "4");
    }

    private int parseInt(SharedPreferences p, String key, String def)
    {
        try
        {
            return Integer.parseInt(p.getString(key, def));
        }
        catch(NumberFormatException e)
        {
            return Integer.parseInt(def);
        }
    }

    public int getResolutionDivisor()
    {
        return resolutionDivisor;
    }

    public Color getCustomColor()
    {
        return customColor;
    }

    public boolean isUseCustomColor()
    {
        return useCustomColor;
    }
}
