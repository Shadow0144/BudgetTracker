package cc.corbin.budgettracker.auxilliary;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.w3c.dom.Text;

import cc.corbin.budgettracker.R;

/**
 * Created by Corbin on 1/29/2018.
 */

public class TableCell extends RelativeLayout
{
    private final String TAG = "TableCell";

    private ProgressBar _progressBar;
    private AppCompatTextView _textView;

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
        _progressBar = new ProgressBar(context);
        _textView = new AppCompatTextView(context);
        int id = 1; // Need some non-zero id set so that the progress bar sizes correctly
        _textView.setId(id);

        _progressBar.setVisibility(INVISIBLE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_TOP, _textView.getId());
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, _textView.getId());
        _progressBar.setLayoutParams(layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        _textView.setLayoutParams(layoutParams);

        _textView.setGravity(Gravity.CENTER);
        setPadding(BUFFER, BUFFER, BUFFER, BUFFER);
        _textView.setEllipsize(TextUtils.TruncateAt.END);
        _textView.setLines(1);

        switch (cellType)
        {
            case DEFAULT_CELL: // Default
                setBackground(context.getDrawable(R.drawable.cell_shape));
                _textView.setTypeface(null, Typeface.NORMAL);
                _textView.setTextColor(context.getColor(R.color.black));
                break;
            case HEADER_CELL: // Header
                setBackground(context.getDrawable(R.drawable.header_cell_shape));
                _textView.setTypeface(null, Typeface.NORMAL);
                _textView.setTextColor(context.getColor(R.color.white));
                break;
            case TITLE_CELL: // Title
                setBackground(context.getDrawable(R.drawable.title_cell_shape));
                _textView.setTypeface(null, Typeface.NORMAL);
                _textView.setTextColor(context.getColor(R.color.white));
                break;
            case TOTAL_CELL: // Total
                setBackground(context.getDrawable(R.drawable.cell_shape));
                _textView.setTypeface(null, Typeface.BOLD);
                _textView.setTextColor(context.getColor(R.color.black));
                break;
            case GRAND_TOTAL_CELL: // Grand Total
                setBackground(context.getDrawable(R.drawable.cell_shape));
                _textView.setTypeface(null, Typeface.BOLD);
                _textView.setTextColor(context.getColor(R.color.blue));
                break;
            case SPECIAL_CELL: // Special
                setBackground(context.getDrawable(R.drawable.special_cell_shape));
                _textView.setTypeface(null, Typeface.BOLD);
                _textView.setTextColor(context.getColor(R.color.black));
                break;
            case SEMI_SPECIAL_CELL: // Semi-Special
                setBackground(context.getDrawable(R.drawable.semi_special_cell_shape));
                _textView.setTypeface(null, Typeface.BOLD);
                _textView.setTextColor(context.getColor(R.color.black));
                break;
            case BOLD_CELL: // Bold
                setBackground(context.getDrawable(R.drawable.cell_shape));
                _textView.setTypeface(null, Typeface.BOLD);
                _textView.setTextColor(context.getColor(R.color.black));
                break;
            default:
                setBackground(context.getDrawable(R.drawable.cell_shape));
                _textView.setTypeface(null, Typeface.NORMAL);
                _textView.setTextColor(context.getColor(R.color.black));
                break;
        }

        addView(_textView);
        addView(_progressBar);

        invalidate();
    }

    public void setText(int resid)
    {
        _textView.setText(resid);
    }

    public void setText(CharSequence text)
    {
        _textView.setText(text);
    }

    public void setLoading(boolean loading)
    {
        if (loading)
        {
            _progressBar.setVisibility(VISIBLE);
            _textView.setVisibility(INVISIBLE);
        }
        else
        {
            _progressBar.setVisibility(INVISIBLE);
            _textView.setVisibility(VISIBLE);
        }
    }
}
