<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/header.jsp" %>
<section class="profile-page">
    <h1>Chỉnh sửa thông tin cá nhân</h1>

    <c:if test="${not empty error}">
        <p class="profile-error" role="alert">
            <c:out value="${error}" />
        </p>
    </c:if>

    <c:if test="${not empty profile}">
        <form class="profile-card profile-form"
              method="post"
              enctype="multipart/form-data"
              action="${pageContext.request.contextPath}/user/edit-profile"
              autocomplete="off">

            <input type="hidden"
                   name="profileToken"
                   value="<c:out value='${sessionScope.profileToken}' />">

            <div class="profile-form-grid">

                <div class="profile-field">
                    <label for="profile-email">
                        Email đăng nhập
                    </label>

                    <input id="profile-email"
                           type="text"
                           readonly
                           value="<c:out value='${sessionScope.user.email}' />">
                </div>

                <div class="profile-field">
                    <label for="profile-role">Vai trò</label>

                    <input id="profile-role"
                           type="text"
                           value="Customer"
                           readonly>
                </div>

                <div class="profile-field">
                    <label for="profile-name">Họ và tên</label>

                    <input id="profile-name"
                           name="fullName"
                           required
                           maxlength="100"
                           value="<c:out value='${profile.fullName}' />"
                           autocomplete="name">
                </div>

                <div class="profile-field">
                    <label for="profile-phone">Số điện thoại</label>

                    <input id="profile-phone"
                           name="phone"
                           type="tel"
                           required
                           minlength="10"
                           maxlength="10"
                           pattern="[0-9]{10}"
                           inputmode="numeric"
                           placeholder="Gồm đúng 10 chữ số"
                           title="Số điện thoại phải gồm đúng 10 chữ số"
                           value="<c:out value='${profile.phone}' />">
                </div>

                <div class="profile-field profile-wide">
                    <label for="profile-avatar">
                        Ảnh đại diện (JPG, PNG hoặc GIF; tối đa 5 MB, khoảng 2000 × 2000 pixel)
                    </label>

                    <%-- True when the user already has an avatar stored on this system --%>
                    <c:set var="hasSavedAvatar"
                           value="${not empty profile.avatarUrl and fn:startsWith(profile.avatarUrl, '/avatars/')}" />

                    <c:if test="${hasSavedAvatar}">
                        <c:url var="currentAvatarSrc"
                               value="${profile.avatarUrl}" />

                        <img id="currentAvatar"
                             class="profile-avatar"
                             src="<c:out value='${currentAvatarSrc}' />"
                             alt="Ảnh hiện tại">
                    </c:if>

                    <%-- Preview of the image picked from the computer (not saved yet) --%>
                    <img id="avatarPreview"
                         class="profile-avatar"
                         hidden
                         alt="Ảnh đã chọn">

                    <%-- Shown when the saved avatar is marked for removal --%>
                    <p id="avatarRemovedNote" class="profile-note" hidden>
                        Ảnh đại diện sẽ bị xóa sau khi bạn bấm Lưu.
                    </p>

                    <%-- 1 = remove the saved avatar when the form is submitted --%>
                    <input type="hidden"
                           id="removeAvatar"
                           name="removeAvatar"
                           value="0">

                    <input id="profile-avatar"
                           name="avatarFile"
                           type="file"
                           accept="image/jpeg,image/png,image/gif">

                    <div class="profile-avatar-actions">
                        <%-- Single button: its label is set by the script
                             ("Xóa ảnh đang chọn" or "Xóa ảnh đại diện") --%>
                        <button type="button"
                                id="avatarActionBtn"
                                class="profile-secondary"
                                hidden>
                            Xóa ảnh đại diện
                        </button>
                    </div>

                    <p class="profile-note">
                        Chọn ảnh từ máy tính.
                        Để trống sẽ giữ ảnh hiện tại.
                    </p>
                </div>

            </div>

            <fieldset class="profile-password">
                <legend>Đổi mật khẩu</legend>

                <p class="profile-note">
                    Để trống mật khẩu mới nếu không muốn đổi mật khẩu.
                </p>

                <div class="profile-form-grid">
                    <div class="profile-field">
                        <label for="profile-new-password">
                            Mật khẩu mới
                        </label>

                        <input id="profile-new-password"
                               name="newPassword"
                               type="password"
                               minlength="6"
                               autocomplete="new-password"
                               placeholder="Ít nhất 6 ký tự">
                    </div>

                    <div class="profile-field">
                        <label for="profile-current-password">
                            Mật khẩu hiện tại
                        </label>

                        <input id="profile-current-password"
                               name="currentPassword"
                               type="password"
                               autocomplete="new-password">
                    </div>

                    <div class="profile-field">
                        <label for="profile-confirm-password">
                            Nhập lại mật khẩu mới
                        </label>

                        <input id="profile-confirm-password"
                               name="confirmPassword"
                               type="password"
                               minlength="6"
                               autocomplete="new-password">
                    </div>
                </div>
            </fieldset>

            <div class="profile-actions">
                <button class="profile-primary" type="submit">
                    Lưu
                </button>

                <a class="profile-secondary"
                   href="${pageContext.request.contextPath}/user/profile">
                    Hủy
                </a>
            </div>
        </form>
    </c:if>
