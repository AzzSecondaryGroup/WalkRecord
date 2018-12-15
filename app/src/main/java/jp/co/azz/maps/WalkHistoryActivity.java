package jp.co.azz.maps;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;
import jp.co.azz.maps.databases.HistoryDto;
import jp.co.azz.maps.databases.WalkRecordDao;

/**
 * 履歴一覧表示
 */
public class WalkHistoryActivity extends ListActivity implements AdapterView.OnItemClickListener {
    private WalkRecordAdapter mAdapter;
    // 履歴一覧情報格納用
    private List<HistoryDto> historys;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_history);

        // リスナー
        // 各履歴タップ時
        this.getListView().setOnItemClickListener(this);

        WalkRecordDao walkRecordDao = new WalkRecordDao(this);
        // 散歩履歴情報を取得して一覧に表示
        historys = walkRecordDao.selectHistory();
        mAdapter = new WalkRecordAdapter(this, 0, historys);
        setListAdapter(mAdapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this.getApplicationContext(), DetailActivity.class);
        Log.d("クリック", "pos:"+position+ " id:"+id);

        // 一覧で選択された散歩情報を取得
        HistoryDto history = historys.get(position);
        // IDを履歴画面に渡す
        intent.putExtra("historyId", history.getId());
        startActivity(intent);
    }

}
