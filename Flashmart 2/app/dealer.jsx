/* ============================================================
   Flashmart — Dealer app (mobile)
   ============================================================ */
function DealerApp() {
  const S = useStore(); const F = S.F;
  const me = F.lookups.dealer(F.ME.dealer);
  const [tab, setTab] = React.useState("home");
  const [detail, setDetail] = React.useState(null);
  const scrollRef = React.useRef(null);
  React.useEffect(() => { if (scrollRef.current) scrollRef.current.scrollTop = 0; }, [tab, detail]);

  const orders = S.dealerOrders;
  const pending = orders.filter(o => o.status === "placed");
  const active = orders.filter(o => o.status === "accepted" || o.status === "out");
  const todayRev = orders.filter(o => o.paid).reduce((a, o) => a + S.orderTotal(o), 0);
  const lowStock = F.DEALER_STOCK.filter(s => s.qty <= s.reorder);

  const nav = (
    <BottomNav active={tab} onChange={(x) => { setDetail(null); setTab(x); }}
      items={[
        { id: "home", icon: "home", label: "Home" },
        { id: "orders", icon: "bag", label: "Orders", badge: pending.length || null },
        { id: "stock", icon: "box", label: "Stock", badge: lowStock.length || null },
        { id: "profile", icon: "user", label: "Profile" },
      ]} />
  );

  if (detail) return <><DealerOrderDetail S={S} id={detail} back={() => setDetail(null)} scrollRef={scrollRef} /><Toast /></>;

  let header, body;
  if (tab === "home") {
    header = <AppHeader pad title={me.name.split(" ").slice(0, 2).join(" ")} kicker="Dealer dashboard"
      right={<><IconBtn name="bell" badge={pending.length || null} /><Avatar name={me.owner} tint="#0e9e6e" /></>} />;
    body = <DealerHome S={S} me={me} pending={pending} active={active} todayRev={todayRev} lowStock={lowStock} setTab={setTab} setDetail={setDetail} />;
  } else if (tab === "orders") {
    header = <AppHeader pad title="Orders" subtitle={pending.length + " awaiting action"} />;
    body = <DealerOrders S={S} orders={orders} setDetail={setDetail} />;
  } else if (tab === "stock") {
    header = <AppHeader pad title="Stock" subtitle={F.DEALER_STOCK.length + " SKUs · " + lowStock.length + " low"} />;
    body = <DealerStock S={S} />;
  } else {
    header = <AppHeader pad title="Profile" />;
    body = <DealerProfile S={S} me={me} todayRev={todayRev} />;
  }
  return <><PhoneShell header={header} nav={nav} scrollRef={scrollRef}>{body}</PhoneShell><Toast /></>;
}

function DealerHome({ S, me, pending, active, todayRev, lowStock, setTab, setDetail }) {
  const F = S.F;
  const recent = S.dealerOrders.slice(0, 5);
  return (
    <div className="stagger" style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 16 }}>
      {/* revenue hero */}
      <div style={{ borderRadius: 22, padding: 20, color: "#fff", position: "relative", overflow: "hidden",
        background: "linear-gradient(150deg, #0e9e6e, #086b4b)", boxShadow: "var(--sh-lg)" }}>
        <div style={{ position: "absolute", right: -28, top: -28, width: 150, height: 150, borderRadius: "50%", background: "rgba(255,255,255,.08)" }} />
        <div style={{ fontSize: 13, fontWeight: 600, opacity: .85 }}>Today's revenue</div>
        <div className="mono" style={{ fontSize: 38, fontWeight: 700, letterSpacing: "-.03em", margin: "6px 0 4px" }}>{F.inr(todayRev)}</div>
        <div style={{ fontSize: 12.5, opacity: .82 }}>{S.dealerOrders.filter(o => o.paid).length} paid orders · {me.area}</div>
      </div>
      {/* kpi row */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 11 }}>
        <Stat label="Pending orders" value={pending.length} icon="clock" accent="var(--warn)" tint="var(--warn-tint)" sub="Need your action" />
        <Stat label="In transit" value={active.length} icon="truck" accent="var(--brand)" sub="Accepted / out" />
      </div>
      {/* pending action strip */}
      {pending.length > 0 && (
        <div>
          <SectionLabel action="View all" onAction={() => setTab("orders")}>Awaiting confirmation</SectionLabel>
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            {pending.slice(0, 2).map(o => <DealerOrderCard key={o.id} S={S} o={o} onOpen={() => setDetail(o.id)} />)}
          </div>
        </div>
      )}
      {/* low stock */}
      {lowStock.length > 0 && (
        <Card onClick={() => setTab("stock")} style={{ display: "flex", alignItems: "center", gap: 13, borderColor: "var(--warn)", background: "var(--warn-tint)" }}>
          <div style={{ width: 42, height: 42, borderRadius: 12, background: "#fff", color: "var(--warn)", display: "grid", placeItems: "center" }}><Icon name="box" size={21} stroke={2} /></div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 14.5, fontWeight: 700, color: "var(--warn)" }}>{lowStock.length} items low on stock</div>
            <div style={{ fontSize: 12.5, color: "#9a6410" }}>Reorder to avoid stockouts</div>
          </div>
          <Icon name="chevR" size={18} color="var(--warn)" />
        </Card>
      )}
      <div>
        <SectionLabel action="See all" onAction={() => setTab("orders")}>Recent orders</SectionLabel>
        <Card pad={4}>
          {recent.map((o, i) => {
            const shop = F.lookups.shop(o.shop);
            return (
              <Row key={o.id} last={i === recent.length - 1} onClick={() => setDetail(o.id)}
                left={<Avatar name={shop.store} size={38} tint="#0e9e6e" />}
                title={shop.store} sub={<span className="mono">{o.id} · {o.items.length} items</span>}
                right={<Badge status={o.status === "placed" ? "pending" : o.status} size="sm" />} />
            );
          })}
        </Card>
      </div>
    </div>
  );
}

