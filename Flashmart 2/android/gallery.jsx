/* ============================================================
   FlashMart Android — gallery (expanded, all screens)
   ============================================================ */
const A_W = 412, A_H = 892;
const aAbStyle = { background: "transparent", boxShadow: "none", overflow: "visible" };
const aboard = (id, label, node) => (
  <DCArtboard id={id} label={label} width={A_W} height={A_H} style={aAbStyle}>
    <AndroidDevice>{node}</AndroidDevice>
  </DCArtboard>
);

function AndroidGallery() {
  return (
    <DesignCanvas>
      <div style={{
        maxWidth: 1180, margin: "0 auto 28px", padding: "28px 24px 0",
        fontFamily: "var(--font, -apple-system, system-ui, sans-serif)",
      }}>
        <div style={{
          borderRadius: 24, padding: "22px 24px", color: "#fff", position: "relative", overflow: "hidden",
          background: "linear-gradient(135deg, #15924b 0%, #0b6332 62%, #16307a 100%)",
          boxShadow: "0 16px 40px rgba(19,23,34,.12)",
        }}>
          <div style={{ position: "absolute", right: -40, top: -40, width: 180, height: 180, borderRadius: "50%", background: "rgba(255,255,255,.08)" }} />
          <div style={{ position: "absolute", left: -20, bottom: -50, width: 140, height: 140, borderRadius: "50%", background: "rgba(227,160,8,.12)" }} />
          <div style={{ position: "relative", display: "flex", flexWrap: "wrap", gap: 16, alignItems: "center" }}>
            <div style={{ flex: "1 1 280px" }}>
              <div style={{ fontSize: 11, fontWeight: 800, letterSpacing: ".08em", textTransform: "uppercase", color: "#f8d57a" }}>FlashMart · Android</div>
              <div style={{ fontSize: 30, fontWeight: 800, letterSpacing: "-.03em", marginTop: 6 }}>Material 3 UI Gallery</div>
              <div style={{ fontSize: 14.5, lineHeight: 1.55, color: "rgba(255,255,255,.82)", marginTop: 8, maxWidth: 560 }}>
                Full-stack reference for shopkeeper, dealer, employee, and admin flows — auth, checkout, notifications, documents, and system states.
              </div>
            </div>
            <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>
              {["48 screens", "M3 components", "Razorpay", "Phase 1–8"].map(tag => (
                <span key={tag} style={{
                  fontSize: 11, fontWeight: 700, letterSpacing: ".04em", textTransform: "uppercase",
                  padding: "7px 11px", borderRadius: 999,
                  background: tag === "Phase 1–8" ? "rgba(227,160,8,.22)" : "rgba(255,255,255,.12)",
                  color: tag === "Phase 1–8" ? "#f8d57a" : "rgba(255,255,255,.92)",
                }}>{tag}</span>
              ))}
            </div>
          </div>
        </div>
      </div>
      <DCSection id="auth" title="Onboarding & Auth" subtitle="Material 3 · splash → welcome → account type → OTP → 5-step registration">
        {aboard("flow",  "▶ Tap-through flow (live)", <AndroidAuthFlow />)}
        {aboard("splash","Splash",                    <AND_Splash />)}
        {aboard("welcome","Welcome",                  <AND_Welcome />)}
        {aboard("type",  "Account type",              <AND_UserType />)}
        {aboard("login", "Phone login",               <AND_Login phone="98110 24567" valid />)}
        {aboard("otp",   "OTP verification",          <AND_Otp code="2489" phone="98110 24567" resendIn={14} />)}
        {aboard("reg1",  "Register · Business",       <AND_Register step={0} />)}
        {aboard("reg2",  "Register · Area",           <AND_Register step={1} />)}
        {aboard("reg3",  "Register · Address",        <AND_Register step={2} />)}
        {aboard("reg4",  "Register · Documents",      <AND_Register step={3} />)}
        {aboard("reg5",  "Register · Referral",       <AND_Register step={4} />)}
      </DCSection>

      <DCSection id="shopkeeper" title="Shopkeeper app" subtitle="Browse · order · pay · track · invoice · wallet">
        {aboard("sk-home",     "Home",             <AND_SK_Home />)}
        {aboard("sk-catalog",  "Catalog",          <AND_SK_Catalog />)}
        {aboard("sk-product",  "Product detail",   <AND_SK_Product />)}
        {aboard("sk-cart",     "Cart & GST",       <AND_SK_Cart />)}
        {aboard("sk-checkout", "Checkout",         <AND_SK_Checkout />)}
        {aboard("sk-doc",      "Document gate",    <AND_SK_DocDialog />)}
        {aboard("sk-pay",      "Payment · Razorpay",<AND_SK_Payment />)}
        {aboard("sk-success",  "Order success",    <AND_SK_Success />)}
        {aboard("sk-orders",   "My orders",        <AND_SK_Orders />)}
        {aboard("sk-track",    "Order tracking",   <AND_SK_Track />)}
        {aboard("sk-invoice",  "Tax invoice",      <AND_SK_Invoice />)}
        {aboard("sk-wallet",   "Wallet & ledger",  <AND_SK_Wallet />)}
        {aboard("sk-profile",  "Profile",          <AND_SK_Profile />)}
      </DCSection>

      <DCSection id="dealer" title="Dealer app" subtitle="Fulfil orders · manage inventory · add SKUs">
        {aboard("dl-home",    "Dashboard",         <AND_DL_Home />)}
        {aboard("dl-orders",  "Order workflow",    <AND_DL_Orders />)}
        {aboard("dl-stock",   "Inventory",         <AND_DL_Stock />)}
        {aboard("dl-add",     "Add SKU",           <AND_DL_AddSKU />)}
        {aboard("dl-approval","Product approvals", <AND_DL_Approval />)}
      </DCSection>

      <DCSection id="employee" title="Employee app" subtitle="Onboard partners · CRM · follow-ups">
        {aboard("em-home",   "Dashboard & tasks",  <AND_EM_Home />)}
        {aboard("em-crm",    "CRM",                <AND_EM_CRM />)}
        {aboard("em-onboard","Onboard partner",    <AND_EM_Onboard />)}
        {aboard("em-follow", "Follow-up detail",   <AND_EM_FollowUp />)}
      </DCSection>

      <DCSection id="admin" title="Admin dashboard" subtitle="GMV · area performance · dealer management · SKU approvals">
        {aboard("ad-home",    "Operations overview", <AND_ADMIN_Home />)}
        {aboard("ad-dealers", "Dealer management",   <AND_ADMIN_Dealers />)}
        {aboard("ad-approvals","SKU approvals",      <AND_ADMIN_Approvals />)}
      </DCSection>

      <DCSection id="shared" title="Shared modules" subtitle="Notifications · document center">
        {aboard("sh-notif","Notifications",   <AND_Notifications />)}
        {aboard("sh-docs", "Document center", <AND_DocCenter />)}
      </DCSection>

      <DCSection id="states" title="System states" subtitle="Loading · empty · error">
        {aboard("st-loading","Loading · skeleton", <AND_ST_Loading />)}
        {aboard("st-empty",  "Empty · no orders",  <AND_ST_Empty />)}
        {aboard("st-error",  "Error · offline",    <AND_ST_Error />)}
      </DCSection>
    </DesignCanvas>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<AndroidGallery />);
