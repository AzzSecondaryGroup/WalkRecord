package jp.co.azz.maps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * WiFiをオフにするか確認するのダイアログ
 */
public class WifiConfirmDialogFragment extends DialogFragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";

    private int mTitle;
    private int mMessage;

    /**
     * ダイアログfragment情報を返却
     * @param title
     * @param message
     * @return
     */
    public static WifiConfirmDialogFragment newInstance(int title, int message) {
        WifiConfirmDialogFragment fragment = new WifiConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE, title);
        args.putInt(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * ダイアログfragmentがshowされた時の処理
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            mTitle = getArguments().getInt(ARG_TITLE);
            mMessage = getArguments().getInt(ARG_MESSAGE);
        }
        // ダイアログを表示
        return new AlertDialog.Builder(getActivity())
            .setTitle(mTitle)
            .setMessage(mMessage)
            // NO押下時の処理
            .setNegativeButton(R.string.alert_dialog_no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // do nothing
                    }
                }
            )
            // YES押下時の処理
            .setPositiveButton(R.string.alert_dialog_yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // wifi OFFの処理を実行
                        ((MainActivity)getActivity()).wifiOff();
                    }
                }
            )
            .create();
    }
}
