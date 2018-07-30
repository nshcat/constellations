package com.constellations.android.livewallpaper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public abstract class GLES31WallpaperService extends GLWallpaperService {
	@Override
	public Engine onCreateEngine()
	{
		return new GLES31Engine();
	}

	class GLES31Engine extends GLEngine {

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);

			// Check if the system supports OpenGL ES 3.1.
			final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

			final boolean supportsEs31 = configurationInfo.reqGlEsVersion >= 0x30001;

			if (supportsEs31)
			{
				// Request an OpenGL ES 3.1 compatible context.
				setEGLContextClientVersion(3);

				// On Honeycomb+ devices, this improves the performance when
				// leaving and resuming the live wallpaper.
				setPreserveEGLContextOnPause(true);

				// Set the renderer to our user-defined renderer.
				setRenderer(getNewRenderer());
			}
			else
			{
				Log.e("GLES31WallpaperService", "Could not create OpenGL ES 3.1 context");
				System.exit(-1);
			}
		}
	}
	
	abstract Renderer getNewRenderer();
}
