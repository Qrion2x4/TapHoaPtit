const API_URL = "http://localhost:8080/api";

async function login() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  const res = await fetch(`${API_URL}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (res.ok) {
    alert("Đăng nhập thành công!");
    window.location.href = "home.html";
  } else {
    alert("Sai tài khoản hoặc mật khẩu!");
  }
}

async function register() {
  const username = document.getElementById("regUser").value;
  const password = document.getElementById("regPass").value;

  const res = await fetch(`${API_URL}/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (res.ok) {
    alert("Đăng ký thành công!");
    hideRegister();
  } else {
    alert("Tên đăng nhập đã tồn tại!");
  }
}

function showRegister() {
  document.querySelector(".login-container").classList.add("hidden");
  document.getElementById("register").classList.remove("hidden");
}

function hideRegister() {
  document.querySelector(".login-container").classList.remove("hidden");
  document.getElementById("register").classList.add("hidden");
}
