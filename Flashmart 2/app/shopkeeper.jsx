/* ============================================================
   Flashmart — Shopkeeper app (mobile)
   ============================================================ */
function ShopkeeperApp() {
  const S = useStore(); const F = S.F;
  const me = F.lookups.shop(F.ME.shop);
  const [tab, setTab] = React.useState("home");
  const [route, setRoute] = React.useState(null);          // {name, id}
  const [cat, setCat] = React.useState("All");
  const [q, setQ] = React.useState("");
  const [pay, setPay] = React.useState("upi");
  const [paying, setPaying] = React.useState(false);
  const [invoiceOf, setInvoiceOf] = React.useState(null);
  const scrollRef = React.useRef(null);
  const go = (name, id) => { setRoute({ name, id }); if (scrollRef.current) scrollRef.current.scrollTop = 0; };
  const back = () => setRoute(null);

  React.useEffect(() => { if (scrollRef.current) scrollRef.current.scrollTop = 0; }, [tab]);

  const cats = ["All", ...Array.from(new Set(F.PRODUCTS.map(p => p.cat)))];
  const products = F.PRODUCTS.filter(p =>
    (cat === "All" || p.cat === cat) &&
    (!q || (p.name + p.brand).toLowerCase().includes(q.toLowerCase())));

  const nav = (
    <BottomNav active={tab} onChange={(t) => { setRoute(null); setTab(t); }}
      items={[
        { id: "home", icon: "home", label: "Home" },
        { id: "products", icon: "grid", label: "Products" },
        { id: "orders", icon: "bag", label: "Orders", badge: S.myOrders.filter(o => o.status !== "delivered").length || null },
        { id: "profile", icon: "user", label: "Profile" },
      ]} />
  );

  /* ---------------- ROUTES (full-screen pushed) ---------------- */
  if (route) {
    if (route.name === "cart") return <CartScreen S={S} back={back} go={go} scrollRef={scrollRef} />;
    if (route.name === "payment") return <PaymentScreen S={S} back={back} pay={pay} setPay={setPay} paying={paying}
      onPay={() => { setPaying(true); setTimeout(() => { const id = S.placeOrder(); setPaying(false); go("success", id); }, 1500); }} />;
    if (route.name === "success") return <SuccessScreen S={S} id={route.id}
      onTrack={() => go("track", route.id)} onInvoice={() => { setInvoiceOf(route.id); }} onDone={() => { setRoute(null); setTab("orders"); }} invoiceOf={invoiceOf} closeInvoice={() => setInvoiceOf(null)} />;
    if (route.name === "order") return <OrderDetail S={S} id={route.id} back={back} go={go} scrollRef={scrollRef}
      onInvoice={() => setInvoiceOf(route.id)} invoiceOf={invoiceOf} closeInvoice={() => setInvoiceOf(null)} />;
    if (route.name === "track") return <TrackScreen S={S} id={route.id} back={back} scrollRef={scrollRef} />;
  }

  /* ---------------- TABS ---------------- */
  let body, header;
  if (tab === "home") {
    header = (
      <AppHeader pad title={me.store}
        kicker={greeting() + ", " + me.owner.split(" ")[0]}
        right={<>
          <IconBtn name="bell" badge={1} />
          <Avatar name={me.owner} />
        </>} />
    );
    body = <ShopHome S={S} me={me} go={go} setTab={setTab} />;
  } else if (tab === "products") {
    header = (
      <div style={{ padding: "8px 20px 12px" }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 14 }}>
          <div style={{ fontSize: 25, fontWeight: 700, letterSpacing: "-.025em" }}>Catalog</div>
          <IconBtn name="cart" badge={S.cartCount || null} onClick={() => go("cart")} />
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 10, height: 46, padding: "0 14px",
          background: "var(--surface)", borderRadius: 13, border: "1px solid var(--line)", boxShadow: "var(--sh-sm)" }}>
          <Icon name="search" size={19} color="var(--ink-4)" />
          <input value={q} onChange={e => setQ(e.target.value)} placeholder="Search products or brands"
            style={{ flex: 1, border: "none", outline: "none", background: "transparent", fontFamily: "inherit", fontSize: 15 }} />
        </div>
        <div className="fm-scroll" style={{ display: "flex", gap: 8, overflowX: "auto", marginTop: 12, paddingBottom: 2 }}>
          {cats.map(c => (
            <button key={c} className="tap" onClick={() => setCat(c)}
              style={{ flexShrink: 0, padding: "8px 14px", borderRadius: 11, border: "1px solid",
                borderColor: c === cat ? "transparent" : "var(--line-2)", cursor: "pointer",
                background: c === cat ? "var(--ink)" : "var(--surface)", color: c === cat ? "#fff" : "var(--ink-2)",
                fontSize: 13, fontWeight: 600 }}>{c}</button>
          ))}
        </div>
      </div>
    );
    body = <ProductList S={S} products={products} go={go} />;
  } else if (tab === "orders") {
    body = <OrdersTab S={S} go={go} />;
    header = <AppHeader pad title="My Orders" subtitle={me.store} right={<IconBtn name="cart" badge={S.cartCount || null} onClick={() => go("cart")} />} />;
  } else {
    body = <ShopProfile S={S} me={me} />;
    header = <AppHeader pad title="Profile" />;
  }

  return (
    <>
      <PhoneShell header={header} nav={nav} scrollRef={scrollRef}>{body}</PhoneShell>
      <Toast />
      {invoiceOf && <InvoiceSheet S={S} id={invoiceOf} onClose={() => setInvoiceOf(null)} />}
    </>
  );
}

