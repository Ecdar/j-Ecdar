grammar EdgeGrammar;

@header {
package EdgeGrammar;
}

options { caseInsensitive = true; }

/*
 * Parser Rules
 */


edge                : (guard | update) EOF ;

guard               : or? ';'? ;
update              : assignments? ;

or                  : (and '||' or) | and ;
and                 : (expression '&&' and) | expression ;
expression          : true | false | compareExpr ;
compareExpr         : TERM OPERATOR TERM ;
true                : TRUE ;
false               : FALSE ;

assignments         : (assignment ',' assignments) | (assignment ','?) ;
assignment          : TERM '=' TERM ;


/*
 * Lexer Rules
 */

TRUE    : 'true' ;
FALSE    : 'false' ;

TERM    : (INT | VARIABLE) ;

fragment DIGIT :   [0-9] ;
INT            :   DIGIT+ ;

fragment LOWERCASE : [a-z] ;
fragment UPPERCASE : [A-Z] ;
fragment SYMBOL    : ('.' | 'þ' | '€' | '_' | 'ð' | 'đ' | 'œ') ;

LETTER        : (LOWERCASE | UPPERCASE)+ ;
VARIABLE    : LETTER (LETTER | DIGIT | SYMBOL)* ;

OPERATOR    : ('>=' | '<=' | '==' | '<' | '>') ;

WS : [ \t\r\n]+ -> skip ;