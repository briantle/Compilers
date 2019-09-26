package meatbol;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * The scanner class is used to read in text lines from source code
 * and to separate each line into tokens.
 * @author Brian
 */
public class Scanner 
{
    // source code file name
    public String sourceFileNm;
    // array list of source text lines
    public ArrayList<String> sourceLineM = new ArrayList<String>();
    // object responsible for providing symbol definitions
    public SymbolTable symbolTable;
    // char [] for the current text line
    public char[] textCharM;
    // Line Number in sourceLineM for current text line
    public int iSourceLineNr;
    // Column Position within the current text line
    public int iColPos;
    // The token established with the most recent call to getNext()
    public Token currentToken;
    // The token following the currentToken
    public Token nextToken;
    // Used to determine when to terminate a token
    private final static String delimiters = " \t;:{}()\'\"=!<>+-*/[]#,^\n";
    // Used to determine a token's primary classification
    private final String operators = "+-*/<>!=#^";
    private final String separators = "{}():;[],";
    private final String stringLiteral = "\"\'";
    private final String escapeDelimiters = "\'\"\\nta";
    // Used to determine when to print the current line based on iSourceLineNr's value
    public int prevSourceLine = 0;
    // Used to determine if a string is a date
    // the date object is also used in other classes such as the parser
    public Date date = new Date();
    /**
     * Constructor for the Scanner.
     * <p>
     *    •	It receives the source file name and symbol table object.  
     *    •	It saves the sourceFileNm and symbolTable.  
     *    •	It reads the specified source file and populates sourceLineM.  
     *    •	Initializes textCharM, iSourceLineNr, and iColPos.  
     *    •	It initializes currentToken and nextToken to new objects (so that the parser doesn't have to check for null).
     *    •	It gets the first token into nextToken.  
     * @param sourceFileNm -  the name of the source file passed in to the scanner
     * @param symbolTable - object that contains symbol definitions
     */
    public Scanner(String sourceFileNm, SymbolTable symbolTable) throws Exception
    {
        // Stores the source file name and symbol table in the Scanner object
        this.sourceFileNm = sourceFileNm;
        this.symbolTable = symbolTable;
        // Reads the source file and populates the sourceLineM arrayList
        try
        {
            File newFile = new File(sourceFileNm);
            java.util.Scanner sc = new java.util.Scanner(newFile);
            // Insert the text lines into the sourceLineM array
            while (sc.hasNextLine())
                sourceLineM.add(sc.nextLine());
            // Initializes textCharM, iSourceLineNr, and iColPos
            iSourceLineNr = 0;
            iColPos = 0;
            textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
            // Initialize currentToken and nextToken
            currentToken = new Token();
            nextToken = new Token();
            nextToken.tokenStr = getNext();
            // Start the current token to the first token in the input line
            getNext();
        }
        // Could not open the specified file
        catch (FileNotFoundException e)
        {
            throw new FileNotFoundException("Failed to open the following file: " + sourceFileNm);
        }
    }
    /**
     * Gets the next token.  Automatically advances to the next source line when necessary.
     * If there are no more tokens, it returns ""; otherwise, it returns the tokenStr for the current token. 
     * <p>
     * It also sets these attributes in scan.currentToken:
     *      tokenStr - the string representation of the token
     *      primClassif - the primary classification of the token (OPERAND, OPERATOR)
     *      subClassif - the sub classification of the token (e.g., IDENTIFIER, INTEGER)
     *      iSourceLineNr - source line number where the token was found
     *      iColPos - beginning column position for the token
     * @return - The text of the next token
     */
    public String getNext() throws Exception
    {
        // Copy the next token into the current token
        copyToken();
        // Reset next token
        nextToken = new Token();      
        // Skip whitespace
        skipWhiteSpace();
        // If we are at EOF
        if (iSourceLineNr >= sourceLineM.size())
        {
            nextToken.primClassif = Classif.EOF;
            return "";
        }
        int iBeg = iColPos, iEnd;
        // Iterate through each character in the current text line
        for (; iColPos < textCharM.length; iColPos++)
        {
            iEnd = iColPos;
            // If our current character is a delimiter
            if (delimiters.indexOf(textCharM[iColPos]) > -1)
            {
                // Found a delimiter at the beginning of the start position, this is an operator
                if (iBeg == iEnd)
                {
                    // If this token is an apostrophe or a double quote, that means we need to get the string literal
                    if (stringLiteral.indexOf(textCharM[iColPos]) > -1)
                    {
                        nextToken.tokenStr = getStringLiteral();
                        // Check if the string is a valid date, if it is then convert it's subclassif to DATE
                        if (date.isValidDate(nextToken.tokenStr))
                            nextToken.subClassif = SubClassif.DATE;
                        break;
                    }
                    // token is not a string literal and is either a separator or operator
                    else
                    {
                        // Set token's string representation
                        nextToken.tokenStr = String.valueOf(textCharM[iColPos]);
                        // Set the token's column position and source line number
                        nextToken.iColPos = iColPos;
                        nextToken.iSourceLineNr = iSourceLineNr + 1;
                        // Get the token's classification
                        if (operators.indexOf(textCharM[iColPos]) > -1)
                            setOperatorClassif();
                        else if (separators.indexOf(textCharM[iColPos]) > -1)
                        {
                            nextToken.primClassif = Classif.SEPARATOR;
                            // If our token is a separator, check for '[' to set array ref
                            if (nextToken.tokenStr.equals("["))
                            {
                                // If our token is a '[', the preceding token must be an operand and an identifier
                                if (currentToken.primClassif != Classif.OPERAND)
                                    throw new Exception("Expected token preceding the '[' to be an operand");
                                if (currentToken.subClassif != SubClassif.IDENTIFIER)
                                    throw new Exception("Expected token preceding the '[' to be an identifier");
                                // Set the array ref classif and precedences
                                currentToken.subClassif = SubClassif.ARRAY_REF;
                                currentToken.iPrecedence = 16;
                                currentToken.iStackPrecedence = 0;
                            }
                        }
                        // Skip over the delimiter
                        iColPos++;
                        // If both the current and next token are operators, we can combine them
                        if (currentToken.primClassif == Classif.OPERATOR && nextToken.primClassif == Classif.OPERATOR && nextToken.subClassif == SubClassif.ASSIGNMENT)
                        {
                            // Combine the tokens
                            nextToken.tokenStr = currentToken.tokenStr + nextToken.tokenStr;
                            setOperatorClassif();
                            // Get the next token
                            nextToken.tokenStr = getNext();
                        }
                        // Checks for unary minus
                        if (currentToken.primClassif != Classif.OPERAND && !currentToken.tokenStr.equals("]") && !currentToken.tokenStr.equals(")") && nextToken.primClassif == Classif.OPERATOR & nextToken.tokenStr.equals("-"))
                        {
                            nextToken.subClassif = SubClassif.UNARY_MINUS;
                            nextToken.iPrecedence = 12;
                            nextToken.iStackPrecedence = 12;
                        }
                        // Acquired token so leave the loop
                        break;
                    }
                }
                // Otherwise, it is an operand
                else
                {
                    setOperand(iBeg, iEnd);
                    break;
                }
            }
            // In the case there isn't a delimiter at the end of the input line
            else if (iColPos == textCharM.length-1)
                setOperand(iBeg, iEnd+1);
        }
        // If we turned on debugging for token, we print what the current token is
        if (Debugger.bShowToken == true)
        {
            System.out.print("... " );
            currentToken.printToken();
        }
        return nextToken.tokenStr;
    }
    /**
     * For the interpreter, this sets the position which is necessary when looping (while, for). 
     * Sets the position to the specified source line number and column position. 
     * <p>
     * @param iSourceLineNr - the line number that the loop is located at
     * @param iColPos - the column where the loop begins
     */
    public void setPosition(int iSourceLineNr, int iColPos) throws Exception
    {
        // Set position to the specified source line number and column position
        this.iSourceLineNr = iSourceLineNr;
        this.prevSourceLine = iSourceLineNr-1;        
        this.iColPos = iColPos;
        // initialize textCharM
        textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
        // It initializes currentToken and nextToken to new objects (so that the parser doesn't have to check for null).
        currentToken = new Token();
        nextToken = new Token();
        // It gets the first token into nextToken.
        nextToken.tokenStr = getNext();
    }
    
