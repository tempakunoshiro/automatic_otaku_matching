package tempakunoshiro.automaticotakumatching;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {
    private ImageView iconImage;
    private TextView nameText;
    private TextView twitterText;
    private LinearLayout tagsList;
    private TextView commentText;

    // アクセサ
    private void setName(String name) {
        nameText.setText(name);
    }
    private String getName() {
        return nameText.getText().toString();
    }
    private void setTwitter(String twitter) {
        twitterText.setText("@" + twitter);
    }
    private String getTwitter() {
        return twitterText.getText().toString().substring(1);
    }
    private void setComment(String comment) {
        commentText.setText(comment);
    }
    private String getComment() {
        return commentText.getText().toString();
    }
    private void setTagsList(String[] tags) {
        for(String tag : tags) {
            TextView tagText = new TextView(this);
            tagText.setTextSize(20);
            tagText.setText("#" + tag);
            tagsList.addView(tagText);
        }
    }
    private String[] getTagsList() {
        int nTags = tagsList.getChildCount();
        String[] tags = new String[nTags];
        for(int i = 0; i < nTags; ++i) {
            TextView textView = (TextView) tagsList.getChildAt(i);
            tags[i] = textView.getText().toString().substring(1);
        }
        return tags;
    }

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
        setName("キモ=オタク");
        setTwitter("yantene");
        String[] tags = {"アニメ", "ゲーム", "電車", "名古屋鉄道", "名鉄"};
        setTagsList(tags);
        setComment("俺の名前はキモ=オタク。どこにでもいるただのオタクさ。" +
                   "ある日、お気に入りのウエストポーチをつけて街に出ると、" +
                   "ふと世のオタク共がリュックサックに乗り換えていることに気がついた。" +
                   "あんなにウエストポーチを愛した俺達が今更別のものに乗り換えるなんて信じられない。" +
                   "オタクとしての自覚に欠けると言わざるを得ないね。");

        // 自分のプロフィールなら各フィールドを編集可能に
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        long myId = pref.getLong("MY_ID", 0);
        if (id == myId) {
            nameText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String value = getName();
                    NameChangeDialog dialog = NameChangeDialog.newInstance(value);
                    dialog.show(getFragmentManager(), "dialog");
                }
            });
            twitterText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String value = getTwitter();
                    TwitterChangeDialog dialog = TwitterChangeDialog.newInstance(value);
                    dialog.show(getFragmentManager(), "dialog");
                }
            });
            commentText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String value = getComment();
                    CommentChangeDialog dialog = CommentChangeDialog.newInstance(value);
                    dialog.show(getFragmentManager(), "dialog");
                }
            });
        }
    }

    public void onTwitterButtonTapped(View view) {
        Uri uri = Uri.parse("https://twitter.com/" + getTwitter());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(i);
    }

    // XXX: I don't want to make these classes!!!
    //      But, But...!
    public static class NameChangeDialog extends DialogFragment {
        private EditText editText;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout content = (LinearLayout) inflater.inflate(R.layout.dialog_edit_text, null);
            editText = (EditText) content.findViewById(R.id.editText);
            builder.setView(content);

            builder.setTitle(R.string.edit_name_text);
            editText.setText(getArguments().getString("value"));

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ProfileActivity activity = (ProfileActivity) getActivity();
                    activity.setName(editText.getText().toString());
                }
            });
            return builder.create();
        }

        public static NameChangeDialog newInstance(String value){
            NameChangeDialog dialog = new NameChangeDialog();
            Bundle args = new Bundle();
            args.putString("value", value);
            dialog.setArguments(args);
            return dialog;
        }
    }
    public static class TwitterChangeDialog extends DialogFragment {
        private EditText editText;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout content = (LinearLayout) inflater.inflate(R.layout.dialog_edit_text, null);
            editText = (EditText) content.findViewById(R.id.editText);
            builder.setView(content);

            builder.setTitle(R.string.edit_name_text);
            editText.setText(getArguments().getString("value"));

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ProfileActivity activity = (ProfileActivity) getActivity();
                    activity.setTwitter(editText.getText().toString());
                }
            });
            return builder.create();
        }

        public static TwitterChangeDialog newInstance(String value){
            TwitterChangeDialog dialog = new TwitterChangeDialog();
            Bundle args = new Bundle();
            args.putString("value", value);
            dialog.setArguments(args);
            return dialog;
        }
    }
    public static class CommentChangeDialog extends DialogFragment {
        private EditText editText;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout content = (LinearLayout) inflater.inflate(R.layout.dialog_edit_text, null);
            editText = (EditText) content.findViewById(R.id.editText);
            builder.setView(content);

            builder.setTitle(R.string.edit_name_text);
            editText.setText(getArguments().getString("value"));

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ProfileActivity activity = (ProfileActivity) getActivity();
                    activity.setComment(editText.getText().toString());
                }
            });
            return builder.create();
        }

        public static CommentChangeDialog newInstance(String value){
            CommentChangeDialog dialog = new CommentChangeDialog();
            Bundle args = new Bundle();
            args.putString("value", value);
            dialog.setArguments(args);
            return dialog;
        }
    }
}
