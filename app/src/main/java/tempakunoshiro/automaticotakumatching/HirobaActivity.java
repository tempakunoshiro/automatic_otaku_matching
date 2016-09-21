package tempakunoshiro.automaticotakumatching;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

public class HirobaActivity extends AppCompatActivity {

    ArrayList userList;
    RelativeLayout lay;
    HashMap<Long, Point> userPos;
    ArrayList<Long> imgUserList;
    Display dis;
    Point imgSize;
    Point actSize;

    final int rows = 8;
    final int cols = 12;
    Long[][] idTable = new Long[rows][cols];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        //debug code
        for(int i = 0; i <= 10; i++)
            addList();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            long id = bundle.getLong("ID");
            System.out.println(id);
        }

        Update();
    }

    //debug本来はプリファレンス
    long myId = 1;

    public void Update(){
        lay = (RelativeLayout)findViewById(R.id.hiroba);
        //View初期化
        System.out.println("laywidth:" + lay.getWidth());
        System.out.println("laheight:" + lay.getHeight());
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
                    System.out.println("loop" + x + ", " + y);
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

                //addList();
                //Update();
                //debug 本来はProfileへ移動
                Intent intent = new Intent(HirobaActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("ID", id);
                System.out.println("userid:" + id);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        };

        img.setOnClickListener(cl);
        name.setOnClickListener(cl);

        lay.addView(img);
        lay.addView(name, tlp);
    }

    BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            userList = intent.getParcelableArrayListExtra("USER");
            Update();
        }
    };

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

                    //HirobaActivity activity = (HirobaActivity) getActivity();
                    //activity.setTwitter(editText.getText().toString());
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
            //ImageView tmp = (ImageView) findViewById(R.id.myImage);
            //imgSize = new Point();
            //imgSize.set(tmp.getWidth(), tmp.getHeight());
            actSize = new Point();
            actSize.set(lay.getWidth(), lay.getHeight());
            //System.out.println("sizex" + lay.getWidth() + "sizey" + lay.getHeight());
            Update();
        }catch(Exception e){
            //System.out.println("sizex" + imgSize.x + "sizey" + imgSize.y);
        }
    }

    //showScream method
    private void showScream(String scream, MyUser user){

    }

    //debug Userのダミーデータ
    int n = 1;
    String namae = "name";
    private void addList(){
        HashSet<String> tags = new HashSet<>();
        tags.add("new_tag1");
        tags.add("new_tag2");
        tags.add("new_tag3");
        userList.add(new MyUser(n, namae, null, "masason", "前進している", tags, 0));
        n++;
   }
}
