<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>JSON展示页面</title>
</head>
  <body>
<script>
const data={
"bb": "1.0",
"nr": "更新啦",
"url": "https://qq.com"
};
document.body.innerText=JSON.stringify(data, null, 2);
</script>
</body>
</html>