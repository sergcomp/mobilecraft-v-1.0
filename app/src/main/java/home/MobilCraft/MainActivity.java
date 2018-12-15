package home.MobilCraft;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import com.MobileCraft.R;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static home.MobilCraft.PermissionHalper.Re;
import static home.MobilCraft.PermissionHalper.TRe;
import static home.MobilCraft.NeedHelper.TAG_BUILD_NUMBER;
import static home.MobilCraft.NeedHelper.TAG_LAUNCH_TIMES;
import static home.MobilCraft.NeedHelper.TAG_SHORTCUT_CREATED;
import static home.MobilCraft.NeedHelper.getBuildNumber;
import static home.MobilCraft.NeedHelper.getLaunchTimes;
import static home.MobilCraft.NeedHelper.isCreateShortcut;
import static home.MobilCraft.NeedHelper.loadSettings;
import static home.MobilCraft.NeedHelper.saveSettings;
import android.os.CountDownTimer;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        loadSettings(this);
        IntentFilter filter = new IntentFilter(UnZipper.ACTION_UPDATE);
        registerReceiver(mR, filter);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        int i = getLaunchTimes();
        i++;
        saveSettings(TAG_LAUNCH_TIMES, i);
        pm = new PermissionHalper(this);
        String[] permList = pm.requestPermissions();
        if (permList.length > 0) {
            ActivityCompat.requestPermissions(this, permList, APR);
        } else {
            init();
        }

        delay();
        }

        public void delay() {
            new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {

                }
            }.start();
        }


    public void mFSn() {
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mFSn();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
        unregisterReceiver(mR);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFSn();
    }

    private void addShortcut() {
        saveSettings(TAG_SHORTCUT_CREATED, false);
        Intent shortcutIntent = new Intent(getApplicationContext(), MainActivity.class);
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_launcher));
        getApplicationContext().sendBroadcast(addIntent);
    }

    @SuppressWarnings("deprecation")
    public void init() {

        if (isCreateShortcut())
            addShortcut();
        PB = findViewById(R.id.PB1);
        Drawable draw;
        draw = getResources().getDrawable(R.drawable.custom_progress_bar);
        PB.setVisibility(View.VISIBLE);
        PB.setProgressDrawable(draw);
        util = new Utilities();
        util.createDataFolder();
        util.checkVersion();
    }

    private void requestPermissionAfterExplain() {
        Toast.makeText(this, R.string.explain, Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WER);
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissionAfterExplain();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WER);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case WER:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    requestStoragePermission();
                }
                break;
            case CLR:
                break;
            case APR:
                for (String perms : TRe) {
                    if (!pm.hasPermission(perms)) {
                        Re.add(perms);
                    }
                }
                if (Re.size() == 0) {
                    init();
                } else if (!Arrays.asList(Re.toArray()).contains(WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.location, Toast.LENGTH_SHORT).show();
                    init();
                } else {
                    requestStoragePermission();
                }
                break;
        }
    }

    private void showSpinnerDialog() {
        if (mPsD == null) {
            mPsD = new ProgressDialog(MainActivity.this);
            mPsD.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mPsD.setCancelable(false);
        }
        mPsD.setMessage(getString(R.string.rm_old));
        mPsD.show();
    }

    private void dismissProgressDialog() {
        if (mPsD != null && mPsD.isShowing()) {
            mPsD.dismiss();
        }
    }

    public void runGame() {
        util.deleteZip(FILES);
        startPlayActivity();
    }

    private void startPlayActivity() {
        Intent intent = new Intent(MainActivity.this, PlayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void startUnZipper(String[] file) {
        Intent intentMyIntentService = new Intent(this, UnZipper.class);
        intentMyIntentService.putExtra(UnZipper.EXTRA_KEY_IN_FILE, file);
        intentMyIntentService.putExtra(UnZipper.EXTRA_KEY_IN_LOCATION, unDir);
        startService(intentMyIntentService);

    }

    private class DeleteTask extends AsyncTask<String, Void, Void> {
        String location;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showSpinnerDialog();
        }

        @Override
        protected Void doInBackground(String... params) {
            location = params[0];
            for (String p : params) {
                util.deleteFiles(p);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isFinishing())
                return;
            dismissProgressDialog();
            new CopyZip().execute(FILES);
        }
    }

    private class CopyZip extends AsyncTask<String, Void, String> {
        String[] zips;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            zips = params;
            for (String s : zips) {
                copyAssets(s);
            }
            return "Done";
        }

        @Override
        protected void onPostExecute(String result)
        {
            if (util.getAvailableSpaceInMB() > 15) {
                       startUnZipper(zips);
            } else
                Toast.makeText(MainActivity.this, R.string.not_enough_space, Toast.LENGTH_LONG).show();
        }

        private void copyAssets(String zipName) {
            String filename = zipName.substring(zipName.lastIndexOf("/") + 1);
            InputStream in;
            OutputStream out;
            try {
                in = getAssets().open(filename);
                out = new FileOutputStream(zipName);
                copyFile(in, out);
                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to copy asset file: " + e.getMessage());
            }
        }

        private void copyFile(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
    private BroadcastReceiver mR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra(UnZipper.ACTION_PROGRESS, 0);
            if (progress >= 0) {
                PB.setVisibility(View.VISIBLE);
                PB.setProgress(progress);
            } else {
                util.createNomedia();
                runGame();
            }
        }
    };

    private class Utilities {

        private void createDataFolder() {
            File folder = new File(unDir);
            if (!(folder.exists()))
                folder.mkdirs();
        }

        private void deleteZip(String fileName) {
            File file = new File(fileName);
            if (file.exists())
                file.delete();
        }

        private void startDeletion(boolean isAll) {
            if (isAll) {
                new DeleteTask().execute(unDir);
            } else {
                new DeleteTask().execute(unDir + "games", unDir + "debug.txt");
            }
        }

        @SuppressWarnings("deprecation")
        @SuppressLint("NewApi")
        private long getAvailableSpaceInMB() {
            final long SIZE_KB = 1024L;
            final long SIZE_MB = SIZE_KB * SIZE_KB;
            long availableSpace;
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
             availableSpace = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
                return availableSpace / SIZE_MB;
        }

        void checkVersion() {
            if (getBuildNumber().equals(getString(R.string.ver))) {
                runGame();
            } else if (getBuildNumber().equals("0")) {
                saveSettings(TAG_BUILD_NUMBER, getString(R.string.ver));
                startDeletion(true);
            } else {
                saveSettings(TAG_BUILD_NUMBER, getString(R.string.ver));
                startDeletion(false);
            }
        }

        private void deleteFiles(String path) {
            File file = new File(path);
            if (file.exists()) {
                String deleteCmd = "rm -r " + path;
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec(deleteCmd);
                } catch (IOException e) {
                    Log.e(TAG, "delete files failed: " + e.getLocalizedMessage());
                }
            }
        }

        void createNomedia() {
            File myFile = new File(unDir, NOMEDIA);
            if (!myFile.exists())
                try {
                    myFile.createNewFile();
                } catch (IOException e) {
                    Log.e(TAG, "nomedia has not been created: " + e.getMessage());
                }
        }
    }

    private final static int CLR = 100;
    private final static int WER = 101;
    private final static int APR = 102;
    public final static String TAG = "Error";
    public final static String FILES = Environment.getExternalStorageDirectory() + "/Files.zip";
    public final static String NOMEDIA = ".nomedia";
    private ProgressDialog mPsD;
    private String dDir = "/Android/data/com.MobileCraft/files/";
    private String unDir = Environment.getExternalStorageDirectory() + dDir;
    private ProgressBar PB;
    private Utilities util;
    private PermissionHalper pm = null;

}