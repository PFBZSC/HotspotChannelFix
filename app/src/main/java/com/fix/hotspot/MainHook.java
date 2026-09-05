package com.fix.hotspot;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {
    private static final int CHANNEL = 149;
    private static final int BAND = 1 << 1; // 5GHz

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!"android".equals(lpparam.packageName)) return;

        XposedHelpers.findAndHookMethod(
            "android.net.wifi.SoftApConfiguration$Builder",
            lpparam.classLoader,
            "setChannel",
            int.class,
            int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.args[0] = CHANNEL;
                    param.args[1] = BAND;
                }
            }
        );

        XposedHelpers.findAndHookMethod(
            "android.net.wifi.SoftApConfiguration$Builder",
            lpparam.classLoader,
            "build",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object config = param.getResult();
                    if (config != null) {
                        try {
                            XposedHelpers.setIntField(config, "mChannel", CHANNEL);
                        } catch (Throwable ignored) {}
                    }
                }
            }
        );
    }
}
