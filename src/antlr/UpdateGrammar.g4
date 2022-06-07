grammar UpdateGrammar;

import CommonLexerRules;

@header {
package UpdateGrammar;
}

options { caseInsensitive = true; }

/*
 * Parser Rules
 */

update              : assignments? EOF ;

assignments         : (assignment ',' assignments) | (assignment ','?) ;
assignment          : clockAssignment | boolAssignment ;
clockAssignment     : VARIABLE '=' INT ;
boolAssignment      : VARIABLE '=' BOOLEAN ;