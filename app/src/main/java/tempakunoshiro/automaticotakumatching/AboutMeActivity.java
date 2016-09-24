package tempakunoshiro.automaticotakumatching;

import android.os.Bundle;
import android.webkit.WebView;

public class AboutMeActivity extends OtakuActivity {
    private final static String ABOUT_HTML = "file:///android_asset/about.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        WebView web = (WebView) findViewById(R.id.webView);

        web.loadUrl(ABOUT_HTML);
    }
}
