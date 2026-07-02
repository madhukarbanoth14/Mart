/* ============================================================
   Flashmart — Shopkeeper: orders list, profile, invoice
   ============================================================ */

function OrdersTab({ S, go }) {
  const F = S.F;
  const [seg, setSeg] = React.useState("All");
  const list = S.myOrders.filter(o =>
    seg === "All" ? true : seg === "Active" ? o.status !== "delivered" : o.status === "delivered");
  return (
    <div style={{ padding: "0 16px" }}>
      <div style={{ marginBottom: 14 }}>
        <Segmented full value={seg} onChange={setSeg} options={["All", "Active", "Delivered"]} />
      </div>
      <div className="stagger" style={{ display: "flex", flexDirection: "column", gap: 11 }}>
        {list.map(o => {
          const fresh = S.flash === o.id;
          return (
            <Card key={o.id} onClick={() => go("order", o.id)}
              style={{ boxShadow: fresh ? "0 0 0 3px var(--brand-tint)" : "var(--sh-sm)", borderColor: fresh ? "var(--brand)" : "var(--line)" }}>
              <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 12 }}>
                <div style={{ display: "flex", alignItems: "center", gap: 9 }}>
                  <span className="mono" style={{ fontSize: 15, fontWeight: 700 }}>{o.id}</span>
                  {o.fresh && <span style={{ fontSize: 10, fontWeight: 700, color: "var(--brand)", background: "var(--brand-tint)", padding: "2px 7px", borderRadius: 6 }}>NEW</span>}
                </div>
                <Badge status={o.status === "placed" ? "pending" : o.status} size="sm" />
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: -6 }}>
                <div style={{ display: "flex" }}>
                  {o.items.slice(0, 4).map((it, i) => (
                    <div key={it.pid} style={{ marginLeft: i ? -10 : 0, borderRadius: 11, border: "2px solid var(--surface)" }}>
                      <ProductThumb p={it} size={38} radius={9} />
                    </div>
                  ))}
                </div>
                <div style={{ flex: 1, marginLeft: 12 }}>
                  <div style={{ fontSize: 12.5, color: "var(--ink-3)" }}>{o.date} · {o.items.length} items</div>
                </div>
                <span className="mono" style={{ fontSize: 16, fontWeight: 700 }}>{F.inr(S.orderTotal(o))}</span>
              </div>
            </Card>
          );
        })}
        {list.length === 0 && <div style={{ textAlign: "center", color: "var(--ink-4)", padding: "50px 0", fontSize: 14 }}>No {seg.toLowerCase()} orders</div>}
      </div>
    </div>
  );
}

function ShopProfile({ S, me }) {
  const F = S.F; const dealer = F.lookups.dealer(me.dealer);
  const rows = [
    { ic: "pin", t: "Store address", s: me.area + ", Mumbai" },
    { ic: "truck", t: "My dealer", s: dealer.name },
    { ic: "card", t: "Payment methods", s: "UPI · 2 cards" },
    { ic: "receipt", t: "GST details", s: "27ABCDE1234F1Z5" },
    { ic: "bell", t: "Notifications", s: "Order & delivery alerts" },
    { ic: "settings", t: "Help & support", s: "Chat, call, FAQs" },
  ];
  return (
    <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 14 }}>
      <Card style={{ display: "flex", alignItems: "center", gap: 14 }}>
        <Avatar name={me.owner} size={56} />
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 18, fontWeight: 700, letterSpacing: "-.02em" }}>{me.store}</div>
          <div style={{ fontSize: 13, color: "var(--ink-3)" }}>{me.owner} · {me.phone}</div>
        </div>
        <Badge status="Active" size="sm" dot />
      </Card>
      <div style={{ display: "flex", gap: 10 }}>
        {[{ l: "Orders", v: me.orders }, { l: "Delivered", v: S.myOrders.filter(o => o.status === "delivered").length }, { l: "Active", v: S.myOrders.filter(o => o.status !== "delivered").length }].map(s => (
          <Card key={s.l} pad={14} style={{ flex: 1, textAlign: "center" }}>
            <div className="mono" style={{ fontSize: 22, fontWeight: 700 }}>{s.v}</div>
            <div style={{ fontSize: 11.5, color: "var(--ink-4)", marginTop: 3, fontWeight: 600 }}>{s.l}</div>
          </Card>
        ))}
      </div>
      <Card pad={6}>
        {rows.map((r, i) => (
          <Row key={r.t} last={i === rows.length - 1}
            left={<div style={{ width: 38, height: 38, borderRadius: 11, background: "var(--surface-2)", color: "var(--ink-2)", display: "grid", placeItems: "center" }}><Icon name={r.ic} size={18} stroke={2} /></div>}
            title={r.t} sub={r.s} right={<Icon name="chevR" size={17} color="var(--ink-4)" />} onClick={() => {}} />
        ))}
      </Card>
    </div>
  );
}

