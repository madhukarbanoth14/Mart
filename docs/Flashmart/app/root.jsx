/* ============================================================
   Flashmart — root: stage, role switcher, scaling, tweaks
   ============================================================ */

const BRAND_PALETTES = {
  Indigo:  { brand: "#2f48d4", b6: "#2839b8", b7: "#1f2c93", tint: "#eef0fd", bink: "#1a2470" },
  Cobalt:  { brand: "#2563eb", b6: "#1d4ed8", b7: "#1e40af", tint: "#eaf1fe", bink: "#1c3aa0" },
  Emerald: { brand: "#0e9e6e", b6: "#0b855d", b7: "#086b4b", tint: "#e3f6ef", bink: "#0a5c41" },
  Violet:  { brand: "#6d4ad4", b6: "#5b3bc0", b7: "#4a2ea0", tint: "#f0ebfc", bink: "#43288f" },
};
const FONTS = {
  "Schibsted Grotesk": "'Schibsted Grotesk'",
  "Hanken Grotesk": "'Hanken Grotesk'",
  "Plus Jakarta Sans": "'Plus Jakarta Sans'",
  "Instrument Sans": "'Instrument Sans'",
};

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "accent": "Indigo",
  "font": "Schibsted Grotesk",
  "adminLayout": "Spotlight",
  "roundedDevice": true
}/*EDITMODE-END*/;

/* ---- scale-to-fit wrapper ---- */
function Fit({ w, h, children, pad = 48 }) {
  const ref = React.useRef(null);
  const [scale, setScale] = React.useState(1);
  React.useEffect(() => {
    const el = ref.current; if (!el) return;
    const calc = () => {
      const aw = el.clientWidth - pad, ah = el.clientHeight - pad;
      setScale(Math.min(1, aw / w, ah / h));
    };
    calc();
    const ro = new ResizeObserver(calc); ro.observe(el);
    return () => ro.disconnect();
  }, [w, h, pad]);
  return (
    <div ref={ref} style={{ flex: 1, minWidth: 0, display: "grid", placeItems: "center", overflow: "hidden" }}>
      <div style={{ width: w, height: h, transform: `scale(${scale})`, transformOrigin: "center", flexShrink: 0 }}>
        {children}
      </div>
    </div>
  );
}

const ROLES = [
  { id: "shopkeeper", label: "Shopkeeper", icon: "bag", tint: "#2f48d4" },
  { id: "dealer", label: "Dealer", icon: "truck", tint: "#0e9e6e" },
  { id: "employee", label: "Employee", icon: "users", tint: "#c97a16" },
  { id: "admin", label: "Admin", icon: "chart", tint: "#11152a" },
];

function RoleSwitcher({ role, setRole }) {
  return (
    <div style={{ display: "flex", gap: 4, background: "rgba(19,23,34,.05)", padding: 5, borderRadius: 16, border: "1px solid var(--line)" }}>
      {ROLES.map(r => {
        const on = r.id === role;
        return (
          <button key={r.id} className="tap" onClick={() => setRole(r.id)}
            style={{ display: "flex", alignItems: "center", gap: 8, border: "none", cursor: "pointer",
              padding: "9px 15px", borderRadius: 11, fontSize: 13.5, fontWeight: 600, letterSpacing: "-.01em",
              background: on ? "var(--surface)" : "transparent", color: on ? "var(--ink)" : "var(--ink-3)",
              boxShadow: on ? "var(--sh-md)" : "none" }}>
            <Icon name={r.icon} size={17} stroke={2.1} color={on ? r.tint : "var(--ink-4)"} />
            {r.label}
          </button>
        );
      })}
    </div>
  );
}

/* contextual hint for the guided demo */
const ROLE_HINT = {
  shopkeeper: "Browse the catalog → add to cart → pay → watch the order appear in the Dealer app.",
  dealer: "Accept a pending order, then mark it delivered — the Shopkeeper's tracker advances live.",
  employee: "Onboard a new dealer or shopkeeper — they appear instantly in the Admin directory.",
  admin: "The investor view. Revenue, orders & network update live as the demo runs.",
};

