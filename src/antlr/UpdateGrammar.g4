grammar UpdateGrammar;

import CommonLexerRules;

@header {
package UpdateGrammar;
}

options { caseInsensitive = true; }

/*
 * Parser Rules
 */

update              : ((assignment ',')* assignment (',')?)? EOF ;

assignment          : clockAssignment | boolAssignment ;
clockAssignment     : VARIABLE '=' INT ;
boolAssignment      : VARIABLE '=' BOOLEAN ;