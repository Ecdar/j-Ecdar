grammar GuardGrammar;

import CommonLexerRules;

@header {
package GuardGrammar;
}

options { caseInsensitive = true; }

/*
 * Parser Rules
 */

guards              : guard EOF ;

guard               : expression
                    | or ';'?
                    | and
                    ;

or                  : (orExpression OR)+ orExpression;
orExpression        : expression | and ;

and                 : (expression AND)+ expression ;
expression          : BOOLEAN | clockExpr | boolExpr | '(' guard ')';
clockExpr           : VARIABLE ('-'VARIABLE)? OPERATOR '-'? INT ;
boolExpr            : VARIABLE OPERATOR BOOLEAN ;

/*
 * Lexer Rules
 */

AND         : '&&' | 'and' ;
OR          : '||' | 'or'  ;

OPERATOR    : ('>=' | '<=' | '==' | '<' | '>') ;