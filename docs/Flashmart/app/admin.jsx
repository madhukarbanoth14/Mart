/* ============================================================
   Flashmart — Admin desktop (web)
   ============================================================ */
const ADMIN_NAV = [
  { id: "overview", icon: "chart", label: "Overview" },
  { id: "orders", icon: "bag", label: "Orders" },
  { id: "products", icon: "box", label: "Products" },
  { id: "users", icon: "users", label: "Users" },
  { id: "reports", icon: "doc", label: "Reports" },
];
const ADMIN_TITLE = {
  overview: ["Overview", "Live performance across your distribution network"],
  orders: ["Orders", "Every order flowing through Flashmart"],
  products: ["Product catalog", "Manage SKUs, pricing & GST"],
  users: ["Network", "Dealers, shopkeepers & field staff"],
  reports: ["Reports", "Exports & business intelligence"],
};

function AdminApp({ layout }) {
  const S = useStore(); const F = S.F;
  const [view, setView] = React.useState("overview");
  const scrollRef = React.useRef(null);
  React.useEffect(() => { if (scrollRef.current) scrollRef.current.scrollTop = 0; }, [view]);
  const [title, sub] = ADMIN_TITLE[view];

  return (
    <div style={{ display: "flex", height: "100%", background: "var(--bg)", fontFamily: "var(--font)", color: "var(--ink)" }}>
      {/* sidebar */}
      <div style={{ width: 234, flexShrink: 0, background: "var(--ink-surface)", display: "flex", flexDirection: "column", padding: "22px 16px" }}>
        <div style={{ padding: "4px 8px 22px" }}><Logo size={26} light /></div>
        <div style={{ display: "flex", flexDirection: "column", gap: 3 }}>
          {ADMIN_NAV.map(n => {
            const on = n.id === view;
            return (
              <button key={n.id} className="tap" onClick={() => setView(n.id)}
                style={{ display: "flex", alignItems: "center", gap: 12, border: "none", cursor: "pointer", textAlign: "left",
                  padding: "11px 12px", borderRadius: 11, fontSize: 14, fontWeight: 600, letterSpacing: "-.01em",
                  background: on ? "rgba(255,255,255,.1)" : "transparent", color: on ? "#fff" : "rgba(255,255,255,.55)" }}>
                <Icon name={n.icon} size={19} stroke={2} color={on ? "var(--brand)" : "rgba(255,255,255,.5)"} />
                {n.label}
                {n.id === "orders" && S.kpis.live.orders > 0 && <span className="mono" style={{ marginLeft: "auto", fontSize: 11, fontWeight: 700, background: "var(--brand)", color: "#fff", padding: "1px 7px", borderRadius: 99 }}>{S.kpis.live.orders}</span>}
              </button>
            );
          })}
        </div>
        <div style={{ marginTop: "auto", display: "flex", flexDirection: "column", gap: 14 }}>
          <div style={{ padding: 14, borderRadius: 14, background: "rgba(255,255,255,.06)", border: "1px solid rgba(255,255,255,.08)" }}>
            <div style={{ fontSize: 12, color: "rgba(255,255,255,.6)", fontWeight: 600 }}>Network health</div>
            <div style={{ display: "flex", alignItems: "center", gap: 8, marginTop: 8 }}>
              <span style={{ width: 8, height: 8, borderRadius: 99, background: "var(--pos)", boxShadow: "0 0 0 4px rgba(14,158,110,.25)" }} />
              <span style={{ fontSize: 13, fontWeight: 700, color: "#fff" }}>All systems live</span>
            </div>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 11, padding: "4px 6px" }}>
            <Avatar name="Priya Kapoor" size={38} tint="var(--brand)" />
            <div style={{ minWidth: 0 }}>
              <div style={{ fontSize: 13.5, fontWeight: 700, color: "#fff" }}>Priya Kapoor</div>
              <div style={{ fontSize: 11.5, color: "rgba(255,255,255,.5)" }}>Operations Admin</div>
            </div>
            <Icon name="logout" size={17} color="rgba(255,255,255,.4)" style={{ marginLeft: "auto" }} />
          </div>
        </div>
      </div>

      {/* main */}
      <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column" }}>
        {/* topbar */}
        <div style={{ display: "flex", alignItems: "center", gap: 16, padding: "18px 28px", borderBottom: "1px solid var(--line)", background: "var(--surface)", flexShrink: 0 }}>
          <div style={{ flex: 1 }}>
            <h1 style={{ margin: 0, fontSize: 22, fontWeight: 700, letterSpacing: "-.025em" }}>{title}</h1>
            <div style={{ fontSize: 13, color: "var(--ink-3)", marginTop: 2 }}>{sub}</div>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 10, height: 42, padding: "0 14px", background: "var(--surface-2)", borderRadius: 12, border: "1px solid var(--line)", width: 240 }}>
            <Icon name="search" size={18} color="var(--ink-4)" />
            <input placeholder="Search orders, dealers…" style={{ flex: 1, border: "none", outline: "none", background: "transparent", fontFamily: "inherit", fontSize: 14 }} />
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 8, height: 42, padding: "0 14px", background: "var(--surface-2)", borderRadius: 12, border: "1px solid var(--line)", fontSize: 13, fontWeight: 600, color: "var(--ink-2)" }}>
            <Icon name="clock" size={17} color="var(--ink-4)" /> Jun 2026
          </div>
          <button className="tap" style={{ width: 42, height: 42, borderRadius: 12, border: "1px solid var(--line)", background: "var(--surface-2)", display: "grid", placeItems: "center", cursor: "pointer", position: "relative" }}>
            <Icon name="bell" size={19} color="var(--ink-2)" />
            <span style={{ position: "absolute", top: 8, right: 9, width: 8, height: 8, borderRadius: 99, background: "var(--neg)", border: "2px solid var(--surface-2)" }} />
          </button>
        </div>

        {/* content */}
        <div ref={scrollRef} className="fm-scroll" style={{ flex: 1, overflowY: "auto", padding: 28 }}>
          {view === "overview" && <AdminOverview S={S} layout={layout} setView={setView} />}
          {view === "orders" && <AdminOrders S={S} />}
          {view === "products" && <AdminProducts S={S} />}
          {view === "users" && <AdminUsers S={S} />}
          {view === "reports" && <AdminReports S={S} />}
        </div>
      </div>
    </div>
  );
}

