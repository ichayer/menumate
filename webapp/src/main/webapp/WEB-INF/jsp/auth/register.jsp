<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
<head>
    <jsp:include page="/WEB-INF/jsp/components/head.jsp">
        <jsp:param name="title" value="Register"/>
    </jsp:include>
</head>
<body>
<h1>Register</h1>

<c:url var="registerUrl" value="/auth/register"/>
<form:form modelAttribute="registerForm" action="${registerUrl}" method="post">
    <div>
        <form:errors path="email" element="p" cssClass="error-message"/>
        <form:label path="email"> Email:
            <form:input path="email" type="text" placeholder="Email"/>
        </form:label>
    </div>
    <div>
        <form:errors path="password" element="p" cssClass="error-message"/>
        <form:label path="password"> Password:
            <form:password path="password" placeholder="Password"/>
        </form:label>
    </div>
    <div>
        <form:errors path="repeatPassword" element="p" cssClass="error-message"/>
        <form:label path="repeatPassword"> Repeat password:
            <form:password path="repeatPassword" placeholder="Repeat password"/>
        </form:label>
    </div>
    <div>
        <form:errors path="name" element="p" cssClass="error-message"/>
        <form:label path="name"> Name:
            <form:input path="name" type="text" placeholder="Name"/>
        </form:label>
    </div>
    <div>
        <input type="submit" value="Register!"/>
    </div>
</form:form>
</body>
</html>
