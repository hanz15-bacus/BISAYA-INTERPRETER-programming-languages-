public enum TokenType {
    //data types
    NUMERO, // NUMBER (alternative: IHAP)
    TINUOD, //represents true or false
    LETRA, // single character symbol
    TIPIK, //decimal
    STRING,
    IPAKITA,
    MUGNA,


    //arithmetic op
    OPERATOR,
    /*parenthesis,
   multiplication, division, modulo,
   addition, subtraction,
   greater_than, lesser_than,
   greater_than_or_equal_to, lesser_than_or_equal_to,
   equal, not_equal
   */
    COMMA,
    KEYWORD,
    IDENTIFIER,
    COLON,

}