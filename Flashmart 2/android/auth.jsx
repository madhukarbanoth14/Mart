/* ============================================================
   FlashMart Android — Auth & onboarding (Material 3)
   Splash → Welcome → User type → Phone → OTP → Registration wizard
   ============================================================ */
const FA = window.FM;

/* brand mark — green tile, white bolt, gold corner wedge (echoes the logo) */
function AND_Mark({ size = 92, radius }) {
  const r = radius != null ? radius : size * 0.28;
  return (
    <div style={{ width: size, height: size, borderRadius: r, position: "relative", overflow: "hidden",
      background: "linear-gradient(150deg, #1aa657, var(--brand))", display: "grid", placeItems: "center",
      boxShadow: "0 18px 44px rgba(11,99,50,.34), inset 0 1px 0 rgba(255,255,255,.3)" }}>
      <div style={{ position: "absolute", right: 0, bottom: 0, width: "56%", height: "56%",
        background: "var(--gold)", clipPath: "polygon(100% 0, 100% 100%, 0 100%)" }} />
      <svg width={size * 0.52} height={size * 0.52} viewBox="0 0 24 24" fill="#fff" style={{ position: "relative" }}><path d="M13 2 4 14h6l-1 8 9-12h-6l1-8z" /></svg>
    </div>
  );
}

/* ---------------- SPLASH ---------------- */
function AND_Splash() {
  return (
    <div style={{ height: "100%", position: "relative", overflow: "hidden",
      background: "linear-gradient(165deg, #1aa657 0%, var(--brand) 55%, var(--brand-700) 100%)" }}>
      <div style={{ position: "absolute", top: "16%", left: "50%", transform: "translateX(-50%)", width: 320, height: 320,
        borderRadius: "50%", background: "radial-gradient(circle, rgba(255,255,255,.18), transparent 65%)" }} />
      <div style={{ position: "absolute", inset: 0, opacity: .5,
        backgroundImage: "repeating-linear-gradient(135deg, rgba(255,255,255,.04) 0 14px, transparent 14px 28px)" }} />
      <div style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 24 }}>
        <div style={{ animation: "fmFloat 3.5s ease-in-out infinite" }}><AND_Mark size={104} /></div>
        <div style={{ textAlign: "center" }}>
          <div style={{ fontSize: 40, fontWeight: 800, letterSpacing: "-.03em", color: "#fff" }}>Flash<span style={{ color: "var(--gold)" }}>Mart</span></div>
          <div style={{ fontSize: 14.5, color: "rgba(255,255,255,.82)", marginTop: 6, fontWeight: 600, letterSpacing: ".02em" }}>Fast Delivery · Trusted Quality</div>
        </div>
      </div>
      <div style={{ position: "absolute", bottom: 64, left: 0, right: 0, display: "flex", flexDirection: "column", alignItems: "center", gap: 18 }}>
        {/* M3 circular indeterminate */}
        <div style={{ width: 28, height: 28, border: "3px solid rgba(255,255,255,.28)", borderTopColor: "#fff", borderRadius: 99, animation: "fmSpin .8s linear infinite" }} />
        <div style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 11.5, color: "rgba(255,255,255,.65)", fontWeight: 700, letterSpacing: ".06em" }}>
          <span>QUALITY</span><span style={{ opacity: .5 }}>·</span><span>SPEED</span><span style={{ opacity: .5 }}>·</span><span>TRUST</span>
        </div>
      </div>
    </div>
  );
}

/* ---------------- WELCOME ---------------- */
function AND_Welcome({ onLogin, onSignup }) {
  const ps = FA.PRODUCTS.slice(0, 3);
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: M.surface, paddingTop: AND_TOP }}>
      {/* hero */}
      <div style={{ flex: 1, position: "relative", overflow: "hidden", display: "grid", placeItems: "center", padding: "10px 24px 0" }}>
        <div style={{ position: "absolute", width: 280, height: 280, borderRadius: "50%", background: "var(--brand-tint)", top: 30 }} />
        <div style={{ position: "relative", display: "flex", flexDirection: "column", alignItems: "center", gap: 16 }}>
          <AND_Mark size={84} />
          <div style={{ display: "flex", gap: 12 }}>
            {ps.map((p, i) => (
              <div key={p.id} style={{ background: M.surf0, borderRadius: 18, padding: 12, boxShadow: "var(--m3-e2)",
                animation: `fmFloat ${3 + i * 0.4}s ease-in-out ${i * 0.2}s infinite`, transform: i === 1 ? "translateY(-16px)" : "none" }}>
                <ProductThumb p={p} size={48} />
              </div>
            ))}
          </div>
        </div>
      </div>
      {/* copy + CTAs */}
      <div style={{ padding: "10px 28px 30px" }}>
        <div style={{ fontSize: 32, fontWeight: 800, letterSpacing: "-.03em", lineHeight: 1.08, color: M.onSurf }}>Your shop's<br />supply, simplified.</div>
        <div style={{ fontSize: 15, color: M.onSurfVar, marginTop: 12, lineHeight: 1.5 }}>Order FMCG stock at dealer prices, track every delivery, and get GST invoices — all in one app.</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 11, marginTop: 24 }}>
          <M3Button variant="filled" size="lg" full onClick={onSignup} iconRight="arrowR">Create account</M3Button>
          <M3Button variant="tonal" size="lg" full onClick={onLogin}>I already have an account</M3Button>
        </div>
        <div style={{ display: "flex", justifyContent: "center", gap: 6, marginTop: 18, fontSize: 12, color: M.onSurfVar }}>
          <span style={{ fontWeight: 600 }}>Privacy Policy</span><span style={{ opacity: .5 }}>·</span><span style={{ fontWeight: 600 }}>Terms &amp; Conditions</span>
        </div>
      </div>
    </div>
  );
}

