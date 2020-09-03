package cc.corbin.budgettracker.auxilliary;

public interface AsyncSummationCallback
{
    void rowComplete(SummationAsyncTask.SummationType summationType,
                     String header, float amount, int id, boolean lastRow);
}
