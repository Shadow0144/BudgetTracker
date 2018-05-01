package cc.corbin.budgettracker.auxilliary;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.w3c.dom.Text;

import cc.corbin.budgettracker.R;

/**
 * Created by Corbin on 1/29/2018.
 */

public class TableCell extends AppCompatTextView
{
    private final String TAG = "TableCell";

    public final static int DEFAULT_CELL = 0;
    public final static int HEADER_CELL = 1;
    public final static int TITLE_CELL = 2;
    public final static int TOTAL_CELL = 3;
    public final static int GRAND_TOTAL_CELL = 4;
    public final static int SPECIAL_CELL = 5;
    public final static int SEMI_SPECIAL_CELL = 6;
    public final static int BOLD_CELL = 7;

    private final int BUFFER = 20;

    public TableCell(Context context)
    {
        super(context);

        setup(context, 0);
    }

    public TableCell(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TableCell,
                0, 0);
        int cellType = 0;

        try
        {
            cellType = a.getInteger(R.styleable.TableCell_cellType, 0);
        }
        catch (Exception e)  {  }
        finally
        {
            a.recycle();
        }

        setup(context, cellType);
    }

    public TableCell(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TableCell,
                0, 0);
        int cellType = 0;

        try
        {
            cellType = a.getInteger(R.styleable.TableCell_cellType, 0);
        }
        catch (Exception e)  {  }
        finally
        {
            a.recycle();
        }

        setup(context, cellType);
    }

    public TableCell(Context context, int cellType)
    {
        super(context);

        setup(context, cellType);
    }

    public void setup(Context context, int cellType)
    {
        setGravity(Gravity.CENTER);
        setPadding(BUFFER, BUFFER, BUFFER, BUFFER);
        setEllipsize(TextUtils.TruncateAt.END);
        setLines(1);

        switch (cellType)
        {
            case DEFAULT_CELL: // Default
                setBackground(context.getDrawable(R.drawable.cell_shape));
                setTypeface(null, Typeface.NORMAL);
                setTextColor(context.getColor(R.color.black));
                break;
            case HEADER_CELL: // Header
                setBackground(context.getDrawable(R.drawable.header_cell_shape));
                setTypeface(null, Typeface.NORMAL);
                setTextColor(context.getColor(R.color.white));
                break;
            case TITLE_CELL: // Title
                setBackground(context.getDrawable(R.drawable.title_cell_shape));
                setTypeface(null, Typeface.NORMAL);
                setTextColor(context.getColor(R.color.white));
                break;
            case TOTAL_CELL: // Total
                setBackground(context.getDrawable(R.drawable.cell_shape));
                setTypeface(null, Typeface.BOLD);
                setTextColor(context.getColor(R.color.black));
                break;
            case GRAND_TOTAL_CELL: // Grand Total
                setBackground(context.getDrawable(R.drawable.cell_shape));
                setTypeface(null, Typeface.BOLD);
                setTextColor(context.getColor(R.color.blue));
                break;
            case SPECIAL_CELL: // Special
                setBackground(context.getDrawable(R.drawable.special_cell_shape));
                setTypeface(null, Typeface.BOLD);
                setTextColor(context.getColor(R.color.black));
                break;
            case SEMI_SPECIAL_CELL: // Semi-Special
                setBackground(context.getDrawable(R.drawable.semi_special_cell_shape));
                setTypeface(null, Typeface.BOLD);
                setTextColor(context.getColor(R.color.black));
                break;
            case BOLD_CELL: // Bold
                setBackground(context.getDrawable(R.drawable.cell_shape));
                setTypeface(null, Typeface.BOLD);
                setTextColor(context.getColor(R.color.black));
                break;
            default:
                setBackground(context.getDrawable(R.drawable.cell_shape));
                setTypeface(null, Typeface.NORMAL);
                setTextColor(context.getColor(R.color.black));
                break;
        }
        invalidate();
    }
}
