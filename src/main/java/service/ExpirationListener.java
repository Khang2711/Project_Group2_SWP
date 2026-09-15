package service;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.*;

@WebListener
public class ExpirationListener implements ServletContextListener {

    private ScheduledExecutorService worker;

    public void contextInitialized(ServletContextEvent e) {
        worker = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "fcinema-expiration");
            t.setDaemon(true);
            return t;
        });
        worker.scheduleWithFixedDelay(() -> {
            try {
                BookingService.expire();
            } catch (Exception ex) {
                e.getServletContext().log("Reservation expiration failed", ex);
            }
        }, 10, 30, TimeUnit.SECONDS);
    }

    public void contextDestroyed(ServletContextEvent e) {
        if (worker != null) {
            worker.shutdownNow();
        }
    }
}
