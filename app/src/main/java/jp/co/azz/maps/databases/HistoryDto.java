package jp.co.azz.maps.databases;

import android.database.Cursor;

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_DOWN;

public class HistoryDto {

    private long id;
    private String startDate;
    private String endDate;
    private int numberOfSteps;
    private double distance;
    private int calorie;

    /**
     * コンストラクタ
     * 値指定
     * @param id
     * @param startDate
     * @param endDate
     * @param numberOfSteps
     * @param distance
     * @param calorie
     */
    public HistoryDto(long id, String startDate, String endDate, int numberOfSteps, double distance, int calorie) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfSteps = numberOfSteps;
        this.distance = distance;
        this.calorie = calorie;
    }

    /**
     *  コンストラクタ2
     *  勝手に設定して欲しければ
     * @param historyCursor
     */
    public HistoryDto(Cursor historyCursor) {
        this.id = historyCursor.getLong(historyCursor.getColumnIndex(DatabaseContract.History._ID));
        this.startDate = historyCursor.getString(historyCursor.getColumnIndex(DatabaseContract.History.COLUMN_START_DATE));
        this.endDate = historyCursor.getString(historyCursor.getColumnIndex(DatabaseContract.History.COLUMN_END_DATE));
        this.numberOfSteps = historyCursor.getInt(historyCursor.getColumnIndex(DatabaseContract.History.COLUMN_NUMBER_OF_STEPS));
        this.distance = historyCursor.getDouble(historyCursor.getColumnIndex(DatabaseContract.History.COLUMN_DISTANCE));
        this.calorie = historyCursor.getInt(historyCursor.getColumnIndex(DatabaseContract.History.COLUMN_CALOLIE));
    }

    public long getId() {
        return id;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getNumberOfSteps() { return numberOfSteps + "歩"; }

    public String getKilometer() {
        if (this.distance > 0) {
            return String.format("%.2f"+" km", this.distance);
        }
        return "0㎞";
    }

    public String getCalorie() {
        return calorie +"kcal";
    }
}
