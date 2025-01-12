package moe.ono.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.tencent.widget.ScrollView;

public class NonScrollingScrollView extends ScrollView {

    public NonScrollingScrollView(Context context) {
        super(context);
    }

    public NonScrollingScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonScrollingScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}