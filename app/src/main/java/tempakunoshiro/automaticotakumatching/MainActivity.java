package tempakunoshiro.automaticotakumatching;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //user list
    private List<User> userList;
    //private List<Scream>
    private List<ImageView> ivlist;

    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Update();
    }

//    private void addList(){
//        userList.add(new User(1, "name", "taro", "jiro", "k1", "fuck"));
//    }

    public void Update(){
        //layout 取得
        RelativeLayout lay = (RelativeLayout) findViewById(R.id.main);
        //image生成
        ImageView img = new ImageView(this);
        img.setImageResource(android.R.drawable.sym_def_app_icon);

        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HirobaActivity.class);
                startActivity(intent);
            }
        };
        img.setOnClickListener(cl);
        //追加
        lay.addView(img);
        //表示
        setContentView(lay);
    }

    BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //userList = intent.getParcelableArrayListExtra("USER");
            Update();
        }
    };




}
