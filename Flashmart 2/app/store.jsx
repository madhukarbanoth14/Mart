/* ============================================================
   Flashmart — global store (live cross-role state)
   → window.useStore, window.StoreProvider
   ============================================================ */
const StoreCtx = React.createContext(null);
const useStore = () => React.useContext(StoreCtx);

let _seq = 1130;
const nextId = () => "ORD-" + (++_seq);

function StoreProvider({ children, role, setRole }) {
  const F = window.FM;
  const [orders, setOrders] = React.useState(() => F.SEED_ORDERS.map(o => ({ ...o })));
  const [cart, setCart] = React.useState({});            // { pid: qty }
  const [dealers, setDealers] = React.useState(() => F.DEALERS.map(d => ({ ...d })));
  const [shops, setShops] = React.useState(() => F.SHOPS.map(s => ({ ...s })));
  const [toast, setToast] = React.useState(null);
  const [flash, setFlash] = React.useState(null);        // id of order to highlight (live ping)

  const notify = (msg, kind = "ok") => {
    setToast({ msg, kind, id: Date.now() });
    clearTimeout(notify._t); notify._t = setTimeout(() => setToast(null), 2600);
  };
  const ping = (id) => { setFlash(id); setTimeout(() => setFlash(f => f === id ? null : f), 2400); };

  // ---- cart ----
  const cartCount = Object.values(cart).reduce((a, b) => a + b, 0);
  const cartItems = Object.entries(cart).map(([pid, qty]) => {
    const p = F.lookups.product(pid);
    return { pid, name: p.name, brand: p.brand, qty, price: p.price, gst: p.gst, disc: p.disc, unit: p.unit, tint: p.tint };
  });
  const cartTotals = F.orderMath(cartItems);
  const addToCart = (pid, qty = 1) => { setCart(c => ({ ...c, [pid]: (c[pid] || 0) + qty })); notify("Added to cart"); };
  const setQty = (pid, qty) => setCart(c => { const n = { ...c }; if (qty <= 0) delete n[pid]; else n[pid] = qty; return n; });
  const clearCart = () => setCart({});

  // ---- orders ----
  function placeOrder() {
    const items = cartItems.map(it => ({ ...it }));
    const id = nextId();
    const order = { id, shop: F.ME.shop, dealer: F.ME.dealer, date: "Jun 8", ts: 8,
      status: "placed", paid: true, items, fresh: true };
    setOrders(o => [order, ...o]);
    clearCart();
    return id;
  }
  const advance = (id, status, msg) => {
    setOrders(os => os.map(o => o.id === id ? { ...o, status } : o));
    ping(id);
    if (msg) notify(msg);
  };
  const dealerAccept  = (id) => advance(id, "accepted", "Order accepted — shopkeeper notified");
  const dealerOut     = (id) => advance(id, "out", "Marked out for delivery");
  const dealerDeliver = (id) => advance(id, "delivered", "Delivered — payment settled");

  // ---- onboarding ----
  const addDealer = (d) => { setDealers(x => [{ ...d, fresh: true }, ...x]); notify("Dealer onboarded"); };
  const addShop = (s) => { setShops(x => [{ ...s, fresh: true }, ...x]); notify("Shopkeeper onboarded"); };

  // ---- derived selectors ----
  const myOrders = orders.filter(o => o.shop === F.ME.shop);
  const dealerOrders = orders.filter(o => o.dealer === F.ME.dealer);
  const orderTotal = (o) => F.orderMath(o.items).total;

  // admin KPIs (baseline + live)
  const BASE = { revenue: 2167000, orders: 320, dealers: 15, shops: 280 };
  const liveRevenue = orders.filter(o => o.fresh && o.paid).reduce((a, o) => a + orderTotal(o), 0);
  const liveOrders = orders.filter(o => o.fresh).length;
  const liveDealers = dealers.filter(d => d.fresh).length;
  const liveShops = shops.filter(s => s.fresh).length;
  const kpis = {
    revenue: BASE.revenue + liveRevenue,
    orders: BASE.orders + liveOrders,
    dealers: BASE.dealers + liveDealers,
    shops: BASE.shops + liveShops,
    live: { revenue: liveRevenue, orders: liveOrders, dealers: liveDealers, shops: liveShops },
  };

  const value = {
    role, setRole, F,
    orders, myOrders, dealerOrders, orderTotal, flash,
    cart, cartItems, cartCount, cartTotals, addToCart, setQty, clearCart,
    placeOrder, dealerAccept, dealerOut, dealerDeliver,
    dealers, shops, addDealer, addShop,
    kpis, toast, notify,
  };
  return <StoreCtx.Provider value={value}>{children}</StoreCtx.Provider>;
}

/* ---------- toast renderer (mount inside any frame) ---------- */
function Toast() {
  const { toast } = useStore();
  if (!toast) return null;
  const colorMap = { ok: "var(--ink)", pos: "var(--pos)", warn: "var(--warn)" };
  return (
    <div style={{ position: "absolute", left: "50%", bottom: 104, transform: "translateX(-50%)", zIndex: 90,
      background: colorMap[toast.kind] || "var(--ink)", color: "#fff", padding: "11px 18px", borderRadius: 13,
      fontSize: 13.5, fontWeight: 600, boxShadow: "var(--sh-xl)", display: "flex", alignItems: "center", gap: 9,
      whiteSpace: "nowrap", animation: "fmRise .3s cubic-bezier(.2,.8,.2,1) both", maxWidth: "84%" }}>
      <Icon name="check" size={16} stroke={3} /> {toast.msg}
    </div>
  );
}

Object.assign(window, { StoreProvider, useStore, Toast });
