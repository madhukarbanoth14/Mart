/* ============================================================
   FlashMart Android — clickable auth flow (tap-through)
   Splash → Welcome → User type → Phone → OTP → Registration → Done
   ============================================================ */
function AndroidAuthFlow() {
  const [step, setStep] = React.useState("splash");
  const [role, setRole] = React.useState("shopkeeper");
  const [phone, setPhone] = React.useState("");
  const [code, setCode] = React.useState("");
  const [resend, setResend] = React.useState(18);
  const [reg, setReg] = React.useState(0);

  React.useEffect(() => {
    if (step !== "splash") return;
    const t = setTimeout(() => setStep("welcome"), 1900);
    return () => clearTimeout(t);
  }, [step]);

  React.useEffect(() => {
    if (step !== "otp") return;
    setResend(18); setCode("");
    const iv = setInterval(() => setResend(r => (r > 0 ? r - 1 : 0)), 1000);
    let i = 0; const demo = "248913";
    const typer = setInterval(() => { i++; setCode(demo.slice(0, i)); if (i >= 6) clearInterval(typer); }, 300);
    return () => { clearInterval(iv); clearInterval(typer); };
  }, [step]);

  const fmtPhone = v => v.replace(/\D/g, "").slice(0, 10).replace(/(\d{5})(\d{0,5})/, (_, a, b) => b ? a + " " + b : a);
  const restart = () => { setStep("splash"); setRole("shopkeeper"); setPhone(""); setCode(""); setReg(0); };

  if (step === "splash") return <AND_Splash />;
  if (step === "welcome") return <AND_Welcome onLogin={() => setStep("login")} onSignup={() => setStep("type")} />;
  if (step === "type") return <AND_UserType selected={role} onSelect={setRole} onBack={() => setStep("welcome")} onContinue={() => setStep("login")} />;
  if (step === "login") return <AND_Login phone={phone || "98110 24567"} valid onBack={() => setStep("welcome")} onContinue={() => setStep("otp")} />;
  if (step === "otp") return <AND_Otp code={code} phone={phone || "98110 24567"} resendIn={resend} onBack={() => setStep("login")} onVerify={() => setStep("reg")} />;
  if (step === "reg") return <AND_Register step={reg} onBack={() => reg > 0 ? setReg(reg - 1) : setStep("otp")}
    onNext={() => { if (reg < 4) setReg(reg + 1); else setStep("done"); }} />;

  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", textAlign: "center",
      padding: "0 30px", background: "linear-gradient(165deg, #1aa657, var(--brand) 60%, var(--brand-700))", color: "#fff" }}>
      <SuccessCheck size={108} color="#fff" />
      <div style={{ fontSize: 28, fontWeight: 800, letterSpacing: "-.025em", marginTop: 22 }}>You're all set</div>
      <div style={{ fontSize: 15.5, color: "rgba(255,255,255,.82)", marginTop: 10, lineHeight: 1.5, maxWidth: 280 }}>
        Welcome to FlashMart, Madhukar. Your store is verified and ready to order.
      </div>
      <div style={{ width: "100%", marginTop: 30 }}>
        <M3Button variant="gold" size="lg" full icon="refresh" onClick={restart}>Replay flow</M3Button>
      </div>
    </div>
  );
}
window.AndroidAuthFlow = AndroidAuthFlow;
