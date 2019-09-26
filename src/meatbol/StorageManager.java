package meatbol;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * StorageManager class
 * Used to store variables in a hashmap and to retrieve the values of those variables
 */
public class StorageManager
{
    // Hashmap that contains primitive variables
    public HashMap<String, ResultValue> varTable;   
    // Hashmap that contains array variables
    public HashMap<String, ArrayList<ResultValue>> arrayTable;
    public Utilities util;
    /**
     * StorageManager constructor
     * Initializes variableTable hashmap
     */
    public StorageManager()
    {
        varTable = new HashMap<>();
        arrayTable = new HashMap<>();
        util = new Utilities();
    }
    /**
     * Stores the value of the variable into the table.
     * If the variable already exists, it's value will be updated to the new value
     * @param variable - name of the variable
     * @param result - the value of the variable
     */
    public void insertVariable(String variable, ResultValue result)
    {
        varTable.put(variable, result);
    }
    /**
     * Gets the value of the variable in the table
     * @param classCalled - the class calling this function
     * @param variable - the name of the variable we are trying to get the value of
     * @return the value of the variable, null if variable doesn't exist in table
     */
    public ResultValue getVariableValue(Object classCalled, String variable)
    {
        return varTable.get(variable);
    }
    /**
     * Stores array in storageManager
     * @param variable - name of array
     * @param arrResult - array value
     */
    public void insertArray(String variable, ArrayList<ResultValue> arrResult, SymbolTable symTable) throws Exception
    {
       STIdentifier stI = (STIdentifier) symTable.getSymbol(variable);
       if (arrResult.size() > stI.maxSize && stI.structure != Structure.UNBOUND_ARRAY)
           throw new Exception("Cannot assign an array of size " + arrResult.size() + " to an array of size " +stI.maxSize);
       arrayTable.put(variable, arrResult);
    }
    /**
     * Gets the array
     * @param variable - name of array
     * @return array of the variable specified
     */
    public ArrayList<ResultValue> getArrayVariable(String variable)
    {
        return arrayTable.get(variable);
    }
    /**
     * Sets the element of the array
     * @param variable - name of array
     * @param arrElem - element of array
     * @param iSubScript - subscript in the array 
     */
    public void setArrayElem(String variable, ResultValue arrElem, int iSubScript, SymbolTable symTable) throws Exception
    {
        STIdentifier stI = (STIdentifier) symTable.getSymbol(variable);
        if (iSubScript >= stI.maxSize && stI.structure != Structure.UNBOUND_ARRAY)
            throw new Exception("Trying to access an array element with an out of range index");
        ArrayList<ResultValue> arr = arrayTable.get(variable);
        arr.set(iSubScript, arrElem);
        arrayTable.put(variable, arr);
    }
    /**
     * Gets the element in the array
     * @param variable - name of array
     * @param iSubScript - subscript in the array
     * @param symTable - used for out of bound checks
     * @return element of array
     * @throws Exception 
     */
    public ResultValue getArrayElem(String variable, int iSubScript, SymbolTable symTable) throws Exception
    {
        ArrayList<ResultValue> arr = arrayTable.get(variable);
        if (iSubScript < 0)
            return arr.get(Integer.valueOf(util.ELEM(variable, this).value) + iSubScript);
        STIdentifier stI = (STIdentifier) symTable.getSymbol(variable);
        if (iSubScript >= stI.maxSize && stI.structure != Structure.UNBOUND_ARRAY)
            throw new Exception("Trying to access an array element with an out of range index");
        return arr.get(iSubScript);
    }
}
