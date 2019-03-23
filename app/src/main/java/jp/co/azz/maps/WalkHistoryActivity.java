package jp.co.azz.maps;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import jp.co.azz.maps.databases.HistoryDto;
import jp.co.azz.maps.databases.WalkRecordDao;

/**
 * 履歴一覧表示
 */
public class WalkHistoryActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener , AdapterView.OnItemLongClickListener, NavigationView.OnNavigationItemSelectedListener{
    private WalkRecordAdapter mAdapter;
    // 履歴一覧情報格納用
    private List<HistoryDto> historys;

    ListView listView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // リスナー
        // 各履歴タップ時
        listView = (ListView)this.findViewById(R.id.list);
        listView.setOnItemClickListener(this);

        // リスナー
        //各履歴ロングタップ時 (お散歩履歴削除処理)
        listView.setOnItemLongClickListener(this);

        WalkRecordDao walkRecordDao = new WalkRecordDao(this);
        // 散歩履歴情報を取得して一覧に表示
        historys = walkRecordDao.selectHistory();
        mAdapter = new WalkRecordAdapter(this, 0, historys);
        listView.setAdapter(mAdapter);
//        listActivity.setListAdapter(mAdapter);

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
        listView.setAdapter(mAdapter);
//        listActivity.setListAdapter(mAdapter);
    }

    @SuppressWarnings("StatementWithEmptyBody")
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
