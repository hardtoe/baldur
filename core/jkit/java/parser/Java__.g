lexer grammar Java;

@members {
protected boolean enumIsKeyword = true;
	public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
      String text = "?";
      if(e.token != null) {
       text = e.token.getText();
      }
	  throw new SyntaxError("error on \"" + text +"\"",e.line,e.charPositionInLine,text.length());
    } 
}
@header {
package jkit.java.parser;
import jkit.compiler.SyntaxError;
}

T__120 : 'package' ;
T__121 : ';' ;
T__122 : 'import' ;
T__123 : 'static' ;
T__124 : '.' ;
T__125 : '*' ;
T__126 : 'class' ;
T__127 : 'extends' ;
T__128 : 'implements' ;
T__129 : '<' ;
T__130 : ',' ;
T__131 : '>' ;
T__132 : '&' ;
T__133 : '{' ;
T__134 : '}' ;
T__135 : 'interface' ;
T__136 : 'void' ;
T__137 : 'throws' ;
T__138 : '[' ;
T__139 : ']' ;
T__140 : '=' ;
T__141 : 'public' ;
T__142 : 'protected' ;
T__143 : 'private' ;
T__144 : 'abstract' ;
T__145 : 'final' ;
T__146 : 'native' ;
T__147 : 'synchronized' ;
T__148 : 'transient' ;
T__149 : 'volatile' ;
T__150 : 'strictfp' ;
T__151 : 'boolean' ;
T__152 : 'char' ;
T__153 : 'byte' ;
T__154 : 'short' ;
T__155 : 'int' ;
T__156 : 'long' ;
T__157 : 'float' ;
T__158 : 'double' ;
T__159 : '?' ;
T__160 : 'super' ;
T__161 : '(' ;
T__162 : ')' ;
T__163 : '...' ;
T__164 : 'null' ;
T__165 : 'true' ;
T__166 : 'false' ;
T__167 : '@' ;
T__168 : 'default' ;
T__169 : 'assert' ;
T__170 : ':' ;
T__171 : 'if' ;
T__172 : 'else' ;
T__173 : 'for' ;
T__174 : 'while' ;
T__175 : 'do' ;
T__176 : 'try' ;
T__177 : 'finally' ;
T__178 : 'switch' ;
T__179 : 'return' ;
T__180 : 'throw' ;
T__181 : 'break' ;
T__182 : 'continue' ;
T__183 : 'catch' ;
T__184 : 'case' ;
T__185 : '+' ;
T__186 : '-' ;
T__187 : '/' ;
T__188 : '&=' ;
T__189 : '|=' ;
T__190 : '^=' ;
T__191 : '%' ;
T__192 : '||' ;
T__193 : '&&' ;
T__194 : '|' ;
T__195 : '^' ;
T__196 : '==' ;
T__197 : '!=' ;
T__198 : 'instanceof' ;
T__199 : '++' ;
T__200 : '--' ;
T__201 : '~' ;
T__202 : '!' ;
T__203 : 'new' ;

// $ANTLR src "jkit/java/parser/Java.g" 947
HexLiteral : '0' ('x'|'X') HexDigit+ IntegerTypeSuffix? ;

// $ANTLR src "jkit/java/parser/Java.g" 949
DecimalLiteral : ('0' | '1'..'9' '0'..'9'*) IntegerTypeSuffix? ;

// $ANTLR src "jkit/java/parser/Java.g" 951
OctalLiteral : '0' ('0'..'7')+ IntegerTypeSuffix? ;

// $ANTLR src "jkit/java/parser/Java.g" 953
fragment
HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

// $ANTLR src "jkit/java/parser/Java.g" 956
fragment
IntegerTypeSuffix : ('l'|'L') ;

// $ANTLR src "jkit/java/parser/Java.g" 959
FloatingPointLiteral
    :   ('0'..'9')+ '.' ('0'..'9')* Exponent? FloatTypeSuffix?
    |   '.' ('0'..'9')+ Exponent? FloatTypeSuffix?
    |   ('0'..'9')+ Exponent
    |	('0'..'9')+ FloatTypeSuffix
    |   ('0'..'9')+ Exponent FloatTypeSuffix
	;

// $ANTLR src "jkit/java/parser/Java.g" 967
fragment
Exponent : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

// $ANTLR src "jkit/java/parser/Java.g" 970
fragment
FloatTypeSuffix : ('f'|'F'|'d'|'D') ;

// $ANTLR src "jkit/java/parser/Java.g" 973
CharacterLiteral
    :   '\'' ( EscapeSequence | ~('\''|'\\') ) '\''
    ;

// $ANTLR src "jkit/java/parser/Java.g" 977
StringLiteral
    :  '"' ( EscapeSequence | ~('\\'|'"') )* '"'
    ;

// $ANTLR src "jkit/java/parser/Java.g" 981
fragment
EscapeSequence
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UnicodeEscape
    |   OctalEscape
    ;

// $ANTLR src "jkit/java/parser/Java.g" 988
fragment
OctalEscape
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

// $ANTLR src "jkit/java/parser/Java.g" 995
fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

// $ANTLR src "jkit/java/parser/Java.g" 1000
ENUM:	'enum' {if ( !enumIsKeyword ) $type=Identifier;}
	;
	
// $ANTLR src "jkit/java/parser/Java.g" 1003
Identifier 
    :   Letter (Letter|JavaIDDigit)*
    ;

// $ANTLR src "jkit/java/parser/Java.g" 1007
/**I found this char range in JavaCC's grammar, but Letter and Digit overlap.
   Still works, but...
 */
fragment
Letter
    :  '\u0024' |
       '\u0041'..'\u005a' |
       '\u005f' |
       '\u0061'..'\u007a' |
       '\u00c0'..'\u00d6' |
       '\u00d8'..'\u00f6' |
       '\u00f8'..'\u00ff' |
       '\u0100'..'\u1fff' |
       '\u3040'..'\u318f' |
       '\u3300'..'\u337f' |
       '\u3400'..'\u3d2d' |
       '\u4e00'..'\u9fff' |
       '\uf900'..'\ufaff'
    ;

// $ANTLR src "jkit/java/parser/Java.g" 1027
fragment
JavaIDDigit
    :  '\u0030'..'\u0039' |
       '\u0660'..'\u0669' |
       '\u06f0'..'\u06f9' |
       '\u0966'..'\u096f' |
       '\u09e6'..'\u09ef' |
       '\u0a66'..'\u0a6f' |
       '\u0ae6'..'\u0aef' |
       '\u0b66'..'\u0b6f' |
       '\u0be7'..'\u0bef' |
       '\u0c66'..'\u0c6f' |
       '\u0ce6'..'\u0cef' |
       '\u0d66'..'\u0d6f' |
       '\u0e50'..'\u0e59' |
       '\u0ed0'..'\u0ed9' |
       '\u1040'..'\u1049'
   ;

// $ANTLR src "jkit/java/parser/Java.g" 1046
WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;}
    ;

// $ANTLR src "jkit/java/parser/Java.g" 1049
COMMENT
    :   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

// $ANTLR src "jkit/java/parser/Java.g" 1053
LINE_COMMENT
    : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    ;
