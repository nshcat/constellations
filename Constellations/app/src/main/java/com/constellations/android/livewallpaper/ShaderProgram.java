package com.constellations.android.livewallpaper;

import android.content.Context;
import android.opengl.GLES31;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * A class managing the creation and usage of a OpenGL program object.
 */
public class ShaderProgram
{
    /**
     * The native OpenGL handle of this program object
     */
    protected int handle;

    /**
     * A flag indicating whether this program was already linked
     */
    protected boolean isLinked;

    /**
     * Create new empty shader program object
     */
    public ShaderProgram() throws ShaderException
    {
        // Attempt to create shader program object and check if it
        // succeeded
        handle = GLES31.glCreateProgram();

        if(handle == 0)
        {
            throw new ShaderException("Failed to create program object");
        }
    }

    /**
     * Mark the program object managed by this instance as the currently used program
     */
    public void useProgram()
    {
        if(isLinked)
        {
            GLES31.glUseProgram(handle);
        }
        else
        {
            throw new IllegalStateException("Tried to use a program object that was not linked yet");
        }
    }

    /**
     * Attach new shader with given type, and read shader source from resource with given ID
     * @param type Type of shader to attach
     * @param context Context used to retrieve resource
     * @param resourceId Resource id of shader source code file
     * @throws ShaderException
     */
    public void attachShader(ShaderType type, Context context, int resourceId) throws ShaderException
    {
        // Open resource stream
        InputStream stream = context.getResources().openRawResource(resourceId);

        // Read contents and attach as shader
        try
        {
            java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
            String contents = s.hasNext() ? s.next() : "";

            stream.close();

            attachShader(type, contents);
        }
        catch(IOException e)
        {
            throw new ShaderException("Failed to load shader source from resource: " + e.getMessage());
        }
    }

    /**
     * Attach new shader with given type and source code to the program object that is
     * managed by this instance
     * @param type Type of shader to attach
     * @param source Shader source code
     * @throws ShaderException If compilation failed
     */
    public void attachShader(ShaderType type, String source) throws ShaderException
    {
        // Create empty shader object
        int shaderHandle = GLES31.glCreateShader(type.getOpenglValue());

        // Check for success
        if(shaderHandle != 0)
        {
            // Attach shader source and attempt to compile it
            GLES31.glShaderSource(shaderHandle, source);
            GLES31.glCompileShader(shaderHandle);

            // Check for success
            int[] status = new int[1];
            GLES31.glGetShaderiv(shaderHandle, GLES31.GL_COMPILE_STATUS, status, 0);

            if(status[0] == 0)
            {
                // Retrieve compile log
                String shaderLog = GLES31.glGetShaderInfoLog(shaderHandle);

                // Build error message and signal error
                String error = "Failed to compile shader:\n" + shaderLog;

                Log.d("ShaderProgram", error);
                throw new ShaderException(error);
            }
            else
            {
                // Everything went okay, attach it to the program object
                GLES31.glAttachShader(handle, shaderHandle);
            }
        }
        else
        {
            throw new ShaderException("Failed to create shader object");
        }
    }

    /**
     * Link all attached shader programs together to create a valid shader program
     * @throws ShaderException If linking fails
     */
    public void linkProgram() throws ShaderException
    {
        // A null pointer will be correctly handled by the implementation and interpreted
        // as "no attributes"
        linkProgram(null);
    }

    /**
     * Link all attached shader programs together to create a valid shader program
     * @param attributes Shader program attributes to bind
     * @throws ShaderException If linking fails
     */
    public void linkProgram(String[] attributes) throws ShaderException
    {
        // Bind attributes
        if(attributes != null)
        {
            for(int i = 0; i < attributes.length; ++i)
            {
                GLES31.glBindAttribLocation(handle, i, attributes[i]);
            }
        }

        // Attempt to link program
        GLES31.glLinkProgram(handle);

        // Check for success
        int[] status = new int[1];
        GLES31.glGetProgramiv(handle, GLES31.GL_LINK_STATUS, status, 0);

        if(status[0] == 0)
        {
            // Retrieve link log
            String programLog = GLES31.glGetProgramInfoLog(handle);

            // Build error message and signal error
            String error = "Failed to link shader program:\n" + programLog;

            Log.d("ShaderProgram", error);
            throw new ShaderException(error);
        }
        else // Linking succeeded
        {
            isLinked = true;
        }
    }

    /**
     * The native OpenGL handle of this program object
     */
    public int getHandle()
    {
        return handle;
    }

    /**
     * A flag indicating whether this program was already linked
     */
    public boolean isLinked()
    {
        return isLinked;
    }
}
