package cn.tinker.reporter;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.tinker.lib.listener.DefaultPatchListener;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.File;
import java.util.Properties;

import cn.tinker.BuildInfo;
import cn.tinker.SimpleUncaughtExceptionHandler;
import cn.tinker.TinkerUtil;
import cn.tinker.UpgradePatchRetry;

/**
 * FBI WARNING ! MAGIC ! DO NOT TOUGH !
 * Created by WangZQ on 2017/1/16 - 16:54.
 */

public class SamplePatchListener extends DefaultPatchListener {
    private static final String TAG = "Tinker.SamplePatchListener";

    protected static final long NEW_PATCH_RESTRICTION_SPACE_SIZE_MIN = 60 * 1024 * 1024;

    private final int maxMemory;

    public SamplePatchListener(Context context) {
        super(context);
        maxMemory = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        TinkerLog.i(TAG, "application maxMemory:" + maxMemory);
    }

    /**
     * because we use the defaultCheckPatchReceived method
     * the error code define by myself should after {@code ShareConstants.ERROR_RECOVER_INSERVICE
     *
     * @param path
     * @param newPatch
     * @return
     */
    @Override
    public int patchCheck(String path) {
        File patchFile = new File(path);
        TinkerLog.i(TAG, "receive a patch file: %s, file size:%d", path, SharePatchFileUtil.getFileOrDirectorySize(patchFile));
        int returnCode = super.patchCheck(path);

        if (returnCode == ShareConstants.ERROR_PATCH_OK) {
            returnCode = TinkerUtil.checkForPatchRecover(NEW_PATCH_RESTRICTION_SPACE_SIZE_MIN, maxMemory);
        }

        if (returnCode == ShareConstants.ERROR_PATCH_OK) {
            String patchMd5 = SharePatchFileUtil.getMD5(patchFile);
            SharedPreferences sp = context.getSharedPreferences(ShareConstants.TINKER_SHARE_PREFERENCE_CONFIG, Context.MODE_MULTI_PROCESS);
            //optional, only disable this patch file with md5
            int fastCrashCount = sp.getInt(patchMd5, 0);
            if (fastCrashCount >= SimpleUncaughtExceptionHandler.MAX_CRASH_COUNT) {
                returnCode = TinkerUtil.ERROR_PATCH_CRASH_LIMIT;
            } else {
                //for upgrade patch, version must be not the same
                //for repair patch, we won't has the tinker load flag
                Tinker tinker = Tinker.with(context);

                if (tinker.isTinkerLoaded()) {
                    TinkerLoadResult tinkerLoadResult = tinker.getTinkerLoadResultIfPresent();
                    if (tinkerLoadResult != null) {
                        String currentVersion = tinkerLoadResult.currentVersion;
                        if (patchMd5.equals(currentVersion)) {
                            returnCode = TinkerUtil.ERROR_PATCH_ALREADY_APPLY;
                        }
                    }
                }
            }
            //check whether retry so many times
            if (returnCode == ShareConstants.ERROR_PATCH_OK) {
                returnCode = UpgradePatchRetry.getInstance(context).onPatchListenerCheck(patchMd5)
                        ? ShareConstants.ERROR_PATCH_OK : TinkerUtil.ERROR_PATCH_RETRY_COUNT_LIMIT;
            }
        }
        // Warning, it is just a sample case, you don't need to copy all of these
        // Interception some of the request
        if (returnCode == ShareConstants.ERROR_PATCH_OK) {
            Properties properties = ShareTinkerInternals.fastGetPatchPackageMeta(patchFile);
            if (properties == null) {
                returnCode = TinkerUtil.ERROR_PATCH_CONDITION_NOT_SATISFIED;
            } else {
                String platform = properties.getProperty(TinkerUtil.PLATFORM);
                TinkerLog.i(TAG, "get platform:" + platform);
                // check patch platform require
                if (platform == null || !platform.equals(BuildInfo.PLATFORM)) {
                    returnCode = TinkerUtil.ERROR_PATCH_CONDITION_NOT_SATISFIED;
                }
            }
        }

        SampleTinkerReport.onTryApply(returnCode == ShareConstants.ERROR_PATCH_OK);
        return returnCode;
    }
}
