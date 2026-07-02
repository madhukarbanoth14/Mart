/* ============================================================
   Flashmart iOS — Dealer + Employee static screens
   ============================================================ */
const dlNav = (active) => [
  { id: "home", icon: "home", label: "Home" }, { id: "orders", icon: "bag", label: "Orders", badge: 2 },
  { id: "stock", icon: "box", label: "Stock", badge: 2 }, { id: "profile", icon: "user", label: "Profile" },
];
const emNav = (active) => [
  { id: "home", icon: "home", label: "Home" }, { id: "network", icon: "layers", label: "Network" }, { id: "profile", icon: "user", label: "Profile" },
];

/* ---------------- DEALER · DASHBOARD ---------------- */
function DL_Home() {
  const me = F.lookups.dealer("DLR-04");
  const orders = F.SEED_ORDERS.filter(o => o.dealer === "DLR-04");
  const todayRev = orders.filter(o => o.paid).reduce((a, o) => a + F.orderMath(o.items).total, 0);
  return (
    <Screen nav={<NavBar items={dlNav("home")} active="home" />}>
      <TopBar title="Shree Balaji" kicker="Dealer dashboard" accent="var(--blue)"
        right={<><GlyphBtn name="bell" badge={2} /><Avatar name={me.owner} tint="var(--blue)" /></>} />
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 16 }}>
        <div style={{ borderRadius: 22, padding: 20, color: "#fff", position: "relative", overflow: "hidden", background: "linear-gradient(150deg, var(--blue), var(--blue-700))", boxShadow: "var(--sh-lg)" }}>
          <div style={{ position: "absolute", right: -28, top: -28, width: 150, height: 150, borderRadius: "50%", background: "rgba(255,255,255,.08)" }} />
          <div style={{ fontSize: 13, fontWeight: 600, opacity: .85 }}>Today's revenue</div>
          <div className="mono" style={{ fontSize: 38, fontWeight: 700, letterSpacing: "-.03em", margin: "6px 0 4px" }}>{F.inr(todayRev)}</div>
          <div style={{ fontSize: 12.5, opacity: .82 }}>{orders.filter(o => o.paid).length} paid orders · Andheri East</div>
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 11 }}>
          <Stat label="Pending orders" value={1} icon="clock" accent="var(--warn)" tint="var(--warn-tint)" sub="Need your action" />
          <Stat label="In transit" value={1} icon="truck" accent="var(--brand)" sub="Accepted / out" />
        </div>
        <div>
          <SectionLabel action="View all">Awaiting confirmation</SectionLabel>
          <DL_OrderCard o={orders.find(o => o.status === "placed")} action="Accept order" variant="primary" />
        </div>
      </div>
    </Screen>
  );
}

