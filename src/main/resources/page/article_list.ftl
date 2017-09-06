<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>文章列表</title>
</head>
<body>
<#list list as obj>
    <article>
        <title>#{obj.title}</title>
        <p></p>
    </article>
</#list>
</body>
</html>