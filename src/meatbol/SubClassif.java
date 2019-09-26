package meatbol;
public enum SubClassif 
{
    EMPTY,      // empty
    // OPERAND's subclassifications
    IDENTIFIER, // identifier 
    INTEGER,    // integer constant
    FLOAT,      // float constant
    BOOLEAN,    // boolean constant
    STRING,     // string constant
    DATE,       // date constant
    VOID,       // void
    // OPERATOR's subclassifications
    ASSIGNMENT, // assignment --> could  be =, +=, -=
    EQUALITY, // ==, >=, <=, >, <, !=
    ADD, // '+' symbol
    SUBTRACT, // '-' symbol
    UNARY_MINUS, // '-' that is preceded by an operator/separator/control instead of an operand --> Ex) x = - 1;
    MULT, // '*' symbol
    DIVD, // '/' symbol
    POWER, // '^' symbol
    // CONTROL's subclassifications
    FLOW,       // flow statement (e.g., if)
    END,        // end statement (e.g., endif)
    DECLARE,    // declare statement (e.g., Int)
    // FUNCTION's subclassfications
    BUILTIN,    // builtin function (e.g., print)
    USER,        // user-defined function
    // When we don't want to execute statements
    SKIPPED,
    // Used in loops
    GOOD,
    // Array related
    ARRAY_REF,
    // Used when an array element hasn't been initialized
    NULL,
    // Subclassifs for BREAK and CONTINUE
    BREAK,
    CONTINUE,
    
}
