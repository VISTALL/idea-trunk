package actions;

import org.jboss.seam.annotations.Name;

public class FooComponentDefinedInComponentsXml {

    @DataModel
    private List<String> strings;

    @Factory("stringsFactory")
    public List<String> createMessages() {return null;}
}