package jp.co.azz.maps;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import jp.co.azz.maps.databases.WalkRecordDao;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    String TAG = SettingActivity.class.getName();
    private WalkRecordDao walkRecordDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        walkRecordDao = new WalkRecordDao(getApplicationContext());
        final int interval = walkRecordDao.getInterval();

        TextView intervalView = this.findViewById(R.id.interval);
        intervalView.setText(String.valueOf(interval));

        this.viewSetting();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveButton:
                walkRecordDao = new WalkRecordDao(getApplicationContext());
                TextView intervalView = this.findViewById(R.id.interval);
                String interval = intervalView.getText().toString();
                walkRecordDao.updateInterval(Integer.parseInt(interval));
        }
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
