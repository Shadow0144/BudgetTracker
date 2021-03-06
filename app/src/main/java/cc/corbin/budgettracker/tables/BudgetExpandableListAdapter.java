package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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

    public BudgetExpandableListAdapter(Context context, int year, int month)
    {
        _context = context;
        _budgets = new ArrayList<BudgetEntity>();
        _adjustments = new HashMap<String, List<BudgetEntity>>();
        _groupSize = Categories.getCategories().length;
        _year = year;
        _month = month;
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
            _budgetCells.add(new BudgetTableRow(_context, categories[i], i, _year, _month));
            // Create the buttons to use to add adjustments
            Button addAdjustmentButton = new Button(_context);
            addAdjustmentButton.setTag(i);
            addAdjustmentButton.setText(R.string.add_adjustment);
            addAdjustmentButton.setMinHeight(0);
            addAdjustmentButton.setMinimumHeight(0);
            addAdjustmentButton.setIncludeFontPadding(false);
            addAdjustmentButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ((MonthViewActivity) _context).createAdjustmentExpenditure(((int) v.getTag()));
                }
            });
            _addAdjustmentButtons.add(addAdjustmentButton);
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
        }

        // Grab the adjustments
        int len = _budgetEntities.size();
        for (int j = 0; j < catCount; j++)
        {
            List<BudgetEntity> adjs = new ArrayList<BudgetEntity>();
            ArrayList<AdjustmentTableCell> adjustmentTableCells = new ArrayList<AdjustmentTableCell>();
            int k = 0;
            float additionalValue = 0.0f;
            BudgetTableRow row = _budgetCells.get(j);
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
                        AdjustmentTableCell cell = ((AdjustmentTableCell) v);
                        BudgetEntity entity = cell.getBudgetEntity();
                        int groupIndex = cell.getGroupIndex();
                        int childIndex = cell.getChildIndex();
                        ((MonthViewActivity) _context).editAdjustmentExpenditure(entity, groupIndex, childIndex);
                    }
                });
                if (k == 0) // If adding an adjustment, don't hide it
                {
                    row.addEllipsis();
                }
                else { }
                adjustmentTableCells.add(adjustmentTableCell);
                float amount = entity.getAmount();
                additionalValue += amount;
                totalAmount += amount;
                i++;
                k++;
            }
            row.addAmount(additionalValue);
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
        List<BudgetEntity> category = _adjustments.get(Categories.getCategories()[groupPosition]);
        int count = category == null ? 1 : category.size()+1; // +1 for the add button
        return count;
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return _budgetCells.get(groupPosition);
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
        else
        {
            child = _addAdjustmentButtons.get(groupPosition);
        }

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
        if (adjustmentTableCells != null && childPosition < adjustmentTableCells.size())
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
