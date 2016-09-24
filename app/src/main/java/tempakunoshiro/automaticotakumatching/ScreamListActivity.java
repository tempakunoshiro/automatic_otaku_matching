package tempakunoshiro.automaticotakumatching;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
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
        if(MyIcon.OTAKU_URI.equals(user.getIconUri())){
            Picasso.with(this).load(MyIcon.OTAKU_URI).placeholder(R.drawable.otaku_icon).into(iconImage);
        }else{
            File iconFile = new File(user.getIconUri().toString());
            Picasso.with(this).load(iconFile).placeholder(R.drawable.otaku_icon).into(iconImage);
        }
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
