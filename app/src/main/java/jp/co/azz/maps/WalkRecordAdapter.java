package jp.co.azz.maps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import jp.co.azz.maps.databases.HistoryDto;

/**
 * Cursorから各フィールドの値を取得し、ListViewの各行に編集する
 */
//public class WalkRecordAdapter extends CursorAdapter {
public class WalkRecordAdapter extends ArrayAdapter<HistoryDto> {
    private static final String TAG = "WalkRecordAdapter";
//    public WalkRecordAdapter(Context context, Cursor c, int flag) {
//        super(context, c, flag);
//    }
//
//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//        // Cursorからデータを取り出します
//        String start_time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_DATE));
//        String end_time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_END_DATE));
//        double distance = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DISTANCE));
//        String step_cnt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NUMBER_OF_STEPS));
//        String calorie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALOLIE));
//
//        TextView tv_start_time = (TextView)view.findViewById(R.id.start_time);
//        TextView tv_end_time = (TextView) view.findViewById(R.id.end_time);
//        TextView tv_distance = (TextView) view.findViewById(R.id.distance);
//        TextView tv_step_cnt = (TextView) view.findViewById(R.id.step_cnt);
//        TextView tv_calorie = (TextView) view.findViewById(R.id.calorie);
//
//        tv_start_time.setText(String.valueOf(start_time));
//        tv_end_time.setText(end_time);
//        tv_distance.setText(String.format("%.2f",distance/1000));
//        tv_step_cnt.setText(step_cnt);
//        tv_calorie.setText(calorie);
//    }
//    @Override
//    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View view = inflater.inflate(R.layout.walk_record_item, null);
//        return view;
//    }


    // 手動でのデータ入出力
    //-----------------------------------------------------------------
    // レイアウトxmlファイルからIDを指定してViewが使用可能
    private LayoutInflater mLayoutInflater;


    public WalkRecordAdapter(Context context, int resourceId, List<HistoryDto> objects) {
        super(context, resourceId, objects);
        // getLayoutInflater()メソッドはActivityじゃないと使えない
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // getView()メソッドは各行を表示しようとした時に呼ばれる
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 各行のデータ
        View rowView = convertView;

        // 特定行(position)のデータを得る
        HistoryDto item = (HistoryDto)getItem(position);
        // convertViewは使いまわされている可能性があるのでnullの時だけ新しく作る
        if (null == rowView) rowView = mLayoutInflater.inflate(R.layout.walk_record_item, null);

        TextView startTime = (TextView)rowView.findViewById(R.id.start_time);
        TextView endTime = (TextView)rowView.findViewById(R.id.end_time);
        TextView distance = (TextView)rowView.findViewById(R.id.distance);
        TextView step = (TextView)rowView.findViewById(R.id.step_cnt);
        TextView calorie = (TextView)rowView.findViewById(R.id.calorie);

        // 特定行(position)のデータを得る
        HistoryDto history = (HistoryDto)getItem(position);

        startTime.setText(String.valueOf(history.getStartDate()));
        endTime.setText(String.valueOf(history.getEndDate()));
        distance.setText(String.valueOf(history.getDistance()));
        step.setText(String.valueOf(history.getNumberOfSteps()));
        calorie.setText(String.valueOf(history.getCalorie()));

        return rowView;
    }
}