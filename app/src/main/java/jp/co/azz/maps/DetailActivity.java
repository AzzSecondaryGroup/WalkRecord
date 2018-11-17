package jp.co.azz.maps;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import jp.co.azz.maps.databases.DatabaseContract;
import jp.co.azz.maps.databases.DatabaseHelper;
import jp.co.azz.maps.databases.WalkRecordDao;

public class DetailActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseHelper helper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
        db = helper.getWritableDatabase();

        setContentView(R.layout.activity_main);
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
        fragmentTransaction.add(R.id.mapView, mapFragment);
        fragmentTransaction.commit();

        mapFragment.getMapAsync(this);

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
        } else if (id == R.id.walk_detail) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.insertMockData();

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(10);

        String historyId = getHistoryId();

        List<LatLng> coordinates = getCoordinates(historyId);

        if (coordinates.size() > 0) {
            polylineOptions.addAll(coordinates);
            mMap.addPolyline(polylineOptions);
        }
    }

    private List<LatLng> getCoordinates (String historyId) {

        Cursor cursor = db.query(
                DatabaseContract.Coordinate.TABLE_NAME // table name
                ,null // fields
                ,DatabaseContract.Coordinate.COLUMN_NUMBER_OF_HISTORY +" = ?" // where
                ,new String[]{historyId} // where args
                ,null // group by
                ,null // having
                ,DatabaseContract.Coordinate._ID // order by
        );

        List<LatLng> coordinates = new ArrayList<>();
        Log.v("test","before");
        while (cursor.moveToNext()) {
            double x =
                    cursor.getDouble(cursor.getColumnIndex(DatabaseContract.Coordinate.COLUMN_COORDINATE_X));
            double y =
                    cursor.getDouble(cursor.getColumnIndex(DatabaseContract.Coordinate.COLUMN_COORDINATE_Y));
            Log.v("test", "x:"+x+",y:"+y);
            coordinates.add(new LatLng(x,y));

        }
        Log.v("test","after");

        return coordinates;
    }

    /**
     * ヒストリーIDを取得する
     * 一覧から呼ぶようになったら不要になる予定
     * @return 履歴ID
     */
    @Nullable
    private String getHistoryId() {
        Cursor cursor = db.query(
                DatabaseContract.History.TABLE_NAME // table name
                ,null // fields
                ,null // where
                ,null // where args
                ,null // group by
                ,null // having
                ,null // order by
                ,"1"
        );

        if (0 < cursor.getCount()) {
            cursor.moveToFirst();
            String id = cursor.getString(cursor.getColumnIndex(DatabaseContract.History._ID));
            return id;
        }

        return null;
    }

    private void insertMockData() {

        WalkRecordDao dao = new WalkRecordDao(getApplicationContext());
        {
            Cursor query = db.query(
                    DatabaseContract.History.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            while (query.moveToNext()) {
                int id = query.getInt(query.getColumnIndex(DatabaseContract.History._ID));
                dao.deleteHistory(id);
            }
        }
        {
            Cursor query = db.query(
                    DatabaseContract.Coordinate.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            while (query.moveToNext()) {
                int id = query.getInt(query.getColumnIndex(DatabaseContract.History._ID));
                dao.deleteCoordinate(id);
            }
        }
        this.insertHistory();

        Cursor historyCursor;
        int historyId;
        historyCursor = db.query(
                DatabaseContract.History.TABLE_NAME // table name
                , null // fields
                , null // where
                , null // where args
                , null // group by
                , null // having
                , null // order by
                , "1"
        );

        historyId = -1;

        if (historyCursor != null) {
            if (0 < historyCursor.getCount()){
                historyCursor.moveToFirst();
                historyId = historyCursor.getInt(historyCursor.getColumnIndex(DatabaseContract.History._ID));
            }
        }

        if (0 <= historyId) {
            insertCoordinate(historyId);
        }
    }

    private boolean insertHistory() {
        WalkRecordDao dao = new WalkRecordDao(getApplicationContext());
        dao.insertHistory(
                "",
                "",
                100,
                2000.333,
                900
                );

        return false;
    }

    private void insertCoordinate(int historyId) {
        WalkRecordDao dao = new WalkRecordDao(getApplicationContext());
        dao.insertCoordinate(historyId, 35.712206, 139.706787);
        dao.insertCoordinate(historyId, 36.712206, 140.706787);
        dao.insertCoordinate(historyId, 37.712206, 141.706787);
        dao.insertCoordinate(historyId, 38.712206, 142.706787);
        dao.insertCoordinate(historyId, 39.712206, 143.706787);
        dao.insertCoordinate(historyId, 40.712206, 144.706787);
    }
}
