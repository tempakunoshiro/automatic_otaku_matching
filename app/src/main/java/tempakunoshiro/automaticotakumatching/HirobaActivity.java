package tempakunoshiro.automaticotakumatching;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class HirobaActivity extends AppCompatActivity {

    ArrayList userList;
    RelativeLayout lay;
    HashMap<Long, Point> userPos;
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
        dis = getWindowManager().getDefaultDisplay();
        for(Object o: userList){
            setUser((User)o);
        }
        //表示
        setContentView(lay);
    }


    private void setUser(User u){

        //ユーザ画面表示
        ImageView img;
        TextView name;
        img = new ImageView(this);
        name = new TextView(this);

        RelativeLayout.LayoutParams tlp =
                new  RelativeLayout.LayoutParams(
                        android.app.ActionBar.LayoutParams.WRAP_CONTENT,
                       ViewGroup.LayoutParams.WRAP_CONTENT
                );

        //アイコン画像セット
        img.setImageResource(android.R.drawable.sym_def_app_icon);
        name.setText(u.getName());

        //位置設定
        int x = 0, y =0;
        if(u.getId() == myId){
            x = 3;
            y = 5;
        }else{
            Random rnd = new Random();
            x = rnd.nextInt(rows - 1);
            y = rnd.nextInt(cols - 1);

        }

        x *= imgSize.x;
        y *= imgSize.y;
        userPos.put(u.getId(), new Point(x, y));
        img.setTranslationX(userPos.get(u.getId()).x);
        img.setTranslationY(userPos.get(u.getId()).y);
        name.setTranslationX(userPos.get(u.getId()).x);
        name.setTranslationY(userPos.get(u.getId()).y + imgSize.y / 2);

        //クリック時Activity移動動作
        final long id = u.getId();
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Update();
                //debug 本来はProfileへ移動
                Intent intent = new Intent(HirobaActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("ID", id);
                System.out.println("userid:" + id);
                intent.putExtras(bundle);
                //startActivity(intent);
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

    //debug Userのダミーデータ
    int n = 1;
    String namae = "name";
    private void addList(){
        //namae += "e";
        userList.add(new User(n, namae, "taro", "jiro", "k1", "fuck"));
        n++;
   }
}
