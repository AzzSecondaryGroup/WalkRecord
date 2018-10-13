package jp.co.azz.maps;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DBNAME = "walkrecord.db";
    public static final int DBVERSION = 1;

    public static final String TABLE_HISTORY = "history";
    public static final String COLUMN_ID_HISTORY = "ID";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_NUMBER_OF_STEPS = "number_of_steps";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_CALOLIE = "calorie";

    //history(履歴テーブル)作成
    private static final String CREATE_TABLE_SQL_HISTORY =
            "create table " + TABLE_HISTORY  + " "
                    + "(" + COLUMN_ID_HISTORY +" INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_START_DATE + " TEXT NOT NULL,"
                    + COLUMN_END_DATE + " TEXT NOT NULL,"
                    + COLUMN_NUMBER_OF_STEPS + " INTEGER,"
                    + COLUMN_DISTANCE + " REAL,"
                    + COLUMN_CALOLIE + " INTEGER)";

    //history(履歴テーブル)SELECT文
    public static final String SELECT_SQL_HISTORY =
            "select " +" * "+" from " + TABLE_HISTORY
                    + " where " + COLUMN_ID_HISTORY +" =( "
                    + " select max( "+COLUMN_ID_HISTORY + " ) "
                    + COLUMN_ID_HISTORY + " from history)";

    //coordinate(座標テーブル)作成
    public static final String TABLE_COORDINATE = "coordinate";
    public static final String COLUMN_ID_COORDINATE = "ID";
    public static final String COLUMN_NUMBER_OF_HISTORY = "number_of_history";
    public static final String COLUMN_COORDINATE_X = "coordinate_x";
    public static final String COLUMN_COORDINATE_Y = "coordinate_y";
    private static final String CREATE_TABLE_SQL_COORDINATE =
            "create table " + TABLE_COORDINATE  + " "
                    + "(" + COLUMN_ID_COORDINATE +" INTEGER,"
                    + COLUMN_NUMBER_OF_HISTORY + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_COORDINATE_X + " REAL NOT NULL,"
                    + COLUMN_COORDINATE_Y + " REAL NOT NULL)";

    //coordinate(座標テーブル)SELECT文
    public static final String SELECT_SQL_COORDINATE =
            "select " +" * "+" from " + TABLE_COORDINATE
                    + " where " + COLUMN_ID_COORDINATE
                    +" = ? " ;


    public DatabaseHelper(Context context) {
        super(context,DBNAME,null,DBVERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //history(履歴テーブル)作成
        db.execSQL(CREATE_TABLE_SQL_HISTORY);
        //coordinate(座標テーブル)作成
        db.execSQL(CREATE_TABLE_SQL_COORDINATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) {
    }

}
