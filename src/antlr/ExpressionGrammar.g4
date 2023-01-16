grammar ExpressionGrammar;

import CommonLexerRules;

@header {
package ExpressionGrammar;
}

options { caseInsensitive = true; }

/*
 * Parser Rules
 */

expressions         : expression EOF ;

expression          : arithExpression
                    | or ';'?
                    | and
                    ;

or                  : (orExpression OR)+ orExpression;
orExpression        : arithExpression | and ;

and                 : (arithExpression AND)+ arithExpression ;
arithExpression     : BOOLEAN | clockExpr | boolExpr | '(' expression ')';
clockExpr           : VARIABLE ('-'VARIABLE)? OPERATOR '-'? INT ;
boolExpr            : VARIABLE OPERATOR BOOLEAN ;

/*
 * Lexer Rules
 */

AND         : '&&' | 'and' ;
OR          : '||' | 'or'  ;

OPERATOR    : ('>=' | '<=' | '==' | '<' | '>') ;