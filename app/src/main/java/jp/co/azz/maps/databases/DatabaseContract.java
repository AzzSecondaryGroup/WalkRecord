package jp.co.azz.maps.databases;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

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

    public static abstract class Setting implements BaseColumns {
        public static final String TABLE_NAME = "settings";
        public static final String COLUMN_KEY = "set_key";
        public static final String COLUMN_VALUE = "value";

        static final String CREATE_TABLE_SQL =
                "create table " + TABLE_NAME + " "
                        + "(" + _ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_KEY + " TEXT NOT NULL,"
                        + COLUMN_VALUE + " TEXT NOT NULL" +
                        ")";
        public static final String SETTING_INTERVAL = "interval";
        public static final String SETTING_TALL = "tall";
        public static final String SETTING_WEIGHT = "weight";
        public static final int DEFAULT_INTERVAL = 5000;
        public static final int DEFAULT_TALL = 170;
        public static final int DEFAULT_WEIGHT = 50;

        public static void create(SQLiteDatabase db) {
            db.execSQL(DatabaseContract.Setting.CREATE_TABLE_SQL);
            insertDefault(db);
        }

        /**
         * 設定テーブルの初期値をInsert（初期値でInsertするのは地図情報取得間隔のみとする）
         * @param db
         */
        private static void insertDefault(SQLiteDatabase db) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_KEY, SETTING_INTERVAL);
            cv.put(COLUMN_VALUE, DEFAULT_INTERVAL);
            db.insert(TABLE_NAME, null, cv);
        }
    }
}
