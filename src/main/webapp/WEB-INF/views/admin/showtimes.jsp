<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ taglib prefix="fn"
           uri="jakarta.tags.functions" %>

<%@ include file="/WEB-INF/include/header.jsp" %>




<section class="showtime-page" data-css-page="admin-showtimes">

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


    <c:if test="${param.bulkSaved eq '1'}">

        <div class="message-success">
            Tạo loạt lịch chiếu thành công.
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


        <div class="calendar-navigation-right">

            <label for="calendarMovieFilter">
                Phim:
            </label>

            <select id="calendarMovieFilter"
                    class="movie-filter">

                <option value="">
                    Tất cả phim
                </option>

            </select>

        </div>

    </div>


    <!-- LIST -->

    <div id="listView">

        <div class="showtime-table-wrap">

            <c:if test="${empty showtimes}">

                <div class="empty-showtime">
                    Chưa có lịch chiếu nào.
                </div>

            </c:if>


            <c:if test="${not empty showtimes}">

                <table class="showtime-table"
                       data-paginate="true"
                       data-page-size="10">

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


    <!-- CALENDAR -->

    <div id="calendarView">

        <div id="calendarContent"
             class="calendar-box">
        </div>

    </div>


    <!-- DATA -->

    <div class="showtime-source-data">

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
                 data-movie-id="${showtime.movieId}"
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

                            movieId:
                                    item.dataset.movieId,

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

    var movieFilter =
            document.getElementById(
                    "calendarMovieFilter"
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

        return pad(date.getDate())
                + "/"
                + pad(date.getMonth() + 1)
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

        var div =
                document.createElement(
                        "div"
                );

        div.textContent =
                value || "";

        return div.innerHTML;
    }


    /*
     * Tự tạo dropdown phim từ chính
     * dữ liệu lịch chiếu hiện có.
     */
    function buildMovieFilter() {

        var movies = {};

        showtimes.forEach(
                function (showtime) {

                    movies[
                            showtime.movieId
                    ] =
                            showtime.movie;
                }
        );

        var entries =
                Object.keys(movies)
                        .map(
                                function (id) {

                                    return {
                                        id: id,
                                        title: movies[id]
                                    };
                                }
                        )
                        .sort(
                                function (a, b) {

                                    return a.title
                                            .localeCompare(
                                                    b.title
                                            );
                                }
                        );

        entries.forEach(
                function (movie) {

                    var option =
                            document.createElement(
                                    "option"
                            );

                    option.value =
                            movie.id;

                    option.textContent =
                            movie.title;

                    movieFilter.appendChild(
                            option
                    );
                }
        );
    }


    function filteredShowtimes() {

        var selectedMovieId =
                movieFilter.value;

        if (!selectedMovieId) {

            return showtimes;
        }

        return showtimes.filter(
                function (showtime) {

                    return showtime.movieId
                            === selectedMovieId;
                }
        );
    }


    function eventsForDate(date) {

        var key =
                dateKey(
                        date
                );

        return filteredShowtimes()
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

        return minutesFromTime(
                showtime.startTime
        ) / 60 * HOUR_HEIGHT;
    }


    function eventHeight(showtime) {

        var start =
                minutesFromTime(
                        showtime.startTime
                );

        var end;

        if (showtime.startDate
                === showtime.endDate) {

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
                    + 'px">';

            html +=
                    pad(hour)
                    + ":00";

            html +=
                    '</div>';
        }

        return html;
    }


    function createEvent(showtime) {

        var inactive =
                showtime.status === "ACTIVE"
                        ? ""
                        : " inactive";

        return '<a href="'
                + editUrl(showtime)
                + '" class="calendar-event'
                + inactive
                + '" style="top:'
                + eventTop(showtime)
                + 'px;height:'
                + eventHeight(showtime)
                + 'px;">'
                + '<div class="calendar-event-time">'
                + showtime.startTime
                + ' - '
                + showtime.endTime
                + '</div>'
                + '<div class="calendar-event-title">'
                + escapeHtml(showtime.movie)
                + '</div>'
                + '<div class="calendar-event-room">'
                + escapeHtml(showtime.room)
                + '</div>'
                + '</a>';
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
                            createEvent(
                                    showtime
                            );
                }
        );

        calendarContent.innerHTML =
                '<div class="time-calendar">'
                + '<div class="time-header day-header">'
                + '<div class="time-header-spacer"></div>'
                + '<div class="time-header-day'
                + (isToday ? ' today' : '')
                + '">'
                + '<div class="header-weekday">'
                + weekdayName(currentDate)
                + '</div>'
                + '<div class="header-date">'
                + currentDate.getDate()
                + '</div>'
                + '</div>'
                + '</div>'
                + '<div class="time-body day-body">'
                + '<div class="time-label-column">'
                + createTimeLabels()
                + '</div>'
                + '<div class="calendar-day-column'
                + (isToday ? ' today-column' : '')
                + '">'
                + eventsHtml
                + '</div>'
                + '</div>'
                + '</div>';

        scrollToUsefulTime(
                events
        );
    }


    function getMonday(date) {

        var result =
                new Date(
                        date
                );

        var day =
                result.getDay();

        result.setDate(
                result.getDate()
                + (
                        day === 0
                                ? -6
                                : 1 - day
                )
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
                monday.getDate() + 6
        );

        calendarTitle.textContent =
                formatDate(monday)
                + " - "
                + formatDate(sunday);

        var header =
                '<div class="time-header-spacer"></div>';

        var body =
                '<div class="time-label-column">'
                + createTimeLabels()
                + '</div>';

        var weekEvents = [];

        var todayKey =
                dateKey(
                        new Date()
                );

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

            var events =
                    eventsForDate(
                            day
                    );

            weekEvents =
                    weekEvents.concat(
                            events
                    );

            var today =
                    dateKey(day)
                    === todayKey;

            header +=
                    '<div class="time-header-day'
                    + (today ? ' today' : '')
                    + '">'
                    + '<div class="header-weekday">'
                    + weekdayName(day)
                    + '</div>'
                    + '<div class="header-date">'
                    + day.getDate()
                    + '</div>'
                    + '</div>';

            var eventsHtml = "";

            events.forEach(
                    function (showtime) {

                        eventsHtml +=
                                createEvent(
                                        showtime
                                );
                    }
            );

            body +=
                    '<div class="calendar-day-column'
                    + (today ? ' today-column' : '')
                    + '">'
                    + eventsHtml
                    + '</div>';
        }

        calendarContent.innerHTML =
                '<div class="time-calendar">'
                + '<div class="time-header week-header">'
                + header
                + '</div>'
                + '<div class="time-body week-body">'
                + body
                + '</div>'
                + '</div>';

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
                firstDay.getDay() - 1;

        if (offset < 0) {
            offset = 6;
        }

        var start =
                new Date(
                        firstDay
                );

        start.setDate(
                firstDay.getDate()
                - offset
        );

        var names = [
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

        names.forEach(
                function (name) {

                    html +=
                            '<div>'
                            + name
                            + '</div>';
                }
        );

        html +=
                '</div>'
                + '<div class="month-grid">';

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
                            start
                    );

            day.setDate(
                    start.getDate()
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

            var classes =
                    "month-day";

            if (day.getMonth()
                    !== currentDate.getMonth()) {

                classes +=
                        " other-month";
            }

            if (key === todayKey) {

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
                    '<div class="month-day-number">'
                    + day.getDate()
                    + '</div>';

            events.slice(
                    0,
                    3
            ).forEach(
                    function (showtime) {

                        html +=
                                '<a class="month-event'
                                + (
                                        showtime.status
                                        === "ACTIVE"
                                                ? ""
                                                : " inactive"
                                )
                                + '" href="'
                                + editUrl(showtime)
                                + '" onclick="event.stopPropagation();">'
                                + showtime.startTime
                                + " "
                                + escapeHtml(showtime.movie)
                                + '</a>';
                    }
            );

            if (events.length > 3) {

                html +=
                        '<div class="more-events">'
                        + "+"
                        + (events.length - 3)
                        + " suất khác"
                        + '</div>';
            }

            html +=
                    '</div>';
        }

        html +=
                '</div>';

        calendarContent.innerHTML =
                html;

        document.querySelectorAll(
                ".month-day"
        ).forEach(
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

                                    earliest =
                                            Math.min(
                                                    earliest,
                                                    minutesFromTime(
                                                            showtime.startTime
                                                    )
                                            );
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

        } else if (
                currentView === "week"
        ) {

            renderWeek();

        } else if (
                currentView === "month"
        ) {

            renderMonth();
        }
    }


    function changeView(view) {

        currentView =
                view;

        viewButtons.forEach(
                function (button) {

                    button.classList.toggle(
                            "active",
                            button.dataset.view
                            === view
                    );
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


    movieFilter.addEventListener(
            "change",
            function () {

                renderCurrentView();
            }
    );


    document.getElementById(
            "previousButton"
    ).addEventListener(
            "click",
            function () {

                if (currentView === "day") {

                    currentDate.setDate(
                            currentDate.getDate() - 1
                    );

                } else if (
                    currentView === "week"
                ) {

                    currentDate.setDate(
                            currentDate.getDate() - 7
                    );

                } else if (
                    currentView === "month"
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

                if (currentView === "day") {

                    currentDate.setDate(
                            currentDate.getDate() + 1
                    );

                } else if (
                    currentView === "week"
                ) {

                    currentDate.setDate(
                            currentDate.getDate() + 7
                    );

                } else if (
                    currentView === "month"
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


    buildMovieFilter();

</script>


<%@ include file="/WEB-INF/include/table-pagination.jsp" %>

<%@ include file="/WEB-INF/include/footer.jsp" %>