package com.cps.legendofbounca;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Random;

public class GravityActivity extends AppCompatActivity {

    private float readSensorTimestamp = 1;
    private float refreshViewTimestamp = 1;

    private float x;
    private float y;

    private double vx = 0;
    private double vy = 0;

    private double fx;
    private double fy;

    private int RIGHTEST_POSITION;
    private int BOTTOMMOST_POSITION;

    private boolean gameStarted = false;
    private boolean gameStartedFirstTime = true;
    private View movingObject;

    public void randomVelocityClicked(View view) {
        this.x = movingObject.getX();
        this.y = movingObject.getY();

        View layout = findViewById(R.id.layout);
        int layoutRight = layout.getRight();
        int layoutBottom = layout.getBottom();

        Random r = new Random();
        int i1 = r.nextInt(500-50)+50;
        int i2 = r.nextInt(500-50)+50;

        if (x > layoutRight/2){
            vx = -i1;
        }
        else{
            vx = i1;
        }
        if (y > layoutBottom/2){
            vy = -i2;
        }
        else{
            vy = i2;
        }
    }

    public void screenClick(View view) {
        if (gameStartedFirstTime){
            this.gameStartedFirstTime = false;
            findViewById(R.id.movingObject).setVisibility(View.VISIBLE);
            findViewById(R.id.introduction_gravity).setVisibility(View.INVISIBLE);

            View layout = findViewById(R.id.layout);
            int layoutRight = layout.getRight();
            int layoutBottom = layout.getBottom();

            int movingObjectWidth = movingObject.getWidth();
            int movingObjectHeight = movingObject.getHeight();

            this.RIGHTEST_POSITION = layoutRight - movingObjectWidth;
            this.BOTTOMMOST_POSITION = layoutBottom - movingObjectHeight;

            Random r = new Random();
            int i1 = r.nextInt(this.RIGHTEST_POSITION);
            int i2 = r.nextInt(this.BOTTOMMOST_POSITION);

            this.x = i1;
            this.y = i2;

            gameStarted = true;
            findViewById(R.id.pauseBanner).setVisibility(View.INVISIBLE);
        }
        else {
            if (gameStarted) {
                this.x = movingObject.getX();
                this.y = movingObject.getY();

                findViewById(R.id.pauseBanner).setVisibility(View.VISIBLE);

                gameStarted = false;
            } else
                resume();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gravity);
        this.movingObject = findViewById(R.id.movingObject);
        findViewById(R.id.movingObject).setVisibility(View.INVISIBLE);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setupGravitySensor(sensorManager);
    }

    public void ballClicked(View view) {
        resume();
    }

    private void resume() {
        View layout = findViewById(R.id.layout);
        int layoutRight = layout.getRight();
        int layoutBottom = layout.getBottom();

        int movingObjectWidth = movingObject.getWidth();
        int movingObjectHeight = movingObject.getHeight();

        this.RIGHTEST_POSITION = layoutRight - movingObjectWidth;
        this.BOTTOMMOST_POSITION = layoutBottom - movingObjectHeight;

        this.x = movingObject.getX();
        this.y = movingObject.getY();

        gameStarted = true;
        findViewById(R.id.pauseBanner).setVisibility(View.INVISIBLE);
    }


    private void setupGravitySensor(SensorManager sensorManager) {
        Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        SensorEventListener rvListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float dT = (sensorEvent.timestamp - readSensorTimestamp) * Config.NS2US;
                if (dT > Config.READ_SENSOR_RATE) {
                    double gravityX = sensorEvent.values[0];
                    gravityX = -gravityX;
                    double gravityY = sensorEvent.values[1];
                    double gravityZ = sensorEvent.values[2];

                    if (gameStarted) {
                        final double time_slice = dT * Config.US2S;
                        fx = vx == 0 ? (gravityX - (gravityZ * Config.MU_S)) : (gravityX - (gravityZ * Config.MU_K));
                        fy = vy == 0 ? (gravityY - (gravityZ * Config.MU_S)) : (gravityY - (gravityZ * Config.MU_K));

                        double ax = fx / Config.MASS;
                        double ay = fy / Config.MASS;


                        double newX = (0.5) * ax * Math.pow(time_slice, 2) + vx * time_slice + x;
                        double newY = (0.5) * ay * Math.pow(time_slice, 2) + vy * time_slice + y;
                        x = (newX >= RIGHTEST_POSITION) ? RIGHTEST_POSITION : (float) ((newX <= 0) ? 0 : newX);
                        y = (newY >= BOTTOMMOST_POSITION) ? BOTTOMMOST_POSITION : (float) ((newY <= 0) ? 0 : newY);

                        double newVX = ax * time_slice + vx;
                        double newVY = ay * time_slice + vy;

                        if((newX >= RIGHTEST_POSITION || newX <= 0) || (newY >= BOTTOMMOST_POSITION || newY <= 0)) {
                            if (newX >= RIGHTEST_POSITION || newX <= 0) {
                                vx = -newVX * Math.sqrt(1 - Config.DISSIPATION_COEFFICIENT);
                                vy = newVY * Math.sqrt(1 - Config.DISSIPATION_COEFFICIENT);
                            }
                            if (newY >= BOTTOMMOST_POSITION || newY <= 0) {
                                vx = newVX * Math.sqrt(1 - Config.DISSIPATION_COEFFICIENT);
                                vy = -newVY * Math.sqrt(1 - Config.DISSIPATION_COEFFICIENT);
                            }
                            if((newX >= RIGHTEST_POSITION || newX <= 0) && (newY >= BOTTOMMOST_POSITION || newY <= 0)){
                                vx = -newVX * Math.sqrt(1 - Config.DISSIPATION_COEFFICIENT);
                                vy = -newVY * Math.sqrt(1 - Config.DISSIPATION_COEFFICIENT);
                            }
                        }
                        else{
                            vx = newVX;
                            vy = newVY;
                        }

                    }
                    readSensorTimestamp = sensorEvent.timestamp;
                }
                dT = (sensorEvent.timestamp - refreshViewTimestamp) * Config.NS2US;
                if (dT > Config.UPDATE_VIEW_RATE && gameStarted) {
                    moveObject();
                    TextView sensorStatus = findViewById(R.id.gravitySensorStatus);
                    String sensorOutputs = getSensorStatus(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                    sensorStatus.setText(sensorOutputs);
                    refreshViewTimestamp = sensorEvent.timestamp;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        sensorManager.registerListener(rvListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private String getSensorStatus(double gravityX, double gravityY, double gravityZ) {
        return String.format(Locale.ENGLISH, "gravity_x: %.4f\ngravity_y: %.4f\ngravity_z: %.4f\nx: %.4f\ny: %.4f\nvx: %.4f\nvy: %.4f\n", gravityX, gravityY, gravityZ, x, y, vx, vy);
    }

    public void moveObject() {
        setPosition(x, y);
    }

    private void setPosition(double newX, double newY) {
        movingObject.setX((float) newX);
        movingObject.setY((float) newY);
    }
}