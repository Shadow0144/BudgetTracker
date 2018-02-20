package cc.corbin.budgettracker;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Corbin on 1/29/2018.
 */

public class MonthTable extends TableLayout implements View.OnClickListener
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

    public void onClick(View v)
    {
        Intent intent = new Intent(_context, DayViewActivity.class);
        Calendar date = Calendar.getInstance();
        int week = v.getId();
        date.set(_year, _month-1, ((week-1)*7) + 1);
        intent.putExtra(DayViewActivity.DATE_INTENT, date.getTimeInMillis());
        _context.startActivity(intent);
        ((MonthViewActivity)_context).finish();
    }

    private void setup()
    {
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)); // TODO
        setStretchAllColumns(true);
        setColumnShrinkable(0, true);

        String[] categories = DayViewActivity.getCategories();
        int rows = categories.length;

        Calendar c = Calendar.getInstance();
        c.set(_year, _month-1, 1);
        int maxDays = c.getActualMaximum(Calendar.DATE);

        TableRow titleRow = new TableRow(_context);
        TableCell titleCell = new TableCell(_context, TableCell.TITLE_CELL);
        titleCell.setText(R.string.month_title);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.span = 7;
        titleCell.setLayoutParams(params);
        titleRow.addView(titleCell);
        addView(titleRow);

        TableRow headerRow = new TableRow(_context);
        TableCell week0 = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell week1 = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell week2 = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell week3 = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell week4 = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell week5 = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell week6 = new TableCell(_context, TableCell.HEADER_CELL);

        // Set the text
        week0.setText(Currencies.symbols[Currencies.default_currency]);
        week1.setText("Week 1");
        week2.setText("Week 2");
        week3.setText("Week 3");
        week4.setText("Week 4");
        week5.setText("Week 5");
        week6.setText("Total");

        int id = 0;
        week1.setId(++id);
        week2.setId(++id);
        week3.setId(++id);
        week4.setId(++id);
        week5.setId(++id);

        week1.setOnClickListener(this);
        week2.setOnClickListener(this);
        week3.setOnClickListener(this);
        week4.setOnClickListener(this);
        if (maxDays > (4*7)) // Only occurs on some Februaries
        {
            week5.setOnClickListener(this);
        }
        else { }

        headerRow.addView(week0);
        headerRow.addView(week1);
        headerRow.addView(week2);
        headerRow.addView(week3);
        headerRow.addView(week4);
        headerRow.addView(week5);
        headerRow.addView(week6);
        addView(headerRow);

        float week1Total = 0.0f;
        float week2Total = 0.0f;
        float week3Total = 0.0f;
        float week4Total = 0.0f;
        float week5Total = 0.0f;
        float monthTotal = 0.0f;
        for (int i = 0; i < rows; i++)
        {
            TableRow categoryRow = new TableRow(_context);
            week0 = new TableCell(_context, TableCell.HEADER_CELL);
            week1 = new TableCell(_context, TableCell.DEFAULT_CELL);
            week2 = new TableCell(_context, TableCell.DEFAULT_CELL);
            week3 = new TableCell(_context, TableCell.DEFAULT_CELL);
            week4 = new TableCell(_context, TableCell.DEFAULT_CELL);
            week5 = new TableCell(_context, TableCell.DEFAULT_CELL);
            week6 = new TableCell(_context, TableCell.TOTAL_CELL);

            // Set the text
            String category = DayViewActivity.getCategories()[i];
            ExpenditureDatabase db = ExpenditureDatabase.getExpenditureDatabase(getContext());

            float week1V = getWeekTotal(db, 1, category);
            float week2V = getWeekTotal(db, 2, category);
            float week3V = getWeekTotal(db, 3, category);
            float week4V = getWeekTotal(db, 4, category);
            float week5V = getWeekTotal(db, 5, category);
            float categoryTotal = week1V + week2V + week3V + week4V + week5V;

            week1Total += week1V;
            week2Total += week2V;
            week3Total += week3V;
            week4Total += week4V;
            week5Total += week5V;
            monthTotal += categoryTotal;

            week0.setText(category);
            if (Currencies.integer[Currencies.default_currency])
            {
                week1.setText(String.format("%.00f", week1V));
                week2.setText(String.format("%.00f", week2V));
                week3.setText(String.format("%.00f", week3V));
                week4.setText(String.format("%.00f", week4V));
                week5.setText(String.format("%.00f", week5V));
                week6.setText(String.format("%.00f", categoryTotal));
            }
            else
            {
                week1.setText(String.format("%.02f", week1V));
                week2.setText(String.format("%.02f", week2V));
                week3.setText(String.format("%.02f", week3V));
                week4.setText(String.format("%.02f", week4V));
                week5.setText(String.format("%.02f", week5V));
                week6.setText(String.format("%.02f", categoryTotal));
            }

            categoryRow.addView(week0);
            categoryRow.addView(week1);
            categoryRow.addView(week2);
            categoryRow.addView(week3);
            categoryRow.addView(week4);
            categoryRow.addView(week5);
            categoryRow.addView(week6);
            addView(categoryRow);
        }

        // Add the final totals row
        TableRow totalRow = new TableRow(_context);
        week0 = new TableCell(_context, TableCell.HEADER_CELL);
        week1 = new TableCell(_context, TableCell.TOTAL_CELL);
        week2 = new TableCell(_context, TableCell.TOTAL_CELL);
        week3 = new TableCell(_context, TableCell.TOTAL_CELL);
        week4 = new TableCell(_context, TableCell.TOTAL_CELL);
        week5 = new TableCell(_context, TableCell.TOTAL_CELL);
        week6 = new TableCell(_context, TableCell.GRAND_TOTAL_CELL);

        week0.setText(R.string.total);
        if (Currencies.integer[Currencies.default_currency])
        {
            week1.setText(String.format("%.00f", week1Total));
            week2.setText(String.format("%.00f", week2Total));
            week3.setText(String.format("%.00f", week3Total));
            week4.setText(String.format("%.00f", week4Total));
            week5.setText(String.format("%.00f", week5Total));
            week6.setText(String.format("%.00f", monthTotal));
        }
        else
        {
            week1.setText(String.format("%.02f", week1Total));
            week2.setText(String.format("%.02f", week2Total));
            week3.setText(String.format("%.02f", week3Total));
            week4.setText(String.format("%.02f", week4Total));
            week5.setText(String.format("%.02f", week5Total));
            week6.setText(String.format("%.02f", monthTotal));
        }

        totalRow.addView(week0);
        totalRow.addView(week1);
        totalRow.addView(week2);
        totalRow.addView(week3);
        totalRow.addView(week4);
        totalRow.addView(week5);
        totalRow.addView(week6);
        addView(totalRow);
    }

    private float getWeekTotal(ExpenditureDatabase db, int weekNum, String category)
    {
        weekNum--; // Saves some subtracting later

        // Set the calendar to the correct month for getting the number of days
        Calendar c = Calendar.getInstance();
        c.set(_year, _month-1, 1);
        int maxDays = c.getActualMaximum(Calendar.DATE);

        float total = 0.0f;
        if (maxDays >= ((weekNum*7)+1))
        {
            c.set(_year, _month-1, (weekNum*7)+1, 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            long sDate = c.getTimeInMillis();
            c.set(_year, _month-1, Math.min(((weekNum+1)*7)+1, maxDays), 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);

            long eDate = c.getTimeInMillis();
            List<ExpenditureEntity> weekExp = db.expenditureDao().getWeek(sDate, eDate, category);

            int count = weekExp.size();
            for (int i = 0; i < count; i++)
            {
                total += weekExp.get(i).getAmount();
            }
        }
        else { } // Return 0

        return total;
    }
}
