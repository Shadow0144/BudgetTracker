package cc.corbin.budgettracker.auxilliary;

// Callback interface for class that implements a row complete function, i.e. for views with tables
public interface AsyncSummationCallback
{
    // Called when a row of calculations are complete
    void rowComplete(SummationAsyncTask.SummationType summationType,
                     String header, float amount, int id, boolean lastRow);
}
