package com.compose.keyboard;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.KeyboardUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author:BYZ
 * @Time:2023/10/17 17:07
 * @blame Android Team
 * @info
 */
public class KeyBoardUtil {
    public static int INPUT_CAR_NUMBER_TYPE = 0; //车牌颜色
    public static int INPUT_COLOR_TYPE = 1; //输入车牌
    private int INPUT_TYPE = INPUT_COLOR_TYPE;

    /**
     * 显示键盘的视图
     */
    private Activity mActivity;
    /**
     * 键盘视图
     */
    private KeyboardView mKeyboardView;
    /**
     * 键盘
     */
    private Keyboard mAbcKeyboard;
    private Keyboard mChineseKeyboard;
    private Keyboard mColorKeyboard;
    /**
     * 输入框
     */
    private EditText mEditText;
    /**
     * 键盘布局
     */
    private View mViewContainer;

    /**
     * 焦点改变监听
     */
    View.OnFocusChangeListener mOnFocusChangeListener = (view, hasFocus) -> {
        if (hasFocus)
            showSoftKeyboard();
        else
            hideSoftKeyboard();
    };

    /**
     * 构造方法
     *
     * @param activity 根视图
     */
    public KeyBoardUtil(Activity activity) {
        this.mActivity = activity;
        this.mAbcKeyboard = new Keyboard(mActivity, R.xml.symbols_abc);
        this.mChineseKeyboard = new Keyboard(mActivity, R.xml.symbols_symbol);
        this.mColorKeyboard = new Keyboard(mActivity, R.xml.symbols_color);
    }

    Dialog mDialog;

    public KeyBoardUtil(Dialog dialog) {
        this.mDialog = dialog;
        this.mActivity = dialog.getOwnerActivity();
        this.mAbcKeyboard = new Keyboard(mActivity, R.xml.symbols_abc);
        this.mChineseKeyboard = new Keyboard(mActivity, R.xml.symbols_symbol);
        this.mColorKeyboard = new Keyboard(mActivity, R.xml.symbols_color);
    }

    /**
     * 绑定输入框
     *
     * @param editText 输入框
     * @param isAuto   是否自动显示
     *  @param inputType 键盘类型
     */
    public void attachTo(EditText editText, boolean isAuto, int inputType) {
        this.mEditText = editText;
        INPUT_TYPE = inputType;
        this.mEditText.setOnClickListener(mClickListener);
//        KeyboardUtils.hideSoftInput(editText);//隐藏系统键盘
        hideSystemSoftKeyboard(this.mEditText);
        setAutoShowOnFocus(isAuto);
    }

    /**
     * 隐藏系统软件盘
     *
     * @param editText 输入框
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void hideSystemSoftKeyboard(EditText editText) {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt < 11) {
            editText.setInputType(InputType.TYPE_NULL);
        } else {
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(editText, false);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 焦点时自动显示
     *
     * @param enabled 是否显示
     */
    private void setAutoShowOnFocus(boolean enabled) {
        if (null == mEditText) return;
        if (enabled) mEditText.setOnFocusChangeListener(mOnFocusChangeListener);
        else mEditText.setOnFocusChangeListener(null);
    }

    /**
     * 显示软键盘
     */
    public void showSoftKeyboard() {
        if (null == mViewContainer) {
            mViewContainer = mActivity.getLayoutInflater().inflate(R.layout.layout_view, null);
        } else {
            if (null != mViewContainer.getParent()) {
                return;
            }
        }
        if(KeyboardUtils.isSoftInputVisible(mActivity)){
           return;
        }

        FrameLayout frameLayout;
        if (mDialog != null) {
            frameLayout = (FrameLayout) mDialog.getWindow().getDecorView();
        } else {
            frameLayout = (FrameLayout) mActivity.getWindow().getDecorView();
        }

        KeyboardView keyboardView = mViewContainer.findViewById(R.id.keyboard_view);
        TextView tv_default_input = mViewContainer.findViewById(R.id.tv_default_input);
        tv_default_input.setOnClickListener(mClickListener);
        this.mKeyboardView = keyboardView;
        if (INPUT_TYPE == INPUT_COLOR_TYPE) {
            this.mKeyboardView.setKeyboard(mColorKeyboard);
        } else if (INPUT_TYPE == INPUT_CAR_NUMBER_TYPE) {
            this.mKeyboardView.setKeyboard(mChineseKeyboard);
        }
        this.mKeyboardView.setEnabled(true);
        this.mKeyboardView.setPreviewEnabled(false);
        this.mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        frameLayout.addView(mViewContainer, layoutParams);
//        mViewContainer.setAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.anim_down_to_up));
    }

    /**
     * 隐藏软键盘
     */
    public void hideSoftKeyboard() {
        if (null != mViewContainer && null != mViewContainer.getParent()) {
//            mViewContainer.setAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.anim_up_to_down));
            ((ViewGroup) mViewContainer.getParent()).removeView(mViewContainer);
        }
    }

    /**
     * 判断是否显示
     *
     * @return true, 显示; false, 不显示
     */
    public boolean isShowing() {
        if (null == mViewContainer) return false;
        return mViewContainer.getVisibility() == View.VISIBLE;
    }

    KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void onPress(int i) {
        }

        @Override
        public void onRelease(int i) {
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            if (null != mEditText) keyCode(primaryCode, mEditText);
            mKeyboardView.postInvalidate();
        }

        @Override
        public void onText(CharSequence charSequence) {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void swipeUp() {
        }
    };

    /**
     * 字符
     *
     * @param primaryCode 主要字符
     * @param editText    编辑框
     */
    private void keyCode(int primaryCode, EditText editText) {
        Editable editable = editText.getText();
        int start = editText.getSelectionStart();
        if (primaryCode == Keyboard.KEYCODE_DELETE) { // 回退
            if (editText.hasFocus()) {
                if (!TextUtils.isEmpty(editable)) {
                    if (start > 0) editable.delete(start - 1, start);
                }
            }
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) { // 大小写切换
            mKeyboardView.setKeyboard(mAbcKeyboard);
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            hideSoftKeyboard();
        } else if (primaryCode == 789789) {
            mKeyboardView.setKeyboard(mChineseKeyboard);
        } else if (primaryCode == 456456) {
            mKeyboardView.setKeyboard(mAbcKeyboard);
        } else {
            if (editText.hasFocus())
                editable.insert(start, inputSpecial(primaryCode));
        }
    }


    /**
     * 一个字以上的时候需要自定义code
     *
     * @param primaryCode
     * @return
     */
    private String inputSpecial(int primaryCode) {
        Log.e("metaRTC", "" + primaryCode);
        switch (primaryCode) {
            case -100:
                return "应急";
            case -200:
                return "WJ";
            case -300:
                return "清障";
            case -400:
                return "渐变绿";
            default:
                return Character.toString((char) primaryCode);
        }
    }


    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.tv_default_input){
                if(mListener != null){
                    hideSoftKeyboard();
                    mListener.onOpenKeyBoard();
                }
            }else {
                if (mEditText.isFocusable()) {
                    showSoftKeyboard();
                }
            }
        }
    };

    private OnDefaultKeyBoardListener mListener;

    public void setDefaultKeyBoardListener(OnDefaultKeyBoardListener mListener) {
        this.mListener = mListener;
    }

    public interface OnDefaultKeyBoardListener{
       void onOpenKeyBoard();
   }
}