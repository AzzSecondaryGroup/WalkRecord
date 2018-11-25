package jp.co.azz.maps.databases;

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

        List<HistoryDto> historyList = new ArrayList<>();

        try(
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(DatabaseContract.History.SELECT_SQL,null)
        ) {
            // 参照先を一番始めに設定
            boolean isEof = cursor.moveToFirst();
            while(isEof) {

                historyList.add(new HistoryDto(
                        cursor.getInt(cursor.getColumnIndex(DatabaseContract.History._ID)),
                        cursor.getString(cursor.getColumnIndex(DatabaseContract.History.COLUMN_START_DATE)),
                        cursor.getString(cursor.getColumnIndex(DatabaseContract.History.COLUMN_END_DATE)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseContract.History.COLUMN_NUMBER_OF_STEPS)),
                        cursor.getDouble(cursor.getColumnIndex(DatabaseContract.History.COLUMN_DISTANCE)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseContract.History.COLUMN_CALOLIE))
                ));

                isEof = cursor.moveToNext();
            }
            cursor.close();
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
        cv.put(DatabaseContract.History.COLUMN_START_DATE, start_date);
        cv.put(DatabaseContract.History.COLUMN_END_DATE, end_date);
        cv.put(DatabaseContract.History.COLUMN_NUMBER_OF_STEPS, number_of_steps);
        cv.put(DatabaseContract.History.COLUMN_DISTANCE, distance);
        cv.put(DatabaseContract.History.COLUMN_CALOLIE, calorie);

        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

        return db.insert(DatabaseContract.History.TABLE_NAME,null,cv);

    }

    public void updateHistory(long ID,
                              String end_date,
                              int number_of_steps,
                              double distance){
        //history(履歴テーブル)UPDATE文
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.History.COLUMN_END_DATE, end_date);
        cv.put(DatabaseContract.History.COLUMN_NUMBER_OF_STEPS, number_of_steps);
        cv.put(DatabaseContract.History.COLUMN_DISTANCE, distance);
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.update(DatabaseContract.History.TABLE_NAME, cv, DatabaseContract.History._ID + " = "+ ID , null);

    }

    public void deleteHistory(long ID){
        //history(履歴テーブル)DELETE文
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.delete(DatabaseContract.History.TABLE_NAME,  "id = "+ ID , null);

    }

    public void selectCoordinate(long ID){
        //history(履歴テーブル)SELECT文
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        db.rawQuery(DatabaseContract.Coordinate.SELECT_SQL,new String[] { String.valueOf(ID) });

    }

    public void insertCoordinate(long number_of_history,
                                 double coordinate_x,
                                 double coordinate_y){
        //history(履歴テーブル)INSERT文
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.Coordinate.COLUMN_NUMBER_OF_HISTORY, number_of_history);
        cv.put(DatabaseContract.Coordinate.COLUMN_COORDINATE_X, coordinate_x);
        cv.put(DatabaseContract.Coordinate.COLUMN_COORDINATE_Y, coordinate_y);
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.insert(DatabaseContract.Coordinate.TABLE_NAME,null,cv);

    }

    public void deleteCoordinate(long ID){
        //history(履歴テーブル)DELETE文
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.delete(DatabaseContract.Coordinate.TABLE_NAME,  "id = "+ ID , null);

    }
    public long selectCoordinateCount(){
        //history(履歴テーブル)SELECT文
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        long recodeCount = DatabaseUtils.queryNumEntries(db, DatabaseContract.Coordinate.TABLE_NAME);
        return recodeCount;
    }
}