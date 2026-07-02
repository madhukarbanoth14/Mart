/* ============================================================
   Flashmart — phone shell + form helpers  → window globals
   ============================================================ */

/* Full-height phone layout: fixed header zone, scroll body, optional bottom nav.
   Renders inside IOSDevice content area (which spans full device incl. status bar). */
function PhoneShell({ header, children, nav, bg = "var(--bg)", scrollRef, padBottom = 28 }) {
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: bg, position: "relative", overflow: "hidden" }}>
      {header && (
        <div style={{ paddingTop: 50, background: bg, position: "relative", zIndex: 5 }}>
          {header}
        </div>
      )}
      <div ref={scrollRef} className="fm-scroll" style={{ flex: 1, overflowY: "auto", overflowX: "hidden",
        padding: `4px 0 ${nav ? 100 : padBottom}px` }}>
        {children}
      </div>
      {nav}
    </div>
  );
}

/* Avatar with initials */
function Avatar({ name, size = 40, tint = "var(--brand)", img }) {
  const initials = (name || "?").split(/\s+/).slice(0, 2).map(w => w[0]).join("").toUpperCase();
  return (
    <div style={{ width: size, height: size, borderRadius: size * 0.32, flexShrink: 0,
      background: `linear-gradient(140deg, ${tint}, color-mix(in srgb, ${tint} 70%, #000))`,
      color: "#fff", display: "grid", placeItems: "center", fontWeight: 700,
      fontSize: size * 0.38, letterSpacing: "-.02em", boxShadow: "inset 0 1px 0 rgba(255,255,255,.25)" }}>
      {initials}
    </div>
  );
}

/* Section label (small caps) */
function SectionLabel({ children, action, onAction, style }) {
  return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between",
      padding: "0 2px 10px", ...style }}>
      <span style={{ fontSize: 12.5, fontWeight: 700, color: "var(--ink-3)", letterSpacing: ".04em", textTransform: "uppercase" }}>{children}</span>
      {action && <button className="tap" onClick={onAction} style={{ border: "none", background: "transparent",
        color: "var(--brand)", fontSize: 13, fontWeight: 600, cursor: "pointer", padding: 0 }}>{action}</button>}
    </div>
  );
}

/* Generic list row */
function Row({ left, title, sub, right, onClick, accent, last }) {
  return (
    <div className={onClick ? "tap" : ""} onClick={onClick}
      style={{ display: "flex", alignItems: "center", gap: 13, padding: "13px 4px",
        borderBottom: last ? "none" : "1px solid var(--line)" }}>
      {left}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 15, fontWeight: 600, color: "var(--ink)", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{title}</div>
        {sub && <div style={{ fontSize: 12.5, color: "var(--ink-3)", marginTop: 2 }}>{sub}</div>}
      </div>
      {right}
    </div>
  );
}

/* Form field */
function Field({ label, value, onChange, placeholder, icon, type = "text", prefix, hint, options }) {
  const [focus, setFocus] = React.useState(false);
  return (
    <label style={{ display: "block" }}>
      {label && <div style={{ fontSize: 13, fontWeight: 600, color: "var(--ink-2)", marginBottom: 7 }}>{label}</div>}
      <div style={{ display: "flex", alignItems: "center", gap: 10, height: 50, padding: "0 14px",
        background: "var(--surface)", borderRadius: 13,
        border: `1.5px solid ${focus ? "var(--brand)" : "var(--line-2)"}`,
        boxShadow: focus ? "0 0 0 4px var(--brand-tint)" : "none", transition: "all .15s" }}>
        {icon && <Icon name={icon} size={18} color="var(--ink-4)" stroke={2} />}
        {prefix && <span className="mono" style={{ color: "var(--ink-3)", fontWeight: 600 }}>{prefix}</span>}
        {options ? (
          <select value={value} onChange={e => onChange(e.target.value)} onFocus={() => setFocus(true)} onBlur={() => setFocus(false)}
            style={{ flex: 1, border: "none", outline: "none", background: "transparent", fontFamily: "inherit",
              fontSize: 15, color: value ? "var(--ink)" : "var(--ink-4)", appearance: "none", cursor: "pointer" }}>
            <option value="" disabled>{placeholder || "Select…"}</option>
            {options.map(o => <option key={o} value={o}>{o}</option>)}
          </select>
        ) : (
          <input type={type} value={value} placeholder={placeholder}
            onChange={e => onChange(e.target.value)} onFocus={() => setFocus(true)} onBlur={() => setFocus(false)}
            style={{ flex: 1, border: "none", outline: "none", background: "transparent", fontFamily: "inherit",
              fontSize: 15, color: "var(--ink)", minWidth: 0 }} />
        )}
        {options && <Icon name="chevD" size={16} color="var(--ink-4)" />}
      </div>
      {hint && <div style={{ fontSize: 12, color: "var(--ink-4)", marginTop: 6 }}>{hint}</div>}
    </label>
  );
}

Object.assign(window, { PhoneShell, Avatar, SectionLabel, Row, Field });
