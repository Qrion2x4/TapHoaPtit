// Đây là mẫu dữ liệu demo, bạn sẽ kết nối backend để lấy thật sau
let cart = [];

function loadCart() {
  const stored = localStorage.getItem("cart");
  cart = stored ? JSON.parse(stored) : [];
  renderCart();
}

function renderCart() {
  const tbody = document.getElementById("cart-items");
  tbody.innerHTML = "";

  let total = 0;
  cart.forEach((item, index) => {
    const subTotal = item.price * item.quantity;
    total += subTotal;

    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><img src="${item.img}" alt="${item.name}"> ${item.name}</td>
      <td>${item.price.toLocaleString()}₫</td>
      <td><input type="number" min="1" value="${item.quantity}" onchange="updateQuantity(${index}, this.value)"></td>
      <td>${subTotal.toLocaleString()}₫</td>
      <td><button onclick="removeItem(${index})">Xóa</button></td>
    `;
    tbody.appendChild(tr);
  });

  document.getElementById("total-price").textContent = total.toLocaleString() + "₫";

  if (cart.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5">Giỏ hàng trống</td></tr>`;
    document.getElementById("total-price").textContent = "0₫";
  }
}

function updateQuantity(index, qty) {
  cart[index].quantity = parseInt(qty);
  localStorage.setItem("cart", JSON.stringify(cart));
  renderCart();
}

function removeItem(index) {
  cart.splice(index, 1);
  localStorage.setItem("cart", JSON.stringify(cart));
  renderCart();
}

function clearCart() {
  cart = [];
  localStorage.removeItem("cart");
  renderCart();
}

function checkout() {
  if (cart.length === 0) {
    alert("Giỏ hàng trống!");
    return;
  }
  // Chuyển tới trang thanh toán hoặc gọi API backend
  window.location.href = "checkout.html";
}

// Khởi động
loadCart();
