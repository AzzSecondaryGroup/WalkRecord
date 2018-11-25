package jp.co.azz.maps;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.PolylineOptions;

import jp.co.azz.maps.databases.CoordinateDto;
import jp.co.azz.maps.databases.CoordinateListDto;
import jp.co.azz.maps.databases.WalkRecordDao;

public class DetailActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private WalkRecordDao walkRecordDao;
    private long historyId;

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

        Bundle extras = this.getIntent().getExtras();
        long historyId = (long)extras.get("historyId");
        walkRecordDao.selectByIdFromHistory(historyId);
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

//    private List<LatLng> getCoordinates (String historyId) {
//
//        Cursor cursor = db.query(
//                DatabaseContract.Coordinate.TABLE_NAME // table name
//                ,null // fields
//                ,DatabaseContract.Coordinate.COLUMN_NUMBER_OF_HISTORY +" = ?" // where
//                ,new String[]{historyId} // where args
//                ,null // group by
//                ,null // having
//                ,DatabaseContract.Coordinate._ID // order by
//        );
//
//        List<LatLng> coordinates = new ArrayList<>();
//        Log.v("test","before");
//        while (cursor.moveToNext()) {
//            double x =
//                    cursor.getDouble(cursor.getColumnIndex(DatabaseContract.Coordinate.COLUMN_COORDINATE_X));
//            double y =
//                    cursor.getDouble(cursor.getColumnIndex(DatabaseContract.Coordinate.COLUMN_COORDINATE_Y));
//            Log.v("test", "x:"+x+",y:"+y);
//            coordinates.add(new LatLng(x,y));
//
//        }
//        Log.v("test","after");
//
//        return coordinates;
//    }
}
