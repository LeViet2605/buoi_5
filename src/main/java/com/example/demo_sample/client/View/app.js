
    const API_BASE = "http://localhost:8080/api";
    let accessToken = null;
    let currentPage = 0;
    let totalPages = 1;
    let isAdmin = false;

    const authScreen = document.getElementById("authScreen");
    const mainScreen = document.getElementById("mainScreen");
    const taskTabBtn = document.getElementById("taskTabBtn");
    const userTabBtn = document.getElementById("userTabBtn");
    const taskTab = document.getElementById("taskTab");
    const userTab = document.getElementById("userTab");

    // ===== REGISTER =====
    document.getElementById("registerForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const email = registerEmail.value;
    const password = registerPassword.value;
    const res = await fetch(`${API_BASE}/account/register`, {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify({ email, password })
});
    const data = await res.json(); // <-- đọc JSON từ backend
    if (res.ok) {
    alert(data.message || "Đăng ký thành công!");
} else {
    alert(data.error || "gmail đã tồn tại");
}
});

    // ===== LOGIN =====
    document.getElementById("loginForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const email = loginEmail.value;
    const password = loginPassword.value;
    const res = await fetch(`${API_BASE}/account/login`, {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify({ email, password })
});
    const data = await res.json();
    //JS lưu tk vào và sau này tự động dùng
    if (res.ok) {
    // fix: đọc token và email đúng
    accessToken = data.data.accessToken;
    isAdmin = data.data.email.toLowerCase() === "admin@gmail.com";

    authScreen.classList.add("hidden");
    mainScreen.classList.remove("hidden");

    if (isAdmin) userTabBtn.classList.remove("hidden");
    else {
    userTabBtn.classList.add("hidden");
    userTab.classList.add("hidden");
}

    loadTasks();
} else {
    alert(data.message || data.error || "Đăng nhập thất bại");
}
});


    // ===== LOGOUT =====
    document.getElementById("logoutBtn").addEventListener("click", async () => {
    await fetch(`${API_BASE}/account/logout`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${accessToken}` }
    });
    accessToken = null;
    taskList.innerHTML = "";
    userList.innerHTML = "";
    mainScreen.classList.add("hidden");
    authScreen.classList.remove("hidden");
    userTabBtn.classList.add("hidden");
    userTab.classList.add("hidden");
    alert("Đăng xuất thành công");
});

    // ===== Helper: render 1 task =====
    function renderTaskUI(t, container) {
    container.innerHTML = `
        <div class="task">
            <b>ID:</b> ${t.taskId}<br>
            <b>Requirement:</b> ${t.requirementName}<br>
            <b>Assignee:</b> ${t.assignee}<br>
            <b>Reviewer:</b> ${t.reviewer}<br>
            <b>TaskTypeId:</b> ${t.taskTypeId}<br>
            <b>Date:</b> ${t.date}<br>
            <b>Plan:</b> ${t.planFrom} - ${t.planTo}<br>
            <div class="task-actions">
                <button onclick="editTask(${t.taskId})">✏️ Sửa</button>
                <button onclick="deleteTask(${t.taskId})">🗑️ Xóa</button>
            </div>
        </div>`;
}

    // ===== Task CRUD =====
    document.getElementById("taskForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const id = taskId.value;
    const body = {
    requirementName: requirementName.value,
    assignee: assignee.value,
    reviewer: reviewer.value,
    taskTypeId: parseInt(taskTypeId.value),
    date: date.value,
    planFrom: parseFloat(planFrom.value),
    planTo: parseFloat(planTo.value)
};
    let url = `${API_BASE}/tasks`, method = "POST";
    if (id) { url = `${API_BASE}/tasks/${id}`; method = "PUT"; }
    const res = await fetch(url, {
    method,
    headers: {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${accessToken}`
},
    body: JSON.stringify(body)
});
    const data = await res.json();
    if (res.ok) {
    alert(data.message);
    resetTaskForm();
    loadTasks(currentPage);
    document.getElementById("searchForm").dispatchEvent(new Event("submit"));
} else alert(JSON.stringify(data));
});

    document.getElementById("cancelEditBtn").addEventListener("click", resetTaskForm);

    async function loadTasks(page = 0) {
    const res = await fetch(`${API_BASE}/tasks?page=${page}&size=2`, {
    headers: { "Authorization": `Bearer ${accessToken}` }
});
    const data = await res.json();
    const tasks = data.content || [];
    currentPage = data.number;
    totalPages = data.totalPages;

    taskList.innerHTML = tasks.map(t => `
        <div class="task">
            <b>ID:</b> ${t.taskId}<br>
            <b>Requirement:</b> ${t.requirementName}<br>
            <b>Assignee:</b> ${t.assignee}<br>
            <b>Reviewer:</b> ${t.reviewer}<br>
            <b>TaskTypeId:</b> ${t.taskTypeId}<br>
            <b>Date:</b> ${t.date}<br>
            <b>Plan:</b> ${t.planFrom} - ${t.planTo}<br>
            <div class="task-actions">
                <button onclick="editTask(${t.taskId})">✏️ Sửa</button>
                <button onclick="deleteTask(${t.taskId})">🗑️ Xóa</button>
            </div>
        </div>`).join("");
    renderPagination();
}

    function renderPagination() {
    const pagination = document.getElementById("pagination");
    let buttons = `<button onclick="changePage(${currentPage-1})" class="${currentPage===0?'disabled':''}">«</button>`;
    for(let i=0;i<totalPages;i++){
    buttons += `<button onclick="changePage(${i})" class="${i===currentPage?'active':''}">${i+1}</button>`;
}
    buttons += `<button onclick="changePage(${currentPage+1})" class="${currentPage===totalPages-1?'disabled':''}">»</button>`;
    pagination.innerHTML = buttons;
}

    function changePage(page){ if(page<0||page>=totalPages)return; loadTasks(page); }

    async function editTask(id){
    const res = await fetch(`${API_BASE}/tasks/${id}`, { headers:{ "Authorization": `Bearer ${accessToken}` } });
    const t = await res.json();
    taskId.value = t.taskId;
    requirementName.value = t.requirementName;
    assignee.value = t.assignee;
    reviewer.value = t.reviewer;
    taskTypeId.value = t.taskTypeId;
    date.value = t.date;
    planFrom.value = t.planFrom;
    planTo.value = t.planTo;
    document.getElementById("taskFormTitle").innerText="Cập nhật Task";
    cancelEditBtn.classList.remove("hidden");
}

    async function deleteTask(id){
    if(!confirm("Bạn có chắc muốn xóa task này?")) return;
    const res = await fetch(`${API_BASE}/tasks/${id}`, {
    method:"DELETE",
    headers:{ "Authorization": `Bearer ${accessToken}` }
});
    if(res.ok){
    alert("Xóa task thành công");
    loadTasks(currentPage);
    document.getElementById("searchForm").dispatchEvent(new Event("submit"));
} else{ const data = await res.json(); alert(data.error || "Xóa thất bại");}
}

    function resetTaskForm(){
    taskForm.reset();
    taskId.value = "";
    document.getElementById("taskFormTitle").innerText="Tạo Task";
    cancelEditBtn.classList.add("hidden");
}

    // ===== SEARCH TASK =====
    document.getElementById("searchForm").addEventListener("submit", async (e)=>{
    e.preventDefault();
    const keyword = searchKeyword.value.trim();
    if (!keyword) return;
    let url = "";
    if (!isNaN(keyword)) {
    url = `${API_BASE}/tasks/search?id=${keyword}`;
} else {
    url = `${API_BASE}/tasks/search?requirementName=${encodeURIComponent(keyword)}`;
}
    const res = await fetch(url, {
    headers:{ "Authorization": `Bearer ${accessToken}` }
});
    const container = document.getElementById("searchResult");
    if(res.ok){
    const data = await res.json();
    const tasks = data.content || (Array.isArray(data) ? data : [data]);
    if(tasks.length > 0){
    container.innerHTML = tasks.map(t => `
                    <div class="task">
                        <b>ID:</b> ${t.taskId}<br>
                        <b>Requirement:</b> ${t.requirementName}<br>
                        <b>Assignee:</b> ${t.assignee}<br>
                        <b>Reviewer:</b> ${t.reviewer}<br>
                        <b>TaskTypeId:</b> ${t.taskTypeId}<br>
                        <b>Date:</b> ${t.date}<br>
                        <div class="task-actions">
                            <button onclick="editTask(${t.taskId})">✏️ Sửa</button>
                            <button onclick="deleteTask(${t.taskId})">🗑️ Xóa</button>
                        </div>
                    </div>
                `).join("");
} else {
    container.innerHTML = "<p>Không tìm thấy Task nào</p>";
}
} else {
    container.innerHTML = `<p style="color:red;">❌ Không tìm thấy task</p>`;
}
});

    // ===== Count All Tasks =====
    document.getElementById("countAllBtn").addEventListener("click", async () => {
    const res = await fetch(`${API_BASE}/tasks/count-all`, {
    headers: { "Authorization": `Bearer ${accessToken}` }
});
    const data = await res.json();
    const resultDiv = document.getElementById("countAllResult");

    if (res.ok) {
    let text = "📊 Count All Status:\n";
    if (Array.isArray(data)) {
    data.forEach(item => {
    text += `TypeId ${item.typeId} (${item.typeName}): Count ${item.count}\n`;
});
} else {
    text += "❌ Dữ liệu trả về không đúng dạng mảng";
}
    resultDiv.textContent = text;
}
    else {
    resultDiv.textContent = data.error || "❌ Lỗi khi lấy thống kê";
}
});

    // ===== USERS (Admin only) =====
    async function loadUsers(){
    try {
    const res = await fetch(`${API_BASE}/account`, { headers:{ "Authorization": `Bearer ${accessToken}` } });
    if(!res.ok) throw new Error("Lấy danh sách user thất bại");

    const result = await res.json();
    const users = Array.isArray(result.data) ? result.data : [];

    userList.innerHTML = users.map(u => `
            <div class="user">
                <b>ID:</b> ${u.id} <br>
                <b>Email:</b> ${u.email} <br>
                <div class="user-actions">
                    <button onclick="deleteUser(${u.id})">🗑️ Xóa</button>
                </div>
            </div>
        `).join("");

} catch(err) {
    userList.innerHTML = `<p style="color:red;">${err.message}</p>`;
}
}


    async function deleteUser(id){
    if(!confirm("Bạn có chắc muốn xóa user này?")) return;
    try {
    const res = await fetch(`${API_BASE}/account/${id}`,{
    method:"DELETE",
    headers:{ "Authorization": `Bearer ${accessToken}` }
});
    if(!res.ok) throw new Error("Xóa thất bại");
    alert("Xóa user thành công");
    await loadUsers();
} catch(err) {
    alert(err.message);
}
}

    // ===== Tabs =====
    taskTabBtn.addEventListener("click", ()=>{
    taskTabBtn.classList.add("active");
    userTabBtn.classList.remove("active");
    taskTab.classList.remove("hidden");
    userTab.classList.add("hidden");
});

    userTabBtn.addEventListener("click", async ()=>{
    taskTabBtn.classList.remove("active");
    userTabBtn.classList.add("active");
    taskTab.classList.add("hidden");
    userTab.classList.remove("hidden");

    if(!isAdmin) return;
    await loadUsers();
});

