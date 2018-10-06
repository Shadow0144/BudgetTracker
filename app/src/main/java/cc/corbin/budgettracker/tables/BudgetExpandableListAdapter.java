package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;

public class BudgetExpandableListAdapter extends BaseExpandableListAdapter
{
    private final String TAG = "BudgetExpandableListAdapter";

    private Context _context;
    private List<BudgetEntity> _budgetEntities; // Complete list
    private List<BudgetEntity> _budgets; // Per category budgets
    private Map<String, List<BudgetEntity>> _adjustments; // Adjustments to the budgets
    private int _groupSize;
    private List<BudgetTableRow> _budgetCells;
    private Map<String, List<AdjustmentTableCell>> _adjustmentCells;
    private List<Button> _addAdjustmentButtons;
    private int _year;
    private int _month;
    private ExpandableBudgetTable.timeframe _timeframe;

    public BudgetExpandableListAdapter(Context context, int year, int month, ExpandableBudgetTable.timeframe timeframe)
    {
        _context = context;
        _budgets = new ArrayList<BudgetEntity>();
        _adjustments = new HashMap<String, List<BudgetEntity>>();
        _groupSize = Categories.getCategories().length;
        _year = year;
        _month = month;
        _timeframe = timeframe;
        setupTable();
    }

    private void setupTable()
    {
        _budgetCells = new ArrayList<BudgetTableRow>();
        _adjustmentCells = new HashMap<String, List<AdjustmentTableCell>>();
        _addAdjustmentButtons = new ArrayList<Button>();
        String[] categories = Categories.getCategories();
        for (int i = 0; i < _groupSize; i++)
        {
            _budgetCells.add(new BudgetTableRow(_context, categories[i], i, _year, _month, _timeframe));
        }
    }

    public BudgetEntity setBudgets(List<BudgetEntity> budgetEntities)
    {
        _budgetEntities = budgetEntities;

        BudgetEntity totalEntity = new BudgetEntity();
        String[] categories = Categories.getCategories();
        int catCount = categories.length;

        // Values for use in the total row
        int mostRecentMonth = 0;
        int mostRecentYear = 0;
        float totalAmount = 0.0f;

        int i = 0; // Grab the categorical budgets
        for (; i < catCount; i++)
        {
            BudgetEntity entity = _budgetEntities.get(i);
            _budgetCells.get(i).setBudgetEntity(entity);
            totalAmount += entity.getAmount();
            if (entity.getYear() > mostRecentYear)
            {
                mostRecentYear = entity.getYear();
                mostRecentMonth = entity.getMonth();
            }
            else if ((entity.getYear() == mostRecentYear) && (entity.getMonth() > mostRecentMonth))
            {
                mostRecentMonth = entity.getMonth();
            }
            else { }

            // Create the buttons to use to add adjustments
            Button addAdjustmentButton = new Button(_context);
            addAdjustmentButton.setId(i);
            addAdjustmentButton.setText(R.string.add_adjustment);
            addAdjustmentButton.setMinHeight(0);
            addAdjustmentButton.setMinimumHeight(0);
            addAdjustmentButton.setIncludeFontPadding(false);
            addAdjustmentButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (_timeframe == ExpandableBudgetTable.timeframe.monthly)
                    {
                        ((MonthViewActivity)_context).createAdjustmentExpenditure(v.getId());
                    }
                    else
                    {
                        //((YearViewActivity)_context).createAdjustmentExpenditure(v.getId()); // TODO
                    }
                }
            });
            _addAdjustmentButtons.add(addAdjustmentButton);
        }

        // Grab the adjustments
        int len = _budgetEntities.size();
        for (int j = 0; j < catCount; j++)
        {
            List<BudgetEntity> adjs = new ArrayList<BudgetEntity>();
            ArrayList<AdjustmentTableCell> adjustmentTableCells = new ArrayList<AdjustmentTableCell>();
            int k = 0;
            while ((i < len) && (_budgetEntities.get(i).getCategory() == j))
            {
                final BudgetEntity entity = _budgetEntities.get(i);
                adjs.add(entity);
                AdjustmentTableCell adjustmentTableCell = new AdjustmentTableCell(_context, entity);
                adjustmentTableCell.setIndices(j, k);
                adjustmentTableCell.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        AdjustmentTableCell cell = ((AdjustmentTableCell)v);
                        BudgetEntity entity = cell.getBudgetEntity();
                        int groupIndex = cell.getGroupIndex();
                        int childIndex = cell.getChildIndex();
                        if (_timeframe == ExpandableBudgetTable.timeframe.monthly)
                        {
                            ((MonthViewActivity)_context).editAdjustmentExpenditure(entity, groupIndex, childIndex);
                        }
                        else
                        {
                            //((YearViewActivity)_context).editAdjustmentExpenditure(entity, group, child); // TODO
                        }
                    }
                });
                adjustmentTableCells.add(adjustmentTableCell);
                totalAmount += entity.getAmount();
                i++;
                k++;
            }
            _adjustments.put(categories[j], adjs);
            _adjustmentCells.put(categories[j], adjustmentTableCells);
        }

        // Setup the total
        totalEntity.setAmount(totalAmount);
        totalEntity.setYear(mostRecentYear);
        totalEntity.setMonth(mostRecentMonth);

        notifyDataSetInvalidated();

        return totalEntity;
    }

    @Override
    public int getGroupCount()
    {
        return _groupSize;
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        return _adjustments.get(Categories.getCategories()[groupPosition]).size()+1; // +1 for the add button
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return _budgets.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        Object child = null;
        List<BudgetEntity> adjustments = _adjustments.get(Categories.getCategories()[groupPosition]);
        if (adjustments != null && childPosition < adjustments.size())
        {
            child = adjustments.get(childPosition);
        }
        else { }

        return child;
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        convertView = _budgetCells.get(groupPosition);
        return  convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        List<AdjustmentTableCell> adjustmentTableCells = _adjustmentCells.get(Categories.getCategories()[groupPosition]);
        if (childPosition < adjustmentTableCells.size())
        {
            convertView = adjustmentTableCells.get(childPosition);
        }
        else
        {
            convertView = _addAdjustmentButtons.get(groupPosition);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }
}
