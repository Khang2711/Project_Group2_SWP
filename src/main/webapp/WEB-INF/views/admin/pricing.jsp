<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ include file="/WEB-INF/include/header.jsp" %>



<div class="pricing-page" data-css-page="admin-pricing">

    <div class="page-header">

        <div>

            <h1>
                Quản lý giá vé
            </h1>

            <p>
                Thiết lập giá theo phim, phòng, ghế,
                ngày và khung giờ.
            </p>

        </div>

        <a class="btn-add"
           href="${pageContext.request.contextPath}/admin/pricing-form">

            + Thêm quy tắc giá

        </a>

    </div>

    <c:if test="${param.saved == '1'}">

        <div class="success-message">

            ✓ Lưu quy tắc giá thành công.

        </div>

    </c:if>

    <c:if test="${param.deleted == '1'}">

        <div class="success-message">

            ✓ Xóa quy tắc giá thành công.

        </div>

    </c:if>

    <c:if test="${not empty error}">

        <div class="error-message">

            ⚠
            <c:out value="${error}" />

        </div>

    </c:if>

    <div class="table-card">

        <table class="pricing-table"
               data-paginate="true"
               data-page-size="10">

            <thead>

                <tr>

                    <th>Phim</th>
                    <th>Phòng</th>
                    <th>Ghế</th>
                    <th>Ngày</th>
                    <th>Khung giờ</th>
                    <th>Giá</th>
                    <th>Hiệu lực</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>

                </tr>

            </thead>

            <tbody>

                <c:forEach var="pricing"
                           items="${pricingList}">

                    <tr>

                        <td>

                            <c:if test="${empty pricing.movieId}">

                                <span class="muted">
                                    Tất cả phim
                                </span>

                            </c:if>

                            <c:if test="${not empty pricing.movieId}">

                                <c:out value="${pricing.movieTitle}" />

                            </c:if>

                        </td>

                        <td>

                            <c:if test="${empty pricing.roomType}">

                                <span class="muted">
                                    Tất cả
                                </span>

                            </c:if>

                            <c:if test="${not empty pricing.roomType}">

                                <span class="tag">

                                    <c:out value="${pricing.roomType}" />

                                </span>

                            </c:if>

                        </td>

                        <td>

                            <span class="
                                  tag
                                  ${pricing.seatType == 'VIP'
                                    ? 'tag-vip'
                                    : pricing.seatType == 'COUPLE'
                                    ? 'tag-couple'
                                    : ''}">

                                <c:out value="${pricing.seatType}" />

                            </span>

                        </td>

                        <td>

                            <c:if test="${not empty pricing.specialDate}">

                                <span class="tag tag-special">

                                    <fmt:formatDate
                                        value="${pricing.specialDate}"
                                        pattern="dd/MM/yyyy" />

                                </span>

                            </c:if>

                            <c:if test="${empty pricing.specialDate
                                          and pricing.dayType == 'WEEKDAY'}">

                                Thứ 2 - Thứ 6

                            </c:if>

                            <c:if test="${empty pricing.specialDate
                                          and pricing.dayType == 'WEEKEND'}">

                                Thứ 7 - CN

                            </c:if>

                            <c:if test="${empty pricing.specialDate
                                          and pricing.dayType != 'WEEKDAY'
                                          and pricing.dayType != 'WEEKEND'}">

                                Tất cả ngày

                            </c:if>

                        </td>

                        <td>

                            <c:if test="${not empty pricing.startTime}">

                                <fmt:formatDate
                                    value="${pricing.startTime}"
                                    pattern="HH:mm" />

                                -

                                <fmt:formatDate
                                    value="${pricing.endTime}"
                                    pattern="HH:mm" />

                            </c:if>

                            <c:if test="${empty pricing.startTime}">

                                <span class="muted">
                                    Cả ngày
                                </span>

                            </c:if>

                        </td>

                        <td class="price">

                            <fmt:formatNumber
                                value="${pricing.price}"
                                pattern="#,##0" />

                            đ

                        </td>

                        <td>

                            <fmt:formatDate
                                value="${pricing.startDate}"
                                pattern="dd/MM/yyyy" />

                            →

                            <c:if test="${not empty pricing.endDate}">

                                <fmt:formatDate
                                    value="${pricing.endDate}"
                                    pattern="dd/MM/yyyy" />

                            </c:if>

                            <c:if test="${empty pricing.endDate}">

                                Không giới hạn

                            </c:if>

                        </td>

                        <td>

                            <c:if test="${pricing.status == 'ACTIVE'}">

                                <span class="badge active">
                                    ACTIVE
                                </span>

                            </c:if>

                            <c:if test="${pricing.status != 'ACTIVE'}">

                                <span class="badge inactive">
                                    INACTIVE
                                </span>

                            </c:if>

                        </td>

                        <td>

                            <div class="actions">

                                <a class="btn-edit"
                                   href="${pageContext.request.contextPath}/admin/pricing-form?id=${pricing.pricingId}">

                                    Sửa

                                </a>

                                <form method="post"
                                      action="${pageContext.request.contextPath}/admin/pricing">

                                    <input type="hidden"
                                           name="action"
                                           value="delete">

                                    <input type="hidden"
                                           name="id"
                                           value="${pricing.pricingId}">

                                    <button class="btn-delete"
                                            type="submit"
                                            onclick="return confirm('Xóa quy tắc giá này?');">

                                        Xóa

                                    </button>

                                </form>

                            </div>

                        </td>

                    </tr>

                </c:forEach>

                <c:if test="${empty pricingList}">

                    <tr>

                        <td colspan="9"
                            class="empty">

                            Chưa có quy tắc giá vé.

                        </td>

                    </tr>

                </c:if>

            </tbody>

        </table>

    </div>

</div>

<%@ include file="/WEB-INF/include/table-pagination.jsp" %>

<%@ include file="/WEB-INF/include/footer.jsp" %>