/* ---------------- USER TYPE ---------------- */
const AND_ROLES = [
  { id: "shopkeeper", icon: "bag", title: "Shopkeeper", body: "Purchase products for your shop at dealer prices.", bg: "var(--brand-tint)", fg: "var(--brand)" },
  { id: "dealer", icon: "box", title: "Dealer", body: "Manage inventory and serve retailers in your area.", bg: "var(--blue-tint)", fg: "var(--blue)" },
  { id: "employee", icon: "users", title: "Employee", body: "Onboard partners and manage relationships.", bg: "var(--gold-tint)", fg: "var(--gold-ink)" },
];
function AND_UserType({ selected = "shopkeeper", onSelect, onContinue, onBack }) {
  return (
    <M3Screen topBar={<M3TopBar variant="large" title="Choose your account" subtitle="You can't change this later" onBack={onBack} />}>
      <div style={{ padding: "4px 16px 0", display: "flex", flexDirection: "column", gap: 12 }}>
        {AND_ROLES.map((r) => {
          const on = r.id === selected;
          return (
            <M3Card key={r.id} variant={on ? "filled" : "outlined"} pad={16} onClick={() => onSelect && onSelect(r.id)}
              style={{ display: "flex", alignItems: "center", gap: 15, border: on ? `2px solid ${M.primary}` : `1px solid ${M.outlineVar}`,
                background: on ? M.secCont : M.surface }}>
              <div style={{ width: 52, height: 52, borderRadius: 14, background: r.bg, color: r.fg, display: "grid", placeItems: "center", flexShrink: 0 }}><Icon name={r.icon} size={26} /></div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 17, fontWeight: 700, color: M.onSurf }}>{r.title}</div>
                <div style={{ fontSize: 13, color: M.onSurfVar, marginTop: 2, lineHeight: 1.4 }}>{r.body}</div>
              </div>
              <div style={{ width: 22, height: 22, borderRadius: 99, flexShrink: 0, border: on ? "none" : `2px solid ${M.outline}`,
                background: on ? M.primary : "transparent", display: "grid", placeItems: "center" }}>
                {on && <Icon name="check" size={14} color="#fff" stroke={3} />}
              </div>
            </M3Card>
          );
        })}
      </div>
      <div style={{ padding: "22px 24px 8px" }}>
        <M3Button variant="filled" size="lg" full iconRight="arrowR" onClick={onContinue}>Continue</M3Button>
      </div>
    </M3Screen>
  );
}

/* ---------------- PHONE LOGIN ---------------- */
function AND_Login({ phone = "", valid, onBack, onContinue }) {
  const ok = valid != null ? valid : phone.replace(/\D/g, "").length === 10;
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: M.surface }}>
      <M3TopBar onBack={onBack} title="" />
      <div style={{ flex: 1, padding: "8px 26px 0", overflow: "auto" }}>
        <AND_Mark size={56} radius={16} />
        <div style={{ fontSize: 28, fontWeight: 800, letterSpacing: "-.03em", marginTop: 24, color: M.onSurf }}>Enter your number</div>
        <div style={{ fontSize: 15, color: M.onSurfVar, marginTop: 8, lineHeight: 1.5 }}>We'll send a 6-digit code to verify it's you. No password needed.</div>
        <div style={{ marginTop: 28 }}>
          <M3Field variant="outlined" label="Mobile number" icon="phone" prefix="+91" value={phone} placeholder="98XXX XXXXX" focused
            trailing={ok ? <Icon name="check" size={20} color="var(--pos)" stroke={2.6} /> : null} />
          <div style={{ fontSize: 12.5, color: M.onSurfVar, marginTop: 12, display: "flex", alignItems: "center", gap: 7 }}>
            <Icon name="bolt" size={14} color="var(--brand)" /> Your number is never shared with other shops.
          </div>
        </div>
      </div>
      <div style={{ padding: "0 26px 30px" }}>
        <M3Button variant="filled" size="lg" full disabled={!ok} iconRight="arrowR" onClick={onContinue}>Send OTP</M3Button>
        <div style={{ fontSize: 11.5, color: M.onSurfVar, textAlign: "center", marginTop: 16, lineHeight: 1.5 }}>
          By continuing you agree to FlashMart's <span style={{ color: "var(--brand)", fontWeight: 700 }}>Terms</span> &amp; <span style={{ color: "var(--brand)", fontWeight: 700 }}>Privacy Policy</span>
        </div>
      </div>
    </div>
  );
}

