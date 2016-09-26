package tempakunoshiro.automaticotakumatching;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

public class AboutMeActivity extends AppCompatActivity {
    private final static String ABOUT_HTML = "file:///android_asset/about.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        WebView web = (WebView) findViewById(R.id.webView);

        web.loadUrl(ABOUT_HTML);
    }
}
