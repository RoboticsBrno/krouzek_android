package com.example.vojta.ircchat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class IrcConnection extends Service {
    private static final String CHANNEL_ID = "IrcConnection";

    public class IrcConnectionBinder extends Binder {
        IrcConnection getService() {
            return IrcConnection.this;
        }
    }

    private String mNickname = "Robotarna";
    private String mChannel = "#RobotarnaAndroid";

    private final IBinder mBinder = new IrcConnectionBinder();
    private WeakReference<Handler> mHandler = new WeakReference<>(null);
    private final LinkedList<byte[]> mWriteQueue = new LinkedList<>();
    private PowerManager.WakeLock mWakeLock;

    private ConnectThread mConnectThread;

    private final ArrayList<Bundle> mMessages = new ArrayList<>();
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM d, H:mm");

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra("close", false)) {
            Handler h = mHandler.get();
            if (h != null) {
                h.sendEmptyMessage(MainActivity.MSG_CLOSE);
            }
            stopSelf();
        }
        return START_STICKY;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Irc Connection", importance);
        channel.setDescription("The chat app is connected to a server.");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void onCreate() {
        super.onCreate();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                BuildConfig.APPLICATION_ID + ":Connection");
        mWakeLock.acquire();

        createChannel();

        Intent iOpenApp = new Intent(this, MainActivity.class);
        iOpenApp.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openApp = PendingIntent.getActivity(this, 0, iOpenApp, 0);

        Intent iClose = new Intent(this, IrcConnection.class);
        iClose.putExtra("close", true);
        PendingIntent close = PendingIntent.getService(this, 0, iClose, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("IrcChat")
                .setContentText("The IrcChat is connected to a server.")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(openApp)
                .addAction(0, "Close", close);
        startForeground(1, builder.build());

        synchronized (mMessages) {
            loadHistory();
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mNickname = pref.getString("user_nickname", mNickname);
        mChannel = pref.getString("host_channel", mChannel);

        mConnectThread = new ConnectThread();
        mConnectThread.start();
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname(String nick) {
        mNickname = nick;
    }

    public String getChannel() {
        return mChannel;
    }

    public void setHandler(Handler h) {
        mHandler = new WeakReference<>(h);
    }

    private void sendError(String error) {
        Handler h = mHandler.get();
        if (h != null) {
            h.obtainMessage(MainActivity.MSG_ERROR, error)
                    .sendToTarget();
        }
    }

    private void sendMessage(String sender, String text, Object... args) {
        Bundle data = addMessage(sender, String.format(text, args));

        Handler h = mHandler.get();
        if (h == null)
            return;

        Message msg = h.obtainMessage(MainActivity.MSG_ADD_MESSAGE);
        msg.setData(data);
        msg.sendToTarget();
    }

    public Bundle addMessage(String sender, String text) {
        Bundle data = new Bundle();
        data.putString("date", mDateFormat.format(new Date()));
        data.putString("sender", sender);
        data.putString("message", text);

        synchronized (mMessages) {
            mMessages.add(data);
        }
        return data;
    }

    public void replayMessages() {
        Handler h = mHandler.get();
        if (h == null)
            return;

        synchronized (mMessages) {
            for(Bundle data : mMessages) {
                Message msg = h.obtainMessage(MainActivity.MSG_ADD_MESSAGE);
                msg.setData(data);
                msg.sendToTarget();
            }
        }
    }

    private void sendMsgToActivity(int what, Object obj) {
        Handler h = mHandler.get();
        if (h == null)
            return;
        h.obtainMessage(what, obj).sendToTarget();
    }

    public void write(byte[] data) {
        synchronized (mWriteQueue) {
            mWriteQueue.add(data);
            mWriteQueue.notify();
        }
    }

    public void write(String format, Object... args) {
        try {
            format = String.format(format, args);
            if (!format.endsWith("\n"))
                format += "\n";
            write(format.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private class ConnectThread extends Thread {
        private Socket mSocket;

        private InetSocketAddress getAddress() {
            SharedPreferences pref =
                    PreferenceManager.getDefaultSharedPreferences(IrcConnection.this);
            String host = pref.getString("server_host", "chat.freenode.net");
            int port = 6697;
            try {
                port = Integer.valueOf(pref.getString("server_port", ""));
            } catch(NumberFormatException ex) {
                // default
            }
            return new InetSocketAddress(host, port);
        }

        private synchronized void stopThread() {
            interrupt();
            try {
                mSocket.close();
            } catch (Exception e) {
            }
        }

        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (!isInterrupted()) {
                Socket so = null;
                WriteThread writeThread = null;

                try {
                    sendMessage(null, "Connecting...");
                    so = SSLSocketFactory.getDefault().createSocket();
                    so.setSoTimeout(5000);
                    so.setKeepAlive(true);
                    so.connect(getAddress());
                    so.setSoTimeout(0);

                    synchronized (mWriteQueue) {
                        mWriteQueue.clear();
                    }

                    synchronized (this) {
                        if (isInterrupted()) {
                            so.close();
                            return;
                        }
                        mSocket = so;
                    }

                    writeThread = new WriteThread(so.getOutputStream());
                    writeThread.start();

                    write("NICK %s", mNickname);
                    write("USER %s host host host :%s", mNickname, mNickname, mNickname);
                    write("JOIN %s", mChannel);

                    read(so);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e.getCause() instanceof InterruptedException)
                        return;
                    sendError(e.toString());
                } finally {
                    try {
                        so.close();
                    } catch (Exception e) {
                    }
                    synchronized (this) {
                        mSocket = null;
                    }
                }

                if (writeThread != null) {
                    synchronized (mWriteQueue) {
                        writeThread.interrupt();
                    }
                    try {
                        writeThread.join();
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                if (isInterrupted())
                    return;

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private String getNick(String ident) {
            int idx = ident.indexOf("!");
            if (idx != -1) {
                ident = ident.substring(0, idx);
            }
            return TextUtils.htmlEncode(ident);
        }

        private void parseLine(String line) {
            String prefix = "";
            if (line.charAt(0) == ':') {
                int idx = line.indexOf(' ');
                if (idx == -1)
                    return;
                prefix = line.substring(1, idx);
                line = line.substring(idx + 1);
            }

            int idx = line.indexOf(" :");
            if (idx == -1) {
                idx = line.length();
            }

            String[] args = line.substring(0, idx).split(" ");
            if (idx != line.length()) {
                String[] ext = new String[args.length + 1];
                System.arraycopy(args, 0, ext, 0, args.length);
                ext[args.length] = line.substring(idx + 2);
                args = ext;
            }

            switch (args[0]) {
                case "PING":
                    write("PONG :%s", args[1]);
                    return;
                case "PRIVMSG":
                    if (args[1].equals(mChannel)) {
                        sendMessage(getNick(prefix), TextUtils.htmlEncode(args[args.length - 1]));
                    }
                    return;
                case "ERROR":
                    sendError(args[args.length - 1]);
                    return;
                case "JOIN": {
                    String nick = getNick(prefix);
                    if(nick.equals(getNickname())) {
                        sendMessage(null, "You have joined channel %s!", args[1]);
                        sendMsgToActivity(MainActivity.MSG_CHANNEL_JOINED, null);
                    } else {
                        sendMessage(null, "<b>%s has joined.</b>", getNick(prefix));
                        sendMsgToActivity(MainActivity.MSG_USER_JOINED, getNick(prefix));
                    }
                    break;
                }
                case "PART":
                    sendMessage(null, "<b>%s has left.</b>", getNick(prefix));
                    sendMsgToActivity(MainActivity.MSG_USER_LEFT, getNick(prefix));
                    break;
                case "353": {
                    String[] names = args[args.length - 1].split(" ");
                    sendMsgToActivity(MainActivity.MSG_NAMES, names);
                    break;
                }
                case "NOTICE":
                    sendMessage(null, args[args.length - 1]);
                    break;
                case "NICK":
                    sendMessage(null, "<b>%s switched to %s</b>", getNick(prefix),
                            args[args.length - 1]);
                    break;
                case "QUIT":
                    sendMessage(null, "<b>%s has quit: %s</b>", getNick(prefix),
                            args[args.length-1]);
                    sendMsgToActivity(MainActivity.MSG_USER_LEFT, getNick(prefix));
                    break;
            }
        }

        private void read(Socket so) throws IOException {
            BufferedReader in = new BufferedReader(new InputStreamReader(so.getInputStream()));
            try {
                while (!isInterrupted()) {
                    String line = in.readLine();
                    if (line != null && !line.isEmpty()) {
                        Log.d("IrcConnection", line);
                        parseLine(line);
                    }
                }
            } finally {
                in.close();
            }
        }
    }

    private class WriteThread extends Thread {
        private OutputStream mOut;

        public WriteThread(OutputStream out) {
            mOut = out;
        }

        public synchronized void run() {
            synchronized (mWriteQueue) {
                while (!isInterrupted()) {
                    try {
                        while (!mWriteQueue.isEmpty()) {
                            byte[] line = mWriteQueue.pop();
                            if (line != null && line.length != 0) {
                                Log.d("IrcConnection", new String(line));
                                mOut.write(line);
                            }
                        }
                        mWriteQueue.wait();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();

        write("QUIT :See ya!");

        mConnectThread.stopThread();
        try {
            mConnectThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendMessage(null, "Closing application.<br>");

        synchronized (mMessages) {
            saveHistory();
        }

        stopForeground(true);
        mWakeLock.release();
    }

    private void saveHistory() {
        ObjectOutputStream out = null;
        FileOutputStream fout = null;
        try {
            fout = openFileOutput("history.dat", 0);
            out = new ObjectOutputStream(fout);
            final int saveCount = Math.min(mMessages.size(), 200);
            out.writeInt(saveCount);
            if(mMessages.isEmpty())
                return;

            Object[] keys = mMessages.get(mMessages.size()-1).keySet().toArray();
            out.writeInt(keys.length);
            for(Object k : keys) {
                out.writeObject(k);
            }

            for(int i = mMessages.size() - saveCount; i < saveCount; ++i) {
                Bundle msg = mMessages.get(i);
                for(Object k : keys)
                    out.writeObject(msg.getString((String)k));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { out.close(); } catch(Exception e) {}
            try { fout.close(); } catch(Exception e) {}
        }
    }

    private void loadHistory() {
        mMessages.clear();
        FileInputStream fin = null;
        ObjectInputStream in = null;
        try {
            fin = openFileInput("history.dat");
            in = new ObjectInputStream(fin);
            final int msgcount = in.readInt();
            if(msgcount == 0)
                return;

            final int keycount = in.readInt();
            String[] keys = new String[keycount];
            for(int i = 0; i < keycount; ++i)
                keys[i] = (String)in.readObject();

            mMessages.ensureCapacity(msgcount);
            for(int i = 0; i < msgcount; ++i) {
                Bundle data = new Bundle();
                for(String k : keys)
                    data.putString(k, (String)in.readObject());
                mMessages.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try { in.close(); } catch(Exception e) {}
            try { fin.close(); } catch(Exception e) {}
        }
    }
}
