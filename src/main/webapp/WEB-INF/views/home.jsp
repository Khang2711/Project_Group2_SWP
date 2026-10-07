<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>


<!-- =========================
     CAROUSEL
     ========================= -->
<section class="home-hero">

    <c:if test="${not empty featuredMovies}">

        <div class="carousel-container">

            <c:forEach var="movie" items="${featuredMovies}">

                <div class="carousel-slide">

                    <!-- Thông tin phim -->
                    <div class="hero-left">

                        <span class="hero-small-title">
                            ĐANG ĐƯỢC QUAN TÂM
                        </span>

                        <h1>
                            ${movie.title}
                        </h1>

                        <div class="hero-meta">

                            <c:if test="${not empty movie.ageRating}">
                                <span class="hero-age">
                                    ${movie.ageRating}
                                </span>
                            </c:if>

                            <span>
                                ${movie.durationMinutes} phút
                            </span>

                            <c:if test="${not empty movie.releaseDate}">
                                <span>
                                    <fmt:formatDate
                                        value="${movie.releaseDate}"
                                        pattern="dd/MM/yyyy" />
                                </span>
                            </c:if>

                        </div>

                        <p class="hero-description">
                            ${movie.description}
                        </p>

                        <div class="hero-buttons">

                            <a href="${pageContext.request.contextPath}/user/movie-detail?id=${movie.movieId}"
                               class="hero-detail-button">
                                Xem chi tiết
                            </a>

                            <a href="${pageContext.request.contextPath}/user/movies?keyword=${movie.title}"
                               class="hero-book-button">
                                Đặt vé ngay
                            </a>

                        </div>

                    </div>


                    <!-- Poster -->
                    <div class="hero-right">

                        <c:if test="${not empty movie.posterUrl}">

                            <img src="${movie.posterUrl}"
                                 alt="${movie.title}"
                                 class="hero-poster-image">

                        </c:if>


                        <c:if test="${empty movie.posterUrl}">

                            <div class="hero-poster-fallback">

                                <span>
                                    F / CINEMA
                                </span>

                                <strong>
                                    ${movie.title}
                                </strong>

                                <small>
                                    NOW SHOWING
                                </small>

                            </div>

                        </c:if>

                    </div>

                </div>

            </c:forEach>


            <!-- Nút trái -->
            <button type="button"
                    class="carousel-button carousel-prev"
                    id="carouselPrev">
                ❮
            </button>


            <!-- Nút phải -->
            <button type="button"
                    class="carousel-button carousel-next"
                    id="carouselNext">
                ❯
            </button>


            <!-- Chấm carousel -->
            <div class="carousel-dots"
                 id="carouselDots">
            </div>

        </div>

    </c:if>


    <c:if test="${empty featuredMovies}">

        <div class="home-empty hero-empty">

            <h2>F-CINEMA</h2>

            <p>
                Hiện chưa có phim đang chiếu.
            </p>

        </div>

    </c:if>

</section>



<!-- =========================
     ĐẶT VÉ NHANH
     ========================= -->
<section class="quick-booking">

    <div class="quick-booking-header">

        <div>
            <h2>Đặt vé nhanh</h2>
            <p>Tìm suất chiếu phù hợp với bạn.</p>
        </div>

        <span>
            Chọn phim · ngày · rạp
        </span>

    </div>


    <form action="${pageContext.request.contextPath}/user/movies"
          method="get"
          class="quick-booking-form">


        <!-- Chọn phim -->
        <div class="quick-field movie-field">

            <label for="quickMovie">
                Tên phim
            </label>

            <select name="keyword"
                    id="quickMovie">

                <option value="">
                    Bạn muốn xem phim gì?
                </option>

                <c:forEach var="movie"
                           items="${nowShowingMovies}">

                    <option value="${movie.title}">
                        ${movie.title}
                    </option>

                </c:forEach>

            </select>

        </div>


        <!-- Chọn ngày -->
        <div class="quick-field">

            <label for="quickDate">
                Ngày chiếu
            </label>

            <input type="date"
                   name="date"
                   id="quickDate">

        </div>


        <!-- Chọn rạp -->
        <div class="quick-field cinema-field">

            <label for="quickCinema">
                Rạp
            </label>

            <select name="cinemaId"
                    id="quickCinema">

                <option value="">
                    Tất cả rạp
                </option>

                <c:forEach var="cinema"
                           items="${cinemas}">

                    <option value="${cinema.cinemaId}">
                        ${cinema.cinemaName}
                    </option>

                </c:forEach>

            </select>

        </div>


        <button type="submit"
                class="quick-search-button">
            Tìm suất chiếu
        </button>

    </form>

</section>



<!-- =========================
     PHIM ĐANG CHIẾU
     ========================= -->
