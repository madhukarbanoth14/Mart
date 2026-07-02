/* ============================================================
   FlashMart iOS — auth & onboarding screens
   ============================================================ */

/* ---------------- SPLASH ---------------- */
function SplashScreen() {
  return (
    <div style={{ height: "100%", position: "relative", overflow: "hidden",
      background: "linear-gradient(165deg, var(--brand) 0%, var(--brand-700) 70%, #08311a 100%)" }}>
      {/* ambient glow */}
      <div style={{ position: "absolute", top: "18%", left: "50%", transform: "translateX(-50%)", width: 320, height: 320,
        borderRadius: "50%", background: "radial-gradient(circle, rgba(255,255,255,.22), transparent 65%)" }} />
      <div style={{ position: "absolute", inset: 0, opacity: .5,
        backgroundImage: "repeating-linear-gradient(135deg, rgba(255,255,255,.04) 0 14px, transparent 14px 28px)" }} />
      <div style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 22 }}>
        <div style={{ width: 96, height: 96, borderRadius: 28, background: "#fff", display: "grid", placeItems: "center",
          boxShadow: "0 20px 50px rgba(0,0,0,.3)", animation: "fmFloat 3.5s ease-in-out infinite" }}>
          <svg width="52" height="52" viewBox="0 0 24 24" fill="var(--brand)"><path d="M13 2 4 14h6l-1 8 9-12h-6l1-8z" /></svg>
        </div>
        <div style={{ textAlign: "center" }}>
          <div style={{ fontSize: 38, fontWeight: 800, letterSpacing: "-.03em", color: "#fff" }}>Flash<span style={{ color: "var(--gold)" }}>Mart</span></div>
          <div style={{ fontSize: 14.5, color: "rgba(255,255,255,.7)", marginTop: 4, fontWeight: 500 }}>Distribution, delivered.</div>
        </div>
      </div>
      <div style={{ position: "absolute", bottom: 70, left: 0, right: 0, display: "flex", flexDirection: "column", alignItems: "center", gap: 16 }}>
        <div style={{ width: 26, height: 26, border: "3px solid rgba(255,255,255,.25)", borderTopColor: "#fff", borderRadius: 99, animation: "fmSpin .8s linear infinite" }} />
        <div style={{ fontSize: 11.5, color: "rgba(255,255,255,.5)", fontWeight: 600, letterSpacing: ".06em" }}>v2.4 · Made in India 🇮🇳</div>
      </div>
    </div>
  );
}

/* ---------------- ONBOARDING ---------------- */
const ONB = [
  { kicker: "Order", title: "Your whole shop,\none tap away", body: "Browse 1,200+ FMCG products at dealer prices. Build your cart and reorder favourites in seconds.", art: "catalog" },
  { kicker: "Deliver", title: "Delivered by your\nlocal dealer", body: "Orders route straight to your assigned distributor. Track every delivery live, right to your counter.", art: "track" },
  { kicker: "Bill", title: "GST invoices,\nsorted automatically", body: "Every order generates a compliant tax invoice. Download, share, and file — no paperwork.", art: "invoice" },
];

