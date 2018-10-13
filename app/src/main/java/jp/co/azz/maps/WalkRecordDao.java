package jp.co.azz.maps;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.provider.ContactsContract;


public class WalkRecordDao extends SQLiteOpenHelper {
    private SQLiteOpenHelper dataBaseHelper;


    public WalkRecordDao(Context context) {
        super(context,DatabaseHelper.DBNAME,null,DatabaseHelper.DBVERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) {
    }

    public void selectHistory(){
        //history(履歴テーブル)SELECT文
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

        db.rawQuery(DatabaseHelper.SELECT_SQL_HISTORY,null);

    }

    public void insertHistory(String start_date,
                              String end_date,
                              int number_of_steps,
                              double distance,
                              int calorie){
        //history(履歴テーブル)INSERT文
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_START_DATE, start_date);
        cv.put(DatabaseHelper.COLUMN_END_DATE, end_date);
        cv.put(DatabaseHelper.COLUMN_NUMBER_OF_STEPS, number_of_steps);
        cv.put(DatabaseHelper.COLUMN_DISTANCE, distance);
        cv.put(DatabaseHelper.COLUMN_CALOLIE, calorie);

        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

        db.insert(DatabaseHelper.TABLE_HISTORY,null,cv);

    }

    public void updateHistory(int ID,
                              String end_date,
                              int number_of_steps,
                              double distance){
        //history(履歴テーブル)UPDATE文
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_END_DATE, end_date);
        cv.put(DatabaseHelper.COLUMN_NUMBER_OF_STEPS, number_of_steps);
        cv.put(DatabaseHelper.COLUMN_DISTANCE, distance);
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.update(DatabaseHelper.TABLE_HISTORY, cv, "id = "+ ID , null);

    }

    public void deleteHistory(int ID){
        //history(履歴テーブル)DELETE文
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_HISTORY,  "id = "+ ID , null);

    }

    public void selectCoordinate(int ID){
        //history(履歴テーブル)SELECT文
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        db.rawQuery(DatabaseHelper.SELECT_SQL_COORDINATE,new String[] { String.valueOf(ID) });

    }

    public void insertCoordinate(int ID,
                              double coordinate_x,
                              double coordinate_y){
        //history(履歴テーブル)INSERT文
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_ID_COORDINATE, ID);
        cv.put(DatabaseHelper.COLUMN_COORDINATE_X, coordinate_x);
        cv.put(DatabaseHelper.COLUMN_COORDINATE_Y, coordinate_y);
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.insert(DatabaseHelper.TABLE_COORDINATE,null,cv);

    }

    public void deleteCoordinate(int ID){
        //history(履歴テーブル)DELETE文
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_COORDINATE,  "id = "+ ID , null);

    }
}
