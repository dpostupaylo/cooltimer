package com.example.cooltimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SeekBar seekBar;
    private TextView textView;
    private boolean isTimerOn = false;
    private Button button;
    private CountDownTimer countDownTimer;
    private int defaultInterval;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.startBtn);

        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.textView4);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        seekBar.setMax(600);
        setIntervalFromSharedPreferences(sharedPreferences);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTimer(progress*1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void start(View view) {
        if (!isTimerOn) {
            button.setText("STOP");
            seekBar.setEnabled(false);

            countDownTimer = new CountDownTimer(seekBar.getProgress() * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    updateTimer(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    if (sharedPreferences.getBoolean("enable_sound", true)) {
                        String timerMelody = sharedPreferences.getString("timer_melody", "bell");

                        if (timerMelody.equals("bell")) {
                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),
                                    R.raw.bell_sound);
                            mediaPlayer.start();
                        } else if (timerMelody.equals("alarm")) {
                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),
                                    R.raw.alarm_siren_sound);
                            mediaPlayer.start();
                        } else if (timerMelody.equals("beep")) {
                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),
                                    R.raw.bip_sound);
                            mediaPlayer.start();
                        }
                    }

                    setDefaultParameters();
                }
            }.start();

            isTimerOn = true;
        } else {
            setDefaultParameters();
        }
    }

    private void setDefaultParameters(){countDownTimer.cancel();
        button.setText("START");
        seekBar.setEnabled(true);
        isTimerOn = false;
        setIntervalFromSharedPreferences(sharedPreferences);
    }

    private void updateTimer(long millisUntilFinish){
        int minutes = (int)millisUntilFinish/1000/60;
        int seconds = (int)millisUntilFinish/1000 - (minutes*60);

        String minutesStr = "";
        String secondsStr = "";

        if (minutes < 10)
            minutesStr = "0" + minutes;
        else
            minutesStr = Integer.toString(minutes);

        if (seconds < 10)
            secondsStr = "0" + seconds;
        else
            secondsStr = Integer.toString(seconds);

        textView.setText(minutesStr+":"+secondsStr);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings){
            Intent openSettings = new Intent(this, SettingsActivity.class);
            startActivity(openSettings);
            return true;
        } else {
            Intent openAbout = new Intent(this, AboutActivity.class);
            startActivity(openAbout);
            return true;
        }
    }

    private void setIntervalFromSharedPreferences(SharedPreferences sharedPreferences){
        defaultInterval = Integer.valueOf(sharedPreferences.getString("default_interval", "30"));
        updateTimer(defaultInterval * 1000);//так делать нельзя, чисто для примера
        seekBar.setProgress(defaultInterval);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("default_interval")){
            setIntervalFromSharedPreferences(sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
