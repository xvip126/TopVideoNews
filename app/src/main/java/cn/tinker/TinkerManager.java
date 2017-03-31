//package cn.tinker;
//
//import com.tencent.tinker.lib.listener.PatchListener;
//import com.tencent.tinker.lib.patch.AbstractPatch;
//import com.tencent.tinker.lib.patch.UpgradePatch;
//import com.tencent.tinker.lib.reporter.LoadReporter;
//import com.tencent.tinker.lib.reporter.PatchReporter;
//import com.tencent.tinker.lib.tinker.TinkerInstaller;
//import com.tencent.tinker.lib.util.TinkerLog;
//import com.tencent.tinker.loader.app.ApplicationLike;
//
//import cn.tinker.reporter.SampleLoadReporter;
//import cn.tinker.reporter.SamplePatchListener;
//import cn.tinker.reporter.SamplePatchReporter;
//
///**
// * FBI WARNING ! MAGIC ! DO NOT TOUGH !
// * Created by WangZQ on 2017/1/16 - 16:41.
// */
//
//public class TinkerManager {
//    private static final String TAG = "Tinker.TinkerManager";
//
//    private static ApplicationLike applicationLike;
//    private static SimpleUncaughtExceptionHandler uncaughtExceptionHandler;
//    private static boolean isInstalled = false;
//
//    public static void setTinkerApplicationLike(ApplicationLike appLike) {
//        applicationLike = appLike;
//    }
//
//    public static ApplicationLike getTinkerApplicationLike() {
//        return applicationLike;
//    }
//
//    public static void initFastCrashProtect() {
//        if (uncaughtExceptionHandler == null) {
//            uncaughtExceptionHandler = new SimpleUncaughtExceptionHandler();
//            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
//        }
//    }
//
//    public static void setUpgradeRetryEnable(boolean enable) {
//        UpgradePatchRetry.getInstance(applicationLike.getApplication()).setRetryEnable(enable);
//    }
//
//
//    /**
//     * all use default class, simply Tinker install method
//     */
//    public static void sampleInstallTinker(ApplicationLike appLike) {
//        if (isInstalled) {
//            TinkerLog.w(TAG, "install tinker, but has installed, ignore");
//            return;
//        }
//        TinkerInstaller.install(appLike);
//        isInstalled = true;
//
//    }
//
//    /**
//     * you can specify all class you want.
//     * sometimes, you can only install tinker in some process you want!
//     *
//     * @param appLike
//     */
//    public static void installTinker(ApplicationLike appLike) {
//        if (isInstalled) {
//            TinkerLog.w(TAG, "install tinker, but has installed, ignore");
//            return;
//        }
//        //or you can just use DefaultLoadReporter
//        LoadReporter loadReporter = new SampleLoadReporter(appLike.getApplication());
//        //or you can just use DefaultPatchReporter
//        PatchReporter patchReporter = new SamplePatchReporter(appLike.getApplication());
//        //or you can just use DefaultPatchListener
//        PatchListener patchListener = new SamplePatchListener(appLike.getApplication());
//        //you can set your own upgrade patch if you need
//        AbstractPatch upgradePatchProcessor = new UpgradePatch();
//
//        TinkerInstaller.install(appLike,
//                loadReporter, patchReporter, patchListener,
//                SampleResultService.class, upgradePatchProcessor);
//
//        isInstalled = true;
//    }
//}
