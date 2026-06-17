/* ============================================================
   Flashmart — Shopkeeper sub-screens
   ============================================================ */

function timelineFor(o) {
  const idx = { placed: 0, accepted: 1, out: 2, delivered: 3 }[o.status];
  const dealer = window.FM.lookups.dealer(o.dealer);
  return [
    { label: "Order placed", time: o.date + " · 10:24 AM", state: "done" },
    { label: "Payment successful", time: o.paid ? "Paid via UPI" : "Awaiting payment", state: o.paid ? "done" : "todo" },
    { label: "Dealer accepted", time: idx >= 1 ? dealer.name : "Awaiting confirmation", state: idx >= 1 ? "done" : (o.status === "placed" ? "active" : "todo") },
    { label: "Out for delivery", time: idx >= 2 ? "On the way to your store" : "Not dispatched yet", state: idx >= 3 ? "done" : (o.status === "out" ? "active" : "todo") },
    { label: "Delivered", time: idx >= 3 ? "Received at store" : "Pending", state: idx >= 3 ? "done" : "todo" },
  ];
}

/* sticky footer helper inside phone */
function PhoneFooter({ children }) {
  return (
    <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, zIndex: 40, padding: "14px 16px 30px",
      background: "linear-gradient(to top, var(--surface) 64%, rgba(255,255,255,0))" }}>{children}</div>
  );
}

/* ---------------- CART ---------------- */
function CartScreen({ S, back, go, scrollRef }) {
  const F = S.F; const items = S.cartItems; const t = S.cartTotals;
  return (
    <>
      <PhoneShell scrollRef={scrollRef}
        header={<AppHeader pad title="Cart" subtitle={items.length ? items.length + " items" : "Empty"} onBack={back} />}>
        {items.length === 0 ? (
          <div style={{ padding: "70px 30px", textAlign: "center" }}>
            <div style={{ width: 72, height: 72, borderRadius: 20, background: "var(--surface-2)", display: "grid", placeItems: "center", margin: "0 auto 16px", color: "var(--ink-4)" }}><Icon name="cart" size={32} stroke={1.6} /></div>
            <div style={{ fontSize: 17, fontWeight: 700 }}>Your cart is empty</div>
            <div style={{ fontSize: 13.5, color: "var(--ink-3)", margin: "6px 0 18px" }}>Browse the catalog to add products.</div>
            <Button variant="soft" icon="grid" onClick={back}>Browse products</Button>
          </div>
        ) : (
          <div style={{ padding: "4px 16px 0", display: "flex", flexDirection: "column", gap: 12 }}>
            <Card pad={6}>
              {items.map((it, i) => {
                const m = F.lineMath(it);
                return (
                  <div key={it.pid} style={{ display: "flex", gap: 12, alignItems: "center", padding: "10px 8px",
                    borderBottom: i === items.length - 1 ? "none" : "1px solid var(--line)" }}>
                    <ProductThumb p={it} size={52} />
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ fontSize: 14, fontWeight: 600, lineHeight: 1.2 }}>{it.name}</div>
                      <div style={{ fontSize: 12, color: "var(--ink-4)", margin: "3px 0 7px" }} className="mono">{F.inr(it.price)} · {it.disc}% off · GST {it.gst}%</div>
                      <Stepper value={it.qty} onChange={(v) => S.setQty(it.pid, v)} />
                    </div>
                    <span className="mono" style={{ fontSize: 14.5, fontWeight: 700, alignSelf: "flex-start", marginTop: 4 }}>{F.inr(m.total)}</span>
                  </div>
                );
              })}
            </Card>
            <Card>
              <MoneyRow label="Subtotal" value={F.inr(t.sub)} />
              <MoneyRow label="Shopkeeper discount" value={"− " + F.inr(t.disc)} accent="var(--pos)" />
              <MoneyRow label="GST" value={"+ " + F.inr(t.gst)} />
              <div style={{ borderTop: "1px dashed var(--line-2)", margin: "4px 0" }} />
              <MoneyRow strong label="Total payable" value={F.inr(t.total)} />
            </Card>
            <div style={{ display: "flex", gap: 8, alignItems: "center", color: "var(--ink-3)", fontSize: 12.5, padding: "2px 4px 90px" }}>
              <Icon name="bolt" size={15} color="var(--brand)" /> Delivered by your dealer · {F.lookups.dealer(F.ME.dealer).name}
            </div>
          </div>
        )}
      </PhoneShell>
      {items.length > 0 && (
        <PhoneFooter>
          <Button variant="primary" size="lg" full iconRight="arrowR" onClick={() => go("payment")}>
            <span style={{ display: "flex", alignItems: "center", gap: 10 }}>Proceed to payment
              <span className="mono" style={{ background: "rgba(255,255,255,.18)", padding: "2px 8px", borderRadius: 7 }}>{F.inr(t.total)}</span></span>
          </Button>
        </PhoneFooter>
      )}
      <Toast />
    </>
  );
}

