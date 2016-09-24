package tempakunoshiro.automaticotakumatching;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScreamListActivity extends OtakuActivity {

    private LinearLayout profileList;
    private SwitcherReceiver receiver;
    private SimpleDateFormat dateFormat;
    private static final int MAX_SCREAMS = 100;

    private void addRecord(MyScream scream, LayoutInflater inflater) {
        LinearLayout record = (LinearLayout) inflater.inflate(R.layout.scream_list_record, null);

        // XXX: LinearLayout だと getChildAt で要素取得するしかないっぽい
        ImageView iconImage = (ImageView) record.getChildAt(0);
        LinearLayout infoLinear = (LinearLayout) record.getChildAt(1);
        TextView postedTimeText =
                (TextView) ((LinearLayout) infoLinear.getChildAt(0)).getChildAt(0);
        TextView nameText = (TextView) infoLinear.getChildAt(1);
        TextView screamText = (TextView) infoLinear.getChildAt(2);

        // レコードに叫び情報を格納
        MyUser user = MyUser.getMyUserById(this, scream.getUserId());
        iconImage.setImageBitmap(user.getIcon());
        postedTimeText.setText(dateFormat.format(scream.getTime()));
        nameText.setText(user.getName());
        screamText.setText(scream.getText());

        // 許容レコード数を超えていれば
        // 最も古いレコードを削除する．
        if (MAX_SCREAMS < profileList.getChildCount()) {
            profileList.removeViewAt(MAX_SCREAMS);
        }

        profileList.addView(record, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);

        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        profileList = (LinearLayout) findViewById(R.id.profileList);

        LayoutInflater inflater = LayoutInflater.from(this);

        // データベース上のscreamのうち新しいものからMAX_SCREAMS件を表示
        List<MyScream> allScreams = MyScream.getAllMyScream(this);
        Collections.sort(allScreams, new ScreamPostedTimeComparator());
        for(MyScream scream : allScreams.subList(0, Math.min(MAX_SCREAMS - 1, allScreams.size()))) {
            addRecord(scream, inflater);
        }

        // 新規に受信したscreamを追加
        receiver = new SwitcherReceiver(inflater);
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(Switcher.ACTION_SCREAM_RECEIVED);
        registerReceiver(receiver, iFilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public class SwitcherReceiver extends BroadcastReceiver {
        private LayoutInflater inflater;
        private ScreamPostedTimeComparator comparator;

        public SwitcherReceiver(LayoutInflater inflater) {
            super();
            this.inflater = inflater;
            this.comparator = new ScreamPostedTimeComparator();
        }
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action.equals(Switcher.ACTION_SCREAM_RECEIVED)) {
                ArrayList<MyScream> screamList = new ArrayList<>();
                for(long screamId : intent.getLongArrayExtra("SCREAM")) {
                    screamList.add(MyScream.getMyScreamById(ScreamListActivity.this, screamId));
                }
                Collections.sort(screamList, comparator);
                for(MyScream scream : screamList) {
                    addRecord(scream, inflater);
                }
            }
        }
    }

    class ScreamPostedTimeComparator implements Comparator<MyScream> {
        @Override
        public int compare(MyScream scr1, MyScream scr2) {
            return (int) (scr1.getTime() - scr2.getTime());
        }
    }
}
