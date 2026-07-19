const MONTHS = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
const COLORS = ["#2563eb", "#f97316", "#14b8a6", "#a855f7", "#eab308", "#f43f5e", "#10b981"];
const DEFAULT_EXPENSES = [
    {name: "Housing", amount: 24000, color: "#4f46e5"},
    {name: "Food", amount: 12500, color: "#f59e0b"},
    {name: "Transport", amount: 6800, color: "#06b6d4"},
    {name: "Utilities", amount: 5200, color: "#8b5cf6"},
    {name: "Health", amount: 3500, color: "#ef4444"},
    {name: "Subscriptions", amount: 2300, color: "#ec4899"},
    {name: "Personal", amount: 5000, color: "#10b981"}
];

const state = {
    year: 2026,
    month: 7,
    income: 85000,
    expenses: [],
    mode: "loading"
};

const $ = (selector) => document.querySelector(selector);
const money = (value) => `৳${Math.round(Number(value || 0)).toLocaleString("en-US")}`;
const localKey = () => `monthly-planner:${state.year}-${String(state.month).padStart(2, "0")}`;

function defaultPlan() {
    return {
        year: state.year,
        month: state.month,
        income: 85000,
        expenses: DEFAULT_EXPENSES.map((expense, index) => ({...expense, id: Date.now() + index, position: index}))
    };
}

async function api(path = "", options = {}) {
    const response = await fetch(`/api/planner/${state.year}/${state.month}${path}`, {
        headers: {"Content-Type": "application/json", ...(options.headers || {})},
        ...options
    });
    if (!response.ok) {
        const payload = await response.json().catch(() => ({}));
        throw new Error(payload.message || `Request failed (${response.status})`);
    }
    return response.json();
}

function readLocal() {
    const saved = localStorage.getItem(localKey());
    if (!saved) {
        const plan = defaultPlan();
        localStorage.setItem(localKey(), JSON.stringify(plan));
        return plan;
    }
    try { return JSON.parse(saved); }
    catch { return defaultPlan(); }
}

function saveLocal() {
    localStorage.setItem(localKey(), JSON.stringify({
        year: state.year, month: state.month, income: state.income, expenses: state.expenses
    }));
}

function applyPlan(plan) {
    state.income = Number(plan.income || 0);
    state.expenses = (plan.expenses || []).map((expense, index) => ({...expense, amount: Number(expense.amount), position: expense.position ?? index}));
    render();
}

async function loadPlan() {
    document.body.classList.add("loading");
    try {
        if (state.mode === "local") throw new Error("Local mode");
        const plan = await api();
        state.mode = "server";
        applyPlan(plan);
    } catch (error) {
        state.mode = "local";
        applyPlan(readLocal());
    } finally {
        document.body.classList.remove("loading");
        updateStorageLabel();
    }
}

function updateStorageLabel() {
    const label = $("#storageLabel");
    const container = label.closest(".saved");
    if (state.mode === "server") {
        label.textContent = "Saved in Spring Boot database";
        container.classList.remove("offline");
    } else {
        label.textContent = "Saved on this device";
        container.classList.add("offline");
    }
}

function totals() {
    const total = state.expenses.reduce((sum, expense) => sum + Number(expense.amount || 0), 0);
    const remaining = state.income - total;
    const spent = state.income > 0 ? total / state.income * 100 : 0;
    const available = state.income > 0 ? remaining / state.income * 100 : 0;
    return {total, remaining, spent, available};
}

