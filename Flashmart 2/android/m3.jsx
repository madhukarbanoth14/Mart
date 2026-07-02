/* ============================================================
   FlashMart Android — Material 3 component kit  → window globals
   True M3 (containers, state layers, ripples, nav indicator pill)
   wearing the FlashMart brand (green seed) + Schibsted Grotesk.
   Reuses Icon / ProductThumb / Avatar from the shared kit.
   ============================================================ */

const M = {
  primary: "var(--m3-primary)", onPrimary: "var(--m3-on-primary)",
  primCont: "var(--m3-primary-container)", onPrimCont: "var(--m3-on-primary-container)",
  sec: "var(--m3-secondary)", secCont: "var(--m3-secondary-container)", onSecCont: "var(--m3-on-secondary-container)",
  tert: "var(--m3-tertiary)", tertCont: "var(--m3-tertiary-container)", onTertCont: "var(--m3-on-tertiary-container)",
  err: "var(--m3-error)", errCont: "var(--m3-error-container)", onErrCont: "var(--m3-on-error-container)",
  surface: "var(--m3-surface)", surf0: "var(--m3-surface-cont-lowest)", surf1: "var(--m3-surface-cont-low)",
  surf2: "var(--m3-surface-cont)", surf3: "var(--m3-surface-cont-high)", surf4: "var(--m3-surface-cont-highest)",
  onSurf: "var(--m3-on-surface)", onSurfVar: "var(--m3-on-surface-var)",
  outline: "var(--m3-outline)", outlineVar: "var(--m3-outline-var)",
};

/* ---------- ripple ---------- */
function mRipple(e, color = "rgba(255,255,255,.45)") {
  const el = e.currentTarget;
  const r = el.getBoundingClientRect();
  const d = Math.max(r.width, r.height);
  const s = document.createElement("span");
  s.style.cssText = `position:absolute;border-radius:50%;pointer-events:none;width:${d}px;height:${d}px;left:${e.clientX - r.left - d / 2}px;top:${e.clientY - r.top - d / 2}px;background:${color};transform:scale(0);opacity:1;animation:m3Ripple .55s ease-out forwards;z-index:0;`;
  el.appendChild(s);
  setTimeout(() => s.remove(), 600);
}

/* ---------- Button ---------- */
function M3Button({ children, variant = "filled", size = "md", icon, iconRight, full, onClick, disabled, style }) {
  const sz = { sm: { h: 40, px: 18, fs: 14 }, md: { h: 48, px: 24, fs: 15 }, lg: { h: 56, px: 28, fs: 16 } }[size];
  const V = {
    filled:   { background: M.primary, color: M.onPrimary, border: "none", rip: "rgba(255,255,255,.5)" },
    tonal:    { background: M.secCont, color: M.onSecCont, border: "none", rip: "rgba(0,0,0,.10)" },
    outlined: { background: "transparent", color: M.primary, border: `1px solid ${M.outlineVar}`, rip: M.primCont },
    text:     { background: "transparent", color: M.primary, border: "none", rip: M.primCont },
    elevated: { background: M.surf1, color: M.primary, border: "none", boxShadow: "var(--m3-e1)", rip: M.primCont },
    gold:     { background: "var(--gold)", color: "#3a2900", border: "none", rip: "rgba(0,0,0,.12)" },
  }[variant];
  return (
    <button onClick={disabled ? undefined : (e) => { mRipple(e, V.rip); onClick && onClick(e); }} disabled={disabled}
      style={{ position: "relative", overflow: "hidden", height: sz.h, padding: `0 ${sz.px}px`,
        borderRadius: 999, fontFamily: "var(--roboto)", fontSize: sz.fs, fontWeight: 600, letterSpacing: ".01em",
        display: "inline-flex", alignItems: "center", justifyContent: "center", gap: 9, cursor: disabled ? "default" : "pointer",
        width: full ? "100%" : undefined, opacity: disabled ? 0.4 : 1, boxShadow: V.boxShadow, ...V, ...style }}>
      {icon && <span style={{ position: "relative", zIndex: 1, display: "flex" }}><Icon name={icon} size={sz.fs + 4} stroke={2.2} /></span>}
      <span style={{ position: "relative", zIndex: 1 }}>{children}</span>
      {iconRight && <span style={{ position: "relative", zIndex: 1, display: "flex" }}><Icon name={iconRight} size={sz.fs + 4} stroke={2.2} /></span>}
    </button>
  );
}

