package jp.co.azz.maps.Service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import jp.co.azz.maps.databases.DatabaseHelper;
import jp.co.azz.maps.databases.WalkRecordDao;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class LocationManageService extends Service implements
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "LocationManageService";

    private boolean isLocationAuthorityReady = false;  // 位置情報の権限周りの準備ができているか

    private GoogleApiClient googleApiClient;
    PendingIntent pendingIntent;

    // GPS,WiFi,電話基地局からの位置情報を取得するAPI
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;

    /** 10秒間隔で位置情報を更新。実際には多少頻度が多くなるかもしれない。 */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /** 最速の更新間隔。この値より頻繁に更新されることはない。 */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 10;
    private LocationRequest mLocationRequest;

    private static int INTERVAL = 5000;
    private static final int FASTESTINTERVAL = 5000;

    // 位置情報の取得間隔などの設定
    private static final LocationRequest LOCATION_REQUEST = LocationRequest.create()
            .setInterval(INTERVAL)              // 位置情報の更新間隔をミリ秒で設定
            .setFastestInterval(FASTESTINTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);   //位置情報取得要求の優先順位（PRIORITY_HIGH_ACCURACY：高い正確性）

    private Context context;

    public LocationManageService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        Log.d(TAG, "!!!!!!!!!!サービスのonBind");
        return null;
    }

    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "!!!!!!!!!!サービスのonCreate");

        // Google Playサービスへの入り口
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Google Playサービスに接続する
        googleApiConnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "!!!!!!!!!!サービスのonStartCommand");

        // インテントをペンディングインテントに変換
        pendingIntent = PendingIntent.getService(this,0, intent, 0);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (googleApiClient.isConnected() ) {
            stopLocationUpdates();
        }
        // Google Playサービスの接続を止める
        googleApiClient.disconnect();

        // Service終了
        stopSelf();
    }

    /**
     * Google Playサービスに接続したときの処理
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "!!!!!!!!!!Google Playサービスに接続");
        // 位置情報取得の権限を保持しているかチェック
        // 権限がない場合は後続処理を行わない
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "!!!!!!!!!!Google Playサービス接続の為の権限がない");
            return;
        }
        Log.d(TAG, "!!!!!!!!!!Google Playサービス接続の為の権限はある");

        // 位置情報が変わった時に通知を受け取るためのリクエスト
        // onLocationChanged が呼び出されるようになる
        fusedLocationProviderApi.requestLocationUpdates(googleApiClient, LOCATION_REQUEST, pendingIntent);
    }

    /**
     * GoogleApiClient の接続が中断された場合
     * @param cause
     */
    @Override
    public void onConnectionSuspended(int cause) {
        // Do nothing
    }

    /**
     * GoogleApiClient の接続に失敗した場合
     * @param result
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do nothing
    }

    /**
     * 位置情報のリクエストを解除する
     */
    protected void stopLocationUpdates() {
        fusedLocationProviderApi.removeLocationUpdates(googleApiClient, pendingIntent);
    }


    /**
     * googleApiClientに接続する
     */
    private void googleApiConnect() {

//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
//        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (!googleApiClient.isConnected() ) {
            googleApiClient.connect();
        }
    }

    /**
     //     * 位置情報の取得準備が整っているときの最初の処理
     //     */
    private void locationReadyProcess() {
//        // TODO 電力消費抑えることを考える場合はもう少し考える
//        // 現状、端末の位置情報設定変更は検知できないので初回マップ表示前に一度接続しておく
//        if (isFirstMapDisp) {
//            // Google Playサービスに接続する
//            googleApiConnect();
//        }

        // 権限、位置情報設定ともに問題ない場合
        if (isLocationAuthorityReady) {

            // Google Playサービスに接続する
            googleApiConnect();
        }
    }

////////// LocationListener の実装群
    /**
     * 位置情報が更新された場合の処理
     * 地図の移動、住所の取得、移動線の描画、移動距離の累計を行う
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        Log.d(TAG, "!!!!!!!!!!位置情報が更新されたとき");
        Log.d(TAG, "!!!!!!!!!!緯度、経路："+location.getLatitude() + ", " + location.getLongitude());

        // 位置情報取得
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onProviderDisabled(String provider) {
    }

}
