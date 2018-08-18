package cc.corbin.budgettracker.budgetdatabase;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Corbin on 4/15/2018.
 */

@Entity
public class BudgetEntity
{
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo
    private int month;

    @ColumnInfo
    private int year;

    @ColumnInfo
    private float amount;

    @ColumnInfo
    private int category;

    @ColumnInfo
    private String categoryName;

    @ColumnInfo
    private int adjustment; // SQLLite does not support booleans

    @ColumnInfo
    private long sisterAdjustment;

    @Ignore
    public BudgetEntity()
    {
        this.id = 0;
        this.month = 0;
        this.year = 0;
        this.amount = 0.0f;
        this.category = 0;
        this.categoryName = "";
        this.adjustment = 0;
        this.sisterAdjustment = -1;
    }

    @Ignore
    public BudgetEntity(int month, int year, float amount, int category, String categoryName)
    {
        this.id = 0;
        this.month = month;
        this.year = year;
        this.amount = amount;
        this.category = category;
        this.categoryName = categoryName;
        this.adjustment = 0;
        this.sisterAdjustment = -1;
    }

    @Ignore
    public BudgetEntity(int month, int year, float amount, int category, String categoryName, int adjustment, long sisterAdjustment)
    {
        this.id = 0;
        this.month = month;
        this.year = year;
        this.amount = amount;
        this.category = category;
        this.categoryName = categoryName;
        this.adjustment = adjustment;
        this.sisterAdjustment = sisterAdjustment;
    }

    @Ignore
    public BudgetEntity(long id, int month, int year, float amount, int category, String categoryName)
    {
        this.id = id;
        this.month = month;
        this.year = year;
        this.amount = amount;
        this.category = category;
        this.categoryName = categoryName;
        this.adjustment = 0;
        this.sisterAdjustment = -1;
    }

    public BudgetEntity(long id, int month, int year, float amount, int category, String categoryName, int adjustment, long sisterAdjustment)
    {
        this.id = id;
        this.month = month;
        this.year = year;
        this.amount = amount;
        this.category = category;
        this.categoryName = categoryName;
        this.adjustment = adjustment;
        this.sisterAdjustment = sisterAdjustment;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public int getMonth()
    {
        return month;
    }

    public void setMonth(int month)
    {
        this.month = month;
    }

    public int getYear()
    {
        return year;
    }

    public void setYear(int year)
    {
        this.year = year;
    }

    public float getAmount()
    {
        return amount;
    }

    public void setAmount(float amount)
    {
        this.amount = amount;
    }

    public int getCategory()
    {
        return category;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategory(int category, String categoryName)
    {
        this.category = category;
        this.categoryName = categoryName;
    }

    public int getAdjustment()
    {
        return adjustment;
    }

    public void setAdjustment(int adjustment)
    {
        this.adjustment = adjustment;
    }

    public long getSisterAdjustment()
    {
        return sisterAdjustment;
    }

    public void setSisterAdjustment(long sisterAdjustment)
    {
        this.sisterAdjustment = sisterAdjustment;
    }
}