<section class="home-section"
         id="now-showing">

    <div class="home-section-header">

        <div>

            <span class="home-section-small">
                ĐANG CHIẾU
            </span>

            <h2>
                Phim đang chiếu
            </h2>

        </div>


        <a href="${pageContext.request.contextPath}/user/movies"
           class="view-all">
            Xem tất cả →
        </a>

    </div>


    <c:if test="${not empty nowShowingMovies}">

        <div class="home-movie-grid">

            <c:forEach var="movie"
                       items="${nowShowingMovies}"
                       end="3">

                <article class="home-movie-card">


                    <!-- Poster -->
                    <a href="${pageContext.request.contextPath}/user/movie-detail?id=${movie.movieId}"
                       class="home-movie-poster">


                        <!-- Age rating -->
                        <c:if test="${not empty movie.ageRating}">

                            <span class="home-age">
                                ${movie.ageRating}
                            </span>

                        </c:if>


                        <!-- Có poster -->
                        <c:if test="${not empty movie.posterUrl}">

                            <img src="${movie.posterUrl}"
                                 alt="${movie.title}">

                        </c:if>


                        <!-- Không có poster -->
                        <c:if test="${empty movie.posterUrl}">

                            <div class="movie-poster-fallback">

                                <span>
                                    F / CINEMA
                                </span>

                                <strong>
                                    ${movie.title}
                                </strong>

                            </div>

                        </c:if>

                    </a>


                    <!-- Nội dung card -->
                    <div class="home-movie-content">

                        <h3>
                            ${movie.title}
                        </h3>


                        <div class="home-movie-meta">

                            <span>
                                ${movie.durationMinutes} phút
                            </span>


                            <!-- Giá từ database -->
                            <c:if test="${not empty priceByMovie[movie.movieId]}">

                                <strong>

                                    <fmt:formatNumber
                                        value="${priceByMovie[movie.movieId]}"
                                        pattern="#,###" />

                                    VNĐ

                                </strong>

                            </c:if>


                            <!-- Không tìm thấy giá -->
                            <c:if test="${empty priceByMovie[movie.movieId]}">

                                <strong>
                                    Chưa có giá
                                </strong>

                            </c:if>

                        </div>


                        <div class="home-card-buttons">

                            <a href="${pageContext.request.contextPath}/user/movie-detail?id=${movie.movieId}"
                               class="home-detail-button">
                                Chi tiết
                            </a>


                            <a href="${pageContext.request.contextPath}/user/movies?keyword=${movie.title}"
                               class="home-book-button">
                                Đặt vé
                            </a>

                        </div>

                    </div>

                </article>

            </c:forEach>

        </div>

    </c:if>


    <c:if test="${empty nowShowingMovies}">

        <div class="home-empty">

            <h3>
                Chưa có phim đang chiếu
            </h3>

        </div>

    </c:if>

</section>



<!-- =========================
     PHIM SẮP CHIẾU
     ========================= -->
<section class="home-section"
         id="coming-soon">

    <div class="home-section-header">

        <div>

            <span class="home-section-small">
                COMING SOON
            </span>

            <h2>
                Phim sắp chiếu
            </h2>

        </div>

    </div>


    <c:if test="${not empty comingSoonMovies}">

        <div class="coming-movie-grid">

            <c:forEach var="movie"
                       items="${comingSoonMovies}"
                       end="3">

                <article class="coming-movie-card">


                    <!-- Poster -->
                    <a href="${pageContext.request.contextPath}/user/movie-detail?id=${movie.movieId}"
                       class="coming-poster">


                        <c:if test="${not empty movie.posterUrl}">

                            <img src="${movie.posterUrl}"
                                 alt="${movie.title}">

                        </c:if>


                        <c:if test="${empty movie.posterUrl}">

                            <div class="coming-poster-fallback">

                                <strong>
                                    ${movie.title}
                                </strong>

                            </div>

                        </c:if>

                    </a>


                    <!-- Nội dung -->
                    <div class="coming-content">


                        <c:if test="${not empty movie.releaseDate}">

                            <span class="coming-date">

                                KHỞI CHIẾU

                                <fmt:formatDate
                                    value="${movie.releaseDate}"
                                    pattern="dd.MM.yyyy" />

                            </span>

                        </c:if>


                        <h3>
                            ${movie.title}
                        </h3>


                        <p>
                            ${movie.description}
                        </p>


                        <a href="${pageContext.request.contextPath}/user/movie-detail?id=${movie.movieId}"
                           class="coming-detail">
                            Xem chi tiết
                        </a>

                    </div>

                </article>

            </c:forEach>

        </div>

    </c:if>


    <c:if test="${empty comingSoonMovies}">

        <div class="home-empty">

            <h3>
                Chưa có phim sắp chiếu
            </h3>

            <p>
                Hiện chưa có thông tin phim sắp chiếu.
            </p>

        </div>

    </c:if>

</section>


<%@ include file="/WEB-INF/include/footer.jsp" %>