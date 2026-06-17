/* ============================================================
   Flashmart — Employee: network, profile
   ============================================================ */

function EmpNetwork({ S }) {
  const F = S.F;
  const [seg, setSeg] = React.useState("Dealers");
  const dealers = S.dealers, shops = S.shops;
  const list = seg === "Dealers" ? dealers : shops;
  return (
    <div style={{ padding: "0 16px" }}>
      <div style={{ marginBottom: 14 }}><Segmented full value={seg} onChange={setSeg} options={["Dealers", "Shopkeepers"]} /></div>
      <div className="stagger" style={{ display: "flex", flexDirection: "column", gap: 10 }}>
        {list.map(x => {
          const name = x.name || x.store;
          return (
            <Card key={x.id} pad={13} style={{ display: "flex", alignItems: "center", gap: 13,
              borderColor: x.fresh ? "var(--pos)" : "var(--line)", boxShadow: x.fresh ? "0 0 0 2px var(--pos-tint)" : "var(--sh-sm)" }}>
              <Avatar name={name} size={42} tint={seg === "Dealers" ? "#0e9e6e" : "#2f48d4"} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 14.5, fontWeight: 700, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{name}</div>
                <div style={{ fontSize: 12.5, color: "var(--ink-3)" }} className="mono">{x.id} · {x.area}</div>
              </div>
              <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: 5 }}>
                <Badge status={x.status} size="sm" />
                <span style={{ fontSize: 11.5, color: "var(--ink-4)", fontWeight: 600 }}>{x.orders} orders</span>
              </div>
            </Card>
          );
        })}
      </div>
    </div>
  );
}

function EmpProfile({ S, me, myDealers, myShops }) {
  const stats = [{ l: "Dealers", v: myDealers }, { l: "Shopkeepers", v: myShops }, { l: "Zone", v: me.area.split(" ")[0] }];
  const rows = [
    { ic: "pin", t: "Assigned zone", s: me.area },
    { ic: "chart", t: "My targets", s: "120 stores / month" },
    { ic: "wallet", t: "Incentives", s: "₹12,500 earned" },
    { ic: "settings", t: "Help & support", s: "Field team line" },
  ];
  return (
    <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 14 }}>
      <Card style={{ display: "flex", alignItems: "center", gap: 14 }}>
        <Avatar name={me.name} size={56} tint="#c97a16" />
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 17, fontWeight: 700, letterSpacing: "-.02em" }}>{me.name}</div>
          <div style={{ fontSize: 13, color: "var(--ink-3)" }}>{me.id} · Field executive</div>
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

Object.assign(window, { EmpNetwork, EmpProfile });
