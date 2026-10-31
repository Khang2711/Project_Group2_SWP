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
        max-width: 950px;
        margin: 0 auto;
        color: #f5f5f5;
    }

    .showtime-form-page h1 {
        margin-bottom: 8px;
    }

    .form-subtitle {
        margin-bottom: 22px;
        color: #aaa;
        line-height: 1.6;
    }

    .mode-switcher {
        display: flex;
        gap: 7px;
        margin-bottom: 20px;
        padding: 4px;
        width: fit-content;
        background: #18181d;
        border: 1px solid #303039;
        border-radius: 8px;
    }

    .mode-button {
        padding: 9px 15px;
        border: none;
        border-radius: 5px;
        background: transparent;
        color: #aaa;
        cursor: pointer;
        font-weight: 600;
    }

    .mode-button.active {
        background: #ff1744;
        color: white;
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

    .form-control {
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
    }

    .end-preview strong {
        color: #ff5873;
    }

    .form-error {
        margin-bottom: 20px;
        padding: 13px 15px;
        background: #36191f;
        color: #ffb9c1;
        border: 1px solid #71343e;
        border-radius: 6px;
    }

    .form-actions {
        display: flex;
        gap: 10px;
        margin-top: 24px;
    }

    .save-button,
    .add-row-button {
        padding: 11px 18px;
        border: none;
        border-radius: 6px;
        background: #ff1744;
        color: white;
        cursor: pointer;
        font-weight: bold;
    }

    .add-row-button {
        background: #292932;
        border: 1px solid #555;
    }

    .add-row-button:hover {
        border-color: #ff1744;
    }

    .cancel-button {
        padding: 11px 18px;
        border-radius: 6px;
        background: #292932;
        border: 1px solid #444;
        color: white;
        text-decoration: none;
    }

    #bulkPanel {
        display: none;
    }

    .bulk-description {
        margin-bottom: 20px;
        padding: 14px 16px;
        background: #202027;
        border-left: 4px solid #ff1744;
        border-radius: 6px;
        color: #bbb;
        line-height: 1.6;
    }

    .bulk-table-wrap {
        overflow-x: auto;
        border: 1px solid #303039;
        border-radius: 8px;
    }

    .bulk-table {
        width: 100%;
        min-width: 720px;
        border-collapse: collapse;
    }

    .bulk-table th {
        padding: 12px;
        background: #202027;
        color: #999;
        text-align: left;
        font-size: 12px;
    }

    .bulk-table td {
        padding: 10px;
        border-top: 1px solid #303039;
        vertical-align: middle;
    }

    .bulk-table .form-control {
        min-width: 140px;
    }

    .bulk-index {
        width: 40px;
        text-align: center;
        color: #999;
        font-weight: bold;
    }

    .remove-row-button {
        padding: 8px 11px;
        border: 1px solid #71323c;
        border-radius: 5px;
        background: transparent;
        color: #ff637d;
        cursor: pointer;
    }

    .bulk-toolbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 15px;
        margin-top: 15px;
    }

    .bulk-count {
        color: #999;
        font-size: 13px;
    }

    @media (max-width: 650px) {

        .date-time-row {
            grid-template-columns: 1fr;
        }

        .mode-switcher {
            width: 100%;
        }

        .mode-button {
            flex: 1;
        }

        .bulk-toolbar {
            align-items: flex-start;
            flex-direction: column;
        }

    }

</style>


