package com.constellations.android.livewallpaper;

import android.opengl.GLES31;

/**
 * Enumeration used to differentiate between different types of shader source
 * code
 */
public enum ShaderType
{
    FRAGMENT_SHADER(GLES31.GL_FRAGMENT_SHADER),
    VERTEX_SHADER(GLES31.GL_VERTEX_SHADER);

    ShaderType(int value)
    {
        openglValue = value;
    }

    /**
     * The integral value used by OpenGL for this particular shader type
     */
    private int openglValue;

    /**
     * Retrieve the integral value used by OpenGL for this particular shader type
     * @return
     */
    public int getOpenglValue()
    {
        return openglValue;
    }
}
