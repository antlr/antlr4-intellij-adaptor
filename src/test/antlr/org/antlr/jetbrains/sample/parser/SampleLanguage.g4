/** A simple language for use with this sample plugin.
 *  It's C-like but without semicolons. Symbol resolution semantics are
 *  C-like: resolve symbol in current scope. If not in this scope, ask
 *  enclosing scope to resolve (recurse up tree until no more scopes or found).
 *  Forward refs allowed for functions but not variables. Globals must
 *  appear first syntactically.
 *
 *  Generate the parser via "mvn compile" from root dir of project.
 */
grammar SampleLanguage;

// TODO: this is an issue in gradle plugin
// https://discuss.gradle.org/t/antlr-plugin-should-preserve-package-structure/10153/8
// there should be better solution
@header {
 package org.antlr.jetbrains.sample.parser;
}

/** The start rule must be whatever you would normally use, such as script
 *  or compilationUnit, etc...
 */
script
	:	(vardef | function | statement)* EOF
	;

function
	:	FUNC ID LPAREN formal_args? RPAREN (COLON type)? block
	;

formal_args : formal_arg (COMMA formal_arg)* ;

formal_arg : ID COLON type ;

type:	TYPEINT                                              // IntTypeSpec
	|	TYPEFLOAT                                            // FloatTypeSpec
	|	TYPESTRING                                           // StringTypeSpec
	|	TYPEBOOLEAN											 // BooleanTypeSpec
	|   LBRACK RBRACK                                        // VectorTypeSpec
	;

block
	:  LBRACE (statement|vardef)* RBRACE;

statement
	:	IF LPAREN expr RPAREN statement (ELSE statement)?	// If
	|	WHILE LPAREN expr RPAREN statement					// While
	|	ID EQUAL expr										// Assign
	|	ID LBRACK expr RBRACK EQUAL expr				    // ElementAssign
	|	call_expr											// CallStatement
	|	PRINT LPAREN expr? RPAREN							// Print
	|	RETURN expr										    // Return
	|	block				 								// BlockStatement
	;

vardef : VAR ID EQUAL expr ;

expr
	:	expr operator expr									// Op
	|	SUB expr											// Negate
	|	BANG expr											// Not
	|	call_expr											// Call
	|	ID LBRACK expr RBRACK								// Index
	|	LPAREN expr RPAREN									// Parens
	|	primary												// Atom
	;

operator  : MUL|DIV|ADD|SUB|GT|GE|LT|LE|EQUAL_EQUAL|NOT_EQUAL|OR|AND|DOT ; // no implicit precedence

call_expr
	: ID LPAREN expr_list? RPAREN ;

expr_list : expr (COMMA expr)* ;

primary
	:	ID													// Identifier
	|	INT													// Integer
	|	FLOAT												// Float
	|	STRING												// String
	|	LBRACK expr_list RBRACK								// Vector
	|	TRUE												// TrueLiteral
	|	FALSE												// FalseLiteral
	;

LPAREN : '(' ;
RPAREN : ')' ;
COLON : ':' ;
COMMA : ',' ;
LBRACK : '[' ;
RBRACK : ']' ;
LBRACE : '{' ;
RBRACE : '}' ;
EQUAL : '=' ;
SUB : '-' ;
BANG : '!' ;
MUL : '*' ;
DIV : '/' ;
ADD : '+' ;
LT : '<' ;
LE : '<=' ;
EQUAL_EQUAL : '==' ;
NOT_EQUAL : '!=' ;
GT : '>' ;
GE : '>=' ;
OR : '||' ;
AND : '&&' ;
DOT : ' . ' ;
IF : 'if' ;
ELSE : 'else' ;
WHILE : 'while' ;
VAR : 'var' ;
RETURN : 'return' ;
PRINT : 'print' ;
FUNC : 'func' ;
TYPEINT : 'int' ;
TYPEFLOAT : 'float' ;
TYPESTRING : 'string' ;
TYPEBOOLEAN : 'boolean' ;
TRUE : 'true' ;
FALSE : 'false' ;

LINE_COMMENT : '//' .*? ('\n'|EOF)	-> channel(HIDDEN) ;
COMMENT      : '/*' .*? '*/'    	-> channel(HIDDEN) ;

ID  : [a-zA-Z_] [a-zA-Z0-9_]* ;
INT : [0-9]+ ;
FLOAT
	:   '-'? INT '.' INT EXP?   // 1.35, 1.35E-9, 0.3, -4.5
	|   '-'? INT EXP            // 1e10 -3e4
	;
fragment EXP :   [Ee] [+\-]? INT ;

STRING :  '"' (ESC | ~["\\])* '"' ;
fragment ESC :   '\\' ["\bfnrt] ;

WS : [ \t\n\r]+ -> channel(HIDDEN) ;

/** "catch all" rule for any char not matche in a token rule of your
 *  grammar. Lexers in Intellij must return all tokens good and bad.
 *  There must be a token to cover all characters, which makes sense, for
 *  an IDE. The parser however should not see these bad tokens because
 *  it just confuses the issue. Hence, the hidden channel.
 */
ERRCHAR
	:	.	-> channel(HIDDEN)
	;

