package cc.corbin.budgettracker.expendituredatabase;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDate;

import cc.corbin.budgettracker.auxilliary.Currencies;

/**
 * Created by Corbin on 2/14/2018.
 */

@Entity
public class ExpenditureEntity implements Parcelable
{
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo
    private int day;

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
    private int baseCurrency;

    @ColumnInfo
    private float baseAmount;

    @ColumnInfo
    private float conversionRate;

    @ColumnInfo
    private String note;

    @Ignore
    public ExpenditureEntity()
    {
        this.id = 0;
        this.day = 0;
        this.month = 0;
        this.year = 0;
        this.amount = 0.0f;
        this.category = 0;
        this.categoryName = "";
        this.baseCurrency = 0;
        this.baseAmount = 0.0f;
        this.conversionRate = 0.0f;
        this.note = "";
    }

    // Previous constructor (currency is ignored as it is always the default)
    @Ignore
    public ExpenditureEntity(long date, int currency, float amount, String categoryName, String note)
    {
        this.id = 0;
        this.amount = amount;
        this.category = category;
        this.categoryName = categoryName;
        this.baseCurrency = Currencies.default_currency;
        this.baseAmount = amount;
        this.conversionRate = 1.0f;
        this.note = note;
        checkNullStrings();
        convertDateToDMY(date);
    }

    @Ignore
    public ExpenditureEntity(int day, int month, int year)
    {
        this.id = 0;
        this.day = day;
        this.month = month;
        this.year = year;
        this.amount = 0.0f;
        this.category = 0;
        this.categoryName = "";
        this.baseCurrency = Currencies.default_currency;
        this.baseAmount = amount;
        this.conversionRate = 1.0f;
        this.note = "";
        checkNullStrings();
    }

    @Ignore
    public ExpenditureEntity(int day, int month, int year, float amount, int category, String categoryName, String note)
    {
        this.id = 0;
        this.day = day;
        this.month = month;
        this.year = year;
        this.amount = amount;
        this.category = category;
        this.categoryName = categoryName;
        this.baseCurrency = Currencies.default_currency;
        this.baseAmount = amount;
        this.conversionRate = 1.0f;
        this.note = note;
        checkNullStrings();
    }

    @Ignore
    public ExpenditureEntity(long id, int day, int month, int year, float amount, int category, String categoryName, String note)
    {
        this.id = id;
        this.day = day;
        this.month = month;
        this.year = year;
        this.amount = amount;
        this.category = 0;
        this.categoryName = categoryName;
        this.baseCurrency = Currencies.default_currency;
        this.baseAmount = amount;
        this.conversionRate = 1.0f;
        this.note = note;
        checkNullStrings();
    }

    public ExpenditureEntity(long id, int day, int month, int year, float amount, int category, String categoryName,
                             int baseCurrency, float baseAmount, float conversionRate, String note)
    {
        this.id = id;
        this.day = day;
        this.month = month;
        this.year = year;
        this.amount = amount;
        this.category = category;
        this.categoryName = categoryName;
        this.baseCurrency = baseCurrency;
        this.baseAmount = baseAmount;
        this.conversionRate = conversionRate;
        this.note = note;
        checkNullStrings();
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public int getDay()
    {
        return day;
    }

    public void setDay(int day)
    {
        this.day = day;
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

    public int getBaseCurrency()
    {
        return baseCurrency;
    }

    public void setBaseCurrency(int baseCurrency)
    {
        this.baseCurrency = baseCurrency;
    }

    public float getBaseAmount()
    {
        return  baseAmount;
    }

    public void setBaseAmount(float baseAmount)
    {
        this.baseAmount = baseAmount;
    }

    public float getConversionRate()
    {
        return conversionRate;
    }

    public void setConversionRate(float conversionRate)
    {
        this.conversionRate = conversionRate;
    }

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        this.note = note;
    }

    @Ignore
    private void checkNullStrings()
    {
        if (this.categoryName == null) this.categoryName = "";
        if (this.note == null) this.note = "";
    }

    @Ignore
    private void convertDateToDMY(long date)
    {
        LocalDate currentDate = LocalDate.ofEpochDay(date / 24 / 60 / 60 / 1000); // Convert to milliseconds

        day = currentDate.getDayOfMonth();
        month = currentDate.getMonthValue();
        year = currentDate.getYear();
    }

    @Ignore
    public void update(ExpenditureEntity expenditureEntity)
    {
        amount = expenditureEntity.amount;
        category = expenditureEntity.category;
        categoryName = expenditureEntity.categoryName;
        baseCurrency = expenditureEntity.baseCurrency;
        baseAmount = expenditureEntity.baseAmount;
        conversionRate = expenditureEntity.conversionRate;
        note = expenditureEntity.note;
    }

    public static final Parcelable.Creator<ExpenditureEntity> CREATOR = new Parcelable.Creator<ExpenditureEntity>()
    {
        public ExpenditureEntity createFromParcel(Parcel in)
        {
            return new ExpenditureEntity(in);
        }

        public ExpenditureEntity[] newArray(int size)
        {
            return new ExpenditureEntity[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeLong(id);
        out.writeInt(day);
        out.writeInt(month);
        out.writeInt(year);
        out.writeFloat(amount);
        out.writeInt(category);
        out.writeString(categoryName);
        out.writeInt(baseCurrency);
        out.writeFloat(baseAmount);
        out.writeFloat(conversionRate);
        out.writeString(note);
    }

    private ExpenditureEntity(Parcel in)
    {
        id = in.readLong();
        day = in.readInt();
        month = in.readInt();
        year = in.readInt();
        amount = in.readFloat();
        category = in.readInt();
        categoryName = in.readString();
        baseCurrency = in.readInt();
        baseAmount = in.readFloat();
        conversionRate = in.readFloat();
        note = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
}
