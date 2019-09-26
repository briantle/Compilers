package meatbol;
public class STEntry
 {
     String symbol;	//string for the symbol
     Classif primClassif;	//primary classification of the symbol
    /**
     * Constructor for STEntry. A superclass for the other ST classes.
     */
     public STEntry(String symbol, Classif primClassif)
     {
         this.symbol = symbol;
         this.primClassif = primClassif;
     }
}