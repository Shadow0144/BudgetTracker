package cc.corbin.budgettracker;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.Gravity;

/**
 * Created by Corbin on 1/29/2018.
 */

public class TableCell extends AppCompatTextView
{
    private final String TAG = "TableCell";

    public final static int DEFAULT_CELL = 0;
    public final static int HEADER_CELL = 1;
    public final static int TITLE_CELL = 2;

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

    private void setup(Context context, int cellType)
    {
        setGravity(Gravity.CENTER);
        setPadding(BUFFER, BUFFER, BUFFER, BUFFER);

        switch (cellType)
        {
            case DEFAULT_CELL: // Default
                setBackground(context.getDrawable(R.drawable.cell_shape));
                setTextColor(context.getColor(R.color.black));
                break;
            case HEADER_CELL: // Header
                setBackground(context.getDrawable(R.drawable.header_cell_shape));
                setTextColor(context.getColor(R.color.white));
                break;
            case TITLE_CELL: // Title
                setBackground(context.getDrawable(R.drawable.title_cell_shape));
                setTextColor(context.getColor(R.color.white));
                break;
            default:
                setBackground(context.getDrawable(R.drawable.cell_shape));
                setTextColor(context.getColor(R.color.black));
                break;
        }
    }
}
