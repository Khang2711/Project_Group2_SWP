<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ taglib prefix="fn"
           uri="jakarta.tags.functions" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>

    .showtime-page {
        color: white;
    }

    .showtime-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 20px;
        margin-bottom: 22px;
    }

    .showtime-header h1 {
        margin: 0 0 8px;
    }

    .showtime-subtitle {
        margin: 0;
        color: #999;
    }

    .showtime-header-right {
        display: flex;
        align-items: center;
        gap: 12px;
        flex-wrap: wrap;
    }

    .add-showtime-button {
        padding: 10px 17px;

        background: #ff1744;
        color: white;

        border-radius: 6px;

        text-decoration: none;
        font-weight: bold;
    }

    /* VIEW BUTTON */

    .view-switcher {
        display: flex;

        padding: 3px;

        background: #18181d;

        border: 1px solid #393941;
        border-radius: 7px;
    }

    .view-button {
        padding: 8px 13px;

        border: none;
        border-radius: 5px;

        background: transparent;
        color: #aaa;

        cursor: pointer;

        font-size: 13px;
        font-weight: bold;
    }

    .view-button:hover {
        background: #292930;
        color: white;
    }

    .view-button.active {
        background: #ff1744;
        color: white;
    }

    /* NAVIGATION */

    .calendar-navigation {
        display: none;

        justify-content: space-between;
        align-items: center;

        gap: 15px;

        margin-bottom: 15px;
        padding: 12px 14px;

        background: #18181d;

        border: 1px solid #303039;
        border-radius: 8px;
    }

    .calendar-navigation.visible {
        display: flex;
    }

    .calendar-navigation-left {
        display: flex;
        align-items: center;
        gap: 7px;
    }

    .nav-button {
        height: 36px;

        padding: 0 13px;

        background: #25252b;
        color: white;

        border: 1px solid #44444d;
        border-radius: 6px;

        cursor: pointer;
    }

    .nav-button:hover {
        border-color: #ff1744;
    }

    .arrow-button {
        width: 38px;
        padding: 0;
        font-size: 20px;
    }

    .calendar-title {
        font-size: 18px;
        font-weight: bold;
    }

    /* MESSAGE */

    .message-success,
    .message-error {
        margin-bottom: 17px;
        padding: 13px 15px;
        border-radius: 6px;
    }

    .message-success {
        background: #163725;
        color: #75e7a3;
        border: 1px solid #28583b;
    }

    .message-error {
        background: #36191f;
        color: #ffb9c1;
        border: 1px solid #71343e;
    }

    /* LIST */

    .showtime-table-wrap {
        overflow-x: auto;

        background: #18181d;

        border: 1px solid #303039;
        border-radius: 10px;
    }

    .showtime-table {
        width: 100%;
        border-collapse: collapse;
    }

    .showtime-table th,
    .showtime-table td {
        padding: 14px 15px;

        border-bottom: 1px solid #303039;

        text-align: left;
        white-space: nowrap;
    }

    .showtime-table th {
        background: #202027;
        color: #aaa;
    }

    .status-active {
        color: #45d483;
        font-weight: bold;
    }

    .status-inactive {
        color: #888;
        font-weight: bold;
    }

    .showtime-actions {
        display: flex;
        align-items: center;
        gap: 7px;
    }

    .edit-button,
    .delete-button {
        padding: 7px 11px;

        border-radius: 5px;

        font-size: 13px;

        cursor: pointer;
    }

    .edit-button {
        background: #292932;
        color: white;

        border: 1px solid #444;

        text-decoration: none;
    }

    .delete-button {
        background: transparent;
        color: #ff637d;

        border: 1px solid #71323c;
    }

    .empty-showtime {
        padding: 40px;
        text-align: center;
        color: #888;
    }

    /* CALENDAR */

    #calendarView {
        display: none;
    }

    .calendar-box {
        overflow: hidden;

        background: #18181d;

        border: 1px solid #303039;
        border-radius: 9px;
    }

    /* DAY + WEEK */

    .time-calendar {
        max-height: 680px;
        overflow: auto;
    }

    .time-header {
        position: sticky;
        top: 0;

        z-index: 20;

        display: grid;

        background: #202027;

        border-bottom: 1px solid #35353e;
    }

    .day-header {
        grid-template-columns: 70px 1fr;
        min-width: 700px;
    }

    .week-header {
        grid-template-columns:
            70px
            repeat(7, minmax(125px, 1fr));

        min-width: 1000px;
    }

    .time-header-spacer {
        border-right: 1px solid #35353e;
    }

    .time-header-day {
        padding: 11px 7px;

        border-right: 1px solid #35353e;

        text-align: center;
    }

    .header-weekday {
        margin-bottom: 4px;

        color: #999;

        font-size: 12px;
    }

    .header-date {
        font-size: 18px;
        font-weight: bold;
    }

    .time-header-day.today .header-date {
        display: inline-flex;

        width: 34px;
        height: 34px;

        align-items: center;
        justify-content: center;

        background: #ff1744;

        border-radius: 50%;

        color: white;
    }

    .time-body {
        display: grid;
    }

    .day-body {
        grid-template-columns: 70px 1fr;
        min-width: 700px;
    }

    .week-body {
        grid-template-columns:
            70px
            repeat(7, minmax(125px, 1fr));

        min-width: 1000px;
    }

    .time-label-column {
        position: relative;

        height: 1536px;

        background: #151519;

        border-right: 1px solid #35353e;
    }

    .time-label {
        position: absolute;

        right: 9px;

        transform: translateY(-7px);

        color: #777;

        font-size: 11px;
    }

    .calendar-day-column {
        position: relative;

        height: 1536px;

        border-right: 1px solid #35353e;

        background-image:
            repeating-linear-gradient(
                to bottom,
                transparent 0,
                transparent 63px,
                #303039 63px,
                #303039 64px
            );
    }

    .calendar-day-column.today-column {
        background-color: #19171c;
    }

    /* EVENT */

    .calendar-event {
        position: absolute;

        left: 4px;
        right: 4px;

        z-index: 5;

        overflow: hidden;

        padding: 6px 7px;

        box-sizing: border-box;

        background: #382028;

        border: 1px solid #6c2635;
        border-left: 4px solid #ff1744;
        border-radius: 5px;

        color: white;

        text-decoration: none;
    }

    .calendar-event:hover {
        z-index: 10;
        background: #48242f;
    }

    .calendar-event.inactive {
        background: #29292e;

        border-color: #555;
        border-left-color: #777;

        opacity: 0.65;
    }

    .calendar-event-time {
        margin-bottom: 3px;

        color: #ff91a4;

        font-size: 11px;
        font-weight: bold;
    }

    .calendar-event-title {
        overflow: hidden;

        margin-bottom: 3px;

        font-size: 12px;
        font-weight: bold;

        white-space: nowrap;
        text-overflow: ellipsis;
    }

    .calendar-event-room {
        overflow: hidden;

        color: #bbb;

        font-size: 10px;

        white-space: nowrap;
        text-overflow: ellipsis;
    }

    /* MONTH */

    .month-week-header {
        display: grid;
        grid-template-columns: repeat(7, 1fr);

        background: #202027;

        border-bottom: 1px solid #303039;
    }

    .month-week-header div {
        padding: 11px;

        color: #999;

        text-align: center;

        font-size: 12px;
        font-weight: bold;
    }

    .month-grid {
        display: grid;
        grid-template-columns: repeat(7, 1fr);
    }

    .month-day {
        min-height: 130px;

        padding: 7px;

        border-right: 1px solid #303039;
        border-bottom: 1px solid #303039;

        cursor: pointer;
    }

    .month-day:hover {
        background: #202027;
    }

    .month-day.other-month {
        background: #121216;
        color: #555;
    }

    .month-day.today {
        background: #21181c;
    }

    .month-day-number {
        display: flex;

        width: 29px;
        height: 29px;

        align-items: center;
        justify-content: center;

        margin-bottom: 5px;

        border-radius: 50%;

        font-size: 13px;
        font-weight: bold;
    }

    .month-day.today
    .month-day-number {
        background: #ff1744;
        color: white;
    }

    .month-event {
        display: block;

        overflow: hidden;

        margin-bottom: 4px;
        padding: 5px 6px;

        background: #322027;

        border-left: 3px solid #ff1744;
        border-radius: 4px;

        color: white;

        text-decoration: none;

        font-size: 10px;

        white-space: nowrap;
        text-overflow: ellipsis;
    }

    .month-event:hover {
        background: #472631;
    }

    .month-event.inactive {
        background: #29292e;

        border-left-color: #777;

        opacity: 0.65;
    }

    .more-events {
        padding: 3px 5px;

        color: #999;

        font-size: 10px;
    }

