
package com.siyeh.igtest.style.unnecessary_semicolon;;
;
public class UnnecessarySemicolonInspection
{
    int i;
    ; // comment
    public UnnecessarySemicolonInspection()
    {
        ; // this is a comment
        ; /* another */
    }
}  ; // comment
enum Status
{
    BUSY    ("BUSY"    ),
    DONE    ("DONE"    )  ; // <<---- THIS IS NOT UNNECESSARY

    public final String text;   ;
    private Status (String i_text) {
        text = i_text;
    }
}
enum BuildType
{
    ;

    public String toString() {
        return super.toString();
    }
}