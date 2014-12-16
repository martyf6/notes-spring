<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<!-- Bootstrap -->
<link href="<c:url value="/resources/css/bootstrap.min.css" />" rel="stylesheet" />
<!-- jQuery -->
<link href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" rel="stylesheet" />
<!-- Notes -->
<link rel="stylesheet" href="<c:url value="/resources/css/notes.css" />" type="text/css" />
<title>Quick Notes</title>
</head>
<body>
	<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
		<div class="container-fluid">
			<div class="navbar-header">
				<span class="navbar-brand">Quick Notes</span>
			</div>
			<div class="collapse navbar-collapse">
				<ul class="nav navbar-nav navbar-right">
					<li><a href="<c:url value='/j_spring_security_logout' />">Sign Out</a></li>
<!-- 					<li class="dropdown"><a href="#" class="dropdown-toggle" -->
<%-- 						data-toggle="dropdown"><c:out value="${username}" /> <b class="caret"></b></a> --%>
<!-- 						<ul class="dropdown-menu"> -->
<!-- 							<li><a href="#">Profile</a></li> -->
<!-- 							<li class="divider"></li> -->
<%-- 							<li><a href="<c:url value='/j_spring_security_logout' />">Sign Out</a></li> --%>
<!-- 						</ul> -->
<!-- 					</li> -->
				</ul>
			</div>
			<!--/.nav-collapse -->
		</div>
	</div>
	<div class="container-fluid">
	
		<!-- Note Navigation Sidebar -->
		<div class="row">
			<div class="col-sm-3 col-md-2 sidebar" id="nav-sub">
				<p>
				  <button type="button" class="btn btn-primary btn-sm" id="new-note">
				  	<span class="glyphicon glyphicon-plus"></span> 
				  	New Note
				  </button>
				</p>
				<!-- glyphicons: file, pencil, align-justify, list-alt -->
				<ul class="nav nav-sidebar" id="note-names">
					<c:forEach items="${notes}" var="note">
					<li class='notename' 
						title='<c:out value="${note.title}" />' 
						data-note-id='<c:out value="${note.id}" />'>
						<a href='#'><span class="glyphicon glyphicon-file"></span><c:out value=" ${note.title}" /></a>
					</li>
					</c:forEach>
				</ul>
			</div>
			
			<!-- Note Contents -->
			<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
<!-- 				<div id="note-data"> -->
					<br>
					<div id="note-msg">
<!-- 						<div class="alert alert-success alert-dismissable"> -->
<!-- 						  <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button> -->
<!-- 						  Note saved. -->
<!-- 						</div> -->
					</div>
					<input type="text" class="form-control" id="note-title" placeholder="Enter new note title here." size="35">
	  				<br>
	  				<textarea class="form-control" id="note-area" rows="20" placeholder="Type your note here."></textarea>
					<br />
					<text id="note-area-info">(Ctrl+S and tabs are enabled in Chrome)</text>
					<br />
					<br /> <input type="button" class="btn btn-default" id="save-button" value="Save" />&nbsp;
					<input type="button" class="btn btn-default" id="delete-button" value="Delete" />
<!-- 				</div> -->
			</div>
		</div>
	</div>
	
	<!-- jQuery -->
	<script src="http://code.jquery.com/jquery-1.11.1.min.js"></script>
	<script src="http://code.jquery.com/ui/1.11.0/jquery-ui.min.js"></script>
	<!-- Bootstrap -->
	<script src="<c:url value="/resources/js/bootstrap.min.js" />"></script>
	<!-- Notes -->
	<script src="<c:url value="/resources/js/notes.js" />"></script>
</body>
</html>