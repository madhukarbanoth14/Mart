/* ============================================================
   Flashmart iOS — gallery (expanded, all screens)
   ============================================================ */
const DEVICE_W = 402, DEVICE_H = 874;
const abStyle = { background: "transparent", boxShadow: "none", overflow: "visible" };

const board = (id, label, node) => (
  <DCArtboard id={id} label={label} width={DEVICE_W} height={DEVICE_H} style={abStyle}>
    <IOSDevice>{node}</IOSDevice>
  </DCArtboard>
);

function Gallery() {
  return (
    <DesignCanvas>
      <DCSection id="auth" title="Onboarding & Auth" subtitle="Splash → onboarding → login → OTP → permissions">
        {board("flow",   "▶ Tap-through flow (live)", <AuthFlow />)}
        {board("splash", "Splash",                    <SplashScreen />)}
        {board("onb1",   "Onboarding · Order",        <OnboardingScreen index={0} />)}
        {board("onb2",   "Onboarding · Deliver",      <OnboardingScreen index={1} />)}
        {board("onb3",   "Onboarding · Bill",         <OnboardingScreen index={2} />)}
        {board("login",  "Login · phone",             <LoginScreen phone="98110 24567" />)}
        {board("otp",    "OTP verification",          <OtpScreen code="2489" phone="98110 24567" resendIn={18} />)}
        {board("loc",    "Permission · location",     <LocationPermScreen />)}
        {board("notif",  "Permission · notifications",<NotifPermScreen />)}
      </DCSection>

      <DCSection id="shopkeeper" title="Shopkeeper app" subtitle="Browse · order · pay · track · invoice">
        {board("sk-home",     "Home",             <SK_Home />)}
        {board("sk-catalog",  "Catalog",          <SK_Catalog />)}
        {board("sk-cart",     "Cart & GST",       <SK_Cart />)}
        {board("sk-checkout", "Checkout",         <SK_Checkout />)}
        {board("sk-payment",  "Payment",          <SK_Payment />)}
        {board("sk-success",  "Order success",    <SK_Success />)}
        {board("sk-orders",   "My orders",        <SK_Orders />)}
        {board("sk-track",    "Order tracking",   <SK_Track />)}
        {board("sk-invoice",  "Tax invoice",      <SK_Invoice />)}
        {board("sk-wallet",   "Wallet & ledger",  <SK_Wallet />)}
        {board("sk-profile",  "Profile",          <SK_Profile />)}
      </DCSection>

      <DCSection id="dealer" title="Dealer app" subtitle="Fulfil orders · manage stock · add SKUs">
        {board("dl-home",     "Dashboard",         <DL_Home />)}
        {board("dl-orders",   "Order workflow",    <DL_Orders />)}
        {board("dl-stock",    "Stock & reorder",   <DL_Stock />)}
        {board("dl-addsku",   "Add SKU",           <DL_AddSKU />)}
        {board("dl-approval", "Product approvals", <DL_Approvals />)}
      </DCSection>

      <DCSection id="employee" title="Employee app" subtitle="Onboard partners · CRM · follow-ups">
        {board("em-home",   "Performance",       <EM_Home />)}
        {board("em-add",    "Add shopkeeper",    <EM_AddForm />)}
        {board("em-crm",    "CRM",               <EM_CRM />)}
        {board("em-follow", "Follow-up detail",  <EM_FollowUp />)}
        {board("em-net",    "Network",           <EM_Network />)}
      </DCSection>

      <DCSection id="admin" title="Admin dashboard" subtitle="GMV · area performance · dealer management · SKU approvals">
        {board("ad-home",     "Operations overview", <ADMIN_Home />)}
        {board("ad-dealers",  "Dealer management",   <ADMIN_Dealers />)}
        {board("ad-approvals","SKU approvals",       <ADMIN_Approvals />)}
      </DCSection>

      <DCSection id="shared" title="Shared modules" subtitle="Notifications · document center">
        {board("sh-notif", "Notifications",    <IOS_Notifications />)}
        {board("sh-docs",  "Document center",  <IOS_DocCenter />)}
      </DCSection>

      <DCSection id="states" title="System states" subtitle="Loading · empty · error">
        {board("st-loading", "Loading · skeleton", <ST_Loading />)}
        {board("st-empty",   "Empty · no orders",  <ST_Empty />)}
        {board("st-error",   "Error · offline",    <ST_Error />)}
      </DCSection>
    </DesignCanvas>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<Gallery />);
