grammar QueryGrammar;

@header {
package QueryGrammar;
}

/*
 * Parser Rules
 */

queries     : query (';' query)* ';'? EOF
            ;

query       : QUERY_TYPE ':' saveSystem
            | refinement
            ;

refinement  : 'refinement:' system '<=' system
            ;

saveSystem  : system
            | system 'save-as' VARIABLE
            ;

system      : VARIABLE
            |  '(' system ')'
            | system CONJUNCTION system
            | system COMPOSITION system
            | system QUOTIENT system
            ;


/*
 * Lexer Rules
 */

QUERY_TYPE      : 'get-component' | 'bisim-minim' | 'consistency' | 'implementation' | 'determinism' | 'prune' ;

fragment DIGIT :   [0-9] ;
INT            :   DIGIT+ ;

fragment LOWERCASE : [a-z] ;
fragment UPPERCASE : [A-Z] ;
fragment SYMBOL    : ('.' | 'þ' | '€' | '_' | 'ð' | 'đ' | 'œ') ;

fragment LETTER      : (LOWERCASE | UPPERCASE) ;
VARIABLE             : LETTER (LETTER | DIGIT | SYMBOL)* ;

CONJUNCTION          : '&&' ;
COMPOSITION          : '||' ;
QUOTIENT             : '\\' ;

WS : [ \t\r\n]+ -> skip ;