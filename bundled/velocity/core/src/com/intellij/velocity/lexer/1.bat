@echo off
set JFLEX_HOME="C:\JetBrains\IntelliJ IDEA 9601\tools\jflex-1.4.2"
%JFLEX_HOME%\bin\jflex.bat -d . --charat --skel %JFLEX_HOME%\idea-flex.skeleton _VtlLexer.flex

