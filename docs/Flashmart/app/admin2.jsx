/* ============================================================
   Flashmart — Admin views: orders, products, users, reports
   ============================================================ */

function THead({ cols }) {
  return (
    <div style={{ display: "grid", gridTemplateColumns: cols.map(c => c.w).join(" "), gap: 16, padding: "0 18px 12px",
      fontSize: 11.5, fontWeight: 700, color: "var(--ink-4)", textTransform: "uppercase", letterSpacing: ".04em", borderBottom: "1px solid var(--line)" }}>
      {cols.map(c => <div key={c.k} style={{ textAlign: c.align || "left" }}>{c.k}</div>)}
    </div>
  );
}
function TRow({ cols, cells, fresh, onClick }) {
  return (
    <div className={onClick ? "tap" : ""} onClick={onClick} style={{ display: "grid", gridTemplateColumns: cols.map(c => c.w).join(" "), gap: 16, padding: "13px 18px", alignItems: "center",
      borderBottom: "1px solid var(--line)", background: fresh ? "var(--brand-tint)" : "transparent" }}>
      {cells.map((c, i) => <div key={i} style={{ textAlign: cols[i].align || "left", minWidth: 0 }}>{c}</div>)}
    </div>
  );
}

/* ---------------- ORDERS ---------------- */
function AdminOrders({ S }) {
  const F = S.F;
  const [seg, setSeg] = React.useState("All");
  const map = { All: () => true, Pending: o => o.status === "placed", Active: o => o.status === "accepted" || o.status === "out", Delivered: o => o.status === "delivered" };
  const list = S.orders.filter(map[seg]);
  const cols = [{ k: "Order", w: "1fr" }, { k: "Shopkeeper", w: "1.4fr" }, { k: "Dealer", w: "1.4fr" }, { k: "Items", w: ".6fr", align: "center" }, { k: "Amount", w: "1fr", align: "right" }, { k: "Status", w: "1fr", align: "right" }];
  return (
    <div className="fm-fade">
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 18 }}>
        <Segmented value={seg} onChange={setSeg} options={["All", "Pending", "Active", "Delivered"]} />
        <Button variant="outline" size="sm" icon="download" onClick={() => S.notify("Orders exported (CSV)")}>Export</Button>
      </div>
      <Panel pad={0} style={{ overflow: "hidden" }}>
        <div style={{ padding: "18px 0 0" }}><THead cols={cols} /></div>
        {list.map(o => {
          const shop = F.lookups.shop(o.shop); const dealer = F.lookups.dealer(o.dealer);
          return <TRow key={o.id} cols={cols} fresh={o.fresh}
            cells={[
              <span className="mono" style={{ fontWeight: 700, fontSize: 13.5 }}>{o.id}</span>,
              <div style={{ display: "flex", alignItems: "center", gap: 9 }}><Avatar name={shop.store} size={30} tint="var(--brand)" /><span style={{ fontSize: 13.5, fontWeight: 600, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{shop.store}</span></div>,
              <span style={{ fontSize: 13.5, color: "var(--ink-2)" }}>{dealer.name}</span>,
              <span className="mono" style={{ fontSize: 13.5, fontWeight: 600 }}>{o.items.length}</span>,
              <span className="mono" style={{ fontSize: 14, fontWeight: 700 }}>{F.inr(S.orderTotal(o))}</span>,
              <div style={{ display: "flex", justifyContent: "flex-end" }}><Badge status={o.status === "placed" ? "pending" : o.status} size="sm" /></div>,
            ]} />;
        })}
      </Panel>
    </div>
  );
}