function greeting() { const h = 10; return h < 12 ? "Good morning" : h < 17 ? "Good afternoon" : "Good evening"; }

/* ---------------- HOME ---------------- */
function ShopHome({ S, me, go, setTab }) {
  const F = S.F;
  const pending = S.myOrders.filter(o => o.status !== "delivered").length;
  const recent = S.myOrders.slice(0, 4);
  return (
    <div className="stagger" style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 16 }}>
      {/* hero */}
      <div style={{ borderRadius: 22, padding: 20, color: "#fff", position: "relative", overflow: "hidden",
        background: "linear-gradient(150deg, var(--brand) 0%, var(--brand-700) 100%)", boxShadow: "var(--sh-lg)" }}>
        <div style={{ position: "absolute", right: -30, top: -30, width: 160, height: 160, borderRadius: "50%",
          background: "rgba(255,255,255,.08)" }} />
        <div style={{ position: "relative" }}>
          <div style={{ fontSize: 13, fontWeight: 600, opacity: .8 }}>Ready to restock?</div>
          <div style={{ display: "flex", gap: 26, margin: "16px 0 18px" }}>
            <div><div className="mono" style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-.03em" }}>{me.orders}</div>
              <div style={{ fontSize: 12, opacity: .78, marginTop: 2 }}>Total orders</div></div>
            <div style={{ width: 1, background: "rgba(255,255,255,.2)" }} />
            <div><div className="mono" style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-.03em" }}>{pending}</div>
              <div style={{ fontSize: 12, opacity: .78, marginTop: 2 }}>In progress</div></div>
          </div>
          <Button variant="outline" full icon="plus" onClick={() => setTab("products")}
            style={{ background: "#fff", color: "var(--brand-700)", border: "none", fontWeight: 700 }}>New order</Button>
        </div>
      </div>

      {/* quick actions */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(3,1fr)", gap: 10 }}>
        {[{ ic: "grid", l: "Browse", fn: () => setTab("products") },
          { ic: "receipt", l: "Invoices", fn: () => setTab("orders") },
          { ic: "truck", l: "Track", fn: () => setTab("orders") }].map(a => (
          <Card key={a.l} pad={14} onClick={a.fn} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 9, textAlign: "center" }}>
            <div style={{ width: 40, height: 40, borderRadius: 12, background: "var(--brand-tint)", color: "var(--brand)", display: "grid", placeItems: "center" }}>
              <Icon name={a.ic} size={20} stroke={2} /></div>
            <span style={{ fontSize: 12.5, fontWeight: 600, color: "var(--ink-2)" }}>{a.l}</span>
          </Card>
        ))}
      </div>

      {/* recent orders */}
      <div>
        <SectionLabel action="See all" onAction={() => setTab("orders")}>Recent orders</SectionLabel>
        <Card pad={4}>
          {recent.map((o, i) => (
            <Row key={o.id} last={i === recent.length - 1} onClick={() => go("order", o.id)}
              left={<div style={{ width: 38, height: 38, borderRadius: 11, background: "var(--surface-2)", display: "grid", placeItems: "center", color: "var(--ink-3)" }}><Icon name="bag" size={18} stroke={1.9} /></div>}
              title={<span className="mono" style={{ fontWeight: 700 }}>{o.id}</span>}
              sub={o.date + " · " + o.items.length + " items"}
              right={<div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: 5 }}>
                <span className="mono" style={{ fontSize: 14, fontWeight: 700 }}>{F.inr(S.orderTotal(o))}</span>
                <Badge status={o.status === "placed" ? "pending" : o.status} size="sm" />
              </div>} />
          ))}
        </Card>
      </div>
    </div>
  );
}

