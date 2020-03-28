package jp.co.azz.maps.Common;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.concurrent.ExecutionException;

public class VersionCheck {
    private static final String TAG = "VersionCheck";

    /**
     * 新しいバージョンが存在するかチェック
     * @param activity {Activity} 呼び出し元のActivity
     * @return {boolean} 端末のバージョン と RemoteConfig から取得した バージョンが 一致 しない場合 true
     */
    public static boolean isExistNewVersion(Activity activity) {
        String latestVersion = null;
        String localVersion = null;
        try {

            //RemoteConfig からアプリの最新バージョン情報を取得
            latestVersion = new VersionCheckRemoteConfig().getForceUpdateFromRemoteConfig(activity);
            Log.d(TAG, "!!! Latest version(RemoteConfig) = " + latestVersion);

            // 端末のバージョン
            localVersion = localVersionName(activity.getApplicationContext());
            Log.d(TAG, "!!! Local version:" + localVersion);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // バージョンが異なる場合は更新対象
        // 取得できていない情報があった場合は判断つかないのでダイアログ表示対象外
        return latestVersion != null
                && localVersion != null
                && !latestVersion.equals(localVersion);
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
