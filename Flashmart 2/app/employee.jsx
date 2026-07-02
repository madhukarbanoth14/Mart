/* ============================================================
   Flashmart — Employee app (mobile) — onboarding
   ============================================================ */
function EmployeeApp() {
  const S = useStore(); const F = S.F;
  const me = F.EMPLOYEES.find(e => e.id === F.ME.employee);
  const [tab, setTab] = React.useState("home");
  const [form, setForm] = React.useState(null);   // 'dealer' | 'shop'
  const scrollRef = React.useRef(null);
  React.useEffect(() => { if (scrollRef.current) scrollRef.current.scrollTop = 0; }, [tab, form]);

  const myDealers = S.dealers.filter(d => d.fresh).length + me.dealers;
  const myShops = S.shops.filter(s => s.fresh).length + me.shops;

  const nav = (
    <BottomNav active={tab} onChange={(x) => { setForm(null); setTab(x); }}
      items={[
        { id: "home", icon: "home", label: "Home" },
        { id: "network", icon: "layers", label: "Network" },
        { id: "profile", icon: "user", label: "Profile" },
      ]} />
  );

  if (form) return <><OnboardForm S={S} kind={form} back={() => setForm(null)} scrollRef={scrollRef} /><Toast /></>;

  let header, body;
  if (tab === "home") {
    header = <AppHeader pad title={me.name.split(" ")[0] + "'s desk"} kicker="Field executive"
      right={<><IconBtn name="bell" /><Avatar name={me.name} tint="#c97a16" /></>} />;
    body = <EmpHome S={S} me={me} myDealers={myDealers} myShops={myShops} setForm={setForm} setTab={setTab} />;
  } else if (tab === "network") {
    header = <AppHeader pad title="My network" subtitle={myDealers + " dealers · " + myShops + " shopkeepers"} />;
    body = <EmpNetwork S={S} />;
  } else {
    header = <AppHeader pad title="Profile" />;
    body = <EmpProfile S={S} me={me} myDealers={myDealers} myShops={myShops} />;
  }
  return <><PhoneShell header={header} nav={nav} scrollRef={scrollRef}>{body}</PhoneShell><Toast /></>;
}

function EmpHome({ S, me, myDealers, myShops, setForm, setTab }) {
  const target = 120, progress = Math.min(100, Math.round((myShops / target) * 100));
  return (
    <div className="stagger" style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 16 }}>
      {/* performance hero */}
      <div style={{ borderRadius: 22, padding: 20, color: "#fff", position: "relative", overflow: "hidden",
        background: "linear-gradient(150deg, #c97a16, #92560a)", boxShadow: "var(--sh-lg)" }}>
        <div style={{ position: "absolute", right: -28, top: -28, width: 150, height: 150, borderRadius: "50%", background: "rgba(255,255,255,.08)" }} />
        <div style={{ fontSize: 13, fontWeight: 600, opacity: .85 }}>This month's onboarding</div>
        <div style={{ display: "flex", gap: 26, margin: "14px 0 16px" }}>
          <div><div className="mono" style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-.03em" }}>{myDealers}</div><div style={{ fontSize: 12, opacity: .8, marginTop: 2 }}>Dealers</div></div>
          <div style={{ width: 1, background: "rgba(255,255,255,.2)" }} />
          <div><div className="mono" style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-.03em" }}>{myShops}</div><div style={{ fontSize: 12, opacity: .8, marginTop: 2 }}>Shopkeepers</div></div>
        </div>
        <div style={{ height: 7, borderRadius: 99, background: "rgba(255,255,255,.2)", overflow: "hidden" }}>
          <div style={{ width: progress + "%", height: "100%", background: "#fff", borderRadius: 99 }} />
        </div>
        <div style={{ fontSize: 11.5, opacity: .85, marginTop: 7 }}>{progress}% of {target} monthly target</div>
      </div>
      {/* actions */}
      <div style={{ display: "flex", flexDirection: "column", gap: 11 }}>
        <Card onClick={() => setForm("dealer")} style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <div style={{ width: 46, height: 46, borderRadius: 13, background: "var(--pos-tint)", color: "var(--pos)", display: "grid", placeItems: "center" }}><Icon name="truck" size={22} stroke={2} /></div>
          <div style={{ flex: 1 }}><div style={{ fontSize: 15.5, fontWeight: 700 }}>Add a dealer</div><div style={{ fontSize: 12.5, color: "var(--ink-3)" }}>Onboard a distributor to an area</div></div>
          <div style={{ width: 38, height: 38, borderRadius: 11, background: "var(--ink)", color: "#fff", display: "grid", placeItems: "center" }}><Icon name="plus" size={20} stroke={2.4} /></div>
        </Card>
        <Card onClick={() => setForm("shop")} style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <div style={{ width: 46, height: 46, borderRadius: 13, background: "var(--brand-tint)", color: "var(--brand)", display: "grid", placeItems: "center" }}><Icon name="bag" size={22} stroke={2} /></div>
          <div style={{ flex: 1 }}><div style={{ fontSize: 15.5, fontWeight: 700 }}>Add a shopkeeper</div><div style={{ fontSize: 12.5, color: "var(--ink-3)" }}>Register a retail store</div></div>
          <div style={{ width: 38, height: 38, borderRadius: 11, background: "var(--ink)", color: "#fff", display: "grid", placeItems: "center" }}><Icon name="plus" size={20} stroke={2.4} /></div>
        </Card>
      </div>
      {/* recent onboards */}
      <div>
        <SectionLabel action="View network" onAction={() => setTab("network")}>Recently onboarded</SectionLabel>
        <Card pad={4}>
          {[...S.dealers.filter(d => d.fresh), ...S.shops.filter(s => s.fresh)].slice(0, 3).map((x, i, arr) => (
            <Row key={x.id} last={i === arr.length - 1}
              left={<Avatar name={x.name || x.store} size={36} tint="#c97a16" />}
              title={x.name || x.store} sub={(x.owner || "") + " · " + x.area}
              right={<span style={{ fontSize: 10.5, fontWeight: 700, color: "var(--pos)", background: "var(--pos-tint)", padding: "3px 8px", borderRadius: 7 }}>NEW</span>} />
          ))}
          {S.dealers.filter(d => d.fresh).length + S.shops.filter(s => s.fresh).length === 0 &&
            <div style={{ padding: "26px 16px", textAlign: "center", color: "var(--ink-4)", fontSize: 13 }}>Onboard someone to see them here</div>}
        </Card>
      </div>
    </div>
  );
}

