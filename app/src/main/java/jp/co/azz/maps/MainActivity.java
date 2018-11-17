package jp.co.azz.maps;

import android.Manifest;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

//import android.location.LocationListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
//        LoaderManager.LoaderCallbacks<Address> {
private static final String TAG = "MainActivity";

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private static final int ADDRESSLOADER_ID = 0;
    // サンプルはINTERVAL:500(ミリ秒) ,FASTESTINTERVAL:16
    private static final int INTERVAL = 1000;
    private static final int FASTESTINTERVAL = 1000;

    private GoogleMap mMap;

    private GoogleApiClient googleApiClient;
    // 位置情報の取得間隔などの設定
    private static final LocationRequest LOCATION_REQUEST = LocationRequest.create()
            .setInterval(INTERVAL)              // 位置情報の更新間隔をミリ秒で設定
            .setFastestInterval(FASTESTINTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);   //位置情報取得要求の優先順位（PRIORITY_HIGH_ACCURACY：高い正確性）

    // GPS,WiFi,電話基地局からの位置情報を取得するAPI
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    // 移動経路を描くための情報のリスト
    private List<LatLng> mRunList = new ArrayList<LatLng>();
    private WifiManager wifiManager;
    private boolean mWifiOff = false;
    private long mStartTimeMillis;
    private double mMeter = 0.0;           // メートル
    private double mElapsedTime =0.0;        // ミリ秒
    private double mSpeed = 0.0;
    private DatabaseHelper dbHelper;
    private boolean mStart = false;
    private boolean mFirst = false;
    private boolean mStop = true;   // 開始時は停止
    private boolean wifiAsked = false;
    private Chronometer mChronometer;
    private WalkRecordDao walkRecordDao;
    private int walkHistoryNum = 0;
    private boolean isFirstMapDisp = true;  // MAP最初の表示かどうか

    /**
     * onPauseの直後に呼ばれる処理
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // メンバー変数の状態を保存
        outState.putBoolean("IS_FIRST_MAP_DISP", isFirstMapDisp);
        outState.putBoolean("WIFI_ASKED",wifiAsked);
    }

    /**
     * onStartの直後に呼ばれる処理
     * 保存後にActivityが破棄された次のライフサイクルのタイミングでのみ呼ばれる
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // メンバー変数の状態を復元
        isFirstMapDisp = savedInstanceState.getBoolean("IS_FIRST_MAP_DISP");
        wifiAsked = savedInstanceState.getBoolean("WIFI_ASKED");
    }

    /**
     * Activity生成時の処理
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ****************** デバッグ用散歩履歴件数表示 ******************
        walkRecordDao = new WalkRecordDao(getApplicationContext());
        List<HistoryDto> historyList = walkRecordDao.selectHistory();
        Log.d(TAG, "◆散歩履歴テーブルのダミーデータの件数：" + historyList.size() + "◆");
        if (historyList.size() > 0) {
            HistoryDto historyDto = historyList.get(historyList.size() - 1);
            Log.d(TAG, "散歩履歴テーブルのダミーデータ（最新）：");
            Log.d(TAG, "　id：" + historyDto.getId());
            Log.d(TAG, "　開始日時：" + historyDto.getStartDate());
            Log.d(TAG, "　終了日時：" + historyDto.getEndDate());
            Log.d(TAG, "　距離：" + historyDto.getDistance());
            Log.d(TAG, "　歩数：" + historyDto.getNumberOfSteps());
            Log.d(TAG, "　カロリー：" + historyDto.getCalorie());
        }

        // ****************************************************************

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 画面をスリープにしない
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Google Playサービスへの入り口
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        // DatabaseHelperのインスタンス生成
        // DBが存在しない場合はこのタイミングで作成される
        dbHelper = new DatabaseHelper(this);

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        // 初期表示はOFF
        toggleButton.setChecked(false);

        //ToggleのCheckが変更したタイミングで呼び出されるリスナー
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // トグルキーが変更された際に呼び出される
                // ONになった場合
                if (isChecked) {
                    Log.d(TAG, "■経路取得開始");
//                    startChronometer();
                    mStart = true;
                    mFirst = true;
                    mStop = false;
                    mMeter = 0.0;
                    mRunList.clear();

                } else {
//                    stopChronometer();
                    mStop = true;
                    // 速度計算は今回不要
//                    calcSpeed();
                    // TODO 開始後保存をすぐに行うから権限確認の処理は開始後に必要かも
//                    saveConfirm();
                    mStart = false;
                }
            }
        });



//        // MapFragmentの生成
//        MapFragment mapFragment = MapFragment.newInstance();
//
//        // MapViewをMapFragmentに変更する
//        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//        fragmentTransaction.add(R.id.mapView, mapFragment);
//        fragmentTransaction.commit();
//
//        mapFragment.getMapAsync(this);

    }

    private void startChronometer() {
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        // 電源ON時からの経過時間の値をベースに
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        mStartTimeMillis=System.currentTimeMillis();
    }
    private void stopChronometer() {
        mChronometer.stop();
        // ミリ秒
        mElapsedTime =SystemClock.elapsedRealtime() - mChronometer.getBase();
    }

    /**
     * Activityの表示、再表示の処理
     * （初回表示、バックグラウンドから復帰後（onStartの後）、他のActivityから戻ったタイミング）
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!wifiAsked) {
            //Log.v("exec wifiAsked","" + wifiAsked);
            // WiFiをオフにするかどうか確認
            wifiConfirm();
            wifiAsked = !wifiAsked;
        }

        // Google Playサービスに接続する
        googleApiClient.connect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.walk_history) {
            Intent intent = new Intent(getApplication(), WalkHistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.calorie_calculation) {
            Intent intent = new Intent(getApplication(), CalorieCalculationActivity.class);
            startActivity(intent);
        } else if (id == R.id.setting) {
            Intent intent = new Intent(getApplication(), SettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.etc) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        // 位置座標のインスタンスを作成(緯度、経度)
//        // ダミー
//        CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(
//                new LatLng(35.712206, 139.706787), 15);
//        mMap.moveCamera(cUpdate);



        // DangerousなPermissionはリクエストして許可をもらわないと使えない
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                //一度拒否された時、Rationale（理論的根拠）を説明して、再度許可ダイアログを出すようにする
                new AlertDialog.Builder(this)
                        .setTitle("許可が必要です")
                        .setMessage("移動に合わせて地図を動かすためには、ACCESS_FINE_LOCATIONを許可してください")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // OK button pressed
                                requestAccessFineLocation();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showToast("GPS機能が使えないので、地図は動きません");
                            }
                        })
                        .show();
            } else {
                // まだ許可を求める前の時、許可を求めるダイアログを表示します。
                requestAccessFineLocation();
            }
        }
    }
    private void requestAccessFineLocation() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // ユーザーが許可したとき
                // 許可が必要な機能を改めて実行する
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //
                }
                else {
                    // ユーザーが許可しなかったとき
                    // 許可されなかったため機能が実行できないことを表示する
                    showToast("GPS機能が使えないので、地図は動きません");
                    // 以下は、java.lang.RuntimeException になる
                    // mMap.setMyLocationEnabled(true);
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // ユーザーが許可したとき
                // 許可が必要な機能を改めて実行する
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //saveConfirmDialog();
                    // TODO　保存処理　ここを通す必要あるか？
                }
                else {
                    // ユーザーが許可しなかったとき
                    // 許可されなかったため機能が実行できないことを表示する
                    showToast("外部へのファイルの保存が許可されなかったので、記録できません");
                }
                return;
            }

        }
    }

    /**
     * WiFiをオフにするかどうか確認するダイアログ
     * （WiFiがオンの場合、近くのWiFiをキャッチして現在地がぶれる可能性があるため）
     */
    private void wifiConfirm(){
        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);

        if(wifiManager.isWifiEnabled()) {
            wifiConfirmDialog();
        }
    }
    private void wifiConfirmDialog() {
        DialogFragment newFragment = WifiConfirmDialogFragment.newInstance(
                R.string.wifi_confirm_dialog_title, R.string.wifi_confirm_dialog_message);

        newFragment.show(getFragmentManager(), "dialog");

    }

    /**
     * WiFiをオフにする
     */
    public void wifiOff() {
        wifiManager.setWifiEnabled(false);
        mWifiOff=true;
    }

    /**
     * Google Playサービスに接続したときの処理
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "■Google Playサービスに接続");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // 位置情報が変わった時に通知を受け取るためのリクエスト
        // onLocationChanged が呼び出されるようになる
        fusedLocationProviderApi.requestLocationUpdates(googleApiClient, LOCATION_REQUEST, this);

    }

    /**
     * 位置情報が更新された場合の処理
     * 地図の移動、住所の取得、移動線の描画、移動距離の累計を行う
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        Log.d(TAG, "■位置情報が更新されたとき");
        Log.d(TAG, "■緯度、経路："+location.getLatitude() + ", " + location.getLongitude());

        // 位置情報取得
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // まだ一度もMap表示していない場合のみ最初のMap表示を行う
        // TODO 一瞬世界地図が表示されてしまうので対応要
        if (isFirstMapDisp) {
            // カメラの倍率、ポジション変更
            CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(
            new LatLng(location.getLatitude(), location.getLongitude()), 16);
            mMap.moveCamera(cUpdate);
            Log.d(TAG, "■最初の地図の位置更新");

            isFirstMapDisp = false;
        }

        // stop後は動かさない
        if (mStop) {
            return;
        }

        // ********** テスト用ダミーデータの作成 *************
        if(!mRunList.isEmpty()) {
            LatLng dummy = mRunList.get(mRunList.size() - 1);
            // 屋内のテスト用に位置を変える
            dummy = new LatLng(dummy.latitude + 0.02, dummy.longitude + 0.02);

            currentLatLng = dummy;
        }

        // ***************************************************


        // カメラの倍率、ポジション変更
        CameraPosition cameraPos = new CameraPosition.Builder()
                .target(currentLatLng).zoom(16)
                .bearing(0).build();
        // 地図の中心を取得した緯度、経度に動かす
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
        Log.d(TAG, "■地図の位置更新");

        //マーカー設定
        // TODO　マーカーはどうするか後で検討
        mMap.clear();
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions();
        options.position(latlng);
//        // ランチャーアイコン
        // ここでエラー出てるみたいなので一旦コメントアウト
//        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
//        options.icon(icon);
//        Log.d(TAG, "■アイコン作成");
        // 現在地にマーカーを追加
        mMap.addMarker(options);
        Log.d(TAG, "■マーカーを追加");
        if (mStart) {
            // 初回スタート時
            if (mFirst) {
                Log.d(TAG, "■スタート後初回の位置情報インサート");
// 住所情報を表示しないので不要
//                Bundle args = new Bundle();
//                args.putDouble("lat", location.getLatitude());
//                args.putDouble("lon", location.getLongitude());
//
//                // LoaderManagerのインスタンスを取得（現在地の緯度、経度から住所を取得）
//                // restartLoaderはローダが現在実行中なら自動的に中断してくれる
//                // restartLoaderまたはinitLoaderを実行するとonCreateLoaderが呼ばれる
//                getLoaderManager().restartLoader(ADDRESSLOADER_ID, args, this);
//
                // 保存可能な場合は散歩履歴をインサート
                if(saveConfirm()) {
                    walkHistoryNum = (int)insertWalkRecord();
                    Log.d(TAG, "■散歩履歴インサート（レコードNo）：" + walkHistoryNum);
                }
                mFirst = !mFirst;
                // 2回目以降の位置取得の場合
            } else {
                // 移動線を描画
                drawTrace(latlng);
                // 走行距離を累積
                sumDistance();

                updateWalkRecord();
            }
        }

    }

    /**
     * 線を引く
     * @param latlng
     */
    private void drawTrace(LatLng latlng) {
        mRunList.add(latlng);
        if (mRunList.size() > 2) {
            PolylineOptions polyOptions = new PolylineOptions();
            for (LatLng polyLatLng : mRunList) {
                polyOptions.add(polyLatLng);
            }
            polyOptions.color(Color.BLUE);
            polyOptions.width(3);
            polyOptions.geodesic(false);
            mMap.addPolyline(polyOptions);
        }
    }

    /**
     * 隣同士の座標の距離を求めて累積する
     */
    private void sumDistance() {

        if (mRunList.size() < 2) {
            return;
        }
        mMeter=0;
        float[] results = new float[3];
        int i = 1;
        while (i<mRunList.size()){
            results[0]=0;
            Location.distanceBetween(mRunList.get(i-1).latitude, mRunList.get(i-1).longitude,
                    mRunList.get(i).latitude, mRunList.get(i).longitude, results);
            mMeter += results[0];
            i++;
        }
        // distanceBetweenの距離はメートル単位
        double disMeter = mMeter / 1000;
        TextView main_distance = (TextView) findViewById(R.id.main_distance);
        main_distance.setText(String.format("%.2f"+" km", disMeter));
    }

