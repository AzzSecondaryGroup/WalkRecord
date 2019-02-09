package jp.co.azz.maps.databases;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class CoordinateListDto {
    private final List<CoordinateDto> list = new ArrayList<>();

    public static CoordinateListDto create(Cursor cursor) {
        CoordinateListDto listDto = new CoordinateListDto();
        while(cursor.moveToNext()) {
            listDto.add(new CoordinateDto(cursor));
        }
        return listDto;
    }

    public List<CoordinateDto> getList() {
        return list;
    }

    /**
     * 要素を追加する（一つバージョン）
     * @see #add(List)
     * @param dto 追加したいCoordinateDto
     * @return 追加したList
     */
    public List<CoordinateDto> add(CoordinateDto dto) {
        this.list.add(dto);
        return list;
    }

    /**
     * 要素を追加する（一つバージョン）
     * @see #add(CoordinateDto)
     * @param dtos 追加したいDtoリスト
     * @return 追加したList
     */
    public List<CoordinateDto> add(List<CoordinateDto> dtos) {
        this.list.addAll(dtos);
        return list;
    }

    /**
     * 詳細描画時に画面中央に表示するべき座標データを特定する
     * @return 座標データ
     */
    @Nullable
    public CoordinateDto cameraPosition() {
        if (this.list.size() < 1) {
            /*
             FIXME:
             エラー処理にした方がよいかも。
             履歴があって座標が無いことがありえない
            */
            return null;
        }
        // 仮に先頭を返しておく
        return this.list.get(0);
    }

    /**
     * リストに中身があるかどうか
     * @return 有無
     */
    public boolean isEmpty() {
        return this.list.size() == 0;
    }

    public int size(){
        return this.list.size();
    }

    public List<LatLng> latLngs() {
        List<LatLng> latLngs = new ArrayList<>();
        for(CoordinateDto dto: this.list){
            latLngs.add(dto.latLng());
        }
        return latLngs;
    }
}