/* ---------------- PAYMENT ---------------- */
function PaymentScreen({ S, back, pay, setPay, onPay, paying }) {
  const F = S.F; const t = S.cartTotals;
  const methods = [
    { id: "upi", label: "UPI", sub: "GPay · PhonePe · Paytm", icon: "upi" },
    { id: "card", label: "Card", sub: "Credit / Debit", icon: "card" },
    { id: "wallet", label: "Flashmart Credit", sub: "₹8,400 available", icon: "wallet" },
  ];
  return (
    <>
      <PhoneShell header={<AppHeader pad title="Payment" subtitle="Secure checkout" onBack={paying ? undefined : back} />}>
        <div style={{ padding: "4px 16px 120px", display: "flex", flexDirection: "column", gap: 16 }}>
          <Card pad={22} style={{ textAlign: "center", background: "linear-gradient(160deg, var(--surface), var(--surface-2))" }}>
            <div style={{ fontSize: 13, color: "var(--ink-3)", fontWeight: 600 }}>Amount payable</div>
            <div className="mono" style={{ fontSize: 42, fontWeight: 700, letterSpacing: "-.04em", margin: "6px 0 2px" }}>{F.inr(t.total)}</div>
            <div style={{ fontSize: 12.5, color: "var(--ink-4)" }}>Incl. {F.inr(t.gst)} GST · {S.cartItems.length} items</div>
          </Card>
          <div>
            <SectionLabel>Payment method</SectionLabel>
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {methods.map(m => {
                const on = pay === m.id;
                return (
                  <Card key={m.id} pad={14} onClick={() => setPay(m.id)}
                    style={{ display: "flex", alignItems: "center", gap: 13, borderColor: on ? "var(--brand)" : "var(--line)",
                      boxShadow: on ? "0 0 0 3px var(--brand-tint)" : "var(--sh-sm)" }}>
                    <div style={{ width: 42, height: 42, borderRadius: 12, background: on ? "var(--brand)" : "var(--surface-2)",
                      color: on ? "#fff" : "var(--ink-2)", display: "grid", placeItems: "center" }}><Icon name={m.icon} size={20} stroke={2} /></div>
                    <div style={{ flex: 1 }}>
                      <div style={{ fontSize: 15, fontWeight: 700 }}>{m.label}</div>
                      <div style={{ fontSize: 12.5, color: "var(--ink-3)" }}>{m.sub}</div>
                    </div>
                    <div style={{ width: 22, height: 22, borderRadius: 99, border: on ? "7px solid var(--brand)" : "2px solid var(--line-2)", transition: "all .15s" }} />
                  </Card>
                );
              })}
            </div>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center", justifyContent: "center", color: "var(--ink-4)", fontSize: 12 }}>
            <Icon name="bolt" size={14} /> 256-bit encrypted · Powered by Flashmart Pay
          </div>
        </div>
      </PhoneShell>
      <PhoneFooter>
        <Button variant="dark" size="lg" full disabled={paying} onClick={onPay}
          icon={paying ? undefined : "bolt"}>
          {paying ? <span style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <span style={{ width: 18, height: 18, border: "2.5px solid rgba(255,255,255,.3)", borderTopColor: "#fff", borderRadius: 99, animation: "fmSpin .7s linear infinite" }} />
            Processing…</span> : "Pay " + F.inr(t.total)}
        </Button>
      </PhoneFooter>
    </>
  );
}

