package jp.co.azz.maps;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jp.co.azz.maps.Common.VersionCheck;
import jp.co.azz.maps.databases.DatabaseContract;
import jp.co.azz.maps.databases.DatabaseHelper;
import jp.co.azz.maps.databases.HistoryDto;
import jp.co.azz.maps.databases.WalkRecordDao;
import jp.co.azz.maps.Service.LocationManageService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, View.OnClickListener {
private static final String TAG = "MainActivity";

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    // サンプルはINTERVAL:5000(ミリ秒) ,FASTESTINTERVAL:16
    private static int INTERVAL = 5000;
    private static final int FASTESTINTERVAL = 5000;

    private GoogleMap mMap;

//    private GoogleApiClient googleApiClient;
    // 位置情報の取得間隔などの設定
    private static final LocationRequest LOCATION_REQUEST = LocationRequest.create()
            .setInterval(INTERVAL)              // 位置情報の更新間隔をミリ秒で設定
            .setFastestInterval(FASTESTINTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);   //位置情報取得要求の優先順位（PRIORITY_HIGH_ACCURACY：高い正確性）

//    // GPS,WiFi,電話基地局からの位置情報を取得するAPI
//    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    // 移動経路を描くための情報のリスト
    private List<LatLng> mRunList = new ArrayList<LatLng>();
    private double mMeter = 0.0;           // メートル
    private DatabaseHelper dbHelper;
    private boolean mStart = false;
    private boolean mFirst = false;
    private boolean mStop = true;   // 開始時は停止
    private WalkRecordDao walkRecordDao;
    private int walkHistoryNum = 0;
    private boolean isFirstMapDisp = true;  // MAP最初の表示かどうか
    private boolean isLocationAuthorityReady = false;  // 位置情報の権限周りの準備ができているか

    //歩数取得
    private int stepCont;
    private double lengths;
    //消費カロリー取得
    private int burnedCalories;
    private double weight;
    private int speed = 4 ;//時速(km/hr)

    // Map初期表示位置をローカルファイルに保持しておくために使用
    SharedPreferences initPosData;

    ///////////// ダミーモード設定 /////////////
    SharedPreferences saveData;
    boolean isDummyMode = false;
    ///////////////////////////////////////

    /**
     * onPauseの直後に呼ばれる処理
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // メンバー変数の状態を保存
        outState.putBoolean("IS_FIRST_MAP_DISP", isFirstMapDisp);
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
//        walkRecordDao = new WalkRecordDao(getApplicationContext());
//
//        List<HistoryDto> historyList = walkRecordDao.selectHistory();
//        Log.d(TAG, "◆散歩履歴テーブルのダミーデータの件数：" + historyList.size() + "◆");
//        if (historyList.size() > 0) {
//            HistoryDto historyDto = historyList.get(historyList.size() - 1);
//            Log.d(TAG, "散歩履歴テーブルのダミーデータ（最新）：");
//            Log.d(TAG, "　id：" + historyDto.getId());
//            Log.d(TAG, "　開始日時：" + historyDto.getStartDate());
//            Log.d(TAG, "　終了日時：" + historyDto.getEndDate());
//            Log.d(TAG, "　距離：" + historyDto.getKilometer());
//            Log.d(TAG, "　歩数：" + historyDto.getNumberOfSteps());
//            Log.d(TAG, "　カロリー：" + historyDto.getCalorie());
//        }

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

//        // Google Playサービスへの入り口
//        googleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        // DatabaseHelperのインスタンス生成
        // DBが存在しない場合はこのタイミングで作成される
        dbHelper = new DatabaseHelper(this);

        this.viewSetting();

        initPosData = getSharedPreferences("PositionData", Context.MODE_PRIVATE);

        // ****************** バージョン確認 ******************
        if (VersionCheck.isExistNewVersion(getApplicationContext())) {
            new AlertDialog.Builder(this)
                    .setTitle("新しいバージョンに更新可能です")
                    .setPositiveButton("今すぐ更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // OK button pressed
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=jp.co.azz.maps")));
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("後で更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
        // ***************************************************
    }

    /**
     * Activityの表示、再表示の処理
     * （初回表示、バックグラウンドから復帰後（onStartの後）、他のActivityから戻ったタイミング）
     */
    @Override
    protected void onResume() {
        super.onResume();
        //////////////////////////////////// ダミーモード設定値取得 ////////////////////////////////////////
//        saveData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
//        isDummyMode = saveData.getBoolean("dummyModeKey", false);
        ////////////////////////////////////////////////////////////////////////////////////////////////////

        walkRecordDao = new WalkRecordDao(getApplicationContext());
        //歩幅を取得（身長*0.45）
        int tall = walkRecordDao.getTall();
        if (tall == 0) {
            tall = DatabaseContract.Setting.DEFAULT_TALL;
        }
        lengths = (double) tall * 45 / 100;

        //体重を取得
        weight = walkRecordDao.getWeight();
        if (weight == 0) {
            weight = DatabaseContract.Setting.DEFAULT_WEIGHT;
        }

//        // Google Playサービスに接続する
//        googleApiClient.connect();

        // バックグラウンドから戻った時にGoogleサービスに接続可能な場合は接続する
        locationReadyProcess();

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.walk_history) {
            Intent intent = new Intent(getApplication(), WalkHistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.setting) {
            Intent intent = new Intent(getApplication(), SettingActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * マップの初期化完了時に呼ばれる処理
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (isFirstMapDisp) {
            // Map初期表示用の位置座標のインスタンスを作成(緯度、経度)
            float latitude = initPosData.getFloat("latitude", 0.0f);
            float longitude = initPosData.getFloat("longitude", 0.0f);
            LatLng initLatLng;
            if (latitude == 0.0f || longitude == 0.0f) {
                // 取得できないときはダミー（高田馬場を表示）
                initLatLng = new LatLng(35.712206, 139.706787);
            } else {
                initLatLng = new LatLng(latitude, longitude);
            }
            CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(initLatLng, 18);
            mMap.moveCamera(cUpdate);
        }

        PolylineOptions option = PolyLineOptionsFactory.create();

        // 位置情報権限周り判定処理
        locationAuthorityJudge();

        // 現在地ボタンを表示
        setMyLocationButton();

    }

    /**
     * 位置情報へのアクセス権を確認して必要な処理を行う
     */
    private void locationAuthorityJudge() {
        isLocationAuthorityReady = false;

        // ***** このアプリ自体に位置情報へのアクセス権が設定されているかの確認と権限リクエスト処理 ***** /
        // DangerousなPermissionはリクエストして許可をもらわないと使えない
        // お散歩アプリに位置情報を使用する権限がない場合
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // お散歩アプリに位置情報へのアクセス許可をしない選択をすでに行っているか
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                //一度拒否された時、Rationale（理論的根拠）を説明して、再度許可ダイアログを出すようにする
                new AlertDialog.Builder(this)
                        .setTitle("アクセス許可が必要です")
                        .setMessage("移動に合わせて地図を動かすためには、当アプリの位置情報へのアクセスを許可してください")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // OK button pressed
                                // このアプリの位置情報へのアクセス権限リクエストのダイアログ表示
                                requestAccessFineLocation();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showToast(getString(R.string.location_unauthorized_msg));
                            }
                        })
                        .show();
            } else {
                // まだ許可を求める前の時、許可を求めるダイアログを表示
                requestAccessFineLocation();
            }
        } else {
            // ***** 端末自体の位置情報がONになっているか確認とOFFの場合はONにするように促す処理 ***** /
            // このアプリに位置情報へのアクセス権がある場合のみ処理を行う
            isLocationAuthorityReady = isTerminalLocationEnabled();

            // アクセス権限、設定ともに問題なければGoogleAPI接続
            locationReadyProcess();
        }
    }

    /**
     * お散歩アプリに位置情報へのアクセスを許可するかどうか確認するダイアログ
     */
    private void requestAccessFineLocation() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

    }

    /**
     * 当アプリのアクセス許可確認ダイアログの承認結果を受け取る
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // ユーザーが許可したとき
                // 許可が必要な機能を改めて実行する
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 現在地表示ボタンを設定する
                    setMyLocationButton();

                }
                else {
                    // ユーザーが許可しなかったとき
                    // 許可されなかったため機能が実行できないことを表示する
                    showToast(getString(R.string.location_unauthorized_msg));
                    // 以下は、java.lang.RuntimeException になる
                    // mMap.setMyLocationEnabled(true);
                }
                return;
            }
        }
    }

    /**
     * 現在地表示ボタンを設定する
     */
    private void setMyLocationButton() {

        // 位置情報アクセス権限があれば現在地ボタンを表示
        if (ActivityCompat.checkSelfPermission (
                getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // MyLocationレイヤーを有効にする
            mMap.setMyLocationEnabled(true);
            UiSettings settings = mMap.getUiSettings();
            // MyLocationButtonを有効に
            settings.setMyLocationButtonEnabled(true);
        }
    }

//    /**
//     * Google Playサービスに接続したときの処理
//     * @param bundle
//     */
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        Log.d(TAG, "■Google Playサービスに接続");
//        // 位置情報取得の権限を保持しているかチェック
//        // 権限がない場合は後続処理を行わない
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        // 位置情報が変わった時に通知を受け取るためのリクエスト
//        // onLocationChanged が呼び出されるようになる
//        fusedLocationProviderApi.requestLocationUpdates(googleApiClient, LOCATION_REQUEST, this);
//
//    }

//    /**
//     * 位置情報が更新された場合の処理
//     * 地図の移動、住所の取得、移動線の描画、移動距離の累計を行う
//     *
//     * @param location
//     */
//    @Override
//    public void onLocationChanged(Location location) {
//
//        Log.d(TAG, "■位置情報が更新されたとき");
//        Log.d(TAG, "■緯度、経路："+location.getLatitude() + ", " + location.getLongitude());
//
//        // 位置情報取得
//        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//        // showToast("currentLatLngの確認"+currentLatLng);
//        // まだ一度もMap表示していない場合のみ最初のMap表示を行う
//        if (isFirstMapDisp) {
//            // カメラの倍率、ポジション変更
//            CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(
//            new LatLng(location.getLatitude(), location.getLongitude()), 18);
//            mMap.moveCamera(cUpdate);
//            Log.d(TAG, "■最初の地図の位置更新");
//
//            // 次の起動時に位置情報がすぐにとれない場合に初期表示する位置として保存する
//            SharedPreferences.Editor editor = initPosData.edit();
//            editor.putFloat("latitude", (float)location.getLatitude());
//            editor.putFloat("longitude", (float)location.getLongitude());
//            editor.apply();
//
//            isFirstMapDisp = false;
//        }
//
//        // stop後は動かさない
//        if (mStop) {
//            return;
//        }
//
//        // ********** テスト用ダミーデータの作成 *************
//        if(!mRunList.isEmpty() && isDummyMode) {
//            LatLng dummy = mRunList.get(mRunList.size() - 1);
//            // 屋内のテスト用に位置を変える
//            dummy = new LatLng(dummy.latitude + 0.02, dummy.longitude + 0.02);
//
//            currentLatLng = dummy;
//        }
//
//        // ***************************************************
//
//
//        // カメラの倍率、ポジション変更
//        CameraPosition cameraPos = new CameraPosition.Builder()
//                .target(currentLatLng).zoom(18)
//                .bearing(0).build();
//        // 地図の中心を取得した緯度、経度に動かす
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
//        Log.d(TAG, "■地図の位置更新");
//
//        //マーカー設定
//        // TODO　マーカーはどうするか後で検討
//
//        mMap.clear();
//        MarkerOptions options = new MarkerOptions();
//        //options.position(latlng);
//        options.position(currentLatLng);
////        // ランチャーアイコン
//        // ここでエラー出てるみたいなので一旦コメントアウト
////        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
////        options.icon(icon);
////        Log.d(TAG, "■アイコン作成");
//        // 現在地にマーカーを追加
//        mMap.addMarker(options);
//        Log.d(TAG, "■マーカーを追加");
//        if (mStart) {
//            //表示されている計測データを初期化
//            creaDate();
//
//            // 初回スタート時
//            if (mFirst) {
//                Log.d(TAG, "■スタート後初回の位置情報インサート");
//                // 散歩履歴をインサート
//                walkHistoryNum = (int) walkStart(currentLatLng);
//                walkRecordDao.insertCoordinate(walkHistoryNum,currentLatLng.latitude,currentLatLng.longitude);
//                Log.d(TAG, "■散歩履歴インサート（レコードNo）：" + walkHistoryNum);
//                mFirst = !mFirst;
//            } else {
//                // 2回目以降の位置取得の場合
//                // 走行距離を累積
//                sumDistance();
//
//                // 歩数計算
//                stepCalc();
//                // 消費カロリー計算
//                calorieCalc();
//
//                //座標更新、履歴テーブル更新
//                updateWalkRecord(currentLatLng);
//            }
//            // 移動線を描画
//            drawTrace(currentLatLng);
//        }
//    }

    /**
     * 線を引く
     * @param latlng
     */
    private void drawTrace(LatLng latlng) {
        mRunList.add(latlng);
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.addAll(mRunList);
        polyOptions.color(Color.BLUE);
        polyOptions.width(15);
        polyOptions.geodesic(false);
        mMap.addPolyline(polyOptions);
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

    /**
     * 距離と歩幅から歩数を計算する
     */
    private void stepCalc(){

        BigDecimal bi = new BigDecimal(lengths);
        double stepSize= bi.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();

        stepCont = (int) ((mMeter*100)/ stepSize); //cm単位で計算

        TextView main_step = findViewById(R.id.main_step);
        main_step.setText(stepCont + "歩");
    }

    /**
     * 体重から消費カロリーを計算する
     */
    private void calorieCalc(){

        //List<HistoryDto> walkingTime = walkRecordDao.selectHistory().;
        //カロリー計算は、距離/時速で所要時間を時間単位で計算し、所要時間からカロリー計算を実施。
        burnedCalories = (int) (1.05 * 3.5 * weight * ( (mMeter / 1000 ) / speed ) ); //kcal単位で計算

        TextView main_calorie = findViewById(R.id.main_calorie);
        main_calorie.setText(burnedCalories + "kcal");
    }

//    /**
//     * Activityがフォアグラウンドでなくなるとき
//     */
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        if (googleApiClient.isConnected() ) {
//            stopLocationUpdates();
//        }
//        // Google Playサービスの接続を止める
//        googleApiClient.disconnect();
//
//    }

    /**
     * Activityが非表示になるとき
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

//    /**
//     * 位置情報のリクエストを解除する
//     */
//    protected void stopLocationUpdates() {
//        fusedLocationProviderApi.removeLocationUpdates(googleApiClient, this);
//    }
//    @Override
//    public void onConnectionSuspended(int cause) {
//        // Do nothing
//    }
//    @Override
//    public void onConnectionFailed(ConnectionResult result) {
//        // Do nothing
//    }

    /**
     * 記録を開始する
     */
    public long walkStart(LatLng currentLatLng) {
        if (walkRecordDao == null) {
            walkRecordDao = new WalkRecordDao(getApplicationContext());
        }
        Log.d(TAG, "■履歴一覧ダミーデータをインサート");

        String startTime = AppContract.now();
        {
            TextView startTimeView = this.findViewById(R.id.main_start_time);
            startTimeView.setText(startTime);
        }

        return walkRecordDao.insertHistory(startTime, startTime, 0, 0.0, 0);

    }

    /**
     * テーブルのレコードを変更する
     */
    public void updateWalkRecord(LatLng currentLatLng) {

        if (walkRecordDao == null) {
            walkRecordDao = new WalkRecordDao(getApplicationContext());
        }
        Log.d(TAG, "■座標データをインサート");
        walkRecordDao.insertCoordinate(walkHistoryNum,currentLatLng.latitude,currentLatLng.longitude);
        Log.d(TAG, "■座標件数"+walkRecordDao.selectCoordinateCount());

        Log.d(TAG, "■履歴一覧データを更新");

        String endTime = AppContract.now();
        TextView endTimeView = this.findViewById(R.id.main_end_time);
        endTimeView.setText(endTime);
        // ダミー値
        walkRecordDao.updateHistory(walkHistoryNum, endTime, stepCont, mMeter, burnedCalories);
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggleButton:
                ToggleButton button = (ToggleButton) view;
                // トグルキーが変更された際に呼び出される
                // ONになった場合
                if (button.isChecked()) {

                    // START押下時にもアプリの位置情報アクセス権と端末の位置情報設定を確認する
                    locationAuthorityJudge();

                    // 位置情報取得準備ができていない場合は処理を終了
                    if (!isLocationAuthorityReady) {
                        //トグルボタンをSTARTに戻す。
                        button.setChecked(false);
                        return;
                    }

//                    // STOPで接続中断後の可能性もあるのでGoogleサービス接続などを行う
//                    locationReadyProcess();

                    Log.d(TAG, "■経路取得開始");
                    int interval = walkRecordDao.getInterval();
                    if (interval == 0) {
                        interval = DatabaseContract.Setting.DEFAULT_INTERVAL;
                    }
                    Log.d(TAG, "■インターバル"+ interval);
                    LOCATION_REQUEST.setInterval(interval);
                    LOCATION_REQUEST.setFastestInterval(interval);
                    mStart = true;
                    mFirst = true;
                    mStop = false;
                    mMeter = 0.0;
                    mRunList.clear();

                    // 散歩記録開始メッセージ表示
                    showToast("散歩の記録を開始しました。");

                    // サービスの位置情報取得処理を呼び出し
                    Intent intent = new Intent(getApplication(), LocationManageService.class);
                    startService(intent);

                } else {
//                    if (googleApiClient.isConnected() ) {
//                        stopLocationUpdates();
//                    }
//                    // Google Playサービスの接続を止める
//                    googleApiClient.disconnect();

                    mStop = true;
                    mStart = false;

                    TextView endTime = this.findViewById(R.id.main_end_time);
                    endTime.setVisibility(View.VISIBLE);
                    endTime.setText(AppContract.now());

                    // 散歩記録終了メッセージ表示
                    showToast("散歩の記録を終了しました。");

                    Intent intent = new Intent(getApplication(), LocationManageService.class);
                    stopService(intent);
                }
        }
    }

    /**
     * 初期表示時のView周りの設定を行う
     */
    private void viewSetting() {
        // メイン画面と詳細画面で画面共用なので、メイン画面で描画不要な項目を消す
        TextView endTime = this.findViewById(R.id.main_end_time);
        endTime.setVisibility(View.INVISIBLE);

        // 初期表示はOFF
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setChecked(false);

        // トグルボタンにリスナーを追加
        toggleButton.setOnClickListener(this);

    }

/**
 * 端末の位置情報機能の状態が有効か無効かを判断する。
 * 位置情報がOFFの場合、ONにするよう促す、ダイアログを出す。
 * 位置情報モードが"GPSのみ利用" の場合ダイアログを出す。
 */
    private boolean isTerminalLocationEnabled() {

        // 位置情報有効か
        boolean isLocationInvalid = false;
        // GPSのみになっていないか
        boolean isGpsOnly = false;

        // APIレベル19以上の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                // 位置情報設定取得
                int locationMode = Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE);
                if (locationMode == Settings.Secure.LOCATION_MODE_OFF){
                    isLocationInvalid = true;
                } else if (locationMode == Settings.Secure.LOCATION_MODE_SENSORS_ONLY) {
                    isGpsOnly = true;
                }
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                showToast("位置情報の設定状況確認に失敗しました");
            }
        } else {
            String gpsStatus = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (!gpsStatus.contains("gps") && !gpsStatus.contains("network")) {
                isLocationInvalid = true;
            } else if (gpsStatus.contains("gps") && !gpsStatus.contains("network")) {
                isGpsOnly = true;
            }
        }


        if (isLocationInvalid) {
            //位置情報が無効だった場合
            //ダイアログで位置情報をONにするように促すメッセージを出す。
            new AlertDialog.Builder(this)
                    .setTitle("端末の位置情報設定がOFFになっています")
                    .setMessage("このアプリを使用するには、端末の位置情報設定をONにして位置情報モードを「GPSのみ」以外にしてください。")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // OK button pressed
                            //何もしない
                        }
                    })
                    .show();
                    return false;
        } else if (isGpsOnly) {
            //GPSの位置情報モードが"GPSのみ利用" の場合
            //ダイアログでGPSの位置情報モードが"GPSのみ利用" 以外にするようメッセージを出す。
            new AlertDialog.Builder(this)
                    .setTitle("端末の位置情報モードが 「GPSのみ」になっています")
                    .setMessage("このアプリを使用するには、端末の位置情報モードを「GPSのみ」以外にしてください。")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // OK button pressed
                            //何もしない
                        }
                    })
                    .show();
                    return false;
        }

        return true;
    }

    /**
     * 位置情報の取得準備が整っているときの最初の処理
     */
    private void locationReadyProcess() {

        // ここでサービスを開始するようなイメージ？

        // TODO 電力消費抑えることを考える場合はもう少し考える
        // 現状、端末の位置情報設定変更は検知できないので初回マップ表示前に一度接続しておく
        if (isFirstMapDisp) {
//            // Google Playサービスに接続する
//            googleApiConnect();
        }

        // 権限、位置情報設定ともに問題ない場合
        if (isLocationAuthorityReady) {

//            // Google Playサービスに接続する
//            googleApiConnect();
        }
    }
//
//    /**
//     * googleApiClientに接続する
//     */
//    private void googleApiConnect() {
//
//        if (!googleApiClient.isConnected() ) {
//            googleApiClient.connect();
//        }
//    }

    /**
     * メイン画面の計測値の初期化
     */
    private void creaDate(){

        TextView main_end_time = this.findViewById(R.id.main_end_time);
        main_end_time.setVisibility(View.INVISIBLE);
        TextView main_distance =(TextView) findViewById(R.id.main_distance);
        main_distance.setText(String.format("0km"));
        TextView main_step = (TextView) findViewById(R.id.main_step);
        main_step.setText(String.format("0歩"));
        TextView main_calorie = (TextView) findViewById(R.id.main_calorie);
        main_calorie.setText(String.format("0cal"));
    }
}