function DL_OrderCard({ o, action, variant, fresh }) {
  if (!o) return null;
  const shop = F.lookups.shop(o.shop);
  return (
    <Card style={{ boxShadow: fresh ? "0 0 0 3px var(--pos-tint)" : "var(--sh-sm)", borderColor: fresh ? "var(--pos)" : "var(--line)" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 12 }}>
        <Avatar name={shop.store} size={42} tint="var(--blue)" />
        <div style={{ flex: 1, minWidth: 0 }}><div style={{ fontSize: 14.5, fontWeight: 700, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{shop.store}</div><div className="mono" style={{ fontSize: 12.5, color: "var(--ink-3)" }}>{o.id} · {shop.area}</div></div>
        <div className="mono" style={{ fontSize: 15, fontWeight: 700 }}>{F.inr(F.orderMath(o.items).total)}</div>
      </div>
      <div style={{ display: "flex", gap: 6, flexWrap: "wrap", marginBottom: action ? 12 : 0 }}>
        {o.items.slice(0, 3).map(it => <span key={it.pid} className="mono" style={{ fontSize: 11.5, fontWeight: 600, color: "var(--ink-2)", background: "var(--surface-2)", padding: "4px 9px", borderRadius: 8, whiteSpace: "nowrap" }}>{it.qty} × {it.name.split(" ").slice(0, 2).join(" ")}</span>)}
      </div>
      {action && <Button variant={variant} full size="sm" icon="arrowR">{action}</Button>}
    </Card>
  );
}

/* ---------------- DEALER · ORDERS ---------------- */
function DL_Orders() {
  const orders = F.SEED_ORDERS.filter(o => o.dealer === "DLR-04");
  return (
    <Screen nav={<NavBar items={dlNav("orders")} active="orders" />}>
      <TopBar title="Orders" subtitle="1 awaiting action" />
      <div style={{ padding: "0 16px" }}>
        <div style={{ marginBottom: 14 }}><Segmented full value="Pending" onChange={() => {}} options={["Pending", "Active", "Done"]} /></div>
        <div style={{ display: "flex", flexDirection: "column", gap: 11 }}>
          <DL_OrderCard o={orders.find(o => o.status === "placed")} action="Accept order" variant="primary" fresh />
          <DL_OrderCard o={orders.find(o => o.status === "accepted")} action="Mark out for delivery" variant="primary" />
          <DL_OrderCard o={orders.find(o => o.status === "out")} action="Mark delivered" variant="pos" />
        </div>
      </div>
    </Screen>
  );
}

/* ---------------- DEALER · STOCK ---------------- */
function DL_Stock() {
  return (
    <Screen nav={<NavBar items={dlNav("stock")} active="stock" />}>
      <TopBar title="Stock" subtitle="8 SKUs · 2 low" />
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 10 }}>
        {F.DEALER_STOCK.map(s => {
          const p = F.lookups.product(s.pid); const low = s.qty <= s.reorder;
          const pct = Math.min(100, (s.qty / (s.reorder * 3)) * 100);
          return (
            <Card key={s.pid} pad={13} style={{ display: "flex", gap: 13, alignItems: "center" }}>
              <ProductThumb p={p} size={48} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 14, fontWeight: 600, lineHeight: 1.2 }}>{p.name}</div>
                <div style={{ display: "flex", alignItems: "center", gap: 8, margin: "6px 0 3px" }}>
                  <div style={{ flex: 1, height: 6, borderRadius: 99, background: "var(--surface-3)", overflow: "hidden" }}><div style={{ width: pct + "%", height: "100%", borderRadius: 99, background: low ? "var(--neg)" : "var(--pos)" }} /></div>
                  <span className="mono" style={{ fontSize: 12, fontWeight: 700, color: low ? "var(--neg)" : "var(--ink-2)" }}>{s.qty}</span>
                </div>
                <div style={{ fontSize: 11.5, color: "var(--ink-4)" }}>Reorder at {s.reorder} units</div>
              </div>
              {low && <Button variant="soft" size="sm" icon="refresh">Reorder</Button>}
            </Card>
          );
        })}
      </div>
    </Screen>
  );
}

/* ---------------- EMPLOYEE · HOME ---------------- */
function EM_Home() {
  const me = F.EMPLOYEES[0];
  const progress = Math.round((me.shops / 120) * 100);
  return (
    <Screen nav={<NavBar items={emNav("home")} active="home" />}>
      <TopBar title="Neha's desk" kicker="Field executive" accent="#c97a16"
        right={<><GlyphBtn name="bell" /><Avatar name={me.name} tint="#c97a16" /></>} />
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 16 }}>
        <div style={{ borderRadius: 22, padding: 20, color: "#fff", position: "relative", overflow: "hidden", background: "linear-gradient(150deg, #c97a16, #92560a)", boxShadow: "var(--sh-lg)" }}>
          <div style={{ position: "absolute", right: -28, top: -28, width: 150, height: 150, borderRadius: "50%", background: "rgba(255,255,255,.08)" }} />
          <div style={{ fontSize: 13, fontWeight: 600, opacity: .85 }}>This month's onboarding</div>
          <div style={{ display: "flex", gap: 26, margin: "14px 0 16px" }}>
            <div><div className="mono" style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-.03em" }}>{me.dealers}</div><div style={{ fontSize: 12, opacity: .8, marginTop: 2 }}>Dealers</div></div>
            <div style={{ width: 1, background: "rgba(255,255,255,.2)" }} />
            <div><div className="mono" style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-.03em" }}>{me.shops}</div><div style={{ fontSize: 12, opacity: .8, marginTop: 2 }}>Shopkeepers</div></div>
          </div>
          <div style={{ height: 7, borderRadius: 99, background: "rgba(255,255,255,.2)", overflow: "hidden" }}><div style={{ width: progress + "%", height: "100%", background: "#fff", borderRadius: 99 }} /></div>
          <div style={{ fontSize: 11.5, opacity: .85, marginTop: 7 }}>{progress}% of 120 monthly target</div>
        </div>
        {[["truck", "Add a dealer", "Onboard a distributor to an area", "var(--pos-tint)", "var(--pos)"], ["bag", "Add a shopkeeper", "Register a retail store", "var(--brand-tint)", "var(--brand)"]].map(([ic, t, s, bg, fg]) => (
          <Card key={t} style={{ display: "flex", alignItems: "center", gap: 14 }}>
            <div style={{ width: 46, height: 46, borderRadius: 13, background: bg, color: fg, display: "grid", placeItems: "center" }}><Icon name={ic} size={22} /></div>
            <div style={{ flex: 1 }}><div style={{ fontSize: 15.5, fontWeight: 700 }}>{t}</div><div style={{ fontSize: 12.5, color: "var(--ink-3)" }}>{s}</div></div>
            <div style={{ width: 38, height: 38, borderRadius: 11, background: "var(--ink)", color: "#fff", display: "grid", placeItems: "center" }}><Icon name="plus" size={20} stroke={2.4} /></div>
          </Card>
        ))}
      </div>
    </Screen>
  );
}

