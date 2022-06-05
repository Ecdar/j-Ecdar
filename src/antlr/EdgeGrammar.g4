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
expression          : true | false | clockExpr | boolExpr ;
clockExpr           : VARIABLE OPERATOR INT ;
boolExpr            : VARIABLE OPERATOR BOOLEAN ;

true                : TRUE ;
false               : FALSE ;

assignments         : (assignment ',' assignments) | (assignment ','?) ;
assignment          : VARIABLE '=' INT ;


/*
 * Lexer Rules
 */

TRUE        : 'true' ;
FALSE       : 'false' ;
BOOLEAN     : TRUE | FALSE ;

fragment DIGIT :   [0-9] ;
INT            :   DIGIT+ ;

fragment LOWERCASE : [a-z] ;
fragment UPPERCASE : [A-Z] ;
fragment SYMBOL    : ('.' | 'þ' | '€' | '_' | 'ð' | 'đ' | 'œ') ;
fragment LETTER        : (LOWERCASE | UPPERCASE) ;

VARIABLE    : LETTER (LETTER | DIGIT | SYMBOL)* ;

OPERATOR    : ('>=' | '<=' | '==' | '<' | '>') ;

WS : [ \t\r\n]+ -> skip ;