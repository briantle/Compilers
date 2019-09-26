package meatbol;
/**
 * A Debugger class with global variables that determine when to execute debug commands
 */
public class Debugger
{
    public static boolean bShowToken = false;   // print the token information for the current token returned by scan.getNext(); 
    public static boolean bShowExpr = false;    // print the result of each expression evaluation for expressions involving at least one operator
    public static boolean bShowAssign = false;  // print the variable and value for an assignment
    public static boolean bShowStmt = false;    // print the statement before executing it
}
