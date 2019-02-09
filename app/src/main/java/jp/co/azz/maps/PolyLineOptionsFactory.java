package jp.co.azz.maps;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * PollyLineOptionsを作成するファクトリー
 */
public class PolyLineOptionsFactory {
    private PolyLineOptionsFactory(){};

    public static PolylineOptions create() {
        return new PolylineOptions()
                .width(10)
                .color(Color.RED)
                ;
    }

    public static PolylineOptions create(List<LatLng> latLngs) {
        if(latLngs.size() == 0) {
            return PolyLineOptionsFactory.create();
        }
        return PolyLineOptionsFactory.create()
                .addAll(latLngs);
    }


}
