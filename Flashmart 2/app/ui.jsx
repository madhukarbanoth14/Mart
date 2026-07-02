/* ============================================================
   Flashmart — shared UI primitives  → window globals
   ============================================================ */

/* ---------- Icons (simple geometric strokes) ---------- */
const ICONS = {
  home:    "M3 10.5 12 3l9 7.5M5.5 9v11h13V9",
  grid:    "M4 4h7v7H4zM13 4h7v7h-7zM4 13h7v7H4zM13 13h7v7h-7z",
  bag:     "M6 8h12l-1 12H7L6 8zM9 8V6a3 3 0 0 1 6 0v2",
  user:    "M4 20a8 8 0 0 1 16 0M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z",
  cart:    "M3 4h2l2.4 11.2A2 2 0 0 0 9.4 17h7.5a2 2 0 0 0 2-1.6L20.5 8H6M9 21a1 1 0 1 0 0-2 1 1 0 0 0 0 2zm8 0a1 1 0 1 0 0-2 1 1 0 0 0 0 2z",
  plus:    "M12 5v14M5 12h14",
  minus:   "M5 12h14",
  check:   "M4 12.5 9 17.5 20 6.5",
  chevR:   "M9 5l7 7-7 7",
  chevL:   "M15 5l-7 7 7 7",
  chevD:   "M5 9l7 7 7-7",
  truck:   "M3 6h11v9H3zM14 9h4l3 3v3h-7M7.5 19a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3zm10 0a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z",
  doc:     "M7 3h7l4 4v14H7zM14 3v4h4M9.5 12h6M9.5 15.5h6",
  box:     "M12 3 4 7v10l8 4 8-4V7l-8-4zM4 7l8 4 8-4M12 11v10",
  chart:   "M4 20V4M4 20h16M8 20v-6M12 20V9M16 20v-9M20 20v-13",
  search:  "M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14zM20 20l-4-4",
  bell:    "M6 9a6 6 0 0 1 12 0c0 5 2 6 2 6H4s2-1 2-6M10 20a2 2 0 0 0 4 0",
  card:    "M3 6h18v12H3zM3 10h18",
  upi:     "M7 12h10M12 7v10",
  download:"M12 4v11M7 11l5 5 5-5M5 20h14",
  upload:  "M12 20V9M7 13l5-5 5 5M5 4h14",
  users:   "M9 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zM2 20a7 7 0 0 1 14 0M17 4.5a3.5 3.5 0 0 1 0 7M22 20a7 7 0 0 0-5-6.7",
  phone:   "M5 4h3l1.5 5-2 1.5a11 11 0 0 0 5 5l1.5-2 5 1.5v3a2 2 0 0 1-2.2 2A16 16 0 0 1 3 6.2 2 2 0 0 1 5 4z",
  mail:    "M3 6h18v12H3zM3 7l9 6 9-6",
  pin:     "M12 21s7-6.2 7-11a7 7 0 1 0-14 0c0 4.8 7 11 7 11zM12 12.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z",
  clock:   "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM12 7v5l3.5 2",
  bolt:    "M13 2 4 14h6l-1 8 9-12h-6l1-8z",
  settings:"M12 15.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zM4 12h2m12 0h2M12 4v2m0 12v2M6.3 6.3l1.4 1.4m8.6 8.6 1.4 1.4M6.3 17.7l1.4-1.4m8.6-8.6 1.4-1.4",
  logout:  "M15 4h4v16h-4M14 12H4m0 0 4-4m-4 4 4 4",
  filter:  "M4 5h16l-6 8v6l-4-2v-4L4 5z",
  star:    "M12 3l2.6 5.6 6 .8-4.4 4.2 1.1 6L12 16.8 6.7 19.6l1.1-6L3.4 9.4l6-.8L12 3z",
  refresh: "M20 11a8 8 0 1 0-1.5 5M20 6v5h-5",
  wallet:  "M3 7h15a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H4a1 1 0 0 1-1-1V7zM3 7l13-3 1 3M16 13h2",
  receipt: "M6 3h12v18l-2-1.4L14 21l-2-1.4L10 21l-2-1.4L6 21V3zM9 8h6M9 12h6",
  arrowR:  "M5 12h14M13 6l6 6-6 6",
  tag:     "M3 12 12 3h7v7l-9 9-7-7zM16 8a1 1 0 1 0 0-2 1 1 0 0 0 0 2z",
  layers:  "M12 3 3 8l9 5 9-5-9-5zM3 13l9 5 9-5M3 17l9 5 9-5",
};