</style>


<section class="showtime-page">

    <div class="showtime-header">

        <div>

            <h1>
                Quản lý lịch chiếu
            </h1>

            <p class="showtime-subtitle">
                Quản lý các suất chiếu của rạp.
            </p>

        </div>


        <div class="showtime-header-right">

            <div class="view-switcher">

                <button type="button"
                        class="view-button active"
                        data-view="list">
                    Danh sách
                </button>

                <button type="button"
                        class="view-button"
                        data-view="day">
                    Ngày
                </button>

                <button type="button"
                        class="view-button"
                        data-view="week">
                    Tuần
                </button>

                <button type="button"
                        class="view-button"
                        data-view="month">
                    Tháng
                </button>

            </div>


            <a class="add-showtime-button"
               href="${pageContext.request.contextPath}/admin/showtime-form">

                + Thêm lịch chiếu

            </a>

        </div>

    </div>


    <c:if test="${param.saved eq '1'}">

        <div class="message-success">
            Lưu lịch chiếu thành công.
        </div>

    </c:if>


    <c:if test="${param.deleted eq '1'}">

        <div class="message-success">
            Xử lý lịch chiếu thành công.
        </div>

    </c:if>


    <c:if test="${not empty error}">

        <div class="message-error">
            <c:out value="${error}" />
        </div>

    </c:if>


    <div id="calendarNavigation"
         class="calendar-navigation">

        <div class="calendar-navigation-left">

            <button type="button"
                    id="previousButton"
                    class="nav-button arrow-button">
                ‹
            </button>

            <button type="button"
                    id="todayButton"
                    class="nav-button">
                Hôm nay
            </button>

            <button type="button"
                    id="nextButton"
                    class="nav-button arrow-button">
                ›
            </button>

        </div>

        <div id="calendarTitle"
             class="calendar-title">
        </div>

    </div>


    <div id="listView">

        <div class="showtime-table-wrap">

            <c:if test="${empty showtimes}">

                <div class="empty-showtime">
                    Chưa có lịch chiếu nào.
                </div>

            </c:if>


            <c:if test="${not empty showtimes}">

                <table class="showtime-table">

                    <thead>

                        <tr>
                            <th>ID</th>
                            <th>Phim</th>
                            <th>Phòng</th>
                            <th>Bắt đầu</th>
                            <th>Kết thúc</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>

                    </thead>


                    <tbody>

                        <c:forEach items="${showtimes}"
                                   var="showtime">

                            <tr>

                                <td>
                                    ${showtime.showtimeId}
                                </td>

                                <td>
                                    <c:out value="${movieById[showtime.movieId].title}" />
                                </td>

                                <td>
                                    <c:out value="${roomById[showtime.roomId].roomName}" />
                                </td>

                                <td>

                                    <fmt:formatDate
                                        value="${showtime.startTime}"
                                        pattern="dd/MM/yyyy HH:mm" />

                                </td>

                                <td>

                                    <fmt:formatDate
                                        value="${showtime.endTime}"
                                        pattern="dd/MM/yyyy HH:mm" />

                                </td>

                                <td>

                                    <c:if test="${showtime.status eq 'ACTIVE'}">

                                        <span class="status-active">
                                            ACTIVE
                                        </span>

                                    </c:if>

                                    <c:if test="${showtime.status ne 'ACTIVE'}">

                                        <span class="status-inactive">
                                            INACTIVE
                                        </span>

                                    </c:if>

                                </td>

                                <td>

                                    <div class="showtime-actions">

                                        <a class="edit-button"
                                           href="${pageContext.request.contextPath}/admin/showtime-form?id=${showtime.showtimeId}">
                                            Sửa
                                        </a>


                                        <form method="post"
                                              action="${pageContext.request.contextPath}/admin/showtimes"
                                              onsubmit="return confirm('Bạn có chắc muốn xóa lịch chiếu này?');">

                                            <input type="hidden"
                                                   name="action"
                                                   value="delete">

                                            <input type="hidden"
                                                   name="id"
                                                   value="${showtime.showtimeId}">

                                            <button type="submit"
                                                    class="delete-button">
                                                Xóa
                                            </button>

                                        </form>

                                    </div>

                                </td>

                            </tr>

                        </c:forEach>

                    </tbody>

                </table>

            </c:if>

        </div>

    </div>


    <div id="calendarView">

        <div id="calendarContent"
             class="calendar-box">
        </div>

    </div>


    <div style="display:none">

        <c:forEach items="${showtimes}"
                   var="showtime">

            <fmt:formatDate
                value="${showtime.startTime}"
                pattern="yyyy-MM-dd"
                var="eventStartDate" />

            <fmt:formatDate
                value="${showtime.startTime}"
                pattern="HH:mm"
                var="eventStartTime" />

            <fmt:formatDate
                value="${showtime.endTime}"
                pattern="yyyy-MM-dd"
                var="eventEndDate" />

            <fmt:formatDate
                value="${showtime.endTime}"
                pattern="HH:mm"
                var="eventEndTime" />


            <div class="showtime-data-item"
                 data-id="${showtime.showtimeId}"
                 data-movie="${fn:escapeXml(movieById[showtime.movieId].title)}"
                 data-room="${fn:escapeXml(roomById[showtime.roomId].roomName)}"
                 data-start-date="${eventStartDate}"
                 data-start-time="${eventStartTime}"
                 data-end-date="${eventEndDate}"
                 data-end-time="${eventEndTime}"
                 data-status="${showtime.status}">
            </div>

        </c:forEach>

    </div>

