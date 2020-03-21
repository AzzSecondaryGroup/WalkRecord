package jp.co.azz.maps;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.ref.WeakReference;

import jp.co.azz.maps.Common.VersionCheck;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, View.OnClickListener {
private static final String TAG = "MainActivity";

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private GoogleMap mMap;

    // 移動経路を描くための情報
    PolylineOptions polyOptions;
    private boolean isFirstMapDisp = true;  // MAP最初の表示かどうか
    private boolean isLocationAuthorityReady = false;  // 位置情報の権限周りの準備ができているか


    // サービス起動状態（getRunningServicesがAndroid8から廃止されたので自前で管理）
    private boolean isServiceStarted = false;

    // Map初期表示位置をローカルファイルに保持しておくために使用
    SharedPreferences initPosData;

    // Serviceから更新結果を受け取る場合に使用するブロードキャスト
    UpdateReceiver receiver;
    IntentFilter filter;

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

        receiver = new UpdateReceiver();
        filter = new IntentFilter("UPDATE_ACTION");

        initPolylineOptions();


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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        this.viewSetting();

        initPosData = getSharedPreferences("PositionData", Context.MODE_PRIVATE);

        // ****************** バージョン確認 ******************
//        if (VersionCheck.isExistNewVersion(getApplicationContext())) {
//            new AlertDialog.Builder(this)
//                    .setTitle("新しいバージョンに更新可能です")
//                    .setPositiveButton("今すぐ更新", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // OK button pressed
//                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=jp.co.azz.maps")));
//                            dialog.dismiss();
//                        }
//                    })
//                    .setNegativeButton("後で更新", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    })
//                    .setCancelable(false)
//                    .show();
//        }
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

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        new AlertDialog.Builder(this)
                .setTitle("アプリを終了しますか")
                .setMessage("散歩情報の記録も終了します")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
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
            // Map初期表示用の位置座標のインスタンスを作成(取得できないときはダミーで高田馬場)
            float latitude = initPosData.getFloat("latitude", 0.0f);
            float longitude = initPosData.getFloat("longitude", 0.0f);
            if (latitude == 0.0f || longitude == 0.0f) {
                // どちらかが取得できないときはダミー（高田馬場を表示）
                latitude = 35.712206f;
                longitude = 139.706787f;
            }
            LatLng initLatLng = new LatLng(latitude, longitude);

            CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(initLatLng, 16);
            mMap.moveCamera(cUpdate);

            // 位置情報権限周り判定処理
            locationAuthorityJudge();

            // 現在地ボタンを表示
            setMyLocationButton();

        }
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

            // Mapを現在地に移動
            setStartLocation();
        }
    }

    /**
     * お散歩アプリに位置情報へのアクセスを許可するかどうか確認するダイアログ
     * （OS依存のダイアログ表示）
     */
    private void requestAccessFineLocation() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

    }

    /**
     * お散歩アプリへのアクセス許可確認ダイアログの承認結果を受け取る
     * （OS依存のダイアログ表示に対する操作結果）
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
     * 端末の位置情報機能の状態が有効か無効かを判断する。
     * 位置情報がOFFの場合、ONにするよう促す、ダイアログを出す。
     */
    private boolean isTerminalLocationEnabled() {

        // 位置情報有効か
        boolean isLocationInvalid = false;

        try {
            // 位置情報設定取得
            int locationMode = Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (locationMode == Settings.Secure.LOCATION_MODE_OFF){
                isLocationInvalid = true;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            showToast("位置情報の設定状況確認に失敗しました");
        }

        if (isLocationInvalid) {
            //位置情報が無効だった場合
            //ダイアログで位置情報をONにするように促すメッセージを出す。
            new AlertDialog.Builder(this)
                    .setTitle("端末の位置情報設定がOFFになっています")
                    .setMessage("このアプリを使用するには、端末の位置情報設定をONにしてください。")
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
     * 操作開始時点での位置情報に画面を移動
     */
    private void setStartLocation() {
        // パーミッションの確認
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
            }, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    Log.w(TAG, "addOnSuccessListener location is null.");
                } else {
                    // 次の起動時に位置情報がすぐにとれない場合に初期表示する位置として保存する
                    SharedPreferences.Editor editor = initPosData.edit();
                    editor.putFloat("latitude", (float)location.getLatitude());
                    editor.putFloat("longitude", (float)location.getLongitude());
                    editor.apply();

                    // 地図の中心を取得した位置に動かす
                    CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 16);
                    mMap.moveCamera(cUpdate);
                }
                isFirstMapDisp = false;
            }
        });
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

    /**
     * Activityがフォアグラウンドでなくなるとき
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Activityが非表示になるとき
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopLocationService();
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
                    // 表示データ初期化
                    clearDisplayData();

                    // サービス開始
                    startLocationService();

                    TextView startTime = this.findViewById(R.id.main_start_time);
                    startTime.setText(AppContract.now());

                    // 散歩記録開始メッセージ表示
                    showToast("散歩の記録を開始しました。");

                } else {
                    // サービス終了
                    stopLocationService();

                    TextView endTime = this.findViewById(R.id.main_end_time);
                    endTime.setVisibility(View.VISIBLE);
                    endTime.setText(AppContract.now());

                    // 散歩記録終了メッセージ表示
                    showToast("散歩の記録を終了しました。");
                }
        }
    }

    /**
     * 位置情報取得サービス開始
     */
    private void startLocationService() {
        if (!isServiceStarted) {
            isServiceStarted = true;

            Intent intent = new Intent(getApplication(), LocationService.class);
            startForegroundService(intent);

            // レシーバー登録
            registerReceiver(receiver, filter);

            LocationHandler handler = new LocationHandler(this);
            receiver.registerHandler(handler);
        }
    }

    /**
     * 位置情報取得サービス終了
     */
    private void stopLocationService() {
        if (isServiceStarted) {
            isServiceStarted = false;

            Intent intent = new Intent(getApplication(), LocationService.class);
            stopService(intent);

            // レシーバー解除
            unregisterReceiver(receiver);
        }
    }

    /**
     * サービスから値を受け取ったら行う処理
     */
    private class LocationHandler extends Handler {
        private final WeakReference<MainActivity> refMainActivity;

        public LocationHandler (MainActivity mainActivity) {
            refMainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {

            MainActivity activity = refMainActivity.get();
            if (activity == null) {
                Log.d(TAG, "handleMessage.activity is null");
                return;
            }

            Bundle bundle = msg.getData();
            int stepCont = bundle.getInt("stepCont");
            double totalDistance = bundle.getDouble("totalDistance");
            int burnedCalories = bundle.getInt("burnedCalories");
            double[] currentLocation = bundle.getDoubleArray("currentLocation");

            TextView main_step = (TextView) findViewById(R.id.main_step);
            main_step.setText(stepCont + "歩");
            TextView main_distance =(TextView) findViewById(R.id.main_distance);
            main_distance.setText(String.format("%.2f"+" km", totalDistance));
            TextView main_calorie = (TextView) findViewById(R.id.main_calorie);
            main_calorie.setText(burnedCalories + "kcal");

            drawTrace(currentLocation);
        }
    }

    /**
     * 線を引く
     * @param currentLocation 現在地
     */
    private void drawTrace(double[] currentLocation) {
        if (currentLocation == null) {
            Log.w(TAG, "drawTrace currentLocation is null.");
            return;
        }

        LatLng currentLatLng = new LatLng(currentLocation[0], currentLocation[1]);
        polyOptions.add(currentLatLng);

        mMap.clear();
        // 現在地のマーカー設定
        MarkerOptions options = new MarkerOptions();
        options.position(currentLatLng);
        mMap.addMarker(options);

        if (polyOptions.getPoints().size() >= 2) {
            // 線を引いてアニメーション付きでカメラを移動
            mMap.addPolyline(polyOptions);

            CameraPosition cameraPos = new CameraPosition.Builder()
                    .target(currentLatLng).zoom(16)
                    .bearing(0).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
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
     * メイン画面の計測値表記の初期化
     */
    private void clearDisplayData() {

        TextView main_end_time = this.findViewById(R.id.main_end_time);
        main_end_time.setVisibility(View.INVISIBLE);
        TextView main_distance = findViewById(R.id.main_distance);
        main_distance.setText(String.format("0km"));
        TextView main_step = findViewById(R.id.main_step);
        main_step.setText(String.format("0歩"));
        TextView main_calorie = findViewById(R.id.main_calorie);
        main_calorie.setText(String.format("0cal"));

        if (!polyOptions.getPoints().isEmpty()) {
            initPolylineOptions();
        }

        mMap.clear();
    }

    /**
     * 経路描画情報初期化
     */
    private void initPolylineOptions() {
        polyOptions = new PolylineOptions();
        polyOptions.color(Color.BLUE);
        polyOptions.width(16);
        polyOptions.geodesic(false);
    }
}
