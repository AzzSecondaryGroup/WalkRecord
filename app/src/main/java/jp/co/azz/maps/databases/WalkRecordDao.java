package jp.co.azz.maps.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

import jp.co.azz.maps.databases.DatabaseHelper;

/**
 * お散歩アプリのDBを扱うクラス
 */
public class WalkRecordDao extends SQLiteOpenHelper {
    private SQLiteOpenHelper dataBaseHelper;
    SQLiteDatabase db;


    public WalkRecordDao(Context context) {
        super(context, DatabaseHelper.DB_NAME,null,DatabaseHelper.DB_VERSION);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) {
    }

    public void selectHistory(){
        //history(履歴テーブル)SELECT文
        db.rawQuery(DatabaseContract.History.SELECT_SQL,null);

    }

    public void insertHistory(String start_date,
                              String end_date,
                              int number_of_steps,
                              double distance,
                              int calorie){
        //history(履歴テーブル)INSERT文
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.History.COLUMN_START_DATE, start_date);
        cv.put(DatabaseContract.History.COLUMN_END_DATE, end_date);
        cv.put(DatabaseContract.History.COLUMN_NUMBER_OF_STEPS, number_of_steps);
        cv.put(DatabaseContract.History.COLUMN_DISTANCE, distance);
        cv.put(DatabaseContract.History.COLUMN_CALOLIE, calorie);

        db.insert(DatabaseContract.History.TABLE_NAME,null,cv);

    }

    public void updateHistory(int ID,
                              String end_date,
                              int number_of_steps,
                              double distance){
        //history(履歴テーブル)UPDATE文
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.History.COLUMN_END_DATE, end_date);
        cv.put(DatabaseContract.History.COLUMN_NUMBER_OF_STEPS, number_of_steps);
        cv.put(DatabaseContract.History.COLUMN_DISTANCE, distance);
        db.update(DatabaseContract.History.TABLE_NAME, cv, "id = "+ ID , null);

    }

    public void deleteHistory(int ID){
        //history(履歴テーブル)DELETE文
        db.delete(DatabaseContract.History.TABLE_NAME,  DatabaseContract.History._ID +" = ?" , new String[]{""+ID});

    }

    public void selectCoordinate(int ID){
        //history(履歴テーブル)SELECT文
        db.rawQuery(DatabaseContract.Coordinate.SELECT_SQL,new String[] { String.valueOf(ID) });

    }

    public void insertCoordinate(int numberOfHistory,
                              double coordinate_x,
                              double coordinate_y){
        //history(履歴テーブル)INSERT文
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.Coordinate._ID, numberOfHistory);
        cv.put(DatabaseContract.Coordinate.COLUMN_COORDINATE_X, coordinate_x);
        cv.put(DatabaseContract.Coordinate.COLUMN_COORDINATE_Y, coordinate_y);
        db.insert(DatabaseContract.Coordinate.TABLE_NAME,null,cv);

    }

    public void deleteCoordinate(int ID){
        //history(履歴テーブル)DELETE文
        db.delete(DatabaseContract.Coordinate.TABLE_NAME,  DatabaseContract.Coordinate._ID+ " = ?" , new String[]{""+ID});

    }
}
