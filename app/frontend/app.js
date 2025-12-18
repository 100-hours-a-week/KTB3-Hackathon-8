const express = require('express');
const app = express();
const port = process.env.PORT || 3000;

app.use(express.static(__dirname));

app.get('/', (req, res) => {
    res.redirect('/auth/signin.html'); // 기본 경로로 접속 시 /auth/signin.html로 리다이렉트
});

app.listen(port, () => {
    console.log(`server is listening at localhost:${port}`);
});