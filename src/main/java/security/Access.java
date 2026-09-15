package security;
public final class Access {
 public static String home(String role) {
  if ("AD".equals(role)) return "/system/users";
  if ("MG".equals(role)) return "/admin/reports";
  if ("ST".equals(role)) return "/staff/checkin";
  return "/movies";
 }
 public static boolean allowed(String role,String path) {
  if(path.startsWith("/system/")) return "AD".equals(role);
  if(path.startsWith("/admin/")) return "MG".equals(role);
  if(path.startsWith("/staff/")) return "ST".equals(role);
  if(path.equals("/movies")||path.equals("/movie")||path.equals("/")) return role==null||"US".equals(role);
  return "US".equals(role) && (path.startsWith("/booking/")||path.equals("/cart")||path.equals("/checkout")||path.equals("/profile")||path.startsWith("/tickets/"));
 }
}
