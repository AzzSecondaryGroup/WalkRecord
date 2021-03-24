package jp.co.azz.maps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class UpdateReceiver extends BroadcastReceiver {

    public static Handler handler;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        int stepCont = bundle.getInt("stepCont");
        double totalDistance = bundle.getDouble("totalDistance");
        int burnedCalories = bundle.getInt("burnedCalories");
        double[] currentLocation = bundle.getDoubleArray("currentLocation");

        if (handler != null) {
            Message msg = new Message();

            Bundle data = new Bundle();
            data.putInt("stepCont", stepCont);
            data.putDouble("totalDistance", totalDistance);
            data.putInt("burnedCalories", burnedCalories);
            data.putDoubleArray("currentLocation", currentLocation);
            msg.setData(data);

            handler.sendMessage(msg);
        }
    }

    /**
     * メイン画面の表示を更新
     */
    public void registerHandler(Handler locationUpdateHandler) {
        handler = locationUpdateHandler;
    }
}
