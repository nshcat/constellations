package com.constellations.android.livewallpaper;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;
import com.constellations.android.R;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Renderer for the "Constellations" live wallpaper
 */
public class ConstellationsRenderer implements GLSurfaceView.Renderer
{
    private static final String TAG = "ConstellationsRenderer";

    /**
     * The current screen width
     */
    private int width;

    /**
     * The current screen height
     */
    private int height;

    /**
     * The shader program used to draw the scene to frame buffer object
     */
    private ShaderProgram firstPassProgram;

    /**
     * The shader program used to draw the FBO texture to a full screen quad
     */
    private ShaderProgram secondPassProgram;


    private int empty_vbo;
    private int empty_vao;

    /**
     * Render target used to render scene to a texture. This is useful since it
     * allows rendering at a lower resolution to save computing power.
     */
    private RenderTarget renderTarget;

    /**
     * The current application context. This is used to retrieve shader resources.
     */
    private Context context;

    /**
     * Flag indicating if the first frame already happened or not. This is used
     * to mark the time of startup (which we interpret as the time when the first
     * frame was drawn, since this causes the wallpaper to always start exactly the
     * same way, independent of a possibly varying startup time)
     */
    private boolean firstFrame = true;

    /**
     * The point in time the application rendered the first frame.
     */
    private long startTime;


    public ConstellationsRenderer(Context c)
    {
        this.context = c;

        ContextManager.setContext(c);
    }


    /**
     * Called when the drawing surface is created for the first time. We initialize all objects here
     * that we need to render a frame.
     * @param glUnused Unused
     * @param config Unused
     */
    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        // Set background color to black
        GLES31.glClearColor(0.f, 0.f, 0.f, 0.f);

        // Try to create the shader programs
        try
        {
            firstPassProgram = new ShaderProgram();
            firstPassProgram.attachShader(ShaderType.FRAGMENT_SHADER, context, R.raw.shader_fragment);
            firstPassProgram.attachShader(ShaderType.VERTEX_SHADER, context, R.raw.shader_vertex);
            firstPassProgram.linkProgram();

            secondPassProgram = new ShaderProgram();
            secondPassProgram.attachShader(ShaderType.FRAGMENT_SHADER, context, R.raw.quad_fragment);
            secondPassProgram.attachShader(ShaderType.VERTEX_SHADER, context, R.raw.quad_vertex);
            secondPassProgram.linkProgram();
        }
        catch(ShaderException ex)
        {
            Log.e(TAG, "Failed to create shader programs: \n" + ex.getMessage());
            System.exit(-1);
        }


        int[] ids = new int[1];
        GLES31.glGenBuffers(1, ids, 0);
        empty_vbo = ids[0];

        int[] ids2 = new int[1];
        GLES31.glGenVertexArrays(1, ids2, 0);
        empty_vbo = ids2[0];

        // Create our render target. Its dimensions will be set later, since we do not
        // know the screen size yet.
        renderTarget = new RenderTarget();

        // Force one initial update of the configuration. Otherwise, changes to the settings would
        // not immediately apply when using the wallpaper preview screen.
        Configuration.getInstance().update(context);
    }

    /**
     * Called every time the dimensions of the drawing surface changed. We use this to set the
     * dimensions of the screen in both the shader uniforms as well as the first-pass render target.
     * @param glUnused Unused
     * @param width New width of the drawing surface
     * @param height New height of the drawing surface
     */
    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {
        this.width = width;
        this.height = height;

        int divisor = Configuration.getInstance().getResolutionDivisor();
        renderTarget.resize(new Dimensions(width / divisor, height / divisor));

        // Update screen dimensions on the GPU
        setDimensionsUniform();
        setQuadTexUniform();
        setConfigUniforms();
    }

    /**
     * Called every time the system wants us to draw a new frame.
     * @param glUnused
     */
    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        // If this is the first frame, we have to remember
        // the exact point in time for later calculations
        if(firstFrame)
        {
            firstFrame = false;
            startTime = SystemClock.uptimeMillis();
        }

        // Calulcate new time-since-startup value and send it to the GPU
        final long currentTime = SystemClock.uptimeMillis() - startTime;
        setTimeUniform(currentTime);

        // Actually render the frame
        renderFrame();
    }

    /**
     * Render a new frame. This is done in two passes: first the scene is drawn to a GPU texture
     * by using a framebuffer object, and then it the contents of that texture are drawn to a full
     * screen quad covering the whole drawing surface. This is done in order to allow rendering at a
     * lower resolution than the screen to save computing resources and to not strain the battery
     * too much.
     */
    private void renderFrame()
    {
        // === FIRST PASS
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);

        firstPassProgram.useProgram();

        // Use empty vbo and vao
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, empty_vbo);
        GLES31.glBindVertexArray(empty_vao);

        // Redirect rendering output to a texture using our render target
        // instance
        renderTarget.enable();

        GLES31.glDrawArraysInstanced(GLES31.GL_TRIANGLES, 0, 6, 1);

        renderTarget.disable();
        // ===============


        // === SECOND PASS
        GLES31.glViewport(0, 0, width, height);

        secondPassProgram.useProgram();

        // Use the render target as a source this time, allowing access to the
        // texture with the previous render result
        renderTarget.useTexture();

        GLES31.glDrawArraysInstanced(GLES31.GL_TRIANGLES, 0, 6, 1);
        // ===============
    }

    private void setConfigUniforms()
    {
        firstPassProgram.useProgram();

        final int useCustomColorPos = GLES31.glGetUniformLocation(firstPassProgram.getHandle(), "iUseCustomColor");
        GLES31.glUniform1i(useCustomColorPos, Configuration.getInstance().isUseCustomColor() ? 1 : 0);

        Color customColor = Configuration.getInstance().getCustomColor();

        final int customColorPos = GLES31.glGetUniformLocation(firstPassProgram.getHandle(), "iCustomColor");
        GLES31.glUniform4f(customColorPos, customColor.red(), customColor.green(), customColor.blue(), customColor.alpha());
    }

    private void setQuadTexUniform()
    {
        secondPassProgram.useProgram();

        final int quad_tex_pos = GLES31.glGetUniformLocation(secondPassProgram.getHandle(), "quad_tex");
        GLES31.glUniform1i(quad_tex_pos, 0);
    }

    private void setDimensionsUniform()
    {
        firstPassProgram.useProgram();

        int divisor = Configuration.getInstance().getResolutionDivisor();

        final int resolution_pos = GLES31.glGetUniformLocation(firstPassProgram.getHandle(), "iResolution");
        GLES31.glUniform3f(resolution_pos, width/divisor, height/divisor, 0.f);

        //GLES31.glUniform3f(resolution_pos, width/10.f, height/10.f, 0.f);
        // ^~~~ very nice effect
    }

    private void setTimeUniform(long currentTime)
    {
        firstPassProgram.useProgram();

        final int time_pos = GLES31.glGetUniformLocation(firstPassProgram.getHandle(), "iTime");
        GLES31.glUniform1f(time_pos, ((float)currentTime)/1000.f);
    }
}
