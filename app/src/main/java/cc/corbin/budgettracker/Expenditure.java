package cc.corbin.budgettracker;

/**
 * Created by Corbin on 1/26/2018.
 */

public class Expenditure
{
    public float cost;
    public String category;

    public Expenditure()
    {
        this.cost = 0.0f;
        this.category = DayViewActivity.getCategories()[DayViewActivity.getCategories().length-1];
    }

    public Expenditure(float cost, String category)
    {
        this.cost = cost;
        this.category = category;
    }
}
