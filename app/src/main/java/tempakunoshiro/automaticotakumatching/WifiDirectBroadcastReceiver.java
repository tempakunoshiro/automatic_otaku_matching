package tempakunoshiro.automaticotakumatching;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiDirectIntentService service;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiDirectIntentService service) {
        super();

        this.manager = manager;
        this.channel = channel;
        this.service = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action == null)return;
        //そういえば「現在のWifiの有効/無効状態を取得」ではなく「有効/無効状態の変化を取得」なので不要だった
/*        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct is enabled
                // OK
            } else {
                // Wi-Fi Direct is not enabled
                // NG
                service.broadcastWifiIsDisabled();
            }
        }else*/
        if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if (manager != null) {
                Log.d(WifiDirectIntentService.TAG, "Peers changed");

                //デバイス一覧を要求
                manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    //見つかったデバイス一覧
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        service.searchGroupOwner(peers);

                        Log.d(WifiDirectIntentService.TAG, "Found peers : \n");
                        for (WifiP2pDevice device : peers.getDeviceList()) {
                            Log.d(WifiDirectIntentService.TAG, device.deviceName);
                        }
                    }
                });
            }
        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            Log.d(WifiDirectIntentService.TAG, "State changed");
            if(manager != null){
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){
                    Log.d(WifiDirectIntentService.TAG, "Connected");
                    WifiP2pInfo info = (WifiP2pInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                    service.setConnected(true);
                    service.notifyConnection(info);
                }else{
                    Log.d(WifiDirectIntentService.TAG, "not Connected");
                    //切断された
                    service.setConnected(false);
                }
            }

        }
    }

    private String toStringDevice(WifiP2pDevice device){
        return String.format("name  : %s\nstatus : %d\n", device.deviceName, device.status);
    }
}
