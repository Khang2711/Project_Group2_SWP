<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>


<fmt:formatDate
    value="${showtime.startTime}"
    pattern="yyyy-MM-dd"
    var="startDateValue" />

<fmt:formatDate
    value="${showtime.startTime}"
    pattern="HH:mm"
    var="startClockValue" />

<fmt:formatDate
    value="${showtime.endTime}"
    pattern="dd/MM/yyyy HH:mm"
    var="endTimeValue" />


<style>

    .showtime-form-page {
        max-width: 760px;
        margin: 0 auto;
    }

    .showtime-form-page h1 {
        margin-bottom: 8px;
    }

    .form-subtitle {
        margin-bottom: 24px;
        color: #aaa;
        line-height: 1.6;
    }

    .showtime-form-box {
        padding: 25px;
        background: #18181d;
        border: 1px solid #303039;
        border-radius: 10px;
    }

    .form-group {
        margin-bottom: 20px;
    }

    .form-group label {
        display: block;
        margin-bottom: 8px;
        color: #ddd;
        font-weight: 600;
    }

    .form-group select,
    .form-group input {
        width: 100%;
        box-sizing: border-box;

        padding: 11px 12px;

        background: #101014;
        color: white;

        border: 1px solid #4a4a54;
        border-radius: 6px;

        font-size: 14px;

        color-scheme: dark;
    }

    .date-time-row {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 18px;
    }

    .form-help {
        margin-top: 7px;
        color: #888;
        font-size: 13px;
        line-height: 1.5;
    }

    .end-preview {
        padding: 14px 15px;

        background: #202027;

        border: 1px solid #383840;
        border-radius: 6px;

        color: #ccc;

        font-size: 15px;
    }

    .end-preview strong {
        color: #ff5873;
    }

    .form-actions {
        display: flex;
        gap: 10px;
        margin-top: 25px;
    }

    .save-button {
        padding: 11px 20px;

        border: none;
        border-radius: 6px;

        background: #ff1744;
        color: white;

        cursor: pointer;

        font-weight: bold;
    }

    .save-button:hover {
        background: #e8143d;
    }

    .cancel-button {
        padding: 11px 20px;

        border-radius: 6px;

        background: #292932;
        border: 1px solid #444;

        color: white;
        text-decoration: none;
    }

    .form-error {
        margin-bottom: 20px;

        padding: 13px 15px;

        background: #36191f;
        color: #ffb9c1;

        border: 1px solid #71343e;
        border-radius: 6px;
    }

    @media (max-width: 600px) {

        .date-time-row {
            grid-template-columns: 1fr;
        }

    }

</style>


