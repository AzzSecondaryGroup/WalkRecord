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
public class WalkRecordAdapter extends ArrayAdapter<HistoryDto> {
    private static final String TAG = "WalkRecordAdapter";

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

        // convertViewは使いまわされている可能性があるのでnullの時だけ新しく作る
        if (null == rowView) rowView = mLayoutInflater.inflate(R.layout.walk_record_item, null);

        TextView startTime = (TextView)rowView.findViewById(R.id.start_time);
        TextView endTime = (TextView)rowView.findViewById(R.id.end_time);
        TextView distance = (TextView)rowView.findViewById(R.id.distance);
        TextView step = (TextView)rowView.findViewById(R.id.step_cnt);
        TextView calorie = (TextView)rowView.findViewById(R.id.calorie);
        TextView historyId = (TextView)rowView.findViewById(R.id.history_id);

        // 特定行(position)のデータを得る
        HistoryDto history = (HistoryDto)getItem(position);

        startTime.setText(String.valueOf(history.getStartDate()));
        endTime.setText(String.valueOf(history.getEndDate()));
        distance.setText(String.valueOf(history.getKilometer()));
        step.setText(String.valueOf(history.getNumberOfSteps()));
        calorie.setText(String.valueOf(history.getCalorie()));
        historyId.setText(String.valueOf(history.getId()));

        return rowView;
    }
}