/* ============================================================
   Flashmart iOS — static app screens for the gallery
   (presentational recreations using the design system)
   ============================================================ */
const F = window.FM;
const skNav = (active) => [
  { id: "home", icon: "home", label: "Home" }, { id: "products", icon: "grid", label: "Products" },
  { id: "orders", icon: "bag", label: "Orders", badge: 2 }, { id: "profile", icon: "user", label: "Profile" },
].map(x => ({ ...x, _a: x.id === active }));

function NavBar({ items, active }) {
  return <BottomNav items={items} active={active} onChange={() => {}} />;
}

/* ---------------- SHOPKEEPER · HOME ---------------- */
function SK_Home() {
  const me = F.lookups.shop("SHP-118");
  const recent = F.SEED_ORDERS.filter(o => o.shop === "SHP-118").slice(0, 3);
  return (
    <Screen nav={<NavBar items={skNav("home")} active="home" />}>
      <TopBar title={me.store} kicker="Good morning, Ramesh"
        right={<><GlyphBtn name="bell" badge={1} /><Avatar name={me.owner} /></>} />
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 16 }}>
        <div style={{ borderRadius: 22, padding: 20, color: "#fff", position: "relative", overflow: "hidden",
          background: "linear-gradient(150deg, var(--brand), var(--brand-700))", boxShadow: "var(--sh-lg)" }}>
          <div style={{ position: "absolute", right: -30, top: -30, width: 160, height: 160, borderRadius: "50%", background: "rgba(255,255,255,.08)" }} />
          <div style={{ position: "relative" }}>
            <div style={{ fontSize: 13, fontWeight: 600, opacity: .8 }}>Ready to restock?</div>
            <div style={{ display: "flex", gap: 26, margin: "16px 0 18px" }}>
              <div><div className="mono" style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-.03em" }}>{me.orders}</div><div style={{ fontSize: 12, opacity: .78, marginTop: 2 }}>Total orders</div></div>
              <div style={{ width: 1, background: "rgba(255,255,255,.2)" }} />
              <div><div className="mono" style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-.03em" }}>2</div><div style={{ fontSize: 12, opacity: .78, marginTop: 2 }}>In progress</div></div>
            </div>
            <Button variant="outline" full icon="plus" style={{ background: "#fff", color: "var(--brand-700)", border: "none", fontWeight: 700 }}>New order</Button>
          </div>
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(3,1fr)", gap: 10 }}>
          {[["grid", "Browse"], ["receipt", "Invoices"], ["truck", "Track"]].map(([ic, l]) => (
            <Card key={l} pad={14} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 9 }}>
              <div style={{ width: 40, height: 40, borderRadius: 12, background: "var(--brand-tint)", color: "var(--brand)", display: "grid", placeItems: "center" }}><Icon name={ic} size={20} /></div>
              <span style={{ fontSize: 12.5, fontWeight: 600, color: "var(--ink-2)" }}>{l}</span>
            </Card>
          ))}
        </div>
        <div>
          <SectionLabel action="See all">Recent orders</SectionLabel>
          <Card pad={4}>
            {recent.map((o, i) => (
              <Row key={o.id} last={i === recent.length - 1}
                left={<div style={{ width: 38, height: 38, borderRadius: 11, background: "var(--surface-2)", display: "grid", placeItems: "center", color: "var(--ink-3)" }}><Icon name="bag" size={18} /></div>}
                title={<span className="mono" style={{ fontWeight: 700 }}>{o.id}</span>} sub={o.date + " · " + o.items.length + " items"}
                right={<div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: 5 }}><span className="mono" style={{ fontSize: 14, fontWeight: 700 }}>{F.inr(F.orderMath(o.items).total)}</span><Badge status={o.status === "placed" ? "pending" : o.status} size="sm" /></div>} />
            ))}
          </Card>
        </div>
      </div>
    </Screen>
  );
}

