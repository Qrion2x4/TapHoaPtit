const username = localStorage.getItem("username");
if (!username) {
  alert("Vui lòng đăng nhập để xem giỏ hàng!");
  window.location.href = "login.html";
}

fetch(`/api/cart/${username}`)
  .then(res => res.json())
  .then(data => {
    const tbody = document.querySelector("#cartTable tbody");
    let total = 0;

    data.forEach(item => {
      const row = document.createElement("tr");
      const price = item.product.price * item.quantity;
      total += price;

      row.innerHTML = `
        <td>${item.product.name}</td>
        <td>${item.quantity}</td>
        <td>${price.toLocaleString()}₫</td>
        <td><button onclick="removeItem(${item.id})">Xóa</button></td>
      `;
      tbody.appendChild(row);
    });

    document.getElementById("totalPrice").innerText = "Tổng cộng: " + total.toLocaleString() + "₫";
  })
  .catch(() => {
    document.querySelector("#cartTable tbody").innerHTML = "<tr><td colspan='4'>Không thể tải giỏ hàng!</td></tr>";
  });

function removeItem(id) {
  fetch(`/api/cart/${id}`, { method: "DELETE" })
    .then(res => res.text())
    .then(msg => {
      alert(msg);
      location.reload();
    })
    .catch(() => alert("Lỗi khi xóa sản phẩm!"));
}
