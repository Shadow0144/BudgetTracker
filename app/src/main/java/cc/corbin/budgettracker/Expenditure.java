package cc.corbin.budgettracker;

/**
 * Created by Corbin on 1/26/2018.
 */

public class Expenditure
{
    public int cost;
    public String category;

    public Expenditure()
    {
        this.cost = 0;
        this.category = DayViewActivity.getCategories()[DayViewActivity.getCategories().length-1];
    }

    public Expenditure(int cost, String category)
    {
        this.cost = cost;
        this.category = category;
    }
}
