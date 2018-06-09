package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.day.ExpenditureEditActivity;
import cc.corbin.budgettracker.day.ExpenditureItem;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;

public class ExtrasTable extends TableLayout
{
    private Context _context;

    private int _month;
    private int _year;

    private List<ExpenditureEntity> _extras;

    public ExtrasTable(Context context)
    {
        super(context);

        _context = context;
        _year = 0;
        _month = 0;

        setupTable();
    }

    public ExtrasTable(Context context, int year)
    {
        super(context);

        _context = context;
        _year = year;
        _month = 0;

        setupTable();
    }

    public ExtrasTable(Context context, int year, int month)
    {
        super(context);

        _context = context;
        _year = year;
        _month = month;

        setupTable();
    }

    private void setupTable()
    {
        setColumnStretchable(0, true);

        setupTitle();
        addAddButtons();
    }

    private void setupTitle()
    {
        TableRow extrasTableRow = new TableRow(_context);
        TableCell extrasTableCell = new TableCell(_context, TableCell.TITLE_CELL);

        extrasTableCell.setText(R.string.extras);

        extrasTableRow.addView(extrasTableCell);
        addView(extrasTableRow);
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

        TableRow extrasTableRow = new TableRow(_context);
        extrasTableRow.addView(extrasTableAddButton);
        addView(extrasTableRow);
    }

    public void updateExpenditures(List<ExpenditureEntity> expenditureEntities)
    {
        removeAllViews();

        _extras = new ArrayList<ExpenditureEntity>();

        setupTitle();

        int selection = 0;

        int size = expenditureEntities.size();
        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = expenditureEntities.get(i);
            if (entity.getDay() == selection)
            {
                ExpenditureItem item = new ExpenditureItem(_context, entity);
                item.setId(i+1); // Need to add 1
                _extras.add(entity);
                item.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        editExtraExpenditure(v);
                    }
                });
                TableRow extrasTableRow = new TableRow(_context);
                extrasTableRow.addView(item);
                addView(extrasTableRow);
            }
            else { }
        }

        addAddButtons();
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
        int index = v.getId();
        index--;
        ExpenditureEntity entity = _extras.get(index);

        if (_month != 0) // Month Activity
        {
            ((MonthViewActivity)_context).editExtraExpenditure(entity, index);
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
