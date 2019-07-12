package cc.corbin.budgettracker.numericalformatting;

public interface NumericalFormattedCallback
{
    // The tag of the calling NumbericalFormattedEditText and the new value
    public void valueChanged(Object tag, float value);
}
