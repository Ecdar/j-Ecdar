grammar EdgeGrammar;

/*
 * Parser Rules
 */

@header {
package EdgeGrammar;
}

edge                : (guard | update) EOF ;

guard               : or? ';'? ;
update              : assignments? ;

or                  : (and '||' or) | and ;
and                 : (compareExpr '&&' and) | compareExpr ;
compareExpr         : TERM OPERATOR TERM ;

assignments         : (assignment ',' assignments) | (assignment ','?) ;
assignment          : TERM '=' TERM ;


/*
 * Lexer Rules
 */

TERM    : (ATOM | VARIABLE) ;
ATOM    : (INT | 'true' | 'false') ;

fragment DIGIT :   [0-9] ;
INT            :   DIGIT+ ;

fragment LOWERCASE : [a-z] ;
fragment UPPERCASE : [A-Z] ;
fragment SYMBOL    : ('.' | 'þ' | '€' | '_' | 'ð' | 'đ' | 'œ') ;

LETTER        : (LOWERCASE | UPPERCASE)+ ;
VARIABLE    : LETTER (LETTER | DIGIT | SYMBOL)* ;

OPERATOR    : ('>=' | '<=' | '==' | '<' | '>') ;

WS : [ \t\r\n]+ -> skip ;