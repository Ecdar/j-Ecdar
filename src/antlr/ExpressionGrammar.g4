grammar ExpressionGrammar;

import CommonLexerRules;

@header {
package ExpressionGrammar;
}

options { caseInsensitive = true; }

/*
 * Parser Rules
 */

constraints              : contstraint EOF ;

contstraint         : expression
                    | or ';'?
                    | and
                    ;

or                  : (orExpression OR)+ orExpression;
orExpression        : expression | and ;

and                 : (expression AND)+ expression ;
expression          : BOOLEAN | clockExpr | boolExpr | '(' contstraint ')';
clockExpr           : VARIABLE ('-'VARIABLE)? OPERATOR '-'? INT ;
boolExpr            : VARIABLE OPERATOR BOOLEAN ;

/*
 * Lexer Rules
 */

AND         : '&&' | 'and' ;
OR          : '||' | 'or'  ;

OPERATOR    : ('>=' | '<=' | '==' | '<' | '>') ;