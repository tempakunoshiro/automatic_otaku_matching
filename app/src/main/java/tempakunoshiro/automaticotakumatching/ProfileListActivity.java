package tempakunoshiro.automaticotakumatching;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ProfileListActivity extends OtakuActivity {

    private LinearLayout profileList;
    private SwitcherReceiver receiver;
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
        iconImage.setImageBitmap(profile.getIcon());
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

        // データベース上のユーザ全てを表示
        List<MyUser> allUsers = MyUser.getAllMyUser(this);
        Collections.sort(allUsers, new UserModifiedTimeComparator());
        for(MyUser profile : allUsers.subList(0, Math.min(MAX_PROFILES - 1, allUsers.size() - 1))) {
            addRecord(profile, inflater);
        }

        // 新規に受信したユーザを追加
        receiver = new SwitcherReceiver(inflater);
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(Switcher.ACTION_USER_RECEIVED);
        registerReceiver(receiver, iFilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public class SwitcherReceiver extends BroadcastReceiver {
        private LayoutInflater inflater;
        private UserModifiedTimeComparator comparator;

        public SwitcherReceiver(LayoutInflater inflater) {
            super();
            this.inflater = inflater;
            this.comparator = new UserModifiedTimeComparator();
        }
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action.equals(Switcher.ACTION_USER_RECEIVED)) {
                ArrayList<MyUser> userList = new ArrayList<>();
                for(long userId : intent.getLongArrayExtra("USER")) {
                    userList.add(MyUser.getMyUserById(ProfileListActivity.this, userId));
                }
                Collections.sort(userList, comparator);
                for(MyUser profile : userList) {
                    addRecord(profile, inflater);
                }
            }
        }
    }

    class UserModifiedTimeComparator implements Comparator<MyUser> {
        @Override
        public int compare(MyUser prf1, MyUser prf2) {
            return (int) (prf1.getModifiedTime() - prf2.getModifiedTime());
        }
    }
}
