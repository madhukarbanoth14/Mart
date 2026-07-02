/* ============================================================
   FlashMart Android — Shopkeeper app (Material 3)
   Home · Catalog · Product · Cart
   ============================================================ */
const SF = window.FM;
const skNavItems = (active) => [
  { id: "home", icon: "home", label: "Home" },
  { id: "products", icon: "grid", label: "Products" },
  { id: "orders", icon: "bag", label: "Orders", badge: 2 },
  { id: "profile", icon: "user", label: "Profile" },
].map(x => ({ ...x }));

/* ---------------- HOME ---------------- */
function AND_SK_Home() {
  const me = SF.lookups.shop("SHP-118");
  const recent = SF.SEED_ORDERS.filter(o => o.shop === "SHP-118").slice(0, 3);
  return (
    <M3Screen nav={<M3NavBar items={skNavItems("home")} active="home" />} fab={<M3FAB icon="plus" label="New order" color="primary" />}>
      {/* greeting */}
      <div style={{ padding: `${AND_TOP + 6}px 12px 12px 20px`, display: "flex", alignItems: "center", gap: 12 }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: 13.5, color: M.onSurfVar, fontWeight: 600 }}>Good morning, Madhukar</div>
          <div style={{ fontSize: 22, fontWeight: 800, letterSpacing: "-.025em", color: M.onSurf, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{me.store}</div>
        </div>
        <M3IconBtn icon="bell" filled badge={1} />
        <Avatar name={me.owner} size={42} />
      </div>

      <div style={{ padding: "0 16px", display: "flex", flexDirection: "column", gap: 16 }}>
        {/* outstanding / wallet hero */}
        <div style={{ borderRadius: 24, padding: 20, color: "#fff", position: "relative", overflow: "hidden",
          background: "linear-gradient(150deg, var(--brand), var(--brand-700))", boxShadow: "var(--m3-e2)" }}>
          <div style={{ position: "absolute", right: -30, top: -30, width: 150, height: 150, borderRadius: "50%", background: "rgba(255,255,255,.08)" }} />
          <div style={{ position: "relative" }}>
            <div style={{ fontSize: 13, fontWeight: 600, opacity: .85 }}>Outstanding balance</div>
            <div className="mono" style={{ fontSize: 34, fontWeight: 700, letterSpacing: "-.03em", margin: "5px 0 14px" }}>{SF.inr(8240)}</div>
            <div style={{ display: "flex", gap: 10 }}>
              <M3Button variant="gold" size="sm" icon="wallet">Pay now</M3Button>
              <M3Button variant="text" size="sm" style={{ color: "#fff" }}>View ledger</M3Button>
            </div>
          </div>
        </div>

        {/* stat grid */}
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 11 }}>
          {[["clock", "Pending", "2", "var(--gold-tint)", "var(--gold-ink)"],
            ["check", "Delivered", "21", "var(--brand-tint)", "var(--brand)"],
            ["receipt", "Invoices", "24", "var(--blue-tint)", "var(--blue)"],
            ["wallet", "This month", SF.inrShort(42600), M.surf3, M.onSurf]].map(([ic, l, v, bg, fg]) => (
            <M3Card key={l} variant="filled" pad={15}>
              <div style={{ width: 36, height: 36, borderRadius: 10, background: bg, color: fg, display: "grid", placeItems: "center", marginBottom: 10 }}><Icon name={ic} size={19} /></div>
              <div className="mono" style={{ fontSize: 22, fontWeight: 700, letterSpacing: "-.02em", color: M.onSurf }}>{v}</div>
              <div style={{ fontSize: 12.5, color: M.onSurfVar, fontWeight: 600, marginTop: 1 }}>{l}</div>
            </M3Card>
          ))}
        </div>

        {/* quick actions */}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(4,1fr)", gap: 8 }}>
          {[["grid", "Browse"], ["bag", "My orders"], ["receipt", "Invoices"], ["phone", "Support"]].map(([ic, l]) => (
            <div key={l} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 8 }}>
              <div style={{ width: 56, height: 56, borderRadius: 18, background: M.secCont, color: M.onSecCont, display: "grid", placeItems: "center" }}><Icon name={ic} size={23} /></div>
              <span style={{ fontSize: 11.5, fontWeight: 600, color: M.onSurfVar }}>{l}</span>
            </div>
          ))}
        </div>

        {/* promo banner */}
        <div style={{ borderRadius: 20, padding: 16, background: "var(--gold-tint)", display: "flex", alignItems: "center", gap: 14, overflow: "hidden", position: "relative" }}>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 11, fontWeight: 800, color: "var(--gold-ink)", letterSpacing: ".05em" }}>THIS WEEK</div>
            <div style={{ fontSize: 16, fontWeight: 800, color: M.onSurf, marginTop: 3, letterSpacing: "-.02em" }}>Extra 8% off staples</div>
            <div style={{ fontSize: 12.5, color: "var(--gold-ink)", marginTop: 2 }}>On orders above ₹5,000</div>
          </div>
          <div style={{ display: "flex", gap: 8 }}>
            <div style={{ transform: "rotate(-6deg)" }}><ProductThumb p={SF.PRODUCTS[2]} size={54} /></div>
            <div style={{ transform: "rotate(5deg)", marginTop: 8 }}><ProductThumb p={SF.PRODUCTS[3]} size={54} /></div>
          </div>
        </div>

        {/* recent orders */}
        <div>
          <M3SectionLabel action="See all">Recent orders</M3SectionLabel>
          <M3Card variant="filled" pad={6}>
            {recent.map((o, i) => (
              <M3ListItem key={o.id} last={i === recent.length - 1}
                leading={<div style={{ width: 40, height: 40, borderRadius: 12, background: M.surf4, display: "grid", placeItems: "center", color: M.onSurfVar }}><Icon name="bag" size={19} /></div>}
                headline={<span className="mono">{o.id}</span>} supporting={o.date + " · " + o.items.length + " items"}
                trailing={<div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: 5 }}><span className="mono" style={{ fontSize: 14, fontWeight: 700, color: M.onSurf }}>{SF.inr(SF.orderMath(o.items).total)}</span><M3Status status={o.status === "placed" ? "pending" : o.status} size="sm" /></div>} />
            ))}
          </M3Card>
        </div>
      </div>
    </M3Screen>
  );
}