/* ---------- FAB / Extended FAB ---------- */
function M3FAB({ icon = "plus", label, onClick, color = "tert", style }) {
  const palette = { tert: { bg: M.tertCont, fg: M.onTertCont }, primary: { bg: M.primCont, fg: M.onPrimCont }, surface: { bg: M.surf3, fg: M.primary } }[color];
  const ext = !!label;
  return (
    <button onClick={(e) => { mRipple(e, "rgba(0,0,0,.10)"); onClick && onClick(e); }}
      style={{ position: "relative", overflow: "hidden", height: 56, width: ext ? undefined : 56,
        padding: ext ? "0 20px" : 0, borderRadius: 18, border: "none", cursor: "pointer",
        background: palette.bg, color: palette.fg, boxShadow: "var(--m3-e3)",
        display: "inline-flex", alignItems: "center", justifyContent: "center", gap: 12,
        fontFamily: "var(--roboto)", fontSize: 15, fontWeight: 700, animation: "m3FabIn .3s cubic-bezier(.2,.8,.2,1) both", ...style }}>
      <span style={{ position: "relative", zIndex: 1, display: "flex" }}><Icon name={icon} size={24} stroke={2.3} /></span>
      {ext && <span style={{ position: "relative", zIndex: 1 }}>{label}</span>}
    </button>
  );
}

/* ---------- Icon button ---------- */
function M3IconBtn({ icon, onClick, badge, filled, color = M.onSurfVar, size = 24 }) {
  return (
    <button onClick={(e) => { mRipple(e, "rgba(0,0,0,.10)"); onClick && onClick(e); }}
      style={{ position: "relative", overflow: "hidden", width: 44, height: 44, borderRadius: 999, border: "none",
        background: filled ? M.surf3 : "transparent", color, cursor: "pointer", display: "grid", placeItems: "center", flexShrink: 0 }}>
      <span style={{ position: "relative", zIndex: 1, display: "flex" }}><Icon name={icon} size={size} stroke={2.1} /></span>
      {badge != null && <span className="mono" style={{ position: "absolute", top: 6, right: 5, minWidth: 16, height: 16, padding: "0 4px",
        borderRadius: 99, background: M.err, color: "#fff", fontSize: 10, fontWeight: 700, display: "grid", placeItems: "center", zIndex: 2 }}>{badge}</span>}
    </button>
  );
}

