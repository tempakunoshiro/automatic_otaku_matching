package tempakunoshiro.automaticotakumatching;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReceiveMessageIntentService extends IntentService {
    public static final String TAG = "RcvMsgIntentService";

    private static final String ACTION_RECEIVE_MESSAGE = "tempakunoshiro.automaticotakumatching.action.ACTION_RECEIVE_MESSAGE";
    private static final String EXTRA_RECEIVED_MESSAGE = "tempakunoshiro.automaticotakumatching.action.EXTRA_RECEIVED_MESSAGE";

    public ReceiveMessageIntentService() {
        super("ReceiveMessageIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent == null){
            Log.d(TAG, "intent is null");
            return;
        }
        WifiP2pInfo info = (WifiP2pInfo)intent.getSerializableExtra(
                WifiDirectIntentService.EXTRA_WIFI_P2P_INFO);
        String mode = intent.getStringExtra(WifiDirectIntentService.EXTRA_MODE);

        ServerSocket serverSocket = WifiDirectIntentService.getServerSocket();

        Socket socket = null;

        if(mode.equals(WifiDirectIntentService.MODE_CLIENT)){
            socket = new Socket();
            Log.d(TAG, "Create socket : client mode");

            try {
                socket.bind(null);
            } catch (IOException e) {
                Log.e("ClientMode/Socket1", e.toString());
            }

            try {
                socket.connect(new InetSocketAddress(info.groupOwnerAddress.getHostAddress(), 8988), 5000);
            } catch (IOException e) {
                Log.e("ClientMode/Socket2", e.toString());
            }
        }else if(mode.equals(WifiDirectIntentService.MODE_GROUP_OWNER)){
            Log.d(TAG, "Create socket : server mode");
            if(serverSocket == null){
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(8988));
                    WifiDirectIntentService.setServerSocket(serverSocket);
                } catch (IOException e) {
                    Log.e("ServerMode/Socket1", e.toString());
                }
            }
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                Log.e("ServerMode/Socket2", e.toString());
            }

        }
        if(socket == null){
            Log.d(TAG, "Cannot create socket");
            return;
        }
        Log.d(TAG, "Socket is opened");
        WifiDirectIntentService.addSocket(socket);
        try {
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            //メッセージを受け付ける処理
            String str;
            while ((str = reader.readLine()) != null){
                Log.d(TAG, "Received : " + str);
                //strをブロードキャストする

                Intent messageIntent = new Intent();
                messageIntent.setAction(ACTION_RECEIVE_MESSAGE);
                messageIntent.putExtra(EXTRA_RECEIVED_MESSAGE, str);
                sendBroadcast(messageIntent);
            }

            Log.d(TAG, "Reading finished");
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

    }
}
