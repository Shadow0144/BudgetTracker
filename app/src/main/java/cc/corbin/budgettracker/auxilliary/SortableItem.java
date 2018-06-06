package cc.corbin.budgettracker.auxilliary;

import android.content.ClipData;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cc.corbin.budgettracker.R;

public class SortableItem extends RelativeLayout
{
    private final String TAG = "SortableItem";

    private Context _context;

    private ImageView _sortButton;
    private TextView _textView;

    public SortableItem(Context context)
    {
        super(context);

        init(context);
    }

    public SortableItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init(context);
    }

    public SortableItem(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context)
    {
        _context = context;

        setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        _sortButton = new ImageView(_context);
        _sortButton.setImageResource(android.R.drawable.ic_media_play);

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

                if (event.getAction() == MotionEvent.ACTION_DOWN)
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
}
