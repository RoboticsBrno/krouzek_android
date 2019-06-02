package com.example.vojta.ircchat;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.os.BuildCompat;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.net.URL;

public class IrcEditText extends AppCompatEditText {
    public IrcEditText(Context context) {
        super(context);
    }

    public IrcEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IrcEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final InputConnectionCompat.OnCommitContentListener callback =
        new InputConnectionCompat.OnCommitContentListener() {
        @Override
        public boolean onCommitContent(InputContentInfoCompat info,
                                       int flags, Bundle opts) {
            Uri link = info.getLinkUri();
            if(link == null)
                return false;

            if(!(getContext() instanceof MainActivity))
                return false;

            MainActivity act = (MainActivity)getContext();
            act.sendMessage("IMG: " + link.toString());
            return true;
        }
    };

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        final InputConnection ic = super.onCreateInputConnection(editorInfo);
        EditorInfoCompat.setContentMimeTypes(editorInfo,
                new String [] {"image/*"});
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback);
    }
}
