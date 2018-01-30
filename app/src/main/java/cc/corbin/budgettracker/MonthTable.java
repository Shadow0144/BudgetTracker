package cc.corbin.budgettracker;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * Created by Corbin on 1/29/2018.
 */

public class MonthTable extends TableLayout
{
    private final String TAG = "MonthTable";

    private Context _context;

    private int _month;
    private int _year;

    public MonthTable(Context context)
    {
        super(context);
        _context = context;

        _month = 1;
        _year = 2018;

        setup();
    }

    public MonthTable(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MonthTable,
                0, 0);

        try
        {
            _month = a.getInteger(R.styleable.MonthTable_month, 1);
            _year = a.getInteger(R.styleable.MonthTable_year, 2018);
        }
        catch (Exception e)
        {
            _month = 1;
            _year = 2018;
        }
        finally
        {
            a.recycle();
        }

        setup();
    }

    public MonthTable(Context context, int month, int year)
    {
        super(context);
        _context = context;

        _month = month;
        _year = year;

        setup();
    }

    private void setup()
    {
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        String[] categories = DayViewActivity.getCategories();
        int rows = categories.length;

        TableRow titleRow = new TableRow(_context);
        TableCell titleCell = new TableCell(_context, TableCell.TITLE_CELL);
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.span = 6;
        titleCell.setLayoutParams(params);

        // Set the text
        DateFormatSymbols dfs = new DateFormatSymbols();
        titleCell.setText(dfs.getShortMonths()[_month] + " " + _year);

        titleRow.addView(titleCell);
        addView(titleRow);

        TableRow headerRow = new TableRow(_context);
        TableCell week0 = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell week1 = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell week2 = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell week3 = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell week4 = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell week5 = new TableCell(_context, TableCell.HEADER_CELL);

        // Set the text
        week0.setText("");
        week1.setText("Week 1");
        week2.setText("Week 2");
        week3.setText("Week 3");
        week4.setText("Week 4");
        week5.setText("Week 5");

        headerRow.addView(week0);
        headerRow.addView(week1);
        headerRow.addView(week2);
        headerRow.addView(week3);
        headerRow.addView(week4);
        headerRow.addView(week5);
        addView(headerRow);


        for (int i = 0; i < rows; i++)
        {
            TableRow categoryRow = new TableRow(_context);
            week0 = new TableCell(_context, TableCell.HEADER_CELL);
            week1 = new TableCell(_context, TableCell.DEFAULT_CELL);
            week2 = new TableCell(_context, TableCell.DEFAULT_CELL);
            week3 = new TableCell(_context, TableCell.DEFAULT_CELL);
            week4 = new TableCell(_context, TableCell.DEFAULT_CELL);
            week5 = new TableCell(_context, TableCell.DEFAULT_CELL);

            // Set the text
            week0.setText(DayViewActivity.getCategories()[i]);

            categoryRow.addView(week0);
            categoryRow.addView(week1);
            categoryRow.addView(week2);
            categoryRow.addView(week3);
            categoryRow.addView(week4);
            categoryRow.addView(week5);
            addView(categoryRow);
        }
    }
}
