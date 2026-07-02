/* ============================================================
   FlashMart Android — Employee app (Material 3 · amber accent)
   Dashboard · CRM · Onboard · Follow-up
   ============================================================ */
const EF = window.FM;
const AMBER = "#b8770f", AMBER_TINT = "var(--gold-tint)";
const emNavItems = () => [
  { id: "home", icon: "home", label: "Home" },
  { id: "crm", icon: "users", label: "CRM" },
  { id: "tasks", icon: "clock", label: "Tasks", badge: 4 },
  { id: "profile", icon: "user", label: "Profile" },
];
const Whatsapp = ({ size = 19 }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor"><path d="M12 2a10 10 0 0 0-8.6 15l-1.3 4.7 4.8-1.3A10 10 0 1 0 12 2Zm5.3 14.1c-.2.6-1.2 1.2-1.7 1.2-.4 0-1 .1-3.3-.9-2.8-1.2-4.5-4-4.6-4.2-.1-.2-1.1-1.4-1.1-2.7s.7-1.9.9-2.2c.2-.2.5-.3.6-.3h.5c.2 0 .4 0 .6.5l.8 2c.1.2.1.4 0 .5l-.4.5c-.2.2-.3.3-.1.6.2.3.8 1.3 1.7 2.1 1.2 1 2.1 1.4 2.4 1.5.2.1.4.1.6-.1l.7-.9c.2-.2.4-.2.6-.1l1.9.9c.3.1.4.2.5.3.1.3.1.7-.1 1.2Z"/></svg>
);

/* ---------------- DASHBOARD ---------------- */
function AND_EM_Home() {
  const me = EF.EMPLOYEES[0];
  const progress = Math.round((me.shops / 120) * 100);
  const tasks = [["doc", "Verify GST · ABC Kirana Mart", "Doc pending · 2 days", M.err], ["phone", "Follow-up call · Gupta Trading", "Due today, 4 PM", AMBER], ["bag", "Onboard · New Bombay Stores", "Visit scheduled", "var(--brand)"]];
  return (
    <M3Screen nav={<M3NavBar items={emNavItems()} active="home" />} fab={<M3FAB icon="plus" label="Onboard" color="primary" />}>
      <div style={{ padding: `${AND_TOP + 6}px 12px 12px 20px`, display: "flex", alignItems: "center", gap: 12 }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: 13, fontWeight: 700, color: AMBER }}>Field executive</div>
          <div style={{ fontSize: 22, fontWeight: 800, letterSpacing: "-.025em", color: M.onSurf }}>Neha's desk</div>
        </div>
        <M3IconBtn icon="bell" filled />
        <Avatar name={me.name} size={42} tint={AMBER} />
      </div>
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 16 }}>
        <div style={{ borderRadius: 24, padding: 20, color: "#fff", position: "relative", overflow: "hidden", background: "linear-gradient(150deg, #c9870f, #8a5a06)", boxShadow: "var(--m3-e2)" }}>
          <div style={{ position: "absolute", right: -28, top: -28, width: 150, height: 150, borderRadius: "50%", background: "rgba(255,255,255,.08)" }} />
          <div style={{ fontSize: 13, fontWeight: 600, opacity: .85 }}>This month's onboarding</div>
          <div style={{ display: "flex", gap: 28, margin: "14px 0 16px" }}>
            <div><div className="mono" style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-.03em" }}>{me.dealers}</div><div style={{ fontSize: 12, opacity: .82, marginTop: 2 }}>Dealers</div></div>
            <div style={{ width: 1, background: "rgba(255,255,255,.2)" }} />
            <div><div className="mono" style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-.03em" }}>{me.shops}</div><div style={{ fontSize: 12, opacity: .82, marginTop: 2 }}>Shopkeepers</div></div>
          </div>
          <div style={{ height: 7, borderRadius: 99, background: "rgba(255,255,255,.22)", overflow: "hidden" }}><div style={{ width: progress + "%", height: "100%", background: "#fff", borderRadius: 99 }} /></div>
          <div style={{ fontSize: 11.5, opacity: .85, marginTop: 7 }}>{progress}% of 120 monthly target</div>
        </div>
        <div>
          <M3SectionLabel action="All tasks">Today's tasks · 4</M3SectionLabel>
          <M3Card variant="filled" pad={6}>
            {tasks.map(([ic, t, s, c], i) => (
              <M3ListItem key={t} last={i === tasks.length - 1} onClick={() => {}}
                leading={<div style={{ width: 40, height: 40, borderRadius: 11, background: M.surf4, color: c, display: "grid", placeItems: "center" }}><Icon name={ic} size={19} /></div>}
                headline={t} supporting={s} trailing={<Icon name="chevR" size={18} color={M.onSurfVar} />} />
            ))}
          </M3Card>
        </div>
      </div>
    </M3Screen>
  );
}