/* ---- KPI card (desktop) ---- */
function AdminKpi({ label, value, icon, accent = "var(--brand)", tint = "var(--brand-tint)", delta, deltaLabel, spark }) {
  return (
    <div style={{ background: "var(--surface)", border: "1px solid var(--line)", borderRadius: 18, padding: 20, boxShadow: "var(--sh-sm)", display: "flex", flexDirection: "column", gap: 14 }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <span style={{ fontSize: 13.5, fontWeight: 600, color: "var(--ink-3)" }}>{label}</span>
        <div style={{ width: 38, height: 38, borderRadius: 11, background: tint, color: accent, display: "grid", placeItems: "center" }}><Icon name={icon} size={20} stroke={2} /></div>
      </div>
      <div>
        <div className="mono" style={{ fontSize: 32, fontWeight: 700, letterSpacing: "-.03em", lineHeight: 1 }}>{value}</div>
        <div style={{ display: "flex", alignItems: "center", gap: 7, marginTop: 9 }}>
          {delta != null && <span style={{ fontSize: 12.5, fontWeight: 700, color: "var(--pos)", background: "var(--pos-tint)", padding: "2px 8px", borderRadius: 7 }}>▲ {delta}</span>}
          <span style={{ fontSize: 12.5, color: "var(--ink-4)" }}>{deltaLabel}</span>
        </div>
      </div>
    </div>
  );
}

window.AdminApp = AdminApp;
Object.assign(window, { AdminKpi, ADMIN_NAV });
