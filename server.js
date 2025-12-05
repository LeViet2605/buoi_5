const express = require("express");
const fs = require("fs");
const path = require("path");

const app = express();

// Static folder (View chứa CSS + JS)
app.use(express.static(
    path.join(__dirname, "src/main/java/com/example/demo_sample/client/View")
));

app.get("/view", (req, res) => {
    const filePath = path.join(
        __dirname,
        "src/main/java/com/example/demo_sample/client/View/View.html"
    );

    let html = fs.readFileSync(filePath, "utf8");

    // Dynamic data
    html = html.replace("{{username}}", "Lee-Dynamic");
    html = html.replace("{{time}}", new Date().toLocaleString());

    res.send(html);
});

app.listen(3000, () => {
    console.log("Server http://localhost:3000/view");
});
