/* ============================================================
   Flashmart iOS — gallery kit (screen scaffolding + bits)
   Reuses design system primitives from ui.jsx / ui2.jsx.
   ============================================================ */

/* status-bar clearance inside IOSDevice (island ≈ 58px) */
const IOS_TOP = 58;

/* Screen — fills the 402×874 device content area */
function Screen({ children, bg = "var(--bg)", nav, dark, padTop = true, pad = 0, style }) {
  return (
    <div style={{ height: "100%", position: "relative", overflow: "hidden", background: bg,
      display: "flex", flexDirection: "column", ...style }}>
      <div className="fm-scroll" style={{ flex: 1, overflowY: "auto", overflowX: "hidden",
        paddingTop: padTop ? IOS_TOP : 0, paddingBottom: nav ? 104 : 30, paddingLeft: pad, paddingRight: pad }}>
        {children}
      </div>
      {nav}
    </div>
  );
}

/* Large-title iOS header */
function TopBar({ title, subtitle, kicker, right, onBack, accent }) {
  return (
    <div style={{ padding: "6px 20px 14px", display: "flex", alignItems: "flex-start", justifyContent: "space-between", gap: 12 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12, minWidth: 0, flex: 1 }}>
        {onBack && (
          <div style={{ width: 40, height: 40, borderRadius: 12, flexShrink: 0, border: "1px solid var(--line)",
            background: "var(--surface)", display: "grid", placeItems: "center", boxShadow: "var(--sh-sm)", marginTop: 2 }}>
            <Icon name="chevL" size={20} stroke={2.2} />
          </div>
        )}
        <div style={{ minWidth: 0 }}>
          {kicker && <div style={{ fontSize: 12.5, fontWeight: 600, color: accent || "var(--brand)", marginBottom: 3 }}>{kicker}</div>}
          <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: "-.03em", lineHeight: 1.05, color: "var(--ink)" }}>{title}</div>
          {subtitle && <div style={{ fontSize: 13.5, color: "var(--ink-3)", marginTop: 4 }}>{subtitle}</div>}
        </div>
      </div>
      {right && <div style={{ display: "flex", gap: 8, flexShrink: 0 }}>{right}</div>}
    </div>
  );
}

/* page dots for carousels */
function PageDots({ n, active, light }) {
  return (
    <div style={{ display: "flex", gap: 7, justifyContent: "center" }}>
      {Array.from({ length: n }).map((_, i) => {
        const on = i === active;
        return <span key={i} style={{ height: 7, width: on ? 22 : 7, borderRadius: 99,
          background: on ? (light ? "#fff" : "var(--brand)") : (light ? "rgba(255,255,255,.35)" : "var(--line-2)"),
          transition: "width .25s, background .25s" }} />;
      })}
    </div>
  );
}

/* OTP boxes */
function OtpBoxes({ code = "", active = 0 }) {
  return (
    <div style={{ display: "flex", gap: 10, justifyContent: "center" }}>
      {Array.from({ length: 6 }).map((_, i) => {
        const ch = code[i]; const isActive = i === active && !ch;
        return (
          <div key={i} style={{ width: 46, height: 56, borderRadius: 14, display: "grid", placeItems: "center",
            background: "var(--surface)", border: `1.5px solid ${ch ? "var(--brand)" : isActive ? "var(--brand)" : "var(--line-2)"}`,
            boxShadow: isActive ? "0 0 0 4px var(--brand-tint)" : "var(--sh-sm)" }}>
            <span className="mono" style={{ fontSize: 24, fontWeight: 700, color: "var(--ink)" }}>{ch || ""}</span>
            {isActive && <span style={{ position: "absolute", width: 2, height: 24, background: "var(--brand)", animation: "fmBlink 1s steps(1) infinite" }} />}
          </div>
        );
      })}
    </div>
  );
}

/* permission scaffold — icon halo, title, body, native-style alert preview, actions */
function PermissionScaffold({ icon, halo = "var(--brand)", haloTint = "var(--brand-tint)", title, body, children, primary, secondary, onPrimary, onSecondary }) {
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", padding: "0 24px 30px", textAlign: "center" }}>
      <div style={{ flex: 1, display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center" }}>
        <div style={{ width: 96, height: 96, borderRadius: 28, background: haloTint, color: halo, display: "grid", placeItems: "center",
          boxShadow: `0 0 0 12px ${haloTint}66`, marginBottom: 26 }}>
          <Icon name={icon} size={44} stroke={1.9} />
        </div>
        <div style={{ fontSize: 25, fontWeight: 700, letterSpacing: "-.025em", maxWidth: 280 }}>{title}</div>
        <div style={{ fontSize: 15, color: "var(--ink-3)", marginTop: 10, lineHeight: 1.5, maxWidth: 290 }}>{body}</div>
        {children}
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 11 }}>
        <Button variant="primary" size="lg" full onClick={onPrimary}>{primary}</Button>
        <Button variant="ghost" full onClick={onSecondary}>{secondary}</Button>
      </div>
    </div>
  );
}

/* skeleton shimmer block */
function Skel({ w = "100%", h = 14, r = 7, style }) {
  return <div style={{ width: w, height: h, borderRadius: r, background: "linear-gradient(100deg, var(--surface-3) 30%, var(--surface-2) 50%, var(--surface-3) 70%)",
    backgroundSize: "200% 100%", animation: "fmShimmer 1.4s linear infinite", ...style }} />;
}

/* small round glyph button used in headers */
function GlyphBtn({ name, badge, accent }) {
  return (
    <div style={{ position: "relative", width: 42, height: 42, borderRadius: 13, border: "1px solid var(--line)",
      background: "var(--surface)", color: accent || "var(--ink-2)", display: "grid", placeItems: "center", boxShadow: "var(--sh-sm)" }}>
      <Icon name={name} size={20} stroke={2} />
      {badge ? <span className="mono" style={{ position: "absolute", top: -4, right: -4, minWidth: 18, height: 18, padding: "0 4px",
        borderRadius: 99, background: "var(--neg)", color: "#fff", fontSize: 10.5, fontWeight: 700, display: "grid", placeItems: "center", border: "2px solid var(--surface)" }}>{badge}</span> : null}
    </div>
  );
}

/* inject keyframes for gallery-only animations */
const _galKf = document.createElement("style");
_galKf.textContent = `
@keyframes fmBlink { 50% { opacity: 0; } }
@keyframes fmShimmer { to { background-position: -200% 0; } }
@keyframes fmFloat { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-7px); } }
`;
document.head.appendChild(_galKf);

Object.assign(window, { Screen, TopBar, PageDots, OtpBoxes, PermissionScaffold, Skel, GlyphBtn, IOS_TOP });
