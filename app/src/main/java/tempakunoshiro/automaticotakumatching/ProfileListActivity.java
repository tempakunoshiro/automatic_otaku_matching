package tempakunoshiro.automaticotakumatching;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileListActivity extends AppCompatActivity {

    private LinearLayout profileList;

    private void addRecord(long id, LayoutInflater inflater) {
        LinearLayout record = (LinearLayout) inflater.inflate(R.layout.profile_list_record, null);

        // XXX: LinearLayout だと getChildAt で要素取得するしかないっぽい
        ImageView iconImage = (ImageView) record.getChildAt(0);
        LinearLayout infoLinear = (LinearLayout) record.getChildAt(1);
        TextView lastReceivedTimeText =
                (TextView) ((LinearLayout) infoLinear.getChildAt(0)).getChildAt(2);
        TextView nameText = (TextView) infoLinear.getChildAt(1);
        TextView tagsText = (TextView) infoLinear.getChildAt(2);

        lastReceivedTimeText.setText("09/21 05:00");
        nameText.setText("おなまえ");
        tagsText.setText("#タグ / #タグ / #タグ / #タグ / #タグ");

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
        }.getInstance(id));
        profileList.addView(record);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);

        profileList = (LinearLayout) findViewById(R.id.profileList);



        LayoutInflater inflater = LayoutInflater.from(this);
        for(int i = 0; i < 100; ++i) {
            addRecord(0, inflater);
        }
    }
}