/* ---------------- PRODUCTS ---------------- */
function AdminProducts({ S }) {
  const F = S.F;
  const cols = [{ k: "Product", w: "2.4fr" }, { k: "Category", w: "1fr" }, { k: "Price", w: "1fr", align: "right" }, { k: "GST", w: ".7fr", align: "center" }, { k: "Stock", w: "1fr", align: "right" }, { k: "Status", w: "1fr", align: "right" }];
  return (
    <div className="fm-fade">
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 18 }}>
        <div style={{ fontSize: 14, color: "var(--ink-3)" }}><b style={{ color: "var(--ink)" }} className="mono">{F.PRODUCTS.length}</b> active SKUs across {new Set(F.PRODUCTS.map(p => p.cat)).size} categories</div>
        <div style={{ display: "flex", gap: 10 }}>
          <Button variant="outline" size="sm" icon="upload" onClick={() => S.notify("Bulk upload — choose .xlsx")}>Upload Excel</Button>
          <Button variant="primary" size="sm" icon="plus" onClick={() => S.notify("New product form")}>Add product</Button>
        </div>
      </div>
      <Panel pad={0} style={{ overflow: "hidden" }}>
        <div style={{ padding: "18px 0 0" }}><THead cols={cols} /></div>
        {F.PRODUCTS.map(p => (
          <TRow key={p.id} cols={cols}
            cells={[
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}><ProductThumb p={p} size={38} /><div style={{ minWidth: 0 }}><div style={{ fontSize: 13.5, fontWeight: 600 }}>{p.name}</div><div style={{ fontSize: 11.5, color: "var(--ink-4)" }}>{p.brand} · {p.unit}</div></div></div>,
              <span style={{ fontSize: 13, color: "var(--ink-2)" }}>{p.cat}</span>,
              <span className="mono" style={{ fontSize: 14, fontWeight: 700 }}>{F.inr(p.price)}</span>,
              <span className="mono" style={{ fontSize: 13, color: "var(--ink-2)" }}>{p.gst}%</span>,
              <span className="mono" style={{ fontSize: 13.5, fontWeight: 600, color: p.stock < 120 ? "var(--warn)" : "var(--ink-2)" }}>{p.stock}</span>,
              <div style={{ display: "flex", justifyContent: "flex-end" }}><Badge status={p.stock < 120 ? "low" : "Active"} size="sm" label={p.stock < 120 ? "Low" : "In stock"} /></div>,
            ]} />
        ))}
      </Panel>
    </div>
  );
}

/* ---------------- USERS ---------------- */
function AdminUsers({ S }) {
  const F = S.F;
  const [tab, setTab] = React.useState("Dealers");
  if (tab === "Dealers") {
    const cols = [{ k: "Dealer", w: "2fr" }, { k: "Area", w: "1.2fr" }, { k: "Orders", w: "1fr", align: "right" }, { k: "Revenue", w: "1fr", align: "right" }, { k: "Status", w: "1fr", align: "right" }];
    return <UsersShell tab={tab} setTab={setTab} S={S}>
      <Panel pad={0} style={{ overflow: "hidden" }}><div style={{ padding: "18px 0 0" }}><THead cols={cols} /></div>
        {S.dealers.map(d => <TRow key={d.id} cols={cols} fresh={d.fresh}
          cells={[
            <div style={{ display: "flex", alignItems: "center", gap: 10 }}><Avatar name={d.name} size={32} tint="#0e9e6e" /><div><div style={{ fontSize: 13.5, fontWeight: 600 }}>{d.name}</div><div className="mono" style={{ fontSize: 11.5, color: "var(--ink-4)" }}>{d.id} · {d.owner}</div></div></div>,
            <span style={{ fontSize: 13, color: "var(--ink-2)" }}>{d.area}</span>,
            <span className="mono" style={{ fontSize: 13.5, fontWeight: 600 }}>{d.orders}</span>,
            <span className="mono" style={{ fontSize: 13.5, fontWeight: 700 }}>{F.inrShort(d.revenue)}</span>,
            <div style={{ display: "flex", justifyContent: "flex-end" }}><Badge status={d.status} size="sm" /></div>,
          ]} />)}
      </Panel>
    </UsersShell>;
  }
  if (tab === "Shopkeepers") {
    const cols = [{ k: "Store", w: "2fr" }, { k: "Area", w: "1.2fr" }, { k: "Dealer", w: "1.4fr" }, { k: "Orders", w: ".8fr", align: "right" }, { k: "Status", w: "1fr", align: "right" }];
    return <UsersShell tab={tab} setTab={setTab} S={S}>
      <Panel pad={0} style={{ overflow: "hidden" }}><div style={{ padding: "18px 0 0" }}><THead cols={cols} /></div>
        {S.shops.map(s => <TRow key={s.id} cols={cols} fresh={s.fresh}
          cells={[
            <div style={{ display: "flex", alignItems: "center", gap: 10 }}><Avatar name={s.store} size={32} tint="var(--brand)" /><div><div style={{ fontSize: 13.5, fontWeight: 600 }}>{s.store}</div><div className="mono" style={{ fontSize: 11.5, color: "var(--ink-4)" }}>{s.id} · {s.owner}</div></div></div>,
            <span style={{ fontSize: 13, color: "var(--ink-2)" }}>{s.area}</span>,
            <span style={{ fontSize: 13, color: "var(--ink-2)" }}>{(F.lookups.dealer(s.dealer) || {}).name || "—"}</span>,
            <span className="mono" style={{ fontSize: 13.5, fontWeight: 600 }}>{s.orders}</span>,
            <div style={{ display: "flex", justifyContent: "flex-end" }}><Badge status={s.status} size="sm" /></div>,
          ]} />)}
      </Panel>
    </UsersShell>;
  }
  const cols = [{ k: "Employee", w: "2fr" }, { k: "Zone", w: "1.4fr" }, { k: "Dealers", w: "1fr", align: "right" }, { k: "Shopkeepers", w: "1fr", align: "right" }, { k: "Status", w: "1fr", align: "right" }];
  return <UsersShell tab={tab} setTab={setTab} S={S}>
    <Panel pad={0} style={{ overflow: "hidden" }}><div style={{ padding: "18px 0 0" }}><THead cols={cols} /></div>
      {F.EMPLOYEES.map(e => <TRow key={e.id} cols={cols}
        cells={[
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}><Avatar name={e.name} size={32} tint="#c97a16" /><div><div style={{ fontSize: 13.5, fontWeight: 600 }}>{e.name}</div><div className="mono" style={{ fontSize: 11.5, color: "var(--ink-4)" }}>{e.id}</div></div></div>,
          <span style={{ fontSize: 13, color: "var(--ink-2)" }}>{e.area}</span>,
          <span className="mono" style={{ fontSize: 13.5, fontWeight: 600 }}>{e.dealers}</span>,
          <span className="mono" style={{ fontSize: 13.5, fontWeight: 600 }}>{e.shops}</span>,
          <div style={{ display: "flex", justifyContent: "flex-end" }}><Badge status={e.status} size="sm" /></div>,
        ]} />)}
    </Panel>
  </UsersShell>;
}
function UsersShell({ tab, setTab, S, children }) {
  return (
    <div className="fm-fade">
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 18 }}>
        <Segmented value={tab} onChange={setTab} options={["Dealers", "Shopkeepers", "Employees"]} />
        <Button variant="primary" size="sm" icon="plus" onClick={() => S.notify("Add user")}>Add user</Button>
      </div>
      {children}
    </div>
  );
}

