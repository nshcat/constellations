package com.constellations.android.livewallpaper;

public class Dimensions
{
    private int w;
    private int h;

    public Dimensions()
    {
        this.w = 0;
        this.h = 0;
    }

    public Dimensions(int w_, int h_)
    {
        this.w = w_;
        this.h = h_;
    }

    public int getWidth()
    {
        return w;
    }

    public int getHeight()
    {
        return h;
    }

    public void setWidth(int val)
    {
        w = val;
    }

    public void setHeight(int val)
    {
        h = val;
    }
}
