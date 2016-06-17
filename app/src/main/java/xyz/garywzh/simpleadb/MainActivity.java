package xyz.garywzh.simpleadb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private static final String[] ENABLE = {
            "setprop service.adb.tcp.port 5555",
            "stop adbd",
            "start adbd",
            "getprop service.adb.tcp.port"};
    private static final String[] DISABLE = {
            "setprop service.adb.tcp.port -1",
            "stop adbd",
            "start adbd",
            "getprop service.adb.tcp.port"};
    private static final String[] CHECK = {"getprop service.adb.tcp.port"};
    private static final String WIFI_ADB_PORT = "5555";

    private SwitchCompat ADBSwitch;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ADBSwitch = (SwitchCompat) findViewById(R.id.ADBSwitch);
        mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setWIFIADB(isChecked);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateADBSwitch();
    }

    private void updateADBSwitch() {
        ADBSwitch.setOnCheckedChangeListener(null);
        configADB(CHECK, true);
    }

    private void setWIFIADB(final boolean enable) {
        configADB(enable ? ENABLE : DISABLE, false);
    }

    private void configADB(final String[] strings, final boolean isInit) {
        ADBSwitch.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean result = sudoForResult(strings).trim().equals(WIFI_ADB_PORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ADBSwitch.setChecked(result);
                        if (isInit) {
                            ADBSwitch.setOnCheckedChangeListener(mOnCheckedChangeListener);
                        }
                        ADBSwitch.setEnabled(true);
                    }
                });
            }
        }).start();
    }

    private String sudoForResult(String[] strings) {
        String res = "";

        try {
            Process proc = Runtime.getRuntime().exec("su");

            //input commands
            try (DataOutputStream outputStream = new DataOutputStream(proc.getOutputStream())) {
                for (String s : strings) {
                    outputStream.writeBytes(s + "\n");
                    outputStream.flush();
                }
                outputStream.writeBytes("exit\n");
                outputStream.flush();
                outputStream.close();
            }

            //wait for execution to finish
            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //read response
            int read;
            char[] buffer = new char[1024];
            StringBuilder builder = new StringBuilder();
            try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                while ((read = inputReader.read(buffer)) > 0) {
                    builder.append(buffer, 0, read);
                }
            }

            res = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