function OnbArt({ kind }) {
  const F = window.FM;
  if (kind === "catalog") {
    const ps = F.PRODUCTS.slice(0, 4);
    return (
      <div style={{ position: "relative", height: 260, display: "grid", placeItems: "center" }}>
        <div style={{ position: "absolute", width: 200, height: 200, borderRadius: "50%", background: "var(--brand-tint)" }} />
        <div style={{ position: "relative", display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12, width: 260 }}>
          {ps.map((p, i) => (
            <div key={p.id} style={{ background: "var(--surface)", borderRadius: 16, padding: 12, boxShadow: "var(--sh-lg)",
              border: "1px solid var(--line)", animation: `fmFloat ${3 + i * 0.4}s ease-in-out ${i * 0.2}s infinite`,
              transform: i % 2 ? "translateY(14px)" : "none" }}>
              <ProductThumb p={p} size={42} />
              <div style={{ fontSize: 11.5, fontWeight: 700, marginTop: 8, lineHeight: 1.15 }}>{p.name.split(" ").slice(0, 2).join(" ")}</div>
              <div className="mono" style={{ fontSize: 12, fontWeight: 700, color: "var(--brand)", marginTop: 3 }}>{F.inr(p.price)}</div>
            </div>
          ))}
        </div>
      </div>
    );
  }
  if (kind === "track") {
    const steps = [["Placed", "done"], ["Accepted", "done"], ["Out for delivery", "active"], ["Delivered", "todo"]];
    return (
      <div style={{ position: "relative", height: 260, display: "grid", placeItems: "center" }}>
        <div style={{ position: "absolute", width: 200, height: 200, borderRadius: "50%", background: "var(--pos-tint)" }} />
        <div style={{ position: "relative", width: 250, background: "var(--surface)", borderRadius: 18, padding: 18, boxShadow: "var(--sh-lg)", border: "1px solid var(--line)" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 14 }}>
            <div style={{ width: 38, height: 38, borderRadius: 11, background: "var(--brand)", color: "#fff", display: "grid", placeItems: "center" }}><Icon name="truck" size={20} /></div>
            <div><div style={{ fontSize: 13, fontWeight: 700 }}>Today, by 6 PM</div><div style={{ fontSize: 11, color: "var(--ink-3)" }}>Sharma Distributors</div></div>
          </div>
          {steps.map(([l, st], i) => (
            <div key={l} style={{ display: "flex", alignItems: "center", gap: 10, padding: "5px 0" }}>
              <div style={{ width: 18, height: 18, borderRadius: 99, flexShrink: 0, display: "grid", placeItems: "center",
                background: st === "done" ? "var(--pos)" : st === "active" ? "var(--brand)" : "var(--surface-3)",
                color: "#fff", boxShadow: st === "active" ? "0 0 0 4px var(--brand-tint)" : "none" }}>
                {st === "done" && <Icon name="check" size={11} stroke={3} />}
              </div>
              <span style={{ fontSize: 12.5, fontWeight: st === "todo" ? 500 : 600, color: st === "todo" ? "var(--ink-4)" : "var(--ink)" }}>{l}</span>
            </div>
          ))}
        </div>
      </div>
    );
  }
  // invoice
  return (
    <div style={{ position: "relative", height: 260, display: "grid", placeItems: "center" }}>
      <div style={{ position: "absolute", width: 200, height: 200, borderRadius: "50%", background: "var(--warn-tint)" }} />
      <div style={{ position: "relative", width: 210, background: "var(--surface)", borderRadius: 16, boxShadow: "var(--sh-lg)", border: "1px solid var(--line)", overflow: "hidden", transform: "rotate(-3deg)" }}>
        <div style={{ background: "var(--ink-surface)", padding: 14, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <span style={{ color: "#fff", fontWeight: 700, fontSize: 13 }}>Tax Invoice</span>
          <span className="mono" style={{ color: "rgba(255,255,255,.6)", fontSize: 11 }}>INV-1245</span>
        </div>
        <div style={{ padding: 14 }}>
          {[["Subtotal", "₹4,180"], ["GST", "₹512"]].map(([l, v]) => (
            <div key={l} style={{ display: "flex", justifyContent: "space-between", padding: "4px 0", fontSize: 12, color: "var(--ink-3)" }}><span>{l}</span><span className="mono">{v}</span></div>
          ))}
          <div style={{ borderTop: "1px dashed var(--line-2)", margin: "8px 0" }} />
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}><span style={{ fontSize: 13, fontWeight: 700 }}>Total</span><span className="mono" style={{ fontSize: 16, fontWeight: 700, color: "var(--pos)" }}>₹4,692</span></div>
        </div>
      </div>
      <div style={{ position: "absolute", bottom: 24, right: 30, width: 44, height: 44, borderRadius: 13, background: "var(--pos)", color: "#fff", display: "grid", placeItems: "center", boxShadow: "var(--sh-lg)", transform: "rotate(6deg)" }}><Icon name="download" size={22} /></div>
    </div>
  );
}

function OnboardingScreen({ index = 0, onNext, onSkip }) {
  const s = ONB[index];
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--surface)", paddingTop: IOS_TOP }}>
      <div style={{ display: "flex", justifyContent: "flex-end", padding: "0 20px" }}>
        <button className="tap" onClick={onSkip} style={{ border: "none", background: "transparent", color: "var(--ink-3)", fontSize: 14, fontWeight: 600, cursor: "pointer", padding: "6px 4px" }}>Skip</button>
      </div>
      <div style={{ flex: 1, display: "flex", flexDirection: "column", justifyContent: "center", padding: "0 28px" }}>
        <OnbArt kind={s.art} />
        <div style={{ marginTop: 30 }}>
          <div style={{ fontSize: 13, fontWeight: 700, color: "var(--brand)", letterSpacing: ".06em", textTransform: "uppercase" }}>{s.kicker}</div>
          <div style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-.03em", lineHeight: 1.12, marginTop: 10, whiteSpace: "pre-line" }}>{s.title}</div>
          <div style={{ fontSize: 15.5, color: "var(--ink-3)", lineHeight: 1.55, marginTop: 14 }}>{s.body}</div>
        </div>
      </div>
      <div style={{ padding: "0 28px 34px", display: "flex", flexDirection: "column", gap: 22 }}>
        <PageDots n={3} active={index} />
        <Button variant="primary" size="lg" full iconRight={index === 2 ? undefined : "arrowR"} onClick={onNext}>
          {index === 2 ? "Get started" : "Continue"}
        </Button>
      </div>
    </div>
  );
}

