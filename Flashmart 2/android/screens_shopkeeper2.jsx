/* ============================================================
   FlashMart Android — Shopkeeper journey (part 2)
   Checkout · Payment (Razorpay-style) · Success · Track
   ============================================================ */
const SF2 = window.FM;

/* ---------------- CHECKOUT ---------------- */
function AND_SK_Checkout() {
  const items = [["atta", 6], ["oil", 12], ["salt", 10]].map(([id, q]) => ({ ...SF2.lookups.product(id), qty: q }));
  const t = SF2.orderMath(items.map(it => ({ ...it })));
  const pay = [["upi", "UPI", "GPay · PhonePe · Paytm", true], ["card", "Credit / Debit card", "Visa, Mastercard, RuPay", false], ["wallet", "FlashMart credit", "₹8,240 outstanding", false]];
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: M.surface, position: "relative" }}>
      <M3TopBar onBack title="Checkout" />
      <div className="fm-scroll" style={{ flex: 1, overflow: "auto", padding: "4px 16px 120px", display: "flex", flexDirection: "column", gap: 14 }}>
        {/* delivery address */}
        <div>
          <M3SectionLabel action="Change">Delivery address</M3SectionLabel>
          <M3Card variant="outlined" style={{ display: "flex", gap: 13, alignItems: "flex-start" }}>
            <div style={{ width: 40, height: 40, borderRadius: 12, background: M.secCont, color: M.onSecCont, display: "grid", placeItems: "center", flexShrink: 0 }}><Icon name="pin" size={20} /></div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 14.5, fontWeight: 700, color: M.onSurf }}>Madhukar General Store</div>
              <div style={{ fontSize: 13, color: M.onSurfVar, marginTop: 2, lineHeight: 1.45 }}>Shop 14, Sai Plaza, M.G. Road, Andheri East, Mumbai 400069</div>
            </div>
          </M3Card>
        </div>
        {/* dealer */}
        <M3Card variant="filled" style={{ display: "flex", alignItems: "center", gap: 13 }}>
          <Avatar name="Shree Balaji" size={40} tint="var(--blue)" />
          <div style={{ flex: 1 }}><div style={{ fontSize: 14, fontWeight: 700, color: M.onSurf }}>Shree Balaji Distributors</div><div style={{ fontSize: 12.5, color: M.onSurfVar }}>Your dealer · Andheri East</div></div>
          <M3Status status="Active" size="sm" dot />
        </M3Card>
        {/* payment method */}
        <div>
          <M3SectionLabel>Payment method</M3SectionLabel>
          <M3Card variant="outlined" pad={6}>
            {pay.map(([ic, title, sub, on], i) => (
              <div key={title} style={{ display: "flex", alignItems: "center", gap: 13, padding: "13px 8px", borderBottom: i === pay.length - 1 ? "none" : "1px solid var(--m3-outline-var)" }}>
                <div style={{ width: 38, height: 38, borderRadius: 11, background: M.surf4, color: M.onSurfVar, display: "grid", placeItems: "center", flexShrink: 0 }}><Icon name={ic} size={19} /></div>
                <div style={{ flex: 1 }}><div style={{ fontSize: 14.5, fontWeight: 700, color: M.onSurf }}>{title}</div><div style={{ fontSize: 12, color: M.onSurfVar }}>{sub}</div></div>
                <div style={{ width: 22, height: 22, borderRadius: 99, border: on ? `7px solid ${M.primary}` : `2px solid ${M.outline}`, transition: "all .15s" }} />
              </div>
            ))}
          </M3Card>
        </div>
        {/* summary */}
        <M3Card variant="filled">
          <MoneyRow label="Subtotal" value={SF2.inr(t.sub)} />
          <MoneyRow label="Discount" value={"− " + SF2.inr(t.disc)} accent="var(--pos)" />
          <MoneyRow label="GST" value={"+ " + SF2.inr(t.gst)} />
          <div style={{ borderTop: "1px dashed var(--m3-outline-var)", margin: "6px 0" }} />
          <MoneyRow strong label="Total payable" value={SF2.inr(t.total)} />
        </M3Card>
      </div>
      <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, padding: "14px 16px", paddingBottom: 14 + AND_BOTTOM,
        background: M.surf2, boxShadow: "0 -1px 0 var(--m3-outline-var)" }}>
        <M3Button variant="filled" size="lg" full icon="bolt">Place order · {SF2.inr(t.total)}</M3Button>
      </div>
    </div>
  );
}

