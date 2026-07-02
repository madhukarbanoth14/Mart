/* ============================================================
   FlashMart Android — system states + shared modules (Material 3)
   Loading · Empty · Error · Notifications · Document center
   ============================================================ */
const TF = window.FM;

/* skeleton shimmer (Android) */
const _andKf = document.createElement("style");
_andKf.textContent = "@keyframes andShimmer{to{background-position:-200% 0}}";
document.head.appendChild(_andKf);
function ASkel({ w = "100%", h = 14, r = 8, style }) {
  return <div style={{ width: w, height: h, borderRadius: r, background: "linear-gradient(100deg, var(--m3-surface-cont-high) 30%, var(--m3-surface-cont) 50%, var(--m3-surface-cont-high) 70%)", backgroundSize: "200% 100%", animation: "andShimmer 1.4s linear infinite", ...style }} />;
}

/* ---------------- LOADING ---------------- */
function AND_ST_Loading() {
  return (
    <M3Screen nav={<M3NavBar items={skNavItems("home")} active="home" />}>
      <div style={{ padding: `${AND_TOP + 6}px 16px 12px 20px`, display: "flex", alignItems: "center", gap: 12 }}>
        <div style={{ flex: 1 }}><ASkel w={130} h={13} /><ASkel w={170} h={22} style={{ marginTop: 8 }} /></div>
        <ASkel w={42} h={42} r={99} /><ASkel w={42} h={42} r={99} />
      </div>
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 16 }}>
        <ASkel w="100%" h={130} r={24} />
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 11 }}>{[0, 1, 2, 3].map(i => <ASkel key={i} h={92} r={16} />)}</div>
        <ASkel w={130} h={14} style={{ margin: "2px 2px 0" }} />
        <M3Card variant="filled" pad={6}>
          {[0, 1, 2].map(i => (
            <div key={i} style={{ display: "flex", alignItems: "center", gap: 14, padding: "13px 8px", borderBottom: i < 2 ? "1px solid var(--m3-outline-var)" : "none" }}>
              <ASkel w={40} h={40} r={12} /><div style={{ flex: 1 }}><ASkel w="55%" h={14} /><ASkel w="35%" h={11} style={{ marginTop: 7 }} /></div><ASkel w={56} h={20} r={99} />
            </div>
          ))}
        </M3Card>
      </div>
    </M3Screen>
  );
}

/* ---------------- EMPTY ---------------- */
function AND_ST_Empty() {
  return (
    <M3Screen nav={<M3NavBar items={skNavItems("orders")} active="orders" />}
      topBar={<M3TopBar variant="large" title="My orders" subtitle="Ramesh General Store" />}>
      <div style={{ padding: "0 16px 12px" }}><M3Segmented full value="All" onChange={() => {}} options={["All", "Pending", "Delivered"]} /></div>
      <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", textAlign: "center", padding: "50px 40px 0" }}>
        <div style={{ width: 110, height: 110, borderRadius: 32, background: M.secCont, display: "grid", placeItems: "center", color: M.onSecCont, position: "relative" }}>
          <Icon name="bag" size={46} stroke={1.6} />
          <div style={{ position: "absolute", bottom: -8, right: -8, width: 42, height: 42, borderRadius: 14, background: M.primary, color: "#fff", display: "grid", placeItems: "center", boxShadow: "var(--m3-e2)", border: `3px solid ${M.surface}` }}><Icon name="plus" size={20} stroke={2.6} /></div>
        </div>
        <div style={{ fontSize: 19, fontWeight: 800, marginTop: 24, color: M.onSurf }}>No orders yet</div>
        <div style={{ fontSize: 14.5, color: M.onSurfVar, marginTop: 8, lineHeight: 1.5, maxWidth: 260 }}>When you place your first order, it'll show up here with live delivery tracking.</div>
        <div style={{ marginTop: 22 }}><M3Button variant="filled" size="lg" icon="grid">Browse catalog</M3Button></div>
      </div>
    </M3Screen>
  );
}

/* ---------------- ERROR ---------------- */
function AND_ST_Error() {
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", textAlign: "center", padding: `${AND_TOP}px 36px 0`, background: M.surface }}>
      <div style={{ width: 110, height: 110, borderRadius: 32, background: M.errCont, color: M.err, display: "grid", placeItems: "center" }}>
        <svg width="50" height="50" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M3 3l18 18M8.5 8.5A6 6 0 0 0 5 10M12 5a11 11 0 0 1 6.5 2.1M1.5 7A16 16 0 0 1 5 4.8M19 13a6 6 0 0 0-2-1.2M12 20h.01" /></svg>
      </div>
      <div style={{ fontSize: 23, fontWeight: 800, letterSpacing: "-.025em", marginTop: 26, color: M.onSurf }}>No internet connection</div>
      <div style={{ fontSize: 15, color: M.onSurfVar, marginTop: 10, lineHeight: 1.5, maxWidth: 290 }}>We couldn't reach FlashMart. Check your connection and try again — your cart is saved.</div>
      <div style={{ display: "flex", flexDirection: "column", gap: 11, width: "100%", marginTop: 30 }}>
        <M3Button variant="filled" size="lg" full icon="refresh">Try again</M3Button>
        <M3Button variant="text" size="md" full>View saved cart</M3Button>
      </div>
    </div>
  );
}

