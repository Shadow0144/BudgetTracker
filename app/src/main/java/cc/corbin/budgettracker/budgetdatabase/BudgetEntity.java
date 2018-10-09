package cc.corbin.budgettracker.budgetdatabase;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

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
    private int isAdjustment; // SQLLite does not support booleans

    @ColumnInfo
    private long linkedID;

    @ColumnInfo
    private int linkedMonth;

    @ColumnInfo
    private int linkedYear;

    @ColumnInfo
    private int linkedCategory;

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
        this.isAdjustment = 0;
        this.linkedID = -1;
        this.linkedMonth = -1;
        this.linkedYear = -1;
        this.linkedCategory = -1;
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
        this.isAdjustment = 0;
        this.linkedID = -1;
        this.linkedMonth = -1;
        this.linkedYear = -1;
        this.linkedCategory = -1;
        this.note = "";
    }

    @Ignore
    public BudgetEntity(int month, int year, float amount, int category, String categoryName, int isAdjustment,
                        long linkedID, int linkedMonth, int linkedYear, int linkedCategory, String note)
    {
        this.id = 0;
        this.month = month;
        this.year = year;
        this.amount = amount;
        this.category = category;
        this.categoryName = categoryName;
        this.isAdjustment = isAdjustment;
        this.linkedID = linkedID;
        this.linkedMonth = linkedMonth;
        this.linkedYear = linkedYear;
        this.linkedCategory = linkedCategory;
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
        this.isAdjustment = 0;
        this.linkedID = -1;
        this.linkedMonth = -1;
        this.linkedYear = -1;
        this.linkedCategory = -1;
        this.note = "";
    }

    // Constructor for updating the database
    @Ignore
    public BudgetEntity(long id, int month, int year, float amount, int category, String categoryName, int isAdjustment,
                        long linkedID, String note)
    {
        this.id = id;
        this.month = month;
        this.year = year;
        this.amount = amount;
        this.category = category;
        this.categoryName = categoryName;
        this.isAdjustment = isAdjustment;
        this.linkedID = linkedID;
        this.linkedMonth = -1;
        this.linkedYear = -1;
        this.linkedCategory = -1;
        this.note = note;
    }

    public BudgetEntity(long id, int month, int year, float amount, int category, String categoryName, int isAdjustment,
                        long linkedID, int linkedMonth, int linkedYear, int linkedCategory, String note)
    {
        this.id = id;
        this.month = month;
        this.year = year;
        this.amount = amount;
        this.category = category;
        this.categoryName = categoryName;
        this.isAdjustment = isAdjustment;
        this.linkedID = linkedID;
        this.linkedMonth = linkedMonth;
        this.linkedYear = linkedYear;
        this.linkedCategory = linkedCategory;
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

    public int getIsAdjustment()
    {
        return isAdjustment;
    }

    public void setIsAdjustment(int isAdjustment)
    {
        this.isAdjustment = isAdjustment;
    }

    public long getLinkedID()
    {
        return linkedID;
    }

    public void setLinkedID(long linkedID)
    {
        this.linkedID = linkedID;
    }

    public int getLinkedMonth()
    {
        return linkedMonth;
    }

    public void setLinkedMonth(int linkedMonth)
    {
        this.linkedMonth = linkedMonth;
    }

    public int getLinkedYear()
    {
        return linkedYear;
    }

    public void setLinkedYear(int linkedYear)
    {
        this.linkedYear = linkedYear;
    }

    public int getLinkedCategory()
    {
        return linkedCategory;
    }

    public void setLinkedCategory(int linkedCategory)
    {
        this.linkedCategory = linkedCategory;
    }

    public void setLinkedAdjustment(long linkedID, int linkedMonth, int linkedYear, int linkedCategory)
    {
        this.linkedID = linkedID;
        this.linkedMonth = linkedMonth;
        this.linkedYear = linkedYear;
        this.linkedCategory = linkedCategory;
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
        out.writeInt(isAdjustment);
        out.writeLong(linkedID);
        out.writeInt(linkedMonth);
        out.writeInt(linkedYear);
        out.writeInt(linkedCategory);
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
        isAdjustment = in.readInt();
        linkedID = in.readLong();
        linkedMonth = in.readInt();
        linkedYear = in.readInt();
        linkedCategory = in.readInt();
        note = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
}
