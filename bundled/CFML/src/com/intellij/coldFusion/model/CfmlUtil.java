package com.intellij.coldFusion.model;

import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.psi.PsiType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.URLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import sun.reflect.Reflection;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Lera Nikolaenko
 * Date: 20.10.2008
 */
public class CfmlUtil {
  public static Map<IElementType, IElementType> myMapToJScript = new HashMap<IElementType, IElementType>();
  public static Set<String> myVariableScopes = new LinkedHashSet<String>();

  private class CfmlStandartFunction {
    private String myName;
    private PsiType myReturnType;

    private CfmlStandartFunction(String myName, PsiType myReturnType) {
      this.myName = myName;
      this.myReturnType = myReturnType;
    }

    public String getMyName() {
      return myName;
    }
  }

  private static Set<String> myPredefinedFunctions = ContainerUtil.map2Set(new String[]{
    "abs", "getHttpTimeString", "min", "aCos", "getK2ServerDocCount", "minute", "arrayAppend", "getK2ServerDocCountLimit", "month",
    "arrayAvg", "getLocale", "monthAsString", "arrayClear", "getMetaData", "now", "arrayDeleteAt", "getMetricData", "numberFormat",
    "arrayInsertAt", "getPageContext", "paragraphFormat", "arrayIsEmpty", "getProfileSections", "parameterExists", "arrayLen",
    "getProfileString", "parseDateTime", "arrayMax", "getTempDirectory", "arrayMin", "getTempFile", "preserveSingleQuotes", "arrayNew",
    "getTemplatePath", "quarter", "arrayPrepend", "getTickCount", "queryAddColumn", "arrayResize", "getTimeZoneInfo", "queryAddRow",
    "arraySet", "getToken", "queryNew", "arraySort", "hash", "querySetCell", "arraySum", "hour", "quotedValueList", "arraySwap",
    "htmlCodeFormat", "rand", "arrayToList", "htmlEditFormat", "randomize", "asc", "iIf", "randRange", "aSin", "incrementValue", "reFind",
    "atn", "inputBaseN", "reFindNoCase", "bitAnd", "insert", "releaseComObject", "bitMaskClear", "int", "removeChars", "bitMaskRead",
    "isArray", "repeatString", "bitMaskSet", "isBinary", "replace", "bitNot", "isBoolean", "replaceList", "bitOr", "isCustomFunction",
    "replaceNoCase", "bitSHLN", "isDate", "rEReplace", "bitSHRN", "isDebugMode", "reReplaceNoCase", "bitXor", "isDefined", "reverse",
    "ceiling", "isK2ServerABroker", "right", "chr", "isK2ServerDocCountExceeded", "rJustify", "cJustify", "isK2ServerOnline", "round",
    "compare", "isLeapYear", "rTrim", "compareNoCase", "isNumeric", "second", "cos", "isNumericDate", "setEncoding", "createDate",
    "isObject", "setLocale", "createDateTime", "isQuery", "setProfileString", "createObject", "isSimpleValue", "setVariable",
    "createODBCDate", "isStruct", "sgn", "createODBCDateTime", "isUserInRole", "sin", "createODBCTime", "isWDDX", "spanExcluding",
    "createTime", "isXmlDoc", "spanIncluding", "createTimeSpan", "isXmlElem", "sqr", "createUUID", "isXmlRoot", "stripCR", "dateAdd",
    "javaCast", "structAppend", "dateCompare", "jsStringFormat", "structClear", "dateConvert", "lCase", "structCopy", "dateDiff", "left",
    "structCount", "dateFormat", "len", "structDelete", "datePart", "listAppend", "structFind", "day", "listChangeDelims", "structFindKey",
    "dayOfWeek", "listContains", "structFindValue", "dayOfWeekAsString", "listContainsNoCase", "structGet", "dayOfYear", "listDeleteAt",
    "structInsert", "daysInMonth", "listFind", "structIsEmpty", "daysInYear", "listFindNoCase", "structKeyArray", "listFirst",
    "structKeyExists", "decimalFormat", "listGetAt", "structKeyList", "decrementValue", "listInsertAt", "structNew", "decrypt", "listLast",
    "structSort", "deleteClientVariable", "listLen", "structUpdate", "directoryExists", "listPrepend", "tan", "dollarFormat", "listQualify",
    "timeFormat", "duplicate", "listRest", "toBase64", "encrypt", "listSetAt", "toBinary", "evaluate", "listSort", "toString", "exp",
    "listToArray", "trim", "expandPath", "listValueCount", "uCase", "fileExists", "listValueCountNoCase", "urlDecode", "find", "lJustify",
    "urlEncodedFormat", "findNoCase", "log", "urlSessionFormat", "findOneOf", "log10", "val", "firstDayOfMonth", "lsCurrencyFormat",
    "valueList", "fix", "lsDateFormat", "week", "formatBaseN", "lsEuroCurrencyFormat", "wrap", "getAuthUser", "lsIsCurrency", "writeOutput",
    "getBaseTagData", "lsIsDate", "xmlChildPos", "getBaseTagList", "lsIsNumeric", "xmlElemNew", "getBaseTemplatePath", "lsNumberFormat",
    "xmlFormat", "getClientVariablesList", "lsParseCurrency", "xmlNew", "getCurrentTemplatePath", "lsParseDateTime", "xmlParse",
    "getDirectoryFromPath", "lsParseEuroCurrency", "xmlSearch", "getEncoding", "lsParseNumber", "xmlTransform", "getException",
    "lsTimeFormat", "year", "getFileFromPath", "lTrim", "yesNoFormat", "getFunctionList", "max", "getHttpRequestData", "mid"},
                                                                           new Function<String, String>() {
                                                                             public String fun(String s) {
                                                                               return s;
                                                                             }
                                                                           });
  private static Set<String> ourLowerCasePredefinedFunctions = new HashSet<String>();

  static {
    for (String s : myPredefinedFunctions) {
      ourLowerCasePredefinedFunctions.add(s.toLowerCase());
    }
  }

  public static String[] getAttributeValues(String tagName, String attributeName) {
    return getAttribute(tagName, attributeName).getValues();
  }

  static {
    // repack predefined functions to LowCase
    Set<String> myPredefinedFunctionsLowCase = new HashSet();
    for (String s : myPredefinedFunctions) {
      myPredefinedFunctionsLowCase.add(s.toLowerCase());
    }

    // myPredefinedFunctions = myPredefinedFunctionsLowCase;

    myVariableScopes.add("application");
    myVariableScopes.add("arguments");
    myVariableScopes.add("attributes");
    myVariableScopes.add("caller");
    myVariableScopes.add("cgi");
    myVariableScopes.add("client");
    myVariableScopes.add("cookie");
    myVariableScopes.add("flash");
    myVariableScopes.add("form");
    myVariableScopes.add("request");
    myVariableScopes.add("server");
    myVariableScopes.add("session");
    myVariableScopes.add("this");
    myVariableScopes.add("thistag");
    myVariableScopes.add("thread");
    myVariableScopes.add("url");
    myVariableScopes.add("variables");
    /*
    // TODO: ?
    myVariableScopes.add("function local");
    myVariableScopes.add("thread local");
    */
  }

  public static class TagDescription {
    private String myDescription = "";
    private AttributeFormat[] myAttributes = null;
    private boolean myIsSingle = false;

    public TagDescription(boolean isSingle, String description, AttributeFormat[] attributes) {
      myDescription = description;
      myAttributes = attributes;
      myIsSingle = isSingle;
    }

    public String getDescription() {
      return myDescription;
    }

    public AttributeFormat[] getAttributes() {
      return myAttributes;
    }

    public boolean isSingle() {
      return myIsSingle;
    }

  }

  public static class AttributeFormat implements Comparable<AttributeFormat> {
    private Pattern myNamePattern;
    // private String myName;
    private int myType;
    private boolean myRequired;
    private String myDescription;
    private String myCompletionExample = null;
    private String[] myValues = null;

    public static final int STRING_TYPE = 0;
    public static final int NUMERIC_TYPE = 1;
    public static final int BOOLEAN_TYPE = 2;
    public static final int TIMESPAN_TYPE = 3;
    public static final int VARIABLENAME_TYPE = 4;
    public static final int URL_TYPE = 5;
    public static final int OBJECT_TYPE = 6;
    public static final int REGEX_TYPE = 7;
    public static final int STRUCT_TYPE = 8;
    public static final int DATETIME_TYPE = 9;
    public static final int CHAR_TYPE = 10;
    public static final int ANY_TYPE = 11;
    public static final int ARRAY_TYPE = 12;
    public static final int BINARY_TYPE = 13;
    public static final int QUERY_TYPE = 14;
    public static final int QUERYCOLUMN_TYPE = 15;
    public static final int NODE_TYPE = 16;

    public AttributeFormat(String name, int type, boolean required, String description) {
      myNamePattern = Pattern.compile(name);
      myType = type;
      myRequired = required;
      myDescription = description;
    }

    public AttributeFormat(String name, int type, boolean required, String description, String completionExample) {
      this(name, type, required, description);
      myCompletionExample = completionExample;
    }

    public void addValue(String value) {
      if (myValues == null) {
        myValues = new String[0];
      }
      myValues = ArrayUtil.append(myValues, value);
    }

    public String[] getValues() {
      return myValues;
    }

    public String getName() {
      return myNamePattern.matcher(myNamePattern.pattern()).matches() ? myNamePattern.pattern() : myCompletionExample;
    }

    public String getDescription() {
      return myDescription;
    }

    public boolean acceptName(String name) {
      return myNamePattern.matcher(name).matches();
    }

    public int getType() {
      return myType;
    }

    public boolean isRequired() {
      return myRequired;
    }

    public int compareTo(AttributeFormat o) {
      return myNamePattern.pattern().compareTo(o.myNamePattern.pattern());

    }

    @Override
    public String toString() {
      return "<div>Name: " +
             myNamePattern.pattern() +
             "</div>" +
             "<div>Description: " +
             getDescription() +
             "</div>" +
             "<div>Type: " +
             getType() +
             "</div>" +
             "<div>IsRequired: " +
             isRequired() +
             "</div>";
    }
  }

  private static String[] mySingleTags = {"cfelse", "cfset"};
  private static HashMap<String, TagDescription> myTagAttributes = new HashMap<String, TagDescription>();
  private static AttributeFormat[] myEmptyAttributesArray = new AttributeFormat[0];