/* ---------------- LOGIN (phone) ---------------- */
function LoginScreen({ phone = "", onPhone, onContinue }) {
  const valid = phone.replace(/\D/g, "").length === 10;
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--surface)", paddingTop: IOS_TOP + 8 }}>
      <div style={{ flex: 1, padding: "0 26px" }}>
        <div style={{ width: 60, height: 60, borderRadius: 18, background: "var(--brand)", display: "grid", placeItems: "center", boxShadow: "var(--sh-md)" }}>
          <svg width="32" height="32" viewBox="0 0 24 24" fill="#fff"><path d="M13 2 4 14h6l-1 8 9-12h-6l1-8z" /></svg>
        </div>
        <div style={{ fontSize: 28, fontWeight: 700, letterSpacing: "-.03em", marginTop: 26 }}>Enter your number</div>
        <div style={{ fontSize: 15, color: "var(--ink-3)", marginTop: 8, lineHeight: 1.5 }}>We'll send a 6-digit code to verify it's you. No password needed.</div>
        <div style={{ marginTop: 30 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10, height: 58, padding: "0 16px", background: "var(--surface)", borderRadius: 16, border: "1.5px solid var(--brand)", boxShadow: "0 0 0 4px var(--brand-tint)" }}>
            <span style={{ fontSize: 20 }}>🇮🇳</span>
            <span className="mono" style={{ fontSize: 18, fontWeight: 700, color: "var(--ink-2)" }}>+91</span>
            <div style={{ width: 1, height: 26, background: "var(--line-2)" }} />
            <input value={phone} onChange={e => onPhone && onPhone(e.target.value)} placeholder="98XXX XXXXX" inputMode="numeric"
              style={{ flex: 1, border: "none", outline: "none", background: "transparent", fontFamily: "var(--mono)", fontSize: 18, fontWeight: 600, letterSpacing: ".02em" }} />
            {valid && <Icon name="check" size={20} color="var(--pos)" stroke={2.6} />}
          </div>
          <div style={{ fontSize: 12.5, color: "var(--ink-4)", marginTop: 12, display: "flex", alignItems: "center", gap: 7 }}>
            <Icon name="bolt" size={14} color="var(--brand)" /> Your number is never shared with other shops.
          </div>
        </div>
      </div>
      <div style={{ padding: "0 26px 30px" }}>
        <Button variant="primary" size="lg" full disabled={!valid} iconRight="arrowR" onClick={onContinue}>Send code</Button>
        <div style={{ fontSize: 11.5, color: "var(--ink-4)", textAlign: "center", marginTop: 16, lineHeight: 1.5 }}>
          By continuing you agree to FlashMart's<br /><span style={{ color: "var(--brand)", fontWeight: 600 }}>Terms of Service</span> & <span style={{ color: "var(--brand)", fontWeight: 600 }}>Privacy Policy</span>
        </div>
      </div>
    </div>
  );
}

