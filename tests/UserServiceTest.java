import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import model.*;
import repository.UserRepository;
import service.UserService;

/** Chạy riêng bằng javac/java; không kết nối DB và không cần JUnit. */
public class UserServiceTest {
    static int checks;
    static class FakeRepository extends UserRepository {
        User stored;
        User duplicate;
        User saved;
        boolean deactivated;
        String savedRole;
        public List<Role> findRoles() {
            return Arrays.asList(new Role(10,"ADMIN","","ACTIVE"), new Role(20,"CUSTOMER","","ACTIVE"),
                    new Role(30,"STAFF","","ACTIVE"), new Role(40,"CINEMA_MANAGER","","ACTIVE"),
                    new Role(50,"STAFF_OLD","","INACTIVE"));
        }
        public List<Cinema> findCinemas() {
            return Arrays.asList(new Cinema(1,"A","","","ACTIVE"), new Cinema(2,"B","","","INACTIVE"));
        }
        public User findById(int id) { return stored != null && stored.getUserId()==id ? stored : null; }
        public User findByEmail(String email) { return duplicate; }
        public void save(User u,String role) { saved=u; savedRole=role; }
        public boolean deactivate(int id) { deactivated=true; return id==2; }
    }
    static User user(int id,int role,Integer cinema) {
        User u=new User();u.setUserId(id);u.setRoleId(role);u.setCinemaId(cinema);
        u.setFullName(" Nguyễn Văn A ");u.setEmail("user@example.com");u.setPhone("0901234567");u.setStatus("ACTIVE");return u;
    }
    interface Action { void run() throws Exception; }
    static void rejects(Action action) throws Exception {
        try { action.run(); throw new AssertionError("Expected rejection"); }
        catch(IllegalArgumentException expected) { checks++; }
    }
    static void check(boolean value) { if(!value) throw new AssertionError(); checks++; }
    public static void main(String[] args) throws Exception {
        FakeRepository repo=new FakeRepository();UserService service=new UserService(repo);
        User u=user(0,20,1);service.save(u,"123456",1);
        check(u.getCinemaId()==null && "CUSTOMER".equals(repo.savedRole));
        check("e10adc3949ba59abbe56e057f20f883e".equals(u.getPasswordHash()));
        check("Nguyễn Văn A".equals(u.getFullName()));
        rejects(()->service.save(user(0,30,null),"123456",1));
        rejects(()->service.save(user(0,30,2),"123456",1));
        rejects(()->service.save(user(0,30,999),"123456",1));
        service.save(user(0,40,1),"123456",1);check("CINEMA_MANAGER".equals(repo.savedRole));
        rejects(()->service.save(user(0,999,null),"123456",1));
        rejects(()->service.save(user(0,50,null),"123456",1));
        rejects(()->service.save(user(0,20,null),"",1));
        rejects(()->service.save(user(0,20,null),"123",1));
        repo.duplicate=user(5,20,null);rejects(()->service.save(user(0,20,null),"123456",1));repo.duplicate=null;
        repo.stored=user(2,30,2);service.save(user(2,30,2),"",1);check(repo.saved.getPasswordHash()==null);
        rejects(()->service.save(user(3,20,null),"123456",1));
        repo.stored=user(1,10,null);rejects(()->service.save(user(1,20,null),"",1));
        User self=user(1,10,null);self.setStatus("INACTIVE");rejects(()->service.save(self,"",1));
        rejects(()->service.deactivate(1,1));check(!repo.deactivated);
        service.deactivate(2,1);check(repo.deactivated);
        rejects(()->service.deactivate(99,1));
        User invalid=user(0,20,null);invalid.setEmail("wrong");rejects(()->service.save(invalid,"123456",1));
        invalid.setEmail("a@b.com");invalid.setPhone("abc");rejects(()->service.save(invalid,"123456",1));
        check(service.isActiveAdmin(user(1,10,null)));check(!service.isActiveAdmin(user(2,20,null)));
        User disabled=user(1,10,null);disabled.setStatus("INACTIVE");check(!service.isActiveAdmin(disabled));
        check(!service.isActiveAdmin(null));
        System.out.println("PASS: "+checks+" service checks");
    }
}
