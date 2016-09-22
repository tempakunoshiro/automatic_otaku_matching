package tempakunoshiro.automaticotakumatching;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProfileListActivity extends AppCompatActivity {

    private LinearLayout profileList;
    private SwitcherReceiver receiver;

    private void delRecord(long id) {
        // TODO: implementation
    }

    private void addRecord(MyUser profile, LayoutInflater inflater) {
        LinearLayout record = (LinearLayout) inflater.inflate(R.layout.profile_list_record, null);

        // XXX: LinearLayout だと getChildAt で要素取得するしかないっぽい
        ImageView iconImage = (ImageView) record.getChildAt(0);
        LinearLayout infoLinear = (LinearLayout) record.getChildAt(1);
        TextView lastReceivedTimeText =
                (TextView) ((LinearLayout) infoLinear.getChildAt(0)).getChildAt(2);
        TextView nameText = (TextView) infoLinear.getChildAt(1);
        TextView tagsText = (TextView) infoLinear.getChildAt(2);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        lastReceivedTimeText.setText(sdf.format(profile.getModifiedTime()));
        nameText.setText(profile.getName());
        StringBuilder tagStrBldr = new StringBuilder();
        for(String tag : profile.getTagList()) {
            tagStrBldr.append(" / #" + tag);
        }
        tagsText.setText(tagStrBldr.toString().replaceFirst(" / ", ""));

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
        delRecord(profile.getId());
        profileList.addView(record, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);

        profileList = (LinearLayout) findViewById(R.id.profileList);

        LayoutInflater inflater = LayoutInflater.from(this);

        // データベース上のユーザ全てを表示
        List<MyUser> allUsers = MyUser.getAllMyUser(this);
        Collections.sort(allUsers, new Comparator<MyUser>() {
            public int compare(MyUser prf1, MyUser prf2) {
                return (int) (prf2.getModifiedTime() - prf1.getModifiedTime());
            }
        });
        for(MyUser profile : allUsers) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem goHirobaItem = menu.add(R.string.go_hiroba_text);
        goHirobaItem.setIcon(android.R.drawable.ic_menu_myplaces);
        goHirobaItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        goHirobaItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(ProfileListActivity.this, HirobaActivity.class);
                startActivity(intent);
                return false;
            }
        });

        MenuItem goScreamListItem = menu.add(R.string.go_scream_list_text);
        goScreamListItem.setIcon(android.R.drawable.ic_menu_agenda);
        goScreamListItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        //goScreamListItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        //    @Override
        //    public boolean onMenuItemClick(MenuItem item) {
        //        Intent intent = new Intent(ProfileListActivity.this, ScreamListActivity.class);
        //        startActivity(intent);
        //        return false;
        //    }
        //});

        MenuItem goProfileListItem = menu.add(R.string.go_profile_list_text);
        goProfileListItem.setIcon(android.R.drawable.ic_menu_my_calendar);
        goProfileListItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        goProfileListItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(ProfileListActivity.this, ProfileListActivity.class);
                startActivity(intent);
                return false;
            }
        });
        return true;
    }

    public class SwitcherReceiver extends BroadcastReceiver {
        private LayoutInflater inflater;

        public SwitcherReceiver(LayoutInflater inflater) {
            super();
            this.inflater = inflater;
        }
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action.equals(Switcher.ACTION_USER_RECEIVED)) {
                ArrayList<MyUser> userList = intent.getParcelableArrayListExtra("USER");
                Collections.sort(userList, new Comparator<MyUser>() {
                    public int compare(MyUser prf1, MyUser prf2) {
                        return (int) (prf2.getModifiedTime() - prf1.getModifiedTime());
                    }
                });
                for(MyUser profile : userList) {
                    addRecord(profile, inflater);
                }
            }
        }
    }
}
