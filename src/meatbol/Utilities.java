package meatbol;

import java.util.ArrayList;

public class Utilities 
{
    Numeric numerics;
    /**
     * Utilities Constructor
     */
    public Utilities()
    {
        numerics = new Numeric();
    }
    /**
     * Performs the addition operation
     * @param leftOp - left operand in the expr
     * @param rightOp - right operand in the expr
     * @return  - result of lOp + rOp
     */
    public ResultValue add(Numeric leftOp, Numeric rightOp)
    {
        ResultValue res;
        // Doing integer addition
        if (leftOp.type == SubClassif.INTEGER)
        {
            int i = leftOp.integervalue + rightOp.integervalue;
            res = new ResultValue(leftOp.type, String.valueOf(i));
        }
        // Doing addition with double values
        else
        {
            double d = leftOp.doubleValue + rightOp.doubleValue;
            res = new ResultValue(leftOp.type, String.valueOf(d));
        }
        return res;  
    }   
    /**
     * Performs the subtraction operation
     * @param leftOp - left operand in the expr
     * @param rightOp - right operand in the expr
     * @return  - result of lOp - rOp
     */
    public ResultValue subtract(Numeric leftOp, Numeric rightOp)
    {
        ResultValue res;
        // Doing integer subtraction
        if (leftOp.type == SubClassif.INTEGER)
        {
            int i = leftOp.integervalue - rightOp.integervalue;
            res = new ResultValue(leftOp.type, String.valueOf(i));
            return res;
        }
        // Doing subtraction with double values
        else
        {
            double d = leftOp.doubleValue - rightOp.doubleValue;
            res = new ResultValue(leftOp.type, String.valueOf(d));
            return res;  
        }
    }
    /**
     * Performs the multiply operation
     * @param leftOp - left operand in the expr
     * @param rightOp - right operand in the expr
     * @return  - result of lOp * rOp
     */
    public ResultValue multiply(Numeric leftOp, Numeric rightOp)
    {
        ResultValue res;
        // Doing integer multiplication
        if (leftOp.type == SubClassif.INTEGER)
        {
            int i = leftOp.integervalue * rightOp.integervalue;
            res = new ResultValue(leftOp.type, String.valueOf(i));
            return res;
        }
        // Doing multiplication with double values
        else
        {
            double d = leftOp.doubleValue * rightOp.doubleValue;
            res = new ResultValue(leftOp.type, String.valueOf(d));
            return res;  
        }
    }
    /**
     * Performs the divide operation
     * @param leftOp - left operand in the expr
     * @param rightOp - right operand in the expr
     * @return  - result of lOp / rOp
     */
    public ResultValue divide(Numeric leftOp, Numeric rightOp)
    {
        ResultValue res;
        // Doing integer division
        if (leftOp.type == SubClassif.INTEGER)
        {
            int i = leftOp.integervalue / rightOp.integervalue;
            res = new ResultValue(leftOp.type, String.valueOf(i));
            return res;
        }
        // Doing division with double values
        else
        {
            double d = leftOp.doubleValue / rightOp.doubleValue;
            res = new ResultValue(leftOp.type, String.valueOf(d));
            return res;  
        }
    }
    /**
     * Raises lOp to the power of rOp
     * @param leftOp - left operand in the expr
     * @param rightOp - right operand in the expr
     * @return  - result of lOp ^ rOp
     */
    public ResultValue power(Numeric leftOp, Numeric rightOp)
    {
        ResultValue res;
        // Doing integer expo
        if (leftOp.type == SubClassif.INTEGER)
        {
            int i = (int) Math.pow(leftOp.integervalue, rightOp.integervalue);
            res = new ResultValue(leftOp.type, String.valueOf(i));
            return res;
        }
        // Doing expo with double values
        else
        {
            double d = Math.pow(leftOp.doubleValue, rightOp.doubleValue);
            res = new ResultValue(leftOp.type, String.valueOf(d));
            return res;  
        }
    }
    /**
     * Concatenates 2 strings 
     * @param lOp - left op
     * @param rOp - right op's string that will be added to left op's string
     * @return result of concatenating lOp's string and rOp's string
     */
    public ResultValue concat(ResultValue lOp, ResultValue rOp)
    {
        return new ResultValue(SubClassif.STRING, lOp.value + rOp.value);
    }
    /**
     * Performs a negative operation on a value
     * @param op - operand that will turn negative
     * @return negative operand
     */
    public ResultValue unaryMinus(Numeric op)
    {
        ResultValue res = new ResultValue(op.type, "-" + op.strValue);
        return res;  
    }  
    /**
     * Checks if lOp is equal to rOp
     * @param lOp - left operand in the expr
     * @param rOp - right operand in the expr
     * @return  - result of lOp == rOp
     * @throws Exception 
     */
    public ResultValue equals(ResultValue lOp, ResultValue rOp) throws Exception
    {
        // Do string comparisons with strings and booleans
        if (lOp.type == SubClassif.STRING || lOp.type == SubClassif.DATE || lOp.type == SubClassif.BOOLEAN)
        {
            if (lOp.value.equals(rOp.value))
                return new ResultValue(SubClassif.BOOLEAN, "T");
            else
                return new ResultValue(SubClassif.BOOLEAN, "F");
        }
        // Numeric comparisons
        else if (lOp.type == SubClassif.INTEGER || lOp.type == SubClassif.FLOAT)
        {
            Numeric nRop = new Numeric(this, rOp, rOp.value, "right operand");
            Numeric nLop = new Numeric(this, lOp, lOp.value, "left operand");
            // Integer comparison
            if (lOp.type == SubClassif.INTEGER)
            {
                if (nLop.integervalue == nRop.integervalue)
                    return new ResultValue(SubClassif.BOOLEAN, "T");
                else
                    return new ResultValue(SubClassif.BOOLEAN, "F");                    
            }
            // Double comparison
            else
            {
                 if (nLop.doubleValue == nRop.doubleValue)
                    return new ResultValue(SubClassif.BOOLEAN, "T");
                else
                    return new ResultValue(SubClassif.BOOLEAN, "F");                 
            }
        }
        return null; // placeholder
    }
    /**
     * Checks if lOp is not equal to rOp
     * @param lOp - left operand in the expr
     * @param rOp - right operand in the expr
     * @return  - result of lOp != rOp
     * @throws Exception 
     */
    public ResultValue notEquals(ResultValue lOp, ResultValue rOp) throws Exception
    {
        // Do string comparisons with strings and booleans
        if (lOp.type == SubClassif.STRING || lOp.type == SubClassif.DATE || lOp.type == SubClassif.BOOLEAN)
        {
            if (!lOp.value.equals(rOp.value))
                return new ResultValue(SubClassif.BOOLEAN, "T");
            else
                return new ResultValue(SubClassif.BOOLEAN, "F");
        }   
        // Numeric comparisons
        else if (lOp.type == SubClassif.INTEGER || lOp.type == SubClassif.FLOAT)
        {
            Numeric nRop = new Numeric(this, rOp, rOp.value, "right operand");
            Numeric nLop = new Numeric(this, lOp, lOp.value, "left operand");
            // Integer comparison
            if (lOp.type == SubClassif.INTEGER)
            {
                if (nLop.integervalue != nRop.integervalue)
                    return new ResultValue(SubClassif.BOOLEAN, "T");
                else
                    return new ResultValue(SubClassif.BOOLEAN, "F");                    
            }
            // Double comparison
            else
            {
                 if (nLop.doubleValue != nRop.doubleValue)
                    return new ResultValue(SubClassif.BOOLEAN, "T");
                else
                    return new ResultValue(SubClassif.BOOLEAN, "F");                 
            }
        }
        return null; // placeholder
    }    
    /**
     * Checks if lOp is greater than or equal to rOp
     * @param lOp - left operand in the expr
     * @param rOp - right operand in the expr
     * @return  - result of lOp >= rOp
     * @throws Exception 
     */
    public ResultValue greaterOrEqual(ResultValue lOp, ResultValue rOp) throws Exception
    {
        // Booleans can only be compared in == and != operations
        if (lOp.type == SubClassif.BOOLEAN || rOp.type == SubClassif.BOOLEAN)
            throw new Exception("Unable to do >= operation on a boolean value");
        // set the result to false by default
        ResultValue res = new ResultValue(SubClassif.BOOLEAN, "F");
        // Handling string comparisons
        if (lOp.type == SubClassif.STRING || lOp.type == SubClassif.DATE || lOp.type == SubClassif.BOOLEAN)
        {
            if (lOp.value.compareTo(rOp.value) >= 0)
                res.value = "T";
        }
        // Handling numeric comparisons
        else
        {
            Numeric nRop = new Numeric(this, rOp, rOp.value, "right operand");
            Numeric nLop = new Numeric(this, lOp, lOp.value, "left operand");
            if (lOp.type == SubClassif.INTEGER && nLop.integervalue >= nRop.integervalue)
                res.value = "T";
            else if (lOp.type == SubClassif.FLOAT && nLop.doubleValue >= nRop.doubleValue)
                res.value = "T";
        }
        return res;
    }
    /**
     * Checks if lOp is less than or equal to rOp
     * @param lOp - left operand in the expr
     * @param rOp - right operand in the expr
     * @return  - result of lOp less than or equal rOp
     * @throws Exception 
     */
    public ResultValue lessOrEqual(ResultValue lOp, ResultValue rOp) throws Exception
    {
        // Booleans can only be compared in == and != operations        
        if (lOp.type == SubClassif.BOOLEAN || rOp.type == SubClassif.BOOLEAN)
            throw new Exception("Unable to do <= operation on a boolean value");       
        // set the result to false by default
        ResultValue res = new ResultValue(SubClassif.BOOLEAN, "F");
        // Handling string comparisons
        if (lOp.type == SubClassif.STRING || lOp.type == SubClassif.DATE || lOp.type == SubClassif.BOOLEAN)
        {
            if (lOp.value.compareTo(rOp.value) <= 0)
                res.value = "T";
        }
        // Handling numeric comparisons
        else
        {
            Numeric nRop = new Numeric(this, rOp, rOp.value, "right operand");
            Numeric nLop = new Numeric(this, lOp, lOp.value, "left operand");
            if (lOp.type == SubClassif.INTEGER && nLop.integervalue <= nRop.integervalue)
                res.value = "T";
            else if (lOp.type == SubClassif.FLOAT && nLop.doubleValue <= nRop.doubleValue)
                res.value = "T";
        }
        return res;
    }   
    /**
     * Checks if lOp is less than rOp
     * @param lOp - left operand in the expr
     * @param rOp - right operand in the expr
     * @return  - result of lOp less than rOp
     * @throws Exception 
     */
    public ResultValue lessThan(ResultValue lOp, ResultValue rOp) throws Exception
    {
        // Booleans can only be compared in == and != operations
        if (lOp.type == SubClassif.BOOLEAN || rOp.type == SubClassif.BOOLEAN)
            throw new Exception("Unable to do < operation on a boolean value");        
        // set the result to false by default
        ResultValue res = new ResultValue(SubClassif.BOOLEAN, "F");
        // Handling string comparisons
        if (lOp.type == SubClassif.STRING || lOp.type == SubClassif.DATE || lOp.type == SubClassif.BOOLEAN)
        { 
            if (lOp.value.compareTo(rOp.value) == -1)
                res.value = "T";
        }
        // Handling numeric comparisons
        else
        {
            Numeric nRop = new Numeric(this, rOp, rOp.value, "right operand");
            Numeric nLop = new Numeric(this, lOp, lOp.value, "left operand");
            if (lOp.type == SubClassif.INTEGER && nLop.integervalue < nRop.integervalue)
                res.value = "T";
            else if (lOp.type == SubClassif.FLOAT && nLop.doubleValue < nRop.doubleValue)
                res.value = "T";
        }
        return res;
    }
    /**
     * Checks if lOp is greater than rOp
     * @param lOp - left operand in the expr
     * @param rOp - right operand in the expr
     * @return  - result of lOp > rOp
     * @throws Exception 
     */
    public ResultValue greaterThan(ResultValue lOp, ResultValue rOp) throws Exception
    {
        // Booleans can only be compared in == and != operations
        if (lOp.type == SubClassif.BOOLEAN || rOp.type == SubClassif.BOOLEAN)
            throw new Exception("Unable to do > operation on a boolean value");        
        // set the result to false by default
        ResultValue res = new ResultValue(SubClassif.BOOLEAN, "F");
        // Handling string comparisons
        if (lOp.type == SubClassif.STRING || lOp.type == SubClassif.DATE || lOp.type == SubClassif.BOOLEAN)
        {
            if (lOp.value.compareTo(rOp.value) == 1)
                res.value = "T";
        }
        // Handling numeric comparisons
        else
        {
            Numeric nRop = new Numeric(this, rOp, rOp.value, "right operand");
            Numeric nLop = new Numeric(this, lOp, lOp.value, "left operand");
            if (lOp.type == SubClassif.INTEGER && nLop.integervalue > nRop.integervalue)
                res.value = "T";
            else if (lOp.type == SubClassif.FLOAT && nLop.doubleValue > nRop.doubleValue)
                res.value = "T";
        }
        return res;
    }
    /**
     * Performs the and logical operator
     * @param lOp - left bool val
     * @param rOp - right bool val
     * @return T is both vals have 'T', otherwise 'F'
     */
    public ResultValue and(ResultValue lOp, ResultValue rOp) throws Exception
    {
        // We expect both resultvalues to be of type BOOLEAN
        if (lOp.type != SubClassif.BOOLEAN)
            throw new Exception("Expected left operand in 'and' operator to be of type BOOLEAN");
        if (rOp.type != SubClassif.BOOLEAN)
            throw new Exception("Expected right operand in 'and' operator to be of type BOOLEAN");
        if (lOp.value.equals("T") && rOp.value.equals("T"))
            return new ResultValue(SubClassif.BOOLEAN, "T");
        else
           return new ResultValue(SubClassif.BOOLEAN, "F");
    }
    /**
     * Performs the or logical operator
     * @param lOp - left bool val
     * @param rOp - right bool val
     * @return T is either val has a 'T', otherwise 'F'
     */
    public ResultValue or(ResultValue lOp, ResultValue rOp) throws Exception
    {
        // We expect both resultvalues to be of type BOOLEAN
        if (lOp.type != SubClassif.BOOLEAN)
            throw new Exception("Expected left operand in 'and' operator to be of type BOOLEAN");
        if (rOp.type != SubClassif.BOOLEAN)
            throw new Exception("Expected right operand in 'and' operator to be of type BOOLEAN");        
       if (lOp.value.equals("T") || rOp.value.equals("T"))
           return new ResultValue(SubClassif.BOOLEAN, "T");
       else
           return new ResultValue(SubClassif.BOOLEAN, "F");        
    }
    /**
     * Inverts a boolean value
     * @param op - boolean value
     * @return T is op is F, F is op is T
     * @throws Exception 
     */
    public ResultValue not(ResultValue op) throws Exception
    {
        if (op.type != SubClassif.BOOLEAN)
            throw new Exception("Expected op to be a boolean");
        if (op.value.equals("T"))
            return new ResultValue(SubClassif.BOOLEAN, "F");   
        else
            return new ResultValue(SubClassif.BOOLEAN, "T");
    }
    // built-in functions
    //•	LENGTH(string) SPACES(string) ELEM(array) MAXELEM(array)
    /**
     * Gets the length of the values string representation
     * @param resStr
     * @return 
     */
    public ResultValue LENGTH(ResultValue resStr) throws Exception
    {
        if (resStr.type != SubClassif.STRING && resStr.type != SubClassif.DATE  && resStr.type != SubClassif.BOOLEAN)
            throw new Exception("Parameter in LENGTH must be a string, instead found type " + resStr.type);
        return new ResultValue(SubClassif.INTEGER, String.valueOf(resStr.value.length()));
    }
    /**
     * Checks if the string contains only spaces or is empty
     * @param resStr - name of string
     * @return true if str only contains space/is empty, otherwise false
     */
    public ResultValue SPACES(ResultValue resStr) throws Exception
    {
        if (resStr.type != SubClassif.STRING && resStr.type != SubClassif.DATE  && resStr.type != SubClassif.BOOLEAN)
            throw new Exception("Parameter in SPACES must be a string, instead found type " + resStr.type);        
        String spaceDelim = " \t\n";
        char[] cArr = resStr.value.toCharArray();
        // Checks if str has a non-whitespace character
        for (int i = 0; i < cArr.length; i++)
        {
            // Found a non-whitespace character
            if (spaceDelim.indexOf(cArr[i]) == -1)
                return new ResultValue(SubClassif.BOOLEAN, "F");
        }
        // Otherwise the string only had spaces
        return new ResultValue(SubClassif.BOOLEAN, "T");
    }
    /**
     * Gets the highest subscript in the array + 1
     * @param arrStr - name of variable
     * @param storageManager - used to check if its an array
     * @return the highest subscript in the array
     * @throws Exception 
     */
    public ResultValue ELEM(String arrStr, StorageManager storageManager) throws Exception
    {
        ArrayList<ResultValue> resArr = storageManager.getArrayVariable(arrStr);
        if (resArr == null)
            throw new Exception("Attempted to call ELEM on array " + arrStr + " which has not been declared/initialized");
        int i = 0;
        int iSubScript = -1;
        // Iterate through the array and get the highest subscript
        for (ResultValue res : resArr)
        {
            if (res != null)
                iSubScript = i;
            i++;
        }
        iSubScript++;
        return new ResultValue(SubClassif.INTEGER, String.valueOf(iSubScript));
    }
    /**
     * Gets the maximum size of an array
     * @param arrStr - name of variable
     * @param storageManager - used to check if its an array
     * @param symTable - used to get max size
     * @return resultValue containing max size of array
     * @throws Exception 
     */
    public ResultValue MAXELEM(String arrStr, StorageManager storageManager, SymbolTable symTable) throws Exception
    {
        ArrayList<ResultValue> resArr = storageManager.getArrayVariable(arrStr);
        if (resArr == null)
            throw new Exception("Attempted to call MAXELEM on array " + arrStr + " which has not been declared/initialized");   
        STIdentifier stI = (STIdentifier) symTable.getSymbol(arrStr);
        return new ResultValue(SubClassif.INTEGER, String.valueOf(stI.maxSize));
    }
    /**
     * 
     * @param lRes
     * @param valList
     * @return
     * @throws Exception 
     */
    public ResultValue IN(ResultValue lRes, ArrayList<ResultValue> valList) throws Exception
    {
        // In the case we need to force a float into an int, or int into a float
        Numeric n;
        for (ResultValue val : valList)
        {
            // If the types are not the same, we can coerce somes type (We can force a FLOAT to become a INT
            if (lRes.type != val.type)
            {
                // We can convert a date into a string
                if (lRes.type == SubClassif.STRING && val.type == SubClassif.DATE)
                    val.type = SubClassif.DATE;
                // we can convert a string into a date
                if (lRes.type == SubClassif.DATE && val.type == SubClassif.STRING)
                    val.type = SubClassif.STRING;                
                // Numeric conversions
                if (lRes.type == SubClassif.INTEGER && val.type == SubClassif.FLOAT)
                {
                    n = new Numeric(this, val, "IN", "Right Operand - Array Element");
                    val.type = SubClassif.INTEGER;
                    val.value = String.valueOf(n.integervalue);
                }
                if (lRes.type == SubClassif.FLOAT && val.type == SubClassif.INTEGER)
                {
                    n = new Numeric(this, val, "IN", "Right Operand - Array Element");
                    val.type = SubClassif.FLOAT;
                    val.value = String.valueOf(n.doubleValue);
                }                
            }
            if (lRes.value.equals(val.value) && val.type == lRes.type)
                return new ResultValue(SubClassif.BOOLEAN, "T");
        }
        // Default value is false
        return new ResultValue(SubClassif.BOOLEAN, "F");
    }
    /**
     * 
     * @param lRes
     * @param valList
     * @return
     * @throws Exception 
     */
    public ResultValue NOTIN(ResultValue lRes, ArrayList<ResultValue> valList) throws Exception
    {
        // In the case we need to force a float into an int, or int into a float
        Numeric n;
        for (ResultValue val : valList)
        {
            // If the types are not the same, we can coerce somes type (We can force a FLOAT to become a INT
            if (lRes.type != val.type)
            {
                // We can convert a date into a string
                if (lRes.type == SubClassif.STRING && val.type == SubClassif.DATE)
                    val.type = SubClassif.DATE;
                // we can convert a string into a date
                if (lRes.type == SubClassif.DATE && val.type == SubClassif.STRING)
                    val.type = SubClassif.STRING;                
                // Numeric conversions
                if (lRes.type == SubClassif.INTEGER && val.type == SubClassif.FLOAT)
                {
                    n = new Numeric(this, val, "IN", "Right Operand - Array Element");
                    val.type = SubClassif.INTEGER;
                    val.value = String.valueOf(n.integervalue);
                }
                if (lRes.type == SubClassif.FLOAT && val.type == SubClassif.INTEGER)
                {
                    n = new Numeric(this, val, "IN", "Right Operand - Array Element");
                    val.type = SubClassif.FLOAT;
                    val.value = String.valueOf(n.doubleValue);
                }                
            }
            if (lRes.value.equals(val.value) && val.type == lRes.type)
                return new ResultValue(SubClassif.BOOLEAN, "F");
        }
        // Default value is false
        return new ResultValue(SubClassif.BOOLEAN, "T");
    }
}
