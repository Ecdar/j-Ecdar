lexer grammar CommonLexerRules;

BOOLEAN     : 'true' | 'false' ;

fragment DIGIT :   [0-9] ;
INT            :   DIGIT+ ;

fragment LOWERCASE : [a-z] ;
fragment UPPERCASE : [A-Z] ;
fragment SYMBOL    : ('.' | 'þ' | '€' | '_' | 'ð' | 'đ' | 'œ' | '-') ;
fragment LETTER        : (LOWERCASE | UPPERCASE) ;

VARIABLE    : LETTER (LETTER | DIGIT | SYMBOL)* ;

WS : [ \t\r\n]+ -> skip ;