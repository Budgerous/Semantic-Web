<!DOCTYPE html>
<html>
<head>
    <title>Please, work</title>
    <link rel="stylesheet" type="text/css" href="css/style.css" />
    <link rel="stylesheet" type="text/css" href="css/series.css" />
    <link rel="stylesheet" type="text/css" href="css/font-awesome.min.css" />
</head>
<body>
<nav class="nav">
    <div class="nav-item home">
        <p><a href="/">Home</a></p>
    </div>
    <div class="nav-item nav-search">
        <form method="get" action="search">
            <#if query?has_content>
                <input type="text" name="query" autocomplete="off" placeholder="Search..." value="${query}"/>
            <#else>
                <input type="text" name="query" autocomplete="off" placeholder="Search..." />
            </#if>
        </form>
    </div>
    <div class="nav-item about">
        <p><a href="/about">About</a></p>
    </div>
</nav>
<#if series?has_content>
<div class="content">
    <h1 class="series-name">${series.name}</h1>
    <div class="picture-overview">
        <div class="series-picture-container">
            <img class="series-picture" src="${series.picture}" />
        </div>
        <div class="series-overview-container">
            <p class="series-year-runtime">${series.year?c}, ${series.runtime} minutes per episode, rated ${series.rating}/10 </p>
            <p class="series-overview">${series.overview}</p>
        </div>
    </div>
    <div class="series-links">
        <a href="http://www.imdb.com/title/${series.imdb}" target="_blank">IMDB</a>
        <a href="https://www.themoviedb.org/tv/${series.tmdb}" target="_blank">TMDB</a>
        <a href="https://trakt.tv/shows/${series.trakt}" target="_blank">Trakt</a>
        <a href="http://thetvdb.com/?tab=series&id=${series.tvdb}" target="_blank">TVDB</a>
    </div>
</div>
<#else>
<div class="empty-set">
    <p>The series you're looking for doesn't exist.</p>
    <i class="fa fa-frown-o" aria-hidden="true"></i>
    <p>Try searching something else!</p>
</div>
</#if>
<#if suggestions?has_content >
    <h1 class="suggestions">Suggestions</h1>
    <div class="results">
        <#list suggestions as result>
            <div class="result" data-type="series" data-value="${result}">
                <p>${result}</p>
                <p class="tag">Series</p>
            </div>
        </#list>
    </div>
<#else>
    <div class="empty-set">
        <p>We couldn't find any suggestions.</p>
        <i class="fa fa-frown-o" aria-hidden="true"></i>
        <p>Try searching something else!</p>
    </div>
</#if>
<footer class="footer">
    <p>Pedro Janeiro, 2017</p>
</footer>
<script src="http://code.jquery.com/jquery-3.1.1.min.js"></script>
</body>
</html>