package com.example.vojta.ircchat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener, View.OnClickListener, UsersAdapter.OnUserClickedListener, DrawerLayout.DrawerListener {
    public static final int MSG_ADD_MESSAGE = 0;
    public static final int MSG_ERROR = 1;
    public static final int MSG_CLOSE = 2;
    public static final int MSG_CHANNEL_JOINED = 3;
    public static final int MSG_USER_JOINED = 4;
    public static final int MSG_USER_LEFT = 5;
    public static final int MSG_NAMES = 6;

    private static final int ACT_SETTINGS = 1;

    private WebView mWebView;
    private EditText mMessageEdit;
    private IrcHandler mHandler;
    private IrcConnection mConnection;
    private DrawerLayout mDrawerLayout;
    private RecyclerView mUserList;
    private List<String> mUsers;

    private final ServiceConnection mServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName n, IBinder iBinder) {
            mConnection = ((IrcConnection.IrcConnectionBinder) iBinder).getService();
            mConnection.setHandler(mHandler);
            mConnection.replayMessages();
        }

        public void onServiceDisconnected(ComponentName n) {
            mConnection = null;
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.addDrawerListener(this);

        mUsers = new ArrayList<>();
        UsersAdapter adapter = new UsersAdapter(this);
        adapter.setUsers(mUsers);

        mUserList = findViewById(R.id.userList);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
        mUserList.setAdapter(adapter);

        mWebView = findViewById(R.id.webView);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        mWebView.loadUrl("file:///android_asset/chat.html");

        mMessageEdit = findViewById(R.id.message);
        mMessageEdit.setOnEditorActionListener(this);
        mMessageEdit.setImeOptions(EditorInfo.IME_ACTION_SEND);

        Button sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(this);

        mHandler = new IrcHandler();
        mHandler.act = this;

        Intent i = new Intent(this, IrcConnection.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }
        bindService(i, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings: {
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, ACT_SETTINGS);
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int request, int result, Intent data) {
        if(request == ACT_SETTINGS && mConnection != null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String nick = mConnection.getNickname();
            if(!nick.equals(pref.getString("user_nickname", nick))) {
                nick = pref.getString("user_nickname", nick);
                mConnection.setNickname(nick);
                mConnection.write("NICK %s", nick);
            }
        }
    }

    private void addMessage(String date, String sender, String message, Object... args) {
        if (date == null)
            date = "";

        if (message == null)
            message = "";

        message = String.format(message, args).replace("'", "\\'");
        if (sender == null || sender.isEmpty()) {
            sender = "null";
        } else {
            sender = String.format("'%s'", sender.replace("'", "\\'"));
        }
        mWebView.loadUrl(String.format("javascript:addMessage('%s', %s, '%s');", date, sender, message));
    }

    private static class IrcHandler extends Handler {
        MainActivity act;

        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            if (act == null)
                return;

            switch (msg.what) {
                case MSG_ADD_MESSAGE:
                    act.addMessage(data.getString("date", ""),
                            data.getString("sender"), data.getString("message"));
                    break;
                case MSG_ERROR:
                    act.addMessage(null, null, "<span style=\"color: red\">%s</span>", msg.obj);
                    break;
                case MSG_CLOSE:
                    act.finish();
                    break;
                case MSG_CHANNEL_JOINED:
                    act.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    act.setUserListVisible(false);
                    if(!act.mUsers.isEmpty()) {
                        act.mUsers.clear();
                        act.mUserList.getAdapter().notifyDataSetChanged();
                    }
                    break;
                case MSG_USER_JOINED:
                    if(!act.mUsers.isEmpty()) {
                        act.addUser((String)msg.obj);
                    }
                    break;
                case MSG_USER_LEFT:
                    act.rmUser((String)msg.obj);
                    break;
                case MSG_NAMES:
                    act.mUsers.clear();
                    Collections.addAll(act.mUsers, (String[])msg.obj);
                    Collections.sort(act.mUsers);
                    act.mUserList.getAdapter().notifyDataSetChanged();
                    act.setUserListVisible(true);
                    break;
            }
        }
    }

    public void onBackPressed() {
        mConnection.stopSelf();
        super.onBackPressed();
    }

    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConn);
        mHandler.act = null;
        mHandler = null;
    }

    private void sendMessage() {
        sendMessage(mMessageEdit.getText().toString());
        mMessageEdit.setText("");
    }

    public void sendMessage(String msg) {
        if (mConnection == null)
            return;

        msg = TextUtils.htmlEncode(msg);
        mConnection.addMessage(mConnection.getNickname(), msg);
        mConnection.write("PRIVMSG %s :%s", mConnection.getChannel(), msg);
        addMessage(null, mConnection.getNickname(), msg);
    }

    public boolean onEditorAction(TextView v, int action, KeyEvent ev) {
        if(action == EditorInfo.IME_ACTION_SEND) {
            sendMessage();
            return true;
        }
        return false;
    }

    public void onClick(View view) {
        if(view.getId() == R.id.sendBtn)
            sendMessage();
    }

    @Override
    public void OnUserClicked(String username) {
        String text = mMessageEdit.getText().toString();
        if(text.isEmpty())
            mMessageEdit.setText(username + " ");
        else
            mMessageEdit.setText(String.format("%s %s ", text, username));
        mDrawerLayout.closeDrawers();
    }

    public void onDrawerSlide(View view, float v) { }
    public void onDrawerClosed(View view) { }
    public void onDrawerStateChanged(int i) { }

    public void onDrawerOpened(View view) {
        if(mConnection != null && mUsers.isEmpty()) {
            mConnection.write("NAMES %s", mConnection.getChannel());
        }
    }

    private void setUserListVisible(boolean visible) {
        ProgressBar bar = findViewById(R.id.usersProgressBar);
        bar.setVisibility(visible ? View.GONE : View.VISIBLE);
        mUserList.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void addUser(String username) {
        if (!mUsers.contains(username)) {
            mUsers.add(username);
            Collections.sort(mUsers);
            mUserList.getAdapter().notifyDataSetChanged();
        }
    }

    public void rmUser(String username) {
        if (mUsers.remove(username))
            mUserList.getAdapter().notifyDataSetChanged();
    }
}