/* ---------------- OTP ---------------- */
function AND_OtpBoxes({ code = "", active = 0 }) {
  return (
    <div style={{ display: "flex", gap: 9, justifyContent: "center" }}>
      {Array.from({ length: 6 }).map((_, i) => {
        const ch = code[i]; const isActive = i === active && !ch;
        return (
          <div key={i} style={{ width: 46, height: 56, borderRadius: 12, display: "grid", placeItems: "center",
            background: M.surf4, border: `${ch || isActive ? 2 : 1}px solid ${ch ? "var(--brand)" : isActive ? "var(--brand)" : M.outline}` }}>
            <span className="mono" style={{ fontSize: 24, fontWeight: 700, color: M.onSurf }}>{ch || ""}</span>
          </div>
        );
      })}
    </div>
  );
}
function AND_Otp({ code = "", phone = "98110 24567", resendIn = 18, onBack, onVerify }) {
  const filled = code.length === 6;
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: M.surface }}>
      <M3TopBar onBack={onBack} title="" />
      <div style={{ flex: 1, padding: "8px 26px 0", overflow: "auto" }}>
        <div style={{ fontSize: 28, fontWeight: 800, letterSpacing: "-.03em", color: M.onSurf }}>Verify your number</div>
        <div style={{ fontSize: 15, color: M.onSurfVar, marginTop: 8, lineHeight: 1.5 }}>Enter the code sent to <b className="mono" style={{ color: M.onSurf }}>+91 {phone}</b></div>
        <div style={{ marginTop: 34 }}><AND_OtpBoxes code={code} active={code.length} /></div>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 7, marginTop: 26, fontSize: 13.5, color: M.onSurfVar }}>
          {resendIn > 0 ? <><Icon name="clock" size={15} /> Resend OTP in <span className="mono" style={{ fontWeight: 700, color: M.onSurf }}>0:{String(resendIn).padStart(2, "0")}</span></>
            : <span style={{ color: "var(--brand)", fontWeight: 700 }}>Resend OTP</span>}
        </div>
      </div>
      <div style={{ padding: "0 26px 30px" }}>
        <M3Button variant="filled" size="lg" full disabled={!filled} icon={filled ? "check" : undefined} onClick={onVerify}>{filled ? "Verify & continue" : "Enter 6-digit code"}</M3Button>
      </div>
    </div>
  );
}

/* ---------------- REGISTRATION WIZARD ---------------- */
const AND_STEPS = ["Business", "Area", "Address", "Documents", "Referral"];
function AND_Progress({ step }) {
  return (
    <div style={{ padding: "0 16px 4px" }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 10 }}>
        <span style={{ fontSize: 13, fontWeight: 700, color: M.primary }}>Step {step + 1} of {AND_STEPS.length}</span>
        <span style={{ fontSize: 13, fontWeight: 600, color: M.onSurfVar }}>{AND_STEPS[step]}</span>
      </div>
      <div style={{ display: "flex", gap: 5 }}>
        {AND_STEPS.map((_, i) => (
          <div key={i} style={{ flex: 1, height: 5, borderRadius: 99, background: i <= step ? M.primary : M.surf4, transition: "background .3s" }} />
        ))}
      </div>
    </div>
  );
}

const AND_REG = [
  { fields: [["Owner name", "user", "Madhukar Joshi"], ["Shop / business name", "box", "Madhukar General Store"], ["Mobile number", "phone", "98110 24567"], ["Email (optional)", "mail", ""]] },
  { selects: [["State", "Maharashtra"], ["District", "Mumbai Suburban"], ["Area / route", "Andheri East"]] },
  { fields: [["Shop address", "pin", "Shop 14, Sai Plaza, M.G. Road"], ["Landmark", "pin", "Opp. Andheri Metro"], ["Pincode", "tag", "400069"]], location: true },
  { docs: true },
  { referral: true },
];