<section class="showtime-form-page">

    <h1>

        <c:if test="${not empty showtime and showtime.showtimeId > 0}">
            Sửa lịch chiếu
        </c:if>

        <c:if test="${empty showtime or showtime.showtimeId == 0}">
            Tạo lịch chiếu
        </c:if>

    </h1>


    <p class="form-subtitle">

        Hệ thống tự tính giờ kết thúc dựa trên
        thời lượng phim và kiểm tra trùng lịch phòng chiếu.

    </p>


    <c:if test="${not empty error}">

        <div class="form-error">

            <c:out value="${error}" />

        </div>

    </c:if>


    <c:if test="${empty showtime or showtime.showtimeId == 0}">

        <div class="mode-switcher">

            <button type="button"
                    id="singleModeButton"
                    class="mode-button active">

                Thêm 1 lịch

            </button>

            <button type="button"
                    id="bulkModeButton"
                    class="mode-button">

                Thêm một loạt

            </button>

        </div>

    </c:if>


    <!-- ==================================================
         THÊM / SỬA MỘT LỊCH
         ================================================== -->

    <div id="singlePanel">

        <form method="post"
              action="${pageContext.request.contextPath}/admin/showtime-form"
              class="showtime-form-box">


            <c:if test="${not empty showtime and showtime.showtimeId > 0}">

                <input type="hidden"
                       name="id"
                       value="${showtime.showtimeId}">

            </c:if>


            <div class="form-group">

                <label>
                    Phim
                </label>

                <select id="movieId"
                        name="movieId"
                        class="form-control"
                        required>

                    <option value="">
                        -- Chọn phim --
                    </option>

                    <c:forEach items="${movies}"
                               var="movie">

                        <option value="${movie.movieId}"
                                data-duration="${movie.durationMinutes}"

                                <c:if test="${not empty showtime and showtime.movieId == movie.movieId}">
                                    selected
                                </c:if>>

                            <c:out value="${movie.title}" />

                            - ${movie.durationMinutes} phút

                        </option>

                    </c:forEach>

                </select>

            </div>


            <div class="form-group">

                <label>
                    Phòng chiếu
                </label>

                <select name="roomId"
                        class="form-control"
                        required>

                    <option value="">
                        -- Chọn phòng --
                    </option>

                    <c:forEach items="${rooms}"
                               var="room">

                        <option value="${room.roomId}"

                                <c:if test="${not empty showtime and showtime.roomId == room.roomId}">
                                    selected
                                </c:if>>

                            <c:out value="${room.roomName}" />

                            <c:if test="${not empty room.roomType}">
                                - ${room.roomType}
                            </c:if>

                        </option>

                    </c:forEach>

                </select>

            </div>


            <div class="date-time-row">

                <div class="form-group">

                    <label>
                        Ngày chiếu
                    </label>

                    <input type="date"
                           id="startDate"
                           name="startDate"
                           class="form-control"
                           value="${startDateValue}"
                           required>

                </div>


                <div class="form-group">

                    <label>
                        Giờ bắt đầu
                    </label>

                    <input type="time"
                           id="startClock"
                           name="startClock"
                           class="form-control"
                           value="${startClockValue}"
                           step="60"
                           required>

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

            </div>


            <div class="form-group">

                <label>
                    Trạng thái
                </label>

                <select name="status"
                        class="form-control"
                        required>

                    <option value="ACTIVE"

                            <c:if test="${empty showtime
                                          or empty showtime.status
                                          or showtime.status eq 'ACTIVE'}">
                                selected
                            </c:if>>

                        ACTIVE

                    </option>

                    <option value="INACTIVE"

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

    </div>


    <!-- ==================================================
         THÊM MỘT LOẠT
         ================================================== -->

    <c:if test="${empty showtime or showtime.showtimeId == 0}">

        <div id="bulkPanel">

            <form method="post"
                  action="${pageContext.request.contextPath}/admin/showtime-bulk"
                  class="showtime-form-box">


                <div class="bulk-description">

                    Chọn <strong>một bộ phim</strong>,
                    sau đó thêm nhiều ca chiếu.

                    Mỗi ca có thể chọn
                    <strong>phòng, ngày và giờ khác nhau</strong>.

                    Nếu một ca bị trùng lịch phòng,
                    hệ thống sẽ không lưu cả loạt.

                </div>


                <div class="form-group">

                    <label>
                        Phim
                    </label>

                    <select id="bulkMovieId"
                            name="movieId"
                            class="form-control"
                            required>

                        <option value="">
                            -- Chọn phim --
                        </option>

                        <c:forEach items="${movies}"
                                   var="movie">

                            <option value="${movie.movieId}"
                                    data-duration="${movie.durationMinutes}">

                                <c:out value="${movie.title}" />

                                - ${movie.durationMinutes} phút

                            </option>

                        </c:forEach>

                    </select>

                    <p class="form-help">

                        Tất cả các ca bên dưới sẽ áp dụng
                        cho bộ phim này.

                    </p>

                </div>


                <div class="form-group">

                    <label>
                        Trạng thái
                    </label>

                    <select name="status"
                            class="form-control">

                        <option value="ACTIVE">
                            ACTIVE
                        </option>

                        <option value="INACTIVE">
                            INACTIVE
                        </option>

                    </select>

                </div>


                <div class="bulk-table-wrap">

                    <table class="bulk-table">

                        <thead>

                            <tr>

                                <th>#</th>
                                <th>Phòng</th>
                                <th>Ngày chiếu</th>
                                <th>Giờ bắt đầu</th>
                                <th>Kết thúc dự kiến</th>
                                <th></th>

                            </tr>

                        </thead>

                        <tbody id="bulkRows">
                        </tbody>

                    </table>

                </div>


                <div class="bulk-toolbar">

                    <button type="button"
                            id="addBulkRowButton"
                            class="add-row-button">

                        + Thêm ca chiếu

                    </button>

                    <div class="bulk-count">

                        Tổng:
                        <strong id="bulkCount">
                            0
                        </strong>
                        ca

                    </div>

                </div>


                <div class="form-actions">

                    <button type="submit"
                            class="save-button">

                        Tạo tất cả lịch chiếu

                    </button>

                    <a href="${pageContext.request.contextPath}/admin/showtimes"
                       class="cancel-button">

                        Hủy

                    </a>

                </div>

            </form>

        </div>


        <template id="bulkRowTemplate">

            <tr class="bulk-row">

                <td class="bulk-index">
                    1
                </td>

                <td>

                    <select name="bulkRoomId"
                            class="form-control"
                            required>

                        <option value="">
                            -- Phòng --
                        </option>

                        <c:forEach items="${rooms}"
                                   var="room">

                            <option value="${room.roomId}">

                                <c:out value="${room.roomName}" />

                                <c:if test="${not empty room.roomType}">
                                    - ${room.roomType}
                                </c:if>

                            </option>

                        </c:forEach>

                    </select>

                </td>

                <td>

                    <input type="date"
                           name="bulkDate"
                           class="form-control bulk-date"
                           required>

                </td>

                <td>

                    <input type="time"
                           name="bulkTime"
                           class="form-control bulk-time"
                           step="60"
                           required>

                </td>

                <td class="bulk-end">
                    -
                </td>

                <td>

                    <button type="button"
                            class="remove-row-button">

                        Xóa

                    </button>

                </td>

            </tr>

        </template>

    </c:if>

