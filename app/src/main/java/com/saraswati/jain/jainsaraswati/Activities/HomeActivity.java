package com.saraswati.jain.jainsaraswati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.saraswati.jain.jainsaraswati.Fragments.BooksFragment;
import com.saraswati.jain.jainsaraswati.Fragments.MusicsFragment;
import com.saraswati.jain.jainsaraswati.Fragments.PachkanFragment;
import com.saraswati.jain.jainsaraswati.Fragments.PanchangFragment;
import com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper;
import com.saraswati.jain.jainsaraswati.Helpers.LocaleHelper;
import com.saraswati.jain.jainsaraswati.R;


public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (GlobalHelper.getSelectedPlace(this)==null){
            startActivity(new Intent(this,StartActivity.class));
            finish();
        }

        setContentView(R.layout.activity_home);


        bottomNavigationView = findViewById(R.id.bottomnavigationviewid);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.panchangmenuid:
                        transactFragment(new PanchangFragment());

                        return true;
                    case R.id.pachkanmenuid:

                        transactFragment(new PachkanFragment());

                        return true;
                    case R.id.musicmenuid:

                        transactFragment(new MusicsFragment());

                        return true;
                    case R.id.bookmenuid:

                        transactFragment(new BooksFragment());
                        return true;
                    default:
                        break;

                }

                return false;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.panchangmenuid);



    }

    void transactFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.homecontentid,fragment).commit();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }


}
