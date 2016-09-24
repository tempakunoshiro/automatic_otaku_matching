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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HirobaActivity extends OtakuActivity {

    ArrayList userList;
    ArrayList screamList;
    SwitcherReceiver receiver;
    BroadcastReceiver timerReceiver;
    HashMap<Integer, Long> iconUserMap;
    ArrayList<Integer> screamTextId;
    Handler handler;
    Runnable sendTimerReceived;

    long dispTime;
    long myId;

    private static final String ACTION_TIMER_RECEIVED = "tempakunoshiro.automaticotakumatching.ACTION_TIMER_RECEIVED";
    private RelativeLayout hirobaLayout;

    WifiDirectManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hiroba);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        myId = pref.getLong("USER_ID", 0);

        hirobaLayout = (RelativeLayout)findViewById(R.id.hiroba);
        dispTime = 10000;
        userList = new ArrayList();
        screamList = new ArrayList();
        iconUserMap = new HashMap<Integer, Long>();
        screamTextId = new ArrayList<Integer>();
        handler = new Handler();
        sendTimerReceived = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ACTION_TIMER_RECEIVED);
                sendBroadcast(intent);
            }
        };

        receiver = new SwitcherReceiver();
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
            myId = id;

            List<String> tags = new ArrayList<String>();
            String name = (String)getString(R.string.default_name);
            String twitter = (String)getString(R.string.default_twitter);
            String comment = (String)getString(R.string.default_comment);
            Switcher.sendData(this, new MyUser(id, name, twitter, comment, tags, 0));

            Intent intent = new Intent(HirobaActivity.this, ProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putLong("ID", id);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        manager = new WifiDirectManager(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        screamTextId.add(R.id.comment0);
        screamTextId.add(R.id.comment1);
        screamTextId.add(R.id.comment2);
        screamTextId.add(R.id.comment3);
        screamTextId.add(R.id.comment4);
        screamTextId.add(R.id.comment5);
        screamTextId.add(R.id.comment6);
        screamTextId.add(R.id.comment7);
        screamTextId.add(R.id.comment8);
        screamTextId.add(R.id.comment9);
        screamTextId.add(R.id.comment10);
        screamTextId.add(R.id.comment11);
        screamTextId.add(R.id.comment12);

        //RandomDataSender.sendRandomData(this, 5000);

        userList = (ArrayList)MyUser.getAllMyUser(this);
        update();
    }

    private void update(){
        hideUser();
        for(Object o: userList){
            dispUser((MyUser)o);
        }
        for(Object o: screamList){
            showScream((MyScream)o);
        }
    }

    public void onScreamButtonTapped(View view) {
        ScreamSendDialog dialog = new ScreamSendDialog();
        dialog.show(getFragmentManager(), "dialog");
    }

    private void showScream(MyScream scream){
        for (int i = 0; i < getOtakuIconCount(); i++) {
            RelativeLayout iconLayout = getOtakuIconAt(i);
            if(iconUserMap.get(iconLayout.getId()) == null) continue;
            if(scream.getUserId() == iconUserMap.get(iconLayout.getId())) {
                TextView text = (TextView)findViewById(screamTextId.get(i));

                if(scream.getTime() + dispTime > System.currentTimeMillis()) {
                    handler.postDelayed(sendTimerReceived, dispTime);
                    text.setText(scream.getText());
                    text.setVisibility(View.VISIBLE);
                }else{
                    text.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void dispUser(MyUser user) {
        final long id = user.getId();
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

        if (user.getId() == myId) {
            RelativeLayout iconLayout = (RelativeLayout)findViewById(R.id.myIcon);
            ImageView icon = (ImageView) iconLayout.getChildAt(0);
            TextView text = (TextView) iconLayout.getChildAt(1);
            if(MyIcon.OTAKU_URI.equals(user.getIconUri())){
                Picasso.with(this).load(MyIcon.OTAKU_URI).placeholder(R.drawable.otaku_icon).into(icon);
            }else{
                File iconFile = new File(user.getIconUri().toString());
                Picasso.with(this).load(iconFile).placeholder(R.drawable.otaku_icon).into(icon);
            }
            text.setText(user.getName());
            icon.setOnClickListener(cl);
            text.setOnClickListener(cl);
            iconUserMap.put(iconLayout.getId(), user.getId());
        } else if(user.getModifiedTime() + dispTime >= System.currentTimeMillis()){
            if(iconUserMap.containsValue(user.getId())) return;
            for (int i = 0; i < getOtakuIconCount(); i++) {
                RelativeLayout iconLayout = getOtakuIconAt(i);
                if (iconUserMap.containsKey(iconLayout.getId())) continue;
                ImageView icon = (ImageView) iconLayout.getChildAt(0);
                TextView text = (TextView) iconLayout.getChildAt(1);
                if(MyIcon.OTAKU_URI.equals(user.getIconUri())){
                    Picasso.with(this).load(MyIcon.OTAKU_URI).placeholder(R.drawable.otaku_icon).into(icon);
                }else{
                    File iconFile = new File(user.getIconUri().toString());
                    Picasso.with(this).load(iconFile).placeholder(R.drawable.otaku_icon).into(icon);
                }
                text.setText(user.getName());
                icon.setOnClickListener(cl);
                text.setOnClickListener(cl);
                iconLayout.setVisibility(View.VISIBLE);
                iconUserMap.put(iconLayout.getId(), user.getId());
                break;
            }
        }
    }


    private void hideUser(){
        int n = getOtakuIconCount();
        for(int i = 0; i < getOtakuIconCount(); i++){
            RelativeLayout iconLayout = getOtakuIconAt(i);
            if(iconLayout.getId() != R.id.myIcon &&
                    (iconUserMap.get(iconLayout.getId()) == null ||
                        MyUser.getMyUserById(this, iconUserMap.get(iconLayout.getId())).getModifiedTime() + dispTime < System.currentTimeMillis() )) {
                ImageView icon = (ImageView) iconLayout.getChildAt(0);
                TextView text = (TextView) iconLayout.getChildAt(1);
                TextView scm = (TextView)findViewById(screamTextId.get(i));
                scm.setVisibility(View.INVISIBLE);
                iconLayout.setVisibility(View.INVISIBLE);
                iconUserMap.remove(iconLayout.getId());
            }
        }
    }

    private class SwitcherReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action.equals(Switcher.ACTION_DATA_RECEIVED)) {
                userList = (ArrayList)MyUser.getAllMyUser(HirobaActivity.this);
                screamList = (ArrayList)MyScream.getAllMyScream(HirobaActivity.this);
            }
            if(action.equals(Switcher.ACTION_USER_RECEIVED)) {
                userList = (ArrayList)MyUser.getAllMyUser(HirobaActivity.this);
            }
            if(action.equals(Switcher.ACTION_SCREAM_RECEIVED)){
                screamList = (ArrayList)MyScream.getAllMyScream(HirobaActivity.this);
            }
            update();
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();

        if(manager != null)manager.destroy();
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

    private class TimerReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ACTION_TIMER_RECEIVED)){
                update();
            }
        }
    }

    private RelativeLayout getOtakuIconAt(int index){
        return (RelativeLayout)hirobaLayout.getChildAt(index);
    }

    private int getOtakuIconCount(){
        return hirobaLayout.getChildCount() / 2;
    }
}