</section>


<script>

    var movieSelect =
            document.getElementById(
                    "movieId"
            );

    var startDateInput =
            document.getElementById(
                    "startDate"
            );

    var startClockInput =
            document.getElementById(
                    "startClock"
            );

    var endTimePreview =
            document.getElementById(
                    "endTimePreview"
            );


    function pad(value) {

        return String(value)
                .padStart(
                        2,
                        "0"
                );
    }


    function updateEndTime() {

        if (!movieSelect
                || !startDateInput
                || !startClockInput
                || !endTimePreview) {

            return;
        }

        var option =
                movieSelect.options[
                        movieSelect.selectedIndex
                ];

        var duration =
                parseInt(
                        option
                        ? option.dataset.duration
                        : "",
                        10
                );

        if (!duration
                || !startDateInput.value
                || !startClockInput.value) {

            endTimePreview.textContent =
                    "Chọn phim, ngày và giờ bắt đầu";

            return;
        }

        var start =
                new Date(
                        startDateInput.value
                        + "T"
                        + startClockInput.value
                );

        var end =
                new Date(
                        start.getTime()
                        + duration * 60000
                );

        endTimePreview.textContent =
                pad(end.getDate())
                + "/"
                + pad(end.getMonth() + 1)
                + "/"
                + end.getFullYear()
                + " "
                + pad(end.getHours())
                + ":"
                + pad(end.getMinutes());
    }


    if (movieSelect) {

        movieSelect.addEventListener(
                "change",
                updateEndTime
        );
    }

    if (startDateInput) {

        startDateInput.addEventListener(
                "change",
                updateEndTime
        );
    }

    if (startClockInput) {

        startClockInput.addEventListener(
                "input",
                updateEndTime
        );
    }

    updateEndTime();


    /* ==============================================
       SINGLE / BULK MODE
       ============================================== */

    var singleButton =
            document.getElementById(
                    "singleModeButton"
            );

    var bulkButton =
            document.getElementById(
                    "bulkModeButton"
            );

    var singlePanel =
            document.getElementById(
                    "singlePanel"
            );

    var bulkPanel =
            document.getElementById(
                    "bulkPanel"
            );


    function changeMode(mode) {

        if (!singlePanel
                || !bulkPanel) {

            return;
        }

        if (mode === "bulk") {

            singlePanel.style.display =
                    "none";

            bulkPanel.style.display =
                    "block";

            singleButton.classList.remove(
                    "active"
            );

            bulkButton.classList.add(
                    "active"
            );

        } else {

            singlePanel.style.display =
                    "block";

            bulkPanel.style.display =
                    "none";

            bulkButton.classList.remove(
                    "active"
            );

            singleButton.classList.add(
                    "active"
            );
        }
    }


    if (singleButton) {

        singleButton.addEventListener(
                "click",
                function () {

                    changeMode(
                            "single"
                    );
                }
        );
    }


    if (bulkButton) {

        bulkButton.addEventListener(
                "click",
                function () {

                    changeMode(
                            "bulk"
                    );
                }
        );
    }


    /* ==============================================
       BULK ROWS
       ============================================== */

    var bulkRows =
            document.getElementById(
                    "bulkRows"
            );

    var rowTemplate =
            document.getElementById(
                    "bulkRowTemplate"
            );

    var addRowButton =
            document.getElementById(
                    "addBulkRowButton"
            );

    var bulkMovie =
            document.getElementById(
                    "bulkMovieId"
            );

    var bulkCount =
            document.getElementById(
                    "bulkCount"
            );


    function updateBulkIndexes() {

        if (!bulkRows) {
            return;
        }

        var rows =
                bulkRows.querySelectorAll(
                        ".bulk-row"
                );

        rows.forEach(
                function (row, index) {

                    row.querySelector(
                            ".bulk-index"
                    ).textContent =
                            index + 1;
                }
        );

        if (bulkCount) {

            bulkCount.textContent =
                    rows.length;
        }
    }


    function updateBulkEnd(row) {

        if (!bulkMovie
                || !row) {

            return;
        }

        var movieOption =
                bulkMovie.options[
                        bulkMovie.selectedIndex
                ];

        var duration =
                parseInt(
                        movieOption
                        ? movieOption.dataset.duration
                        : "",
                        10
                );

        var dateInput =
                row.querySelector(
                        ".bulk-date"
                );

        var timeInput =
                row.querySelector(
                        ".bulk-time"
                );

        var endCell =
                row.querySelector(
                        ".bulk-end"
                );

        if (!duration
                || !dateInput.value
                || !timeInput.value) {

            endCell.textContent =
                    "-";

            return;
        }

        var start =
                new Date(
                        dateInput.value
                        + "T"
                        + timeInput.value
                );

        var end =
                new Date(
                        start.getTime()
                        + duration * 60000
                );

        endCell.textContent =
                pad(end.getDate())
                + "/"
                + pad(end.getMonth() + 1)
                + "/"
                + end.getFullYear()
                + " "
                + pad(end.getHours())
                + ":"
                + pad(end.getMinutes());
    }


    function updateAllBulkEnds() {

        if (!bulkRows) {
            return;
        }

        bulkRows.querySelectorAll(
                ".bulk-row"
        ).forEach(
                function (row) {

                    updateBulkEnd(
                            row
                    );
                }
        );
    }


    function addBulkRow() {

        if (!bulkRows
                || !rowTemplate) {

            return;
        }

        var clone =
                rowTemplate.content
                        .cloneNode(true);

        var row =
                clone.querySelector(
                        ".bulk-row"
                );

        row.querySelector(
                ".bulk-date"
        ).addEventListener(
                "change",
                function () {

                    updateBulkEnd(
                            row
                    );
                }
        );

        row.querySelector(
                ".bulk-time"
        ).addEventListener(
                "input",
                function () {

                    updateBulkEnd(
                            row
                    );
                }
        );

        row.querySelector(
                ".remove-row-button"
        ).addEventListener(
                "click",
                function () {

                    /*
                     * Luôn giữ ít nhất 1 dòng.
                     */
                    if (bulkRows.children.length <= 1) {

                        row.querySelector(
                                '[name="bulkRoomId"]'
                        ).value = "";

                        row.querySelector(
                                ".bulk-date"
                        ).value = "";

                        row.querySelector(
                                ".bulk-time"
                        ).value = "";

                        updateBulkEnd(
                                row
                        );

                        return;
                    }

                    row.remove();

                    updateBulkIndexes();
                }
        );

        bulkRows.appendChild(
                clone
        );

        updateBulkIndexes();
    }


    if (addRowButton) {

        addRowButton.addEventListener(
                "click",
                addBulkRow
        );

        /*
         * Mặc định tạo 3 dòng để Manager
         * nhập nhanh 3 ca đầu tiên.
         */
        addBulkRow();
        addBulkRow();
        addBulkRow();
    }


    if (bulkMovie) {

        bulkMovie.addEventListener(
                "change",
                updateAllBulkEnds
        );
    }


    <c:if test="${bulkMode == true or param.mode eq 'bulk'}">
        changeMode("bulk");
    </c:if>

</script>


<%@ include file="/WEB-INF/include/footer.jsp" %>