// $antlr-format alignTrailingComments true, columnLimit 150, minEmptyLines 1, maxEmptyLinesToKeep 1, reflowComments false, useTab false
// $antlr-format allowShortRulesOnASingleLine false, allowShortBlocksOnASingleLine true, alignSemicolons hanging, alignColons hanging

grammar Fun;

eval
    : compilationUnit
    ;

// Parser Rules

compilationUnit
    : namespaceDeclaration? useDeclaration* exposeDeclaration* classDeclaration EOF
    ;

namespaceDeclaration
    : 'namespace' qualifierIdentifier
    ;

useDeclaration
    : 'use' qualifierIdentifier
    ;

exposeDeclaration
    : 'expose' 'namespace'? (Id | identifierSet)
    ;

classDeclaration
    : 'class' Id parameterList ':' (functionDeclaration | effectDeclaration)*
    ;

effectDeclaration
    : 'effect' Id parameterList ':' simpleType '=' '{' statement* expression '}'
    ;

functionDeclaration
    : 'fun' Id parameterList ':' simpleType '=' '{' statement* expression '}'
    ;

identifierSet
    : '{' Id (',' Id)* '}'
    ;

qualifierIdentifier
    : Id ('/' Id)*
    ;

parameterList
    : '(' (Id ':' simpleType)? (',' Id ':' simpleType)* ')'
    ;

simpleType
    : Id
    | Id '[]'
    ;

statement
    : valueDeclaration
    ;

valueDeclaration
    : 'val' Id '=' expression
    ;

functionApplication
    : Id '(' expression? (';' expression)* ')'
    ;

expression
    : printExpression
    | simpleExpression
    | functionApplication
    | '(' expression ')'
    | String
    ;

printExpression
    : 'println(' expression ')'
    ;

simpleExpression
    : term (operand = (PLUS | MINUS) expression)*
    ;

term
    : factor (operand = (MULTIPLY | DIVIDE) expression)*
    ;

factor
    : Number
    ;

// Lexer

Id
    : Letter (Letter | Digit)*
    ;

BooleanLiteral
    : 'true'
    | 'false'
    ;

Type
    : Letter+
    ;

String
    : '"' StringContent '"'
    ;

Number
    : Digit+
    ;

PLUS
    : '+'
    ;

MINUS
    : '-'
    ;

MULTIPLY
    : '*'
    ;

DIVIDE
    : '/'
    ;

// fragments

fragment WhiteSpace
    : ' '
    | '\t'
    | '\r'
    | '\n'
    ;

fragment NL
    : '\n'
    | '\r' '\n'?
    ;

fragment Letter
    : Upper
    | Lower
    ;

fragment Lower
    : 'a' .. 'z'
    ;

fragment Upper
    : 'A' .. 'Z'
    | '_'
    ;

fragment Digit
    : '0'
    | NonZeroDigit
    ;

fragment NonZeroDigit
    : '1' ..'9'
    ;

fragment HexDigit
    : '0' ..'9'
    | 'A' .. 'F'
    | 'a' .. 'f'
    ;

fragment StringElement
    : '\u0020'
    | '\u0021'
    | '\u0023' .. '\u007F'
    ;

fragment StringContent
    : StringElement*
    ;

// Whitespace

NEWLINE
    : NL+ -> skip
    ;

WS
    : WhiteSpace+ -> skip
    ;