function OnboardForm({ S, kind, back, scrollRef }) {
  const F = S.F; const isDealer = kind === "dealer";
  const [v, setV] = React.useState({ name: "", owner: "", phone: "", email: "", area: "" });
  const [done, setDone] = React.useState(false);
  const valid = v.name && v.phone && v.area;
  const submit = () => {
    if (!valid) return;
    const id = (isDealer ? "DLR-" : "SHP-") + Math.floor(10 + Math.random() * 89);
    if (isDealer) S.addDealer({ id, name: v.name, owner: v.owner || v.name, area: v.area, phone: v.phone, orders: 0, revenue: 0, status: "Onboarding" });
    else S.addShop({ id, store: v.name, owner: v.owner || v.name, area: v.area, phone: v.phone, dealer: F.ME.dealer, orders: 0, status: "Active" });
    setDone(true);
  };
  if (done) return (
    <PhoneShell bg="var(--surface)">
      <div style={{ minHeight: "100%", display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", padding: "20px 26px", textAlign: "center" }}>
        <SuccessCheck size={100} color="#c97a16" />
        <div style={{ fontSize: 24, fontWeight: 700, marginTop: 18 }}>{isDealer ? "Dealer" : "Shopkeeper"} onboarded</div>
        <div style={{ fontSize: 14, color: "var(--ink-3)", marginTop: 6, maxWidth: 250 }}>{v.name} is now live in the Flashmart network and visible to Admin.</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10, width: "100%", marginTop: 24 }}>
          <Button variant="dark" size="lg" full icon="plus" onClick={() => { setV({ name: "", owner: "", phone: "", email: "", area: "" }); setDone(false); }}>Onboard another</Button>
          <Button variant="ghost" full onClick={back}>Back to dashboard</Button>
        </div>
      </div>
    </PhoneShell>
  );
  return (
    <>
      <PhoneShell scrollRef={scrollRef} header={<AppHeader pad title={isDealer ? "Add dealer" : "Add shopkeeper"} subtitle="Field onboarding" onBack={back} />}>
        <div style={{ padding: "4px 16px 120px", display: "flex", flexDirection: "column", gap: 14 }}>
          <Field label={isDealer ? "Business name" : "Store name"} icon="box" value={v.name} onChange={x => setV({ ...v, name: x })} placeholder={isDealer ? "e.g. Krishna Wholesale" : "e.g. Sharma General Store"} />
          <Field label="Owner name" icon="user" value={v.owner} onChange={x => setV({ ...v, owner: x })} placeholder="Full name" />
          <Field label="Phone number" icon="phone" prefix="+91" type="tel" value={v.phone} onChange={x => setV({ ...v, phone: x })} placeholder="98XXX XXXXX" />
          <Field label="Email (optional)" icon="mail" type="email" value={v.email} onChange={x => setV({ ...v, email: x })} placeholder="name@email.com" />
          <Field label="Area / route" icon="pin" value={v.area} onChange={x => setV({ ...v, area: x })} options={F.AREAS} placeholder="Select area" />
        </div>
      </PhoneShell>
      <PhoneFooter>
        <Button variant="dark" size="lg" full disabled={!valid} icon="check" onClick={submit}>Save {isDealer ? "dealer" : "shopkeeper"}</Button>
      </PhoneFooter>
    </>
  );
}

window.EmployeeApp = EmployeeApp;
Object.assign(window, { EmpHome, OnboardForm });
