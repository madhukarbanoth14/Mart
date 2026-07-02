/* ============================================================
   Flashmart — composite widgets  → window globals
   (depends on Icon, Badge from ui.jsx)
   ============================================================ */

/* ---------- Mobile app header (inside phone, below status bar) ---------- */
function AppHeader({ title, subtitle, kicker, right, onBack, accent, pad = true }) {
  return (
    <div style={{ padding: pad ? "8px 20px 14px" : 0, display: "flex", alignItems: "flex-start",
      justifyContent: "space-between", gap: 12 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12, minWidth: 0, flex: 1 }}>
        {onBack && (
          <button className="tap" onClick={onBack} style={{ width: 40, height: 40, borderRadius: 12, flexShrink: 0,
            border: "1px solid var(--line)", background: "var(--surface)", display: "grid", placeItems: "center",
            boxShadow: "var(--sh-sm)", cursor: "pointer", marginTop: 2 }}>
            <Icon name="chevL" size={20} stroke={2.2} />
          </button>
        )}
        <div style={{ minWidth: 0 }}>
          {kicker && <div style={{ fontSize: 12.5, fontWeight: 600, color: accent || "var(--brand)", letterSpacing: ".02em", marginBottom: 3 }}>{kicker}</div>}
          <div style={{ fontSize: 25, fontWeight: 700, letterSpacing: "-.025em", lineHeight: 1.1, color: "var(--ink)", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{title}</div>
          {subtitle && <div style={{ fontSize: 13.5, color: "var(--ink-3)", marginTop: 4 }}>{subtitle}</div>}
        </div>
      </div>
      {right && <div style={{ display: "flex", gap: 8, flexShrink: 0 }}>{right}</div>}
    </div>
  );
}

/* round icon button used in headers */
function IconBtn({ name, onClick, badge, active }) {
  return (
    <button className="tap" onClick={onClick} style={{ position: "relative", width: 42, height: 42, borderRadius: 13,
      border: "1px solid var(--line)", background: active ? "var(--brand-tint)" : "var(--surface)",
      color: active ? "var(--brand)" : "var(--ink-2)", display: "grid", placeItems: "center",
      boxShadow: "var(--sh-sm)", cursor: "pointer" }}>
      <Icon name={name} size={20} stroke={2} />
      {badge ? <span style={{ position: "absolute", top: -4, right: -4, minWidth: 18, height: 18, padding: "0 4px",
        borderRadius: 99, background: "var(--neg)", color: "#fff", fontSize: 10.5, fontWeight: 700,
        display: "grid", placeItems: "center", border: "2px solid var(--surface)" }} className="mono">{badge}</span> : null}
    </button>
  );
}

/* ---------- Bottom tab bar (mobile) ---------- */
function BottomNav({ items, active, onChange }) {
  return (
    <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, paddingBottom: 26, paddingTop: 8,
      background: "linear-gradient(to top, var(--surface) 72%, rgba(255,255,255,0))", zIndex: 30 }}>
      <div style={{ margin: "0 14px", height: 62, background: "var(--surface)", borderRadius: 20,
        border: "1px solid var(--line)", boxShadow: "var(--sh-lg)", display: "flex", alignItems: "center", padding: "0 6px" }}>
        {items.map(it => {
          const on = it.id === active;
          return (
            <button key={it.id} className="tap" onClick={() => onChange(it.id)}
              style={{ flex: 1, height: 50, border: "none", background: "transparent", cursor: "pointer",
                display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 3,
                color: on ? "var(--brand)" : "var(--ink-4)" }}>
              <div style={{ position: "relative" }}>
                <Icon name={it.icon} size={23} stroke={on ? 2.3 : 2} />
                {it.badge ? <span style={{ position: "absolute", top: -3, right: -7, minWidth: 16, height: 16,
                  padding: "0 4px", borderRadius: 99, background: "var(--neg)", color: "#fff", fontSize: 10, fontWeight: 700,
                  display: "grid", placeItems: "center", border: "2px solid var(--surface)" }} className="mono">{it.badge}</span> : null}
              </div>
              <span style={{ fontSize: 11, fontWeight: on ? 700 : 600, letterSpacing: "-.01em" }}>{it.label}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}

/* ---------- KPI / stat ---------- */
function Stat({ label, value, sub, icon, accent = "var(--brand)", tint = "var(--brand-tint)", trend, big }) {
  return (
    <Card pad={big ? 20 : 16} style={{ display: "flex", flexDirection: "column", gap: big ? 14 : 10 }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <span style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-3)", letterSpacing: "-.01em" }}>{label}</span>
        {icon && <div style={{ width: 34, height: 34, borderRadius: 10, background: tint, color: accent, display: "grid", placeItems: "center" }}>
          <Icon name={icon} size={18} stroke={2} /></div>}
      </div>
      <div style={{ display: "flex", alignItems: "baseline", gap: 9, flexWrap: "wrap" }}>
        <span className="mono" style={{ fontSize: big ? 34 : 26, fontWeight: 700, letterSpacing: "-.03em", color: "var(--ink)", lineHeight: 1 }}>{value}</span>
        {trend != null && <span style={{ fontSize: 12.5, fontWeight: 700, color: trend >= 0 ? "var(--pos)" : "var(--neg)" }}>
          {trend >= 0 ? "▲" : "▼"} {Math.abs(trend)}%</span>}
      </div>
      {sub && <div style={{ fontSize: 12.5, color: "var(--ink-4)" }}>{sub}</div>}
    </Card>
  );
}

/* ---------- money summary rows ---------- */
function MoneyRow({ label, value, strong, accent, sub }) {
  return (
    <div style={{ display: "flex", alignItems: "baseline", justifyContent: "space-between", gap: 12,
      padding: strong ? "12px 0 0" : "6px 0" }}>
      <span style={{ fontSize: strong ? 16 : 14, fontWeight: strong ? 700 : 500, minWidth: 0,
        color: strong ? "var(--ink)" : "var(--ink-2)" }}>{label}{sub && <span style={{ color: "var(--ink-4)", fontWeight: 500 }}> {sub}</span>}</span>
      <span className="mono" style={{ fontSize: strong ? 19 : 14, fontWeight: strong ? 700 : 600, flexShrink: 0, whiteSpace: "nowrap",
        color: accent || (strong ? "var(--ink)" : "var(--ink-2)"), letterSpacing: "-.02em" }}>{value}</span>
    </div>
  );
}

/* ---------- Bottom sheet (mobile modal) ---------- */
function Sheet({ open, onClose, children, title, height }) {
  if (!open) return null;
  return (
    <div onClick={onClose} style={{ position: "absolute", inset: 0, zIndex: 80, display: "flex",
      flexDirection: "column", justifyContent: "flex-end", background: "rgba(15,18,30,.42)",
      backdropFilter: "blur(2px)", animation: "fmFade .22s ease both" }}>
      <div onClick={e => e.stopPropagation()} className="fm-scroll"
        style={{ background: "var(--surface)", borderTopLeftRadius: 28, borderTopRightRadius: 28,
          maxHeight: height || "86%", overflow: "auto", boxShadow: "0 -10px 40px rgba(0,0,0,.2)",
          animation: "fmRise .3s cubic-bezier(.2,.8,.2,1) both", paddingBottom: 30 }}>
        <div style={{ position: "sticky", top: 0, background: "var(--surface)", paddingTop: 12, zIndex: 2 }}>
          <div style={{ width: 40, height: 5, borderRadius: 99, background: "var(--line-2)", margin: "0 auto 8px" }} />
          {title && <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "4px 20px 12px" }}>
            <span style={{ fontSize: 18, fontWeight: 700, letterSpacing: "-.02em" }}>{title}</span>
            <button className="tap" onClick={onClose} style={{ width: 32, height: 32, borderRadius: 99, border: "none",
              background: "var(--surface-3)", color: "var(--ink-2)", cursor: "pointer", fontSize: 17, lineHeight: 1 }}>✕</button>
          </div>}
        </div>
        {children}
      </div>
    </div>
  );
}

/* ---------- Order timeline (vertical) ---------- */
function OrderTimeline({ steps }) {
  // steps: [{label, time, state: done|active|todo, icon}]
  return (
    <div style={{ display: "flex", flexDirection: "column" }}>
      {steps.map((s, i) => {
        const last = i === steps.length - 1;
        const done = s.state === "done", active = s.state === "active";
        const col = done ? "var(--pos)" : active ? "var(--brand)" : "var(--line-2)";
        return (
          <div key={i} style={{ display: "flex", gap: 14, minHeight: last ? 0 : 58 }}>
            <div style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
              <div style={{ width: 30, height: 30, borderRadius: 99, flexShrink: 0,
                background: done ? "var(--pos)" : active ? "var(--brand)" : "var(--surface)",
                border: done || active ? "none" : "2px solid var(--line-2)",
                color: "#fff", display: "grid", placeItems: "center",
                boxShadow: active ? "0 0 0 5px var(--brand-tint)" : "none" }}>
                {done ? <Icon name="check" size={16} stroke={3} />
                  : active ? <span style={{ width: 9, height: 9, borderRadius: 99, background: "#fff",
                      animation: "fmPulse 1.4s ease-in-out infinite" }} />
                  : <span style={{ width: 8, height: 8, borderRadius: 99, background: "var(--line-2)" }} />}
              </div>
              {!last && <div style={{ width: 2, flex: 1, background: done ? "var(--pos)" : "var(--line)", marginTop: 2 }} />}
            </div>
            <div style={{ paddingBottom: last ? 0 : 16, paddingTop: 3 }}>
              <div style={{ fontSize: 15, fontWeight: 600, color: s.state === "todo" ? "var(--ink-4)" : "var(--ink)" }}>{s.label}</div>
              {s.time && <div style={{ fontSize: 12.5, color: active ? "var(--brand)" : "var(--ink-4)", marginTop: 2, fontWeight: active ? 600 : 500 }}>{s.time}</div>}
            </div>
          </div>
        );
      })}
    </div>
  );
}

/* ---------- Bar chart (monthly sales) ---------- */
function BarChart({ data, height = 180, accent = "var(--brand)", fmt = v => v, dark }) {
  const max = Math.max(...data.map(d => d.v));
  const axis = dark ? "rgba(255,255,255,.5)" : "var(--ink-4)";
  return (
    <div style={{ display: "flex", alignItems: "flex-end", gap: 14, height, paddingTop: 8 }}>
      {data.map((d, i) => {
        const h = Math.max(6, (d.v / max) * (height - 34));
        const peak = d.v === max;
        return (
          <div key={d.m} style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", gap: 8, height: "100%", justifyContent: "flex-end" }}>
            <span className="mono" style={{ fontSize: 11, fontWeight: 700, color: peak ? accent : axis }}>{fmt(d.v)}</span>
            <div style={{ width: "100%", maxWidth: 46, height: h, borderRadius: "8px 8px 4px 4px",
              background: peak ? accent : (dark ? "rgba(255,255,255,.16)" : "var(--brand-tint)"),
              animation: `fmRise .6s cubic-bezier(.2,.8,.2,1) ${i * 0.06}s both`,
              border: peak ? "none" : (dark ? "none" : "1px solid var(--brand-tint)") }} />
            <span style={{ fontSize: 12, fontWeight: 600, color: axis }}>{d.m}</span>
          </div>
        );
      })}
    </div>
  );
}

/* ---------- Donut (conic-gradient) ---------- */
function Donut({ data, size = 150, colors }) {
  const palette = colors || ["var(--brand)", "#5b74e8", "#0e9e6e", "#c97a16", "#9aa1ad"];
  let acc = 0; const stops = [];
  data.forEach((d, i) => {
    const start = acc; acc += d.pct;
    stops.push(`${palette[i % palette.length]} ${start}% ${acc}%`);
  });
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 22 }}>
      <div style={{ width: size, height: size, borderRadius: "50%", flexShrink: 0,
        background: `conic-gradient(${stops.join(",")})`, position: "relative" }}>
        <div style={{ position: "absolute", inset: size * 0.22, borderRadius: "50%", background: "var(--surface)",
          display: "grid", placeItems: "center", boxShadow: "inset 0 1px 3px rgba(0,0,0,.05)" }}>
          <div style={{ textAlign: "center" }}>
            <div className="mono" style={{ fontSize: 20, fontWeight: 700, letterSpacing: "-.03em" }}>{data.length}</div>
            <div style={{ fontSize: 10.5, color: "var(--ink-4)", fontWeight: 600 }}>categories</div>
          </div>
        </div>
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 9, flex: 1 }}>
        {data.map((d, i) => (
          <div key={d.cat} style={{ display: "flex", alignItems: "center", gap: 9 }}>
            <span style={{ width: 10, height: 10, borderRadius: 3, background: palette[i % palette.length], flexShrink: 0 }} />
            <span style={{ fontSize: 13, color: "var(--ink-2)", flex: 1, fontWeight: 500 }}>{d.cat}</span>
            <span className="mono" style={{ fontSize: 13, fontWeight: 700 }}>{d.pct}%</span>
          </div>
        ))}
      </div>
    </div>
  );
}

