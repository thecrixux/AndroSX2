package com.efrei.emulator;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.view.Surface;
import java.io.File;
import java.lang.ref.WeakReference;

public class NativeApp {
	static {
		try {
			System.loadLibrary("emucore");
			hasNoNativeBinary = false;
		} catch (UnsatisfiedLinkError e) {
			hasNoNativeBinary = true;
		}
	}

	public static boolean hasNoNativeBinary;

	protected static WeakReference<Context> mContext;
	public static Context getContext() {
		if (mContext != null) {
			return mContext.get();
		}
		return null;
	}

	public static void initializeOnce(Context context) {
		mContext = new WeakReference<>(context);
		File externalFilesDir = context.getExternalFilesDir(null);
		if (externalFilesDir == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                externalFilesDir = context.getDataDir();
            }
        }
		initialize(externalFilesDir.getAbsolutePath(), android.os.Build.VERSION.SDK_INT);
	}

	public static native void initialize(String path, int apiVer);
	public static native String getGameTitle(String path);
	public static native String getGameSerial();
	public static native String getGameCRC();
	public static native float getFPS();

	public static native String getPauseGameTitle();
	public static native String getPauseGameSerial();

	public static native void setPadVibration(boolean isonoff);
	public static native void setPadButton(int index, int range, boolean iskeypressed);
	public static native void resetKeyStatus();

	public static native void setAspectRatio(int type);
	public static native int getAspectRatio();
	public static native void setEnableFastBoot(boolean enabled);
	public static native void speedhackLimitermode(int value);
	public static native void speedhackEecyclerate(int value);
	public static native void speedhackEecycleskip(int value);

	public static native void renderUpscalemultiplier(float value);
	public static native void renderMipmap(int value);
	public static native void renderHalfpixeloffset(int value);
	public static native void renderGpu(int value);
	public static native void setDefaultRenderer(int value);
	public static native void renderPreloading(int value);

	public static native void setOsdShowFPS(boolean enabled);
	public static native void setOsdShowSpeed(boolean enabled);
	public static native void setOsdShowCPU(boolean enabled);
	public static native void setOsdShowResolution(boolean enabled);
	public static native void setOsdShowGSStats(boolean enabled);
	public static native void setOsdShowVersion(boolean enabled);
	public static native void setOsdShowMessages(boolean enabled);
	public static native void setOsdShowGameInfo(boolean enabled);
	public static native void setOsdScale(float scale);

	public static native void addOSDMessage(String message, float duration);
	public static native void clearOSDMessages();

	public static native void setVsyncEnable(boolean enabled);
	public static native void setUpscaleMultiplier(float multiplier);
	public static native void setAnisotropicFiltering(int level);
	public static native void setTextureFiltering(int mode);
	public static native void setDithering(int mode);

	public static native void setEnableEE(boolean enabled);
	public static native void setEnableVU0(boolean enabled);
	public static native void setEnableVU1(boolean enabled);
	public static native void setFPUClampMode(int mode);

	public static native void setEnablePatches(boolean enabled);
	public static native void setEnableCheats(boolean enabled);
	public static native void setEnableWideScreenPatches(boolean enabled);
	public static native void setEnableNoInterlacingPatches(boolean enabled);

	public static native boolean isMemoryCardPresent(int port, int slot);
	public static native void setMemoryCardEnabled(int slot, boolean enabled);
	public static native boolean isMemoryCardEnabled(int slot);
	public static native void setMemoryCardPath(int slot, String path);
	public static native String getMemoryCardPath(int slot);
	public static native void setMemoryCardType(int slot, int type);
	public static native int getMemoryCardType(int slot);
	public static native String getDefaultMemoryCardName(int slot);
	public static native boolean createMemoryCardFile(String path, int sizeInMB);
	public static native void refreshMemoryCards();

	public static native void onNativeSurfaceCreated();
	public static native void onNativeSurfaceChanged(Surface surface, int w, int h);
	public static native void onNativeSurfaceDestroyed();

	public static native boolean runVMThread(String path);

	public static native void pause();
	public static native void resume();
	public static native void shutdown();

	public static native boolean saveStateToSlot(int slot);
	public static native boolean loadStateFromSlot(int slot);
	public static native String getGamePathSlot(int slot);
	public static native byte[] getImageSlot(int slot);

	// Call jni
	public static int openContentUri(String uriString) {
		Context _context = getContext();
		if(_context != null) {
			ContentResolver _contentResolver = _context.getContentResolver();
			try {
				ParcelFileDescriptor filePfd = _contentResolver.openFileDescriptor(Uri.parse(uriString), "r");
				if (filePfd != null) {
					return filePfd.detachFd();  // Take ownership of the fd.
				}
			} catch (Exception ignored) {}
		}
		return -1;
	}

}