package com.constellations.android.livewallpaper;

import android.content.SharedPreferences;
import android.opengl.GLSurfaceView.Renderer;
import android.preference.PreferenceManager;


public class ConstellationsWallpaperService extends GLES31WallpaperService {
	@Override
	Renderer getNewRenderer()
	{
		registerSettingsListener();

		//android.os.Debug.waitForDebugger();

		return new ConstellationsRenderer(this);
	}

	private void registerSettingsListener()
	{
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
				Configuration.getInstance().update(sharedPreferences);
			}
		};

		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(listener);
	}

	private SharedPreferences.OnSharedPreferenceChangeListener listener;
}
