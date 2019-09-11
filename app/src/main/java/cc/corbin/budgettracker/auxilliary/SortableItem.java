package cc.corbin.budgettracker.auxilliary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SortableItem extends RelativeLayout
{
    private final String TAG = "SortableItem";

    private Context _context;

    private ImageView _sortButton;
    private TextView _textView;

    private boolean _sortable;
    private boolean _sortEnabled;
    private final int SORT_DISABLED_ALPHA = 25;
    private final int SORT_ENABLED_ALPHA = 255;

    public SortableItem(Context context)
    {
        super(context);

        init(context, true);
    }

    public SortableItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init(context, true);
    }

    public SortableItem(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        init(context, true);
    }

    public SortableItem(Context context, boolean sortable)
    {
        super(context);

        init(context, sortable);
    }

    private void init(Context context, boolean sortable)
    {
        _context = context;

        _sortEnabled = true;

        setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        _sortButton = new ImageView(_context);
        setSortable(sortable);

        _textView = new TextView(_context);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        _textView.setGravity(Gravity.CENTER);
        _textView.setLayoutParams(params);

        addView(_textView);
        addView(_sortButton);

        _sortButton.setTag(this);

        _sortButton.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                boolean handled = false;

                if (!_sortEnabled || !_sortable)
                {
                    handled = true;
                }
                else if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    SortableItem parent = (SortableItem) v.getTag();
                    v.setVisibility(INVISIBLE);
                    View.DragShadowBuilder shadow = new View.DragShadowBuilder(parent);
                    parent.startDragAndDrop(null, shadow, parent, 0);
                    parent.setVisibility(GONE);

                    handled = true;
                }
                else { }

                return handled;
            }
        });
    }

    public void setText(CharSequence text)
    {
        _textView.setText(text);
    }

    public void setText(int resid)
    {
        _textView.setText(resid);
    }

    public CharSequence getText()
    {
        return _textView.getText();
    }

    public void finishDrop()
    {
        setVisibility(VISIBLE);
        _sortButton.setVisibility(VISIBLE);
    }

    public void setSortable(boolean sortable)
    {
        _sortable = sortable;
        if (_sortable)
        {
            _sortButton.setImageResource(android.R.drawable.ic_media_play);
        }
        else
        {
            _sortButton.setImageResource(android.R.drawable.ic_secure);
        }
    }

    public boolean getSortable()
    {
        return _sortable;
    }

    public void setSortEnabled(boolean sortEnabled)
    {
        _sortEnabled = sortEnabled;
        if (_sortEnabled)
        {
            _sortButton.setImageAlpha(SORT_ENABLED_ALPHA);
        }
        else
        {
            _sortButton.setImageAlpha(SORT_DISABLED_ALPHA);
        }
    }

    public boolean getSortEnabled()
    {
        return _sortEnabled;
    }
}