<section class="showtime-form-page">

    <h1>

        <c:if test="${empty showtime or showtime.showtimeId == 0}">
            Thêm lịch chiếu
        </c:if>

        <c:if test="${not empty showtime and showtime.showtimeId > 0}">
            Sửa lịch chiếu
        </c:if>

    </h1>


    <p class="form-subtitle">

        Chọn phim, phòng, ngày chiếu và giờ bắt đầu.
        Hệ thống tự tính giờ kết thúc dựa trên
        thời lượng của phim.

    </p>


    <c:if test="${not empty error}">

        <div class="form-error">

            <c:out value="${error}" />

        </div>

    </c:if>


    <form method="post"
          action="${pageContext.request.contextPath}/admin/showtime-form"
          class="showtime-form-box">


        <c:if test="${not empty showtime and showtime.showtimeId > 0}">

            <input type="hidden"
                   name="id"
                   value="${showtime.showtimeId}">

        </c:if>


        <div class="form-group">

            <label for="movieId">
                Phim
            </label>

            <select id="movieId"
                    name="movieId"
                    required>

                <option value="">
                    -- Chọn phim --
                </option>


                <c:forEach items="${movies}"
                           var="movie">

                    <option
                        value="${movie.movieId}"

                        data-duration="${movie.durationMinutes}"

                        <c:if test="${not empty showtime and showtime.movieId == movie.movieId}">
                            selected
                        </c:if>>

                        <c:out value="${movie.title}" />

                        - ${movie.durationMinutes} phút

                        <c:if test="${movie.status ne 'ACTIVE'}">
                            [INACTIVE]
                        </c:if>

                    </option>

                </c:forEach>

            </select>


            <p class="form-help">

                Thời lượng phim được lấy trực tiếp
                từ thông tin phim trong database.

            </p>

        </div>


        <div class="form-group">

            <label for="roomId">
                Phòng chiếu
            </label>

            <select id="roomId"
                    name="roomId"
                    required>

                <option value="">
                    -- Chọn phòng --
                </option>


                <c:forEach items="${rooms}"
                           var="room">

                    <option
                        value="${room.roomId}"

                        <c:if test="${not empty showtime and showtime.roomId == room.roomId}">
                            selected
                        </c:if>>

                        <c:out value="${room.roomName}" />

                        - ${room.roomType}

                        <c:if test="${room.status ne 'ACTIVE'}">
                            [INACTIVE]
                        </c:if>

                    </option>

                </c:forEach>

            </select>

        </div>


        <div class="date-time-row">


            <div class="form-group">

                <label for="startDate">
                    Ngày chiếu
                </label>

                <input type="date"
                       id="startDate"
                       name="startDate"
                       lang="vi-VN"
                       value="${startDateValue}"
                       required>


                <p class="form-help">

                    Chọn theo ngày / tháng / năm.

                </p>

            </div>


            <div class="form-group">

                <label for="startClock">
                    Giờ bắt đầu
                </label>

                <input type="time"
                       id="startClock"
                       name="startClock"
                       value="${startClockValue}"
                       step="60"
                       required>


                <p class="form-help">

                    Giờ 24h.
                    Ví dụ: 08:30, 14:00, 19:45.

                </p>

            </div>


        </div>


        <div class="form-group">

            <label>
                Thời gian kết thúc
            </label>

            <div class="end-preview">

                <strong id="endTimePreview">

                    <c:if test="${not empty endTimeValue}">
                        ${endTimeValue}
                    </c:if>

                    <c:if test="${empty endTimeValue}">
                        Chọn phim, ngày và giờ bắt đầu
                    </c:if>

                </strong>

            </div>


            <p class="form-help">

                Hệ thống tự tính:

                <strong>
                    giờ bắt đầu + thời lượng phim
                </strong>.

                Bạn không cần nhập giờ kết thúc.

            </p>

        </div>


        <div class="form-group">

            <label for="status">
                Trạng thái
            </label>

            <select id="status"
                    name="status"
                    required>


                <option
                    value="ACTIVE"

                    <c:if test="${empty showtime
                                  or empty showtime.status
                                  or showtime.status eq 'ACTIVE'}">
                        selected
                    </c:if>>

                    ACTIVE

                </option>


                <option
                    value="INACTIVE"

                    <c:if test="${not empty showtime
                                  and showtime.status eq 'INACTIVE'}">
                        selected
                    </c:if>>

                    INACTIVE

                </option>


            </select>

        </div>


        <div class="form-actions">

            <button type="submit"
                    class="save-button">

                Lưu lịch chiếu

            </button>


            <a href="${pageContext.request.contextPath}/admin/showtimes"
               class="cancel-button">

                Hủy

            </a>

        </div>


    </form>

</section>


<script>

    const movieSelect =
        document.getElementById(
            "movieId"
        );

    const startDateInput =
        document.getElementById(
            "startDate"
        );

    const startClockInput =
        document.getElementById(
            "startClock"
        );

    const endTimePreview =
        document.getElementById(
            "endTimePreview"
        );


    function updateEndTime() {

        const selectedOption =
            movieSelect.options[
                movieSelect.selectedIndex
            ];


        const duration =
            parseInt(
                selectedOption
                    ? selectedOption.dataset.duration
                    : ""
            );


        const dateValue =
            startDateInput.value;


        const timeValue =
            startClockInput.value;


        if (!duration
                || !dateValue
                || !timeValue) {

            endTimePreview.textContent =
                "Chọn phim, ngày và giờ bắt đầu";

            return;
        }


        const startDate =
            new Date(
                dateValue
                + "T"
                + timeValue
            );


        if (isNaN(
                startDate.getTime()
        )) {

            endTimePreview.textContent =
                "Ngày hoặc giờ không hợp lệ";

            return;
        }


        const endDate =
            new Date(
                startDate.getTime()
                + duration * 60 * 1000
            );


        const day =
            String(
                endDate.getDate()
            ).padStart(
                2,
                "0"
            );


        const month =
            String(
                endDate.getMonth() + 1
            ).padStart(
                2,
                "0"
            );


        const year =
            endDate.getFullYear();


        const hour =
            String(
                endDate.getHours()
            ).padStart(
                2,
                "0"
            );


        const minute =
            String(
                endDate.getMinutes()
            ).padStart(
                2,
                "0"
            );


        endTimePreview.textContent =
            day
            + "/"
            + month
            + "/"
            + year
            + " "
            + hour
            + ":"
            + minute;
    }


    movieSelect.addEventListener(
        "change",
        updateEndTime
    );


    startDateInput.addEventListener(
        "change",
        updateEndTime
    );


    startClockInput.addEventListener(
        "input",
        updateEndTime
    );


    updateEndTime();

</script>


<%@ include file="/WEB-INF/include/footer.jsp" %>