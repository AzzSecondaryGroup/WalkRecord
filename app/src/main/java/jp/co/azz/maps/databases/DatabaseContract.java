package jp.co.azz.maps.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public final class DatabaseContract {

    // 定数クラスなのでデフォルトコンストラクタは隠す
    private DatabaseContract() {}

    public static abstract class History implements BaseColumns {
        public static final String TABLE_NAME = "history";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_END_DATE = "end_date";
        public static final String COLUMN_NUMBER_OF_STEPS = "number_of_steps";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_CALOLIE = "calorie";

        //history(履歴テーブル)作成
        static final String CREATE_TABLE_SQL =
                "create table " + TABLE_NAME + " "
                        + "(" + _ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_START_DATE + " TEXT NOT NULL,"
                        + COLUMN_END_DATE + " TEXT NOT NULL,"
                        + COLUMN_NUMBER_OF_STEPS + " INTEGER,"
                        + COLUMN_DISTANCE + " REAL,"
                        + COLUMN_CALOLIE + " INTEGER)";

        //history(履歴テーブル)SELECT文
        public static final String SELECT_SQL =
                "select " +" * "+" from " + TABLE_NAME;

        // TODO なぜMax指定？
//        public static final String SELECT_SQL =
//                "select " +" * "+" from " + TABLE_NAME
//                        + " where " + _ID +" =( "
//                        + " select max( "+_ID + " ) "
//                        + _ID + " from history)";
    }

    public static abstract class Coordinate implements BaseColumns  {
        //coordinate(座標テーブル)作成
        public static final String TABLE_NAME = "coordinate";
        public static final String COLUMN_NUMBER_OF_HISTORY = "number_of_history";
        public static final String COLUMN_COORDINATE_X = "coordinate_x";
        public static final String COLUMN_COORDINATE_Y = "coordinate_y";
        static final String CREATE_TABLE_SQL =
                "create table " + TABLE_NAME + " "
                        + "(" + _ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_NUMBER_OF_HISTORY + " INTEGER,"
                        + COLUMN_COORDINATE_X + " REAL NOT NULL,"
                        + COLUMN_COORDINATE_Y + " REAL NOT NULL)";

        //coordinate(座標テーブル)SELECT文
        public static final String SELECT_SQL =
                "select " +" * "+" from " + TABLE_NAME
                        + " where " + COLUMN_NUMBER_OF_HISTORY
                        +" = ? " ;

    }
}
