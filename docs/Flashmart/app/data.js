/* ============================================================
   Flashmart — shared mock data + helpers   (plain JS → window.FM)
   India / ₹ / GST / Kirana distribution context
   ============================================================ */
(function () {
  // ---- currency / number helpers (Indian grouping) ----
  function inr(n, withPaise) {
    const v = Math.round((n + Number.EPSILON) * 100) / 100;
    const neg = v < 0;
    let [int, dec] = Math.abs(v).toFixed(2).split(".");
    let last3 = int.slice(-3);
    let other = int.slice(0, -3);
    if (other) last3 = "," + last3;
    other = other.replace(/\B(?=(\d{2})+(?!\d))/g, ",");
    let out = other + last3;
    if (withPaise) out += "." + dec;
    return (neg ? "-₹" : "₹") + out;
  }
  // compact: ₹4.5L, ₹1.2Cr
  function inrShort(n) {
    if (n >= 1e7) return "₹" + (n / 1e7).toFixed(n % 1e7 === 0 ? 0 : 1) + "Cr";
    if (n >= 1e5) return "₹" + (n / 1e5).toFixed(n % 1e5 === 0 ? 0 : 1) + "L";
    if (n >= 1e3) return "₹" + (n / 1e3).toFixed(n % 1e3 === 0 ? 0 : 1) + "K";
    return "₹" + n;
  }
  function num(n) { return n.toLocaleString("en-IN"); }

  // ---- product imagery (subtle striped placeholder tints) ----
  // each product carries a 2-tone accent for its placeholder chip
  const P = (id, name, brand, cat, price, gst, disc, unit, stock, tint) =>
    ({ id, name, brand, cat, price, gst, disc, unit, stock, tint });

  const PRODUCTS = [
    P("milk-powder", "Everyday Dairy Whitener", "Nestlé", "Dairy", 124, 12, 5, "400g pouch", 320, "#3f6fd6"),
    P("tea-gold", "Taj Mahal Tea Gold", "Brooke Bond", "Beverages", 285, 5, 6, "500g pack", 210, "#b5402f"),
    P("atta", "Aashirvaad Whole Wheat Atta", "ITC", "Staples", 248, 0, 4, "5kg bag", 140, "#caa12f"),
    P("oil", "Fortune Sunflower Oil", "Adani Wilmar", "Staples", 165, 5, 3, "1L pouch", 260, "#d4a017"),
    P("biscuits", "Parle-G Gold Biscuits", "Parle", "Snacks", 50, 18, 8, "Pack of 12", 540, "#d98a2b"),
    P("maggi", "Maggi 2-Minute Noodles", "Nestlé", "Snacks", 168, 18, 7, "Pack of 12", 410, "#e0a52e"),
    P("soap", "Lifebuoy Soap Bar", "HUL", "Personal Care", 132, 18, 6, "Pack of 4", 300, "#c0392b"),
    P("detergent", "Surf Excel Easy Wash", "HUL", "Home Care", 215, 18, 5, "1kg pack", 185, "#2f7fd6"),
    P("shampoo", "Clinic Plus Shampoo", "HUL", "Personal Care", 178, 18, 6, "340ml bottle", 150, "#1f8f6b"),
    P("salt", "Tata Salt Iodised", "Tata", "Staples", 28, 5, 3, "1kg pack", 600, "#3a7bd5"),
    P("coffee", "Bru Instant Coffee", "HUL", "Beverages", 320, 18, 7, "200g jar", 95, "#7a4b2b"),
    P("biscuit-cream", "Bourbon Cream Biscuits", "Britannia", "Snacks", 144, 18, 8, "Pack of 6", 280, "#8a5a2b"),
    P("chips", "Lay's Classic Salted", "PepsiCo", "Snacks", 200, 12, 9, "Pack of 10", 240, "#d4b021"),
    P("toothpaste", "Colgate MaxFresh", "Colgate", "Personal Care", 99, 18, 6, "150g tube", 320, "#c0392b"),
  ];

  // ---- areas / routes ----
  const AREAS = ["Andheri East", "Bandra West", "Dadar", "Ghatkopar", "Borivali", "Thane West", "Vashi"];

  // ---- dealers ----
  const DEALERS = [
    { id: "DLR-04", name: "Shree Balaji Distributors", owner: "Mahesh Patil", area: "Andheri East", phone: "98201 44512", orders: 86, revenue: 412000, status: "Active" },
    { id: "DLR-07", name: "Krishna Wholesale", owner: "Sunita Rao", area: "Dadar", phone: "98330 22119", orders: 64, revenue: 298500, status: "Active" },
    { id: "DLR-02", name: "Gupta Trading Co.", owner: "Anil Gupta", area: "Ghatkopar", phone: "99670 88234", orders: 52, revenue: 241200, status: "Active" },
    { id: "DLR-11", name: "Sai Enterprises", owner: "Ramesh Iyer", area: "Thane West", phone: "98677 51200", orders: 38, revenue: 176800, status: "Active" },
    { id: "DLR-09", name: "Maa Vaishno Agencies", owner: "Deepak Sharma", area: "Borivali", phone: "97690 33410", orders: 21, revenue: 94300, status: "Onboarding" },
  ];

  // ---- shopkeepers ----
  const SHOPS = [
    { id: "SHP-118", store: "Ramesh General Store", owner: "Ramesh Yadav", area: "Andheri East", phone: "99201 77345", dealer: "DLR-04", orders: 24, status: "Active" },
    { id: "SHP-092", store: "ABC Kirana Mart", owner: "Vijay Shah", area: "Andheri East", phone: "98191 22008", dealer: "DLR-04", orders: 41, status: "Active" },
    { id: "SHP-205", store: "Annapurna Provisions", owner: "Lata Joshi", area: "Dadar", phone: "98213 90876", dealer: "DLR-07", orders: 33, status: "Active" },
    { id: "SHP-156", store: "New Bombay Stores", owner: "Imran Shaikh", area: "Ghatkopar", phone: "99875 11290", dealer: "DLR-02", orders: 18, status: "Active" },
    { id: "SHP-241", store: "Sai Krupa Super Shoppe", owner: "Prakash More", area: "Thane West", phone: "98334 56701", dealer: "DLR-11", orders: 12, status: "Active" },
  ];

  // ---- employees (field onboarding staff) ----
  const EMPLOYEES = [
    { id: "EMP-21", name: "Neha Kulkarni", area: "West Zone", dealers: 10, shops: 85, status: "Active" },
    { id: "EMP-14", name: "Arjun Mehta", area: "Central Zone", dealers: 7, shops: 62, status: "Active" },
    { id: "EMP-33", name: "Pooja Nair", area: "Harbour Zone", dealers: 5, shops: 44, status: "Active" },
  ];

  // ---- seed orders (history powering KPIs / lists) ----
  // status: placed | accepted | out | delivered ; paid bool
  const orderItems = (specs) => specs.map(([pid, qty]) => {
    const p = PRODUCTS.find(x => x.id === pid);
    return { pid, name: p.name, brand: p.brand, qty, price: p.price, gst: p.gst, disc: p.disc, unit: p.unit, tint: p.tint };
  });

  const SEED_ORDERS = [
    { id: "ORD-1124", shop: "SHP-118", dealer: "DLR-04", date: "Jun 6", ts: 6, status: "delivered", paid: true,  items: orderItems([["atta",6],["oil",12],["salt",10]]) },
    { id: "ORD-1122", shop: "SHP-118", dealer: "DLR-04", date: "Jun 5", ts: 5, status: "out",       paid: true,  items: orderItems([["maggi",4],["biscuits",6]]) },
    { id: "ORD-1121", shop: "SHP-118", dealer: "DLR-04", date: "Jun 4", ts: 4, status: "placed",    paid: false, items: orderItems([["soap",5],["shampoo",3]]) },
    { id: "ORD-1119", shop: "SHP-092", dealer: "DLR-04", date: "Jun 4", ts: 4, status: "accepted",  paid: true,  items: orderItems([["tea-gold",8],["coffee",4]]) },
    { id: "ORD-1116", shop: "SHP-092", dealer: "DLR-04", date: "Jun 3", ts: 3, status: "delivered", paid: true,  items: orderItems([["detergent",10],["soap",8]]) },
    { id: "ORD-1112", shop: "SHP-205", dealer: "DLR-07", date: "Jun 3", ts: 3, status: "delivered", paid: true,  items: orderItems([["chips",10],["biscuit-cream",6]]) },
    { id: "ORD-1108", shop: "SHP-156", dealer: "DLR-02", date: "Jun 2", ts: 2, status: "delivered", paid: true,  items: orderItems([["atta",4],["milk-powder",6]]) },
  ];

  // ---- dealer stock (DLR-04) ----
  const DEALER_STOCK = [
    { pid: "atta", qty: 140, reorder: 40 },
    { pid: "oil", qty: 260, reorder: 60 },
    { pid: "salt", qty: 600, reorder: 100 },
    { pid: "maggi", qty: 18, reorder: 50 },     // low
    { pid: "biscuits", qty: 540, reorder: 120 },
    { pid: "soap", qty: 32, reorder: 60 },      // low
    { pid: "shampoo", qty: 150, reorder: 40 },
    { pid: "tea-gold", qty: 210, reorder: 50 },
  ];

  // ---- admin analytics ----
  const MONTHLY_SALES = [
    { m: "Jan", v: 286000 }, { m: "Feb", v: 312000 }, { m: "Mar", v: 358000 },
    { m: "Apr", v: 341000 }, { m: "May", v: 408000 }, { m: "Jun", v: 462000 },
  ];
  const CATEGORY_MIX = [
    { cat: "Staples", pct: 34 }, { cat: "Snacks", pct: 24 },
    { cat: "Personal Care", pct: 18 }, { cat: "Beverages", pct: 14 }, { cat: "Home Care", pct: 10 },
  ];

  // ---- compute item math (per order) ----
  function lineMath(it) {
    const base = it.price * it.qty;
    const discAmt = base * (it.disc / 100);
    const taxable = base - discAmt;
    const gstAmt = taxable * (it.gst / 100);
    return { base, discAmt, taxable, gstAmt, total: taxable + gstAmt };
  }
  function orderMath(items) {
    let sub = 0, disc = 0, gst = 0;
    items.forEach(it => { const m = lineMath(it); sub += m.base; disc += m.discAmt; gst += m.gstAmt; });
    return { sub, disc, gst, total: sub - disc + gst };
  }

  const lookups = {
    shop: id => SHOPS.find(s => s.id === id),
    dealer: id => DEALERS.find(d => d.id === id),
    product: id => PRODUCTS.find(p => p.id === id),
  };

  window.FM = {
    inr, inrShort, num,
    PRODUCTS, AREAS, DEALERS, SHOPS, EMPLOYEES, SEED_ORDERS, DEALER_STOCK,
    MONTHLY_SALES, CATEGORY_MIX, lineMath, orderMath, lookups,
    // identity of the demo shopkeeper / dealer / employee
    ME: { shop: "SHP-118", dealer: "DLR-04", employee: "EMP-21" },
  };
})();
