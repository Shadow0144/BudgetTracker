package cc.corbin.budgettracker;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by Corbin on 1/29/2018.
 */

public class TableCell extends AppCompatTextView
{
    public TableCell(Context context)
    {
        super(context);
        setBackground(context.getDrawable(R.drawable.cell_shape));
    }

    public TableCell(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setBackground(context.getDrawable(R.drawable.cell_shape));
    }

    public TableCell(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setBackground(context.getDrawable(R.drawable.cell_shape));
    }
}
