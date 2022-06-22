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

CONJUNCTION          : '&&' ;
COMPOSITION          : '||' ;
QUOTIENT             : '\\\\' ;