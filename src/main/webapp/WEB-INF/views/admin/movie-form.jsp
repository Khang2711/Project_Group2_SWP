<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<c:choose>

    <c:when test="${movie.movieId > 0}">
        <h1>Cập nhật phim</h1>
    </c:when>

    <c:otherwise>
        <h1>Thêm phim</h1>
    </c:otherwise>

</c:choose>

<c:if test="${not empty error}">
    <p>
        <c:out value="${error}" />
    </p>
</c:if>

<form method="post"
      action="${pageContext.request.contextPath}/admin/movie-form">

    <c:if test="${movie.movieId > 0}">
        <input type="hidden"
               name="id"
               value="${movie.movieId}">
    </c:if>

    <p>
        <label>Tên phim:</label>
        <br>

        <input type="text"
               name="title"
               maxlength="255"
               required
               value="<c:out value='${movie.title}' />">
    </p>

    <p>
        <label>Mô tả:</label>
        <br>

        <textarea name="description"
                  rows="5"
                  cols="50"><c:out value="${movie.description}" /></textarea>
    </p>

    <p>
        <label>Thời lượng (phút):</label>
        <br>

        <input type="number"
               name="durationMinutes"
               min="1"
               required
               value="${movie.durationMinutes > 0
                        ? movie.durationMinutes : ''}">
    </p>

    <p>
        <label>Phân loại độ tuổi:</label>
        <br>

        <input type="text"
               name="ageRating"
               maxlength="20"
               value="<c:out value='${movie.ageRating}' />">
    </p>

    <p>
        <label>Ngày phát hành:</label>
        <br>

        <input type="date"
               name="releaseDate"
               value="${movie.releaseDate}">
    </p>

    <p>
        <label>Ngày kết thúc:</label>
        <br>

        <input type="date"
               name="endDate"
               value="${movie.endDate}">
    </p>

    <p>
        <label>Poster URL:</label>
        <br>

        <input type="text"
               name="posterUrl"
               maxlength="500"
               value="<c:out value='${movie.posterUrl}' />">
    </p>

    <p>
        <label>Trailer URL:</label>
        <br>

        <input type="text"
               name="trailerUrl"
               maxlength="500"
               value="<c:out value='${movie.trailerUrl}' />">
    </p>

    <p>
        <strong>Thể loại:</strong>
    </p>

    <c:forEach var="genre" items="${genres}">

        <label>

            <input type="checkbox"
                   name="genreId"
                   value="${genre.genreId}"
                   <c:if test="${selectedGenreIds.contains(genre.genreId)}">
                       checked
                   </c:if>>

            <c:out value="${genre.genreName}" />

        </label>

        <br>

    </c:forEach>

    <p>
        <label>Trạng thái:</label>
        <br>

        <select name="status">

            <option value="ACTIVE"
                    ${empty movie.status
                    || movie.status == 'ACTIVE'
                    ? 'selected' : ''}>
                ACTIVE
            </option>

            <option value="INACTIVE"
                    ${movie.status == 'INACTIVE'
                    ? 'selected' : ''}>
                INACTIVE
            </option>

        </select>
    </p>

    <button type="submit">
        Lưu
    </button>

    <a href="${pageContext.request.contextPath}/admin/movies">
        Quay lại
    </a>

</form>

<%@ include file="/WEB-INF/include/footer.jsp" %>