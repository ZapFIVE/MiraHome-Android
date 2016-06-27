package com.test.testdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.test.testdemo.waitingdots.DotsTextView;

/**
 * Created by Will on 16/6/23.
 */
public class TestActivity1 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_1);

        dotsTextView = (DotsTextView) findViewById(R.id.dots);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);
        buttonHide = (Button) findViewById(R.id.buttonHide);
        buttonHideAndStop = (Button) findViewById(R.id.buttonHideAndStop);

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dotsTextView.isPlaying()) {
                    dotsTextView.stop();
                } else {
                    dotsTextView.start();
                }
            }
        });

        buttonHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dotsTextView.isHide()) {
                    dotsTextView.show();
                } else {
                    dotsTextView.hide();
                }
            }
        });

        buttonHideAndStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dotsTextView.isHide()) {
                    dotsTextView.showAndPlay();
                } else {
                    dotsTextView.hideAndStop();
                }
            }
        });

        SpannableString spannable = new SpannableString("\u200B");
        Log.e("SpannableString", spannable.toString());

    }

    DotsTextView dotsTextView;
    Button buttonPlay;
    Button buttonHide;
    Button buttonHideAndStop;


}