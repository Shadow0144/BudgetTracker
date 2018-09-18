package cc.corbin.budgettracker.numericalformatting;

public interface NumericalFormattedCallback
{
    // The id of the calling EditText and the new value
    public void valueChanged(int id, float value);
}
