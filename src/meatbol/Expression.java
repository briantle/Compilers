package meatbol;

import java.util.ArrayList;
import java.util.Stack;

public class Expression 
{
    public Parser p;
    public Scanner scan;
    public Utilities util;
    public StorageManager storageManager;
    /**
     * Expression constructor
     * @param p - parser class object
     */
    public Expression(Parser p)
    {
        this.p = p;
        this.scan = p.scan;
        this.util = p.util;
        this.storageManager = p.storageManager;
    }
    /**
     * Converts an expression from infix to postfix
     * Assumptions: we are on the first token of the expression
     * <p>
     * @param posEndSeparator - a separator that we could potentially end at
     * @param endSeparator - the separator we expect to end at
     * @return an array that represents the expression converted to postfix
     * @throws Exception 
     */
    public ArrayList<Token> infixToPostfix(String posEndSeparator, String endSeparator) throws Exception
    {
        // The final result of converting infix to postfix
        ArrayList<Token> out = new ArrayList<>();
        // Used to store and remove operators based on their precedence
        Stack<Token> stack = new Stack<>();
        // The current token we popped from the stack
        Token popped = new Token();
        Boolean bFoundLBrace = false;
        try 
        {
            // As of pgm 3, we expect the first token in the infix expr to either be an operand, function, '(', '[' or unary minus
            if (scan.currentToken.primClassif != Classif.OPERAND && scan.currentToken.primClassif != Classif.FUNCTION && !scan.currentToken.tokenStr.equals("(") && !scan.currentToken.tokenStr.equals("[") && !scan.currentToken.tokenStr.equals("not") && scan.currentToken.subClassif != SubClassif.UNARY_MINUS)
                p.error("Expected the first token in the infix expr to be a operand, '(', '[' or a unary minus, instead found '%s'", scan.currentToken.tokenStr);
            // Read the tokens from left to right until we've found EOF or we've reached our endSeparator
            while (scan.currentToken.primClassif != Classif.EOF)
            {
                // Only break if the "" is associated with EOF
                if (scan.currentToken.tokenStr.equals(endSeparator))
                {
                    if (endSeparator.equals("") && scan.currentToken.primClassif == Classif.EOF)
                        break;
                   if (!endSeparator.equals(""))
                       break;
                }
                // Only break if the "" is associated with EOF
                else if (scan.currentToken.tokenStr.equals(posEndSeparator))
                {
                    boolean hasFunc = false;
                    for (Token t: stack)
                    {
                        if (t.primClassif == Classif.FUNCTION)
                        {
                            hasFunc = true;
                            break;
                        }
                    }
                    if (posEndSeparator.equals("") && scan.currentToken.primClassif == Classif.EOF)
                        break;
                    if (!posEndSeparator.equals(""))
                    {
                        // If our expr has a function and our possible end separator is a ',', do nothing
                        if (posEndSeparator.equals(",") && hasFunc || bFoundLBrace)
                            ;
                        // other leave loop
                        else
                            break;
                    }
                }
                // Break out of the loop if our posEndSeparator is a , and our next token is a ;. We assume current token is a )
                if (posEndSeparator.equals(",") && scan.nextToken.tokenStr.equals(";"))
                    break;
                // If our next token is a ',' we expect it to be preceded by an operand
                if (scan.nextToken.tokenStr.equals(",") && scan.currentToken.primClassif != Classif.OPERAND && !scan.currentToken.tokenStr.equals(")") && !scan.currentToken.tokenStr.equals("}") && !scan.currentToken.tokenStr.equals("]"))
                    p.error("',' found at column %d on line %d is not preceded by an operand", scan.nextToken.iColPos, (scan.iSourceLineNr+1));
                // If our next token is an binary operator, we expect it to be preceded by an operand or )
                if (scan.nextToken.primClassif == Classif.OPERATOR && scan.nextToken.subClassif != SubClassif.UNARY_MINUS && scan.currentToken.primClassif != Classif.OPERAND && !scan.currentToken.tokenStr.equals(")") && !scan.currentToken.tokenStr.equals("]"))
                    p.error("Operator '%s' found at column %d on line %d is not preceded by an operand, ] or ), instead found '%s'", scan.nextToken.tokenStr, scan.nextToken.iColPos, scan.nextToken.iSourceLineNr, scan.currentToken.tokenStr);                
                // Determine what to do based on the token's primary classification
                switch(scan.currentToken.primClassif)
                {
                    case FUNCTION:
                        // Always add function to the stack
                        stack.push(new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence));
                        // We must make sure that the token after the FUNCTION must be a (
                        scan.getNext();
                        // The token after the FUNCTION wasn't a '(' so it's an error
                        if (!scan.currentToken.tokenStr.equals("("))
                            p.error("Expected a '(' after the function '%s', instead found '%s'", stack.peek().tokenStr, scan.currentToken.tokenStr);
                        // We break, don't do anything with the '(' since we just want to skip it
                        break;
                    case OPERAND:
                        // If our operand is an array reference, we want to throw away the '[' and store the array ref in the stack
                        if (scan.currentToken.subClassif == SubClassif.ARRAY_REF)
                        {
                            stack.push(new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence));                            
                            scan.getNext();
                        }
                        // Otherwise it is not an array ref, add it to the out                        
                        else
                            out.add(new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence));
                        break;
                    case OPERATOR:
                        // We expect our operator to be followed by an operand or unary minus or (
                        if (scan.nextToken.primClassif != Classif.OPERAND && scan.nextToken.subClassif != SubClassif.UNARY_MINUS && scan.nextToken.primClassif != Classif.FUNCTION && !scan.nextToken.tokenStr.equals("(") && !scan.nextToken.tokenStr.equals("{"))
                            p.error("Expected operator '%s' at column %d on line %d to be followed by an operand, FUNCTION, ( or unary minus, found '%s'", scan.currentToken.tokenStr, scan.currentToken.iColPos, (scan.iSourceLineNr+1), scan.nextToken.tokenStr);
                        // If our current operator is a unary minus, we expect it to be followed by an operand or (
                        if (scan.currentToken.subClassif == SubClassif.UNARY_MINUS && scan.nextToken.primClassif != Classif.OPERAND && !scan.nextToken.tokenStr.equals("("))
                            p.error("Expected U- at column %d on line %d to be followed by an operand or (, instead found '%s'", scan.currentToken.iColPos, scan.currentToken.iSourceLineNr, scan.nextToken.tokenStr);
                        // As long as the stack isn't empty, we want to check if our token's precedence is greater than the one in the stack
                        while (!stack.isEmpty())
                        {
                            // If our token's stack precedence is greater the the token in the stack's stack precedence, we don't want to pop anymore tokens
                            if (scan.currentToken.iPrecedence > stack.peek().iStackPrecedence)
                                break;
                            // Remove the token from the stack and add it to the out
                            popped = stack.pop();
                            out.add(popped);
                        }
                        // Add our current token to the stack
                        stack.push(new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence));
                        break;
                    case SEPARATOR:
                        // Based on the separator
                        switch(scan.currentToken.tokenStr)
                        {
                            // Add '[' to the stack
                            case "[":
                                stack.push(new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence));
                                break;    
                            case "]":
                                // determines if we aren't missing a '[' or an array ref
                                boolean bFoundLBorArrRef = false;
                                while (!stack.isEmpty())
                                {
                                    popped = stack.pop();
                                    if (popped.tokenStr.equals("["))
                                    {
                                        bFoundLBorArrRef = true;
                                        break;
                                    }
                                    out.add(popped);
                                    if (popped.subClassif == SubClassif.ARRAY_REF)
                                    {
                                        bFoundLBorArrRef = true;
                                        break;                                        
                                    }
                                }
                                // Didn't find corresponding '[' or array ref
                                if (!bFoundLBorArrRef)
                                    p.error("Expected to find a '[' or array reference after finding a ']'");
                                break;
                            // We always want to add '(' and '{' to the stack
                            case "(":
                                stack.push(new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence));
                                break;
                            case "{":
                                bFoundLBrace = true;
                                stack.push(new Token(scan.currentToken.tokenStr, scan.currentToken.primClassif, scan.currentToken.subClassif, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence));
                                // We want to know when our array starts
                                out.add(new Token("ARRAYSTART", Classif.OPERAND, SubClassif.STRING, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence));
                                break;
                            case "}":
                                // Determines if '}' matches a '{'
                                boolean bMatchedLBrace = false;
                                // As long as stack isn't empty, we want to make sure our '}' matches a '{'
                                while (!stack.isEmpty())
                                {
                                    popped = stack.pop();
                                    // Skip commas
                                    if (popped.tokenStr.equals(","))
                                        continue;
                                    if (popped.tokenStr.equals("{"))
                                    {
                                        // Add a indicator to know when the array ends
                                        out.add(new Token("ARRAYEND", Classif.OPERAND, SubClassif.STRING, scan.currentToken.iSourceLineNr, scan.currentToken.iColPos, scan.currentToken.iPrecedence, scan.currentToken.iStackPrecedence));
                                        bMatchedLBrace = true;
                                        break;
                                    }
                                    out.add(popped);
                                }
                                if (!bMatchedLBrace)
                                    p.error("Missing a '{'");
                                bFoundLBrace = false;
                                break;
                            case ")":
                                // Determines if ')' isn't missing a '('
                                boolean bFoundLPorFunc = false;
                                // As long as the stack isn't empty, we want to make sure our ')' finds a '(' and to store the popped tokens into the out
                                while (!stack.isEmpty())
                                {
                                    popped = stack.pop();
                                    // We found a '(' so we want to break but don't want to include the '(' in the out
                                    if (popped.tokenStr.equals("("))
                                    {
                                        bFoundLPorFunc = true;
                                        break;
                                    }
                                    out.add(popped);
                                    // If we found a FUNCTION we want to include it in the out
                                    if (popped.primClassif == Classif.FUNCTION)
                                    {
                                        bFoundLPorFunc = true;
                                        break;
                                    }                                        
                                }
                                // We haven't found a '(' or func call, which means our ')' is missing a '(' or function
                                if (!bFoundLPorFunc)
                                {
                                    // If we are in a print stmt and we have a ; after the ) do nothing
                                    if (!posEndSeparator.equals(",") && !scan.nextToken.tokenStr.equals(";"))
                                        p.error("Expected to find a '(' or function call after encountering a ')'");
                                }
                                break;
                            case ",":
                                // If we encounter a , when the stack is empty, it's an error
                                if (stack.isEmpty() && !bFoundLBrace)
                                    p.error("Found a ',' that doesn't reside within a function");
                                // As long as the top element isn't a function, keep popping and adding to the out 
                                if (!bFoundLBrace)
                                {
                                    while (!stack.isEmpty() && stack.peek().primClassif != Classif.FUNCTION)
                                    {
                                        popped = stack.pop();
                                        out.add(popped);
                                    }
                                }
                                break;
                        }
                        break;
                }
                // Get the next token in the expr
                scan.getNext();
            }
            // As long as the stack isn't empty, add the remaining tokens in the stack to the out
            while (!stack.isEmpty())
            {
                popped = stack.pop();
                // If we found a '(', we never found a ')' to remove it from the stack
                if (popped.tokenStr.equals("("))
                    p.error("Expression missing a ')'");
                // If we found a '[' or an array ref, we are missing a ']'
                if (popped.tokenStr.equals('[') || popped.subClassif == SubClassif.ARRAY_REF)
                    p.error("Expression missing a ']'");
                out.add(popped);
            }
        }
        catch (ParserException ex)
        {
            throw ex;
        }
        
        for (Token t : out)
            System.out.printf(" %s", t.tokenStr);
        System.out.println("");
        return out;
    }
    /**
     * Evaluates an expr and gets the result of that expr
     * <p>
     * @param posEndSeparator
     * @param endSeparator - the separator we should stop at when converting infix to postfix
     * @return - result of expr 
     * @throws Exception 
     */
    public ResultValue expr(String posEndSeparator, String endSeparator) throws Exception
    {
        // Used to store values in the valueList
        Boolean bFoundARRSTART = false;
        // Result of expr
        ResultValue res;
        // Used when expr involves numeric operations
        Numeric rOp, lOp;
        // Convert infix expr to postfix expr
        ArrayList<Token> postFixOut = infixToPostfix(posEndSeparator, endSeparator);
        Stack<ResultValue> stack = new Stack();
        String arrName = "";
        // Used in IN and NOTIN operations
        ArrayList<ResultValue> valList = null;
        // Loop through each token in the postfix expr
        for (Token token : postFixOut)
        {
            switch (token.primClassif)
            {
                // We want to execute functions
                case FUNCTION:
                    if (token.subClassif == SubClassif.BUILTIN)
                    {
                        ResultValue argRes = new ResultValue();
                        if (!token.tokenStr.equals("ELEM") && !token.tokenStr.equals("MAXELEM"))
                        {
                            // functions require at least 1 argument
                            if (stack.isEmpty())
                            {
                                if (valList.size() > 0)
                                    p.error("Builtin function '%s' does not take in an array", token.tokenStr);
                                p.error("Builtin functions require at least 1 argument, found none");
                            }
                            argRes = stack.pop();
                        }
                        ResultValue argRes2;
                        ResultValue funcRes;
                        // Based on the function name, we want to execute different functions
                        switch (token.tokenStr)
                        {
                            case "LENGTH":
                                funcRes = util.LENGTH(argRes);
                                stack.add(funcRes);
                                if (Debugger.bShowExpr)
                                    System.out.printf("... LENGTH(%s) is %s\n", argRes.value, funcRes.value);
                                break;
                            case "SPACES":
                                funcRes = util.SPACES(argRes);
                                stack.add(funcRes);
                                if (Debugger.bShowExpr)
                                    System.out.printf("... SPACES(%s) is %s\n", argRes.value, funcRes.value);
                                break;
                            case "ELEM":
                                funcRes = util.ELEM(arrName, storageManager);
                                stack.add(funcRes);
                                if (Debugger.bShowExpr)
                                    System.out.printf("... ELEM(%s) is %s\n", argRes.value, funcRes.value);
                                break;
                            case "MAXELEM":
                                funcRes = util.MAXELEM(arrName, p.storageManager, scan.symbolTable);
                                stack.add(funcRes);
                                if (Debugger.bShowExpr)
                                    System.out.printf("... MAXELEM(%s) is %s\n", argRes.value, funcRes.value);
                                break;
                            // argRes will be date2
                            case "dateDiff":
                                if (stack.isEmpty())
                                    p.error("dateDiff requires 2 arguments, instead found 1");
                                // date1
                                argRes2 = stack.pop();
                                funcRes = scan.date.dateDiff(argRes2, argRes);
                                stack.add(funcRes);
                                if (Debugger.bShowExpr)
                                    System.out.printf("... dateDiff(%s, %s) is %s\n", argRes2.value, argRes.value, funcRes.value);
                                break;
                            // argRes will be days
                            case "dateAdj":
                                if (stack.isEmpty())
                                    p.error("dateAdj requires 2 arguments, instead found 1");
                                // date
                                argRes2 = stack.pop();
                                funcRes = scan.date.dateAdj(argRes2, argRes);
                                stack.add(funcRes);      
                                if (Debugger.bShowExpr)
                                    System.out.printf("... dateAdj(%s, %s) is %s\n", argRes2.value, argRes.value, funcRes.value);                                
                                break;
                            // argRes will be date2
                            case "dateAge":
                                if (stack.isEmpty())
                                    p.error("dateAge requires 2 arguments, instead found 1");    
                                // date1
                                argRes2 = stack.pop();
                                funcRes = scan.date.dateAge(argRes2, argRes);
                                stack.add(funcRes);      
                                if (Debugger.bShowExpr)
                                    System.out.printf("... dateAge(%s, %s) is %s\n", argRes2.value, argRes.value, funcRes.value);                                
                                break;
                        }
                    }
                    break;
                // We always want to add operands to the stack
                case OPERAND:
                    if (token.tokenStr.equals("ARRAYEND"))
                    {
                        bFoundARRSTART = false;
                        break;
                    }
                    if (bFoundARRSTART)
                    {
                        valList.add(token.toResult());
                        break;
                    }
                    // If we have a value list
                    if (token.tokenStr.equals("ARRAYSTART"))
                    {
                        valList = new ArrayList<>();
                        bFoundARRSTART = true;
                    }
                    // If we are on a variable
                    else if (token.subClassif == SubClassif.IDENTIFIER)
                    {
                        // We need to check if the variable has been declared
                        if (scan.symbolTable.getSymbol(token.tokenStr) == null)
                            p.error("Variable '%s' on line %d has not been declared", token.tokenStr, token.iSourceLineNr);
                        STIdentifier varSTI = (STIdentifier) scan.symbolTable.getSymbol(token.tokenStr);
                        // We need to check if the variable has been defined/initialized
                        if (storageManager.getVariableValue(this, token.tokenStr) == null && varSTI.structure != Structure.FIXED_ARRAY)
                            p.error("Attempted to evaluate variable '%s' on line %d in an expression when it has not been defined/initialized", token.tokenStr, token.iSourceLineNr);
                        // Store the value of the variable into the stack
                        if (varSTI.structure == Structure.FIXED_ARRAY)
                        {
                            arrName = token.tokenStr;
                            valList = storageManager.getArrayVariable(token.tokenStr);
                        }
                        else
                            stack.add(storageManager.getVariableValue(this, token.tokenStr));
                    }
                    // we are on a array ref
                    else if (token.subClassif == SubClassif.ARRAY_REF)
                    {
                        // We expect the stack to not be empty
                        if (stack.isEmpty())
                            p.error("Stack is empty when it should have at least 1 element to handle array ref's subscript");
                        ResultValue arrRefSubScript = stack.pop();
                        // We expect the subscript to be an integer
                        if (arrRefSubScript.type != SubClassif.INTEGER)
                            p.error("Expected array's subscript to be an integer, instead found '%s' which is of type '%s'", arrRefSubScript.value, arrRefSubScript.type);
                        // Used to check if the array ref is a fixed_array or a primitive, if it is a primitive it must be a string
                        STIdentifier stArr = (STIdentifier) scan.symbolTable.getSymbol(token.tokenStr);
                        // Check if it exists
                        if (stArr == null)
                            p.error("Array reference '%s' has not been declared", token.tokenStr);
                        int iSubscript = Integer.valueOf(arrRefSubScript.value);
                        // It is a fixed array, so get the value from the storage manager and push it to the stack
                        if (stArr.structure == Structure.FIXED_ARRAY)
                        {
                            ResultValue resElem = new ResultValue();
                            // Get the value of the array element and store in stack
                            if (iSubscript < 0)
                              resElem = p.storageManager.getArrayElem(token.tokenStr, (Integer.valueOf(util.ELEM(token.tokenStr, storageManager).value) + iSubscript), scan.symbolTable);
                            else
                                resElem = p.storageManager.getArrayElem(token.tokenStr, Integer.valueOf(arrRefSubScript.value), scan.symbolTable);
                            stack.add(resElem);
                        }
                        // Array ref is a primitive, in this case it must be a string
                        else if (stArr.structure == Structure.PRIMITIVE)
                        {
                            ResultValue resPrimArr = new ResultValue();
                            if (stArr.dclType != SubClassif.STRING)
                                p.error("Expected array ref '%s' which is a primitive to be of type STRING, instead found type '%s'", token.tokenStr, stArr.dclType);
                            // Get the value of the string and convert to a char array
                            ResultValue pRes = p.storageManager.getVariableValue(this, token.tokenStr);
                            char scArr[] = pRes.value.toCharArray();
                            // Get the value at the index of the array and store it in the array
                            if (iSubscript < 0)
                                resPrimArr = new ResultValue(SubClassif.STRING, String.valueOf(scArr[scArr.length + iSubscript]));
                            else
                                resPrimArr = new ResultValue(SubClassif.STRING, String.valueOf(scArr[Integer.valueOf(arrRefSubScript.value)]));
                            stack.add(resPrimArr);
                        }
                    }
                    // We are on a string, int, float
                    else
                        stack.add(token.toResult());
                    break;
                case OPERATOR:
                    // If the stack is empty, then we are trying to do operations on nothing which is an error
                    if (stack.isEmpty())
                        p.error("Found operator '%s' when there are no operands to perform the operation on", token.tokenStr);
                    // The token is an equality, which should result in a boolean
                    if (token.subClassif == SubClassif.EQUALITY)
                    {
                        // Get the right operand
                        ResultValue rRes = stack.pop();
                        // Get the left operand
                        ResultValue lRes = stack.pop();
                        // Based on the token, we want to do different operations
                        switch(token.tokenStr)
                        {
                            case "==":
                                // Determine if the L operand is equal to the R operand and store that result into the stack
                                res = util.equals(rRes, lRes);
                                stack.add(res);  
                                // We want to show the result of the expr if debug mode for expr has been turned on
                                if (Debugger.bShowExpr)
                                    System.out.printf("... '%s' == '%s' is '%s'\n", lRes.value, rRes.value, res.value);                                    
                                break;
                            case "!=":
                                // Determine if the L operand is not equal to the R operand and store that result into the stack
                                res = util.notEquals(lRes, rRes);
                                stack.add(res); 
                                // We want to show the result of the expr if debug mode for expr has been turned on
                                if (Debugger.bShowExpr)
                                    System.out.printf("... '%s' != '%s' is '%s'\n", lRes.value, rRes.value, res.value);                                
                                break;                                
                            case ">=":
                                // Determine if the L operand is greater than or equal to the R operand and store that result into the stack
                                res = util.greaterOrEqual(lRes, rRes);
                                stack.add(res);  
                                // We want to show the result of the expr if debug mode for expr has been turned on
                                if (Debugger.bShowExpr)
                                    System.out.printf("... '%s' >= '%s' is '%s'\n", lRes.value, rRes.value, res.value);                                
                                break;
                            case "<=":
                                // Determine if the L operand is less than or equal to the R operand and store that result into the stack
                                res = util.lessOrEqual(lRes, rRes);
                                stack.add(res);  
                                // We want to show the result of the expr if debug mode for expr has been turned on
                                if (Debugger.bShowExpr)
                                    System.out.printf("... '%s' <= '%s' is '%s'\n", lRes.value, rRes.value, res.value);                                
                                break;
                            case ">":
                                // Determine if the L operand is greater than the R operand and store that result into the stack
                                res = util.greaterThan(lRes, rRes);
                                stack.add(res);  
                                // We want to show the result of the expr if debug mode for expr has been turned on
                                if (Debugger.bShowExpr)
                                    System.out.printf("... '%s' > '%s' is '%s'\n", lRes.value, rRes.value, res.value);                                
                                break;
                            case "<":
                                // Determine if the L operand is less than the R operand and store that result into the stack
                                res = util.lessThan(lRes, rRes);
                                stack.add(res);    
                                // We want to show the result of the expr if debug mode for expr has been turned on
                                if (Debugger.bShowExpr)
                                    System.out.printf("... '%s' < '%s' is '%s'\n", lRes.value, rRes.value, res.value);                               
                                break;
                        }
                    }
                    // Otherwise, we are doing numeric operations
                    else
                    {
                        // If we have a unary minus, we only need to pop 1 operand
                        if (token.subClassif == SubClassif.UNARY_MINUS)
                        {
                            // Grab the operand
                            rOp = new Numeric(this, stack.pop(), token.tokenStr, "right operand");
                            // Perform the unary operation and then add it to the stack
                            ResultValue unaryRes = util.unaryMinus(rOp);                     
                            stack.add(unaryRes);
                            // We want to show the result of the expr if debug mode for expr has been turned on
                            if (Debugger.bShowExpr)
                                System.out.printf("... - '%s' is '%s'\n", rOp.strValue, unaryRes.value);                               
                            break;
                        }
                        // String concatenation
                        if (token.tokenStr.equals("#"))
                        {
                            if (stack.isEmpty() || stack.size() < 2)
                            {
                                if (valList.size() > 0)
                                    p.error("Concatenate cannot do operation on an array");
                                p.error("# requires at least 2 operands");
                            }
                            ResultValue rRV = stack.pop();
                            ResultValue lRV = stack.pop();
                            ResultValue concatRes = util.concat(lRV, rRV);
                            stack.add(concatRes);
                            if (Debugger.bShowExpr)
                                System.out.printf("... '%s' # '%s' is '%s'\n", lRV.value, rRV.value, concatRes.value);
                            break;
                        }
                        // not operator
                        if (token.tokenStr.equals("not"))
                        {
                            ResultValue notOp = stack.pop();
                            ResultValue notRes = util.not(notOp);
                            stack.add(notRes);
                            if (Debugger.bShowExpr)
                                System.out.printf("... not '%s' is '%s'\n", notOp.value, notRes.value);                            
                            break;
                        }
                        // and operation
                        if (token.tokenStr.equals("and"))
                        {
                            // Get the right operand
                            ResultValue rRes = stack.pop();
                            // Get the left operand
                            ResultValue lRes = stack.pop();
                            ResultValue andRes = util.and(lRes, rRes);
                            stack.add(andRes);
                            if (Debugger.bShowExpr)
                                System.out.printf("... '%s' and '%s' is '%s'\n", lRes.value, rRes.value, andRes.value);
                            break;
                        }
                        // or operation
                        if (token.tokenStr.equals("or"))
                        {
                            // Get the right operand
                            ResultValue rRes = stack.pop();
                            // Get the left operand
                            ResultValue lRes = stack.pop();   
                            ResultValue orRes = util.or(lRes, rRes);
                            stack.add(orRes);
                            if (Debugger.bShowExpr)
                                System.out.printf("... '%s' or '%s' is '%s'\n", lRes.value, rRes.value, orRes.value);                            
                            break;
                        }
                        // IN operation
                        if (token.tokenStr.equals("IN"))
                        {
                            if (valList == null)
                                p.error("Expected an array when doing 'IN' operation");
                            ResultValue lRes = stack.pop();
                            ResultValue inRes = util.IN(lRes, valList);
                            stack.add(inRes);
                            if (Debugger.bShowExpr)
                            {
                                System.out.printf("... '%s' IN ", lRes.value);
                                for (ResultValue val : valList)
                                    System.out.printf("%s ", val.value);
                                System.out.printf(" is '%s'\n", inRes.value);
                            }
                            // Clear the array list for the next operation
                            //valList.clear();
                            break;
                        }
                        // NOTIN operation
                        if (token.tokenStr.equals("NOTIN"))
                        {
                            if (valList == null)
                                p.error("Expected an array when doing 'NOTIN' operation");                            
                            ResultValue lRes = stack.pop();
                            ResultValue notInRes = util.NOTIN(lRes, valList);
                            stack.add(notInRes);
                            if (Debugger.bShowExpr)
                            {
                                System.out.printf("... '%s' NOTIN ", lRes.value);
                                for (ResultValue val : valList)
                                    System.out.printf("%s ", val.value);
                                System.out.printf(" is '%s'\n", notInRes.value);
                            }
                            // Clear the array list for the next operation
                            //valList.clear();
                            break;                            
                        }
                        // Get the left and right operand in the expression and convert them to numerics
                        rOp = new Numeric(this, stack.pop(), token.tokenStr, "right operand");
                        lOp = new Numeric(this, stack.pop(), token.tokenStr, "left operand");
                        // Based on the operator's subclass, we want to do different calculations
                        switch(token.subClassif)
                        {
                            case ADD:
                                ResultValue addRes = util.add(lOp, rOp);
                                // If showExpr mode for debugger is turned on, show the result of the add operation
                                if (Debugger.bShowExpr)
                                    System.out.printf("... '%s' + '%s' is '%s'\n", lOp.strValue, rOp.strValue, addRes.value);
                                stack.add(addRes);
                                break;
                            case SUBTRACT:
                                ResultValue subRes = util.subtract(lOp, rOp);
                                stack.add(subRes);                           
                                // If showExpr mode for debugger is turned on, show the result of the subtract operation
                                if (Debugger.bShowExpr)
                                    System.out.printf("... '%s' - '%s' is '%s'\n", lOp.strValue, rOp.strValue, subRes.value);                                
                                break;
                            case MULT:
                                ResultValue multRes = util.multiply(lOp, rOp);
                                stack.add(multRes);                           
                                // If showExpr mode for debugger is turned on, show the result of the multiply operation
                                if (Debugger.bShowExpr)
                                    System.out.printf("... '%s' * '%s' is '%s'\n", lOp.strValue, rOp.strValue, multRes.value);
                                break;
                            case DIVD:
                                ResultValue divRes = util.divide(lOp, rOp);
                                stack.add(divRes);                         
                                // If showExpr mode for debugger is turned on, show the result of the divide operation
                                if (Debugger.bShowExpr)
                                    System.out.printf("... '%s' / '%s' is '%s'\n", lOp.strValue, rOp.strValue, divRes.value);
                                break;
                            case POWER:
                                ResultValue powRes = util.power(lOp, rOp);
                                stack.add(powRes);                        
                                // If showExpr mode for debugger is turned on, show the result of the power operation
                                if (Debugger.bShowExpr)
                                    System.out.printf("... '%s' ^ '%s' is '%s'\n", lOp.strValue, rOp.strValue, powRes.value);
                                break;
                        }
                    }
                    break;
                case SEPARATOR:
                    break;
            }
        }
        return stack.pop();
    }
}