/* ---------------- OTP ---------------- */
function OtpScreen({ code = "", phone = "98110 24567", onBack, onVerify, resendIn = 24 }) {
  const filled = code.length === 6;
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--surface)", paddingTop: IOS_TOP }}>
      <div style={{ padding: "0 20px" }}>
        <div style={{ width: 40, height: 40, borderRadius: 12, border: "1px solid var(--line)", background: "var(--surface)", display: "grid", placeItems: "center", boxShadow: "var(--sh-sm)" }} onClick={onBack}>
          <Icon name="chevL" size={20} stroke={2.2} />
        </div>
      </div>
      <div style={{ flex: 1, padding: "0 26px", marginTop: 22 }}>
        <div style={{ fontSize: 28, fontWeight: 700, letterSpacing: "-.03em" }}>Verify your number</div>
        <div style={{ fontSize: 15, color: "var(--ink-3)", marginTop: 8, lineHeight: 1.5 }}>Enter the code sent to <b style={{ color: "var(--ink)" }} className="mono">+91 {phone}</b></div>
        <div style={{ marginTop: 34 }}>
          <OtpBoxes code={code} active={code.length} />
        </div>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 7, marginTop: 26, fontSize: 13.5, color: "var(--ink-4)" }}>
          {resendIn > 0 ? <><Icon name="clock" size={15} /> Resend code in <span className="mono" style={{ fontWeight: 700, color: "var(--ink-2)" }}>0:{String(resendIn).padStart(2, "0")}</span></>
            : <span style={{ color: "var(--brand)", fontWeight: 700 }}>Resend code</span>}
        </div>
      </div>
      <div style={{ padding: "0 26px 30px" }}>
        <Button variant="primary" size="lg" full disabled={!filled} icon={filled ? "check" : undefined} onClick={onVerify}>{filled ? "Verify & continue" : "Enter 6-digit code"}</Button>
      </div>
    </div>
  );
}

