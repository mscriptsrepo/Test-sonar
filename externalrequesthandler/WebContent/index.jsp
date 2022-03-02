<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html lang="en">
<head>
  <title>mscripts</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
  <link rel="icon" href="http://build-server.mscripts.com:9100/impldash/internal/images/favicon.png" type="image/gif" sizes="16x16">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
  <style>
	#sta{
	width: 20px;
	height: 20px;
	background-color: green;
	border-radius: 10px;
}

#sta1{
	width: 20px;
	height: 20px;
	background-color: red;
	border-radius: 10px;
}

.nav-logo{
	width: 200px;
	height: auto;
}

.gap{
	padding: 10px;
}

.around{
	background-color: #F9F9F9;
	border-radius: 10px;
	border: 1px solid #EAEAEC;
	margin: 2%;
}
</style>
</head>
<body>

<nav class="navbar navbar-inverse">
  <div class="container-fixed">
    <div class="navbar-header">
      <a class="navbar-brand nav-logo" href="#"><img src="http://build-server.mscripts.com:9100/impldash/internal/images/logo.png"></a>
    </div>
  </div>
</nav>
<div class="container" style="position: absolute;
    top: 50%;
    left: 50%;
    display: block;
    margin-top: -220px;
    margin-left: -235px;
    padding: 25px;
    width: 420px;
    height: 320px;
    background: white;
    text-align: center;">
<h2 style="text-align:center">${clients}</h2>
  <p style="text-align:center">${modules} Version Information</p> 
  <div class="row">
  <div class="around">
  	<div class="gap">
  		<span><b>Version : </b></span> <span>${version}</span>
  	</div>
  	<div  class="gap">
  		<span><b>Build # : </b></span> <span>${BUILD_NUMBER}</span>
  	</div>
  	<div class="gap">
  		<span><b>Build Date : </b></span> <span>${BUILD_DATE}</span>
  	</div>
  	<div class="gap">
  		<span><b>Build Environment : </b></span> <span>${ENVIRONMENT1}</span>
  	</div>
	<div class="gap">
  		<span><b>Release Ticket (JIRA) : </b></span> <span><a href="http://jira.mscripts.com:8080/browse/${JIRA_ID}" target="_blank">${JIRA_ID}</a></span>
  	</div>
  </div>
  </div>
</div>
</body>
</html>

