package com.mobile.exampledrawapp;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FloatingWindow extends Service {
    int LAYOUT_FLAG;
    View mFloatView;
    WindowManager windowManager;
    ImageView imageClose;
    TextView tvWidget;
    TextView tvEraserWidget;
    PaintView paintView;
    float height,width;
    private WindowManager win;
    private LinearLayout ll;
    private PaintView pp;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*public void onCreate(){
        super.onCreate();
        image = new ImageView(this);
        image.setImageResource(R.drawable.ic_launcher_foreground);
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        final WindowManager.LayoutParams paramsF = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        paramsF.gravity = Gravity.TOP|Gravity.LEFT;
        paramsF.x=0;
        paramsF.y=100;
        windowManager.addView(image,paramsF);
        try{
            image.setOnTouchListener(new View.OnTouchListener() {
                WindowManager.LayoutParams paramsT = paramsF;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            initialX = paramsF.x;
                            initialY = paramsF.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                            paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(v, paramsF);
                            break;
                    }

                    return false;
                }
            });
        }
        catch (Exception e){

        }
    }*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else{
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        //inflate widget layout
        mFloatView = LayoutInflater.from(this).inflate(R.layout.overlay_activity,null);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        //initial position
        layoutParams.gravity = Gravity.TOP|Gravity.RIGHT;
        layoutParams.x=0;
        layoutParams.y=100;

        WindowManager.LayoutParams imageParams = new WindowManager.LayoutParams(1920,
                1080,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        imageParams.gravity = Gravity.BOTTOM|Gravity.CENTER;
        imageParams.y=100;

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        imageClose= new ImageView(this);
        imageClose.setImageResource(R.drawable.icon_close);
        imageClose.setVisibility(View.INVISIBLE);
        paintView = new PaintView(this);
        paintView.setVisibility(View.INVISIBLE);
        windowManager.addView(imageClose,imageParams);
        windowManager.addView(mFloatView,layoutParams);
        mFloatView.setVisibility(View.VISIBLE);

        height = windowManager.getDefaultDisplay().getHeight();
        width = windowManager.getDefaultDisplay().getWidth();

        tvWidget = (TextView)mFloatView.findViewById(R.id.text_widget);
        tvEraserWidget = (TextView)mFloatView.findViewById(R.id.text_widget_eraser);
        paintView = (PaintView)mFloatView.findViewById(R.id.paintView);

        /*//show current time in textview
        Handler handler  =new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
             tvWidget.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
             handler.postDelayed(this,1000);
            }
        },10);*/
        //drag movement for widget
        tvEraserWidget.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                paintView.clear();
                return false;
            }
        });
        tvWidget.setOnTouchListener(new View.OnTouchListener() {
            int initialX,initialY;
            float initialTouchX,initialTouchY;
            long startClickTime;

            int MAX_CLICK_DURATION=200;
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        startClickTime  = Calendar.getInstance().getTimeInMillis();
                        imageClose.setVisibility(View.VISIBLE);

                        initialX = layoutParams.x;
                        initialY = layoutParams.y;

                        //touch position
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        long clickDuraiton = Calendar.getInstance().getTimeInMillis()-startClickTime;
                        imageClose.setVisibility(View.GONE);

                        layoutParams.x = initialX+(int)(initialTouchX-motionEvent.getRawX());
                        layoutParams.y = initialY+(int)(motionEvent.getRawY()-initialTouchY);
                        if (clickDuraiton<MAX_CLICK_DURATION){
                            DisplayMetrics metrics = new DisplayMetrics();
                            metrics = getApplicationContext().getResources().getDisplayMetrics();
                            paintView.init(metrics);
                            paintView.setVisibility(View.VISIBLE);
                            Toast.makeText(FloatingWindow.this, "Time :"+tvWidget.getText(), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            //remove widget
                            if (layoutParams.y>(height*0.6)){
                                stopSelf();
                            }

                        }


                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //calculeate X VE Y
                        layoutParams.x=initialX+(int)(initialTouchX-motionEvent.getRawX());
                        layoutParams.y=initialY+(int)(motionEvent.getRawY()-initialTouchY);

                        //update layout with new coordinates
                        windowManager.updateViewLayout(mFloatView,layoutParams);
                        if (layoutParams.y>height*0.6)
                        {
                            imageClose.setImageResource(R.drawable.icon_orange);
                        }
                        else
                        {
                            imageClose.setImageResource(R.drawable.icon_close);
                        }
                        return true;
                }
                return false;
            }
        });
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mFloatView!=null){
            windowManager.removeView(mFloatView);
        }
        if (imageClose!=null){
            windowManager.removeView(imageClose);
        }
    }
    /* @Override
    public void onCreate(){
        pp = new PaintView(this);
        super.onCreate();
        win = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);
        LinearLayout.LayoutParams llParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setBackgroundColor(Color.WHITE);
        ll.setLayoutParams(llParameters);
        WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(400,150,WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        parameters.x=0;
        parameters.y=0;
        parameters.gravity = Gravity.CENTER | Gravity.CENTER;
        win.addView(ll,parameters);
    }*/
}