/* ---------------- GST INVOICE ---------------- */
function InvoiceSheet({ S, id, onClose }) {
  const F = S.F; const o = S.orders.find(x => x.id === id);
  if (!o) return null;
  const t = F.orderMath(o.items);
  const me = F.lookups.shop(o.shop); const dealer = F.lookups.dealer(o.dealer);
  return (
    <Sheet open onClose={onClose} title="Tax invoice">
      <div style={{ padding: "0 16px" }}>
        <div style={{ border: "1px solid var(--line)", borderRadius: 16, overflow: "hidden" }}>
          {/* invoice header */}
          <div style={{ padding: 18, background: "var(--ink-surface)", color: "#fff" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
              <Logo size={24} light />
              <div style={{ textAlign: "right" }}>
                <div style={{ fontSize: 11, opacity: .7 }}>Invoice</div>
                <div className="mono" style={{ fontSize: 14, fontWeight: 700 }}>INV-{o.id.replace("ORD-", "")}</div>
              </div>
            </div>
            <div style={{ fontSize: 11, opacity: .6, marginTop: 12 }}>GSTIN 27AAFCF1234M1Z9 · {o.date}, 2026</div>
          </div>
          {/* parties */}
          <div style={{ display: "flex", padding: 16, gap: 12, borderBottom: "1px solid var(--line)" }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 10.5, color: "var(--ink-4)", fontWeight: 700, textTransform: "uppercase", letterSpacing: ".04em" }}>Billed to</div>
              <div style={{ fontSize: 13.5, fontWeight: 700, marginTop: 4 }}>{me.store}</div>
              <div style={{ fontSize: 11.5, color: "var(--ink-3)" }}>{me.area}, Mumbai</div>
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 10.5, color: "var(--ink-4)", fontWeight: 700, textTransform: "uppercase", letterSpacing: ".04em" }}>Supplied by</div>
              <div style={{ fontSize: 13.5, fontWeight: 700, marginTop: 4 }}>{dealer.name}</div>
              <div style={{ fontSize: 11.5, color: "var(--ink-3)" }}>{dealer.area}</div>
            </div>
          </div>
          {/* line items */}
          <div style={{ padding: "6px 16px" }}>
            <div style={{ display: "flex", fontSize: 10.5, color: "var(--ink-4)", fontWeight: 700, textTransform: "uppercase", letterSpacing: ".03em", padding: "8px 0", borderBottom: "1px solid var(--line)" }}>
              <span style={{ flex: 1 }}>Item</span><span style={{ width: 34, textAlign: "right" }}>Qty</span><span style={{ width: 70, textAlign: "right" }}>Amount</span>
            </div>
            {o.items.map(it => (
              <div key={it.pid} style={{ display: "flex", alignItems: "center", padding: "9px 0", borderBottom: "1px solid var(--line)" }}>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 12.5, fontWeight: 600 }}>{it.name}</div>
                  <div className="mono" style={{ fontSize: 10.5, color: "var(--ink-4)" }}>GST {it.gst}% · {it.disc}% disc</div>
                </div>
                <span className="mono" style={{ width: 34, textAlign: "right", fontSize: 12.5, fontWeight: 600 }}>{it.qty}</span>
                <span className="mono" style={{ width: 70, textAlign: "right", fontSize: 12.5, fontWeight: 700 }}>{F.inr(F.lineMath(it).total)}</span>
              </div>
            ))}
          </div>
          {/* totals */}
          <div style={{ padding: "4px 16px 16px" }}>
            <MoneyRow label="Taxable value" value={F.inr(t.sub - t.disc)} />
            <MoneyRow label="CGST + SGST" value={F.inr(t.gst)} />
            <div style={{ borderTop: "1px dashed var(--line-2)", margin: "4px 0" }} />
            <MoneyRow strong label="Total" value={F.inr(t.total)} />
          </div>
        </div>
        <div style={{ display: "flex", gap: 10, marginTop: 16 }}>
          <Button variant="outline" full icon="download" onClick={() => { S.notify("Invoice downloaded (PDF)"); }}>Download PDF</Button>
          <Button variant="dark" full icon="mail" onClick={() => { S.notify("Invoice sent to email"); }}>Email</Button>
        </div>
      </div>
    </Sheet>
  );
}

Object.assign(window, { OrdersTab, ShopProfile, InvoiceSheet });