/* ---------------- CATALOG ---------------- */
function AND_SK_Catalog() {
  const cats = ["All", "Staples", "Dairy", "Snacks", "Beverages", "Home Care"];
  const products = SF.PRODUCTS.slice(0, 6);
  return (
    <M3Screen nav={<M3NavBar items={skNavItems("products")} active="products" />} fab={<M3FAB icon="cart" color="primary" />}>
      <div style={{ paddingTop: AND_TOP }}>
        <div style={{ padding: "8px 16px 0", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div style={{ fontSize: 26, fontWeight: 800, letterSpacing: "-.03em", color: M.onSurf }}>Products</div>
          <M3IconBtn icon="filter" filled />
        </div>
        {/* search bar */}
        <div style={{ padding: "12px 16px 4px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, height: 52, padding: "0 16px", background: M.surf3, borderRadius: 999 }}>
            <Icon name="search" size={20} color={M.onSurfVar} />
            <span style={{ flex: 1, color: M.onSurfVar, fontSize: 15 }}>Search products or brands</span>
            <Icon name="grid" size={19} color={M.onSurfVar} />
          </div>
        </div>
        {/* filter chips */}
        <div className="fm-scroll" style={{ display: "flex", gap: 8, padding: "12px 16px", overflowX: "auto" }}>
          {cats.map((c, i) => <M3Chip key={c} selected={i === 0} leadingCheck>{c}</M3Chip>)}
        </div>
      </div>
      <div style={{ padding: "0 16px 8px", display: "flex", flexDirection: "column", gap: 10 }}>
        {products.map((p, idx) => (
          <M3Card key={p.id} variant="outlined" pad={12} style={{ display: "flex", gap: 13, alignItems: "center" }}>
            <ProductThumb p={p} size={64} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 10.5, fontWeight: 800, color: p.tint, textTransform: "uppercase", letterSpacing: ".03em" }}>{p.brand}</div>
              <div style={{ fontSize: 14.5, fontWeight: 700, lineHeight: 1.2, margin: "2px 0 5px", color: M.onSurf }}>{p.name}</div>
              <div style={{ display: "flex", alignItems: "center", gap: 7 }}>
                <span className="mono" style={{ fontSize: 15, fontWeight: 700, color: M.onSurf }}>{SF.inr(p.price)}</span>
                <span style={{ fontSize: 10.5, fontWeight: 700, color: "var(--gold-ink)", background: "var(--gold-tint)", padding: "2px 7px", borderRadius: 6 }}>{p.disc}% off</span>
              </div>
            </div>
            {idx === 0
              ? <M3Stepper value={2} onChange={() => {}} />
              : <M3IconBtn icon="plus" filled color={M.onPrimCont} size={22} />}
          </M3Card>
        ))}
      </div>
    </M3Screen>
  );
}

/* ---------------- PRODUCT DETAIL ---------------- */
function AND_SK_Product() {
  const p = SF.PRODUCTS[4]; // Parle-G
  const m = SF.lineMath({ ...p, qty: 1 });
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: M.surface, position: "relative" }}>
      <M3TopBar onBack title="" actions={<><M3IconBtn icon="star" /><M3IconBtn icon="cart" badge={3} /></>} />
      <div className="fm-scroll" style={{ flex: 1, overflow: "auto", paddingBottom: 100 }}>
        {/* gallery */}
        <div style={{ margin: "0 16px", height: 240, borderRadius: 24, background: `linear-gradient(140deg, ${p.tint}1f, ${p.tint}0d)`, border: `1px solid ${p.tint}26`, position: "relative", overflow: "hidden", display: "grid", placeItems: "center" }}>
          <div style={{ position: "absolute", inset: 0, opacity: .5, backgroundImage: `repeating-linear-gradient(135deg, ${p.tint}12 0 8px, transparent 8px 16px)` }} />
          <ProductThumb p={p} size={140} radius={28} />
          <div style={{ position: "absolute", bottom: 12, left: 0, right: 0, display: "flex", justifyContent: "center", gap: 6 }}>
            {[0, 1, 2].map(i => <span key={i} style={{ width: i === 0 ? 20 : 7, height: 7, borderRadius: 99, background: i === 0 ? p.tint : `${p.tint}40` }} />)}
          </div>
        </div>
        <div style={{ padding: "18px 20px 0" }}>
          <div style={{ fontSize: 11.5, fontWeight: 800, color: p.tint, textTransform: "uppercase", letterSpacing: ".04em" }}>{p.brand}</div>
          <div style={{ fontSize: 22, fontWeight: 800, letterSpacing: "-.025em", marginTop: 4, color: M.onSurf }}>{p.name}</div>
          <div style={{ fontSize: 13.5, color: M.onSurfVar, marginTop: 4 }}>{p.unit} · {p.cat}</div>
          <div style={{ display: "flex", alignItems: "baseline", gap: 10, marginTop: 14 }}>
            <span className="mono" style={{ fontSize: 28, fontWeight: 700, letterSpacing: "-.03em", color: M.onSurf }}>{SF.inr(p.price)}</span>
            <span style={{ fontSize: 11, fontWeight: 700, color: "var(--gold-ink)", background: "var(--gold-tint)", padding: "3px 8px", borderRadius: 7 }}>{p.disc}% off</span>
            <span style={{ fontSize: 12.5, color: M.onSurfVar }}>incl. {p.gst}% GST</span>
          </div>
          {/* pricing breakdown */}
          <M3Card variant="filled" pad={14} style={{ marginTop: 16 }}>
            <MoneyRow label="Base price" value={SF.inr(p.price)} />
            <MoneyRow label={`Shopkeeper discount (${p.disc}%)`} value={"− " + SF.inr(m.discAmt)} accent="var(--pos)" />
            <MoneyRow label={`GST (${p.gst}%)`} value={"+ " + SF.inr(m.gstAmt)} />
            <div style={{ borderTop: "1px dashed var(--m3-outline-var)", margin: "6px 0" }} />
            <MoneyRow strong label="Your price / unit" value={SF.inr(m.total)} />
          </M3Card>
          <M3SectionLabel style={{ marginTop: 18 }}>Product details</M3SectionLabel>
          <div style={{ fontSize: 14, color: M.onSurfVar, lineHeight: 1.6 }}>
            India's favourite glucose biscuit. Case pack of 12 units, MRP printed. GST-compliant tax invoice generated on every order.
          </div>
        </div>
      </div>
      {/* sticky add bar */}
      <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, padding: "14px 16px", paddingBottom: 14 + AND_BOTTOM,
        background: M.surf2, boxShadow: "0 -1px 0 var(--m3-outline-var)", display: "flex", alignItems: "center", gap: 14 }}>
        <M3Stepper value={3} onChange={() => {}} />
        <M3Button variant="filled" size="lg" full icon="cart" style={{ flex: 1 }}>Add to cart · {SF.inr(p.price * 3)}</M3Button>
      </div>
    </div>
  );
}

