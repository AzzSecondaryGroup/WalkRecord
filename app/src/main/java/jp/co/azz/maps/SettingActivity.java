package jp.co.azz.maps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import jp.co.azz.maps.databases.WalkRecordDao;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    String TAG = SettingActivity.class.getName();
    private WalkRecordDao walkRecordDao;

    String msg;

    private String spinnerItems[] = {"1", "5", "10", "15", "30", "45", "60"};

    ///////////// ダミーモード設定 /////////////
    private Switch dummyModeSwitch;
    private SharedPreferences saveData;
    private boolean isDummyMode = false;
    ///////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

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

        TextView tall = (TextView) findViewById(R.id.tall);
        tall.setText(String.valueOf(walkRecordDao.getTall()));

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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveButton:
                walkRecordDao = new WalkRecordDao(getApplicationContext());
                Spinner selectInterval = this.findViewById(R.id.spinner);
                String item = selectInterval.getSelectedItem().toString();
                String interval = String.valueOf(Integer.valueOf(item) * 1000);
                walkRecordDao.updateInterval(Integer.parseInt(interval));

                String selectTall = ((TextView)this.findViewById(R.id.tall)).getText().toString();
                selectTall = selectTall.isEmpty() ? "170" : selectTall;
                walkRecordDao.updateTall(Integer.parseInt(selectTall));

                /////////////////////////////////// ダミーモード設定 /////////////////////////////////

                // ダミーモードの設定状態を保存
                SharedPreferences.Editor editor = saveData.edit();
                editor.putBoolean("dummyModeKey", isDummyMode);
                editor.apply();
                ////////////////////////////////////////////////////////////////////////////////////
        }
        msg = "設定を更新しました";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