/* ---------------- EMPLOYEE · ADD SHOPKEEPER (form) ---------------- */
function EM_AddForm() {
  const fields = [["Store name", "box", "Sharma General Store", true], ["Owner name", "user", "Ravi Sharma", true], ["Phone number", "phone", "98213 44567", true], ["Email (optional)", "mail", "", false], ["Area / route", "pin", "Andheri East", true]];
  return (
    <Screen>
      <TopBar title="Add shopkeeper" subtitle="Field onboarding" onBack />
      <div style={{ padding: "4px 16px 0", display: "flex", flexDirection: "column", gap: 14 }}>
        {fields.map(([label, icon, val, filled], i) => (
          <label key={label} style={{ display: "block" }}>
            <div style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)", marginBottom: 7 }}>{label}</div>
            <div style={{ display: "flex", alignItems: "center", gap: 10, height: 50, padding: "0 14px", background: "var(--surface)", borderRadius: 13,
              border: `1.5px solid ${i === 0 ? "var(--brand)" : "var(--line-2)"}`, boxShadow: i === 0 ? "0 0 0 4px var(--brand-tint)" : "none" }}>
              <Icon name={icon} size={18} color="var(--ink-4)" />
              {label.startsWith("Phone") && <span className="mono" style={{ color: "var(--ink-3)", fontWeight: 600 }}>+91</span>}
              <span style={{ flex: 1, fontSize: 15, color: val ? "var(--ink)" : "var(--ink-4)" }}>{val || "Enter " + label.toLowerCase()}</span>
              {label.startsWith("Area") && <Icon name="chevD" size={16} color="var(--ink-4)" />}
            </div>
          </label>
        ))}
      </div>
      <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, padding: "14px 16px 30px", background: "linear-gradient(to top, var(--surface) 64%, transparent)" }}>
        <Button variant="dark" size="lg" full icon="check">Save shopkeeper</Button>
      </div>
    </Screen>
  );
}

/* ---------------- EMPLOYEE · NETWORK ---------------- */
function EM_Network() {
  return (
    <Screen nav={<NavBar items={emNav("network")} active="network" />}>
      <TopBar title="My network" subtitle="10 dealers · 85 shopkeepers" />
      <div style={{ padding: "0 16px" }}>
        <div style={{ marginBottom: 14 }}><Segmented full value="Dealers" onChange={() => {}} options={["Dealers", "Shopkeepers"]} /></div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {F.DEALERS.map((d, i) => (
            <Card key={d.id} pad={13} style={{ display: "flex", alignItems: "center", gap: 13, borderColor: i === 0 ? "var(--pos)" : "var(--line)", boxShadow: i === 0 ? "0 0 0 2px var(--pos-tint)" : "var(--sh-sm)" }}>
              <Avatar name={d.name} size={42} tint="var(--blue)" />
              <div style={{ flex: 1, minWidth: 0 }}><div style={{ fontSize: 14.5, fontWeight: 700, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{d.name}</div><div className="mono" style={{ fontSize: 12.5, color: "var(--ink-3)" }}>{d.id} · {d.area}</div></div>
              <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: 5 }}><Badge status={d.status} size="sm" /><span style={{ fontSize: 11.5, color: "var(--ink-4)", fontWeight: 600 }}>{d.orders} orders</span></div>
            </Card>
          ))}
        </div>
      </div>
    </Screen>
  );
}

Object.assign(window, { DL_Home, DL_Orders, DL_Stock, DL_OrderCard, EM_Home, EM_AddForm, EM_Network, dlNav, emNav });