/* ---------------- CART ---------------- */
function AND_SK_Cart() {
  const items = [["atta", 6], ["oil", 12], ["salt", 10]].map(([id, q]) => ({ ...SF.lookups.product(id), qty: q }));
  const t = SF.orderMath(items.map(it => ({ ...it })));
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: M.surface, position: "relative" }}>
      <M3TopBar onBack title="Cart" subtitle="3 items" />
      <div className="fm-scroll" style={{ flex: 1, overflow: "auto", padding: "4px 16px 120px", display: "flex", flexDirection: "column", gap: 12 }}>
        <M3Card variant="filled" pad={6}>
          {items.map((it, i) => (
            <div key={it.id} style={{ display: "flex", gap: 12, alignItems: "center", padding: "12px 8px", borderBottom: i === items.length - 1 ? "none" : "1px solid var(--m3-outline-var)" }}>
              <ProductThumb p={it} size={54} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 14, fontWeight: 700, lineHeight: 1.2, color: M.onSurf }}>{it.name}</div>
                <div className="mono" style={{ fontSize: 12, color: M.onSurfVar, margin: "3px 0 8px" }}>{SF.inr(it.price)} · {it.disc}% off</div>
                <M3Stepper value={it.qty} onChange={() => {}} />
              </div>
              <span className="mono" style={{ fontSize: 14.5, fontWeight: 700, alignSelf: "flex-start", marginTop: 4, color: M.onSurf }}>{SF.inr(SF.lineMath(it).total)}</span>
            </div>
          ))}
        </M3Card>
        <M3Card variant="outlined">
          <MoneyRow label="Subtotal" value={SF.inr(t.sub)} />
          <MoneyRow label="Shopkeeper discount" value={"− " + SF.inr(t.disc)} accent="var(--pos)" />
          <MoneyRow label="GST" value={"+ " + SF.inr(t.gst)} />
          <div style={{ borderTop: "1px dashed var(--m3-outline-var)", margin: "6px 0" }} />
          <MoneyRow strong label="Total payable" value={SF.inr(t.total)} />
        </M3Card>
        <div style={{ display: "flex", alignItems: "center", gap: 10, fontSize: 12.5, color: M.onSurfVar, padding: "0 4px" }}>
          <Icon name="bolt" size={15} color="var(--brand)" /> Delivered by Shree Balaji Distributors · usually within a day
        </div>
      </div>
      <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, padding: "14px 16px", paddingBottom: 14 + AND_BOTTOM,
        background: M.surf2, boxShadow: "0 -1px 0 var(--m3-outline-var)" }}>
        <M3Button variant="filled" size="lg" full iconRight="arrowR">Checkout · {SF.inr(t.total)}</M3Button>
      </div>
    </div>
  );
}

Object.assign(window, { skNavItems, AND_SK_Home, AND_SK_Catalog, AND_SK_Product, AND_SK_Cart });
