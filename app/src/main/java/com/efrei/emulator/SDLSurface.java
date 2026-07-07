package com.efrei.emulator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SDLSurface extends SurfaceView implements SurfaceHolder.Callback {
    public SDLSurface(Context p_context) {
        super(p_context);
        myInit();
    }

    public SDLSurface(Context p_context, AttributeSet attrs) {
        super(p_context, attrs);
        myInit();
    }

    public SDLSurface(Context p_context, AttributeSet attrs, int defStyle) {
        super(p_context, attrs, defStyle);
        myInit();
    }

    private void myInit() {
        getHolder().addCallback(this);
    }

    // Called when we have a valid drawing surface
    @Override
    public void surfaceCreated(SurfaceHolder p_holder) {
    }

    // Called when the surface is resized
    @Override
    public void surfaceChanged(SurfaceHolder p_holder, int p_format, int p_width, int p_height) {
        NativeApp.onNativeSurfaceChanged(p_holder.getSurface(), p_width, p_height);
        ////
        EmulatorActivity _nativeActivity = (EmulatorActivity) getContext();
        if(_nativeActivity != null) {
            _nativeActivity.startEmuThread();
        }
    }

    // Called when we lose the surface
    @Override
    public void surfaceDestroyed(SurfaceHolder p_holder) {
        NativeApp.onNativeSurfaceChanged(null, 0, 0);
    }
}
