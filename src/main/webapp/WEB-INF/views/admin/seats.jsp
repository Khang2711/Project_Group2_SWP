<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fn"
           uri="jakarta.tags.functions" %>

<%@ include file="/WEB-INF/include/header.jsp" %>

<style>

    .seat-admin-page {
        max-width: 1250px;
        margin: 0 auto;
        padding: 42px 24px 70px;
        color: #f5f5f5;
    }

    /* =========================================================
       HEADER
       ========================================================= */

    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 20px;
        margin-bottom: 22px;
    }

    .page-header h1 {
        margin: 0 0 7px;
        font-size: 32px;
    }

    .page-header p {
        margin: 0;
        color: #999;
    }

    .header-actions {
        display: flex;
        align-items: center;
        gap: 10px;
        flex-wrap: wrap;
    }

    .btn-primary {
        display: inline-block;
        padding: 10px 16px;
        border: none;
        border-radius: 7px;
        background: #ff1744;
        color: white;
        text-decoration: none;
        font-weight: 600;
        cursor: pointer;
    }

    .btn-primary:hover {
        background: #e3133b;
    }

    .back-button {
        color: #ccc;
        border: 1px solid #444;
        padding: 10px 15px;
        border-radius: 7px;
        text-decoration: none;
    }

    .back-button:hover {
        color: white;
        border-color: #ff1744;
    }

    /* =========================================================
       ROOM INFO
       ========================================================= */

    .room-info {
        display: flex;
        gap: 25px;
        flex-wrap: wrap;
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 10px;
        padding: 17px 20px;
        margin-bottom: 20px;
    }

    .room-info-item {
        color: #999;
    }

    .room-info-item strong {
        margin-left: 5px;
        color: white;
    }

    /* =========================================================
       MESSAGE
       ========================================================= */

    .success-message {
        padding: 13px 16px;
        margin-bottom: 20px;
        border: 1px solid #225c3c;
        background: #13291e;
        color: #70d99a;
        border-radius: 7px;
    }

    .error-message {
        padding: 13px 16px;
        margin-bottom: 20px;
        border: 1px solid #6b2935;
        background: #32181d;
        color: #ff8095;
        border-radius: 7px;
    }

    /* =========================================================
       ADD ROW PANEL
       ========================================================= */

    .add-row-panel {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 11px;
        padding: 20px 22px;
        margin-bottom: 22px;
    }

    .add-row-panel h3 {
        margin: 0 0 6px;
        color: white;
    }

    .add-row-description {
        margin: 0 0 17px;
        color: #888;
        font-size: 13px;
    }

    .add-row-controls {
        display: grid;
        grid-template-columns: 1fr 1fr auto;
        align-items: end;
        gap: 14px;
    }

    /* =========================================================
       FORM
       ========================================================= */

    .form-group label {
        display: block;
        margin-bottom: 7px;
        color: #bbb;
        font-size: 13px;
        font-weight: 600;
    }

    .form-control {
        width: 100%;
        box-sizing: border-box;
        padding: 11px 12px;
        background: #1c1c20;
        color: white;
        border: 1px solid #38383e;
        border-radius: 7px;
        outline: none;
    }

    .form-control:focus {
        border-color: #ff1744;
    }

    /* =========================================================
       SEAT MAP
       ========================================================= */

    .seat-map-card {
        background: #111113;
        border: 1px solid #29292d;
        border-radius: 13px;
        padding: 32px 28px;
        overflow-x: auto;
    }

    /* =========================================================
       SCREEN
       ========================================================= */

    .screen-area {
        width: 72%;
        min-width: 500px;
        margin: 0 auto 45px;
        text-align: center;
    }

    .screen {
        height: 8px;

        background: linear-gradient(
            90deg,
            transparent,
            #ddd,
            transparent
        );

        border-radius: 50%;

        box-shadow:
            0 5px 18px
            rgba(255, 255, 255, 0.2);
    }

    .screen-text {
        margin-top: 10px;
        color: #777;
        font-size: 12px;
        letter-spacing: 4px;
    }

    /* =========================================================
       SEAT ROW
       ========================================================= */

    .seat-rows {
        min-width: 820px;
    }

    .seat-row {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 16px;
        margin-bottom: 17px;
    }

    .row-control {
        width: 105px;
        flex-shrink: 0;

        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;
    }

    .row-name {
        text-align: center;
        color: #999;
        font-size: 13px;
        font-weight: 700;
    }

    .row-select-button {
        width: 95px;

        padding: 5px 7px;

        border: 1px solid #444;
        border-radius: 5px;

        background: #1c1c20;
        color: #bbb;

        cursor: pointer;

        font-size: 10px;
    }

    .row-select-button:hover {
        border-color: #ff1744;
        color: white;
    }

    .row-select-button.row-selected {
        background: #ff1744;
        border-color: #ff1744;
        color: white;
    }

    .row-seats {
        display: flex;
        justify-content: center;
        gap: 9px;
        flex-wrap: nowrap;
    }

    /* =========================================================
       SEAT
       ========================================================= */

    .seat-card {
        position: relative;

        width: 62px;
        height: 58px;

        border-radius: 9px 9px 6px 6px;

        background: #28282d;
        border: 2px solid #3f3f45;

        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        cursor: pointer;

        transition: 0.18s;

        user-select: none;

        box-sizing: border-box;
    }

    .seat-card:hover {
        transform: translateY(-3px);
        border-color: #999;
    }

    .seat-checkbox {
        position: absolute;
        opacity: 0;
        pointer-events: none;
    }

    .seat-name {
        color: white;
        font-size: 13px;
        font-weight: 700;
    }

    .seat-type-text {
        margin-top: 3px;
        color: #aaa;
        font-size: 9px;
    }

    /* STANDARD */

    .seat-standard {
        background: #29292e;
        border-color: #45454c;
    }

    /* VIP */

    .seat-vip {
        background: #3a3016;
        border-color: #8c7427;
    }

    .seat-vip .seat-type-text {
        color: #e6c65a;
    }

    /* COUPLE */

    .seat-couple {
        background: #342238;
        border-color: #84528c;
    }

    .seat-couple .seat-type-text {
        color: #d792e0;
    }

    /* INACTIVE */

    .seat-inactive {
        background: #18181a;
        border-color: #323236;
        opacity: 0.5;
    }

    .seat-inactive::after {
        content: "×";

        position: absolute;
        right: 4px;
        top: 1px;

        color: #ff6b81;
        font-weight: 700;
    }

    /* SELECTED */

    .seat-card.selected {
        background: #ff1744;
        border-color: #ff6b81;

        opacity: 1;

        transform: translateY(-4px);

        box-shadow:
            0 5px 18px
            rgba(255, 23, 68, 0.25);
    }

    .seat-card.selected .seat-name,
    .seat-card.selected .seat-type-text {
        color: white;
    }

    /* =========================================================
       LEGEND
       ========================================================= */

    .legend {
        display: flex;
        justify-content: center;
        flex-wrap: wrap;
        gap: 20px;

        margin-top: 35px;
        padding-top: 22px;

        border-top: 1px solid #29292d;

        color: #aaa;
        font-size: 13px;
    }

    .legend-item {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .legend-box {
        width: 21px;
        height: 16px;

        border-radius: 4px;
        border: 2px solid #444;
    }

    .legend-standard {
        background: #29292e;
    }

    .legend-vip {
        background: #3a3016;
        border-color: #8c7427;
    }

    .legend-couple {
        background: #342238;
        border-color: #84528c;
    }

    .legend-inactive {
        background: #18181a;
        opacity: 0.5;
    }

    .legend-selected {
        background: #ff1744;
        border-color: #ff6b81;
    }

    /* =========================================================
       SELECTION TOOLBAR
       ========================================================= */

    .selection-toolbar {
        margin-top: 22px;

        display: flex;
        justify-content: space-between;
        align-items: center;

        gap: 15px;

        flex-wrap: wrap;
    }

    .selection-info {
        color: #aaa;
    }

    .selection-info strong {
        color: white;
    }

    .toolbar-buttons {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
    }

    .small-button {
        border: 1px solid #444;

        background: #1c1c20;
        color: #ddd;

        padding: 8px 13px;

        border-radius: 6px;

        cursor: pointer;
    }

    .small-button:hover {
        border-color: #ff1744;
    }

    .small-button:disabled {
        opacity: 0.4;
        cursor: not-allowed;
    }

    /* =========================================================
       BULK PANEL
       ========================================================= */

    .bulk-panel {
        margin-top: 22px;

        background: #151517;
        border: 1px solid #29292d;
        border-radius: 11px;

        padding: 22px;
    }

    .bulk-panel h3 {
        margin: 0 0 7px;
    }

    .bulk-description {
        color: #888;
        font-size: 13px;
        margin-bottom: 18px;
        line-height: 1.5;
    }

    .bulk-controls {
        display: grid;

        grid-template-columns:
            1fr
            1fr
            auto
            auto;

        gap: 14px;

        align-items: end;
    }

    .update-button {
        border: none;

        padding: 11px 19px;

        border-radius: 7px;

        background: #ff1744;
        color: white;

        cursor: pointer;
        font-weight: 600;

        white-space: nowrap;
    }

    .update-button:hover {
        background: #e3133b;
    }

    .delete-button {
        padding: 10px 17px;

        background: transparent;
        color: #ff6b81;

        border: 1px solid #73313d;
        border-radius: 7px;

        cursor: pointer;
        font-weight: 600;

        white-space: nowrap;
    }

    .delete-button:hover {
        background: #c71837;
        color: white;
        border-color: #c71837;
    }

    .empty {
        text-align: center;
        color: #888;
        padding: 50px;
    }

    /* =========================================================
       RESPONSIVE
       ========================================================= */

    @media (max-width: 950px) {

        .bulk-controls {
            grid-template-columns: 1fr 1fr;
        }

        .add-row-controls {
            grid-template-columns: 1fr 1fr;
        }
    }

    @media (max-width: 700px) {

        .page-header {
            align-items: flex-start;
            flex-direction: column;
        }

        .header-actions {
            width: 100%;
        }

        .bulk-controls {
            grid-template-columns: 1fr;
        }

        .add-row-controls {
            grid-template-columns: 1fr;
        }

        .screen-area {
            width: 600px;
        }
    }

</style>

<div class="seat-admin-page">

    <!-- =====================================================
         HEADER
         ===================================================== -->

    <div class="page-header">

        <div>

            <h1>
                Quản lý sơ đồ ghế
            </h1>

            <p>
                Thêm, xóa và cập nhật nhiều ghế
                trong phòng chiếu.
            </p>

        </div>

        <div class="header-actions">

            <a class="btn-primary"
               href="${pageContext.request.contextPath}/admin/seat-form?roomId=${room.roomId}">

                + Thêm ghế

            </a>

            <a class="back-button"
               href="${pageContext.request.contextPath}/admin/rooms">

                ← Quay lại phòng

            </a>

        </div>

    </div>

    <!-- =====================================================
         ROOM INFO
         ===================================================== -->

    <div class="room-info">

        <div class="room-info-item">

            Phòng:

            <strong>
                <c:out value="${room.roomName}" />
            </strong>

        </div>

        <div class="room-info-item">

            Loại:

            <strong>
                <c:out value="${room.roomType}" />
            </strong>

        </div>

        <div class="room-info-item">

            Sức chứa hiện tại:

            <strong>
                ${room.capacity} ghế
            </strong>

        </div>

        <div class="room-info-item">

            Trạng thái:

            <strong>
                <c:out value="${room.status}" />
            </strong>

        </div>

    </div>

    <!-- =====================================================
         MESSAGE
         ===================================================== -->

    <c:if test="${param.updated == '1'}">

        <div class="success-message">

            ✓ Cập nhật ghế thành công.

        </div>

    </c:if>

    <c:if test="${param.rowAdded == '1'}">

        <div class="success-message">

            ✓ Thêm hàng ghế mới thành công.

        </div>

    </c:if>

    <c:if test="${param.deleted == '1'}">

        <div class="success-message">

            ✓ Cập nhật số lượng ghế thành công.
            Ghế đã có lịch sử vé sẽ được giữ lại
            và chuyển sang INACTIVE.

        </div>

    </c:if>

    <c:if test="${not empty error}">

        <div class="error-message">

            ⚠
            <c:out value="${error}" />

        </div>

    </c:if>

    <!-- =====================================================
         THÊM NGUYÊN MỘT HÀNG
         ===================================================== -->

    <div class="add-row-panel">

        <h3>
            Thêm hàng ghế mới
        </h3>

        <p class="add-row-description">

            Hệ thống sẽ tự tạo hàng tiếp theo.
            Ví dụ đang có A–E thì hàng mới sẽ là F.

        </p>

        <form method="post"
              action="${pageContext.request.contextPath}/admin/seats">

            <input type="hidden"
                   name="action"
                   value="addRow">

            <input type="hidden"
                   name="roomId"
                   value="${room.roomId}">

            <div class="add-row-controls">

                <div class="form-group">

                    <label>
                        Số ghế của hàng mới
                    </label>

                    <input class="form-control"
                           type="number"
                           name="seatCount"
                           min="1"
                           max="50"
                           value="8"
                           required>

                </div>

                <div class="form-group">

                    <label>
                        Loại ghế mặc định
                    </label>

                    <select class="form-control"
                            name="rowSeatType">

                        <option value="STANDARD">
                            STANDARD
                        </option>

                        <option value="VIP">
                            VIP
                        </option>

                        <option value="COUPLE">
                            COUPLE
                        </option>

                    </select>

                </div>

                <button type="submit"
                        class="btn-primary">

                    + Thêm hàng

                </button>

            </div>

        </form>

    </div>

    <!-- =====================================================
         FORM CHỌN NHIỀU GHẾ
         ===================================================== -->

    <form id="seatBulkForm"
          method="post"
          action="${pageContext.request.contextPath}/admin/seats">

        <input type="hidden"
               name="roomId"
               value="${room.roomId}">

        <div class="seat-map-card">

            <!-- SCREEN -->

            <div class="screen-area">

                <div class="screen"></div>

                <div class="screen-text">

                    MÀN HÌNH

                </div>

            </div>

            <c:choose>

                <c:when test="${not empty seatRows}">

                    <div class="seat-rows">

                        <c:forEach var="rowEntry"
                                   items="${seatRows}">

                            <c:choose>

                                <c:when test="${rowEntry.key <= 26}">

                                    <c:set var="rowLabel"
                                           value="${fn:substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ', rowEntry.key - 1, rowEntry.key)}" />

                                </c:when>

                                <c:otherwise>

                                    <c:set var="rowLabel"
                                           value="R${rowEntry.key}" />

                                </c:otherwise>

                            </c:choose>

                            <div class="seat-row">

                                <!-- =================================
                                     TÊN HÀNG + CHỌN NGUYÊN HÀNG
                                     ================================= -->

                                <div class="row-control">

                                    <div class="row-name">

                                        HÀNG ${rowLabel}

                                    </div>

                                    <button type="button"
                                            class="row-select-button"
                                            data-row-label="${rowLabel}">

                                        Chọn hàng ${rowLabel}

                                    </button>

                                </div>

                                <!-- =================================
                                     GHẾ
                                     ================================= -->

                                <div class="row-seats">

                                    <c:forEach var="seat"
                                               items="${rowEntry.value}">

                                        <label class="
                                               seat-card

                                               ${seat.seatType == 'VIP'
                                                 ? 'seat-vip'
                                                 : seat.seatType == 'COUPLE'
                                                 ? 'seat-couple'
                                                 : 'seat-standard'}

                                               ${seat.status != 'ACTIVE'
                                                 ? 'seat-inactive'
                                                 : ''}
                                               ">

                                            <input class="seat-checkbox"
                                                   type="checkbox"
                                                   name="seatId"
                                                   value="${seat.seatId}">

                                            <span class="seat-name">

                                                ${rowLabel}${seat.seatNumber}

                                            </span>

                                            <span class="seat-type-text">

                                                <c:out value="${seat.seatType}" />

                                            </span>

                                        </label>

                                    </c:forEach>

                                </div>

                            </div>

                        </c:forEach>

                    </div>

                </c:when>

                <c:otherwise>

                    <div class="empty">

                        Phòng này chưa có ghế.

                    </div>

                </c:otherwise>

            </c:choose>

            <!-- =================================================
                 LEGEND
                 ================================================= -->

            <div class="legend">

                <div class="legend-item">

                    <span class="
                          legend-box
                          legend-standard">
                    </span>

                    STANDARD

                </div>

                <div class="legend-item">

                    <span class="
                          legend-box
                          legend-vip">
                    </span>

                    VIP

                </div>

                <div class="legend-item">

                    <span class="
                          legend-box
                          legend-couple">
                    </span>

                    COUPLE

                </div>

                <div class="legend-item">

                    <span class="
                          legend-box
                          legend-inactive">
                    </span>

                    INACTIVE

                </div>

                <div class="legend-item">

                    <span class="
                          legend-box
                          legend-selected">
                    </span>

                    Đang chọn

                </div>

            </div>

        </div>

        <!-- =================================================
             TOOLBAR
             ================================================= -->

        <div class="selection-toolbar">

            <div class="selection-info">

                Đã chọn:

                <strong id="selectedCount">
                    0
                </strong>

                ghế

            </div>

            <div class="toolbar-buttons">

                <button type="button"
                        class="small-button"
                        id="selectAllButton">

                    Chọn tất cả

                </button>

                <button type="button"
                        class="small-button"
                        id="clearButton">

                    Bỏ chọn tất cả

                </button>

                <button type="button"
                        class="small-button"
                        id="editOneButton"
                        disabled>

                    Sửa chi tiết 1 ghế

                </button>

            </div>

        </div>

        <!-- =================================================
             BULK EDIT
             ================================================= -->

        <div class="bulk-panel">

            <h3>
                Xử lý các ghế đã chọn
            </h3>

            <div class="bulk-description">

                Bạn có thể chọn từng ghế,
                chọn nguyên một hàng hoặc chọn tất cả ghế.

                <br>

                Sau đó có thể đổi loại ghế,
                đổi trạng thái hoặc xóa nhiều ghế cùng lúc.

                <br>

                Ghế đã từng có Ticket sẽ không bị xóa khỏi database;
                khi bấm Xóa, hệ thống sẽ chuyển ghế đó sang INACTIVE
                để giữ lịch sử vé.

            </div>

            <div class="bulk-controls">

                <div class="form-group">

                    <label>
                        Loại ghế
                    </label>

                    <select id="bulkSeatType"
                            class="form-control"
                            name="seatType">

                        <option value="">
                            -- Giữ nguyên --
                        </option>

                        <option value="STANDARD">
                            STANDARD
                        </option>

                        <option value="VIP">
                            VIP
                        </option>

                        <option value="COUPLE">
                            COUPLE
                        </option>

                    </select>

                </div>

                <div class="form-group">

                    <label>
                        Trạng thái
                    </label>

                    <select id="bulkStatus"
                            class="form-control"
                            name="status">

                        <option value="">
                            -- Giữ nguyên --
                        </option>

                        <option value="ACTIVE">
                            ACTIVE
                        </option>

                        <option value="INACTIVE">
                            INACTIVE
                        </option>

                    </select>

                </div>

                <button type="submit"
                        name="action"
                        value="bulkUpdate"
                        class="update-button">

                    Cập nhật ghế

                </button>

                <button type="submit"
                        name="action"
                        value="deleteSelected"
                        class="delete-button"
                        onclick="
                            return confirm(
                                'Bạn có chắc muốn xóa các ghế đã chọn? '
                                + 'Ghế đã có lịch sử vé sẽ chỉ chuyển sang INACTIVE.'
                            );
                        ">

                    Xóa ghế đã chọn

                </button>

            </div>

        </div>

    </form>

</div>

<script>

    const checkboxes =
            document.querySelectorAll(
                    ".seat-checkbox"
            );

    const rowSelectButtons =
            document.querySelectorAll(
                    ".row-select-button"
            );

    const selectedCount =
            document.getElementById(
                    "selectedCount"
            );

    const selectAllButton =
            document.getElementById(
                    "selectAllButton"
            );

    const clearButton =
            document.getElementById(
                    "clearButton"
            );

    const editOneButton =
            document.getElementById(
                    "editOneButton"
            );

    const form =
            document.getElementById(
                    "seatBulkForm"
            );

    const bulkSeatType =
            document.getElementById(
                    "bulkSeatType"
            );

    const bulkStatus =
            document.getElementById(
                    "bulkStatus"
            );

    /* =========================================================
       UPDATE SELECT UI
       ========================================================= */

    function updateSelectionUI() {

        let count = 0;

        checkboxes.forEach(
            function (checkbox) {

                const seatCard =
                        checkbox.closest(
                                ".seat-card"
                        );

                if (checkbox.checked) {

                    count++;

                    seatCard.classList.add(
                            "selected"
                    );

                } else {

                    seatCard.classList.remove(
                            "selected"
                    );
                }
            }
        );

        selectedCount.textContent =
                count;

        editOneButton.disabled =
                count !== 1;

        /* =====================================================
           UPDATE NÚT CHỌN TỪNG HÀNG
           ===================================================== */

        rowSelectButtons.forEach(
            function (button) {

                const seatRow =
                        button.closest(
                                ".seat-row"
                        );

                const rowCheckboxes =
                        seatRow.querySelectorAll(
                                ".seat-checkbox"
                        );

                let allSelected =
                        rowCheckboxes.length > 0;

                rowCheckboxes.forEach(
                    function (checkbox) {

                        if (!checkbox.checked) {

                            allSelected =
                                    false;
                        }
                    }
                );

                const rowLabel =
                        button.dataset.rowLabel;

                if (allSelected) {

                    button.textContent =
                            "Bỏ hàng "
                            + rowLabel;

                    button.classList.add(
                            "row-selected"
                    );

                } else {

                    button.textContent =
                            "Chọn hàng "
                            + rowLabel;

                    button.classList.remove(
                            "row-selected"
                    );
                }
            }
        );
    }

    /* =========================================================
       CLICK TỪNG GHẾ
       ========================================================= */

    checkboxes.forEach(
        function (checkbox) {

            checkbox.addEventListener(
                    "change",
                    updateSelectionUI
            );
        }
    );

    /* =========================================================
       CHỌN / BỎ CHỌN NGUYÊN MỘT HÀNG
       ========================================================= */

    rowSelectButtons.forEach(
        function (button) {

            button.addEventListener(
                    "click",
                    function () {

                        const seatRow =
                                button.closest(
                                        ".seat-row"
                                );

                        const rowCheckboxes =
                                seatRow.querySelectorAll(
                                        ".seat-checkbox"
                                );

                        let allSelected =
                                rowCheckboxes.length > 0;

                        rowCheckboxes.forEach(
                            function (checkbox) {

                                if (!checkbox.checked) {

                                    allSelected =
                                            false;
                                }
                            }
                        );

                        /*
                           Nếu cả hàng đang được chọn
                           thì bỏ chọn cả hàng.

                           Nếu chưa chọn đủ
                           thì chọn nguyên hàng.
                        */
                        rowCheckboxes.forEach(
                            function (checkbox) {

                                checkbox.checked =
                                        !allSelected;
                            }
                        );

                        updateSelectionUI();
                    }
            );
        }
    );

    /* =========================================================
       SELECT ALL
       ========================================================= */

    selectAllButton.addEventListener(
            "click",
            function () {

                checkboxes.forEach(
                    function (checkbox) {

                        checkbox.checked =
                                true;
                    }
                );

                updateSelectionUI();
            }
    );

    /* =========================================================
       CLEAR ALL
       ========================================================= */

    clearButton.addEventListener(
            "click",
            function () {

                checkboxes.forEach(
                    function (checkbox) {

                        checkbox.checked =
                                false;
                    }
                );

                updateSelectionUI();
            }
    );

    /* =========================================================
       EDIT ONE SEAT
       ========================================================= */

    editOneButton.addEventListener(
            "click",
            function () {

                const selected =
                        document.querySelectorAll(
                                ".seat-checkbox:checked"
                        );

                if (selected.length !== 1) {
                    return;
                }

                const seatId =
                        selected[0].value;

                window.location.href =
                        "${pageContext.request.contextPath}"
                        + "/admin/seat-form"
                        + "?roomId=${room.roomId}"
                        + "&id="
                        + seatId;
            }
    );

    /* =========================================================
       VALIDATE SUBMIT
       ========================================================= */

    form.addEventListener(
            "submit",
            function (event) {

                const selected =
                        document.querySelectorAll(
                                ".seat-checkbox:checked"
                        );

                if (selected.length === 0) {

                    event.preventDefault();

                    alert(
                            "Vui lòng chọn ít nhất một ghế."
                    );

                    return;
                }

                const action =
                        event.submitter
                        ? event.submitter.value
                        : "";

                if (action === "bulkUpdate") {

                    if (bulkSeatType.value === ""
                            && bulkStatus.value === "") {

                        event.preventDefault();

                        alert(
                                "Vui lòng chọn loại ghế "
                                + "hoặc trạng thái cần cập nhật."
                        );
                    }
                }
            }
    );

    updateSelectionUI();

</script>

<%@ include file="/WEB-INF/include/footer.jsp" %>