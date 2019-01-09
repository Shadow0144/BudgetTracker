package cc.corbin.budgettracker.auxilliary;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

public class EnhancedRadioGroup extends RadioGroup
{
    private ArrayList<View> _checkables = new ArrayList<View>();

    public EnhancedRadioGroup(Context context)
    {
        super(context);
    }

    public EnhancedRadioGroup(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public EnhancedRadioGroup(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs); // No super with defStyle
    }

    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params)
    {
        super.addView(child, index, params);
        parseChild(child);
    }

    public void parseChild(final View child)
    {
        if(child instanceof Checkable)
        {
            _checkables.add(child);
            child.setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    for(int i = 0; i < _checkables.size(); i++)
                    {
                        Checkable view = (Checkable) _checkables.get(i);
                        if(view == v)
                        {
                            view.setChecked(true);
                        }
                        else
                        {
                            view.setChecked(false);
                        }
                    }
                }
            });
        }
        else if(child instanceof ViewGroup)
        {
            parseChildren((ViewGroup)child);
        }
        else { }
    }

    public void parseChildren(final ViewGroup child)
    {
        for (int i = 0; i < child.getChildCount(); i++)
        {
            parseChild(child.getChildAt(i));
        }
    }
}