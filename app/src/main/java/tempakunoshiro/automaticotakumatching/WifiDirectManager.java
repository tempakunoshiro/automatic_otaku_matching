package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by kamiori on 2016/09/24.
 */

public class WifiDirectManager {
    public static final String TAG = "WifiDirectManager";
    public static final String EXTRA_WIFI_P2P_INFO = "tempakunoshiro.automaticotakumatching.extra.WIFI_P2P_INFO";
    public static final String EXTRA_MODE = "tempakunoshiro.automaticotakumatching.extra.MODE";

    //WifiがOFFになっていることを通知するアクション
    public static final String ACTION_WIFI_DISABLED = "tempakunoshiro.automaticotakumatching.action.WIFI_DISABLED";

    private static final int DELAY = 1000;
    public static final String MODE_GROUP_OWNER = "Group Owner";
    public static final String MODE_CLIENT = "Client";

    private Context context;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private IntentFilter intentFilter = new IntentFilter();
    private WifiDirectBroadcastReceiver receiver;


    private static String mode = MODE_CLIENT;
    private static boolean isConnected = false;

    private static List<Socket> socketList;

    private static ServerSocket serverSocket = null;

    //端末を探すハンドラ
    private android.os.Handler requestPeersHandler = new android.os.Handler();
    private Runnable requestPeersTask = new Runnable() {
        @Override
        public void run() {
            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Discovery Initiated");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "Discovery Failed : " + reason);
                }
            });
            requestPeersHandler.postDelayed(this, DELAY);
        }
    };

    /**
     * 一度だけする必要がある初期化処理
     */
    public WifiDirectManager(Context context) {
        this.context = context;
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(context, context.getMainLooper(), null);

        receiver = new WifiDirectBroadcastReceiver(manager, channel, this);

        socketList = Collections.synchronizedList(new ArrayList<Socket>());

        context.registerReceiver(receiver, intentFilter);
        //端末の探索を1秒ごとに行う
        requestPeersHandler.postDelayed(requestPeersTask, DELAY);

        WifiManager mManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int wifiState = mManager.getWifiState();
        if(!(wifiState == WifiManager.WIFI_STATE_ENABLED || wifiState == WifiManager.WIFI_STATE_ENABLING)){
            broadcastWifiIsDisabled();
        }

        Log.d(TAG, "Send Broadcast");

        broadcastWifiIsDisabled();

    }

    //WifiがOFFであることをブロードキャストする
    public void broadcastWifiIsDisabled(){
        Intent intent = new Intent();
        intent.setAction(ACTION_WIFI_DISABLED);
        context.sendBroadcast(intent);

    }

    public void searchGroupOwner(WifiP2pDeviceList deviceList){
        //接続済みなら接続しない
        //ただし接続済みかつ
        if(isConnected){
            return;
        }

        WifiP2pDevice defaultDevice = null;
        for(WifiP2pDevice device : deviceList.getDeviceList()){
            if(defaultDevice == null){
                defaultDevice = device;
            }

            if(device.isGroupOwner()){
                if(device.status != 3){
                    if(defaultDevice.status != 3){
                        Log.d(TAG, "default device is not available");
                    }
                    return;
                }
                //見つかった場合、はじめに見つかったものに対して接続する
                Log.d(TAG, "Connecting : \n" + device.toString());
                WifiP2pConfig config = new WifiP2pConfig();

                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;

                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
//                        Toast.makeText(getApplicationContext(), "Connection initiated", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Connection initiated");
                    }

                    @Override
                    public void onFailure(int reason) {
//                        Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Connection failed : " + reason);
                    }
                });
                return;
            }else{

            }
        }
        if(defaultDevice == null){
            Log.d(TAG, "default device is null");
            return;
        }
        Log.d(TAG, "Default device : \n" + defaultDevice.toString());
        if(defaultDevice.status != 3){
            Log.d(TAG, "default device is not available");
            return;
        }

        WifiP2pConfig config = new WifiP2pConfig();

        config.deviceAddress = defaultDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Connection initiated");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Connection failed : " + reason);
            }
        });

    }

    public void notifyConnection(WifiP2pInfo info){
        if(info.isGroupOwner){
            mode = MODE_GROUP_OWNER;
            Log.d(TAG, "This device is Group Owner");
        }else{
            Log.d(TAG, "This device is Client");
        }

        Intent intent = new Intent(context, ReceiveMessageIntentService.class);
        intent.putExtra(EXTRA_WIFI_P2P_INFO, info);
        context.startService(intent);

    }

    public void reOpenSocket(WifiP2pInfo info){
        Intent intent = new Intent(context, ReceiveMessageIntentService.class);
        intent.putExtra(EXTRA_WIFI_P2P_INFO, info);
        context.startService(intent);
    }

    public static boolean isConnected() {
        return isConnected;
    }
    public static void setConnected(boolean connected) {
        isConnected = connected;
    }

    protected void onHandleIntent(Intent intent) {
    }

    public static ServerSocket getServerSocket(){
        return serverSocket;
    }

    public static void setServerSocket(ServerSocket serverSocket){
        WifiDirectManager.serverSocket = serverSocket;
    }

    public static List<Socket> getSocketList(){
        return socketList;
    }

    public static void addSocket(Socket socket){
        socketList.add(socket);
    }

    public static String getMode(){
        return mode;
    }

    public void destroy() {
        context.unregisterReceiver(receiver);
    }
}
