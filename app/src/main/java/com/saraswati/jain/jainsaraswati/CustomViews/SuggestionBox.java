package com.saraswati.jain.jainsaraswati.CustomViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.saraswati.jain.jainsaraswati.Apis.SuggestionApi;
import com.saraswati.jain.jainsaraswati.Helpers.GlobalHelper;
import com.saraswati.jain.jainsaraswati.Models.ResponseBody;
import com.saraswati.jain.jainsaraswati.Models.Suggestion;
import com.saraswati.jain.jainsaraswati.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SuggestionBox extends LinearLayout {

    private EditText suggestionEditText;
    private Button suggestionSendButton;
    private ProgressBar suggestionProgressBar;
    String type;

    public SuggestionBox(Context context, String type) {
        super(context);
        this.type = type;


        initView(context);
    }

    public SuggestionBox(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);


        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuggestionBox,0,0);

        try{
            type = a.getString(R.styleable.SuggestionBox_type);
        }finally{
            a.recycle();
        }

        initView(context);

    }

    private void initView(final Context context){
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.BOTTOM);

        LayoutInflater.from(context).inflate(R.layout.suggestionbox,this,true);

        suggestionEditText = findViewById(R.id.suggestionedittextid);
        suggestionProgressBar = findViewById(R.id.suggestionprogressbarid);
        suggestionSendButton = findViewById(R.id.suggestionsendbuttonid);


        suggestionSendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                suggestionSendButton.setEnabled(false);
                suggestionProgressBar.setVisibility(VISIBLE);
                sendSuggestion(context,suggestionEditText.getText().toString(),type);

            }
        });
    }

    private void setSuggestionType(String type){
        this.type = type;
    }


    private void sendSuggestion(final Context context , String text, String type){

        if(GlobalHelper.isNetworkAvailable(context)){
            Retrofit retrofit = GlobalHelper.getRetrofitInstance();

            SuggestionApi suggestionApi = retrofit.create(SuggestionApi.class);

            Call<ResponseBody> call = suggestionApi.sendSuggestion(text,type);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    suggestionSendButton.setEnabled(true);
                    suggestionProgressBar.setVisibility(GONE);
                    if(response.isSuccessful()){

                        suggestionEditText.setText(null);
                        Toast.makeText(context, context.getString(R.string.suggestion_sent), Toast.LENGTH_SHORT).show();

                    }else{
                        Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(context, context.getString(R.string.low_network_connection), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            Toast.makeText(context, context.getString(R.string.low_network_connection), Toast.LENGTH_SHORT).show();
        }

        suggestionSendButton.setEnabled(true);
    }
}
