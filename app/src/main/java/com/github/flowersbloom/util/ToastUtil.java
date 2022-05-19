package com.github.flowersbloom.util;

import android.widget.Toast;

import com.github.flowersbloom.UimApp;

public class ToastUtil {
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;

    private ToastUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void toast(CharSequence message) {
        Toast.makeText(UimApp.getContext(), message, LENGTH_SHORT).show();
    }
}
