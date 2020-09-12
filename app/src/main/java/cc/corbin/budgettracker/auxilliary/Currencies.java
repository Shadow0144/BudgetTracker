package cc.corbin.budgettracker.auxilliary;

import java.text.DecimalFormat;

/**
 * Created by Corbin on 2/18/2018.
 */

// Enum holder and formatting helper
public final class Currencies
{
    public enum currencies
    {
        USD,
        EUR,
        JPY,
        KRW
    }

    public static boolean[] integer =
    {
        false,
        false,
        true,
        true
    };

    public static String[] symbols =
    {
        "$",
        "€",
        "¥",
        "₩"
    };

    public static final String DEFAULT_CURRENCY_KEY = "DEFAULT_CURRENCY";
    public static int default_currency;

    // Maximum number of digits in a number (excluding decimal places)
    public static final int MAX_INT_DIGITS = 15;

    public static String formatCurrency(boolean integer, float amount)
    {
        String cost;

        if (integer)
        {
            DecimalFormat formatter = new DecimalFormat("###,###,###,###,##0");
            cost = formatter.format(amount);
        }
        else
        {
            DecimalFormat formatter = new DecimalFormat("###,###,###,###,##0.00");
            cost = formatter.format(amount);
        }

        return cost;
    }

    public static String formatCurrency(int currency, float amount)
    {
        String cost;

        boolean decimal = !integer[currency];

        DecimalFormat formatter;
        if (!decimal)
        {
            formatter = new DecimalFormat("###,###,###,###,##0");
        }
        else
        {
            formatter = new DecimalFormat("###,###,###,###,##0.00");
        }

        if (amount >= 0)
        {
            cost = symbols[currency] + formatter.format(amount);
        }
        else
        {
            cost = "-" + symbols[currency] + formatter.format((-1)*amount);
        }

        return cost;
    }
}