/* ---------------- PERMISSIONS ---------------- */
function LocationPermScreen({ onAllow, onSkip }) {
  return (
    <Screen padTop={false}>
      <div style={{ height: 874, display: "flex", flexDirection: "column" }}>
        {/* faux map */}
        <div style={{ height: 360, position: "relative", overflow: "hidden", background: "linear-gradient(160deg, #e8edf4, #dde4ee)" }}>
          <div style={{ position: "absolute", inset: 0, opacity: .6,
            backgroundImage: "linear-gradient(var(--line-2) 1px, transparent 1px), linear-gradient(90deg, var(--line-2) 1px, transparent 1px)", backgroundSize: "40px 40px" }} />
          {/* roads */}
          <div style={{ position: "absolute", top: "55%", left: 0, right: 0, height: 14, background: "#fff", opacity: .8 }} />
          <div style={{ position: "absolute", top: 0, bottom: 0, left: "42%", width: 12, background: "#fff", opacity: .8 }} />
          {/* dealer pins */}
          {[[30, 30, "#0e9e6e"], [68, 44, "#0e9e6e"], [54, 70, "#c97a16"]].map(([l, t, c], i) => (
            <div key={i} style={{ position: "absolute", left: l + "%", top: t + "%", transform: "translate(-50%,-100%)" }}>
              <div style={{ width: 30, height: 30, borderRadius: "50% 50% 50% 0", background: c, transform: "rotate(-45deg)", boxShadow: "var(--sh-md)", display: "grid", placeItems: "center" }}>
                <div style={{ transform: "rotate(45deg)", color: "#fff" }}><Icon name="truck" size={14} /></div>
              </div>
            </div>
          ))}
          {/* you */}
          <div style={{ position: "absolute", left: "50%", top: IOS_TOP + 150, transform: "translate(-50%,-50%)" }}>
            <div style={{ width: 22, height: 22, borderRadius: 99, background: "var(--brand)", border: "3px solid #fff", boxShadow: "0 0 0 8px rgba(47,72,212,.18)" }} />
          </div>
        </div>
        <div style={{ flex: 1, padding: "26px 26px 30px", display: "flex", flexDirection: "column", marginTop: -28, background: "var(--surface)", borderRadius: "28px 28px 0 0", position: "relative" }}>
          <div style={{ width: 96, height: 96, borderRadius: 28, background: "var(--brand-tint)", color: "var(--brand)", display: "grid", placeItems: "center", marginTop: -76, boxShadow: "var(--sh-lg)", border: "5px solid var(--surface)" }}><Icon name="pin" size={44} stroke={1.9} /></div>
          <div style={{ fontSize: 25, fontWeight: 700, letterSpacing: "-.025em", marginTop: 22 }}>Find dealers near you</div>
          <div style={{ fontSize: 15, color: "var(--ink-3)", marginTop: 10, lineHeight: 1.5 }}>Share your location so we can match you with the closest distributor for faster delivery.</div>
          <div style={{ flex: 1 }} />
          <div style={{ display: "flex", flexDirection: "column", gap: 11 }}>
            <Button variant="primary" size="lg" full icon="pin" onClick={onAllow}>Allow location access</Button>
            <Button variant="ghost" full onClick={onSkip}>Enter address manually</Button>
          </div>
        </div>
      </div>
    </Screen>
  );
}

function NotifPermScreen({ onAllow, onSkip }) {
  return (
    <Screen padTop>
      <PermissionScaffold icon="bell" title="Stay updated on every order" body="Get notified the moment your dealer accepts an order, dispatches it, and delivers — plus restock reminders."
        primary="Turn on notifications" secondary="Maybe later" onPrimary={onAllow} onSecondary={onSkip}>
        {/* native-style alert preview */}
        <div style={{ marginTop: 30, width: 300, background: "rgba(247,247,250,.9)", backdropFilter: "blur(8px)", borderRadius: 20, overflow: "hidden", boxShadow: "var(--sh-xl)", border: "1px solid var(--line)" }}>
          <div style={{ padding: "18px 20px 16px", textAlign: "center", borderBottom: "1px solid var(--line)" }}>
            <div style={{ fontSize: 15, fontWeight: 700 }}>“FlashMart” would like to send you notifications</div>
            <div style={{ fontSize: 12.5, color: "var(--ink-3)", marginTop: 5 }}>Alerts may include order updates and reminders.</div>
          </div>
          <div style={{ display: "flex" }}>
            <div style={{ flex: 1, padding: "13px 0", textAlign: "center", fontSize: 15, color: "var(--ink-3)", fontWeight: 500, borderRight: "1px solid var(--line)" }}>Don't Allow</div>
            <div style={{ flex: 1, padding: "13px 0", textAlign: "center", fontSize: 15, color: "var(--brand)", fontWeight: 700 }}>Allow</div>
          </div>
        </div>
      </PermissionScaffold>
    </Screen>
  );
}

Object.assign(window, { SplashScreen, OnboardingScreen, OnbArt, ONB, LoginScreen, OtpScreen, LocationPermScreen, NotifPermScreen });
