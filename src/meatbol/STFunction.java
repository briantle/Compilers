package meatbol;
import java.util.ArrayList;
public class STFunction extends STEntry
{
    SubClassif returnType;	//return data type (Int, Float, String, Bool, Date, Void)
    SubClassif definedBy;	//what defined it (user, builtin)
    int numArgs;	//the number of arguments.  For variable length, VAR_ARGS.
    ArrayList<String> parmList = new ArrayList<>();	//reference to an ArrayList of formal parameters
    SymbolTable symbolTable;	//reference to the function's symbol table if it is a user-defined function
    /**
     * Constructor for STFunction
     */
    public STFunction(String symbol, Classif primClassif, SubClassif returnType, SubClassif definedBy, int numArgs)
    {
        super(symbol, primClassif);
        this.returnType = returnType;
        this.definedBy = definedBy;
        this.numArgs = numArgs;
    }
}