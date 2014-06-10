package actions;

import org.jboss.seam.annotations.Name;

@Name("fooTestElHighlighting")
public class FooTestComponent {
   private String str = "#{strings.size()}";
   private String str2 = "#{stringsFactory.size()}";
}