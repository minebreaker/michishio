// Java-related grammers are taken from ANTLR grammers repo, licensed under BSD
// https://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4


grammar Michishio;

@header {
    package rip.deadcode.michishio.generated;
}

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

NATURAL : DIGIT+ ;
fragment DIGIT: [0-9] ;

HEX : '0x' HEX_DIGIT+ ;
fragment HEX_DIGIT: [0-9A-Fa-f] ;

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

fragment ESCAPE_SEQUENCE
    : '\\' [btnfr"'\\]
    | '\\' 'u' HEX_DIGIT
    ;

file
    : version import_declaration* constant_declaration? class_declaration
    ;

version
    : 'major' NATURAL ';' 'minor' NATURAL ';'
    ;

import_declaration
    : 'import' java_type_name ';'
    ;

constant_declaration
    : 'constant' ('(' NATURAL ')')? '{' '}'  // TODO
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
    : '=' STRING_LITERAL ';'  // TODO
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
    | 'aconst_null'
    | 'aloat'
    | 'aload_0'
    | 'aload_1'
    | 'aload_2'
    | 'aload_3'
    | 'anewarray'
    | 'areturn'
    | 'arraylength'
    | 'astore'
    | 'astore_0'
    | 'astore_1'
    | 'astore_2'
    | 'astore_3'
    | 'athrow'
    | 'baload'
    | 'bastore'
    | 'bipush'
    | 'caload'
    | 'castore'
    | 'checkcast'
    | 'd2f'
    | 'd2i'
    | 'd2l'
    | 'dadd'
    | 'daload'
    | 'dastore'
    | 'dcmpg'
    | 'dcmpl'
    | 'dconst_0'
    | 'dconst_1'
    | 'ddiv'
    | 'dload'
    | 'dload_0'
    | 'dload_1'
    | 'dload_2'
    | 'dload_3'
    | 'dmul'
    | 'dneg'
    | 'drem'
    | 'dreturn'
    | 'dstore'
    | 'dstore_0'
    | 'dstore_1'
    | 'dstore_2'
    | 'dstore_3'
    | 'dsub'
    | 'dup'
    | 'dup_x1'
    | 'dup_x2'
    | 'dup2'
    | 'dup2_x1'
    | 'dup2_x2'
    | 'f2d'
    | 'f2i'
    | 'f2l'
    | 'fadd'
    | 'faload'
    | 'fastore'
    | 'fcmpg'
    | 'fcmpl'
    | 'fconst_0'
    | 'fconst_1'
    | 'fconst_2'
    | 'fdiv'
    | 'fload'
    | 'fload_0'
    | 'fload_1'
    | 'fload_2'
    | 'fload_3'
    | 'fmul'
    | 'fneg'
    | 'frem'
    | 'freturn'
    | 'fstore'
    | 'fstore_0'
    | 'fstore_1'
    | 'fstore_2'
    | 'fstore_3'
    | 'fsub'
    | 'getfield'
    | 'getstatic'
    | 'goto'
    | 'goto_w'
    | 'i2b'
    | 'i2c'
    | 'i2d'
    | 'i2f'
    | 'i2l'
    | 'i2s'
    | 'iadd'
    | 'iaload'
    | 'iand'
    | 'iastore'
    | 'iconst_m1'
    | 'iconst_0'
    | 'iconst_1'
    | 'iconst_2'
    | 'iconst_3'
    | 'iconst_4'
    | 'iconst_5'
    | 'idiv'
    | 'if_acmpeq'
    | 'if_acmpne'
    | 'if_icmpeq'
    | 'if_icmpne'
    | 'if_icmplt'
    | 'if_icmpfe'
    | 'if_icmpgt'
    | 'if_icmple'
    | 'ifeq'
    | 'ifne'
    | 'iflt'
    | 'ifge'
    | 'ifgt'
    | 'ifle'
    | 'ifnonnull'
    | 'ifnull'
    | 'iinc'
    | 'iload'
    | 'iload_0'
    | 'iload_1'
    | 'iload_2'
    | 'iload_3'
    | 'imul'
    | 'ineg'
    | 'instanceof'
    | 'invokedynamic'
    | 'invokeinterface'
    | 'invokespecial'
    | 'invokestatic'
    | 'invokevirtual'
    | 'ior'
    | 'irem'
    | 'ireturn'
    | 'ishl'
    | 'ishr'
    | 'istore'
    | 'istore_0'
    | 'istore_1'
    | 'istore_2'
    | 'istore_3'
    | 'isub'
    | 'iushr'
    | 'ixor'
    | 'jsr'
    | 'jsr_w'
    | 'l2d'
    | 'l2f'
    | 'l2i'
    | 'ladd'
    | 'laload'
    | 'land'
    | 'lastore'
    | 'lcmp'
    | 'lconst_0'
    | 'lconst_1'
    | 'ldc'
    | 'ldc_w'
    | 'ldc2_w'
    | 'ldiv'
    | 'lload'
    | 'lload_0'
    | 'lload_1'
    | 'lload_2'
    | 'lload_3'
    | 'lmul'
    | 'lneg'
    | 'lookupswitch'
    | 'lor'
    | 'lrem'
    | 'lreturn'
    | 'lshl'
    | 'lshr'
    | 'lstore'
    | 'lstore_0'
    | 'lstore_1'
    | 'lstore_2'
    | 'lstore_3'
    | 'lsub'
    | 'lushr'
    | 'lxor'
    | 'monitorenter'
    | 'monitorexit'
    | 'multianewarray'
    | 'new'
    | 'newarray'
    | 'nop'
    | 'pop'
    | 'pop2'
    | 'putfield'
    | 'putstatic'
    | 'ret'
    | 'return'
    | 'saload'
    | 'sastore'
    | 'sipush'
    | 'swap'
    | 'tableswitch'
    | 'wide'
    ;

argument
    : java_type_name
    | STRING_LITERAL
    | HEX
    ;

general_attribute
    : STRING_LITERAL '=' attribute_value+ ';'
    ;

attribute_value
    : STRING_LITERAL  // TODO
    ;

WS
    : (' ' | '\t' | '\r' | '\n') -> skip
    ;

COMMENT
    :   '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;
