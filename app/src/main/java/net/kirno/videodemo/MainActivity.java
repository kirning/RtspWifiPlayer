package net.kirno.videodemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WifiManager mWifiManager;
    private List<ScanResult> mSeanResultList;
    private MListAdapter mAdapter;
    private ProgressBar mProgressBar;
    private String mSSIDPassword;
    private AlertDialog.Builder mPasswordDialog;
    private TextView mCurrentWifi;
    private ListView mListView;
    private Button mBtn;
    private ScanResult mCurrentScanResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //查找控件
        findView();

        //初始化控件
        initView();

        //初始化事件
        initEvent();

        //注册广播监听
        registerBroadcast();

        //开启wifi硬件
        startWifiHardware();
    }

    private void startWifiHardware() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    private void registerBroadcast() {
        IntentFilter mWifiFilter = new IntentFilter();
        mWifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mWifiFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mWifiFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        mWifiFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(new WifiBroad(), mWifiFilter);
    }

    private void initEvent() {
        mListView.setOnItemClickListener(new MItemClickListener());


        mBtn.setOnClickListener(new MBtnClick());
    }

    private void initView() {
        //初始化列表
        mSeanResultList = new ArrayList<>();
        mAdapter = new MListAdapter(this, mSeanResultList);
        mListView.setAdapter(mAdapter);

        //初始化密码输入框
        final EditText text = new EditText(this);
        mPasswordDialog = new AlertDialog.Builder(this)
                .setView(text)
                .setMessage("请输入wifi密码")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSSIDPassword = text.getText().toString();
                    }
                });

        //初始化wifi管理工具
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //初始化当前wifi信息
        WifiInfo info = mWifiManager.getConnectionInfo();
        if (info != null) {
            mCurrentWifi.setText(info.getSSID());
            new AlertDialog.Builder(this).setMessage("当前已与" + info.getSSID() + "连接，是否直接使用?")
                    .setTitle("受能萌萌哒")
                    .setPositiveButton("去吧，皮卡丘", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this, VideoActivity.class));
                        }
                    }).setNegativeButton("换一个wifi再说", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        } else {
            mCurrentWifi.setText("当前没有连接wifi");
        }

        //
        Button btn = (Button) findViewById(R.id.btn_enterVideo);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                if (wifiInfo == null) {
                    Toast.makeText(MainActivity.this, "当前没有连接wifi，请先选择一个wifi", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(MainActivity.this, VideoActivity.class));
                }
            }
        });
    }

    private void findView() {
        mCurrentWifi = (TextView) findViewById(R.id.text_current_wifi);
        mListView = (ListView) findViewById(R.id.list);
        mBtn = (Button) findViewById(R.id.btn_search);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
    }

    private class WifiBroad extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                //连接接入点成功并可以使用，或者已经断开了接入点
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    SupplicantState newState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    if (mCurrentScanResult != null) {
                        switch (newState.ordinal()) {
                            case 3:
                                mCurrentWifi.setText("开始连接 " + mCurrentScanResult.SSID);
                                break;
                            case 9:
                                mCurrentWifi.setText(mCurrentScanResult.SSID + " 连接成功");
                                startActivity(new Intent(MainActivity.this, VideoActivity.class));
                                break;
                        }
                    }
                    break;
                //扫描wifi热点结束后调用
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    mSeanResultList.clear();
                    List<ScanResult> list = mWifiManager.getScanResults();
                    mSeanResultList.addAll(list);
                    mAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.GONE);
                    break;
                //wifi硬件状态改变时调用
                case WifiManager.WIFI_STATE_CHANGED_ACTION: {
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_ENABLED:
                            mWifiManager.startScan();
                            break;
                    }
                }
            }
        }

    }

    private class MBtnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mWifiManager.startScan();
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private class MItemClickListener implements android.widget.AdapterView.OnItemClickListener {

        private WifiConfiguration config;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mCurrentScanResult = mSeanResultList.get(position);
            config = isSSIDExits(mCurrentScanResult.SSID);
            if (config == null) {
                if (mCurrentScanResult.capabilities.contains("WPA")) {
                    mPasswordDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            config = createWifiConfig(mCurrentScanResult.SSID, mSSIDPassword, 2);
                            connect(config);
                        }
                    });
                    mPasswordDialog.show();

                } else {
                    config = createWifiConfig(mCurrentScanResult.SSID, null, 0);
                    connect(config);
                }
            } else {
                connect(config);
            }
        }

        private void connect(WifiConfiguration config) {
            mWifiManager.enableNetwork(config.networkId, true);
        }

        public WifiConfiguration createWifiConfig(String SSID, String Password, int Type) {
            WifiConfiguration config = new WifiConfiguration();
            config.allowedAuthAlgorithms.clear();
            config.allowedGroupCiphers.clear();
            config.allowedKeyManagement.clear();
            config.allowedPairwiseCiphers.clear();
            config.allowedProtocols.clear();

            config.SSID = SSID;

            switch (Type) {
                case 0: //无密码
                    config.wepKeys[0] = "";
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    config.wepTxKeyIndex = 0;
                    break;
                case 1: //WEP加密
                    config.preSharedKey = "\"" + Password + "\"";
                    config.hiddenSSID = true;
                    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    config.wepTxKeyIndex = 0;
                    break;
                case 2:
                    config.preSharedKey = "\"" + Password + "\"";
                    config.hiddenSSID = true;
                    break;
                default:
                    return null;
            }
            return config;
        }
    }

    private WifiConfiguration isSSIDExits(String ssid) {
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifi : list) {
            if (wifi.SSID.equals("\"" + ssid + "\"")) {
                return wifi;
            }
        }
        return null;
    }
}