/* ---------------- REPORTS ---------------- */
function AdminReports({ S }) {
  const F = S.F; const k = S.kpis;
  const reports = [
    { ic: "receipt", l: "GST summary", v: F.inrShort(k.revenue * 0.13), s: "Tax collected · FY26", col: "var(--brand)" },
    { ic: "pin", l: "Sales by area", v: F.AREAS.length + " zones", s: "Regional breakdown", col: "var(--pos)" },
    { ic: "truck", l: "Dealer performance", v: F.num(k.dealers) + " dealers", s: "Ranked by revenue", col: "#c97a16" },
    { ic: "wallet", l: "Outstanding payments", v: F.inrShort(48200), s: "3 invoices pending", col: "var(--neg)" },
  ];
  return (
    <div className="fm-fade stagger" style={{ display: "flex", flexDirection: "column", gap: 18 }}>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(2,1fr)", gap: 16 }}>
        {reports.map(r => (
          <Panel key={r.l}>
            <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
              <div style={{ width: 46, height: 46, borderRadius: 13, background: "var(--surface-2)", color: r.col, display: "grid", placeItems: "center" }}><Icon name={r.ic} size={22} stroke={2} /></div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 14.5, fontWeight: 700 }}>{r.l}</div>
                <div style={{ fontSize: 12.5, color: "var(--ink-3)" }}>{r.s}</div>
              </div>
              <div style={{ textAlign: "right" }}>
                <div className="mono" style={{ fontSize: 18, fontWeight: 700 }}>{r.v}</div>
              </div>
              <Button variant="outline" size="sm" icon="download" onClick={() => S.notify(r.l + " exported")}>Export</Button>
            </div>
          </Panel>
        ))}
      </div>
      <Panel title="Sales trend · first half 2026" action="Download full report" onAction={() => S.notify("Full report generated (PDF)")}>
        <BarChart data={F.MONTHLY_SALES} height={240} fmt={v => F.inrShort(v)} />
      </Panel>
    </div>
  );
}

Object.assign(window, { AdminOrders, AdminProducts, AdminUsers, AdminReports, THead, TRow, UsersShell });