/* ---------------- SHOPKEEPER · CATALOG ---------------- */
function SK_Catalog() {
  const cats = ["All", "Staples", "Dairy", "Snacks", "Beverages"];
  const products = F.PRODUCTS.slice(0, 5);
  return (
    <Screen nav={<NavBar items={skNav("products")} active="products" />}>
      <div style={{ padding: `${IOS_TOP - 50}px 0 0` }} />
      <div style={{ padding: "8px 20px 12px" }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 14 }}>
          <div style={{ fontSize: 25, fontWeight: 700, letterSpacing: "-.025em" }}>Catalog</div>
          <GlyphBtn name="cart" badge={3} />
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 10, height: 46, padding: "0 14px", background: "var(--surface)", borderRadius: 13, border: "1px solid var(--line)", boxShadow: "var(--sh-sm)" }}>
          <Icon name="search" size={19} color="var(--ink-4)" /><span style={{ color: "var(--ink-4)", fontSize: 15 }}>Search products or brands</span>
        </div>
        <div style={{ display: "flex", gap: 8, marginTop: 12, overflow: "hidden" }}>
          {cats.map((c, i) => (
            <span key={c} style={{ flexShrink: 0, padding: "8px 14px", borderRadius: 11, border: "1px solid", borderColor: i === 0 ? "transparent" : "var(--line-2)", background: i === 0 ? "var(--ink)" : "var(--surface)", color: i === 0 ? "#fff" : "var(--ink-2)", fontSize: 13, fontWeight: 600 }}>{c}</span>
          ))}
        </div>
      </div>
      <div style={{ padding: "4px 16px 8px", display: "flex", flexDirection: "column", gap: 10 }}>
        {products.map((p, idx) => (
          <Card key={p.id} pad={12} style={{ display: "flex", gap: 13, alignItems: "center" }}>
            <ProductThumb p={p} size={62} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 11, fontWeight: 700, color: p.tint, textTransform: "uppercase" }}>{p.brand}</div>
              <div style={{ fontSize: 14.5, fontWeight: 600, lineHeight: 1.2, margin: "2px 0 5px" }}>{p.name}</div>
              <div style={{ display: "flex", alignItems: "center", gap: 7, flexWrap: "wrap" }}>
                <span className="mono" style={{ fontSize: 15, fontWeight: 700 }}>{F.inr(p.price)}</span>
                <span style={{ fontSize: 10.5, fontWeight: 700, color: "var(--pos)", background: "var(--pos-tint)", padding: "2px 6px", borderRadius: 6 }}>{p.disc}% off</span>
              </div>
            </div>
            {idx === 0 ? <Stepper value={2} onChange={() => {}} />
              : <div style={{ width: 40, height: 40, borderRadius: 12, background: "var(--brand)", color: "#fff", display: "grid", placeItems: "center", boxShadow: "var(--sh-sm)" }}><Icon name="plus" size={20} stroke={2.4} /></div>}
          </Card>
        ))}
      </div>
    </Screen>
  );
}

/* ---------------- SHOPKEEPER · CART ---------------- */
function SK_Cart() {
  const items = [["atta", 6], ["oil", 12], ["salt", 10]].map(([id, q]) => ({ ...F.lookups.product(id), qty: q }));
  const t = F.orderMath(items.map(it => ({ ...it })));
  return (
    <Screen>
      <TopBar title="Cart" subtitle="3 items" onBack />
      <div style={{ padding: "4px 16px 0", display: "flex", flexDirection: "column", gap: 12 }}>
        <Card pad={6}>
          {items.map((it, i) => (
            <div key={it.id} style={{ display: "flex", gap: 12, alignItems: "center", padding: "10px 8px", borderBottom: i === items.length - 1 ? "none" : "1px solid var(--line)" }}>
              <ProductThumb p={it} size={52} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 14, fontWeight: 600, lineHeight: 1.2 }}>{it.name}</div>
                <div className="mono" style={{ fontSize: 12, color: "var(--ink-4)", margin: "3px 0 7px" }}>{F.inr(it.price)} · {it.disc}% off</div>
                <Stepper value={it.qty} onChange={() => {}} />
              </div>
              <span className="mono" style={{ fontSize: 14.5, fontWeight: 700, alignSelf: "flex-start", marginTop: 4 }}>{F.inr(F.lineMath(it).total)}</span>
            </div>
          ))}
        </Card>
        <Card>
          <MoneyRow label="Subtotal" value={F.inr(t.sub)} />
          <MoneyRow label="Shopkeeper discount" value={"− " + F.inr(t.disc)} accent="var(--pos)" />
          <MoneyRow label="GST" value={"+ " + F.inr(t.gst)} />
          <div style={{ borderTop: "1px dashed var(--line-2)", margin: "4px 0" }} />
          <MoneyRow strong label="Total payable" value={F.inr(t.total)} />
        </Card>
      </div>
      <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, padding: "14px 16px 30px", background: "linear-gradient(to top, var(--surface) 64%, transparent)" }}>
        <Button variant="primary" size="lg" full iconRight="arrowR"><span style={{ display: "flex", alignItems: "center", gap: 10 }}>Proceed to payment <span className="mono" style={{ background: "rgba(255,255,255,.18)", padding: "2px 8px", borderRadius: 7 }}>{F.inr(t.total)}</span></span></Button>
      </div>
    </Screen>
  );
}

