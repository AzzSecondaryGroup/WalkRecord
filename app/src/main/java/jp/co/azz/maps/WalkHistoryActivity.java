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
 * LoaderCallbacksを実装してCursorを取得する
 * （DBをコンテンツプロバイダとして公開して、CursorLoaderで非同期で取得する）
 */
//public class WalkHistoryActivity extends ListActivity  implements LoaderManager.LoaderCallbacks<Cursor> {
public class WalkHistoryActivity extends ListActivity implements AdapterView.OnItemClickListener {
    private WalkRecordAdapter mAdapter;
    // 履歴一覧情報格納用
    private List<HistoryDto> historys;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_history);


//        mAdapter = new WalkRecordAdapter(this, null, 0);
//        setListAdapter(mAdapter);


        // リスナー
        // 各履歴タップ時
        this.getListView().setOnItemClickListener(this);

        //一旦手動でDBから取得
//-----------------------------------------------
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

//    /**
//     *
//     * CursorLoader(ContentProviderから非同期にデータをロードする)を返却
//     *
//     * @param id
//     * @param args
//     * @return
//     */
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        return new CursorLoader(this,
//                WalkRecordContentProvider.CONTENT_URI, null, null, null, "_id DESC");
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//        mAdapter.swapCursor(cursor);
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//        mAdapter.swapCursor(null);
//    }

}
