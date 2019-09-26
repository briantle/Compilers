/*
  This is a simple driver for the first programming assignment.
  Command Arguments:
      java Meatbol arg1
             arg1 is the meatbol source file name.
  Output:
      Prints each token in a table.
  Notes:
      1. This creates a SymbolTable object which doesn't do anything
         for this first programming assignment.
      2. This uses the student's Scanner class to get each token from
         the input file.  It uses the getNext method until it returns
         an empty string.
      3. If the Scanner raises an exception, this driver prints 
         information about the exception and terminates.
      4. The token is printed using the Token::printToken() method.
 */
package meatbol;

public class Meatbol 
{
    public static void main(String[] args) 
    {
        try
        {
            // Create the SymbolTable
            SymbolTable symbolTable = new SymbolTable();            
            // Initialize scanner and parser objects
            Scanner scan = new Scanner(args[0], symbolTable);
            Parser parser = new Parser(scan);
            // Used to check if we found break/continue, error if we did
            ResultValue res;
            // As long as we haven't reached EOF, we want to keep executing statements
            /*
            while (! scan.currentToken.tokenStr.isEmpty())
            {
                res = parser.statements(true); 
                if (res.terminatingStr != null && (res.terminatingStr.equals("break") || res.terminatingStr.equals("continue")))
                    throw new Exception("Found a break/continue that is not within a loop");
            }
            */
            parser.expr.infixToPostfix("", ";");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
