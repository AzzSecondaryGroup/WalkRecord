package jp.co.azz.maps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import jp.co.azz.maps.databases.WalkRecordDao;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{

    String TAG = SettingActivity.class.getName();
    private WalkRecordDao walkRecordDao;

    String msg;

    private String spinnerItems[] = {"1", "5", "10", "15", "30", "45", "60"};
    int currentTall = 0;
    int currentWeight = 0;

    ///////////// ダミーモード設定 /////////////
    private Switch dummyModeSwitch;
    private SharedPreferences saveData;
    private boolean isDummyMode = false;
    ///////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view2);
        navigationView.setNavigationItemSelectedListener(this);

        walkRecordDao = new WalkRecordDao(getApplicationContext());
        final int interval = walkRecordDao.getInterval();

        Spinner spinner = findViewById(R.id.spinner);

        // ArrayAdapter
        ArrayAdapter<String> adapter
                = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // spinner に adapter をセット
        spinner.setAdapter(adapter);
        // 地図表示間隔のDB保存値からスピナーの初期表示位置を取得
        int initPosition = adapter.getPosition(String.valueOf(interval/1000));
        // ポジション取得失敗(-1)の場合はひとまず5秒の位置を設定しておく
        if (initPosition == -1) {
            initPosition = 1;
        }
        // スピナーにセット
        spinner.setSelection(initPosition,false);

        // 身長のDB保存値があればセット
        currentTall = walkRecordDao.getTall();
        if (currentTall > 0) {
            TextView tall = findViewById(R.id.tall);
            tall.setText(String.valueOf(walkRecordDao.getTall()));
        }

        // 体重のDB保存値があればセット
        currentWeight = walkRecordDao.getWeight();
        if (currentWeight > 0) {
            TextView weight = findViewById(R.id.weight);
            weight.setText(String.valueOf(walkRecordDao.getWeight()));
        }
        this.viewSetting();

        ///////////////////////// ダミーモード設定 ///////////////////////////////////////////
        dummyModeSwitch = this.findViewById(R.id.dummy_record);
        saveData = getSharedPreferences("SettingData", Context.MODE_PRIVATE);
        // ダミーモードの状態保存値を取り出して復元する
        boolean isDummy = saveData.getBoolean("dummyModeKey", false);
        dummyModeSwitch.setChecked(isDummy);
        isDummyMode = isDummy;

        // ダミーモードSwitch変更時の状態を退避
        dummyModeSwitch.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                        isDummyMode = isChecked;
                    }
                }
        );
        ////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.main) {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.walk_history) {
            Intent intent = new Intent(getApplication(), WalkHistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.setting) {
            Intent intent = new Intent(getApplication(), SettingActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveButton:
                walkRecordDao = new WalkRecordDao(getApplicationContext());
                Spinner selectInterval = this.findViewById(R.id.spinner);
                String item = selectInterval.getSelectedItem().toString();
                String interval = String.valueOf(Integer.valueOf(item) * 1000);
                walkRecordDao.updateInterval(Integer.parseInt(interval));

                String selectTall = ((TextView)this.findViewById(R.id.tall)).getText().toString();
                // 入力された場合のみ更新
                if (!selectTall.isEmpty()) {
                    if (currentTall > 0) {
                        walkRecordDao.updateTall(Integer.parseInt(selectTall));
                    } else {
                        walkRecordDao.insertTall(Integer.parseInt(selectTall));
                    }
                }
                String selectWeight = ((TextView)this.findViewById(R.id.weight)).getText().toString();
                // 入力された場合のみ更新
                if (!selectWeight.isEmpty()) {
                    if (currentWeight > 0) {
                        walkRecordDao.updateWeight(Integer.parseInt(selectWeight));
                    } else {
                        walkRecordDao.insertWeight(Integer.parseInt(selectWeight));
                    }
                }

                /////////////////////////////////// ダミーモード設定 /////////////////////////////////

                // ダミーモードの設定状態を保存
                SharedPreferences.Editor editor = saveData.edit();
                editor.putBoolean("dummyModeKey", isDummyMode);
                editor.apply();
                ////////////////////////////////////////////////////////////////////////////////////
        }
        msg = "設定を更新しました";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        // 保存後、設定画面を終了させる
        finish();

    }

    /**
     * 初期表示時のView周りの設定を行う
     */
    private void viewSetting() {
        // 保存ボタンにリスナーを追加
        Button button = findViewById(R.id.saveButton);
        button.setOnClickListener(this);
    }
}
