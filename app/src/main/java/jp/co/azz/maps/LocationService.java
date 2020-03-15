package jp.co.azz.maps;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import jp.co.azz.maps.databases.DatabaseContract;
import jp.co.azz.maps.databases.WalkRecordDao;


public class LocationService extends Service {

    private static final int LOCATION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Context context;

    private WalkRecordDao walkRecordDao;
    private int walkHistoryNum = 0;
    // 歩幅
    private double stepSizeMeter;
    // 体重
    private double weight;
    // 時速(km/hr)
    private int walkHourlySpeed = 4;
    private boolean isFirst = true;

    private static final String TAG = "LocationService";

    // 位置情報取得最終座標
    private Location lastLocation = null;
    // 移動距離合計
    private double totalDistanceMeter = 0.0;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("LocationService", "onCreate開始！！！！！！！！！");

        context = getApplicationContext();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        walkRecordDao = new WalkRecordDao(context);

        setCalcBaseInfo();
    }

    /**
     * サービス開始時の処理
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int requestCode = 0;
        String channelId = "default";
        String title = context.getString(R.string.app_name);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // ForegroundにするためNotificationが必要、Contextを設定
        NotificationManager notificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification　Channel 設定
        NotificationChannel channel = new NotificationChannel(
                channelId, title , NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Silent Notification");
        // 通知音を消さないと毎回通知音が出てしまう
        // この辺りの設定はcleanにしてから変更
        channel.setSound(null,null);
        // 通知ランプを消す
        channel.enableLights(false);
        channel.setLightColor(Color.BLUE);
        // 通知バイブレーション無し
        channel.enableVibration(false);

        if(notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(context, channelId)
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.toolbar_icon)
                    .setContentText("位置情報記録")
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .build();

            // startForegroundServiceで呼び出された後に5秒以内に実施する必要がある
            startForeground(1, notification);
        }

        // 位置情報取得開始
        startAcquisition();

        return START_NOT_STICKY;
    }

    /**
     * 位置情報取得開始
     */
    private void startAcquisition() {
        // パーミッションの確認
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
            }, LOCATION_REQUEST_CODE);

            return;
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(800); // 更新間隔(ms)
        locationRequest.setFastestInterval(5000); // 最速更新間隔(ms)
        // 高精度の位置情報を取得
        locationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
//                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//                LocationRequest.PRIORITY_LOW_POWER);
//                LocationRequest.PRIORITY_NO_POWER);
        fusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback,null);

        toastMake("位置情報取得開始");
    }

    /**
     * 位置情報更新時に行う処理
     */
    LocationCallback mLocationCallback = new LocationCallback() {
        // nullが返る可能性もあるためその場合は捨てる
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (location != null)
                    saveLocationData(location);
            }
        }
    };

    /**
     *
     * @param location
     */
    private void saveLocationData(Location location) {

            // 初回スタート時
            if (isFirst) {
                Log.d(TAG, "■スタート後初回の位置情報インサート");
                // 散歩履歴をインサート
                walkStart(location);

                Log.d(TAG, "■散歩履歴インサート（レコードNo）：" + walkHistoryNum);
                isFirst = !isFirst;
            } else {

                //座標更新、履歴テーブル更新
                updateWalkRecord(location);
            }
    }

    /**
     * 計測終了
     */
    private void stopAcquisition() {
        isFirst = true;
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopAcquisition();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // トーストの生成
    private void toastMake(String message){
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        // 位置調整
        toast.show();
    }

    /**
     * 初回位置情報取得時のDB登録
     */
    public void walkStart(Location location) {
        if (walkRecordDao == null) {
            walkRecordDao = new WalkRecordDao(context);
        }

        String startTime = AppContract.now();

        walkHistoryNum = (int)walkRecordDao.insertHistory(startTime, startTime, 0, 0.0, 0);
toastMake("散歩記録インサートNo:"+walkHistoryNum);
        walkRecordDao.insertCoordinate(walkHistoryNum, location.getLatitude(), location.getLongitude());

        lastLocation = location;
    }

    /**
     * 2回目以降の位置情報取得時のDB更新
     */
    public void updateWalkRecord(Location location) {

        if (lastLocation != null) {
            // 移動距離計算(メートル)
            totalDistanceMeter += currentDistance(location);

            // 歩数計算
            int stepCont = (int) ((totalDistanceMeter) / stepSizeMeter);

            double totalDistanceKm = totalDistanceMeter / 1000;

            // 消費カロリー計算（kcal単位）
            // 体重×メッツ値×時間
            int burnedCalories = (int) (1.05 * 3.5 * weight * ( totalDistanceKm / walkHourlySpeed ) );

            walkRecordDao.insertCoordinate(walkHistoryNum, location.getLatitude(), location.getLongitude());

            String endTime = AppContract.now();
            walkRecordDao.updateHistory(walkHistoryNum, endTime, stepCont, totalDistanceKm, burnedCalories);

            double[] currentLocation = {location.getLatitude(), location.getLongitude()};
            toastMake("位置情報:["+location.getLatitude()+ "],[" + location.getLongitude()+"]" + stepCont+"歩 "+burnedCalories);

            sendBroadCast(stepCont, totalDistanceKm, burnedCalories, currentLocation);
        }

        lastLocation = location;
    }

    /**
     * 前回取得の座標との距離を取得（メートル）
     * @param location
     * @return 前回位置情報からの移動距離
     */
    private double currentDistance(Location location) {

        float[] results = new float[3];
        results[0] = 0;
        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                location.getLatitude(), location.getLongitude(), results);

        return results[0];
    }

    /**
     * レシーバーにブロードキャストを送信
     * @param stepCont 歩数
     * @param totalDistance 距離
     * @param burnedCalories 消費カロリー
     * @param currentLocation 現在地
     */
    protected void sendBroadCast(int stepCont, double totalDistance, int burnedCalories, double[] currentLocation) {

        Intent intent = new Intent();
        intent.putExtra("stepCont", stepCont);
        intent.putExtra("totalDistance", totalDistance);
        intent.putExtra("burnedCalories", burnedCalories);
        intent.putExtra("currentLocation", currentLocation);
        intent.setAction("UPDATE_ACTION");

        getBaseContext().sendBroadcast(intent);
    }

    /**
     * 歩幅やカロリー計算で必要となる情報を設定
     */
    private void setCalcBaseInfo() {
        //歩幅を計算（身長*0.45）
        int tall = walkRecordDao.getTall();
        if (tall == 0) {
            tall = DatabaseContract.Setting.DEFAULT_TALL;
        }
        stepSizeMeter = (double) tall * 0.45 / 100;

        weight = walkRecordDao.getWeight();
        if (weight == 0) {
            weight = DatabaseContract.Setting.DEFAULT_WEIGHT;
        }
    }
}
