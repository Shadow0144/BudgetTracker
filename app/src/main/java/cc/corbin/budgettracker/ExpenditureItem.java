package cc.corbin.budgettracker;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ExpenditureItem extends LinearLayout
{
    private ExpenditureEntity _entity;

    public ExpenditureItem(Context context)
    {
        super(context);

        setup(context, null);
    }

    public ExpenditureItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        setup(context, null);
    }

    public ExpenditureItem(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        setup(context, null);
    }

    public ExpenditureItem(Context context, ExpenditureEntity entity)
    {
        super(context);

        setup(context, entity);
    }

    private void setup(Context context, ExpenditureEntity entity)
    {
        _entity = entity;
    }
}
