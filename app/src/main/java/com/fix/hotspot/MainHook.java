package com.fix.hotspot;

import android.util.SparseIntArray;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {
    private static final int TARGET_CHANNEL = 149;
    private static final int BAND_5GHZ = 1 << 1; // SoftApConfiguration.BAND_5GHZ

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!"android".equals(lpparam.packageName)) return;

        // Hook Builder 的 build() 方法前，强行将 mChannels 注入
        XposedHelpers.findAndHookMethod(
            "android.net.wifi.SoftApConfiguration$Builder",
            lpparam.classLoader,
            "build",
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Object builder = param.thisObject;

                    // 1. 设置频段为 5GHz (BAND_5GHZ)
                    try {
                        XposedHelpers.callMethod(builder, "setBand", BAND_5GHZ);
                    } catch (Throwable t) {
                        XposedBridge.log("[HotspotFix] setBand failed: " + t.getMessage());
                    }

                    // 2. 注入 Android 11+ 的核心信道映射表: SparseIntArray mChannels
                    try {
                        SparseIntArray channels = new SparseIntArray();
                        channels.put(BAND_5GHZ, TARGET_CHANNEL);
                        XposedHelpers.setObjectField(builder, "mChannels", channels);
                        XposedBridge.log("[HotspotFix] Successfully injected channel 149 into mChannels");
                    } catch (Throwable t) {
                        XposedBridge.log("[HotspotFix] Inject mChannels failed: " + t.getMessage());
                    }
                }
            }
        );
    }
}