/* ---------------- SUCCESS ---------------- */
function SuccessScreen({ S, id, onTrack, onInvoice, onDone }) {
  const F = S.F; const o = S.orders.find(x => x.id === id);
  const total = o ? S.orderTotal(o) : 0;
  return (
    <PhoneShell bg="var(--surface)">
      <div style={{ minHeight: "100%", display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", padding: "20px 26px 40px", textAlign: "center" }}>
        <SuccessCheck size={104} />
        <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: "-.025em", marginTop: 18 }}>Payment successful</div>
        <div style={{ fontSize: 14.5, color: "var(--ink-3)", marginTop: 6, maxWidth: 240 }}>Your order has been placed and sent to your dealer for confirmation.</div>
        <div style={{ marginTop: 22, width: "100%", borderRadius: 18, border: "1px solid var(--line)", background: "var(--surface-2)", padding: 16 }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <span style={{ fontSize: 13, color: "var(--ink-3)", fontWeight: 600 }}>Order ID</span>
            <span className="mono" style={{ fontWeight: 700, whiteSpace: "nowrap" }}>{id}</span>
          </div>
          <div style={{ borderTop: "1px dashed var(--line-2)", margin: "12px 0" }} />
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <span style={{ fontSize: 13, color: "var(--ink-3)", fontWeight: 600 }}>Amount paid</span>
            <span className="mono" style={{ fontSize: 18, fontWeight: 700, color: "var(--pos)" }}>{F.inr(total)}</span>
          </div>
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10, width: "100%", marginTop: 22 }}>
          <Button variant="primary" size="lg" full icon="truck" onClick={onTrack}>Track delivery</Button>
          <div style={{ display: "flex", gap: 10 }}>
            <Button variant="outline" full icon="download" onClick={onInvoice}>Invoice</Button>
            <Button variant="ghost" full onClick={onDone}>Done</Button>
          </div>
        </div>
      </div>
    </PhoneShell>
  );
}

/* ---------------- ORDER DETAIL ---------------- */
function OrderDetail({ S, id, back, go, onInvoice }) {
  const F = S.F; const o = S.orders.find(x => x.id === id);
  if (!o) return null;
  const t = F.orderMath(o.items);
  const dealer = F.lookups.dealer(o.dealer);
  return (
    <>
      <PhoneShell header={<AppHeader pad title={o.id} subtitle={o.date} onBack={back}
        right={<Badge status={o.status === "placed" ? "pending" : o.status} />} />}>
        <div style={{ padding: "4px 16px 110px", display: "flex", flexDirection: "column", gap: 14 }}>
          <Card onClick={() => go("track", o.id)} style={{ display: "flex", alignItems: "center", gap: 13 }}>
            <div style={{ width: 44, height: 44, borderRadius: 13, background: "var(--brand-tint)", color: "var(--brand)", display: "grid", placeItems: "center" }}><Icon name="truck" size={22} stroke={2} /></div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 14.5, fontWeight: 700 }}>{statusLine(o.status)}</div>
              <div style={{ fontSize: 12.5, color: "var(--ink-3)" }}>{dealer.name} · {dealer.area}</div>
            </div>
            <Icon name="chevR" size={18} color="var(--ink-4)" />
          </Card>
          <div>
            <SectionLabel>Items · {o.items.length}</SectionLabel>
            <Card pad={6}>
              {o.items.map((it, i) => (
                <div key={it.pid} style={{ display: "flex", gap: 12, alignItems: "center", padding: "10px 8px", borderBottom: i === o.items.length - 1 ? "none" : "1px solid var(--line)" }}>
                  <ProductThumb p={it} size={46} />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 14, fontWeight: 600 }}>{it.name}</div>
                    <div style={{ fontSize: 12, color: "var(--ink-4)" }} className="mono">{it.qty} × {F.inr(it.price)}</div>
                  </div>
                  <span className="mono" style={{ fontSize: 14, fontWeight: 700 }}>{F.inr(F.lineMath(it).total)}</span>
                </div>
              ))}
            </Card>
          </div>
          <Card>
            <MoneyRow label="Subtotal" value={F.inr(t.sub)} />
            <MoneyRow label="Discount" value={"− " + F.inr(t.disc)} accent="var(--pos)" />
            <MoneyRow label="GST" value={"+ " + F.inr(t.gst)} />
            <div style={{ borderTop: "1px dashed var(--line-2)", margin: "4px 0" }} />
            <MoneyRow strong label="Total paid" value={F.inr(t.total)} accent="var(--pos)" />
          </Card>
        </div>
      </PhoneShell>
      <PhoneFooter>
        <div style={{ display: "flex", gap: 10 }}>
          <Button variant="outline" full icon="download" onClick={onInvoice}>Invoice</Button>
          <Button variant="primary" full icon="truck" onClick={() => go("track", o.id)}>Track</Button>
        </div>
      </PhoneFooter>
      <Toast />
    </>
  );
}
function statusLine(s) { return { placed: "Awaiting dealer confirmation", accepted: "Dealer is preparing your order", out: "Out for delivery", delivered: "Delivered to your store" }[s]; }

