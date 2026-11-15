
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/include/header.jsp" %>



<div class="seat-admin-page" data-css-page="admin-seats">

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
