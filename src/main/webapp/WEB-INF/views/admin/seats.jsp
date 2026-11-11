
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<style>
    .seat-admin-page {
        max-width: 1250px;
        margin: 0 auto;
        padding: 42px 24px 70px;
        color: #f5f5f5;
    }

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
        padding: 11px 16px;
        border: none;
        border-radius: 7px;
        background: #ff1744;
        color: white;
        text-decoration: none;
        font-weight: 600;
        cursor: pointer;
        white-space: nowrap;
    }

    .btn-primary:hover {
        background: #e3133b;
        color: white;
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

    /* =====================================================
       ADD ROW / ADD SEATS PANELS
       ===================================================== */

    .add-row-panel,
    .add-seats-panel {
        background: #151517;
        border: 1px solid #29292d;
        border-radius: 11px;
        padding: 20px 22px;
        margin-bottom: 22px;
    }

    .add-row-panel h3,
    .add-seats-panel h3 {
        margin: 0 0 6px;
        color: white;
        font-size: 19px;
    }

    .add-description {
        margin: 0 0 17px;
        color: #999;
        font-size: 13px;
        line-height: 1.6;
    }

    .add-row-controls {
        display: grid;
        grid-template-columns: 1fr 1fr auto;
        align-items: end;
        gap: 14px;
    }

    .add-seats-controls {
        display: grid;
        grid-template-columns: 1fr 1fr 1fr auto;
        align-items: end;
        gap: 14px;
    }

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

    .add-seats-panel {
        border-color: #533039;
        scroll-margin-top: 24px;
    }

    .add-seats-panel.highlight {
        border-color: #ff1744;
        box-shadow: 0 0 0 2px rgba(255, 23, 68, .12);
    }

    .add-seats-hint {
        margin-top: 12px;
        color: #aaa;
        font-size: 12px;
    }

    /* =====================================================
       SEAT MAP
       ===================================================== */

    .seat-map-card {
        background: #111113;
        border: 1px solid #29292d;
        border-radius: 13px;
        padding: 32px 28px;
        overflow-x: auto;
    }

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
        box-shadow: 0 5px 18px rgba(255, 255, 255, .2);
    }

    .screen-text {
        margin-top: 10px;
        color: #777;
        font-size: 12px;
        letter-spacing: 4px;
    }

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
        color: #aaa;
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

    /* =====================================================
       NEW: PLUS BUTTON BESIDE EACH SEAT ROW
       ===================================================== */

    .row-add-button {
        flex-shrink: 0;
        width: 39px;
        height: 39px;
        border: 1px dashed #ff1744;
        border-radius: 9px;
        background: rgba(255, 23, 68, .09);
        color: #ff4568;
        font-size: 26px;
        font-weight: 500;
        line-height: 1;
        cursor: pointer;
        transition: .18s;
    }

    .row-add-button:hover {
        background: #ff1744;
        color: white;
        border-style: solid;
        transform: scale(1.06);
    }

    .row-add-button:focus-visible {
        outline: 2px solid white;
        outline-offset: 3px;
    }

    /* =====================================================
       SEATS
       ===================================================== */

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
        transition: .18s;
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

    .seat-standard {
        background: #29292e;
        border-color: #45454c;
    }

    .seat-vip {
        background: #3a3016;
        border-color: #8c7427;
    }

    .seat-vip .seat-type-text {
        color: #e6c65a;
    }

    .seat-couple {
        background: #342238;
        border-color: #84528c;
    }

    .seat-couple .seat-type-text {
        color: #d792e0;
    }

    .seat-inactive {
        background: #18181a;
        border-color: #323236;
        opacity: .5;
    }

    .seat-inactive::after {
        content: "×";
        position: absolute;
        right: 4px;
        top: 1px;
        color: #ff6b81;
        font-weight: 700;
    }

    .seat-card.selected {
        background: #ff1744;
        border-color: #ff6b81;
        opacity: 1;
        transform: translateY(-4px);
        box-shadow: 0 5px 18px rgba(255, 23, 68, .25);
    }

    .seat-card.selected .seat-name,
    .seat-card.selected .seat-type-text {
        color: white;
    }

    /* =====================================================
       LEGEND
       ===================================================== */

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
        opacity: .5;
    }

    .legend-selected {
        background: #ff1744;
        border-color: #ff6b81;
    }

    /* =====================================================
       SELECTION TOOLBAR
       ===================================================== */

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
        opacity: .4;
        cursor: not-allowed;
    }

    /* =====================================================
       BULK EDIT
       ===================================================== */

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
        line-height: 1.6;
    }

    .bulk-controls {
        display: grid;
        grid-template-columns: 1fr 1fr auto auto;
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

    @media (max-width: 950px) {
        .bulk-controls,
        .add-seats-controls {
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

        .bulk-controls,
        .add-row-controls,
        .add-seats-controls {
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
            <h1>Quản lý sơ đồ ghế</h1>
            <p>
                Thêm, xóa, sửa và mở rộng hàng ghế
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
            <strong>${room.capacity} ghế</strong>
        </div>

        <div class="room-info-item">
            Trạng thái:
            <strong>
                <c:out value="${room.status}" />
            </strong>
        </div>
    </div>

    <!-- =====================================================
         MESSAGES
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

    <c:if test="${param.seatsAdded == '1'}">
        <div class="success-message">
            ✓ Đã thêm ghế vào hàng có sẵn thành công.
            Sức chứa phòng đã được cập nhật.
        </div>
    </c:if>

    <c:if test="${param.deleted == '1'}">
        <div class="success-message">
            ✓ Xóa các ghế đã chọn thành công.
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="error-message">
            ⚠ <c:out value="${error}" />
        </div>
    </c:if>

    <!-- =====================================================
         ADD A BRAND NEW ROW
         ===================================================== -->

    <div class="add-row-panel">
        <h3>Thêm hàng ghế mới</h3>

        <p class="add-description">
            Tạo một hàng mới sau hàng cuối cùng.
            Ví dụ: đang có A–D thì thêm hàng E.
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
                    <label for="newRowCount">
                        Số ghế của hàng mới
                    </label>

                    <input class="form-control"
                           id="newRowCount"
                           type="number"
                           name="seatCount"
                           min="1"
                           max="50"
                           value="8"
                           required>
                </div>

                <div class="form-group">
                    <label for="newRowType">
                        Loại ghế mặc định
                    </label>

                    <select class="form-control"
                            id="newRowType"
                            name="rowSeatType">
                        <option value="STANDARD">STANDARD</option>
                        <option value="VIP">VIP</option>
                        <option value="COUPLE">COUPLE</option>
                    </select>
                </div>

                <button type="submit" class="btn-primary">
                    + Thêm hàng
                </button>
            </div>
        </form>
    </div>

    <!-- =====================================================
         NEW: ADD MULTIPLE SEATS TO AN EXISTING ROW
         ===================================================== -->

    <div class="add-seats-panel" id="addSeatsPanel">

        <h3>+ Thêm nhiều ghế vào hàng có sẵn</h3>

        <p class="add-description">
            Chọn một hàng hiện có, nhập số lượng ghế
            và loại ghế muốn thêm.
            Ví dụ: hàng C có C1–C8, thêm 5 ghế
            sẽ tạo C9–C13.
        </p>

        <form method="post"
              id="addSeatsToRowForm"
              action="${pageContext.request.contextPath}/admin/seats">

            <input type="hidden"
                   name="action"
                   value="addSeatsToRow">

            <input type="hidden"
                   name="roomId"
                   value="${room.roomId}">

            <div class="add-seats-controls">

                <!-- Chọn hàng đang có -->
                <div class="form-group">
                    <label for="existingSeatRow">
                        Hàng ghế
                    </label>

                    <select class="form-control"
                            id="existingSeatRow"
                            name="seatRow"
                            required>

                        <option value="">
                            -- Chọn hàng --
                        </option>

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

                            <option value="${rowEntry.key}">
                                Hàng ${rowLabel}
                            </option>

                        </c:forEach>
                    </select>
                </div>

                <!-- Số lượng ghế -->
                <div class="form-group">
                    <label for="appendSeatCount">
                        Số ghế cần thêm
                    </label>

                    <input class="form-control"
                           type="number"
                           id="appendSeatCount"
                           name="seatCount"
                           min="1"
                           max="50"
                           value="1"
                           required>
                </div>

                <!-- Loại ghế -->
                <div class="form-group">
                    <label for="appendSeatType">
                        Loại ghế mới
                    </label>

                    <select class="form-control"
                            id="appendSeatType"
                            name="rowSeatType"
                            required>
                        <option value="STANDARD">STANDARD</option>
                        <option value="VIP">VIP</option>
                        <option value="COUPLE">COUPLE</option>
                    </select>
                </div>

                <button type="submit"
                        class="btn-primary">
                    + Thêm ghế
                </button>
            </div>

            <div class="add-seats-hint"
                 id="appendSeatHint">
                Bạn cũng có thể bấm dấu + bên cạnh
                mỗi hàng trong sơ đồ bên dưới.
            </div>
        </form>
    </div>

    <!-- =====================================================
         BULK SEAT FORM
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
                <div class="screen-text">MÀN HÌNH</div>
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

                                <!-- Tên hàng và chọn hàng -->
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

                                <!-- Danh sách ghế -->
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
                                                 : ''}">

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

                                <!-- =================================
                                     NEW: PLUS BUTTON FOR THIS ROW
                                     ================================= -->

                                <button type="button"
                                        class="row-add-button"
                                        data-seat-row="${rowEntry.key}"
                                        data-row-label="${rowLabel}"
                                        title="Thêm ghế vào hàng ${rowLabel}"
                                        aria-label="Thêm ghế vào hàng ${rowLabel}">
                                    +
                                </button>

                            </div>
                        </c:forEach>
                    </div>

                </c:when>

                <c:otherwise>
                    <div class="empty">
                        Phòng này chưa có ghế.
                        Hãy dùng chức năng Thêm hàng ghế mới.
                    </div>
                </c:otherwise>

            </c:choose>

            <!-- LEGEND -->
            <div class="legend">

                <div class="legend-item">
                    <span class="legend-box legend-standard"></span>
                    STANDARD
                </div>

                <div class="legend-item">
                    <span class="legend-box legend-vip"></span>
                    VIP
                </div>

                <div class="legend-item">
                    <span class="legend-box legend-couple"></span>
                    COUPLE
                </div>

                <div class="legend-item">
                    <span class="legend-box legend-inactive"></span>
                    INACTIVE
                </div>

                <div class="legend-item">
                    <span class="legend-box legend-selected"></span>
                    Đang chọn
                </div>
            </div>

        </div>

        <!-- =================================================
             SELECT TOOLBAR
             ================================================= -->

        <div class="selection-toolbar">

            <div class="selection-info">
                Đã chọn:
                <strong id="selectedCount">0</strong>
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
             BULK EDIT / DELETE
             ================================================= -->

        <div class="bulk-panel">

            <h3>Xử lý các ghế đã chọn</h3>

            <div class="bulk-description">
                Chọn từng ghế, nguyên hàng hoặc tất cả.
                Sau đó đổi loại ghế, trạng thái hoặc xóa.

                Các ghế đã có lịch sử vé sẽ được bảo vệ
                và không được xóa hoặc thay đổi thông tin.
            </div>

            <div class="bulk-controls">

                <div class="form-group">
                    <label for="bulkSeatType">
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
                    <label for="bulkStatus">
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
                        class="delete-button">
                    Xóa ghế đã chọn
                </button>

            </div>
        </div>
    </form>
</div>

<script>
(function () {
    "use strict";

    // =====================================================
    // ELEMENTS
    // =====================================================

    const checkboxes =
        document.querySelectorAll(".seat-checkbox");

    const rowSelectButtons =
        document.querySelectorAll(".row-select-button");

    const rowAddButtons =
        document.querySelectorAll(".row-add-button");

    const selectedCount =
        document.getElementById("selectedCount");

    const selectAllButton =
        document.getElementById("selectAllButton");

    const clearButton =
        document.getElementById("clearButton");

    const editOneButton =
        document.getElementById("editOneButton");

    const form =
        document.getElementById("seatBulkForm");

    const bulkSeatType =
        document.getElementById("bulkSeatType");

    const bulkStatus =
        document.getElementById("bulkStatus");

    const addSeatsPanel =
        document.getElementById("addSeatsPanel");

    const existingSeatRow =
        document.getElementById("existingSeatRow");

    const appendSeatCount =
        document.getElementById("appendSeatCount");

    const appendSeatType =
        document.getElementById("appendSeatType");

    const appendSeatHint =
        document.getElementById("appendSeatHint");

    const addSeatsToRowForm =
        document.getElementById("addSeatsToRowForm");

    // =====================================================
    // UPDATE SELECTION UI
    // =====================================================

    function updateSelectionUI() {
        let count = 0;

        checkboxes.forEach(function (checkbox) {
            const seatCard = checkbox.closest(".seat-card");

            if (checkbox.checked) {
                count++;
                seatCard.classList.add("selected");
            } else {
                seatCard.classList.remove("selected");
            }
        });

        selectedCount.textContent = count;
        editOneButton.disabled = count !== 1;

        rowSelectButtons.forEach(function (button) {
            const seatRow = button.closest(".seat-row");

            const rowCheckboxes =
                seatRow.querySelectorAll(".seat-checkbox");

            const allSelected =
                rowCheckboxes.length > 0
                && Array.from(rowCheckboxes).every(
                    checkbox => checkbox.checked
                );

            const rowLabel = button.dataset.rowLabel;

            if (allSelected) {
                button.textContent = "Bỏ hàng " + rowLabel;
                button.classList.add("row-selected");
            } else {
                button.textContent = "Chọn hàng " + rowLabel;
                button.classList.remove("row-selected");
            }
        });
    }

    // =====================================================
    // SELECT INDIVIDUAL SEAT
    // =====================================================

    checkboxes.forEach(function (checkbox) {
        checkbox.addEventListener(
            "change",
            updateSelectionUI
        );
    });

    // =====================================================
    // SELECT AN ENTIRE ROW
    // =====================================================

    rowSelectButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const seatRow = button.closest(".seat-row");

            const rowCheckboxes =
                seatRow.querySelectorAll(".seat-checkbox");

            const allSelected =
                rowCheckboxes.length > 0
                && Array.from(rowCheckboxes).every(
                    checkbox => checkbox.checked
                );

            rowCheckboxes.forEach(function (checkbox) {
                checkbox.checked = !allSelected;
            });

            updateSelectionUI();
        });
    });

    // =====================================================
    // NEW: PLUS BUTTON BESIDE A ROW
    //
    // Click "+":
    // 1. Select correct existing row.
    // 2. Scroll to the add-seats panel.
    // 3. Focus on seat count.
    //
    // This button DOES NOT submit immediately.
    // The manager confirms quantity and type first.
    // =====================================================

    rowAddButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const rowNumber = button.dataset.seatRow;
            const rowLabel = button.dataset.rowLabel;

            existingSeatRow.value = rowNumber;
            appendSeatCount.value = "1";

            appendSeatHint.textContent =
                "Đang chọn hàng " + rowLabel
                + ". Nhập số ghế và loại ghế muốn thêm.";

            addSeatsPanel.classList.add("highlight");

            addSeatsPanel.scrollIntoView({
                behavior: "smooth",
                block: "center"
            });

            appendSeatCount.focus({
                preventScroll: true
            });
        });
    });

    // =====================================================
    // NEW: VALIDATE ADD SEATS TO EXISTING ROW
    // =====================================================

    addSeatsToRowForm.addEventListener(
        "submit",
        function (event) {
            const row = parseInt(existingSeatRow.value, 10);
            const count = parseInt(appendSeatCount.value, 10);

            if (!Number.isInteger(row)
                    || row < 1 || row > 30) {

                event.preventDefault();

                alert("Vui lòng chọn hàng ghế có sẵn.");
                return;
            }

            if (!Number.isInteger(count)
                    || count < 1 || count > 50) {

                event.preventDefault();

                alert("Số ghế cần thêm phải từ 1 đến 50.");
                return;
            }

            if (!appendSeatType.value) {
                event.preventDefault();

                alert("Vui lòng chọn loại ghế.");
            }
        }
    );

    existingSeatRow.addEventListener(
        "change",
        function () {
            addSeatsPanel.classList.remove("highlight");

            appendSeatHint.textContent =
                "Hệ thống sẽ thêm ghế sau số ghế "
                + "lớn nhất của hàng được chọn.";
        }
    );

    // =====================================================
    // SELECT ALL
    // =====================================================

    selectAllButton.addEventListener("click", function () {
        checkboxes.forEach(function (checkbox) {
            checkbox.checked = true;
        });

        updateSelectionUI();
    });

    // =====================================================
    // CLEAR ALL
    // =====================================================

    clearButton.addEventListener("click", function () {
        checkboxes.forEach(function (checkbox) {
            checkbox.checked = false;
        });

        updateSelectionUI();
    });

    // =====================================================
    // EDIT ONE SEAT
    // =====================================================

    editOneButton.addEventListener("click", function () {
        const selected =
            document.querySelectorAll(
                ".seat-checkbox:checked"
            );

        if (selected.length !== 1) {
            return;
        }

        const seatId = selected[0].value;

        window.location.href =
            "${pageContext.request.contextPath}"
            + "/admin/seat-form"
            + "?roomId=${room.roomId}"
            + "&id=" + encodeURIComponent(seatId);
    });

    // =====================================================
    // VALIDATE BULK UPDATE / DELETE
    // =====================================================

    form.addEventListener("submit", function (event) {
        const selected =
            document.querySelectorAll(
                ".seat-checkbox:checked"
            );

        if (selected.length === 0) {
            event.preventDefault();

            alert("Vui lòng chọn ít nhất một ghế.");
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

        if (action === "deleteSelected") {
            const confirmed = confirm(
                "Bạn có chắc muốn xóa "
                + selected.length + " ghế đã chọn?\n"
                + "Nếu có ghế từng có vé, "
                + "hệ thống sẽ từ chối xóa cả nhóm."
            );

            if (!confirmed) {
                event.preventDefault();
            }
        }
    });

    updateSelectionUI();
})();
</script>

<%@ include file="/WEB-INF/include/footer.jsp" %>
