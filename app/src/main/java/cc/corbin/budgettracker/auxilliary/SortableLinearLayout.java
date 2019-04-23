package cc.corbin.budgettracker.auxilliary;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cc.corbin.budgettracker.R;

import static android.view.DragEvent.ACTION_DRAG_ENDED;
import static android.view.DragEvent.ACTION_DRAG_ENTERED;
import static android.view.DragEvent.ACTION_DRAG_EXITED;
import static android.view.DragEvent.ACTION_DRAG_LOCATION;
import static android.view.DragEvent.ACTION_DRAG_STARTED;
import static android.view.DragEvent.ACTION_DROP;

public class SortableLinearLayout extends LinearLayout implements View.OnDragListener
{
    private final String TAG = "SortableLinearLayout";

    private Context _context;

    private TextView _currentLocationView;
    private int _dropLocation;

    private int _color;

    private boolean _sortingEnabled;

    public SortableLinearLayout(Context context)
    {
        super(context);

        init(context);
    }

    public SortableLinearLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init(context);
    }

    public SortableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context)
    {
        _context = context;

        _sortingEnabled = true;

        setOrientation(VERTICAL);

        setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);

        setOnDragListener(this);

        _color = ContextCompat.getColor(_context, R.color.insert);

        addTopView();
    }

    private void addTopView()
    {
        TextView topLocationView = new TextView(_context);
        topLocationView.setBackgroundColor(_color);
        topLocationView.setVisibility(GONE);
        addView(topLocationView);
    }

    public void setSortingEnabled(boolean sortingEnabled)
    {
        _sortingEnabled = sortingEnabled;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            View child = getChildAt(i);
            if (child instanceof SortableItem)
            {
                ((SortableItem) getChildAt(i)).setSortable(sortingEnabled);
            }
            else { }
        }
    }

    public boolean getSortingEnabled()
    {
        return _sortingEnabled;
    }

    @Override
    public boolean onDrag(View v, DragEvent event)
    {
        if (_sortingEnabled)
        {
            switch (event.getAction())
            {
                case ACTION_DRAG_STARTED:
                    break;
                case ACTION_DRAG_ENTERED:
                    updateDrag(event);
                    break;
                case ACTION_DRAG_LOCATION:
                    updateDrag(event);
                    break;
                case ACTION_DRAG_EXITED:
                    if (_currentLocationView != null)
                    {
                        _currentLocationView.setVisibility(GONE);
                    }
                    else { }
                    break;
                case ACTION_DROP:
                    View child = null;
                    View bottomDrop = null;
                    int pullLocation = -1;
                    int childCount = getChildCount();
                    for (int i = 1; i < childCount; i += 2)
                    {
                        child = getChildAt(i);
                        if (child == event.getLocalState())
                        {
                            bottomDrop = getChildAt(i + 1);
                            pullLocation = i;
                            break;
                        }
                        else { }
                    }
                    if (pullLocation != -1)
                    {
                        removeViewAt(pullLocation); // Do in this order because of reshuffling
                        removeViewAt(pullLocation); // (Bottom view)
                        if (_dropLocation > pullLocation)
                        {
                            _dropLocation -= 2;
                        }
                        else { }
                        addView(bottomDrop, _dropLocation);
                        addView(child, _dropLocation); // Do in this order because of reshuffling
                    }
                    else
                    {
                    }
                    break;
                case ACTION_DRAG_ENDED:
                    if (_currentLocationView != null)
                    {
                        _currentLocationView.setVisibility(GONE);
                    }
                    else { }
                    ((SortableItem) (event.getLocalState())).finishDrop();
                    break;
            }
        }
        else { }
        return true;
    }

    private void updateDrag(DragEvent event)
    {
        float dragY = event.getY();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            View child = getChildAt(i);
            float topY = child.getTop();
            float bottomY = child.getBottom();

            if (dragY >= topY && dragY <= bottomY)
            {
                if (i % 2 == 0)
                {
                    // Do nothing
                }
                else // Set the currentLocationView to this item
                {
                    if (_currentLocationView != null) // Hide the previous one
                    {
                        _currentLocationView.setVisibility(GONE);
                    }
                    else { }
                    float halfY = ((bottomY - topY) / 2.0f) + topY;
                    if (dragY < halfY)
                    {
                        _currentLocationView = (TextView) getChildAt(i - 1);
                        _dropLocation = i;
                    }
                    else
                    {
                        _currentLocationView = (TextView) getChildAt(i + 1);
                        _dropLocation = i + 2;
                    }
                    break;
                }
            }
            else { }
        }

        if (_currentLocationView != null)
        {
            _currentLocationView.setVisibility(VISIBLE);
        }
        else { }
    }

    public void insertSortableView(SortableItem v)
    {
        addView(v);
        v.setSortable(_sortingEnabled);
        TextView locationView = new TextView(_context);
        locationView.setVisibility(GONE);
        locationView.setBackgroundColor(_color);
        addView(locationView);
    }

    public void removeSortableView(SortableItem v)
    {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            if (v == getChildAt(i))
            {
                removeViewAt(i);
                removeViewAt(i);
                break;
            }
            else { }
        }
    }

    public void removeSortableView(int index)
    {
        index = (index*2)+1;
        removeViewAt(index);
        removeViewAt(index);
    }

    public void updateItemText(int index, String newText)
    {
        SortableItem item = ((SortableItem)getChildAt((index*2)+1));
        item.setText(newText);
    }

    public SortableItem[] getSortableItemList()
    {
        int viewCount = getChildCount();
        int count = (viewCount / 2); // Automatically subtracts one from rounding down
        SortableItem[] sortableItems = new SortableItem[count];
        int index = 0;
        for (int i = 1; i < viewCount; i += 2)
        {
            sortableItems[index++] = ((SortableItem)getChildAt(i));
        }
        return sortableItems;
    }

    public void setSortableItemList(SortableItem[] sortableItems)
    {
        removeAllViews();
        addTopView();
        int count = sortableItems.length;
        for (int i = 0; i < count; i++)
        {
            insertSortableView(sortableItems[i]);
        }
    }
}
