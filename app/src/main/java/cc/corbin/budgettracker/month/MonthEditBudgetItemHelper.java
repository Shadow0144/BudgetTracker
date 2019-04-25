package cc.corbin.budgettracker.month;

import android.arch.lifecycle.MutableLiveData;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.numericalformatting.NumericalFormattedEditText;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

public class MonthEditBudgetItemHelper
{
    private final String TAG = "MonthEditBudgetItemHelper";

    private MonthViewActivity _parent;
    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<BudgetEntity>> _budgets; // Changes will be posted
    private int _budgetId; // The budget entity being edited

    private int _year;
    private int _month;

    private PopupWindow _popupWindow; // For editing budgets
    private NumericalFormattedEditText _amountEditText;

    public MonthEditBudgetItemHelper(MonthViewActivity parent, MutableLiveData<List<BudgetEntity>> budgets,
                                     int budgetId, int year, int month, ExpenditureViewModel viewModel)
    {
        _parent = parent;
        _viewModel = viewModel;
        _budgets = budgets;
        _budgetId = budgetId;
        _year = year;
        _month = month;

        BudgetEntity budgetEntity = _budgets.getValue().get(_budgetId);

        final View budgetEditView = _parent.getLayoutInflater().inflate(R.layout.popup_set_budget, null);

        final TextView categoryTextView = budgetEditView.findViewById(R.id.categoryTextView);
        categoryTextView.setText(budgetEntity.getCategoryName() + ": ");

        final TextView currencyTextView = budgetEditView.findViewById(R.id.currencyTextView);
        currencyTextView.setText(Currencies.symbols[Currencies.default_currency]);

        _amountEditText = budgetEditView.findViewById(R.id.amountEditText);
        if (budgetEntity.getId() != 0)
        {
            _amountEditText.setup(null, Currencies.default_currency, budgetEntity.getAmount());
            if (budgetEntity.getMonth() == _month && budgetEntity.getYear() == _year) // If the ID is not 0
            {
                final Button removeButton = budgetEditView.findViewById(R.id.removeButton);
                removeButton.setEnabled(true);
            }
            else { }
        }
        else
        {
            _amountEditText.setup(null);
        }

        _popupWindow = new PopupWindow(budgetEditView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(_parent.findViewById(R.id.rootLayout), Gravity.CENTER, 0, 0);
    }

    public void confirmBudgetItemEdit(View v)
    {
        float amount = _amountEditText.getAmount();
        BudgetEntity budgetEntity = _budgets.getValue().get(_budgetId);
        budgetEntity.setAmount(amount);

        if (budgetEntity.getMonth() == _month && budgetEntity.getYear() == _year) // Edit
        {
            budgetEntity.setAmount(amount);
            _viewModel.updateBudgetEntity(budgetEntity);
        }
        else // Add
        {
            budgetEntity.setId(0);
            budgetEntity.setMonth(_month);
            budgetEntity.setYear(_year);
            _viewModel.insertBudgetEntity(budgetEntity, _year, _month);
        }

        _viewModel.getMonthBudget(_budgets, _year, _month); // Update the budget tables etc with the correct values (including from previous months)

        _popupWindow.dismiss();
    }

    public void cancelBudgetItemEdit(View v)
    {
        _popupWindow.dismiss();
    }

    public void removeBudgeItem(View v)
    {
        BudgetEntity budgetEntity = _budgets.getValue().get(_budgetId);
        _viewModel.removeBudgetEntity(budgetEntity);
        _viewModel.getMonthBudget(_budgets, _year, _month); // Update the budget tables etc with the correct values (including from previous months)

        _popupWindow.dismiss();
    }

    public int getBudgetId()
    {
        return _budgetId;
    }
}