/* ---------- Success check animation ---------- */
function SuccessCheck({ size = 96, color = "var(--pos)" }) {
  return (
    <div style={{ width: size, height: size, position: "relative", animation: "fmPop .5s cubic-bezier(.2,.8,.2,1) both" }}>
      <svg width={size} height={size} viewBox="0 0 100 100">
        <circle cx="50" cy="50" r="46" fill="none" stroke={color} strokeOpacity="0.18" strokeWidth="6" />
        <circle cx="50" cy="50" r="46" fill={color} fillOpacity="0.1" />
        <path d="M30 51 L44 65 L71 36" fill="none" stroke={color} strokeWidth="7" strokeLinecap="round" strokeLinejoin="round"
          strokeDasharray="70" strokeDashoffset="70" style={{ animation: "fmCheck .5s .25s cubic-bezier(.6,0,.3,1) forwards" }} />
      </svg>
    </div>
  );
}

/* ---------- Empty / illustration placeholder ---------- */
function PlaceholderTile({ label, height = 120, icon = "box" }) {
  return (
    <div style={{ height, borderRadius: 14, border: "1.5px dashed var(--line-2)", background: "var(--surface-2)",
      display: "grid", placeItems: "center", color: "var(--ink-4)",
      backgroundImage: "repeating-linear-gradient(135deg, rgba(0,0,0,.018) 0 8px, transparent 8px 16px)" }}>
      <div style={{ textAlign: "center" }}>
        <Icon name={icon} size={26} stroke={1.6} style={{ margin: "0 auto 6px" }} />
        <div style={{ fontFamily: "var(--mono)", fontSize: 11, letterSpacing: ".02em" }}>{label}</div>
      </div>
    </div>
  );
}

const _pulseStyle = document.createElement("style");
_pulseStyle.textContent = "@keyframes fmPulse{0%,100%{opacity:1;transform:scale(1)}50%{opacity:.4;transform:scale(.7)}}";
document.head.appendChild(_pulseStyle);

Object.assign(window, { AppHeader, IconBtn, BottomNav, Stat, MoneyRow, Sheet, OrderTimeline, BarChart, Donut, SuccessCheck, PlaceholderTile });
