<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <spring:message code="restaurantorders.title" var="title"/>
    <jsp:include page="/WEB-INF/jsp/components/head.jsp">
        <jsp:param name="title" value="${title}"/>
    </jsp:include>
    <script src="<c:url value="/static/js/restaurant_orders.js"/>"></script>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/components/navbar.jsp"/>
<main>
    <div class="restaurant-orders">
        <h1><spring:message code="restaurantorders.orders"/></h1>
        <div class="card w-75">
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-hover">
                        <thead>
                        <tr>
                            <th scope="col"><spring:message code="restaurantorders.table.id"/></th>
                            <th scope="col"><spring:message code="restaurantorders.table.order_type"/></th>
                            <th scope="col"><spring:message code="restaurantorders.table.table_number"/></th>
                            <th scope="col"><spring:message code="restaurantorders.table.address"/></th>
                            <th scope="col"><spring:message code="restaurantorders.table.order_date"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${orders}" var="order">
                            <tr
                                    class="clickable-object clickable-row"
                                    data-bs-toggle="modal"
                                    data-bs-target="#order-details"
                                    data-order-id="${order.orderId}"
                                    <c:forEach items="${order.items}" var="item">
                                        data-order-item-${item.lineNumber}-line-number="${item.lineNumber}"
                                        data-order-item-${item.lineNumber}-comment="${item.comment}"
                                        data-order-item-${item.lineNumber}-product-name="${item.product.name}"
                                        data-order-item-${item.lineNumber}-product-price="${item.product.price}"
                                        data-order-item-${item.lineNumber}-quantity="${item.quantity}"
                                    </c:forEach>
                                    data-order-items-quantity="${fn:length(order.items)}"
                                    data-order-total-price="${order.price}"
                            >
                                <fmt:parseDate value="${order.dateOrdered}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedDateOrdered" type="both"/>
                                <fmt:formatDate pattern="yyyy-MM-dd HH:mm" value="${parsedDateOrdered}" var="dateOrdered"/>
                                <td class="text-center"><c:out value="${order.orderId}"/></td>
                                <td><spring:message code="restaurant.menu.form.${order.orderType.messageCode}"/></td>
                                <c:choose>
                                    <c:when test="${order.orderType == 'DINE_IN'}">
                                        <td><c:out value="${order.tableNumber}"/></td>
                                        <td>-</td>
                                    </c:when>
                                    <c:when test="${order.orderType == 'TAKEAWAY'}">
                                        <td>-</td>
                                        <td>-</td>
                                    </c:when>
                                    <c:when test="${order.orderType == 'DELIVERY'}">
                                        <td>-</td>
                                        <td><c:out value="${order.address}"/></td>
                                    </c:when>
                                </c:choose>
                                <td><c:out value="${dateOrdered}"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="order-details" tabindex="-1">
        <div class="modal-dialog modal-dialog-scrollable modal-xl">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><spring:message code="restaurantorders.orderid"/><span id="order-title"></span></h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <table class="table table-hover">
                        <thead>
                        <tr>
                            <th scope="col">#</th>
                            <th scope="col"><spring:message code="restaurantorders.modal.product_name"/></th>
                            <th scope="col"><spring:message code="restaurantorders.modal.comments"/></th>
                            <th scope="col" class="text-center"><spring:message code="restaurantorders.modal.quantity"/></th>
                            <th scope="col"><spring:message code="restaurantorders.modal.product_price"/></th>
                        </tr>
                        </thead>
                        <tbody id="order-items"></tbody>
                    </table>
                </div>
                <div class="modal-footer">
                    <p style="font-weight: bold"><spring:message code="restaurantorders.modal.total"/> <span id="order-total-price"></span></p>
                </div>
            </div>
        </div>
    </div>
</main>

<c:choose>
    <c:when test="${empty param.page}">
        <c:set var="currentPage" value="1"/>
    </c:when>
    <c:otherwise>
        <c:set var="currentPage" value="${param.page}"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${empty param.size}">
        <c:set var="currentSize" value="20"/>
    </c:when>
    <c:otherwise>
        <c:set var="currentSize" value="${param.size}"/>
    </c:otherwise>
</c:choose>

<nav class="d-flex justify-content-center">
    <ul class="pagination">
        <li class="page-item">
            <c:url value="/restaurants/${id}/orders" var="previousUrl">
                <c:param name="page" value="${currentPage - 1}"/>
                <c:param name="size" value="${currentSize}"/>
            </c:url>
            <a class="page-link ${currentPage == 1 ? "disabled" : ""}" href="${previousUrl}" aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
            </a>
        </li>
        <c:forEach begin="1" end="${pageCount}" var="pageNo">
            <c:url value="/restaurants/${id}/orders" var="pageUrl">
                <c:param name="page" value="${pageNo}"/>
                <c:param name="size" value="${currentSize}"/>
            </c:url>
            <li class="page-item ${pageNo == currentPage ? "active" : ""}"><a class="page-link" href="${pageUrl}">${pageNo}</a></li>
        </c:forEach>
        <li class="page-item">
            <c:url value="/restaurants/${id}/orders" var="nextUrl">
                <c:param name="page" value="${currentPage + 1}"/>
                <c:param name="size" value="${currentSize}"/>
            </c:url>
            <a class="page-link ${currentPage == pageCount ? "disabled" : ""}" href="${nextUrl}" aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
            </a>
        </li>
    </ul>
</nav>

</body>
</html>