<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<section class="movies-page">

    <div class="movies-page-header">
        <h1>PHIM</h1>
    </div>

    <!-- TABS -->
    <div class="movies-tabs">

        <c:choose>
            <c:when test="${type == 'now'}">
                <a href="${ctx}/user/movies?type=now" class="movies-tab active">PHIM ĐANG CHIẾU</a>
            </c:when>
            <c:otherwise>
                <a href="${ctx}/user/movies?type=now" class="movies-tab">PHIM ĐANG CHIẾU</a>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test="${type == 'coming'}">
                <a href="${ctx}/user/movies?type=coming" class="movies-tab active">PHIM SẮP CHIẾU</a>
            </c:when>
            <c:otherwise>
                <a href="${ctx}/user/movies?type=coming" class="movies-tab">PHIM SẮP CHIẾU</a>
            </c:otherwise>
        </c:choose>

    </div>

    <!-- SEARCH -->
    <div class="movies-toolbar">

        <form action="${ctx}/user/movies" method="get" class="movies-search-form">

            <input type="hidden" name="type" value="${type}">

            <input type="text" name="keyword" value="${keyword}" class="movies-search-input" placeholder="Tìm tên phim..." list="movieOptions" autocomplete="off">

            <datalist id="movieOptions">

                <c:forEach var="movie" items="${nowShowingMovies}">

                    <option value="${movie.title}"></option>

                </c:forEach>

            </datalist>

            <button type="submit" class="movies-search-button">Tìm kiếm</button>

            <c:if test="${not empty keyword}">
                <a href="${ctx}/user/movies?type=${type}" class="movies-clear-button">Xóa</a>
            </c:if>

        </form>

        <span class="movies-result">${totalMovies} phim</span>

    </div>

    <!-- MOVIE LIST -->
    <c:choose>

        <c:when test="${not empty movies}">

            <div class="movies-grid">

                <c:forEach var="movie" items="${movies}">

                    <article class="movies-card">

                        <!-- POSTER -->
                        <a href="${ctx}/user/movie-detail?id=${movie.movieId}" class="movies-poster">

                            <c:if test="${not empty movie.ageRating}">
                                <span class="movies-age">${movie.ageRating}</span>
                            </c:if>

                            <c:choose>

                                <c:when test="${not empty movie.posterUrl}">
                                    <img src="${movie.posterUrl}" alt="${movie.title}">
                                </c:when>

                                <c:otherwise>
                                    <div class="movies-poster-fallback">
                                        <span>F / CINEMA</span>
                                        <strong>${movie.title}</strong>
                                    </div>
                                </c:otherwise>

                            </c:choose>

                            <div class="movies-poster-overlay">
                                <span>Xem chi tiết</span>
                            </div>

                        </a>

                        <!-- CONTENT -->
                        <div class="movies-card-content">

                            <h2>
                                <a href="${ctx}/user/movie-detail?id=${movie.movieId}">${movie.title}</a>
                            </h2>

                            <!-- GENRES -->
                            <c:if test="${not empty genresByMovie[movie.movieId]}">

                                <div class="movie-genres">

                                    <c:forEach var="genre" items="${genresByMovie[movie.movieId]}">
                                        <span>${genre.genreName}</span>
                                    </c:forEach>

                                </div>

                            </c:if>

                            <!-- INFO -->
                            <div class="movies-info">

                                <p><strong>Thời lượng:</strong> ${movie.durationMinutes} phút</p>

                                <c:if test="${not empty movie.releaseDate}">
                                    <p><strong>Khởi chiếu:</strong> <fmt:formatDate value="${movie.releaseDate}" pattern="dd/MM/yyyy" /></p>
                                </c:if>

                            </div>

                            <!-- BUTTONS -->
                            <c:choose>

                                <c:when test="${type == 'now'}">

                                    <div class="movies-card-buttons">

                                        <a href="${ctx}/user/movie-detail?id=${movie.movieId}"
                                           class="movies-detail-button">
                                            Chi tiết
                                        </a>

                                        <a href="${ctx}/user/showtimes?movieId=${movie.movieId}"
                                           class="movies-book-button">
                                            Đặt vé
                                        </a>

                                    </div>

                                </c:when>

                                <c:otherwise>

                                    <div class="movies-card-buttons coming-buttons">

                                        <a href="${ctx}/user/movie-detail?id=${movie.movieId}"
                                           class="movies-detail-button">
                                            Chi tiết
                                        </a>

                                    </div>

                                </c:otherwise>

                            </c:choose>

                        </div>

                    </article>

                </c:forEach>

            </div>

        </c:when>

        <c:otherwise>

            <div class="movies-empty">

                <h2>Không tìm thấy phim</h2>

                <c:choose>
                    <c:when test="${type == 'coming'}">
                        <p>Hiện chưa có phim sắp chiếu.</p>
                    </c:when>
                    <c:otherwise>
                        <p>Hiện chưa có phim đang chiếu.</p>
                    </c:otherwise>
                </c:choose>

            </div>

        </c:otherwise>

    </c:choose>

    <!-- PAGINATION -->
    <c:if test="${totalPages > 1}">

        <div class="movies-pagination">

            <c:if test="${currentPage > 1}">
                <a href="${ctx}/user/movies?type=${type}&keyword=${keyword}&page=${currentPage - 1}" class="pagination-arrow">‹</a>
            </c:if>

            <c:forEach begin="1" end="${totalPages}" var="pageNumber">

                <c:choose>

                    <c:when test="${pageNumber == currentPage}">
                        <span class="pagination-number active">${pageNumber}</span>
                    </c:when>

                    <c:otherwise>
                        <a href="${ctx}/user/movies?type=${type}&keyword=${keyword}&page=${pageNumber}" class="pagination-number">${pageNumber}</a>
                    </c:otherwise>

                </c:choose>

            </c:forEach>

            <c:if test="${currentPage < totalPages}">
                <a href="${ctx}/user/movies?type=${type}&keyword=${keyword}&page=${currentPage + 1}" class="pagination-arrow">›</a>
            </c:if>

        </div>

    </c:if>

</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>