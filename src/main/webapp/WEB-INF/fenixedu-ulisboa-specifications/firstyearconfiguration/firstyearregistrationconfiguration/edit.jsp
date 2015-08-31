<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js"/>
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css"/>

<link rel="stylesheet" href="${datatablesCssUrl}"/>
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json"/>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css"/>

${portal.toolkit()}

<link href="${pageContext.request.contextPath}/static/fenixedu-ulisboa-specifications/css/dataTables.responsive.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/static/fenixedu-ulisboa-specifications/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>						
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js" ></script>
<script src="${pageContext.request.contextPath}/static/fenixedu-ulisboa-specifications/js/omnis.js"></script>



<%-- TITLE --%>
<div class="page-header">
	<h1><spring:message code="label.firstYearConfiguration.edit" />
		<small></small>
	</h1>
</div>
<%-- NAVIGATION --%>
<div class="well well-sm" style="display:inline-block">
	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class="" onClick="saveForms()"><spring:message code="label.event.firstYearConfiguration.save" /> </a> |&nbsp;&nbsp;
	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/fenixedu-ulisboa-specifications/firstyearconfiguration/firstyearregistrationconfiguration/"  ><spring:message code="label.event.firstYearConfiguration.cancel" /></a>	
</div>
	<c:if test="${not empty infoMessages}">
				<div class="alert alert-info" role="alert">
					
					<c:forEach items="${infoMessages}" var="message"> 
						<p> <span class="glyphicon glyphicon glyphicon-ok-sign" aria-hidden="true">&nbsp;</span>
  							${message}
  						</p>
					</c:forEach>
					
				</div>	
			</c:if>
			<c:if test="${not empty warningMessages}">
				<div class="alert alert-warning" role="alert">
					
					<c:forEach items="${warningMessages}" var="message"> 
						<p> <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
  							${message}
  						</p>
					</c:forEach>
					
				</div>	
			</c:if>
			<c:if test="${not empty errorMessages}">
				<div class="alert alert-danger" role="alert">
					
					<c:forEach items="${errorMessages}" var="message"> 
						<p> <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
  							${message}
  						</p>
					</c:forEach>
					
				</div>	
			</c:if>





<c:choose>
	<c:when test="${not empty editResultsDataSet}">
		<table id="editTable" class="table responsive table-bordered table-hover">
			<thead>
				<tr>
					<%--!!!  Field names here --%>
							<th><spring:message code="label.FirstYearRegistrationConfiguration.degreeName"/></th>
					<th><spring:message code="label.FirstYearRegistrationConfiguration.degreeCode"/></th>
<th><spring:message code="label.FirstYearRegistrationConfiguration.requiresVaccination"/></th>
<th><spring:message code="label.FirstYearRegistrationConfiguration.requiresCoursesEnrolment"/></th>
<th><spring:message code="label.FirstYearRegistrationConfiguration.requiresClassesEnrolment"/></th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>-</td>
					<td>-</td>
					<td>
						<a onclick="changeCheckBoxesState('.requiresVaccination', true)"><spring:message code="label.event.firstYearConfiguration.selectAll" /> </a>
						<br />
						<a onclick="changeCheckBoxesState('.requiresVaccination', false)"><spring:message code="label.event.firstYearConfiguration.unselectAll" /> </a>
					</td>
					<td>
						<a onclick="changeCheckBoxesState('.requiresCoursesEnrolment', true)"><spring:message code="label.event.firstYearConfiguration.selectAll" /> </a>
						<br />
						<a onclick="changeCheckBoxesState('.requiresCoursesEnrolment', false)"><spring:message code="label.event.firstYearConfiguration.unselectAll" /> </a>
					</td>
					<td>
						<a onclick="changeCheckBoxesState('.requiresClassesEnrolment', true)"><spring:message code="label.event.firstYearConfiguration.selectAll" /> </a>
						<br />
						<a onclick="changeCheckBoxesState('.requiresClassesEnrolment', false)"><spring:message code="label.event.firstYearConfiguration.unselectAll" /> </a>
					</td>
					
				</tr>
				<c:forEach items="${editResultsDataSet}" var="searchResult">
				<form action="${pageContext.request.contextPath}/fenixedu-ulisboa-specifications/firstyearconfiguration/firstyearregistrationconfiguration/edit/save" method="POST">
					<input type="hidden" name="degreeExternalId" value="${searchResult.degreeExternalId}">
					<tr>
						<td>${searchResult.degreeName}</td>
						<td>${searchResult.degreeCode}</td>
						<td>
							<c:if test="${searchResult.requiresVaccination}">
								<input type="checkbox" name="requiresVaccination" checked class="requiresVaccination">							
							</c:if>
							<c:if test="${!searchResult.requiresVaccination}">
								<input type="checkbox" name="requiresVaccination" class="requiresVaccination">
							</c:if>
						</td>
						<td>
							<c:if test="${searchResult.requiresCoursesEnrolment}">
								<input type="checkbox" name="requiresCoursesEnrolment" checked class="requiresCoursesEnrolment">							
							</c:if>
							<c:if test="${!searchResult.requiresCoursesEnrolment}">
								<input type="checkbox" name="requiresCoursesEnrolment" class="requiresCoursesEnrolment">
							</c:if>
						</td>
						<td>
							<c:if test="${searchResult.requiresClassesEnrolment}">
								<input type="checkbox" name="requiresClassesEnrolment" checked class="requiresClassesEnrolment">							
							</c:if>
							<c:if test="${!searchResult.requiresClassesEnrolment}">
								<input type="checkbox" name="requiresClassesEnrolment" class="requiresClassesEnrolment">
							</c:if>
						</td>
		            </tr>
	            </form>
            </c:forEach>
			</tbody>
		</table>
	</c:when>
	<c:otherwise>
				<div class="alert alert-warning" role="alert">
					
					<p> <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>			<spring:message code="label.noResultsFound" /></p>
					
				</div>	
		
	</c:otherwise>
</c:choose>

<script>
$("form").submit(function(){
    $.post($(this).attr('action'), $(this).serialize(), function(response){},'json');
    return false;
 });

function saveForms(){
	$("form").submit();
	changesDetected = false;
};

function changeCheckBoxesState(cssSelector, newValue){
	fields = $(cssSelector);
	for(i in fields){
		fields[i].checked=newValue;
	}	
	changesDetected = true;
}

$(":checkbox").on("change", function(){
	changesDetected = true;
});
window.onbeforeunload = function (e) {
	if(changesDetected){
		return "<spring:message code="label.event.firstYearConfiguration.notSavedError" />";
	}
}
</script>

