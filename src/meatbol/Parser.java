package meatbol;
import java.util.ArrayList;
public class Parser
{
    Scanner scan;                   // Scanner class, used to get token info and to grab next tokens
    Utilities util;                 // Used to call functions regarding numeric calculations
    StorageManager storageManager;  // Used to store values of variables
    Expression expr;                // used for converting infix to postfix and evaluation expressions
    /**
     * Parser Constructor
     * <p>
     * @param scan - Scanner class, used to get token info, grab next tokens and store variables in the symbol table 
     */
    public Parser(Scanner scan)
    {
        this.scan = scan;
        util = new Utilities();
        storageManager = new StorageManager();
        expr = new Expression(this);
    }
    /**
     * Assumptions: Current token is a assignment operator.
     * Parses an assignment statement.
     * Performs assignment operations based on the assignment operator.
     * <p>
     * @param variableStr - the variable that is being assigned a value
     * @param bExec - determines whether to execute the assignment statement or skip it
     * @return result of the assignment operation
     */
    public ResultValue assignStmt(Token variable, int iSubscript, boolean bExec) throws Exception
    {
        String variableStr = variable.tokenStr;
        // The result of variableStr being assigned a value
        ResultValue res = new ResultValue();
        // The result of the expr that will then be assigned to res
        ResultValue res2;
        // Numeric operands in the case we are doing += or -=
        Numeric nOp1;
        Numeric nOp2;
        // If we are not executing, skip this statement
        if (!bExec)
        {
            scan.skipTo(";");
            res.type = SubClassif.SKIPPED;
            return res;
        }
        // We expect an assignment operator for our current token
        if (scan.currentToken.subClassif != SubClassif.ASSIGNMENT)
            error("Expected an assignment operator at column %d on line %d, instead found '%s'", scan.currentToken.iColPos, scan.currentToken.iSourceLineNr, scan.currentToken.tokenStr);
        // Store what the assignment operator is in a temp variable
        String assignmentStr = scan.currentToken.tokenStr;
        // Used to determine if we are doing regular assignment, or scalar/array assignment
        STIdentifier varIdentifier = (STIdentifier) scan.symbolTable.getSymbol(variableStr);
        Structure dataStruct = varIdentifier.structure;
        // We expect the next token after the assignment operator to be a operand, or a U-
        if (scan.nextToken.primClassif != Classif.OPERAND && scan.nextToken.subClassif != SubClassif.UNARY_MINUS && scan.nextToken.primClassif != Classif.FUNCTION)
            error("Expected token after '=' on column %d at line %d to either be an operand or U-, found '%s'", scan.nextToken.iColPos, scan.nextToken.iSourceLineNr, scan.nextToken.tokenStr);
        // Set the current token to be the operand or U-
        scan.getNext();        
        // Based on the assignment operator, we want to do different operations
        switch(assignmentStr)
        {
            case "=":
                // If we have a FIXED_ARRAY, we expect scalar or array assignment
                if (dataStruct == Structure.FIXED_ARRAY && variable.subClassif == SubClassif.IDENTIFIER)
                {
                    // Check if we have an array or scalar variable val
                    if (scan.currentToken.subClassif == SubClassif.IDENTIFIER)
                    {
                        // Used to determine if we are doing regular assignment, or scalar/array assignment
                        STIdentifier currIdentifier = (STIdentifier) scan.symbolTable.getSymbol(scan.currentToken.tokenStr);
                        Structure currDataStruct = currIdentifier.structure;
                        // array to array assignment
                        if (currDataStruct == Structure.FIXED_ARRAY)
                        {
                            ArrayList<ResultValue> arr = storageManager.getArrayVariable(currIdentifier.symbol);
                            ArrayList<ResultValue> ourArr = storageManager.getArrayVariable(variableStr);
                            int i = 0;
                            // Iterate through the array and set it's values
                            for (; i < varIdentifier.maxSize; i++)
                            {
                                // If arr has a smaller array and we've reached the end of it, break out
                                if (i >= arr.size())
                                    break;
                                // Append value is our array wasn't given a value list
                                if (ourArr.size() <= i)
                                    ourArr.add(arr.get(i));
                                // otherwise overwrite the values
                                else
                                    ourArr.set(i, arr.get(i));
                            }
                            // insert the array into storageManager
                            storageManager.insertArray(variableStr, ourArr, scan.symbolTable);
                            // grab the ';'
                            scan.getNext();
                        }
                        // else scalar assignment from variable value
                        else
                        {
                            // Get the scalar value
                            res2 = expr.expr("", ";");
                            // get the array
                            ArrayList<ResultValue> ourArr = storageManager.getArrayVariable(variableStr);
                            int i = 0;
                            // Iterate through the array and set it's values
                            for (i = 0; i < ourArr.size(); i++)
                            {
                                // Append value is our array wasn't given a value list
                                if (ourArr.size() <= i)
                                    ourArr.add(res2);
                                // otherwise overwrite the values
                                else
                                    ourArr.set(i, res2);
                            }
                            storageManager.insertArray(variableStr, ourArr, scan.symbolTable);                            
                        }
                    }
                    // Otherwise it's a scalar constant
                    else
                    {
                        res2 = expr.expr("", ";");
                        ArrayList<ResultValue> ourArr = storageManager.getArrayVariable(variableStr);
                        int i = 0;
                        for (; i < ourArr.size(); i++)
                        {
                            if (ourArr.size() <= i)
                                ourArr.add(res2);
                            else
                                ourArr.set(i, res2);
                        }
                        storageManager.insertArray(variableStr, ourArr, scan.symbolTable); 
                        if (Debugger.bShowAssign)
                        {
                            System.out.printf("... Assign result to '%s' is [ ", variableStr);
                            for (ResultValue arrVal : ourArr)
                                System.out.printf("%s ", arrVal.value);
                            System.out.println("]");
                        }
                    }
                }
                // If we have an array ref, we have an array element assignment
                else if (variable.subClassif == SubClassif.ARRAY_REF)
                {
                    res2 = expr.expr("", ";");
                            // If data types are different, INTEGER and FLOAT are allowed
                    if (varIdentifier.dclType != res2.type)
                    {
                        Numeric n = new Numeric();
                        // Convert float to int, then assign to int var
                        if (varIdentifier.dclType == SubClassif.INTEGER && res2.type == SubClassif.FLOAT)
                        {
                            res2.type = SubClassif.INTEGER;
                            res2.value = String.valueOf(n.strToInt(res2.value));
                        }
                        // Convert int to float, then assign to float var
                        else if (varIdentifier.dclType == SubClassif.FLOAT && res2.type == SubClassif.INTEGER)
                        {
                            res2.type = SubClassif.FLOAT;
                            res2.value = String.valueOf(n.strToDouble(res2.value));
                        }
                        // Otherwise, our identifier's type is either a string, date or boolean
                        else
                            error("Tried to assign '%s' which is of data type %s to '%s' which is of data type %s", variableStr, varIdentifier.dclType, res2.value, res2.type);
                    }
                    // Check if we are doing array ref assignment to a primitive, or fixed array
                    if (varIdentifier.structure == Structure.PRIMITIVE)
                    {
                        // varIdentifier must be a string
                        if (varIdentifier.dclType != SubClassif.STRING)
                            error("Expected array ref '%s' which is a primitive to be of type STRING, instead found type '%s'", variableStr, varIdentifier.dclType);
                        // Get the value of the string and convert to a char array
                        ResultValue pRes = storageManager.getVariableValue(this, variableStr);
                        // Check if subscript is out of bounds
                        if (iSubscript >= pRes.value.length())
                            error("Subscript in array ref assignment is out of bounds");
                        char scArr[] = pRes.value.toCharArray();
                        // Used to add values to the string
                        String newStrVal = "";
                        int i = 0;
                        for (; i < scArr.length; i++)
                        {
                            if (i == iSubscript)
                                newStrVal += res2.value;
                            else
                                newStrVal += scArr[i];
                        }
                        // Convert the new string to resultValue and replace the variable value in the storage manager
                        ResultValue newRes = new ResultValue(SubClassif.STRING, newStrVal);
                        storageManager.insertVariable(variableStr, newRes);
                    }
                    else if (varIdentifier.structure == Structure.FIXED_ARRAY)
                    { 
                        // Check if subscript is out of bounds
                        if (iSubscript >= Integer.valueOf(varIdentifier.maxSize))
                            error("Subscript in array ref assignment is out of bounds");
                        storageManager.setArrayElem(variableStr, res2, iSubscript, scan.symbolTable);
                    }
                    else
                        error("Invalid structure type for array ref assignment, found structure '%s'", varIdentifier.structure);
                    if (Debugger.bShowAssign)
                    {
                        ArrayList<ResultValue> ourArr = storageManager.getArrayVariable(variableStr);
                        System.out.printf("... Assign result to '%s[%d]' is [ ", variableStr, iSubscript);
                        for (ResultValue arrVal : ourArr)
                            System.out.printf("%s ", arrVal.value);
                        System.out.println("]");                        
                    }
                }
                // Doing primitive assignment
                else
                {
                    if (scan.currentToken.subClassif == SubClassif.IDENTIFIER)
                    {
                        STIdentifier sI = (STIdentifier) scan.symbolTable.getSymbol(scan.currentToken.tokenStr);
                        if (sI == null)
                            error("Variable '%s' undeclared", scan.currentToken.tokenStr);
                        if (sI.structure != Structure.PRIMITIVE)
                            error("Cannot assign a array to a primitive variable");
                    }
                    res2 = expr.expr("", ";");
                    res = assign(variableStr, res2);
                }
                break;
            // Get the sum of the expr and variable and assign that result into the variable
            case "+=":
                // Get the value of the variable
                res = storageManager.getVariableValue(this, variableStr);
                // The variable hasn;t been defined, it might haven't been declared
                if (res == null)
                    error("Attempted to do += operation on variable '%s' when it has not been initialized", variableStr);                
                res2 = expr.expr("", ";");
                // Convert the results into numerics and then add them and assign the value of the result
                nOp2 = new Numeric(this, res2, "+=", "2nd operand");
                nOp1 = new Numeric(this, res, "+=", "1st operand");
                res = assign(variableStr, util.add(nOp1, nOp2));
                break;
            // Get the difference of the expr and variable and assign that result into the variable
            case "-=":
                // Get the value of the variable
                res = storageManager.getVariableValue(this, variableStr);
                // The variable hasn;t been defined, it might haven't been declared
                if (res == null)
                    error("Attempted to do -= operation on variable '%s' when it has not been initialized", variableStr);                   
                res2 = expr.expr("", ";");
                // Convert the results into numerics and then subtract them and assign the value of the result
                nOp2 = new Numeric(this, res2, "+=", "2nd operand");             
                nOp1 = new Numeric(this, res, "+=", "1st operand");
                res = assign(variableStr, util.subtract(nOp1, nOp2));
                break;
        }
        return res;
    }
    /**
     * Sets the debug mode to be on or off
     * Assumptions: First token is 'debug'
     * <p>
     * @param bExec determines whether to execute the debug statement or skip it
     * @throws Exception 
     */
    public void setDebugMode(boolean bExec) throws Exception
    {
        // Skip the statement if we aren't executing
        if (!bExec)
            scan.skipTo(";");
        // We are executing the debug stmt
        else
        {
            try 
            {
                // Get the debug type
                // could be Expr, Assign, Stmt or Token
                scan.getNext();
                String debugStr = scan.currentToken.tokenStr;
                // If we did not get a valid debug type, we have an error
                if (!debugStr.equals("Expr") && !debugStr.equals("Assign") && !debugStr.equals("Stmt") && !debugStr.equals("Token"))
                    error("Expected a valid debug type after 'debug', found '%s'", debugStr);
                // We expect for that debug type to be 'on' or 'off'
                scan.getNext();
                String debugMode = scan.currentToken.tokenStr;
                if (!debugMode.equals("on") && !debugMode.equals("off"))
                    error("Expected 'on' or 'off' after '%s', found '%s'", debugStr, debugMode);
                // We expect a semi colon
                scan.getNext();
                if (!scan.currentToken.tokenStr.equals(";"))
                    error("Expected a ';' after the debug statement at line %d, column %d", scan.currentToken.iSourceLineNr, scan.currentToken.iColPos);            
                // Set the debug type to be on or off
                switch(debugStr)
                {
                    case "Expr":
                        Debugger.bShowExpr = debugMode.equals("on");
                        break;
                    case "Assign":
                        Debugger.bShowAssign = debugMode.equals("on");
                        break;
                    case "Stmt":
                        scan.prevSourceLine = scan.currentToken.iSourceLineNr - 1;
                        Debugger.bShowStmt = debugMode.equals("on");
                        break;
                    case "Token":
                        Debugger.bShowToken = debugMode.equals("on");
                        break;
                }
            }
            catch (Exception ex)
            {
                throw ex;
            }
        }
    }
    /**
     * Assumptions: Current token is a control variable with subclassif DECLARE
     *              The tokenStr could be 'Int', 'Float', 'Bool' or 'String' as of p3
     * Declares a variable and stores it in the symbol table, if it is also initialized
     * it's value will be put in the storage manager
     * @param bExec - determines if we should execute the stmt or skip
     * @throws Exception
     * @return result of declare
     */
    public ResultValue declareStmt(boolean bExec) throws Exception
    {
        try 
        {
            ResultValue res = new ResultValue();
            // If we are not executing, skip this line
            if (!bExec)
            {
                scan.skipTo(";");
                res.type = SubClassif.SKIPPED;
                return res;
            }
            // Initial dclType
            SubClassif dclType = SubClassif.EMPTY;
            // Name of variable
            String varSymbol = "";
            // Set the dclType based on the token string
            switch(scan.currentToken.tokenStr)
            {
                case "Int":
                    dclType = SubClassif.INTEGER;
                    break;
                case "Float":
                    dclType = SubClassif.FLOAT;
                    break;
                case "String":
                    dclType = SubClassif.STRING;
                    break;
                case "Bool":
                    dclType = SubClassif.BOOLEAN;
                    break;
                case "Date":
                    dclType = SubClassif.DATE;
                    break;
                default:
                    error("Invalid Declare Variable found at column " + scan.currentToken.iColPos + " on line " + scan.currentToken.iSourceLineNr);
            }
            // We expect to get an operand
            scan.getNext();
            if (scan.currentToken.primClassif != Classif.OPERAND)
                error("Expected to find an operand at column %d on line %d, instead found '%s'", scan.currentToken.iColPos, scan.currentToken.iSourceLineNr, scan.currentToken.tokenStr);
            // We expect that operand to be an identifier
            if (scan.currentToken.subClassif != SubClassif.IDENTIFIER && scan.currentToken.subClassif != SubClassif.ARRAY_REF)
                error("Expected operand at column " + scan.currentToken.iColPos + "on line " + scan.currentToken.iSourceLineNr + " to be an identifier or an array reference");
            varSymbol = scan.currentToken.tokenStr;
            Token varToken = new Token(varSymbol, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence);
            // We need to make sure that the variable hasn't been declared yet
            //if (scan.symbolTable.getSymbol(varSymbol) != null)
            //    error("Tried to declare variable '%s' when it has already been declared on line %d", varSymbol, scan.currentToken.iSourceLineNr);
            // Insert the identifier into the symbol table
            if (bExec)
            {
                if (scan.currentToken.subClassif == SubClassif.IDENTIFIER)
                {
                    scan.symbolTable.putSymbol(varSymbol, new STIdentifier(varSymbol, Classif.OPERAND, dclType, Structure.PRIMITIVE));
                    // We need to check if the next token is a '=' or a ';'
                    if (scan.nextToken.primClassif == Classif.OPERATOR)
                    {
                        // If our next token is an operator, we expect it to be a '='
                        if (!scan.nextToken.tokenStr.equals("="))
                            error("Expected operator to be a '='");
                        // Grab the '=' token
                        scan.getNext();
                        // Call assignment stmt function
                        res = assignStmt(varToken, -1, bExec);
                    }
                    // If the next token is a separator, we expect it to be a ';'
                    else if (scan.nextToken.primClassif == Classif.SEPARATOR)
                    {
                        // We expected a ';' to close the statement, instead found another separator which is an error
                        if (!scan.nextToken.tokenStr.equals(";"))
                            error("Expected a ';' at column %d on line %d, instead found '%s'", scan.nextToken.iColPos, scan.nextToken.iSourceLineNr, scan.nextToken.tokenStr);
                        // Grab the semi-colon
                        scan.getNext();
                    }                    
                }
                // Declaration related to arrays
                else if (scan.currentToken.subClassif == SubClassif.ARRAY_REF)
                {
                    // Get the '['
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals("["))
                        error("Expected a '[' after the array ref, instead found '%s'", scan.currentToken.tokenStr);
                    // Counts how many values appear in the valueList --> also used to determine the array's max_size
                    int valCount = 0;
                    String valListStr = "";
                    Numeric n;
                    ArrayList<ResultValue> valList = new ArrayList<>();
                    ResultValue valRes;
                    // We need to check if the array has specified a maxSize, or we need to check for a valueList
                    // Array did not specify a maxSize, so we need to get it from a value list
                    if (scan.nextToken.tokenStr.equals("]"))
                    {
                        // Skip the ']'
                        scan.getNext();
                        // We expect to begin on a '='
                        scan.getNext();
                        if (!scan.currentToken.tokenStr.equals("="))
                            error("Expected to begin on '=' for array declaration when maxSize was not specified in the brackets, instead found '%s'", scan.currentToken.tokenStr);
                        // We should start on the first token of the valueList
                        scan.getNext();
                        // We need to keep grabbing values from the list until we reach ';', if we reach EOF that's an error
                        while (!scan.currentToken.tokenStr.isEmpty() && !scan.currentToken.tokenStr.equals(";"))
                        {
                            switch(scan.currentToken.primClassif)
                            {
                                case OPERAND:
                                    // Check if identifier or constant
                                    if (scan.currentToken.subClassif == SubClassif.IDENTIFIER)
                                    {
                                        valRes = storageManager.getVariableValue(this, scan.currentToken.tokenStr);
                                        if (valRes == null)
                                            error("Variable '%s' has not been declared/initialized", scan.currentToken.tokenStr);
                                    }
                                    else
                                        valRes = scan.currentToken.toResult();
                                    // Check if token type is compatible with the arrays data type
                                    if (dclType == SubClassif.BOOLEAN && !valRes.value.equals("T") && !valRes.value.equals("F"))
                                        error("Expected value in valueList to be a BOOLEAN, instead found '%s'", scan.currentToken.subClassif);
                                    if (dclType ==  SubClassif.INTEGER || dclType == SubClassif.FLOAT)
                                        n = new Numeric(this, valRes, "n/a", "left");
                                    if (dclType == SubClassif.DATE)
                                    {
                                        if (valRes.type != SubClassif.STRING && valRes.type != SubClassif.DATE)
                                            error("Expected value in valueList to be a date, instead found '%s'", valRes.type);
                                        if (!scan.date.isValidDate(valRes.value))
                                            error("'%s' is an invalid date", scan.currentToken.tokenStr);
                                    }                                    
                                    // Add value to array
                                    valList.add(valRes);
                                    // Add value to str in the case bShowAssign has been turned on
                                    valListStr += valRes.value + " ";
                                    // Increment subscript
                                    valCount++;
                                    break;
                                case SEPARATOR:
                                    // We expect a ',' if separator
                                    if (!scan.currentToken.tokenStr.equals(","))
                                        error("Expected a ',' as a separator in the valueList, instead found '%s'", scan.currentToken.tokenStr);
                                    // We want to skip past the ',' so do nothing
                                    break;
                                default:
                                    error("Expected token in valueList to be an operand or separator, instead found '%s'", scan.currentToken.primClassif);
                                    break;
                            }
                            // Get next token
                            scan.getNext();
                        }
                        // If we reach EOF, error
                        if (scan.currentToken.primClassif == Classif.EOF)
                            error("Reached EOF when we should have ended on a ';' for array declaration");
                        // If valCount is 0, error since we didn't get any values and it's probably a syntax error
                        if (valCount <= 0)
                            error("Expected at least 1 value in the valueList for array declaration, found no values");
                        // We expect to end on a ';'
                        if (!scan.currentToken.tokenStr.equals(";"))
                            error("Expected to end on a ';' for array declaration, instead found '%s'", scan.currentToken.tokenStr);
                        // Set the max size of the array in the symbol table
                        scan.symbolTable.putSymbol(varSymbol, new STIdentifier(varSymbol, Classif.OPERAND, dclType, Structure.FIXED_ARRAY, valCount)); 
                        // Store the array in the storage manager
                        storageManager.insertArray(varSymbol, valList, scan.symbolTable);
                        // If debug mode has been turned on
                        if (Debugger.bShowAssign)
                            System.out.printf("... Assign result into '%s' is [ %s]\n", varSymbol, valListStr);
                        
                    }
                    // The array has specified a max size, so we need to find out what is it
                    else
                    {
                        // Max size can be an expr, we can either stop at a = or a ;
                        ResultValue maxSizeRS = expr.expr("=", ";");
                        int maxSize = Integer.valueOf(maxSizeRS.value);
                        // Set the max size of the array in the symbol table
                        scan.symbolTable.putSymbol(varSymbol, new STIdentifier(varSymbol, Classif.OPERAND, dclType, Structure.FIXED_ARRAY, maxSize)); 
                        // Need to check if current token is either a '=' or ';'
                        if (scan.currentToken.tokenStr.equals("="))
                        {
                            // Start on the first value of the valueList
                            scan.getNext();
                            // We need to keep grabbing values from the list until we reach ';', if we reach EOF that's an error
                            while (!scan.currentToken.tokenStr.isEmpty() && !scan.currentToken.tokenStr.equals(";"))
                            {
                                switch(scan.currentToken.primClassif)
                                {
                                    case OPERAND:
                                        // Check if identifier or constant
                                        if (scan.currentToken.subClassif == SubClassif.IDENTIFIER)
                                        {
                                            valRes = storageManager.getVariableValue(this, scan.currentToken.tokenStr);
                                            if (valRes == null)
                                                error("Variable '%s' has not been declared/initialized", scan.currentToken.tokenStr);
                                        }
                                        else
                                            valRes = scan.currentToken.toResult();
                                        // Check if token type is compatible with the arrays data type
                                        if (dclType == SubClassif.BOOLEAN && !valRes.value.equals("T") && !valRes.value.equals("F"))
                                            error("Expected value in valueList to be a BOOLEAN, instead found '%s'", valRes.type);
                                        if (dclType ==  SubClassif.INTEGER || dclType == SubClassif.FLOAT)
                                            n = new Numeric(this, valRes, "n/a", "left");
                                        if (dclType == SubClassif.DATE)
                                        {
                                            if (valRes.type != SubClassif.STRING && valRes.type != SubClassif.DATE)
                                                error("Expected value in valueList to be a date, instead found '%s'", valRes.type);
                                            if (!scan.date.isValidDate(valRes.value))
                                                error("'%s' is an invalid date", scan.currentToken.tokenStr);
                                        }
                                        // Add value to array
                                        valList.add(valRes);
                                        // Add value to str in the case bShowAssign has been turned on
                                        valListStr += valRes.value + " ";
                                        // Increment subscript
                                        valCount++;
                                        break;
                                    case SEPARATOR:
                                        // We expect a ',' if separator
                                        if (!scan.currentToken.tokenStr.equals(","))
                                            error("Expected a ',' as a separator in the valueList, instead found '%s'", scan.currentToken.tokenStr);
                                        // We want to skip past the ',' so do nothing
                                        break;
                                    default:
                                        error("Expected token in valueList to be an operand or separator, instead found '%s'", scan.currentToken.primClassif);
                                        break;
                                }
                                // Get next token
                                scan.getNext();
                            }
                            // If we reach EOF, error
                            if (scan.currentToken.primClassif == Classif.EOF)
                                error("Reached EOF when we should have ended on a ';' for array declaration");
                            // We expect to end on a ';'
                            if (!scan.currentToken.tokenStr.equals(";"))
                                error("Expected to end on a ';' for array declaration, instead found '%s'", scan.currentToken.tokenStr);
                            // Store the array in the storage manager
                            storageManager.insertArray(varSymbol, valList, scan.symbolTable);
                            // If debug mode has been turned on
                            if (Debugger.bShowAssign)
                                System.out.printf("... Assign result into '%s' is [ %s]\n", varSymbol, valListStr);
                            if (valCount < maxSize)
                            {
                                for (; valCount < maxSize; valCount++)
                                {
                                    valList.add(null);
                                }
                            }
                        }
                        else if (scan.currentToken.tokenStr.equals(";"))
                        {
                            int i = 0;
                            // Set default values in array to be null
                            for (i = 0; i < maxSize; i++)
                                valList.add(null);
                            // Store the array in the storage manager
                            storageManager.insertArray(varSymbol, valList, scan.symbolTable);
                        }
                        else
                            error("Expected array declaration to have a '=' or end with a ';'");
                    }
                }
            }
            return res;
        }
        catch (Exception ex) 
        {
            throw ex;
        }
    }
    /**
     * Assigns a result into a variable
     * @param variableStr - name of the variable
     * @param value - the value to be assigned to the variable
     * @return result of assigning value to variable
     * @throws Exception 
     */
    public ResultValue assign(String variableStr, ResultValue value) throws Exception
    {
        // In the case our variable is a number
        Numeric n = new Numeric();
        // Make sure the variable has been declared
        STIdentifier id = (STIdentifier) scan.symbolTable.getSymbol(variableStr);
        if (id == null)
            error("Tried to assign '%s' to '%s', but variable '%s' was not declared", value.value, variableStr, variableStr);
        // If data types are different, INTEGER and FLOAT are allowed
        if (id.dclType != value.type)
        {
            // Convert float to int, then assign to int var
            if (id.dclType == SubClassif.INTEGER && value.type == SubClassif.FLOAT)
            {
                value.type = SubClassif.INTEGER;
                value.value = String.valueOf(n.strToInt(value.value));
            }
            // Convert int to float, then assign to float var
            else if (id.dclType == SubClassif.FLOAT && value.type == SubClassif.INTEGER)
            {
                value.type = SubClassif.FLOAT;
                value.value = String.valueOf(n.strToDouble(value.value));
            }
            else if (id.dclType == SubClassif.DATE && value.type == SubClassif.STRING)
            {
                if (!scan.date.isValidDate(value.value))
                    error("'%s' is an invalid date", value.value);
                value.type = SubClassif.DATE;
            }
            else if (id.dclType == SubClassif.STRING && value.type == SubClassif.DATE)
                value.type = SubClassif.STRING;
            // Otherwise, our identifier's type is either a string, date or boolean
            else
                error("Tried to assign '%s' which is of data type %s to '%s' which is of data type %s", variableStr, id.dclType, value.value, value.type);
        }
        // Update the value in the storageManager
        storageManager.insertVariable(variableStr, value);
        // Display assignment result if debugger mode has been turned on
        if (Debugger.bShowAssign)
            System.out.printf("... Assign result into '%s' is '%s'\n", variableStr, value.value);
        return value;
    }
    /**
     * Executes the print function
     * Assumptions: print is the current token
     * @param bExec - determines whether to execute stmt or not
     * @throws Exception
     */
    public ResultValue showPrint(boolean bExec) throws Exception
    {
        // If not executing, skip stmt
        if (!bExec)
            scan.skipTo(";");
        // Execute print stmt
        else
        {
            // We expect our next token to be a '('
            if (!scan.nextToken.tokenStr.equals("("))
                error("We expect to find a '(' after 'print', found " + scan.currentToken.tokenStr);
            // Get the next token, which is the "("
            scan.getNext();
            // We expect print to have at least 1 argument
            if (scan.nextToken.primClassif != Classif.OPERAND && scan.nextToken.primClassif != Classif.FUNCTION && scan.nextToken.subClassif != SubClassif.UNARY_MINUS && !scan.nextToken.tokenStr.equals("("))
                error("Expected print at line %d to have at least 1 argument", scan.currentToken.iSourceLineNr);
            // Get the expression in the print
            String strToPrint = "";
            ResultValue printRes = new ResultValue();
            // Start on the expr
            scan.getNext();
            // As long as we haven't reached EOF and our next token isn't a ;
            while (!scan.currentToken.tokenStr.isEmpty() && !scan.currentToken.tokenStr.equals(";"))
            {
                printRes = expr.expr(",", ";");
                if (printRes == null)
                    strToPrint += "null";
                else
                    strToPrint += printRes.value;
                if (scan.currentToken.tokenStr.equals(","))
                    strToPrint += " ";
                if (scan.currentToken.tokenStr.equals(")") && scan.nextToken.tokenStr.equals(";"))
                    break;
                scan.getNext();
            }
            if (scan.currentToken.tokenStr.isEmpty())
                error("Expected to find a ')' and then a  ';', instead never found a ')' or ';'");
            if (!scan.currentToken.tokenStr.equals(")"))
                error("Expected a ) for the print, instead found %s", scan.currentToken.tokenStr);
            if (!scan.nextToken.tokenStr.equals(";"))
                error("Expected a ; after the ) for print, instead found %s", scan.currentToken.tokenStr);
            // Skip the ) and ;
            scan.skipTo(";");
            // Execute the print function
            System.out.println(strToPrint);
        }
        return new ResultValue(SubClassif.VOID, ""); 
    }
    /**
     * Assumptions: current token is select
     * @param bExec - whether to execute or not
     * @return result of selectStmt
     * @throws Exception 
     */
    public ResultValue selectStmt(Boolean bExec) throws Exception
    {
        // We expect the current token to be a select
        if (!scan.currentToken.tokenStr.equals("select"))
            error("Expected to start with select in a select statement");
        // Used when if to execute the default in select
        Boolean caseMatched = false;
        // Used to determine if we should execute this case
        Boolean execCase = false;
        // Determine when to execute or not
        Boolean foundBreak = false;
        ResultValue resStmts = new ResultValue();
        // The value that will be compared to the ones in the 'when' stmts
        ResultValue switchVar = new ResultValue();
        // There may be multiple values in a 'when'
        ArrayList<Token> valList = new ArrayList<>();
        // Executing
        if (bExec)
        {
            // We expect to get a STRING, INT, or DATE type variable. We can also get an identifier but
            // if must be a STRING INT or DATE
            scan.getNext();
            if (scan.currentToken.subClassif != SubClassif.STRING && scan.currentToken.subClassif != SubClassif.INTEGER && scan.currentToken.subClassif !=  SubClassif.DATE && scan.currentToken.subClassif != SubClassif.IDENTIFIER)
                error("Expected value/variable after switch to be a string, date, int or an identifier");
            // Make sure variable has been declared and initialized
            if (scan.currentToken.subClassif == SubClassif.IDENTIFIER)
            {
                STIdentifier swST = (STIdentifier) scan.symbolTable.getSymbol(scan.currentToken.tokenStr);
                if (swST == null)
                    error("Variable '%s' in switch was not declared", swST.symbol);
                if (swST.structure != Structure.PRIMITIVE)
                    error("Expected switch variable '%s' to be a primitive, instead it is a '%s'", swST.symbol, swST.structure);
                if (swST.dclType != SubClassif.STRING && swST.dclType != SubClassif.INTEGER && swST.dclType != SubClassif.DATE)
                    error("Expected switch variable '%s' to be a string, integer or date. Instead it is a '%s'", swST.symbol, swST.dclType);
                switchVar = storageManager.getVariableValue(this, swST.symbol);
                if (switchVar == null)
                    error("Switch variable '%s' has not been initialized", swST.symbol);
            }
            // Otherwise it is a constant
            else
                switchVar = scan.currentToken.toResult();
            // We expect a ':' after the value
            scan.getNext();
            if (!scan.currentToken.tokenStr.equals(":"))
                error("Expected a ':' after the switch condition, instead found '%s'", scan.currentToken.tokenStr);
            // Start on the first token of the next statement
            scan.getNext();
            // We expect to either start on a 'when', 'default' or 'endselect'
            if (!scan.currentToken.tokenStr.equals("when") && !scan.currentToken.tokenStr.equals("default") && !scan.currentToken.tokenStr.equals("endselect"))
                error("Expected to start on a 'when', 'default' or 'endselect. Instead, found '%s'", scan.currentToken.tokenStr);
            // If we are on a 'when' statement
            if (scan.currentToken.tokenStr.equals("when"))
            {
                // There may be multiple 'when' statements, so keep executing the 'when' statements until there aren't anymore
                while (scan.currentToken.tokenStr.equals("when"))
                {
                    // We expect a value
                    scan.getNext();
                    if (scan.currentToken.primClassif != Classif.OPERAND)
                        error("Expected to get a operand after 'when', instead found type '%s'", scan.currentToken.primClassif);
                    // Add initial value to the valueList
                    valList.add(new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, 0, 0));
                    // We expect either a : or , after the value
                    if (!scan.nextToken.tokenStr.equals(":") && !scan.nextToken.tokenStr.equals(","))
                        error("Expected a ',' or a ':' after the value for 'when'");
                    scan.getNext();
                    // If ',', we have multiple values in the valueList
                    if (scan.currentToken.tokenStr.equals(","))
                    {
                        if (scan.nextToken.primClassif != Classif.OPERAND)
                            error("Expected an operand after the ',', instead found '%s'", scan.nextToken.primClassif);
                        // start on the operand
                        scan.getNext();
                        // As long as we haven't reached EOF and we haven't reached a ':', keep grabbing values
                        while (!scan.currentToken.tokenStr.equals(":") && !scan.currentToken.tokenStr.isEmpty())
                        {
                            if (scan.currentToken.primClassif == Classif.OPERAND)
                                valList.add(new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, 0, 0)); 
                            else if (scan.currentToken.tokenStr.equals(","))
                            {
                                // We need to make sure there is a value after the ','
                                if (scan.nextToken.primClassif != Classif.OPERAND)
                                    error("Expected an operand after the ',', instead found '%s'", scan.nextToken.primClassif);                                
                            }
                            else
                                error("Expected either a operand or ',' while grabbing values for the valueList when, instead found '%s' which is of CLASSIF '%s'", scan.currentToken.tokenStr, scan.currentToken.primClassif);
                            // Get the next value
                            scan.getNext();
                        }
                    }
                    // We expect a ':' after getting the values
                    if (!scan.currentToken.tokenStr.equals(":"))
                        error("Expected a ':' after getting the values in the valueList, instead found '%s'", scan.currentToken.tokenStr);
                    // Check if our switchVal is in the valList to determine if we should execute
                    for (Token val : valList)
                    {
                        if (switchVar.value.equals(val.tokenStr))
                        {
                            caseMatched = true;
                            break;
                        }
                    }
                    // Start on first token of statement
                    scan.getNext();
                    // If we are executing this case
                    if (caseMatched && !execCase && !foundBreak)
                    {
                        execCase = true;
                        resStmts = executeStmts(true);
                        if (resStmts.terminatingStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                        {
                            foundBreak = true;
                            if (scan.currentToken.tokenStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                            {
                                // expect ';' after break/continue
                                scan.getNext();
                                if (!scan.currentToken.tokenStr.equals(";"))
                                    error("Expected a ';' after the break/continue, instead found '%s'", scan.currentToken.tokenStr);
                                scan.getNext();
                            }
                            resStmts = executeStmts(false);
                        }
                    }
                    // not executing
                    else
                    {
                        resStmts = executeStmts(false);
                        if (resStmts.terminatingStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                        {
                            if (scan.currentToken.tokenStr.equals("break")|| resStmts.terminatingStr.equals("continue"))
                            {
                                // expect ';' after break
                                scan.getNext();
                                if (!scan.currentToken.tokenStr.equals(";"))
                                    error("Expected a ';' after the break/continue, instead found '%s'", scan.currentToken.tokenStr);
                                scan.getNext();
                            }
                            resStmts = executeStmts(false);
                        }                        
                    }
                    // reset execCase back to false for the next when
                    caseMatched = false;
                }
            }
            // If we have a default case
            if (scan.currentToken.tokenStr.equals("default"))
            {
                // We expect a ':' after the default
                scan.getNext();
                if (!scan.currentToken.tokenStr.equals(":"))
                    error("Expected a ':' after the default, instead found '%s'", scan.currentToken.tokenStr);
                // Start on first token of statement
                scan.getNext();
                // If we have execute a 'when' statement, we can just skip the default
                if (execCase)
                {
                    resStmts = executeStmts(false);
                    if (resStmts.terminatingStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                    {
                        if (scan.currentToken.tokenStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                        {
                            // expect ';' after break
                            scan.getNext();
                            if (!scan.currentToken.tokenStr.equals(";"))
                                error("Expected a ';' after the break/continue, instead found '%s'", scan.currentToken.tokenStr);
                            scan.getNext();
                        }
                        resStmts = executeStmts(false);
                    }                
                }
                // Otherwise we want to execute the stmts
                else
                {
                    resStmts = executeStmts(true);
                    if (resStmts.terminatingStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                    {
                        foundBreak = true;
                        if (scan.currentToken.tokenStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                        {
                            // expect ';' after break
                            scan.getNext();
                            if (!scan.currentToken.tokenStr.equals(";"))
                                error("Expected a ';' after the break/continue, instead found '%s'", scan.currentToken.tokenStr);
                            scan.getNext();
                        }
                        resStmts = executeStmts(false);
                    }                    
                }
            }
            // We expect a endselect
            if (!scan.currentToken.tokenStr.equals("endselect"))
                error("Expected a 'endselect' after the select statement, instead found '%s'", scan.currentToken.tokenStr);
            // We expect a ';' after the endselect
            scan.getNext();
            if (!scan.currentToken.tokenStr.equals(";"))
                error("Expected the ';' after the endselect");
            scan.getNext();
        }
        // Not executing select
        else
        {
            // skip to end of select cond
            scan.skipTo(":");
            // start on next stmt
            scan.getNext();
            if (scan.currentToken.tokenStr.equals("when"))
            {
                // There may be multiple 'when' stmts, so skip them all
                while (scan.currentToken.tokenStr.equals("when"))
                {
                    scan.skipTo(":");
                    scan.getNext();
                    resStmts = executeStmts(false);
                    if (resStmts.terminatingStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                    {
                        if (scan.currentToken.tokenStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                        {
                            // expect ';' after break
                            scan.getNext();
                            if (!scan.currentToken.tokenStr.equals(";"))
                                error("Expected a ';' after the break/continue, instead found '%s'", scan.currentToken.tokenStr);
                            scan.getNext();
                        }
                        resStmts = executeStmts(false);
                    }
                }
            }
            // skip default stmt
            if (scan.currentToken.tokenStr.equals("default"))
            {
                scan.skipTo(":");
                scan.getNext();
                resStmts = executeStmts(false); 
                if (resStmts.terminatingStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                {
                    if (scan.currentToken.tokenStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                    {
                        // expect ';' after break
                        scan.getNext();
                        if (!scan.currentToken.tokenStr.equals(";"))
                            error("Expected a ';' after the break/continue, instead found '%s'", scan.currentToken.tokenStr);
                        scan.getNext();
                    }
                    resStmts = executeStmts(false);
                }                
            }
            // After when and default, expect to be on endselect
            if (!scan.currentToken.tokenStr.equals("endselect"))
                error("Expected endselect for select");
            // Expect a ';' after endselect
            scan.getNext();
            if (!scan.currentToken.tokenStr.equals(";"))
                error("Expected a ';' after the endselect");
            scan.getNext();
        }
        return new ResultValue(SubClassif.GOOD, "GOOD");
    }
    /**
     * While loop
     * Assumptions: While is the current token
     * @param bExec - whether to execute while or skip
     * @return result of whileLoop
     * @throws Exception 
     */
    public ResultValue whileStmt(Boolean bExec) throws Exception
    {
        // Store the whileTkn since we need it's iColPos and iSourceLineNr
        Token whileToken = new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, 0, 0);
        // Result of while cond, must be a bool
        ResultValue resCond = new ResultValue();
        // Result of the stmts in the while loop
        ResultValue resStmts = new ResultValue();
        // We are executing while loop
        if (bExec)
        {
            // Evaluate while cond
            resCond = evalCond();
            // Cond is true, execute until it is false
            while (resCond.value.equals("T"))
            {
                // Execute stmts in the while
                resStmts = executeStmts(true);
                // if we encounter a break
                if (resStmts.terminatingStr.equals("break"))
                {
                    // found break in for loop, not from outside source
                    if (scan.currentToken.tokenStr.equals("break"))
                    {
                        // expect next token to be a ';'
                        scan.getNext();
                        if (!scan.currentToken.tokenStr.equals(";"))
                            error("Expected a ';' after the break in for loop");
                        // start on 1st token of next stmt
                        scan.getNext();
                    } 
                    resStmts = executeStmts(false);
                    break;
                }
                // if we encountered a continue
                if (resStmts.terminatingStr.equals("continue"))
                {
                    // found break in for loop, not from outside source
                    if (scan.currentToken.tokenStr.equals("continue"))
                    {
                        // expect next token to be a ';'
                        scan.getNext();
                        if (!scan.currentToken.tokenStr.equals(";"))
                            error("Expected a ';' after the continue in for loop");
                        // start on 1st token of next stmt
                        scan.getNext();
                    } 
                    resStmts = executeStmts(false);                        
                }
                // We expect an endwhile after the stmts in the while
                if (!resStmts.terminatingStr.equals("endwhile"))
                    error("Expected a 'endwhile' for the 'while' at line %d, instead found '%s'", whileToken.iSourceLineNr, resStmts.terminatingStr);
                // We expect to find a ; after the endwhile
                scan.getNext();
                if (!scan.currentToken.tokenStr.equals(";"))
                    error("Expected a ';' after the 'endwhile', instead found '%s'", scan.currentToken.tokenStr);
                // Set the position in the file back to the while loop
                scan.setPosition(whileToken.iSourceLineNr - 1, whileToken.iColPos);
                scan.getNext();
                // We expect the current token to be on the while position
                if (!scan.currentToken.tokenStr.equals("while"))
                    error("Expected current token to be a 'while', instead found '%s'", scan.currentToken.tokenStr);
                // Evaluate the while cond again
                resCond = evalCond();
                
            }
            // Cond returned false, don't execute stmts
            resStmts = executeStmts(false);
            // if we encounter a break
            if (resStmts.terminatingStr.equals("break"))
            {
                // found break in for loop, not from outside source
                if (scan.currentToken.tokenStr.equals("break"))
                {
                    // expect next token to be a ';'
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(";"))
                        error("Expected a ';' after the break in for loop");
                    // start on 1st token of next stmt
                    scan.getNext();
                } 
                resStmts = executeStmts(false);
                //break;
            }
            // if we encountered a continue
            if (resStmts.terminatingStr.equals("continue"))
            {
                // found break in for loop, not from outside source
                if (scan.currentToken.tokenStr.equals("continue"))
                {
                    // expect next token to be a ';'
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(";"))
                        error("Expected a ';' after the continue in for loop");
                    // start on 1st token of next stmt
                    scan.getNext();
                } 
                resStmts = executeStmts(false);                        
            }
            // We expect to find an endwhile after the stmts in the while
            if (!resStmts.terminatingStr.equals("endwhile"))
                error("Expected a 'endwhile' for the 'while' at line %d, instead found '%s'", whileToken.iSourceLineNr, resStmts.terminatingStr);
            // We expect to find a ; after the endwhile
            scan.getNext();
            if (!scan.currentToken.tokenStr.equals(";"))
                error("Expected a ';' after the 'endwhile', instead found '%s'", scan.currentToken.tokenStr); 
            scan.getNext();
        }
        // Otherwise, ignore execution
        else
        {
            // Skip the cond
            scan.skipTo(":");
            // Start the current token on the start of the next statement
            scan.getNext();
            // Don't execute statements
            resStmts = executeStmts(false);
            // if we encounter a break
            if (resStmts.terminatingStr.equals("break"))
            {
                // found break in for loop, not from outside source
                if (scan.currentToken.tokenStr.equals("break"))
                {
                    // expect next token to be a ';'
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(";"))
                        error("Expected a ';' after the break in for loop");
                    // start on 1st token of next stmt
                    scan.getNext();
                } 
                resStmts = executeStmts(false);
                //break;
            }
            // if we encountered a continue
            if (resStmts.terminatingStr.equals("continue"))
            {
                // found break in for loop, not from outside source
                if (scan.currentToken.tokenStr.equals("continue"))
                {
                    // expect next token to be a ';'
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(";"))
                        error("Expected a ';' after the continue in for loop");
                    // start on 1st token of next stmt
                    scan.getNext();
                } 
                resStmts = executeStmts(false);                        
            }
            // We expect to find an endwhile
            if (!resStmts.terminatingStr.equals("endwhile"))
                error("Expected a 'endwhile' for the 'while' at line %d, instead found '%s'", whileToken.iSourceLineNr, resStmts.terminatingStr);
            // We expect to find a ; after the endwhile
            scan.getNext();
            if (!scan.currentToken.tokenStr.equals(";"))
                error("Expected a ';' after the 'endwhile', instead found '%s'", scan.currentToken.tokenStr);
            scan.getNext();
        }
        return resStmts;
    }
    /**
     * Executes for statement
     * Assumption: current token is 'for'
     * @param bExec - determines whether to execute the statement
     * @return 
     */
    public ResultValue forStmt(Boolean bExec) throws Exception
    {
        // Store the forTkn since we need it's iColPos and iSourceLineNr
        Token forToken = new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence);
        // Result of for cond, must be a bool
        ResultValue resCond = new ResultValue();
        // Result of the stmts in the while loop
        ResultValue resStmts = new ResultValue();
        // Execute the for if executing
        if (bExec)
        {
            // Get the control variable
            scan.getNext();
            // We expect the control variable to be an identifier
            if (scan.currentToken.subClassif != SubClassif.IDENTIFIER)
                error("Expected control variable to be an identifier");
            Token controlValTkn = new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence);
            // If the next token is a '=', we are doing an incrementing for loop
            if (scan.nextToken.tokenStr.equals("="))
            {
                // the default increment value will be 1
                int inc = 1;
                // grab the '='
                scan.getNext();
                // grab the sv
                scan.getNext();
                Token svTkn = new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence);
                // We expect the next token to be a '='
                if (!scan.nextToken.tokenStr.equals("to"))
                    error("Expected a 'to' after the sv in the for loop");
                // skip the 'to'
                scan.getNext();
                // get the endValue
                scan.getNext();
                ResultValue limitRes = expr.expr("by", ":");
                int limit = Integer.valueOf(limitRes.value);
                // There can be an optional 'by' after the endvalue
                if (scan.currentToken.tokenStr.equals("by"))
                {
                    ResultValue incrRes = new ResultValue();
                    // we expect the next token to be an INT and to be positive
                    scan.getNext();
                    if (scan.currentToken.subClassif == SubClassif.IDENTIFIER)
                        incrRes = storageManager.getVariableValue(this, scan.currentToken.tokenStr);
                    else
                        incrRes = scan.currentToken.toResult();
                    if (incrRes.type != SubClassif.INTEGER)
                        error("Expected incr value '%s' to be an integer, instead found '%s'", scan.currentToken.tokenStr, scan.currentToken.subClassif);
                    int incV = Integer.valueOf(incrRes.value);
                    if (incV < 0)
                        error("The increment value must be a positive number");
                    inc = incV;
                    // we expect to be on a ':'
                    scan.getNext();
                }
                if (!scan.currentToken.tokenStr.equals(":"))
                    error("Expected a ':' after the for loop condition, instead found '%s'", scan.currentToken.tokenStr);
                
                // set the control variable to the sv
                scan.symbolTable.putSymbol(controlValTkn.tokenStr, new STIdentifier(controlValTkn.tokenStr, Classif.OPERAND, svTkn.subClassif, Structure.PRIMITIVE));
                storageManager.insertVariable(controlValTkn.tokenStr, svTkn.toResult());
                int cvVal = Integer.valueOf(svTkn.tokenStr);
                // Grab the first token of the statement
                scan.getNext();
                while (cvVal < limit)
                {
                    // Execute stmts
                    resStmts = executeStmts(true);
                    // if we encounter a break
                    if (resStmts.terminatingStr.equals("break"))
                    {
                        // found break in for loop, not from outside source
                        if (scan.currentToken.tokenStr.equals("break"))
                        {
                            // expect next token to be a ';'
                            scan.getNext();
                            if (!scan.currentToken.tokenStr.equals(";"))
                                error("Expected a ';' after the break in for loop");
                            // start on 1st token of next stmt
                            scan.getNext();
                        } 
                        resStmts = executeStmts(false);
                        break;
                    }
                    // if we encountered a continue
                    if (resStmts.terminatingStr.equals("continue"))
                    {
                        // found break in for loop, not from outside source
                        if (scan.currentToken.tokenStr.equals("continue"))
                        {
                            // expect next token to be a ';'
                            scan.getNext();
                            if (!scan.currentToken.tokenStr.equals(";"))
                                error("Expected a ';' after the continue in for loop");
                            // start on 1st token of next stmt
                            scan.getNext();
                        } 
                        resStmts = executeStmts(false);                        
                    }
                    // we expect to be at endfor
                    if (!resStmts.terminatingStr.equals("endfor"))
                        error("Expected to be at 'endfor' after executing statements in for loop, instead found '%s'", scan.currentToken.tokenStr);
                    // we expect to be at a ';'
                    scan.getNext();
                     if (!scan.currentToken.tokenStr.equals(";"))
                        error("Expected '; after the endfor, instead found '%s'", scan.currentToken.tokenStr);
                     // increment the cvV by inc and store the value
                     cvVal += inc;
                     storageManager.insertVariable(controlValTkn.tokenStr, new ResultValue(svTkn.subClassif, String.valueOf(cvVal)));
                     // Go back to the beginning of the for
                    scan.setPosition(forToken.iSourceLineNr - 1, forToken.iColPos);
                    scan.getNext();
                    // We expect the current token to be on the for position
                    if (!scan.currentToken.tokenStr.equals("for"))
                        error("Expected current token to be a 'for', instead found '%s'", scan.currentToken.tokenStr);
                    // Skip to ':' and start on first token of the statements
                    scan.skipTo(":");
                    scan.getNext();
                }
                // Condition is false
                resStmts = executeStmts(false);
                // we expect to be at endfor
                if (!resStmts.terminatingStr.equals("endfor"))
                    error("Expected to be at 'endfor' after executing statements in for loop, instead found '%s'", scan.currentToken.tokenStr);
                // we expect to be at a ';'
                scan.getNext();
                if (!scan.currentToken.tokenStr.equals(";"))
                    error("Expected '; after the endfor, instead found '%s'", scan.currentToken.tokenStr);
                scan.getNext();
            }
            // Otherwise, we are looping through a str / array
            else if (scan.nextToken.tokenStr.equals("in"))
            {
                // skip the in
                scan.getNext();
                // grab the array or string
                scan.getNext();
                Token arrOrStr = new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence);
                // Could either be an array or string
                if (arrOrStr.subClassif == SubClassif.IDENTIFIER)
                {
                    STIdentifier stI = (STIdentifier) scan.symbolTable.getSymbol(arrOrStr.tokenStr);
                    if (stI.structure == Structure.FIXED_ARRAY)
                    {
                        // get the array
                        ArrayList<ResultValue> arrL = storageManager.getArrayVariable(stI.symbol);
                        // store the control variable
                        scan.symbolTable.putSymbol(controlValTkn.tokenStr, new STIdentifier(controlValTkn.tokenStr, Classif.OPERAND, stI.dclType, Structure.PRIMITIVE));
                        // grab the ':'
                        scan.getNext();
                        // We expect a ':' after the for condition
                        if (!scan.currentToken.tokenStr.equals(":"))
                            error("Expected ':' after the for condition");
                        // start on first token of stmt
                        scan.getNext();
                        // Iterate through the array
                        for (ResultValue rr : arrL)
                        {
                            // if the value is null we don't want to execute stmts
                            if (rr == null)
                                break;
                            storageManager.insertVariable(controlValTkn.tokenStr, rr);
                            resStmts = executeStmts(true);
                            // if we encounter a break
                            if (resStmts.terminatingStr.equals("break"))
                            {
                                // found break in for loop, not from outside source
                                if (scan.currentToken.tokenStr.equals("break"))
                                {
                                    // expect next token to be a ';'
                                    scan.getNext();
                                    if (!scan.currentToken.tokenStr.equals(";"))
                                        error("Expected a ';' after the break in for loop");
                                    // start on 1st token of next stmt
                                    scan.getNext();
                                }
                                resStmts = executeStmts(false);
                                break;
                            }
                            // if we encountered a continue
                            if (resStmts.terminatingStr.equals("continue"))
                            {
                                // found break in for loop, not from outside source
                                if (scan.currentToken.tokenStr.equals("continue"))
                                {
                                    // expect next token to be a ';'
                                    scan.getNext();
                                    if (!scan.currentToken.tokenStr.equals(";"))
                                        error("Expected a ';' after the continue in for loop");
                                    // start on 1st token of next stmt
                                    scan.getNext();
                                } 
                                resStmts = executeStmts(false);                        
                            }                            
                            // we expect to be at endfor
                            if (!resStmts.terminatingStr.equals("endfor"))
                                error("Expected to be at 'endfor' after executing statements in for loop, instead found '%s'", scan.currentToken.tokenStr);
                            // we expect to be at a ';'
                            scan.getNext();
                            if (!scan.currentToken.tokenStr.equals(";"))
                                error("Expected '; after the endfor, instead found '%s'", scan.currentToken.tokenStr);
                            // Go back to the beginning of the for
                            scan.setPosition(forToken.iSourceLineNr - 1, forToken.iColPos);
                            scan.getNext();
                            // We expect the current token to be on the for position
                            if (!scan.currentToken.tokenStr.equals("for"))
                                error("Expected current token to be a 'for', instead found '%s'", scan.currentToken.tokenStr);
                            // Skip to ':' and start on first token of the statements
                            scan.skipTo(":");
                            scan.getNext();
                        } 
                       // Condition is false
                       resStmts = executeStmts(false);
                       // we expect to be at endfor
                       if (!resStmts.terminatingStr.equals("endfor"))
                           error("Expected to be at 'endfor' after executing statements in for loop, instead found '%s'", scan.currentToken.tokenStr);
                       // we expect to be at a ';'
                       scan.getNext();
                       if (!scan.currentToken.tokenStr.equals(";"))
                           error("Expected '; after the endfor, instead found '%s'", scan.currentToken.tokenStr);
                       scan.getNext();                          
                    }
                    else if (stI.structure == Structure.PRIMITIVE)
                    {
                        if (arrOrStr.subClassif != SubClassif.STRING)
                            error("Expected primitive type to be a STRING");
                        // convert string to char array 
                       char cArr[] = arrOrStr.tokenStr.toCharArray();
                       // store the control variable
                       scan.symbolTable.putSymbol(controlValTkn.tokenStr, new STIdentifier(controlValTkn.tokenStr, Classif.OPERAND, SubClassif.STRING, Structure.PRIMITIVE));
                       if (cArr.length <= 0)
                           storageManager.insertVariable(controlValTkn.tokenStr, new ResultValue(SubClassif.STRING, ""));
                       // grab the ':'
                       scan.getNext();
                       if (!scan.currentToken.tokenStr.equals(":"))
                           error("Expected ':' after the for condition");
                       // start on first token of stmt
                       scan.getNext();
                       // Iterate through loop
                       for (char c : cArr)
                       {
                           storageManager.insertVariable(controlValTkn.tokenStr, new ResultValue(SubClassif.STRING, String.valueOf(c)));
                           resStmts = executeStmts(true);
                            // if we encounter a break
                            if (resStmts.terminatingStr.equals("break"))
                            {
                                // found break in for loop, not from outside source
                                if (scan.currentToken.tokenStr.equals("break"))
                                {
                                    // expect next token to be a ';'
                                    scan.getNext();
                                    if (!scan.currentToken.tokenStr.equals(";"))
                                        error("Expected a ';' after the break in for loop");
                                    // start on 1st token of next stmt
                                    scan.getNext();
                                }
                                resStmts = executeStmts(false);
                                break;
                            }
                            // if we encountered a continue
                            if (resStmts.terminatingStr.equals("continue"))
                            {
                                // found break in for loop, not from outside source
                                if (scan.currentToken.tokenStr.equals("continue"))
                                {
                                    // expect next token to be a ';'
                                    scan.getNext();
                                    if (!scan.currentToken.tokenStr.equals(";"))
                                        error("Expected a ';' after the continue in for loop");
                                    // start on 1st token of next stmt
                                    scan.getNext();
                                } 
                                resStmts = executeStmts(false);                        
                            }                            
                           // we expect to be at endfor
                           if (!resStmts.terminatingStr.equals("endfor"))
                               error("Expected to be at 'endfor' after executing statements in for loop, instead found '%s'", scan.currentToken.tokenStr);
                           // we expect to be at a ';'
                           scan.getNext();
                           if (!scan.currentToken.tokenStr.equals(";"))
                               error("Expected '; after the endfor, instead found '%s'", scan.currentToken.tokenStr);
                           // Go back to the beginning of the for
                           scan.setPosition(forToken.iSourceLineNr - 1, forToken.iColPos);
                           scan.getNext();
                           // We expect the current token to be on the for position
                           if (!scan.currentToken.tokenStr.equals("for"))
                               error("Expected current token to be a 'for', instead found '%s'", scan.currentToken.tokenStr);
                           // Skip to ':' and start on first token of the statements
                           scan.skipTo(":");
                           scan.getNext();
                       }
                       // Condition is false
                       resStmts = executeStmts(false);
                       // we expect to be at endfor
                       if (!resStmts.terminatingStr.equals("endfor"))
                           error("Expected to be at 'endfor' after executing statements in for loop, instead found '%s'", scan.currentToken.tokenStr);
                       // we expect to be at a ';'
                       scan.getNext();
                       if (!scan.currentToken.tokenStr.equals(";"))
                           error("Expected '; after the endfor, instead found '%s'", scan.currentToken.tokenStr);
                       scan.getNext();                          
                    }
                }
                // String for iteration
                else if (arrOrStr.subClassif == SubClassif.STRING)
                {
                    // convert string to char array 
                    char cArr[] = arrOrStr.tokenStr.toCharArray();
                    // store the control variable
                    scan.symbolTable.putSymbol(controlValTkn.tokenStr, new STIdentifier(controlValTkn.tokenStr, Classif.OPERAND, SubClassif.STRING, Structure.PRIMITIVE));
                    if (cArr.length <= 0)
                        storageManager.insertVariable(controlValTkn.tokenStr, new ResultValue(SubClassif.STRING, ""));
                    // grab the ':'
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(":"))
                        error("Expected ':' after the for condition");
                    // start on first token of stmt
                    scan.getNext();
                    // Iterate through loop
                    for (char c : cArr)
                    {
                        storageManager.insertVariable(controlValTkn.tokenStr, new ResultValue(SubClassif.STRING, String.valueOf(c)));
                        resStmts = executeStmts(true);
                        // if we encounter a break
                        if (resStmts.terminatingStr.equals("break"))
                        {
                            // found break in for loop, not from outside source
                            if (scan.currentToken.tokenStr.equals("break"))
                            {
                                // expect next token to be a ';'
                                scan.getNext();
                                if (!scan.currentToken.tokenStr.equals(";"))
                                    error("Expected a ';' after the break in for loop");
                                // start on 1st token of next stmt
                                scan.getNext();
                            }
                            resStmts = executeStmts(false);
                            break;
                        }
                        // if we encountered a continue
                        if (resStmts.terminatingStr.equals("continue"))
                        {
                            // found break in for loop, not from outside source
                            if (scan.currentToken.tokenStr.equals("continue"))
                            {
                                // expect next token to be a ';'
                                scan.getNext();
                                if (!scan.currentToken.tokenStr.equals(";"))
                                    error("Expected a ';' after the continue in for loop");
                                // start on 1st token of next stmt
                                scan.getNext();
                            } 
                            resStmts = executeStmts(false);                        
                        }                        
                        // we expect to be at endfor
                        if (!resStmts.terminatingStr.equals("endfor"))
                            error("Expected to be at 'endfor' after executing statements in for loop, instead found '%s'", scan.currentToken.tokenStr);
                        // we expect to be at a ';'
                        scan.getNext();
                        if (!scan.currentToken.tokenStr.equals(";"))
                            error("Expected '; after the endfor, instead found '%s'", scan.currentToken.tokenStr);
                        // Go back to the beginning of the for
                        scan.setPosition(forToken.iSourceLineNr - 1, forToken.iColPos);
                        scan.getNext();
                        // We expect the current token to be on the for position
                        if (!scan.currentToken.tokenStr.equals("for"))
                            error("Expected current token to be a 'for', instead found '%s'", scan.currentToken.tokenStr);
                        // Skip to ':' and start on first token of the statements
                        scan.skipTo(":");
                        scan.getNext();
                    }
                    // Condition is false
                    resStmts = executeStmts(false);
                    // we expect to be at endfor
                    if (!resStmts.terminatingStr.equals("endfor"))
                        error("Expected to be at 'endfor' after executing statements in for loop, instead found '%s'", scan.currentToken.tokenStr);
                    // we expect to be at a ';'
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(";"))
                        error("Expected '; after the endfor, instead found '%s'", scan.currentToken.tokenStr);
                    scan.getNext();                    
                }
                // Otherwise error, we expect either an identifier or string
                else
                    error("Expected an array or string in for condition, instead found '%s' which is of type '%s'", scan.currentToken.tokenStr, scan.currentToken.subClassif);
            }
            else if (scan.nextToken.tokenStr.equals("from"))
            {
                // Get the from
                scan.getNext();
                // Default string delimiter is by space
                String defDelim = " ";
                // Check if the control variable has already been declared, and if it is it's type must be string
                STIdentifier strI = (STIdentifier) scan.symbolTable.getSymbol(controlValTkn.tokenStr);
                if (strI != null)
                {
                    if (strI.structure != Structure.PRIMITIVE)
                        error("Expected control variable to be a primitive variable, instead it is a '%s'", strI.structure);
                    if (strI.dclType != SubClassif.STRING)
                        error("Expected '%s' to be a string in a 'for from loop', instead found type '%s'", controlValTkn.tokenStr, strI.dclType);
                }
                // skip the from
                scan.getNext();
                // the current token should either be a string or identifier
                if (scan.currentToken.subClassif != SubClassif.STRING && scan.currentToken.subClassif != SubClassif.IDENTIFIER)
                    error("Expected '%s' to either be a string or identifier in 'for from loop', instead found type '%s'", scan.currentToken.tokenStr, scan.currentToken.subClassif);
                ResultValue svRes = new ResultValue();
                Token svTkn = new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence);
                // if svTkn is an identifier, it must be a PRIMITIVE and a STRING
                if (svTkn.subClassif == SubClassif.IDENTIFIER)
                {
                    STEntry sSV = scan.symbolTable.getSymbol(svTkn.tokenStr);
                    STIdentifier strSV = (STIdentifier) sSV;
                    if (strSV != null)
                    {
                        if (strSV.structure != Structure.PRIMITIVE)
                            error("Expected control variable to be a primitive variable, instead it is a '%s'", strSV.structure);
                        if (strSV.dclType != SubClassif.STRING)
                            error("Expected '%s' to be a string in a 'for from loop', instead found type '%s'", svTkn.tokenStr, strSV.dclType);
                        svRes = storageManager.getVariableValue(this, svTkn.tokenStr);
                        // if it has been initialized, but not declared. 
                        if (svRes == null)
                            error("Expected the string '%s' in the 'for from' to have been intialized", svTkn.tokenStr);
                    }
                    else
                        error("Identifier '%s' has not been declared/initialized", svTkn.tokenStr);
                }
                else
                    svRes = scan.currentToken.toResult();
                // We should either get a 'by', or a ':'
                scan.getNext();
                // If we have an optional 'by' that changes our delimiter
                if (scan.currentToken.tokenStr.equals("by"))
                {
                    // skip the 'by' and start on the delimiter
                    scan.getNext();
                    Token newDelim = new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence);
                    if (newDelim.subClassif != SubClassif.STRING && newDelim.subClassif != SubClassif.IDENTIFIER)
                        error("Expected the delimiter in 'for from loop' to either be a string or an identifier, instead found type '%s'", newDelim.subClassif);
                    // If newDelim is a identifier, it must be a primitive and of type STRING
                    if (newDelim.subClassif == SubClassif.IDENTIFIER)
                    {
                        STIdentifier delimI = (STIdentifier) scan.symbolTable.getSymbol(newDelim.tokenStr);
                        if (delimI != null)
                        {
                            if (delimI.structure != Structure.PRIMITIVE)
                                error("Expected control variable to be a primitive variable, instead it is a '%s'", delimI.structure);
                            if (delimI.dclType != SubClassif.STRING)
                                error("Expected delimiter '%s' to be a string in a 'for from loop', instead found type '%s'", newDelim.tokenStr, delimI.dclType);
                        }   
                        else
                            error("Identifier '%s' for delimiter has not been declared yet", newDelim.tokenStr);
                        if (storageManager.getVariableValue(this, scan.currentToken.tokenStr) == null)
                            error("Expected string delmiter to have been initialized");
                        defDelim = storageManager.getVariableValue(this, scan.currentToken.tokenStr).value;
                    }
                    else
                        defDelim = newDelim.tokenStr;
                    // Should get the ':'
                    scan.getNext();
                }
                // We expect a ':'
                if (!scan.currentToken.tokenStr.equals(":"))
                    error("Expected a ':' to close the for from statement, instead found '%s'", scan.currentToken.tokenStr);
                // start on the first token of the statement following the for condition
                scan.getNext();
                // Split the string into an array based on the delimiter
                String strM[] = svRes.value.split(defDelim);
                // Store the control variable in the symbol table and storageManager
                scan.symbolTable.putSymbol(controlValTkn.tokenStr, new STIdentifier(controlValTkn.tokenStr, Classif.OPERAND, SubClassif.STRING, Structure.PRIMITIVE));
                //storageManager.insertVariable(controlValTkn.tokenStr, new ResultValue(SubClassif.STRING, strM[0]));
                // Iterate through string array
                for (String s : strM)
                {
                    storageManager.insertVariable(controlValTkn.tokenStr, new ResultValue(SubClassif.STRING, s));
                    resStmts = executeStmts(true);
                    // if we encounter a break
                    if (resStmts.terminatingStr.equals("break"))
                    {
                        // found break in for loop, not from outside source
                        if (scan.currentToken.tokenStr.equals("break"))
                        {
                            // expect next token to be a ';'
                            scan.getNext();
                            if (!scan.currentToken.tokenStr.equals(";"))
                                error("Expected a ';' after the break in for loop");
                            // start on 1st token of next stmt
                            scan.getNext();
                        }
                        resStmts = executeStmts(false);
                        break;
                    }
                    // if we encountered a continue
                    if (resStmts.terminatingStr.equals("continue"))
                    {
                        // found break in for loop, not from outside source
                        if (scan.currentToken.tokenStr.equals("continue"))
                        {
                            // expect next token to be a ';'
                            scan.getNext();
                            if (!scan.currentToken.tokenStr.equals(";"))
                                error("Expected a ';' after the continue in for loop");
                            // start on 1st token of next stmt
                            scan.getNext();
                        } 
                        resStmts = executeStmts(false);                        
                    }                        
                    
                    // we expect to be at endfor
                    if (!resStmts.terminatingStr.equals("endfor"))
                        error("Expected to be at 'endfor' after executing statements in for loop, instead found '%s'", scan.currentToken.tokenStr);
                    // we expect to be at a ';'
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(";"))
                        error("Expected '; after the endfor, instead found '%s'", scan.currentToken.tokenStr);
                    // Go back to the beginning of the for
                    scan.setPosition(forToken.iSourceLineNr - 1, forToken.iColPos);
                    scan.getNext();
                    // We expect the current token to be on the for position
                    if (!scan.currentToken.tokenStr.equals("for"))
                        error("Expected current token to be a 'for', instead found '%s'", scan.currentToken.tokenStr);
                    // Skip to ':' and start on first token of the statements
                    scan.skipTo(":");
                    scan.getNext();                    
                    
                }
                // Condition is false
                resStmts = executeStmts(false);
                // if we encounter a break
                if (resStmts.terminatingStr.equals("break"))
                {
                    // found break in for loop, not from outside source
                    if (scan.currentToken.tokenStr.equals("break"))
                    {
                        // expect next token to be a ';'
                        scan.getNext();
                        if (!scan.currentToken.tokenStr.equals(";"))
                            error("Expected a ';' after the break in for loop");
                        // start on 1st token of next stmt
                        scan.getNext();
                    }
                    resStmts = executeStmts(false);
                }
                // if we encountered a continue
                if (resStmts.terminatingStr.equals("continue"))
                {
                    // found break in for loop, not from outside source
                    if (scan.currentToken.tokenStr.equals("continue"))
                    {
                        // expect next token to be a ';'
                        scan.getNext();
                        if (!scan.currentToken.tokenStr.equals(";"))
                            error("Expected a ';' after the continue in for loop");
                        // start on 1st token of next stmt
                        scan.getNext();
                    } 
                    resStmts = executeStmts(false);                        
                }    
                // we expect to be at endfor
                if (!resStmts.terminatingStr.equals("endfor"))
                    error("Expected to be at 'endfor' after executing statements in for loop, instead found '%s'", scan.currentToken.tokenStr);
                // we expect to be at a ';'
                scan.getNext();
                if (!scan.currentToken.tokenStr.equals(";"))
                    error("Expected '; after the endfor, instead found '%s'", scan.currentToken.tokenStr);
                scan.getNext();                  
            }
            // Error since we expected to find '=', 'from' or 'in'
            else
                error("Expected to find '=', 'from' or 'in' after the control variable in for loop");
        }
        // Not executing the for, skip it
        else
        {
            // skip to the ':'
            scan.skipTo(":");
            // Start on the first token of the stmts
            scan.getNext();
            // Don't execute statements
            resStmts = executeStmts(false);
            // if we encounter a break
            if (resStmts.terminatingStr.equals("break"))
            {
                // found break in for loop, not from outside source
                if (scan.currentToken.tokenStr.equals("break"))
                {
                    // expect next token to be a ';'
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(";"))
                        error("Expected a ';' after the break in for loop");
                    // start on 1st token of next stmt
                    scan.getNext();
                }
                resStmts = executeStmts(false);
            }
            // if we encountered a continue
            if (resStmts.terminatingStr.equals("continue"))
            {
                // found break in for loop, not from outside source
                if (scan.currentToken.tokenStr.equals("continue"))
                {
                    // expect next token to be a ';'
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(";"))
                        error("Expected a ';' after the continue in for loop");
                    // start on 1st token of next stmt
                    scan.getNext();
                } 
                resStmts = executeStmts(false);                        
            }            
            // We expect to find an endfor
            if (!resStmts.terminatingStr.equals("endfor"))
                error("Expected a 'endfor' for the 'for' at line %d, instead found '%s'", forToken.iSourceLineNr, resStmts.terminatingStr);
            // We expect to find a ; after the endfor
            scan.getNext();
            if (!scan.currentToken.tokenStr.equals(";"))
                error("Expected a ';' after the 'endfor', instead found '%s'", scan.currentToken.tokenStr);
            scan.getNext();            
        }
        return new ResultValue(SubClassif.GOOD, "GOOD");
    }
    /**
     * Executes If statement
     * Assumption: current token is an 'if'
     * @param bExec - whether to execute or not
     * @return result of ifStmt
     */
    public ResultValue ifStmt(Boolean bExec) throws Exception
    {
        // The line where the if begins
        int ifLineNr = scan.currentToken.iSourceLineNr;
        // Result of if
        ResultValue res = new ResultValue();
        // Result of stmts within if
        ResultValue resStmts = new ResultValue();
        // Execute if stmt
        if (bExec)
        {
            // Evaluate if cond
            res = evalCond();
            // If cond returned true, continue executing
            if (res.value.equals("T"))
            {
                resStmts = executeStmts(true);
                // if we found a break/continue
                if (resStmts.terminatingStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                {
                    if (resStmts.terminatingStr.equals("break"))
                        res = new ResultValue(SubClassif.BREAK, "break");
                    else
                        res = new ResultValue(SubClassif.CONTINUE, "continue");
                    // if we encountered a break
                    if (scan.currentToken.tokenStr.equals("break") || scan.currentToken.tokenStr.equals("continue"))
                    {
                        
                        scan.getNext();
                        // expect ';' after break/continue
                        if (!scan.currentToken.tokenStr.equals(";"))
                            error("Expected a ';' after the break/continue");
                        // start on first token of next stmt
                        scan.getNext();
                    }
                    // don't execute stmts
                    resStmts = executeStmts(false);
                }
                // If we found an 'else' instead of an 'endif'
                if (resStmts.terminatingStr.equals("else"))
                {
                    // We expect to get a ":"
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(":"))
                        error("Expected ':' after 'else', instead found '%s'", scan.currentToken.tokenStr);
                    // get the next token
                    scan.getNext();  
                    // Since our condition was true, we don't need to execute the else
                    resStmts = executeStmts(false);
                    // if we found a break/continue
                    if (resStmts.terminatingStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                    {
                        //if (resStmts.terminatingStr.equals("break"))
                        //    res = new ResultValue(SubClassif.BREAK, "break");
                        //else
                        //    res = new ResultValue(SubClassif.CONTINUE, "continue");
                        // if we encountered a break
                        if (scan.currentToken.tokenStr.equals("break") || scan.currentToken.tokenStr.equals("continue"))
                        {

                            scan.getNext();
                            // expect ';' after break/continue
                            if (!scan.currentToken.tokenStr.equals(";"))
                                error("Expected a ';' after the break/continue");
                            // start on first token of next stmt
                            scan.getNext();
                        }
                        // don't execute stmts
                        resStmts = executeStmts(false);
                    }
                }
                // We expect to find an 'endif'
                if (!resStmts.terminatingStr.equals("endif"))
                    error("Expected an 'endif' for the 'if' at line %d, instead found '%s'", ifLineNr, resStmts.terminatingStr);
                // Expect to find a ';' after the endif
                scan.getNext();
                if (!scan.currentToken.tokenStr.equals(";"))
                    error("Expected a ';' after the endif, instead found '%s'", scan.currentToken.tokenStr);
                // Get next token
                scan.getNext();                
            }
            // Cond returned false, don't execute
            else
            {
                resStmts = executeStmts(false);
                // if we found a break/continue
                if (resStmts.terminatingStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                {
                    //if (resStmts.terminatingStr.equals("break"))
                    //    res = new ResultValue(SubClassif.BREAK, "break");
                    //else
                    //    res = new ResultValue(SubClassif.CONTINUE, "continue");
                    // if we encountered a break
                    if (scan.currentToken.tokenStr.equals("break") || scan.currentToken.tokenStr.equals("continue"))
                    {
                        
                        scan.getNext();
                        // expect ';' after break/continue
                        if (!scan.currentToken.tokenStr.equals(";"))
                            error("Expected a ';' after the break/continue");
                        // start on first token of next stmt
                        scan.getNext();
                    }
                    // don't execute stmts
                    resStmts = executeStmts(false);
                }
                if (resStmts.terminatingStr.equals("else"))
                {
                    // We expect to get a ":"
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(":"))
                        error("Expected ':' after 'else', instead found '%s'", scan.currentToken.tokenStr);
                    // get the next token
                    scan.getNext();
                    // Since our condition was false, we need to execute the else
                    resStmts = executeStmts(true);
                    // if we found a break/continue
                    if (resStmts.terminatingStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                    {
                        if (resStmts.terminatingStr.equals("break"))
                            res = new ResultValue(SubClassif.BREAK, "break");
                        else
                            res = new ResultValue(SubClassif.CONTINUE, "continue");
                        // if we encountered a break
                        if (scan.currentToken.tokenStr.equals("break") || scan.currentToken.tokenStr.equals("continue"))
                        {

                            scan.getNext();
                            // expect ';' after break/continue
                            if (!scan.currentToken.tokenStr.equals(";"))
                                error("Expected a ';' after the break/continue");
                            // start on first token of next stmt
                            scan.getNext();
                        }
                        // don't execute stmts
                        resStmts = executeStmts(false);
                    }
                }
                // We expect to find an 'endif'
                if (!resStmts.terminatingStr.equals("endif"))
                    error("Expected an 'endif' for the 'if' at line %d, instead found '%s'", ifLineNr, resStmts.terminatingStr);
                // Expect to find a ';' after the endif
                scan.getNext();
                if (!scan.currentToken.tokenStr.equals(";"))
                    error("Expected a ';' after the endif, instead found '%s'", scan.currentToken.tokenStr);   
                // Get next token
                scan.getNext();                
            }
        }
        // Ignore execution of if
        else
        {
            scan.skipTo(":");
            scan.getNext();
            resStmts = executeStmts(false);
            // if we found a break/continue
            if (resStmts.terminatingStr.equals("break") || resStmts.terminatingStr.equals("continue"))
            {
                //if (resStmts.terminatingStr.equals("break"))
                //    res = new ResultValue(SubClassif.BREAK, "break");
                //else
                //    res = new ResultValue(SubClassif.CONTINUE, "continue");
                // if we encountered a break
                if (scan.currentToken.tokenStr.equals("break") || scan.currentToken.tokenStr.equals("continue"))
                {

                    scan.getNext();
                    // expect ';' after break/continue
                    if (!scan.currentToken.tokenStr.equals(";"))
                        error("Expected a ';' after the break/continue");
                    // start on first token of next stmt
                    scan.getNext();
                }
                // don't execute stmts
                resStmts = executeStmts(false);
            }
            if (resStmts.terminatingStr.equals("else"))
            {
                // We expect to get a ":"
                scan.getNext();
                if (!scan.currentToken.tokenStr.equals(":"))
                    error("Expected ':' after 'else', instead found '%s'", scan.currentToken.tokenStr);
                // get the next token
                scan.getNext();
                // Since bExec is false, we don't execute any stmts
                resStmts = executeStmts(false);  
                // if we found a break/continue
                if (resStmts.terminatingStr.equals("break") || resStmts.terminatingStr.equals("continue"))
                {
                    //if (resStmts.terminatingStr.equals("break"))
                    //    res = new ResultValue(SubClassif.BREAK, "break");
                    //else
                    //    res = new ResultValue(SubClassif.CONTINUE, "continue");
                    // if we encountered a break
                    if (scan.currentToken.tokenStr.equals("break") || scan.currentToken.equals("continue"))
                    {
                        
                        scan.getNext();
                        // expect ';' after break/continue
                        if (!scan.currentToken.tokenStr.equals(";"))
                            error("Expected a ';' after the break/continue");
                        // start on first token of next stmt
                        scan.getNext();
                    }
                    // don't execute stmts
                    resStmts = executeStmts(false);
                }
            }
            // We expect to find an 'endif'
            if (!resStmts.terminatingStr.equals("endif"))
                error("Expected an 'endif' for the 'if' at line %d, instead found '%s'", ifLineNr, resStmts.terminatingStr);
            // Expect to find a ';' after the endif
            scan.getNext();
            if (!scan.currentToken.tokenStr.equals(";"))
                error("Expected a ';' after the endif, instead found '%s'", scan.currentToken.tokenStr);                  
            // get the next token
            scan.getNext();
        }
        if (res.terminatingStr != null && (res.terminatingStr.equals("break") || res.terminatingStr.equals("continue")))
            return res;
        else
            return resStmts;
    }
    /**
     * Evals condition of a flow stmt (e.g. if while)
     * Assumption: current token is a operand or unary operator
     * @return result of cond
     */
    public ResultValue evalCond() throws Exception
    {
        ResultValue res = new ResultValue();
        scan.getNext();
        // Get result of expression
        res = expr.expr("", ":");
        // Expect result to be boolean
        if (res.type != SubClassif.BOOLEAN)
            error("Expected result of expression to be a boolean type, instead found '%s'", res.type);
        // Expect ':' after the cond
        if (!scan.currentToken.tokenStr.equals(":"))
            error("Expected a ':' after the if condition, instead found '%s'", scan.currentToken.tokenStr);
        // Get the token after the ':'
        scan.getNext();
        return res;
    }
    /**
     * Executes stmts until we reached an END (e.g., endwhile endif)
     * @param bExec - whether to execute stmts or skip them
     * @return result of executeStmts
     * @throws Exception 
     */
    public ResultValue executeStmts(boolean bExec) throws Exception
    {
        ResultValue res = new ResultValue();
        // As long as we haven't hit an END token to our FLOW, we keep going through each statement
        while (scan.currentToken.subClassif != SubClassif.END && !scan.currentToken.tokenStr.isEmpty())
        {
            res = statements(bExec);
            if (res.type != null && (res.type == SubClassif.BREAK || res.type == SubClassif.CONTINUE)){
                
                break;
            }
        }
        if (res.type != null && (res.type == SubClassif.BREAK || res.type == SubClassif.CONTINUE))
            ;
        else
        {
            // Convert token into a resultvalue
            res = scan.currentToken.toResult();
        }
        return res;
    }
    /**
     * Determines how to parse based on first token
     * Assumption: Starting on first token of stmt
     * @param bExec - whether to execute or skip
     * @return result of parse
     * @throws Exception 
     */
    public ResultValue statements(boolean bExec) throws Exception
    {
        ResultValue res = new ResultValue();
        // If debug mode for stmt has been turned on
        if (Debugger.bShowStmt)
        {
            // We want to print all the lines that were skipped or advanced from until we reach our current line
            while (scan.prevSourceLine != scan.iSourceLineNr && scan.iSourceLineNr != scan.sourceLineM.size())
            {
                scan.prevSourceLine++;
                System.out.println("Line " + (scan.prevSourceLine+1) + ": " + scan.sourceLineM.get(scan.prevSourceLine));
            }
        }
        // Call different parse function based on the token's classification
        // We are assuming we are getting the first token on a line
        switch(scan.currentToken.primClassif)
        {
            // could be a variable identifier, a debug identifier, or a constant
            case OPERAND:
                Token varToken = new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence);
                // If the first token is an operand, it must be an identifier
                if (scan.currentToken.subClassif != SubClassif.IDENTIFIER && scan.currentToken.subClassif != SubClassif.ARRAY_REF)
                    error("Expected operand token to be an identifier or array ref, instead found '%s' which is of type %s", scan.currentToken.tokenStr, scan.currentToken.subClassif);
                // We need to check if the variable has been initialized
                if (scan.symbolTable.getSymbol(scan.currentToken.tokenStr) == null)
                    error("Variable '%s' has not been initialized", scan.currentToken.tokenStr);
                String varStr = scan.currentToken.tokenStr;
                // Subscript is negative by default, negative means identifier assignment, positive means array element assignment
                int iSubScript = -2;                
                // Dealing with array ref assignments
                if (scan.currentToken.subClassif == SubClassif.ARRAY_REF)
                {
                    // start on the '['
                    scan.getNext();
                    ResultValue ResSubScript = expr.expr(";", "=");
                    if (ResSubScript.type != SubClassif.INTEGER)
                        error("Expected array subscript to be an integer, instead found '%s' which is of type '%s'", ResSubScript.value, ResSubScript.type);
                    iSubScript = Integer.valueOf(ResSubScript.value);
                    // Subscript cannot be negative for assignment variables
                    if (iSubScript < 0)
                        error("Subscript for array assignment cannot be negative");
                    // Call assignStmt
                    res = assignStmt(varToken, iSubScript, bExec);
                    scan.getNext();    
                }
                else
                {
                    // Get the assignment operator
                    scan.getNext();
                    // Call assignStmt
                    res = assignStmt(varToken, iSubScript, bExec);
                    scan.getNext();
                }
                break;
            // Could be an if, while, declare or break/continue
            case CONTROL:
                // If DECLARE, call declareStmt
                if (scan.currentToken.subClassif == SubClassif.DECLARE)
                {
                    res = declareStmt(bExec);
                    scan.getNext();
                }
                // If flow, check if or while
                else if (scan.currentToken.subClassif == SubClassif.FLOW)
                {
                    // call if
                    if (scan.currentToken.tokenStr.equals("if"))
                        res = ifStmt(bExec);
                    // call while
                    if (scan.currentToken.tokenStr.equals("while"))
                        res = whileStmt(bExec);
                    // call for
                    if (scan.currentToken.tokenStr.equals("for"))
                        res = forStmt(bExec);
                    // call select
                    if (scan.currentToken.tokenStr.equals("select"))
                        res = selectStmt(bExec);
                }
                else if (scan.currentToken.tokenStr.equals("break"))
                    return new ResultValue(SubClassif.BREAK, "break");
                else if (scan.currentToken.tokenStr.equals("continue"))
                    return new ResultValue(SubClassif.CONTINUE, "continue");
                break;
            case FUNCTION:
                if (scan.currentToken.tokenStr.equals("print"))
                    showPrint(bExec);
                else
                    res = expr.expr("", ";");
                scan.getNext();
                break;
            // Set debug options
            case DEBUG:
                setDebugMode(bExec);
                scan.getNext();
                break;
            default:
                error("Expected token '%s' at column %d at line %d to be an operand, control variable, function or a debug mode setter", scan.currentToken.tokenStr, scan.currentToken.iColPos, scan.currentToken.iSourceLineNr);
        }           
        return res;
    }
  /**
   * Throws an exception
   * @param fmt - string, could have format codes
   * @param varArgs - if there are format codes, these are the args to follow it
   * @throws ParserException 
   */
    public void error(String fmt, Object ... varArgs) throws ParserException
    {
        String diagnosticTxt = String.format(fmt, varArgs);
        throw new ParserException(scan.currentToken.iSourceLineNr
                , diagnosticTxt
                , scan.sourceFileNm);

    }
}
