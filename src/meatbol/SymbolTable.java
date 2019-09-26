package meatbol;
import java.util.HashMap;
public class SymbolTable 
{
    private final int VAR_ARGS = -1;
    HashMap<String, STEntry> ht = new HashMap<>();
    /**
     * Constructor for the symbol table
     */
    public SymbolTable()
    {
        // Insert all the resesrved symbols into the hashmap
        initGlobal();
    }
    
    /**
     * Returns the symbol table entry for the given symbol.
     * @param symbol in the symbol table
     * @return the STEntry of the symbol
     */
    STEntry getSymbol(String symbol)
    {
        return ht.get(symbol);
    }
    /**
     * Stores the symbol and its corresponding entry in the symbol table.
     * @param symbol to be inserted into the symbol table
     * @param entry containing addition information regarding the symbol
     */
    void putSymbol(String symbol, STEntry entry)
    {
        ht.put(symbol, entry);
    }
    /**
     * Inserts reserved symbol entries into the global symbol table.
     */
    private void initGlobal()
    {
        // insert end/flow control symbols
        ht.put("def", new STControl("def", Classif.CONTROL, SubClassif.FLOW));
        ht.put("enddef", new STControl("enddef", Classif.CONTROL, SubClassif.END));
        ht.put("if", new STControl("if", Classif.CONTROL, SubClassif.FLOW));
        ht.put("endif", new STControl("endif", Classif.CONTROL, SubClassif.END));
        ht.put("else", new STControl("else", Classif.CONTROL, SubClassif.END));
        ht.put("for", new STControl("for", Classif.CONTROL, SubClassif.FLOW));
        ht.put("endfor", new STControl("endfor", Classif.CONTROL, SubClassif.END));
        ht.put("while", new STControl("while", Classif.CONTROL, SubClassif.FLOW));
        ht.put("endwhile", new STControl("endwhile", Classif.CONTROL, SubClassif.END));
        ht.put("select", new STControl("select", Classif.CONTROL, SubClassif.FLOW));
        ht.put("endselect", new STControl("endselect", Classif.CONTROL, SubClassif.END));
        // when and default
        ht.put("when", new STControl("when", Classif.CONTROL, SubClassif.END));
        ht.put("default", new STControl("default", Classif.CONTROL, SubClassif.END));
        // insert break and continue end control symbols
        ht.put("break", new STControl("break", Classif.CONTROL, SubClassif.END));
        ht.put("continue", new STControl("continue", Classif.CONTROL, SubClassif.END));
        // insert declare control symbols
        ht.put("Int", new STControl("Int", Classif.CONTROL, SubClassif.DECLARE));
        ht.put("Float", new STControl("Float", Classif.CONTROL, SubClassif.DECLARE));
        ht.put("String", new STControl("String", Classif.CONTROL, SubClassif.DECLARE));
        ht.put("Bool", new STControl("Bool", Classif.CONTROL, SubClassif.DECLARE));
        ht.put("Date", new STControl("Date", Classif.CONTROL, SubClassif.DECLARE));
        // insert function symbol
        ht.put("print", new STFunction("print", Classif.FUNCTION, SubClassif.VOID, SubClassif.BUILTIN, VAR_ARGS));
        ht.put("LENGTH", new STFunction("LENGTH", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 0));
        ht.put("MAXLENGTH", new STFunction("MAXLENGTH", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 0));
        ht.put("SPACES", new STFunction("SPACES", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 0));
        ht.put("ELEM", new STFunction("ELEM", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 0));
        ht.put("MAXELEM", new STFunction("MAXELEM", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 0));
        // insert date function symbol
        ht.put("dateDiff", new STFunction("dateDiff", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 2));
        ht.put("dateAdj", new STFunction("dateAdj", Classif.FUNCTION, SubClassif.DATE, SubClassif.BUILTIN, 2));
        ht.put("dateAge", new STFunction("dateAge", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 2));
        // insert operator symbols
        ht.put("and", new STEntry("and", Classif.OPERATOR));
        ht.put("or", new STEntry("or", Classif.OPERATOR));
        ht.put("not", new STEntry("not", Classif.OPERATOR));
        ht.put("IN", new STEntry("in", Classif.OPERATOR));
        ht.put("NOTIN", new STEntry("notin", Classif.OPERATOR));
    }
}
