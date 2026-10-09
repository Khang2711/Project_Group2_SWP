
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />

<section class="showtimes-page">

    <!-- PAGE HEADER -->
    <div class="showtimes-header">
        <h1>CHỌN SUẤT CHIẾU</h1>

        <div class="showtimes-steps">
            <span class="active">1. Suất chiếu</span>
            <span>→</span>
            <span>2. Chọn ghế</span>
            <span>→</span>
            <span>3. Thanh toán</span>
        </div>
    </div>

    <!-- MOVIE INFORMATION -->
    <div class="showtimes-movie">

        <div class="showtimes-poster">
            <c:choose>
                <c:when test="${not empty movie.posterUrl}">
                    <img src="${movie.posterUrl}" alt="${movie.title}">
                </c:when>

                <c:otherwise>
                    <div class="showtimes-no-poster">
                        F-CINEMA
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="showtimes-movie-info">

            <h2><c:out value="${movie.title}" /></h2>

            <p>
                Thời lượng: ${movie.durationMinutes} phút
            </p>

            <c:if test="${not empty movie.ageRating}">
                <p>Phân loại: ${movie.ageRating}</p>
            </c:if>

            <a href="${ctx}/user/movie-detail?id=${movie.movieId}"
               class="showtimes-detail-button">
                Xem chi tiết phim
            </a>

        </div>
    </div>

    <!-- SHOWTIME CONTENT -->
    <div class="showtimes-layout">

        <div class="showtimes-content">

            <!-- SELECT DATE -->
            <div class="showtimes-date-section">

                <h3>Chọn ngày xem</h3>

                <div class="showtimes-dates">

                    <c:forEach var="day" items="${days}">

                        <a href="${ctx}/user/showtimes?movieId=${movie.movieId}&date=${day}&cinemaId=${selectedCinemaId}"
                           class="showtimes-date ${selectedDate eq day ? 'active' : ''}">

                            <span>
                                <fmt:formatDate
                                    value="${day}"
                                    pattern="EEE" />
                            </span>

                            <strong>
                                <fmt:formatDate
                                    value="${day}"
                                    pattern="dd" />
                            </strong>

                            <small>
                                <fmt:formatDate
                                    value="${day}"
                                    pattern="MM/yyyy" />
                            </small>

                        </a>

                    </c:forEach>

                </div>
            </div>

            <!-- CINEMA FILTER -->
            <div class="showtimes-filter">

                <h3>Chọn rạp</h3>

                <form action="${ctx}/user/showtimes" method="get">

                    <input type="hidden"
                           name="movieId"
                           value="${movie.movieId}">

                    <input type="hidden"
                           name="date"
                           value="${selectedDate}">

                    <select name="cinemaId" id="cinemaFilter">

                        <option value="0">Tất cả rạp</option>

                        <c:forEach var="cinema" items="${cinemas}">

                            <option value="${cinema.cinemaId}"
                                    ${selectedCinemaId eq cinema.cinemaId ? 'selected' : ''}>
                                <c:out value="${cinema.cinemaName}" />
                            </option>

                        </c:forEach>

                    </select>

                </form>
            </div>

            <!-- SHOWTIME LIST -->
            <div class="showtimes-list">

                <h3>
                    Suất chiếu ngày
                    <fmt:formatDate
                        value="${selectedDate}"
                        pattern="dd/MM/yyyy" />
                </h3>

                <c:choose>

                    <c:when test="${not hasShowtimes}">

                        <div class="showtimes-empty">
                            Không có suất chiếu trong ngày này.
                            Vui lòng chọn ngày hoặc rạp khác.
                        </div>

                    </c:when>

                    <c:otherwise>

                        <c:forEach var="cinema" items="${cinemas}">

                            <c:if test="${not empty cinemaSlots[cinema.cinemaId]
                                          and (selectedCinemaId eq 0
                                          or selectedCinemaId eq cinema.cinemaId)}">

                                  <div class="showtimes-cinema">

                                      <!-- CINEMA -->
                                      <div class="showtimes-cinema-info">

                                          <h4 class="showtimes-cinema-name">
                                              <c:out value="${cinema.cinemaName}" />
                                          </h4>

                                          <p>
                                              <c:out value="${cinema.address}" />
                                          </p>

                                      </div>

                                      <!-- SHOWTIME SLOTS -->
                                      <div class="showtimes-slots">

                                          <c:forEach var="slot"
                                                     items="${cinemaSlots[cinema.cinemaId]}">

                                              <c:set var="room"
                                                     value="${roomsById[slot.roomId]}" />

                                              <button type="button"
                                                      class="showtimes-slot"
                                                      data-id="${slot.showtimeId}"
                                                      data-start="${slot.startTime.time}"
                                                      ${slot.startTime.time le nowMillis ? 'disabled' : ''}>

                                                  <strong class="showtimes-time">
                                                      <fmt:formatDate
                                                          value="${slot.startTime}"
                                                          pattern="HH:mm" />
                                                  </strong>

                                                  <span class="showtimes-room">
                                                      <c:out value="${room.roomName}" />
                                                  </span>

                                                  <small>
                                                      <c:out value="${room.roomType}" />
                                                  </small>

                                              </button>

                                          </c:forEach>

                                      </div>
                                  </div>

                            </c:if>

                        </c:forEach>

                        <p class="showtimes-note">
                            Các suất chiếu đã bắt đầu sẽ bị mờ
                            và không thể lựa chọn.
                        </p>

                    </c:otherwise>

                </c:choose>

            </div>
        </div>

        <!-- BOOKING SUMMARY -->
        <div class="showtimes-summary">

            <h3>THÔNG TIN ĐẶT VÉ</h3>

            <div class="showtimes-summary-row">
                <span>Phim</span>
                <strong><c:out value="${movie.title}" /></strong>
            </div>

            <div class="showtimes-summary-row">
                <span>Ngày xem</span>
                <strong>
                    <fmt:formatDate
                        value="${selectedDate}"
                        pattern="dd/MM/yyyy" />
                </strong>
            </div>

            <div class="showtimes-summary-row">
                <span>Rạp</span>
                <strong id="selectedCinema">Chưa chọn</strong>
            </div>

            <div class="showtimes-summary-row">
                <span>Phòng</span>
                <strong id="selectedRoom">Chưa chọn</strong>
            </div>

            <div class="showtimes-summary-row">
                <span>Giờ chiếu</span>
                <strong id="selectedTime">Chưa chọn</strong>
            </div>

            <!-- CONTINUE TO BOOKING -->
            <form action="${ctx}/user/select-seats"
                  method="get"
                  id="bookingForm">

                <input type="hidden"
                       name="showtimeId"
                       id="selectedShowtimeId">

                <button type="submit"
                        class="showtimes-book-button"
                        id="continueButton"
                        disabled>
                    TIẾP TỤC CHỌN GHẾ →
                </button>

            </form>

            <p class="showtimes-note">
                Vui lòng chọn suất chiếu trước khi tiếp tục.
            </p>

        </div>
    </div>

</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>