</section>


<script>

    var contextPath =
            "${pageContext.request.contextPath}";

    var HOUR_HEIGHT = 64;

    var DAY_HEIGHT =
            24 * HOUR_HEIGHT;


    var showtimes =
            Array.from(
                    document.querySelectorAll(
                            ".showtime-data-item"
                    )
            ).map(
                    function (item) {

                        return {

                            id:
                                    item.dataset.id,

                            movie:
                                    item.dataset.movie,

                            room:
                                    item.dataset.room,

                            startDate:
                                    item.dataset.startDate,

                            startTime:
                                    item.dataset.startTime,

                            endDate:
                                    item.dataset.endDate,

                            endTime:
                                    item.dataset.endTime,

                            status:
                                    item.dataset.status
                        };
                    }
            );


    var listView =
            document.getElementById(
                    "listView"
            );


    var calendarView =
            document.getElementById(
                    "calendarView"
            );


    var calendarContent =
            document.getElementById(
                    "calendarContent"
            );


    var calendarNavigation =
            document.getElementById(
                    "calendarNavigation"
            );


    var calendarTitle =
            document.getElementById(
                    "calendarTitle"
            );


    var viewButtons =
            document.querySelectorAll(
                    ".view-button"
            );


    var currentView =
            "list";


    var currentDate =
            new Date();


    currentDate.setHours(
            0,
            0,
            0,
            0
    );


    function pad(number) {

        return String(number)
                .padStart(
                        2,
                        "0"
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


    function dateFromKey(key) {

        var parts =
                key.split("-");


        return new Date(
                Number(parts[0]),
                Number(parts[1]) - 1,
                Number(parts[2])
        );
    }


    function formatDate(date) {

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


    function escapeHtml(value) {

        var element =
                document.createElement(
                        "div"
                );


        element.textContent =
                value == null
                        ? ""
                        : value;


        return element.innerHTML;
    }


    function eventsForDate(date) {

        var key =
                dateKey(date);


        return showtimes
                .filter(
                        function (showtime) {

                            return showtime.startDate
                                    === key;
                        }
                )
                .sort(
                        function (a, b) {

                            return a.startTime
                                    .localeCompare(
                                            b.startTime
                                    );
                        }
                );
    }


    function minutesFromTime(value) {

        var parts =
                value.split(":");


        return Number(parts[0]) * 60
                + Number(parts[1]);
    }


    function eventTop(showtime) {

        var minutes =
                minutesFromTime(
                        showtime.startTime
                );


        return minutes / 60
                * HOUR_HEIGHT;
    }


    function eventHeight(showtime) {

        var start =
                minutesFromTime(
                        showtime.startTime
                );


        var end;


        if (showtime.endDate
                === showtime.startDate) {

            end =
                    minutesFromTime(
                            showtime.endTime
                    );

        } else {

            end =
                    24 * 60;
        }


        var duration =
                Math.max(
                        30,
                        end - start
                );


        return Math.max(
                32,
                duration / 60
                * HOUR_HEIGHT
        );
    }


    function editUrl(showtime) {

        return contextPath
                + "/admin/showtime-form?id="
                + showtime.id;
    }


    function createTimeLabels() {

        var html = "";


        for (
                var hour = 0;
                hour < 24;
                hour++
        ) {

            html +=
                    '<div class="time-label" '
                    + 'style="top:'
                    + (hour * HOUR_HEIGHT)
                    + 'px">'

                    + pad(hour)
                    + ':00'

                    + '</div>';
        }


        return html;
    }


    function createCalendarEvent(
            showtime) {

        var inactiveClass =
                showtime.status === "ACTIVE"
                        ? ""
                        : " inactive";


        var top =
                eventTop(
                        showtime
                );


        var height =
                eventHeight(
                        showtime
                );


        var html = "";


        html +=
                '<a href="'
                + editUrl(showtime)
                + '" ';


        html +=
                'class="calendar-event'
                + inactiveClass
                + '" ';


        html +=
                'style="top:'
                + top
                + 'px; height:'
                + height
                + 'px;">';


        html +=
                '<div class="calendar-event-time">';


        html +=
                showtime.startTime
                + " - "
                + showtime.endTime;


        html +=
                '</div>';


        html +=
                '<div class="calendar-event-title">';


        html +=
                escapeHtml(
                        showtime.movie
                );


        html +=
                '</div>';


        html +=
                '<div class="calendar-event-room">';


        html +=
                escapeHtml(
                        showtime.room
                );


        html +=
                '</div>';


        html +=
                '</a>';


        return html;
    }


    function renderDay() {

        calendarTitle.textContent =
                weekdayName(currentDate)
                + ", "
                + formatDate(currentDate);


        var events =
                eventsForDate(
                        currentDate
                );


        var isToday =
                dateKey(currentDate)
                === dateKey(
                        new Date()
                );


        var eventsHtml = "";


        events.forEach(
                function (showtime) {

                    eventsHtml +=
                            createCalendarEvent(
                                    showtime
                            );
                }
        );


        var html = "";


        html +=
                '<div class="time-calendar">';


        html +=
                '<div class="time-header day-header">';


        html +=
                '<div class="time-header-spacer"></div>';


        html +=
                '<div class="time-header-day'
                + (isToday ? " today" : "")
                + '">';


        html +=
                '<div class="header-weekday">'
                + weekdayName(currentDate)
                + '</div>';


        html +=
                '<div class="header-date">'
                + currentDate.getDate()
                + '</div>';


        html +=
                '</div>';

        html +=
                '</div>';


        html +=
                '<div class="time-body day-body">';


        html +=
                '<div class="time-label-column">';

        html +=
                createTimeLabels();

        html +=
                '</div>';


        html +=
                '<div class="calendar-day-column'
                + (isToday
                        ? " today-column"
                        : "")
                + '">';


        html +=
                eventsHtml;


        html +=
                '</div>';

        html +=
                '</div>';

        html +=
                '</div>';


        calendarContent.innerHTML =
                html;


        scrollToUsefulTime(
                events
        );
    }


    function getMonday(date) {

        var result =
                new Date(date);


        var day =
                result.getDay();


        var difference =
                day === 0
                        ? -6
                        : 1 - day;


        result.setDate(
                result.getDate()
                + difference
        );


        result.setHours(
                0,
                0,
                0,
                0
        );


        return result;
    }


    function renderWeek() {

        var monday =
                getMonday(
                        currentDate
                );


        var sunday =
                new Date(
                        monday
                );


        sunday.setDate(
                monday.getDate()
                + 6
        );


        calendarTitle.textContent =
                formatDate(monday)
                + " - "
                + formatDate(sunday);


        var headerHtml =
                '<div class="time-header-spacer"></div>';


        var bodyHtml =
                '<div class="time-label-column">'
                + createTimeLabels()
                + '</div>';


        var todayKey =
                dateKey(
                        new Date()
                );


        var weekEvents = [];


        for (
                var i = 0;
                i < 7;
                i++
        ) {

            var day =
                    new Date(
                            monday
                    );


            day.setDate(
                    monday.getDate()
                    + i
            );


            var key =
                    dateKey(day);


            var events =
                    eventsForDate(
                            day
                    );


            weekEvents =
                    weekEvents.concat(
                            events
                    );


            var isToday =
                    key === todayKey;


            headerHtml +=
                    '<div class="time-header-day'
                    + (isToday
                            ? " today"
                            : "")
                    + '">';


            headerHtml +=
                    '<div class="header-weekday">'
                    + weekdayName(day)
                    + '</div>';


            headerHtml +=
                    '<div class="header-date">'
                    + day.getDate()
                    + '</div>';


            headerHtml +=
                    '</div>';


            var eventsHtml = "";


            events.forEach(
                    function (showtime) {

                        eventsHtml +=
                                createCalendarEvent(
                                        showtime
                                );
                    }
            );


            bodyHtml +=
                    '<div class="calendar-day-column'
                    + (isToday
                            ? " today-column"
                            : "")
                    + '">';


            bodyHtml +=
                    eventsHtml;


            bodyHtml +=
                    '</div>';
        }


        var html = "";


        html +=
                '<div class="time-calendar">';


        html +=
                '<div class="time-header week-header">';


        html +=
                headerHtml;


        html +=
                '</div>';


        html +=
                '<div class="time-body week-body">';


        html +=
                bodyHtml;


        html +=
                '</div>';


        html +=
                '</div>';


        calendarContent.innerHTML =
                html;


        scrollToUsefulTime(
                weekEvents
        );
    }


    function renderMonth() {

        calendarTitle.textContent =
                "Tháng "
                + (currentDate.getMonth() + 1)
                + "/"
                + currentDate.getFullYear();


        var firstDay =
                new Date(
                        currentDate.getFullYear(),
                        currentDate.getMonth(),
                        1
                );


        var offset =
                firstDay.getDay()
                - 1;


        if (offset < 0) {
            offset = 6;
        }


        var calendarStart =
                new Date(
                        firstDay
                );


        calendarStart.setDate(
                firstDay.getDate()
                - offset
        );


        var weekdayNames = [
            "Thứ 2",
            "Thứ 3",
            "Thứ 4",
            "Thứ 5",
            "Thứ 6",
            "Thứ 7",
            "Chủ nhật"
        ];


        var html =
                '<div class="month-week-header">';


        weekdayNames.forEach(
                function (name) {

                    html +=
                            '<div>'
                            + name
                            + '</div>';
                }
        );


        html +=
                '</div>';


        html +=
                '<div class="month-grid">';


        var todayKey =
                dateKey(
                        new Date()
                );


        for (
                var i = 0;
                i < 42;
                i++
        ) {

            var day =
                    new Date(
                            calendarStart
                    );


            day.setDate(
                    calendarStart.getDate()
                    + i
            );


            var key =
                    dateKey(
                            day
                    );


            var events =
                    eventsForDate(
                            day
                    );


            var otherMonth =
                    day.getMonth()
                    !== currentDate.getMonth();


            var isToday =
                    key === todayKey;


            var classes =
                    "month-day";


            if (otherMonth) {
                classes +=
                        " other-month";
            }


            if (isToday) {
                classes +=
                        " today";
            }


            html +=
                    '<div class="'
                    + classes
                    + '" data-date="'
                    + key
                    + '">';


            html +=
                    '<div class="month-day-number">';


            html +=
                    day.getDate();


            html +=
                    '</div>';


            var visibleEvents =
                    events.slice(
                            0,
                            3
                    );


            visibleEvents.forEach(
                    function (showtime) {

                        var inactiveClass =
                                showtime.status
                                === "ACTIVE"
                                        ? ""
                                        : " inactive";


                        html +=
                                '<a href="'
                                + editUrl(showtime)
                                + '" ';


                        html +=
                                'class="month-event'
                                + inactiveClass
                                + '" ';


                        html +=
                                'onclick="event.stopPropagation();">';


                        html +=
                                showtime.startTime
                                + " "
                                + escapeHtml(
                                        showtime.movie
                                );


                        html +=
                                '</a>';
                    }
            );


            if (events.length > 3) {

                html +=
                        '<div class="more-events">';


                html +=
                        "+"
                        + (events.length - 3)
                        + " suất khác";


                html +=
                        '</div>';
            }


            html +=
                    '</div>';
        }


        html +=
                '</div>';


        calendarContent.innerHTML =
                html;


        var monthDays =
                document.querySelectorAll(
                        ".month-day"
                );


        monthDays.forEach(
                function (element) {

                    element.addEventListener(
                            "click",
                            function () {

                                currentDate =
                                        dateFromKey(
                                                element.dataset.date
                                        );


                                changeView(
                                        "day"
                                );
                            }
                    );
                }
        );
    }


    function scrollToUsefulTime(
            events) {

        setTimeout(
                function () {

                    var scroller =
                            document.querySelector(
                                    ".time-calendar"
                            );


                    if (!scroller) {
                        return;
                    }


                    var hour = 8;


                    if (events.length > 0) {

                        var earliest =
                                24 * 60;


                        events.forEach(
                                function (showtime) {

                                    var value =
                                            minutesFromTime(
                                                    showtime.startTime
                                            );


                                    if (value < earliest) {

                                        earliest =
                                                value;
                                    }
                                }
                        );


                        hour =
                                Math.max(
                                        0,
                                        Math.floor(
                                                earliest / 60
                                        ) - 1
                                );
                    }


                    scroller.scrollTop =
                            hour
                            * HOUR_HEIGHT;

                },
                0
        );
    }


    function renderCurrentView() {

        if (currentView === "day") {

            renderDay();

            return;
        }


        if (currentView === "week") {

            renderWeek();

            return;
        }


        if (currentView === "month") {

            renderMonth();
        }
    }


    function changeView(view) {

        currentView =
                view;


        viewButtons.forEach(
                function (button) {

                    if (
                            button.dataset.view
                            === view
                    ) {

                        button.classList.add(
                                "active"
                        );

                    } else {

                        button.classList.remove(
                                "active"
                        );
                    }
                }
        );


        if (view === "list") {

            listView.style.display =
                    "block";


            calendarView.style.display =
                    "none";


            calendarNavigation
                    .classList
                    .remove(
                            "visible"
                    );


            return;
        }


        listView.style.display =
                "none";


        calendarView.style.display =
                "block";


        calendarNavigation
                .classList
                .add(
                        "visible"
                );


        renderCurrentView();
    }


    viewButtons.forEach(
            function (button) {

                button.addEventListener(
                        "click",
                        function () {

                            changeView(
                                    button.dataset.view
                            );
                        }
                );
            }
    );


    document.getElementById(
            "previousButton"
    ).addEventListener(
            "click",
            function () {

                if (
                        currentView
                        === "day"
                ) {

                    currentDate.setDate(
                            currentDate.getDate()
                            - 1
                    );
                }


                if (
                        currentView
                        === "week"
                ) {

                    currentDate.setDate(
                            currentDate.getDate()
                            - 7
                    );
                }


                if (
                        currentView
                        === "month"
                ) {

                    currentDate =
                            new Date(
                                    currentDate.getFullYear(),
                                    currentDate.getMonth() - 1,
                                    1
                            );
                }


                renderCurrentView();
            }
    );


    document.getElementById(
            "nextButton"
    ).addEventListener(
            "click",
            function () {

                if (
                        currentView
                        === "day"
                ) {

                    currentDate.setDate(
                            currentDate.getDate()
                            + 1
                    );
                }


                if (
                        currentView
                        === "week"
                ) {

                    currentDate.setDate(
                            currentDate.getDate()
                            + 7
                    );
                }


                if (
                        currentView
                        === "month"
                ) {

                    currentDate =
                            new Date(
                                    currentDate.getFullYear(),
                                    currentDate.getMonth() + 1,
                                    1
                            );
                }


                renderCurrentView();
            }
    );


    document.getElementById(
            "todayButton"
    ).addEventListener(
            "click",
            function () {

                currentDate =
                        new Date();


                currentDate.setHours(
                        0,
                        0,
                        0,
                        0
                );


                renderCurrentView();
            }
    );

</script>


<%@ include file="/WEB-INF/include/footer.jsp" %>