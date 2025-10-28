function login() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const result = document.getElementById("result");

  if (!username || !password) {
    result.innerText = "Vui lòng nhập đầy đủ thông tin!";
    result.style.color = "red";
    return;
  }

  const user = { username, password };

  fetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(user),
  })
    .then(res => res.text())
    .then(msg => {
      result.innerText = msg;
      if (msg.includes("thành công")) {
        result.style.color = "green";
        localStorage.setItem("username", username);
        setTimeout(() => (window.location.href = "/"), 1000);
      } else {
        result.style.color = "red";
      }
    })
    .catch(() => {
      result.innerText = "Lỗi kết nối server!";
      result.style.color = "red";
    });
}
