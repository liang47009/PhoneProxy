package com.yunfeng.tools.phoneproxy.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.yunfeng.tools.phoneproxy.R;
import com.yunfeng.tools.phoneproxy.util.Logger;

public class SettingsFragment extends Fragment implements TextWatcher {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    private EditText mPopView;
    private PopupWindow p;
    private View view;
    private float mDensityScale;
    private EditText mTextFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDensityScale = getContext().getResources().getDisplayMetrics().density;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.settings_fragment, container, false);

        if (null != view) {
            mTextFilter = view.findViewById(R.id.settings_port);
            mTextFilter.addTextChangedListener(this);
        }

        return view;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Logger.e(String.format("beforeTextChanged: s=%s, start=%d, count=%d, after=%d", s.toString(), start, count, after));
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Logger.e(String.format("onTextChanged: s=%s, start=%d, before=%d, count=%d", s.toString(), start, before, count));
    }

    @Override
    public void afterTextChanged(Editable s) {
//        showPopupWindow(s);
        Logger.e("afterTextChanged: s=" + s.toString());
    }

    private void showPopupWindow(Editable s) {
        if (p == null) {
            p = new PopupWindow(getContext());
            p.setFocusable(false);
            p.setTouchable(false);
            p.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
            p.setContentView(getTextFilterInput());
            p.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            p.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            p.setBackgroundDrawable(null);
            p.setAnimationStyle(R.style.Animation_TypingFilter);
        }
        positionPopup();
    }

    private View getTextFilterInput() {
        if (mPopView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            mPopView = (EditText) layoutInflater.inflate(R.layout.typing_filter, null);
            // For some reason setting this as the "real" input type changes
            // the text view in some way that it doesn't work, and I don't
            // want to figure out why this is.
            mPopView.setRawInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_FILTER);
            mPopView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        }
        return mPopView;
    }

    private void positionPopup() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        final int[] xy = new int[2];
        mTextFilter.getLocationOnScreen(xy);
        // TODO: The 20 below should come from the theme
        // TODO: And the gravity should be defined in the theme as well
        final int bottomGap = screenHeight - xy[1] - mTextFilter.getHeight() + (int) (mDensityScale * 20);
        if (!p.isShowing()) {
            p.showAtLocation(mPopView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, xy[0], bottomGap);
//            p.showAsDropDown(mPopView, 10, 10);
        } else {
            p.update(xy[0], bottomGap, -1, -1);
        }
    }

}
