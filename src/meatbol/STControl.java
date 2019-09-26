package meatbol;
public class STControl extends STEntry
{
    SubClassif subClassif; //subClassification (flow, end, declare)
    /**
     STControl Constructor
     */
    public STControl(String symbol, Classif primClassif, SubClassif subClassif)
    {
        super(symbol, primClassif);
        this.subClassif = subClassif;
    }
}