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
        max-width: 1000px;
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

        width: fit-content;

        margin-bottom: 20px;
        padding: 4px;

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

    .common-grid {
        display: grid;

        grid-template-columns:
            1.5fr
            1fr
            1fr;

        gap: 16px;
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

    .end-preview,
    .bulk-end-box {
        padding: 14px 15px;

        background: #202027;

        border: 1px solid #383840;
        border-radius: 6px;

        color: #ccc;
    }

    .end-preview strong,
    .bulk-end-box strong {
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
    .secondary-button {
        padding: 11px 18px;

        border-radius: 6px;

        cursor: pointer;

        font-weight: bold;
    }

    .save-button {
        border: none;

        background: #ff1744;
        color: white;
    }

    .save-button:hover {
        background: #df143c;
    }

    .secondary-button {
        background: #292932;
        color: white;

        border: 1px solid #555;
    }

    .secondary-button:hover {
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
        margin-bottom: 22px;
        padding: 14px 16px;

        background: #202027;

        border-left: 4px solid #ff1744;
        border-radius: 6px;

        color: #bbb;

        line-height: 1.6;
    }

    .bulk-end-box {
        margin-top: -5px;
        margin-bottom: 22px;
    }

    .date-mode-title {
        margin-bottom: 10px;

        font-weight: 600;
    }

    .date-mode-options {
        display: flex;

        gap: 10px;

        margin-bottom: 18px;
    }

    .date-mode-option {
        display: flex;

        align-items: center;

        gap: 7px;

        padding: 10px 13px;

        background: #202027;

        border: 1px solid #393942;
        border-radius: 7px;

        cursor: pointer;
    }

    .date-mode-option input {
        accent-color: #ff1744;
    }

    .date-panel {
        padding: 18px;

        background: #141418;

        border: 1px solid #303039;
        border-radius: 8px;
    }

    #rangeDatePanel {
        display: none;
    }

    .specific-date-add {
        display: grid;

        grid-template-columns: 1fr auto;

        gap: 10px;
    }

    .selected-dates {
        display: flex;

        flex-wrap: wrap;

        gap: 8px;

        margin-top: 14px;
    }

    .selected-date-chip {
        display: inline-flex;

        align-items: center;

        gap: 8px;

        padding: 7px 10px;

        background: #29292f;

        border: 1px solid #44444d;
        border-radius: 18px;

        color: #ddd;

        font-size: 13px;
    }

    .remove-date {
        width: 20px;
        height: 20px;

        padding: 0;

        border: none;
        border-radius: 50%;

        background: #4b242c;
        color: #ff9aac;

        cursor: pointer;
    }

    .no-date-message {
        color: #777;

        font-size: 13px;
    }

    .range-grid {
        display: grid;

        grid-template-columns: 1fr 1fr;

        gap: 16px;
    }

    .weekday-title {
        margin-top: 8px;
        margin-bottom: 10px;

        color: #ccc;

        font-weight: 600;
    }

    .weekday-presets {
        display: flex;

        flex-wrap: wrap;

        gap: 8px;

        margin-bottom: 12px;
    }

    .weekday-preset {
        padding: 7px 11px;

        background: #29292f;
        color: #bbb;

        border: 1px solid #45454d;
        border-radius: 5px;

        cursor: pointer;

        font-size: 12px;
    }

    .weekday-preset:hover {
        border-color: #ff1744;
        color: white;
    }

    .weekday-list {
        display: flex;

        flex-wrap: wrap;

        gap: 8px;
    }

    .weekday-option {
        position: relative;
    }

    .weekday-option input {
        position: absolute;

        opacity: 0;

        pointer-events: none;
    }

    .weekday-option span {
        display: flex;

        min-width: 42px;
        height: 38px;

        align-items: center;
        justify-content: center;

        padding: 0 7px;

        background: #25252b;
        color: #aaa;

        border: 1px solid #3c3c45;
        border-radius: 6px;

        cursor: pointer;
    }

    .weekday-option input:checked + span {
        background: #ff1744;
        color: white;

        border-color: #ff1744;
    }

    .preview-section {
        margin-top: 24px;
    }

    .preview-header {
        display: flex;

        justify-content: space-between;
        align-items: center;

        gap: 15px;

        margin-bottom: 10px;
    }

    .preview-header h3 {
        margin: 0;
    }

    .preview-count {
        color: #aaa;

        font-size: 13px;
    }

    .preview-count strong {
        color: #ff607a;

        font-size: 16px;
    }

    .preview-table-wrap {
        max-height: 390px;

        overflow: auto;

        border: 1px solid #303039;
        border-radius: 8px;
    }

    .preview-table {
        width: 100%;

        border-collapse: collapse;
    }

    .preview-table th {
        position: sticky;
        top: 0;

        z-index: 2;

        padding: 11px 12px;

        background: #202027;
        color: #999;

        text-align: left;

        font-size: 12px;
    }

    .preview-table td {
        padding: 11px 12px;

        border-top: 1px solid #303039;

        color: #ddd;

        font-size: 13px;
    }

    .preview-empty {
        padding: 32px !important;

        text-align: center;

        color: #777 !important;
    }

    .preview-warning {
        color: #f2c864 !important;
    }

    @media (max-width: 750px) {

        .common-grid,
        .date-time-row,
        .range-grid {
            grid-template-columns: 1fr;
        }

        .mode-switcher {
            width: 100%;
        }

        .mode-button {
            flex: 1;
        }

        .date-mode-options {
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

        Hệ thống tự tính giờ kết thúc theo thời lượng phim
        và kiểm tra trùng lịch phòng chiếu trước khi lưu.

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


    <!-- =====================================================
         THÊM / SỬA 1 LỊCH
         ===================================================== -->

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


    <!-- =====================================================
         THÊM MỘT LOẠT
         ===================================================== -->

    <c:if test="${empty showtime or showtime.showtimeId == 0}">

        <div id="bulkPanel">

            <form method="post"
                  id="bulkForm"
                  action="${pageContext.request.contextPath}/admin/showtime-bulk"
                  class="showtime-form-box">


                <div class="bulk-description">

                    Chọn <strong>1 phim + 1 phòng + 1 khung giờ</strong>
                    một lần.

                    Sau đó chọn nhiều ngày.
                    Hệ thống sẽ tự tạo lịch chiếu cho tất cả
                    những ngày đã chọn.

                </div>


                <div class="common-grid">


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
                                        data-duration="${movie.durationMinutes}"

                                        <c:if test="${param.movieId == movie.movieId}">
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

                        <select id="bulkRoomId"
                                name="roomId"
                                class="form-control"
                                required>

                            <option value="">
                                -- Chọn phòng --
                            </option>

                            <c:forEach items="${rooms}"
                                       var="room">

                                <option value="${room.roomId}"

                                        <c:if test="${param.roomId == room.roomId}">
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


                    <div class="form-group">

                        <label>
                            Giờ bắt đầu
                        </label>

                        <input type="time"
                               id="bulkStartClock"
                               name="startClock"
                               class="form-control"
                               step="60"
                               value="${param.startClock}"
                               required>

                    </div>


                </div>


                <div class="bulk-end-box">

                    Khung giờ:

                    <strong id="bulkEndPreview">
                        Chọn phim và giờ bắt đầu
                    </strong>

                </div>


                <div class="form-group">

                    <label>
                        Trạng thái
                    </label>

                    <select name="status"
                            class="form-control">

                        <option value="ACTIVE"

                                <c:if test="${empty param.status or param.status eq 'ACTIVE'}">
                                    selected
                                </c:if>>

                            ACTIVE

                        </option>

                        <option value="INACTIVE"

                                <c:if test="${param.status eq 'INACTIVE'}">
                                    selected
                                </c:if>>

                            INACTIVE

                        </option>

                    </select>

                </div>


                <div class="date-mode-title">
                    Chọn các ngày áp dụng
                </div>


                <div class="date-mode-options">


                    <label class="date-mode-option">

                        <input type="radio"
                               name="dateMode"
                               value="specific"
                               id="specificMode"

                               <c:if test="${empty param.dateMode
                                             or param.dateMode eq 'specific'}">
                                   checked
                               </c:if>>

                        Chọn từng ngày

                    </label>


                    <label class="date-mode-option">

                        <input type="radio"
                               name="dateMode"
                               value="range"
                               id="rangeMode"

                               <c:if test="${param.dateMode eq 'range'}">
                                   checked
                               </c:if>>

                        Chọn khoảng ngày

                    </label>


                </div>


                <!-- ================================
                     CHỌN TỪNG NGÀY
                     ================================ -->

                <div id="specificDatePanel"
                     class="date-panel">


                    <div class="specific-date-add">

                        <input type="date"
                               id="specificDatePicker"
                               class="form-control">

                        <button type="button"
                                id="addSpecificDateButton"
                                class="secondary-button">

                            + Thêm ngày

                        </button>

                    </div>


                    <p class="form-help">

                        Dùng khi lịch chiếu rời rạc.
                        Ví dụ: 02/11, 05/11, 08/11.

                    </p>


                    <div id="selectedDates"
                         class="selected-dates">
                    </div>


                    <div id="selectedDateHiddenInputs">

                        <c:forEach items="${paramValues.selectedDate}"
                                   var="savedDate">

                            <input type="hidden"
                                   class="selected-date-input"
                                   name="selectedDate"
                                   value="${savedDate}">

                        </c:forEach>

                    </div>


                </div>


                <!-- ================================
                     KHOẢNG NGÀY
                     ================================ -->

                <div id="rangeDatePanel"
                     class="date-panel">


                    <div class="range-grid">


                        <div class="form-group">

                            <label>
                                Từ ngày
                            </label>

                            <input type="date"
                                   id="rangeStart"
                                   name="rangeStart"
                                   value="${param.rangeStart}"
                                   class="form-control">

                        </div>


                        <div class="form-group">

                            <label>
                                Đến ngày
                            </label>

                            <input type="date"
                                   id="rangeEnd"
                                   name="rangeEnd"
                                   value="${param.rangeEnd}"
                                   class="form-control">

                        </div>


                    </div>


                    <div class="weekday-title">

                        Chiếu vào các thứ:

                    </div>


                    <div class="weekday-presets">

                        <button type="button"
                                class="weekday-preset"
                                data-preset="all">

                            Tất cả ngày

                        </button>

                        <button type="button"
                                class="weekday-preset"
                                data-preset="weekday">

                            Thứ 2 - Thứ 6

                        </button>

                        <button type="button"
                                class="weekday-preset"
                                data-preset="weekend">

                            Cuối tuần

                        </button>

                        <button type="button"
                                class="weekday-preset"
                                data-preset="none">

                            Bỏ chọn

                        </button>

                    </div>


                    <div class="weekday-list">


                        <label class="weekday-option">

                            <input type="checkbox"
                                   name="weekday"
                                   value="1"
                                   checked>

                            <span>T2</span>

                        </label>


                        <label class="weekday-option">

                            <input type="checkbox"
                                   name="weekday"
                                   value="2"
                                   checked>

                            <span>T3</span>

                        </label>


                        <label class="weekday-option">

                            <input type="checkbox"
                                   name="weekday"
                                   value="3"
                                   checked>

                            <span>T4</span>

                        </label>


                        <label class="weekday-option">

                            <input type="checkbox"
                                   name="weekday"
                                   value="4"
                                   checked>

                            <span>T5</span>

                        </label>


                        <label class="weekday-option">

                            <input type="checkbox"
                                   name="weekday"
                                   value="5"
                                   checked>

                            <span>T6</span>

                        </label>


                        <label class="weekday-option">

                            <input type="checkbox"
                                   name="weekday"
                                   value="6"
                                   checked>

                            <span>T7</span>

                        </label>


                        <label class="weekday-option">

                            <input type="checkbox"
                                   name="weekday"
                                   value="7"
                                   checked>

                            <span>CN</span>

                        </label>


                    </div>


                </div>


                <!-- ================================
                     PREVIEW
                     ================================ -->

                <div class="preview-section">


                    <div class="preview-header">

                        <h3>
                            Các lịch sẽ tạo
                        </h3>


                        <div class="preview-count">

                            Tổng:

                            <strong id="previewCount">
                                0
                            </strong>

                            suất

                        </div>

                    </div>


                    <div class="preview-table-wrap">


                        <table class="preview-table">


                            <thead>

                                <tr>

                                    <th>#</th>
                                    <th>Ngày</th>
                                    <th>Thứ</th>
                                    <th>Phòng</th>
                                    <th>Khung giờ</th>
                                    <th>Trạng thái</th>

                                </tr>

                            </thead>


                            <tbody id="previewBody">

                                <tr>

                                    <td colspan="6"
                                        class="preview-empty">

                                        Chưa chọn ngày chiếu.

                                    </td>

                                </tr>

                            </tbody>


                        </table>


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

    </c:if>

</section>


<script>

    /* =====================================================
       COMMON
       ===================================================== */

    function pad(value) {

        return String(value)
                .padStart(
                        2,
                        "0"
                );
    }


    function parseDateKey(value) {

        if (!value) {
            return null;
        }

        var parts =
                value.split("-");

        if (parts.length !== 3) {
            return null;
        }

        return new Date(
                Number(parts[0]),
                Number(parts[1]) - 1,
                Number(parts[2])
        );
    }


    function dateKey(date) {

        return date.getFullYear()
                + "-"
                + pad(
                        date.getMonth() + 1
                )
                + "-"
                + pad(
                        date.getDate()
                );
    }


    function formatDate(value) {

        var date =
                typeof value === "string"
                        ? parseDateKey(value)
                        : value;

        if (!date) {
            return "";
        }

        return pad(
                date.getDate()
        )
                + "/"
                + pad(
                        date.getMonth() + 1
                )
                + "/"
                + date.getFullYear();
    }


    function weekdayName(date) {

        var names = [
            "Chủ nhật",
            "Thứ 2",
            "Thứ 3",
            "Thứ 4",
            "Thứ 5",
            "Thứ 6",
            "Thứ 7"
        ];

        return names[
                date.getDay()
        ];
    }


    function getDuration(select) {

        if (!select) {
            return 0;
        }

        var option =
                select.options[
                        select.selectedIndex
                ];

        return parseInt(
                option
                ? option.dataset.duration
                : "",
                10
        ) || 0;
    }


    function calculateEndClock(
            startClock,
            duration) {

        if (!startClock
                || !duration) {

            return "";
        }

        var parts =
                startClock.split(":");

        var totalMinutes =
                Number(parts[0]) * 60
                + Number(parts[1])
                + duration;

        var nextDay =
                totalMinutes >= 1440;

        totalMinutes =
                totalMinutes % 1440;

        var hour =
                Math.floor(
                        totalMinutes / 60
                );

        var minute =
                totalMinutes % 60;

        var result =
                pad(hour)
                + ":"
                + pad(minute);

        if (nextDay) {

            result +=
                    " (+1 ngày)";
        }

        return result;
    }


    /* =====================================================
       SINGLE SHOWTIME
       ===================================================== */

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


    function updateSingleEndTime() {

        if (!movieSelect
                || !startDateInput
                || !startClockInput
                || !endTimePreview) {

            return;
        }

        var duration =
                getDuration(
                        movieSelect
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
                updateSingleEndTime
        );
    }


    if (startDateInput) {

        startDateInput.addEventListener(
                "change",
                updateSingleEndTime
        );
    }


    if (startClockInput) {

        startClockInput.addEventListener(
                "input",
                updateSingleEndTime
        );
    }


    updateSingleEndTime();


    /* =====================================================
       SINGLE / BULK MODE
       ===================================================== */

    var singleModeButton =
            document.getElementById(
                    "singleModeButton"
            );

    var bulkModeButton =
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


    function changeMainMode(mode) {

        if (!singlePanel
                || !bulkPanel) {

            return;
        }

        if (mode === "bulk") {

            singlePanel.style.display =
                    "none";

            bulkPanel.style.display =
                    "block";

            singleModeButton
                    .classList
                    .remove(
                            "active"
                    );

            bulkModeButton
                    .classList
                    .add(
                            "active"
                    );

            updateBulkPreview();

        } else {

            singlePanel.style.display =
                    "block";

            bulkPanel.style.display =
                    "none";

            bulkModeButton
                    .classList
                    .remove(
                            "active"
                    );

            singleModeButton
                    .classList
                    .add(
                            "active"
                    );
        }
    }


    if (singleModeButton) {

        singleModeButton.addEventListener(
                "click",
                function () {

                    changeMainMode(
                            "single"
                    );
                }
        );
    }


    if (bulkModeButton) {

        bulkModeButton.addEventListener(
                "click",
                function () {

                    changeMainMode(
                            "bulk"
                    );
                }
        );
    }


    /* =====================================================
       BULK COMMON
       ===================================================== */

    var bulkForm =
            document.getElementById(
                    "bulkForm"
            );

    var bulkMovie =
            document.getElementById(
                    "bulkMovieId"
            );

    var bulkRoom =
            document.getElementById(
                    "bulkRoomId"
            );

    var bulkStartClock =
            document.getElementById(
                    "bulkStartClock"
            );

    var bulkEndPreview =
            document.getElementById(
                    "bulkEndPreview"
            );


    function updateBulkEndPreview() {

        if (!bulkMovie
                || !bulkStartClock
                || !bulkEndPreview) {

            return;
        }

        var duration =
                getDuration(
                        bulkMovie
                );

        var endClock =
                calculateEndClock(
                        bulkStartClock.value,
                        duration
                );

        if (!endClock) {

            bulkEndPreview.textContent =
                    "Chọn phim và giờ bắt đầu";

        } else {

            bulkEndPreview.textContent =
                    bulkStartClock.value
                    + " - "
                    + endClock;
        }
    }


    /* =====================================================
       DATE MODE
       ===================================================== */

    var specificMode =
            document.getElementById(
                    "specificMode"
            );

    var rangeMode =
            document.getElementById(
                    "rangeMode"
            );

    var specificDatePanel =
            document.getElementById(
                    "specificDatePanel"
            );

    var rangeDatePanel =
            document.getElementById(
                    "rangeDatePanel"
            );


    function updateDateMode() {

        if (!specificMode
                || !rangeMode) {

            return;
        }

        if (rangeMode.checked) {

            specificDatePanel.style.display =
                    "none";

            rangeDatePanel.style.display =
                    "block";

        } else {

            specificDatePanel.style.display =
                    "block";

            rangeDatePanel.style.display =
                    "none";
        }

        updateBulkPreview();
    }


    if (specificMode) {

        specificMode.addEventListener(
                "change",
                updateDateMode
        );
    }


    if (rangeMode) {

        rangeMode.addEventListener(
                "change",
                updateDateMode
        );
    }


    /* =====================================================
       SPECIFIC DATES
       ===================================================== */

    var specificDatePicker =
            document.getElementById(
                    "specificDatePicker"
            );

    var addSpecificDateButton =
            document.getElementById(
                    "addSpecificDateButton"
            );

    var selectedDatesContainer =
            document.getElementById(
                    "selectedDates"
            );

    var selectedDateHiddenInputs =
            document.getElementById(
                    "selectedDateHiddenInputs"
            );

    var selectedDates = [];


    if (selectedDateHiddenInputs) {

        selectedDateHiddenInputs
                .querySelectorAll(
                        ".selected-date-input"
                )
                .forEach(
                        function (input) {

                            if (input.value
                                    && selectedDates
                                            .indexOf(
                                                    input.value
                                            ) === -1) {

                                selectedDates.push(
                                        input.value
                                );
                            }
                        }
                );
    }


    function syncSelectedDateInputs() {

        if (!selectedDateHiddenInputs) {
            return;
        }

        selectedDateHiddenInputs.innerHTML =
                "";

        selectedDates.forEach(
                function (value) {

                    var input =
                            document.createElement(
                                    "input"
                            );

                    input.type =
                            "hidden";

                    input.name =
                            "selectedDate";

                    input.value =
                            value;

                    input.className =
                            "selected-date-input";

                    selectedDateHiddenInputs
                            .appendChild(
                                    input
                            );
                }
        );
    }


    function renderSelectedDates() {

        if (!selectedDatesContainer) {
            return;
        }

        selectedDates.sort();

        selectedDatesContainer.innerHTML =
                "";

        if (selectedDates.length === 0) {

            var empty =
                    document.createElement(
                            "div"
                    );

            empty.className =
                    "no-date-message";

            empty.textContent =
                    "Chưa chọn ngày nào.";

            selectedDatesContainer
                    .appendChild(
                            empty
                    );

            syncSelectedDateInputs();

            return;
        }

        selectedDates.forEach(
                function (value) {

                    var chip =
                            document.createElement(
                                    "div"
                            );

                    chip.className =
                            "selected-date-chip";


                    var label =
                            document.createElement(
                                    "span"
                            );

                    label.textContent =
                            formatDate(
                                    value
                            );


                    var remove =
                            document.createElement(
                                    "button"
                            );

                    remove.type =
                            "button";

                    remove.className =
                            "remove-date";

                    remove.textContent =
                            "×";


                    remove.addEventListener(
                            "click",
                            function () {

                                selectedDates =
                                        selectedDates.filter(
                                                function (date) {

                                                    return date
                                                            !== value;
                                                }
                                        );

                                renderSelectedDates();

                                updateBulkPreview();
                            }
                    );


                    chip.appendChild(
                            label
                    );

                    chip.appendChild(
                            remove
                    );

                    selectedDatesContainer
                            .appendChild(
                                    chip
                            );
                }
        );

        syncSelectedDateInputs();
    }


    if (addSpecificDateButton) {

        addSpecificDateButton.addEventListener(
                "click",
                function () {

                    var value =
                            specificDatePicker.value;

                    if (!value) {

                        alert(
                                "Vui lòng chọn ngày."
                        );

                        return;
                    }

                    if (selectedDates
                            .indexOf(
                                    value
                            ) === -1) {

                        selectedDates.push(
                                value
                        );
                    }

                    specificDatePicker.value =
                            "";

                    renderSelectedDates();

                    updateBulkPreview();
                }
        );
    }


    /* =====================================================
       RANGE DATES
       ===================================================== */

    var rangeStart =
            document.getElementById(
                    "rangeStart"
            );

    var rangeEnd =
            document.getElementById(
                    "rangeEnd"
            );

    var weekdayCheckboxes =
            document.querySelectorAll(
                    'input[name="weekday"]'
            );


    /*
     * Nếu submit lỗi thì giữ lại
     * các thứ Manager đã chọn.
     */
    var submittedWeekdays = [];

    <c:forEach items="${paramValues.weekday}"
               var="savedWeekday">

        submittedWeekdays.push(
                "${savedWeekday}"
        );

    </c:forEach>


    if (submittedWeekdays.length > 0) {

        weekdayCheckboxes.forEach(
                function (checkbox) {

                    checkbox.checked =
                            submittedWeekdays
                                    .indexOf(
                                            checkbox.value
                                    ) !== -1;
                }
        );
    }


    function getRangeDates() {

        var result = [];

        if (!rangeStart
                || !rangeEnd
                || !rangeStart.value
                || !rangeEnd.value) {

            return result;
        }

        var start =
                parseDateKey(
                        rangeStart.value
                );

        var end =
                parseDateKey(
                        rangeEnd.value
                );

        if (!start
                || !end
                || end < start) {

            return result;
        }

        var selectedWeekdays = [];

        weekdayCheckboxes.forEach(
                function (checkbox) {

                    if (checkbox.checked) {

                        selectedWeekdays.push(
                                Number(
                                        checkbox.value
                                )
                        );
                    }
                }
        );

        if (selectedWeekdays.length === 0) {

            return result;
        }

        var current =
                new Date(
                        start.getFullYear(),
                        start.getMonth(),
                        start.getDate()
                );

        while (current <= end) {

            var javaWeekday =
                    current.getDay() === 0
                            ? 7
                            : current.getDay();

            if (selectedWeekdays
                    .indexOf(
                            javaWeekday
                    ) !== -1) {

                result.push(
                        dateKey(
                                current
                        )
                );
            }

            current.setDate(
                    current.getDate()
                    + 1
            );

            if (result.length > 50) {
                break;
            }
        }

        return result;
    }


    if (rangeStart) {

        rangeStart.addEventListener(
                "change",
                updateBulkPreview
        );
    }


    if (rangeEnd) {

        rangeEnd.addEventListener(
                "change",
                updateBulkPreview
        );
    }


    weekdayCheckboxes.forEach(
            function (checkbox) {

                checkbox.addEventListener(
                        "change",
                        updateBulkPreview
                );
            }
    );


    /* =====================================================
       WEEKDAY PRESETS
       ===================================================== */

    document.querySelectorAll(
            ".weekday-preset"
    ).forEach(
            function (button) {

                button.addEventListener(
                        "click",
                        function () {

                            var preset =
                                    button.dataset.preset;

                            weekdayCheckboxes.forEach(
                                    function (checkbox) {

                                        var value =
                                                Number(
                                                        checkbox.value
                                                );

                                        if (preset === "all") {

                                            checkbox.checked =
                                                    true;

                                        } else if (
                                                preset === "weekday"
                                        ) {

                                            checkbox.checked =
                                                    value >= 1
                                                    && value <= 5;

                                        } else if (
                                                preset === "weekend"
                                        ) {

                                            checkbox.checked =
                                                    value === 6
                                                    || value === 7;

                                        } else {

                                            checkbox.checked =
                                                    false;
                                        }
                                    }
                            );

                            updateBulkPreview();
                        }
                );
            }
    );


    /* =====================================================
       PREVIEW
       ===================================================== */

    var previewBody =
            document.getElementById(
                    "previewBody"
            );

    var previewCount =
            document.getElementById(
                    "previewCount"
            );


    function getSelectedBulkDates() {

        if (rangeMode
                && rangeMode.checked) {

            return getRangeDates();
        }

        return selectedDates.slice();
    }


    function updateBulkPreview() {

        if (!previewBody
                || !previewCount) {

            return;
        }

        updateBulkEndPreview();

        var dates =
                getSelectedBulkDates();

        dates.sort();

        previewCount.textContent =
                dates.length;

        previewBody.innerHTML =
                "";

        if (dates.length === 0) {

            var row =
                    document.createElement(
                            "tr"
                    );

            var cell =
                    document.createElement(
                            "td"
                    );

            cell.colSpan =
                    6;

            cell.className =
                    "preview-empty";

            cell.textContent =
                    "Chưa chọn ngày chiếu.";

            row.appendChild(
                    cell
            );

            previewBody.appendChild(
                    row
            );

            return;
        }

        var roomText =
                "-";

        if (bulkRoom
                && bulkRoom.selectedIndex > 0) {

            roomText =
                    bulkRoom.options[
                            bulkRoom.selectedIndex
                    ].textContent.trim();
        }

        var duration =
                getDuration(
                        bulkMovie
                );

        var endClock =
                calculateEndClock(
                        bulkStartClock
                        ? bulkStartClock.value
                        : "",
                        duration
                );

        dates.forEach(
                function (value, index) {

                    var date =
                            parseDateKey(
                                    value
                            );

                    var row =
                            document.createElement(
                                    "tr"
                            );


                    var numberCell =
                            document.createElement(
                                    "td"
                            );

                    numberCell.textContent =
                            index + 1;


                    var dateCell =
                            document.createElement(
                                    "td"
                            );

                    dateCell.textContent =
                            formatDate(
                                    date
                            );


                    var weekdayCell =
                            document.createElement(
                                    "td"
                            );

                    weekdayCell.textContent =
                            weekdayName(
                                    date
                            );


                    var roomCell =
                            document.createElement(
                                    "td"
                            );

                    roomCell.textContent =
                            roomText;


                    var timeCell =
                            document.createElement(
                                    "td"
                            );

                    if (bulkStartClock
                            && bulkStartClock.value
                            && endClock) {

                        timeCell.textContent =
                                bulkStartClock.value
                                + " - "
                                + endClock;

                    } else {

                        timeCell.textContent =
                                "-";
                    }


                    var statusCell =
                            document.createElement(
                                    "td"
                            );

                    statusCell.className =
                            "preview-warning";

                    statusCell.textContent =
                            "Kiểm tra khi lưu";


                    row.appendChild(
                            numberCell
                    );

                    row.appendChild(
                            dateCell
                    );

                    row.appendChild(
                            weekdayCell
                    );

                    row.appendChild(
                            roomCell
                    );

                    row.appendChild(
                            timeCell
                    );

                    row.appendChild(
                            statusCell
                    );

                    previewBody.appendChild(
                            row
                    );
                }
        );
    }


    if (bulkMovie) {

        bulkMovie.addEventListener(
                "change",
                updateBulkPreview
        );
    }


    if (bulkRoom) {

        bulkRoom.addEventListener(
                "change",
                updateBulkPreview
        );
    }


    if (bulkStartClock) {

        bulkStartClock.addEventListener(
                "input",
                updateBulkPreview
        );
    }


    /* =====================================================
       VALIDATE
       ===================================================== */

    if (bulkForm) {

        bulkForm.addEventListener(
                "submit",
                function (event) {

                    var dates =
                            getSelectedBulkDates();

                    if (dates.length === 0) {

                        event.preventDefault();

                        alert(
                                "Vui lòng chọn ít nhất một ngày chiếu."
                        );

                        return;
                    }

                    if (dates.length > 50) {

                        event.preventDefault();

                        alert(
                                "Mỗi lần chỉ được tạo tối đa 50 lịch chiếu."
                        );

                        return;
                    }

                    if (rangeMode
                            && rangeMode.checked) {

                        if (!rangeStart.value
                                || !rangeEnd.value) {

                            event.preventDefault();

                            alert(
                                    "Vui lòng chọn đầy đủ khoảng ngày."
                            );

                            return;
                        }

                        if (rangeEnd.value
                                < rangeStart.value) {

                            event.preventDefault();

                            alert(
                                    "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu."
                            );

                            return;
                        }
                    }
                }
        );
    }


    /* =====================================================
       MIN DATE
       ===================================================== */

    var today =
            new Date();

    var todayValue =
            dateKey(
                    today
            );


    if (specificDatePicker) {

        specificDatePicker.min =
                todayValue;
    }


    if (rangeStart) {

        rangeStart.min =
                todayValue;
    }


    if (rangeEnd) {

        rangeEnd.min =
                todayValue;
    }


    /* =====================================================
       INITIAL
       ===================================================== */

    renderSelectedDates();

    updateDateMode();

    updateBulkPreview();


    <c:if test="${bulkMode == true or param.mode eq 'bulk'}">

        changeMainMode(
                "bulk"
        );

    </c:if>

</script>


<%@ include file="/WEB-INF/include/footer.jsp" %>