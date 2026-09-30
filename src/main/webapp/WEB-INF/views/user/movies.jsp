<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<section class="movie-page">

    <!-- Page title -->
    <div class="movie-page-title">
        <h1>Hôm nay, xem gì?</h1>
        <p>Khám phá những bộ phim đang được chiếu tại F-CINEMA</p>
    </div>

    <!-- Search and filter -->
    <form class="movie-filter"
          action="${pageContext.request.contextPath}/user/movies"
          method="get">

        <div class="filter-group">
            <label for="keyword">Tìm phim</label>

            <input type="text"
                   id="keyword"
                   name="keyword"
                   value="${keyword}"
                   placeholder="Nhập tên phim...">
        </div>

        <div class="filter-group">
            <label for="date">Ngày chiếu</label>

            <input type="date"
                   id="date"
                   name="date"
                   value="${date}">
        </div>

        <div class="filter-group">
            <label for="cinema">Rạp</label>

            <select id="cinema"
                    name="cinemaId">

                <option value="">Tất cả rạp</option>

                <c:forEach var="cinema" items="${cinemas}">

                    <option value="${cinema.cinemaId}"
                            ${cinemaId == cinema.cinemaId ? 'selected' : ''}>

                        ${cinema.cinemaName}

                    </option>

                </c:forEach>

            </select>
        </div>

        <button type="submit" class="search-button">
            Tìm kiếm
        </button>

    </form>

    <!-- Result -->
    <div class="movie-result-header">

        <h2>Phim đang chiếu</h2>

        <span>
            ${empty movies ? 0 : movies.size()} phim
        </span>

    </div>

    <!-- Movie list -->
    <div class="movie-grid">

        <c:choose>

            <!-- Has movies -->
            <c:when test="${not empty movies}">

                <c:forEach var="movie" items="${movies}">

                    <article class="movie-card">

                        <div class="movie-poster">

                            <c:choose>

                                <c:when test="${not empty movie.posterUrl}">
                                    <img src="${movie.posterUrl}" alt="${movie.title}">
                                </c:when>

                                <c:otherwise>
                                    <div class="poster-placeholder">
                                        F-CINEMA
                                    </div>
                                </c:otherwise>

                            </c:choose>

                        </div>

                        <div class="movie-info">

                            <h3>${movie.title}</h3>

                            <div class="movie-meta">

                                <span>
                                    ${movie.ageRating}
                                </span>

                                <span>
                                    ${movie.durationMinutes} phút
                                </span>

                            </div>

                            <p>${movie.description}</p>

                            <a class="detail-button"href="${pageContext.request.contextPath}/user/movie-detail?id=${movie.movieId}">
                                Xem chi tiết
                            </a>

                        </div>

                    </article>

                </c:forEach>

            </c:when>

            <!-- No movies -->
            <c:otherwise>

                <div class="empty-movies">
                    <h3>Không tìm thấy phim</h3>
                    <p>Hiện tại chưa có phim phù hợp với điều kiện tìm kiếm.</p>
                </div>

            </c:otherwise>

        </c:choose>

    </div>

</section>

<%@ include file="/WEB-INF/include/footer.jsp" %>