/* ---------------- PRODUCTS ---------------- */
function ProductList({ S, products, go }) {
  const F = S.F;
  return (
    <>
      <div className="stagger" style={{ padding: "4px 16px 8px", display: "flex", flexDirection: "column", gap: 10 }}>
        {products.map(p => {
          const inCart = S.cart[p.id] || 0;
          return (
            <Card key={p.id} pad={12} style={{ display: "flex", gap: 13, alignItems: "center" }}>
              <ProductThumb p={p} size={62} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 11, fontWeight: 700, color: p.tint, letterSpacing: ".02em", textTransform: "uppercase" }}>{p.brand}</div>
                <div style={{ fontSize: 14.5, fontWeight: 600, lineHeight: 1.2, margin: "2px 0 5px" }}>{p.name}</div>
                <div style={{ display: "flex", alignItems: "center", gap: 7, flexWrap: "wrap" }}>
                  <span className="mono" style={{ fontSize: 15, fontWeight: 700 }}>{F.inr(p.price)}</span>
                  <span style={{ fontSize: 11, color: "var(--ink-4)" }}>/ {p.unit}</span>
                  <span style={{ fontSize: 10.5, fontWeight: 700, color: "var(--pos)", background: "var(--pos-tint)", padding: "2px 6px", borderRadius: 6 }}>{p.disc}% off</span>
                  <span style={{ fontSize: 10.5, fontWeight: 600, color: "var(--ink-3)", background: "var(--surface-3)", padding: "2px 6px", borderRadius: 6 }}>GST {p.gst}%</span>
                </div>
              </div>
              <div style={{ alignSelf: "stretch", display: "flex", alignItems: "center" }}>
                {inCart ? <Stepper value={inCart} onChange={(v) => S.setQty(p.id, v)} />
                  : <button className="tap" onClick={() => S.addToCart(p.id)} style={{ width: 40, height: 40, borderRadius: 12,
                      border: "none", background: "var(--brand)", color: "#fff", display: "grid", placeItems: "center",
                      boxShadow: "var(--sh-sm)", cursor: "pointer" }}><Icon name="plus" size={20} stroke={2.4} /></button>}
              </div>
            </Card>
          );
        })}
      </div>
      {S.cartCount > 0 && (
        <div style={{ position: "absolute", left: 16, right: 16, bottom: 96, zIndex: 40 }}>
          <button className="tap" onClick={() => go("cart")} style={{ width: "100%", height: 56, borderRadius: 16, border: "none",
            background: "var(--ink)", color: "#fff", display: "flex", alignItems: "center", justifyContent: "space-between",
            padding: "0 8px 0 18px", boxShadow: "var(--sh-xl)", cursor: "pointer", animation: "fmRise .3s both" }}>
            <span style={{ display: "flex", alignItems: "center", gap: 10, fontSize: 15, fontWeight: 600 }}>
              <span style={{ background: "rgba(255,255,255,.16)", borderRadius: 8, padding: "3px 9px", fontWeight: 700 }} className="mono">{S.cartCount}</span>
              View cart</span>
            <span style={{ display: "flex", alignItems: "center", gap: 8, background: "var(--brand)", height: 42, padding: "0 16px", borderRadius: 12, fontWeight: 700 }}>
              <span className="mono">{F.inr(S.cartTotals.total)}</span><Icon name="arrowR" size={18} /></span>
          </button>
        </div>
      )}
    </>
  );
}

window.ShopkeeperApp = ShopkeeperApp;
