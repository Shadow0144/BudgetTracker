package cc.corbin.budgettracker.setup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

// Gives callbacks to switch the page via Buttons etc. or to disable swiping via gesture
public class EnhancedViewPager extends ViewPager
{
    private final String TAG = "EnhancedViewPager";

    private boolean _fingerSwipingEnabled;

    public EnhancedViewPager(@NonNull Context context)
    {
        super(context);
        _fingerSwipingEnabled = true;
    }

    public EnhancedViewPager(@NonNull Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        _fingerSwipingEnabled = true;
    }

    public void setFingerSwipingEnabled(boolean fingerSwipingEnabled)
    {
        _fingerSwipingEnabled = fingerSwipingEnabled;
    }

    public boolean getFingerSwipingEnabled()
    {
        return _fingerSwipingEnabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        boolean handled;
        if (_fingerSwipingEnabled)
        {
            handled = super.onTouchEvent(event);
        }
        else
        {
            handled = false;
        }
        return handled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        boolean handled;
        if (_fingerSwipingEnabled)
        {
            handled = super.onInterceptTouchEvent(event);
        }
        else
        {
            handled = false;
        }
        return handled;
    }

    public void previousPage()
    {
        setCurrentItem(getCurrentItem() - 1);
    }

    public void nextPage()
    {
        setCurrentItem(getCurrentItem() + 1);
    }
}
