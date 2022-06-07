grammar EdgeGrammar;

import CommonLexerRules;

@header {
package EdgeGrammar;
}

options { caseInsensitive = true; }

/*
 * Parser Rules
 */


edge                : (guard | update) EOF ;

guard               : expression
                    | or ';'?
                    | and
                    ;

update              : assignments? ;

or                  : (orExpression '||')+ orExpression;
orExpression        : expression | and ;

and                 : (expression '&&')+ expression ;
expression          : BOOLEAN | clockExpr | boolExpr | '(' guard ')';
clockExpr           : VARIABLE OPERATOR INT ;
boolExpr            : VARIABLE OPERATOR BOOLEAN ;

assignments         : (assignment ',' assignments) | (assignment ','?) ;
assignment          : clockAssignment | boolAssignment ;
clockAssignment     : VARIABLE '=' INT ;
boolAssignment      : VARIABLE '=' BOOLEAN ;


/*
 * Lexer Rules
 */

BOOLEAN     : 'true' | 'false' ;

OPERATOR    : ('>=' | '<=' | '==' | '<' | '>') ;