//    /**
//     * 移動速度の取得
//     */
//    private void calcSpeed() {
//        sumDistance();
//        mSpeed = (mMeter/1000) / (mElapsedTime /1000) * 60 * 60;
//    }


    /**
     * 保存処理を行うための権限確認
     */
    private boolean saveConfirm() {

        boolean isPossibleSave = false;

        // DangerousなPermissionはリクエストして許可をもらわないと使えない
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //一度拒否された時、Rationale（理論的根拠）を説明して、再度許可ダイアログを出すようにする
                new AlertDialog.Builder(this)
                        .setTitle("許可が必要です")
                        .setMessage("ジョギングの記録を保存するためには、WRITE_EXTERNAL_STORAGEを許可してください")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // OK button pressed
                                requestWriteExternalStorage();
                            }
                        })
                        .setNegativeButton("Cancel",  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showToast("外部へのファイルの保存が許可されなかったので、記録できません");
                            }
                        })
                        .show();
            } else {
                // まだ許可を求める前の時、許可を求めるダイアログを表示します。
                requestWriteExternalStorage();
            }
        } else {
            // 保存するかどうかの確認ダイアログは不要
            // saveConfirmDialog();

            isPossibleSave = true;
        }
        return isPossibleSave;
    }

    private void requestWriteExternalStorage() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

    }

