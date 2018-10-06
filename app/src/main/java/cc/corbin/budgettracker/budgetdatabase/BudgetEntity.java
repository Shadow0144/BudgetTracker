package cc.corbin.budgettracker.budgetdatabase;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

/**
 * Created by Corbin on 4/15/2018.
 */

@Entity
public class BudgetEntity implements Parcelable
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

    @ColumnInfo
    private String note;

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
        this.note = "";
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
        this.note = "";
    }

    @Ignore
    public BudgetEntity(int month, int year, float amount, int category, String categoryName, int adjustment, long sisterAdjustment, String note)
    {
        this.id = 0;
        this.month = month;
        this.year = year;
        this.amount = amount;
        this.category = category;
        this.categoryName = categoryName;
        this.adjustment = adjustment;
        this.sisterAdjustment = sisterAdjustment;
        this.note = note;
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
        this.note = "";
    }

    public BudgetEntity(long id, int month, int year, float amount, int category, String categoryName, int adjustment, long sisterAdjustment, String note)
    {
        this.id = id;
        this.month = month;
        this.year = year;
        this.amount = amount;
        this.category = category;
        this.categoryName = categoryName;
        this.adjustment = adjustment;
        this.sisterAdjustment = sisterAdjustment;
        this.note = note;
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

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        this.note = note;
    }

    public static final Parcelable.Creator<BudgetEntity> CREATOR = new Parcelable.Creator<BudgetEntity>()
    {
        public BudgetEntity createFromParcel(Parcel in)
        {
            return new BudgetEntity(in);
        }

        public BudgetEntity[] newArray(int size)
        {
            return new BudgetEntity[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeLong(id);
        out.writeInt(month);
        out.writeInt(year);
        out.writeFloat(amount);
        out.writeInt(category);
        out.writeString(categoryName);
        out.writeInt(adjustment);
        out.writeLong(sisterAdjustment);
        out.writeString(note);
    }

    private BudgetEntity(Parcel in)
    {
        id = in.readLong();
        month = in.readInt();
        year = in.readInt();
        amount = in.readFloat();
        category = in.readInt();
        categoryName = in.readString();
        adjustment = in.readInt();
        sisterAdjustment = in.readLong();
        note = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
}
