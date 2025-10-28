function register() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const confirm = document.getElementById("confirm").value;
  const result = document.getElementById("result");

  if (!username || !password || !confirm) {
    result.innerText = "Vui lòng nhập đầy đủ thông tin!";
    result.style.color = "red";
    return;
  }

  if (password !== confirm) {
    result.innerText = "Mật khẩu nhập lại không khớp!";
    result.style.color = "red";
    return;
  }

  const user = { username, password };

  fetch("/api/auth/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(user),
  })
    .then(res => res.text())
    .then(msg => {
      result.innerText = msg;
      if (msg.includes("thành công")) {
        result.style.color = "green";
        setTimeout(() => (window.location.href = "login.html"), 1000);
      } else {
        result.style.color = "red";
      }
    })
    .catch(() => {
      result.innerText = "Lỗi kết nối server!";
      result.style.color = "red";
    });
}
