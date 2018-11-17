package jp.co.azz.maps;

public class HistoryDto {

    private int id;
    private String startDate;
    private String endDate;
    private int numberOfSteps;
    private double distance;
    private int calorie;

    public HistoryDto(int id, String startDate, String endDate, int numberOfSteps, double distance, int calorie) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfSteps = numberOfSteps;
        this.distance = distance;
        this.calorie = calorie;
    }

    public int getId() {
        return id;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public int getNumberOfSteps() {
        return numberOfSteps;
    }

    public double getDistance() {
        return distance;
    }

    public int getCalorie() {
        return calorie;
    }
}
