/* ============================================================
   FlashMart Android — Dealer app (Material 3 · blue accent)
   Dashboard · Orders · Stock · Add SKU · Product approval
   ============================================================ */
const DF = window.FM;
const dlNavItems = () => [
  { id: "home", icon: "home", label: "Home" },
  { id: "orders", icon: "bag", label: "Orders", badge: 2 },
  { id: "stock", icon: "box", label: "Stock", badge: 2 },
  { id: "profile", icon: "user", label: "Profile" },
];
const BLUE = "var(--blue)", BLUE_TINT = "var(--blue-tint)";

function AND_DL_OrderCard({ o, action, variant = "filled", fresh }) {
  if (!o) return null;
  const shop = DF.lookups.shop(o.shop);
  return (
    <M3Card variant={fresh ? "filled" : "outlined"} pad={15} style={fresh ? { border: `2px solid ${M.primary}` } : {}}>
      <div style={{ display: "flex", alignItems: "center", gap: 11, marginBottom: 12 }}>
        <Avatar name={shop.store} size={40} tint={BLUE} />
        <div style={{ flex: 1, minWidth: 0 }}><div style={{ fontSize: 14.5, fontWeight: 700, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap", color: M.onSurf }}>{shop.store}</div><div className="mono" style={{ fontSize: 12, color: M.onSurfVar }}>{o.id} · {shop.area}</div></div>
        <span className="mono" style={{ fontSize: 15, fontWeight: 700, color: M.onSurf }}>{DF.inr(DF.orderMath(o.items).total)}</span>
      </div>
      <div style={{ display: "flex", gap: 6, flexWrap: "wrap", marginBottom: action ? 12 : 0 }}>
        {o.items.slice(0, 3).map(it => <span key={it.pid} className="mono" style={{ fontSize: 11, fontWeight: 600, color: M.onSurfVar, background: M.surf3, padding: "4px 9px", borderRadius: 8 }}>{it.qty} × {it.name.split(" ").slice(0, 2).join(" ")}</span>)}
      </div>
      {action && <M3Button variant={variant} size="sm" full iconRight="arrowR">{action}</M3Button>}
    </M3Card>
  );
}

/* ---------------- DASHBOARD ---------------- */
function AND_DL_Home() {
  const me = DF.lookups.dealer("DLR-04");
  const orders = DF.SEED_ORDERS.filter(o => o.dealer === "DLR-04");
  const todayRev = orders.filter(o => o.paid).reduce((a, o) => a + DF.orderMath(o.items).total, 0);
  return (
    <M3Screen nav={<M3NavBar items={dlNavItems()} active="home" />} fab={<M3FAB icon="plus" label="Add SKU" color="primary" />}>
      <div style={{ padding: `${AND_TOP + 6}px 12px 12px 20px`, display: "flex", alignItems: "center", gap: 12 }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: 13, color: M.onSurfVar, fontWeight: 700, color: BLUE }}>Dealer dashboard</div>
          <div style={{ fontSize: 22, fontWeight: 800, letterSpacing: "-.025em", color: M.onSurf, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>Shree Balaji</div>
        </div>
        <M3IconBtn icon="bell" filled badge={2} />
        <Avatar name={me.owner} size={42} tint={BLUE} />
      </div>
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 16 }}>
        <div style={{ borderRadius: 24, padding: 20, color: "#fff", position: "relative", overflow: "hidden", background: "linear-gradient(150deg, var(--blue), var(--blue-700))", boxShadow: "var(--m3-e2)" }}>
          <div style={{ position: "absolute", right: -28, top: -28, width: 150, height: 150, borderRadius: "50%", background: "rgba(255,255,255,.08)" }} />
          <div style={{ fontSize: 13, fontWeight: 600, opacity: .85 }}>Today's revenue</div>
          <div className="mono" style={{ fontSize: 36, fontWeight: 700, letterSpacing: "-.03em", margin: "6px 0 4px" }}>{DF.inr(todayRev)}</div>
          <div style={{ fontSize: 12.5, opacity: .82 }}>{orders.filter(o => o.paid).length} paid orders · Andheri East</div>
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 11 }}>
          {[["clock", "Pending", "1", "var(--gold-tint)", "var(--gold-ink)"], ["truck", "In transit", "1", BLUE_TINT, BLUE], ["box", "Inventory", DF.inrShort(486000), M.surf3, M.onSurf], ["users", "Shopkeepers", "12", "var(--brand-tint)", "var(--brand)"]].map(([ic, l, v, bg, fg]) => (
            <M3Card key={l} variant="filled" pad={15}>
              <div style={{ width: 36, height: 36, borderRadius: 10, background: bg, color: fg, display: "grid", placeItems: "center", marginBottom: 10 }}><Icon name={ic} size={19} /></div>
              <div className="mono" style={{ fontSize: 22, fontWeight: 700, letterSpacing: "-.02em", color: M.onSurf }}>{v}</div>
              <div style={{ fontSize: 12.5, color: M.onSurfVar, fontWeight: 600, marginTop: 1 }}>{l}</div>
            </M3Card>
          ))}
        </div>
        <div>
          <M3SectionLabel action="View all">Awaiting confirmation</M3SectionLabel>
          <AND_DL_OrderCard o={orders.find(o => o.status === "placed")} action="Accept order" variant="filled" fresh />
        </div>
      </div>
    </M3Screen>
  );
}

