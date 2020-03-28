package jp.co.azz.maps.Common;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import jp.co.azz.maps.BuildConfig;

public class VersionCheckRemoteConfig {
    //Firebaseのパラメータ名
    private static final String KEY_FORCE_UPDATE = "force_update";

    /**
     * Firebase から force_update パラメータ取得する
     * @param activity {Activity} 呼ばれた先のactivity
     * @return  {String} 取得した パラメータ
     */
    public String getForceUpdateFromRemoteConfig(Activity activity){
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        //firebaseRemoteConfig の初期設定
        initRemoteConfig(firebaseRemoteConfig);
        //firebaseからフェッチ
        fetchRemoteConfig(activity, firebaseRemoteConfig);
        //force_update パラメータ取得
        String paramforceUpdate = getStringValueFromRemoteConfig(firebaseRemoteConfig, KEY_FORCE_UPDATE);

        return paramforceUpdate;
    }

    /**
     * FirebaseRemoteConfig の初期設定
     * @param firebaseRemoteConfig {FirebaseRemoteConfig} 設定するfirebaseRemoteConfig
     */
    private void initRemoteConfig(FirebaseRemoteConfig firebaseRemoteConfig) {
        FirebaseRemoteConfigSettings.Builder builder = new FirebaseRemoteConfigSettings.Builder();
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            builder.setDeveloperModeEnabled(BuildConfig.DEBUG);
        }

        firebaseRemoteConfig.setConfigSettings(builder.build());

    }

    /**
     * FirebaseのRemotoConfig からフェッチする
     * @param activity {Activity} 呼ばれた先のactivity
     * @param firebaseRemoteConfig {FirebaseRemoteConfig} 設定したfirebaseRemoteConfig
     */
    private void fetchRemoteConfig(Activity activity, final FirebaseRemoteConfig firebaseRemoteConfig) {
        long catchExpiration = 3600;
        if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            catchExpiration = 0;
        }

        firebaseRemoteConfig.fetch(catchExpiration)
                .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        firebaseRemoteConfig.activateFetched();
                    }
                });
    }

    /**
     * フェッチしたRemoteConfig から値を取得する
     * @param firebaseRemoteConfig {FirebaseRemoteConfig} 設定したfirebaseRemoteConfig
     * @param key {String} 取得するパラメータのKEY
     * @return {String} 取得したパラメータの値
     */
    private String getStringValueFromRemoteConfig(FirebaseRemoteConfig firebaseRemoteConfig, String key) {
        return firebaseRemoteConfig.getString(key);
    }
}