/* ---------------- TRACK ---------------- */
function TrackScreen({ S, id, back, scrollRef }) {
  const o = S.orders.find(x => x.id === id);
  if (!o) return null;
  const steps = timelineFor(o);
  const dealer = S.F.lookups.dealer(o.dealer);
  return (
    <PhoneShell scrollRef={scrollRef} header={<AppHeader pad title="Track order" subtitle={o.id} onBack={back} />}>
      <div style={{ padding: "4px 16px 30px", display: "flex", flexDirection: "column", gap: 14 }}>
        <Card pad={18} style={{ background: o.status === "delivered" ? "var(--pos-tint)" : "linear-gradient(150deg, var(--brand), var(--brand-700))",
          color: o.status === "delivered" ? "var(--pos)" : "#fff", border: "none" }}>
          <div style={{ fontSize: 12.5, fontWeight: 600, opacity: o.status === "delivered" ? 1 : .82 }}>{o.status === "delivered" ? "Completed" : "Estimated delivery"}</div>
          <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: "-.02em", marginTop: 4 }}>{o.status === "delivered" ? "Order delivered" : "Today, by 6:00 PM"}</div>
          <div style={{ fontSize: 13, opacity: o.status === "delivered" ? .9 : .82, marginTop: 4 }}>{statusLine(o.status)}</div>
        </Card>
        <Card>
          <SectionLabel style={{ paddingBottom: 14 }}>Order journey</SectionLabel>
          <OrderTimeline steps={steps} />
        </Card>
        <Card style={{ display: "flex", alignItems: "center", gap: 13 }}>
          <Avatar name={dealer.owner} tint="#0e9e6e" />
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 14.5, fontWeight: 700 }}>{dealer.name}</div>
            <div style={{ fontSize: 12.5, color: "var(--ink-3)" }}>Your dealer · {dealer.area}</div>
          </div>
          <button className="tap" style={{ width: 42, height: 42, borderRadius: 12, border: "1px solid var(--line)", background: "var(--surface)", color: "var(--pos)", display: "grid", placeItems: "center", cursor: "pointer", boxShadow: "var(--sh-sm)" }}><Icon name="phone" size={19} stroke={2} /></button>
        </Card>
      </div>
    </PhoneShell>
  );
}

window.Object && Object.assign(window, { CartScreen, PaymentScreen, SuccessScreen, OrderDetail, TrackScreen, timelineFor, PhoneFooter, statusLine });
