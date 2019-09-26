package meatbol;
public class STIdentifier extends STEntry
{
    public SubClassif dclType; //declaration type (Int, Float, String, Bool, Date)
    public Structure structure; //data structure (primitive, fixed array, unbound array)
    Parameter parm; //parameter type (not a parm, by reference, by value)
    int nonLocal; //nonLocal base Address Ref (0 - local, 1 - surrounding, ..., k- surrounding,99-global)
    public int maxSize;  // max size of an array if identifier is a fixed array
    /**
     Constructor for STIdentifier - Primitive data types
     */
    public STIdentifier(String symbol, Classif primClassif, SubClassif dclType, Structure structure) 
    {
        super(symbol, primClassif);
        this.dclType = dclType;
        this.structure = structure;
    }
    /**
     Constructor for STIdentifier - array data types
     */
    public STIdentifier(String symbol, Classif primClassif, SubClassif dclType, Structure structure, int maxSize) 
    {
        super(symbol, primClassif);
        this.dclType = dclType;
        this.structure = structure;
        this.maxSize = maxSize;
    }
}