function render() {
    const {total, remaining, spent, available} = totals();
    const label = `${MONTHS[state.month - 1]} ${state.year}`;
    $("#monthLabel").textContent = label;
    $("#incomeCaption").textContent = `Total income for ${label}`;
    $("#editIncome").innerHTML = `${money(state.income)} <b>✎</b>`;
    $("#totalExpenses").textContent = money(total);
    $("#expensePercent").textContent = `${spent.toFixed(1)}% of income`;
    $("#remaining").textContent = money(remaining);
    $("#remaining").className = remaining >= 0 ? "positive" : "negative";
    $("#availablePercent").textContent = `${available.toFixed(1)}% available`;
    $("#tableTotal").textContent = money(total);
    $("#tablePercent").textContent = `${spent.toFixed(1)}%`;
    $("#donutTotal").textContent = money(total);
    $("#categoryCount").textContent = `${state.expenses.length} ${state.expenses.length === 1 ? "category" : "categories"}`;

    const rows = $("#expenseRows");
    if (!state.expenses.length) {
        rows.innerHTML = '<div class="empty-state">No expense categories yet. Add your first expense.</div>';
    } else {
        rows.innerHTML = state.expenses.map(expense => {
            const percent = state.income > 0 ? expense.amount / state.income * 100 : 0;
            return `<div class="expense-row">
                <div class="category"><i style="background:${escapeHtml(expense.color)}"></i><span>${escapeHtml(expense.name)}</span></div>
                <div class="amount">${money(expense.amount)}</div>
                <div class="percent"><span>${percent.toFixed(1)}%</span><div><i style="width:${Math.max(0, Math.min(percent, 100))}%;background:${escapeHtml(expense.color)}"></i></div></div>
                <div class="actions"><button data-edit="${expense.id}" aria-label="Edit ${escapeHtml(expense.name)}">✎</button><button class="delete" data-delete="${expense.id}" aria-label="Delete ${escapeHtml(expense.name)}">×</button></div>
            </div>`;
        }).join("");
    }

    let cursor = 0;
    const slices = state.expenses.map(expense => {
        const start = cursor;
        cursor += total > 0 ? expense.amount / total * 360 : 0;
        return `${expense.color} ${start}deg ${cursor}deg`;
    });
    $("#donut").style.background = slices.length ? `conic-gradient(${slices.join(",")})` : "#e2e8f0";
    $("#legend").innerHTML = state.expenses.map(expense => {
        const percent = state.income > 0 ? expense.amount / state.income * 100 : 0;
        return `<div><span><i style="background:${escapeHtml(expense.color)}"></i>${escapeHtml(expense.name)}</span><b>${money(expense.amount)}</b><em>${percent.toFixed(1)}%</em></div>`;
    }).join("");
}

async function updateIncome(income) {
    if (state.mode === "server") applyPlan(await api("/income", {method: "PUT", body: JSON.stringify({income})}));
    else { state.income = income; saveLocal(); render(); }
    showToast("Income updated");
}

async function addExpense(payload) {
    if (state.mode === "server") applyPlan(await api("/expenses", {method: "POST", body: JSON.stringify(payload)}));
    else {
        state.expenses.push({...payload, id: Date.now(), position: state.expenses.length});
        saveLocal(); render();
    }
    showToast("Expense added");
}

async function editExpense(id, payload) {
    if (state.mode === "server") applyPlan(await api(`/expenses/${id}`, {method: "PUT", body: JSON.stringify(payload)}));
    else {
        state.expenses = state.expenses.map(expense => String(expense.id) === String(id) ? {...expense, ...payload} : expense);
        saveLocal(); render();
    }
    showToast("Expense updated");
}

async function deleteExpense(id) {
    if (state.mode === "server") applyPlan(await api(`/expenses/${id}`, {method: "DELETE"}));
    else {
        state.expenses = state.expenses.filter(expense => String(expense.id) !== String(id));
        saveLocal(); render();
    }
    showToast("Expense deleted");
}

function openIncomeModal() {
    openModal({
        icon: "৳", title: "Update monthly income", subtitle: `${MONTHS[state.month - 1]} ${state.year}`,
        fields: [{name: "income", label: "Income amount", type: "number", value: state.income, min: 0}],
        submitLabel: "Save income",
        onSubmit: values => updateIncome(Number(values.income))
    });
}