    /**
     * Skips all whitespace (e.g spaces, tabs, newlines)
     * <p>
     * Automatically advances to the next line if needed
     * Function is left once we found the first non-whitespace character or reached EOF
     */
    public void skipWhiteSpace()
    {
        //int iBeg = iColPos;
        boolean skippedWhitespace = false;
        // As long we haven't found a non-whitespace char or reached EOF
        // We need to keep searching for a non-whitespace char
        while (!skippedWhitespace && iSourceLineNr != sourceLineM.size())
        {
            // Get the next text line
            textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
            // If a blank line
            if (sourceLineM.get(iSourceLineNr).isEmpty())
            {
                // Advance to the next line and reset the column position
                iSourceLineNr++;
                iColPos = 0;
                // If we've reached EOF, exit loop
                if (iSourceLineNr >= sourceLineM.size())
                    break;
                continue;
            }
            
            // If we reached the end of the text, advance to the next line
            if (iColPos == textCharM.length)
            {
                // Advance to the next line
                iSourceLineNr++;
                // reset column position
                iColPos = 0;
                continue;
            }
            
            // Go through the text line and skip the whitespace
            for (; iColPos < textCharM.length; iColPos++)
            {
                // Did not encounter a space or tab
                if (textCharM[iColPos] != ' ' && textCharM[iColPos] != '\t')
                {
                    // If it is a comment, skip it by advancing to the next line
                    if (iColPos != textCharM.length - 1 && textCharM[iColPos] == '/' && textCharM[iColPos+1] == '/')
                    {
                        iSourceLineNr++;
                        iColPos = 0;
                        break;
                    }
                    // Otherwise it is not a comment and is a non-whitespace character
                    skippedWhitespace = true;
                    break;
                }
                // If we reached the last character in the textline and haven't
                // found a non-whitespace char, we need to advance to the next line
                if (iColPos == textCharM.length)
                {
                    // Advance to the next line and reset the column position
                    iSourceLineNr++;
                    iColPos = 0;
                    break;
                }
            }
        }
    }
    /**
     * Copies the values of the nextToken into the current token
     * <p>
     */
    public void copyToken()
    {
        currentToken.tokenStr = nextToken.tokenStr;
        currentToken.primClassif = nextToken.primClassif;
        currentToken.subClassif = nextToken.subClassif;
        currentToken.iSourceLineNr = nextToken.iSourceLineNr;
        currentToken.iColPos = nextToken.iColPos; 
        currentToken.iPrecedence = nextToken.iPrecedence;
        currentToken.iStackPrecedence = nextToken.iStackPrecedence;
    }
    /**
     * Gets the string literal of the token. Used when apostrophe/quote is encountered
     * <p>
     * @return the string literal, otherwise blank if the string literal was not properly terminated 
     */
    public String getStringLiteral() throws Exception
    {
        // The number of characters in the array to copy
        int iRet = 0;
        // String position will start after the quote
        int iBeg = iColPos + 1;
        boolean termStrLit = false;
        boolean singQuote = false, dQuote = false;
        String tokenStr = "";
        char retCharM[] = textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
        if (textCharM[iColPos] == '\'')
            singQuote = true;
        else
            dQuote = true;
        // Loop through string
        for (iColPos = iBeg; iColPos < textCharM.length; iColPos++)
        {
            // If there isn't an escape char before the apos/quote, this is the end of the str lit
            if (textCharM[iColPos] == '\'' && singQuote && textCharM[iColPos-1] != '\\'
             || textCharM[iColPos] == '\"' && dQuote && textCharM[iColPos-1] != '\\')
            {
                tokenStr = String.valueOf(retCharM, 0, iRet);
                nextToken.iColPos = iBeg;
                nextToken.iSourceLineNr = iSourceLineNr + 1;
                nextToken.primClassif = Classif.OPERAND;
                nextToken.subClassif = SubClassif.STRING;
                nextToken.iPrecedence = 0;
                // Advance to the next position to skip over the quote/apostrophe on the next call to getNext
                iColPos++;
                termStrLit = true;
                break;
            }
            // If we need to take care of special characters
            else if (iColPos != textCharM.length-1 && textCharM[iColPos] == '\\' && escapeDelimiters.indexOf(textCharM[iColPos+1]) > -1)
            {
                if (textCharM[iColPos+1] == '\'')
                    retCharM[iRet] = '\'';
                else if (textCharM[iColPos+1] == '\"')
                    retCharM[iRet] = '\"';
                else if (textCharM[iColPos+1] == '\\')
                    retCharM[iRet] = '\\';
                else if (textCharM[iColPos+1] == 'n')
                    retCharM[iRet] = 0x0A;
                else if (textCharM[iColPos+1] == 't')
                    retCharM[iRet] = 0x09;
                else if (textCharM[iColPos+1] == 'a')
                    retCharM[iRet] = 0x07;
                iRet++;
                iColPos++;
            }
            // Otherwise, character is a alphanumeric
            else
            {
                retCharM[iRet] = textCharM[iColPos];
                iRet++;
            }
        }
        // The string literal was not properly terminated
        if (!termStrLit)
        {
            String token = sourceLineM.get(iSourceLineNr).substring(iBeg, iColPos);
            throw new Exception("Line " + (iSourceLineNr+1) + " String literal was not terminated at column " + iColPos + " " + token + " File: " + sourceFileNm);
        }   
        return tokenStr;
    }
    /**
     Sets the token attributes for an operand.
     The token can be a symbol in the symbolTable or a number/identifier
     <p>
     @param iBeg where the first character of the token is
     @param iEnd where the token ends
     */
    public void setOperand(int iBeg, int iEnd) throws Exception
    {
        // Set token attributes
        nextToken.tokenStr = sourceLineM.get(iSourceLineNr).substring(iBeg, iEnd);
        nextToken.iColPos = iBeg;
        nextToken.iSourceLineNr = iSourceLineNr+1;
        nextToken.primClassif = Classif.OPERAND;
        
        // check if the operand has the word' debug', if it does that means it's setting a debug mode
        if (nextToken.tokenStr.equals("debug"))
        {
            nextToken.primClassif = Classif.DEBUG;
            return;
        }
        
        // The token has a value stored in the global symbol table
        if (symbolTable.ht.containsKey(nextToken.tokenStr))
        {
            // Get the token's classification
            STEntry st = symbolTable.getSymbol(nextToken.tokenStr);
            nextToken.primClassif = st.primClassif;
            
            // Token is an operator (Ex. in notin and or)
            if (nextToken.primClassif == Classif.OPERATOR)
                setOperatorClassif();
            // Token is a control variable
            else if (nextToken.primClassif == Classif.CONTROL)
            {
                STControl sc = (STControl) st;
                nextToken.subClassif = sc.subClassif;
            }
            // Token is a function
            else if (nextToken.primClassif == Classif.FUNCTION)
            {
                STFunction sf = (STFunction) st;
                nextToken.subClassif = sf.definedBy;
            }
            // Otherwise it is an identifier
            else
                nextToken.subClassif = SubClassif.IDENTIFIER;
        }
        // Otherwise, it is either a variable or a constant
        else
        {
            // If the first character of the token is a number
            if (Character.isDigit(textCharM[iBeg]))
            {
                Numeric numeric = new Numeric();
                // Our token is a number, find out what type it is
                int rc = numeric.getNumericType(nextToken.tokenStr);
                if (rc == 0)
                    nextToken.subClassif = SubClassif.INTEGER;
                else if (rc == 1)
                    nextToken.subClassif = SubClassif.FLOAT;
                // Token is not a valid numeric constant
                else
                    throw new Exception("Line " + (iSourceLineNr+1) + " Invalid Numeric Constant: " + nextToken.tokenStr + " File: " + sourceFileNm);
            }
            // Otherwise it's an identifier or a boolean
            else
            {
                // If it has a T or F, it's a boolean
                if (nextToken.tokenStr.equals("T") || nextToken.tokenStr.equals("F"))
                    nextToken.subClassif = SubClassif.BOOLEAN;
                else
                    nextToken.subClassif = SubClassif.IDENTIFIER;
            }
        }
    }
    /**
     * Set's the classification and precedence/stack precedence of a operator token based
     * on it's string value
     * <p>
     */
    public void setOperatorClassif()
    {
        nextToken.primClassif = Classif.OPERATOR;
        // Set the subclassification based on the string
        switch(nextToken.tokenStr)
        {
            case "and":
            case "or":
                nextToken.iPrecedence = 4;
                nextToken.iStackPrecedence = 4;
                break;
            case "not":
                nextToken.iPrecedence = 5;
                nextToken.iStackPrecedence = 5;               
                break;
            case "IN":
            case "NOTIN":
                nextToken.iPrecedence = 6;
                nextToken.iStackPrecedence = 6;    
                break;
            case "=":
            case "+=":
            case "-=":
                nextToken.subClassif = SubClassif.ASSIGNMENT;
                break;
            case "==":
            case ">=":
            case "<=":
            case ">":
            case "<":
            case "!=":
                nextToken.subClassif = SubClassif.EQUALITY;
                nextToken.iPrecedence = 6;
                nextToken.iStackPrecedence = 6;
                break;
            case "-":
                nextToken.subClassif = SubClassif.SUBTRACT;
                nextToken.iPrecedence = 8;
                nextToken.iStackPrecedence = 8;
                break;
            case "+":
                nextToken.subClassif = SubClassif.ADD;
                nextToken.iPrecedence = 8;
                nextToken.iStackPrecedence = 8;
                break;
            case "*":
                nextToken.subClassif = SubClassif.MULT;
                nextToken.iPrecedence = 9;
                nextToken.iStackPrecedence = 9;                
                break;
            case "/":
                nextToken.subClassif = SubClassif.DIVD;
                nextToken.iPrecedence = 9;
                nextToken.iStackPrecedence = 9;
                break;
            case "^":
                nextToken.subClassif = SubClassif.POWER;
                nextToken.iPrecedence = 11;
                nextToken.iStackPrecedence = 10;
                break;  
            case "#":
                nextToken.iPrecedence = 7;
                nextToken.iStackPrecedence = 7;
        }
    }
    /**
     * Skips tokens in the code until it reaches skipToStr
     * @param skipToStr - the string we want our token to end at
     * @throws Exception 
     */
    public void skipTo(String skipToStr) throws Exception
    {
        // As long as we haven't reached EOF and our current token's string doesn't equal skipToStr, keep grabbing tokens
        while (!currentToken.tokenStr.equals(skipToStr) && !currentToken.tokenStr.isEmpty())
            getNext();
    }
}
