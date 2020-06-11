package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    //added here
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final int SERVER_PORT = 10000;
    static int msgSeqNum = 0;
    static final String[] REMOTE_PORT = {"11108", "11112", "11116", "11120", "11124"};
    //end of added code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        //code added start
        //using code from PA1
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        }
        catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        //code added ends

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        //code added start
        //using some code from PA1
        final EditText editText = (EditText) findViewById(R.id.editText1);
        Button sendButton;
        sendButton = (Button) findViewById(R.id.button4);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString() + "\n";
                editText.setText("");
                //TextView localTextView = (TextView) findViewById(R.id.textView1);
                //localTextView.append("\t" + msg);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

            }
        });
        //code added ends
    }
    //code added
    //using PA1 code
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            try {
                while (true) {
                    Socket s1 = serverSocket.accept();
                    DataInputStream data_input = new DataInputStream(s1.getInputStream());
                    String read_str = data_input.readUTF();
                    publishProgress(read_str);
                }
            } catch (Exception e) {
                Log.e(TAG, "Msg receive failed");
            }
            return null;
        }

        protected void onProgressUpdate(String...strings) {
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append("\n");

            ContentValues keyValueToInsert = new ContentValues();
            keyValueToInsert.put("key", String.valueOf(msgSeqNum));
            keyValueToInsert.put("value", strReceived);
            Uri providerUri=Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger1.provider");
            getContentResolver().insert(providerUri, keyValueToInsert);
            msgSeqNum = msgSeqNum + 1;

            return;
        }
    }
    //code added ends

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    //code added
    //using code from PA1
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                for(int i = 0; i < REMOTE_PORT.length; i++) {

                    //String remotePort = REMOTE_PORT0;
                    //if (msgs[1].equals(REMOTE_PORT0))
                      //  remotePort = REMOTE_PORT1;

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORT[i]));

                    String msgToSend = msgs[0];
                    DataOutputStream data_out = new DataOutputStream(socket.getOutputStream());
                    data_out.writeUTF(msgToSend);
                    //data_out.flush();
                    //data_out.close();
                    //socket.close();
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }
    //code added ends
}
