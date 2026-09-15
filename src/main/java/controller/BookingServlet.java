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
                loadCartPage(request, response, null);
                return;
            }

            if ("/checkout".equals(path)) {
                loadCheckoutPage(request, response, null);
                return;
            }

            loadSeatPage(request, response, null);
        } catch (ServletException | IOException ex) {
            // Preserve rendering failures; forwarding to the same broken JSP hides the cause.
            throw ex;
        } catch (Exception ex) {
            Logger.getLogger(BookingServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute("error", bookingError(ex));
            String page="/cart".equals(path)?CART_PAGE:"/checkout".equals(path)?CHECKOUT_PAGE:"/booking/history".equals(path)?HISTORY_PAGE:SEAT_PAGE;
            request.getRequestDispatcher(page).forward(request,response);
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
                    loadCheckoutPage(request, response, bookingError(ex));
                } else {
                    if("add".equals(action)) loadSeatPage(request,response,bookingError(ex));
                    else loadCartPage(request, response, bookingError(ex));
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

        service.BookingService.expire();
        List<String> bookedSeats = new BookingDAO().getBookedSeats(showtimeID);
        for(java.util.Map<String,Object> row:dao.Sql.rows("SELECT seatID FROM tblReservations WHERE showtimeID=?",showtimeID))bookedSeats.add(row.get("seatID").toString());
        request.setAttribute("physicalSeats",dao.Sql.rows("SELECT se.seatID,se.seatType,se.status FROM tblSeats se WHERE se.roomID=? ORDER BY LEFT(se.seatID,1),TRY_CAST(SUBSTRING(se.seatID,2,9) AS int)",showtime.getRoomID()));

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
        request.setAttribute("paymentConfigured", service.VNPay.isConfigured());
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
            request.getSession().removeAttribute("FULL_THEATER");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        if ("remove".equals(action)) {
            removeCartItem(cart, request.getParameter("key"));
            request.getSession().removeAttribute("FULL_THEATER");
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

        if(!"add".equals(action)) throw new IllegalArgumentException("Thao tác giỏ vé không hợp lệ. Vui lòng tải lại trang.");
        synchronized(request.getSession()) {addSeatsToCart(request, response, cart);}
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

        if (!cart.isEmpty() && !cart.get(0).getShowtimeID().equals(showtimeID)) {
            loadSeatPage(request,response,"Mỗi đơn chỉ đặt một suất chiếu. Vui lòng hoàn tất hoặc xóa giỏ vé trước.");return;
        }
        if(seats.length>260)throw new IllegalArgumentException("Số ghế không hợp lệ.");
        java.util.Set<String> selected=new java.util.LinkedHashSet<>(java.util.Arrays.asList(seats));
        List<CartItem> additions=new ArrayList<>();
        service.BookingService.expire();
        java.util.Set<String> unavailable=new java.util.HashSet<>(new BookingDAO().getBookedSeats(showtimeID));
        for(java.util.Map<String,Object> hold:dao.Sql.rows("SELECT seatID FROM tblReservations WHERE showtimeID=?",showtimeID))unavailable.add(hold.get("seatID").toString());
        // Validate all seats first; a rejected selection must not partly change the cart.
        try(java.sql.Connection c=utils.DBUtils.getConnection()) {
            for(String seat:selected) {
                if(unavailable.contains(seat))throw new IllegalArgumentException("Ghế "+seat+" đã được đặt hoặc đang được giữ. Vui lòng chọn ghế khác.");
                float price=service.BookingService.money(service.BookingService.quoteSeat(c,showtimeID,seat).get("price")).floatValue();
                if(!cartContains(cart,showtimeID,seat))additions.add(new CartItem(showtimeID,showtime.getMovieTitle(),showtime.getRoomName(),showtime.getShowDate(),showtime.getStartTime(),showtime.getEndTime(),seat,price));
            }
        }
        cart.addAll(additions);
        request.getSession().setAttribute("FULL_THEATER","true".equals(request.getParameter("fullTheater")));

        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private int parseQuantity(String value) {
        if(value==null||value.isBlank())return 0;
        try {int quantity=Integer.parseInt(value);if(quantity<0||quantity>20)throw new NumberFormatException();return quantity;}
        catch(NumberFormatException ex){throw new IllegalArgumentException("Số lượng mỗi combo phải từ 0 đến 20.");}
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
                if(item.getQuantity()+quantity>20)throw new IllegalArgumentException("Mỗi combo tối đa 20 phần.");
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
        List<ComboItem> updatedCombos=new ArrayList<>();

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
                    updatedCombos.add(item);
                }
            }
        }
        comboCart.clear();comboCart.addAll(updatedCombos);
    }

    private void processCheckout(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        synchronized(request.getSession()) {
            Object previous=request.getSession().getAttribute("PENDING_ORDER");
            if(previous!=null && getCart(request).isEmpty()) {
                java.util.List<java.util.Map<String,Object>> existing=dao.Sql.rows("SELECT orderID FROM tblOrders WHERE orderID=? AND userID=? AND paymentState='PENDING' AND expiresAt>SYSDATETIME()",previous,getLoginUser(request).getUserID());
                if(!existing.isEmpty()){response.sendRedirect(request.getContextPath()+"/booking/orders?orderID="+previous);return;}
            }
            java.util.Map<String,Object> order=new service.BookingService().reserve(getLoginUser(request).getUserID(),getCart(request),getComboCart(request),request.getParameter("promoCode"),Boolean.TRUE.equals(request.getSession().getAttribute("FULL_THEATER")));
            request.getSession().setAttribute("PENDING_ORDER",order.get("orderID"));
            getCart(request).clear();getComboCart(request).clear();
            request.getSession().removeAttribute("FULL_THEATER");
            response.sendRedirect(request.getContextPath()+"/booking/orders?orderID="+order.get("orderID"));
        }
    }

    private void loadHistory(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        User user = getLoginUser(request);
        List<Ticket> tickets = new BookingDAO().getHistoryByUser(user.getUserID());
        request.setAttribute("tickets", tickets);
        request.getRequestDispatcher(HISTORY_PAGE).forward(request, response);
    }

    private String bookingError(Exception error) {
        if(error instanceof IllegalArgumentException)return error.getMessage();
        if(error instanceof java.sql.SQLException){int code=((java.sql.SQLException)error).getErrorCode();
            if(code==2601||code==2627)return "Ghế vừa được người khác đặt hoặc giữ. Vui lòng chọn lại ghế.";
            if(code==207||code==208)return "Hệ thống đặt vé chưa được cập nhật dữ liệu. Vui lòng báo quản lý rạp.";
            if(code==1205)return "Có yêu cầu đặt vé đồng thời. Vui lòng thử lại; đơn chưa được tạo.";
        }
        return "Không thể xử lý đặt vé lúc này. Vui lòng thử lại hoặc liên hệ rạp.";
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
