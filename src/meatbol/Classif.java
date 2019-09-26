package meatbol;
public enum Classif 
{
    EMPTY,      // empty
    OPERAND,    // constants, identifier
    DEBUG,      // contains the word 'debug'
    OPERATOR,   // + - * / < > = !
    SEPARATOR,  // ( ) , : ; [ ] 
    FUNCTION,   // TBD
    CONTROL,    // TBD
    EOF         // EOF encountered
}

