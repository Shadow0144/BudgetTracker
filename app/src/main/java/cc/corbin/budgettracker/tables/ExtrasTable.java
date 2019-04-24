package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.day.ExpenditureItem;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;

public class ExtrasTable extends LinearLayout
{
    private final String TAG = "ExtrasTable";

    private final int BORDER_HEIGHT = 4;

    private Context _context;

    private int _month;
    private int _year;

    public ExtrasTable(Context context)
    {
        super(context);

        _context = context;
        _year = 0;
        _month = 0;

        setupEmptyTable();
    }

    public ExtrasTable(Context context, int year)
    {
        super(context);

        _context = context;
        _year = year;
        _month = 0;

        setupEmptyTable();
    }

    public ExtrasTable(Context context, int year, int month)
    {
        super(context);

        _context = context;
        _year = year;
        _month = month;

        setupEmptyTable();
    }

    private void setupEmptyTable() // Note this gets replaced when the items arrive
    {
        //setColumnStretchable(0, true); // No longer a table
        setOrientation(VERTICAL);

        addSpace();
        setupTitle();
        addAddButtons();
        addSpace();
    }

    private void addSpace()
    {
        ViewGroup.LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                BORDER_HEIGHT);
        View space = new View(_context);
        space.setBackgroundColor(Color.BLACK);
        space.setLayoutParams(params);
        addView(space);
    }

    private void setupTitle()
    {
        TextView extrasTitleTextView = new TextView(_context);
        extrasTitleTextView.setBackgroundColor(_context.getColor(R.color.colorPrimaryDark));
        extrasTitleTextView.setTextColor(Color.WHITE);
        extrasTitleTextView.setTextSize(18);
        extrasTitleTextView.setPadding(0, 18, 0, 18);
        extrasTitleTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        extrasTitleTextView.setText(R.string.extras);
        addView(extrasTitleTextView);
    }

    private void addAddButtons()
    {
        Button extrasTableAddButton = new Button(_context);
        extrasTableAddButton.setText(R.string.add_item);
        extrasTableAddButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createExtraExpenditure(v);
            }
        });
        extrasTableAddButton.setMinHeight(0);
        extrasTableAddButton.setMinimumHeight(0);
        extrasTableAddButton.setIncludeFontPadding(false);
        addView(extrasTableAddButton);
    }

    public void updateExpenditures(List<ExpenditureEntity> expenditureEntities)
    {
        removeAllViews();

        addSpace();

        setupTitle();

        int selection = 0;

        int size = expenditureEntities.size();
        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = expenditureEntities.get(i);
            if (entity.getDay() == selection)
            {
                ExpenditureItem item = new ExpenditureItem(_context, entity);
                item.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        editExtraExpenditure(v);
                    }
                });
                addView(item);
            }
            else { }
        }

        addAddButtons();

        addSpace();
    }

    private void createExtraExpenditure(View v)
    {
        if (_month != 0) // Month Activity
        {
            ((MonthViewActivity)_context).createExtraExpenditure();
        }
        else
        {
            if (_year != 0) // Year Activity
            {

            }
            else // Total Activity
            {

            }
        }
    }

    private void editExtraExpenditure(View v)
    {
        ExpenditureEntity entity = ((ExpenditureEntity)v.getTag());
        if (_month != 0) // Month Activity
        {
            ((MonthViewActivity)_context).editExtraExpenditure(entity);
        }
        else
        {
            if (_year != 0) // Year Activity
            {

            }
            else // Total Activity
            {

            }
        }
    }
}
