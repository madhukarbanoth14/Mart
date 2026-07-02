/* ============================================================
   FlashMart Android — Shopkeeper journey (part 3)
   My Orders · Invoice · Profile
   ============================================================ */
const SF3 = window.FM;

/* ---------------- MY ORDERS ---------------- */
function AND_SK_Orders() {
  const orders = SF3.SEED_ORDERS.filter(o => o.shop === "SHP-118" || o.shop === "SHP-092");
  return (
    <M3Screen nav={<M3NavBar items={skNavItems("orders")} active="orders" />}
      topBar={<M3TopBar variant="large" title="My orders" subtitle="Ramesh General Store" actions={<M3IconBtn icon="search" />} />}>
      <div style={{ padding: "0 16px 12px" }}>
        <M3Segmented full value="All" onChange={() => {}} options={["All", "Pending", "Delivered", "Cancelled"]} />
      </div>
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 11 }}>
        {orders.map(o => {
          const shop = SF3.lookups.shop(o.shop);
          return (
            <M3Card key={o.id} variant="outlined" pad={14}>
              <div style={{ display: "flex", alignItems: "center", gap: 11, marginBottom: 12 }}>
                <div style={{ width: 40, height: 40, borderRadius: 12, background: M.surf3, color: M.onSurfVar, display: "grid", placeItems: "center" }}><Icon name="bag" size={19} /></div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div className="mono" style={{ fontSize: 14.5, fontWeight: 700, color: M.onSurf }}>{o.id}</div>
                  <div style={{ fontSize: 12, color: M.onSurfVar }}>{o.date} · {o.items.length} items</div>
                </div>
                <M3Status status={o.status === "placed" ? "pending" : o.status} size="sm" />
              </div>
              <div style={{ display: "flex", gap: 6, flexWrap: "wrap", marginBottom: 12 }}>
                {o.items.slice(0, 3).map(it => <span key={it.pid} className="mono" style={{ fontSize: 11, fontWeight: 600, color: M.onSurfVar, background: M.surf3, padding: "4px 9px", borderRadius: 8 }}>{it.qty} × {it.name.split(" ").slice(0, 2).join(" ")}</span>)}
              </div>
              <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", paddingTop: 12, borderTop: "1px solid var(--m3-outline-var)" }}>
                <span className="mono" style={{ fontSize: 15, fontWeight: 700, color: M.onSurf }}>{SF3.inr(SF3.orderMath(o.items).total)}</span>
                <div style={{ display: "flex", gap: 8 }}>
                  {o.status === "delivered" ? <M3Button variant="outlined" size="sm" icon="receipt">Invoice</M3Button> : null}
                  <M3Button variant={o.status === "delivered" ? "tonal" : "filled"} size="sm" icon={o.status === "delivered" ? "refresh" : "truck"}>{o.status === "delivered" ? "Reorder" : "Track"}</M3Button>
                </div>
              </div>
            </M3Card>
          );
        })}
      </div>
    </M3Screen>
  );
}

