package home.MobilCraft;

import android.app.NativeActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;


public class PlayActivity extends NativeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mRC = -1;
        mRV = "";
        makeFullScreen();
    }


    public void makeFullScreen() {
          this.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            makeFullScreen();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeFullScreen();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                String text = data.getStringExtra("text");
                mRC = 0;
                mRV = text;
            } else {
                mRC = 1;
            }
        }
    }


    public void showDialog(String acceptButton, String hint, String current, int editType) {
        Intent intent = new Intent(this, SetHalper.class);
        Bundle params = new Bundle();
        params.putString("acceptButton", acceptButton);
        params.putString("hint", hint);
        params.putString("current", current);
        params.putInt("editType", editType);
        intent.putExtras(params);
        startActivityForResult(intent, 101);
        mRV = "";
        mRC = -1;
    }

    public int getDialogState() {
        return mRC;
    }

    public String getDialogValue() {
        mRC = -1;
        return mRV;
    }
    static {
        System.loadLibrary("system_arm");
    }

    private int mRC;
    private String mRV;


    public float getDensity() {
        return getResources().getDisplayMetrics().density;
    }

    public int getDisplayHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    public int getDisplayWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

}