/* ---------------- SHOPKEEPER · TRACK ---------------- */
function SK_Track() {
  const steps = [
    { label: "Order placed", time: "Jun 6 · 10:24 AM", state: "done" },
    { label: "Payment successful", time: "Paid via UPI", state: "done" },
    { label: "Dealer accepted", time: "Sharma Distributors", state: "done" },
    { label: "Out for delivery", time: "On the way to your store", state: "active" },
    { label: "Delivered", time: "Pending", state: "todo" },
  ];
  return (
    <Screen>
      <TopBar title="Track order" subtitle="ORD-1122" onBack />
      <div style={{ padding: "4px 16px 30px", display: "flex", flexDirection: "column", gap: 14 }}>
        <Card pad={18} style={{ background: "linear-gradient(150deg, var(--brand), var(--brand-700))", color: "#fff", border: "none" }}>
          <div style={{ fontSize: 12.5, fontWeight: 600, opacity: .82 }}>Estimated delivery</div>
          <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: "-.02em", marginTop: 4 }}>Today, by 6:00 PM</div>
          <div style={{ fontSize: 13, opacity: .82, marginTop: 4 }}>Out for delivery</div>
        </Card>
        <Card>
          <SectionLabel style={{ paddingBottom: 14 }}>Order journey</SectionLabel>
          <OrderTimeline steps={steps} />
        </Card>
        <Card style={{ display: "flex", alignItems: "center", gap: 13 }}>
          <Avatar name="Sharma Distributors" tint="#0e9e6e" />
          <div style={{ flex: 1 }}><div style={{ fontSize: 14.5, fontWeight: 700 }}>Sharma Distributors</div><div style={{ fontSize: 12.5, color: "var(--ink-3)" }}>Your dealer · Andheri East</div></div>
          <div style={{ width: 42, height: 42, borderRadius: 12, border: "1px solid var(--line)", background: "var(--surface)", color: "var(--pos)", display: "grid", placeItems: "center", boxShadow: "var(--sh-sm)" }}><Icon name="phone" size={19} /></div>
        </Card>
      </div>
    </Screen>
  );
}

/* ---------------- SHOPKEEPER · PROFILE ---------------- */
function SK_Profile() {
  const me = F.lookups.shop("SHP-118");
  const rows = [["pin", "Store address", "Andheri East, Mumbai"], ["truck", "My dealer", "Sharma Distributors"], ["card", "Payment methods", "UPI · 2 cards"], ["receipt", "GST details", "27ABCDE1234F1Z5"], ["bell", "Notifications", "Order & delivery alerts"], ["settings", "Help & support", "Chat, call, FAQs"]];
  return (
    <Screen nav={<NavBar items={skNav("profile")} active="profile" />}>
      <TopBar title="Profile" />
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 14 }}>
        <Card style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <Avatar name={me.owner} size={56} />
          <div style={{ flex: 1 }}><div style={{ fontSize: 18, fontWeight: 700, letterSpacing: "-.02em" }}>{me.store}</div><div style={{ fontSize: 13, color: "var(--ink-3)" }}>{me.owner} · {me.phone}</div></div>
          <Badge status="Active" size="sm" dot />
        </Card>
        <div style={{ display: "flex", gap: 10 }}>
          {[["Orders", 24], ["Delivered", 21], ["Active", 2]].map(([l, v]) => (
            <Card key={l} pad={14} style={{ flex: 1, textAlign: "center" }}><div className="mono" style={{ fontSize: 22, fontWeight: 700 }}>{v}</div><div style={{ fontSize: 11.5, color: "var(--ink-4)", marginTop: 3, fontWeight: 600 }}>{l}</div></Card>
          ))}
        </div>
        <Card pad={6}>
          {rows.map((r, i) => (
            <Row key={r[1]} last={i === rows.length - 1}
              left={<div style={{ width: 38, height: 38, borderRadius: 11, background: "var(--surface-2)", color: "var(--ink-2)", display: "grid", placeItems: "center" }}><Icon name={r[0]} size={18} /></div>}
              title={r[1]} sub={r[2]} right={<Icon name="chevR" size={17} color="var(--ink-4)" />} />
          ))}
        </Card>
      </div>
    </Screen>
  );
}

Object.assign(window, { SK_Home, SK_Catalog, SK_Cart, SK_Track, SK_Profile, NavBar, skNav });
