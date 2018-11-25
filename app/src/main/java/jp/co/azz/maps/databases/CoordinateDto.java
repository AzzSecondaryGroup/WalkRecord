package jp.co.azz.maps.databases;

import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;

public class CoordinateDto {

    private long id;
    private long numberOfHistory;
    private double x;
    private double y;


    /**
     *  コンストラクタ
     *  勝手に設定して欲しければ
     * @param coordinateCursor
     */
    public CoordinateDto(Cursor coordinateCursor) {
        this.id = coordinateCursor.getLong(coordinateCursor.getColumnIndex(DatabaseContract.Coordinate._ID));
        this.numberOfHistory = coordinateCursor.getLong(coordinateCursor.getColumnIndex(DatabaseContract.Coordinate.COLUMN_NUMBER_OF_HISTORY));
        this.x = coordinateCursor.getDouble(coordinateCursor.getColumnIndex(DatabaseContract.Coordinate.COLUMN_COORDINATE_X));
        this.y = coordinateCursor.getDouble(coordinateCursor.getColumnIndex(DatabaseContract.Coordinate.COLUMN_COORDINATE_Y));
    }

    public long getId() {
        return id;
    }

    public long getNumberOfHistory() {
        return numberOfHistory;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public LatLng latLng() {
        return new LatLng(this.x, this.y);
    }
}
