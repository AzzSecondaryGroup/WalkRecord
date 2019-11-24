package jp.co.azz.maps.Common;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.regex.Pattern;

public class GetLatestVersion extends AsyncTask<String, String, String> {
    String latestVersion;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            //It retrieves the latest version by scraping the content of current version from play store at runtime
            String urlOfAppFromPlayStore = "https://play.google.com/store/apps/details?id=jp.co.azz.maps";
            Document doc = Jsoup.connect(urlOfAppFromPlayStore).get();

            String val;
            Elements Version = doc.select(".htlgb");
            for (int i = 0; i < 10 ; i++) {
                val = Version.get(i).text();
                if (Pattern.matches("^[0-9]{1}.[0-9]{1}.[0-9]{1}$", val)) {
                    latestVersion = val;
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return latestVersion;
    }
}
