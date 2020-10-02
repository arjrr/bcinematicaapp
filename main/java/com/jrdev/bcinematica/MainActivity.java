package com.jrdev.bcinematica;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;


import android.Manifest;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private View ball;
    private static FlingAnimation flingY;
    private TextView timerTextViewSec, textViewPosition;
    private Timer timer;
    private int min, seconds, decseconds = 0;
    private String positions = "", positions_sec = "";
    private String positions_log = "", positions_sec_log = "";
    private int lastposition = 0;
    private int dpi = 0;
    private float friction = 0.000001f;
    private float position_ball_start = 0.0f;
    private RadioGroup radioGroup;
    private TextView textViewDesaceleracao;
    private int height;
    private View mainView;
    private boolean pausecontrol = true;
    private Button pauseButton;


    private String pattern = "##.##";
    private DecimalFormat decimalFormat = new DecimalFormat(pattern);


    private ArrayList<String> arrayList_decSc = new ArrayList<>();
    private ArrayList<String> arrayList_sec = new ArrayList<>();


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainView = findViewById(R.id.main_activity);
        ball = findViewById(R.id.imageView);
        radioGroup = findViewById(R.id.radioGroupAceleration);
        textViewDesaceleracao = findViewById(R.id.textViewDesaceleracao);
        pauseButton = findViewById(R.id.buttonPause);

        timerTextViewSec = findViewById(R.id.textViewSeconds);
        textViewPosition = findViewById(R.id.textViewPosition);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        //Toast.makeText(this, String.valueOf(height), Toast.LENGTH_LONG).show();

        float density_screen = getResources().getDisplayMetrics().density;
        dpi = (int) density_screen * 160;
        //Toast.makeText(this, String.valueOf(dpi), Toast.LENGTH_SHORT).show();

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // You can use the API that requires the permission.


    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radioButton1:
                if (checked)
                    friction = 0.000001f;
                break;
            case R.id.radioButton2:
                if (checked)
                    friction = 0.16f;
                break;
            case R.id.radioButton3:
                if (checked)
                    friction = 0.38f;
                break;
            case R.id.radioButton4:
                if (checked)
                    friction = 0.6f;
                break;
        }
    }

    public void onRadioButtonClickedTipo(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radioButtonMRU:
                if (checked) {
                    radioGroup.setVisibility(View.INVISIBLE);
                    textViewDesaceleracao.setVisibility(View.INVISIBLE);
                    friction = 0.000001f;
                    //radioGroup.clearCheck();
                    radioGroup.check(R.id.radioButton1);
                }
                break;
            case R.id.radioButtonMRUV:
                if (checked) {
                    radioGroup.setVisibility(View.VISIBLE);
                    textViewDesaceleracao.setVisibility(View.VISIBLE);

                }
                break;
        }
    }

    public void screenShot(View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Screenshot screenshot = new Screenshot();
                Bitmap bitmap = screenshot.takescreenshot(mainView);
                try {
                    screenshot.saveScreenShot(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        Toast.makeText(MainActivity.this, "Captura de tela realizada com sucesso! Confira sua pasta de Downloads", Toast.LENGTH_SHORT).show();
    }

    public void pausarMovimento(View view) {
        if (pausecontrol) {
            flingY.cancel();
            pausecontrol = false;

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Atenção");
            builder.setMessage("Você deve voltar a bola para iniciar novamente");
            builder.setPositiveButton("Ok", null);
            builder.show();
        }
    }


    void startTime() {
        timer = new Timer();
        timer.schedule(new RemindTask(), 0, 100);
    }

        /*
       dpi is the pixel density or dots per inch.
       96 dpi means there are 96 pixels per inch.
       1 inch is equal to 2.54 centimeters.

       1 inch = 2.54 cm
       dpi = 96 px / in
       96 px / 2.54 cm

       Therefore one pixel is equal to
       1 px = 2.54 cm / 96
       1 px = 0.026458333 cm
        */

    class RemindTask extends TimerTask {
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (decseconds < 9) {
                        String s;
                        if (seconds < 10) {
                            s = min + ":0" + seconds + "," + ++decseconds;
                        } else {
                            s = min + ":" + seconds + "," + ++decseconds;
                        }

                        timerTextViewSec.setText(s);

                        int[] location = new int[2];
                        ball.getLocationOnScreen(location);
                        int y = location[1];
                        double y_string = (((height - location[1]) * 2.54) / dpi) * 10;

                        if (y != lastposition) { //y != lastposition
                            //positions_log = positions_log + y_string + "\n";
                            positions_log = positions_log + decimalFormat.format(y_string) + "\n";
                            positions = decimalFormat.format(y_string) + "\n";
                            textViewPosition.setText(positions);
                            Log.i("POSITION_", positions_log);
                            arrayList_decSc.add(String.valueOf(y));
                        } else {
                            timer.cancel();
                            flingY.setStartVelocity(0);
                            Log.i("POSI_DS", arrayList_decSc.toString());
                        }

                        if (y <= 180) {
                            flingY.setStartVelocity(0);
                            timer.cancel();
                            //flingY.cancel();
                        }

                        lastposition = y;

                    } else if (seconds < 59) {

                        int[] location = new int[2];
                        ball.getLocationOnScreen(location);
                        int y = location[1];
                        double y_string = (((height - location[1]) * 2.54) / dpi) * 10;

                        if (y != lastposition) {
                            positions_sec_log = positions_sec_log + y_string + "\n";
                            positions = positions + decimalFormat.format(y_string) + "\n";
                            Log.i("POSITION_SEC", positions);
                            arrayList_sec.add(String.valueOf(y));
                        } else {
                            timer.cancel();
                            Log.i("POSI_DS", arrayList_sec.toString());
                        }

                        String s;

                        if (seconds < 10) {
                            s = min + ":0" + ++seconds + "," + 0;
                        } else {
                            s = min + ":" + ++seconds + "," + 0;
                        }


                        timerTextViewSec.setText(s);
                        decseconds = 0;

                    } else {
                        min++;
                        String s = min + ":" + 0 + "," + 0;
                        timerTextViewSec.setText(s);
                        seconds = 0;
                        decseconds = 0;
                    }

                }
            });
        }
    }

    public void startAnimationMRUV(View view) {
        if (seconds == 0 && pausecontrol) {
            pauseButton.setClickable(true);
            pauseButton.setEnabled(true);
            position_ball_start = ball.getY();
            float velocity = dpi / 2.54f; //1 cm/s
            startTime();
            flingY = new FlingAnimation(ball, DynamicAnimation.TRANSLATION_Y);
            flingY.setStartVelocity(velocity * -4) //40 mm/s
                    .setFriction(friction) //1.16 = 1,0 cm/s ... 0.5 = 2,5 cm/s ... 0.6 = 3,0 cm/s
                    .start();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Atenção");
            builder.setMessage("Você deve voltar a bola para iniciar novamente!");
            builder.setPositiveButton("Ok", null);
            builder.show();
        }
    }

    public void resetAnimation(View view) {
        timer.cancel();
        min = 0;
        seconds = 0;
        decseconds = 0;

        timerTextViewSec.setText("0:00,0");
        positions = "";
        positions_sec = "";
        textViewPosition.setText("");


        Log.i("pxball", String.valueOf(ball.getX()));
        Log.i("pyball", String.valueOf(ball.getY()));
        ball.setX(0.0f);
        ball.setY(position_ball_start);

        if (flingY != null) {
            flingY.cancel();
        }

        pausecontrol = true;
        pauseButton.setClickable(false);
        pauseButton.setEnabled(false);

    }

}
