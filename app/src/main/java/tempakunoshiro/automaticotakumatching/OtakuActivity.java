package tempakunoshiro.automaticotakumatching;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class OtakuActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem goHirobaItem = menu.add(R.string.go_hiroba_text);
        goHirobaItem.setIcon(android.R.drawable.ic_menu_myplaces);
        goHirobaItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        goHirobaItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(OtakuActivity.this, HirobaActivity.class);
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
        //        Intent intent = new Intent(OtakuActivity.this, ScreamListActivity.class);
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
                Intent intent = new Intent(OtakuActivity.this, ProfileListActivity.class);
                startActivity(intent);
                return false;
            }
        });
        return true;
    }
}
