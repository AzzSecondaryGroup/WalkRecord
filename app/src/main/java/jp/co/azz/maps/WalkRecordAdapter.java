package jp.co.azz.maps;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class WalkRecordAdapter extends CursorAdapter {

    public WalkRecordAdapter(Context context, Cursor c, int flag) {
        super(context, c, flag);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Cursorからデータを取り出します
        String start_time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_DATE));
        String end_time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_END_DATE));
        double distance = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DISTANCE));
        String step_cnt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NUMBER_OF_STEPS));
        String calorie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALOLIE));

        TextView tv_start_time = (TextView)view.findViewById(R.id.start_time);
        TextView tv_end_time = (TextView) view.findViewById(R.id.end_time);
        TextView tv_distance = (TextView) view.findViewById(R.id.distance);
        TextView tv_step_cnt = (TextView) view.findViewById(R.id.step_cnt);
        TextView tv_calorie = (TextView) view.findViewById(R.id.calorie);

        tv_start_time.setText(String.valueOf(start_time));
        tv_end_time.setText(end_time);
        tv_distance.setText(String.format("%.2f",distance/1000));
        tv_step_cnt.setText(step_cnt);
        tv_calorie.setText(calorie);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.walk_record_item, null);
        return view;
    }
}