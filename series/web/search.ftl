<!DOCTYPE html>
<html>
<head>
    <title>Please, work</title>
    <link rel="stylesheet" type="text/css" href="css/style.css" />
    <link rel="stylesheet" type="text/css" href="css/search.css" />
    <link rel="stylesheet" type="text/css" href="css/font-awesome.min.css" />
</head>
<body>
    <nav class="nav">
        <div class="nav-item home">
            <p><a href="/">Home</a></p>
        </div>
        <div class="nav-item nav-search">
            <form method="get" action="search">
                <input type="text" name="query" autocomplete="off" placeholder="Search..." />
            </form>
        </div>
        <div class="nav-item about">
            <p><a href="/about">About</a></p>
        </div>
    </nav>
    <#if series?has_content>
        <div class="results">
            <#list series as result>
                <div class="result" data-type="series" data-value="${result}">
                    <p>${result}</p>
                    <p class="tag">Series</p>
                </div>
            </#list>
        </div>
    <#elseif searchRatings?has_content || searchNetworks?has_content || searchGenres?has_content || searchSeries?has_content || searchPeople?has_content>
        <div class="results">
            <#if searchSeries?has_content>
                <#list searchSeries as result>
                    <div class="result" data-type="series" data-value="${result}">
                        <p>${result}</p>
                        <p class="tag">Series</p>
                    </div>
                </#list>
            </#if>
            <#if searchPeople?has_content>
                <#list searchPeople as result>
                    <div class="result" data-type="people" data-value="${result}">
                        <p>${result}</p>
                        <p class="tag">Person</p>
                    </div>
                </#list>
            </#if>
            <#if searchRatings?has_content>
                <#list searchRatings as result>
                    <div class="result" data-type="rating" data-value="${result}">
                        <p>Rating ${result} and higher?</p>
                    </div>
                </#list>
            </#if>
            <#if searchNetworks?has_content>
                <#list searchNetworks as result>
                    <div class="result" data-type="network" data-value="${result}">
                        <p>${result}</p>
                        <p class="tag">Network</p>
                    </div>
                </#list>
            </#if>
            <#if searchGenres?has_content>
                <#list searchGenres as result>
                    <div class="result" data-type="genre" data-value="${result}">
                        <p>${result}</p>
                        <p class="tag">Genre</p>
                    </div>
                </#list>
            </#if>
        </div>
    <#else>
        <div class="empty-set">
            <p>We couldn't find any results.</p>
            <i class="fa fa-frown-o" aria-hidden="true"></i>
            <p>Try searching something else!</p>
        </div>
    </#if>
    <footer class="footer">
        <p>Pedro Janeiro, 2017</p>
    </footer>
    <script src="http://code.jquery.com/jquery-3.1.1.min.js"></script>
    <script type="application/javascript">
        $(".result").click(function(){
            var type = $(this).attr("data-type");
            var value = encodeURIComponent($(this).attr("data-value"));
            window.location.href = "/"+type+"?query="+value;
        });
    </script>
</body>
</html>