package tempakunoshiro.automaticotakumatching;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {
    private ImageView iconImage;
    private TextView nameText;
    private TextView twitterText;
    private LinearLayout tagsList;
    private TextView commentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 各Viewの取り出し
        iconImage = (ImageView) findViewById(R.id.iconImage);
        nameText = (TextView) findViewById(R.id.nameText);
        twitterText = (TextView) findViewById(R.id.twitterText);
        tagsList = (LinearLayout) findViewById(R.id.tagsList);
        commentText = (TextView) findViewById(R.id.commentText);

        // 表示するプロフィールのオタクIDを取得
        Intent intent = getIntent();
        long id = intent.getLongExtra("ID", 0);

        // データベースからデータを取得し各Viewにセット
        //iconImage.setImageDrawable();
        nameText.setText("キモ=オタク");
        twitterText.setText("@yantene");
        String[] tags = {"アニメ", "ゲーム", "電車", "名古屋鉄道", "名鉄"};
        for(String tag : tags) {
            TextView tagText = new TextView(this);
            tagText.setTextSize(20);
            tagText.setText("#" + tag);
            tagsList.addView(tagText);
        }

        commentText.setText("俺の名前はキモ=オタク。どこにでもいるただのオタクさ。" +
                            "ある日、お気に入りのウエストポーチをつけて街に出ると、" +
                            "ふと世のオタク共がリュックサックに乗り換えていることに気がついた。" +
                            "あんなにウエストポーチを愛した俺達が今更別のものに乗り換えるなんて信じられない。" +
                            "オタクとしての自覚に欠けると言わざるを得ないね。");

        // 自分のプロフィールなら各フィールドを編集可能に
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        long myId = pref.getLong("MY_ID", 0);
        if (id == myId) {

        }
    }

    public void onTwitterButtonTapped(View view) {
        Uri uri = Uri.parse("https://twitter.com/"
                + twitterText.getText().subSequence(1, twitterText.length()));
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(i);
    }
}