function App() {
  const [role, setRole] = React.useState("shopkeeper");
  const [t, setTweak] = useTweaks(TWEAK_DEFAULTS);

  // apply theme tweaks → :root
  React.useEffect(() => {
    const p = BRAND_PALETTES[t.accent] || BRAND_PALETTES.Indigo;
    const r = document.documentElement.style;
    r.setProperty("--brand", p.brand); r.setProperty("--brand-600", p.b6);
    r.setProperty("--brand-700", p.b7); r.setProperty("--brand-tint", p.tint);
    r.setProperty("--brand-ink", p.bink); r.setProperty("--info", p.brand);
    r.setProperty("--font", (FONTS[t.font] || FONTS["Schibsted Grotesk"]) + ", system-ui, sans-serif");
  }, [t.accent, t.font]);

  return (
    <StoreProvider role={role} setRole={setRole}>
      <Stage t={t} setTweak={setTweak} />
    </StoreProvider>
  );
}

function Stage({ t, setTweak }) {
  const S = useStore();
  const role = S.role;
  return (
    <div style={{ position: "fixed", inset: 0, display: "flex", flexDirection: "column",
      background: "radial-gradient(120% 120% at 50% 0%, #f4f6fa 0%, #e7eaf1 100%)" }}>
      {/* top bar */}
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 16,
        padding: "16px 22px", borderBottom: "1px solid var(--line)", background: "rgba(255,255,255,.7)",
        backdropFilter: "blur(12px)", zIndex: 10, flexShrink: 0 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <Logo size={26} />
          <span style={{ fontSize: 12, fontWeight: 600, color: "var(--ink-4)", background: "var(--surface-3)",
            padding: "4px 10px", borderRadius: 8, letterSpacing: ".02em" }}>Distribution OS · Demo</span>
        </div>
        <RoleSwitcher role={role} setRole={S.setRole} />
        <div style={{ display: "flex", alignItems: "center", gap: 10, minWidth: 260, justifyContent: "flex-end" }}>
          <span style={{ fontSize: 12, color: "var(--ink-3)", fontWeight: 500, maxWidth: 300, textAlign: "right", lineHeight: 1.4 }}>{ROLE_HINT[role]}</span>
        </div>
      </div>

      {/* stage */}
      <div style={{ flex: 1, minHeight: 0, display: "flex" }}>
        {role === "admin" ? (
          <Fit w={1280} h={812} pad={56}>
            <ChromeWindow width={1280} height={812} url="admin.flashmart.in/overview">
              <AdminApp layout={t.adminLayout} />
            </ChromeWindow>
          </Fit>
        ) : (
          <Fit w={402} h={874} pad={40}>
            <IOSDevice>
              {role === "shopkeeper" && <ShopkeeperApp />}
              {role === "dealer" && <DealerApp />}
              {role === "employee" && <EmployeeApp />}
            </IOSDevice>
          </Fit>
        )}
      </div>

      {/* tweaks */}
      <TweaksPanel>
        <TweakSection label="Brand" />
        <TweakColor label="Accent" value={BRAND_PALETTES[t.accent].brand}
          options={Object.values(BRAND_PALETTES).map(p => p.brand)}
          onChange={(v) => { const name = Object.keys(BRAND_PALETTES).find(k => BRAND_PALETTES[k].brand === v); setTweak("accent", name); }} />
        <TweakSelect label="Typeface" value={t.font} options={Object.keys(FONTS)} onChange={(v) => setTweak("font", v)} />
        <TweakSection label="Admin dashboard" />
        <TweakRadio label="Layout" value={t.adminLayout} options={["Spotlight", "Grid", "Analytics"]} onChange={(v) => setTweak("adminLayout", v)} />
      </TweaksPanel>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);
