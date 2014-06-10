set lib=..\lib
set cp=..\classes\production\modelgen;%lib%\dtdparser113.jar;%lib%\jdom.jar;%lib%\xercesImpl.jar
java -cp %cp% org.jbez.modelgen.Main xsd "persistence 2.0" "persistence 2.0" ../default-config.xml