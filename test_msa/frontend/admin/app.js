const API = "http://localhost:8080";

let token = localStorage.getItem("admin_token") || "";
const tokenBox = document.getElementById("token-box");
const ordersBody = document.getElementById("orders-body");
const searchResults = document.getElementById("search-results");
const notifyResults = document.getElementById("notify-results");
const statusOptions = ["CREATED", "PAID", "PACKING", "SHIPPED", "DELIVERED", "CANCELLED"];

function setToken(value) {
  token = value;
  if (value) localStorage.setItem("admin_token", value);
  tokenBox.textContent = value ? `Token: ${value.slice(0, 24)}...` : "Token: -";
}

async function login() {
  const username = document.getElementById("login-username").value;
  const password = document.getElementById("login-password").value;
  const res = await fetch(`${API}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password })
  });
  if (!res.ok) {
    alert("login failed");
    return;
  }
  const data = await res.json();
  setToken(data.token);
}

async function loadOrders() {
  if (!token) return alert("login first");
  const res = await fetch(`${API}/order/orders`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  if (!res.ok) {
    alert("failed to load orders");
    return;
  }
  const list = await res.json();
  ordersBody.innerHTML = list.map(order => (
    `<tr>
      <td>#${order.id}</td>
      <td>${order.customerId}</td>
      <td>Admin</td>
      <td>
        <select class="status-select" data-id="${order.id}">
          ${statusOptions.map(s => `<option value="${s}" ${s === order.status ? "selected" : ""}>${s}</option>`).join("")}
        </select>
      </td>
      <td>₩ ${order.totalAmount}</td>
      <td><button class="ghost btn-update-status" data-id="${order.id}">Update</button></td>
    </tr>`
  )).join("");
}

async function createProduct() {
  if (!token) return alert("login first");
  const sku = `SKU-${Math.floor(Math.random() * 9999)}`;
  const res = await fetch(`${API}/catalog/products`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
    body: JSON.stringify({ sku, name: "Admin New Item", price: 88000 })
  });
  if (!res.ok) {
    alert("failed to create product");
    return;
  }
  alert("product created");
}

async function healthCheck() {
  const res = await fetch(`${API}/auth/status`);
  alert(`auth: ${res.status}`);
}

async function searchIndex() {
  if (!token) return alert("login first");
  const q = document.getElementById("admin-search").value || "";
  const res = await fetch(`${API}/search/query?q=${encodeURIComponent(q)}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  if (!res.ok) return alert("search failed");
  const list = await res.json();
  searchResults.innerHTML = list.map(item => (
    `<li><span>${item.name}</span><strong>${item.sku}</strong></li>`
  )).join("");
}

async function loadNotifications() {
  if (!token) return alert("login first");
  const res = await fetch(`${API}/notification/inbox`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  if (!res.ok) return alert("load failed");
  const list = await res.json();
  notifyResults.innerHTML = list.map(item => (
    `<li>
      <span>${item.type}</span>
      <strong>#${item.orderId}</strong>
      <button class="ghost btn-read" data-id="${item.id}">Read</button>
    </li>`
  )).join("");
}

async function createCustomer() {
  if (!token) return alert("login first");
  const name = document.getElementById("cust-name").value;
  const email = document.getElementById("cust-email").value;
  if (!name || !email) return alert("name/email required");
  const res = await fetch(`${API}/customer/customers`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
    body: JSON.stringify({ name, email })
  });
  if (!res.ok) return alert("create failed");
  alert("customer created");
}

async function updateOrderStatus(id, status) {
  if (!token) return alert("login first");
  const res = await fetch(`${API}/order/orders/${id}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
    body: JSON.stringify({ status })
  });
  if (!res.ok) return alert("update failed");
  alert("status updated");
}

if (token) setToken(token);

document.getElementById("btn-login").addEventListener("click", login);
document.getElementById("btn-load-orders").addEventListener("click", loadOrders);
document.getElementById("btn-create-product").addEventListener("click", createProduct);
document.getElementById("btn-health").addEventListener("click", healthCheck);
document.getElementById("btn-admin-search").addEventListener("click", searchIndex);
document.getElementById("btn-load-notify").addEventListener("click", loadNotifications);
document.getElementById("btn-create-customer").addEventListener("click", createCustomer);

ordersBody.addEventListener("click", (e) => {
  const btn = e.target.closest(".btn-update-status");
  if (!btn) return;
  const id = btn.dataset.id;
  const select = ordersBody.querySelector(`select[data-id="${id}"]`);
  updateOrderStatus(id, select.value);
});

notifyResults.addEventListener("click", async (e) => {
  const btn = e.target.closest(".btn-read");
  if (!btn) return;
  const id = btn.dataset.id;
  const res = await fetch(`${API}/notification/read/${id}`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` }
  });
  if (res.ok) btn.closest("li").remove();
});
