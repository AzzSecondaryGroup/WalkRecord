package jp.co.azz.maps.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    static final String DB_NAME = "walk_record.db";
    static final int DB_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME,null, DB_VERSION);
    }

    /**
     * データベースがない場合作成してくれる
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        //history(履歴テーブル)作成
        db.execSQL(DatabaseContract.History.CREATE_TABLE_SQL);
        //coordinate(座標テーブル)作成
        db.execSQL(DatabaseContract.Coordinate.CREATE_TABLE_SQL);
        //setting(設定テーブル)作成
        DatabaseContract.Setting.create(db);
    }

    /**
     * テーブル定義変更を適用する
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) {
        // TODO 何もしていない
        // TODO とりあえずDROPしてonCreate呼ぶとかでいい気がする(データ移行したいなら適宜)
    }
}