/* ---------------- DOCUMENT VALIDATION DIALOG ---------------- */
function AND_SK_DocDialog() {
  return (
    <div style={{ height: "100%", position: "relative", overflow: "hidden", background: M.surface }}>
      {/* dimmed checkout behind */}
      <div style={{ position: "absolute", inset: 0, opacity: .5, filter: "saturate(.7)" }}>
        <div style={{ paddingTop: AND_TOP + 8, padding: `${AND_TOP + 8}px 16px 0` }}>
          <div style={{ height: 56, background: M.surf2, borderRadius: 16, marginBottom: 12 }} />
          <div style={{ height: 90, background: M.surf2, borderRadius: 16, marginBottom: 12 }} />
          <div style={{ height: 160, background: M.surf2, borderRadius: 16 }} />
        </div>
      </div>
      <div style={{ position: "absolute", inset: 0, background: "rgba(0,0,0,.4)" }} />
      {/* M3 basic dialog */}
      <div style={{ position: "absolute", left: 24, right: 24, top: "50%", transform: "translateY(-50%)",
        background: M.surf3, borderRadius: 28, padding: 24, boxShadow: "var(--m3-e3)" }}>
        <div style={{ width: 56, height: 56, borderRadius: 16, background: "var(--gold-tint)", color: "var(--gold-ink)", display: "grid", placeItems: "center", margin: "0 auto 16px" }}><Icon name="doc" size={28} /></div>
        <div style={{ fontSize: 22, fontWeight: 700, textAlign: "center", letterSpacing: "-.02em", color: M.onSurf }}>Complete verification</div>
        <div style={{ fontSize: 14, color: M.onSurfVar, textAlign: "center", marginTop: 10, lineHeight: 1.5 }}>Please upload at least one valid document (Aadhaar, PAN, GST or trade license) before placing orders.</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10, marginTop: 22 }}>
          <M3Button variant="filled" size="lg" full icon="upload">Upload now</M3Button>
          <M3Button variant="text" size="md" full>Later</M3Button>
        </div>
      </div>
    </div>
  );
}

/* ---------------- PAYMENT (Razorpay-style sheet) ---------------- */
function AND_SK_Payment() {
  const upi = [["GPay", "#1a73e8"], ["PhonePe", "#5f259f"], ["Paytm", "#00b9f1"]];
  return (
    <div style={{ height: "100%", position: "relative", overflow: "hidden", background: "rgba(0,0,0,.45)" }}>
      <div style={{ paddingTop: AND_TOP }} />
      {/* payment sheet */}
      <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, top: 90, background: M.surface, borderRadius: "28px 28px 0 0", overflow: "hidden", display: "flex", flexDirection: "column" }}>
        {/* merchant header */}
        <div style={{ background: "var(--brand-700)", color: "#fff", padding: "18px 20px 16px", display: "flex", alignItems: "center", gap: 12 }}>
          <AND_Mark size={40} radius={12} />
          <div style={{ flex: 1 }}><div style={{ fontWeight: 800, fontSize: 16 }}>FlashMart</div><div style={{ fontSize: 12, opacity: .75 }}>Secured by Razorpay</div></div>
          <div style={{ textAlign: "right" }}><div className="mono" style={{ fontSize: 18, fontWeight: 700 }}>{SF2.inr(3998)}</div><div style={{ fontSize: 11, opacity: .75 }}>ORD-1126</div></div>
        </div>
        <div className="fm-scroll" style={{ flex: 1, overflow: "auto", padding: "18px 16px 24px" }}>
          <M3SectionLabel>UPI</M3SectionLabel>
          <div style={{ display: "flex", gap: 10, marginBottom: 20 }}>
            {upi.map(([n, c]) => (
              <div key={n} style={{ flex: 1, border: `1px solid ${M.outlineVar}`, borderRadius: 16, padding: "14px 8px", textAlign: "center", background: M.surf1 }}>
                <div style={{ width: 36, height: 36, borderRadius: 10, background: c, color: "#fff", display: "grid", placeItems: "center", margin: "0 auto 8px", fontWeight: 800, fontSize: 16 }}>{n[0]}</div>
                <div style={{ fontSize: 12, fontWeight: 700, color: M.onSurf }}>{n}</div>
              </div>
            ))}
          </div>
          <M3SectionLabel>Other methods</M3SectionLabel>
          <M3Card variant="outlined" pad={6}>
            {[["card", "Cards", "Visa · Mastercard · RuPay"], ["wallet", "Wallets", "Amazon Pay, Mobikwik"], ["doc", "Net banking", "All major banks"]].map(([ic, t, s], i) => (
              <M3ListItem key={t} last={i === 2}
                leading={<div style={{ width: 40, height: 40, borderRadius: 11, background: M.surf4, color: M.onSurfVar, display: "grid", placeItems: "center" }}><Icon name={ic} size={20} /></div>}
                headline={t} supporting={s} trailing={<Icon name="chevR" size={18} color={M.onSurfVar} />} />
            ))}
          </M3Card>
        </div>
        <div style={{ padding: "12px 16px", paddingBottom: 14 + AND_BOTTOM, boxShadow: "0 -1px 0 var(--m3-outline-var)", background: M.surf2 }}>
          <M3Button variant="filled" size="lg" full icon="bolt">Pay {SF2.inr(3998)}</M3Button>
        </div>
      </div>
    </div>
  );
}

