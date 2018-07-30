package com.constellations.android.livewallpaper;


import android.content.Context;

import java.lang.ref.WeakReference;

public class ContextManager
{
    private static WeakReference<Context> ctx;

    public static void setContext(Context c)
    {
        ctx = new WeakReference<>(c);
    }

    public static Context getContext()
    {
        return ctx.get();
    }
}