function Icon({ name, size = 22, stroke = 2, color = "currentColor", fill = "none", style }) {
  const d = ICONS[name] || "";
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill={fill} stroke={color}
         strokeWidth={stroke} strokeLinecap="round" strokeLinejoin="round"
         style={{ flexShrink: 0, display: "block", ...style }}>
      <path d={d} />
    </svg>
  );
}

/* ---------- Brand logo ---------- */
function Logo({ size = 28, color = "var(--brand)", word = true, light = false }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: size * 0.34 }}>
      <div style={{
        width: size, height: size, borderRadius: size * 0.28, position: "relative", overflow: "hidden",
        background: color, display: "grid", placeItems: "center",
        boxShadow: "inset 0 1px 0 rgba(255,255,255,.28)", flexShrink: 0,
      }}>
        {/* gold corner wedge — echoes the logo's two-tone mark */}
        <div style={{ position: "absolute", right: 0, bottom: 0, width: "58%", height: "58%",
          background: "var(--gold)", clipPath: "polygon(100% 0, 100% 100%, 0 100%)" }} />
        <svg width={size * 0.56} height={size * 0.56} viewBox="0 0 24 24" fill="#fff" style={{ position: "relative" }}>
          <path d="M13 2 4 14h6l-1 8 9-12h-6l1-8z" />
        </svg>
      </div>
      {word && (light
        ? <span style={{ fontWeight: 800, fontSize: size * 0.66, letterSpacing: "-.03em", color: "#fff" }}>Flash<span style={{ color: "var(--gold)" }}>Mart</span></span>
        : <span className="fm-word" style={{ fontSize: size * 0.66 }}><span className="fl">Flash</span><span className="mt">Mart</span></span>)}
    </div>
  );
}

/* ---------- Buttons ---------- */
function Button({ children, variant = "primary", size = "md", icon, iconRight, full, onClick, disabled, style }) {
  const sizes = {
    sm: { h: 38, px: 14, fs: 14, r: 11, gap: 7 },
    md: { h: 48, px: 18, fs: 15.5, r: 14, gap: 9 },
    lg: { h: 56, px: 22, fs: 17, r: 16, gap: 10 },
  }[size];
  const variants = {
    primary: { background: "var(--brand)", color: "#fff", border: "1px solid transparent", boxShadow: "var(--sh-sm)" },
    dark:    { background: "var(--ink)", color: "#fff", border: "1px solid transparent", boxShadow: "var(--sh-sm)" },
    pos:     { background: "var(--pos)", color: "#fff", border: "1px solid transparent", boxShadow: "var(--sh-sm)" },
    soft:    { background: "var(--brand-tint)", color: "var(--brand-ink)", border: "1px solid transparent" },
    outline: { background: "var(--surface)", color: "var(--ink)", border: "1px solid var(--line-2)" },
    ghost:   { background: "transparent", color: "var(--ink-2)", border: "1px solid transparent" },
  }[variant];
  return (
    <button className="tap" onClick={disabled ? undefined : onClick} disabled={disabled}
      style={{
        height: sizes.h, padding: `0 ${sizes.px}px`, borderRadius: sizes.r,
        fontSize: sizes.fs, fontWeight: 600, letterSpacing: "-.01em",
        display: "inline-flex", alignItems: "center", justifyContent: "center", gap: sizes.gap,
        width: full ? "100%" : undefined, opacity: disabled ? 0.45 : 1,
        cursor: disabled ? "not-allowed" : "pointer", ...variants, ...style,
      }}>
      {icon && <Icon name={icon} size={sizes.fs + 3} stroke={2.1} />}
      {children}
      {iconRight && <Icon name={iconRight} size={sizes.fs + 3} stroke={2.1} />}
    </button>
  );
}

/* ---------- Badge / status pill ---------- */
const STATUS = {
  placed:    { label: "Placed",        bg: "var(--surface-3)", fg: "var(--ink-2)" },
  pending:   { label: "Pending",       bg: "var(--warn-tint)", fg: "var(--warn)" },
  accepted:  { label: "Accepted",      bg: "var(--brand-tint)", fg: "var(--brand-ink)" },
  out:       { label: "Out for delivery", bg: "var(--warn-tint)", fg: "var(--warn)" },
  delivered: { label: "Delivered",     bg: "var(--pos-tint)", fg: "var(--pos)" },
  paid:      { label: "Paid",          bg: "var(--pos-tint)", fg: "var(--pos)" },
  unpaid:    { label: "Unpaid",        bg: "var(--neg-tint)", fg: "var(--neg)" },
  Active:    { label: "Active",        bg: "var(--pos-tint)", fg: "var(--pos)" },
  Onboarding:{ label: "Onboarding",    bg: "var(--warn-tint)", fg: "var(--warn)" },
  low:       { label: "Low stock",     bg: "var(--neg-tint)", fg: "var(--neg)" },
};
function Badge({ status, label, bg, fg, dot, size = "md" }) {
  const s = STATUS[status] || {};
  const _bg = bg || s.bg || "var(--surface-3)";
  const _fg = fg || s.fg || "var(--ink-2)";
  const _label = label || s.label || status;
  const sm = size === "sm";
  return (
    <span style={{
      display: "inline-flex", alignItems: "center", gap: 6,
      background: _bg, color: _fg, fontWeight: 600,
      fontSize: sm ? 11.5 : 12.5, lineHeight: 1, letterSpacing: "-.01em",
      padding: sm ? "5px 9px" : "6px 11px", borderRadius: 999, whiteSpace: "nowrap",
    }}>
      {dot && <span style={{ width: 6, height: 6, borderRadius: 99, background: _fg }} />}
      {_label}
    </span>
  );
}

