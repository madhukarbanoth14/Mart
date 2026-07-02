/* ============================================================
   Flashmart — Dealer: order detail, stock, profile
   ============================================================ */

function DealerOrderDetail({ S, id, back, scrollRef }) {
  const F = S.F; const o = S.dealerOrders.find(x => x.id === id) || S.orders.find(x => x.id === id);
  if (!o) return null;
  const t = F.orderMath(o.items); const shop = F.lookups.shop(o.shop);
  const action = {
    placed: { label: "Accept order", variant: "primary", fn: () => { S.dealerAccept(o.id); } },
    accepted: { label: "Mark out for delivery", variant: "primary", fn: () => { S.dealerOut(o.id); } },
    out: { label: "Mark delivered", variant: "pos", fn: () => { S.dealerDeliver(o.id); } },
    delivered: null,
  }[o.status];
  return (
    <>
      <PhoneShell scrollRef={scrollRef}
        header={<AppHeader pad title={o.id} subtitle={o.date} onBack={back} right={<Badge status={o.status === "placed" ? "pending" : o.status} />} />}>
        <div style={{ padding: "4px 16px 110px", display: "flex", flexDirection: "column", gap: 14 }}>
          <Card style={{ display: "flex", alignItems: "center", gap: 13 }}>
            <Avatar name={shop.store} size={46} tint="#0e9e6e" />
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 15.5, fontWeight: 700 }}>{shop.store}</div>
              <div style={{ fontSize: 12.5, color: "var(--ink-3)" }}>{shop.owner} · {shop.area}</div>
            </div>
            <button className="tap" style={{ width: 42, height: 42, borderRadius: 12, border: "1px solid var(--line)", background: "var(--surface)", color: "var(--pos)", display: "grid", placeItems: "center", cursor: "pointer", boxShadow: "var(--sh-sm)" }}><Icon name="phone" size={19} stroke={2} /></button>
          </Card>
          <div>
            <SectionLabel>Items to fulfil · {o.items.length}</SectionLabel>
            <Card pad={6}>
              {o.items.map((it, i) => (
                <div key={it.pid} style={{ display: "flex", gap: 12, alignItems: "center", padding: "10px 8px", borderBottom: i === o.items.length - 1 ? "none" : "1px solid var(--line)" }}>
                  <ProductThumb p={it} size={46} />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 14, fontWeight: 600 }}>{it.name}</div>
                    <div style={{ fontSize: 12, color: "var(--ink-4)" }} className="mono">{it.unit}</div>
                  </div>
                  <span className="mono" style={{ fontSize: 16, fontWeight: 700, background: "var(--surface-2)", padding: "4px 11px", borderRadius: 9 }}>× {it.qty}</span>
                </div>
              ))}
            </Card>
          </div>
          <Card>
            <MoneyRow label="Order value" value={F.inr(t.sub - t.disc)} />
            <MoneyRow label="GST collected" value={F.inr(t.gst)} />
            <div style={{ borderTop: "1px dashed var(--line-2)", margin: "4px 0" }} />
            <MoneyRow strong label="Total" value={F.inr(t.total)} />
            <div style={{ marginTop: 10, display: "flex", alignItems: "center", gap: 8, fontSize: 12.5, color: "var(--pos)", fontWeight: 600 }}>
              <Icon name="check" size={15} stroke={2.6} /> Payment received via Flashmart Pay
            </div>
          </Card>
        </div>
      </PhoneShell>
      <PhoneFooter>
        {action ? <Button variant={action.variant} size="lg" full icon={o.status === "out" ? "check" : "arrowR"} onClick={action.fn}>{action.label}</Button>
          : <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 9, height: 50, color: "var(--pos)", fontWeight: 700, fontSize: 15, background: "var(--pos-tint)", borderRadius: 14 }}><Icon name="check" size={19} stroke={2.6} /> Order completed</div>}
      </PhoneFooter>
    </>
  );
}

function DealerStock({ S }) {
  const F = S.F;
  return (
    <div className="stagger" style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 10 }}>
      {F.DEALER_STOCK.map(s => {
        const p = F.lookups.product(s.pid); const low = s.qty <= s.reorder;
        const pct = Math.min(100, (s.qty / (s.reorder * 3)) * 100);
        return (
          <Card key={s.pid} pad={13} style={{ display: "flex", gap: 13, alignItems: "center" }}>
            <ProductThumb p={p} size={48} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 14, fontWeight: 600, lineHeight: 1.2 }}>{p.name}</div>
              <div style={{ display: "flex", alignItems: "center", gap: 8, margin: "6px 0 3px" }}>
                <div style={{ flex: 1, height: 6, borderRadius: 99, background: "var(--surface-3)", overflow: "hidden" }}>
                  <div style={{ width: pct + "%", height: "100%", borderRadius: 99, background: low ? "var(--neg)" : "var(--pos)" }} />
                </div>
                <span className="mono" style={{ fontSize: 12, fontWeight: 700, color: low ? "var(--neg)" : "var(--ink-2)" }}>{s.qty}</span>
              </div>
              <div style={{ fontSize: 11.5, color: "var(--ink-4)" }}>Reorder at {s.reorder} units</div>
            </div>
            {low && <Button variant="soft" size="sm" icon="refresh" onClick={() => S.notify("Reorder placed for " + p.name)}>Reorder</Button>}
          </Card>
        );
      })}
    </div>
  );
}

function DealerProfile({ S, me, todayRev }) {
  const F = S.F;
  const stats = [{ l: "Lifetime orders", v: me.orders }, { l: "Revenue", v: F.inrShort(me.revenue) }, { l: "Today", v: F.inrShort(todayRev) }];
  const rows = [
    { ic: "pin", t: "Service area", s: me.area },
    { ic: "box", t: "Manage stock", s: F.DEALER_STOCK.length + " SKUs" },
    { ic: "users", t: "My shopkeepers", s: F.SHOPS.filter(x => x.dealer === me.id).length + " stores" },
    { ic: "wallet", t: "Settlements", s: "Daily payout · UPI" },
    { ic: "settings", t: "Help & support", s: "Chat or call" },
  ];
  return (
    <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 14 }}>
      <Card style={{ display: "flex", alignItems: "center", gap: 14 }}>
        <Avatar name={me.owner} size={56} tint="#0e9e6e" />
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 17, fontWeight: 700, letterSpacing: "-.02em" }}>{me.name}</div>
          <div style={{ fontSize: 13, color: "var(--ink-3)" }}>{me.owner} · {me.phone}</div>
        </div>
        <Badge status="Active" size="sm" dot />
      </Card>
      <div style={{ display: "flex", gap: 10 }}>
        {stats.map(s => (
          <Card key={s.l} pad={14} style={{ flex: 1, textAlign: "center" }}>
            <div className="mono" style={{ fontSize: 20, fontWeight: 700 }}>{s.v}</div>
            <div style={{ fontSize: 11, color: "var(--ink-4)", marginTop: 3, fontWeight: 600 }}>{s.l}</div>
          </Card>
        ))}
      </div>
      <Card pad={6}>
        {rows.map((r, i) => (
          <Row key={r.t} last={i === rows.length - 1} onClick={() => {}}
            left={<div style={{ width: 38, height: 38, borderRadius: 11, background: "var(--surface-2)", color: "var(--ink-2)", display: "grid", placeItems: "center" }}><Icon name={r.ic} size={18} stroke={2} /></div>}
            title={r.t} sub={r.s} right={<Icon name="chevR" size={17} color="var(--ink-4)" />} />
        ))}
      </Card>
    </div>
  );
}

Object.assign(window, { DealerOrderDetail, DealerStock, DealerProfile });
