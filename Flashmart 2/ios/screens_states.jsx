/* ============================================================
   Flashmart iOS — system states: loading, empty, error
   ============================================================ */

/* ---------------- LOADING (skeleton) ---------------- */
function ST_Loading() {
  return (
    <Screen nav={<NavBar items={skNav("home")} active="home" />}>
      <div style={{ padding: "6px 20px 14px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div><Skel w={150} h={24} /><Skel w={110} h={13} style={{ marginTop: 9 }} /></div>
        <Skel w={42} h={42} r={13} />
      </div>
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 16 }}>
        <Skel w="100%" h={150} r={22} />
        <div style={{ display: "grid", gridTemplateColumns: "repeat(3,1fr)", gap: 10 }}>
          {[0, 1, 2].map(i => <Skel key={i} h={86} r={16} />)}
        </div>
        <div>
          <Skel w={120} h={13} style={{ margin: "0 2px 14px" }} />
          <Card pad={6}>
            {[0, 1, 2].map(i => (
              <div key={i} style={{ display: "flex", alignItems: "center", gap: 13, padding: "13px 8px", borderBottom: i < 2 ? "1px solid var(--line)" : "none" }}>
                <Skel w={38} h={38} r={11} />
                <div style={{ flex: 1 }}><Skel w="55%" h={14} /><Skel w="35%" h={11} style={{ marginTop: 7 }} /></div>
                <Skel w={56} h={20} r={7} />
              </div>
            ))}
          </Card>
        </div>
      </div>
    </Screen>
  );
}

/* ---------------- EMPTY (no orders) ---------------- */
function ST_Empty() {
  return (
    <Screen nav={<NavBar items={skNav("orders")} active="orders" />}>
      <TopBar title="My Orders" subtitle="Ramesh General Store" right={<GlyphBtn name="cart" />} />
      <div style={{ padding: "0 16px" }}>
        <div style={{ marginBottom: 14 }}><Segmented full value="All" onChange={() => {}} options={["All", "Active", "Delivered"]} /></div>
      </div>
      <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", textAlign: "center", padding: "60px 40px 0" }}>
        <div style={{ width: 110, height: 110, borderRadius: 30, background: "var(--surface-2)", display: "grid", placeItems: "center", color: "var(--ink-4)", position: "relative", border: "1px solid var(--line)" }}>
          <Icon name="bag" size={46} stroke={1.5} />
          <div style={{ position: "absolute", bottom: -8, right: -8, width: 40, height: 40, borderRadius: 13, background: "var(--brand)", color: "#fff", display: "grid", placeItems: "center", boxShadow: "var(--sh-md)", border: "3px solid var(--bg)" }}><Icon name="plus" size={20} stroke={2.6} /></div>
        </div>
        <div style={{ fontSize: 19, fontWeight: 700, marginTop: 24 }}>No orders yet</div>
        <div style={{ fontSize: 14.5, color: "var(--ink-3)", marginTop: 8, lineHeight: 1.5, maxWidth: 260 }}>When you place your first order, it'll show up here with live delivery tracking.</div>
        <div style={{ marginTop: 22 }}><Button variant="primary" size="lg" icon="grid">Browse catalog</Button></div>
      </div>
    </Screen>
  );
}

/* ---------------- ERROR (offline) ---------------- */
function ST_Error() {
  return (
    <Screen>
      <div style={{ height: 874, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", textAlign: "center", padding: "0 36px" }}>
        <div style={{ width: 110, height: 110, borderRadius: 30, background: "var(--neg-tint)", color: "var(--neg)", display: "grid", placeItems: "center", boxShadow: "0 0 0 12px rgba(214,69,63,.07)" }}>
          <svg width="50" height="50" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 3l18 18M8.5 8.5A6 6 0 0 0 5 10M12 5a11 11 0 0 1 6.5 2.1M1.5 7A16 16 0 0 1 5 4.8M19 13a6 6 0 0 0-2-1.2M12 20h.01" />
          </svg>
        </div>
        <div style={{ fontSize: 23, fontWeight: 700, letterSpacing: "-.025em", marginTop: 26 }}>No internet connection</div>
        <div style={{ fontSize: 15, color: "var(--ink-3)", marginTop: 10, lineHeight: 1.5, maxWidth: 290 }}>We couldn't reach Flashmart. Check your connection and try again — your cart is saved.</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 11, width: "100%", marginTop: 30 }}>
          <Button variant="primary" size="lg" full icon="refresh">Try again</Button>
          <Button variant="ghost" full>View saved cart</Button>
        </div>
      </div>
    </Screen>
  );
}

Object.assign(window, { ST_Loading, ST_Empty, ST_Error });
