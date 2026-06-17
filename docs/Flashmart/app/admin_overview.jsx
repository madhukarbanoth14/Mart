/* ============================================================
   Flashmart — Admin overview (3 layout variations) + panels
   ============================================================ */

function kpiData(S) {
  const F = S.F; const k = S.kpis; const live = k.live;
  const aov = Math.round(k.revenue / k.orders);
  return [
    { key: "revenue", label: "Total revenue", value: F.inrShort(k.revenue), icon: "wallet", accent: "var(--pos)", tint: "var(--pos-tint)",
      delta: live.revenue > 0 ? "+" + F.inrShort(live.revenue) : "18.4%", deltaLabel: live.revenue > 0 ? "live today" : "vs last month" },
    { key: "orders", label: "Total orders", value: F.num(k.orders), icon: "bag", accent: "var(--brand)", tint: "var(--brand-tint)",
      delta: live.orders > 0 ? "+" + live.orders : "12.1%", deltaLabel: live.orders > 0 ? "new today" : "vs last month" },
    { key: "dealers", label: "Active dealers", value: F.num(k.dealers), icon: "truck", accent: "#c97a16", tint: "var(--warn-tint)",
      delta: live.dealers > 0 ? "+" + live.dealers : "2", deltaLabel: live.dealers > 0 ? "just onboarded" : "this week" },
    { key: "shops", label: "Shopkeepers", value: F.num(k.shops), icon: "bag", accent: "#6d4ad4", tint: "#f0ebfc",
      delta: live.shops > 0 ? "+" + live.shops : "24", deltaLabel: live.shops > 0 ? "just onboarded" : "this week" },
    { key: "aov", label: "Avg. order value", value: F.inr(aov), icon: "chart", accent: "var(--ink)", tint: "var(--surface-3)",
      delta: "4.6%", deltaLabel: "vs last month" },
  ];
}

/* ---- shared panels ---- */
function Panel({ title, action, onAction, children, style, pad = 22, dark }) {
  return (
    <div style={{ background: "var(--surface)", border: "1px solid var(--line)", borderRadius: 18, padding: pad, boxShadow: "var(--sh-sm)", ...style }}>
      {title && <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 18 }}>
        <h3 style={{ margin: 0, fontSize: 16, fontWeight: 700, letterSpacing: "-.02em", color: dark ? "#fff" : "var(--ink)" }}>{title}</h3>
        {action && <button className="tap" onClick={onAction} style={{ border: "none", background: dark ? "rgba(255,255,255,.1)" : "var(--surface-2)", color: dark ? "#fff" : "var(--ink-2)", padding: "6px 12px", borderRadius: 9, fontSize: 12.5, fontWeight: 600, cursor: "pointer", display: "flex", alignItems: "center", gap: 6 }}>{action} <Icon name="chevR" size={13} /></button>}
      </div>}
      {children}
    </div>
  );
}

function SalesChartPanel({ S, height = 200, dark }) {
  const F = S.F; const data = F.MONTHLY_SALES;
  const total = data.reduce((a, d) => a + d.v, 0);
  return (
    <Panel title="Monthly sales" action="Export" onAction={() => S.notify("Sales report exported (CSV)")} dark={dark}
      style={dark ? { background: "var(--ink-surface)", border: "none" } : null}>
      <div style={{ display: "flex", alignItems: "baseline", gap: 12, marginTop: -6, marginBottom: 14 }}>
        <span className="mono" style={{ fontSize: 26, fontWeight: 700, letterSpacing: "-.03em", color: dark ? "#fff" : "var(--ink)" }}>{F.inrShort(total)}</span>
        <span style={{ fontSize: 13, fontWeight: 700, color: "var(--pos)" }}>▲ 18.4%</span>
        <span style={{ fontSize: 12.5, color: dark ? "rgba(255,255,255,.5)" : "var(--ink-4)" }}>first half 2026</span>
      </div>
      <BarChart data={data} height={height} fmt={v => F.inrShort(v)} dark={dark} />
    </Panel>
  );
}

function CategoryPanel({ S }) {
  return <Panel title="Category mix"><Donut data={S.F.CATEGORY_MIX} size={140} /></Panel>;
}

function TopDealersPanel({ S, setView }) {
  const F = S.F;
  const dealers = [...S.dealers].sort((a, b) => b.revenue - a.revenue).slice(0, 5);
  const max = dealers[0].revenue || 1;
  return (
    <Panel title="Top dealers" action="All dealers" onAction={() => setView && setView("users")}>
      <div style={{ display: "flex", flexDirection: "column", gap: 15 }}>
        {dealers.map((d, i) => (
          <div key={d.id} style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <span className="mono" style={{ fontSize: 12, fontWeight: 700, color: "var(--ink-4)", width: 16 }}>{i + 1}</span>
            <Avatar name={d.name} size={34} tint="#0e9e6e" />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 13.5, fontWeight: 600, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{d.name}</div>
              <div style={{ height: 5, borderRadius: 99, background: "var(--surface-3)", marginTop: 5, overflow: "hidden" }}>
                <div style={{ width: (d.revenue / max * 100) + "%", height: "100%", background: "var(--pos)", borderRadius: 99 }} />
              </div>
            </div>
            <span className="mono" style={{ fontSize: 13, fontWeight: 700 }}>{F.inrShort(d.revenue)}</span>
          </div>
        ))}
      </div>
    </Panel>
  );
}

