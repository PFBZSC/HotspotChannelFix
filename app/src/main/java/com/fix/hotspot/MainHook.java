package com.fix.hotspot;

import android.util.SparseIntArray;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {
    private static final String PKG_NAME = "com.fix.hotspot";
    private static final String PREF_NAME = "config";
    private XSharedPreferences prefs;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!"android".equals(lpparam.packageName)) return;

        prefs = new XSharedPreferences(PKG_NAME, PREF_NAME);
        prefs.makeWorldReadable();

        XposedHelpers.findAndHookMethod(
            "android.net.wifi.SoftApConfiguration$Builder",
            lpparam.classLoader,
            "build",
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (prefs != null) {
                        prefs.reload();
                    }

                    int band = (prefs != null) ? prefs.getInt("band", 2) : 2;
                    int channel = (prefs != null) ? prefs.getInt("channel", 149) : 149;

                    Object builder = param.thisObject;

                    try {
                        XposedHelpers.callMethod(builder, "setBand", band);
                    } catch (Throwable t) {
                        XposedBridge.log("[HotspotFix] setBand error: " + t.getMessage());
                    }

                    try {
                        SparseIntArray channels = new SparseIntArray();
                        channels.put(band, channel);
                        XposedHelpers.setObjectField(builder, "mChannels", channels);
                        XposedBridge.log("[HotspotFix] Applied - Band: " + band + ", Channel: " + channel);
                    } catch (Throwable t) {
                        XposedBridge.log("[HotspotFix] setChannels error: " + t.getMessage());
                    }
                }
            }
        );
    }
}