function openExpenseModal(expense = null) {
    openModal({
        icon: expense ? "✎" : "＋",
        title: expense ? "Edit expense" : "Add an expense",
        subtitle: expense ? "Update this monthly cost category." : "Create a new monthly cost category.",
        fields: [
            {name: "name", label: "Category name", type: "text", value: expense?.name || "", placeholder: "e.g. Education"},
            {name: "amount", label: "Monthly amount", type: "number", value: expense?.amount || "", min: 0, placeholder: "0"},
            {name: "color", label: "Category color", type: "color", value: expense?.color || COLORS[state.expenses.length % COLORS.length]}
        ],
        submitLabel: expense ? "Save changes" : "Add expense",
        onSubmit: values => {
            const payload = {name: values.name.trim(), amount: Number(values.amount), color: values.color};
            return expense ? editExpense(expense.id, payload) : addExpense(payload);
        }
    });
}

function openDeleteModal(expense) {
    openModal({
        icon: "!", title: "Delete expense", subtitle: `Remove ${expense.name} from this month?`, fields: [],
        submitLabel: "Delete", danger: true, onSubmit: () => deleteExpense(expense.id)
    });
}

function openModal({icon, title, subtitle, fields, submitLabel, onSubmit, danger = false}) {
    const root = $("#modalRoot");
    root.innerHTML = `<div class="modal-backdrop"><form class="modal">
        <div class="modal-title"><div><span class="mini-icon">${icon}</span><div><h2>${escapeHtml(title)}</h2><p>${escapeHtml(subtitle)}</p></div></div><button type="button" data-close aria-label="Close">×</button></div>
        ${fields.map(field => `<label>${escapeHtml(field.label)}<input name="${field.name}" type="${field.type}" value="${escapeHtml(String(field.value ?? ""))}" ${field.placeholder ? `placeholder="${escapeHtml(field.placeholder)}"` : ""} ${field.min !== undefined ? `min="${field.min}"` : ""} required></label>`).join("")}
        <div class="modal-actions"><button type="button" data-close>Cancel</button><button type="submit" class="${danger ? "danger" : ""}">${escapeHtml(submitLabel)}</button></div>
    </form></div>`;
    const close = () => { root.innerHTML = ""; };
    root.querySelectorAll("[data-close]").forEach(button => button.addEventListener("click", close));
    root.querySelector(".modal-backdrop").addEventListener("click", event => { if (event.target.classList.contains("modal-backdrop")) close(); });
    const form = root.querySelector("form");
    form.addEventListener("submit", async event => {
        event.preventDefault();
        const submit = form.querySelector("button[type=submit]");
        submit.disabled = true;
        try {
            const values = Object.fromEntries(new FormData(form).entries());
            await onSubmit(values);
            close();
        } catch (error) {
            showToast(error.message || "Could not save", true);
            submit.disabled = false;
        }
    });
    setTimeout(() => form.querySelector("input")?.focus(), 0);
}

function changeMonth(delta) {
    state.month += delta;
    if (state.month < 1) { state.month = 12; state.year--; }
    if (state.month > 12) { state.month = 1; state.year++; }
    loadPlan();
}

function showToast(message, error = false) {
    const toast = $("#toast");
    toast.textContent = message;
    toast.className = `toast show${error ? " error" : ""}`;
    clearTimeout(showToast.timer);
    showToast.timer = setTimeout(() => toast.className = "toast", 2300);
}

function escapeHtml(value) {
    return value.replace(/[&<>'"]/g, character => ({"&":"&amp;","<":"&lt;",">":"&gt;","'":"&#39;",'"':"&quot;"})[character]);
}

$("#previousMonth").addEventListener("click", () => changeMonth(-1));
$("#nextMonth").addEventListener("click", () => changeMonth(1));
$("#editIncome").addEventListener("click", openIncomeModal);
$("#addExpense").addEventListener("click", () => openExpenseModal());
$("#expenseRows").addEventListener("click", event => {
    const editId = event.target.dataset.edit;
    const deleteId = event.target.dataset.delete;
    if (editId) openExpenseModal(state.expenses.find(expense => String(expense.id) === editId));
    if (deleteId) openDeleteModal(state.expenses.find(expense => String(expense.id) === deleteId));
});

document.addEventListener("keydown", event => {
    if (event.key === "Escape") $("#modalRoot").innerHTML = "";
});

loadPlan();
