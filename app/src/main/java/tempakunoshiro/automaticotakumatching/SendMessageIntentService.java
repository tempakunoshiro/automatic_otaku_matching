package tempakunoshiro.automaticotakumatching;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class SendMessageIntentService extends IntentService {
    private static final String TAG = "SendMsgIntentService";

    private static final String ACTION_SEND_MESSAGE = "tempakunoshiro.automaticotakumatching.action.SEND_MESSAGE";

    private static final String EXTRA_MESSAGE = "tempakunoshiro.automaticotakumatching.extra.MESSAGE";

    public SendMessageIntentService() {
        super("SendMessageIntentService");
    }

    public static void startSendAction(Context context, String message) {
        Intent intent = new Intent(context, SendMessageIntentService.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent == null)return;
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        String mode = WifiDirectManager.getMode();
        List<Socket> socketList = WifiDirectManager.getSocketList();
        try {
            if (mode.equals(WifiDirectManager.MODE_CLIENT)) {
                Log.d(TAG, "Send : client mode");
                Log.d(TAG, "socket : " + Arrays.toString(socketList.toArray(new Socket[]{})));
                //クライアント時
                message = "#" + message;
                for(Socket socket : socketList){
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    Log.d(TAG, "Sent : " + message);
                    writer.write(message+"\n");
                    writer.flush();
                }
            } else if(mode.equals(WifiDirectManager.MODE_GROUP_OWNER)){
                Log.d(TAG, "Send : server mode");
                Log.d(TAG, "socket : " + Arrays.toString(socketList.toArray(new Socket[]{})));
                //サーバ時
  //              message = "#" + message + "$";
                for(Socket socket : socketList){
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    Log.d(TAG, "Sent : " + message);
                    writer.write(message+"\n");
                    writer.flush();
                }
            }
            Log.d(TAG, "Sending finished");
        }catch(Exception e){
            Log.e(TAG, e.toString());
        }

    }


}
