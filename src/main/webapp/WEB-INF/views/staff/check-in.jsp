<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/include/header.jsp" %>

<c:set var="ctx"
       value="${pageContext.request.contextPath}" />

<style>
    .checkin-page {
        max-width: 700px;
        margin: 0 auto;
        padding: 40px 30px 60px;
    }

    .page-title h1 {
        margin: 0;
        font-size: 30px;
        font-weight: bold;
    }

    .title-line {
        width: 50px;
        height: 4px;
        margin-top: 12px;
        margin-bottom: 18px;
        background: #ff3045;
        border-radius: 2px;
    }

    .checkin-subtitle {
        margin: 0;
        color: #cccccc;
    }

    .checkin-box {
        margin-top: 25px;
        padding: 25px;
        background: #18181c;
        border: 1px solid #33333a;
        border-radius: 8px;
    }

    .checkin-field {
        margin-bottom: 18px;
    }

    .checkin-field label {
        display: block;
        margin-bottom: 7px;
        font-weight: bold;
    }

    .checkin-field input {
        width: 100%;
        box-sizing: border-box;
        padding: 10px;
        background: #101012;
        color: white;
        border: 1px solid #55555f;
        border-radius: 5px;
    }

    .checkin-field input:focus {
        outline: none;
        border-color: #ff3045;
    }

    .checkin-button {
        padding: 10px 18px;
        border: none;
        border-radius: 5px;
        background: #ff3045;
        color: white;
        font-weight: bold;
        cursor: pointer;
    }

    .checkin-button:hover {
        background: #e6293d;
    }

    .message-error {
        margin-top: 20px;
        padding: 12px;
        border: 1px solid #ff3045;
        background: #40191d;
        border-radius: 5px;
    }
</style>

<div class="checkin-page">

    <div class="page-title">
        <h1>Kiểm tra và check-in vé</h1>
        <div class="title-line"></div>
    </div>

    <p class="checkin-subtitle">
        Nhập hoặc quét mã vé để kiểm tra thông tin vé.
    </p>

    <c:if test="${not empty error}">
        <div class="message-error">
            <c:out value="${error}" />
        </div>
    </c:if>

    <div class="checkin-box">

        <form action="${ctx}/staff/check-in"
              method="post">

            <div class="checkin-field">

                <label for="ticketCode">
                    Mã vé
                </label>

                <input type="text"
                       id="ticketCode"
                       name="ticketCode"
                       value="${ticketCode}"
                       placeholder="Ví dụ: DEMO-1-01" />

            </div>

            <button type="submit"
                    class="checkin-button">
                Tra cứu vé
            </button>

        </form>

    </div>

</div>

<%@ include file="/WEB-INF/include/footer.jsp" %>