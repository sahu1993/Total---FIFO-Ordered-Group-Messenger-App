package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static android.content.ContentValues.TAG;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    int key_SeqNo = 0;
    String failure_port;
    ArrayList<Msg> holdingQueue = new ArrayList<Msg>();
    int proposedSequenceNumber  = 0;
    private ContentResolver mContentResolver;
    private Uri mUri;
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    int process_flag;
    ArrayList<String> ports = new ArrayList<String>();
    static final int SERVER_PORT = 10000;
    EditText mEditText;
    String myPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

    /*
     * TODO: Use the TextView to display your messages. Though there is no grading component
     * on how you display the messages, if you implement it, it'll make your debugging easier.
     */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());


        mEditText = (EditText) findViewById(R.id.editText1);
        mEditText.setFocusable(true);
        mEditText.requestFocus();

        ports.add(REMOTE_PORT0);
        ports.add(REMOTE_PORT1);
        ports.add(REMOTE_PORT2);
        ports.add(REMOTE_PORT3);
        ports.add(REMOTE_PORT4);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        process_flag =  ports.indexOf(myPort);
        Log.e(TAG,"process_flag"+process_flag);
    /*
     * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
     * OnPTestClickListener demonstrates how to access a ContentProvider.
     */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        try {
        /*
         * Create a server socket as well as a thread (AsyncTask) that listens on the server
         * port.
         *
         * AsyncTask is a simplified thread construct that Android provides. Please make sure
         * you know how it works by reading
         * http://developer.android.com/reference/android/os/AsyncTask.html
         */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
        /*
         * //Log is a good way to debug your code. //LogCat prints out all the messages that
         * //Log class writes.
         *
         * Please read http://developer.android.com/tools/debugging/debugging-projects.html
         * and http://developer.android.com/tools/debugging/debugging-log.html
         * for more information on debugging.
         */
            //Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

    /*
     * TODO: You need to register and implement an OnClickListener for the "Send" button.
     * In your implementation you need to get the message from the input box (EditText)
     * and send it to other AVDs.
     */
    }

    public void onSendButtonClicked(View view) {
        //Log.e(TAG, "Inside onSendButtonClicked");
        //Log.e(TAG, "process_flag"+process_flag);
        String msgToSend =  mEditText.getText().toString()+"\n";
        if(msgToSend.length() == 1)
        {
            //Log.e(TAG, "inside if true");
            return;
        }
        //Log.e(TAG, "msg: "+msgToSend);
        TextView localTextView = (TextView) findViewById(R.id.local_text_display);
        localTextView.append("\t" + msgToSend); // This is one way to display a string.
        TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
        remoteTextView.append("\n");
        try {
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msgToSend, myPort);
            mEditText.setText("");
        }catch (Exception e){
            e.printStackTrace();
        }
        return;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            //Log.e(TAG, "Inside ServerTask doInBackground");
            ServerSocket serverSocket = sockets[0];

            try {
                while (true) {
                    //Log.e(TAG, "Inside while true");
                /* Following code is reference from https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html*/
                    Socket inputSocket = serverSocket.accept();
                    InputStream inputStream = inputSocket.getInputStream();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String recvMsg = bufferedReader.readLine();
                    //Log.e(TAG, "Msg in server: "+recvMsg);

                    String [] arrOfStr = recvMsg.split("##");

                    int msgNo = Integer.parseInt(arrOfStr[0]);

                    if(arrOfStr[1].equals("Failure")){
                        String failPort = arrOfStr[3];
                        Log.e(TAG, "MyPort: "+myPort+" FailPort: "+failPort);
                        int index = ports.indexOf(failPort);

                        if(index != -1){
                            Log.e(TAG, "Index: "+index);
                            ports.set(index,"0");
                        }

                        for (int j = 0; j < holdingQueue.size(); j++) {
                            if(holdingQueue.get(j).senderPort == Integer.parseInt(failPort)){
                                holdingQueue.remove(holdingQueue.get(j));
                                j--;
                            }
                        }

                    }else {
                        if (arrOfStr[4].equals("false")) {

                            proposedSequenceNumber++;
                            String proposedStr = proposedSequenceNumber + "." + process_flag;
                            Msg obj = new Msg(msgNo, arrOfStr[1], Float.parseFloat(proposedStr), false, Integer.parseInt(arrOfStr[2]));
                            boolean add = holdingQueue.add(obj);
                            Collections.sort(holdingQueue);
                            OutputStream outputStream = inputSocket.getOutputStream();
                            PrintWriter printWriter = new PrintWriter(outputStream, true);
                            printWriter.println(proposedStr);

                        }
                        if (arrOfStr[4].equals("true")) {
                            //Log.e(TAG, "Inside CanDeliver true");
                            //Log.e(TAG, "Recieved MSg In server: "+recvMsg);
                            float agreedSeqNo = Float.parseFloat(arrOfStr[5]);
                            //Log.e(TAG, "Inside CanDeliver true agreedSeqNo: " + agreedSeqNo);

                            Msg objOldMsg;
                            for (int i = 0; i < holdingQueue.size(); i++) {
                                if (holdingQueue.get(i).msgNo == msgNo) {
                                    //Log.e(TAG,"In true");
                                    holdingQueue.get(i).seqNo = agreedSeqNo;
                                    holdingQueue.get(i).canDeliver = true;
                                    break;
                                }
                            }
                            Collections.sort(holdingQueue);
                            proposedSequenceNumber = Math.max(proposedSequenceNumber, (int) (agreedSeqNo));
                            while (true) {
                                if (!holdingQueue.isEmpty()) {
                                    Msg obj = holdingQueue.get(0);
                                    //Log.e(TAG, "Inside deliverMessage MsgNo: " + obj.msgNo);
                                    //Log.e(TAG, "Inside deliverMessage CanDeliver: " + obj.canDeliver);
                                    if (obj.canDeliver == true) {
                                        mContentResolver = getContentResolver();
                                        Uri.Builder uriBuilder = new Uri.Builder();
                                        uriBuilder.authority("edu.buffalo.cse.cse486586.groupmessenger2.provider");
                                        uriBuilder.scheme("content");
                                        mUri = uriBuilder.build();
                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put(KEY_FIELD, Integer.toString(key_SeqNo));
                                        contentValues.put(VALUE_FIELD, obj.msg.trim());
                                        Log.e(TAG, "Befor deliver: " + obj.seqNo+ " : "+obj.msg.trim()+" : "+key_SeqNo);
                                        mContentResolver.insert(mUri, contentValues);
                                        key_SeqNo++;
                                        publishProgress(obj.msg);
                                        holdingQueue.remove(0);
                                    } else {
                                        //Log.e(TAG, "First Object don't have canDeliver true ");
                                        break;
                                    }
                                } else {
                                    //Log.e(TAG, "Holding Queue is empty: ");
                                    break;
                                }
                            }


                        }
                    }
                    inputSocket.close();
                /*referenced code ends*/
                }

            } catch (IOException e) {
                Log.e(TAG, "Error in Server Side");
            }

        /*
         * TODO: Fill in your server code that receives messages and passes them
         * to onProgressUpdate().
         */
            return null;
        }

        protected void onProgressUpdate(String...strings) {
        /*
         * The following code displays what is received in doInBackground().
         */
            //String strReceived = strings[0].trim();
            //Log.e(TAG, "Inside ServerTask onProgressUpdate");
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
            remoteTextView.append(strReceived + "\t\n");
            TextView localTextView = (TextView) findViewById(R.id.local_text_display);
            localTextView.append("\n");
            return;
        }
    }


    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            ////Log.e(TAG, "Inside ClientTask");
            ArrayList<Float> proposedNo = new ArrayList<Float>();
            Socket sockets;
            Random rn = new Random();
            int i;
            int randomMsgNo = rn.nextInt();
            for(i = 0; i < ports.size(); i++) {
                try {
                    if(Integer.parseInt(ports.get(i)) != 0) {
                        sockets = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(ports.get(i)));
                        String recv = msgs[0].trim();
                        sockets.setSoTimeout(2000);
                        String msgToSend = randomMsgNo + "##" + recv + "##" + myPort + "##" + ports.get(i) + "##" + "false" + "##" + proposedSequenceNumber;
                        //Log.e(TAG, "In ClientTask Message sent to the server : " + msgToSend);

                        /* Following code is reference from https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html*/
                        OutputStream outputStream = sockets.getOutputStream();
                        PrintWriter printWriter = new PrintWriter(outputStream, true);
                        printWriter.println(msgToSend);
                        /*referenced code ends*/

                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sockets.getInputStream()));
                        String recvMsg = bufferedReader.readLine();
                        if (recvMsg == null) {
                            Log.e(TAG, "In ClientTask null true:");
                            throw new SocketTimeoutException();
                        }


                        //Log.e(TAG, "In ClientTask Reply Back Messgae Proposal: "+recvMsg);
                        proposedNo.add(Float.parseFloat(recvMsg));

                        //Log.e(TAG, "In ClientTask Message Reply Back Messgae: "+recvMsg);
                        //proposedNo.add(Float.parseFloat(recvMsg));
                        sockets.close();
                    }
                    /*referenced code ends */
                }catch(IndexOutOfBoundsException e){
                    Log.e(TAG, "In ClientTask  IndexOutOfBoundsException: "+ports.get(i));
                }catch (UnknownHostException e) {
                    Log.e(TAG, "In ClientTask  UnknownHostException initial: "+ports.get(i));
                    e.printStackTrace();
                }
                catch (SocketTimeoutException e) {
                    Log.e(TAG, "In ClientTask  SocketTimeoutException initial: "+ports.get(i));
                    failure_port = ports.get(i);
                    try {
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(myPort));
                        String failureSting =randomMsgNo+"##"+"Failure"+"##"+myPort+"##"+failure_port;

                        /* Following code is reference from https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html*/
                        OutputStream outputStream = socket.getOutputStream();
                        PrintWriter printWriter = new PrintWriter(outputStream, true);
                        printWriter.println(failureSting);
                        /*referenced code ends*/

                        socket.close();

                    } catch (UnknownHostException e1) {
                        Log.e(TAG, "In ClientTask  UnknownHostException failure: ");
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        Log.e(TAG, "In ClientTask  IOException failure: ");
                        e1.printStackTrace();
                    }

                    e.printStackTrace();
                }
                catch (IOException e) {
                    Log.e(TAG, "In ClientTask  IOException initial: "+ports.get(i));
                    e.printStackTrace();
                }

            }
            Float agreedNo = Collections.max(proposedNo);

            for(i = 0; i < ports.size(); i++) {
                try {
                    if(Integer.parseInt(ports.get(i)) != 0) {
                        sockets = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(ports.get(i)));
                        sockets.setSoTimeout(2000);
                        String recv = msgs[0].trim();
                        String msgToSend = randomMsgNo + "##" + recv + "##" + myPort + "##" + ports.get(i) + "##" + "true" + "##" + agreedNo;
                        //Log.e(TAG, "ClientTask Message sent to the server with agreed No: " + msgToSend);

                        /* Following code is reference from https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html*/
                        OutputStream outputStream = sockets.getOutputStream();
                        PrintWriter printWriter = new PrintWriter(outputStream, true);
                        printWriter.println(msgToSend);
                        /*referenced code ends*/

                        sockets.close();
                    }
                }catch (UnknownHostException e) {
                    Log.e(TAG, "In ClientTask  UnknownHostException with agreed: "+ports.get(i));
                    e.printStackTrace();
                }
                catch (SocketTimeoutException e) {
                    Log.e(TAG, "In ClientTask  SocketTimeoutException initial: "+ports.get(i));
                    failure_port = ports.get(i);
                    try {
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(myPort));
                        String failureSting =randomMsgNo+"##"+"Failure"+"##"+myPort+"##"+failure_port;

                        /* Following code is reference from https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html*/
                        OutputStream outputStream = socket.getOutputStream();
                        PrintWriter printWriter = new PrintWriter(outputStream, true);
                        printWriter.println(failureSting);
                         /*referenced code ends*/

                        socket.close();
                    } catch (UnknownHostException e1) {
                        Log.e(TAG, "In ClientTask  UnknownHostException failure: ");
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        Log.e(TAG, "In ClientTask  IOException failure: ");
                        e1.printStackTrace();
                    }

                    e.printStackTrace();

                }
                catch (IOException e) {
                    Log.e(TAG, "In ClientTask  IOException with agreed: "+ports.get(i));
                    e.printStackTrace();
                }
                    /*referenced code ends*/
            }

            /*
             * TODO: Fill in your client code that sends out a message.
             */

            return null;
        }
    }
}
