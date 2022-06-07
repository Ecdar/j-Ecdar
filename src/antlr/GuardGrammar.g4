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

or                  : (orExpression '||')+ orExpression;
orExpression        : expression | and ;

and                 : (expression '&&')+ expression ;
expression          : BOOLEAN | clockExpr | boolExpr | '(' guard ')';
clockExpr           : VARIABLE OPERATOR INT ;
boolExpr            : VARIABLE OPERATOR BOOLEAN ;

/*
 * Lexer Rules
 */

OPERATOR    : ('>=' | '<=' | '==' | '<' | '>') ;