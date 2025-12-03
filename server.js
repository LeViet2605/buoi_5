const express = require("express");
const app = express();
const path = require("path");

// STATIC folder trỏ vào View
app.use(express.static(path.join(__dirname, "src/main/java/com/example/demo_sample/client/View")));

app.get("/view", (req, res) => {
    res.sendFile(path.join(__dirname, "src/main/java/com/example/demo_sample/client/View/View.html"));
});

app.listen(3000, () => {
    console.log("Server đang chạy: http://localhost:3000/view");
});
