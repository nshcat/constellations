package com.constellations.android.livewallpaper;

import android.opengl.GLES31;
import android.util.Log;

import java.nio.ByteBuffer;

public class RenderTarget
{
    protected int destTexture;
    protected int frameBuffer;
    protected boolean initialized = false;
    protected int previousBuffer;
    protected Dimensions dims;  //< Dimensions in pixels

    /**
     * Set this instance as the currently active render target. This will cause all
     * draw calls to be redirected to the associated frame buffer object.
     * This will also set the viewport to match the given texture dimensions.
     */
    public void enable()
    {
        // Save current buffer
        save_fbo();

        // Bind framebuffer
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, frameBuffer);

        // Set viewport
        GLES31.glViewport(0, 0, dims.getWidth(), dims.getHeight());
    }

    /**
     * Disable the render redirection. All draw calls after this will be handled normally.
     */
    public void disable()
    {
        // Restore previous buffer
        restore_fbo();
    }

    public int getTexHandle()
    {
        return destTexture;
    }

    protected void save_fbo()
    {
        int[] retVal = new int[1];
        GLES31.glGetIntegerv(GLES31.GL_FRAMEBUFFER_BINDING, retVal, 0);
        this.previousBuffer = retVal[0];
    }

    protected void restore_fbo()
    {
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0 /*previousBuffer*/);
        previousBuffer = 0;
    }

    /**
     * Change the dimension of the texture associated with the managed frame buffer object.
     * @param d New texture dimensions
     */
    public void resize(Dimensions d)
    {
        this.dims = d;

        if(initialized)
        {
            GLES31.glDeleteTextures(1, new int[]{destTexture}, 0);
        }

        // --- Destination texture
        int[] texId = new int[1];
        GLES31.glGenTextures(1, texId, 0);
        this.destTexture = texId[0];

        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, this.destTexture);

        ByteBuffer buffer = ByteBuffer.allocate(dims.getWidth() * dims.getHeight() * 3);

        GLES31.glTexImage2D(GLES31.GL_TEXTURE_2D, 0, GLES31.GL_RGB, d.getWidth(), d.getHeight(), 0, GLES31.GL_RGB, GLES31.GL_UNSIGNED_BYTE, buffer);

        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_NEAREST);
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_NEAREST);
        // ---

        if(!initialized)
        {
            Log.d("RenderTarget", "Creating frame buffer object");

            // --- Framebuffer object
            int[] fbo = new int[1];
            GLES31.glGenFramebuffers(1, fbo, 0);
            this.frameBuffer = fbo[0];
            // ---
        }

        save_fbo();
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, this.frameBuffer);
        GLES31.glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0, GLES31.GL_TEXTURE_2D, destTexture, 0);
        restore_fbo();

        initialized = true;
    }

    /**
     * Bind the texture associated with this render target to texture unit 0.
     */
    public void useTexture()
    {
        useTexture(GLES31.GL_TEXTURE0);
    }

    /**
     *
     * Bind the texture associated with this render target to given texture unit.
     * @param texUnit Texture unit to bind render target texture to
     */
    public void useTexture(int texUnit)
    {
        GLES31.glActiveTexture(texUnit);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, destTexture);
    }
}