/* ---------------- NOTIFICATIONS ---------------- */
function AND_Notifications() {
  const groups = [
    { day: "Today", items: [["truck", "Out for delivery", "ORD-1122 is on the way to your store.", "var(--brand-tint)", "var(--brand)", true], ["check", "Order delivered", "ORD-1124 delivered. Tap to rate.", "var(--brand-tint)", "var(--pos)", false]] },
    { day: "Yesterday", items: [["doc", "Document verified", "Your GST certificate was approved.", "var(--blue-tint)", "var(--blue)", false], ["tag", "Offer just for you", "Extra 8% off staples this week.", "var(--gold-tint)", "var(--gold-ink)", false]] },
  ];
  return (
    <M3Screen topBar={<M3TopBar onBack variant="large" title="Notifications" actions={<M3IconBtn icon="check" />} />}>
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 18 }}>
        {groups.map(g => (
          <div key={g.day}>
            <div style={{ fontSize: 12.5, fontWeight: 700, color: M.onSurfVar, padding: "0 4px 8px" }}>{g.day}</div>
            <M3Card variant="filled" pad={6}>
              {g.items.map(([ic, t, s, bg, fg, unread], i) => (
                <div key={t} style={{ display: "flex", gap: 13, alignItems: "flex-start", padding: "13px 8px", borderBottom: i === g.items.length - 1 ? "none" : "1px solid var(--m3-outline-var)", position: "relative" }}>
                  <div style={{ width: 42, height: 42, borderRadius: 12, background: bg, color: fg, display: "grid", placeItems: "center", flexShrink: 0 }}><Icon name={ic} size={20} /></div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 14.5, fontWeight: 700, color: M.onSurf }}>{t}</div>
                    <div style={{ fontSize: 13, color: M.onSurfVar, marginTop: 2, lineHeight: 1.4 }}>{s}</div>
                  </div>
                  {unread && <span style={{ width: 9, height: 9, borderRadius: 99, background: M.primary, flexShrink: 0, marginTop: 6 }} />}
                </div>
              ))}
            </M3Card>
          </div>
        ))}
      </div>
    </M3Screen>
  );
}

/* ---------------- DOCUMENT CENTER ---------------- */
function AND_DocCenter() {
  const docs = [["Aadhaar card", "doc", "Verified", "Jun 2, 2026"], ["PAN card", "card", "Verified", "Jun 2, 2026"], ["GST certificate", "receipt", "Verified", "Jun 4, 2026"], ["Trade license", "layers", "Pending", "Uploaded Jun 22"]];
  return (
    <M3Screen topBar={<M3TopBar onBack title="Document center" subtitle="3 verified · 1 pending" />} fab={<M3FAB icon="upload" label="Upload" color="primary" />}>
      <div style={{ padding: "4px 16px 0" }}>
        <M3Card variant="filled" pad={16} style={{ display: "flex", alignItems: "center", gap: 14, marginBottom: 14 }}>
          <div style={{ position: "relative", width: 52, height: 52 }}>
            <svg width="52" height="52" viewBox="0 0 52 52"><circle cx="26" cy="26" r="22" fill="none" stroke="var(--m3-surface-cont-highest)" strokeWidth="6" /><circle cx="26" cy="26" r="22" fill="none" stroke="var(--pos)" strokeWidth="6" strokeLinecap="round" strokeDasharray={`${0.75 * 138} 138`} transform="rotate(-90 26 26)" /></svg>
            <span className="mono" style={{ position: "absolute", inset: 0, display: "grid", placeItems: "center", fontSize: 13, fontWeight: 700, color: M.onSurf }}>75%</span>
          </div>
          <div style={{ flex: 1 }}><div style={{ fontSize: 15, fontWeight: 700, color: M.onSurf }}>Verification status</div><div style={{ fontSize: 13, color: M.onSurfVar, marginTop: 2 }}>One more document to fully unlock credit.</div></div>
        </M3Card>
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {docs.map(([name, ic, status, date]) => {
            const ok = status === "Verified";
            return (
              <M3Card key={name} variant="outlined" pad={14} style={{ display: "flex", alignItems: "center", gap: 14 }}>
                <div style={{ width: 44, height: 44, borderRadius: 12, background: ok ? "var(--pos-tint)" : "var(--gold-tint)", color: ok ? "var(--pos)" : "var(--gold-ink)", display: "grid", placeItems: "center", flexShrink: 0 }}><Icon name={ic} size={20} /></div>
                <div style={{ flex: 1 }}><div style={{ fontSize: 14.5, fontWeight: 700, color: M.onSurf }}>{name}</div><div style={{ fontSize: 12, color: M.onSurfVar, marginTop: 1 }}>{date}</div></div>
                <M3Status status={ok ? "Approved" : "Pending"} label={status} size="sm" />
              </M3Card>
            );
          })}
        </div>
      </div>
    </M3Screen>
  );
}

Object.assign(window, { AND_ST_Loading, AND_ST_Empty, AND_ST_Error, AND_Notifications, AND_DocCenter });
