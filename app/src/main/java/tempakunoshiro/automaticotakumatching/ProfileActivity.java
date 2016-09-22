package tempakunoshiro.automaticotakumatching;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private MyUser profile;
    private boolean editableFlg = false;
    private ImageView iconImage;
    private Bitmap iconBitmap;
    private TextView nameText;
    private TextView twitterText;
    private LinearLayout tagList;
    private TextView commentText;
    private LinearLayout addTagButton;
    private final static int CHOOSE_IMAGE_CODE = 10000;

    // アクセサ
    private void setIcon(Bitmap bitmap) {
        iconBitmap = bitmap;
        iconImage.setImageBitmap(bitmap);
    }
    private Bitmap getIcon() {
        return iconBitmap;
    }
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
    private void addTag(String tag) {
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_VERTICAL;
        line.setLayoutParams(lp);

        // タグを作成
        TextView tagText = new TextView(this);
        tagText.setTextSize(20);
        tagText.setText("#" + tag);
        line.addView(tagText);

        // 削除ボタンを作成
        if (editableFlg) {
            ImageView delButton = new ImageView(this);
            delButton.setImageResource(android.R.drawable.ic_delete);
            delButton.setMaxHeight(20);
            delButton.setMaxWidth(20);
            delButton.setOnClickListener(new View.OnClickListener() {
                private LinearLayout line;
                public View.OnClickListener getInstance(LinearLayout line) {
                    this.line = line;
                    return this;
                }
                @Override
                public void onClick(View view) {
                    tagList.removeView(line);
                }
            }.getInstance(line));
            line.addView(delButton);
        }

        tagList.addView(line);
    }
    private void setTagList(List<String> tags) {
        for(String tag : tags) {
            addTag(tag);
        }
    }
    private List<String> getTagList() {
        List<String> tagArrayList = new ArrayList<>();
        for(int i = 0; i < tagList.getChildCount(); ++i){
            LinearLayout line = (LinearLayout) tagList.getChildAt(i);
            TextView tagText = (TextView) line.getChildAt(0);
            tagArrayList.add(tagText.getText().toString().substring(1));
        }
        return tagArrayList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1089);

        // 表示するプロフィールのオタクIDを取得
        Intent intent = getIntent();
        long id = intent.getLongExtra("ID", 0);

        // 自分のプロフィールであれば編集可能フラグを立てる
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        long myId = pref.getLong("USER_ID", 0);
        editableFlg = (id == myId);

        // 各Viewの取り出し
        iconImage = (ImageView) findViewById(R.id.iconImage);
        nameText = (TextView) findViewById(R.id.nameText);
        twitterText = (TextView) findViewById(R.id.twitterText);
        tagList = (LinearLayout) findViewById(R.id.tagList);
        commentText = (TextView) findViewById(R.id.commentText);
        addTagButton = (LinearLayout) findViewById(R.id.addTagButton);

        // データベースからデータを取得し各Viewにセット
        profile = MyUser.getMyUserById(this, id);
        Bitmap iconBitmap = profile.getIcon();
        if (iconBitmap != null) {
            setIcon(profile.getIcon());
        } else {
            setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.otaku_icon));
        }
        setName(profile.getName());
        setTwitter(profile.getTwitterId());
        setComment(profile.getComment());
        setTagList(profile.getTagList());

        // 自分のプロフィールなら各フィールドを編集可能に
        if (editableFlg) {
            iconImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, CHOOSE_IMAGE_CODE);
                }
            });
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
            addTagButton.setVisibility(View.VISIBLE);
            addTagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String newTagText = getString(R.string.new_tag_text);
                    AddTagDialog dialog = AddTagDialog.newInstance(newTagText);
                    dialog.show(getFragmentManager(), "dialog");
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_IMAGE_CODE && resultCode == RESULT_OK) {
            try {
                Uri uri = data.getData();
                ParcelFileDescriptor parcelFileDescriptor =
                        getContentResolver().openFileDescriptor(uri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                iconImage.setImageBitmap(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause start!");
        if(!getIcon().equals(profile.getIcon()) ||
           !getName().equals(profile.getName()) ||
           !getTwitter().equals(profile.getTwitterId()) ||
           !getComment().equals(profile.getComment()) ||
           !getTagList().equals(profile.getTagList())) {
            MyUser user = currentProfile();
            System.out.println("書き込み開始");
            Switcher.sendData(this, user);
            System.out.println("書き込み終了");
        }
        System.out.println("ouPause end!");
    }

    private MyUser currentProfile() {
        return new MyUser(
                profile.getId(),
                getName(),
                getIcon(),
                getTwitter(),
                getComment(),
                getTagList(),
                System.currentTimeMillis()
        );
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
            editText.setSingleLine(true);
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

            builder.setTitle(R.string.edit_twitter_text);
            editText.setSingleLine(true);
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

            builder.setTitle(R.string.edit_comment_text);
            editText.setSingleLine(false);
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
    public static class AddTagDialog extends DialogFragment {
        private EditText editText;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout content = (LinearLayout) inflater.inflate(R.layout.dialog_edit_text, null);
            editText = (EditText) content.findViewById(R.id.editText);
            builder.setView(content);
            builder.setTitle(R.string.add_tag_text);
            editText.setSingleLine(true);
            editText.setText(getArguments().getString("value"));

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ProfileActivity activity = (ProfileActivity) getActivity();
                    activity.addTag(editText.getText().toString());
                }
            });
            return builder.create();
        }

        public static AddTagDialog newInstance(String value){
            AddTagDialog dialog = new AddTagDialog();
            Bundle args = new Bundle();
            args.putString("value", value);
            dialog.setArguments(args);
            return dialog;
        }
    }
}
