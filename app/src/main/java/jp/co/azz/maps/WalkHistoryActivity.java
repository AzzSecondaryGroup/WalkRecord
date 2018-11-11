package jp.co.azz.maps;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

/**
 * 履歴一覧表示
 * LoaderCallbacksを実装してCursorを取得する
 * （DBをコンテンツプロバイダとして公開して、CursorLoaderで非同期で取得する）
 */
public class WalkHistoryActivity extends ListActivity  implements LoaderManager.LoaderCallbacks<Cursor> {
    private WalkRecordAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_history);

        mAdapter = new WalkRecordAdapter(this, null, 0);
        setListAdapter(mAdapter);

    }

    /**
     *
     * CursorLoader(ContentProviderから非同期にデータをロードする)を返却
     *
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                WalkRecordContentProvider.CONTENT_URI, null, null, null, "_id DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