// 保存するかどうか確認するダイアログは今回不要
//    /**
//     * 保存するかどうかを確認するダイアログを表示
//     */
//    private void saveConfirmDialog() {
//        String message ="時間:";
//        TextView main_distance = (TextView) findViewById(R.id.main_distance);
//
//        message = message + mChronometer.getText().toString() + " " +
//                "距離" + main_distance.getText() + "\n" +
//                "時速" + String.format("%.2f"+" km", mSpeed);
//
//        DialogFragment newFragment = SaveConfirmDialogFragment.newInstance(
//                R.string.save_confirm_dialog_title, message);
//
//        newFragment.show(getFragmentManager(), "dialog");
//
//    }

    /**
     * Activityがフォアグラウンドでなくなるとき
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient.isConnected() ) {
            stopLocationUpdates();
        }
        // Google Playサービスの接続を止める
        googleApiClient.disconnect();

    }

    /**
     * Activityが非表示になるとき
     */
    @Override
    protected void onStop() {
        super.onStop();
        // 自プログラムがオフにした場合はWIFIをオンにする処理
        if (mWifiOff) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 位置情報のリクエストを解除する
     */
    protected void stopLocationUpdates() {
        fusedLocationProviderApi.removeLocationUpdates(googleApiClient, this);
    }
    @Override
    public void onConnectionSuspended(int cause) {
        // Do nothing
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do nothing
    }

// 住所取得ローダは使わないので不要
//    /**
//     * LoaderManagerのインスタンス取得時の処理（ローダが新しく生成された時）
//     *
//     * @param id
//     * @param args
//     * @return
//     */
//    @Override
//    public Loader<Address> onCreateLoader(int id, Bundle args) {
//        double lat = args.getDouble("lat");
//        double lon = args.getDouble("lon");
//
//        // 緯度、経度からAddressTaskLoaderを生成する
//        return new AddressTaskLoader(this, lat,lon);
//    }
//
//    /**
//     * ローダ内の処理が終了したときの処理
//     * AddressTaskLoader.loadInBackgroundからAddressオブジェクトが渡される
//     *
//     * @param loader
//     * @param result
//     */
//    @Override
//    public void onLoadFinished(Loader<Address> loader, Address result) {
//        if (result != null) {
//            StringBuilder sb = new StringBuilder();
//            for (int i = 1; i < result.getMaxAddressLineIndex() + 1; i++) {
//                String item = result.getAddressLine(i);
//                if (item == null) {
//                    break;
//                }
//
//                sb.append(item);
//            }
//            TextView address = (TextView) findViewById(R.id.address);
//
//            address.setText(sb.toString());
//        }
//    }
//
//    /**
//     * ローダがリセットされたときの処理（restartLoaderが呼ばれた時など）
//     * @param loader
//     */
//    @Override
//    public void onLoaderReset(Loader<Address> loader) {
//
//    }

    /**
     * コンテンツプロバイダを経由してテーブルにレコードを追加する
     * データベースの取得とクローズが不要
     */
    public void saveWalkRecordViaCTP(){

//        String strDate = new SimpleDateFormat("yyyy/MM/dd").format(mStartTimeMillis);
//
//        TextView txtAddress = (TextView)findViewById(R.id.address);
//
//        ContentValues values = new ContentValues();
//        values.put(DatabaseHelper.COLUMN_DATE, strDate);
//        values.put(DatabaseHelper.COLUMN_ELAPSEDTIME,mChronometer.getText().toString());
//        values.put(DatabaseHelper.COLUMN_DISTANCE, mMeter);
//        values.put(DatabaseHelper.COLUMN_SPEED, mSpeed);
//        values.put(DatabaseHelper.COLUMN_ADDRESS, txtAddress.getText().toString());
//        Uri uri = getContentResolver().insert(JogRecordContentProvider.CONTENT_URI, values);
//        showToast("データを保存しました");
    }

    /**
     * テーブルに直接レコードを追加する（コンテンツプロバイダを使わない場合）
     */
    public long insertWalkRecord() {
        if (walkRecordDao == null) {
            walkRecordDao = new WalkRecordDao(getApplicationContext());
        }
        Log.d(TAG, "■履歴一覧ダミーデータをインサート");
        // ダミー値
        return walkRecordDao.insertHistory("20181111", "20181111", 4, 10.0, 1000);

    }

    /**
     * テーブルのレコードを直接変更する（コンテンツプロバイダを使わない場合）
     */
    public void updateWalkRecord() {
        if (walkRecordDao == null) {
            walkRecordDao = new WalkRecordDao(getApplicationContext());
        }

        Log.d(TAG, "■履歴一覧ダミーデータを更新");
        // ダミー値
        walkRecordDao.updateHistory(walkHistoryNum, "20181111", 4, mMeter);

    }


    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
}
