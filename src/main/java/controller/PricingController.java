package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import model.Pricing;
import model.User;
import service.MovieService;
import service.PricingService;

@WebServlet(urlPatterns = {
    "/admin/pricing",
    "/admin/pricing-form"
})
public class PricingController
        extends HttpServlet {

    private final PricingService pricingService =
            new PricingService();

    private final MovieService movieService =
            new MovieService();

    @Override
    /** Hiển thị danh sách hoặc form giá vé. */
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user =
                getManager(
                        request,
                        response
                );

        if (user == null) {
            return;
        }
        //Lấy đường dẫn hiện tại để biết cần hiển thị trang nào
        String path =
                request.getServletPath();

        if ("/admin/pricing".equals(path)) {
            //Lấy các chính sách giá của rạp.
            loadPricingList(
                    request,
                    user
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/pricing.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }

        if ("/admin/pricing-form".equals(path)) {

            String id =
                    request.getParameter("id");
            //Không có id tạo/ có id sửa
            if (id != null
                    && !id.trim().isEmpty()) {

                try {
                    //Lấy chính sách theo cả pricingId và rạp của Manager.
                    int pricingId =
                            Integer.parseInt(id);

                    Pricing pricing =
                            pricingService
                                    .getPricing(
                                            pricingId,
                                            user.getCinemaId()
                                    );

                    if (pricing == null) {

                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND,
                                "Không tìm thấy giá vé."
                        );

                        return;
                    }
                    //Đưa thông tin giá hiện tại sang form.
                    request.setAttribute(
                            "pricing",
                            pricing
                    );

                } catch (NumberFormatException e) {

                    response.sendError(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Mã giá vé không hợp lệ."
                    );

                    return;
                }
            }
            //Tải danh sách phim của rạp để hiển thị trong form.
            loadMovies(
                    request,
                    user
            );
            //Hiển thị trang thêm/sửa chính sách giá.
            request.getRequestDispatcher(
                    "/WEB-INF/views/admin/pricing-form.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }

        response.sendError(
                HttpServletResponse.SC_NOT_FOUND
        );
    }

    @Override
    /** Xử lý thêm, sửa và xóa chính sách giá. */
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        User user =
                getManager(
                        request,
                        response
                );

        if (user == null) {
            return;
        }

        String path =
                request.getServletPath();
        //Chỉ chấp nhận thao tác action=delete.
        if ("/admin/pricing".equals(path)) {

            String action =
                    request.getParameter(
                            "action"
                    );

            if (!"delete".equals(action)) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                return;
            }

            try {
                //Lấy ID chính sách muốn xóa.
                int pricingId =
                        Integer.parseInt(
                                request.getParameter(
                                        "id"
                                )
                        );
                //Gọi Service kiểm tra và xóa chính sách thuộc rạp của Manager.
                pricingService.deletePricing(
                        pricingId,
                        user.getCinemaId()
                );
                //Nếu thành công
                //Chuyển về danh sách chính sách giá.
                response.sendRedirect(
                        request.getContextPath()
                        + "/admin/pricing?deleted=1"
                );

            } catch (NumberFormatException e) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );

            } catch (IllegalArgumentException e) {

                request.setAttribute(
                        "error",
                        e.getMessage()
                );

                loadPricingList(
                        request,
                        user
                );

                request.getRequestDispatcher(
                        "/WEB-INF/views/admin/pricing.jsp"
                ).forward(
                        request,
                        response
                );
            }

            return;
        }

        if (!"/admin/pricing-form".equals(path)) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        try {
            //Nếu POST vào /admin/pricing-form, Controller bắt đầu đọc các dữ liệu từ form:
            String id =
                    request.getParameter("id");

            Integer movieId =
                    parseOptionalInteger(
                            request.getParameter(
                                    "movieId"
                            )
                    );

            String roomType =
                    request.getParameter(
                            "roomType"
                    );

            String seatType =
                    request.getParameter(
                            "seatType"
                    );

            String dayType =
                    request.getParameter(
                            "dayType"
                    );
            //Các hàm parse...() giúp chuyển chuỗi do form gửi lên thành đúng kiểu dữ liệu Java.
            Date specialDate =
                    parseOptionalDate(
                            request.getParameter(
                                    "specialDate"
                            ),
                            "Ngày đặc biệt không hợp lệ."
                    );

            Time startTime =
                    parseOptionalTime(
                            request.getParameter(
                                    "startTime"
                            ),
                            "Giờ bắt đầu không hợp lệ."
                    );

            Time endTime =
                    parseOptionalTime(
                            request.getParameter(
                                    "endTime"
                            ),
                            "Giờ kết thúc không hợp lệ."
                    );

            BigDecimal price =
                    parsePrice(
                            request.getParameter(
                                    "price"
                            )
                    );

            Date startDate =
                    parseRequiredDate(
                            request.getParameter(
                                    "startDate"
                            )
                    );

            Date endDate =
                    parseOptionalDate(
                            request.getParameter(
                                    "endDate"
                            ),
                            "Ngày kết thúc không hợp lệ."
                    );

            String status =
                    request.getParameter(
                            "status"
                    );
            //Nếu ID rỗng → gọi createPricing().
            if (id == null
                    || id.trim().isEmpty()) {

                pricingService.createPricing(
                        user.getCinemaId(),
                        movieId,
                        roomType,
                        seatType,
                        dayType,
                        specialDate,
                        startTime,
                        endTime,
                        price,
                        startDate,
                        endDate,
                        status
                );

            } else {
                //Nếu có ID → gọi updatePricing().
                int pricingId =
                        Integer.parseInt(id);

                pricingService.updatePricing(
                        pricingId,
                        user.getCinemaId(),
                        movieId,
                        roomType,
                        seatType,
                        dayType,
                        specialDate,
                        startTime,
                        endTime,
                        price,
                        startDate,
                        endDate,
                        status
                );
            }
            //Khi thành công
            //Chuyển về danh sách.
            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/pricing?saved=1"
            );
            //Nếu dữ liệu sai hoặc Service báo lỗi
        } catch (NumberFormatException e) {

            request.setAttribute(
                    "error",
                    "Giá vé hoặc mã dữ liệu không hợp lệ."
            );

            forwardForm(
                    request,
                    response,
                    user
            );

        } catch (IllegalArgumentException e) {

            request.setAttribute(
                    "error",
                    e.getMessage()
            );

            forwardForm(
                    request,
                    response,
                    user
            );
        }
    }

    /** Đọc và kiểm tra giá vé người dùng nhập. 
     *      Kiểm tra giá không được bỏ trống.
     *      Chuyển chuỗi thành BigDecimal.
     *      Báo lỗi nếu nhập không đúng định dạng số.
     * 
     *  ***Giá phải lớn hơn 0 được kiểm tra ở PricingService.validatePricing(), không phải trong parsePrice().
     */
    private BigDecimal parsePrice(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Giá vé không được để trống."
            );
        }

        try {

            return new BigDecimal(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Giá vé không hợp lệ."
            );
        }
    }

    /** Đọc số nguyên tùy chọn; để trống thì trả null. */
    private Integer parseOptionalInteger(
            String value) {
        //Dùng cho movieId.
        //Nếu không chọn phim thì trả null; nếu có thì chuyển thành số nguyên.
        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            return Integer.valueOf(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Phim không hợp lệ."
            );
        }
    }

    /** Đọc ngày bắt buộc và kiểm tra định dạng. */
    private Date parseRequiredDate(
            String value) {
        //Dùng cho ngày bắt đầu, bắt buộc phải có giá trị hợp lệ.
        if (value == null
                || value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được để trống."
            );
        }

        try {

            return Date.valueOf(
                    value.trim()
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Ngày bắt đầu không hợp lệ."
            );
        }
    }

    /** Đọc ngày tùy chọn; để trống thì trả null. */
    private Date parseOptionalDate(
            String value,
            String errorMessage) {
        //Dùng cho ngày đặc biệt và ngày kết thúc. Để trống thì trả null.
        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            return Date.valueOf(
                    value.trim()
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    errorMessage
            );
        }
    }

    /** Đọc giờ tùy chọn; để trống thì trả null. */
    private Time parseOptionalTime(
            String value,
            String errorMessage) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {
            //LocalTime.parse(): Chuyển chuỗi thời gian như "18:30" thành LocalTime.
            LocalTime localTime =
                    LocalTime.parse(
                            value.trim()
                    );
            //Time.valueOf(): Chuyển sang java.sql.Time để sử dụng với JDBC.
            return Time.valueOf(
                    localTime
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    errorMessage
            );
        }
    }

    /** Tải các chính sách giá của rạp để hiển thị trên pricing.jsp. */
    private void loadPricingList(
            HttpServletRequest request,
            User user) {

        request.setAttribute(
                "pricingList",
                pricingService
                        .getPricingByCinema(
                                user.getCinemaId()
                        )
        );
    }

    /** Tải danh sách phim cho form giá. */
    private void loadMovies(
            HttpServletRequest request,
            User user) {

        request.setAttribute(
                "movies",
                movieService
                        .getMoviesByCinema(
                                user.getCinemaId()
                        )
        );
    }

    /** Mở lại form khi nhập sai. */
    private void forwardForm(
            HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws ServletException, IOException {
        //Tạo đối tượng Pricing tạm.
        Pricing pricing =
                new Pricing();
        //Sau đó đọc lại các trường
        try {

            String id =
                    request.getParameter("id");

            if (id != null
                    && !id.trim().isEmpty()) {

                pricing.setPricingId(
                        Integer.parseInt(id)
                );
            }

        } catch (Exception e) {
            pricing.setPricingId(0);
        }

        pricing.setCinemaId(
                user.getCinemaId()
        );

        try {

            pricing.setMovieId(
                    parseOptionalInteger(
                            request.getParameter(
                                    "movieId"
                            )
                    )
            );

        } catch (Exception e) {
            pricing.setMovieId(null);
        }

        pricing.setRoomType(
                request.getParameter(
                        "roomType"
                )
        );

        pricing.setSeatType(
                request.getParameter(
                        "seatType"
                )
        );

        pricing.setDayType(
                request.getParameter(
                        "dayType"
                )
        );

        try {

            pricing.setSpecialDate(
                    parseOptionalDate(
                            request.getParameter(
                                    "specialDate"
                            ),
                            ""
                    )
            );

        } catch (Exception e) {
            pricing.setSpecialDate(null);
        }

        try {

            pricing.setStartTime(
                    parseOptionalTime(
                            request.getParameter(
                                    "startTime"
                            ),
                            ""
                    )
            );

        } catch (Exception e) {
            pricing.setStartTime(null);
        }

        try {

            pricing.setEndTime(
                    parseOptionalTime(
                            request.getParameter(
                                    "endTime"
                            ),
                            ""
                    )
            );

        } catch (Exception e) {
            pricing.setEndTime(null);
        }

        try {

            pricing.setPrice(
                    parsePrice(
                            request.getParameter(
                                    "price"
                            )
                    )
            );

        } catch (Exception e) {
            pricing.setPrice(null);
        }

        try {

            String value =
                    request.getParameter(
                            "startDate"
                    );

            if (value != null
                    && !value.trim().isEmpty()) {

                pricing.setStartDate(
                        Date.valueOf(
                                value.trim()
                        )
                );
            }

        } catch (Exception e) {
            pricing.setStartDate(null);
        }

        try {

            pricing.setEndDate(
                    parseOptionalDate(
                            request.getParameter(
                                    "endDate"
                            ),
                            ""
                    )
            );

        } catch (Exception e) {
            pricing.setEndDate(null);
        }

        pricing.setStatus(
                request.getParameter(
                        "status"
                )
        );
        //Gửi đối tượng Pricing cùng danh sách phim trở lại form.
        request.setAttribute(
                "pricing",
                pricing
        );

        loadMovies(
                request,
                user
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/pricing-form.jsp"
        ).forward(
                request,
                response
        );
    }

    /** Kiểm tra quyền và rạp của quản lý. */
    private User getManager(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        //Chưa đăng nhập → chuyển về trang login.
        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute(
                        "user"
                ) == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/auth/login"
            );

            return null;
        }
        //Không phải Cinema Manager → HTTP 403.
        String roleName =
                (String) session.getAttribute(
                        "roleName"
                );

        if (!"CINEMA_MANAGER".equals(
                roleName)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return null;
        }
        //Manager chưa có rạp → không được quản lý giá.
        User user =
                (User) session.getAttribute(
                        "user"
                );

        if (user.getCinemaId() == null) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Tài khoản Manager chưa được phân công rạp."
            );

            return null;
        }
        //Trả User để Controller sử dụng user.getCinemaId().
        return user;
    }
}