/* order card with contextual action */
function DealerOrderCard({ S, o, onOpen }) {
  const F = S.F; const shop = F.lookups.shop(o.shop);
  const fresh = S.flash === o.id;
  const action = {
    placed: { label: "Accept order", variant: "primary", fn: () => S.dealerAccept(o.id) },
    accepted: { label: "Mark out for delivery", variant: "primary", fn: () => S.dealerOut(o.id) },
    out: { label: "Mark delivered", variant: "pos", fn: () => S.dealerDeliver(o.id) },
    delivered: null,
  }[o.status];
  return (
    <Card style={{ boxShadow: fresh ? "0 0 0 3px var(--pos-tint)" : "var(--sh-sm)", borderColor: fresh ? "var(--pos)" : "var(--line)" }}>
      <div className="tap" onClick={onOpen} style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 12 }}>
        <Avatar name={shop.store} size={42} tint="#0e9e6e" />
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: 14.5, fontWeight: 700, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{shop.store}</div>
          <div style={{ fontSize: 12.5, color: "var(--ink-3)" }} className="mono">{o.id} · {shop.area}</div>
        </div>
        <div style={{ textAlign: "right" }}>
          <div className="mono" style={{ fontSize: 15, fontWeight: 700 }}>{F.inr(S.orderTotal(o))}</div>
          {o.fresh && <span style={{ fontSize: 10, fontWeight: 700, color: "var(--pos)" }}>● new</span>}
        </div>
      </div>
      <div style={{ display: "flex", gap: 6, flexWrap: "wrap", marginBottom: action ? 12 : 0 }}>
        {o.items.slice(0, 3).map(it => (
          <span key={it.pid} style={{ fontSize: 11.5, fontWeight: 600, color: "var(--ink-2)", background: "var(--surface-2)", padding: "4px 9px", borderRadius: 8, whiteSpace: "nowrap" }} className="mono">{it.qty} × {it.name.split(" ").slice(0, 2).join(" ")}</span>
        ))}
        {o.items.length > 3 && <span style={{ fontSize: 11.5, fontWeight: 600, color: "var(--ink-4)", padding: "4px 4px" }}>+{o.items.length - 3}</span>}
      </div>
      {action ? <Button variant={action.variant} full size="sm" icon={o.status === "out" ? "check" : "arrowR"} onClick={action.fn}>{action.label}</Button>
        : <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 8, color: "var(--pos)", fontWeight: 700, fontSize: 13.5, padding: "8px 0" }}><Icon name="check" size={17} stroke={2.6} /> Delivered & settled</div>}
    </Card>
  );
}

function DealerOrders({ S, orders, setDetail }) {
  const [seg, setSeg] = React.useState("Pending");
  const map = { Pending: o => o.status === "placed", Active: o => o.status === "accepted" || o.status === "out", Done: o => o.status === "delivered" };
  const list = orders.filter(map[seg]);
  return (
    <div style={{ padding: "0 16px" }}>
      <div style={{ marginBottom: 14 }}><Segmented full value={seg} onChange={setSeg} options={["Pending", "Active", "Done"]} /></div>
      <div className="stagger" style={{ display: "flex", flexDirection: "column", gap: 11 }}>
        {list.map(o => <DealerOrderCard key={o.id} S={S} o={o} onOpen={() => setDetail(o.id)} />)}
        {list.length === 0 && <div style={{ textAlign: "center", color: "var(--ink-4)", padding: "50px 0", fontSize: 14 }}>No {seg.toLowerCase()} orders</div>}
      </div>
    </div>
  );
}

window.DealerApp = DealerApp;
Object.assign(window, { DealerHome, DealerOrderCard, DealerOrders });
