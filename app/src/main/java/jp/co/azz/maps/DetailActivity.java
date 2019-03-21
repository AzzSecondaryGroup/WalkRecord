package jp.co.azz.maps;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.PolylineOptions;

import jp.co.azz.maps.databases.CoordinateDto;
import jp.co.azz.maps.databases.CoordinateListDto;
import jp.co.azz.maps.databases.HistoryDto;
import jp.co.azz.maps.databases.WalkRecordDao;

public class DetailActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private WalkRecordDao walkRecordDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        walkRecordDao = new WalkRecordDao(getApplicationContext());

        setContentView(R.layout.history_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // MapFragmentの生成
        MapFragment mapFragment = MapFragment.newInstance();

        // MapViewをMapFragmentに変更する
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapFragment, mapFragment);
        fragmentTransaction.commit();

        mapFragment.getMapAsync(this);

        this.viewSetting();
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        int id = item.getItemId();
        Intent intent = null;

        switch (id) {
            case R.id.walk_history:
                intent = new Intent(getApplication(), WalkHistoryActivity.class);
                break;
            case R.id.setting:
                intent = new Intent(getApplication(), SettingActivity.class);
                break;
            case R.id.main:
                intent = new Intent(getApplication(), MainActivity.class);
                break;
            default:
                Log.d(DetailActivity.class.getName(), "未設定の遷移先が指定されました");
                return false;
        }

        startActivity(intent);

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Bundle extras = this.getIntent().getExtras();
        long historyId = (long)extras.get("historyId");
        Log.d("詳細","historyId="+historyId);

        CoordinateListDto coordinates = walkRecordDao.selectCoordinate(historyId);

        PolylineOptions option = PolyLineOptionsFactory.create(coordinates.latLngs());
        mMap.addPolyline(option);

        CoordinateDto cameraPosition = coordinates.cameraPosition();
        CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(
                cameraPosition.latLng(), 16);
        mMap.moveCamera(cUpdate);
    }

    /**
     * 初期表示時のView周りの設定を行う
     */
    private void viewSetting() {
        Bundle extras = this.getIntent().getExtras();
        long historyId = (long)extras.get("historyId");
        HistoryDto history = walkRecordDao.selectByIdFromHistory(historyId);

        // 詳細画面はメイン画面を使いまわしている。
        // ただし、ボタンは不要なので消す
        ToggleButton toggleButton = findViewById(R.id.toggleButton);

        TextView distance = this.findViewById(R.id.main_distance);
        distance.setText(history.getKilometer());
        
        TextView step_cnt = this.findViewById(R.id.main_step);
        step_cnt.setText(String.valueOf(history.getNumberOfSteps()));

        TextView calorie = this.findViewById(R.id.main_calorie);
        calorie.setText(String.valueOf(history.getCalorie()));

        TextView start = this.findViewById(R.id.main_start_time);
        start.setText(history.getStartDate());

        TextView end = this.findViewById(R.id.main_end_time);
        end.setText(history.getEndDate());

        // メイン画面と詳細画面で画面共用なので、画面で描画不要な項目を消す
        toggleButton.setVisibility(View.INVISIBLE);
    }
}
