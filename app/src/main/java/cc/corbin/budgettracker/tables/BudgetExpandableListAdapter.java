package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudgetExpandableListAdapter extends BaseExpandableListAdapter
{
    private final String TAG = "BudgetExpandableListAdapter";

    private Context _context;
    private List<BudgetEntity> _budgetEntities; // Complete list
    private List<BudgetEntity> _budgets; // Per category budgets
    private Map<String, List<BudgetEntity>> _adjustments; // Adjustments to the budgets
    private int _groupSize;
    private List<BudgetTableRow> _budgetCells;
    private Map<String, List<BudgetTableRow>> _adjustmentCells;
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
        }

        // Grab the adjustments
        int len = _budgetEntities.size();
        for (int j = 0; j < catCount; j++)
        {
            List<BudgetEntity> adjs = new ArrayList<BudgetEntity>();
            while (i < len && _budgetEntities.get(i).getCategoryName().equals(categories[j]))
            {
                BudgetEntity entity = _budgetEntities.get(i);
                adjs.add(entity);
                totalAmount += entity.getAmount();
                i++;
            }
            _adjustments.put(categories[j], adjs);
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
        return _adjustments.get(Categories.getCategories()[groupPosition]).size();
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return _budgets.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return _adjustments.get(Categories.getCategories()[groupPosition]).get(childPosition);
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
        convertView = new AdjustmentTableCell(_context, ((BudgetEntity) getChild(groupPosition, childPosition)));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }
}
