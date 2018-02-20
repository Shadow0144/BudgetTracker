package cc.corbin.budgettracker;

/**
 * Created by Corbin on 2/18/2018.
 */

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
}