/* ---------------- ORDER SUCCESS ---------------- */
function AND_SK_Success() {
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: M.surface, position: "relative" }}>
      <div style={{ paddingTop: AND_TOP }} />
      <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", textAlign: "center", padding: "0 30px" }}>
        <SuccessCheck size={104} color="var(--pos)" />
        <div style={{ fontSize: 26, fontWeight: 800, letterSpacing: "-.025em", marginTop: 22, color: M.onSurf }}>Order placed!</div>
        <div style={{ fontSize: 14.5, color: M.onSurfVar, marginTop: 8, lineHeight: 1.5, maxWidth: 280 }}>Your dealer has been notified and will confirm shortly.</div>
        <M3Card variant="filled" pad={18} style={{ width: "100%", marginTop: 26, textAlign: "left" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", paddingBottom: 12, borderBottom: "1px dashed var(--m3-outline-var)" }}>
            <span style={{ fontSize: 13, color: M.onSurfVar, fontWeight: 600 }}>Order number</span>
            <span className="mono" style={{ fontSize: 14, fontWeight: 700, color: M.onSurf }}>ORD-1126</span>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", padding: "12px 0" }}>
            <span style={{ fontSize: 13, color: M.onSurfVar, fontWeight: 600 }}>Amount paid</span>
            <span className="mono" style={{ fontSize: 14, fontWeight: 700, color: M.onSurf }}>{SF2.inr(3998)}</span>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <span style={{ fontSize: 13, color: M.onSurfVar, fontWeight: 600 }}>Expected delivery</span>
            <span style={{ fontSize: 14, fontWeight: 700, color: "var(--pos)" }}>Tomorrow, by 6 PM</span>
          </div>
        </M3Card>
      </div>
      <div style={{ padding: "0 24px 30px", paddingBottom: 30 + AND_BOTTOM, display: "flex", flexDirection: "column", gap: 11 }}>
        <M3Button variant="filled" size="lg" full icon="truck">Track order</M3Button>
        <M3Button variant="tonal" size="lg" full icon="receipt">View invoice</M3Button>
      </div>
    </div>
  );
}

/* ---------------- TRACK ---------------- */
function AND_SK_Track() {
  const steps = [
    { label: "Order placed", time: "Jun 6 · 10:24 AM", state: "done" },
    { label: "Payment successful", time: "Paid via UPI", state: "done" },
    { label: "Dealer accepted", time: "Shree Balaji Distributors", state: "done" },
    { label: "Out for delivery", time: "On the way to your store", state: "active" },
    { label: "Delivered", time: "Pending", state: "todo" },
  ];
  return (
    <M3Screen topBar={<M3TopBar onBack title="Track order" subtitle="ORD-1122" />}>
      <div style={{ padding: "4px 16px 16px", display: "flex", flexDirection: "column", gap: 14 }}>
        <div style={{ borderRadius: 24, padding: 20, color: "#fff", background: "linear-gradient(150deg, var(--brand), var(--brand-700))", boxShadow: "var(--m3-e2)" }}>
          <div style={{ fontSize: 12.5, fontWeight: 600, opacity: .85 }}>Estimated delivery</div>
          <div style={{ fontSize: 23, fontWeight: 800, letterSpacing: "-.02em", marginTop: 4 }}>Today, by 6:00 PM</div>
          <div style={{ display: "flex", alignItems: "center", gap: 7, marginTop: 8, fontSize: 13, opacity: .9 }}><Icon name="truck" size={16} /> Out for delivery</div>
        </div>
        <M3Card variant="filled">
          <M3SectionLabel style={{ paddingBottom: 14 }}>Order journey</M3SectionLabel>
          <OrderTimeline steps={steps} />
        </M3Card>
        <M3Card variant="outlined" style={{ display: "flex", alignItems: "center", gap: 13 }}>
          <Avatar name="Shree Balaji" size={42} tint="var(--blue)" />
          <div style={{ flex: 1 }}><div style={{ fontSize: 14.5, fontWeight: 700, color: M.onSurf }}>Shree Balaji Distributors</div><div style={{ fontSize: 12.5, color: M.onSurfVar }}>Your dealer · Andheri East</div></div>
          <M3IconBtn icon="phone" filled color="var(--pos)" />
        </M3Card>
      </div>
    </M3Screen>
  );
}

Object.assign(window, { AND_SK_Checkout, AND_SK_DocDialog, AND_SK_Payment, AND_SK_Success, AND_SK_Track });
