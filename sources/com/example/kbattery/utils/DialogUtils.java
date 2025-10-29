package com.example.kbattery.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AlertDialog;
import com.example.kbattery.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* loaded from: classes4.dex */
public class DialogUtils {
    public static MaterialAlertDialogBuilder createConfirmDialog(Context context, String title, String message, String positiveText, DialogInterface.OnClickListener positiveListener, String negativeText, DialogInterface.OnClickListener negativeListener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context).setTitle((CharSequence) title).setMessage((CharSequence) message).setPositiveButton((CharSequence) positiveText, positiveListener);
        if (!TextUtils.isEmpty(negativeText)) {
            builder.setNegativeButton((CharSequence) negativeText, negativeListener);
        }
        return builder;
    }

    public static MaterialAlertDialogBuilder createBlurConfirmDialog(Context context, String title, String message, String positiveText, DialogInterface.OnClickListener positiveListener, String negativeText, DialogInterface.OnClickListener negativeListener) {
        return createConfirmDialog(context, title, message, positiveText, positiveListener, negativeText, negativeListener);
    }

    public static MaterialAlertDialogBuilder createInfoDialog(Context context, String title, String message, String buttonText, DialogInterface.OnClickListener listener) {
        return new MaterialAlertDialogBuilder(context).setTitle((CharSequence) title).setMessage((CharSequence) message).setPositiveButton((CharSequence) buttonText, listener);
    }

    public static MaterialAlertDialogBuilder createBlurInfoDialog(Context context, String title, String message, String buttonText, DialogInterface.OnClickListener listener) {
        return createInfoDialog(context, title, message, buttonText, listener);
    }

    public static MaterialAlertDialogBuilder createErrorDialog(Context context, String message, DialogInterface.OnClickListener listener) {
        return new MaterialAlertDialogBuilder(context).setTitle((CharSequence) context.getString(R.string.error_title)).setMessage((CharSequence) message).setPositiveButton((CharSequence) context.getString(R.string.ok), listener);
    }

    public static MaterialAlertDialogBuilder createBlurErrorDialog(Context context, String message, DialogInterface.OnClickListener listener) {
        return createErrorDialog(context, message, listener);
    }

    public static MaterialAlertDialogBuilder createWarningDialog(Context context, String title, String message, String positiveText, DialogInterface.OnClickListener positiveListener, String negativeText, DialogInterface.OnClickListener negativeListener) {
        return new MaterialAlertDialogBuilder(context).setTitle((CharSequence) title).setMessage((CharSequence) message).setPositiveButton((CharSequence) positiveText, positiveListener).setNegativeButton((CharSequence) negativeText, negativeListener);
    }

    public static MaterialAlertDialogBuilder createBlurWarningDialog(Context context, String title, String message, String positiveText, DialogInterface.OnClickListener positiveListener, String negativeText, DialogInterface.OnClickListener negativeListener) {
        return createWarningDialog(context, title, message, positiveText, positiveListener, negativeText, negativeListener);
    }

    public static MaterialAlertDialogBuilder createListDialog(Context context, String title, CharSequence[] items, int checkedItem, DialogInterface.OnClickListener listener, String negativeText, DialogInterface.OnClickListener negativeListener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context).setTitle((CharSequence) title);
        if (checkedItem >= 0) {
            builder.setSingleChoiceItems(items, checkedItem, listener);
        } else {
            builder.setItems(items, listener);
        }
        return builder.setNegativeButton((CharSequence) negativeText, negativeListener);
    }

    public static MaterialAlertDialogBuilder createBlurListDialog(Context context, String title, CharSequence[] items, int checkedItem, DialogInterface.OnClickListener listener, String negativeText, DialogInterface.OnClickListener negativeListener) {
        return createListDialog(context, title, items, checkedItem, listener, negativeText, negativeListener);
    }

    public static MaterialAlertDialogBuilder createProgressDialog(Context context, String message) {
        return new MaterialAlertDialogBuilder(context).setMessage((CharSequence) message).setCancelable(false);
    }

    public static MaterialAlertDialogBuilder createBlurProgressDialog(Context context, String message) {
        return createProgressDialog(context, message);
    }

    private static void applyBlurEffect(MaterialAlertDialogBuilder builder) {
    }

    public static AlertDialog showWithBlurEffect(MaterialAlertDialogBuilder builder) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        AlertDialog dialog = builder.create();
        dialog.show();
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                Window window = dialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    try {
                        int flagBlurBehind = WindowManager.LayoutParams.class.getField("FLAG_BLUR_BEHIND").getInt(null);
                        params.flags |= flagBlurBehind;
                    } catch (Exception e) {
                    }
                    try {
                        Method setBlurBehindRadius = params.getClass().getMethod("setBlurBehindRadius", Integer.TYPE);
                        setBlurBehindRadius.invoke(params, 20);
                    } catch (Exception e2) {
                    }
                    try {
                        Method setBackgroundBlurRadius = window.getClass().getMethod("setBackgroundBlurRadius", Integer.TYPE);
                        setBackgroundBlurRadius.invoke(window, 80);
                    } catch (Exception e3) {
                    }
                    window.setDimAmount(0.3f);
                    window.setAttributes(params);
                }
            } catch (Exception e4) {
            }
        }
        return dialog;
    }

    public static boolean isWindowBlurSupported(Context context) throws NoSuchMethodException, SecurityException {
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                WindowManager windowManager = (WindowManager) context.getSystemService("window");
                if (windowManager != null) {
                    try {
                        Method isCrossWindowBlurEnabled = windowManager.getClass().getMethod("isCrossWindowBlurEnabled", new Class[0]);
                        return ((Boolean) isCrossWindowBlurEnabled.invoke(windowManager, new Object[0])).booleanValue();
                    } catch (Exception e) {
                        return true;
                    }
                }
            } catch (Exception e2) {
            }
        }
        return false;
    }
}
