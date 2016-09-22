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
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class HirobaActivity extends AppCompatActivity {

    ArrayList userList;
    ArrayList screamList;
    RelativeLayout lay;
    HashMap<Long, Point> userPos;
    ArrayList<Long> imgUserList;
    Point imgSize;
    Point actSize;
    SwicherReceiver receiver;
    BroadcastReceiver timerReceiver;

    final int rows = 8;
    final int cols = 12;
    Long[][] idTable;
    Long myId;

    private static final String ACTION_TIMER_RECEIVED = "tempakunoshiro.automaticotakumatching.ACTION_TIMER_RECEIVED";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();

        myId = pref.getLong("USER_ID", 0);


        userList = new ArrayList();
        screamList = new ArrayList();
        imgUserList = new ArrayList<Long>();
        userPos = new HashMap<Long, Point>();
        imgSize = new Point();
        idTable = new Long[rows][cols];

        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                idTable[i][j] = new Long(0);
            }
        }

        setContentView(R.layout.activity_hiroba);

        receiver = new SwicherReceiver();
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(Switcher.ACTION_USER_RECEIVED);
        iFilter.addAction(Switcher.ACTION_SCREAM_RECEIVED);
        iFilter.addAction(Switcher.ACTION_DATA_RECEIVED);

        registerReceiver(receiver, iFilter);

        timerReceiver = new TimerReceiver();
        IntentFilter timerFilter = new IntentFilter();
        timerFilter.addAction(ACTION_TIMER_RECEIVED);

        registerReceiver(timerReceiver, timerFilter);



        if(myId == 0){

            Long udid = new BigInteger(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), 16).longValue();
            long id = (((long) Math.ceil(Math.random()*Integer.MAX_VALUE)) << 32) + udid.hashCode();
            editor.putLong("USER_ID", id);
            editor.commit();

            myId = new Long(id);

            List<String> tags = new ArrayList<String>();

            String name = (String)getString(R.string.default_name);
            String twitter = (String)getString(R.string.default_twitter);
            String comment = (String)getString(R.string.default_comment);

            Switcher.sendData(this, new MyUser(id, name, null, twitter, comment, tags, 0));


            Intent intent = new Intent(HirobaActivity.this, ProfileActivity.class);
            Bundle bundle = new Bundle();
            System.out.println("myId:" + id);
            bundle.putLong("ID", id);
            intent.putExtras(bundle);

            userList = (ArrayList)MyUser.getAllMyUser(this);

            startActivity(intent);
        }

        userList = (ArrayList)MyUser.getAllMyUser(this);
        update();
    }


    public void update(){
        lay = (RelativeLayout)findViewById(R.id.hiroba);
        imgSize.set(lay.getWidth() / rows, lay.getHeight() / cols);
        lay.removeAllViewsInLayout();

        //画面サイズ取得
        setScreamButton();
        for(Object o: userList){
            setUser((MyUser)o);
        }

        for(Object o: screamList){
            showScream((MyScream)o);
        }
        //表示
        //setContentView(lay);
    }

    private void setScreamButton(){
        FloatingActionButton fabutton = new FloatingActionButton(this);
        RelativeLayout.LayoutParams lp =
                new  RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = "";
                ScreamSendDialog dialog = ScreamSendDialog.newInstance(value);
                dialog.show(getFragmentManager(), "dialog");
            }
        };

        fabutton.setOnClickListener(cl);

        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        lay.addView(fabutton, lp);
    }


    //名前表示と画像表示を分けたいが時間がないので放置
    private void setUser(MyUser u){

        //ユーザ画面表示
        ImageView img;
        TextView name;
        img = new ImageView(this);
        name = new TextView(this);

        RelativeLayout.LayoutParams tlp =
                new  RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                       ViewGroup.LayoutParams.WRAP_CONTENT
                );

        //アイコン画像セット
        img.setImageResource(android.R.drawable.sym_def_app_icon);
        name.setText(u.getName());

        //位置設定
        int x = 0, y =0;
        if(imgUserList.indexOf(u.getId()) == -1) {
            if (u.getId() == myId) {
                x = 3;
                y = 5;
            } else {
                Random rnd = new Random();
                //暫定処理　有限時間で終わらせます
                while (true) {
                    boolean posFlag = true;
                    x = rnd.nextInt(rows - 1);
                    y = rnd.nextInt(cols - 1);
                    for (Map.Entry<Long, Point> e : userPos.entrySet()) {
                        if (x == e.getValue().x && y == e.getValue().y) {
                            posFlag = posFlag && false;
                        }
                    }
                    if (posFlag) break;
                }
            }
            userPos.put(u.getId(), new Point(x, y));
            imgUserList.add(u.getId());
        }

        img.setTranslationX(userPosition(u.getId()).x);
        img.setTranslationY(userPosition(u.getId()).y);
        name.setTranslationX(userPosition(u.getId()).x);
        name.setTranslationY(userPosition(u.getId()).y + imgSize.y / 2);

        //クリック時Activity移動動作
        final long id = u.getId();
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HirobaActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("ID", id);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        };

        img.setOnClickListener(cl);
        name.setOnClickListener(cl);

        lay.addView(img);
        lay.addView(name, tlp);
    }

    private Point userPosition(long id){
        Point userPosition = new Point();
        userPosition.set(userPos.get(id).x * imgSize.x, userPos.get(id).y * imgSize.y);
        return userPosition;
    }


    public static class ScreamSendDialog extends DialogFragment
    {
        private EditText editText;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout content = (LinearLayout) inflater.inflate(R.layout.dialog_edit_text, null);
            editText = (EditText) content.findViewById(R.id.editText);
            builder.setView(content);

            builder.setTitle(R.string.send_scream_text);
            editText.setSingleLine(true);
            editText.setText(getArguments().getString("value"));

            builder.setPositiveButton("送信", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //SendSwicher
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences((HirobaActivity)getActivity());
                    long id = pref.getLong("USER_ID", 0);
                    Switcher.sendData((HirobaActivity)getActivity(), new MyScream(id, editText.getText().toString(), System.currentTimeMillis()));
                }
            });
            return builder.create();
        }

        public static ScreamSendDialog newInstance(String value){
            ScreamSendDialog dialog = new ScreamSendDialog();
            Bundle args = new Bundle();
            args.putString("value", value);
            dialog.setArguments(args);
            return dialog;
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        try {
            lay = (RelativeLayout)findViewById(R.id.hiroba);
            actSize = new Point();
            actSize.set(lay.getWidth(), lay.getHeight());
            update();
        }catch(Exception e){
        }
    }

    //showScream method
    private void showScream(MyScream scream){
        TextView screamView = new TextView(this);
        //発信後 DispTime　以内のscreamを表示
        long currentTime = System.currentTimeMillis();
        long dispTime = 5000;

        if(currentTime < scream.getTime() + dispTime){
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    Intent timerIntent = new Intent(ACTION_TIMER_RECEIVED);
                    sendBroadcast(timerIntent);
                }
            };

            timer.schedule(task, dispTime);
            RelativeLayout.LayoutParams lp =
                    new  RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
            screamView.setBackgroundResource(android.R.drawable.editbox_dropdown_light_frame);
            screamView.setTranslationX(userPosition(scream.getUserId()).x);
            screamView.setTranslationY(userPosition(scream.getUserId()).y);
            screamView.setText(  scream.getText());
            lay.addView(screamView, lp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem goHirobaItem = menu.add(R.string.go_hiroba_text);
        goHirobaItem.setIcon(android.R.drawable.ic_menu_myplaces);
        goHirobaItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        goHirobaItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(HirobaActivity.this, HirobaActivity.class);
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
        //        Intent intent = new Intent(HirobaActivity.this, ScreamListActivity.class);
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
                Intent intent = new Intent(HirobaActivity.this, ProfileListActivity.class);
                startActivity(intent);
                return false;
            }
        });
        return true;
    }

    private class SwicherReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action.equals(Switcher.ACTION_DATA_RECEIVED)) {
                userList = (ArrayList)MyUser.getAllMyUser((HirobaActivity)HirobaActivity.this);
                screamList = (ArrayList)MyScream.getAllMyScream((HirobaActivity)HirobaActivity.this);
            }
            if(action.equals(Switcher.ACTION_USER_RECEIVED)) {
                userList = intent.getParcelableArrayListExtra("USER");
            }
            if(action.equals(Switcher.ACTION_SCREAM_RECEIVED)){
                screamList = intent.getParcelableArrayListExtra("SCREAM");
            }
            update();
        }
    }

    private class TimerReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ACTION_TIMER_RECEIVED)){
                update();
            }
        }
    }
}
