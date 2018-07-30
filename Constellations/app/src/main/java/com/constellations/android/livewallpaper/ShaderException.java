package com.constellations.android.livewallpaper;

/**
 * An exception type used to signal errors when working with OpenGL shaders
 */
public class ShaderException extends Exception
{
    /**
     * Create empty exception
     */
    public ShaderException()
    {

    }

    /**
     * Create exception with given message
     * @param msg A message detailing the error
     */
    public ShaderException(String msg)
    {
        super(msg);
    }
}
