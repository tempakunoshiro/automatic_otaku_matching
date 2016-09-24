package tempakunoshiro.automaticotakumatching;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class ProfileListActivity extends OtakuActivity {

    private LinearLayout profileList;
    private SimpleDateFormat dateFormat;
    private HashMap<Long, LinearLayout> liveRecords;
    private static final int MAX_PROFILES = 30;

    private void addRecord(MyUser profile, LayoutInflater inflater) {
        LinearLayout record = (LinearLayout) inflater.inflate(R.layout.profile_list_record, null);

        // XXX: LinearLayout だと getChildAt で要素取得するしかないっぽい
        ImageView iconImage = (ImageView) record.getChildAt(0);
        LinearLayout infoLinear = (LinearLayout) record.getChildAt(1);
        TextView modifiedTimeText =
                (TextView) ((LinearLayout) infoLinear.getChildAt(0)).getChildAt(2);
        TextView nameText = (TextView) infoLinear.getChildAt(1);
        TextView tagsText = (TextView) infoLinear.getChildAt(2);

        // レコードにユーザ情報を格納
        if(MyIcon.OTAKU_URI.equals(profile.getIconUri())){
            Picasso.with(this).load(MyIcon.OTAKU_URI).placeholder(R.drawable.otaku_icon).into(iconImage);
        }else{
            File iconFile = new File(profile.getIconUri().toString());
            Picasso.with(this).load(iconFile).placeholder(R.drawable.otaku_icon).into(iconImage);
        }
        modifiedTimeText.setText(dateFormat.format(profile.getModifiedTime()));
        nameText.setText(profile.getName());
        StringBuilder tagStrBldr = new StringBuilder();
        for(String tag : profile.getTagList()) {
            tagStrBldr.append(" / #" + tag);
        }
        tagsText.setText(tagStrBldr.toString().replaceFirst(" / ", ""));

        // クリックされたら ProfileActivity を起動
        record.setOnClickListener(new View.OnClickListener() {
            private long id;

            public View.OnClickListener getInstance(long id) {
                this.id = id;
                return this;
            }

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileListActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("ID", id);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }.getInstance(profile.getId()));

        // すでに表示されているプロフィールであれば
        // 既存の record を削除する．
        if (liveRecords.containsKey(profile.getId())) {
            profileList.removeView(liveRecords.get(profile.getId()));
            liveRecords.put(profile.getId(), record);
        }

        // 許容レコード数を超えていれば
        // 最も古いレコードを削除する．
        if (MAX_PROFILES < profileList.getChildCount()) {
            LinearLayout remRecord = (LinearLayout) profileList.getChildAt(MAX_PROFILES);
            liveRecords.remove(remRecord.getChildAt(1));
            profileList.removeViewAt(MAX_PROFILES);
        }

        profileList.addView(record, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);

        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        liveRecords = new HashMap<>();

        profileList = (LinearLayout) findViewById(R.id.profileList);

        LayoutInflater inflater = LayoutInflater.from(this);

        Comparator comparator = new Comparator<MyUser> () {
            @Override
            public int compare(MyUser prf1, MyUser prf2) {
                return (int) (prf1.getModifiedTime() - prf2.getModifiedTime());
            }
        };

        // データベース上のユーザ全てを表示
        List<MyUser> allUsers = MyUser.getAllMyUser(this);
        Collections.sort(allUsers, comparator);
        for(MyUser profile : allUsers.subList(0, Math.min(MAX_PROFILES - 1, allUsers.size()))) {
            addRecord(profile, inflater);
        }

        // 現在所持している最新プロフィールの時間を取得
        long latestProfileTime;
        if (0 < allUsers.size()) {
            latestProfileTime = allUsers.get(allUsers.size() - 1).getModifiedTime();
        } else {
            latestProfileTime = currentTimeMillis();
        }

        // 新規に受信したユーザを追加
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener () {
            protected long latestProfileTime;
            protected Comparator comparator;
            protected LayoutInflater inflater;
            protected SwipeRefreshLayout swipeLayout;

            @Override
            public void onRefresh() {
                List<MyUser> users = MyUser.getAllMyUserWithinTime(ProfileListActivity.this, latestProfileTime + 1);
                Collections.sort(users, comparator);
                for (MyUser user : users) {
                    addRecord(user, inflater);
                }
                if (0 < users.size()) {
                    latestProfileTime = users.get(users.size() - 1).getModifiedTime();
                }
                swipeLayout.setRefreshing(false);
            }

            public SwipeRefreshLayout.OnRefreshListener getInstance(long lastRefreshedTime,
                                                                    Comparator comparator,
                                                                    LayoutInflater inflater,
                                                                    SwipeRefreshLayout swipeLayout) {
                this.latestProfileTime = lastRefreshedTime;
                this.comparator = comparator;
                this.inflater = inflater;
                this.swipeLayout = swipeLayout;
                return this;
            }
        }.getInstance(latestProfileTime, comparator, inflater, swipeLayout));
    }

    public void onScreamButtonTapped(View view) {
        ScreamSendDialog dialog = new ScreamSendDialog();
        dialog.show(getFragmentManager(), "dialog");
    }

    public static class ScreamSendDialog extends DialogFragment {
        private EditText editText;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout content = (LinearLayout) inflater.inflate(R.layout.dialog_edit_text, null);
            editText = (EditText) content.findViewById(R.id.editText);
            builder.setView(content);

            builder.setTitle(R.string.send_scream_text);

            builder.setPositiveButton(R.string.send_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    long id = pref.getLong("USER_ID", 0);
                    Switcher.sendData(getActivity(), new MyScream(id, editText.getText().toString(), System.currentTimeMillis()));
                }
            });
            return builder.create();
        }
    }
}