/* ---------------- ORDERS ---------------- */
function AND_DL_Orders() {
  const orders = DF.SEED_ORDERS.filter(o => o.dealer === "DLR-04");
  return (
    <M3Screen nav={<M3NavBar items={dlNavItems()} active="orders" />}
      topBar={<M3TopBar variant="large" title="Orders" subtitle="1 awaiting action" />}>
      <div style={{ padding: "0 16px 12px" }}>
        <M3Segmented full value="Pending" onChange={() => {}} options={["Pending", "Active", "Done"]} />
      </div>
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 11 }}>
        <AND_DL_OrderCard o={orders.find(o => o.status === "placed")} action="Accept order" variant="filled" fresh />
        <AND_DL_OrderCard o={orders.find(o => o.status === "accepted")} action="Mark out for delivery" variant="filled" />
        <AND_DL_OrderCard o={orders.find(o => o.status === "out")} action="Mark delivered" variant="tonal" />
      </div>
    </M3Screen>
  );
}

/* ---------------- STOCK ---------------- */
function AND_DL_Stock() {
  return (
    <M3Screen nav={<M3NavBar items={dlNavItems()} active="stock" />} fab={<M3FAB icon="plus" label="Add SKU" color="primary" />}
      topBar={<M3TopBar variant="large" title="Inventory" subtitle="8 SKUs · 2 low" actions={<M3IconBtn icon="search" />} />}>
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 10 }}>
        {DF.DEALER_STOCK.map(s => {
          const p = DF.lookups.product(s.pid); const low = s.qty <= s.reorder;
          const pct = Math.min(100, (s.qty / (s.reorder * 3)) * 100);
          return (
            <M3Card key={s.pid} variant="outlined" pad={13} style={{ display: "flex", gap: 13, alignItems: "center" }}>
              <ProductThumb p={p} size={48} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 14, fontWeight: 700, lineHeight: 1.2, color: M.onSurf }}>{p.name}</div>
                <div style={{ display: "flex", alignItems: "center", gap: 8, margin: "6px 0 3px" }}>
                  <div style={{ flex: 1, height: 6, borderRadius: 99, background: M.surf4, overflow: "hidden" }}><div style={{ width: pct + "%", height: "100%", borderRadius: 99, background: low ? M.err : "var(--pos)" }} /></div>
                  <span className="mono" style={{ fontSize: 12, fontWeight: 700, color: low ? M.err : M.onSurf }}>{s.qty}</span>
                </div>
                <div style={{ fontSize: 11.5, color: M.onSurfVar }}>Reorder at {s.reorder} units</div>
              </div>
              {low && <M3Button variant="tonal" size="sm" icon="refresh">Reorder</M3Button>}
            </M3Card>
          );
        })}
      </div>
    </M3Screen>
  );
}

