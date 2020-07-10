package com.saraswati.jain.jainsaraswati.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatRadioButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper;
import com.saraswati.jain.jainsaraswati.Helpers.LocaleHelper;
import com.saraswati.jain.jainsaraswati.Models.Place;
import com.saraswati.jain.jainsaraswati.R;

import java.util.List;

public class StartActivity extends AppCompatActivity {

    AppCompatAutoCompleteTextView appCompatAutoCompleteTextView;
    RadioGroup languageselectRadioGroup;
    Button startActivitySubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);



        appCompatAutoCompleteTextView = findViewById(R.id.placesearchautocompleteid);
        languageselectRadioGroup = findViewById(R.id.languageselectradiogroupid);
        startActivitySubmitButton = findViewById(R.id.startactivitysubmitbuttonid);

        if(GlobalHelper.getSelectedPlace(this)!=null){
            appCompatAutoCompleteTextView.setText(GlobalHelper.getSelectedPlace(this).getTitle());
        }


        List<Place> placeList = GlobalHelper.getPlacesList(this);
        String[] places = new String[placeList.size()];
        int i =0 ;
        for(Place place : GlobalHelper.getPlacesList(this)){
            places[i] = place.getTitle();
            i++;
        }

        appCompatAutoCompleteTextView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, places));


        startActivitySubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedCityName = appCompatAutoCompleteTextView.getText().toString();
                Place selectedPlace = GlobalHelper.getPlace(selectedCityName,StartActivity.this);
                AppCompatRadioButton selectedRadioButton = findViewById(languageselectRadioGroup.getCheckedRadioButtonId());
                if(selectedRadioButton!=null && selectedPlace!=null){

                    GlobalHelper.setSelectedPlace(StartActivity.this,selectedPlace);
                    LocaleHelper.setLocale(StartActivity.this,selectedRadioButton.getTag().toString());

                    startActivity(new Intent(StartActivity.this,HomeActivity.class));
                    finish();

                }else{
                    Toast.makeText(StartActivity.this, getString(R.string.select_all_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
