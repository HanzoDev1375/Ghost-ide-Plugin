lexer grammar DiffLexer;
/* ---------- Headers ---------- */


DIFF
   : 'diff' ' ' '--git'
   ;

INDEX
   : 'index' ' ' HEX '..' HEX
   ;

OLD_FILE
   : '---'
   ;

NEW_FILE
   : '+++'
   ;

HUNK_HEADER
   : '@@' ' ' REMOVED_LINE INT (',' INT)? ' ' ADDED_LINE INT (',' INT)? ' ' '@@'
   ;
/* ---------- Line types ---------- */
   
   
ADDED_LINE
   : '+'
   ;

REMOVED_LINE
   : '-'
   ;

NO_NEWLINE
   : '\\' ' No newline at end of file'
   ;
/* ---------- Fragments ---------- */
   
   
fragment HEX
   : [0-9a-fA-F]+
   ;

fragment INT
   : [0-9]+
   ;

ID
   : [a-z] [a-zA-Z-0-9]+
   ;

NEWLINE
   : '\r'? '\n'
   ;

WS
   : [ \t]+ -> skip
   ;

