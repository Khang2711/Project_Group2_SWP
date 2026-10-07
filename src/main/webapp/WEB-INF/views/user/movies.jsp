<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<section class="schedule-page">

    <div class="schedule-title">

        <h1>Hôm nay, xem gì?</h1>

        <p>Khám phá phim, chọn rạp và tìm suất chiếu dành cho bạn.</p>

    </div>


    <form class="schedule-search"
          action="${ctx}/user/movies"
          method="get">

        <input type="text"
               name="keyword"
               class="schedule-keyword"
               value="<c:out value='${keyword}'/>"
               placeholder="Tìm tên phim"
               aria-label="Tìm tên phim">

        <div class="date-picker-wrapper">

            <input type="text"
                   id="dateDisplay"
                   class="date-display"
                   placeholder="dd/mm/yyyy"
                   aria-label="Ngày chiếu"
                   readonly>

            <input type="date"
                   id="date"
                   name="date"
                   value="<c:out value='${date}'/>"
                   class="real-date-input">

            <span class="date-icon">📅</span>

        </div>

        <select name="cinemaId"
                class="schedule-cinema"
                aria-label="Rạp">

            <option value="">Tất cả rạp</option>

            <c:forEach var="cinema" items="${cinemaById.values()}">
                <option value="${cinema.cinemaId}"
                        ${cinemaId == cinema.cinemaId ? 'selected' : ''}>
                    <c:out value="${cinema.cinemaName}" />
                </option>
            </c:forEach>

        </select>

        <button type="submit" class="search-button">Tìm kiếm</button>

        <c:if test="${not empty keyword or not empty date or cinemaId != null}">
            <a class="schedule-clear" href="${ctx}/user/movies">Xóa bộ lọc</a>
        </c:if>

    </form>


    <p class="schedule-result-count">
        Có <strong>${resultCount}</strong> kết quả phim và lịch chiếu.
    </p>


    <c:choose>

        <c:when test="${not empty scheduleByDate}">

            <c:forEach var="dayEntry" items="${scheduleByDate}">

                <section class="schedule-day-section">

                    <div class="schedule-date-heading">
                        <span class="schedule-date-label">Ngày chiếu</span>
                        <h2><c:out value="${dayEntry.key}" /></h2>
                    </div>

                    <div class="schedule-movie-grid">

                        <c:forEach var="movieEntry" items="${dayEntry.value}">

                            <c:set var="movie" value="${movieEntry.key}" />
                            <c:set var="showtimes" value="${movieEntry.value}" />

                            <article class="schedule-movie-card">

                                <div class="schedule-poster">
                                    <c:choose>
                                        <c:when test="${not empty movie.posterUrl}">
                                            <img src="<c:out value='${movie.posterUrl}'/>"
                                                 alt="<c:out value='${movie.title}'/>">
                                            
                                            <span class="poster-fallback" style="display:none;">
                                                <small>F / CINEMA</small>
                                                <strong><c:out value="${movie.title}" /></strong>
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="poster-fallback">
                                                <small>F / CINEMA</small>
                                                <strong><c:out value="${movie.title}" /></strong>
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="schedule-card-content">

                                    <h3 class="schedule-movie-title">
                                        <a class="card-link"
                                           href="${ctx}/user/movie-detail?id=${movie.movieId}">
                                            <c:out value="${movie.title}" />
                                        </a>
                                    </h3>

                                    <c:if test="${not empty movie.ageRating}">
                                        <span class="schedule-badge"><c:out value="${movie.ageRating}" /></span>
                                    </c:if>

                                    <div class="schedule-movie-meta">
                                        <span>Thời lượng: ${movie.durationMinutes} phút</span>
                                        <c:if test="${priceByMovie[movie.movieId] != null}">
                                            <strong>Từ <fmt:formatNumber value="${priceByMovie[movie.movieId]}" pattern="#,###" /> VNĐ</strong>
                                        </c:if>
                                    </div>

                                    <c:if test="${not empty movie.description}">
                                        <p class="schedule-description">
                                            <c:out value="${movie.description}" />
                                        </p>
                                    </c:if>

                                    <c:choose>

                                        <c:when test="${not empty showtimes}">

                                            <div class="showtime-area">

                                                <div class="showtime-heading">
                                                    <span class="book-label">Đặt vé</span>
                                                    <span class="showtime-label">Chọn giờ chiếu:</span>
                                                </div>

                                                <div class="showtime-buttons">
                                                    <c:forEach var="showtime" items="${showtimes}">
                                                        <c:set var="showtimeRoom" value="${roomById[showtime.roomId]}" />
                                                        <c:set var="showtimeCinema" value="${cinemaById[showtimeRoom.cinemaId]}" />
                                                        <a class="showtime-button"
                                                           href="${ctx}/user/select-seats?showtimeId=${showtime.showtimeId}"
                                                           title="<c:out value='${showtimeCinema.cinemaName}'/> - <c:out value='${showtimeRoom.roomName}'/>">
                                                            <strong><fmt:formatDate value="${showtime.startTime}" pattern="HH:mm" /></strong>
                                                            <small><c:out value="${showtimeRoom.roomName}" /></small>
                                                        </a>
                                                    </c:forEach>
                                                </div>

                                            </div>

                                        </c:when>

                                        <c:otherwise>
                                            <p class="no-showtime">Chưa có suất chiếu mở bán.</p>
                                        </c:otherwise>

                                    </c:choose>

                                </div>

                            </article>

                        </c:forEach>

                    </div>

                </section>

            </c:forEach>

        </c:when>

        <c:otherwise>

            <div class="empty-schedule">

                <strong>Không tìm thấy phim</strong>

                <p>Hiện chưa có phim hoặc suất chiếu phù hợp với điều kiện tìm kiếm.</p>

            </div>

        </c:otherwise>

    </c:choose>

</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>