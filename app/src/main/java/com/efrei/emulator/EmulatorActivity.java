package com.androsx2.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.*;
import java.lang.annotation.Native;

public class EmulatorActivity extends AppCompatActivity {

    private FrameLayout emulatorView;
    private SharedPreferences SharedPreference1;
    private com.google.android.material.button.MaterialButton buttonL1;
    private com.google.android.material.button.MaterialButton buttonR1;
    private com.google.android.material.button.MaterialButton buttonL2;
    private com.google.android.material.button.MaterialButton buttonR2;
    private com.google.android.material.button.MaterialButton buttonL3;
    private com.google.android.material.button.MaterialButton buttonR3;
    private com.google.android.material.button.MaterialButton buttonPadUp;
    private com.google.android.material.button.MaterialButton buttonPadDown;
    private com.google.android.material.button.MaterialButton buttonPadLeft;
    private com.google.android.material.button.MaterialButton buttonPadRight;
    private com.google.android.material.button.MaterialButton buttonTriangle;
    private com.google.android.material.button.MaterialButton buttonCircle;
    private com.google.android.material.button.MaterialButton buttonCross;
    private com.google.android.material.button.MaterialButton buttonSquare;
    private com.google.android.material.button.MaterialButton buttonStart;
    private com.google.android.material.button.MaterialButton buttonSelect;
    private String m_szGamefile = "";
    private HIDDeviceManager mHIDDeviceManager;
    private Thread mEmulationThread = null;

