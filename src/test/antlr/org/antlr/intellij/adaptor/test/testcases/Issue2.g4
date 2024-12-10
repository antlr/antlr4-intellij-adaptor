grammar Issue2;

@header {
package org.antlr.intellij.adaptor.test.testcases;
}

block
    :   'start' ID ';'
        usesList
        'end' ID ';'
    ;

usesList
    :   'uses' ID (',' ID)* ';'
    ;

ID: [a-zA-Z]+;
WS: [\t\r\n ]+ -> skip;