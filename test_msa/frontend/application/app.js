const API = "http://localhost:8081";
let token = localStorage.getItem("app_token") || "";
const tokenBox = document.getElementById("app-token");
const cards = document.getElementById("product-cards");

function setToken(value) {
  token = value;
  if (value) localStorage.setItem("app_token", value);
  tokenBox.textContent = value ? `Token: ${value.slice(0, 24)}...` : "Token: -";
}

async function login() {
  const res = await fetch(`${API}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: "customer", password: "customer1234" })
  });
  if (!res.ok) {
    alert("login failed");
    return;
  }
  const data = await res.json();
  setToken(data.token);
}

async function loadProducts() {
  if (!token) return alert("login first");
  const res = await fetch(`${API}/catalog/products`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  if (!res.ok) {
    alert("failed to load products");
    return;
  }
  const list = await res.json();
  renderProducts(list);
}

async function searchIndex() {
  if (!token) return alert("login first");
  const q = document.getElementById("app-search").value || "";
  const res = await fetch(`${API}/search/query?q=${encodeURIComponent(q)}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  if (!res.ok) return alert("search failed");
  const list = await res.json();
  renderProducts(list);
}

function renderProducts(list) {
  cards.innerHTML = list.map((p, i) => (
    `<article class="product">
      <div class="tag">${i % 2 === 0 ? "New" : "Hot"}</div>
      <div class="thumb t${(i % 4) + 1}"></div>
      <h4>${p.name}</h4>
      <p>${p.sku}</p>
      <span class="price">₩ ${p.price}</span>
    </article>`
  )).join("");
}

async function checkout() {
  if (!token) return alert("login first");
  const res = await fetch(`${API}/order/orders`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
    body: JSON.stringify({ customerId: 1, totalAmount: 129000 })
  });
  if (!res.ok) {
    alert("checkout failed");
    return;
  }
  alert("order created");
}

async function joinCustomer() {
  if (!token) return alert("login first");
  const name = document.getElementById("app-name").value;
  const email = document.getElementById("app-email").value;
  if (!name || !email) return alert("name/email required");
  const res = await fetch(`${API}/customer/customers`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
    body: JSON.stringify({ name, email })
  });
  if (!res.ok) return alert("join failed");
  alert("joined");
}

if (token) setToken(token);

document.getElementById("btn-app-login").addEventListener("click", login);
document.getElementById("btn-load-products").addEventListener("click", loadProducts);
document.getElementById("btn-checkout").addEventListener("click", checkout);
document.getElementById("btn-app-search").addEventListener("click", searchIndex);
document.getElementById("btn-join").addEventListener("click", joinCustomer);
