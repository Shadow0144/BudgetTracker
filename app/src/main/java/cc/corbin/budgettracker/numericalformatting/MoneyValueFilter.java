package cc.corbin.budgettracker.numericalformatting;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.util.Log;

public class MoneyValueFilter extends DigitsKeyListener
{
    private final String TAG = "MoneyValueFilter";

    private final int MAX_LENGTH = 19;
    private int _digits = 2;
    private int _maxLength = MAX_LENGTH + _digits + (_digits > 0 ? 1 : 0);

    public MoneyValueFilter()
    {
        super(false, true);
    }

    public void setDigits(int d)
    {
        _digits = d;
        _maxLength = MAX_LENGTH + _digits + (_digits > 0 ? 1 : 0);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                                     Spanned dest, int dstart, int dend)
    {
        CharSequence out = null;

        int len = end - start;
        int dlen = dest.length();

        if ((len-(dend-dstart)) > _maxLength)
        {
            int overlap = (len-(dend-dstart)) - _maxLength;
            out = source.subSequence((start + overlap), end);
        }
        else if (source.toString().equals(",")) // If it is just a comma, only insert it if it is in the ones' place
        {
            if ((dlen > 0) && ((dend == dlen) || (((dend+1) < dlen) && (dest.toString().charAt(dend+1) == '.'))))
            {
                out = "000";
            }
            else
            {
                out = "";
            }
        }
        else
        {
            boolean found = false;
            // Find the position of the decimal .
            for (int i = 0; i < dstart; i++)
            {
                if (dest.charAt(i) == '.')
                {
                    // Being here means that a number has been inserted after the dot
                    // Check if the amount of digits is correct, and that it does not contain a comma or another period
                    out = ((dlen - (i + 1) + len > _digits) || (source.toString().contains(",")) || (source.toString().contains("."))) ?
                            "" :
                            new SpannableStringBuilder(source, start, end);
                    found = true;
                    break;
                }
                else { }
            }

            if (!found)
            {
                for (int i = start; i < end; ++i)
                {
                    if (source.charAt(i) == '.')
                    {
                        // Being here means that the dot has been inserted
                        // Check if the amount of digits is right
                        if (((dlen - dend) + (end - (i + 1)) > _digits) || (_digits == 0))
                        {
                            out = "";
                            break;
                        }
                        else
                        {
                            out = new SpannableStringBuilder(source, start, end);
                            break;
                        }
                    }
                }
            }
            else { }
        }

        return out;
    }
}