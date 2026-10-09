
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="movie-detail-page">

    <!-- BREADCRUMB -->
    <div class="detail-breadcrumb">
        <a href="${ctx}/user/movies">Phim</a>
        <span>/</span>
        <span><c:out value="${movie.title}" /></span>
    </div>

    <h1 class="detail-page-title">CHI TIẾT PHIM</h1>

    <div class="detail-container">

        <!-- MOVIE POSTER -->
        <div class="detail-poster">

            <c:choose>
                <c:when test="${not empty movie.posterUrl}">
                    <img src="${movie.posterUrl}"
                         alt="<c:out value='${movie.title}' />">
                </c:when>

                <c:otherwise>
                    <div class="detail-no-poster">
                        F-CINEMA
                    </div>
                </c:otherwise>
            </c:choose>

            <c:if test="${not empty movie.ageRating}">
                <span class="detail-age">
                    <c:out value="${movie.ageRating}" />
                </span>
            </c:if>

        </div>

        <!-- MOVIE INFORMATION -->
        <div class="detail-info">

            <!-- MOVIE STATUS -->
            <c:choose>

                <c:when test="${comingSoon}">
                    <span class="detail-status coming">
                        SẮP CHIẾU
                    </span>
                </c:when>

                <c:when test="${ended}">
                    <span class="detail-status ended">
                        NGỪNG CHIẾU
                    </span>
                </c:when>

                <c:when test="${not empty movie.releaseDate}">
                    <span class="detail-status showing">
                        ĐANG CHIẾU
                    </span>
                </c:when>

                <c:otherwise>
                    <span class="detail-status ended">
                        ĐANG CẬP NHẬT
                    </span>
                </c:otherwise>

            </c:choose>

            <!-- MOVIE TITLE -->
            <h2 class="detail-movie-title">
                <c:out value="${movie.title}" />
            </h2>

            <div class="detail-line"></div>

            <!-- GENRES -->
            <div class="detail-info-row">
                <span class="detail-label">Thể loại:</span>

                <span class="detail-value">

                    <c:choose>
                        <c:when test="${not empty movieGenres}">

                            <c:forEach var="genre"
                                       items="${movieGenres}"
                                       varStatus="loop">

                                <c:if test="${not loop.first}">, </c:if>

                                <c:out value="${genre.genreName}" />

                            </c:forEach>

                        </c:when>

                        <c:otherwise>
                            Đang cập nhật
                        </c:otherwise>
                    </c:choose>

                </span>
            </div>

            <!-- DURATION -->
            <div class="detail-info-row">
                <span class="detail-label">Thời lượng:</span>

                <span class="detail-value">
                    <c:choose>
                        <c:when test="${not empty movie.durationMinutes}">
                            <c:out value="${movie.durationMinutes}" /> phút
                        </c:when>
                        <c:otherwise>
                            Đang cập nhật
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>

            <!-- RELEASE DATE -->
            <div class="detail-info-row">
                <span class="detail-label">Khởi chiếu:</span>

                <span class="detail-value">
                    <c:choose>
                        <c:when test="${not empty movie.releaseDate}">
                            <fmt:formatDate
                                value="${movie.releaseDate}"
                                pattern="dd/MM/yyyy" />
                        </c:when>

                        <c:otherwise>
                            Đang cập nhật
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>

            <!-- AGE RATING -->
            <div class="detail-info-row">
                <span class="detail-label">Phân loại:</span>

                <span class="detail-value">
                    <c:out value="${empty movie.ageRating ? 'Đang cập nhật' : movie.ageRating}" />
                </span>
            </div>

            <!-- ACTION BUTTONS -->
            <div class="detail-buttons">

                <!-- BOOK TICKET -->
                <c:if test="${not empty movie.releaseDate and not comingSoon and not ended}">
                    <a href="${ctx}/user/showtimes?movieId=${movie.movieId}"
                       class="detail-book-btn">
                        ĐẶT VÉ
                    </a>
                </c:if>

                <!-- WATCH TRAILER -->
                <c:if test="${not empty movie.trailerUrl}">
                    <a href="${movie.trailerUrl}"
                       target="_blank"
                       rel="noopener noreferrer"
                       class="detail-trailer-btn">
                        ▶ XEM TRAILER
                    </a>
                </c:if>

            </div>

            <!-- COMING SOON MESSAGE -->
            <c:if test="${comingSoon}">
                <p class="detail-coming-message">
                    Phim sắp khởi chiếu, chưa mở bán vé.
                </p>
            </c:if>

        </div>

    </div>

    <!-- MOVIE DESCRIPTION -->
    <div class="detail-description">

        <h2>NỘI DUNG PHIM</h2>

        <div class="detail-description-line"></div>

        <c:choose>

            <c:when test="${not empty movie.description}">
                <p><c:out value="${movie.description}" /></p>
            </c:when>

            <c:otherwise>
                <p>Nội dung phim đang được cập nhật.</p>
            </c:otherwise>

        </c:choose>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>
