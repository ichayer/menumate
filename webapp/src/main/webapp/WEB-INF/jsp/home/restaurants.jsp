<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
    <spring:message code="restaurants.title" var="home"/>
    <spring:message code="restaurants.search.placeholder" var="placeholder"/>
    <jsp:include page="/WEB-INF/jsp/components/head.jsp">
        <jsp:param name="title" value="${home}"/>
    </jsp:include>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/components/navbar.jsp"/>
    <c:if test="${error}">
        <jsp:include page="/WEB-INF/jsp/components/param_error.jsp"/>
    </c:if>
    <c:url var="search" value="/restaurants"/>
    <form:form modelAttribute="searchForm" action="${search}" method="get">
        <div class="input-group flex-nowrap">
            <span class="input-group-text search-input"><i class="bi bi-search"></i></span>
            <form:input type="text" path="search" cssClass="form-control search-input" placeholder="${placeholder}"/>
            <form:errors path="search" element="div" cssClass="form-error invalid-tooltip"/>
        </div>
        <form:input type="hidden" path="page" value="1"/>
        <input type="submit" class="btn btn-primary" value='<spring:message code="restaurants.search"/>'>
    </form:form>
    <main class="restaurant-feed">
        <c:forEach items="${restaurants}" var="restaurant">
            <c:url var="restaurantUrl" value="/restaurants/${restaurant.restaurantId}"/>
            <c:url var="mainImage" value="/images/${restaurant.portraitId1}"/>
            <c:url var="hoverImage" value="/images/${restaurant.portraitId2}"/>
            <jsp:include page="/WEB-INF/jsp/components/restaurant_card.jsp">
                <jsp:param name="name" value="${restaurant.name}"/>
                <jsp:param name="address" value="${restaurant.address}"/>
                <jsp:param name="main_image" value="${mainImage}"/>
                <jsp:param name="hover_image" value="${hoverImage}"/>
                <jsp:param name="link" value="${restaurantUrl}"/>
            </jsp:include>
        </c:forEach>
        <c:if test="${fn:length(restaurants) == 0}">
            <div class="empty-results">
                <h1><i class="bi bi-slash-circle"></i></h1>
                <p>  <spring:message code="restaurants.search.noresult"/></p>
            </div>
        </c:if>
    </main>

    <c:choose>
        <c:when test="${empty searchForm.page}">
            <c:set var="currentPage" value="1"/>
        </c:when>
        <c:otherwise>
            <c:set var="currentPage" value="${searchForm.page}"/>
        </c:otherwise>
    </c:choose>
    <c:choose>
        <c:when test="${empty searchForm.size}">
            <c:set var="currentSize" value="12"/>
        </c:when>
        <c:otherwise>
            <c:set var="currentSize" value="${searchForm.size}"/>
        </c:otherwise>
    </c:choose>

    <nav class="d-flex justify-content-center">
        <ul class="pagination">
            <li class="page-item">
                <c:url value="/restaurants" var="previousUrl">
                    <c:param name="search" value="${searchForm.search}"/>
                    <c:param name="page" value="${currentPage - 1}"/>
                    <c:param name="size" value="${currentSize}"/>
                </c:url>
                <a class="page-link ${currentPage == 1 ? "disabled" : ""}" href="${previousUrl}" aria-label="Previous">
                    <span aria-hidden="true">&laquo;</span>
                </a>
            </li>
            <c:forEach begin="1" end="${pageCount}" var="pageNo">
                <c:url value="/restaurants" var="pageUrl">
                    <c:param name="search" value="${searchForm.search}"/>
                    <c:param name="page" value="${pageNo}"/>
                    <c:param name="size" value="${currentSize}"/>
                </c:url>
                <li class="page-item ${pageNo == currentPage ? "active" : ""}"><a class="page-link" href="${pageUrl}">${pageNo}</a></li>
            </c:forEach>
            <li class="page-item">
                <c:url value="/restaurants" var="nextUrl">
                    <c:param name="search" value="${searchForm.search}"/>
                    <c:param name="page" value="${currentPage + 1}"/>
                    <c:param name="size" value="${currentSize}"/>
                </c:url>
                <a class="page-link ${(currentPage == pageCount || pageCount == 0) ? "disabled" : ""}" href="${nextUrl}" aria-label="Next">
                    <span aria-hidden="true">&raquo;</span>
                </a>
            </li>
        </ul>
    </nav>
</body>
</html>
