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
import android.view.Display;
import android.view.LayoutInflater;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class HirobaActivity extends AppCompatActivity {

    ArrayList userList;
    RelativeLayout lay;
    HashMap<Long, Point> userPos;
    ArrayList<Long> imgUserList;
    Display dis;
    Point imgSize;
    Point actSize;
    bReceiver receiver;

    final int rows = 8;
    final int cols = 12;
    Long[][] idTable = new Long[rows][cols];
    Long myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();

        myId = pref.getLong("USER_ID", 0);

        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                idTable[i][j] = new Long(0);
            }
        }
        userList = new ArrayList();
        imgUserList = new ArrayList<Long>();
        userPos = new HashMap<Long, Point>();
        imgSize = new Point();

        setContentView(R.layout.activity_hiroba);

        receiver = new bReceiver();
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(Switcher.ACTION_USER_RECEIVED);
        registerReceiver(receiver, iFilter);



        if(myId == 0){

            Long udid = new BigInteger(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), 16).longValue();
            long id = (((long) Math.ceil(Math.random()*Integer.MAX_VALUE)) << 32) + udid.hashCode();
            editor.putLong("USER_ID", id);
            editor.commit();

            myId = new Long(id);

            List<String> tags = new ArrayList<String>();

            String name = (String)getString(R.string.default_name);

            Switcher.sendData(this, new MyUser(id, name, null, "twitter", "コメントを入力してください.", tags, 0));


            Intent intent = new Intent(HirobaActivity.this, ProfileActivity.class);
            Bundle bundle = new Bundle();
            System.out.println("myId:" + id);
            bundle.putLong("ID", id);
            intent.putExtras(bundle);

            userList = (ArrayList)MyUser.getAllMyUser(this);

            startActivity(intent);
        }

        userList = (ArrayList)MyUser.getAllMyUser(this);
        Update();
    }


    public void Update(){
        lay = (RelativeLayout)findViewById(R.id.hiroba);
        imgSize.set(lay.getWidth() / rows, lay.getHeight() / cols);
        lay.removeAllViews();

        //画面サイズ取得
        setScreamButton();
        dis = getWindowManager().getDefaultDisplay();
        for(Object o: userList){
            setUser((MyUser)o);
        }
        //表示
        setContentView(lay);
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
                screamSendDialog dialog = screamSendDialog.newInstance(value);
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

        img.setTranslationX(userPos.get(u.getId()).x * imgSize.x);
        img.setTranslationY(userPos.get(u.getId()).y * imgSize.y);
        name.setTranslationX(userPos.get(u.getId()).x * imgSize.x);
        name.setTranslationY(userPos.get(u.getId()).y * imgSize.y + imgSize.y / 2);

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


    public static class screamSendDialog extends DialogFragment
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

                }
            });
            return builder.create();
        }

        public static screamSendDialog newInstance(String value){
            HirobaActivity.screamSendDialog dialog = new HirobaActivity.screamSendDialog();
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
            Update();
        }catch(Exception e){
        }
    }

    //showScream method
    private void showScream(String scream, MyUser user){

    }


    public class bReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action.equals(Switcher.ACTION_USER_RECEIVED)){
                userList = intent.getParcelableArrayListExtra("USER");
            }
            Update();
        }
    }
}