function ActivityPanel({ S }) {
  const F = S.F;
  const evts = S.orders.slice(0, 6).map(o => {
    const shop = F.lookups.shop(o.shop);
    const map = { placed: ["placed an order", "var(--brand)", "bag"], accepted: ["order accepted", "var(--brand)", "check"], out: ["out for delivery", "var(--warn)", "truck"], delivered: ["order delivered", "var(--pos)", "check"] };
    const [verb, col, ic] = map[o.status];
    return { id: o.id, who: shop.store, verb, col, ic, amt: F.inr(S.orderTotal(o)), fresh: o.fresh, date: o.date };
  });
  return (
    <Panel title="Live activity">
      <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
        {evts.map((e, i) => (
          <div key={e.id + i} style={{ display: "flex", alignItems: "center", gap: 12, padding: "9px 0", borderBottom: i === evts.length - 1 ? "none" : "1px solid var(--line)" }}>
            <div style={{ width: 32, height: 32, borderRadius: 9, background: "var(--surface-2)", color: e.col, display: "grid", placeItems: "center", flexShrink: 0 }}><Icon name={e.ic} size={16} stroke={2.2} /></div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 13, color: "var(--ink-2)" }}><b style={{ color: "var(--ink)", fontWeight: 700 }}>{e.who}</b> {e.verb}</div>
              <div className="mono" style={{ fontSize: 11.5, color: "var(--ink-4)" }}>{e.id} · {e.date}</div>
            </div>
            {e.fresh && <span style={{ width: 7, height: 7, borderRadius: 99, background: "var(--pos)", flexShrink: 0 }} />}
            <span className="mono" style={{ fontSize: 12.5, fontWeight: 700 }}>{e.amt}</span>
          </div>
        ))}
      </div>
    </Panel>
  );
}

function QuickActions({ S, setView }) {
  const acts = [
    { ic: "upload", l: "Upload products", v: "products" },
    { ic: "users", l: "Manage users", v: "users" },
    { ic: "bag", l: "View orders", v: "orders" },
    { ic: "doc", l: "Reports", v: "reports" },
  ];
  return (
    <div style={{ display: "grid", gridTemplateColumns: "repeat(4,1fr)", gap: 12 }}>
      {acts.map(a => (
        <button key={a.l} className="tap" onClick={() => setView(a.v)}
          style={{ background: "var(--surface)", border: "1px solid var(--line)", borderRadius: 14, padding: "16px", cursor: "pointer",
            display: "flex", alignItems: "center", gap: 11, boxShadow: "var(--sh-sm)", textAlign: "left" }}>
          <div style={{ width: 38, height: 38, borderRadius: 11, background: "var(--brand-tint)", color: "var(--brand)", display: "grid", placeItems: "center" }}><Icon name={a.ic} size={19} stroke={2} /></div>
          <span style={{ fontSize: 13.5, fontWeight: 600 }}>{a.l}</span>
        </button>
      ))}
    </div>
  );
}

/* ============================================================
   LAYOUT VARIATIONS
   ============================================================ */
function AdminOverview({ S, layout, setView }) {
  if (layout === "Grid") return <OverviewGrid S={S} setView={setView} />;
  if (layout === "Analytics") return <OverviewAnalytics S={S} setView={setView} />;
  return <OverviewSpotlight S={S} setView={setView} />;
}

