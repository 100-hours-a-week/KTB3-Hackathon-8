const receiptInput = document.getElementById("receiptInput");
const uploadBtn = document.getElementById("uploadBtn");
const receiptPreview = document.getElementById("receiptPreview");
const itemsEl = document.getElementById("items");

const totalPeopleEl = document.getElementById("totalPeople");
const totalAmountEl = document.getElementById("totalAmount");
const settlementAmountEl = document.getElementById("settlementAmount");

const API_URL = "http://98.92.106.85:8000/receipt";

let currentItems = [];
const picked = {}; // itemIndex -> true/false

uploadBtn.onclick = () => receiptInput.click();

receiptInput.onchange = async () => {
  const file = receiptInput.files[0];
  if (!file) return;

  previewImage(file);

  const data = await uploadToAI(file);
  currentItems = parseReceiptText(data.text);

  const total = currentItems.reduce(
    (s, it) => s + it.unitPrice * it.qty,
    0
  );

  totalAmountEl.textContent = formatWon(total);

  renderItemButtons(currentItems);
  updateSettlementAmount();
};

totalPeopleEl.oninput = updateSettlementAmount;

function previewImage(file) {
  const img = document.createElement("img");
  img.src = URL.createObjectURL(file);
  img.style.maxWidth = "200px";
  receiptPreview.innerHTML = "";
  receiptPreview.appendChild(img);
}

async function uploadToAI(file) {
  const form = new FormData();
  form.append("image", file);

  const res = await fetch(API_URL, { method: "POST", body: form });
  return res.json();
}

function parseReceiptText(text) {
  if (!text) return [];
  return text.split("\n").map(line => {
    const [name, price, qty] = line.split("|");
    return {
      name: name.trim(),
      unitPrice: parseInt(price),
      qty: parseInt(qty),
    };
  });
}

function renderItemButtons(items) {
  itemsEl.innerHTML = "";

  items.forEach((item, idx) => {
    picked[idx] ??= false;

    const wrap = document.createElement("div");
    wrap.style.margin = "8px 0";

    const btn = document.createElement("button");
    btn.textContent = item.name;
    btn.style.padding = "6px 10px";
    btn.style.background = picked[idx] ? "#ffd6d6" : "#eee";

    btn.onclick = () => {
      picked[idx] = !picked[idx];
      renderItemButtons(items);
      updateSettlementAmount();
    };

    const price = document.createElement("span");
    price.style.marginLeft = "10px";
    price.textContent = formatWon(item.unitPrice * item.qty);

    wrap.appendChild(btn);
    wrap.appendChild(price);
    itemsEl.appendChild(wrap);
  });
}

function updateSettlementAmount() {
  const n = parseInt(totalPeopleEl.value, 10) || 1;

  const selectedTotal = currentItems.reduce((sum, item, idx) => {
    return picked[idx] ? sum + item.unitPrice * item.qty : sum;
  }, 0);

  settlementAmountEl.textContent = formatWon(Math.floor(selectedTotal / n));
}

function formatWon(n) {
  return `${n.toLocaleString()}원`;
}
