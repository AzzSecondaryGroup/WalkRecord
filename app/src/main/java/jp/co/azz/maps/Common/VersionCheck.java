package jp.co.azz.maps.Common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.concurrent.ExecutionException;

public class VersionCheck {
    private static final String TAG = "VersionCheck";

    public static boolean isExistNewVersion(Context context) {
        String latestVersion = null;
        String localVersion = null;
        try {
            // ストアの最新バージョン
            latestVersion = new GetLatestVersion().execute().get();
            Log.d(TAG, "!!! Latest version = " + latestVersion);

            // 端末のバージョン
            localVersion = localVersionName(context);
            Log.d(TAG, "!!! versionName:" + localVersion);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // 取得できていない情報があった場合は判断つかないのでダイアログ表示対象外
        return latestVersion != null
                && localVersion != null
                && latestVersion.equals(localVersion);
    }

   private static String localVersionName(Context context) {
       PackageManager pm = context.getPackageManager();
       String versionName = null;
       try {
           PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);

           versionName = packageInfo.versionName;

       } catch (PackageManager.NameNotFoundException e) {
           e.printStackTrace();
       }

       return versionName;
   }
}