</section>

<script>
    (function () {
        const avatarInput = document.getElementById("profile-avatar");

        // The form is not rendered (e.g. profile failed to load)
        if (!avatarInput) {
            return;
        }

        const avatarPreview = document.getElementById("avatarPreview");
        const currentAvatar = document.getElementById("currentAvatar");
        const removeInput = document.getElementById("removeAvatar");
        const removedNote = document.getElementById("avatarRemovedNote");
        const actionBtn = document.getElementById("avatarActionBtn");

        // True if the user already has an avatar stored in the database
        // (read again from the database every time the edit page is opened)
        const hasSaved = ${hasSavedAvatar == true};

        let avatarPreviewUrl = null;

        // Update the UI to match the current state
        function render() {
            const hasPicked = avatarInput.files.length > 0;
            const removed = removeInput.value === "1";

            avatarPreview.hidden = !hasPicked;

            // Hide the saved avatar while a new one is picked or removal is pending
            if (currentAvatar) {
                currentAvatar.hidden = hasPicked || removed;
            }

            removedNote.hidden = !removed;

            // Show the button when:
            // - an image was picked from the computer (not saved yet), or
            // - a saved avatar exists and is not already marked for removal
            actionBtn.hidden = !(hasPicked || (hasSaved && !removed));

            // The label tells the user which image will be removed
            actionBtn.textContent = hasPicked
                    ? "Xóa ảnh đang chọn"
                    : "Xóa ảnh đại diện";
        }

        // Clear the picked file and free its preview URL
        function clearPicked() {
            if (avatarPreviewUrl) {
                URL.revokeObjectURL(avatarPreviewUrl);
                avatarPreviewUrl = null;
            }

            avatarInput.value = "";
            avatarPreview.removeAttribute("src");
        }

        avatarInput.addEventListener("change", function () {
            const file = this.files[0];

            if (avatarPreviewUrl) {
                URL.revokeObjectURL(avatarPreviewUrl);
                avatarPreviewUrl = null;
            }

            // The user opened the dialog and cancelled
            if (!file) {
                avatarPreview.removeAttribute("src");
                render();
                return;
            }

            const allowedTypes = ["image/jpeg", "image/png", "image/gif"];

            // Reject wrong type or file larger than 5 MB
            if (!allowedTypes.includes(file.type)
                    || file.size > 5 * 1024 * 1024) {

                alert("Vui lòng chọn ảnh JPG, PNG hoặc GIF, tối đa 5 MB.");
                clearPicked();
                render();
                return;
            }

            avatarPreviewUrl = URL.createObjectURL(file);
            avatarPreview.src = avatarPreviewUrl;

            // Picking a new image cancels any pending removal
            removeInput.value = "0";
            render();
        });

        actionBtn.addEventListener("click", function () {
            if (avatarInput.files.length > 0) {
                // "Xóa ảnh đang chọn": discard only the picked image,
                // the saved avatar shows again
                clearPicked();

            } else {
                // "Xóa ảnh đại diện": mark the saved avatar for removal
                // (actually removed on Save)
                removeInput.value = "1";
            }

            render();
        });

        render();
    })();
</script>
<%@ include file="/WEB-INF/include/footer.jsp" %>