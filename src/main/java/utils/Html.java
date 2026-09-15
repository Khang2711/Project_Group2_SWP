package utils;
public final class Html {
 public static String e(Object o) {return o==null?"":o.toString().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
}
