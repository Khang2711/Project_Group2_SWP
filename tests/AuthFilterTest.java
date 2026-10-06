package security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import model.Role;
import model.User;
import repository.RoleRepository;
import repository.UserRepository;

/** Kiểm tra session cũ sau khi admin đổi trạng thái/quyền. Không dùng DB thật. */
public class AuthFilterTest {
    static int checks;
    static class Users extends UserRepository {
        User current;
        boolean fail;
        int calls;
        public User findById(int id) throws SQLException {
            calls++;
            if (fail) throw new SQLException("test outage");
            return current;
        }
    }
    static class Roles extends RoleRepository {
        Role current = new Role(1,"ADMIN","","ACTIVE");
        public Role findById(int id) { return current; }
    }
    static class Fixture {
        Users users = new Users();
        Roles roles = new Roles();
        Map<String,Object> attributes = new HashMap<>();
        String path = "/system/users";
        String redirect;
        int status;
        boolean invalidated;
        boolean reached;
        boolean guest;
        Fixture() {
            User u=new User();u.setUserId(1);u.setRoleId(1);u.setStatus("ACTIVE");u.setPasswordHash("secret");
            users.current=u;attributes.put("user",u);attributes.put("roleName","ADMIN");
        }
        void run() throws Exception {
            HttpSession session = (HttpSession) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{HttpSession.class}, (p,m,a)->{
                switch(m.getName()) {
                    case "getAttribute": return attributes.get(a[0]);
                    case "setAttribute": attributes.put((String)a[0],a[1]);return null;
                    case "invalidate": invalidated=true;attributes.clear();return null;
                    default:return null;
                }
            });
            ServletContext context=(ServletContext)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{ServletContext.class},(p,m,a)->null);
            HttpServletRequest request=(HttpServletRequest)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{HttpServletRequest.class},(p,m,a)->{
                switch(m.getName()) {
                    case "getServletPath":return path;
                    case "getContextPath":return "/FCinema";
                    case "getSession":return guest?null:session;
                    case "getServletContext":return context;
                    default:return null;
                }
            });
            HttpServletResponse response=(HttpServletResponse)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{HttpServletResponse.class},(p,m,a)->{
                if(m.getName().equals("sendRedirect"))redirect=(String)a[0];
                if(m.getName().equals("sendError"))status=(int)a[0];
                return null;
            });
            new AuthFilter(users,roles).doFilter(request,response,(a,b)->reached=true);
        }
    }
    static void check(boolean value) {if(!value)throw new AssertionError();checks++;}
    public static void main(String[] args) throws Exception {
        Fixture f=new Fixture();f.guest=true;f.run();check(!f.reached && f.redirect.endsWith("/auth/login"));
        f=new Fixture();f.run();check(f.reached && ((User)f.attributes.get("user")).getPasswordHash()==null);
        f=new Fixture();f.users.current.setStatus("INACTIVE");f.run();check(!f.reached && f.invalidated);
        f=new Fixture();f.users.current=null;f.run();check(!f.reached && f.invalidated);
        f=new Fixture();f.roles.current.setStatus("INACTIVE");f.run();check(!f.reached && f.invalidated);
        f=new Fixture();f.roles.current.setRoleName("CUSTOMER");f.run();check(!f.reached && f.status==403 && "CUSTOMER".equals(f.attributes.get("roleName")));
        f=new Fixture();f.attributes.put("roleName","CUSTOMER");f.run();check(f.reached && "ADMIN".equals(f.attributes.get("roleName")));
        f=new Fixture();f.users.fail=true;f.run();check(!f.reached && f.status==500);
        f=new Fixture();f.path="/user/movies";f.guest=true;f.run();check(f.reached && f.users.calls==0);
        f=new Fixture();f.path="/staff/check-in";f.roles.current.setRoleName("STAFF");f.run();check(f.reached);
        f=new Fixture();f.path="/admin/dashboard";f.roles.current.setRoleName("CINEMA_MANAGER");f.run();check(f.reached);
        f=new Fixture();f.path="/user/bookings";f.roles.current.setRoleName("CUSTOMER");f.run();check(f.reached);
        System.out.println("PASS: "+checks+" filter checks");
    }
}
