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
                    <label for="profile-account">
                        Tài khoản đăng nhập hiện tại
                    </label>

                    <input id="profile-account"
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
                           value="<c:out value='${profile.phone}' />"
                           oninput="this.value = this.value.replace(/[^0-9]/g, '').slice(0, 10)">
                </div>

                <div class="profile-field profile-wide">
                    <label for="profile-email">Email đăng nhập</label>

                    <input id="profile-email"
                           type="text"
                           value="<c:out value='${sessionScope.user.email}' />"
                           readonly>
                </div>

                <div class="profile-field profile-wide">
                    <label for="profile-avatar">
                        Ảnh đại diện (JPG, PNG hoặc GIF; tối đa 5 MB)
                    </label>

                    <c:if test="${not empty profile.avatarUrl}">
                        <c:url var="currentAvatarSrc"
                               value="${profile.avatarUrl}" />

                        <img id="currentAvatar"
                             class="profile-avatar"
                             src="<c:out value='${currentAvatarSrc}' />"
                             alt="Ảnh hiện tại">
                    </c:if>

                    <img id="avatarPreview"
                         class="profile-avatar"
                         hidden
                         alt="Ảnh đã chọn">

                    <input id="profile-avatar"
                           name="avatarFile"
                           type="file"
                           accept="image/jpeg,image/png,image/gif">

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
                        <label for="profile-current-password">
                            Mật khẩu hiện tại
                        </label>

                        <input id="profile-current-password"
                               name="currentPassword"
                               type="password"
                               autocomplete="new-password">
                    </div>

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
    const avatarInput = document.getElementById("profile-avatar");
    const avatarPreview = document.getElementById("avatarPreview");
    const currentAvatar = document.getElementById("currentAvatar");

    let avatarPreviewUrl;

    if (avatarInput) {
        avatarInput.addEventListener("change", function () {
            if (avatarPreviewUrl) {
                URL.revokeObjectURL(avatarPreviewUrl);
            }

            avatarPreview.hidden = true;
            avatarPreview.removeAttribute("src");

            if (currentAvatar) {
                currentAvatar.hidden = false;
            }

            const file = this.files[0];

            if (!file) {
                return;
            }

            const allowedTypes = [
                "image/jpeg",
                "image/png",
                "image/gif"
            ];

            if (!allowedTypes.includes(file.type)
                    || file.size > 5 * 1024 * 1024) {

                alert("Vui lòng chọn ảnh JPG, PNG hoặc GIF, tối đa 5 MB.");
                this.value = "";
                return;
            }

            avatarPreviewUrl = URL.createObjectURL(file);
            avatarPreview.src = avatarPreviewUrl;
            avatarPreview.hidden = false;

            if (currentAvatar) {
                currentAvatar.hidden = true;
            }
        });
    }
</script>
<%@ include file="/WEB-INF/include/footer.jsp" %>
