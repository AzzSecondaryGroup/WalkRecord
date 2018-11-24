package jp.co.azz.maps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.DatabaseUtils;

import java.util.ArrayList;
import java.util.List;


public class WalkRecordDao {
    private DatabaseHelper dataBaseHelper;

    public WalkRecordDao(Context context) {
        dataBaseHelper = new DatabaseHelper(context);
    }


    public List<HistoryDto> selectHistory() {
        //history(履歴テーブル)SELECT文
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

        List<HistoryDto> historyList = new ArrayList<>();

        try {

            Cursor cursor = db.rawQuery(DatabaseHelper.SELECT_SQL_HISTORY,null);

            // 参照先を一番始めに設定
            boolean isEof = cursor.moveToFirst();
            while(isEof) {

                historyList.add(new HistoryDto(
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID_HISTORY)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_START_DATE)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_END_DATE)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_NUMBER_OF_STEPS)),
                        cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_DISTANCE)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CALOLIE))
                ));

                isEof = cursor.moveToNext();
            }
            cursor.close();
        } finally {
            db.close();
        }
        return historyList;
    }

    /**
     * 履歴一覧Insert
     *
     * @param start_date
     * @param end_date
     * @param number_of_steps
     * @param distance
     * @param calorie
     * @return
     */
    public long insertHistory(String start_date,
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

        return db.insert(DatabaseHelper.TABLE_HISTORY,null,cv);

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
        db.update(DatabaseHelper.TABLE_HISTORY, cv, DatabaseHelper.COLUMN_ID_HISTORY + " = "+ ID , null);

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

    public void insertCoordinate(int number_of_history,
                              double coordinate_x,
                              double coordinate_y){
        //history(履歴テーブル)INSERT文
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NUMBER_OF_HISTORY, number_of_history);
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
    public long selectCoordinateCount(){
        //history(履歴テーブル)SELECT文
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        long recodeCount = DatabaseUtils.queryNumEntries(db, DatabaseHelper.TABLE_COORDINATE);
        return recodeCount;
    }
}