function AND_Register({ step = 0, onBack, onNext }) {
  const data = AND_REG[step];
  const last = step === AND_STEPS.length - 1;
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: M.surface }}>
      <M3TopBar onBack={onBack} title="Create your account" />
      <AND_Progress step={step} />
      <div className="fm-scroll" style={{ flex: 1, overflow: "auto", padding: "14px 16px 0", display: "flex", flexDirection: "column", gap: 14 }}>
        {/* Step 1 — business details */}
        {data.fields && data.fields.map(([label, icon, val], i) => (
          <M3Field key={label} variant="outlined" label={label} icon={icon} value={val} placeholder={"Enter " + label.toLowerCase()}
            prefix={label.startsWith("Mobile") ? "+91" : undefined} focused={i === 0 && !val ? false : false} />
        ))}
        {/* Step 2 — area selects */}
        {data.selects && data.selects.map(([label, val]) => (
          <M3Field key={label} variant="outlined" label={label} value={val} trailing={<Icon name="chevD" size={18} color={M.onSurfVar} />} />
        ))}
        {/* Step 3 — address: map + use current location */}
        {data.location && (
          <M3Card variant="outlined" pad={0} style={{ overflow: "hidden" }}>
            <div style={{ height: 130, position: "relative", background: "linear-gradient(160deg, #e8efe9, #dde7e0)" }}>
              <div style={{ position: "absolute", inset: 0, opacity: .6, backgroundImage: "linear-gradient(var(--m3-outline-var) 1px, transparent 1px), linear-gradient(90deg, var(--m3-outline-var) 1px, transparent 1px)", backgroundSize: "34px 34px" }} />
              <div style={{ position: "absolute", left: "50%", top: "52%", transform: "translate(-50%,-100%)" }}>
                <div style={{ width: 30, height: 30, borderRadius: "50% 50% 50% 0", background: M.primary, transform: "rotate(-45deg)", boxShadow: "var(--m3-e2)", display: "grid", placeItems: "center" }}>
                  <div style={{ transform: "rotate(45deg)", color: "#fff" }}><Icon name="pin" size={15} /></div>
                </div>
              </div>
            </div>
            <div style={{ padding: 12 }}><M3Button variant="tonal" size="sm" full icon="pin">Use current location</M3Button></div>
          </M3Card>
        )}
        {/* Step 4 — documents */}
        {data.docs && (<>
          <div style={{ fontSize: 13, color: M.onSurfVar, lineHeight: 1.5, padding: "0 2px" }}>Upload at least one document before placing orders. Verification is usually instant.</div>
          {[["Aadhaar card", "doc", true], ["PAN card", "card", true], ["GST certificate", "receipt", false], ["Trade license", "layers", false]].map(([label, icon, done]) => (
            <M3Card key={label} variant="outlined" pad={14} style={{ display: "flex", alignItems: "center", gap: 14 }}>
              <div style={{ width: 44, height: 44, borderRadius: 12, background: done ? "var(--pos-tint)" : M.surf3, color: done ? "var(--pos)" : M.onSurfVar, display: "grid", placeItems: "center", flexShrink: 0 }}><Icon name={done ? "check" : icon} size={done ? 22 : 20} stroke={done ? 2.6 : 2} /></div>
              <div style={{ flex: 1 }}><div style={{ fontSize: 14.5, fontWeight: 700, color: M.onSurf }}>{label}</div><div style={{ fontSize: 12, color: done ? "var(--pos)" : M.onSurfVar, marginTop: 1 }}>{done ? "Uploaded · verified" : "Tap to upload"}</div></div>
              {!done && <M3IconBtn icon="upload" filled color={M.primary} size={20} />}
            </M3Card>
          ))}
        </>)}
        {/* Step 5 — referral */}
        {data.referral && (<>
          <M3Card variant="filled" pad={18} style={{ textAlign: "center" }}>
            <div style={{ width: 56, height: 56, borderRadius: 16, background: "var(--gold-tint)", color: "var(--gold-ink)", display: "grid", placeItems: "center", margin: "0 auto 12px" }}><Icon name="star" size={28} /></div>
            <div style={{ fontSize: 16, fontWeight: 700, color: M.onSurf }}>Were you referred?</div>
            <div style={{ fontSize: 13, color: M.onSurfVar, marginTop: 4, lineHeight: 1.5 }}>If a FlashMart employee onboarded you, enter their referral code to link your account.</div>
          </M3Card>
          <M3Field variant="outlined" label="Referral code (optional)" icon="tag" value="NEHA-FM21" focused />
        </>)}
      </div>
      <div style={{ padding: "14px 24px 30px", display: "flex", gap: 12 }}>
        <M3Button variant="filled" size="lg" full iconRight={last ? "check" : "arrowR"} onClick={onNext}>{last ? "Submit & finish" : "Continue"}</M3Button>
      </div>
    </div>
  );
}

Object.assign(window, { AND_Mark, AND_Splash, AND_Welcome, AND_UserType, AND_ROLES, AND_Login, AND_Otp, AND_OtpBoxes, AND_Register, AND_Progress, AND_STEPS });
