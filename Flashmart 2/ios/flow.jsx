/* ============================================================
   FlashMart iOS — clickable onboarding/auth flow
   Splash → Onboarding ×3 → Login → OTP → Location → Notif → Done
   ============================================================ */
function AuthFlow() {
  const [step, setStep] = React.useState("splash");
  const [onb, setOnb] = React.useState(0);
  const [phone, setPhone] = React.useState("");
  const [code, setCode] = React.useState("");
  const [resend, setResend] = React.useState(24);

  // splash auto-advances
  React.useEffect(() => {
    if (step !== "splash") return;
    const t = setTimeout(() => setStep("onb"), 1900);
    return () => clearTimeout(t);
  }, [step]);

  // otp countdown + auto-fill demo
  React.useEffect(() => {
    if (step !== "otp") return;
    setResend(24);
    const iv = setInterval(() => setResend(r => (r > 0 ? r - 1 : 0)), 1000);
    // auto-type a demo code
    let i = 0; const demo = "248913";
    const typer = setInterval(() => { i++; setCode(demo.slice(0, i)); if (i >= 6) clearInterval(typer); }, 320);
    return () => { clearInterval(iv); clearInterval(typer); };
  }, [step]);

  const fmtPhone = (v) => v.replace(/\D/g, "").slice(0, 10).replace(/(\d{5})(\d{0,5})/, (_, a, b) => b ? a + " " + b : a);

  const restart = () => { setStep("splash"); setOnb(0); setPhone(""); setCode(""); };

  if (step === "splash") return <SplashScreen />;
  if (step === "onb") return <OnboardingScreen index={onb}
    onSkip={() => setStep("login")}
    onNext={() => { if (onb < 2) setOnb(onb + 1); else setStep("login"); }} />;
  if (step === "login") return <LoginScreen phone={phone} onPhone={v => setPhone(fmtPhone(v))} onContinue={() => { setCode(""); setStep("otp"); }} />;
  if (step === "otp") return <OtpScreen code={code} phone={phone || "98110 24567"} resendIn={resend}
    onBack={() => setStep("login")} onVerify={() => setStep("loc")} />;
  if (step === "loc") return <LocationPermScreen onAllow={() => setStep("notif")} onSkip={() => setStep("notif")} />;
  if (step === "notif") return <NotifPermScreen onAllow={() => setStep("done")} onSkip={() => setStep("done")} />;

  // done — welcome / hand-off
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", textAlign: "center",
      padding: "0 30px", background: "linear-gradient(165deg, var(--brand), var(--brand-700))", color: "#fff" }}>
      <SuccessCheck size={108} color="#fff" />
      <div style={{ fontSize: 28, fontWeight: 700, letterSpacing: "-.025em", marginTop: 22 }}>You're all set</div>
      <div style={{ fontSize: 15.5, color: "rgba(255,255,255,.78)", marginTop: 10, lineHeight: 1.5, maxWidth: 280 }}>
        Welcome to FlashMart, Ramesh. Your store is verified and ready to order.
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 11, width: "100%", marginTop: 30 }}>
        <Button variant="outline" size="lg" full style={{ background: "#fff", color: "var(--brand-700)", border: "none", fontWeight: 700 }} onClick={restart}>Replay flow</Button>
      </div>
    </div>
  );
}

window.AuthFlow = AuthFlow;
