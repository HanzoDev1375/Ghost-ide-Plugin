grammar JsonLexer;

// Parser rules
json: value? EOF;

obj: '{' pair (',' pair)* ','? '}' | '{' '}';

pair: key ':' value;

key: STRING | IDENTIFIER | LITERAL ;

value: STRING | number | obj | arr | LITERAL;

arr: '[' value (',' value)* ','? ']' | '[' ']';

number: SYMBOL? (NUMBER);

// Lexer rules - با تعریف توکنهای ساختاری
LCURLY: '{';
RCURLY: '}';
LBRACKET: '[';
RBRACKET: ']';
COLON: ':';
COMMA: ',';
LPAREN: '(';
RPAREN: ')';


//SINGLE_LINE_COMMENT: '//' ~[\r\n]* -> skip; //comment not work in normal json
MULTI_LINE_COMMENT: '/*' .*? '*/' -> skip;

LITERAL: 'true' | 'false' | 'null';

STRING: '"' DOUBLE_QUOTE_CHAR* '"' | '\'' SINGLE_QUOTE_CHAR* '\'';

fragment DOUBLE_QUOTE_CHAR: ~["\\\r\n] | ESCAPE_SEQUENCE;
fragment SINGLE_QUOTE_CHAR: ~['\\\r\n] | ESCAPE_SEQUENCE;

fragment ESCAPE_SEQUENCE: '\\' (
    NEWLINE
    | UNICODE_SEQUENCE
    | ['"\\/bfnrtv]
    | '0'
    | 'x' HEX HEX
);

NUMBER: INT ('.' DIGIT*)? EXP? | '.' DIGIT+ EXP? | '0' [xX] HEX+;

HEXCOLOR: '#' HEX+;

//NUMERIC_LITERAL: 'Infinity' | 'NaN';

SYMBOL: '+' | '-';

fragment HEX: [0-9a-fA-F];
fragment INT: '0' | [1-9] DIGIT*;
fragment DIGIT: [0-9];
fragment EXP: [Ee] SYMBOL? DIGIT+;

IDENTIFIER: IDENTIFIER_START IDENTIFIER_PART*;

fragment IDENTIFIER_START: [a-zA-Z_$] | '\\' UNICODE_SEQUENCE;
fragment IDENTIFIER_PART: IDENTIFIER_START | DIGIT;

fragment UNICODE_SEQUENCE: 'u' HEX HEX HEX HEX;
fragment NEWLINE: '\r'? '\n' | '\r';

WS: [ \t\n\r\u00A0\uFEFF\u2003]+ -> skip;