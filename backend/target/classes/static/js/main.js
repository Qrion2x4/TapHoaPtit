// ----- Lấy danh sách sản phẩm -----
fetch('/api/products')
  .then(res => res.json())
  .then(data => {
    const list = document.getElementById('product-list');
    list.innerHTML = '';
    data.forEach(p => {
      const item = document.createElement('div');
      item.className = 'product';
      item.innerHTML = `
        <img src="https://via.placeholder.com/200x150?text=${p.name}" alt="${p.name}">
        <div class="name">${p.name}</div>
        <div class="price">${p.price.toLocaleString()}₫</div>
        <button onclick="addToCart(${p.id})">🛒 Thêm vào giỏ</button>
      `;
      list.appendChild(item);
    });
  })
  .catch(() => {
    document.getElementById('product-list').innerHTML = "<p>Lỗi khi tải sản phẩm.</p>";
  });

// ----- Kiểm tra đăng nhập -----
const userArea = document.getElementById("userArea");
const username = localStorage.getItem("username");

if (username) {
  userArea.innerHTML = `
    <div class="dropdown">
      <button class="account-btn">Tài khoản ⌄</button>
      <div class="dropdown-content">
        <a href="cart.html">Giỏ hàng</a>
        <a href="#" onclick="logout()">Đăng xuất</a>
      </div>
    </div>
  `;
} else {
  userArea.innerHTML = `
    <button id="loginBtn" onclick="window.location.href='login.html'">Đăng nhập</button>
  `;
}

// ----- Thêm vào giỏ hàng -----
function addToCart(productId) {
  if (!username) {
    alert("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!");
    return;
  }

  fetch(`/api/cart/add?username=${username}&productId=${productId}&quantity=1`, {
    method: "POST",
  })
    .then(res => res.text())
    .then(msg => alert(msg))
    .catch(() => alert("Lỗi khi thêm vào giỏ hàng!"));
}

// ----- Đăng xuất -----
function logout() {
  localStorage.removeItem("username");
  window.location.reload();
}