/* ---- 1. SPOTLIGHT: dark hero revenue + supporting cards ---- */
function OverviewSpotlight({ S, setView }) {
  const F = S.F; const kpis = kpiData(S); const k = S.kpis;
  const rev = kpis[0];
  return (
    <div className="stagger" style={{ display: "flex", flexDirection: "column", gap: 18 }}>
      <div style={{ display: "grid", gridTemplateColumns: "1.4fr 1fr", gap: 18 }}>
        {/* hero */}
        <div style={{ borderRadius: 22, padding: 28, color: "#fff", position: "relative", overflow: "hidden",
          background: "linear-gradient(140deg, var(--ink-surface), var(--ink-surface-2))", boxShadow: "var(--sh-lg)" }}>
          <div style={{ position: "absolute", right: -40, top: -40, width: 200, height: 200, borderRadius: "50%", background: "radial-gradient(circle, var(--brand) 0%, transparent 70%)", opacity: .4 }} />
          <div style={{ position: "relative" }}>
            <div style={{ display: "flex", alignItems: "center", gap: 9, fontSize: 13.5, fontWeight: 600, color: "rgba(255,255,255,.6)" }}>
              <span style={{ width: 8, height: 8, borderRadius: 99, background: "var(--pos)", boxShadow: "0 0 0 4px rgba(14,158,110,.3)" }} /> Total revenue · FY 2026
            </div>
            <div className="mono" style={{ fontSize: 54, fontWeight: 700, letterSpacing: "-.04em", margin: "10px 0 6px" }}>{F.inr(k.revenue)}</div>
            <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
              <span style={{ fontSize: 13.5, fontWeight: 700, color: "#fff", background: "rgba(14,158,110,.25)", padding: "4px 11px", borderRadius: 8 }}>▲ 18.4% YoY</span>
              <span style={{ fontSize: 13, color: "rgba(255,255,255,.55)" }}>{rev.deltaLabel === "live today" ? rev.delta + " booked live" : "Tracking above target"}</span>
            </div>
            <div style={{ marginTop: 24 }}><BarChart data={F.MONTHLY_SALES} height={140} fmt={v => F.inrShort(v)} dark /></div>
          </div>
        </div>
        {/* mini kpis */}
        <div style={{ display: "grid", gridTemplateRows: "repeat(2,1fr)", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
          {[kpis[1], kpis[2], kpis[3], kpis[4]].map(m => <AdminKpi key={m.key} {...m} />)}
        </div>
      </div>
      <QuickActions S={S} setView={setView} />
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 18 }}>
        <CategoryPanel S={S} />
        <TopDealersPanel S={S} setView={setView} />
        <ActivityPanel S={S} />
      </div>
    </div>
  );
}

/* ---- 2. GRID: classic balanced KPI grid ---- */
function OverviewGrid({ S, setView }) {
  const kpis = kpiData(S).slice(0, 4);
  return (
    <div className="stagger" style={{ display: "flex", flexDirection: "column", gap: 18 }}>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4,1fr)", gap: 16 }}>
        {kpis.map(m => <AdminKpi key={m.key} {...m} />)}
      </div>
      <div style={{ display: "grid", gridTemplateColumns: "1.7fr 1fr", gap: 18 }}>
        <SalesChartPanel S={S} height={230} />
        <CategoryPanel S={S} />
      </div>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 18 }}>
        <TopDealersPanel S={S} setView={setView} />
        <ActivityPanel S={S} />
      </div>
    </div>
  );
}

/* ---- 3. ANALYTICS: dense, chart-forward ---- */
function OverviewAnalytics({ S, setView }) {
  const F = S.F; const kpis = kpiData(S);
  const statuses = ["placed", "accepted", "out", "delivered"];
  const counts = statuses.map(s => ({ s, n: S.orders.filter(o => o.status === s).length }));
  const maxC = Math.max(...counts.map(c => c.n), 1);
  return (
    <div className="stagger" style={{ display: "flex", flexDirection: "column", gap: 18 }}>
      {/* compact strip */}
      <div style={{ background: "var(--surface)", border: "1px solid var(--line)", borderRadius: 18, boxShadow: "var(--sh-sm)", display: "flex" }}>
        {kpis.map((m, i) => (
          <div key={m.key} style={{ flex: 1, padding: "18px 22px", borderLeft: i ? "1px solid var(--line)" : "none" }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <div style={{ width: 28, height: 28, borderRadius: 8, background: m.tint, color: m.accent, display: "grid", placeItems: "center" }}><Icon name={m.icon} size={15} stroke={2.2} /></div>
              <span style={{ fontSize: 12.5, fontWeight: 600, color: "var(--ink-3)" }}>{m.label}</span>
            </div>
            <div className="mono" style={{ fontSize: 26, fontWeight: 700, letterSpacing: "-.03em", marginTop: 10 }}>{m.value}</div>
            <span style={{ fontSize: 11.5, fontWeight: 700, color: "var(--pos)" }}>▲ {m.delta}</span>
          </div>
        ))}
      </div>
      <div style={{ display: "grid", gridTemplateColumns: "2fr 1fr", gap: 18 }}>
        <SalesChartPanel S={S} height={250} dark />
        <Panel title="Order pipeline">
          <div style={{ display: "flex", flexDirection: "column", gap: 16, marginTop: 2 }}>
            {counts.map(c => (
              <div key={c.s}>
                <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                  <Badge status={c.s === "placed" ? "pending" : c.s} size="sm" />
                  <span className="mono" style={{ fontSize: 13, fontWeight: 700 }}>{c.n}</span>
                </div>
                <div style={{ height: 8, borderRadius: 99, background: "var(--surface-3)", overflow: "hidden" }}>
                  <div style={{ width: (c.n / maxC * 100) + "%", height: "100%", borderRadius: 99, background: "var(--brand)" }} />
                </div>
              </div>
            ))}
          </div>
        </Panel>
      </div>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 18 }}>
        <CategoryPanel S={S} />
        <TopDealersPanel S={S} setView={setView} />
        <ActivityPanel S={S} />
      </div>
    </div>
  );
}

Object.assign(window, { AdminOverview, OverviewSpotlight, OverviewGrid, OverviewAnalytics, Panel, SalesChartPanel, CategoryPanel, TopDealersPanel, ActivityPanel, QuickActions, kpiData });