  static {
    myTagAttributes.put("cfabort", new TagDescription(true,
                                                      "Stops the processing of a CFML page at the tag location.CFML returns everything that was processed before thetag. The tag is often used with conditional logic to stopprocessing a page when a condition occurs.",
                                                      new AttributeFormat[]{
                                                        new AttributeFormat("showerror", AttributeFormat.STRING_TYPE, false,
                                                                            "Error to display, in a standard CFML error page,when tag executes")}));
    myTagAttributes.put("cfapplet", new TagDescription(true,
                                                       "This tag references a registered custom Java applet. Toregister a Java applet, in the CFML Administrator, clickExtensions \\ Java Applets.Using this tag within a cfform tag is optional. If you use itwithin cfform, and the method attribute is defined in theAdministrator, the return value is incorporated into theform.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("appletsource", AttributeFormat.STRING_TYPE, true,
                                                                             "Name of registered applet"),
                                                         new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                             "Form variable name for applet"),
                                                         new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Control's height, in pixels."),
                                                         new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Control's width, in pixels."),
                                                         new AttributeFormat("vspace", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Vertical margin above and below control, in pixels."),
                                                         new AttributeFormat("hspace", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Horizontal spacing to left and right of control, in pixels."),
                                                         new AttributeFormat("align", AttributeFormat.STRING_TYPE, false, "Alignment"),
                                                         new AttributeFormat("notsupported", AttributeFormat.STRING_TYPE, false,
                                                                             "Text to display if a page that contains a Java applet-basedcfform control is opened by a browser that does notsupport Java or has Java support disabled.Default:\"<b>Browser must support Java to <br>view ColdFusion JavaApplets!</b>\"")}));
    myTagAttributes.put("cfapplication", new TagDescription(true,
                                                            "Defines the scope of a CFML application; enables anddisables storage of Client variables; specifies the Clientvariable storage mechanism; enables Session variables; andsets Application variable timeouts.",
                                                            new AttributeFormat[]{
                                                              new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                                  "Name of application. Up to 64 characters"),
                                                              new AttributeFormat("loginstorage", AttributeFormat.STRING_TYPE, false, ""),
                                                              new AttributeFormat("clientmanagement", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                  "enables client variables"),
                                                              new AttributeFormat("clientstorage", AttributeFormat.STRING_TYPE, false,
                                                                                  "How client variables are stored* datasource_name: in ODBC or native data source.You must create storage repository in theAdministrator.* registry: in the system registry.* cookie: on client computer in a cookie. Scalable.If client disables cookies in the browser, clientvariables do not work"),
                                                              new AttributeFormat("setclientcookies", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                  "No: CFML does not automatically send CFID and CFTOKENcookies to client browser; you must manually code CFID andCFTOKEN on the URL for every page that uses Session orClient variables"),
                                                              new AttributeFormat("sessionmanagement", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                  "enables session variables"),
                                                              new AttributeFormat("sessiontimeout", AttributeFormat.TIMESPAN_TYPE, false,
                                                                                  "Lifespan of session variables. CreateTimeSpan function andvalues in days, hours, minutes, and seconds, separated bycommas"),
                                                              new AttributeFormat("applicationtimeout", AttributeFormat.TIMESPAN_TYPE,
                                                                                  false,
                                                                                  "Lifespan of application variables. CreateTimeSpan functionand values in days, hours, minutes, and seconds, separatedby commas."),
                                                              new AttributeFormat("setdomaincookies", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                  "Yes: Sets CFID and CFTOKEN cookies for a domain (not a host)Required, for applications running on clusters."),
                                                              new AttributeFormat("scriptprotect", AttributeFormat.STRING_TYPE, false,
                                                                                  "Specifies whether to protect variables from cross-site scripting attacks.- none: do not protect variables- all: protect Form, URL, CGI, and Cookie variables- comma-delimited list of ColdFusion scopes: protect variables in the specified scopes")}));
    myTagAttributes.put("cfargument", new TagDescription(true,
                                                         "Creates a parameter definition within a component definition.Defines a function argument. Used within a cffunction tag.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, true,
                                                                               "An argument name."),
                                                           new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                               "a type name; data type of the argument."),
                                                           new AttributeFormat("required", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Whether the parameter is required to execute the componentmethod."),
                                                           new AttributeFormat("default", AttributeFormat.STRING_TYPE, false,
                                                                               "If no argument is passed, specifies a default argumentvalue."),
                                                           new AttributeFormat("displayname", AttributeFormat.STRING_TYPE, false,
                                                                               "Meaningful only for CFC method parameters. A value to bedisplayed when using introspection to show informationabout the CFC."),
                                                           new AttributeFormat("hint", AttributeFormat.STRING_TYPE, false,
                                                                               "Meaningful only for CFC method parameters. Text to bedisplayed when using introspection to show informationabout the CFC. The hint attribute value follows thedisplayname attribute value in the parameter descriptionline. This attribute can be useful for describing thepurpose of the parameter")}));
    myTagAttributes.put("cfassociate",
                        new TagDescription(true, "Allows subtag data to be saved with a base tag. Applies onlyto custom tags.",
                                           new AttributeFormat[]{
                                             new AttributeFormat("basetag", AttributeFormat.STRING_TYPE, true, "Base tag name"),
                                             new AttributeFormat("datacollection", AttributeFormat.STRING_TYPE, false,
                                                                 "Structure in which base tag stores subtag data")}));
    myTagAttributes.put("cfbreak", new TagDescription(true, "Used within a cfloop tag. Breaks out of a loop.", myEmptyAttributesArray));
    myTagAttributes.put("cfcache", new TagDescription(true,
                                                      "Stores a copy of a page on the server and/or client computer,to improve page rendering performance. To do this, the tagcreates temporary files that contain the static HTML returnedfrom a CFML page.Use this tag if it is not necessary to get dynamic content eachtime a user accesses a page.You can use this tag for simple URLs and for URLs that containURL parameters.",
                                                      new AttributeFormat[]{
                                                        new AttributeFormat("action", AttributeFormat.STRING_TYPE, false,
                                                                            "* cache: server-side and client-side caching.* flush: refresh cached page(s).* clientcache: browser-side caching only. To cache a personalized page, use this option.* servercache: server-side caching only. Not recommended.* optimal: same as \"cache\"."),
                                                        new AttributeFormat("directory", AttributeFormat.STRING_TYPE, false,
                                                                            "Absolute path of cache directory."),
                                                        new AttributeFormat("Timespan", AttributeFormat.TIMESPAN_TYPE, false,
                                                                            "Absolute path of cache directory."),
                                                        new AttributeFormat("expireurl", AttributeFormat.STRING_TYPE, false,
                                                                            "Used with action = \"flush\". A URL reference. CFMLmatches it against the mappings in the specified cachedirectory. Can include wildcards. For example:\"*/view.cfm?id=*\"."),
                                                        new AttributeFormat("username", AttributeFormat.STRING_TYPE, false, "A username"),
                                                        new AttributeFormat("password", AttributeFormat.STRING_TYPE, false, "A password"),
                                                        new AttributeFormat("port", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Remote port to which to connect"),
                                                        new AttributeFormat("protocol", AttributeFormat.STRING_TYPE, false,
                                                                            "Protocol that is used to create URL from cache.")}));
    myTagAttributes.put("cfcalendar", new TagDescription(true,
                                                         "Puts an interactive Macromedia Flash format calendar in an HTMLor Flash form. Not supported in XML format forms. The calendarlets a user select a date for submission as a form variable.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                               "The name of the calendar."),
                                                           new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "The vertical dimension of the calendar specified in pixels."),
                                                           new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "The horizontal dimension of the calendar specified in pixels."),
                                                           new AttributeFormat("selecteddate", AttributeFormat.STRING_TYPE, false,
                                                                               "The date that is initially selected. It is highlighted in acolor determined by the form skin. Must be in mm/dd/yyyyor dd/mm/yyyy format, depending on the current locale.(Use the setlocale tagto set the locale, if necessary.)"),
                                                           new AttributeFormat("startrange", AttributeFormat.STRING_TYPE, false,
                                                                               "The start of a range of dates that are disabled. Userscannot select dates from this date through the datespecified by the endRange attribute."),
                                                           new AttributeFormat("endrange", AttributeFormat.STRING_TYPE, false,
                                                                               "The end of a range of dates that are disabled. Userscannot select dates from the date specified by thestartRange attribute through this date."),
                                                           new AttributeFormat("disabled", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Disables all user input, making the control read only.To disable input, specify disabled without an attributeor disabled=\"true\". To enable input, omit the attributeor specify disabled=\"false\".Default is: false"),
                                                           new AttributeFormat("mask", AttributeFormat.STRING_TYPE, false,
                                                                               "A pattern that specifies the format of the submitted date.Mask characters are:- D = day, can use 0-2 mask characters- M = month, can use 0-4 mask characters- Y = year, can use 0, 2, or 4 characters- E = day in week, can use 0-4 characters- Any other character = put the character in the specified locationDefault is: MM/DD/YYYY"),
                                                           new AttributeFormat("firstdayofweek", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "Integer in the range 0-6 specifying the first day of theweek in the calendar, 0 indicates Sunday, 6 indicates Saturday.Default is: 0"),
                                                           new AttributeFormat("daynames", AttributeFormat.STRING_TYPE, false,
                                                                               "A comma-delimited list that sets the names of theweekdays displayed in the calendar. Sunday is thefirst day and the rest of the weekday names follow inthe normal order.Default is: S,M,T,W,Th,F,S"),
                                                           new AttributeFormat("monthnames", AttributeFormat.STRING_TYPE, false,
                                                                               "A comma-delimited list of the month names that aredisplayed at the top of the calendar."),
                                                           new AttributeFormat("enabled", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Flash only: Specifying whether the control is enabled. Adisabled control appears in light gray. This is the inverseof the disabled attribute."),
                                                           new AttributeFormat("visible", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Flash only: Specifying whether to show the control. Spacethat would be occupied by an invisible control is blank."),
                                                           new AttributeFormat("tooltip", AttributeFormat.STRING_TYPE, false,
                                                                               "Flash only: Text to display when the mouse pointer hoversover the control."),
                                                           new AttributeFormat("style", AttributeFormat.STRING_TYPE, false,
                                                                               "Flash only: Actionscript style or styles to apply to the calendar.Default is: haloGreen"),
                                                           new AttributeFormat("onchange", AttributeFormat.STRING_TYPE, false,
                                                                               "Flash only: ActionScript that runs when the user selects adate.")}));
    myTagAttributes.put("cfcase", new TagDescription(false,
                                                     "Used only inside the cfswitch tag body. Contains code toexecute when the expression specified in the cfswitch tag hasone or more specific values.",
                                                     new AttributeFormat[]{new AttributeFormat("value", AttributeFormat.STRING_TYPE, true,
                                                                                               "The value or values that the expression attribute of thecfswitch tag must match. To specify multiple matchingvalues, separate the values with the delimiter character.The value or values must be simple constants or constantexpressions, not variables."),
                                                       new AttributeFormat("delimiters", AttributeFormat.STRING_TYPE, false,
                                                                           "Specifies the delimiter character or characters thatseparate multiple values to match. If you specify multipledelimiter characters, you can use any of them to separatethe values to be matched.")}));
    myTagAttributes.put("cfcatch", new TagDescription(false,
                                                      "Used inside a cftry tag. Together, they catch and processexceptions in CFML pages. Exceptions are events thatdisrupt the normal flow of instructions in a CFML page,such as failed database operations, missing include files, anddeveloper-specified events.",
                                                      new AttributeFormat[]{
                                                        new AttributeFormat("type", AttributeFormat.STRING_TYPE, false, "")}));
    myTagAttributes.put("cfchart", new TagDescription(false, "Generates and displays a chart.", new AttributeFormat[]{
      new AttributeFormat("format", AttributeFormat.STRING_TYPE, false, "File format in which to save graph."),
      new AttributeFormat("chartheight", AttributeFormat.NUMERIC_TYPE, false, "Chart height; integer number of pixels"),
      new AttributeFormat("chartwidth", AttributeFormat.NUMERIC_TYPE, false, "Chart width; integer number of pixels"),
      new AttributeFormat("scalefrom", AttributeFormat.NUMERIC_TYPE, false, "Y-axis minimum value; integer"),
      new AttributeFormat("scaleto", AttributeFormat.NUMERIC_TYPE, false, "Y-axis max value; integer"),
      new AttributeFormat("showxgridlines", AttributeFormat.BOOLEAN_TYPE, false, "yes: display X-axis gridlines"),
      new AttributeFormat("showygridlines", AttributeFormat.BOOLEAN_TYPE, false, "yes: display Y-axis gridlines"),
      new AttributeFormat("gridlines", AttributeFormat.NUMERIC_TYPE, false,
                          "Number of grid lines to display on value axis, includingaxis; positive integer."),
      new AttributeFormat("seriesplacement", AttributeFormat.STRING_TYPE, false,
                          "Applies to charts that have more than one data series.Relative positions of series."),
      new AttributeFormat("foregroundcolor", AttributeFormat.STRING_TYPE, false,
                          "color for control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
      new AttributeFormat("backgroundcolor", AttributeFormat.STRING_TYPE, false,
                          "Color of the area between the data background and the chartborder, around labels and around the legend. Hexadecimalvalue or supported named color. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
      new AttributeFormat("databackgroundcolor", AttributeFormat.STRING_TYPE, false,
                          "color for control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
      new AttributeFormat("showborder", AttributeFormat.BOOLEAN_TYPE, false, ""),
      new AttributeFormat("font", AttributeFormat.STRING_TYPE, false, "Font of data in column."),
      new AttributeFormat("fontsize", AttributeFormat.NUMERIC_TYPE, false, "Size of text in column."),
      new AttributeFormat("fontitalic", AttributeFormat.BOOLEAN_TYPE, false, "Yes: displays grid control text in italics"),
      new AttributeFormat("fontbold", AttributeFormat.BOOLEAN_TYPE, false, "Yes: displays grid control text in bold"),
      new AttributeFormat("labelformat", AttributeFormat.STRING_TYPE, false, "Format for Y-axis labels."),
      new AttributeFormat("xaxistitle", AttributeFormat.STRING_TYPE, false, "text; X-axis title"),
      new AttributeFormat("yaxistitle", AttributeFormat.STRING_TYPE, false, "text; X-axis title"),
      new AttributeFormat("xaxistype", AttributeFormat.STRING_TYPE, false,
                          "* category The axis indicates the data category. Data issorted according to the sortXAxis attribute.* scale The axis is numeric. All cfchartdata item attributevalues must numeric. The X axis is automatically sortednumerically."),
      new AttributeFormat("yaxistype", AttributeFormat.STRING_TYPE, false,
                          "Currently has no effect, as Y axis is always used for datavalues. Valid values are category and scale"),
      new AttributeFormat("sortxaxis", AttributeFormat.BOOLEAN_TYPE, false,
                          "Display column labels in alphabetic order along X-axis.Ignored if the xAxisType attribute is scale."),
      new AttributeFormat("show3d", AttributeFormat.BOOLEAN_TYPE, false, "Display chart with three-dimensional appearance."),
      new AttributeFormat("xoffset", AttributeFormat.NUMERIC_TYPE, false,
                          "Applies if show3D=\"yes\". Number of units by which todisplay the chart as angled, horizontally"),
      new AttributeFormat("yoffset", AttributeFormat.NUMERIC_TYPE, false,
                          "Applies if show3D=\"yes\". Number of units by which todisplay the chart as angled, horizontally."),
      new AttributeFormat("rotated", AttributeFormat.BOOLEAN_TYPE, false,
                          "This attribute is deprecated.yes: rotate chart 90 degrees. For a horizontal bar chart,use this option."),
      new AttributeFormat("showlegend", AttributeFormat.BOOLEAN_TYPE, false,
                          "yes: if chart contains more than one data series, displaylegend"),
      new AttributeFormat("tipstyle", AttributeFormat.STRING_TYPE, false,
                          "Determines the action that opens a popup window to displayinformation about the current chart element.* mouseDown: display if the user positions the cursor at the elementand clicks the mouse. Applies only to Flash format graph files.* mouseOver: displays if the user positions the cursor at the element* none: suppresses display"),
      new AttributeFormat("tipbgcolor", AttributeFormat.STRING_TYPE, false,
                          "color for control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
      new AttributeFormat("showmarkers", AttributeFormat.BOOLEAN_TYPE, false,
                          "Applies to chartseries type attribute values line, curveand scatter.yes: display markers at data points"),
      new AttributeFormat("markersize", AttributeFormat.NUMERIC_TYPE, false, "Size of data point marker. in pixels. Integer."),
      new AttributeFormat("pieslicestyle", AttributeFormat.STRING_TYPE, false, "Applies to chartseries type attribute value pie."),
      new AttributeFormat("URL", AttributeFormat.URL_TYPE, false,
                          "URL to open if the user clicks item in a data series; theonClick destination page.You can specify variables within the URL string;ColdFusion passes current values of the variables.* $VALUE$: the value of the selected row. If none, the value is an empty string.* $ITEMLABEL$: the label of the selected item. If none, the value is an empty string.* $SERIESLABEL$: the label of the selected series. If none, the value is an empty string."),
      new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                          "Page variable name. String. Generates the the graph asbinary data and assigns it to the specified variable.Suppresses chart display. You can use the name value inthe cffile tag to write the chart to a file."),
      new AttributeFormat("style", AttributeFormat.STRING_TYPE, false, "XML file or string to use to specify the style of the chart."),
      new AttributeFormat("title", AttributeFormat.STRING_TYPE, false, "Title of the chart.")}));
    myTagAttributes.put("cfchartdata", new TagDescription(true,
                                                          "Used with the cfchart and cfchartseries tags. This tag defineschart data points. Its data is submitted to the cfchartseriestag.",
                                                          new AttributeFormat[]{
                                                            new AttributeFormat("item", AttributeFormat.STRING_TYPE, true,
                                                                                "string; data point name"),
                                                            new AttributeFormat("value", AttributeFormat.STRING_TYPE, true,
                                                                                "number or expression; data point value")}));
    myTagAttributes.put("cfchartseries", new TagDescription(false,
                                                            "Used with the cfchart tag. This tag defines the style in whichchart data displays: bar, line, pie, and so on.",
                                                            new AttributeFormat[]{
                                                              new AttributeFormat("type", AttributeFormat.STRING_TYPE, true,
                                                                                  "Sets the chart display style"),
                                                              new AttributeFormat("query", AttributeFormat.STRING_TYPE, false,
                                                                                  "Name of CFML query from which to get data."),
                                                              new AttributeFormat("itemcolumn", AttributeFormat.STRING_TYPE, false,
                                                                                  "Name of a column in the query specified in the queryattribute; contains the item label for a data point tograph."),
                                                              new AttributeFormat("valuecolumn", AttributeFormat.STRING_TYPE, false,
                                                                                  "Name of a column in the query specified in the queryattribute; contains data values to graph."),
                                                              new AttributeFormat("serieslabel", AttributeFormat.STRING_TYPE, false,
                                                                                  "Text of data series label"),
                                                              new AttributeFormat("seriescolor", AttributeFormat.STRING_TYPE, false,
                                                                                  "Color of the main element (such as the bars) of a chart.For a pie chart, the color of the first slice.Hex value or supported named color; see name list in thecfchart Usage section.For a hex value, use the form \"##xxxxxx\", where x = 0-9 orA-F; use two pound signs or none."),
                                                              new AttributeFormat("paintstyle", AttributeFormat.STRING_TYPE, false,
                                                                                  "Sets the paint display style of the data series."),
                                                              new AttributeFormat("markerstyle", AttributeFormat.STRING_TYPE, false,
                                                                                  "Applies to chartseries type attribute values line, curveand scatter, and show3D attribute value no."),
                                                              new AttributeFormat("colorlist", AttributeFormat.STRING_TYPE, false,
                                                                                  "Applies if chartseries type attribute = \"pie\". Sets pieslice colors.Comma-delimited list of hex values or supported, named webcolors; see name list in the cfchart Usage section.For a hex value, use the form \"##xxxxxx\", where x = 0-9 orA-F; use two pound signs or none."),
                                                              new AttributeFormat("datalabelstyle", AttributeFormat.STRING_TYPE, false,
                                                                                  "Specifies the way in which the color is applied to theitem in the series:- None = nothing is printed (default)- Value = the value of the datapoint- Rowlabel = the row's label- Columnlabel = the column's label- Pattern")}));
    myTagAttributes.put("cfcol",
                        new TagDescription(true, "Defines table column header, width, alignment, and text. Usedwithin a cftable tag.",
                                           new AttributeFormat[]{new AttributeFormat("header", AttributeFormat.STRING_TYPE, true,
                                                                                     "Column header text. To use this attribute, you must alsouse the cftable colHeaders attribute."),
                                             new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "Column width. If the length of data displayed exceeds thisvalue, data is truncated to fit. To avoid this, use anHTML table tag."),
                                             new AttributeFormat("align", AttributeFormat.STRING_TYPE, false, "Column alignment"),
                                             new AttributeFormat("text", AttributeFormat.STRING_TYPE, true,
                                                                 "Double-quotation mark-delimited text; determines what todisplay. Rules: same as for cfoutput sections. You canembed hyperlinks, image references, and input controls")}));
    myTagAttributes.put("cfcollection", new TagDescription(true,
                                                           "Creates, registers, and administers Verity search enginecollections.A collection that is created with the cfcollection tag isinternal. A collection created any other way is external.A collection that is registered with CFML using thecfcollection tag or registered with the K2 Server by editingthe k2server.ini file is registered. Other collections areunregistered.",
                                                           new AttributeFormat[]{
                                                             new AttributeFormat("action", AttributeFormat.STRING_TYPE, true,
                                                                                 "categorylist: retrieves categories from the collection andindicates how many documents are in each one. Returnsa structure of structures in which the categoryrepresenting each substructure is associated with anumber of documents. For a category in a category tree,the number ofdocuments is the number at or below thatlevel in the tree.create: registers the collection with CFML.- If the collection is present: creates a map to it- If the collection is not present: creates itdelete: unregisters a collection.- If the collection was registered with action = create:deletes its directories- If the collection was registered and mapped: does notdelete collection directoriesoptimize: optimizes the structure and contents of thecollection for searching; recovers space.list: returns a query result set, named from the nameattribute value, of the attributes of the collectionsthat are registered by CFML and K2 Server.map: creates a map to the collection. It is not necessaryto specify this value. Deprecated in CF7.repair: fixes data corruption in a collection. Deprecated in CF7."),
                                                             new AttributeFormat("collection", AttributeFormat.STRING_TYPE, false,
                                                                                 "A collection name. The name can include spaces"),
                                                             new AttributeFormat("path", AttributeFormat.STRING_TYPE, false,
                                                                                 "Absolute path to a Verity collection."),
                                                             new AttributeFormat("language", AttributeFormat.STRING_TYPE, false,
                                                                                 "Options are listed in Usage section. Requires theappropriate (European or Asian) Verity Locales languagepack."),
                                                             new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                                 "Name for the query results returned by the list action."),
                                                             new AttributeFormat("categories", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "Used only for creating a collection:- true: This collection includes support for categories.- false: This collection does not support categories. Default.")}));
    myTagAttributes.put("cfcomponent", new TagDescription(false,
                                                          "Creates and defines a component object; encloses functionalitythat you build in CFML and enclose within cffunction tags.This tag contains one or more cffunction tags that definemethods. Code within the body of this tag, other thancffunction tags, is executed when the component isinstantiated.",
                                                          new AttributeFormat[]{
                                                            new AttributeFormat("extends", AttributeFormat.STRING_TYPE, false,
                                                                                "Name of parent component from which to inherit methods andproperties."),
                                                            new AttributeFormat("output", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                "Specifies whether constructor code in the component cangenerate HTML output; does not affect output in the bodyof cffunction tags in the component."),
                                                            new AttributeFormat("displayname", AttributeFormat.STRING_TYPE, false,
                                                                                "A string to be displayed when using introspection to showinformation about the CFC. The information appears on theheading, following the component name.If the style attribute is set to document, the displaynameattribute is used as the name of the service element in the WSDL."),
                                                            new AttributeFormat("hint", AttributeFormat.STRING_TYPE, false,
                                                                                "Text to be displayed when using introspection to showinformation about the CFC. The hint attribute valueappears below the component name heading. This attributecan be useful for describing the purpose of the parameter.If the style attribute is set to document, the hintattribute is used as the document element of the servicein the WSDL."),
                                                            new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                                "Name for the component"),
                                                            new AttributeFormat("style", AttributeFormat.STRING_TYPE, false,
                                                                                "Specifies whether a CFC used for web services usesRPC-encoded style or document-literal style:- rpc: RPC-encoded style, default- document: Document-literal styleIf you specify document, you must also specify the namespace,serviceportname, porttypename, and bindingname attributes."),
                                                            new AttributeFormat("namespace", AttributeFormat.STRING_TYPE, false,
                                                                                "Specifies the namespace used in the WSDL whenusing the CFC as a document-literal style web service.If you don't specify this attribute, ColdFusion MXderives the namespace from the CFC class name.This attribute applies only when style=\"document\"."),
                                                            new AttributeFormat("serviceportname", AttributeFormat.STRING_TYPE, false,
                                                                                "Specifies the name of the port element in the WSDL.This attribute applies only when style=\"document\"."),
                                                            new AttributeFormat("porttypename", AttributeFormat.STRING_TYPE, false,
                                                                                "Specifies the name of the porttype element in the WSDL.This attribute applies only when style=\"document\"."),
                                                            new AttributeFormat("bindingname", AttributeFormat.STRING_TYPE, false,
                                                                                "Specifies the name of the binding element in the WSDL.This attribute applies only when style=\"document\"."),
                                                            new AttributeFormat("wsdlfile", AttributeFormat.STRING_TYPE, false,
                                                                                "A properly formatted WSDL file to be used instead ofWSDL generated by ColdFusion MX.This attribute applies only when style=\"document\".")}));
    myTagAttributes.put("cfcontent", new TagDescription(true,
                                                        "Does either or both of the following:* Sets the MIME content encoding header for the current page* Sends the contents of a file from the server as the pageoutput",
                                                        new AttributeFormat[]{
                                                          new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                              "The MIME contenttype of the page, optionally followed bya semicolon and the character encoding. By default,CFML sends pages as text/html content type inthe UTF-8 character encoding."),
                                                          new AttributeFormat("deletefile", AttributeFormat.BOOLEAN_TYPE, false,
                                                                              "Applies only if you specify a file with the file attribute.Yes: deletes the file on the server after sending itscontents to the client.No: leaves the file on the server."),
                                                          new AttributeFormat("file", AttributeFormat.STRING_TYPE, false,
                                                                              "Name of file whose contents will be the page output. Whenusing CFML in a distributed runner, the fileattribute must refer to a path on the system on which theweb server runs. When you use this attribute, any otheroutput on the current CFML page is ignored; only thecontents of the file is sent to the client."),
                                                          new AttributeFormat("variable", AttributeFormat.STRING_TYPE, false,
                                                                              "Name of a ColdFusion MX binary variable whose contents canbe displayed by the browser, such as the contents of a chartgenerated by the cfchart tag or a PDF or Excel fileretrieved by a cffile action=\"readBinary\" tag.When you use this attribute, any other output on the currentCFML page is ignored; only the contents of the file are sentto the client."),
                                                          new AttributeFormat("reset", AttributeFormat.BOOLEAN_TYPE, false,
                                                                              "The reset and file attributes are mutually exclusive.If you specify a file, this attribute has no effect.Yes: discards output that precedes call to cfcontentNo: preserves output that precedes call to cfcontent. Inthis case all output is sent with the specified type.")}));
    myTagAttributes.put("cfcookie",
                        new TagDescription(true, "Defines web browser cookie variables, including expiration andsecurity options.",
                                           new AttributeFormat[]{new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, true,
                                                                                     "Name of cookie variable. CFML converts cookie namesto all-uppercase. Cookie names set using this tag caninclude any printable ASCII characters except commas,semicolons or white space characters."),
                                             new AttributeFormat("value", AttributeFormat.STRING_TYPE, false,
                                                                 "Value to assign to cookie variable. Must be a string orvariable that can be stored as a string."),
                                             new AttributeFormat("expires", AttributeFormat.OBJECT_TYPE, false,
                                                                 "Expiration of cookie variable.* The default: the cookie expires when the user closes thebrowser, that is, the cookie is \"session only\".* A date or date/time object (for example, 10/09/97)* A number of days (for example, 10, or 100)* now: deletes cookie from client cookie.txt file(but does not delete the corresponding variable theCookie scope of the active page).* never: The cookie expires in 30 years from the time itwas created (effectively never in web years)."),
                                             new AttributeFormat("secure", AttributeFormat.BOOLEAN_TYPE, false,
                                                                 "If browser does not support Secure Sockets Layer (SSL)security, the cookie is not sent. To use the cookie, thepage must be accessed using the https protocol."),
                                             new AttributeFormat("path", AttributeFormat.STRING_TYPE, false,
                                                                 "URL, within a domain, to which the cookie applies;typically a directory. Only pages in this path can use thecookie. By default, all pages on the server that set thecookie can access the cookie.path = \"/services/login\""),
                                             new AttributeFormat("domain", AttributeFormat.STRING_TYPE, false,
                                                                 "Domain in which cookie is valid and to which cookie contentcan be sent from the user's system. By default, the cookieis only available to the server that set it. Use thisattribute to make the cookie available to other servers.Must start with a period. If the value is a subdomain, thevalid domain is all domain names that end with this string.This attribute sets the available subdomains on the siteupon which the cookie can be used.")}));
    myTagAttributes.put("cfdefaultcase", new TagDescription(false,
                                                            "Used only inside the cfswitch tag body. Contains code toexecute when the expression specified in the cfswitch tag doesnot match any of the values specified by preceeding cfcase tags.",
                                                            myEmptyAttributesArray));
    myTagAttributes.put("cfdirectory", new TagDescription(true,
                                                          "Manages interactions with directories.Different combos cause different attributes to berequired.",
                                                          new AttributeFormat[]{
                                                            new AttributeFormat("action", AttributeFormat.STRING_TYPE, false, ""),
                                                            new AttributeFormat("directory", AttributeFormat.STRING_TYPE, true,
                                                                                "Absolute pathname of directory against which to performaction."),
                                                            new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                                "Name for output record set."),
                                                            new AttributeFormat("filter", AttributeFormat.STRING_TYPE, false,
                                                                                "File extension filter applied to returned names. Forexample: *.cfm. One filter can be applied."),
                                                            new AttributeFormat("mode", AttributeFormat.STRING_TYPE, false,
                                                                                "Applies only to UNIX and Linux. Permissions. Octal valuesof Unix chmod command. Assigned to owner, group, andother, respectively."),
                                                            new AttributeFormat("sort", AttributeFormat.STRING_TYPE, false,
                                                                                "Query column(s) by which to sort directory listing.Delimited list of columns from query output."),
                                                            new AttributeFormat("newdirectory", AttributeFormat.STRING_TYPE, false,
                                                                                "New name for directory."),
                                                            new AttributeFormat("recurse", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                "Whether ColdFusion performs the action on subdirectories.")}));
    myTagAttributes.put("cfdiv", new TagDescription(false,
                                                    "Creates an HTML tag with specified contents and lets you to use bind expressions todynamically control the tag contents.",
                                                    new AttributeFormat[]{new AttributeFormat("id", AttributeFormat.STRING_TYPE, false,
                                                                                              "The HTML ID attribute value to assign to thegenerated container tag."),
                                                      new AttributeFormat("onBindError", AttributeFormat.STRING_TYPE, false,
                                                                          "The name of a JavaScript function to execute ifevaluating a bind expression results in an error. Thefunction must take two attributes: an HTTP statuscode and a message.If you omit this attribute, and have specified aglobal error handler (by using theColdFusion.setGlobalErrorHandler function), itdisplays the error message; otherwise a defaulterror pop-up displays."),
                                                      new AttributeFormat("source", AttributeFormat.STRING_TYPE, false,
                                                                          "A URL that returns the container contents.ColdFusion uses standard page path resolutionrules.Note: If a CFML page specified in this attributecontains tags that use AJAX features, such ascfform, cfgrid, and cfwindow, you must use acfajaximport tag on the page with the cfpod tag.For more information, see cfajaximport."),
                                                      new AttributeFormat("tagName", AttributeFormat.STRING_TYPE, false,
                                                                          "The HTML container tag to create.")}));
    myTagAttributes.put("cfdocument",
                        new TagDescription(false, "Creates PDF or FlashPaper output from a text block containing CFML and HTML.",
                                           new AttributeFormat[]{new AttributeFormat("format", AttributeFormat.STRING_TYPE, true,
                                                                                     "Specifies the report format."),
                                             new AttributeFormat("filename", AttributeFormat.STRING_TYPE, false,
                                                                 "Specifies the fully qualified path name of a file tocontain the PDF or FlashPaper output. If you omit thefilename attribute, ColdFusion MX streams output tothe browser."),
                                             new AttributeFormat("overwrite", AttributeFormat.BOOLEAN_TYPE, false,
                                                                 "Specifies whether ColdFusion MX overwrites anexisting file. Used in conjunction with filename.Default is: false"),
                                             new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                 "Specifies the name of an existing variable into whichthe tag stores the PDF or FlashPaper output."),
                                             new AttributeFormat("pagetype", AttributeFormat.STRING_TYPE, false,
                                                                 "Specifies the page size into which ColdFusiongenerates the report.- legal: 8.5 inches x 14 inches- letter: 8.5 inches x 11 inches- A4: 8.27 inches x 11.69 inches- A5: 5.81 inches x 8.25 inches- B5: 9.81 inches x 13.88 inches- Custom: Custom height and width.If you specify custom, you must also specify the pageheightand pagewidth attributes, can optionally specify marginattributes, and can optionally specify whether the unitsare inches or centimeters."),
                                             new AttributeFormat("pageheight", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "Specifies the page height in inches (default) orcentimeters. This attribute is only valid ifpagetype=custom. To specify page height incentimeters, include the unit=cm attribute."),
                                             new AttributeFormat("pagewidth", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "Specifies the page width in inches (default) orcentimeters. This attribute is only valid ifpagetype=custom. To specify page width incentimeters, include the unit=cm attribute."),
                                             new AttributeFormat("orientation", AttributeFormat.STRING_TYPE, false,
                                                                 "Specifies the page orientation. Specify either of thefollowing:- portrait (default)- landscape"),
                                             new AttributeFormat("margintop", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "Specifies the top margin in inches (default) orcentimeters. To specify top margin in centimeters,include the unit=cm attribute."),
                                             new AttributeFormat("marginbottom", AttributeFormat.STRING_TYPE, false,
                                                                 "Specifies the bottom margin in inches (default) orcentimeters. To specify bottom margin incentimeters, include the unit=cm attribute."),
                                             new AttributeFormat("marginleft", AttributeFormat.STRING_TYPE, false,
                                                                 "Specifies the left margin in inches (default) orcentimeters. To specify left margin in centimeters,include the unit=cm attribute."),
                                             new AttributeFormat("marginright", AttributeFormat.STRING_TYPE, false,
                                                                 "Specifies the right margin in inches (default) orcentimeters. To specify right margin in centimeters,include the unit=cm attribute."),
                                             new AttributeFormat("unit", AttributeFormat.STRING_TYPE, false,
                                                                 "Specifies the default unit (inches or centimeters) forpageheight, pagewidth, and margin attributes."),
                                             new AttributeFormat("encryption", AttributeFormat.STRING_TYPE, false,
                                                                 "Specifies whether the output is encrypted (format=\"PDF\" only).Default is: none"),
                                             new AttributeFormat("ownerpassword", AttributeFormat.STRING_TYPE, false,
                                                                 "Specifies an owner password (format=\"PDF\" only)."),
                                             new AttributeFormat("userpassword", AttributeFormat.STRING_TYPE, false,
                                                                 "Specifies a user password (format=\"PDF\" only)."),
                                             new AttributeFormat("permissions", AttributeFormat.STRING_TYPE, false,
                                                                 "Specifies one or more permissions (format=\"PDF\" only).Separate multiple permissions with a comma."),
                                             new AttributeFormat("fontembed", AttributeFormat.BOOLEAN_TYPE, false,
                                                                 "Specifies whether ColdFusion embeds fonts in the output.Specify one of the following:- true: Embed fonts- false: Do not embed fonts.Selective: Embed all fonts except Java fonts and core fonts."),
                                             new AttributeFormat("backgroundvisible", AttributeFormat.BOOLEAN_TYPE, false,
                                                                 "Specifies whether the background prints when theuser prints the document:- yes: include the background when printing.- no: do not include the background when printing."),
                                             new AttributeFormat("scale", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "Specifies a scale factor as a percentage. Use thisoption to reduce the size of the HTML output so thatit fits on that paper. Specify a number less than 100.")}));
    myTagAttributes.put("cfdocumentitem",
                        new TagDescription(false, "Specifies action items for a PDF or FlashPaper documentcreated by the cfdocument tag.",
                                           new AttributeFormat[]{new AttributeFormat("type", AttributeFormat.STRING_TYPE, true,
                                                                                     "Specifies the action:- pagebreak: start a new page at the location of the tag.- header: use the text between the <cfdocumentitem>and </cfdocumentitem> tags as the running header.- footer: use the text between the <cfdocumentitem>and </cfdocumentitem> tags as the running footer.")}));
    myTagAttributes.put("cfdocumentsection", new TagDescription(false,
                                                                "Divides a PDF or FlashPaper document into sections.By using this tag in conjunction with a cfdocumentitemtag, each section can have unique headers, footers,and page numbers.",
                                                                new AttributeFormat[]{
                                                                  new AttributeFormat("margintop", AttributeFormat.NUMERIC_TYPE, false,
                                                                                      "Specifies the top margin in inches (default) orcentimeters. To specify the top margin in centimeters,include the unit=\"cm\" attribute in the parent cfdocumenttag."),
                                                                  new AttributeFormat("marginbottom", AttributeFormat.NUMERIC_TYPE, false,
                                                                                      "Specifies the bottom margin in inches (default) orcentimeters. To specify the bottom margin incentimeters, include the unit=\"cm\" attribute in theparent cfdocument tag."),
                                                                  new AttributeFormat("marginleft", AttributeFormat.NUMERIC_TYPE, false,
                                                                                      "Specifies the left margin in inches (default) orcentimeters. To specify the left margin in centimeters,include the unit=\"cm\" attribute in the parent cfdocumenttag."),
                                                                  new AttributeFormat("marginright", AttributeFormat.NUMERIC_TYPE, false,
                                                                                      "Specifies the right margin in inches (default) orcentimeters. To specify the right margin in centimeters,include the unit=\"cm\" attribute in the parent cfdocumenttag.")}));
    myTagAttributes.put("cfdump", new TagDescription(true,
                                                     "Outputs the elements, variables and values of most kinds ofCFML objects. Useful for debugging. You can display thecontents of simple and complex variables, objects, components,user-defined functions, and other elements.",
                                                     new AttributeFormat[]{new AttributeFormat("var", AttributeFormat.OBJECT_TYPE, true,
                                                                                               "Variable to display. Enclose a variable name in poundsigns."),
                                                       new AttributeFormat("expand", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: In Internet Explorer and Mozilla, expands views"),
                                                       new AttributeFormat("label", AttributeFormat.STRING_TYPE, false,
                                                                           "A string; header for the dump output.")}));
    myTagAttributes.put("cfelse", new TagDescription(true,
                                                     "Used as the last control block in a cfif tag block to handleany case not identified by the cfif tag or a cfelseif tag.",
                                                     myEmptyAttributesArray));
    myTagAttributes.put("cfelseif", new TagDescription(true,
                                                       "Used as a control block in a cfif tag block to handle any casenot identified by the cfif tag or a cfelseif tag.",
                                                       myEmptyAttributesArray));
    myTagAttributes.put("cferror", new TagDescription(true,
                                                      "Displays a custom HTML page when an error occurs. This letsyou maintain a consistent look and feel among an application'sfunctional and error pages",
                                                      new AttributeFormat[]{new AttributeFormat("type", AttributeFormat.STRING_TYPE, true,
                                                                                                "Type of error that the custom error page handles. The typealso determines how CFML handles the error page. Formore information, see Specifying a custom error page inDeveloping CFML MX Applications.exception: a exception of the type specified by theexception attribute.validation: errors recognized by sever-side typevalidation.request: any encountered error.monitor: deprecated."),
                                                        new AttributeFormat("template", AttributeFormat.STRING_TYPE, true,
                                                                            "Relative path to the custom error page.(A CFML page was formerly called a template.)"),
                                                        new AttributeFormat("mailto", AttributeFormat.STRING_TYPE, false,
                                                                            "An E-mail address. This attribute is available on theerror page as the variable error.mailto. CFML doesnot automatically send anything to this address."),
                                                        new AttributeFormat("exception", AttributeFormat.STRING_TYPE, false,
                                                                            "Type of exception that the tag handles:application: application exceptionsdatabase: database exceptionstemplate: CFML page exceptionssecurity: security exceptionsobject: object exceptionsmissingInclude: missing include file exceptionsexpression: expression exceptionslock: lock exceptionscustom_type: developer-defined exceptions, defined in thecfthrow tagany: all exception types")}));
    myTagAttributes.put("cfexecute", new TagDescription(false, "Executes a CFML developer-specified process on a servercomputer.",
                                                        new AttributeFormat[]{new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                                  "Absolute path of the application to execute.On Windows, you must specify an extension; for example,C:\\myapp.exe."),
                                                          new AttributeFormat("arguments", AttributeFormat.OBJECT_TYPE, false,
                                                                              "Command-line variables passed to application. If specifiedas string, it is processed as follows:* Windows: passed to process control subsystem for parsing.* UNIX: tokenized into an array of arguments. The defaulttoken separator is a space; you can delimit argumentsthat have embedded spaces with double quotation marks.If passed as array, it is processed as follows:* Windows: elements are concatenated into a string oftokens, separated by spaces. Passed to process controlsubsystem for parsing.* UNIX: elements are copied into an array of exec()arguments"),
                                                          new AttributeFormat("outputfile", AttributeFormat.STRING_TYPE, false,
                                                                              "File to which to direct program output. If no outputfile orvariable attribute is specified, output is displayed onthe page from which it was called.If not an absolute path (starting a with a drive letter anda colon, or a forward or backward slash), it is relativeto the CFML temporary directory, which is returnedby the GetTempDirectory function."),
                                                          new AttributeFormat("variable", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                              "Variable in which to put program output. If no outputfileor variable attribute is specified, output is displayed onpage from which it was called."),
                                                          new AttributeFormat("timeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                              "Length of time, in seconds, that CFML waits foroutput from the spawned program.")}));
    myTagAttributes.put("cfexit", new TagDescription(true,
                                                     "This tag aborts processing of the currently executing CFMLcustom tag, exits the page within the currently executing CFMLcustom tag, or re-executes a section of code within thecurrently executing CFML custom tag.",
                                                     new AttributeFormat[]{new AttributeFormat("method", AttributeFormat.STRING_TYPE, false,
                                                                                               "exittag: aborts processing of currently executing tagexittemplate: exits page of currently executing tagloop: reexecutes body of currently executing tag")}));
    myTagAttributes.put("cffile", new TagDescription(true,
                                                     "Manages interactions with server files.Different combonations cause different attributes to berequired.",
                                                     new AttributeFormat[]{new AttributeFormat("action", AttributeFormat.STRING_TYPE, true,
                                                                                               "Type of file manipulation that the tag performs."),
                                                       new AttributeFormat("file", AttributeFormat.STRING_TYPE, false,
                                                                           "Pathname of the file."),
                                                       new AttributeFormat("mode", AttributeFormat.STRING_TYPE, false,
                                                                           "Applies only to UNIX and Linux. Permissions. Octal valuesof Unix chmod command. Assigned to owner, group, andother, respectively."),
                                                       new AttributeFormat("output", AttributeFormat.STRING_TYPE, false,
                                                                           "String to add to the file"),
                                                       new AttributeFormat("addnewline", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: appends newline character to text written to file"),
                                                       new AttributeFormat("attributes", AttributeFormat.STRING_TYPE, false,
                                                                           "Applies to Windows. A comma-delimited list of attributesto set on the file.If omitted, the file's attributes are maintained."),
                                                       new AttributeFormat("charset", AttributeFormat.STRING_TYPE, false,
                                                                           "The character encoding in which the file contents isencoded.For more information on character encodings, see:www.w3.org/International/O-charset.html."),
                                                       new AttributeFormat("source", AttributeFormat.STRING_TYPE, false,
                                                                           "Pathname of the file (durring copy)."),
                                                       new AttributeFormat("destination", AttributeFormat.STRING_TYPE, false,
                                                                           "Pathname of a directory or file on web server(durring copy)."),
                                                       new AttributeFormat("variable", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                           "Name of variable to contain contents of text file."),
                                                       new AttributeFormat("filefield", AttributeFormat.STRING_TYPE, false,
                                                                           "Name of form field used to select the file.Do not use pound signs (#) to specify the field name."),
                                                       new AttributeFormat("nameconflict", AttributeFormat.STRING_TYPE, false,
                                                                           "Action to take if filename is the same as that of a filein the directory."),
                                                       new AttributeFormat("accept", AttributeFormat.STRING_TYPE, false,
                                                                           "Limits the MIME types to accept. Comma-delimited list. Forexample, to permit JPG and Microsoft Word file uploads:accept = \"image/jpg, application/msword\""),
                                                       new AttributeFormat("result", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                           "Allows you to specify a name for the variable in which cffilereturns the result (or status) parameters. If you do not specifya value for this attribute, cffile uses the prefix \"cffile\"."),
                                                       new AttributeFormat("fixnewline", AttributeFormat.STRING_TYPE, false,
                                                                           "* Yes: changes embedded line-ending characters in stringvariables to operating-system specific line endings* No: (default) do not change embedded line-endingcharacters in string variables.")}));
    myTagAttributes.put("cfflush", new TagDescription(true, "Flushes currently available data to the client.", new AttributeFormat[]{
      new AttributeFormat("interval", AttributeFormat.NUMERIC_TYPE, false,
                          "Flushes output each time this number of bytes becomesavailable. HTML headers, and data that is alreadyavailable when the tag is executed, are omitted from thecount.")}));
    myTagAttributes.put("cfform", new TagDescription(false,
                                                     "Builds a form with CFML custom control tags; these providemore functionality than standard HTML form input elements.",
                                                     new AttributeFormat[]{new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                                               "In HTML format, if you omit this attribute and specifyan id attribute, ColdFusion does not include a nameattribute in the HTML sent to the browser; thisbehavior lets you use the cfform tag to createXHTML-compliant forms. If you omit the nameattribute and the id attribute, ColdFusion generatesa name of the form CFForm_n where n is a numberthat assigned serially to the forms on a page."),
                                                       new AttributeFormat("action", AttributeFormat.STRING_TYPE, false,
                                                                           "Name of CFML page to execute when the form issubmitted for processing."),
                                                       new AttributeFormat("method", AttributeFormat.STRING_TYPE, false,
                                                                           "The method the browser uses to send the form datato the server:- post: Send the data using the HTTP post method,This method sends the data in a separate messageto the server.- get: Send the data using the HTTP get method,which puts the form field contents in the URLquery string."),
                                                       new AttributeFormat("format", AttributeFormat.STRING_TYPE, false,
                                                                           "- HTML: Generate an HTML form and send it to theclient. cfgrid and cftree child controlscan be inFlash or applet format.- Flash: Generate a Flash form and send it to theclient. All controls are in Flash format.- XML: Generate XForms-compliant XML and savethe results in a variable specified by the nameattribute. By default, ColdFusion also applies anXSL skin and displays the result. For moreinformation, see the skin attribute"),
                                                       new AttributeFormat("skin", AttributeFormat.STRING_TYPE, false,
                                                                           "Flash: Use a Macromedia halo color to stylize the output.XML: Specfies whether to apply an XSL skin anddisplay the resulting HTML to the client. Can be anyof the following:- ColdFusion MX skin name: Apply the specified skin.- XSL file name: Apply the skin located in the specified path.- \"none\": Do not apply an XSL skin. You must use XForms XML then.- (omitted) or \"default\": Use the ColdFusion MX default skin."),
                                                       new AttributeFormat("preservedata", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "When the cfform action attribute posts back to the samepage as the form, this determines whether to override thecontrol values with the submitted values.- false: values specified in the control tag attributes are used- true: corresponding submitted values are used"),
                                                       new AttributeFormat("onload", AttributeFormat.STRING_TYPE, false,
                                                                           "JavaScript to execute when the form loads."),
                                                       new AttributeFormat("onsubmit", AttributeFormat.STRING_TYPE, false,
                                                                           "JavaScript or Actionscript function to execute topreprocess data before form is submitted. If anychild tags specify onSubmit field validation, ColdFusiondoes the validation before executing this JavaScript."),
                                                       new AttributeFormat("codebase", AttributeFormat.STRING_TYPE, false,
                                                                           "URL of downloadable JRE plug-in (for Internet Explorer only).Default: /CFIDE/classes/cf-j2re-win.cab"),
                                                       new AttributeFormat("archive", AttributeFormat.STRING_TYPE, false,
                                                                           "URL of downloadable Java classes for CFML controls.Default: /CFIDE/classes/cfapplets.jar"),
                                                       new AttributeFormat("height", AttributeFormat.STRING_TYPE, false,
                                                                           "The height of the form. Use a number to specifypixels, In Flash, you can use a percentage value tospecify a percentage of the available width. Thedisplayed height might be less than the specified size."),
                                                       new AttributeFormat("width", AttributeFormat.STRING_TYPE, false,
                                                                           "The width of the form. Use a number to specifypixels, In Flash, you can use a percentage value tospecify a percentage of the available width."),
                                                       new AttributeFormat("onerror", AttributeFormat.STRING_TYPE, false,
                                                                           "Applies only for onSubmit or onBlur validation; hasno effect for onServer validation. An ActionScriptexpression or expressions to execute if the usersubmits a form with one or more validation errors."),
                                                       new AttributeFormat("wmode", AttributeFormat.STRING_TYPE, false,
                                                                           "Specifies how the Flash form appears relative toother displayable content that occupies the samespace on an HTML page.- window: The Flash form is the topmost layer on thepage and obscures anything that would share thespace, such as drop-down dynamic HTML lists.- transparent: The Flash form honors the z-index ofdhtml so you can float items above it. If the Flashform is above any item, transparent regions in theform show the content that is below it.- opaque: The Flash form honors the z-index ofdhtml so you can float items above it. If the Flashform is above any item, it blocks any content that isbelow it.Default is: window."),
                                                       new AttributeFormat("accessible", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Specifies whether to include support screen readersin the Flash form. Screen reader support addsapproximately 80KB to the SWF file sent to theclient. Default is: false."),
                                                       new AttributeFormat("preloader", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Specifies whether to display a progress bar whenloading the Flash form. Default is: true."),
                                                       new AttributeFormat("timeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Integer number of seconds for which to keep theform data in the Flash cache on the server. A value of0 prevents the data from being cached."),
                                                       new AttributeFormat("scriptsrc", AttributeFormat.STRING_TYPE, false,
                                                                           "Specifies the URL, relative to the web root, of thedirectory that contains the cfform.js file with theclient-side JavaScript used by this tag and its childtags. For XML format forms, this directory is also thedefault directory for XSLT skins."),
                                                       new AttributeFormat("style", AttributeFormat.STRING_TYPE, false,
                                                                           "Styles to apply to the form. In HTML or XML format,ColdFusion passes the style attribute to the browseror XML. In Flash format, must be a style specificationin CSS format."),
                                                       new AttributeFormat("onreset", AttributeFormat.STRING_TYPE, false,
                                                                           "JavaScript to execute when the user clicks a reset button."),
                                                       new AttributeFormat("enctype", AttributeFormat.STRING_TYPE, false,
                                                                           "MIME type passed through to <FORM>."),
                                                       new AttributeFormat("id", AttributeFormat.STRING_TYPE, false,
                                                                           "HTML id passed through to <FORM>."),
                                                       new AttributeFormat("target", AttributeFormat.STRING_TYPE, false,
                                                                           "Target window or frame passed through to <FORM>."),
                                                       new AttributeFormat("class", AttributeFormat.STRING_TYPE, false,
                                                                           "Form CSS class passed through to <FORM>."),
                                                       new AttributeFormat("passthrough", AttributeFormat.STRING_TYPE, false,
                                                                           "This attribute is deprecated.Passes arbitrary attribute-value pairs to the HTML codethat is generated for the tag. You can use either of thefollowing formats:passthrough=\"title=\"\"myTitle\"\"\"passthrough='title=\"mytitle\"'"),
                                                       new AttributeFormat("enablecab", AttributeFormat.STRING_TYPE, false,
                                                                           "This attribute is deprecated.")}));
    myTagAttributes.put("cfformgroup", new TagDescription(false,
                                                          "Creates a container control for multiple form controls.Used in the cfform tag body of Macromedia Flash and XMLforms. Ignored in HTML forms.",
                                                          new AttributeFormat[]{
                                                            new AttributeFormat("type", AttributeFormat.STRING_TYPE, true,
                                                                                "For XML forms can be any XForms group type defined in the XSLT.For Flash see the value options and docs for more information."),
                                                            new AttributeFormat("query", AttributeFormat.STRING_TYPE, false,
                                                                                "The query to use with the repeater. Flash creates aninstance of each of the cfformgroup tag's child tags foreach row in the query. You can use the bind attribute inthe child tags to use data from the query row for theinstance."),
                                                            new AttributeFormat("startrow", AttributeFormat.NUMERIC_TYPE, false,
                                                                                "Used only for the repeater type; ignored otherwise.Specifies the row number of the first row of the query touse in the Flash form repeater. This attribute is zerobased:the first row is row 0, not row 1 (as in most ColdFusion tags).Default: 0"),
                                                            new AttributeFormat("maxrows", AttributeFormat.NUMERIC_TYPE, false,
                                                                                "Used only for for the repeater type; ignored otherwise.Specifies the maximum number of query rows to use inthe Flash form repeater. If the query has more rows thanthe sum of the startrow attribute and this value, therepeater does not use the remaining rows."),
                                                            new AttributeFormat("label", AttributeFormat.STRING_TYPE, false,
                                                                                "Label to apply to the form group. In Flash, does the following:- For a page or panel form group, determines the label toput on the corresponding accordion pleat, the tabnavigator tab,or the panel title bar. For a Flash horizontal or vertical formgroup, specifies the label to put to the left of the group.- Ignored in Flash for repeater, hbox, hdividedbox, vbox,vdividedbox, tile, accordion, and tabnavigator types."),
                                                            new AttributeFormat("id", AttributeFormat.STRING_TYPE, false,
                                                                                "ID for form input element."),
                                                            new AttributeFormat("style", AttributeFormat.STRING_TYPE, false,
                                                                                "Flash: A Flash style specification in CSS format.XML: An inline CSS style specification."),
                                                            new AttributeFormat("selectedindex", AttributeFormat.NUMERIC_TYPE, false,
                                                                                "Used only for accordion and tabnavigator types; ignoredotherwise. Specifies the page control to display as open,where 0 (not 1) specifies the first page control defined inthe group."),
                                                            new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                                "Width of the group container, in pixels. If you omit thisattribute, Flash automatically sizes the container width.Ignored for Flash repeater type."),
                                                            new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                                "Height of the group container, in pixels. If you omit thisattribute, Flash automatically sizes the container height.Ignored for Flash repeater type."),
                                                            new AttributeFormat("enabled", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                "Flash only: Boolean value specifying whether the controls in theform group are enabled. Disabled controls appear inlight gray.Default: true"),
                                                            new AttributeFormat("visible", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                "Flash only: Boolean value specifying whether the controls in theform group are visible. If the controls are invisible, thespace that would be occupied by visible controls is blank.Default: true"),
                                                            new AttributeFormat("onchange", AttributeFormat.STRING_TYPE, false,
                                                                                "Flash only: tabnavigator and accordion types only: ActionScriptexpression or expressions to execute when a new tab oraccordion page is selected. Note: The onChange eventoccurs when the form first displays."),
                                                            new AttributeFormat("tooltip", AttributeFormat.STRING_TYPE, false,
                                                                                "Flash only: Text to display when the mouse pointer hovers in theform group area. If a control in the form group alsospecifies a tooltip, Flash displays the control's tolltipwhen the mouse pointer hovers over the control.")}));
    myTagAttributes.put("cfformitem", new TagDescription(false,
                                                         "Inserts a horizontal line, a vertical line, a spacer,or text in a Flash form. Used in the cfform or cfformgrouptag body for Flash and XML forms. Ignored in HTML forms.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("type", AttributeFormat.STRING_TYPE, true,
                                                                               "Form item type. See docs for more details."),
                                                           new AttributeFormat("style", AttributeFormat.STRING_TYPE, true,
                                                                               "Flash: Must be a style specification in CSS format.Ignored if the type attribute is html or text.XML: ColdFusion passes the style attribute to the XML.ColdFusion skins include the style attribute to thegenerated HTML."),
                                                           new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "Width of the item, in pixels. If you omit this attribute, Flashautomatically sizes the width. In ColdFusion XSL skins,use the style attribute, instead."),
                                                           new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "Height of the item, in pixels. If you omit this attribute,Flash automatically sizes the width. In ColdFusion XSLskins, use the style attribute, instead."),
                                                           new AttributeFormat("enabled", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Boolean value specifying whether the control is enabled.Disabled text appear in light gray. Has no effect onspacers and rules.Default: true"),
                                                           new AttributeFormat("visible", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Boolean value specifying whether to show the control.Space that would be occupied by an invisible control isblank. Has no effect on spacers.Default: true"),
                                                           new AttributeFormat("tooltip", AttributeFormat.STRING_TYPE, false,
                                                                               "Text to display when the mouse pointer hovers over thecontrol. Has no effect on spacers."),
                                                           new AttributeFormat("bind", AttributeFormat.STRING_TYPE, false,
                                                                               "A Flash bind expression that populates the field withinformation from other form fields. If you use thisattribute, ColdFusion MX ignores any text that youspecify in the body of the cftextitem tag. This attributecan be useful if the cfformitem tag is in a cfformgrouptype=\"repeater\" tag.")}));
    myTagAttributes.put("cfftp", new TagDescription(true, "Lets users implement File Transfer Protocol (FTP) operations.",
                                                    new AttributeFormat[]{new AttributeFormat("action", AttributeFormat.STRING_TYPE, true,
                                                                                              "FTP operation to perform.open: create an FTP connectionclose: terminate an FTP connection"),
                                                      new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                          "Overrides username specified in ODBC setup."),
                                                      new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                          "Overrides password specified in ODBC setup."),
                                                      new AttributeFormat("server", AttributeFormat.STRING_TYPE, false,
                                                                          "FTP server to which to connect; for example,ftp.myserver.com"),
                                                      new AttributeFormat("timeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                          "Value in seconds for the timeout of all operations,including individual data request operations."),
                                                      new AttributeFormat("port", AttributeFormat.NUMERIC_TYPE, false,
                                                                          "Remote port to which to connect"),
                                                      new AttributeFormat("connection", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                          "Name of the FTP connection. If you specify the username,password, and server attributes, and if no connectionexists for them, CFML creates one. Calls to cfftpwith the same connection name reuse the connection."),
                                                      new AttributeFormat("proxyserver", AttributeFormat.STRING_TYPE, false,
                                                                          "The proxy server required to access the URL."),
                                                      new AttributeFormat("retrycount", AttributeFormat.NUMERIC_TYPE, false,
                                                                          "Number of retries until failure is reported."),
                                                      new AttributeFormat("stoponerror", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "Yes: halts processing, displays an appropriate error.No: populates the error	variables"),
                                                      new AttributeFormat("passive", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "Yes: enable passive mode"),
                                                      new AttributeFormat("asciiextensionist", AttributeFormat.STRING_TYPE, false,
                                                                          "Delimited list of file extensions that force ASCIItransfer mode, if transferMode = \"auto\"."),
                                                      new AttributeFormat("transfermode", AttributeFormat.STRING_TYPE, false,
                                                                          "ASCII FTP transfer modeBinary FTP transfer modeAuto FTP transfer mode"),
                                                      new AttributeFormat("failifexists", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "Yes: if a local file with same name exists, getFile fails"),
                                                      new AttributeFormat("directory", AttributeFormat.STRING_TYPE, false,
                                                                          "Directory on which to perform an operation"),
                                                      new AttributeFormat("localfile", AttributeFormat.STRING_TYPE, false,
                                                                          "Name of the file on the local file system"),
                                                      new AttributeFormat("remotefile", AttributeFormat.STRING_TYPE, false,
                                                                          "Name of the file on the FTP server file system."),
                                                      new AttributeFormat("item", AttributeFormat.STRING_TYPE, false,
                                                                          "Object of these actions: file or directory."),
                                                      new AttributeFormat("existing", AttributeFormat.STRING_TYPE, false,
                                                                          "Current name of the file or directory on the remote server."),
                                                      new AttributeFormat("new", AttributeFormat.STRING_TYPE, false,
                                                                          "New name of file or directory on the remote server"),
                                                      new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                          "Query name of directory listing."),
                                                      new AttributeFormat("result", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                          "Specifies a name for the structure in which cfftpstores the returnValue variable. If set, this valuereplaces cfftp as the prefix to use when accessingreturnVariable.")}));
    myTagAttributes.put("cffunction", new TagDescription(false,
                                                         "Defines a function that you can call in CFML. Required todefined CFML component methods.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                               "A string; a component method that is used within thecfcomponent tag."),
                                                           new AttributeFormat("returntype", AttributeFormat.STRING_TYPE, false,
                                                                               "String; a type name; data type of the function return valueRequired for a web service; Optional, otherwise."),
                                                           new AttributeFormat("roles", AttributeFormat.STRING_TYPE, false,
                                                                               "A comma-delimited list of CFML security roles thatcan invoke the method. Only users who are logged in withthe specified roles can execute the function. If thisattribute is omitted, all users can invoke the method"),
                                                           new AttributeFormat("access", AttributeFormat.STRING_TYPE, false,
                                                                               "The client security context from which the method can beinvoked:private: available only to the component that declares themethodand any components that extend the component inwhich it is definedpackage: available only to the component that declares themethod, components that extend the component, or anyother components in the packagepublic: available to a locally executing page or componentmethodremote: available to a locally or remotely executing pageor component method, or a remote client through a URL,Flash, or a web service. To publish the function as aweb service, this option is required."),
                                                           new AttributeFormat("output", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Specifies under which conditions the function can generateHTML output."),
                                                           new AttributeFormat("displayname", AttributeFormat.STRING_TYPE, false,
                                                                               "Meaningful only for CFC method parameters. A value to bedisplayed in parentheses following the function name whenusing introspection to show information about the CFC"),
                                                           new AttributeFormat("hint", AttributeFormat.STRING_TYPE, false,
                                                                               "Meaningful only for CFC method parameters. Text to bedisplayed when using introspection to show informationabout the CFC. The hint attribute value follows the syntaxline in the function description"),
                                                           new AttributeFormat("returnformat", AttributeFormat.STRING_TYPE, false,
                                                                               "The format in which to return values to a remote caller. This attribute has no effect on values returned to a local caller."),
                                                           new AttributeFormat("description", AttributeFormat.STRING_TYPE, false,
                                                                               "Supplies a short text description of the function.")}));
    myTagAttributes.put("cfgrid", new TagDescription(false,
                                                     "Used within the cfform tag. Puts a grid control (a table ofdata) in a CFML form. To specify grid columns and rowdata, use the cfgridcolumn and cfgridrow tags, or use thequery attribute, with or without cfgridcolumn tags.",
                                                     new AttributeFormat[]{new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                               "Name of grid element."),
                                                       new AttributeFormat("bind", AttributeFormat.STRING_TYPE, false,
                                                                           "A bind expression specifying used to fill thecontents of the grid. Cannot be used with thequery attribute."),
                                                       new AttributeFormat("pagesize", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "The number of rows to display per page for adynamic grid. If the number of available rowsexceeds the page size, the grid displays onlythe specified number of entries on a singlepage, and the user navigates between pagesto show all data. The grid retrieves data foreach page only when it is required for display.This attribute is ignored if you specify a queryattribute."),
                                                       new AttributeFormat("striperowcolor", AttributeFormat.STRING_TYPE, false,
                                                                           "The color to use for one of the alternatingstripes. The bgColor setting determines theother color"),
                                                       new AttributeFormat("preservepageonsort", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Specifies whether to display the page withthe current page number, or display page 1,after sorting (or resorting) the grid"),
                                                       new AttributeFormat("striperows", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Specifies whether to display the page withthe current page number, or display page 1,after sorting (or resorting) the grid"),
                                                       new AttributeFormat("format", AttributeFormat.STRING_TYPE, false,
                                                                           "- applet: generates a Java applet.- Flash: generates a Flash grid control.- xml: generates an XMLrepresentation of the grid.In XML format forms, includes the generated XML in the form.In HTML format forms, puts the XML in a string variablewith the name specified by the name attribute.Default: applet"),
                                                       new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Control's height, in pixels.Default for applet: 300"),
                                                       new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Control's width, in pixels.Default for applet: 300"),
                                                       new AttributeFormat("autowidth", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: sets column widths so that all columns display withingrid width.No: sets columns to equal widths. User can resize columns.Horizontal scroll bars are not available, because ifyou specify a column width and set autoWidth = \"Yes\",CFML sets to this width, if possible"),
                                                       new AttributeFormat("vspace", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Vertical margin above and below control, in pixels."),
                                                       new AttributeFormat("hspace", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Horizontal spacing to left and right of control, in pixels."),
                                                       new AttributeFormat("align", AttributeFormat.STRING_TYPE, false,
                                                                           "Alignment of the grid cell contents"),
                                                       new AttributeFormat("query", AttributeFormat.STRING_TYPE, false,
                                                                           "Name of query associated with grid control."),
                                                       new AttributeFormat("insert", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "User can insert row data in grid.Takes effect only if selectmode=\"edit\""),
                                                       new AttributeFormat("delete", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "User can delete row data from grid.Takes effect only if selectmode=\"edit\""),
                                                       new AttributeFormat("sort", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "The sort button performs simple text sort on column. Usercan sort columns by clicking column head or by clickingsort buttons. Not valid with selectmode=browse.Yes: sort buttons display on grid control"),
                                                       new AttributeFormat("font", AttributeFormat.STRING_TYPE, false,
                                                                           "Font of data in column."),
                                                       new AttributeFormat("fontsize", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Size of text in column."),
                                                       new AttributeFormat("italic", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: displays grid control text in italics"),
                                                       new AttributeFormat("bold", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: displays grid control text in bold"),
                                                       new AttributeFormat("textcolor", AttributeFormat.STRING_TYPE, false,
                                                                           "Text color for control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
                                                       new AttributeFormat("href", AttributeFormat.STRING_TYPE, false,
                                                                           "URL or query column name that contains a URL to hyperlinkeach grid column with."),
                                                       new AttributeFormat("hrefkey", AttributeFormat.STRING_TYPE, false,
                                                                           "The query column to use for the value appended to the hrefURL of each column, instead of the column's value."),
                                                       new AttributeFormat("target", AttributeFormat.STRING_TYPE, false,
                                                                           "Frame in which to open link specified in href."),
                                                       new AttributeFormat("appendkey", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "When used with href, passes CFTREEITEMKEY variablewith the value of the selected tree item in URL to theapplication page specified in the cfform actionattribute"),
                                                       new AttributeFormat("highlighthref", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: Highlights links that are associated with a cftreeitemwith a URL attribute value.No: Disables highlight."),
                                                       new AttributeFormat("onvalidate", AttributeFormat.STRING_TYPE, false,
                                                                           "JavaScript function to validate user input. The form object,input object, and input object value are passed to thespecified routine, which should return True if validationsucceeds; False, otherwise."),
                                                       new AttributeFormat("onerror", AttributeFormat.STRING_TYPE, false,
                                                                           "JavaScript function to execute if validation fails."),
                                                       new AttributeFormat("griddataalign", AttributeFormat.STRING_TYPE, false,
                                                                           "Left: left-aligns data within column.Right: right-aligns data within column.Center: center-aligns data within column"),
                                                       new AttributeFormat("gridlines", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: enables row and column rules in grid control"),
                                                       new AttributeFormat("rowheight", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Minimum row height, in pixels, of grid control. Used withcfgridcolumn type = \"Image\"; defines space for graphics todisplay in row."),
                                                       new AttributeFormat("rowheaders", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: displays a column of numeric row labels in gridcontrol"),
                                                       new AttributeFormat("rowheaderalign", AttributeFormat.STRING_TYPE, false,
                                                                           "Left: left-aligns data within row headerRight: right-aligns data within row headerCenter: center-aligns data within row header"),
                                                       new AttributeFormat("rowheaderfont", AttributeFormat.STRING_TYPE, false,
                                                                           "Font of data in column."),
                                                       new AttributeFormat("rowheaderfontsize", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Size of text in column."),
                                                       new AttributeFormat("rowheaderitalic", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: displays grid control text in italics"),
                                                       new AttributeFormat("rowheaderbold", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: displays grid control text in bold"),
                                                       new AttributeFormat("rowheadertextcolor", AttributeFormat.STRING_TYPE, false,
                                                                           "Text color for control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
                                                       new AttributeFormat("colheaders", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: displays a column of numeric row labels in gridcontrol"),
                                                       new AttributeFormat("colheaderalign", AttributeFormat.STRING_TYPE, false,
                                                                           "Left: left-aligns data within row headerRight: right-aligns data within row headerCenter: center-aligns data within row header"),
                                                       new AttributeFormat("colheaderfont", AttributeFormat.STRING_TYPE, false,
                                                                           "Font of data in column."),
                                                       new AttributeFormat("colheaderfontsize", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Size of text in column."),
                                                       new AttributeFormat("colheaderitalic", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: displays grid control text in italics"),
                                                       new AttributeFormat("colheaderbold", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: displays grid control text in bold"),
                                                       new AttributeFormat("colheadertextcolor", AttributeFormat.STRING_TYPE, false,
                                                                           "Text color for control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
                                                       new AttributeFormat("bgcolor", AttributeFormat.STRING_TYPE, false,
                                                                           "Background color of grid control."),
                                                       new AttributeFormat("selectcolor", AttributeFormat.STRING_TYPE, false,
                                                                           "Background color for a selected item."),
                                                       new AttributeFormat("selectmode", AttributeFormat.STRING_TYPE, false,
                                                                           "Selection mode for items in the control.- Edit: user can edit grid data. Selecting a cell opensthe editor for the cell type.- Row: user selections automatically extend to the rowthat contains selected cell.- Single: user selections are limited to selected cell.(Applet only)- Column: user selections automatically extendto column that contains selected cell.  (Applet only)- Browse: user can only browse grid data.  (Applet only)"),
                                                       new AttributeFormat("maxrows", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Maximum number of rows to display in grid."),
                                                       new AttributeFormat("notsupported", AttributeFormat.STRING_TYPE, false,
                                                                           "Text to display if a page that contains a Java applet-basedcfform control is opened by a browser that does notsupport Java or has Java support disabled.Default:\"<b>Browser must support Java to <br>view ColdFusion JavaApplets!</b>\""),
                                                       new AttributeFormat("picturebar", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes: images for Insert, Delete, Sort buttons"),
                                                       new AttributeFormat("insertbutton", AttributeFormat.STRING_TYPE, false,
                                                                           "Text for the insert button.Takes effect only ifselectmode=\"edit\"."),
                                                       new AttributeFormat("deletebutton", AttributeFormat.STRING_TYPE, false,
                                                                           "Text of Delete button text. Takes effect only ifselectmode=\"edit\"."),
                                                       new AttributeFormat("sortascendingbutton", AttributeFormat.STRING_TYPE, false,
                                                                           "Sort button text"),
                                                       new AttributeFormat("sortdescendingbutton", AttributeFormat.STRING_TYPE, false,
                                                                           "Sort button text"),
                                                       new AttributeFormat("style", AttributeFormat.STRING_TYPE, false,
                                                                           "Flash only: Must be a style specification in CSS format.Ignored for type=\"text\"."),
                                                       new AttributeFormat("enabled", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Flash only: Boolean value specifyingwhether the control is enabled. A disabledcontrol appears in light gray.Default: true"),
                                                       new AttributeFormat("visible", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Flash only: Boolean value specifyingwhether to show the control. Space that wouldbe occupied by an invisible control is blank.Default: true"),
                                                       new AttributeFormat("tooltip", AttributeFormat.STRING_TYPE, false,
                                                                           "Flash only: text to display when themouse pointer hovers over the control."),
                                                       new AttributeFormat("onchange", AttributeFormat.STRING_TYPE, false,
                                                                           "Flash only: ActionScript to run when the control changesdue to user action in the control.")}));
    myTagAttributes.put("cfgridcolumn", new TagDescription(true,
                                                           "Used with the cfgrid tag in a cfform. Use this tag to specifycolumn data in a cfgrid control. The font and alignmentattributes used in cfgridcolumn override global font oralignment settings defined in cfgrid.",
                                                           new AttributeFormat[]{
                                                             new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                 "Name of grid column element. If grid uses a query, columnname must specify name of a query column."),
                                                             new AttributeFormat("header", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "Column header text. Used only if cfgrid colHeaders = \"Yes\"."),
                                                             new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                                 "Column width, in pixels."),
                                                             new AttributeFormat("font", AttributeFormat.STRING_TYPE, false,
                                                                                 "Font of data in column."),
                                                             new AttributeFormat("fontsize", AttributeFormat.NUMERIC_TYPE, false,
                                                                                 "Size of text in column."),
                                                             new AttributeFormat("italic", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "Yes: displays grid control text in italics"),
                                                             new AttributeFormat("bold", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "Yes: displays grid control text in bold"),
                                                             new AttributeFormat("bgcolor", AttributeFormat.STRING_TYPE, false,
                                                                                 "Background color of control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
                                                             new AttributeFormat("textcolor", AttributeFormat.STRING_TYPE, false,
                                                                                 "Text color for control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
                                                             new AttributeFormat("href", AttributeFormat.STRING_TYPE, false,
                                                                                 "URL o r query column name that contains a URL to hyperlinkeach grid column with."),
                                                             new AttributeFormat("hrefkey", AttributeFormat.STRING_TYPE, false,
                                                                                 "The query column to use for the value appended to the hrefURL of each column, instead of the column's value."),
                                                             new AttributeFormat("target", AttributeFormat.STRING_TYPE, false,
                                                                                 "Frame in which to open link specified in href."),
                                                             new AttributeFormat("select", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "Yes: user can select the column in grid control."),
                                                             new AttributeFormat("display", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "No: hides column"),
                                                             new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                                 "image: grid displays image that corresponds to value incolumn (a built-in CFML image name, or an image incfide\\classes directory or subdirectory referenced withrelative URL). If image is larger than column cell, it isclipped to fit. Built-in image names"),
                                                             new AttributeFormat("headerfont", AttributeFormat.STRING_TYPE, false,
                                                                                 "Font of data in column."),
                                                             new AttributeFormat("headerfontsize", AttributeFormat.NUMERIC_TYPE, false,
                                                                                 "Size of text in column."),
                                                             new AttributeFormat("headeritalic", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "Yes: displays grid control text in italics"),
                                                             new AttributeFormat("headerbold", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "Yes: displays grid control text in bold"),
                                                             new AttributeFormat("headertextcolor", AttributeFormat.STRING_TYPE, false,
                                                                                 "Text color for control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
                                                             new AttributeFormat("dataalign", AttributeFormat.STRING_TYPE, false,
                                                                                 "Column data alignment"),
                                                             new AttributeFormat("headeralign", AttributeFormat.STRING_TYPE, false,
                                                                                 "Column header text alignment"),
                                                             new AttributeFormat("numberformat", AttributeFormat.STRING_TYPE, false,
                                                                                 "Format for displaying numeric data in grid. SeenumberFormat mask characters."),
                                                             new AttributeFormat("values", AttributeFormat.STRING_TYPE, false,
                                                                                 "Formats cells in column as drop-down list boxes; specifyitems in drop-down list. Example:values = \"arthur, scott, charles, 1-20, mabel\""),
                                                             new AttributeFormat("valuesdisplay", AttributeFormat.STRING_TYPE, false,
                                                                                 "Maps elements in values attribute to string to display indrop-down list. Delimited strings and/or numeric range(s)."),
                                                             new AttributeFormat("valuesdelimiter", AttributeFormat.STRING_TYPE, false,
                                                                                 "Maps elements in values attribute to string to display indrop-down list. Delimited strings and/or numeric range(s)."),
                                                             new AttributeFormat("mask", AttributeFormat.STRING_TYPE, false,
                                                                                 "A mask pattern that controls the character patternthat the form displays or allows users to input andsends to ColdFusion.For currency type data, use currency symbol.For text or numeric type data use:- A = [A-Za-z]- X = [A-Za-z0-9]- 9 = [0-9]- ? = Any character- all other = the literal characterFor date type data use datetime masks.")}));
    myTagAttributes.put("cfgridrow", new TagDescription(true,
                                                        "Lets you define a cfgrid that does not use a query as sourcefor row data. If a query attribute is specified in cfgrid, thecfgridrow tags are ignored.",
                                                        new AttributeFormat[]{new AttributeFormat("data", AttributeFormat.STRING_TYPE, true,
                                                                                                  "Delimited list of column values. If a value contains acomma, it must be escaped with another comma")}));
    myTagAttributes.put("cfgridupdate", new TagDescription(true,
                                                           "Used within a cfgrid tag. Updates data sources directly fromedited grid data. This tag provides a direct interface withyour data source.This tag applies delete row actions first, then insert rowactions, then update row actions. If it encounters an error,it stops processing rows.",
                                                           new AttributeFormat[]{
                                                             new AttributeFormat("grid", AttributeFormat.STRING_TYPE, true,
                                                                                 "Name of cfgrid form element that is the source for theupdate action."),
                                                             new AttributeFormat("datasource", AttributeFormat.STRING_TYPE, true,
                                                                                 "Name of data source for the update action."),
                                                             new AttributeFormat("tablename", AttributeFormat.STRING_TYPE, true,
                                                                                 "Table in which to insert form fields.ORACLE drivers: must be uppercase.Sybase driver: case-sensitive. Must be the same case usedwhen table was created"),
                                                             new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                                 "Overrides username specified in ODBC setup."),
                                                             new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                                 "Overrides password specified in ODBC setup."),
                                                             new AttributeFormat("tableowner", AttributeFormat.STRING_TYPE, false,
                                                                                 "Table owner, if supported."),
                                                             new AttributeFormat("tablequalifier", AttributeFormat.STRING_TYPE, false,
                                                                                 "For data sources that support table qualifiers, use thisfield to specify qualifier for table. The purpose of tablequalifiers varies among drivers. For SQL Server andOracle, qualifier refers to name of database that containstable. For Intersolv dBASE driver, qualifier refers todirectory where DBF files are located."),
                                                             new AttributeFormat("keyonly", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "Applies to the update action:Yes: the WHERE criteria are limited to the key valuesNo: the WHERE criteria include key values and the originalvalues of changed fields")}));
    myTagAttributes.put("cfheader", new TagDescription(true, "Generates custom HTTP response headers to return to the client.",
                                                       new AttributeFormat[]{new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                                 "Header nameRequired if statusCode not specified"),
                                                         new AttributeFormat("value", AttributeFormat.STRING_TYPE, false,
                                                                             "HTTP header value"),
                                                         new AttributeFormat("charset", AttributeFormat.STRING_TYPE, false,
                                                                             "The character encoding in which to encode the header value.For more information on character encodings, see:www.w3.org/International/O-charset.html."),
                                                         new AttributeFormat("statuscode", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "HTTP status codeRequired if name not specified"),
                                                         new AttributeFormat("statustext", AttributeFormat.STRING_TYPE, false,
                                                                             "Explains status code")}));
    myTagAttributes.put("cfhtmlhead", new TagDescription(true,
                                                         "Writes text to the head section of a generated HTML page. It isuseful for embedding JavaScript code, or putting other HTMLtags, such as meta, link, title, or base in an HTML pageheader.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("text", AttributeFormat.STRING_TYPE, true,
                                                                               "Text to add to the <head> area of an HTML page.")}));
    myTagAttributes.put("cfhttp", new TagDescription(false, "Generates an HTTP request and handles the response from theserver.",
                                                     new AttributeFormat[]{new AttributeFormat("url", AttributeFormat.URL_TYPE, true,
                                                                                               "Address of the resource on the server which will handlethe request. The URL must include the hostname or IPaddress.If you do not specify the transaction protocol (http:// orhttps://), CFML defaults to http.If you specify a port number in this attribute, itoverrides any port attribute value.The cfhttppparam tag URL attribute appends query stringattribute-value pairs to the URL."),
                                                       new AttributeFormat("port", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Port number on the server to which to send the request.A port value in the url attribute overrides this value.(default: http 80 - https 443)"),
                                                       new AttributeFormat("method", AttributeFormat.STRING_TYPE, false,
                                                                           "* GET Requests information from the server. Any data thatthe server requires to identify the requestedinformation must be in the URL or in cfhttp type=\"URL\"tags.* POST Sends information to the server for processing.Requires one or more cfhttpparam tags. Often used forsubmitting form-like data.* PUT Requests the server to store the message body at thespecified URL. Use this method to send files to theserver.* DELETE Requests the server to delete the specified URL.* HEAD Identical to the GET method, but the server doesnot send a message body in the response. Use thismethod for testing hypertext links for validity andaccessibility, determining the type or modificationtime of a document, or determining the type of server.* TRACE Requests that the server echo the received HTTPheaders back to the sender in the response body. Tracerequests cannot have bodies. This method enables theCFML application to see what is being receivedat the server, and use that data for testing ordiagnostic information.* OPTIONS A request for information about thecommunication options available forthe server or thespecified URL. This method enables the CFMLapplication to determine the options and requirementsassociated with a URL, or the capabilities of a server,without requesting any additional activity by theserver."),
                                                       new AttributeFormat("proxyserver", AttributeFormat.STRING_TYPE, false,
                                                                           "The proxy server required to access the URL."),
                                                       new AttributeFormat("proxyport", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "The port to use on The proxy server."),
                                                       new AttributeFormat("proxyuser", AttributeFormat.STRING_TYPE, false,
                                                                           "The user ID to send to the proxy server."),
                                                       new AttributeFormat("proxypassword", AttributeFormat.STRING_TYPE, false,
                                                                           "The user's password on the proxy server."),
                                                       new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                           "A username. May be required by server."),
                                                       new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                           "A password. May be required by server"),
                                                       new AttributeFormat("useragent", AttributeFormat.STRING_TYPE, false,
                                                                           "Text to put in the user agent request header. Used toidentify the request client software. Can make theCFML application appear to be a browser."),
                                                       new AttributeFormat("charset", AttributeFormat.STRING_TYPE, false,
                                                                           "TThe character encoding of the request, including the URLquery string and form or file data, and the response.For more information on character encodings, see:www.w3.org/International/O-charset.html."),
                                                       new AttributeFormat("resolveurl", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "No does not resolveFunction URLs in the response body. As a result,any relative URL links in the response body do not work.Yes resolves URLs in the response body to absolute URLs,including the port number, so that links in a retrievedpage remain functional."),
                                                       new AttributeFormat("throwonerror", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Yes if the server returns an error response code, throwsan exception that can be caught using the cftry andcfcatch or CFML error pages.No does not throw an exception if an error response isreturned. In this case, your application can use thecfhttp.StatusCode variable to determine if there wasan error and its cause."),
                                                       new AttributeFormat("redirect", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "If the response header includes a Location field,determines whether to redirect execution to the URLspecified in the field."),
                                                       new AttributeFormat("timeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Value, in seconds of the maximum time the request can take.If the timeout passes without a response, CFMLconsiders the request to have failed."),
                                                       new AttributeFormat("getasbinary", AttributeFormat.STRING_TYPE, false,
                                                                           "* No If CFML does not recognize the response bodytype as text, convert it to a CFML object.* Auto If CFML does not recognize the response bodytype as text, convert it to CFML Binary type data.* Yes Always convert the response body content intoCFML Binary type data, even if CFMLrecognizes the response body type as text."),
                                                       new AttributeFormat("result", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                           "Specifies the name of the variable in which you wantthe result returned.Default: CFHTTP"),
                                                       new AttributeFormat("delimiter", AttributeFormat.STRING_TYPE, false,
                                                                           "Sepcifies a character that separates quiery columns."),
                                                       new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                           "Tells ColdFusion to create a query object with the givenname from the returned HTTP response body."),
                                                       new AttributeFormat("columns", AttributeFormat.STRING_TYPE, false,
                                                                           "The column names for the query, separated by commas, with nospaces. Columnnames must start with a letter. The remainingcharacters can be letters, numbers, or underscorecharacters (_).If there are no column name headers in the response,specify this attribute to identify the column names.If you specify this attribute, and the firstrowasHeaderattribute is True (the default), the column names specifiedby this attribute replace the first line of the response.You can use this behavior to replace the column namesretrieved by the request with your own names.If a duplicate column heading is encountered in either thisattribute or in the column names from the response,ColdFusion appends an underscore to the name to make itunique.If the number of columns specified by this attribute doesnot equal the number of columns in the HTTP response body,ColdFusion generates an error."),
                                                       new AttributeFormat("firstrowasheaders", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Determines how ColdFusion processes the first row of thequery record set:* yes: processes the first row as column heads. If youspecify a columns attribute, ColdFusion ignores thefirst row of the file.* no: processes the first row as data. If you do notspecify a columns attribute, ColdFusion generates columnnames by appending numbers to the word \"column\"; forexample, \"column_1\"."),
                                                       new AttributeFormat("delimiter", AttributeFormat.STRING_TYPE, false,
                                                                           "A character that separates query columns. The responsebody must use this character to separate the query columns."),
                                                       new AttributeFormat("textqualifier", AttributeFormat.STRING_TYPE, false,
                                                                           "A character that, optionally, specifies the start and endof a text column. This character must surround any textfields in the response body that contain the delimitercharacter as part of the field value.To include this character in column text, escape it byusing two characters in place of one. For example, if thequalifier is a double-quotation mark, escape it as \"\".")}));
    myTagAttributes.put("cfhttpparam", new TagDescription(true,
                                                          "Allowed inside cfhttp tag bodies only. Required for cfhttp POSToperations. Optional for all others. Specifies parameters tobuild an HTTP request.",
                                                          new AttributeFormat[]{
                                                            new AttributeFormat("type", AttributeFormat.STRING_TYPE, true,
                                                                                "Information type* Header: The parameter specifies an HTTP header.(ColdFusion does not URL encode the header)* Body: Specifies the body of the HTTP request.(ColdFusion does not URL encode the body contents)* XML: Identifies the request as having a content-type oftext/xml. Specifies that the value attribute containsthe body of the HTTP request. Used to send XML to thedestination URL. CFML does not URL encode the XMLdata.* CGI: Specifies an HTTP header. CFML URL encodesthe header by default.* File: Tells CFML to send the contents of thespecified file. CFML does not URL encode thefile contents* URL: Specifies a URL query string name-value pair toappend to the cfhttp url attribute. CFML URLencodes the query string.* FormField: Specifies a form field to send. CFMLURL encodes the Form field by default.* Cookie: Specifies a cookie to send as an HTTP header.CFML URL encodes the cookie."),
                                                            new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                                "Variable name for data that is passed. Ignored for Bodyand XML types. For File type, specifies the filename tosend in the request."),
                                                            new AttributeFormat("value", AttributeFormat.STRING_TYPE, false,
                                                                                "Value of the data that is sent. Ignored for File type. Thevalue must contain string data or data that CFML canconvert to a string for all type attributes except Body.Body types can have string or binary values."),
                                                            new AttributeFormat("file", AttributeFormat.STRING_TYPE, false,
                                                                                "Applies to File type; ignored for all other types. Theabsolute path to the file that is sent in the request body."),
                                                            new AttributeFormat("encoded", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                "Applies to FormField and CGI types; ignored for all othertypes. Specifies whether to URLEncode the form field orheader."),
                                                            new AttributeFormat("mimetype", AttributeFormat.STRING_TYPE, false,
                                                                                "Applies to File type; invalid for all other types.Specifies the MIME media type of the file contents.The content type can include an identifier for thecharacter encoding of the file; for example, text/html;charset=ISO-8859-1 indicates that the file is HTML text inthe ISO Latin-1 character encoding.")}));
    myTagAttributes.put("cfif", new TagDescription(false,
                                                   "Creates simple and compound conditional statements in CFML.Tests an expression, variable, function return value, orstring. Used, optionally, with the cfelse and cfelseif tags.",
                                                   myEmptyAttributesArray));
    myTagAttributes.put("cfimport", new TagDescription(true,
                                                       "You can use the cfimport tag to import either of the following:* All CFML pages in a directory, as a tag custom taglibrary.* A Java Server Page (JSP) tag library. A JSP tag library is apackaged set of tag handlers that conform to the JSP 1.1 tagextension API.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("taglib", AttributeFormat.STRING_TYPE, true,
                                                                             "Tag library URI. The path must be relative to the web root(and start with /), the current page location, or adirectory specified in the Administrator CFMLmappings page.A directory inwhich custom CFML tags are stored. Inthis case, all the cfm pages in this directory are treatedas custom tags in a tag library.A path to a JAR in a web-application; for example,\"/WEB-INF/lib/sometags.jar\"A path to a tag library descriptor; for example,\"/sometags.tld\"Note: You must put JSP custom tag libraries in the/WEB-IN/lib directory. This limitation does not apply toCFML pages."),
                                                         new AttributeFormat("prefix", AttributeFormat.STRING_TYPE, true,
                                                                             "Prefix by which to access the imported custom CFML tags JSPtags.If you import a CFML custom tag directory and specify anempty value, \"\", for this attribute, you can call thecustom tags without using a prefix. You must specify anduse a prefix for a JSP tag library.")}));
    myTagAttributes.put("cfinclude", new TagDescription(true,
                                                        "Embeds references to CFML pages in CFML. You can embedcfinclude tags recursively. For another way to encapsulateCFML, see cfmodule. (A CFML page was formerly sometimescalled a CFML template or a template.)",
                                                        new AttributeFormat[]{
                                                          new AttributeFormat("template", AttributeFormat.STRING_TYPE, true,
                                                                              "A logical path to a CFML page.")}));
    myTagAttributes.put("cfindex", new TagDescription(true,
                                                      "Populates a Verity search engine collection with an index ofdocuments on a file system or of CFML query result sets.A collection must exist before it can be populated.",
                                                      new AttributeFormat[]{
                                                        new AttributeFormat("collection", AttributeFormat.STRING_TYPE, true,
                                                                            "Name of a collection that is registered by CFML; forexample, \"personnel\"Name and absolute path of a collection that is notregistered by CFML; for example:\"e:\\collections\\personnel\""),
                                                        new AttributeFormat("action", AttributeFormat.STRING_TYPE, false,
                                                                            "- update: updates a collection and adds key to the index.- delete: removes collection documents as specified bythe key attribute.- purge: deletes all of the documents in a collection.Causes the collection to be taken offline, preventingsearches.- refresh: deletes all of the documents in a collection,and then performs an update."),
                                                        new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                            "file: using the key attribute value of the query result asinput, applies action value to filenames or filepaths.path: using the key attribute value of the query result asinput, applies action to filenames or filepaths thatpass the extensions filtercustom: If action = \"update\" or \"delete\": applies action tocustom entities in query results."),
                                                        new AttributeFormat("title", AttributeFormat.STRING_TYPE, false,
                                                                            "* Title for collection* Query column name for type and a valid query namePermits searching collections by title or displaying aseparate title from the key"),
                                                        new AttributeFormat("key", AttributeFormat.STRING_TYPE, false,
                                                                            "* Absolute path and filename, if type = \"file\"* Absolute path, if type = \"path\"* A query column name (typically, the primary key columnname), if type = \"custom\"* A query column name, if type = any other valueThis attribute is required for the actions listed, unlessyou intend for its value to be an empty string."),
                                                        new AttributeFormat("body", AttributeFormat.STRING_TYPE, false,
                                                                            "* ASCII text to index* Query column name(s), if name is specified in queryYou can specify columns in a delimited list. For example:\"emp_name, dept_name, location\""),
                                                        new AttributeFormat("custom1", AttributeFormat.STRING_TYPE, false,
                                                                            "Custom field in which you can store data during an indexingoperation. Specify a query column name for type, and aquery name."),
                                                        new AttributeFormat("custom2", AttributeFormat.STRING_TYPE, false,
                                                                            "Custom field in which you can store data during an indexingoperation. Specify a query column name for type, and aquery name."),
                                                        new AttributeFormat("custom3", AttributeFormat.STRING_TYPE, false,
                                                                            "Custom field in which you can store data during an indexingoperation. Specify a query column name for type, and aquery name. (Added in ColdFusion 7)"),
                                                        new AttributeFormat("custom4", AttributeFormat.STRING_TYPE, false,
                                                                            "Custom field in which you can store data during an indexingoperation. Specify a query column name for type, and aquery name. (Added in ColdFusion 7)"),
                                                        new AttributeFormat("category", AttributeFormat.STRING_TYPE, false,
                                                                            "A string value that specifies one or more search categoriesfor which to index the data. You can define multiplecategories, separated by commas, for a single index."),
                                                        new AttributeFormat("categoryTree", AttributeFormat.STRING_TYPE, false,
                                                                            "A string value that specifies a hierarchical category orcategory tree for searching. It is a series of categoriesseparated by forward slashes (\"/\"). You can specify onlyone category tree."),
                                                        new AttributeFormat("urlpath", AttributeFormat.URL_TYPE, false,
                                                                            "If type=\"file\" or \"path\", specifies the URL path. When thecollection is searched with cfsearch, this pathname isprefixed to filenames and returned as the url attribute."),
                                                        new AttributeFormat("extensions", AttributeFormat.STRING_TYPE, false,
                                                                            "Delimited list of file extensions that CFML uses toindex files, if type = \"Path\".\"*.\" returns files with no extension.For example: the following code returns files with alisted extension or no extension:extensions = \".htm, .html, .cfm, .cfml, \"*.\""),
                                                        new AttributeFormat("query", AttributeFormat.STRING_TYPE, false,
                                                                            "Query against which collection is generated"),
                                                        new AttributeFormat("recurse", AttributeFormat.BOOLEAN_TYPE, false,
                                                                            "Yes: if type = \"path\", directories below the pathspecified in key are included in indexing operation"),
                                                        new AttributeFormat("language", AttributeFormat.STRING_TYPE, false,
                                                                            "For options, see cfcollection. Requires the appropriateVerity Locales language pack (Western Europe, Asia,Multilanguage, Eastern Europe/Middle Eastern)."),
                                                        new AttributeFormat("status", AttributeFormat.STRING_TYPE, false,
                                                                            "The name of the structure into which ColdFusion MXreturns status information.")}));
    myTagAttributes.put("cfinput", new TagDescription(true,
                                                      "Used within the cfform tag, to place radio buttons, check boxes,or text boxes on a form. Provides input validation for thespecified control type.",
                                                      new AttributeFormat[]{new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                                "Name for form input element."),
                                                        new AttributeFormat("autosuggest", AttributeFormat.STRING_TYPE, false,
                                                                            "Specifies entry completion suggestions todisplay as the user types into a text input. Theuser can select a suggestion to complete the textentry.The valid value can be either of the following:вЂў A string consisting of the suggestion valuesseparated by the delimiter specified by thedelimiter attribute.вЂў A bind expression that gets the suggestionvalues based on the current input text.Valid only for cfinput type=\"text\"."),
                                                        new AttributeFormat("autosuggestbinddelay", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "The minimum time between autosuggest bindexpression invocations, in seconds. Use thisattribute to limit the number of requests that aresent to the server when a user types.Valid only for cfinput type=\"text\""),
                                                        new AttributeFormat("autosuggestminlength", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "The minimum number of characters required inthe text box before invoking a bind expression toreturn items for suggestion.Valid only for cfinput type=\"text\"."),
                                                        new AttributeFormat("bindattribute", AttributeFormat.STRING_TYPE, false,
                                                                            "Specifies the HTML tag attribute whose value isset by the bind attribute. You can only specifyattributes in the browserвЂ™s HTML DOM tree, notColdFusion-specific attributes.Ignored if there is no bind attribute.Valid only for cfinput type=\"text\"."),
                                                        new AttributeFormat("bindonload", AttributeFormat.BOOLEAN_TYPE, false,
                                                                            "A Boolean value that specifies whether toexecute the bind attribute expression when firstloading the form.Ignored if there is no bind attribute.Valid only for cfinput type=\"text\"."),
                                                        new AttributeFormat("id", AttributeFormat.STRING_TYPE, false,
                                                                            "ID for form input element."),
                                                        new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                            "The input control type to create:- button: push button.- checkbox: check box.- file: file selector; not supported in Flash.- hidden: invisible control.- image: clickable button with an image.- password: password entry control; hides input values.- radio: radio button.- reset: form reset button.- submit: form submission button.- text: text entry box.- datefield: Flash only; date entry field with anexpanding calendar for selecting dates."),
                                                        new AttributeFormat("label", AttributeFormat.STRING_TYPE, false,
                                                                            "Label to put next to the control on a Flash or XML form.Not used for button, hidden, image, reset, or submit types."),
                                                        new AttributeFormat("style", AttributeFormat.STRING_TYPE, false,
                                                                            "In HTML or XML format, ColdFusion passes the style attributeto the browser or XML.In Flash format, must be a style specification in CSS format."),
                                                        new AttributeFormat("class", AttributeFormat.STRING_TYPE, false,
                                                                            "Stylesheet class for form input element."),
                                                        new AttributeFormat("required", AttributeFormat.BOOLEAN_TYPE, false, ""),
                                                        new AttributeFormat("mask", AttributeFormat.STRING_TYPE, false,
                                                                            "A mask pattern that controls the character pattern thatusers can enter, or that the form sends to ColdFusion.In HTML and Flash for type=text use:- A = [A-Za-z]- X = [A-Za-z0-9]- 9 = [0-9]- ? = Any character- all other = the literal characterIn Flash for type=datefield use:- D = day; can use 0-2 mask characters.- M = month; can use 0-4 mask characters.- Y = year; can use 0, 2, or 4 characters.- E = day in week; can use 0-4 characters."),
                                                        new AttributeFormat("validate", AttributeFormat.STRING_TYPE, false,
                                                                            "date: verifies format mm/dd/yy.eurodate: verifies date format dd/mm/yyyy.time: verifies time format hh:mm:ss.float: verifies floating point format.integer: verifies integer format.telephone: verifies telephone format ###-###-####. Theseparator can be a blank. Area code and exchange mustbegin with digit 1 - 9.zipcode: verifies, in U.S. formats only, 5- or 9-digitformat #####-####. The separator can be a blank.creditcard: strips blanks and dashes; verifies number usingmod10 algorithm. Number must have 13-16 digits.social_security_number: verifies format ###-##-####. Theseparator can be a blank.regular_expression: matches input against patternattribute."),
                                                        new AttributeFormat("validateat", AttributeFormat.STRING_TYPE, false,
                                                                            "How to do the validation; one or more of the following:onSubmit, onServer or onBlur.onBlur and onSubmit are identical in Flash forms. Formultiple values, use a comma-delimited list.Default: onSubmit"),
                                                        new AttributeFormat("message", AttributeFormat.STRING_TYPE, false,
                                                                            "Message text to display if validation fails"),
                                                        new AttributeFormat("range", AttributeFormat.STRING_TYPE, false,
                                                                            "Minimum and maximum value range, separated by a comma. Iftype = \"text\" or \"password\", this applies only to numericdata."),
                                                        new AttributeFormat("maxlength", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Maximum length of text entered, if type=text or password."),
                                                        new AttributeFormat("pattern", AttributeFormat.REGEX_TYPE, false,
                                                                            "JavaScript regular expression pattern to validate input.ColdFusion uses this attribute only if you specify regexin the validate attribute. Omit leading and trailing slashes."),
                                                        new AttributeFormat("onvalidate", AttributeFormat.STRING_TYPE, false,
                                                                            "Custom JavaScript function to validate user input. The formobject, input object, and input object values are passedto the routine, which should return True if validationsucceeds, and False otherwise. If used, the validateattribute is ignored."),
                                                        new AttributeFormat("onerror", AttributeFormat.STRING_TYPE, false,
                                                                            "Custom JavaScript function to execute if validation fails."),
                                                        new AttributeFormat("size", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Size of input control. Ignored, if type=radio or checkbox.If specified in a Flash form, ColdFusion sets the controlwidth pixel value to 10 times the specified size andignores the width attribute."),
                                                        new AttributeFormat("value", AttributeFormat.STRING_TYPE, false,
                                                                            "HTML: corresponds to the HTML value attribute. Its usedepends on control type.Flash: optional; specifies text for button type inputs:button, submit, and image."),
                                                        new AttributeFormat("bind", AttributeFormat.STRING_TYPE, false,
                                                                            "A Flash bind expression that populates the field withinformation from other form fields."),
                                                        new AttributeFormat("checked", AttributeFormat.BOOLEAN_TYPE, false,
                                                                            "Selects a control. No value is required. Applies iftype=radio or checkbox.Default: false"),
                                                        new AttributeFormat("disabled", AttributeFormat.BOOLEAN_TYPE, false,
                                                                            "Disables user input, making the control read-only. Todisable input, specify disabled without an attribute ordisabled=\"true\". To enable input, omit the attributeor specify disabled=\"false\"."),
                                                        new AttributeFormat("src", AttributeFormat.STRING_TYPE, false,
                                                                            "Applies to Flash button, reset, submit, and image types,and the HTML image type. URL of an image to use onthe button. Flash does not support GIF images."),
                                                        new AttributeFormat("onkeyup", AttributeFormat.STRING_TYPE, false,
                                                                            "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the user releases a keyboard key in the control."),
                                                        new AttributeFormat("onkeydown", AttributeFormat.STRING_TYPE, false,
                                                                            "JavaScript (HTML/XML) or ActionScript (Flash)ActionScript to run when the user presses a keyboardkey in the control."),
                                                        new AttributeFormat("onmouseup", AttributeFormat.STRING_TYPE, false,
                                                                            "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the user presses a mouse button in the control."),
                                                        new AttributeFormat("onmousedown", AttributeFormat.STRING_TYPE, false,
                                                                            "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the user releases a mouse button in the control."),
                                                        new AttributeFormat("onchange", AttributeFormat.STRING_TYPE, false,
                                                                            "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the control changes due to user action. In Flash,applies to datefield, password, and text types only."),
                                                        new AttributeFormat("onclick", AttributeFormat.STRING_TYPE, false,
                                                                            "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the user clicks the control. In Flash, applies tobutton, checkbox, image, radio, reset, and submit typesonly."),
                                                        new AttributeFormat("daynames", AttributeFormat.STRING_TYPE, false,
                                                                            "A comma-delimited list that sets the names of theweekdays displayed in the calendar. Sunday is thefirst day and the rest of the weekday names follow inthe normal order.Default is: S,M,T,W,Th,F,S"),
                                                        new AttributeFormat("firstdayofweek", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Integer in the range 0-6 specifying the first day of theweek in the calendar, 0 indicates Sunday, 6 indicates Saturday.Default is: 0"),
                                                        new AttributeFormat("monthnames", AttributeFormat.STRING_TYPE, false,
                                                                            "A comma-delimited list of the month names that aredisplayed at the top of the calendar."),
                                                        new AttributeFormat("enabled", AttributeFormat.BOOLEAN_TYPE, false,
                                                                            "Flash only: Boolean value specifying whether the control isenabled. A disabled control appears in light gray. Theinverse of the disabled attribute. Flash only.Default: true"),
                                                        new AttributeFormat("visible", AttributeFormat.BOOLEAN_TYPE, false,
                                                                            "Flash only: Boolean value specifying whether to show thecontrol. Space that would be occupied by an invisiblecontrol is blank.Default: true"),
                                                        new AttributeFormat("tooltip", AttributeFormat.STRING_TYPE, false,
                                                                            "Flash only: Text to display when the mouse pointer hoversover the control."),
                                                        new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Applies to most Flash types, HTML image type onsome browsers. The width of the control, in pixels. ForFlash forms, ColdFusion ignores this attribute if you alsospecify a size attribute value."),
                                                        new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Applies to most Flash types, HTML image type onsome browsers. The height of the control, in pixels. Thedisplayed height might be less than the specified size."),
                                                        new AttributeFormat("passthrough", AttributeFormat.STRING_TYPE, false,
                                                                            "This attribute is deprecated.Passes arbitrary attribute-value pairs to the HTML codethat is generated for the tag. You can use either of thefollowing formats:passthrough=\"title=\"\"myTitle\"\"\"passthrough='title=\"mytitle\"'")}));
    myTagAttributes.put("cfinsert", new TagDescription(true, "Inserts records in data sources from data in a CFML formor form Scope.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("datasource", AttributeFormat.STRING_TYPE, true,
                                                                             "Data source; contains table."),
                                                         new AttributeFormat("tablename", AttributeFormat.STRING_TYPE, true,
                                                                             "Table in which to insert form fields.ORACLE drivers: must be uppercase.Sybase driver: case-sensitive. Must be the same case usedwhen table was created"),
                                                         new AttributeFormat("tableowner", AttributeFormat.STRING_TYPE, false,
                                                                             "For data sources that support table ownership (such as SQLServer, Oracle, and Sybase SQL Anywhere), use this field tospecify the owner of the table."),
                                                         new AttributeFormat("tablequalifier", AttributeFormat.STRING_TYPE, false,
                                                                             "For data sources that support table qualifiers, use thisfield to specify qualifier for table. The purpose of tablequalifiers varies among drivers. For SQL Server andOracle, qualifier refers to name of database that containstable. For Intersolv dBASE driver, qualifier refers todirectory where DBF files are located."),
                                                         new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                             "Overrides username specified in ODBC setup."),
                                                         new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                             "Overrides password specified in ODBC setup."),
                                                         new AttributeFormat("formfields", AttributeFormat.STRING_TYPE, false,
                                                                             "Comma-delimited list of form fields to insert. If notspecified, all fields in the form are included.If a form field is not matched by a column name in thedatabase, CFML throws an error.The database table key field must be present in the form.It may be hidden.")}));
    myTagAttributes.put("cfinvoke", new TagDescription(false,
                                                       "Does either of the following:* Invokes a component method from within a CFML page orcomponent.* Invokes a web service.Different attribute combonations make some attributes requiredat sometimes and not at others.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("component", AttributeFormat.STRING_TYPE, false,
                                                                             "String or component object; a reference to a component, orcomponent to instantiate."),
                                                         new AttributeFormat("method", AttributeFormat.STRING_TYPE, true,
                                                                             "Name of a method. For a web service, the name of anoperation."),
                                                         new AttributeFormat("returnvariable", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                             "Name of a variable for the invocation result."),
                                                         new AttributeFormat("argumentcollection", AttributeFormat.STRING_TYPE, false,
                                                                             "Name of a structure; associative array of arguments to passto the method."),
                                                         new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                             "Overrides username specified in Administrator > Web Services"),
                                                         new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                             "Overrides passowrd specified in Administrator > Web Services"),
                                                         new AttributeFormat("webservice", AttributeFormat.URL_TYPE, false,
                                                                             "The URL of the WSDL file for the web service."),
                                                         new AttributeFormat("timeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "The timeout for the web service request, in seconds"),
                                                         new AttributeFormat("proxyserver", AttributeFormat.STRING_TYPE, false,
                                                                             "The proxy server required to access the webservice URL."),
                                                         new AttributeFormat("proxyport", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "The port to use on The proxy server."),
                                                         new AttributeFormat("proxyuser", AttributeFormat.STRING_TYPE, false,
                                                                             "The user ID to send to the proxy server."),
                                                         new AttributeFormat("proxypassword", AttributeFormat.STRING_TYPE, false,
                                                                             "The user's password on the proxy server."),
                                                         new AttributeFormat("serviceport", AttributeFormat.STRING_TYPE, false,
                                                                             "The port name for the web service. This value iscase-sensitive and corresponds to the port element'sname attribute under the service element. Specify thisattribute if the web service contains multiple ports.Default: first port found in the WSDL.")}));
    myTagAttributes.put("cfinvokeargument", new TagDescription(true,
                                                               "Passes the name and value of a parameter to a component methodor a web service. This tag is used within the cfinvoke tag.",
                                                               new AttributeFormat[]{
                                                                 new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                     "Argument name"),
                                                                 new AttributeFormat("value", AttributeFormat.STRING_TYPE, true,
                                                                                     "Argument value"),
                                                                 new AttributeFormat("omit", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                     "Enables you to omit a parameter when invoking a web service.It is an error to specify omit=\"true\" if the cfinvokewebservice attribute is not specified.- true: omit this parameter when invoking a web service.- false: do not omit this parameter when invoking a web service.")}));
    myTagAttributes.put("cflayout", new TagDescription(false,
                                                       "Creates a region of its container (such as the browserwindow or a cflayoutarea tag) with a specific layoutbehavior: a bordered area, a horizontal or verticallyarranged box, or a tabbed navigator.",
                                                       new AttributeFormat[]{new AttributeFormat("type", AttributeFormat.STRING_TYPE, true,
                                                                                                 "The type of layout."),
                                                         new AttributeFormat("align", AttributeFormat.STRING_TYPE, false,
                                                                             "Specifies the default alignment of the content ofchild layout areas. Each cflayoutarea tag can specifyan alignment attribute to override this value."),
                                                         new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                             "The name of the layout region.  Must be uniqueon a page."),
                                                         new AttributeFormat("padding", AttributeFormat.STRING_TYPE, false,
                                                                             "Applies only to hbox and vbox layouts.You can use any valid CSS length or percent format,such as 10, 10% 10px, or 10em, for this attribute.The padding is included in the child layout areaand takes the style of the layout area."),
                                                         new AttributeFormat("style", AttributeFormat.STRING_TYPE, false,
                                                                             "A CSS style specification that defines layout styles.")}));
    myTagAttributes.put("cflayoutarea", new TagDescription(false,
                                                           "Defines a region within a cflayout tag body, such as anindividual tab of a tabbed layout. This tag is not used inFlash forms.",
                                                           new AttributeFormat[]{
                                                             new AttributeFormat("position", AttributeFormat.STRING_TYPE, false,
                                                                                 "The position...(docs don't explain this one)."),
                                                             new AttributeFormat("align", AttributeFormat.STRING_TYPE, false,
                                                                                 "Specifies how to align child controls within thelayout area."),
                                                             new AttributeFormat("closable", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "A Boolean value specifying whether the area can close.Specifying this attribute adds an x icon on the tab ortitle bar that a user can click to close the area.You cannot use this attribute for border layout areaswith a position attribute valueof center."),
                                                             new AttributeFormat("collapsible", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "A Boolean value specifying whether the area can collapse.Specifying this attribute adds a >> or << icon on thetitle bar that a user can click to collapse the area.You cannot use this attribute for border layout areaswith a position attribute value of center."),
                                                             new AttributeFormat("direction", AttributeFormat.STRING_TYPE, false,
                                                                                 "The position of the area in the layout.  Border stylelayouts can have at most one layout area of each type."),
                                                             new AttributeFormat("disabled", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "A Boolean value specifying whether the tab is disabled,that is, whether user can select the tab to display itscontents. Disabled tabs are greyed out. Ignored if theselected attribute value is true."),
                                                             new AttributeFormat("initCollapsed", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "A Boolean value specifying whether the area is initiallycollapsed. You cannot use this attribute for border layoutareas with a position attribute value of center. Ignoredif the collapsible attribute value is false."),
                                                             new AttributeFormat("initHide", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "A Boolean value specifying whether the area is initiallyhidden. To show an initially hidden area, use theColdFusion.Layout.showArea or ColdFusion.Layout.showTabfunction. You cannot use this attribute for border layoutareas with a position attribute value of center."),
                                                             new AttributeFormat("maxSize", AttributeFormat.NUMERIC_TYPE, false,
                                                                                 "For layouts with top or bottom position attributes, the maximumheight of the area, in pixels, that you can set by dragging asplitter. For layouts with left or right position attributes,the maximum width of the area. You cannot use this attributefor border layout areas with a position attribute value ofcenter."),
                                                             new AttributeFormat("minSize", AttributeFormat.NUMERIC_TYPE, false,
                                                                                 "For layouts with top or bottom position attributes, the minimumheight of the area, in pixels, that you can set by dragging asplitter. For layouts with left or right position attributes,the minimum width of the area., You cannot use this attributefor border layout areas with a position attribute value of center."),
                                                             new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                                 "The name of the layout area."),
                                                             new AttributeFormat("onBindError", AttributeFormat.STRING_TYPE, false,
                                                                                 "The name of a JavaScript function to execute if evaluating abind expression results in an error. The function must taketwo attributes: an HTTP status code and a message. If you omitthis attribute, and have specified a global error handler(by using the ColdFusion.setGlobalErrorHandlerfunction ),it displays the error message; otherwise a default errorpop-up displays."),
                                                             new AttributeFormat("overflow", AttributeFormat.STRING_TYPE, false,
                                                                                 "Specifies how to display child content whose size would causethe control to overflow the window boundaries.  пїЅ In Internet Explorer, layout areas withthe visible setting expand to fit the size of the contents,rather than having the contents extend beyond the layout area."),
                                                             new AttributeFormat("selected", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "A Boolean value specifying whether this tab is initiallyselected so that its contents appears in the layout."),
                                                             new AttributeFormat("size", AttributeFormat.NUMERIC_TYPE, false,
                                                                                 "For hbox layouts and border layouts with top or bottom positionattributes, the initial height of the area. For vbox layoutsand border layouts with left or right position attributes, theinitial width of the area. For hbox and vbox layouts, you canuse any valid CSS length or percent format(such as 10, 10% 10px, or 10em) for this attribute. For borderlayouts, this attribute value must be an integer number ofpixels. You cannot use this attribute for border layout areaswith a position attribute value of center. ColdFusionautomatically determines the center size based on thesize of all other layout areas."),
                                                             new AttributeFormat("source", AttributeFormat.STRING_TYPE, false,
                                                                                 "A URL that returns the layout area contents. ColdFusion usesstandard page path resolution rules. You can use a bind expressionwith dependencies in this attribute. If file specified in thisattribute includes tags that use AJAX features, such as cfform,cfgrid, and cfpod, you must use the cfajaximport tag on the pagethat includes the cflayoutarea tag. For more information,see cfajaximport."),
                                                             new AttributeFormat("splitter", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "A Boolean value specifying whether the layout area has a dividerbetween it and the adjacent layoutarea control. Users can drag thesplitter to change the relative sizes of the areas. If thisattribute is set true on a left or right position layout area,the splitter resizes the area and its adjacent area horizontally.If this attribute is set true on a top or bottom positionlayout area, the splitter resizes the layout vertically.You cannot use this attribute for border layout areas witha position attribute value of center"),
                                                             new AttributeFormat("style", AttributeFormat.STRING_TYPE, false,
                                                                                 "A CSS style specification that controls the appearance of the area."),
                                                             new AttributeFormat("title", AttributeFormat.STRING_TYPE, false,
                                                                                 "For tab layouts, the text to display on the tab. For borderlayouts, if you specify this attribute ColdFusion createsa title bar for the layout area with the specified text asthe title. By default, these layouts do not have a titlebar if they are not closable or collapsible. You cannotuse this attribute for border layout areas with a positionattribute value of center.")}));
    myTagAttributes.put("cfldap", new TagDescription(true,
                                                     "Provides an interface to a Lightweight Directory Access Protocol(LDAP) directory server, such as the Netscape Directory Server.",
                                                     new AttributeFormat[]{new AttributeFormat("server", AttributeFormat.STRING_TYPE, true,
                                                                                               "Host name or IP address of LDAP server."),
                                                       new AttributeFormat("port", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Port of the LDAP server (default 389)."),
                                                       new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                           "The User ID. Required if secure = \"CFSSL_BASIC\""),
                                                       new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                           "Password that corresponds to user name.If secure = \"CFSSL_BASIC\", V2 encrypts the password beforetransmission."),
                                                       new AttributeFormat("action", AttributeFormat.STRING_TYPE, false,
                                                                           "* query: returns LDAP entry information only. Requires name,start, and attributes attributes.* add: adds LDAP entries to LDAP server. Requires attributesattribute.* modify: modifies LDAP entries, except distinguished name dnattribute, on LDAP server. Requires dn. See modifyType attribute.* modifyDN: modifies distinguished name attribute for LDAentries on LDAP server. Requires dn.* delete: deletes LDAP entries on an LDAP server. Requires dn."),
                                                       new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                           "Required if action = \"Query\"Name of LDAP query. The tag validates the value."),
                                                       new AttributeFormat("timeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Maximum length of time, in seconds, to wait for LDAP processing.Default 60000"),
                                                       new AttributeFormat("maxrows", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Maximum number of entries for LDAP queries."),
                                                       new AttributeFormat("start", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Required if action = \"Query\"Distinguished name of entry to be used to start a search."),
                                                       new AttributeFormat("scope", AttributeFormat.STRING_TYPE, false,
                                                                           "Scope of search, from entry specified in start attribute foraction = \"Query\".* oneLevel: entries one level below entry.* base: only the entry.* subtree: entry and all levels below it."),
                                                       new AttributeFormat("attributes", AttributeFormat.STRING_TYPE, false,
                                                                           "Required if action = \"Query\", \"Add\", \"ModifyDN\", or \"Modify\"For queries: comma-delimited list of attributes to return. Forqueries, to get all attributes, specify \"*\".If action = \"add\" or \"modify\", you can specify a list of updatecolumns. Separate attributes with a semicolon.If action = \"ModifyDN\", CFML passes attributes to theLDAP server without syntax checking."),
                                                       new AttributeFormat("returnasbinary", AttributeFormat.STRING_TYPE, false,
                                                                           "A comma-delimited list of columns that are tobe returned as binary values."),
                                                       new AttributeFormat("filter", AttributeFormat.STRING_TYPE, false,
                                                                           "Search criteria for action = \"Query\".List attributes in the form:\"(attribute operator value)\" Example: \"(sn = Smith)\""),
                                                       new AttributeFormat("sort", AttributeFormat.STRING_TYPE, false,
                                                                           "Attribute(s) by which to sort query results. Use a commadelimiter."),
                                                       new AttributeFormat("sortcontrol", AttributeFormat.STRING_TYPE, false,
                                                                           "Default asc* nocase: case-insensitive sort* asc: ascending (a to z) case-sensitive sort* desc: descending (z to a) case-sensitive sortYou can enter a combination of sort types; for example,sortControl = \"nocase, asc\"."),
                                                       new AttributeFormat("dn", AttributeFormat.STRING_TYPE, false,
                                                                           "Distinguished name, for update action. Example:\"cn = Bob Jensen, o = Ace Industry, c = US\""),
                                                       new AttributeFormat("startrow", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "sed with action = \"query\". First row of LDAP query to insertinto a CFML query."),
                                                       new AttributeFormat("modifytype", AttributeFormat.STRING_TYPE, false,
                                                                           "Default replaceHow to process an attribute in a multi-value list.* add: appends it to any attributes* delete: deletes it from the set of attributes* replace: replaces it with specified attributesYou cannot add an attribute that is already present or that isempty."),
                                                       new AttributeFormat("rebind", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "* Yes: attempt to rebind referral callback and reissue query byreferred address using original credentials.* No: referred connections are anonymous"),
                                                       new AttributeFormat("referral", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Number of hops allowed in a referral. A value of 0 disablesreferred addresses for LDAP; no data is returned."),
                                                       new AttributeFormat("secure", AttributeFormat.STRING_TYPE, false,
                                                                           "Security to employ, and required information. One option:* CFSSL_BASIC\"CFSSL_BASIC\" provides V2 SSL encryptionand server authentication."),
                                                       new AttributeFormat("separator", AttributeFormat.STRING_TYPE, false,
                                                                           "Default , (a comma)Delimiter to separate attribute values of multi-valueattributes. Used by query, add, and modify actions, and bycfldap to output multi-value attributes.For example, if $ (dollar sign), the attributes attribute couldbe \"objectclass = top$person\", where the firstvalue ofobjectclass is top, and the second value is person. This avoidsconfusion if values include commas."),
                                                       new AttributeFormat("delimiter", AttributeFormat.STRING_TYPE, false,
                                                                           "Separator between attribute name-value pairs. Use thisattribute if:* the attributes attribute specifies more than one item, or* an attribute contains the default delimiter (semicolon). Forexample: mgrpmsgrejecttext;lang-enUsed by query, add, and modify actions, and by cfldap to outputmulti-value attributes.For example, if $ (dollar sign), you could specify\"cn = Double Tree Inn$street = 1111 Elm; Suite 100 where thesemicolon is part of the street value.")}));
    myTagAttributes.put("cflocation", new TagDescription(true, "Stops execution of the current page and opens a CFML page or HTML file.",
                                                         new AttributeFormat[]{new AttributeFormat("url", AttributeFormat.STRING_TYPE, true,
                                                                                                   "URL of HTML file or CFML page to open."),
                                                           new AttributeFormat("addtoken", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "clientManagement must be enabled (see cfapplication).")}));
    myTagAttributes.put("cflock", new TagDescription(false,
                                                     "Ensures the integrity of shared data. Instantiates thefollowing kinds of locks:* Exclusive allows single-thread access to the CFML constructs* Read-only allows multiple requests to access CFML constructs",
                                                     new AttributeFormat[]{
                                                       new AttributeFormat("timeout", AttributeFormat.NUMERIC_TYPE, true,
                                                                           "Maximum length of time, in seconds, to wait to obtain alock. If lock is obtained, tag execution continues.Otherwise, behavior depends on throwOnTimeout attributevalue."),
                                                       new AttributeFormat("scope", AttributeFormat.STRING_TYPE, false,
                                                                           "Lock scope. Mutually exclusive with the name attribute.Lock name. Only one request in the specified scope canexecute the code within this tag (or within any othercflock tag with the same lock scope scope) at a time."),
                                                       new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                           "Lock name. Mutually exclusive with the scope attribute.Only one request can execute the code within a cflock tagwith a given name at a time. Cannot be an empty string."),
                                                       new AttributeFormat("throwontimeout", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "How timeout conditions are handled."),
                                                       new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                           "readOnly: lets more than one request read shared data.exclusive: lets one request read or write shared data.")}));
    myTagAttributes.put("cflog", new TagDescription(true, "Writes a message to a log file.", new AttributeFormat[]{
      new AttributeFormat("text", AttributeFormat.STRING_TYPE, true, "Message text to log."),
      new AttributeFormat("log", AttributeFormat.STRING_TYPE, false,
                          "If you omit the file attribute, writes messages to standardlog file. Ignored, if you specify file attribute.Application: writes to Application.log, normally used forapplication-specific messages.Scheduler: writes to Scheduler.log, normally used to logthe execution of scheduled tasks."),
      new AttributeFormat("file", AttributeFormat.STRING_TYPE, false,
                          "Message file. Specify only the main part of the filename.For example, to log to the Testing.log file, specify\"Testing\".The file must be located in the default log directory. Youcannot specify a directory path. If the file does notexist, it is created automatically, with the suffix .log."),
      new AttributeFormat("type", AttributeFormat.STRING_TYPE, false, "Type (severity) of the message"),
      new AttributeFormat("application", AttributeFormat.BOOLEAN_TYPE, false,
                          "log application name, if it is specified in a cfapplicationtag.")}));
    myTagAttributes.put("cflogin", new TagDescription(false,
                                                      "A container for user login and authentication code. CFMLruns the code in this tag if a user is not already logged in.You put code in the tag that authenticates the user andidentifies the user with a set of roles. Used with cfloginusertag.",
                                                      new AttributeFormat[]{
                                                        new AttributeFormat("idletimeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Time interval with no keyboard activity after whichCFML logs the user off. Seconds."),
                                                        new AttributeFormat("applicationtoken", AttributeFormat.STRING_TYPE, false,
                                                                            "Unique application identifier. Limits the login validity toone application, as specified by the cfapplication tag."),
                                                        new AttributeFormat("cookiedomain", AttributeFormat.STRING_TYPE, false,
                                                                            "Domain of the cookie that is used to mark a user as loggedin. Use this attribute to enable a user login cookie towork with multiple clustered servers in the same domain.")}));
    myTagAttributes.put("cfloginuser", new TagDescription(true,
                                                          "Identifies an authenticated user to CFML. Specifies theuser ID and roles. Used within a cflogin tag.",
                                                          new AttributeFormat[]{
                                                            new AttributeFormat("name", AttributeFormat.STRING_TYPE, true, "A username."),
                                                            new AttributeFormat("password", AttributeFormat.STRING_TYPE, true,
                                                                                "A user password."),
                                                            new AttributeFormat("roles", AttributeFormat.STRING_TYPE, true,
                                                                                "A comma-delimited list of role identifiers.CFML processes spaces in a list element as part ofthe element.")}));
    myTagAttributes.put("cflogout", new TagDescription(true,
                                                       "Logs the current user out. Removes knowledge of the user ID,password, and roles from the server. If you do not use thistag, the user is automatically logged out when the sessionends.",
                                                       myEmptyAttributesArray));
    myTagAttributes.put("cfloop", new TagDescription(false,
                                                     "Different items are required based on loop type. Items listedas required may not be depending on your loop type.Loop forms:[query] [condition] [index + from + to ] [index + list][collection + item ]",
                                                     new AttributeFormat[]{new AttributeFormat("index", AttributeFormat.STRING_TYPE, false,
                                                                                               "Index value. CFML sets it to from value andincrements or decrements by step value, until it equals tovalue."),
                                                       new AttributeFormat("to", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Beginning value of index."),
                                                       new AttributeFormat("from", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Ending value of index."),
                                                       new AttributeFormat("step", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Ending value of index."),
                                                       new AttributeFormat("condition", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Condition that controls the loop."),
                                                       new AttributeFormat("query", AttributeFormat.STRING_TYPE, false,
                                                                           "Query that controls the loop."),
                                                       new AttributeFormat("startrow", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "First row of query that is included in the loop."),
                                                       new AttributeFormat("endrow", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Last row of query that is included in the loop."),
                                                       new AttributeFormat("list", AttributeFormat.STRING_TYPE, false,
                                                                           "A list, variable, or filename; contains a list"),
                                                       new AttributeFormat("delimiters", AttributeFormat.STRING_TYPE, false,
                                                                           "Character(s) that separates items in list"),
                                                       new AttributeFormat("collection", AttributeFormat.OBJECT_TYPE, false,
                                                                           "The collection attribute is used with the item attribute.often to loop over a structure"),
                                                       new AttributeFormat("array", AttributeFormat.OBJECT_TYPE, false, "An array"),
                                                       new AttributeFormat("item", AttributeFormat.STRING_TYPE, false,
                                                                           "The item attribute is used with the collection attribute.often to loop over a structure")}));
    myTagAttributes.put("cfmail",
                        new TagDescription(false, "Sends an e-mail message that optionally contains query output,using an SMTP server.",
                                           new AttributeFormat[]{new AttributeFormat("to", AttributeFormat.STRING_TYPE, true,
                                                                                     "Message recipient e-mail addresses."),
                                             new AttributeFormat("from", AttributeFormat.STRING_TYPE, true, "E-mail message sender:"),
                                             new AttributeFormat("cc", AttributeFormat.STRING_TYPE, false,
                                                                 "Address(es) to which to copy the message"),
                                             new AttributeFormat("bcc", AttributeFormat.STRING_TYPE, false,
                                                                 "Address(es) to which to copy the message, without listingthem in the message header."),
                                             new AttributeFormat("subject", AttributeFormat.STRING_TYPE, true,
                                                                 "Message subject. Can be dynamically generated."),
                                             new AttributeFormat("replyto", AttributeFormat.STRING_TYPE, false,
                                                                 "Address(es) to which the recipient is directed to sendreplies."),
                                             new AttributeFormat("failto", AttributeFormat.STRING_TYPE, false,
                                                                 "Address to which mailing systems should send deliveryfailure notifications. Sets the mail envelope reverse-pathvalue."),
                                             new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                 "A user name to send to SMTP servers that requireauthentication. Requires a password attribute"),
                                             new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                 "A password to send to SMTP servers that requireauthentication. Requires a username attribute."),
                                             new AttributeFormat("wraptext", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "Specifies the maximum line length, in characters of themail text. If a line has more than the specified number ofcharacters, replaces the last white space character, suchas a tab or space, preceding the specified position witha line break. If there are no white space characters,inserts a line break at the specified position. A commonvalue for this attribute is 72."),
                                             new AttributeFormat("charset", AttributeFormat.STRING_TYPE, false,
                                                                 "The character encoding in which the part text is encoded.For more information on character encodings, see:www.w3.org/International/O-charset.html."),
                                             new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                 "The MIME media type of the part. Can be a can be valid MIMEmedia type"),
                                             new AttributeFormat("mimeattach", AttributeFormat.STRING_TYPE, false,
                                                                 "Path of file to attach to message. Attached file isMIME-encoded. CFML attempts to determine the MIMEtype of the file; use the cfmailparam tag to send anattachement and specify the MIME type."),
                                             new AttributeFormat("query", AttributeFormat.STRING_TYPE, false,
                                                                 "Name of cfquery from which to draw data for message(s).Use this attribute to send more than one message, or tosend query results within a message."),
                                             new AttributeFormat("group", AttributeFormat.STRING_TYPE, false,
                                                                 "Query column to use when you group sets of records to sendas a message. For example, to send a set of billingstatements to a customer, group on \"Customer_ID.\"Case-sensitive. Eliminates adjacent duplicates when data issorted by the specified field."),
                                             new AttributeFormat("groupcasesensitive", AttributeFormat.BOOLEAN_TYPE, false,
                                                                 "Whether to consider case when using the group attribute. Togroup on case-sensitive records, set this attribute to Yes."),
                                             new AttributeFormat("startrow", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "Row in a query to start from."),
                                             new AttributeFormat("maxrows", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "Maximum number of messages to send when looping over aquery."),
                                             new AttributeFormat("server", AttributeFormat.STRING_TYPE, false,
                                                                 "SMTP server address, or (Enterprise edition only) acomma-delimited list of server addresses, to use forsending messages. At least one server must be specifiedhere or in the CFML MX Administrator. A value hereoverrides the Administrator. A value that includes a portspecification overrides the port attribute. See the Usagesection for details."),
                                             new AttributeFormat("port", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "TCP/IP port on which SMTP server listens for requests(normally 25). A value here overrides the Administrator."),
                                             new AttributeFormat("mailerid", AttributeFormat.STRING_TYPE, false,
                                                                 "Mailer ID to be passed in X-Mailer SMTP header, whichidentifies the mailer application."),
                                             new AttributeFormat("timeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "Number of seconds to wait before timing out connection toSMTP server. A value here overrides the Administrator."),
                                             new AttributeFormat("spoolenable", AttributeFormat.BOOLEAN_TYPE, false,
                                                                 "Specifies whether to spool mail or always send itImmediately. Overrides the CFML MX AdministratorSpool mail messages to disk for delivery setting."),
                                             new AttributeFormat("debug", AttributeFormat.BOOLEAN_TYPE, false,
                                                                 "* Yes: sends debugging output to standard output. Bydefault, if the console window is unavailable, ColdFusionsends output to cf_root\\runtime\\logs\\coldfusion-out.log onserver configurations. On J2EE configurations, with JRun,the default location is jrun_home/logs/servername-out.log.* No: does not generate debugging output.")}));
    myTagAttributes.put("cfmailparam", new TagDescription(true,
                                                          "Attaches a file or adds a header to an e-mail message. Can onlybe used in the cfmail tag. You can use more than onecfmailparam tag within a cfmail tag.",
                                                          new AttributeFormat[]{
                                                            new AttributeFormat("file", AttributeFormat.STRING_TYPE, false,
                                                                                "Attaches file to a message. Mutually exclusive with nameattribute. The file is MIME encoded before sending."),
                                                            new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                                "The MIME media type of the part. Can be a can be valid MIMEmedia type"),
                                                            new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                                "Name of header. Case-insensitive. Mutually exclusive withfile attribute.The values listed are from rfc2822"),
                                                            new AttributeFormat("value", AttributeFormat.STRING_TYPE, false,
                                                                                "Value of the header."),
                                                            new AttributeFormat("contentID", AttributeFormat.STRING_TYPE, false,
                                                                                "The Identifierfor the attached file. This ID shouldbe globally unique and is used to identify the file inan IMG or other tag in the mail body that referencesthe file content."),
                                                            new AttributeFormat("disposition", AttributeFormat.STRING_TYPE, false,
                                                                                "How the attached file is to be handled. Can be oneof the following:- attachment: present the file as an attachment- inline: display the file contents in the message")}));
    myTagAttributes.put("cfmailpart", new TagDescription(false,
                                                         "Specifies one part of a multipart e-mail message. Can only beused in the cfmail tag. You can use more than one cfmailparttag within a cfmail tag.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("type", AttributeFormat.STRING_TYPE, true,
                                                                               "The MIME media type of the part. Can be a can be valid MIMEmedia type"),
                                                           new AttributeFormat("wraptext", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "Specifies the maximum line length, in characters of themail text. If a line has more than the specified number ofcharacters, replaces the last white space character, suchas a tab or space, preceding the specified position with aline break. If there are no white space characters,inserts a line break at the specified position. A commonvalue for this attribute is 72."),
                                                           new AttributeFormat("charset", AttributeFormat.STRING_TYPE, false,
                                                                               "The character encoding in which the part text is encoded.For more information on character encodings, see:www.w3.org/International/O-charset.html.")}));
    myTagAttributes.put("cfmodule", new TagDescription(true,
                                                       "Invokes a custom tag for use in CFML application pages.This tag processes custom tag name conflicts.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("template", AttributeFormat.STRING_TYPE, false,
                                                                             "Mutually exclusive with the name attribute. A path to thepage that implements the tag.Relative path: expanded from the current pageAbsolute path: expanded using CFML mappingA physical path is not valid."),
                                                         new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                             "Mutually exclusive with the template attribute. A customtag name, in the form \"Name.Name.Name...\" Identifiessubdirectory, under the CFML tag root directory,that contains custom tag page. For example (Windows format):<cfmodule name = \"superduper.Forums40.GetUserOptions\">This identifies the page GetUserOptions.cfm in thedirectory CustomTags\\superduper\\Forums40 under theCFML root directory."),
                                                         new AttributeFormat("attributecollection", AttributeFormat.STRUCT_TYPE, false,
                                                                             "A collection of key-value pairs that representattribute names and values. You can specify multiplekey-value pairs. You can specify this attribute onlyonce."),
                                                         new AttributeFormat("attribute_name[1-9]([0-9])*", AttributeFormat.STRUCT_TYPE,
                                                                             false,
                                                                             "Attribute for a custom tag. You can include multiple instances of this attribute to specify the parameters of a custom tag.",
                                                                             "attribute_name"),
                                                         new AttributeFormat("[A-Za-z_][A-Za-z0-9_]*", AttributeFormat.STRUCT_TYPE, false,
                                                                             "Attribute for a custom tag. You can include multiple instances of this attribute to specify the parameters of a custom tag.",
                                                                             "attribute_name")}));
    myTagAttributes.put("cfntauthenticate", new TagDescription(true,
                                                               "Authenticates a user name and password against theNT domain on which ColdFusion server is running,and optionally retrieves the user's groups.",
                                                               new AttributeFormat[]{
                                                                 new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                     "User's login name."),
                                                                 new AttributeFormat("password", AttributeFormat.STRING_TYPE, true,
                                                                                     "User's login name."),
                                                                 new AttributeFormat("domain", AttributeFormat.STRING_TYPE, true,
                                                                                     "Domain against which to authenticate the user. TheColdFusion J2EE server must be running on this domain."),
                                                                 new AttributeFormat("result", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                                     "Name of the variable in which to return the results.Default: cfntauthenticate"),
                                                                 new AttributeFormat("listgroups", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                     "Boolean value specifying whether to Include acomma-delimited list of the user's groups in theresult structure.Default: false"),
                                                                 new AttributeFormat("throwonerror", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                     "Boolean value specifying whether to throw anexception if the validation fails. If this attribute is true,ColdFusion throws an error if the user name or password isinvalid; the application must handle such errors in atry/catch block or ColdFusion error handler page.Default: false")}));
    myTagAttributes.put("cfobject", new TagDescription(true,
                                                       "Creates a CFML object, of a specified type.The tag syntax depends on the object type. Some types use thetype attribute; others do not.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("type", AttributeFormat.STRING_TYPE, false, ""),
                                                         new AttributeFormat("action", AttributeFormat.STRING_TYPE, false, ""),
                                                         new AttributeFormat("class", AttributeFormat.STRING_TYPE, false, ""),
                                                         new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, true, ""),
                                                         new AttributeFormat("context", AttributeFormat.STRING_TYPE, false, ""),
                                                         new AttributeFormat("server", AttributeFormat.STRING_TYPE, false, ""),
                                                         new AttributeFormat("component", AttributeFormat.STRING_TYPE, false, ""),
                                                         new AttributeFormat("locale", AttributeFormat.STRING_TYPE, false, ""),
                                                         new AttributeFormat("webservice", AttributeFormat.URL_TYPE, false, "")}));
    myTagAttributes.put("cfobjectcache", new TagDescription(false, "Flushes the query cache.", new AttributeFormat[]{
      new AttributeFormat("action", AttributeFormat.STRING_TYPE, true, "clear: Clears queries from the cache in the Applicationscope")}));
    myTagAttributes.put("cfoutput", new TagDescription(false,
                                                       "Displays output that can contain the results of processingCFML variables and functions. Can loop over the resultsof a database query.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("query", AttributeFormat.STRING_TYPE, false,
                                                                             "Name of cfquery from which to draw data for output section."),
                                                         new AttributeFormat("group", AttributeFormat.STRING_TYPE, false,
                                                                             "Query column to use to group sets of records. Eliminatesadjacent duplicate rows when data is sorted. Use if youretrieved a record set ordered on one or more a querycolumns. For example, if a record set is ordered on\"Customer_ID\" in the cfquery tag, you can group the outputon \"Customer_ID.\""),
                                                         new AttributeFormat("groupcasesensitive", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "Whether to consider the case in grouping rows."),
                                                         new AttributeFormat("startrow", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Row from which to start output."),
                                                         new AttributeFormat("maxrows", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Maximum number of rows to display.")}));
    myTagAttributes.put("cfparam", new TagDescription(true,
                                                      "Tests for a parameter's existence, tests its data type, and, ifa default value is not assigned, optionally provides one.",
                                                      new AttributeFormat[]{
                                                        new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, true,
                                                                            "Name of parameter to test (such as \"client.email\" or\"cookie.backgroundColor\"). If omitted, and if theparameter does not exist, an error is thrown."),
                                                        new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                            "The valid format for the data; one of the following.* any: any type of value.* array: an array of values.* binary: a binary value.* boolean: a Boolean value: yes, no, true, false, or a number.* creditcard: a 13-16 digit number conforming to the mod10 algorithm.* date or time: a date-time value.* email: a valid e-mail address.* eurodate: a date-time value. Any date part must be in the format dd/mm/yy, The format can use /, -, or . characters as delimiters.* float or numeric: a numeric value.* guid: a Universally Unique Identifier of the form \"XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX\" where 'X' is a hexadecimal number.* numeric: a numeric value* integer: an integer.* query: a query object.* range: a numeric range, specified by the min and max attributes.* regex or regular_expression: matches input against pattern attribute.* ssn or social_security_number: a U.S. social security number.* string: a string value or single character.* struct: a structure.* telephone: a standard U.S. telephone number.* URL: an http, https, ftp, file, mailto, or news URL.* UUID: a ColdFusion Universally Unique Identifier, formatted 'XXXXXXXX-XXXX-XXXX-XXXXXXXXXXXXXXX', where 'X' is a hexadecimal number. See CreateUUID.* USdate: a U.S. date of the format mm/dd/yy, with 1-2 digit days and months, 1-4 digit years.* variableName: a string formatted according to ColdFusion variable naming conventions.* xml: XML objects and XML strings.* zipcode: U.S., 5- or 9-digit format ZIP codes."),
                                                        new AttributeFormat("default", AttributeFormat.OBJECT_TYPE, false,
                                                                            "Value to set parameter to if it does not exist. Anyexpression used for the default attribute is evaluated,even if the parameter exists.The result is not assigned if the parameter exists,but if the expression has side effects, they still occur."),
                                                        new AttributeFormat("max", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "The maximum valid value; used only for range validation."),
                                                        new AttributeFormat("min", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "The minimum valid value; used only for range validation."),
                                                        new AttributeFormat("pattern", AttributeFormat.STRING_TYPE, false,
                                                                            "A regular experession that the parameter must match;used only for regex or regular_expression validation.")}));
    myTagAttributes.put("cfpod", new TagDescription(false,
                                                    "Creates a pod, an area of the browser window or layoutarea with an optional title bar and body that containsdisplay elements.",
                                                    new AttributeFormat[]{
                                                      new AttributeFormat("bodyStyle", AttributeFormat.STRING_TYPE, false,
                                                                          "A CSS style specification for the pod body. As ageneral rule, use this attribute to set color andfont styles. Using this attribute to set the heightand width, for example, can result in distorted output."),
                                                      new AttributeFormat("headerStyle", AttributeFormat.STRING_TYPE, false,
                                                                          "A CSS style specification for the pod header. As ageneral rule, use this attribute to set color and fontstyles. Using this attribute to set the height and width,for example, can result in distorted output."),
                                                      new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                          "Height if the control, including the title bar and borders,in pixels.  Default is 100."),
                                                      new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                          "Name of the pod control."),
                                                      new AttributeFormat("onBindError", AttributeFormat.STRING_TYPE, false,
                                                                          "The name of a JavaScript function to execute ifevaluating a bind expression results in an error.The function must take two attributes: an HTTPstatus code and a message. If you omit thisattribute, and have specified a global error handler(by using the ColdFusion.setGlobalErrorHandlerfunction), it displays the error message; otherwisea default error pop-up displays."),
                                                      new AttributeFormat("overflow", AttributeFormat.STRING_TYPE, false,
                                                                          "Specifies how to display child content whose sizewould cause the control to overflow the podboundaries.  Note: In Internet Explorer, podswith the visible setting expand to fit the size ofthe contents, rather than having the contents extendbeyond the layout area."),
                                                      new AttributeFormat("source", AttributeFormat.STRING_TYPE, false,
                                                                          "A URL that returns the pod contents. ColdFusionuses standard page path resolution rules. If youspecify this attribute and the cfpod tag has a body,ColdFusion ignores the body contents. You can use a abind expression with dependencies in thisattribute; for more information see the Usage section.Note: If a CFML page specified in this attribute containstags that use AJAX features, such as cfform, cfgrid,and cfwindow, you must use a cfajaximport tag on thepage with the cfpod tag.For more information, see cfajaximport."),
                                                      new AttributeFormat("title", AttributeFormat.STRING_TYPE, false,
                                                                          "Text to display in the podпїЅs title bar. You can useHTML mark up to control the title appearance, ofexample to show the text in red italic font.If you omit this attribute, the pod does nothave a title bar."),
                                                      new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                          "Width if the control, including the title bar andborders, in pixels. Default is 500.")}));
    myTagAttributes.put("cfpop", new TagDescription(true, "Retrieves or deletes e-mail messages from a POP mail server.",
                                                    new AttributeFormat[]{new AttributeFormat("server", AttributeFormat.STRING_TYPE, true,
                                                                                              "POP server identifier:A host name; for example, \"biff.upperlip.com\"An IP address; for example, \"192.1.2.225\""),
                                                      new AttributeFormat("port", AttributeFormat.NUMERIC_TYPE, false, "POP port"),
                                                      new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                          "Overrides username."),
                                                      new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                          "Overrides password"),
                                                      new AttributeFormat("action", AttributeFormat.STRING_TYPE, false,
                                                                          "getHeaderOnly: returns message header information onlygetAll: returns message header information, message text,and attachments if attachmentPath is specifieddelete: deletes messages on POP server"),
                                                      new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                          "Name for query object that contains the retrieved messageinformation."),
                                                      new AttributeFormat("messagenumber", AttributeFormat.NUMERIC_TYPE, false,
                                                                          "Message number or comma-delimited list of message numbersto get or delete. Invalid message numbers are ignored.Ignored if uid is specified."),
                                                      new AttributeFormat("uid", AttributeFormat.STRING_TYPE, false,
                                                                          "UID or a comma-delimited list of UIDs to get or delete.Invalid UIDs are ignored."),
                                                      new AttributeFormat("attachmentpath", AttributeFormat.STRING_TYPE, false,
                                                                          "If action=\"getAll\", specifies a directory in which to saveany attachments. If the directory does not exist,CFML creates it.If you omit this attribute, CFML does not save anyattachments. If you specify a relative path, the path rootis the CFML temporary directory, which is returned bythe GetTempDirectory function."),
                                                      new AttributeFormat("timeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                          "Maximum time, in seconds, to wait for mail processing"),
                                                      new AttributeFormat("maxrows", AttributeFormat.NUMERIC_TYPE, false,
                                                                          "Number of messages to return or delete, starting with thenumber in startRow. Ignored if messageNumber or uid isspecified."),
                                                      new AttributeFormat("startrow", AttributeFormat.NUMERIC_TYPE, false,
                                                                          "First row number to get or delete. Ignored if messageNumberor uid is specified."),
                                                      new AttributeFormat("generateuniquefilenames", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "Yes: Generate unique filenames for files attached to ane-mail message, to avoid naming conflicts when files aresaved")}));
    myTagAttributes.put("cfprocessingdirective", new TagDescription(false,
                                                                    "Provides the following insformation to CFML on how toprocess the current page:* Specifies whether to remove excess whitespace character fromCFML generated content in the tag body.* Identifies the character encoding (character set) of the pagecontents.",
                                                                    new AttributeFormat[]{new AttributeFormat("suppresswhitespace",
                                                                                                              AttributeFormat.BOOLEAN_TYPE,
                                                                                                              false,
                                                                                                              "Boolean; whether to suppress white space characters withinthe cfprocessingdirective block that are generated by CFMLtags and often do not affect HTML appearance. Does notaffect any white space in HTML code"),
                                                                      new AttributeFormat("pageencoding", AttributeFormat.STRING_TYPE,
                                                                                          false,
                                                                                          "A string literal; cannot be a variable. Identifies thecharacter encoding of the current CFML page. This attributeaffects the entire page, not just the cfprocessing tag body.The value may be enclosed in single or double quotationmarks, or none.For more information on character encodings, see:www.w3.org/International/O-charset.html.")}));
    myTagAttributes.put("cfprocparam",
                        new TagDescription(true, "Defines stored procedure parameters. This tag is nested withina cfstoredproc tag.",
                                           new AttributeFormat[]{new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                                     "in: The parameter is used to send data to the databasesystem only. Passes the parameter by value.out: The parameter is used to receive data from thedatabase system only. Passes the parameter as a boundvariable.inout: The parameter is used to send and receive data.Passes the parameter as a bound variable."),
                                             new AttributeFormat("variable", AttributeFormat.STRING_TYPE, false,
                                                                 "CFML variable name; references the value that theoutput parameter has after the stored procedure is called."),
                                             new AttributeFormat("value", AttributeFormat.STRING_TYPE, false,
                                                                 "Value that CFML passes to the stored procedure."),
                                             new AttributeFormat("cfsqltype", AttributeFormat.STRING_TYPE, true,
                                                                 "SQL type to which the parameter (any type) is bound.CFML supports the following values, where the lastelement of the name corresponds to the SQL data type.Different database systems might support different subsetsof this list. See your DBMS documentation for informationon supported parameter types."),
                                             new AttributeFormat("maxlength", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "Maximum length of a string or character IN or INOUT valueattribute. A maxLength of 0 allows any length. ThemaxLength attribute is not required when specifyingtype=out."),
                                             new AttributeFormat("scale", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "Number of decimal places in numeric parameter. A scale of 0allows any number of decimal places."),
                                             new AttributeFormat("null", AttributeFormat.BOOLEAN_TYPE, false,
                                                                 "Whether the parameter is passed in as a null value. Notused with OUT type parameters."),
                                             new AttributeFormat("dbvarname", AttributeFormat.STRING_TYPE, false,
                                                                 "Stored procedure variable name.  Directly matches to the nameof the procedure parameter.")}));
    myTagAttributes.put("cfprocresult", new TagDescription(true,
                                                           "Associates a query object with a result set returned by astored procedure. Other CFML tags, such as cfoutput andcftable, use this query object to access the result set. Thistag is nested within a cfstoredproc tag.",
                                                           new AttributeFormat[]{
                                                             new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, true,
                                                                                 "Name for the query result set."),
                                                             new AttributeFormat("resultset", AttributeFormat.NUMERIC_TYPE, false,
                                                                                 "Names one result set, if stored procedure returns more thanone."),
                                                             new AttributeFormat("maxrows", AttributeFormat.NUMERIC_TYPE, false,
                                                                                 "Maximum number of rows returned in result set.")}));
    myTagAttributes.put("cfproperty", new TagDescription(true,
                                                         "Defines properties of a CFML component (CFC). Used tocreate complex data types for web services. The attributes ofthis tag are exposed as component metadata and are subject toinheritance rules.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                               "A string; a property name. Must be a static value."),
                                                           new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                               "A string; identifies the property data type"),
                                                           new AttributeFormat("required", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Whether the parameter is required"),
                                                           new AttributeFormat("default", AttributeFormat.OBJECT_TYPE, false,
                                                                               "If no property value is set when the component is used fora web service, specifies a default value.If this attribute is present, the required attribute mustbe set to \"no\" or not specified."),
                                                           new AttributeFormat("displayname", AttributeFormat.STRING_TYPE, false,
                                                                               "A value to be displayed when using introspection to showinformation about the CFC. The value appears in parenthesesfollowing the property name."),
                                                           new AttributeFormat("hint", AttributeFormat.STRING_TYPE, false,
                                                                               "Text to be displayed when using introspection to showinformation about the CFC. This attribute can be usefulfor describing the purpose of the parameter.")}));
    myTagAttributes.put("cfquery", new TagDescription(false,
                                                      "Passes queries or SQL statements to a data source.It is recommended that you use the cfqueryparam tag withinevery cfquery tag, to help secure your databases fromunauthorized users",
                                                      new AttributeFormat[]{
                                                        new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                            "Name of query. Used in page to reference query record set.Must begin with a letter. Can include letters, numbers,and underscores."),
                                                        new AttributeFormat("datasource", AttributeFormat.STRING_TYPE, false,
                                                                            "Name of data source from which query gets data."),
                                                        new AttributeFormat("dbtype", AttributeFormat.STRING_TYPE, false,
                                                                            "query. Use this value to specify the results of a query asinput."),
                                                        new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                            "Overrides username in data source setup."),
                                                        new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                            "Overrides password in data source setup."),
                                                        new AttributeFormat("maxrows", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Maximum number of rows to return in record set.-1 returns all records."),
                                                        new AttributeFormat("blockfactor", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Maximum rows to get at a time from server. Range: 1 - 100.Might not be supported by some database systems."),
                                                        new AttributeFormat("timeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Maximum number of seconds that each action of a query ispermitted to execute before returning an error. Thecumulative time may exceed this value.For JDBC statements, CFML sets this attribute. Forother drivers, check driver documentation."),
                                                        new AttributeFormat("cachedafter", AttributeFormat.DATETIME_TYPE, false,
                                                                            "Date value (for example, April 16, 1999, 4-16-99). If dateof original query is after this date, CFML usescached query data. To use cached data, current query mustuse same SQL statement, data source, query name, user name,password.A date/time object is in the range 100 AD-9999 AD.When specifying a date value as a string, you must encloseit in quotation marks."),
                                                        new AttributeFormat("cachedwithin", AttributeFormat.TIMESPAN_TYPE, false,
                                                                            "Timespan, using the CreateTimeSpan function. If originalquery date falls within the time span, cached query data isused. CreateTimeSpan defines a period from the present,back. Takes effect only if query caching is enabled in theAdministrator.To use cached data, the current query must use the same SQLstatement, data source, query name, user name, and password."),
                                                        new AttributeFormat("debug", AttributeFormat.BOOLEAN_TYPE, false,
                                                                            "Yes: If debugging is enabled, but the AdministratorDatabase Activity option is not enabled, displays SQLsubmitted to datasource and number of records returnedby query.No: If the Administrator Database Activity option isenabled, suppresses display."),
                                                        new AttributeFormat("result", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                            "Specifies a name for the structure in which cfquery returnsthe result variables.* SQL: The SQL statement that was executed. (string)* Cached: If the query was cached. (boolean)* SqlParameters: An ordered Array of cfqueryparam values. (array)* RecordCount: Total number of records in the query. (numeric)* ColumnList: Column list, comma seperated. (numeric)* ExecutionTime: Execution time for the SQL request. (numeric)")}));
    myTagAttributes.put("cfqueryparam", new TagDescription(true,
                                                           "Verifies the data type of a query parameter and, for DBMSs thatsupport bind variables, enables CFML to use bind variablesin the SQL statement. Bind variable usage enhances performancewhen executing a cfquery statement multiple times.This tag is nested within a cfquery tag, embedded in a query SQLstatement. If you specify optional parameters, this tag performsdata validation.",
                                                           new AttributeFormat[]{
                                                             new AttributeFormat("value", AttributeFormat.STRING_TYPE, true,
                                                                                 "Value that CFML passes to the right of the comparisonoperator in a where clause.If CFSQLType is a date or time option, ensure that the datevalue uses your DBMS-specific date format. Use theCreateODBCDateTime or DateFormat and TimeFormat functionsto format the date value."),
                                                             new AttributeFormat("cfsqltype", AttributeFormat.STRING_TYPE, false,
                                                                                 "SQL type that parameter (any type) is bound to."),
                                                             new AttributeFormat("maxlength", AttributeFormat.NUMERIC_TYPE, false,
                                                                                 "Maximum length of parameter."),
                                                             new AttributeFormat("scale", AttributeFormat.NUMERIC_TYPE, false,
                                                                                 "Number of decimal places in parameter. Applies toCF_SQL_NUMERIC and CF_SQL_DECIMAL."),
                                                             new AttributeFormat("null", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "Whether parameter is passed as a null value.Yes: tag ignores the value attributeNo: does not"),
                                                             new AttributeFormat("list", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "Yes: The value attribute value is a delimited listNo: it is not"),
                                                             new AttributeFormat("separator", AttributeFormat.CHAR_TYPE, false,
                                                                                 "Character that separates values in list, in value attribute.")}));
    myTagAttributes.put("cfregistry", new TagDescription(true,
                                                         "Reads, writes, and deletes keys and values in the system registry.Provides persistent storage of client variables.Deprecated for the UNIX platform.Note: For this tag to execute, it must be enabled in the ColdFusion MXAdministrator.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("action", AttributeFormat.STRING_TYPE, true, ""),
                                                           new AttributeFormat("branch", AttributeFormat.STRING_TYPE, true,
                                                                               "Name of a registry branch."),
                                                           new AttributeFormat("entry", AttributeFormat.STRING_TYPE, false,
                                                                               "Registry value to access.Note: For key deletion this attribute is required."),
                                                           new AttributeFormat("variable", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                               "Variable into which to put value."),
                                                           new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                               "* string: returns string values.* dWord: returns DWord values.* key: returns keys.* any: returns keys and values."),
                                                           new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                               "Name of record set to contain returned keys and values."),
                                                           new AttributeFormat("sort", AttributeFormat.STRING_TYPE, false,
                                                                               "Sorts query column data (case-insensitive). Sorts on Entry, Type,and Value columns as text. Specify a combination of columns fromquery output, in a comma-delimited list.For example: sort = \"value desc, entry asc\"* asc: ascending (a to z) sort order.* desc: descending (z to a) sort order."),
                                                           new AttributeFormat("directory", AttributeFormat.STRING_TYPE, false,
                                                                               "Absolute pathname of directory against which to performaction."),
                                                           new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                               "Name for output record set."),
                                                           new AttributeFormat("filter", AttributeFormat.STRING_TYPE, true,
                                                                               "File extension filter applied to returned names. Forexample: *.cfm. One filter can be applied."),
                                                           new AttributeFormat("mode", AttributeFormat.STRING_TYPE, false,
                                                                               "Applies only to UNIX and Linux. Permissions. Octal valuesof Unix chmod command. Assigned to owner, group, andother, respectively."),
                                                           new AttributeFormat("sort", AttributeFormat.STRING_TYPE, false,
                                                                               "Query column(s) by which to sort directory listing.Delimited list of columns from query output."),
                                                           new AttributeFormat("newdirectory", AttributeFormat.STRING_TYPE, false,
                                                                               "New name for directory."),
                                                           new AttributeFormat("recurse", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Whether ColdFusion performs the action on subdirectories.")}));
    myTagAttributes.put("cfreport", new TagDescription(false,
                                                       "Used to do either of the following:- Execute a report definition created with the ColdFusionReport Builder, displaying it in PDF, FlashPaper, or Excelformat. You can optionally save this report to a file.- Run a predefined Crystal Reports report. Applies only toWindows systems. Uses the CFCRYSTAL.exe file to generatereports. Sets parameters in the Crystal Reports engineaccording to its attribute values.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("template", AttributeFormat.STRING_TYPE, true,
                                                                             "Specifies the path to the report definition file,relative to the web root."),
                                                         new AttributeFormat("format", AttributeFormat.STRING_TYPE, true,
                                                                             "Specifies the output format."),
                                                         new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                             "The name of the ColdFusion variable that will holdthe report output. You cannot specify both name andfilename."),
                                                         new AttributeFormat("filename", AttributeFormat.STRING_TYPE, false,
                                                                             "The filename to contain the report. You cannotspecify both name and filename."),
                                                         new AttributeFormat("query", AttributeFormat.STRING_TYPE, false,
                                                                             "The name of the query that contains input data forthe report. If you omit this parameter, the reportdefinition obtains data from the internal SQL or fromcfreportparam items."),
                                                         new AttributeFormat("overwrite", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "Specifies whether to overwrite files that have thesame name as that specified in the filename attribute.Default: false"),
                                                         new AttributeFormat("encryption", AttributeFormat.STRING_TYPE, false,
                                                                             "Specifies whether the output is encrypted. PDF format only.Default: none"),
                                                         new AttributeFormat("ownerpassword", AttributeFormat.STRING_TYPE, false,
                                                                             "Specifies an owner password. PDF format only."),
                                                         new AttributeFormat("userpassword", AttributeFormat.STRING_TYPE, false,
                                                                             "Specifies a user password. PDF format only."),
                                                         new AttributeFormat("permissions", AttributeFormat.STRING_TYPE, false,
                                                                             "Specifies one or more permissions. PDF format only.Separate multiple permissions with a comma."),
                                                         new AttributeFormat("datasource", AttributeFormat.STRING_TYPE, false,
                                                                             "Name of registered or native data source."),
                                                         new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                             "* standard (not valid for Crystal Reports 8.0)* netscape* microsoft"),
                                                         new AttributeFormat("timeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Maximum time, in seconds, in which a connection must bemade to a Crystal Report."),
                                                         new AttributeFormat("report", AttributeFormat.STRING_TYPE, true,
                                                                             "Report path. Store Crystal Reports files in the samedirectories as CFML page files."),
                                                         new AttributeFormat("orderby", AttributeFormat.STRING_TYPE, false,
                                                                             "Orders results according to your specifications."),
                                                         new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                             "Username required for entry into database from which reportis created. Overrides default settings for data source inCFML Administrator."),
                                                         new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                             "Password that corresponds to username required for databaseaccess. Overrides default settings for data source inCFML Administrator."),
                                                         new AttributeFormat("formula", AttributeFormat.STRING_TYPE, false,
                                                                             "One or more named formulas. Terminate each formula with asemicolon. Use the format:formula = \"formulaname1='formula1';formulaname2='formula2';\"If you use a semicolon in a formula, you must escape it bytyping it twice (;;). For example:formula = \"Name1 = 'Val_1a;;Val_1b';Name2 = 'Val2';\"")}));
    myTagAttributes.put("cfreportparam", new TagDescription(true,
                                                            "Passes input parameters to a ColdFusion Report Builderreport definition. Allowed inside cfreport tag bodies only.",
                                                            new AttributeFormat[]{
                                                              new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                  "Variable name for data that is passed. The ColdFusionReport Builder report definition must include an inputparameter that matches this name."),
                                                              new AttributeFormat("value", AttributeFormat.STRING_TYPE, true,
                                                                                  "Value of the data that is sent.")}));
    myTagAttributes.put("cfrethrow", new TagDescription(true,
                                                        "Rethrows the currently active exception. Preserves theexception's cfcatch.type and cfcatch.tagContext variablevalues.",
                                                        myEmptyAttributesArray));
    myTagAttributes.put("cfreturn", new TagDescription(true,
                                                       "Returns result values from a component method. Contains anexpression returned as result of the function.An expression; the result of the function from which this tagis called.",
                                                       myEmptyAttributesArray));
    myTagAttributes.put("cfsavecontent", new TagDescription(false,
                                                            "Saves the generated content of the cfsavecontent tag, includingthe results of evaluating expressions and executing custom tags,in the specified variable.",
                                                            new AttributeFormat[]{
                                                              new AttributeFormat("variable", AttributeFormat.VARIABLENAME_TYPE, true,
                                                                                  "Name ofthe variable in which to save the generated contentof the tag.")}));
    myTagAttributes.put("cfschedule", new TagDescription(true,
                                                         "Provides a programmatic interface to the CFML schedulingengine. Can run a CFML page at scheduled intervals, with theoption to write the page output to a static HTML page. Thisfeature enables you to schedule pages that publish data, suchas reports, without waiting while a database transaction isperformed to populate the page.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("action", AttributeFormat.STRING_TYPE, true,
                                                                               "delete: deletes the specified taskupdate: updates an existing task or creates a new task,if one with the name specified by thetask attributedoes not existrun: executes the specified task"),
                                                           new AttributeFormat("task", AttributeFormat.STRING_TYPE, true,
                                                                               "Name of the task."),
                                                           new AttributeFormat("operation", AttributeFormat.STRING_TYPE, false,
                                                                               "Operation that the scheduler performs. Must be HTTPRequest."),
                                                           new AttributeFormat("file", AttributeFormat.STRING_TYPE, false,
                                                                               "Name of the file in which to store the published output ofthe scheuled task"),
                                                           new AttributeFormat("path", AttributeFormat.STRING_TYPE, false,
                                                                               "Path to the directory in which to put the published file."),
                                                           new AttributeFormat("startdate", AttributeFormat.STRING_TYPE, false,
                                                                               "Date on which to first run the scheuled task."),
                                                           new AttributeFormat("starttime", AttributeFormat.STRING_TYPE, false,
                                                                               "Time at which to run the scheduled of task starts."),
                                                           new AttributeFormat("URL", AttributeFormat.URL_TYPE, false,
                                                                               "URL of the page to execute."),
                                                           new AttributeFormat("port", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "Port to use on the server that is specified by the urlparameter. If resolveURL = \"yes\", retrieved document URLsthat specify a port number are automatically resolved, topreserve links in the retrieved document. A port value inthe url attribute overrides this value."),
                                                           new AttributeFormat("publish", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Yes: save the result to a fileNo: does no"),
                                                           new AttributeFormat("enddate", AttributeFormat.STRING_TYPE, false,
                                                                               "Date when scheduled task ends."),
                                                           new AttributeFormat("endtime", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "Time when scheduled task ends (seconds)."),
                                                           new AttributeFormat("interval", AttributeFormat.STRING_TYPE, false,
                                                                               "Interval at which task is scheduled.* number of seconds (minimum is 60)* once* daily* weekly* monthly"),
                                                           new AttributeFormat("requesttimeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "Can be used to extend the default timeout period."),
                                                           new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                               "Username, if URL is protected."),
                                                           new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                               "Password, if URL is protected."),
                                                           new AttributeFormat("proxyserver", AttributeFormat.STRING_TYPE, false,
                                                                               "Host name or IP address of a proxy server."),
                                                           new AttributeFormat("proxyport", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "Port number to use on the proxy server."),
                                                           new AttributeFormat("proxyuser", AttributeFormat.STRING_TYPE, false,
                                                                               "User name to provide to the proxy server."),
                                                           new AttributeFormat("proxypassword", AttributeFormat.STRING_TYPE, false,
                                                                               "Password to provide to the proxy server."),
                                                           new AttributeFormat("resolveurl", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Yes: resolveFunction links in the output page to absolutereferencesNo: does not")}));
    myTagAttributes.put("cfscript", new TagDescription(false,
                                                       "Encloses a code block that contains cfscript statements.You cannot use some CFML reserved words in this tag. Youcannot put a user-defined function whose name begins with anyof these strings within this tag:cf,cf_,_cf,CFML,CFML_,_CFML",
                                                       myEmptyAttributesArray));
    myTagAttributes.put("cfsearch", new TagDescription(true,
                                                       "Searches Verity collections using CFML or K2Server,whichever search engine a collection is registered by.(CFML can also search collections that have not beenregistered, with the cfcollection tag.)A collection must be created and indexed before this tag canreturn search results.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("name", AttributeFormat.VARIABLENAME_TYPE, true,
                                                                             "Name of the search query."),
                                                         new AttributeFormat("collection", AttributeFormat.STRING_TYPE, true,
                                                                             "One or more collection names. You can specify morethan one collection unless you are performing acategory search (that is, specifying category orcategoryTree).One or more collection names. You can specify morethan one collection unless you are performing acategory search (that is, specifying category orcategoryTree)."),
                                                         new AttributeFormat("category", AttributeFormat.STRING_TYPE, false,
                                                                             "A list of categories, separated by commas, to whichthe search is limited. If specified, and the collectiondoes not have categories enabled, ColdFusionthrows an exception."),
                                                         new AttributeFormat("categorytree", AttributeFormat.STRING_TYPE, false,
                                                                             "The location in a hierarchical category tree at whichto start the search. ColdFusion searches at andbelow this level. If specified, and the collection doesnot have categories enabled, ColdFusion throws anexception. Can be used in addition to categoryattribute."),
                                                         new AttributeFormat("status", AttributeFormat.STRING_TYPE, false,
                                                                             "Specifies the name of the structure variable intowhich ColdFusion places search information, includingalternative criteria suggestions (spelling corrections)."),
                                                         new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                             "Used to specify the parser that Verity uses to processthe criteria.- simple: STEM and MANY operators are implicitly used.- explicit: operators must be invoked explicitly. Alsoknown as Bool_Plus)- internet: for documents that are mostly WYSIWIG(what-you-see-is-what-you-get) documents. Alsoknown as Internet_advanced.- internet_basic: minimizes search time.- natural: specifies the Query By Example (QBE)parser. Also known as FreeText."),
                                                         new AttributeFormat("criteria", AttributeFormat.STRING_TYPE, false,
                                                                             "Search criteria. Follows the syntax rules of the typeattribute. If you pass a mixed-case entry in this attribute,the search is case-sensitive. If you pass all uppercase orall lowercase, the search is case-insensitive. FollowVerity syntax and delimiter character rules; see UsingVerity Search Expressions in Developing CFML MXApplications."),
                                                         new AttributeFormat("maxrows", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Maximum number of rows to return in query results.Default: all"),
                                                         new AttributeFormat("startrow", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "First row number to get.Default: 1"),
                                                         new AttributeFormat("suggestions", AttributeFormat.STRING_TYPE, false,
                                                                             "Specifies whether Verity returns spelling suggestionsfor possibly misspelled words. Use one of thefollowing options:- always: Verity always returns spelling suggestions.- never: Verity never returns spelling suggestions.- positive integer: Verity returns spelling suggestionsif the number of documents retrieved by the searchis less than or equal to the number specified.There is a small performance penalty for retrievingsuggestion data.Default: never"),
                                                         new AttributeFormat("contextpassages", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "The number of passages/sentences Verity returns inthe context summary (that is, the context column ofthe results).Default: 3"),
                                                         new AttributeFormat("contextbytes", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "The maximum number of bytes Verity returns in thecontext summary.Default: 300"),
                                                         new AttributeFormat("contexthighlightbegin", AttributeFormat.STRING_TYPE, false,
                                                                             "The HTML to prepend to search terms in the contextsummary. Use this attribute in conjunction withcontextHighlightEnd to highlight search terms in thecontext summary.Default: <b>"),
                                                         new AttributeFormat("contexthighlightend", AttributeFormat.STRING_TYPE, false,
                                                                             "The HTML to prepend to search terms in the contextsummary. Use this attribute in conjunction withcontextHighlightEnd to highlight search terms in thecontext summary.Default: </b>"),
                                                         new AttributeFormat("previouscriteria", AttributeFormat.STRING_TYPE, false,
                                                                             "The name of a result set from an existing set of searchresults. Verity searches the result set for criteriawithout regard to the previous search score or rank.Use this attribute to implement searching within resultsets.")}));
    myTagAttributes.put("cfselect", new TagDescription(false,
                                                       "Constructs a drop-down list box form control. Used within acfform tag.You can populate the list from a query, or by using the HTMLoption tag.",
                                                       new AttributeFormat[]{new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                                 "Name of the select form element"),
                                                         new AttributeFormat("id", AttributeFormat.STRING_TYPE, false,
                                                                             "ID for form input element."),
                                                         new AttributeFormat("bind", AttributeFormat.STRING_TYPE, false,
                                                                             "A bind expression that dynamically sets an attributeof the control."),
                                                         new AttributeFormat("bindAttribute", AttributeFormat.STRING_TYPE, false,
                                                                             "Specifies the HTML tag attribute whose value is setby the bind attribute. You can only specify attributesin the browserвЂ™s HTML DOM tree, not ColdFusion-specific attributes.Ignored if there is no bind attribute."),
                                                         new AttributeFormat("bindOnLoad", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "A Boolean value that specifies whether to executethe bind attribute expression when first loading theform. Ignored if there is no bind attribute."),
                                                         new AttributeFormat("editable", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "Boolean value specifying whether you can edit thecontents of the control."),
                                                         new AttributeFormat("label", AttributeFormat.STRING_TYPE, false,
                                                                             "Label to put next to the control on a Flash or XML-format form."),
                                                         new AttributeFormat("style", AttributeFormat.STRING_TYPE, false,
                                                                             "In HTML or XML format forms, ColdFusion passes thestyle attribute to the browser or XML.In Flash format, must be a style specification in CSSformat, with the same syntax and contents as used inMacromedia Flex for the corresponding Flash element.Post alpha we will document specifics."),
                                                         new AttributeFormat("sourceForTooltip", AttributeFormat.STRING_TYPE, false,
                                                                             "The URL of a page to display as a tool tip. The pagecan include CFML and HTML markup to control thetip contents and format, and the tip can includeimages.If you specify this attribute, an animated iconappears with the text \"Loading...\" while the tip isbeing loaded."),
                                                         new AttributeFormat("size", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Number of entries to display at one time. The default, 1,displays a drop-down list. Any other value displays a listbox with size number of entries visible at one time."),
                                                         new AttributeFormat("required", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "If true a list element must be selected when form is submitted.Note: This attribute has no effect if you omit the sizeattribute or set it to 1 because the browser always submitsthe displayed item. You can work around this issue formatforms by having an initial option tag with value=\" \" (note thespace character between the quotation marks).Default: false"),
                                                         new AttributeFormat("message", AttributeFormat.STRING_TYPE, false,
                                                                             "Message to display if required=\"true\" and no selection is made."),
                                                         new AttributeFormat("onerror", AttributeFormat.STRING_TYPE, false,
                                                                             "Custom JavaScript function to execute if validation fails."),
                                                         new AttributeFormat("multiple", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "- true: allow selecting multiple elements in drop-down list- false: don't allow selecting multiple elementsDefault: false"),
                                                         new AttributeFormat("query", AttributeFormat.STRING_TYPE, false,
                                                                             "Name of query to populate drop-down list."),
                                                         new AttributeFormat("value", AttributeFormat.STRING_TYPE, false,
                                                                             "Query column to use for the value of each list element.Used with query attribute."),
                                                         new AttributeFormat("display", AttributeFormat.STRING_TYPE, false,
                                                                             "Query column to use for the display label of each listelement. Used with query attribute."),
                                                         new AttributeFormat("group", AttributeFormat.STRING_TYPE, false,
                                                                             "Query column to use to group the items in the drop-downlist into a two-level hierarchical list.."),
                                                         new AttributeFormat("queryposition", AttributeFormat.STRING_TYPE, false,
                                                                             "If you populate the options list with a query and use HTMLoption child tags to specify additional entries, determinesthe location of the items from the query relative to the itemsfrom the option tags:- above: Put the query items above the options items.- below: Put the query items below the options items.Default: above"),
                                                         new AttributeFormat("selected", AttributeFormat.STRING_TYPE, false,
                                                                             "One or more option values to preselect in the selection list.To specify multiple values, use a comma-delimited list. Thisattribute applies only if selection list items are generatedfrom a query. The cfform preservedata attribute value canoverride this value."),
                                                         new AttributeFormat("onerror", AttributeFormat.STRING_TYPE, false,
                                                                             "Custom JavaScript function to execute if validationfails."),
                                                         new AttributeFormat("onkeyup", AttributeFormat.STRING_TYPE, false,
                                                                             "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the user releases a keyboard key in the control."),
                                                         new AttributeFormat("onkeydown", AttributeFormat.STRING_TYPE, false,
                                                                             "JavaScript (HTML/XML) or ActionScript (Flash)ActionScript to run when the user depresses a keyboardkey in the control."),
                                                         new AttributeFormat("onmouseup", AttributeFormat.STRING_TYPE, false,
                                                                             "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the user presses a mouse button in the control."),
                                                         new AttributeFormat("onmousedown", AttributeFormat.STRING_TYPE, false,
                                                                             "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the user releases a mouse button in the control."),
                                                         new AttributeFormat("onchange", AttributeFormat.STRING_TYPE, false,
                                                                             "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the control changes due to user action."),
                                                         new AttributeFormat("onclick", AttributeFormat.STRING_TYPE, false,
                                                                             "JavaScript to run when the user clicks the control."),
                                                         new AttributeFormat("enabled", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "Flash only: Boolean value specifying whether to show the control.Space that would be occupied by an invisible control isblank.Default: true"),
                                                         new AttributeFormat("visible", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "Flash only: Boolean value specifying whether to show the control.Space that would be occupied by an invisible control isblank.Default: true"),
                                                         new AttributeFormat("tooltip", AttributeFormat.STRING_TYPE, false,
                                                                             "Flash only: Text to display when the mouse pointer hovers over the control."),
                                                         new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "The height of the control, in pixels."),
                                                         new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "The width of the control, in pixels."),
                                                         new AttributeFormat("passthrough", AttributeFormat.STRING_TYPE, false,
                                                                             "This attribute is deprecated.Passes arbitrary attribute-value pairs to the HTML codethat is generated for the tag. You can use either of thefollowing formats:passthrough=\"title=\"\"myTitle\"\"\"passthrough='title=\"mytitle\"'")}));
    myTagAttributes.put("cfset", new TagDescription(true,
                                                    "Sets a value in CFML. Used to create a variable, if itdoes not exist, and assign it a value. Also used to callfunctions.",
                                                    myEmptyAttributesArray));
    myTagAttributes.put("cfsetting",
                        new TagDescription(true, "Controls aspects of page processing, such as the output ofHTML code in pages.",
                                           new AttributeFormat[]{
                                             new AttributeFormat("enablecfoutputonly", AttributeFormat.BOOLEAN_TYPE, false,
                                                                 "Yes: blocks output of HTML that is outside cfoutput tagsNo: displays HTML that is outside cfoutput tags."),
                                             new AttributeFormat("showdebugoutput", AttributeFormat.BOOLEAN_TYPE, false,
                                                                 "Yes: If debugging is enabled in the Administrator, displaysdebugging informationNo: suppresses debugging information that would otherwisedisplay at end of generated page."),
                                             new AttributeFormat("requesttimeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                 "Integer; number of seconds. Time limit, after whichCFML processes the page as an unresponsive thread.Overrides the timeout set in the CFML Administrator.")}));
    myTagAttributes
      .put("cfsilent", new TagDescription(false, "Suppresses output produced by CFML within a tag's scope.", myEmptyAttributesArray));
    myTagAttributes.put("cfslider", new TagDescription(true,
                                                       "Puts a slider control, for selecting a numeric value from arange, in a ColdFusion form. The slider moves over the slidergroove. As the user moves the slider, the current valuedisplays. Used within a cfform tag.Not supported with Flash forms.",
                                                       new AttributeFormat[]{new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                                 "Name for cfslider control."),
                                                         new AttributeFormat("label", AttributeFormat.STRING_TYPE, false,
                                                                             "Label to display with control.For example, \"Volume\" This displays: \"Volume %value%\"To reference the value, use \"%value%\". If %% is omitted,slider value displays directly after label."),
                                                         new AttributeFormat("range", AttributeFormat.STRING_TYPE, false,
                                                                             "Numeric slider range values.Separate values with a comma."),
                                                         new AttributeFormat("scale", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Unsigned integer. Defines slider scale, within range.For example: if range = \"0,1000\" and scale = \"100\",the display values are: 0, 100, 200, 300, ...Signed and unsigned integers in ColdFusion are in therange -2,147,483,648 to 2,147,483,647."),
                                                         new AttributeFormat("value", AttributeFormat.STRING_TYPE, false,
                                                                             "Starting slider setting. Must be within the range values."),
                                                         new AttributeFormat("onvalidate", AttributeFormat.STRING_TYPE, false,
                                                                             "Custom JavaScript function to validate user input; in thiscase, a change to the default slider value. Specify onlythe function name."),
                                                         new AttributeFormat("message", AttributeFormat.STRING_TYPE, false,
                                                                             "Message text to appear if validation fails."),
                                                         new AttributeFormat("onerror", AttributeFormat.STRING_TYPE, false,
                                                                             "Custom JavaScript function to execute if validation fails.Specify only the function name."),
                                                         new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Slider control height, in pixels."),
                                                         new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Slider control width, in pixels."),
                                                         new AttributeFormat("vspace", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Vertical spacing above and below slider, in pixels."),
                                                         new AttributeFormat("hspace", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Horizontal spacing to left and right of slider, in pixels."),
                                                         new AttributeFormat("align", AttributeFormat.STRING_TYPE, false,
                                                                             "Alignment of slider:* top* left* bottom* baseline* texttop* absbottom* middle* absmiddle* right"),
                                                         new AttributeFormat("lookandfeel", AttributeFormat.STRING_TYPE, false,
                                                                             "- motif: renders slider in Motif style- windows: renders slider in Windows style- metal: renders slider in Java Swing styleIf platform does not support style option, tag defaults toplatform default style.Default: windows"),
                                                         new AttributeFormat("vertical", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "Yes: Renders slider in browser vertically. You must setwidth and height attributes; ColdFusion does notautomatically swap width and height values.No: Renders slider horizontally."),
                                                         new AttributeFormat("bgcolor", AttributeFormat.STRING_TYPE, false,
                                                                             "Background color of control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
                                                         new AttributeFormat("textcolor", AttributeFormat.STRING_TYPE, false,
                                                                             "Text color for control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
                                                         new AttributeFormat("font", AttributeFormat.STRING_TYPE, false,
                                                                             "Font name for data in tree control."),
                                                         new AttributeFormat("fontsize", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Font size for text in tree control, in points."),
                                                         new AttributeFormat("italic", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "Yes: displays tree control text in italicsNo: it does not"),
                                                         new AttributeFormat("bold", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "Yes: displays tree control text in boldNo: it does not"),
                                                         new AttributeFormat("notsupported", AttributeFormat.STRING_TYPE, false,
                                                                             "Text to display if a page that contains a Java applet-basedcfform control is opened by a browser that does notsupport Java or has Java support disabled.Default:\"<b>Browser must support Java to <br>view ColdFusion JavaApplets!</b>\"")}));
    myTagAttributes.put("cfstoredproc", new TagDescription(false,
                                                           "Executes a stored procedure in a server database. Itspecifies database connection information and identifiesthe stored procedure.",
                                                           new AttributeFormat[]{
                                                             new AttributeFormat("procedure", AttributeFormat.STRING_TYPE, true,
                                                                                 "Name of stored procedure on database server."),
                                                             new AttributeFormat("datasource", AttributeFormat.STRING_TYPE, true,
                                                                                 "Name of data source that points to database that containsstored procedure."),
                                                             new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                                 "Overrides username in data source setup."),
                                                             new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                                 "Overrides password in data source setup."),
                                                             new AttributeFormat("blockfactor", AttributeFormat.NUMERIC_TYPE, false,
                                                                                 "Maximum number of rows to get at a time from server.Range is 1 to 100."),
                                                             new AttributeFormat("debug", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "Yes: Lists debug information on each statementNo: does not"),
                                                             new AttributeFormat("returncode", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                 "Yes: Tag populates cfstoredproc.statusCode with statuscode returned by stored procedure.No: does not"),
                                                             new AttributeFormat("result", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                                 "Specifies a name for the structure in which cfstoredprocreturns the statusCode and ExecutionTime variables. Ifset, this value replaces cfstoredproc as the prefix touse when accessing those variables.")}));
    myTagAttributes.put("cfswitch", new TagDescription(false,
                                                       "Evaluates a passed expression and passes control to the cfcasetag that matches the expression result. You can, optionally,code a cfdefaultcase tag, which receives control if there is nomatching cfcase tag value.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("expression", AttributeFormat.STRING_TYPE, true,
                                                                             "CFML expression that yields a scalar value.CFML converts integers, real numbers, Booleans, anddates to numeric values. For example, True, 1, and 1.0 areall equal.")}));
    myTagAttributes.put("cftable", new TagDescription(false,
                                                      "Builds a table in a CFML page. This tag renders data aspreformatted text, or, with the HTMLTable attribute, in anHTML table. If you do not want to write HTML table tag code, orif your data can be presented as preformatted text, use thistag.Preformatted text (defined in HTML with the <pre> and </pre>tags) displays text in a fixed-width font. It displays whitespace and line breaks exactly as they are written within thepre tags. For more information, see an HTML reference guide.To define table column and row characteristics, use the cfcoltag within this tag.",
                                                      new AttributeFormat[]{new AttributeFormat("query", AttributeFormat.STRING_TYPE, true,
                                                                                                "Name of cfquery from which to draw data."),
                                                        new AttributeFormat("maxrows", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Maximum number of rows to display in the table."),
                                                        new AttributeFormat("colspacing", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Number of spaces between columns"),
                                                        new AttributeFormat("headerlines", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Number of lines to use for table header (the default leavesone line between header and first row of table)."),
                                                        new AttributeFormat("htmltable", AttributeFormat.ANY_TYPE, false,
                                                                            "Renders data in an HTML 3.0 table.If you use this attribute (regardless of its value),CFML renders data in an HTML table."),
                                                        new AttributeFormat("border", AttributeFormat.ANY_TYPE, false,
                                                                            "Displays border around table.If you use this attribute (regardless of its value),CFML displays a border around the table.Use this only if you use the HTMLTable attribute."),
                                                        new AttributeFormat("colheaders", AttributeFormat.ANY_TYPE, false,
                                                                            "Displays column heads. If you use this attribute, you mustalso use the cfcol tag header attributeto define them.If you use this attribute (regardless of its value),CFML displays column heads."),
                                                        new AttributeFormat("startrow", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "The query result row to put in the first table row.")}));
    myTagAttributes.put("cftextarea", new TagDescription(false,
                                                         "Puts a multiline text entry box in a cfform tag andcontrols its display characteristics.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                               "Name of the cftextinput control."),
                                                           new AttributeFormat("id", AttributeFormat.STRING_TYPE, true,
                                                                               "ID for form input element."),
                                                           new AttributeFormat("label", AttributeFormat.STRING_TYPE, false,
                                                                               "Label to put beside the control on a form."),
                                                           new AttributeFormat("style", AttributeFormat.STRING_TYPE, false,
                                                                               "In HTML or XMLformat forms, ColdFusion passes thestyle attribute to the browser or XML.In Flash format forms, must be a style specification inCSS format, with the same syntax and contents as usedin Macromedia Flex for the corresponding Flash element."),
                                                           new AttributeFormat("required", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "- true: the field must contain text.- false: the field can be empty.Default: false"),
                                                           new AttributeFormat("validate", AttributeFormat.STRING_TYPE, false,
                                                                               "The type or types of validation to do. Available validationtypes and algorithms depend on the format. For details,see the Usage section of the cfinput tag reference."),
                                                           new AttributeFormat("validateat", AttributeFormat.STRING_TYPE, false,
                                                                               "How to do the validation. For Flash format forms, onSubmitand onBlur are identical; validation is done on submit.For multiple values, use a comma-delimited list.For details, see the Usage section of the cfinput tagreference.Default: onSubmit"),
                                                           new AttributeFormat("message", AttributeFormat.STRING_TYPE, false,
                                                                               "Message text to display if validation fails."),
                                                           new AttributeFormat("range", AttributeFormat.STRING_TYPE, false,
                                                                               "Minimum and maximum numeric allowed values. ColdFusionuses this attribute only if you specify range in thevalidate attribute.If you specify a single number or a single number afollowed by a comma, it is treated as a minimum, with nomaximum. If you specify a comma followed by a number,the maximum is set to the specified number, with nominimum."),
                                                           new AttributeFormat("maxlength", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "The maximum length of text that can be entered.ColdFusion uses this attribute only if you specifymaxlength in the validate attribute."),
                                                           new AttributeFormat("pattern", AttributeFormat.STRING_TYPE, false,
                                                                               "JavaScript regular expression pattern to validate input.Omit leading and trailing slashes. ColdFusion uses thisattribute only if you specify regex in the validate attribute."),
                                                           new AttributeFormat("onvalidate", AttributeFormat.STRING_TYPE, false,
                                                                               "Custom JavaScript function to validate user input. TheJavaScript DOM form object, input object, and inputobject value are passed to routine, which should returnTrue if validation succeeds, False otherwise. If you specifythis attribute, ColdFusion ignores the validate attribute."),
                                                           new AttributeFormat("onerror", AttributeFormat.STRING_TYPE, false,
                                                                               "Custom JavaScript function to execute if validation fails."),
                                                           new AttributeFormat("disabled", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Disables user input, making the control read-only. Todisable input, specify disabled without an attribute, ordisabled=\"true\". To enable input, omit the attribute orspecify disabled=\"false\".Default: false"),
                                                           new AttributeFormat("value", AttributeFormat.STRING_TYPE, false,
                                                                               "Initial value to display in text control. You can specify aninitial value as an attribute or in the tag body, but not inboth places. If you specify the value as an attribute, yomust put the closing cftextarea tag immediately after theopening cftextarea tag, with no spaces or line feeds between,or place a closing slash at the end of the opening cftextareatag. For example:<cftextarea name=\"description\" value=\"Enter a description.\" />"),
                                                           new AttributeFormat("bind", AttributeFormat.STRING_TYPE, false,
                                                                               "Flash only: A Flex bind expression that populates the field withinformation from other form fields."),
                                                           new AttributeFormat("onkeyup", AttributeFormat.STRING_TYPE, false,
                                                                               "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the user releases a keyboard key in the control."),
                                                           new AttributeFormat("onkeydown", AttributeFormat.STRING_TYPE, false,
                                                                               "JavaScript (HTML/XML) or ActionScript (Flash)ActionScript to run when the user presses a keyboardkey in the control."),
                                                           new AttributeFormat("onmouseup", AttributeFormat.STRING_TYPE, false,
                                                                               "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the user presses a mouse button in the control."),
                                                           new AttributeFormat("onmousedown", AttributeFormat.STRING_TYPE, false,
                                                                               "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the user releases a mouse button in the control."),
                                                           new AttributeFormat("onchange", AttributeFormat.STRING_TYPE, false,
                                                                               "JavaScript (HTML/XML) or ActionScript (Flash) to runwhen the control changes due to user action."),
                                                           new AttributeFormat("onclick", AttributeFormat.STRING_TYPE, false,
                                                                               "JavaScript (HTML/XML) to run when the user clicks thecontrol. Not supported for Flash forms."),
                                                           new AttributeFormat("enabled", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Flash only: Boolean value specifying whether the control is enabled.A disabled control appears in light gray. The inverse of thediabled attribute.Default: true"),
                                                           new AttributeFormat("visible", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Flash only: Boolean value specifying whether toshow the control. Space that would be occupied by aninvisible control is blank.Default: true"),
                                                           new AttributeFormat("tooltip", AttributeFormat.STRING_TYPE, false,
                                                                               "Flash only: Text to display when the mouse pointer hoversover the control."),
                                                           new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "Flash only: The height of the control, in pixels."),
                                                           new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                               "Flash only: The width of the control, in pixels.")}));
    myTagAttributes.put("cftextinput", new TagDescription(true,
                                                          "This tag is deprecated.Puts a single-line text entry box in a cfform tag and controlsits display characteristics.",
                                                          new AttributeFormat[]{
                                                            new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                "Name for the cftextinput control."),
                                                            new AttributeFormat("value", AttributeFormat.STRING_TYPE, false,
                                                                                "Initial value to display in text control."),
                                                            new AttributeFormat("required", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                "Yes: the user must enter or change textNo"),
                                                            new AttributeFormat("range", AttributeFormat.STRING_TYPE, false,
                                                                                "Minimum-maximum value range, delimited by a comma.Valid only for numeric data."),
                                                            new AttributeFormat("validate", AttributeFormat.STRING_TYPE, false,
                                                                                "date: verifies format mm/dd/yy.eurodate: verifies date format dd/mm/yyyy.time: verifies time format hh:mm:ss.float: verifies floating point format.integer: verifies integer format.telephone: verifies telephone format ###-###-####. Theseparator can be a blank. Area code and exchange mustbegin with digit 1 - 9.zipcode: verifies, in U.S. formats only, 5- or 9-digitformat #####-####. The separator can be a blank.creditcard: strips blanks and dashes; verifies number usingmod10 algorithm. Number must have 13-16 digits.social_security_number: verifies format ###-##-####. Theseparator can be a blank.regular_expression: matches input against patternattribute."),
                                                            new AttributeFormat("onvalidate", AttributeFormat.STRING_TYPE, false,
                                                                                "Custom JavaScript function to validate user input. The formobject, input object, and input object value are passed toroutine, which should return True if validation succeeds,False otherwise. The validate attribute is ignored."),
                                                            new AttributeFormat("pattern", AttributeFormat.REGEX_TYPE, false,
                                                                                "JavaScript regular expression pattern to validate input.Omit leading and trailing slashes"),
                                                            new AttributeFormat("message", AttributeFormat.STRING_TYPE, false,
                                                                                "Message text to display if validation fails"),
                                                            new AttributeFormat("onerror", AttributeFormat.STRING_TYPE, false,
                                                                                "Custom JavaScript function to execute if validation fails."),
                                                            new AttributeFormat("size", AttributeFormat.NUMERIC_TYPE, false,
                                                                                "Number of characters displayed before horizontal scrollbar displays."),
                                                            new AttributeFormat("font", AttributeFormat.STRING_TYPE, false,
                                                                                "Font name for data in tree control."),
                                                            new AttributeFormat("fontsize", AttributeFormat.NUMERIC_TYPE, false,
                                                                                "Font size for text in tree control, in points."),
                                                            new AttributeFormat("italic", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                "Yes: displays tree control text in italicsNo: it does not"),
                                                            new AttributeFormat("bold", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                "Yes: displays tree control text in boldNo: it does not"),
                                                            new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                                "Tree control height, in pixels."),
                                                            new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                                "Tree control width, in pixels."),
                                                            new AttributeFormat("vspace", AttributeFormat.NUMERIC_TYPE, false,
                                                                                "Vertical margin above and below tree control, in pixels."),
                                                            new AttributeFormat("hspace", AttributeFormat.NUMERIC_TYPE, false,
                                                                                "Horizontal spacing to left and right of tree control, in pixels."),
                                                            new AttributeFormat("align", AttributeFormat.STRING_TYPE, false,
                                                                                "* top* left* bottom* baseline* texttop* absbottom* middle* absmiddle* right"),
                                                            new AttributeFormat("bgcolor", AttributeFormat.STRING_TYPE, false,
                                                                                "Background color of control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
                                                            new AttributeFormat("textcolor", AttributeFormat.STRING_TYPE, false,
                                                                                "Text color for control. For a hex value, use the form:textColor = \"##xxxxxx\", where x = 0-9 or A-F; use two hashsigns or none."),
                                                            new AttributeFormat("maxlength", AttributeFormat.NUMERIC_TYPE, false,
                                                                                "The maximum length of text entered."),
                                                            new AttributeFormat("notsupported", AttributeFormat.STRING_TYPE, false,
                                                                                "Text to display if a page that contains a Java applet-basedcfform control is opened by a browser that does notsupport Java or has Java support disabled.Default:\"<b>Browser must support Java to <br>view ColdFusion JavaApplets!</b>\"")}));
    myTagAttributes.put("cfthrow", new TagDescription(true,
                                                      "Throws a developer-specified exception, which can be caughtwith a cfcatch tag that has any of the following type attributeoptions:ype = \"custom_type\"type = \"Application\"type = \"Any\"",
                                                      new AttributeFormat[]{new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                                                "* A custom type* ApplicationDo not enter another predefined type; types are notgenerated by CFML applications. If you specifyApplication, you need not specify a type for cfcatch."),
                                                        new AttributeFormat("message", AttributeFormat.STRING_TYPE, false,
                                                                            "Message that describes exception event."),
                                                        new AttributeFormat("detail", AttributeFormat.STRING_TYPE, false,
                                                                            "Description of the event. CFML appends error positionto description; server uses this parameter if an error isnot caught by your code."),
                                                        new AttributeFormat("errorcode", AttributeFormat.STRING_TYPE, false,
                                                                            "A custom error code that you supply."),
                                                        new AttributeFormat("extendedinfo", AttributeFormat.STRING_TYPE, false,
                                                                            "A custom error code that you supply."),
                                                        new AttributeFormat("object", AttributeFormat.OBJECT_TYPE, false,
                                                                            "Requires the value of the cfobject tag name attribute.Throws a Java exception from a CFML tag.This attribute is mutually exclusive with all otherattributes of this tag.")}));
    myTagAttributes.put("cftimer", new TagDescription(false,
                                                      "Displays execution time for a specified section ofCFML code. ColdFusion MX displays the timing informationalong with any output produced by the timed code.",
                                                      new AttributeFormat[]{new AttributeFormat("label", AttributeFormat.STRING_TYPE, false,
                                                                                                "Label to display with timing information.Default: \" \""),
                                                        new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                            "- inline: displays timing information inline, following theresulting HTML.- outline: displays timing information and also displays a linearound the output produced by the timed code. The browsermust support the FIELDSET tag to display the outline.- comment: displays timing information in an HTML commentin the format <!-- label: elapsed-time ms -->. The default labelis cftimer.- debug: displays timing information in the debug outputunder the heading CFTimer Times.Default: debug")}));
    myTagAttributes.put("cftrace", new TagDescription(false,
                                                      "Displays and logs debugging data about the state of anapplication at the time the cftrace tag executes. Tracksruntime logic flow, variable values, and execution time.Displays output at the end of the request or in the debuggingsection at the end of the request;CFML logs cftrace output to the file logs\\cftrace.log, inthe CFML installation directory.Note: To permit this tag to execute, you must enable debuggingin the CFML Administrator. Optionally, to report tracesummaries, enable the Trace section.",
                                                      new AttributeFormat[]{
                                                        new AttributeFormat("abort", AttributeFormat.BOOLEAN_TYPE, false,
                                                                            "Calls cfabort tag when the tag is executed"),
                                                        new AttributeFormat("category", AttributeFormat.STRING_TYPE, false,
                                                                            "User-defined string for identifying trace groups"),
                                                        new AttributeFormat("inline", AttributeFormat.BOOLEAN_TYPE, false,
                                                                            "Displays trace code in line on the page in thelocation of the cftrace tag, addition to the debugginginformation output."),
                                                        new AttributeFormat("text", AttributeFormat.STRING_TYPE, false,
                                                                            "User-defined string, which can include simple variable,but not complex variables such as arrays. Outputs to cflogtext attribute"),
                                                        new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                            "Corresponds to the cflog type attribute; displays anappropriate icon.* Information* Warning* Error* Fatal Information"),
                                                        new AttributeFormat("var", AttributeFormat.STRING_TYPE, false,
                                                                            "The name of a simple or complex variable to display.Useful for displaying a temporary value, or a value thatdoes not display on any CFM page.")}));
    myTagAttributes.put("cftransaction", new TagDescription(false,
                                                            "Instructs the database management system to treat multipledatabase operations as a single transaction. Provides databasecommit and rollback processing.",
                                                            new AttributeFormat[]{
                                                              new AttributeFormat("action", AttributeFormat.STRING_TYPE, false,
                                                                                  "<ul><li>begin: the start of the block of code to execute</li><li>commit: commits a pending transaction</li><li>rollback: rolls back a pending transaction</li></ul>"),
                                                              new AttributeFormat("isolation", AttributeFormat.STRING_TYPE, false,
                                                                                  "ODBC lock type.")}));
    myTagAttributes.put("cftree", new TagDescription(false,
                                                     "Inserts a tree control in a form. Validates user selections.Used within a cftree tag block. You can use a CFML queryto supply data to the tree.",
                                                     new AttributeFormat[]{new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                               "Name for tree control."),
                                                       new AttributeFormat("format", AttributeFormat.STRING_TYPE, false,
                                                                           "- applet: displays the tree using a Java applet in thebrowser,- flash: displays the tree using a Flash control- object: returns the tree as a ColdFusion structure with thename specified by the name attribute, For details of thestructure contents, see \"object format\", below.- xml: Generates an XMLrepresentation of the tree.In XML format forms, includes the generated XML in theform. and puts the XML in a string variable with the namespecified by the name attribute.Default: applet"),
                                                       new AttributeFormat("required", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "- true: user must select an item in tree control- false: they do notDefault: false"),
                                                       new AttributeFormat("delimiter", AttributeFormat.STRING_TYPE, false,
                                                                           "Character to separate elements in form variable path.Default: \\\\"),
                                                       new AttributeFormat("completepath", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "- true: start the Form.treename.path variable with the rootof the tree path when cftree is submitted.- false: omit the root level from the Form.treename.pathvariable; the value starts with the first child node in thetree.For the preserveData attribute of cfform to work with thetree, you must set this attribute to Yes.For tree items populated by a query, if you use thecftreeitem queryasroot attribute to specify a root name,that value is returned. If you do not specify a root name,ColdFusion returns the query name.Default: false"),
                                                       new AttributeFormat("appendkey", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "- true: if you use cftreeitem href attributes, ColdFusionappends a CFTREEITEMKEY query string variable withthe value of the selected tree item to the cfform action URL.- false: do not append the tree item value to the URL.Default: true"),
                                                       new AttributeFormat("highlighthref", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "- true: highlights as a link the displayed value for anycftreeitem tag that specifies a href attribute.- false: disables highlighting.Default: true"),
                                                       new AttributeFormat("onvalidate", AttributeFormat.STRING_TYPE, false,
                                                                           "JavaScript function to validate user input. The form object,input object, and input object value are passed to thespecified routine, which should return True if validationsucceeds; False, otherwise."),
                                                       new AttributeFormat("message", AttributeFormat.STRING_TYPE, false,
                                                                           "Message to display if validation fails."),
                                                       new AttributeFormat("onerror", AttributeFormat.STRING_TYPE, false,
                                                                           "JavaScript function to execute if validation fails."),
                                                       new AttributeFormat("lookandfeel", AttributeFormat.STRING_TYPE, false,
                                                                           "- motif: renders slider in Motif style- windows: renders slider in Windows style- metal: renders slider in Java Swing styleIf platform does not support style option, tag defaults toplatform default style.Default: windows"),
                                                       new AttributeFormat("font", AttributeFormat.STRING_TYPE, false,
                                                                           "Font name for data in tree control."),
                                                       new AttributeFormat("fontsize", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Font size for text in tree control, in points."),
                                                       new AttributeFormat("italic", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "- true: displays tree control text in italics- false: it does notDefault: false"),
                                                       new AttributeFormat("bold", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "- true: displays tree control text in bold- false: it does notDefault: false"),
                                                       new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Tree control height, in pixels. If you omit this attribute inFlash format, Flash automatically sizes the tree.Default: 320 (applet only)"),
                                                       new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Tree control width, in pixels. If you omit this attribute inFlash format, Flash automatically sizes the tree.Default: 200 (applet only)"),
                                                       new AttributeFormat("vspace", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Vertical margin above and below tree control, in pixels."),
                                                       new AttributeFormat("hspace", AttributeFormat.NUMERIC_TYPE, false,
                                                                           "Horizontal spacing to left and right of tree control, in pixels."),
                                                       new AttributeFormat("align", AttributeFormat.STRING_TYPE, false,
                                                                           "Alignment of the tree control applet object."),
                                                       new AttributeFormat("border", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "- true: display a border around the tree control.- false: no borderDefault: true"),
                                                       new AttributeFormat("hscroll", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "- true: permits horizontal scrolling- false: no horizontal scrollingDefault: true"),
                                                       new AttributeFormat("vscroll", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "- true: permits vertical scrolling- false: no vertical scrollingDefault: true"),
                                                       new AttributeFormat("style", AttributeFormat.STRING_TYPE, false,
                                                                           "Flash only: Must be a style specification in CSS format, with the samesyntax and contents as used in Macromedia Flex for thecorresponding Flash element."),
                                                       new AttributeFormat("enabled", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Flash only: Boolean value specifying whether thecontrol is enabled. A disabled control appears in light gray.Default: true"),
                                                       new AttributeFormat("visible", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Flash only: Boolean value specifying whether toshow the control. Space that would be occupied by aninvisible control is blank.Default: true"),
                                                       new AttributeFormat("tooltip", AttributeFormat.STRING_TYPE, false,
                                                                           "Flash only: Text to display when the mouse pointerhovers over the control."),
                                                       new AttributeFormat("onchange", AttributeFormat.STRING_TYPE, false,
                                                                           "Flash only: ActionScript to run when the control changes due to user action.If you specify an onChange event handler, the Form scope ofthe ColdFusion action page does not automatically getinformation about selected items. The ActionScript onChangeevent handler must handle all changes and selections."),
                                                       new AttributeFormat("onblur", AttributeFormat.STRING_TYPE, false,
                                                                           "Flash only: ActionScript that runs when the calendar loses focus.(Added in 7.0.1)"),
                                                       new AttributeFormat("onfocus", AttributeFormat.STRING_TYPE, false,
                                                                           "Flash only: ActionScript that runs when the calendar loses focus.(Added in 7.0.1)"),
                                                       new AttributeFormat("notsupported", AttributeFormat.STRING_TYPE, false,
                                                                           "Text to display if a page that contains a Java applet-basedcfform control is opened by a browser that does notsupport Java or has Java support disabled.Default:\"<b>Browser must support Java to <br>view ColdFusion JavaApplets!</b>\"")}));
    myTagAttributes.put("cftreeitem", new TagDescription(true,
                                                         "Populates a form tree control, created with the cftree tag,with elements. To display icons, you can use the img valuesthat CFML provides, or reference your own icons.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("value", AttributeFormat.STRING_TYPE, true,
                                                                               "Value passed when cfform is submitted. When populating atree with data from a cfquery, specify columns in adelimited list. Example: value = \"dept_id,emp_id\""),
                                                           new AttributeFormat("display", AttributeFormat.STRING_TYPE, false,
                                                                               "Tree item label. When populating a tree with data from aquery, specify names in a delimited list. Example:display = \"dept_name,emp_name\""),
                                                           new AttributeFormat("parent", AttributeFormat.STRING_TYPE, false,
                                                                               "Value for tree item parent."),
                                                           new AttributeFormat("img", AttributeFormat.STRING_TYPE, false,
                                                                               "Image name, filename, or file URL for tree item icon.You can specify a custom image. To do so, include path andfile extension; for example:img = \"../images/page1.gif\"To specify more than one image in a tree, or an image atthe second or subsequent level, use commas to separatenames, corresponding to level; for example:img = \"folder,document\"img = \",document\" (example of second level)"),
                                                           new AttributeFormat("imgopen", AttributeFormat.STRING_TYPE, false,
                                                                               "Icon displayed with open tree item. You can specify iconfilename with a relative path. You can use a CFMLimage."),
                                                           new AttributeFormat("href", AttributeFormat.STRING_TYPE, false,
                                                                               "URL to associate with tree item or query column for a treethat is populated from a query. If href is a query column,its value is the value populated by query. If href is notrecognized as a query column, it is assumed that its textis an HTML href.When populating a tree with data from a query, HREFs can bespecified in delimited list; for example:href = \"http://dept_svr,http://emp_svr\""),
                                                           new AttributeFormat("target", AttributeFormat.STRING_TYPE, false,
                                                                               "Target attribute of href URL. When populating a tree withdata from a query, specify target in delimited list:target = \"FRAME_BODY,_blank\""),
                                                           new AttributeFormat("query", AttributeFormat.STRING_TYPE, false,
                                                                               "Query name to generate data for the treeitem."),
                                                           new AttributeFormat("queryAsRoot", AttributeFormat.STRING_TYPE, false,
                                                                               "Defines query as the root level. This avoids having tocreate another parent cftreeitem.* Yes* No* String to use as the root nameIf you do not specify a root name, CFML returns thequery name as the root."),
                                                           new AttributeFormat("expand", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Yes: expands tree to show tree item childrenNo: keeps tree item collapsed")}));
    myTagAttributes.put("cftry", new TagDescription(false,
                                                    "Used with one or more cfcatch tags. Together, they catch andprocess exceptions in CFML pages. Exceptions are eventsthat disrupt the normal flow of instructions in a CFMLpage, such as failed database operations, missing includefiles, and developer-specified events.",
                                                    myEmptyAttributesArray));
    myTagAttributes.put("cfupdate", new TagDescription(true, "Updates records in a data source from data in a CFML formor form Scope.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("datasource", AttributeFormat.STRING_TYPE, true,
                                                                             "Name of the data source that contains the table"),
                                                         new AttributeFormat("tablename", AttributeFormat.STRING_TYPE, true,
                                                                             "Name of table to update.For ORACLE drivers, must be uppercase.For Sybase driver: case-sensitive; must be in same caseas used when the table was created"),
                                                         new AttributeFormat("tableowner", AttributeFormat.STRING_TYPE, false,
                                                                             "For data sources that support table ownership (for example,SQL Server, Oracle, Sybase SQL Anywhere), the table owner."),
                                                         new AttributeFormat("tablequalifier", AttributeFormat.STRING_TYPE, false,
                                                                             "For data sources that support table qualifiers. The purposeof table qualifiers is as follows:SQL Server and Oracle: name of database that containstableIntersolv dBASE driver: directory of DBF files"),
                                                         new AttributeFormat("username", AttributeFormat.STRING_TYPE, false,
                                                                             "Overrides username value specified in ODBC setup."),
                                                         new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                             "Overrides password value specified in ODBC setup."),
                                                         new AttributeFormat("formfields", AttributeFormat.STRING_TYPE, false,
                                                                             "Comma-delimited list of form fields to update.If a form field is not matched by a column name in thedatabase, CFML throws an error.The formFields lies must include the database table primarykey field, which must be present in the form. It can behidden.")}));
    myTagAttributes.put("cfwddx", new TagDescription(true,
                                                     "Serializes and deserializes CFML data structures to theXML-based WDDX format. The WDDX is an XML vocabulary fordescribing complex data structures in a standard, generic way.Implementing it lets you use the HTTP protocol to suchinformation among application server platforms, applicationservers, and browsers.This tag generates JavaScript statements to instantiateJavaScript objects equivalent to the contents of a WDDX packetor CFML data structure. Interoperates with Unicode.",
                                                     new AttributeFormat[]{new AttributeFormat("action", AttributeFormat.STRING_TYPE, true,
                                                                                               "cfml2wddx: serialize CFML to WDDXwddx2cfml: deserialize WDDX to CFMLcfml2js: serialize CFML to JavaScriptwddx2js: deserialize WDDX to JavaScript"),
                                                       new AttributeFormat("input", AttributeFormat.STRING_TYPE, true,
                                                                           "A value to process"),
                                                       new AttributeFormat("output", AttributeFormat.VARIABLENAME_TYPE, false,
                                                                           "Name of variable for output. If action = \"WDDX2JS\" or\"CFML2JS\", and this attribute is omitted, result is outputin HTML stream."),
                                                       new AttributeFormat("toplevelvariable", AttributeFormat.STRING_TYPE, false,
                                                                           "Name of top-level JavaScript object created bydeserialization. The object is an instance of theWddxRecordset object."),
                                                       new AttributeFormat("usetimezoneinfo", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Whether to output time-zone information when serializingCFML to WDDX.- Yes: the hour-minute offset, represented in ISO8601format, is output.- No: the local time is output."),
                                                       new AttributeFormat("validate", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Applies if action = \"wddx2cfml\" or \"wddx2js\".- Yes: validates WDDX input with an XML parser usingWDDX DTD. If parser processes input without error,packet is deserialized. Otherwise, an error isthrown.- No: no input validation")}));
    myTagAttributes.put("cfwindow", new TagDescription(false,
                                                       "Creates a pop-up window in the browser. Does not create aseparate browser pop-up instance.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("bodyStyle", AttributeFormat.STRING_TYPE, false,
                                                                             "A CSS style specification for the window body. As ageneral rule, use this attribute to set color andfont styles. Using this attribute to set the heightand width, for example, can result in distorted output."),
                                                         new AttributeFormat("center", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "A Boolean value specifying whether to centerthe window over the browser window.вЂў If true, ColdFusion ignores the x and yattribute values.вЂў If false, and you do not specify x and yattributes, ColdFusion centers the window."),
                                                         new AttributeFormat("closable", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "A Boolean value specifying whether the user canclose the window. If true, the window has an Xclose icon."),
                                                         new AttributeFormat("draggable", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "A Boolean value specifying whether the user candrag the window. To drag the window, click themouse on the title bar and hold the button downwhile dragging. If the window does not have atitle, users cannot drag it."),
                                                         new AttributeFormat("headerStyle", AttributeFormat.STRING_TYPE, false,
                                                                             "A CSS style specification for the window header.As a general rule, use this attribute to setcolor and font styles. Using this attribute toset the height and width, for example, canresult in distorted output."),
                                                         new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Height of the window in pixels. If you specifya value greater than the available space, thewindow occupies the available space and theresize handles do not appear.  Default is 300."),
                                                         new AttributeFormat("initShow", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "A Boolean value specifying whether to displaythe window when the containing page first displays.If this value is false, use theColdFusion.Layout.show JavaScript function todisplay the window."),
                                                         new AttributeFormat("minHeight", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "The minimum height, in pixels, to which userscan resize the window. Default is 0."),
                                                         new AttributeFormat("minWidth", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "The minimum width, in pixels, to which userscan resize the window.  Default is 0."),
                                                         new AttributeFormat("modal", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "A Boolean value specifying whether the windowis modal, that is, whether the user can interactwith the main window while this window displays.If true, the user cannot interact with the mainwindow."),
                                                         new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                             "The name of the window. Must be unique on thepages. This attribute is required to interactwith the window, including to dynamically showor hide it."),
                                                         new AttributeFormat("onBindError", AttributeFormat.STRING_TYPE, false,
                                                                             "The name of a JavaScript function to executeif evaluating a bind expression results in anerror. The function must take two attributes:an HTTP status code and a message.If you omit this attribute, and have specifieda global error handler(by using the ColdFusion.setGlobalErrorHandlerfunction ),it displays the error message; otherwise a defaulterror pop-up displays."),
                                                         new AttributeFormat("resizable", AttributeFormat.BOOLEAN_TYPE, false,
                                                                             "A Boolean value specifying whether the user canresize the window."),
                                                         new AttributeFormat("source", AttributeFormat.STRING_TYPE, false,
                                                                             "A URL that returns the window contents. Thisattribute can use URL parameters to pass datato the page. ColdFusion uses standard page pathresolution rules to locatethe page. You can usea bind expressions in this attribute; for moreinformation see the Usage section. Note: If aCFML page specified in this attribute containstags that use AJAX features, such as cfform,cfgrid, and cfpod, you must use a cfajaximporttag on the page with the cfwindow tag.For more information, see cfajaximport"),
                                                         new AttributeFormat("title", AttributeFormat.STRING_TYPE, false,
                                                                             "Text to display in the window's title bar. Youcan use HTML mark up to control the titleappearance, of example to show the text inred italic font. If you omit this attribute,the window does not have a title bar."),
                                                         new AttributeFormat("width", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "Width of the control, including the title barand borders, in pixels.  Default is 500."),
                                                         new AttributeFormat("x", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "The X (horizontal) coordinate of the upper-leftcorner of the window, relative to the browserwindow. ColdFusion ignores this attribute ifthe center attribute value is true and if youdo not set the y attribute value."),
                                                         new AttributeFormat("y", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "The Y (vertical) coordinate of the upper-leftcorner of the window, relative to the browserwindow. ColdFusion ignores this attribute ifthe center attribute value is true and if youdo not set the x attribute value.")}));
    myTagAttributes.put("cfxml", new TagDescription(false,
                                                    "Creates a CFML XML document object that contains themarkup in the tag body. This tagcan include XML and CFML tags.CFML processes the CFML code in the tag body, then assignsthe resulting text to an XML document object variable.",
                                                    new AttributeFormat[]{
                                                      new AttributeFormat("variable", AttributeFormat.VARIABLENAME_TYPE, true,
                                                                          "Name of an xml variable"),
                                                      new AttributeFormat("casesensitive", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "Maintains the case of document elements and attributes")}));
    myTagAttributes.put("cfajaximport", new TagDescription(false,
                                                           "Controls the JavaScript files that are imported for use on pages that use ColdFusion AJAXtags and features.",
                                                           new AttributeFormat[]{
                                                             new AttributeFormat("scriptsrc", AttributeFormat.STRING_TYPE, false,
                                                                                 "Specifies the URL, relative to the web root, of thedirectory that contains the JavaScript files usedused by ColdFusion. When you use this attribute,the specified directory must have the samestructure as the /CFIDE/scripts directory."),
                                                             new AttributeFormat("tags", AttributeFormat.STRING_TYPE, false,
                                                                                 "A comma-delimited list of tags or tag-attributecombinations for which to import the supportingJavaScript files on this page.")}));
    myTagAttributes.put("cfmenu", new TagDescription(false,
                                                     "Creates a menu or tool bar (a horizontally arranged menu). Any menu item can be the toplevel of a submenu.",
                                                     new AttributeFormat[]{
                                                       new AttributeFormat("bgcolor", AttributeFormat.STRING_TYPE, false,
                                                                           "The color of the menu background. You canuse any valid HTML color specification.This specification is locally overridden by themenuStyle attribute of this tag and anycfmenuitem tag, but does affect the backgroundof the surrounding color of a submenu whosebackground is controlled by a childStyleattribute"),
                                                       new AttributeFormat("childstyle", AttributeFormat.STRING_TYPE, false,
                                                                           "A CSS style specification that applies to theitems of the top level menu and all child menuitems, including the children of submenus. Thisattribute lets you use a single style specificationfor all menu items."),
                                                       new AttributeFormat("font", AttributeFormat.STRING_TYPE, false,
                                                                           "The font to be use for all child menu items. Youcan use any valid HTML font-family styleattribute."),
                                                       new AttributeFormat("fontcolor", AttributeFormat.STRING_TYPE, false,
                                                                           "The color of the menu text. You can use anyvalid HTML color specification."),
                                                       new AttributeFormat("fontsize", AttributeFormat.STRING_TYPE, false,
                                                                           "The size of the font. Use a numeric value, suchas 8, to specify a pixel character size. Use apercentage value, such as 80% to specify asize relative to the default font size.Font sizes over 20 pixels can result in submenutext exceeding the menu boundary."),
                                                       new AttributeFormat("menustyle", AttributeFormat.STRING_TYPE, false,
                                                                           "A CSS style specification that applies to themenu, including any parts of the menu that do nothave items. If you do not specify style informationin the cfmenuitem tags, this attribute controls thestyle of the top-level items."),
                                                       new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                           "The name of the menu."),
                                                       new AttributeFormat("selectedfontcolor", AttributeFormat.STRING_TYPE, false,
                                                                           "The color of the text for the menu item that hasthe focus. You can use any valid HTML colorspecification."),
                                                       new AttributeFormat("selecteditemcolor", AttributeFormat.STRING_TYPE, false,
                                                                           "The color that highlights the menu item that hasthe focus. You can use any valid HTML colorspecification."),
                                                       new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                           "The orientation of menu."),
                                                       new AttributeFormat("width", AttributeFormat.STRING_TYPE, false,
                                                                           "The width of a vertical menu; not valid forhorizontal menus.")}));
    myTagAttributes.put("cfmenuitem",
                        new TagDescription(false, "Defines an entry in a menu, including an item that is the head of a submenu.",
                                           new AttributeFormat[]{new AttributeFormat("display", AttributeFormat.STRING_TYPE, false,
                                                                                     "The text to show as the menu item label."),
                                             new AttributeFormat("childstyle", AttributeFormat.STRING_TYPE, false,
                                                                 "A CSS style specification that applies to all childmenu items, including the children of submenus."),
                                             new AttributeFormat("divider", AttributeFormat.STRING_TYPE, false,
                                                                 "This attribute specifies that the item is a divider. Ifyou specify this attribute, you cannot specify anyother attributes."),
                                             new AttributeFormat("href", AttributeFormat.STRING_TYPE, false,
                                                                 "A URL link to activate or JavaScript function tocall when the user clicks the menu item."),
                                             new AttributeFormat("image", AttributeFormat.STRING_TYPE, false,
                                                                 "URL of an image to display at the left side of themenu item. The file type can be any format thebrowser can display.For most displays, you should use 15x15 pixelimages, because larger images conflict with themenu item text"),
                                             new AttributeFormat("menustyle", AttributeFormat.STRING_TYPE, false,
                                                                 "A CSS style specification that controls the overallstyle of any submenu of this menu item. Thisattribute controls the submenu of the current menuitem, but not to any child submenus of the submenu."),
                                             new AttributeFormat("name", AttributeFormat.STRING_TYPE, false, "The name of the menu item."),
                                             new AttributeFormat("style", AttributeFormat.STRING_TYPE, false,
                                                                 "A CSS style specification that applies to the currentmenu item only. It is not overridden by thechildStyle attribute."),
                                             new AttributeFormat("target", AttributeFormat.STRING_TYPE, false,
                                                                 "The target in which to display the contentsreturned by the href attribute. The attribute can bea browser window or frame name, an HTML targetvalue, such as _self.")}));
    myTagAttributes.put("cfprint", new TagDescription(false,
                                                      "Prints specified pages from a PDF file. Use this tag to perform automated batch print jobs.You can use the cfprint tag to print any PDF document, including those generated by thecfdocument, cfpdf, and cfpdfform tag. Also, you can use this tag to print Report Builderreports exported in PDF format.",
                                                      new AttributeFormat[]{
                                                        new AttributeFormat("source", AttributeFormat.STRING_TYPE, false,
                                                                            "Source document to print. You can specifyone of the following:вЂў An absolute or relative pathname to a PDFdocument; for example,c:\\work\\myPDF.pdf or myPDF.pdf. Thedefault directory is the template directory.вЂў A PDF document variable in memory thatis generated by the cfdocument tag or thecfpdf tag; for example, \"myPDFdoc\"."),
                                                        new AttributeFormat("attributestruct", AttributeFormat.STRING_TYPE, false,
                                                                            "ColdFusion structure used to specifyadditional print instructions. Individuallynamed attributes take precedence over thekey-value pairs in the attribute structure."),
                                                        new AttributeFormat("copies", AttributeFormat.STRING_TYPE, false,
                                                                            "Number of copies to print. The value must begreater than or equal to 1."),
                                                        new AttributeFormat("jobname", AttributeFormat.STRING_TYPE, false, ""),
                                                        new AttributeFormat("orientation", AttributeFormat.STRING_TYPE, false,
                                                                            "Orientation of the page to be printed."),
                                                        new AttributeFormat("pages", AttributeFormat.STRING_TYPE, false,
                                                                            "Pages in the source file to print. Duplicatepages and pages beyond the total count ofpages in the document are ignored as longas there is at least one page between 1 andthe total number of pages in the document.You can combine individual page numbersand page ranges; for example, 1вЂ“3,6,10вЂ“20.If you do not specify a value for the pagesattribute, ColdFusion prints the entiredocument."),
                                                        new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                            "Specifies either the owner or user passwordfor the PDF source file. If the PDF file ispassword-protected, you must specify thisattribute for the file to print."),
                                                        new AttributeFormat("papersize", AttributeFormat.STRING_TYPE, false,
                                                                            "Paper used for the print job. The value can beany returned by the GetPrinterInfofunction. Valid"),
                                                        new AttributeFormat("quality", AttributeFormat.STRING_TYPE, false, ""),
                                                        new AttributeFormat("color", AttributeFormat.STRING_TYPE, false,
                                                                            "Color or monochrome printing"),
                                                        new AttributeFormat("coverpage", AttributeFormat.STRING_TYPE, false, ""),
                                                        new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                            "Specifies the file type of the document beingprinted. PDF is the only valid value."),
                                                        new AttributeFormat("printer", AttributeFormat.STRING_TYPE, false,
                                                                            "The name of a printer. An example inWindows is вЂњ\\\\s1001prn02\\NTN-2W-HP_BW02вЂќ. The default is the default printerfor the account where the ColdFusion serveris running."),
                                                        new AttributeFormat("sides", AttributeFormat.STRING_TYPE, false, "")}));
    myTagAttributes.put("cfsprydataset", new TagDescription(false,
                                                            "Creates a Spry data set; can use bind parameters to get data from ColdFusion AJAX controlsto populate the data set.",
                                                            new AttributeFormat[]{
                                                              new AttributeFormat("bind", AttributeFormat.STRING_TYPE, false,
                                                                                  "A bind expression that returns XML data to populatethe Spry data set. The bind expression can specify aCFC function or URL and can include bind parametersthat represent the values of ColdFusion controls. Fordetailed information on specifying bind expressions,see HTML form data binding in cfinput."),
                                                              new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                                  "The name of the Spry data set."),
                                                              new AttributeFormat("onbinderror", AttributeFormat.STRING_TYPE, false,
                                                                                  "The name of a JavaScript function to execute ifevaluating the bind expression results in an error. Thefunction must take two attributes: an HTTP statuscode and a message.If you omit this attribute, and have specified a globalerror handler (by using theColdFusion.setGlobalErrorHandler function), thehandler displays the error message; otherwise adefault error pop-up displays."),
                                                              new AttributeFormat("options", AttributeFormat.STRING_TYPE, false,
                                                                                  "A JavaScript object containing options to pass to thedata set."),
                                                              new AttributeFormat("type", AttributeFormat.STRING_TYPE, false,
                                                                                  "Specifies data set type, corresponding to the format ofthe data that is returned by the bind expression."),
                                                              new AttributeFormat("xpath", AttributeFormat.STRING_TYPE, false,
                                                                                  "Valid for XML type data sets only. An XPathexpression that extracts data from the XML returnedby the bind expression. The data set contains only thedata that matches the xpath expression.")}));
    myTagAttributes.put("cftooltip", new TagDescription(false,
                                                        "Specifies tool tip text that displays when the user hovers the mouse pointer over the elementsin the tag body. This tag does not require a form and is not used inside Flash forms.",
                                                        new AttributeFormat[]{
                                                          new AttributeFormat("preventoverlap", AttributeFormat.STRING_TYPE, false,
                                                                              "A Boolean value specifying whether to prevent thetooltip from overlapping the component that itdescribes."),
                                                          new AttributeFormat("autodismissdelay", AttributeFormat.STRING_TYPE, false,
                                                                              "The number of milliseconds between the timewhen the user moves the mouse pointer over thecomponent(and leaves it there) and when thetooltip disappears."),
                                                          new AttributeFormat("hidedelay", AttributeFormat.STRING_TYPE, false,
                                                                              "The number of milliseconds to delay between thetime when the user moves the mouse pointer awayfrom the component and when the tooltipdisappears."),
                                                          new AttributeFormat("showdelay", AttributeFormat.STRING_TYPE, false,
                                                                              "The number of milliseconds to delay between thetime when the user moves the mouse over thecomponent and when the tooltip appears."),
                                                          new AttributeFormat("sourcefortooltip", AttributeFormat.STRING_TYPE, false,
                                                                              "The URL of a page with the tool tip contents. Thepage can include HTML markup to control theformat, and the tip can include images.If you specify this attribute, an animated iconappears with the text \"Loading...\" while the tip isbeing loaded."),
                                                          new AttributeFormat("tooltip", AttributeFormat.STRING_TYPE, false,
                                                                              "Tip text to display. The text can include HTMLformatting.Ignored if you specify a sourceForTooltip attribute")}));
    myTagAttributes.put("cfajaxproxy",
                        new TagDescription(true, "Creates a JavaScript proxy for a ColdFusion component, for use in an AJAX client.",
                                           new AttributeFormat[]{new AttributeFormat("cfc", AttributeFormat.STRING_TYPE, true,
                                                                                     "The CFC for which to create a proxy. You must specify a dot-delimited path to the CFC.The path can be absolute or relative to location of the CFML page.For example, if the myCFC CFC is in the cfcs subdirectory of the ColdFusion page, specify cfcs.myCFC.On UNIX based systems, the tag searches first for a file whos name or path corresponds to the specified name or path, but is in all lower case.If it does not find it, ColdFusion then searches for a file name or path that coresponds to the attribute value exactly, with identical character casing. (required)"),
                                             new AttributeFormat("jsclassname", AttributeFormat.STRING_TYPE, false,
                                                                 "The name to use for the JavaScript proxy class. (optional)")}));
    myTagAttributes.put("cfexchangecalendar", new TagDescription(true,
                                                                 "Creates, deletes, modifies, gets, and responds to Microsoft Exchange calendar events, and gets calendar event attachments.",
                                                                 new AttributeFormat[]{
                                                                   new AttributeFormat("action", AttributeFormat.STRING_TYPE, true,
                                                                                       "The action to take. Must be one of the following: create, delete, get, getAttachments, modify, respond (required)"),
                                                                   new AttributeFormat("attachmentPath", AttributeFormat.STRING_TYPE, false,
                                                                                       "The file path of the destination directory.If the directory does not exist, ColdFusion creates it.If you omit this attribute, ColdFusion does not save any attachments.If you specify a relative path, the path root is the ColdFusion temporary directory, which is returned by the GetTempDirectory function. (optional)"),
                                                                   new AttributeFormat("connection", AttributeFormat.STRING_TYPE, false,
                                                                                       "The name of the connection to the Exchange server, as specified in the cfexchangeconnection tag.If you omit this attribute, you must create a temporary connection by specifying cfexchangeconnection tag connection attributes in the cfexchangecalendar tag. (optional)"),
                                                                   new AttributeFormat("event", AttributeFormat.ANY_TYPE, true,
                                                                                       "A reference to the structure that contains the event properties to be set or changed and their values.You must specify this attribute in number signs (#). (required)"),
                                                                   new AttributeFormat("generateUnique Filenames",
                                                                                       AttributeFormat.BOOLEAN_TYPE, false,
                                                                                       "A Boolean value specifying whether to generate unique file names if multiple attachments have the same file names.Case \"yes\": 3x myfile.txt -> myfile.txt, myfile1.txt, and myfile2.txt. (optional, default=no)"),
                                                                   new AttributeFormat("message", AttributeFormat.STRING_TYPE, false,
                                                                                       "The text of an optional message to send in the response or deletion notification. (optional)"),
                                                                   new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                       "The name of the ColdFusion query variable that will contain the retrieved events or information about the attachments that were retrieved. (required)"),
                                                                   new AttributeFormat("notify", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                       "Boolean value specifying whether to notify others of the changes made to the event (optional)"),
                                                                   new AttributeFormat("responseType", AttributeFormat.STRING_TYPE, true,
                                                                                       "(respond) Must be one of the following: accept, decline, tentative (required)"),
                                                                   new AttributeFormat("result", AttributeFormat.STRING_TYPE, false,
                                                                                       "The name of a variable that will contain the UID of the event that is created.You use the UID value in the uid attribute other actions to identify the event to be acted on. (optional)"),
                                                                   new AttributeFormat("uid", AttributeFormat.STRING_TYPE, true,
                                                                                       "Case-sensitive Exchange UID value or values that uniquely identify the event or eventson which to perform the action.For the delete action, this attribute can be a comma delimited list of UID values.The getAttachments, modify, and respond actions allow only a single UID value. (required)")}));
    myTagAttributes.put("cfexchangeconnection", new TagDescription(true,
                                                                   "Opens or closes a persistent connection to an Microsoft Exchange server.You must have a persistent or temporary connection to use the cfexchangecalendar,cfexchangecontact, cfexchangemail, and cfexchangetask tags to get or changeinformation on the Exchange server.",
                                                                   new AttributeFormat[]{
                                                                     new AttributeFormat("action", AttributeFormat.STRING_TYPE, true,
                                                                                         "The action to take. Must be open or close. (required)"),
                                                                     new AttributeFormat("connection", AttributeFormat.STRING_TYPE, true,
                                                                                         "The name of the connection. You specify this ID when you close the connectionand in tags such as cfexchangemail. (required)"),
                                                                     new AttributeFormat("mailboxName", AttributeFormat.STRING_TYPE, false,
                                                                                         "The ID of the Exchange mailbox to use.Specify this attribute to access a mailbox whose owner has delegated accessrights to the account specified in the username attribute. (optional)"),
                                                                     new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                                         "(open) The users password for accessing the Exchange server. (optional)"),
                                                                     new AttributeFormat("port", AttributeFormat.NUMERIC_TYPE, false,
                                                                                         "The port on the server connect to, most commonly port 80. (optional)"),
                                                                     new AttributeFormat("protocol", AttributeFormat.STRING_TYPE, false,
                                                                                         "The protocol to use forthe connection. Valid values are http and https. (optional)"),
                                                                     new AttributeFormat("proxyHost", AttributeFormat.STRING_TYPE, false,
                                                                                         "The URL or IP address of the proxy host required for access to the network. (optional)"),
                                                                     new AttributeFormat("proxyPort", AttributeFormat.STRING_TYPE, false,
                                                                                         "The port on the proxy server to connect to, most commonly port 80. (optional)"),
                                                                     new AttributeFormat("server", AttributeFormat.STRING_TYPE, true,
                                                                                         "The IP address or URL of the server that is providing access to Exchange. (required)"),
                                                                     new AttributeFormat("username", AttributeFormat.STRING_TYPE, true,
                                                                                         "The Exchange user ID (required)")}));
    myTagAttributes.put("cfexchangecontact", new TagDescription(true,
                                                                "Creates, deletes, modifies, and gets Microsoft Exchange contact records, and gets contact record attachments.",
                                                                new AttributeFormat[]{
                                                                  new AttributeFormat("action", AttributeFormat.STRING_TYPE, true,
                                                                                      "The action to take. Must be one of the following: create, delete, get, getAttachments, modify (required)"),
                                                                  new AttributeFormat("attachmentPath", AttributeFormat.STRING_TYPE, false,
                                                                                      "The absolute file path of the directory in which to put the attachments.If the directory does not exist, ColdFusion creates it.If you omit this attribute, ColdFusion does not save any attachments. (optional)"),
                                                                  new AttributeFormat("connection", AttributeFormat.STRING_TYPE, false,
                                                                                      "The name of the connection to the Exchange server, as specified in the cfexchangeconnection tag.If you omit this attribute, you must create a temporary connection by specifyingcfexchangeconnection tag connection attributes in the cfexchangecontact tag. (optional)"),
                                                                  new AttributeFormat("contact", AttributeFormat.ANY_TYPE, true,
                                                                                      "A reference to the structure that contains the contact properties to be set or changed and their values.You must specify this attribute in number signs (#).For more information on the event structure, see the Usage section. (required)"),
                                                                  new AttributeFormat("generateUniqueFilenames",
                                                                                      AttributeFormat.BOOLEAN_TYPE, false,
                                                                                      "A Boolean value specifying whether to generate unique file names if multiple attachments have the same file names.Case \"yes\": 3x myfile.txt -> myfile.txt, myfile1.txt, and myfile2.txt. (optional, default=no)"),
                                                                  new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                      "The name of the ColdFusion query variable that will contain the retrieved events orinformation about the attachments that were retrieved. (required)"),
                                                                  new AttributeFormat("result", AttributeFormat.STRING_TYPE, false,
                                                                                      "The name of a variable that will contain the UID of the contact that is created.You use this value in the uid attribute other actions to identify the contact to be acted on. (optional)"),
                                                                  new AttributeFormat("uid", AttributeFormat.STRING_TYPE, true,
                                                                                      "A case-sensitive Exchange UID value that uniquely identifies the contacts on which to perform the action.For the delete action, this attribute can be a comma delimited list of UID values.The getAttachments and modify action allow only a single UID value. (required)")}));
    myTagAttributes.put("cfexchangefilter", new TagDescription(true,
                                                               "Specifies the filter parameter for cfexchangemail, cfexchangecalendar, cfexchangetask, and cfexchangecontact, get operations.",
                                                               new AttributeFormat[]{
                                                                 new AttributeFormat("name", AttributeFormat.ANY_TYPE, true,
                                                                                     "The type of filter to use. (required)"),
                                                                 new AttributeFormat("from", AttributeFormat.ANY_TYPE, false,
                                                                                     "The start date or or date/time combination of the range to use for filtering.Cannot be used with the value attribute.If you specify a from attribute without a to attribute, the filter selects forall entries on or after the specified date or time.The value can be in any date/time format recognized by ColdFusion, but mustcorrespond to a value that is appropriate for the filter type. (optional)"),
                                                                 new AttributeFormat("to", AttributeFormat.ANY_TYPE, false,
                                                                                     "The end date or date/time combination for the range used for filtering.Cannot be used with the value attribute.If you specify a to attribute without a from attribute, the filter selects forall entries on or before the specified date or time.The value can be in any date/time format recognized by ColdFusion, but mustcorrespond to a value that is appropriate for the filter type. (optional)"),
                                                                 new AttributeFormat("value", AttributeFormat.ANY_TYPE, false,
                                                                                     "The filter value for all filters that do not take a date or time range.Cannot be used with the from and to attributes.If the name attribute requires this attribute, ColdFusion generates an errorif it has an empty or null value. (optional)")}));
    myTagAttributes.put("cfexchangemail", new TagDescription(false,
                                                             "Gets mail messages and attachments, deletes messages, and sets properties for messages on a Microsoft Exchange server.The cfexchangemail tag performs mail actions on an Exchange server that you cannot do using the cfmail tag.",
                                                             new AttributeFormat[]{
                                                               new AttributeFormat("action", AttributeFormat.STRING_TYPE, false,
                                                                                   "The name to use for the JavaScript proxy class. (required)"),
                                                               new AttributeFormat("attachmentPath", AttributeFormat.STRING_TYPE, false,
                                                                                   "The file path of the directory in which to put the attachments.If the directory does not exist, ColdFusion creates it.If you omit this attribute, ColdFusion does not save any attachments.If you specify a relative path, the path root is the ColdFusion temporary directory, which is returned by the GetTempDirectory function. (optional)"),
                                                               new AttributeFormat("connection", AttributeFormat.STRING_TYPE, false,
                                                                                   "The name of the connection to the Exchange server, as specified in the cfexchangeconnection tag.If you omit this attribute, and you specify cfexchangeconnection tag attributes in this tag, Coldfusion creates a temporary connection. (optional)"),
                                                               new AttributeFormat("folder", AttributeFormat.STRING_TYPE, false,
                                                                                   "The forward slash (/) delimited path, relative to the inbox, of the folder that contains the message or messages.The cfechangemail tag looks in the specified folder only, and does not search subfolders.If you omit this attribute, Exchange looks in the top level of the inbox. (optional)"),
                                                               new AttributeFormat("generateUnique Filenames", AttributeFormat.BOOLEAN_TYPE,
                                                                                   false,
                                                                                   "A Boolean value specifying whether to generate unique file names if multiple attachments have the same file names.Case \"yes\": 3x myfile.txt -> myfile.txt, myfile1.txt, and myfile2.txt. (optional, default=no)"),
                                                               new AttributeFormat("mailUID", AttributeFormat.STRING_TYPE, false,
                                                                                   "The case-sensitive UID of the mail message that contains the meeting request, response, or cancellation notfication.You can use this attribute if there are multiple messages about a single meeting. (optional)"),
                                                               new AttributeFormat("meetingUID", AttributeFormat.STRING_TYPE, true,
                                                                                   "The case-sensitive UID of the meeting for which you received the notification. (required)"),
                                                               new AttributeFormat("message", AttributeFormat.ANY_TYPE, true,
                                                                                   "A reference to a structure that contains the properties to be set and their values.You must specify this attribute in number signs (#). (required)"),
                                                               new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                   "The name of the ColdFusion query variable that will contain the returned mail messages or informationabout the attachments that were retrieved. (required)"),
                                                               new AttributeFormat("UID", AttributeFormat.STRING_TYPE, true,
                                                                                   "The case-sensitive UIDs of the messages on which to perform the action.For the delete action, this attribute can be a comma delimited list of UID values.The getAttachments and set actions allow only a single UID value. (required)")}));
    myTagAttributes.put("cfexchangetask", new TagDescription(true,
                                                             "Creates, deletes, modifies, and gets Microsoft Exchange tasks, and gets task attachments.",
                                                             new AttributeFormat[]{
                                                               new AttributeFormat("action", AttributeFormat.STRING_TYPE, false,
                                                                                   "The action to take. Must be one of the following: create, delete, get, getAttachments, modify (optional)"),
                                                               new AttributeFormat("attachmentPath", AttributeFormat.STRING_TYPE, false,
                                                                                   "The file path of the directory in which to put the attachments.If the directory does not exist, ColdFusion creates it.If you omit this attribute, ColdFusion does not save any attachments.If you specify a relative path, the path root is the ColdFusion temporary directory, which is returnedby the GetTempDirectory function. (optional)"),
                                                               new AttributeFormat("connection", AttributeFormat.STRING_TYPE, false,
                                                                                   "The name of the connection to the Exchange server, as specified in the cfexchangeconnection tag.If you omit this attribute, and you specify cfexchangeconnection tag attributes in this tag,Coldfusion creates a temporary connection. (optional)"),
                                                               new AttributeFormat("task", AttributeFormat.ANY_TYPE, true,
                                                                                   "A reference to the structure that contains the task properties to be set or changed and their values.You must specify this attribute in number signs (#).For more information on the event structure, see the Usage section. (required)"),
                                                               new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                   "The name of the ColdFusion query variable that will contain the returned mail messages or informationabout the attachments that were retrieved. (required)"),
                                                               new AttributeFormat("results", AttributeFormat.STRING_TYPE, false,
                                                                                   "(create) The name of a variable that will contain the UID of the task that is created.You use this value in the uid attribute of other actions to identify the task to be acted on. (optional)"),
                                                               new AttributeFormat("uid", AttributeFormat.STRING_TYPE, true,
                                                                                   "A case-sensitive Exchange UID value that uniquely identifies the tasks on which to perform the action.For the delete action, this attribute can be a comma delimited list of UID values.The getAttachments and modify action allow only a single UID value. (optional)")}));
    myTagAttributes.put("cffeed", new TagDescription(true,
                                                     "Reads or creates an RSS or Atom syndication feed.This tag can read RSS versions 0.90, 0.91, 0.92, 0.93, 0.94, 1.0, and 2.0, and Atom 0.3 or 1.0.It can create RSS 2.0 or Atom 1.0 feeds.",
                                                     new AttributeFormat[]{new AttributeFormat("action", AttributeFormat.ANY_TYPE, false,
                                                                                               "The name to use for the JavaScript proxy class.The action to take, one of the following values:\"create\" Creates an RSS 2.0 or Atom 1.0 feed XML document and saves it in a variable, writes it to a file, or both.\"read\" Parses an RSS or Atom feed from a URL or an XML file and saves it in a structure or query.You can also get query metadata in a separate structure. (optional, default= read)"),
                                                       new AttributeFormat("columnmap", AttributeFormat.ANY_TYPE, false,
                                                                           "Used only for the create action with a query attribute.A structure specifying a mapping between the names of the columns in the object specified by the query attributeand the columns of the ColdFusion feed format, as described in the Query attribute section.The key for each field must be a column name listed in the Query columns section. The value of the field must bethe name of the corresponding column in the query object used as input to the create action. (optional)"),
                                                       new AttributeFormat("enclosuredir", AttributeFormat.STRING_TYPE, false,
                                                                           "Used only for the read action.Path to the directory in which to save any enclosures that are available in the feed being read.The path can be absolute or relative to the CFML file.If the directory does not exist, ColdFusion generates an error.If you omit this attribute, ColdFusion does not save enclosures.To specify the directory that contains the current page, set this attribute to .(period). (optional)"),
                                                       new AttributeFormat("ignoreenclosureerror", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "If this attribute is true, ColdFusion attempts to save all enclosures.If it encounters an error downloading one enclosure, it continues downloading other enclosures and writes theerror information in the server log.If this attribute is false, ColdFusion stops downloading all enclosures and generates an error when it encountersan error downloading an enclosure.Note: Enclosure errors can occur if the specified enclosure is of a type that the web server does not allow to bedownloaded. (optional, default=false)"),
                                                       new AttributeFormat("name", AttributeFormat.ANY_TYPE, false,
                                                                           "A structure that contains complete feed data:The output of a read action.The input definition of the feed to create.This structure contains complete feed information.When you specify the name attribute for a create action, you must put it in pound signs (#). (optional)"),
                                                       new AttributeFormat("outputfile", AttributeFormat.STRING_TYPE, false,
                                                                           "Path of the file in which to write the feed as XML text.The path can be absolute, or relative to the CFML file. (optional)"),
                                                       new AttributeFormat("overwrite", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Whether to overwrite the XML feed file if it already exists.If you do not set this attribute to true and the cffeed tag tries to write to a file that exists, ColdFusiongenerates an error. (optional, default=false)"),
                                                       new AttributeFormat("overwriteenclosure", AttributeFormat.BOOLEAN_TYPE, false,
                                                                           "Used only for the read action.Whether to overwrite files in the enclosure directory if they already exist.If you do not set this attribute to true and the cffeed tag tries to write to a file that exists, ColdFusiongenerates an error. (optional, default=false)"),
                                                       new AttributeFormat("properties", AttributeFormat.ANY_TYPE, false,
                                                                           "A structure containing the feed metadata, the information about the entire feed.Can contain either of the following:The output of a read action.Input to a create action.The properties and query attributes combined provide complete feed information.When you specify the properties attribute for a create action, you must put it in pound signs (#) (optional)"),
                                                       new AttributeFormat("query", AttributeFormat.STRING_TYPE, false,
                                                                           "A query object containing the Atom entries or RSS items in the feed. Can contain either of the following:The output of a read action.Input to a create action.The properties and query attributes combined provide complete feed information.When you specify the query attribute for a create action, you must put it in pound signs (#) (optional)"),
                                                       new AttributeFormat("source", AttributeFormat.STRING_TYPE, true,
                                                                           "Used only for the read action.The URL of the feed or the path to the XML file that contains the feed contents.A path can be absolute, or relative to the CFML file. (required)"),
                                                       new AttributeFormat("jsclassname", AttributeFormat.STRING_TYPE, false,
                                                                           "A variable in which to save the read or created feed as XML text. (optional)")}));
    myTagAttributes.put("cfimage", new TagDescription(true,
                                                      "Creates a ColdFusion image that can be manipulated by using Image functions.You can use the cfimage tag to perform common image manipulation operations as a shortcut to Image functions.You can use the cfimage tag independently or in conjunction with Image functions.",
                                                      new AttributeFormat[]{
                                                        new AttributeFormat("action", AttributeFormat.STRING_TYPE, false,
                                                                            "The action to take. Must be one of the following:bordercaptchaconvertinforeadresizerotatewritewriteToBrowserYou do not need to specify the default action, read, explicitly. (optional, default=read)"),
                                                        new AttributeFormat("angle", AttributeFormat.NUMERIC_TYPE, true,
                                                                            "Angle in degrees to rotate the image.You must specify an integer for the value. (required)"),
                                                        new AttributeFormat("color", AttributeFormat.STRING_TYPE, true,
                                                                            "(border) Border color.Hexadecimal value or supported named color.For a hexadecimal value, use the form \"##xxxxxx\" or \"xxxxxx\". (required)"),
                                                        new AttributeFormat("destination", AttributeFormat.STRING_TYPE, false,
                                                                            "Absolute or relative pathname where the image output is written.The image format is determined by the file extension.The convert and write actions require a destination.The border, captcha, resize, and rotate actions require either a name attribute or a destination attribute.You can specify both.Scorpio supports only CAPTCHA images in PNG format.If you do not enter a destination, the CAPTCHA image is placed inline in the HTML output and displayed in the web browser. (optional)"),
                                                        new AttributeFormat("difficulty", AttributeFormat.STRING_TYPE, false,
                                                                            "Level of complexity of the CAPTCHA text.Specify one of the following levels of text distortion:highmediumlow (optional, default=low)"),
                                                        new AttributeFormat("fontSize", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Font size of the text in the CAPTCHA image.The value must be an integer. (optional)"),
                                                        new AttributeFormat("format", AttributeFormat.STRING_TYPE, false,
                                                                            "Format of the image displayed in the browser.If you do not specify a format, the image is displayed in PNG format.You cannot display a GIF image in a browser.GIF images are displayed in PNG format. (optional, Default=PNG)"),
                                                        new AttributeFormat("height", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Height in pixels of the image.For the resize attribute, you also can specify the height as a percentage (an integer followed by the \"%\" symbol).The value must be an integer. (optional)"),
                                                        new AttributeFormat("isBase64", AttributeFormat.BOOLEAN_TYPE, false,
                                                                            "Specifies whether the source is a Base64 string.The isBase64 values are:yes: the source is a Base64 string.no: the source is not a Base64 string. (optional, default=no)"),
                                                        new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                            "Name of the ColdFusion image variable to create.The read action requires name attribute.The border, resize, and rotate options require a name attribute or a destination attribute.You can specify both. (optional)"),
                                                        new AttributeFormat("overwrite", AttributeFormat.BOOLEAN_TYPE, false,
                                                                            "Valid only if the destination attribute is specified. The overwrite values are:yes: overwrites the destination file.no: does not overwrite the destination file.If the destination file already exists, ColdFusion generates an error if the overwrite option is not set to yes. (optional, default=no)"),
                                                        new AttributeFormat("quality", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Quality of the JPEG destination file.Applies only to files with an extension of JPG or JPEG.Valid values are fractions that range from 0 through 1(the lower the number, the lower the quality). (optional, default=0.75)"),
                                                        new AttributeFormat("source", AttributeFormat.ANY_TYPE, true,
                                                                            "URL of the source image; for example, \"http://www.google.com/ images/logo.gif\"Absolute or relative pathname of the source image; for example, \"c:\\wwwroot\\images\\logo.jpg\"ColdFusion image variable containing another image, BLOB, or byte array; for example, \"#myImage#\"Base64 string; for example, \"data:image/jpg;base64,/9j/ 4AAQSkZJRgABAQA..............\" (required)"),
                                                        new AttributeFormat("structName", AttributeFormat.STRING_TYPE, true,
                                                                            "() Name of the ColdFusion structure to be created. (required)"),
                                                        new AttributeFormat("text", AttributeFormat.STRING_TYPE, true,
                                                                            "Text string displayed in the CAPTCHA image.Use capital letters for better readability. (required)"),
                                                        new AttributeFormat("thickness", AttributeFormat.NUMERIC_TYPE, false,
                                                                            "Border thickness in pixels.The border is added to the outside edge of the source image,increasing the image area accordingly.The value must be an integer. (optional, default=1)"),
                                                        new AttributeFormat("width", AttributeFormat.STRING_TYPE, true,
                                                                            "Width in pixels of the image.For resize, you also can specify the width as a percentage(an integer followed by the \"%\" symbol).The value must be an integer. (required)")}));
    myTagAttributes.put("cfinterface", new TagDescription(true,
                                                          "Defines an interface that consists of a set of signatures for functions.The interface does not include the full function definitions;instead, you implement the functions in a CFC.The interfaces that you define by using this tag can make upthe structure of a reusable application framework.",
                                                          new AttributeFormat[]{
                                                            new AttributeFormat("displayName", AttributeFormat.STRING_TYPE, false,
                                                                                "A value to be displayed when using introspection toshow a descriptive name for the interface. (optional)"),
                                                            new AttributeFormat("extends", AttributeFormat.STRING_TYPE, false,
                                                                                "A comma delimited list of one or more interfaces that this interface extends.Any CFC that implements an interface must also implement all the functionsin the interfaces specified by this property.If an interface extends another interface, and the child interface specifiesa function with the same name as one in the parent interface, both functionsmust have the same attribues; otherwise	ColdFusion generates an error. (optional)"),
                                                            new AttributeFormat("displayName", AttributeFormat.STRING_TYPE, false,
                                                                                "Text to be displayed when using introspection to show information about the interface.The hint attribute value follows the syntax line in the function description. (optional)")}));
    myTagAttributes.put("cfpdf", new TagDescription(false,
                                                    "Manipulates existing PDF documents. The following list describes some of thetasks you can perform with the cfpdf tag:Merge several PDF documents into one PDF document.Extract pages from multiple PDF documents and generate a new PDF document.Linearize multipage PDF documents for faster display.Encrypt or decrypt PDF files for security.",
                                                    new AttributeFormat[]{new AttributeFormat("action", AttributeFormat.STRING_TYPE, true,
                                                                                              "The action to take. Must be one of the following:addwatermarkdeletepagesgetinfomergeprotectreadremovewatermarksetinfowrite(required)"),
                                                      new AttributeFormat("ascending", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "Order in which the PDF files are sorted:yes: files are sorted in ascending orderno: files are sorted in descending order (optional, default=no)"),
                                                      new AttributeFormat("copyfrom", AttributeFormat.STRING_TYPE, false,
                                                                          "The filename of the PDF document from which to copy the watermark (optional)"),
                                                      new AttributeFormat("destination", AttributeFormat.STRING_TYPE, false,
                                                                          "The pathname of the modified PDF document.If the destination file exists, you must set the overwrite attribute to yes.If the destination file does not exist, ColdFusion creates it as long asthe parent directory exists. (optional)"),
                                                      new AttributeFormat("directory", AttributeFormat.STRING_TYPE, false,
                                                                          "Specify the directory of the PDF documents to merge.You must specify either the directory or the source.If you specify the directory, you must also specify the order. (optional)"),
                                                      new AttributeFormat("encrypt", AttributeFormat.STRING_TYPE, false,
                                                                          "Specify the type of encryption used on the source PDF document:RC4_40, RC4_128M, AES_128, none (optional)"),
                                                      new AttributeFormat("flatten", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "Specify whether the output file is flattened:yes: the formatting is removed and the file is flattenedno: the format of the source PDF is maintained in the output file. (optional,default=no)"),
                                                      new AttributeFormat("foreground", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "Specify whether the watermark is placed in the foreground of the PDF document:yes: the watermark appears in the foregroundno: the watermark appears in the background (optional, default=no)"),
                                                      new AttributeFormat("image", AttributeFormat.STRING_TYPE, false,
                                                                          "Specify the image used as a watermark.You can specify a filename or a ColdFusion image variable. (optional)"),
                                                      new AttributeFormat("info", AttributeFormat.STRING_TYPE, true,
                                                                          "Specify the structure variable for relevant information, for example, #infoStruct#.ColdFusion ignores read only information, such as the creation date, application used to createthe PDF document, and encryption parameters. (required)"),
                                                      new AttributeFormat("isBase64", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "Specify whether the image used a watermark is in Base64 format:yes: the image is in Base64 formatno: the image is not in Base64 format (optional, default=no)"),
                                                      new AttributeFormat("keepbookmark", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "Specify whether bookmarks from the source PDFdocuments are retained in the merged document:yes: the bookmarks are retainedno: the bookmarks are removed (optional, default=no)"),
                                                      new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                          "Specify the PDF document variable name, for example, myPDFdoc.If the source is a PDF document variable, you cannot specify thename attribute again; you can write the modified PDF documentto the destination. (optional)"),
                                                      new AttributeFormat("newownerpassword", AttributeFormat.STRING_TYPE, false,
                                                                          "Specify the password for the owner of the PDF document. (optional)"),
                                                      new AttributeFormat("newuserpassword", AttributeFormat.STRING_TYPE, false,
                                                                          "Specify the password for the user of the PDF document. (optional)"),
                                                      new AttributeFormat("opacity", AttributeFormat.STRING_TYPE, false,
                                                                          "Specify the opacity of the watermark.Valid values are integers in the range 0 (transparent)through 10 (opaque). (optional, default=3)"),
                                                      new AttributeFormat("order", AttributeFormat.STRING_TYPE, true,
                                                                          "Specify the order in which the PDF documentsin the directory are merged:name: orders the documents alphabeticallytime: orders the documents by timestamp (required)"),
                                                      new AttributeFormat("overwrite", AttributeFormat.STRING_TYPE, false,
                                                                          "Specify whether to overwrite the destination file:yes: overwrites the destination fileno: does not overwrite the destination file (optional)"),
                                                      new AttributeFormat("page", AttributeFormat.STRING_TYPE, true,
                                                                          "Specify page or pages in the source PDF document onwhich to perform the action. You can specify multiplepages as ranges separated by commas; for example,3-10,12-18. The page attribute applies only to thewatermark type for the removewatermark action. (required)"),
                                                      new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                          "Specify the owner or user password of the source PDF document, if it exists. (optional)"),
                                                      new AttributeFormat("permissions", AttributeFormat.STRING_TYPE, false,
                                                                          "Specify the type of permissions on the PDF document:AllowPrintHighAllowPrintLowAllowModifyAllowCopyAllowAddAllowSecureAllowModifyAnnotationsAllowExtractAllowFillInallnoneExcept for all or none, you can specify acommaseparated list of permissions. (optional)"),
                                                      new AttributeFormat("position", AttributeFormat.STRING_TYPE, false,
                                                                          "Specify the position on the page	wherethe watermark is placed. The position represents thetop-left corner of the watermark.Specify the x and y coordinates; for example 50,30. (optional)"),
                                                      new AttributeFormat("rotation", AttributeFormat.NUMERIC_TYPE, false,
                                                                          "Specify the degree of rotation ofthe watermark image on the page; for example, 30. (optional)"),
                                                      new AttributeFormat("showonprint", AttributeFormat.STRING_TYPE, false,
                                                                          "Specify whether the watermark is printed withthe PDF document:yes: the watermark is printed with the PDF documentno: the watermark is not printed with the PDF document (optional)"),
                                                      new AttributeFormat("source", AttributeFormat.STRING_TYPE, true,
                                                                          "Specify the source. The source can be:The pathname to a PDF document; for example, c:\\work\\myPDF.pdfA PDF document variable in memory that is generated by thecfdocument tag or the cfpdf tag; for example, #myPDFdoc#The binary content of PDF document variable. (required)"),
                                                      new AttributeFormat("type", AttributeFormat.STRING_TYPE, true,
                                                                          "Specify the type to remove from the source PDF document:attachmentbookmarkwatermark (optional)"),
                                                      new AttributeFormat("version", AttributeFormat.STRING_TYPE, false,
                                                                          "(write) Specify the version of the PDF document to write.Valid values are:1.11.21.31.41.51.6 (optional)")}));
    myTagAttributes.put("cfpdfform", new TagDescription(false,
                                                        "Manipulates existing Adobe Acrobat forms and Adobe LiveCycle forms.The following list describes some of the tasks you can perform with the cfpdfform tag:Embed an interactive Acrobat form or LiveCycle form within a PDF document.You use the cfpdfform tag to embed the PDF form within a cfdocument tag.Render an existing Acrobat form or LiveCycle form. This includes prefillingfields from a database or an XML data file and processing form data submittedvia HTTP post or PDF format.Extract or prefill values in stored PDF forms and save the output to a fileor use it to update a data source.",
                                                        new AttributeFormat[]{
                                                          new AttributeFormat("action", AttributeFormat.STRING_TYPE, true,
                                                                              "The action to perform on the source:populateread (required)"),
                                                          new AttributeFormat("datafile", AttributeFormat.STRING_TYPE, false,
                                                                              "(populate read) Pathname for the XML data file.if action=\"populate\", the data from this filepopulates the fields of the PDF form.if action=\"read\", ColdFusion writes the datato this file.You can specify a pathname relative to the contextroot or a relative pathname.You must specify either the datafile attributeor the result attribute for the read action;you can specify both. (optional)"),
                                                          new AttributeFormat("destination", AttributeFormat.STRING_TYPE, false,
                                                                              "(populate) Pathname for the output file.You can specify an absolute pathname ora pathname relative to the context root.The file extension must be PDF or XDP.The file extension determines the formatof the file. (The XDP format applies onlyto LiveCycle forms.)If you do not specify the destination,ColdFusion displays the form in the browser.Do not specify the destination when youembed a form in a PDF document. (optional)"),
                                                          new AttributeFormat("overwrite", AttributeFormat.BOOLEAN_TYPE, false,
                                                                              "(populate, read) Overwrite the destination file(if action=\"populate)or the data file (if action=\"read\"):yesno (optional)"),
                                                          new AttributeFormat("result", AttributeFormat.ANY_TYPE, false,
                                                                              "(read) ColdFusion structure that contains the form field values.You must specify the datafile attribute or the result attribute;you can specify both. (optional)"),
                                                          new AttributeFormat("source", AttributeFormat.STRING_TYPE, true,
                                                                              "(populate, read) Pathname of the source PDF (absolute path or pathrelative to the context root) or byte array representing a PDF. (required)")}));
    myTagAttributes.put("cfpdfformparam", new TagDescription(true,
                                                             "Provides additional information to the cfpdfform tag.The cfpdfformparam tag is always a child tag of the cfpdfform or cfpdfsubform tag.Use the cfpdfformparam tag to populate fields in a PDF form.",
                                                             new AttributeFormat[]{
                                                               new AttributeFormat("index", AttributeFormat.NUMERIC_TYPE, false,
                                                                                   "Specify the index associated with the field name.If multiple fields have the same name, the indexvalue is used to locate one of them. (optional, default=1)"),
                                                               new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                   "The field name on the PDF form. (requiredl)"),
                                                               new AttributeFormat("value", AttributeFormat.STRING_TYPE, false,
                                                                                   "The value associated with the field name.For interactive fields, specify a ColdFusion variable. (optional)")}));
    myTagAttributes.put("cfpdfparam", new TagDescription(true,
                                                         "Provides additional information for the cfpdf tag.The cfpdfparam tag applies only to the 	merge action of	the cfpdf tag.The cfpdfparam tag is always a child tag of the cfpdf tag.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("page", AttributeFormat.STRING_TYPE, false,
                                                                               "Specify the page or pages of the PDF source file to merge.You can specify a range of pages; for example, 1-5 ,or a comma-separated list of pages; for example, 1-5,6-10,18. (optional)"),
                                                           new AttributeFormat("password", AttributeFormat.STRING_TYPE, false,
                                                                               "Specify the user or owner password, if the source PDF file is passwordprotected. (optional)"),
                                                           new AttributeFormat("source", AttributeFormat.STRING_TYPE, false,
                                                                               "Specify the source PDF file to merge. (optional)")}));
    myTagAttributes.put("cfpdfsubform", new TagDescription(false,
                                                           "Populates a subform within the cfpdfform tag.The cfpdfsubform tag can be a child tag of the cfpdfform tagor nested in another cfpdfsubform tag.",
                                                           new AttributeFormat[]{
                                                             new AttributeFormat("index", AttributeFormat.NUMERIC_TYPE, false,
                                                                                 "Index associated with the field name.If multiple fields have the same name, ColdFusionuses the index value is to locate one of them. (optional, default=1)"),
                                                             new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                 "Name of the subform corresponding to subform name in the PDF form. (required)")}));
    myTagAttributes.put("cfpresentation", new TagDescription(false,
                                                             "Defines the look and feel of a dynamic slide presentation.Use the cfpresentation tag as the parent tag for one or more cfpresentationslide tags,where you define the content for the presentation.",
                                                             new AttributeFormat[]{
                                                               new AttributeFormat("backgroundColor", AttributeFormat.STRING_TYPE, false,
                                                                                   "Specifies the background color of the presentation.The value is hexadecimal: use the form \"##xxxxxx\" or \"##xxxxxxxx\",where x = 0-9 or A-F; use two number signs or none. (optional, default=0x727971)"),
                                                               new AttributeFormat("control", AttributeFormat.STRING_TYPE, false,
                                                                                   "Specifies the presentation control:normalbrief (optional, default=normal)"),
                                                               new AttributeFormat("controlLocation", AttributeFormat.STRING_TYPE, false,
                                                                                   "Specifies the location of the presentation control:rightleft (optional, default= right)"),
                                                               new AttributeFormat("directory", AttributeFormat.STRING_TYPE, false,
                                                                                   "Specifies the directory where the presentation is saved.This can be absolute path or a path relative to the CFM page.ColdFusion automatically generates the files necessary torun the presentation, including:index.htmcomponents.swfloadflash.jsviewer.swfColdFusion stores any data files in the presentation,including images, video clips, and SWF files referenced by thecfpresentationslide tags in a subdirectory called data.To run the presentation, open the index.htm file.If you do not specify a directory, the presentationruns in the client browser. (optional)"),
                                                               new AttributeFormat("glowColor", AttributeFormat.STRING_TYPE, false,
                                                                                   "Specifies the color used for glow effects on the buttons.The value is hexadecimal: use the form \"##xxxxxx\" or \"##xxxxxxxx\",where x = 0-9 or AF; use two number signs or none. (optional, default=0x35D334)"),
                                                               new AttributeFormat("initialTab", AttributeFormat.STRING_TYPE, false,
                                                                                   "Specifies which tab will be on top when the presentation is displayed.This applies only when the control value is normal:outlinesearchnotes (optional, default=outline)"),
                                                               new AttributeFormat("lightColor", AttributeFormat.STRING_TYPE, false,
                                                                                   "Specifies the light color used for light-and shadow effects.The value is hexadecimal: use the form \"##xxxxxx\" or \"##xxxxxxxx\",where x = 0-9 or A-F; use two number signs or none. (optional, default=0x4E5D60)"),
                                                               new AttributeFormat("overwrite", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                   "Specifies whether files in the directory are overwritten.Specify this attribute only when the you specify the directory.yes: overwrites files if they are already presentno: create new files (optional, default=yes)"),
                                                               new AttributeFormat("primaryColor", AttributeFormat.STRING_TYPE, false,
                                                                                   "Specifies the primary color of the presentation.The value is hexadecimal: use the form \"##xxxxxx\" or \"##xxxxxxxx\",where x = 0-9 or AF; use two number signs or none. (optional, default=0x6F8488)"),
                                                               new AttributeFormat("shadowColor", AttributeFormat.STRING_TYPE, false,
                                                                                   "Specifies the shadow color used for light-and shadow effects.The value is hexadecimal: use the form \"##xxxxxx\" or \"##xxxxxxxx\",where x = 0-9 or A-F; use two number signs or none. (optional, default=0x000000)"),
                                                               new AttributeFormat("showNotes", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                   "Specifies whether the notes tab is present:yesno (optional, default=no)"),
                                                               new AttributeFormat("showOutline", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                   "Specifies whether the outline is present:yesno (optional, default=yes)"),
                                                               new AttributeFormat("showSearch", AttributeFormat.BOOLEAN_TYPE, false,
                                                                                   "Specifies whether the search tab is present:yesno (optional, default=yes)"),
                                                               new AttributeFormat("textColor", AttributeFormat.STRING_TYPE, false,
                                                                                   "Specifies the color for all the text in the presentation user interface. (optional, default=0xFFFFFF)"),
                                                               new AttributeFormat("title", AttributeFormat.STRING_TYPE, false,
                                                                                   "Specifies the title of the presentation. (optional)")}));
    myTagAttributes.put("cfpresentationslide", new TagDescription(false,
                                                                  "Creates a slide dynamically from an HTML source file,HTML and CFML code, or an SWF source file.If you do not specify a source file, you must include the HMTL or CFML code forthe body of the slide within the cfpresentationslide tag. The cfpresentationslideis the child tag of the cfpresentation tag.",
                                                                  new AttributeFormat[]{
                                                                    new AttributeFormat("audio", AttributeFormat.STRING_TYPE, false,
                                                                                        "Specifies the path of the audio file relative to the CFM page.The audio file must be an MP3 file.A slide cannot have both audio and video specified (optional)"),
                                                                    new AttributeFormat("bottomMargin", AttributeFormat.NUMERIC_TYPE, false,
                                                                                        "Specifies the bottom margin of the slide. (optional, default=0)"),
                                                                    new AttributeFormat("duration", AttributeFormat.NUMERIC_TYPE, true,
                                                                                        "Specifies the duration in seconds that the slide is played. (required)"),
                                                                    new AttributeFormat("leftMargin", AttributeFormat.NUMERIC_TYPE, false,
                                                                                        "Specifies the left margin of the slide. (optional, default=0)"),
                                                                    new AttributeFormat("notes", AttributeFormat.STRING_TYPE, false,
                                                                                        "Specifies the notes used for the slide. (optional)"),
                                                                    new AttributeFormat("presenter", AttributeFormat.STRING_TYPE, false,
                                                                                        "Specifies the presenter of the slide.A slide can have only one presenter.This name must match one of the presenter names in the cfpresenter tag.If no presenter is specified, it will take the first presenterspecified in the presenter list. (optional)"),
                                                                    new AttributeFormat("rightMargin", AttributeFormat.NUMERIC_TYPE, false,
                                                                                        "Specifies the right margin of the slide. (optional, default=0)"),
                                                                    new AttributeFormat("scale", AttributeFormat.NUMERIC_TYPE, false,
                                                                                        "Specifies the scale used for the HTML content on the slidepresentation. If the scale attribute is not specified andthe content cannot fit in one slide, it will automaticallybe scaled down to fit on the slide. (optional, default=1.0)"),
                                                                    new AttributeFormat("src", AttributeFormat.STRING_TYPE, false,
                                                                                        "HTML or SWF source files used as a slide. You can specifythe following as the slide source:an absolute patha path relative to the CFM pagea URL: you can specify the URL only if the source is an HTML fileSWF files must be present on the system running ColdFusion and the pathmust either be an absolute path or path relative to the CFM page.If the src value is not specified, you must specify HTML/CFML codeas the body. If you specify a source file and HTML /CFML, ColdFusionignores the source file and displays the HTML/CFML in the slide. (optional)"),
                                                                    new AttributeFormat("title", AttributeFormat.STRING_TYPE, true,
                                                                                        "Specifies the title of the slide. (required)"),
                                                                    new AttributeFormat("topMargin", AttributeFormat.NUMERIC_TYPE, false,
                                                                                        "Specifies the top margin of the slide. (optional, default=0)"),
                                                                    new AttributeFormat("video", AttributeFormat.STRING_TYPE, false,
                                                                                        "Specifies the video file used for the presenter of the slide.If you specify video for the slide and an image for the presenter,the video is used instead of the image for the slide. You cannot specifyboth audio and video for a slide. The video must be an FLV file. (optional)")}));
    myTagAttributes.put("cfpresenter", new TagDescription(true,
                                                          "Describes a presenter in a slide presentation. A slide presentation can have multiple presenters.The presenters must be referenced from the slides defined by the cfpresentationslide tag.",
                                                          new AttributeFormat[]{
                                                            new AttributeFormat("biography", AttributeFormat.STRING_TYPE, true,
                                                                                "Specifies the biography of the presenter. (required)"),
                                                            new AttributeFormat("email", AttributeFormat.STRING_TYPE, false,
                                                                                "The name to use for the JavaScript proxy class. (optional)"),
                                                            new AttributeFormat("image", AttributeFormat.STRING_TYPE, false,
                                                                                "Specifies the path for the presenter's image in JPEG format.The JPG file must be relative to theCFM page. (optional)"),
                                                            new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                                "Specifies the name of the presenter. (required)"),
                                                            new AttributeFormat("logo", AttributeFormat.STRING_TYPE, false,
                                                                                "Specifies the path for the presenter's logo or the logo	ofthe presenters organization. The logo must be in JPEG format.The file must be relative to the CFM page. (optional)"),
                                                            new AttributeFormat("title", AttributeFormat.STRING_TYPE, false,
                                                                                "Specifies the title of the presenter. (optional)")}));
    myTagAttributes.put("cfthread", new TagDescription(true,
                                                       "The cfthread tag enables multithreaded programming in ColdFusion.Threads are independent streams of execution, and multiple threadson a page can execute simultaneously and asynchronously, letting youperform asynchronous processing in CFML. CFML code within the cfthreadtag body executes on a separate thread while the page request threadcontinues processing without waiting for the cfthread body to finish.You use this tag to run or end a thread, temporarily stop thread execution,or join together multiple threads.",
                                                       new AttributeFormat[]{
                                                         new AttributeFormat("action", AttributeFormat.STRING_TYPE, false,
                                                                             "The action to take, one of the following values:join Makes the current thread wait until the thread or threadsspecified in the name attribute complete processing, or until theperiod specified in the timeout attribute passes, before continuingprocessing. If you dont specify a timeout and thread you are joiningto doesnt finish, the current thread also cannot finish processing.run Creates a thread and starts it processing.sleep Suspends the current threads processing for the time specifiedby the duration attribute. This action is useful if one thread must waitfor another thread to do processing without joining the threads.terminate Stops processing of the thread specified in the name attribute.If you terminate a thread, the thread scope includes an ERROR metadatastructure with information about the termination. (optional, default=run)"),
                                                         new AttributeFormat("duration", AttributeFormat.STRING_TYPE, true,
                                                                             "(sleep) The number of milliseconds for which to suspend thread processing. (required)"),
                                                         new AttributeFormat("name", AttributeFormat.STRING_TYPE, false,
                                                                             "The name of the thread to which the action applies:terminate The name of the thread to stop.join The name of the thread or threads to join to the current thread.To specify multiple threads, use a comma-delimited list.run The name to use to identify the thread being created. (required)"),
                                                         new AttributeFormat("priority", AttributeFormat.STRING_TYPE, false,
                                                                             "The priority level at which to run the thread.The following values are valid:HIGH, LOW, NORMALHigher priority threads get more processing time than lower prioritythreads. Page-level code, the code that is outside of cfthread tags,always has NORMAL priority. (optional, default=NORMAL)"),
                                                         new AttributeFormat("timeout", AttributeFormat.NUMERIC_TYPE, false,
                                                                             "The number of milliseconds that the current thread waits forthe thread or threads being joined to finish. If any thread does notfinish by the specified time, the current thread proceeds.If the attribute value is 0, the default, the current thread continueswaiting until all joining threads finish. If the current thread is thepage thread, the page continues waiting until the threads are joined,even if you specify a page timeout. (optional, default=0)")}));
    myTagAttributes.put("cfzip", new TagDescription(false,
                                                    "Manipulates ZIP and JavaTM Archive (JAR) files.In addition to the basic zip and unzip functions, use the cfzip tag to deleteentries from an archive, filter files, read files in binary format, list thecontents of an archive, and specify an entrypath used in an executable JAR file.",
                                                    new AttributeFormat[]{new AttributeFormat("action", AttributeFormat.STRING_TYPE, false,
                                                                                              "The action to take. Must be one of the following:deletelistreadreadBinaryunzipzipIf you do not specify an action, ColdFusionapplies the default action, zip. (optional)"),
                                                      new AttributeFormat("charset", AttributeFormat.STRING_TYPE, false,
                                                                          "The character set used to translate the ZIP or JARentry into a text string. Examples of character sets are:JISRFC1345UTF-16 (optional, default=encoding of the host machine)"),
                                                      new AttributeFormat("destination", AttributeFormat.STRING_TYPE, false,
                                                                          "Destination directory where the ZIP or JAR file is extracted. (optional)"),
                                                      new AttributeFormat("entrypath", AttributeFormat.STRING_TYPE, false,
                                                                          "Pathname on which the action is performed. (optional)"),
                                                      new AttributeFormat("file", AttributeFormat.STRING_TYPE, true,
                                                                          "Absolute pathname of the file on which the action is performed.For example, the full pathname of the ZIP file: c:\\temp\\log.zip.If you do not specify the full pathname (for example, file=\"log.zip\"),ColdFusion creates the file in a temporary directory. You can use theGetTempDirectory function to access the ZIP or JAR file. (required)"),
                                                      new AttributeFormat("filter", AttributeFormat.STRING_TYPE, false,
                                                                          "File filter applied to the action. The actionapplies to all files in the pathname specified that match the filter. (optional)"),
                                                      new AttributeFormat("name", AttributeFormat.STRING_TYPE, true,
                                                                          "Record set name in which the result of the list action is stored.The record set columns are:name: filename of the entry in the JAR file. For example, if the entry ishelp/docs/index.htm, the name is index.htm.directory: directory containing the entry. For the example above, thedirectory is help/docs. You can obtain the full entry name by concatenatingdirectory and name. If an entry is at the root level, the directory is empty ('').size: uncompressed size of the entry, in bytes.compressedSize: compressed size of the entry, in bytes.type: type of entry (directory or file).dateLastModified: last modified date of the entry, cfdate object.comment: any comment, if present, for the entry.crc: crc-32 checksum of the uncompressed entry data. (required)"),
                                                      new AttributeFormat("overwrite", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "unzip: Specifies whether to overwrite the extracted files:yes: if the extracted file already exists at the destination specified,the file is overwritten.no: if the extracted file already exists at the destination specified,the file is not overwritten and that entry is not extracted. The remainingentries are extracted.zip: Specifies whether to overwrite the contents of a ZIP or JAR file:yes: overwrites all of the content in the ZIP or JAR file if it exists.no: updates existing entries and adds new entries to the ZIP or JAR fileif it exists. (optional, default=no)"),
                                                      new AttributeFormat("prefix", AttributeFormat.STRING_TYPE, false,
                                                                          "String added as a prefix to the ZIP or JAR entry.The string is the name of a subdirectory in which theentries are added. (optional)"),
                                                      new AttributeFormat("recurse", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "Specifies whether the actionapplies to subdirectories:yes: includes subdirectories.no: does not include subdirectories. (optional, default=yes)"),
                                                      new AttributeFormat("showDirectory", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "yes: lists the directories.no: does not list directories. (optional, default= no)"),
                                                      new AttributeFormat("source", AttributeFormat.STRING_TYPE, true,
                                                                          "Source directory to be zipped. Not requiredif cfzipparam is specified. (required)"),
                                                      new AttributeFormat("storePath", AttributeFormat.BOOLEAN_TYPE, false,
                                                                          "zip: Specifies whether pathnames are stored in the ZIP or JAR file:yes: pathnames of entries are stored in the ZIP or JAR file.no: pathnames of the entries are not stored in the ZIP or JAR file.All the files are placedat the root level. In case of a name conflict,the last file in the iteration is added.unzip: Specifies whether files are stored at the entrypath:yes: the files are extracted to the entrypath.no: the entrypath is ignored and all the files are extractedat the root level. (optional, default= yes)"),
                                                      new AttributeFormat("variable", AttributeFormat.STRING_TYPE, true,
                                                                          "Variable in which the read content is stored. (required)")}));
    myTagAttributes.put("cfzipparam", new TagDescription(true,
                                                         "Provides additional information to the cfzip tag.The cfzipparam tag is always a child tag of the cfzip tag.",
                                                         new AttributeFormat[]{
                                                           new AttributeFormat("charset", AttributeFormat.STRING_TYPE, true,
                                                                               "Converts string content into binary data before puttingit into a ZIP or JAR file. Used only when cfzipaction=\"zip\" and the cfzipparam content is a string.Examples of character sets are:JISRFC1345UTF-16 (optional, default=encoding of the host machine)"),
                                                           new AttributeFormat("content", AttributeFormat.STRING_TYPE, false,
                                                                               "Content written to the ZIP or JAR entry. Used only when cfzip action=\"zip\".Valid content data types are binary and string. If you specify the contentattribute, you must specify the entrypath attribute. (optional)"),
                                                           new AttributeFormat("entrypath", AttributeFormat.STRING_TYPE, false,
                                                                               "Pathanme used:For cfzip action=\"zip\", it is the entrypath used. This is valid onlywhen the source is a file. The entrypath creates a subdirectory withinthe ZIP or JAR file.For cfzip action=\"unzip\", it is the pathname to unzip.For cfzip action=\"delete\", it is the pathname to delete from theZIP or JAR file. (optional)"),
                                                           new AttributeFormat("filter", AttributeFormat.STRING_TYPE, false,
                                                                               "File filter applied to the action. For example, for the zip action,all the files in the source directory matching the filter are zipped. (optional)"),
                                                           new AttributeFormat("prefix", AttributeFormat.STRING_TYPE, false,
                                                                               "String added as a prefix to the ZIP or JAR entry. Used onlywhen cfzip action=\"zip\". (optional)"),
                                                           new AttributeFormat("recurse", AttributeFormat.BOOLEAN_TYPE, false,
                                                                               "Recurse the directory to be zipped, unzipped, or deleted,as specified by the cfzip parent tag. (optional, default=yes)"),
                                                           new AttributeFormat("source", AttributeFormat.STRING_TYPE, false,
                                                                               "Source directory or file. Used only when cfzip action=\"zip\".Specified file(s) are added to the ZIP or JAR file:If you specify source attribute for the cfzip tag, thecfzipparam source is relative to it.If you do not specify a source attribute for the cfziptag, the cfzipparam source must be an absolute path. (optional)")}));
  }

  // specifiing attributes predefined values
  private static void addValue(String tagName, String attributeName, String valueName) {
    TagDescription tagDescr = myTagAttributes.get(tagName);
    if (tagDescr == null) {
      return;
    }
    AttributeFormat result = null;
    for (AttributeFormat attribute : tagDescr.getAttributes()) {
      if (attribute.acceptName(attributeName)) {
        result = attribute;
        break;
      }
    }
    if (result == null) {
      return;
    }
    result.addValue(valueName);
  }

  private static class SAXEx extends DefaultHandler {
    private BufferedWriter bw;
    private String myCurrentTagName;
    private String myCurrentAttributeName;

    public void startDocument() throws SAXException {
      //try {
      //    bw = new BufferedWriter(new FileWriter("res.txt"));
      //} catch (IOException e) {
      //    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      //}
    }

    public void endDocument() throws SAXException {
      //try {
      //    bw.close();
      //} catch (IOException e) {
      //    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      //}
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes attr) throws SAXException {
      if (localName.equals("tag")) {
        myCurrentTagName = attr.getValue("name");
      }
      else if (localName.equals("parameter")) {
        myCurrentAttributeName = attr.getValue("name");
      }
      else if (localName.equals("value")) {
        String valueName = attr.getValue("default");
        if (valueName == null) {
          valueName = attr.getValue("option");
        }
        addValue(myCurrentTagName, myCurrentAttributeName, valueName);
      }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    }
  }

  static {
    int stackFrameCount = 2;
    Class callerClass = Reflection.getCallerClass(stackFrameCount);
    while (callerClass != null && callerClass.getClassLoader() == null) { // looks like a system class
      callerClass = Reflection.getCallerClass(++stackFrameCount);
    }
    if (callerClass == null) {
      callerClass = Reflection.getCallerClass(1);
    }
    URL url = callerClass.getResource("/cf8.xml");
    try {
      InputStream istream = URLUtil.openStream(url);
      InputSource isource = new InputSource(istream);

      XMLReader xr = XMLReaderFactory.createXMLReader();
      xr.setContentHandler(new SAXEx());
      xr.parse(isource);
    }
    catch (Exception e) {
    }
  }

  public static Set<String> getTagList() {
    return myTagAttributes.keySet();
  }

  public static boolean hasAnyAttributes(String tagName) {
    if (isUserDefined(tagName)) {
      return true;
    }
    if (myTagAttributes.get(tagName) != null && myTagAttributes.get(tagName).getAttributes() != null) {
      return myTagAttributes.get(tagName).getAttributes().length != 0;
    }
    return false;
  }

  public static List<AttributeFormat> getAttributes(String tagName) {
    if (myTagAttributes.get(tagName) != null && myTagAttributes.get(tagName).getAttributes() != null) {
      return Collections.unmodifiableList(Arrays.asList(myTagAttributes.get(tagName).getAttributes()));
    }
    return Collections.emptyList();
  }

  public static boolean isStandardTag(String tagName) {
    return myTagAttributes.containsKey(tagName);
  }

  public static boolean isAttribute(String tagName, String attributeName) {
    if (isUserDefined(tagName)) {
      return true;
    }

    if (!myTagAttributes.containsKey(tagName)) {
      return false;
    }
    TagDescription a = myTagAttributes.get(tagName);
    if (a.getAttributes() == null) {
      return false;
    }
    for (AttributeFormat af : a.getAttributes()) {
      if (af.acceptName(attributeName)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isUserDefined(String tagName) {
    return tagName != null && tagName.toLowerCase().startsWith("cf_");
  }

  public static boolean isSingleCfmlTag(String tagName) {
    if (isUserDefined(tagName)) {
      return false;
    }
    if (!myTagAttributes.containsKey(tagName)) {
      return false;
    }
    return myTagAttributes.get(tagName).isSingle();
  }

  public static String getTagDescription(String tagName) {
    if (!myTagAttributes.containsKey(tagName)) {
      return null;
    }
    TagDescription a = myTagAttributes.get(tagName);
    return "<div>Name: " +
           tagName +
           "</div>" +
           "<div>IsSingle: " +
           a.isSingle() +
           "</div>" +
           "<div>Descriprion: " +
           a.getDescription() +
           "</div>" +
           "<div>For more information visit <a href = \"http://livedocs.adobe.com/coldfusion/8/htmldocs/Tags-pt0_01.html\">" +
           "\"http://livedocs.adobe.com/coldfusion/8/htmldocs/Tags-pt0_01.html\"</div>";
  }

  public static String getAttributeDescription(String tagName, String attributeName) {
    AttributeFormat af = getAttribute(tagName, attributeName);
    if (af == null) {
      return "";
    }
    return af.toString();
  }

  public static AttributeFormat getAttribute(String tagName, String attributeName) {
    AttributeFormat[] attributesArray = myTagAttributes.get(tagName).getAttributes();
    for (AttributeFormat af : attributesArray) {
      if (af.acceptName(attributeName)) {
        return af;
      }
    }
    return null;
  }

  public static boolean isControlToken(IElementType type) {
    return type == CfmlTokenTypes.OPENER ||
           type == CfmlTokenTypes.CLOSER ||
           type == CfmlTokenTypes.LSLASH_ANGLEBRACKET ||
           type == CfmlTokenTypes.R_ANGLEBRACKET;
  }

  public static Set<String> getPredifinedFunctions() {
    return myPredefinedFunctions;
  }

  public static boolean isPredefinedFunction(String functionName) {
    return ourLowerCasePredefinedFunctions.contains(functionName.toLowerCase());
  }
}