/* ---------- Card ---------- */
function Card({ children, pad = 16, style, onClick, hover, className = "" }) {
  return (
    <div onClick={onClick} className={(onClick || hover ? "tap " : "") + className}
      style={{
        background: "var(--surface)", borderRadius: "var(--r-md)", padding: pad,
        border: "1px solid var(--line)", boxShadow: "var(--sh-sm)", ...style,
      }}>
      {children}
    </div>
  );
}

/* ---------- Product thumbnail (tinted placeholder, brand monogram) ---------- */
function ProductThumb({ p, size = 56, radius = 13 }) {
  const initials = p.brand.split(/\s+/).slice(0, 2).map(w => w[0]).join("").toUpperCase();
  return (
    <div style={{
      width: size, height: size, borderRadius: radius, flexShrink: 0,
      position: "relative", overflow: "hidden",
      background: `linear-gradient(140deg, ${p.tint}22, ${p.tint}10)`,
      border: `1px solid ${p.tint}33`, display: "grid", placeItems: "center",
    }}>
      <div style={{
        position: "absolute", inset: 0, opacity: 0.5,
        backgroundImage: `repeating-linear-gradient(135deg, ${p.tint}14 0 6px, transparent 6px 12px)`,
      }} />
      <span style={{
        position: "relative", fontFamily: "var(--mono)", fontWeight: 700,
        fontSize: size * 0.26, color: p.tint, letterSpacing: "-.04em",
      }}>{initials}</span>
    </div>
  );
}

/* ---------- Quantity stepper ---------- */
function Stepper({ value, onChange, min = 0, max = 99 }) {
  const btn = (ic, fn, dis) => (
    <button className="tap" onClick={dis ? undefined : fn} disabled={dis}
      style={{ width: 32, height: 32, borderRadius: 9, border: "none",
        background: dis ? "var(--surface-3)" : "var(--surface)", color: dis ? "var(--ink-4)" : "var(--ink)",
        display: "grid", placeItems: "center", boxShadow: dis ? "none" : "var(--sh-sm)", cursor: dis ? "default" : "pointer" }}>
      <Icon name={ic} size={16} stroke={2.4} />
    </button>
  );
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 10, background: "var(--surface-2)",
      padding: 4, borderRadius: 12, border: "1px solid var(--line)" }}>
      {btn("minus", () => onChange(Math.max(min, value - 1)), value <= min)}
      <span className="mono" style={{ minWidth: 18, textAlign: "center", fontWeight: 700, fontSize: 15 }}>{value}</span>
      {btn("plus", () => onChange(Math.min(max, value + 1)), value >= max)}
    </div>
  );
}

/* ---------- Segmented control ---------- */
function Segmented({ options, value, onChange, full }) {
  return (
    <div style={{ display: "inline-flex", background: "var(--surface-3)", padding: 4,
      borderRadius: 12, gap: 2, width: full ? "100%" : undefined }}>
      {options.map(o => {
        const val = typeof o === "string" ? o : o.value;
        const lab = typeof o === "string" ? o : o.label;
        const on = val === value;
        return (
          <button key={val} className="tap" onClick={() => onChange(val)}
            style={{ flex: full ? 1 : undefined, border: "none", cursor: "pointer",
              padding: "8px 14px", borderRadius: 9, fontSize: 13.5, fontWeight: 600, letterSpacing: "-.01em",
              background: on ? "var(--surface)" : "transparent", color: on ? "var(--ink)" : "var(--ink-3)",
              boxShadow: on ? "var(--sh-sm)" : "none", whiteSpace: "nowrap" }}>
            {lab}
          </button>
        );
      })}
    </div>
  );
}

Object.assign(window, { Icon, ICONS, Logo, Button, Badge, STATUS, Card, ProductThumb, Stepper, Segmented });
