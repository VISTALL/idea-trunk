set lib=..\lib
set cp=..\classes\production\modelgen;%lib%\dtdparser113.jar;%lib%\jdom.jar;%lib%\xercesImpl.jar
java -cp %cp% org.jbez.modelgen.Main xsd maven maven/by-xsd ../default-config.xml