/* ---------------- ADD SKU ---------------- */
function AND_DL_AddSKU() {
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: M.surface, position: "relative" }}>
      <M3TopBar onBack title="Add SKU" />
      <div className="fm-scroll" style={{ flex: 1, overflow: "auto", padding: "10px 16px 110px", display: "flex", flexDirection: "column", gap: 14 }}>
        {/* image upload */}
        <div style={{ display: "flex", gap: 10 }}>
          <div style={{ width: 84, height: 84, borderRadius: 16, border: `1.5px dashed ${M.outline}`, background: M.surf1, display: "grid", placeItems: "center", color: M.onSurfVar }}>
            <div style={{ textAlign: "center" }}><Icon name="upload" size={22} style={{ margin: "0 auto 4px" }} /><div style={{ fontSize: 10.5, fontWeight: 600 }}>Add photo</div></div>
          </div>
          <div style={{ width: 84, height: 84, borderRadius: 16, overflow: "hidden" }}><ProductThumb p={DF.PRODUCTS[5]} size={84} radius={16} /></div>
        </div>
        <M3Field variant="outlined" label="Product name" icon="box" value="Maggi 2-Minute Noodles" />
        <div style={{ display: "flex", gap: 12 }}>
          <M3Field variant="outlined" label="Category" value="Snacks" trailing={<Icon name="chevD" size={18} color={M.onSurfVar} />} style={{ flex: 1 }} />
          <M3Field variant="outlined" label="Brand" value="Nestlé" style={{ flex: 1 }} />
        </div>
        <div style={{ display: "flex", gap: 12 }}>
          <M3Field variant="outlined" label="Price (₹)" prefix="₹" value="168" focused style={{ flex: 1 }} />
          <M3Field variant="outlined" label="GST %" value="18" style={{ flex: 1 }} />
          <M3Field variant="outlined" label="Stock" value="410" style={{ flex: 1 }} />
        </div>
        <M3Field variant="outlined" label="Description" value="Case pack of 12 units. MRP printed." multiline />
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "4px 4px 0" }}>
          <div><div style={{ fontSize: 14.5, fontWeight: 700, color: M.onSurf }}>List immediately</div><div style={{ fontSize: 12.5, color: M.onSurfVar }}>Sends to admin for approval</div></div>
          <M3Switch on={true} onToggle={() => {}} />
        </div>
      </div>
      <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, padding: "14px 16px", paddingBottom: 14 + AND_BOTTOM, background: M.surf2, boxShadow: "0 -1px 0 var(--m3-outline-var)" }}>
        <M3Button variant="filled" size="lg" full icon="check">Save SKU</M3Button>
      </div>
    </div>
  );
}

/* ---------------- PRODUCT APPROVAL ---------------- */
function AND_DL_Approval() {
  const rows = [
    { p: DF.PRODUCTS[5], status: "Approved", note: "Listed at ₹168. GST verified." },
    { p: DF.PRODUCTS[10], status: "Pending", note: "Awaiting admin review · since Jun 5" },
    { p: DF.PRODUCTS[12], status: "Rejected", note: "Price exceeds MRP cap. Revise to ₹185." },
  ];
  return (
    <M3Screen topBar={<M3TopBar onBack title="Product approvals" subtitle="3 submissions" />}>
      <div style={{ padding: "4px 16px", display: "flex", flexDirection: "column", gap: 11 }}>
        {rows.map(({ p, status, note }) => (
          <M3Card key={p.id} variant="outlined" pad={14}>
            <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 10 }}>
              <ProductThumb p={p} size={44} />
              <div style={{ flex: 1, minWidth: 0 }}><div style={{ fontSize: 14, fontWeight: 700, color: M.onSurf }}>{p.name}</div><div className="mono" style={{ fontSize: 12, color: M.onSurfVar, marginTop: 1 }}>{DF.inr(p.price)}</div></div>
              <M3Status status={status} size="sm" />
            </div>
            <div style={{ display: "flex", gap: 9, alignItems: "flex-start", background: M.surf1, borderRadius: 12, padding: "10px 12px" }}>
              <Icon name={status === "Rejected" ? "bell" : "doc"} size={15} color={status === "Rejected" ? M.err : M.onSurfVar} style={{ marginTop: 1 }} />
              <span style={{ fontSize: 12.5, color: M.onSurfVar, lineHeight: 1.45 }}>{note}</span>
            </div>
          </M3Card>
        ))}
      </div>
    </M3Screen>
  );
}

Object.assign(window, { dlNavItems, AND_DL_OrderCard, AND_DL_Home, AND_DL_Orders, AND_DL_Stock, AND_DL_AddSKU, AND_DL_Approval });
