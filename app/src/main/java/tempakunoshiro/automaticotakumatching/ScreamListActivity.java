package tempakunoshiro.automaticotakumatching;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class ScreamListActivity extends AppCompatActivity {

    private LinearLayout screamList;
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
            Picasso.with(this).load(MyIcon.OTAKU_URI).placeholder(R.drawable.yokootokob).into(iconImage);
        }else{
            File iconFile = new File(user.getIconUri().toString());
            Picasso.with(this).load(iconFile).placeholder(R.drawable.yokootokob).into(iconImage);
        }
        postedTimeText.setText(dateFormat.format(scream.getTime()));
        nameText.setText(user.getName());
        screamText.setText(scream.getText());

        // 許容レコード数を超えていれば
        // 最も古いレコードを削除する．
        if (MAX_SCREAMS < screamList.getChildCount()) {
            screamList.removeViewAt(MAX_SCREAMS);
        }

        screamList.addView(record, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scream_list);

        setTitle(R.string.scream_list_activity_name);

        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        screamList = (LinearLayout) findViewById(R.id.screamList);

        LayoutInflater inflater = LayoutInflater.from(this);

        Comparator comparator = new Comparator<MyScream> () {
            @Override
            public int compare(MyScream scr1, MyScream scr2) {
                return (int) (scr1.getTime() - scr2.getTime());
            }
        };

        // データベース上のscreamのうち新しいものからMAX_SCREAMS件を表示
        List<MyScream> allScreams = MyScream.getAllMyScream(this);
        Collections.sort(allScreams, comparator);
        for(MyScream scream : allScreams.subList(0, Math.min(MAX_SCREAMS - 1, allScreams.size()))) {
            addRecord(scream, inflater);
        }

        // 現在所持している最新スクリームの時間を取得
        long latestScreamTime;
        if (0 < allScreams.size()) {
            latestScreamTime = allScreams.get(allScreams.size() - 1).getTime();
        } else {
            latestScreamTime = currentTimeMillis();
        }

        // 新規に受信したscreamを追加
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener () {
            protected long latestScreamTime;
            protected Comparator comparator;
            protected LayoutInflater inflater;
            protected SwipeRefreshLayout swipeLayout;

            @Override
            public void onRefresh() {
                List<MyScream> screams = MyScream.getAllMyScreamWithinTime(ScreamListActivity.this, latestScreamTime + 1);
                Collections.sort(screams, comparator);
                for (MyScream scream : screams) {
                    addRecord(scream, inflater);
                }
                if (0 < screams.size()) {
                    latestScreamTime = screams.get(screams.size() - 1).getTime();
                }
                swipeLayout.setRefreshing(false);
            }

            public SwipeRefreshLayout.OnRefreshListener getInstance(long lastRefreshedTime,
                                                                    Comparator comparator,
                                                                    LayoutInflater inflater,
                                                                    SwipeRefreshLayout swipeLayout) {
                this.latestScreamTime = lastRefreshedTime;
                this.comparator = comparator;
                this.inflater = inflater;
                this.swipeLayout = swipeLayout;
                return this;
            }
        }.getInstance(latestScreamTime, comparator, inflater, swipeLayout));
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
                    Switcher.sendData(getActivity(), new MyScream(id, editText.getText().toString(), currentTimeMillis()));
                }
            });
            return builder.create();
        }
    }
}