/* ---------------- INVOICE ---------------- */
function AND_SK_Invoice() {
  const items = [["atta", 6], ["oil", 12], ["salt", 10]].map(([id, q]) => ({ ...SF3.lookups.product(id), qty: q }));
  const t = SF3.orderMath(items.map(it => ({ ...it })));
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: M.surface, position: "relative" }}>
      <M3TopBar onBack title="Tax invoice" subtitle="INV-1124" actions={<M3IconBtn icon="download" />} />
      <div className="fm-scroll" style={{ flex: 1, overflow: "auto", padding: "4px 16px 110px" }}>
        <M3Card variant="outlined" pad={0} style={{ overflow: "hidden" }}>
          {/* invoice header */}
          <div style={{ background: "var(--ink-surface)", color: "#fff", padding: "18px 18px 16px", display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
            <div>
              <div style={{ fontWeight: 800, fontSize: 17 }}>Flash<span style={{ color: "var(--gold)" }}>Mart</span></div>
              <div style={{ fontSize: 11, opacity: .6, marginTop: 3 }}>Tax Invoice · GST compliant</div>
            </div>
            <div style={{ textAlign: "right" }}>
              <div className="mono" style={{ fontSize: 13, fontWeight: 700 }}>INV-1124</div>
              <div style={{ fontSize: 11, opacity: .6, marginTop: 3 }}>Jun 6, 2026</div>
            </div>
          </div>
          {/* parties */}
          <div style={{ display: "flex", padding: "14px 18px", gap: 14, borderBottom: "1px solid var(--m3-outline-var)" }}>
            <div style={{ flex: 1 }}><div style={{ fontSize: 10.5, color: M.onSurfVar, fontWeight: 700, textTransform: "uppercase" }}>Billed to</div><div style={{ fontSize: 13, fontWeight: 700, marginTop: 4, color: M.onSurf }}>Ramesh General Store</div><div className="mono" style={{ fontSize: 11, color: M.onSurfVar, marginTop: 2 }}>27ABCDE1234F1Z5</div></div>
            <div style={{ flex: 1 }}><div style={{ fontSize: 10.5, color: M.onSurfVar, fontWeight: 700, textTransform: "uppercase" }}>Dealer</div><div style={{ fontSize: 13, fontWeight: 700, marginTop: 4, color: M.onSurf }}>Shree Balaji Distributors</div><div className="mono" style={{ fontSize: 11, color: M.onSurfVar, marginTop: 2 }}>Andheri East</div></div>
          </div>
          {/* line items */}
          <div style={{ padding: "8px 18px" }}>
            {items.map((it, i) => {
              const m = SF3.lineMath(it);
              return (
                <div key={it.id} style={{ display: "flex", alignItems: "center", gap: 10, padding: "10px 0", borderBottom: i === items.length - 1 ? "none" : "1px solid var(--m3-outline-var)" }}>
                  <div style={{ flex: 1, minWidth: 0 }}><div style={{ fontSize: 13, fontWeight: 600, color: M.onSurf }}>{it.name}</div><div className="mono" style={{ fontSize: 11, color: M.onSurfVar, marginTop: 1 }}>{it.qty} × {SF3.inr(it.price)} · GST {it.gst}%</div></div>
                  <span className="mono" style={{ fontSize: 13, fontWeight: 700, color: M.onSurf }}>{SF3.inr(m.total)}</span>
                </div>
              );
            })}
          </div>
          {/* totals */}
          <div style={{ padding: "10px 18px 18px", background: M.surf1 }}>
            <MoneyRow label="Taxable value" value={SF3.inr(t.sub - t.disc)} />
            <MoneyRow label="CGST + SGST" value={SF3.inr(t.gst)} />
            <div style={{ borderTop: "1px dashed var(--m3-outline-var)", margin: "6px 0" }} />
            <MoneyRow strong label="Invoice total" value={SF3.inr(t.total)} accent="var(--pos)" />
          </div>
        </M3Card>
      </div>
      <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, padding: "14px 16px", paddingBottom: 14 + AND_BOTTOM,
        background: M.surf2, boxShadow: "0 -1px 0 var(--m3-outline-var)", display: "flex", gap: 12 }}>
        <M3Button variant="tonal" size="lg" full icon="upload">Share</M3Button>
        <M3Button variant="filled" size="lg" full icon="download">Download PDF</M3Button>
      </div>
    </div>
  );
}

/* ---------------- PROFILE ---------------- */
function AND_SK_Profile() {
  const me = SF3.lookups.shop("SHP-118");
  const rows = [["pin", "Store address", "Andheri East, Mumbai"], ["truck", "My dealer", "Shree Balaji Distributors"], ["doc", "Document center", "3 verified · 1 pending"], ["card", "Payment methods", "UPI · 2 cards"], ["receipt", "GST details", "27ABCDE1234F1Z5"], ["bell", "Notifications", "Order & delivery alerts"]];
  return (
    <M3Screen nav={<M3NavBar items={skNavItems("profile")} active="profile" />}
      topBar={<M3TopBar variant="large" title="Profile" actions={<M3IconBtn icon="settings" />} />}>
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 14 }}>
        <M3Card variant="filled" style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <Avatar name={me.owner} size={56} />
          <div style={{ flex: 1 }}><div style={{ fontSize: 18, fontWeight: 800, letterSpacing: "-.02em", color: M.onSurf }}>{me.store}</div><div style={{ fontSize: 13, color: M.onSurfVar }}>{me.owner} · {me.phone}</div></div>
          <M3Status status="Active" size="sm" dot />
        </M3Card>
        <div style={{ display: "flex", gap: 10 }}>
          {[["Orders", 24], ["Delivered", 21], ["Active", 2]].map(([l, v]) => (
            <M3Card key={l} variant="outlined" pad={14} style={{ flex: 1, textAlign: "center" }}><div className="mono" style={{ fontSize: 22, fontWeight: 700, color: M.onSurf }}>{v}</div><div style={{ fontSize: 11.5, color: M.onSurfVar, marginTop: 3, fontWeight: 600 }}>{l}</div></M3Card>
          ))}
        </div>
        <M3Card variant="filled" pad={6}>
          {rows.map((r, i) => (
            <M3ListItem key={r[1]} last={i === rows.length - 1} onClick={() => {}}
              leading={<div style={{ width: 40, height: 40, borderRadius: 11, background: M.surf4, color: M.onSurfVar, display: "grid", placeItems: "center" }}><Icon name={r[0]} size={19} /></div>}
              headline={r[1]} supporting={r[2]} trailing={<Icon name="chevR" size={18} color={M.onSurfVar} />} />
          ))}
        </M3Card>
        <M3Button variant="text" size="md" full icon="logout" style={{ color: M.err }}>Log out</M3Button>
      </div>
    </M3Screen>
  );
}

Object.assign(window, { AND_SK_Orders, AND_SK_Invoice, AND_SK_Profile });
