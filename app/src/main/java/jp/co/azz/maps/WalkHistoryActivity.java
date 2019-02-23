package jp.co.azz.maps;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.List;

import jp.co.azz.maps.databases.HistoryDto;
import jp.co.azz.maps.databases.WalkRecordDao;

/**
 * 履歴一覧表示
 */
public class WalkHistoryActivity extends ListActivity
        implements AdapterView.OnItemClickListener , AdapterView.OnItemLongClickListener {
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

        // リスナー
        //各履歴ロングタップ時 (お散歩履歴削除処理)
        this.getListView().setOnItemLongClickListener(this);

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

    /**
     * ロングタップ時、タップされたお散歩履歴の削除処理
     * @param parent
     * @param view
     * @param position
     * @param id
     * @return true(ロングタップ後に、タップ処理を同時に実行させない)
     */
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
        // 一覧で選択された散歩情報を取得
        HistoryDto history = historys.get(position);
        final long historyId = history.getId();

        new AlertDialog.Builder(this)
                .setTitle("お散歩履歴を削除しますか")
                .setMessage("開始時刻："+history.getStartDate())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // OK button pressed
                        // お散歩履歴削除
                        deleteRecord(historyId);
                        showToast("お散歩履歴削除しました。");
                        //散歩履歴再取得
                        reacquire();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // cancel button pressed
                        //何も表示しない
                    }
                })
                .show();

        return true;
    }

    /**
     * TOASTメッセージ表示
     * @param msg
     */
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    /**
     * 履歴テーブル,座標テーブルより、レコード削除
     * @param historyId  履歴ID
     */
    private void deleteRecord(long historyId){
        WalkRecordDao walkRecordDao = new WalkRecordDao(this);
        //履歴テーブルのレコード削除
        walkRecordDao.deleteHistory(historyId);
        //座標テーブルのレコード削除
        walkRecordDao.deleteCoordinate(historyId);
    }

    /**
     * 散歩履歴再取得処理
     */
    private void reacquire() {
        WalkRecordDao walkRecordDao = new WalkRecordDao(this);
        // 散歩履歴情報を再取得してリスト更新
        historys = walkRecordDao.selectHistory();
        mAdapter = new WalkRecordAdapter(this, 0, historys);
        setListAdapter(mAdapter);
    }

}