/* ---------------- CRM ---------------- */
function AND_EM_CRM() {
  const dealers = EF.DEALERS;
  return (
    <M3Screen nav={<M3NavBar items={emNavItems()} active="crm" />}
      topBar={<M3TopBar variant="large" title="CRM" subtitle="10 dealers · 85 shopkeepers" actions={<M3IconBtn icon="search" />} />}>
      <div style={{ padding: "0 16px 10px" }}>
        <M3Segmented full value="Dealers" onChange={() => {}} options={["Dealers", "Shopkeepers"]} />
      </div>
      <div className="fm-scroll" style={{ display: "flex", gap: 8, padding: "0 16px 12px", overflowX: "auto" }}>
        {["All areas", "Doc pending", "Active", "Follow-up due"].map((c, i) => <M3Chip key={c} selected={i === 0} leadingCheck>{c}</M3Chip>)}
      </div>
      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 11 }}>
        {dealers.map((d, i) => {
          const docPending = i === 1 || i === 4;
          return (
            <M3Card key={d.id} variant="outlined" pad={14}>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <Avatar name={d.name} size={42} tint="var(--blue)" />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 14.5, fontWeight: 700, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap", color: M.onSurf }}>{d.name}</div>
                  <div className="mono" style={{ fontSize: 12, color: M.onSurfVar, marginTop: 1 }}>{d.phone} · {d.area}</div>
                </div>
                {docPending ? <M3Status status="Pending" size="sm" /> : <M3Status status="Approved" label="Verified" size="sm" />}
              </div>
              <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginTop: 12, paddingTop: 12, borderTop: "1px solid var(--m3-outline-var)" }}>
                <span style={{ fontSize: 12, color: M.onSurfVar }}>Next follow-up · <b style={{ color: M.onSurf }}>Jun 26</b></span>
                <div style={{ display: "flex", gap: 8 }}>
                  <button onClick={(e) => mRipple(e, "rgba(0,0,0,.1)")} style={{ position: "relative", overflow: "hidden", width: 40, height: 40, borderRadius: 999, border: "none", background: "var(--brand-tint)", color: "var(--pos)", display: "grid", placeItems: "center", cursor: "pointer" }}><span style={{ position: "relative", zIndex: 1 }}><Whatsapp /></span></button>
                  <button onClick={(e) => mRipple(e, "rgba(0,0,0,.1)")} style={{ position: "relative", overflow: "hidden", width: 40, height: 40, borderRadius: 999, border: "none", background: M.surf4, color: M.onSurf, display: "grid", placeItems: "center", cursor: "pointer" }}><span style={{ position: "relative", zIndex: 1, display: "flex" }}><Icon name="phone" size={18} /></span></button>
                </div>
              </div>
            </M3Card>
          );
        })}
      </div>
    </M3Screen>
  );
}