/* ---------- Top app bar (small / center / large) ---------- */
function M3TopBar({ title, subtitle, variant = "small", onBack, actions, scrolled, color }) {
  const bg = scrolled ? M.surf2 : M.surface;
  const large = variant === "large";
  const center = variant === "center";
  return (
    <div style={{ background: bg, paddingTop: AND_TOP, transition: "background .2s", position: "relative", zIndex: 10,
      boxShadow: scrolled ? "0 1px 0 var(--m3-outline-var)" : "none" }}>
      <div style={{ height: 56, display: "flex", alignItems: "center", padding: "0 4px 0 6px", gap: 4 }}>
        {onBack
          ? <M3IconBtn icon="chevL" onClick={onBack} color={M.onSurf} />
          : <div style={{ width: 8 }} />}
        {!large && <div style={{ flex: 1, textAlign: center ? "center" : "left", paddingLeft: center ? 0 : 6, minWidth: 0 }}>
          <div style={{ fontSize: 20, fontWeight: 700, letterSpacing: "-.02em", color: color || M.onSurf, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{title}</div>
          {subtitle && <div style={{ fontSize: 12.5, color: M.onSurfVar, marginTop: 1 }}>{subtitle}</div>}
        </div>}
        {large && <div style={{ flex: 1 }} />}
        <div style={{ display: "flex", alignItems: "center", gap: 2 }}>{actions}</div>
      </div>
      {large && <div style={{ padding: "4px 16px 18px" }}>
        <div style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-.03em", color: color || M.onSurf, lineHeight: 1.08 }}>{title}</div>
        {subtitle && <div style={{ fontSize: 14, color: M.onSurfVar, marginTop: 5 }}>{subtitle}</div>}
      </div>}
    </div>
  );
}

/* ---------- Bottom navigation bar (M3 active-indicator pill) ---------- */
function M3NavBar({ items, active, onChange }) {
  return (
    <div style={{ background: M.surf2, paddingTop: 12, paddingBottom: 12 + AND_BOTTOM, position: "relative", zIndex: 30,
      boxShadow: "0 -1px 0 var(--m3-outline-var)" }}>
      <div style={{ display: "flex", padding: "0 4px" }}>
        {items.map((it) => {
          const on = it.id === active;
          return (
            <button key={it.id} onClick={(e) => { mRipple(e, "rgba(0,0,0,.06)"); onChange && onChange(it.id); }}
              style={{ position: "relative", overflow: "hidden", flex: 1, border: "none", background: "transparent", cursor: "pointer",
                display: "flex", flexDirection: "column", alignItems: "center", gap: 4, padding: "0 0 2px" }}>
              <div style={{ position: "relative", width: 64, height: 32, borderRadius: 999, display: "grid", placeItems: "center",
                background: on ? M.secCont : "transparent", transition: "background .2s" }}>
                <span style={{ position: "relative", zIndex: 1, display: "flex", color: on ? M.onSecCont : M.onSurfVar }}>
                  <Icon name={it.icon} size={24} stroke={on ? 2.4 : 2} />
                </span>
                {it.badge != null && <span className="mono" style={{ position: "absolute", top: -1, right: 9, minWidth: 16, height: 16, padding: "0 4px",
                  borderRadius: 99, background: M.err, color: "#fff", fontSize: 10, fontWeight: 700, display: "grid", placeItems: "center", zIndex: 2, border: `2px solid ${M.surf2}` }}>{it.badge}</span>}
              </div>
              <span style={{ fontSize: 12, fontWeight: on ? 700 : 600, color: on ? M.onSurf : M.onSurfVar, letterSpacing: "-.01em" }}>{it.label}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}

/* ---------- Card (filled / elevated / outlined) ---------- */
function M3Card({ children, variant = "filled", pad = 16, onClick, style, className = "" }) {
  const V = {
    filled:   { background: M.surf2, border: "none", boxShadow: "none" },
    elevated: { background: M.surf1, border: "none", boxShadow: "var(--m3-e1)" },
    outlined: { background: M.surface, border: `1px solid ${M.outlineVar}`, boxShadow: "none" },
  }[variant];
  return (
    <div onClick={onClick} className={(onClick ? "tap " : "") + className}
      style={{ borderRadius: 16, padding: pad, ...V, ...style }}>{children}</div>
  );
}

/* ---------- Chips (filter / assist / input) ---------- */
function M3Chip({ children, selected, onClick, icon, leadingCheck, elevated, color }) {
  return (
    <button onClick={(e) => { mRipple(e, "rgba(0,0,0,.08)"); onClick && onClick(e); }}
      style={{ position: "relative", overflow: "hidden", height: 32, padding: "0 14px", borderRadius: 8,
        border: selected ? "none" : `1px solid ${M.outlineVar}`, cursor: "pointer", flexShrink: 0,
        background: selected ? (color || M.secCont) : (elevated ? M.surf1 : "transparent"),
        color: selected ? M.onSecCont : M.onSurfVar, boxShadow: elevated && !selected ? "var(--m3-e1)" : "none",
        display: "inline-flex", alignItems: "center", gap: 7, fontFamily: "var(--roboto)", fontSize: 13.5, fontWeight: 600 }}>
      {selected && leadingCheck && <span style={{ position: "relative", zIndex: 1, display: "flex" }}><Icon name="check" size={16} stroke={2.6} /></span>}
      {icon && !leadingCheck && <span style={{ position: "relative", zIndex: 1, display: "flex" }}><Icon name={icon} size={16} stroke={2.2} /></span>}
      <span style={{ position: "relative", zIndex: 1 }}>{children}</span>
    </button>
  );
}

/* ---------- Segmented buttons (connected) ---------- */
function M3Segmented({ options, value, onChange, full }) {
  return (
    <div style={{ display: "inline-flex", width: full ? "100%" : undefined, borderRadius: 999, overflow: "hidden",
      border: `1px solid ${M.outlineVar}` }}>
      {options.map((o, i) => {
        const val = typeof o === "string" ? o : o.value;
        const lab = typeof o === "string" ? o : o.label;
        const on = val === value;
        return (
          <button key={val} onClick={(e) => { mRipple(e, "rgba(0,0,0,.07)"); onChange && onChange(val); }}
            style={{ position: "relative", overflow: "hidden", flex: full ? 1 : undefined, height: 40, padding: "0 14px",
              border: "none", borderLeft: i ? `1px solid ${M.outlineVar}` : "none", cursor: "pointer",
              background: on ? M.secCont : "transparent", color: on ? M.onSecCont : M.onSurfVar,
              fontFamily: "var(--roboto)", fontSize: 13.5, fontWeight: 600, display: "inline-flex", alignItems: "center", justifyContent: "center", gap: 6 }}>
            {on && <span style={{ position: "relative", zIndex: 1, display: "flex" }}><Icon name="check" size={16} stroke={2.6} /></span>}
            <span style={{ position: "relative", zIndex: 1, whiteSpace: "nowrap" }}>{lab}</span>
          </button>
        );
      })}
    </div>
  );
}

/* ---------- Text field (filled / outlined) ---------- */
function M3Field({ label, value, placeholder, icon, trailing, prefix, supporting, error, variant = "filled", focused, multiline, style }) {
  const active = focused || value;
  const outlined = variant === "outlined";
  const borderCol = error ? M.err : focused ? M.primary : M.outline;
  return (
    <div style={style}>
      <div style={{ position: "relative", display: "flex", alignItems: multiline ? "flex-start" : "center", gap: 12,
        minHeight: 56, padding: multiline ? "14px 16px" : "0 16px",
        background: outlined ? "transparent" : M.surf4,
        borderRadius: outlined ? 8 : "8px 8px 0 0",
        border: outlined ? `${focused ? 2 : 1}px solid ${borderCol}` : "none",
        borderBottom: outlined ? undefined : `${focused ? 2 : 1}px solid ${borderCol}`,
        paddingTop: !outlined && active && label ? 18 : (multiline ? 14 : 0) }}>
        {icon && <Icon name={icon} size={20} color={M.onSurfVar} stroke={2} style={{ marginTop: multiline ? 1 : 0 }} />}
        {label && <span style={{ position: "absolute", left: icon ? 46 : 16,
          top: active ? (outlined ? -8 : 8) : "50%", transform: active ? "none" : "translateY(-50%)",
          fontSize: active ? 12 : 15.5, color: error ? M.err : focused ? M.primary : M.onSurfVar, fontWeight: 500,
          background: outlined ? M.surface : "transparent", padding: outlined ? "0 4px" : 0, pointerEvents: "none", transition: "all .15s" }}>{label}</span>}
        {prefix && active && <span className="mono" style={{ color: M.onSurfVar, fontWeight: 600, fontSize: 15 }}>{prefix}</span>}
        <span style={{ flex: 1, fontSize: 15.5, color: value ? M.onSurf : M.onSurfVar, paddingTop: !outlined && active && label ? 0 : 0,
          minHeight: multiline ? 44 : "auto", whiteSpace: multiline ? "pre-wrap" : "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
          {value || (active ? placeholder : "")}
        </span>
        {trailing}
      </div>
      {supporting && <div style={{ fontSize: 12, color: error ? M.err : M.onSurfVar, padding: "5px 16px 0" }}>{supporting}</div>}
    </div>
  );
}

/* ---------- List item ---------- */
function M3ListItem({ leading, headline, supporting, trailing, onClick, last }) {
  return (
    <button onClick={onClick ? (e) => { mRipple(e, "rgba(0,0,0,.06)"); onClick(e); } : undefined}
      style={{ position: "relative", overflow: "hidden", width: "100%", textAlign: "left", border: "none", background: "transparent",
        cursor: onClick ? "pointer" : "default", display: "flex", alignItems: "center", gap: 16, padding: "12px 4px", minHeight: 56,
        borderBottom: last ? "none" : `1px solid var(--m3-outline-var)`, fontFamily: "var(--roboto)" }}>
      {leading && <div style={{ position: "relative", zIndex: 1, flexShrink: 0 }}>{leading}</div>}
      <div style={{ position: "relative", zIndex: 1, flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 15.5, fontWeight: 600, color: M.onSurf, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{headline}</div>
        {supporting && <div style={{ fontSize: 13, color: M.onSurfVar, marginTop: 1 }}>{supporting}</div>}
      </div>
      {trailing && <div style={{ position: "relative", zIndex: 1, flexShrink: 0 }}>{trailing}</div>}
    </button>
  );
}

/* ---------- Switch ---------- */
function M3Switch({ on, onToggle }) {
  return (
    <button onClick={onToggle} style={{ width: 52, height: 32, borderRadius: 999, border: on ? "none" : `2px solid ${M.outline}`,
      background: on ? M.primary : M.surf4, position: "relative", cursor: "pointer", transition: "background .2s", flexShrink: 0, padding: 0 }}>
      <span style={{ position: "absolute", top: "50%", left: on ? 28 : 6, transform: "translateY(-50%)",
        width: on ? 24 : 16, height: on ? 24 : 16, borderRadius: 999, background: on ? "#fff" : M.outline,
        transition: "all .2s", display: "grid", placeItems: "center" }}>
        {on && <Icon name="check" size={14} color={M.primary} stroke={3} />}
      </span>
    </button>
  );
}

/* ---------- Status pill (tonal) ---------- */
const M3_STATUS = {
  placed:    { l: "Placed", bg: M.surf4, fg: M.onSurfVar },
  pending:   { l: "Pending", bg: "var(--gold-tint)", fg: "var(--gold-ink)" },
  accepted:  { l: "Accepted", bg: M.secCont, fg: M.onSecCont },
  out:       { l: "Out for delivery", bg: "var(--gold-tint)", fg: "var(--gold-ink)" },
  delivered: { l: "Delivered", bg: M.primCont, fg: M.onPrimCont },
  paid:      { l: "Paid", bg: M.primCont, fg: M.onPrimCont },
  cancelled: { l: "Cancelled", bg: M.errCont, fg: M.onErrCont },
  Active:    { l: "Active", bg: M.primCont, fg: M.onPrimCont },
  low:       { l: "Low stock", bg: M.errCont, fg: M.onErrCont },
  Approved:  { l: "Approved", bg: M.primCont, fg: M.onPrimCont },
  Rejected:  { l: "Rejected", bg: M.errCont, fg: M.onErrCont },
  Pending:   { l: "Pending", bg: "var(--gold-tint)", fg: "var(--gold-ink)" },
};
function M3Status({ status, label, dot, size = "md" }) {
  const s = M3_STATUS[status] || { l: status, bg: M.surf4, fg: M.onSurfVar };
  const sm = size === "sm";
  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: 6, background: s.bg, color: s.fg,
      fontWeight: 700, fontSize: sm ? 11 : 12, letterSpacing: "-.01em", padding: sm ? "4px 9px" : "5px 11px", borderRadius: 999, whiteSpace: "nowrap" }}>
      {dot && <span style={{ width: 6, height: 6, borderRadius: 99, background: s.fg }} />}{label || s.l}
    </span>
  );
}

/* ---------- Screen scaffold ---------- */
function M3Screen({ children, bg = M.surface, topBar, nav, fab, sheet }) {
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: bg, position: "relative", overflow: "hidden" }}>
      {topBar}
      <div className="fm-scroll" style={{ flex: 1, overflowY: "auto", overflowX: "hidden",
        paddingTop: topBar ? 0 : AND_TOP, paddingBottom: nav ? 0 : 16 + AND_BOTTOM }}>
        {children}
        {!nav && <div style={{ height: 8 }} />}
      </div>
      {fab && <div style={{ position: "absolute", right: 16, bottom: (nav ? 96 : 24) + AND_BOTTOM, zIndex: 40 }}>{fab}</div>}
      {nav}
      {sheet}
    </div>
  );
}

/* ---------- small helpers ---------- */
function M3SectionLabel({ children, action, onAction, style }) {
  return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 4px 10px", ...style }}>
      <span style={{ fontSize: 13.5, fontWeight: 700, color: M.onSurf, letterSpacing: "-.01em" }}>{children}</span>
      {action && <button onClick={onAction} style={{ border: "none", background: "transparent", color: M.primary, fontSize: 13, fontWeight: 700, cursor: "pointer", padding: 0 }}>{action}</button>}
    </div>
  );
}

function M3Stepper({ value, onChange, min = 0, max = 99 }) {
  const btn = (ic, fn, dis) => (
    <button onClick={dis ? undefined : (e) => { mRipple(e, "rgba(0,0,0,.08)"); fn(); }}
      style={{ position: "relative", overflow: "hidden", width: 32, height: 32, borderRadius: 999, border: "none",
        background: dis ? M.surf3 : M.primCont, color: dis ? M.onSurfVar : M.onPrimCont, display: "grid", placeItems: "center", cursor: dis ? "default" : "pointer" }}>
      <span style={{ position: "relative", zIndex: 1, display: "flex" }}><Icon name={ic} size={16} stroke={2.6} /></span>
    </button>
  );
  return (
    <div style={{ display: "inline-flex", alignItems: "center", gap: 10 }}>
      {btn("minus", () => onChange(Math.max(min, value - 1)), value <= min)}
      <span className="mono" style={{ minWidth: 16, textAlign: "center", fontWeight: 700, fontSize: 15, color: M.onSurf }}>{value}</span>
      {btn("plus", () => onChange(Math.min(max, value + 1)), value >= max)}
    </div>
  );
}

Object.assign(window, {
  M, mRipple, M3Button, M3FAB, M3IconBtn, M3TopBar, M3NavBar, M3Card, M3Chip, M3Segmented,
  M3Field, M3ListItem, M3Switch, M3Status, M3_STATUS, M3Screen, M3SectionLabel, M3Stepper,
});
