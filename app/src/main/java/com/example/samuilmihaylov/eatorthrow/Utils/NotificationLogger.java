package com.example.samuilmihaylov.eatorthrow.Utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.Toast;

import com.example.samuilmihaylov.eatorthrow.Enums.MessageType;

public class NotificationLogger {

    public static void showToast(Context context, String message, MessageType messageType) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        View view = toast.getView();

        view.getBackground().setColorFilter(messageType == MessageType.ERROR ? Color.RED : Color.GREEN, PorterDuff.Mode.SRC_IN);

        toast.show();
    }
}
