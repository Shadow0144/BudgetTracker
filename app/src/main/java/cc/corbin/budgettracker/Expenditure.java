package cc.corbin.budgettracker;

/**
 * Created by Corbin on 1/26/2018.
 */

public class Expenditure
{
    public int cost;
    public String type;

    public Expenditure()
    {
        this.cost = 0;
        this.type = DayViewActivity.getCategories()[0];
    }

    public Expenditure(int cost, String type)
    {
        this.cost = cost;
        this.type = type;
    }
}
