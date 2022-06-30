grammar QueryGrammar;

import CommonLexerRules;

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

refinement  : 'refinement:' expression '<=' expression
            ;

saveSystem  : expression
            | expression 'save-as' VARIABLE
            ;

expression  : conjunction
            | composition
            | quotient
            | system
            ;

conjunction             : (conjunctionExpression CONJUNCTION)+ conjunctionExpression ;
conjunctionExpression   : system | composition | quotient ;

composition             : (compositionExpression COMPOSITION)+ compositionExpression ;
compositionExpression   : system | quotient ;

quotient                : system QUOTIENT system ;

system      : VARIABLE
            | '(' expression ')'
            ;


/*
 * Lexer Rules
 */

QUERY_TYPE      : 'get-component' | 'bisim-minim' | 'consistency' | 'implementation' | 'determinism' | 'prune' ;

CONJUNCTION          : '&&' ;
COMPOSITION          : '||' ;
QUOTIENT             : '\\\\' ;