package jp.co.azz.maps;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import jp.co.azz.maps.databases.WalkRecordDao;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    String TAG = SettingActivity.class.getName();
    private WalkRecordDao walkRecordDao;

    String msg;

    private String spinnerItems[] = {"1", "5", "10", "15", "30", "45", "60"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        walkRecordDao = new WalkRecordDao(getApplicationContext());
        final int interval = walkRecordDao.getInterval();

        Toast.makeText(this, "DB取得" + interval, Toast.LENGTH_LONG).show();

//        TextView intervalView = this.findViewById(R.id.interval);
//        intervalView.setText(String.valueOf(interval / 1000));

//        this.viewSetting();

        Spinner spinner = findViewById(R.id.spinner);

        // ArrayAdapter
        ArrayAdapter<String> adapter
                = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // spinner に adapter をセット
        spinner.setAdapter(adapter);
        //todo POSIONを取得して設定すること　久保田対応中
        spinner.setSelection(4,false);
        //setText(String.valueOf(interval / 1000));

        this.viewSetting();

        // リスナーを登録
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                String item = (String) spinner.getSelectedItem();
                Toast.makeText(getApplicationContext(), "DB取得" + item, Toast.LENGTH_LONG).show();

            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveButton:
//                walkRecordDao = new WalkRecordDao(getApplicationContext());
                walkRecordDao = new WalkRecordDao(getApplicationContext());
//                TextView intervalView = this.findViewById(R.id.interval);
                Spinner selectInterval = this.findViewById(R.id.spinner);
                String item = selectInterval.getSelectedItem().toString();
                Toast.makeText(this, "選択地" + selectInterval.getSelectedItem(), Toast.LENGTH_SHORT).show();
                // String interval = intervalView.getText().toString();
                String interval = String.valueOf(Integer.valueOf(item) * 1000);
                walkRecordDao.updateInterval(Integer.parseInt(interval));
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
