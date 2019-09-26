package meatbol;
public class ResultValue 
{
    SubClassif type;       // usually data type of the result
    String value;          // value of the result
    Structure structure;   // primitive, fixed array, unbounded array
    String terminatingStr; // used for end of lists of things (e.g., a list of 
                           // statements might be terminated by "endwhile")
    /**
     * Empty constructor
     */
    public ResultValue()
    {
       
    }
    /**
     * ResultValue Constructor
     * The result of an expression
     * <p>
     * @param type  - the SubClassif of the result
     * @param value - the value of the result
     */
    public ResultValue(SubClassif type, String value)
    {
        this.type = type;
        this.value = value;
        this.terminatingStr = value;
    }
}
