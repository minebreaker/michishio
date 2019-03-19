grammar Michishio;

@header {
    package rip.deadcode.michishio.generated;
    import rip.deadcode.michishio.ErrorAccumulator;
}

// 初期化子を使い、パーサー/レクサーのインスタンス作成時にリスナーをセットする。
// http://stackoverflow.com/questions/11194458/forcing-antlr-to-use-my-custom-treeadaptor-in-a-parser
@members {
    {
        this.removeErrorListeners();
        this.addErrorListener(ErrorAccumulator.INSTANCE);
    }
}

WS
    : (' ' | '\t' | '\r' | '\n') -> skip
    ;

// keywords
PUBLIC : 'public' ;
FINAL : 'final' ;
SUPER : 'super' ;
INTERFACE : 'interface' ;
ABSTRACT : 'abstract' ;
SYNTHETIC : 'synthetic' ;
ANNOTATION : 'annotation' ;
ENUM : 'enum' ;
MODULE : 'module' ;

PRIVATE : 'private' ;
PROTECTED : 'protected' ;
STATIC : 'static' ;
VOLATILE : 'volatile' ;
TRANSIENT : 'transient' ;

SYNCHRONIZED : 'synchronized' ;
BRIDGE : 'bridge' ;
VARARGS : 'varargs' ;
NATIVE : 'native' ;
STRICT : 'strict' ;

INT : DIGIT+ ;
fragment DIGIT: [0-9] ;

HEX : '0x' HEX_DIGIT+ ;
fragment HEX_DIGIT: [0-9A-Fa-f] ;

// https://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4
JAVA_IDENTIFIER
    : JAVA_LETTER JAVA_LETTER_OR_DIGIT*
    ;
fragment JAVA_LETTER
    : [a-zA-Z$_]
    | ~[\u0000-\u007F\uD800-\uDBFF] {Character.isJavaIdentifierStart(_input.LA(-1))}?
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;
fragment JAVA_LETTER_OR_DIGIT
    : [a-zA-Z0-9$_]
    | ~[\u0000-\u007F\uD800-\uDBFF] {Character.isJavaIdentifierPart(_input.LA(-1))}?
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

STRING_LITERAL
    : '"' STRING_CHARACTERS? '"'
    ;

fragment STRING_CHARACTERS
    : STRING_CHARACTER+
    ;

fragment STRING_CHARACTER
    : ~["\\\r\n]
    | ESCAPE_SEQUENCE
    ;

file
    : version import_declaration* constant_declaration? class_declaration
    ;

version
    : 'major' INT ';' 'minor' INT ';'
    ;

import_declaration
    : 'import' java_type_name ';'
    ;

constant_declaration
    : 'constant' ('(' INT ')')? '{' '}'  // TODO
    ;

java_type_name
    : JAVA_IDENTIFIER
    | package_or_type_name
    ;

package_or_type_name
    : JAVA_IDENTIFIER
    | package_or_type_name '.' JAVA_IDENTIFIER
    ;

class_declaration
    : class_access_flag* 'class' java_type_name inheritance '{' (field_declaration | method_declaration)* attribute* '}'
    ;

class_access_flag
    : PUBLIC
    | FINAL
    | SUPER
    | INTERFACE
    | ABSTRACT
    | SYNTHETIC
    | ANNOTATION
    | ENUM
    | MODULE
    ;

inheritance
    : ('extends' java_type_name)? ('implements' interfaces )?
    ;

interfaces
    : java_type_name (',' java_type_name)*
    ;

field_declaration
    : field_access_flag* field_type java_type_name (';' | attribute_notation | constant_field_notation)
    ;

field_access_flag
    : PUBLIC
    | PRIVATE
    | PROTECTED
    | STATIC
    | FINAL
    | VOLATILE
    | TRANSIENT
    | SYNTHETIC
    | ENUM
    ;

field_type
    : java_type_name
    | STRING_LITERAL
    ;

attribute_notation
    : '{' attribute* '}'
    ;

constant_field_notation
    : '=' STRING_LITERAL ';'
    ;

fragment ESCAPE_SEQUENCE
    : '\\' [btnfr"'\\]
    | '\\' 'u' HEX_DIGIT
    ;

method_declaration
    : method_access_flag* method_return_type java_type_name '(' method_arguments? ')' (';' | attribute_notation)
    ;

method_access_flag
    : PUBLIC
    | PRIVATE
    | PROTECTED
    | STATIC
    | FINAL
    | SYNCHRONIZED
    | BRIDGE
    | VARARGS
    | NATIVE
    | ABSTRACT
    | STRICT
    | SYNTHETIC
    ;

method_return_type
    : STRING_LITERAL
    | java_type_name
    ;

method_arguments
    : method_argument (',' method_argument)*
    ;

method_argument
    : java_type_name
    ;

attribute
    : predefined_attribute
    | general_attribute
    ;

predefined_attribute
    : constant_value_attribute
    | code_attribute
    // TODO
    ;

constant_value_attribute
    : 'ConstantValue' '=' STRING_LITERAL ';'
    ;

code_attribute
    : 'Code' '{' operation* '}'
    ;

operation
    : instruction argument* ';'
    ;

instruction
    : 'aaload'
    | 'aastore'
    // TODO
    ;

argument
    : java_type_name
    | STRING_LITERAL
    | HEX
    ;

general_attribute
    : STRING_LITERAL '=' STRING_LITERAL ';'  // TODO
    ;


