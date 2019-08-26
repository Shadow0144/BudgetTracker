package cc.corbin.budgettracker.workerthread.combinedevent;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;

public class ComExportDatabasesEvent implements ComDatabaseEvent
{
    private final String TAG = "ComExportDatabasesEvent";

    private MutableLiveData<Boolean> _complete;
    private String _whereQuery;
    private String _dstFolder;
    private String _srcExpFileName;
    private String _dstExpFileName;
    private String _srcBudFileName;
    private String _dstBudFileName;
    private boolean _exportBudgets;

    public ComExportDatabasesEvent(MutableLiveData<Boolean> complete,
                                   String whereQuery, String dstFolder,
                                   String srcExpFileName, String dstExpFileName,
                                   String srcBudFileName, String dstBudFileName,
                                   boolean exportBudgets)
    {
        _complete = complete;
        _complete.setValue(false);
        _whereQuery = whereQuery;
        _dstFolder = dstFolder;
        _srcExpFileName = srcExpFileName;
        _dstExpFileName = dstExpFileName;
        _srcBudFileName = srcBudFileName;
        _dstBudFileName = dstBudFileName;
        _exportBudgets = exportBudgets;
    }

    @Override
    public void processEvent(BudgetDatabase dbB, ExpenditureDatabase dbE)
    {
        ExpenditureDatabase.createDatabaseFile(_srcExpFileName, _dstFolder, _dstExpFileName, _whereQuery);
        if (_exportBudgets)
        {
            BudgetDatabase.createDatabaseFile(_srcBudFileName, _dstFolder, _dstBudFileName, _whereQuery);
        }
        else { }
        _complete.postValue(true);
    }
}