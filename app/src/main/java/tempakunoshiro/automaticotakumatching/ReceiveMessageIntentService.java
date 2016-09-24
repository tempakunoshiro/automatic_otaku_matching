package tempakunoshiro.automaticotakumatching;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;
import android.widget.Switch;

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
        WifiP2pInfo info = (WifiP2pInfo)intent.getParcelableExtra(WifiDirectManager.EXTRA_WIFI_P2P_INFO);
        String mode = WifiDirectManager.getMode();

        ServerSocket serverSocket = WifiDirectManager.getServerSocket();

        Socket socket = null;

        if(mode.equals(WifiDirectManager.MODE_CLIENT)){
            socket = new Socket();
            Log.d(TAG, "Create socket : client mode");

            try {
                socket.bind(null);
            } catch (IOException e) {
                Log.e("ClientMode/Socket1", e.toString());
//                WifiDirectManager.setConnected(false);
//                onHandleIntent(intent);
                return;
            }

            try {
                socket.connect(new InetSocketAddress(info.groupOwnerAddress.getHostAddress(), 8988), 5000);
            } catch (IOException e) {
                Log.e("ClientMode/Socket2", e.toString());
//                WifiDirectManager.setConnected(false);
//                onHandleIntent(intent);
                return;
            }
        }else if(mode.equals(WifiDirectManager.MODE_GROUP_OWNER)){
            Log.d(TAG, "Create socket : server mode");
            if(serverSocket == null){
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(8988));
                    WifiDirectManager.setServerSocket(serverSocket);
                } catch (IOException e) {
                    Log.e("ServerMode/Socket1", e.toString());
//                    WifiDirectManager.setConnected(false);
//                    onHandleIntent(intent);
                    return;
                }
            }
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                Log.e("ServerMode/Socket2", e.toString());
//                WifiDirectManager.setConnected(false);
//                onHandleIntent(intent);
                return;
            }

        }
        if(socket == null || !socket.isConnected()){
            Log.d(TAG, "Cannot create socket");
//            WifiDirectManager.setConnected(false);
//            onHandleIntent(intent);
            return;
        }
        Log.d(TAG, "Socket is opened");
        WifiDirectManager.addSocket(socket);
        try {
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            //メッセージを受け付ける処理
            StringBuilder sb = new StringBuilder();
            String str;
            boolean reSendFlag = false;
            while ((str = reader.readLine()) != null){
                Log.d(TAG, "Received : " + str);
                /**
                 1.#で始まる文字列
                 2.$で終わる文字列
                 3.どちらでもない文字列
                 4.&で始まる文字列

                 1の場合、文字列の始まりであるのでsbをクリアし#を除いてappendし続行
                 2の場合、文字列の続きであるのでsbにappendし続行
                 3の場合、文字列の終わりであるので$を除いてsbにappendし、再送信フラグがtrueならsbを送信しフラグをfalse
                 さらにSwitcherにsb.toStringを渡す
                 4の場合、再送信しなければならない文字列なので再送信フラグをtrue、あとは1と同じ
                 */
                /*
                if(str.startsWith("#")){
                    sb.append(str, 1, str.length());
                }else if(str.startsWith("&")){
                    reSendFlag = true;
                    sb.append(str, 1, str.length());
                }else if(str.endsWith("$")){
                    sb.append(str, 0, str.length() - 1);
                    String rcvdMsg = sb.toString();
                    if(reSendFlag){
                        SendMessageIntentService.startSendAction(getApplicationContext(), rcvdMsg);
                        reSendFlag = false;
                    }
                    Switcher.sendData(getApplicationContext(), rcvdMsg);
                    sb = new StringBuilder();
                }else{
                    sb.append(str);
                }
                */
                if(WifiDirectManager.getMode().equals(WifiDirectManager.MODE_GROUP_OWNER)){
                    if(str.startsWith("#")) {
//                        SendMessageIntentService.startSendAction(getApplicationContext(), str.substring(1));
                    }
                }
                Switcher.sendData(getApplicationContext(), str);
            }

            Log.d(TAG, "Reading finished");
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

    }
}