    private boolean isThread() {
        if (mEmulationThread != null) {
            Thread.State _thread_state = mEmulationThread.getState();
            return _thread_state == Thread.State.BLOCKED
                    || _thread_state == Thread.State.RUNNABLE
                    || _thread_state == Thread.State.TIMED_WAITING
                    || _thread_state == Thread.State.WAITING;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emulator);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vues
        emulatorView = findViewById(R.id.emulatorView);

        buttonL1 = findViewById(R.id.buttonL1);
        buttonR1 = findViewById(R.id.buttonR1);
        buttonL2 = findViewById(R.id.buttonL2);
        buttonR2 = findViewById(R.id.buttonR2);
        buttonL3 = findViewById(R.id.buttonL3);
        buttonR3 = findViewById(R.id.buttonR3);

        buttonPadUp = findViewById(R.id.buttonPadUp);
        buttonPadDown = findViewById(R.id.buttonPadDown);
        buttonPadLeft = findViewById(R.id.buttonPadLeft);
        buttonPadRight = findViewById(R.id.buttonPadRight);

        buttonTriangle = findViewById(R.id.buttonTriangle);
        buttonCircle = findViewById(R.id.buttonCircle);
        buttonCross = findViewById(R.id.buttonCross);
        buttonSquare = findViewById(R.id.buttonSquare);

        buttonStart = findViewById(R.id.buttonStart);
        buttonSelect = findViewById(R.id.buttonSelect);

        // Fullscreen
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        SharedPreference1 = getSharedPreferences("Data", Context.MODE_PRIVATE);

        // Chemin ROM
        m_szGamefile = SharedPreference1.getString("gamePath", "");

        copyAssetAll(getApplicationContext(), "bios");
        copyAssetAll(getApplicationContext(), "resources");

        NativeApp.initializeOnce(getApplicationContext());

        mHIDDeviceManager = HIDDeviceManager.acquire(this);
        setSurfaceView(new SDLSurface(this));

        // Settings

        // OpenGL ES Hardware = 12
        // OpenGL ES Software = 13
        // Vulkan Hardware = 14
        if (SharedPreference1.getInt("Renderer", 14) == 12) {
            NativeApp.setDefaultRenderer(12);
        } else if (SharedPreference1.getInt("Renderer", 14) == 13) {
            NativeApp.setDefaultRenderer(13);
        } else if (SharedPreference1.getInt("Renderer", 14) == 14) {
            NativeApp.setDefaultRenderer(14);
        }

        if (SharedPreference1.getBoolean("FastBoot", true)) {
            NativeApp.setEnableFastBoot(true);
        } else {
            NativeApp.setEnableFastBoot(false);
        }

        // Strech = 0
        // Auto 4:3/3:2 = 1
        // 4:3 = 2
        // 16:9 = 3
        if (SharedPreference1.getInt("Display", 2) == 0) {
            NativeApp.setAspectRatio(0);
        } else if (SharedPreference1.getInt("Display", 2) == 1) {
            NativeApp.setAspectRatio(1);
        } else if (SharedPreference1.getInt("Display", 2) == 2) {
            NativeApp.setAspectRatio(2);
        } else if (SharedPreference1.getInt("Display", 2) == 3) {
            NativeApp.setAspectRatio(3);
        }

        if (SharedPreference1.getBoolean("VSync", false)) {
            NativeApp.setVsyncEnable(true);
        } else {
            NativeApp.setVsyncEnable(false);
        }

        // On-Screen Display
        NativeApp.setOsdScale(150.0f);
        if (SharedPreference1.getBoolean("OSD_FPS", false)) {
            NativeApp.setOsdShowFPS(true);
        } else {
            NativeApp.setOsdShowFPS(false);
        }

        if (SharedPreference1.getBoolean("OSD_Stats", false)) {
            NativeApp.setOsdShowGSStats(true);
            NativeApp.setOsdShowSpeed(true);
            NativeApp.setOsdShowCPU(true);
        } else {
            NativeApp.setOsdShowGSStats(false);
            NativeApp.setOsdShowSpeed(false);
            NativeApp.setOsdShowCPU(false);
        }

        if (SharedPreference1.getBoolean("OSD_Resolution", false)) {
            NativeApp.setOsdShowResolution(true);
        } else {
            NativeApp.setOsdShowResolution(false);
        }

        if (SharedPreference1.getBoolean("OSD_Version", false)) {
            NativeApp.setOsdShowVersion(true);
        } else {
            NativeApp.setOsdShowVersion(false);
        }

        NativeApp.setOsdShowMessages(true);
        // DEBUG
        NativeApp.setOsdShowGameInfo(false);

        // configurer les boutons
        configureOnTouchListener(buttonL1, KeyEvent.KEYCODE_BUTTON_L1);
        configureOnTouchListener(buttonR1, KeyEvent.KEYCODE_BUTTON_R1);
        configureOnTouchListener(buttonL2, KeyEvent.KEYCODE_BUTTON_L2);
        configureOnTouchListener(buttonR2, KeyEvent.KEYCODE_BUTTON_R2);
        configureOnTouchListener(buttonL3, KeyEvent.KEYCODE_BUTTON_THUMBL);
        configureOnTouchListener(buttonR3, KeyEvent.KEYCODE_BUTTON_THUMBR);

        configureOnTouchListener(buttonPadUp, KeyEvent.KEYCODE_DPAD_UP);
        configureOnTouchListener(buttonPadDown, KeyEvent.KEYCODE_DPAD_DOWN);
        configureOnTouchListener(buttonPadLeft, KeyEvent.KEYCODE_DPAD_LEFT);
        configureOnTouchListener(buttonPadRight, KeyEvent.KEYCODE_DPAD_RIGHT);

        configureOnTouchListener(buttonTriangle, KeyEvent.KEYCODE_BUTTON_Y);
        configureOnTouchListener(buttonCircle, KeyEvent.KEYCODE_BUTTON_B);
        configureOnTouchListener(buttonCross, KeyEvent.KEYCODE_BUTTON_A);
        configureOnTouchListener(buttonSquare, KeyEvent.KEYCODE_BUTTON_X);

        configureOnTouchListener(buttonStart, KeyEvent.KEYCODE_BUTTON_START);
        configureOnTouchListener(buttonSelect, KeyEvent.KEYCODE_BUTTON_SELECT);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void configureOnTouchListener(View button, int keyCode) {
        button.setOnTouchListener((view, event) -> {
            view.onTouchEvent(event);
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    NativeApp.setPadButton(keyCode, 110, true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    NativeApp.setPadButton(keyCode, 0, false);
                    break;
            }
            return true;
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        NativeApp.pause();

        if (mHIDDeviceManager != null) {
            mHIDDeviceManager.setFrozen(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        NativeApp.resume();

        if (mHIDDeviceManager != null) {
            mHIDDeviceManager.setFrozen(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NativeApp.shutdown();

        if (mHIDDeviceManager != null) {
            HIDDeviceManager.release(mHIDDeviceManager);
            mHIDDeviceManager = null;
        }

        if (mEmulationThread != null) {
            try {
                mEmulationThread.join();
                mEmulationThread = null;
            }
            catch (InterruptedException ignored) {}
        }
    }

    public static void copyAssetAll(Context p_context, String srcPath) {
        AssetManager assetMgr = p_context.getAssets();
        String[] assets = null;
        try {
            String destPath = p_context.getExternalFilesDir(null) + File.separator + srcPath;
            assets = assetMgr.list(srcPath);
            if(assets != null) {
                if (assets.length == 0) {
                    copyFile(p_context, srcPath, destPath);
                } else {
                    File dir = new File(destPath);
                    if (!dir.exists())
                        dir.mkdir();
                    for (String element : assets) {
                        copyAssetAll(p_context, srcPath + File.separator + element);
                    }
                }
            }
        }
        catch (IOException ignored) {}
    }

    public static void copyFile(Context p_context, String srcFile, String destFile) {
        AssetManager assetMgr = p_context.getAssets();

        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = assetMgr.open(srcFile);
            boolean _exists = new File(destFile).exists();
            if(srcFile.contains("shaders")) {
                _exists = false;
            }
            if(!_exists)
            {
                os = new FileOutputStream(destFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                is.close();
                os.flush();
                os.close();
            }
        }
        catch (IOException ignored) {}
    }

    private void setSurfaceView(Object p_value) {
        if(emulatorView != null) {
            if(emulatorView.getChildCount() > 0) {
                emulatorView.removeAllViews();
            }
            ////
            if(p_value instanceof SDLSurface) {
                emulatorView.addView((SDLSurface)p_value);
            }
        }
    }

    public void startEmuThread() {
        if(!isThread()) {
            mEmulationThread = new Thread(() -> NativeApp.runVMThread(m_szGamefile));
            mEmulationThread.start();
        }
    }

    private void restartEmuThread() {
        NativeApp.shutdown();
        if (mEmulationThread != null) {
            try {
                mEmulationThread.join();
                mEmulationThread = null;
            }
            catch (InterruptedException ignored) {}
        }
        startEmuThread();
    }
}