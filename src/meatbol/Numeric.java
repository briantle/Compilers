package meatbol;
/**
 * Numeric Class
 * Used to determine a variable's data type, and to convert strings to either a int or double value
 * Also used to convert ResultValue objects into Numeric objects to be used for numeric operations.
 */
public class Numeric 
{
    int integervalue;   // integer representation of strValue
    double doubleValue; // double representation of strValue
    String strValue;    // display value
    SubClassif type;    // INTEGER, FLOAT 
    /**
     * EMpty Numeric Constructor used when getting the numeric type in the scanner.
     */
    public Numeric()
    {
        
    }
    /**
     * Numeric Constructor used when converting a resultValue object to a Numeric object.
     * @param calledFrom - the class that is calling this constructor
     * @param operand - the operand that is being converted to a numeric
     * @param operation - the operation that the operand is involved in. Could be + = - / += -= etc
     * @param whichOperand - if this is the 1st, 2nd, 3rd, .... nth operand
     */
    public Numeric(Object calledFrom, ResultValue operand, String operation, String whichOperand) throws Exception
    {
        // If we are trying to convert booleans or dates into Numeric, we should give an error.
        if (operand.type != SubClassif.INTEGER && operand.type != SubClassif.FLOAT && operand.type != SubClassif.STRING)
            throw new Exception("For operation " + operation + ": tried to convert " + whichOperand + " '" + operand.value + "' into a numeric when it is not of type integer or float, instead found type: " + operand.type);
        // Set the attributes of the Numeric object
        this.type = operand.type;
        this.strValue = operand.value;
        this.integervalue = strToInt(operand.value);
        this.doubleValue = strToDouble(operand.value);
    }
    /**
     * Checks to determine if the token is an integer, float or is an invalid number
     * <p>
     * @param token the numeric constant
     * @return  if the token is an integer, float, or that it's an invalid number
     */
    public int getNumericType(String token)
    {
        // Check if token is an integer
        try
        {
            Integer i = Integer.valueOf(token);
            return 0;
        }
        // Token is not an integer, but it might be a float so don't do anything in this catch
        catch (NumberFormatException e){}
        
        // Check if token is a float
        try
        {
            Float f = Float.valueOf(token);
            return 1;
        }
        // Token was neither an integer or float, therefore it is an invalid number
        catch(NumberFormatException e)
        {
            return -1;
        }
    }
    /**
     * Converts a string to it's Integer value
     * @param str - the string that represents a number
     * @return str converted to int if str is a valid int, otherwise error
     */
    public int strToInt(String str) throws Exception
    {
        // Initialize int variable
        int i = 0;
        // Attempt to convert str to int
        try
        {
            
            i = (int) Double.parseDouble(str);
        }
        // Unable to convert str to int
        catch (NumberFormatException e)
        {
            throw new Exception("Failed to convert '" + str + "' to an Int");
        }
        // Str was successfully converted to int, return it's int value
        return i;
    }
    /**
     * Converts a string to it's Double value
     * @param str - the string that represents a number
     * @return str converted to double if str is a valid double, otherwise error 
     */
    public double strToDouble(String str) throws Exception
    {
        // Initialize double variable
        double d = 0;
        // Attempt to convert str to double
        try
        {
            d = Double.parseDouble(str);
        }
        // Unable to convert str to double
        catch (NumberFormatException e)
        {
            // In our error we specify Float instead of double since our language represents doubles as floats
            throw new Exception("Failed to convert '" + str + "' to a Float");
        }
        // Str was successfully converted to double, return it's double value
        return d;
    }
}
