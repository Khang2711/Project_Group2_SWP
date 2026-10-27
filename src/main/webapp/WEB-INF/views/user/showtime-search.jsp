
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<section class="showtimes-page showtimes-search-page">

    <div class="showtimes-header">
        <h1>TÌM SUẤT CHIẾU</h1>
        <div class="showtimes-steps">
            <span class="active">1. Suất chiếu</span>
            <span>→</span>
            <span>2. Chọn ghế</span>
            <span>→</span>
            <span>3. Thanh toán</span>
        </div>
    </div>

    <%-- Search filters --%>
    <section class="showtimes-search-filter">
        <div class="showtimes-search-filter-heading">
            <div>
                <h2>Chọn phim, ngày và rạp</h2>
                <p>Tìm giờ chiếu phù hợp và tiếp tục đặt vé chỉ với một lần chọn.</p>
            </div>

            <a href="${ctx}/home" class="showtimes-search-back">
                ← Trang chủ
            </a>
        </div>

        <form action="${ctx}/user/showtimes"
              method="get"
              class="quick-booking-form showtimes-search-form">


            <div class="quick-field">

                <label for="quickMovie">Tên phim</label>

                <input type="text"
                       name="keyword"
                       id="quickMovie"
                       list="movieOptions"
                       value="<c:out value='${keyword}' />"
                       placeholder="Tìm tên phim..."
                       autocomplete="off">

                <datalist id="movieOptions">

                    <c:forEach var="movie" items="${nowShowingMovies}">

                        <option value="<c:out value='${movie.title}' />"></option>

                    </c:forEach>

                </datalist>

            </div>


            <div class="quick-field">
                <label for="dateDisplay">Ngày chiếu</label>

                <div class="date-picker-wrapper">
                    <input type="text"
                           id="dateDisplay"
                           class="date-display"
                           placeholder="dd/mm/yyyy"
                           readonly>

                    <input type="date"
                           name="date"
                           id="date"
                           class="real-date-input"
                           value="${date}">

                    <span class="date-icon">📅</span>
                </div>
            </div>

            <div class="quick-field">
                <label for="quickCinema">Rạp chiếu</label>

                <select name="cinemaId" id="quickCinema">
                    <option value="0">Tất cả rạp</option>

                    <c:forEach var="cinema" items="${cinemas}">
                        <option value="${cinema.cinemaId}"
                                ${cinemaId eq cinema.cinemaId ? 'selected' : ''}>
                            <c:out value="${cinema.cinemaName}" />
                        </option>
                    </c:forEach>
                </select>
            </div>

            <button type="submit" class="quick-search-button">
                TÌM SUẤT CHIẾU
            </button>
        </form>
    </section>

    <%-- Search results --%>
    <section class="showtimes-search-results">

        <div class="showtimes-search-results-heading">
            <h2>KẾT QUẢ TÌM KIẾM</h2>
            <p>Chọn khung giờ bên dưới để tiếp tục chọn ghế</p>
        </div>

        <c:choose>

            <c:when test="${empty scheduleByDate}">
                <div class="showtimes-empty">
                    Không có suất chiếu phù hợp.
                    <br>
                    Vui lòng thử ngày chiếu, rạp hoặc tên phim khác.
                </div>
            </c:when>

            <c:otherwise>

                <c:forEach var="dayEntry"
                           items="${scheduleByDate}"
                           varStatus="loop">

                    <details class="showtimes-search-day"
                             name="showtime-day"
                             ${loop.first ? 'open' : ''}>

                        <summary class="showtimes-search-day-heading">
                            <span>NGÀY CHIẾU</span>

                            <h3>
                                <c:out value="${dayEntry.key}" />
                            </h3>

                            <span class="showtimes-day-arrow">▼</span>
                        </summary>

                        <c:forEach var="movieEntry" items="${dayEntry.value}">

                            <c:set var="movie" value="${movieEntry.key}" />

                            <article class="showtimes-movie showtimes-search-movie">

                                <%-- Movie poster --%>
                                <a class="showtimes-poster"
                                   href="${ctx}/user/movie-detail?id=${movie.movieId}"
                                   aria-label="Chi tiết phim">

                                    <c:choose>
                                        <c:when test="${not empty movie.posterUrl}">
                                            <c:choose>

                                                <c:when test="${fn:startsWith(movie.posterUrl, 'http://') or fn:startsWith(movie.posterUrl, 'https://')}">

                                                    <c:set var="posterSrc" value="${movie.posterUrl}" />

                                                </c:when>

                                                <c:otherwise>

                                                    <c:url var="posterSrc" value="/${movie.posterUrl}" />

                                                </c:otherwise>

                                            </c:choose>
                                            <img src="<c:out value='${posterSrc}' />"
                                                 alt="<c:out value='${movie.title}' />">
                                        </c:when>

                                        <c:otherwise>
                                            <div class="showtimes-no-poster">
                                                F-CINEMA
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </a>

                                <%-- Movie information --%>
                                <div class="showtimes-movie-info">

                                    <div class="showtimes-search-movie-heading">

                                        <div>
                                            <h2>
                                                <c:out value="${movie.title}" />
                                            </h2>

                                            <div class="showtimes-search-movie-meta">

                                                <span>
                                                    ${movie.durationMinutes} phút
                                                </span>

                                                <c:if test="${not empty movie.ageRating}">
                                                    <span class="showtimes-search-age">
                                                        <c:out value="${movie.ageRating}" />
                                                    </span>
                                                </c:if>

                                            </div>
                                        </div>

                                        <a class="showtimes-detail-button"
                                           href="${ctx}/user/movie-detail?id=${movie.movieId}">
                                            Chi tiết phim →
                                        </a>
                                    </div>

                                    <div class="showtimes-search-times-title">
                                        Giờ chiếu &amp; rạp
                                    </div>

                                    <%-- Showtime slots --%>
                                    <div class="showtimes-slots showtimes-search-slots">

                                        <c:forEach var="slot"
                                                   items="${movieEntry.value}">

                                            <c:set var="room"
                                                   value="${roomById[slot.roomId]}" />

                                            <c:if test="${not empty room}">

                                                <a class="showtimes-slot showtimes-search-slot"
                                                   href="${ctx}/user/select-seats?showtimeId=${slot.showtimeId}"
                                                   title="Tiếp tục chọn ghế">

                                                    <strong class="showtimes-time">
                                                        <fmt:formatDate
                                                            value="${slot.startTime}"
                                                            pattern="HH:mm" />
                                                    </strong>

                                                    <span class="showtimes-room">
                                                        <c:out value="${cinemaById[room.cinemaId].cinemaName}" />
                                                    </span>

                                                    <small>
                                                        <c:out value="${room.roomName}" />
                                                    </small>

                                                </a>

                                            </c:if>

                                        </c:forEach>

                                    </div>

                                </div>
                            </article>

                        </c:forEach>
                            
                    </details>

                </c:forEach>

            </c:otherwise>

        </c:choose>

    </section>

</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>