/* ---------------- ONBOARD ---------------- */
function AND_EM_Onboard() {
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: M.surface, position: "relative" }}>
      <M3TopBar onBack title="Onboard partner" />
      <div className="fm-scroll" style={{ flex: 1, overflow: "auto", padding: "10px 16px 110px", display: "flex", flexDirection: "column", gap: 14 }}>
        <M3Segmented full value="Shopkeeper" onChange={() => {}} options={["Shopkeeper", "Dealer"]} />
        <M3Field variant="outlined" label="Owner name" icon="user" value="Ravi Sharma" />
        <M3Field variant="outlined" label="Shop name" icon="box" value="Sharma General Store" />
        <M3Field variant="outlined" label="Mobile number" icon="phone" prefix="+91" value="98213 44567" focused />
        <M3Field variant="outlined" label="Area / route" icon="pin" value="Andheri East" trailing={<Icon name="chevD" size={18} color={M.onSurfVar} />} />
        {/* location capture */}
        <M3Card variant="outlined" pad={0} style={{ overflow: "hidden" }}>
          <div style={{ height: 110, position: "relative", background: "linear-gradient(160deg, #e8efe9, #dde7e0)" }}>
            <div style={{ position: "absolute", inset: 0, opacity: .6, backgroundImage: "linear-gradient(var(--m3-outline-var) 1px, transparent 1px), linear-gradient(90deg, var(--m3-outline-var) 1px, transparent 1px)", backgroundSize: "30px 30px" }} />
            <div style={{ position: "absolute", left: "50%", top: "55%", transform: "translate(-50%,-100%)" }}>
              <div style={{ width: 28, height: 28, borderRadius: "50% 50% 50% 0", background: "var(--brand)", transform: "rotate(-45deg)", boxShadow: "var(--m3-e2)", display: "grid", placeItems: "center" }}><div style={{ transform: "rotate(45deg)", color: "#fff" }}><Icon name="pin" size={14} /></div></div>
            </div>
          </div>
          <div style={{ padding: 12 }}><M3Button variant="tonal" size="sm" full icon="pin">Capture store location</M3Button></div>
        </M3Card>
        {/* documents */}
        <M3SectionLabel>Documents</M3SectionLabel>
        <div style={{ display: "flex", gap: 10 }}>
          {[["Aadhaar", true], ["PAN", false], ["GST", false]].map(([l, done]) => (
            <div key={l} style={{ flex: 1, borderRadius: 14, border: `1.5px dashed ${done ? "var(--pos)" : M.outline}`, background: done ? "var(--pos-tint)" : M.surf1, padding: "14px 6px", textAlign: "center", color: done ? "var(--pos)" : M.onSurfVar }}>
              <Icon name={done ? "check" : "upload"} size={20} stroke={done ? 2.6 : 2} style={{ margin: "0 auto 6px" }} />
              <div style={{ fontSize: 11.5, fontWeight: 700 }}>{l}</div>
            </div>
          ))}
        </div>
      </div>
      <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, padding: "14px 16px", paddingBottom: 14 + AND_BOTTOM, background: M.surf2, boxShadow: "0 -1px 0 var(--m3-outline-var)" }}>
        <M3Button variant="filled" size="lg" full icon="check">Save & submit</M3Button>
      </div>
    </div>
  );
}

/* ---------------- FOLLOW-UP DETAIL ---------------- */
function AND_EM_FollowUp() {
  const steps = [
    { label: "Onboarding started", time: "Jun 1 · Neha K.", state: "done" },
    { label: "Aadhaar uploaded & verified", time: "Jun 2", state: "done" },
    { label: "GST document requested", time: "Jun 4 · via WhatsApp", state: "done" },
    { label: "Awaiting GST upload", time: "Follow-up due Jun 26", state: "active" },
    { label: "Account activated", time: "Pending", state: "todo" },
  ];
  return (
    <M3Screen topBar={<M3TopBar onBack title="Gupta Trading Co." subtitle="DLR-02 · Ghatkopar" actions={<M3IconBtn icon="phone" />} />}>
      <div style={{ padding: "4px 16px 16px", display: "flex", flexDirection: "column", gap: 14 }}>
        <div style={{ display: "flex", gap: 10 }}>
          <M3Button variant="tonal" size="md" full icon="phone">Call</M3Button>
          <M3Button variant="filled" size="md" full>Add remark</M3Button>
        </div>
        <M3Card variant="filled">
          <M3SectionLabel style={{ paddingBottom: 14 }}>Verification timeline</M3SectionLabel>
          <OrderTimeline steps={steps} />
        </M3Card>
        <M3Card variant="outlined" style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
          <div style={{ width: 40, height: 40, borderRadius: 11, background: "var(--gold-tint)", color: "var(--gold-ink)", display: "grid", placeItems: "center", flexShrink: 0 }}><Icon name="doc" size={20} /></div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 14, fontWeight: 700, color: M.onSurf }}>Last remark</div>
            <div style={{ fontSize: 13, color: M.onSurfVar, marginTop: 3, lineHeight: 1.45 }}>“Owner travelling till Jun 25, will share GST cert by 26th. Confirmed on WhatsApp.”</div>
            <div style={{ fontSize: 11.5, color: M.onSurfVar, marginTop: 6 }}>Neha K. · Jun 22</div>
          </div>
        </M3Card>
      </div>
    </M3Screen>
  );
}

Object.assign(window, { emNavItems, AND_EM_Home, AND_EM_CRM, AND_EM_Onboard, AND_EM_FollowUp });
