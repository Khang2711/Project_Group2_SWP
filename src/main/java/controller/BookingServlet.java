package controller;

import dao.BookingDAO;
import dao.ShowtimeDAO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.CartItem;
import model.ComboItem;
import model.Showtime;
import model.Ticket;
import model.User;

@WebServlet(name = "BookingServlet", urlPatterns = {"/booking/seats", "/cart", "/checkout", "/booking/history"})
public class BookingServlet extends HttpServlet {

    private static final String SEAT_PAGE = "/WEB-INF/views/user/selectSeats.jsp";
    private static final String CART_PAGE = "/WEB-INF/views/user/cart.jsp";
    private static final String CHECKOUT_PAGE = "/WEB-INF/views/user/checkout.jsp";
    private static final String SUCCESS_PAGE = "/WEB-INF/views/user/checkoutSuccess.jsp";
    private static final String HISTORY_PAGE = "/WEB-INF/views/user/history.jsp";

    private User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("LOGIN_USER");
    }

    private boolean checkLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (getLoginUser(request) == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(HttpServletRequest request) {
        HttpSession session = request.getSession();
        List<CartItem> cart = (List<CartItem>) session.getAttribute("CART");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("CART", cart);
        }
        return cart;
    }

    @SuppressWarnings("unchecked")
    private List<ComboItem> getComboCart(HttpServletRequest request) {
        HttpSession session = request.getSession();
        List<ComboItem> comboCart = (List<ComboItem>) session.getAttribute("COMBO_CART");
        if (comboCart == null) {
            comboCart = new ArrayList<>();
            session.setAttribute("COMBO_CART", comboCart);
        }
        return comboCart;
    }

    private float getCartTotal(List<CartItem> cart, List<ComboItem> comboCart) {
        float total = 0;
        if (cart != null) {
            for (CartItem item : cart) {
                total += item.getPrice();
            }
        }
        if (comboCart != null) {
            for (ComboItem item : comboCart) {
                total += item.getTotal();
            }
        }
        return total;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!checkLogin(request, response)) {
            return;
        }

        String path = request.getServletPath();

        try {
            if ("/booking/history".equals(path)) {
                loadHistory(request, response);
                return;
            }

            if ("/cart".equals(path)) {
                String action = request.getParameter("action");
                if ("remove".equals(action)) {
                    removeCartItem(getCart(request), request.getParameter("key"));
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }
                if ("removeCombo".equals(action)) {
                    removeComboItem(getComboCart(request), request.getParameter("comboID"));
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }
                if ("clear".equals(action)) {
                    getCart(request).clear();
                    getComboCart(request).clear();
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }
                loadCartPage(request, response, null);
                return;
            }

            if ("/checkout".equals(path)) {
                loadCheckoutPage(request, response, null);
                return;
            }

            loadSeatPage(request, response, null);
        } catch (Exception ex) {
            Logger.getLogger(BookingServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute("error", "Không thể tải dữ liệu đặt vé.");
            request.getRequestDispatcher(SEAT_PAGE).forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!checkLogin(request, response)) {
            return;
        }

        String path = request.getServletPath();
        String action = request.getParameter("action");

        try {
            if ("/cart".equals(path)) {
                processCart(request, response, action);
                return;
            }

            if ("/checkout".equals(path)) {
                processCheckout(request, response);
                return;
            }

            response.sendRedirect(request.getContextPath() + "/movies");
        } catch (Exception ex) {
            Logger.getLogger(BookingServlet.class.getName()).log(Level.SEVERE, null, ex);
            try {
                if ("/checkout".equals(path)) {
                    loadCheckoutPage(request, response, ex.getMessage());
                } else {
                    loadCartPage(request, response, ex.getMessage());
                }
            } catch (Exception e) {
                Logger.getLogger(BookingServlet.class.getName()).log(Level.SEVERE, null, e);
                response.sendRedirect(request.getContextPath() + "/cart");
            }
        }
    }

    private void loadSeatPage(HttpServletRequest request, HttpServletResponse response, String error)
            throws Exception {

        String showtimeID = request.getParameter("showtimeID");
        Showtime showtime = new ShowtimeDAO().getBookableShowtimeByID(showtimeID);

        if (showtime == null) {
            request.setAttribute("error", "Suất chiếu không còn mở bán hoặc phòng đang bảo trì.");
            request.getRequestDispatcher(SEAT_PAGE).forward(request, response);
            return;
        }

        List<String> bookedSeats = new BookingDAO().getBookedSeats(showtimeID);
        request.setAttribute("showtime", showtime);
        request.setAttribute("bookedSeats", bookedSeats);
        if (error != null) {
            request.setAttribute("error", error);
        }
        request.getRequestDispatcher(SEAT_PAGE).forward(request, response);
    }

    private void loadCartPage(HttpServletRequest request, HttpServletResponse response, String error)
            throws Exception {

        List<CartItem> cart = getCart(request);
        List<ComboItem> comboCart = getComboCart(request);
        request.setAttribute("cart", cart);
        request.setAttribute("comboCart", comboCart);
        request.setAttribute("combos", new BookingDAO().getAllCombos());
        request.setAttribute("total", getCartTotal(cart, comboCart));
        if (error != null) {
            request.setAttribute("error", error);
        }
        request.getRequestDispatcher(CART_PAGE).forward(request, response);
    }

    private void loadCheckoutPage(HttpServletRequest request, HttpServletResponse response, String error)
            throws Exception {

        List<CartItem> cart = getCart(request);
        List<ComboItem> comboCart = getComboCart(request);
        request.setAttribute("cart", cart);
        request.setAttribute("comboCart", comboCart);
        request.setAttribute("total", getCartTotal(cart, comboCart));
        if (error != null) {
            request.setAttribute("error", error);
        }
        request.getRequestDispatcher(CHECKOUT_PAGE).forward(request, response);
    }

    private void processCart(HttpServletRequest request, HttpServletResponse response, String action)
            throws Exception {

        List<CartItem> cart = getCart(request);
        List<ComboItem> comboCart = getComboCart(request);

        if ("clear".equals(action)) {
            cart.clear();
            comboCart.clear();
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        if ("remove".equals(action)) {
            removeCartItem(cart, request.getParameter("key"));
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        if ("removeCombo".equals(action)) {
            removeComboItem(comboCart, request.getParameter("comboID"));
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        if ("addCombo".equals(action)) {
            addComboToCart(comboCart, request);
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        if ("updateCombos".equals(action) || "add-combo".equals(action)) {
            updateComboCart(comboCart, request);
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        addSeatsToCart(request, response, cart);
    }

    private void addSeatsToCart(HttpServletRequest request, HttpServletResponse response, List<CartItem> cart)
            throws Exception {

        String showtimeID = request.getParameter("showtimeID");
        String[] seats = request.getParameterValues("seatID");

        Showtime showtime = new ShowtimeDAO().getBookableShowtimeByID(showtimeID);
        if (showtime == null) {
            loadSeatPage(request, response, "Suất chiếu không còn mở bán hoặc phòng đang bảo trì.");
            return;
        }

        if (seats == null || seats.length == 0) {
            loadSeatPage(request, response, "Vui lòng chọn ít nhất một ghế.");
            return;
        }

        BookingDAO bookingDAO = new BookingDAO();
        for (String seatID : seats) {
            if (bookingDAO.isSeatBooked(showtimeID, seatID)) {
                loadSeatPage(request, response, "Ghế " + seatID + " đã được đặt.");
                return;
            }
            if (!cartContains(cart, showtimeID, seatID)) {
                cart.add(new CartItem(
                        showtime.getShowtimeID(),
                        showtime.getMovieTitle(),
                        showtime.getRoomName(),
                        showtime.getShowDate(),
                        showtime.getStartTime(),
                        showtime.getEndTime(),
                        seatID,
                        showtime.getBasePrice()
                ));
            }
        }

        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private int parseQuantity(String value) {
        try {
            int quantity = Integer.parseInt(value);
            return quantity < 0 ? 0 : quantity;
        } catch (Exception e) {
            return 0;
        }
    }

    private void addComboToCart(List<ComboItem> comboCart, HttpServletRequest request)
            throws Exception {

        String comboID = request.getParameter("comboID");
        int quantity = parseQuantity(request.getParameter("quantity"));
        if (quantity <= 0) {
            quantity = 1;
        }

        ComboItem combo = new BookingDAO().getComboByID(comboID);
        if (combo == null) {
            return;
        }

        for (ComboItem item : comboCart) {
            if (item.getComboID().equals(comboID)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        combo.setQuantity(quantity);
        comboCart.add(combo);
    }

    private void updateComboCart(List<ComboItem> comboCart, HttpServletRequest request)
            throws Exception {

        BookingDAO bookingDAO = new BookingDAO();
        List<ComboItem> combos = bookingDAO.getAllCombos();
        comboCart.clear();

        for (ComboItem combo : combos) {
            String value = request.getParameter("comboQuantity_" + combo.getComboID());
            if (value == null) {
                value = request.getParameter(combo.getComboID());
            }
            int quantity = parseQuantity(value);
            if (quantity > 0) {
                ComboItem item = bookingDAO.getComboByID(combo.getComboID());
                if (item != null) {
                    item.setQuantity(quantity);
                    comboCart.add(item);
                }
            }
        }
    }

    private void processCheckout(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        User user = getLoginUser(request);
        List<CartItem> cart = getCart(request);
        List<ComboItem> comboCart = getComboCart(request);

        if (cart.isEmpty() && comboCart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        ShowtimeDAO showtimeDAO = new ShowtimeDAO();
        BookingDAO bookingDAO = new BookingDAO();

        for (CartItem item : cart) {
            Showtime showtime = showtimeDAO.getBookableShowtimeByID(item.getShowtimeID());
            if (showtime == null) {
                throw new Exception("Có suất chiếu không còn mở bán hoặc phòng đang bảo trì.");
            }
            if (bookingDAO.isSeatBooked(item.getShowtimeID(), item.getSeatID())) {
                throw new Exception("Ghế " + item.getSeatID() + " đã được đặt.");
            }
        }

        float total = getCartTotal(cart, comboCart);
        bookingDAO.createBookingFromCart(user.getUserID(), cart, comboCart);
        request.setAttribute("total", total);
        cart.clear();
        comboCart.clear();
        request.getRequestDispatcher(SUCCESS_PAGE).forward(request, response);
    }

    private void loadHistory(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        User user = getLoginUser(request);
        List<Ticket> tickets = new BookingDAO().getHistoryByUser(user.getUserID());
        request.setAttribute("tickets", tickets);
        request.getRequestDispatcher(HISTORY_PAGE).forward(request, response);
    }

    private boolean cartContains(List<CartItem> cart, String showtimeID, String seatID) {
        for (CartItem item : cart) {
            if (item.getShowtimeID().equals(showtimeID) && item.getSeatID().equals(seatID)) {
                return true;
            }
        }
        return false;
    }

    private void removeCartItem(List<CartItem> cart, String key) {
        if (key == null) {
            return;
        }
        Iterator<CartItem> iterator = cart.iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (key.equals(item.getKey())) {
                iterator.remove();
                return;
            }
        }
    }

    private void removeComboItem(List<ComboItem> comboCart, String comboID) {
        if (comboID == null) {
            return;
        }
        Iterator<ComboItem> iterator = comboCart.iterator();
        while (iterator.hasNext()) {
            ComboItem item = iterator.next();
            if (comboID.equals(item.getComboID())) {
                iterator.remove();
                return;
            }
        }
    }
}
