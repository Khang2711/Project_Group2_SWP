<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="movie-page-title">
    <h1>Quản lý khuyến mãi</h1>
    <div class="title-line"></div>
    <p>Quản lý mã khuyến mãi của hệ thống.</p>
</div>

<div style="margin-bottom: 25px;">
    <a href="${ctx}/admin/promotion-form"
       class="login-button">
        Thêm khuyến mãi
    </a>
</div>

<c:if test="${empty promotions}">
    <section>
        <p>Chưa có khuyến mãi nào.</p>
    </section>
</c:if>

<c:if test="${not empty promotions}">

    <div style="overflow-x: auto;">

        <table style="
               width: 100%;
               border-collapse: collapse;
               background-color: #18181c;
               border: 1px solid #303038;
               ">

            <thead>

                <tr style="background-color: #222228;">

                    <th style="padding: 12px; border: 1px solid #303038;">
                        ID
                    </th>

                    <th style="padding: 12px; border: 1px solid #303038;">
                        Promo Code
                    </th>

                    <th style="padding: 12px; border: 1px solid #303038;">
                        Mô tả
                    </th>

                    <th style="padding: 12px; border: 1px solid #303038;">
                        Loại
                    </th>

                    <th style="padding: 12px; border: 1px solid #303038;">
                        Giá trị
                    </th>

                    <th style="padding: 12px; border: 1px solid #303038;">
                        Đơn tối thiểu
                    </th>

                    <th style="padding: 12px; border: 1px solid #303038;">
                        Giảm tối đa
                    </th>

                    <th style="padding: 12px; border: 1px solid #303038;">
                        Bắt đầu
                    </th>

                    <th style="padding: 12px; border: 1px solid #303038;">
                        Kết thúc
                    </th>

                    <th style="padding: 12px; border: 1px solid #303038;">
                        Giới hạn
                    </th>

                    <th style="padding: 12px; border: 1px solid #303038;">
                        Trạng thái
                    </th>

                    <th style="padding: 12px; border: 1px solid #303038;">
                        Thao tác
                    </th>

                </tr>

            </thead>

            <tbody>

                <c:forEach var="promotion" items="${promotions}">

                    <tr>

                        <td style="padding: 10px; border: 1px solid #303038; text-align: center;">
                            <c:out value="${promotion.promotionId}" />
                        </td>

                        <td style="padding: 10px; border: 1px solid #303038;">
                            <strong>
                                <c:out value="${promotion.promoCode}" />
                            </strong>
                        </td>

                        <td style="padding: 10px; border: 1px solid #303038;">
                            <c:out value="${promotion.description}" />
                        </td>

                        <td style="padding: 10px; border: 1px solid #303038; text-align: center;">
                            <c:out value="${promotion.discountType}" />
                        </td>

                        <td style="padding: 10px; border: 1px solid #303038; text-align: center;">

                            <c:if test="${promotion.discountType eq 'PERCENT'}">
                                <fmt:formatNumber
                                    value="${promotion.discountValue}"
                                    maxFractionDigits="2" />%
                            </c:if>

                            <c:if test="${promotion.discountType eq 'FIXED'}">
                                <fmt:formatNumber
                                    value="${promotion.discountValue}"
                                    maxFractionDigits="2" />
                                VNĐ
                            </c:if>

                        </td>

                        <td style="padding: 10px; border: 1px solid #303038; text-align: center;">

                            <fmt:formatNumber
                                value="${promotion.minOrderAmount}"
                                maxFractionDigits="2" />
                            VNĐ

                        </td>

                        <td style="padding: 10px; border: 1px solid #303038; text-align: center;">

                            <c:if test="${promotion.maxDiscountAmount != null}">

                                <fmt:formatNumber
                                    value="${promotion.maxDiscountAmount}"
                                    maxFractionDigits="2" />
                                VNĐ

                            </c:if>

                            <c:if test="${promotion.maxDiscountAmount == null}">
                                -
                            </c:if>

                        </td>

                        <td style="
                            padding: 10px;
                            border: 1px solid #303038;
                            text-align: center;
                            white-space: nowrap;
                            ">

                            <fmt:formatDate
                                value="${promotion.startDate}"
                                pattern="dd/MM/yyyy" />

                        </td>

                        <td style="
                            padding: 10px;
                            border: 1px solid #303038;
                            text-align: center;
                            white-space: nowrap;
                            ">

                            <fmt:formatDate
                                value="${promotion.endDate}"
                                pattern="dd/MM/yyyy" />

                        </td>

                        <td style="padding: 10px; border: 1px solid #303038; text-align: center;">

                            <c:if test="${promotion.usageLimit != null}">
                                <c:out value="${promotion.usageLimit}" />
                            </c:if>

                            <c:if test="${promotion.usageLimit == null}">
                                Không giới hạn
                            </c:if>

                        </td>

                        <td style="padding: 10px; border: 1px solid #303038; text-align: center;">

                            <c:if test="${promotion.status eq 'ACTIVE'}">
                                <span style="color: #4caf50; font-weight: bold;">
                                    ACTIVE
                                </span>
                            </c:if>

                            <c:if test="${promotion.status eq 'INACTIVE'}">
                                <span style="color: #aaaaaa; font-weight: bold;">
                                    INACTIVE
                                </span>
                            </c:if>

                        </td>

                        <td style="
                            padding: 10px;
                            border: 1px solid #303038;
                            white-space: nowrap;
                            ">

                            <div style="
                                 display: flex;
                                 justify-content: center;
                                 align-items: center;
                                 gap: 8px;
                                 ">

                                <a href="${ctx}/admin/promotion-form?id=${promotion.promotionId}"
                                   style="
                                   display: inline-flex;
                                   align-items: center;
                                   justify-content: center;
                                   padding: 8px 16px;
                                   min-width: 55px;
                                   border: 1px solid #55555f;
                                   border-radius: 6px;
                                   color: #ffffff;
                                   text-decoration: none;
                                   font-weight: 600;
                                   ">
                                    Sửa
                                </a>

                                <c:if test="${promotion.status eq 'ACTIVE'}">

                                    <form action="${ctx}/admin/promotions"
                                          method="post"
                                          style="margin: 0;">

                                        <input type="hidden"
                                               name="promotionId"
                                               value="${promotion.promotionId}">

                                        <input type="hidden"
                                               name="status"
                                               value="INACTIVE">

                                        <button type="submit"
                                                style="
                                                padding: 8px 14px;
                                                border: 1px solid #ff2638;
                                                border-radius: 6px;
                                                background-color: #ff2638;
                                                color: #ffffff;
                                                font-weight: 600;
                                                cursor: pointer;
                                                white-space: nowrap;
                                                ">
                                            Vô hiệu hóa
                                        </button>

                                    </form>

                                </c:if>

                                <c:if test="${promotion.status eq 'INACTIVE'}">

                                    <form action="${ctx}/admin/promotions"
                                          method="post"
                                          style="margin: 0;">

                                        <input type="hidden"
                                               name="promotionId"
                                               value="${promotion.promotionId}">

                                        <input type="hidden"
                                               name="status"
                                               value="ACTIVE">

                                        <button type="submit"
                                                style="
                                                padding: 8px 14px;
                                                border: 1px solid #4caf50;
                                                border-radius: 6px;
                                                background-color: transparent;
                                                color: #4caf50;
                                                font-weight: 600;
                                                cursor: pointer;
                                                white-space: nowrap;
                                                ">
                                            Kích hoạt
                                        </button>

                                    </form>

                                </c:if>

                            </div>

                        </td>

                    </tr>

                </c:forEach>

            </tbody>

        </table>

    </div>

</c:if>

<%@ include file="/WEB-INF/include/footer.jsp" %>